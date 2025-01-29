package com.mentalfrostbyte.jello.event.impl.player.movement;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.util.math.Vec3d;

public class EventMove extends CancellableEvent {
    public Vec3d vector;

    public EventMove(Vec3d Vec3d) {
        this.vector = Vec3d;
    }

    public double getX() {
        return this.vector.x;
    }

    public void setX(double x) {
        this.vector.x = x;
    }

    public double getY() {
        return this.vector.y;
    }

    public void setY(double y) {
        this.vector.y = y;
    }

    public double getZ() {
        return this.vector.z;
    }

    public void setZ(double z) {
        this.vector.z = z;
    }

    public Vec3d getVector() {
        return this.vector;
    }

    public void setVector(Vec3d Vec3d) {
        this.vector = Vec3d;
    }
}
