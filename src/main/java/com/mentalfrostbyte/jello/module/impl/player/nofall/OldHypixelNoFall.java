package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import team.sdhq.eventBus.annotations.EventTarget;

public class OldHypixelNoFall extends Module {
    private double stage;

    public OldHypixelNoFall() {
        super(ModuleCategory.PLAYER, "OldHypixel", "Old Hypixel NoFall");
    }
    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;
		assert mc.player != null;
		if (mc.player.getPosY() < 2.0) return;
        if (!event.isPre()) return;
        if (BlockUtil.isAboveBounds(mc.player, 1.0E-4F)) {
            this.stage = 0.0;
            return;
        }

        if (mc.player.getMotion().y < -0.1) {
            this.stage = this.stage - mc.player.getMotion().y;
        }

        if (this.stage > 3.0) {
            this.stage = 1.0E-14;
            event.setOnGround(true);
        }
    }

}
