package com.mentalfrostbyte.jello.event;

import team.sdhq.eventBus.Event;

public class CancellableEvent extends Event {

    public boolean cancelled;

    public boolean setCancelled(boolean var1) {
        return this.cancelled = var1;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
