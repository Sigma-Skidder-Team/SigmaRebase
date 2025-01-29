package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventWalkingUpdate;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import team.sdhq.eventBus.annotations.EventTarget;

public class Parkour extends Module {
    public Parkour() {
        super(ModuleCategory.PLAYER, "Parkour", "Automatically jumps at the edge of blocks");
    }

    // doesnt jump not fixing rn
    @EventTarget
    public void EventWalkingUpdate(EventWalkingUpdate event) {
        if (this.isEnabled()) {
            if (mc.player.isOnGround()) {
                if (!MultiUtilities.method17729()) {
                    mc.player.jump();
                }
            }
        }
    }
}
