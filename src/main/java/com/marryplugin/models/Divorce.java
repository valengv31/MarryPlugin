package com.marryplugin.models;

public class Divorce {
    private final Marriage marriage;
    private final long createdAt;

    public Divorce(Marriage marriage) {
        this.marriage = marriage;
        this.createdAt = System.currentTimeMillis();
    }

    public Marriage getMarriage() {
        return marriage;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
