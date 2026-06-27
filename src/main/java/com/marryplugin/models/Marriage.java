package com.marryplugin.models;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Representa un matrimonio entre dos jugadores.
 * Es inmutable: para "cambiar" un matrimonio se elimina y se crea uno nuevo.
 */
public class Marriage {

    private final UUID player1Id;
    private final String player1Name;
    private final UUID player2Id;
    private final String player2Name;
    private final long marriedAt;


    public Marriage(UUID player1Id, String player1Name, UUID player2Id, String player2Name,  long marriedAt) {
        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.player2Id = player2Id;
        this.player2Name = player2Name;
        this.marriedAt = marriedAt;
    }

    public Marriage(UUID player1Id, String player1Name, UUID player2Id, String player2Name) {
        this(player1Id, player1Name, player2Id, player2Name, System.currentTimeMillis());
    }

    public Marriage(OfflinePlayer p1, OfflinePlayer p2){
        this(p1.getUniqueId(), p1.getName(), p2.getUniqueId(), p2.getName());
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public long getMarriedAt() {
        return marriedAt;
    }

    public boolean involves(UUID playerId) {
        return playerId.equals(player1Id) || playerId.equals(player2Id);
    }

    public UUID getPartner(UUID playerId) {
        if (playerId.equals(player1Id)) return player2Id;
        if (playerId.equals(player2Id)) return player1Id;
        return null;
    }

    public String getPartnerName(UUID playerId) {
        if (playerId.equals(player1Id)) return player2Name;
        if (playerId.equals(player2Id)) return player1Name;
        return null;
    }
}
