package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import team.sdhq.eventBus.annotations.EventTarget;

public class HypixelNoFall extends Module {
    public HypixelNoFall() {
        super(ModuleCategory.PLAYER, "Hypixel", "Hypixel NoFall");
    }
    @EventTarget
    public void onUpdate(EventMotion event) {
        if (!this.isEnabled()) return;
		assert mc.player != null;
		if (mc.player.getPosY() < 2.0) return;
        if (event.isPre() && mc.player.getMotion().y < 0.0 && !mc.player.isOnGround() && ServerUtil.isHypixel()) {
            for (double verticalOffset : MovementUtil.getVerticalOffsets()) {
                if ((double) ((int) event.getY()) - event.getY() + verticalOffset == 0.0) {
                    event.setOnGround(true);
                    break;
                }
            }
        }
    }

}
