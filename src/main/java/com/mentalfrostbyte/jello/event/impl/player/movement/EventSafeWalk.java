package com.mentalfrostbyte.jello.event.impl.player.movement;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventSafeWalk extends CancellableEvent {
    public boolean onEdge;
    public Situation situation;

    public EventSafeWalk(boolean onEdge) {
        this.onEdge = onEdge;
        this.situation = Situation.DEFAULT;
    }

    public Situation getSituation() {
        return this.situation;
    }

    public void setSafe(boolean safe) {
        this.situation = safe ? Situation.SAFE : Situation.PLAYER;
    }

    public boolean isOnEdge() {
        return this.onEdge;
    }

    public enum Situation {
        DEFAULT, // IDK, it's only used in constructor
        PLAYER,
        SAFE
    }
}