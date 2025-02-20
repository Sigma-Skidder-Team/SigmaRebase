package com.mentalfrostbyte.jello.event.impl.game.network;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.network.IPacket;

import java.util.ArrayList;
import java.util.List;

public class EventSendPacket extends CancellableEvent {
    public IPacket packet;

	public EventSendPacket(IPacket packet) {
        this.packet = packet;
    }
}