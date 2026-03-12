package com.mentalfrostbyte.jello.event.impl.game.render;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.entity.Entity;

public class EventRenderNameTag extends CancellableEvent {
    private final Entity entity;

    public EventRenderNameTag(Entity var1) {
        this.entity = var1;
    }

    public Entity getEntity() {
        return this.entity;
    }
}

