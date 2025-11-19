package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import team.sdhq.eventBus.annotations.EventTarget;

public class VerusNoFall extends Module {
    private double stage;

    public VerusNoFall() {
        super(ModuleCategory.PLAYER, "Verus", "Verus NoFall");
    }

    @EventTarget
    public void onUpdate(EventMotion event) {
        if (!this.isEnabled()) return;
        // Thanks, @alarmingly_good.
        if (!mc.player.onGround && mc.player.getMotion().y < 0 && mc.player.fallDistance > 2) {
            mc.player.onGround = true;
            event.setOnGround(true);
            mc.player.setMotion(mc.player.getMotion().x, 0.0, mc.player.getMotion().z);
            mc.player.fallDistance = 0;
        }
    }

}
