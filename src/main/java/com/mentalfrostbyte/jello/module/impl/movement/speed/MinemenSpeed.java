package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;

public class MinemenSpeed extends Module {
    public MinemenSpeed() {
        super(ModuleCategory.MOVEMENT, "Minemen", "Speed for AGC");
    }

    @EventTarget
    @HigherPriority
    public void EventMove(EventMove event) {
        if (mc.player.isOnGround()) {
            double calculatedSpeed = 0.3399 + (double) MovementUtil.getSpeedBoost() * 0.06;
            if (mc.player.ticksExisted % 3 == 0) {
                calculatedSpeed = 0.679 + (double) MovementUtil.getSpeedBoost() * 0.12;
            }

            MovementUtil.setMotion(event, calculatedSpeed);
        }
    }
}
