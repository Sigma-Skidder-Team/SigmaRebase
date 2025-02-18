package com.mentalfrostbyte.jello.event.impl.player.movement;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventMoveRelative extends CancellableEvent {
    public static float yaw;

    public EventMoveRelative(float yaw){
        this.yaw = yaw;
    }


    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

}