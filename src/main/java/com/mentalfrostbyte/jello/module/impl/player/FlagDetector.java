package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class FlagDetector extends Module {
    private int flagCount;

    public FlagDetector() {
        super(ModuleCategory.PLAYER, "FlagDetector", "Detects flags");
        this.flagCount = 0;
    }

    @EventTarget
    public void RecievePacketEvent(EventReceivePacket event) {
        if (event.getPacket() instanceof SPlayerPositionLookPacket && mc.player != null) {
            flagCount++;
            Client.getInstance().notificationManager.send(new Notification("FlagDetector", "Detected Flag x" + flagCount));
        }
    }
}
