package com.mentalfrostbyte.jello.event.impl.game.network;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

<<<<<<<< HEAD:src/main/java/com/mentalfrostbyte/jello/event/impl/SendPacketEvent.java
public class SendPacketEvent extends CancellableEvent {
    private Packet packet;
    private final List<Packet> field21514 = new ArrayList<Packet>();

    public SendPacketEvent(Packet var1) {
========
public class EventSendPacket extends CancellableEvent {
    private IPacket packet;
    private final List<IPacket> field21514 = new ArrayList<IPacket>();

    public EventSendPacket(IPacket var1) {
>>>>>>>> d57481e640c641fa8f5a34328b34f3c523118347:src/main/java/com/mentalfrostbyte/jello/event/impl/game/network/EventSendPacket.java
        this.packet = var1;
        this.field21514.add(var1);
    }

    public Packet getPacket() {
        return this.packet;
    }

    public List<Packet> method13933() {
        return this.field21514;
    }

    public void method13934(Packet var1) {
        this.packet = var1;
    }
}