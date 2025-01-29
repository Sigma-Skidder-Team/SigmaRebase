package com.mentalfrostbyte.jello.event.impl.game.render;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventRender2DOffset extends CancellableEvent {
    private int yOffset = 99;

    public int getyOffset() {
        return this.yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public void addOffset(int yOffset) {
        this.yOffset += yOffset;
    }
}
