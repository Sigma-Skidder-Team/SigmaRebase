package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class VanillaNoFall extends Module {
    public VanillaNoFall() {
        super(ModuleCategory.PLAYER, "Vanilla", "Vanilla NoFall");
    }
    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;
        if (mc.player.getMotion().y < -0.1) {
            event.setOnGround(true);
        }
    }

}
