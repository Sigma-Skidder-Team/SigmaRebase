package com.mentalfrostbyte.jello.module.impl.movement.phase;


import com.mentalfrostbyte.jello.event.impl.game.world.EventPushBlock;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;

public class FullBlockPhase extends Module {
    public FullBlockPhase() {
        super(ModuleCategory.MOVEMENT, "FullBlock", "Basic phase");
    }

    @EventTarget
    public void EventUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && MultiUtilities.method17761()) {
            event.setMoving(true);
        }
    }

    @EventTarget
    public void EventMove(EventMove event) {
        if (this.isEnabled()) {
            if (!MultiUtilities.method17761()) {
                if (mc.player.collidedHorizontally) {
                    MovementUtil.setSpeed(event, 0.0);
                    MovementUtil.method37095(1.1920931E-8);
                }
            } else {
                MovementUtil.method37095(0.617);
            }
        }
    }

    @EventTarget
    public void EventPushBlock(EventPushBlock event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }
}
