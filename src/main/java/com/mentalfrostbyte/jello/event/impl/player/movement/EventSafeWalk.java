package com.mentalfrostbyte.jello.event.impl.player.movement;


import com.mentalfrostbyte.jello.event.CancellableEvent;
import com.mentalfrostbyte.jello.misc.Situation;

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
}