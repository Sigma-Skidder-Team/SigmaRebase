package com.mentalfrostbyte.jello.misc;

import com.mentalfrostbyte.jello.module.impl.world.disabler.ViperDisabler;
import net.minecraft.network.IPacket;

public class ViperEvent {
    private final long timeToSendPacket;
    private final IPacket<?> packet;
    public final ViperDisabler instance;

    public ViperEvent(ViperDisabler instance, IPacket<?> packet, long delay) {
        this.instance = instance;
        this.packet = packet;
        this.timeToSendPacket = System.currentTimeMillis() + delay;
    }

    public boolean shouldSendPacket() {
        return this.timeToSendPacket - System.currentTimeMillis() < 0L;
    }

    public IPacket getPacket() {
        return this.packet;
    }
}