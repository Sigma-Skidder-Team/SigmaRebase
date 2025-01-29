package com.mentalfrostbyte.jello.event.impl.game.network;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.network.Packet;

public class EventReceivePacket extends CancellableEvent {
    private Packet<?> packet;

    public EventReceivePacket(Packet<?> var1) {
        this.packet = var1;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
