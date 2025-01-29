package com.mentalfrostbyte.jello.event.impl.player;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventGetFovModifier extends CancellableEvent {
    public float fovModifier;

    public EventGetFovModifier(float fovModifier) {
        this.fovModifier = fovModifier;
    }
}
