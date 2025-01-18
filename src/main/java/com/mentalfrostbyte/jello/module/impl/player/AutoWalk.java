package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import org.lwjgl.glfw.GLFW;
import team.sdhq.eventBus.annotations.EventTarget;

public class AutoWalk extends Module {
    public AutoWalk() {
        super(ModuleCategory.PLAYER, "AutoWalk", "Automatically walks forward");
    }

    @EventTarget
    public void TickEvent(TickEvent event) {
        if (this.isEnabled()) {
            mc.gameSettings.keyBindForward.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindForward.setPressed(false);

    }
}
