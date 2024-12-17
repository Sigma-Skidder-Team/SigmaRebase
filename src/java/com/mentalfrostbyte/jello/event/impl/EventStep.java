package com.mentalfrostbyte.jello.event.impl;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.util.math.vector.Vector3d;


public class EventStep extends CancellableEvent {
    private final double height;
    private final Vector3d vector;

    public EventStep(double var1, Vector3d vector) {
        this.height = var1;
        this.vector = vector;
    }

    public double getHeight() {
        return this.height;
    }

    public void setY(double y) {
        this.cancelled = true;
        this.vector.y = y;
    }

    public double getY() {
        return this.vector.y;
    }

    public Vector3d getVector() {
        return this.vector;
    }
}