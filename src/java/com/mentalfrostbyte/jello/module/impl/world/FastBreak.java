package com.mentalfrostbyte.jello.module.impl.world;

import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.misc.*;


public class FastBreak extends Module {
    public FastBreak() {
        super(ModuleCategory.WORLD, "FastBreak", "Break blocks faster");
    }

    @EventTarget
    private void TickEvent(TickEvent event) {
        if (this.isEnabled()) {
            if (mc.playerController.field31362 > 0.7F) {
                mc.playerController.field31362 = 1.0F;
            }

            mc.playerController.field31364 = 0;
        }
    }
}
