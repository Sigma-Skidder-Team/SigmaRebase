package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;

/**
 * @author alarmingly_good (on discord)
 */
public class VerusGlideFly extends Module {
    int airTicks;
    public VerusGlideFly() {
        super(ModuleCategory.MOVEMENT, "Verus Glide", "A fly for Verus");
    }

    @EventTarget
    public void onMotion(EventUpdateWalkingPlayer event) {
        if (!MovementUtil.isMoving()) {
            MovementUtil.stop();
        }

        if (!mc.player.onGround) MovementUtil.strafe(0.334);

        if (mc.gameSettings.keyBindJump.pressed) {
            if (mc.player.ticksExisted % 2 == 0) {
                mc.player.setMotion(mc.player.getMotion().x, 0.42F, mc.player.getMotion().z);
            }
        } else if (mc.gameSettings.keyBindSneak.pressed){

        } else {
            mc.player.setMotion(mc.player.getMotion().x, Math.max(mc.player.getMotion().y, -0.09800000190734863), mc.player.getMotion().z);
        }
    }
}
