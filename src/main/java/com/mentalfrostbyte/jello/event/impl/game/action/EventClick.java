package com.mentalfrostbyte.jello.event.impl.game.action;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventClick extends CancellableEvent {
    private final Button butoon;

    public EventClick(Button var1) {
        this.butoon = var1;
    }

    public Button getButton() {
        return this.butoon;
    }

    public enum Button {
        LEFT,
        RIGHT,
        MID;
    }
}
