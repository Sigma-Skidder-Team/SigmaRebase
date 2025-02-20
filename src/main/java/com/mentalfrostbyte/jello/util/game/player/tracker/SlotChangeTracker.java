package com.mentalfrostbyte.jello.util.game.player.tracker;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.Date;
import java.util.HashMap;

public class SlotChangeTracker {
    private final HashMap<Integer, Date> modificationMap = new HashMap<>();

    public void init() {
        EventBus.register(this);
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket var1) {
        if (var1.packet instanceof SSetSlotPacket) {
            SSetSlotPacket slotPacket = (SSetSlotPacket) var1.packet;
            if (slotPacket.getWindowId() != 0) {
                return;
            }

            this.modificationMap.put(slotPacket.getSlot(), new Date());
        }
    }

    public long method33238(int time) {
        if (time != -1) {
            if (this.modificationMap.containsKey(time)) {
                long var4 = System.currentTimeMillis();
                return var4 - this.modificationMap.get(time).getTime();
            } else {
                return 2147483647L;
            }
        } else {
            return 0L;
        }
    }
}