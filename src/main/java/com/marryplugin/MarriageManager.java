package com.marryplugin;

import com.marryplugin.model.Marriage;
import com.marryplugin.model.Proposal;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Maneja el registro de matrimonios, persistido en una base de datos SQLite
 * (archivo plugins/MarryPlugin/marriages.db). Esto evita por completo el
 * problema de "se divorcian solos al reiniciar": cada casamiento y cada
 * divorcio se escribe en disco en el momento en que ocurre, así que al
 * volver a prender el servidor el estado se recarga tal cual quedó.
 *
 * Las propuestas pendientes y las confirmaciones de divorcio siguen viviendo
 * solo en memoria a propósito: no tiene sentido que sobrevivan a un reinicio.
 */
public class MarriageManager {

    private final MarryPlugin plugin;
    private Connection connection;

    // Cada matrimonio se indexa dos veces (una por cónyuge) apuntando al mismo objeto.
    private final Map<UUID, Marriage> marriages = new HashMap<>();

    // Solo puede haber una propuesta pendiente por destinatario a la vez.
    private final Map<UUID, Proposal> pendingProposals = new HashMap<>();

    // Marca de tiempo de cuándo un jugador pidió /divorce, para exigir /divorce confirm.
    private final Map<UUID, Long> pendingDivorceConfirm = new HashMap<>();

    public MarriageManager(MarryPlugin plugin) {
        this.plugin = plugin;
        connect();
        createTables();
        loadFromDatabase();
    }

    // ---------- Conexión y esquema ----------

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            plugin.getDataFolder().mkdirs();
            File dbFile = new File(plugin.getDataFolder(), "marriages.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo conectar a la base de datos SQLite", e);
        }
    }

    private void createTables() {
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
            plugin.getLogger().log(Level.SEVERE, "No se pudieron crear las tablas en la base de datos", e);
        }
    }

    /**
     * Vuelve a leer todos los matrimonios desde la base de datos, descartando
     * lo que hubiera en memoria. Pensado para poder resincronizar manualmente
     * con /marryadmin reload sin tener que reiniciar el servidor entero.
     */
    public void loadFromDatabase() {
        marriages.clear();
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
            plugin.getLogger().log(Level.SEVERE, "No se pudieron cargar los matrimonios desde la base de datos", e);
        }
    }

    /** Cierra la conexión a la base de datos. Llamar en onDisable(). */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error al cerrar la conexión SQLite", e);
            }
        }
    }

    // ---------- Matrimonios ----------

    public boolean isMarried(UUID playerId) {
        return marriages.containsKey(playerId);
    }

    public Marriage getMarriage(UUID playerId) {
        return marriages.get(playerId);
    }

    public boolean marry(Player p1, Player p2) {
        return marry(p1.getUniqueId(), p1.getName(), p2.getUniqueId(), p2.getName());
    }

    /**
     * Versión por UUID/nombre, usada también por el comando admin cuando alguno
     * de los jugadores puede estar desconectado.
     */
    public boolean marry(UUID id1, String name1, UUID id2, String name2) {
        long now = System.currentTimeMillis();
        Marriage marriage = new Marriage(id1, name1, id2, name2, now);

        String insertSql = "INSERT INTO marriages (player1_id, player1_name, player2_id, player2_name, married_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, id1.toString());
            ps.setString(2, name1);
            ps.setString(3, id2.toString());
            ps.setString(4, name2);
            ps.setLong(5, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar el matrimonio en la base de datos, no se casan", e);
            return false;
        }

        // Solo reflejamos el cambio en memoria si el INSERT en la base de datos funcionó.
        // Así nunca queda alguien "casado" en RAM sin que exista en la base.
        marriages.put(id1, marriage);
        marriages.put(id2, marriage);

        logHistory("MARRY", id1, name1, id2, name2, now);
        return true;
    }

    /**
     * Disuelve el matrimonio del jugador indicado (si tiene uno).
     * @return true si efectivamente había un matrimonio que disolver.
     */
    public boolean divorce(UUID playerId) {
        Marriage marriage = marriages.get(playerId);
        if (marriage == null) return false;

        String deleteSql = "DELETE FROM marriages WHERE player1_id = ? OR player2_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, playerId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo eliminar el matrimonio de la base de datos, no se divorcian", e);
            return false;
        }

        // Solo tocamos la memoria si el DELETE en la base de datos funcionó.
        marriages.remove(marriage.getPlayer1Id());
        marriages.remove(marriage.getPlayer2Id());

        logHistory("DIVORCE", marriage.getPlayer1Id(), marriage.getPlayer1Name(),
                marriage.getPlayer2Id(), marriage.getPlayer2Name(), System.currentTimeMillis());

        return true;
    }

    public List<Marriage> getAllMarriages() {
        return new ArrayList<>(new HashSet<>(marriages.values()));
    }

    // ---------- Historial (registro permanente, incluso de divorciados) ----------

    private void logHistory(String eventType, UUID id1, String name1, UUID id2, String name2, long timestamp) {
        String sql = "INSERT INTO marriage_history (event_type, player1_id, player1_name, player2_id, player2_name, event_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, id1.toString());
            ps.setString(3, name1);
            ps.setString(4, id2.toString());
            ps.setString(5, name2);
            ps.setLong(6, timestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar el historial de matrimonios", e);
        }
    }

    /**
     * Devuelve el historial (casamientos y divorcios) de un jugador, del más
     * reciente al más antiguo. Cada línea tiene el formato "TIPO|pareja|timestamp".
     */
    public List<String> getHistory(UUID playerId, int limit) {
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
            plugin.getLogger().log(Level.SEVERE, "No se pudo leer el historial de matrimonios", e);
        }
        return lines;
    }

    // ---------- Propuestas (en memoria, no se persisten a propósito) ----------

    public void addProposal(Proposal proposal) {
        pendingProposals.put(proposal.getTargetId(), proposal);
    }

    public Proposal getProposal(UUID targetId) {
        return pendingProposals.get(targetId);
    }

    public void removeProposal(UUID targetId) {
        pendingProposals.remove(targetId);
    }

    public boolean hasPendingProposalFrom(UUID proposerId) {
        for (Proposal p : pendingProposals.values()) {
            if (p.getProposerId().equals(proposerId)) return true;
        }
        return false;
    }

    public void cancelProposalsFrom(UUID proposerId) {
        pendingProposals.values().removeIf(p -> p.getProposerId().equals(proposerId));
    }

    public void cleanExpiredProposals(long expireMillis) {
        long now = System.currentTimeMillis();
        pendingProposals.values().removeIf(p -> now - p.getCreatedAt() > expireMillis);
    }

    // ---------- Confirmación de divorcio (en memoria) ----------

    public void requestDivorceConfirm(UUID playerId) {
        pendingDivorceConfirm.put(playerId, System.currentTimeMillis());
    }

    public boolean hasDivorceConfirmPending(UUID playerId, long expireMillis) {
        Long ts = pendingDivorceConfirm.get(playerId);
        if (ts == null) return false;
        if (System.currentTimeMillis() - ts > expireMillis) {
            pendingDivorceConfirm.remove(playerId);
            return false;
        }
        return true;
    }

    public void clearDivorceConfirm(UUID playerId) {
        pendingDivorceConfirm.remove(playerId);
    }
}
