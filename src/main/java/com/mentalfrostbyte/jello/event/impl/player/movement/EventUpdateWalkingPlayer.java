package com.mentalfrostbyte.jello.event.impl.player.movement;

import com.mentalfrostbyte.jello.event.CancellableEvent;

import java.util.ArrayList;
import java.util.List;

public class EventUpdateWalkingPlayer extends CancellableEvent {
    public static float prevPitch;
    public static float prevYaw;
    public static float postPitch;
    public static float postYaw;
    public boolean pre;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private boolean moving;
    private final List<Runnable> runnables = new ArrayList<>();

    public EventUpdateWalkingPlayer(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.pre = true;
        this.moving = false;
    }

    public void postUpdate() {
        postPitch = prevPitch;
        postYaw = prevYaw;
        prevPitch = this.yaw;
        prevYaw = this.pitch;
        this.pre = false;
    }

    public boolean isMoving() {
        return this.moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isPre() {
        return this.pre;
    }

    public void attackPost(Runnable runnable) {
        this.runnables.add(runnable);
    }

    public List<Runnable> getRunnableList() {
        return this.runnables;
    }
}
