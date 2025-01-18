package com.mentalfrostbyte.jello.event.impl;


import com.mentalfrostbyte.jello.event.Event;
import com.mentalfrostbyte.jello.event.ICancellableEvent;

public class CancellableEvent implements Event, ICancellableEvent {
    public boolean cancelled = false;

    @Override
    public boolean setCancelled(boolean var1) {
        return this.cancelled = var1;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
