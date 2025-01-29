package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import team.sdhq.eventBus.annotations.EventTarget;

public class AutoSprint extends Module {

    public AutoSprint() {
        super(ModuleCategory.PLAYER, "AutoSprint", "Sprints for you");
        this.registerSetting(new BooleanSetting("Keep Sprint", "Keep Sprinting after hitting a player", true));
    }

    @EventTarget
    public void TickEvent(EventPlayerTick event) {
        mc.gameSettings.keyBindSprint.setPressed(true);
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSprint.setPressed(mc.gameSettings.keyBindSprint.isKeyDown());
    }
}
