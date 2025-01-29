package com.mentalfrostbyte.jello.module.impl.render;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class NoHurtCam extends Module {
    public NoHurtCam() {
        super(ModuleCategory.RENDER, "NoHurtCam", "Disables the hurt animation");
    }

    @EventTarget
    public void Render2DEvent(EventRender2D event) {
        if (this.isEnabled()) {

            mc.player.hurtTime = 0;
        }
    }
}
