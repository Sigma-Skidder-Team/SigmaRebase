package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class VanillaLegitNoFall extends Module {
    private boolean vanillaLegitFalling;

    public VanillaLegitNoFall() {
        super(ModuleCategory.PLAYER, "VanillaLegit", "Legit version of Vanilla NoFall");
    }
    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;
        if (mc.player.getMotion().y < -0.1) {
            event.setOnGround(true);
        }

        if (mc.player.fallDistance > 3.0F) {
            this.vanillaLegitFalling = true;
        }

        if (this.vanillaLegitFalling && mc.player.isOnGround() && !mc.player.isInWater()) {
            double cX = mc.player.getPosX();
            double cY = mc.player.getPosY();
            double cZ = mc.player.getPosZ();
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(cX, cY + 3.01, cZ, false));
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(cX, cY, cZ, false));
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(cX, cY, cZ, true));
            this.vanillaLegitFalling = false;
        }
    }

}
