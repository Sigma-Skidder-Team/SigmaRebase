package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.Client;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.SafeWalkEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;

public class SafeWalk extends Module {
    public SafeWalk() {
        super(ModuleCategory.MOVEMENT, "SafeWalk", "Doesn't let you run off edges");
    }

    @EventTarget
    public void SafeWalkEvent(SafeWalkEvent event) {
        if (this.isEnabled() && mc.player.isOnGround()) {
            Module BlockFly = Client.getInstance().moduleManager.getModuleByClass(BlockFly.class);
            if (!BlockFly.isEnabled()) {
                event.setSafe(true);
            }
        }
    }
}
