package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class VerusNoFall extends Module {
    public VerusNoFall() {
        super(ModuleCategory.PLAYER, "Verus", "Verus NoFall");
    }
    @EventTarget
    public void onTick(EventPlayerTick __) {
        if (!this.isEnabled()) return;
        // Thanks, @alarmingly_good.
        if (!mc.player.onGround && mc.player.getMotion().y < 0 && mc.player.fallDistance > 2) {
            mc.player.onGround = true;
            mc.player.setMotion(mc.player.getMotion().x, 0.0, mc.player.getMotion().z);
            mc.player.fallDistance = 0;
        }
    }

}
