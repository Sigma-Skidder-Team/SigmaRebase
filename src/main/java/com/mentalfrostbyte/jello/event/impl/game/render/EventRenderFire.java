package com.mentalfrostbyte.jello.event.impl.game.render;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventRenderFire extends CancellableEvent {
    private float fireHeight;

    public EventRenderFire() {
        this.fireHeight = 1.0F;
    }

    public EventRenderFire(float fireHeight) {
        this.fireHeight = fireHeight;
    }

    public void setFireHeight(float fireHeightIn) {
        this.fireHeight = fireHeightIn;
    }

    public float getFireHeight() {
        return this.fireHeight;
    }
}
