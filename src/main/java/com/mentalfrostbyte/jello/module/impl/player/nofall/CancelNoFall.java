package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class CancelNoFall extends Module {
    public static boolean falling;

    public CancelNoFall() {
        super(ModuleCategory.PLAYER, "Cancel", "Cancel NoFall");
    }
    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (!this.isEnabled() || event.cancelled) return;
        if (event.getPacket() instanceof CPlayerPacket) {
            if (mc.player.fallDistance > 3f) {
                falling = true;
                event.cancelled = true;
                return;
            }
            falling = false;
        }
    }
    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getPacket() instanceof SPlayerPositionLookPacket packet && falling) {
            mc.getConnection().sendPacket(
                    new CPlayerPacket.PositionRotationPacket(
                            packet.getX(), packet.getY(),
                            packet.getZ(), packet.getYaw(),
                            packet.getPitch(), true
                    )
            );
            falling = false;
        }
    }
}
