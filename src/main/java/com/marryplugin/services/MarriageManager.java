package com.marryplugin.services;

import com.marryplugin.Config;
import com.marryplugin.MarryPlugin;
import com.marryplugin.databases.IDatabase;
import com.marryplugin.databases.SqliteDatabase;
import com.marryplugin.models.Divorce;
import com.marryplugin.models.Marriage;
import com.marryplugin.models.Proposal;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

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
    private Logger logger;
    private IDatabase database;
    private Map<UUID, Marriage> marriagesCache;

    private final Map<UUID, Proposal> pendingProposals = new HashMap<>();
    private final Map<UUID, Divorce> pendingDivorce = new HashMap<>();

    private BukkitTask cleanerTask;

    public MarriageManager(MarryPlugin plugin, Config cfg) {
        this.logger = plugin.getLogger();
        this.database = new SqliteDatabase(logger, plugin.getDataFolder());
        marriagesCache = database.loadFromDatabase();
        startCleanerTask(plugin,cfg);
    }

    private void startCleanerTask(JavaPlugin plugin, Config cfg){
        long expireSeconds = cfg.getLong("proposal-expire-seconds");
        cleanerTask= plugin.getServer().getScheduler().runTaskTimer(plugin,
                () -> {
                    cleanExpiredProposals(expireSeconds * 1000L);
                    cleanExpiredDivorces( expireSeconds * 1000L);
                },
                20L * 10, 20L * 10);
    }

    public boolean isMarried(UUID playerId) {
        return marriagesCache.containsKey(playerId);
    }

    public Marriage getMarriage(UUID playerId) {
        return marriagesCache.get(playerId);
    }

    public boolean marry(OfflinePlayer p1, OfflinePlayer p2) {
        Marriage marriage = new Marriage(p1,p2);

        if (!this.database.save(marriage)) return false;

        marriagesCache.put(p1.getUniqueId(), marriage);
        marriagesCache.put(p2.getUniqueId(), marriage);

        pendingProposals.remove(p1.getUniqueId());
        pendingProposals.remove(p2.getUniqueId());
        return true;
    }

    public boolean divorce(UUID playerId) {
        Marriage marriage = marriagesCache.get(playerId);
        if (marriage == null) return false;
        if (!this.database.delete(marriage)) return false;

        marriagesCache.remove(marriage.getPlayer1Id());
        marriagesCache.remove(marriage.getPlayer2Id());

        pendingDivorce.remove(marriage.getPlayer1Id());
        pendingDivorce.remove(marriage.getPlayer2Id());
        return true;
    }

    public List<Marriage> getAllMarriages() {
        return new ArrayList<>(new HashSet<>(marriagesCache.values()));
    }

    public void addProposal(Proposal proposal) {
        pendingProposals.put(proposal.getTargetId(), proposal);
        pendingProposals.put(proposal.getProposerId(), proposal);
    }

    public void addDivorce(Divorce divorce) {
        pendingDivorce.put(divorce.getMarriage().getPlayer1Id(), divorce);
        pendingDivorce.put(divorce.getMarriage().getPlayer2Id(), divorce);
    }

    public Proposal getProposal(UUID targetId) {
        return pendingProposals.get(targetId);
    }

    public void removeProposal(UUID targetId) {
        Proposal proposal =pendingProposals.remove(targetId);
        pendingProposals.remove(proposal.getProposerId());
    }

    public void cancelProposals(UUID proposerId) {
        pendingProposals.values().removeIf(p -> p.getProposerId().equals(proposerId));
    }

    public boolean hasPendingProposal(UUID proposerId) {
        return pendingProposals.containsKey(proposerId);
    }

    public boolean hasPendingDivorce(UUID playerId) {
        return pendingDivorce.containsKey(playerId);
    }

    private void cleanExpiredProposals(long expireMillis) {
        long now = System.currentTimeMillis();
        boolean clean = pendingProposals.values().removeIf(p -> now - p.getCreatedAt() > expireMillis);

        if (clean) {
            //al expirar informar a los usuarios que expiro
        }
    }

    private void cleanExpiredDivorces(long expireMillis) {
        long now = System.currentTimeMillis();
        boolean clean = pendingDivorce.entrySet().removeIf(entry -> now - entry.getValue().getCreatedAt() > expireMillis);

        if (clean) {
            //al expirar informar a los usuarios que expiro
        }
    }

    public List<String> getHistory(UUID playerId,int limit) {
        return this.database.getHistory( playerId, limit);
    }

    public void reload(){
        this.marriagesCache= this.database.loadFromDatabase();
    }
    public void stop() {
        if (cleanerTask != null){
            cleanerTask.cancel();
        }
        database.close();
    }
}
