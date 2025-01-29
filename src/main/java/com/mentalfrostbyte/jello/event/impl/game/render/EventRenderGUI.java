package com.mentalfrostbyte.jello.event.impl.game.render;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventRenderGUI extends CancellableEvent {
    public boolean isRendering;

    public EventRenderGUI(boolean isRendering) {
        this.isRendering = isRendering;
    }
}
