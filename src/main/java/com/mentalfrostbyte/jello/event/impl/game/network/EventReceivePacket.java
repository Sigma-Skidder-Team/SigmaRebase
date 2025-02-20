package com.mentalfrostbyte.jello.event.impl.game.network;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.network.IPacket;

public class EventReceivePacket extends CancellableEvent {
    public IPacket<?> packet;

    public EventReceivePacket(IPacket<?> var1) {
        this.packet = var1;
    }

    public IPacket<?> getPacket() {
        return this.packet;
    }

    public void setPacket(IPacket packet) {
        this.packet = packet;
    }
}

