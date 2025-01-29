package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class AutoWalk extends Module {
    public AutoWalk() {
        super(ModuleCategory.PLAYER, "AutoWalk", "Automatically walks forward");
    }

    @EventTarget
    public void TickEvent(EventPlayerTick event) {
        if (this.isEnabled()) {
            mc.gameSettings.keyBindForward.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindForward.setPressed(false);

    }
}
