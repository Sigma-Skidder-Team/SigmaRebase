package com.mentalfrostbyte.jello.event.impl.game.render;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventRender2D extends CancellableEvent {
    public final float partialTicks;
    public final long field21555;

    public EventRender2D() {
        this.field21555 = 0L;
        this.partialTicks = 0.0F;
    }

    public EventRender2D(float var1, long var2) {
        this.field21555 = var2;
        this.partialTicks = var1;
    }
}
