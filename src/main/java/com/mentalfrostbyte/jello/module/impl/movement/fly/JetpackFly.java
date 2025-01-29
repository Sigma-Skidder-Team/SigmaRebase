package com.mentalfrostbyte.jello.module.impl.movement.fly;

import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;

public class JetpackFly extends Module {
    public JetpackFly() {
        super(ModuleCategory.MOVEMENT, "Jetpack", "A jetpack type fly");
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (mc.player.isJumping) {
                mc.player.jump();
            }
        }
    }
}
