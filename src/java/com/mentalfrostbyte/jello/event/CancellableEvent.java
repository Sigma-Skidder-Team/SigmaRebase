package com.mentalfrostbyte.jello.event;

import team.sdhq.eventBus.Event;

public class CancellableEvent extends Event {
    public boolean cancelled;
    public boolean isCancelled() {
        return cancelled;
    }
}
