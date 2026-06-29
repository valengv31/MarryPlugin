package com.marryplugin.databases;

import com.marryplugin.models.Marriage;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqliteDatabase implements IDatabase{
    private Connection connection;
    private Logger logger;
    private File getDataFolder;

    public SqliteDatabase(Logger logger, File dbFile) {
        this.logger = logger;
        this.getDataFolder = dbFile;
        connect();
        createTables();
    }


    @Override
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            getDataFolder.mkdirs();
            File dbFile = new File(getDataFolder, "marriages.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se pudo conectar a la base de datos SQLite", e);
        }
    }

    @Override
    public void createTables() {
        String marriagesTable =
                "CREATE TABLE IF NOT EXISTS marriages (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  player1_id TEXT NOT NULL," +
                        "  player1_name TEXT NOT NULL," +
                        "  player2_id TEXT NOT NULL," +
                        "  player2_name TEXT NOT NULL," +
                        "  married_at INTEGER NOT NULL" +
                        ")";

        String historyTable =
                "CREATE TABLE IF NOT EXISTS marriage_history (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  event_type TEXT NOT NULL," +
                        "  player1_id TEXT NOT NULL," +
                        "  player1_name TEXT NOT NULL," +
                        "  player2_id TEXT NOT NULL," +
                        "  player2_name TEXT NOT NULL," +
                        "  event_at INTEGER NOT NULL" +
                        ")";

        try (Statement st = connection.createStatement()) {
            st.execute(marriagesTable);
            st.execute(historyTable);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "No se pudieron crear las tablas en la base de datos", e);
        }
    }

    @Override
    public boolean save(Marriage marriage) {
        String insertSql = "INSERT INTO marriages (player1_id, player1_name, player2_id, player2_name, married_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, marriage.getPlayer1Id().toString());
            ps.setString(2, marriage.getPlayer1Name());
            ps.setString(3, marriage.getPlayer2Id().toString());
            ps.setString(4, marriage.getPlayer2Name());
            ps.setLong(5, marriage.getMarriedAt());
            ps.executeUpdate();

            logHistory("MARRY", marriage);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "No se pudo guardar el matrimonio en la base de datos, no se casan", e);
            return false;
        }
    }

    @Override
    public boolean delete(Marriage marriage) {
        //trae a unos casados, vamos a borrar la asociacion de ellos en la base de datos
        String deleteSql = "DELETE FROM marriages WHERE player1_id = ? OR player2_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setString(1, marriage.getPlayer1Id().toString());
            ps.setString(2, marriage.getPlayer1Id().toString());
            ps.executeUpdate();

            logHistory("DIVORCE", marriage);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "No se pudo eliminar el matrimonio de la base de datos, no se divorcian", e);
            return false;
        }
    }

    @Override
    public List<String> getHistory(UUID playerId,int limit) {
        List<String> lines = new ArrayList<>();
        String sql = "SELECT event_type, player1_id, player1_name, player2_name, event_at " +
                "FROM marriage_history WHERE player1_id = ? OR player2_id = ? ORDER BY event_at DESC LIMIT ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, playerId.toString());
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("event_type");
                    UUID id1 = UUID.fromString(rs.getString("player1_id"));
                    String name1 = rs.getString("player1_name");
                    String name2 = rs.getString("player2_name");
                    long eventAt = rs.getLong("event_at");

                    String partnerName = id1.equals(playerId) ? name2 : name1;
                    lines.add(type + "|" + partnerName + "|" + eventAt);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "No se pudo leer el historial de matrimonios", e);
        }
        return lines;
    }

    private void logHistory(String eventType, Marriage marriage) {
        String insertSql = "INSERT INTO marriage_history (event_type, player1_id, player1_name, player2_id, player2_name, event_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, eventType);
            ps.setString(2, marriage.getPlayer1Id().toString());
            ps.setString(3, marriage.getPlayer1Name());
            ps.setString(4, marriage.getPlayer2Id().toString());
            ps.setString(5, marriage.getPlayer2Name());
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "No se pudo registrar el evento de historia en la base de datos", e);
        }

    }

    @Override
    public Map<UUID, Marriage> loadFromDatabase() {
        Map<UUID, Marriage> marriages = new java.util.HashMap<>();
        String sql = "SELECT player1_id, player1_name, player2_id, player2_name, married_at FROM marriages";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                UUID id1 = UUID.fromString(rs.getString("player1_id"));
                String name1 = rs.getString("player1_name");
                UUID id2 = UUID.fromString(rs.getString("player2_id"));
                String name2 = rs.getString("player2_name");
                long marriedAt = rs.getLong("married_at");

                Marriage marriage = new Marriage(id1, name1, id2, name2, marriedAt);
                marriages.put(id1, marriage);
                marriages.put(id2, marriage);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "No se pudieron cargar los matrimonios desde la base de datos", e);
        }
        return marriages;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error al cerrar la conexión SQLite", e);
            }
        }
    }
}
