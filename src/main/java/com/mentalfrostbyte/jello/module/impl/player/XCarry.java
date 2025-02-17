package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class XCarry extends Module {
    public XCarry() {
        super(ModuleCategory.PLAYER,"XCarry", "Allows you to carry more items in your inventory");
    }

    @EventTarget
    public void onPacket(EventSendPacket event) {
        if (event.getPacket() instanceof net.minecraft.network.play.client.CCloseWindowPacket) {
            event.cancelled = true;
        }
    }

}
