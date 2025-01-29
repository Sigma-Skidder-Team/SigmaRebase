package com.mentalfrostbyte.jello.module.impl.combat.killaura;


public class ExpirationTimer {
    private final long expirationTime;

    public ExpirationTimer(long duration) {
        this.expirationTime = System.currentTimeMillis() + duration;
    }

    public boolean hasExpired() {
        return this.expirationTime - System.currentTimeMillis() < 0L;
    }
}
