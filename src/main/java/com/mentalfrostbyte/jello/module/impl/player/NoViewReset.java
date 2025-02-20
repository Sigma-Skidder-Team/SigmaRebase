package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class NoViewReset extends Module {
    public NoViewReset() {
        super(ModuleCategory.PLAYER, "NoViewReset", "Prevents the server from resetting your client yaw/pitch");
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (mc.player != null) {
                if (mc.player.ticksExisted >= 10) {
                    if (mc.player != null && event.packet instanceof SPlayerPositionLookPacket) {
                        SPlayerPositionLookPacket positionLookPacket = (SPlayerPositionLookPacket) event.packet;
                        mc.player.prevRotationYaw = positionLookPacket.yaw;
                        mc.player.prevRotationPitch = positionLookPacket.pitch;
                        positionLookPacket.yaw = mc.player.rotationYaw;
                        positionLookPacket.pitch = mc.player.rotationPitch;
                    }
                }
            }
        }
    }
}
