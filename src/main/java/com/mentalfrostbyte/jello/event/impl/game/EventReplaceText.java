package com.mentalfrostbyte.jello.event.impl.game;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventReplaceText extends CancellableEvent {
    private String text;

    public EventReplaceText(String var1) {
        this.text = var1;
    }

    public String setText() {
        return this.text;
    }

    public void setText(String var1) {
        this.text = var1;
    }
}
