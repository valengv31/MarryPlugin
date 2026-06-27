package com.marryplugin.models;

import java.util.UUID;

/**
 * Representa una propuesta de matrimonio pendiente de respuesta.
 */
public class Proposal {

    private final UUID proposerId;
    private final String proposerName;
    private final UUID targetId;
    private final String targetName;
    private final long createdAt;

    public Proposal(UUID proposerId, String proposerName, UUID targetId,String targetName) {
        this.proposerId = proposerId;
        this.proposerName = proposerName;
        this.targetId = targetId;
        this.targetName = proposerName;
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getProposerId() {
        return proposerId;
    }

    public String getProposerName() {
        return proposerName;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
