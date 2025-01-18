package com.mentalfrostbyte.jello.event.impl;


import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.util.math.vector.Vector3d;

public class EventStep extends CancellableEvent {
    private final double height;
    private final Vector3d vector;

    public EventStep(double height, Vector3d vector) {
        this.height = height;
        this.vector = vector;
    }

    public double getHeight() {
        return this.height;
    }

    public void setY(double var1) {
        this.cancelled = true;
        this.vector.y = var1;
    }

    public double getY() {
        return this.vector.y;
    }

    public Vector3d getVector() {
        return this.vector;
    }
}