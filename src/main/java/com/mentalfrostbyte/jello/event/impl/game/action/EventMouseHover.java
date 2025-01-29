package com.mentalfrostbyte.jello.event.impl.game.action;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventMouseHover extends CancellableEvent {
    private final int button;

    public EventMouseHover(int button) {
        this.button = button;
    }

    public int getMouseButton() {
        return this.button;
    }
}
