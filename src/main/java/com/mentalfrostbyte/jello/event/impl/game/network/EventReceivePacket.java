package com.mentalfrostbyte.jello.event.impl.game.network;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.network.Packet;

<<<<<<<< HEAD:src/main/java/com/mentalfrostbyte/jello/event/impl/ReceivePacketEvent.java
public class ReceivePacketEvent extends CancellableEvent {
    private Packet<?> packet;

    public ReceivePacketEvent(Packet<?> var1) {
========
public class EventReceivePacket extends CancellableEvent {
    private IPacket<?> packet;

    public EventReceivePacket(IPacket<?> var1) {
>>>>>>>> d57481e640c641fa8f5a34328b34f3c523118347:src/main/java/com/mentalfrostbyte/jello/event/impl/game/network/EventReceivePacket.java
        this.packet = var1;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
