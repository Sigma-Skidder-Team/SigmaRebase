package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class AACNoFall extends Module {
    private boolean falling;

    public AACNoFall() {
        super(ModuleCategory.PLAYER, "AAC", "NoFall for AAC");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.falling = false;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;
		assert mc.player != null;
		if (mc.player.getPosY() < 2.0) return;
        if (!event.isPre()) return;
        if (mc.player.ticksExisted == 1) {
            this.falling = false;
        }

        if (!this.falling && mc.player.fallDistance > 3.0F) {
            this.falling = !this.falling;
            CPlayerPacket.PositionPacket pos = new CPlayerPacket.PositionPacket(
                    mc.player.getPosX(),
                    Double.NaN,
                    mc.player.getPosZ(),
                    true
            );
            mc.getConnection().sendPacket(pos);
        }
    }

}
