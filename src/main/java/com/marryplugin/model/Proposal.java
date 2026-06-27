package com.marryplugin.model;

import java.util.UUID;

/**
 * Representa una propuesta de matrimonio pendiente de respuesta.
 */
public class Proposal {

    private final UUID proposerId;
    private final String proposerName;
    private final UUID targetId;
    private final long createdAt;

    public Proposal(UUID proposerId, String proposerName, UUID targetId, long createdAt) {
        this.proposerId = proposerId;
        this.proposerName = proposerName;
        this.targetId = targetId;
        this.createdAt = createdAt;
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

    public long getCreatedAt() {
        return createdAt;
    }
}
