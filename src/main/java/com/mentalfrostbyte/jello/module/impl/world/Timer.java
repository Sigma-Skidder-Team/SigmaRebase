package com.mentalfrostbyte.jello.module.impl.world;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import team.sdhq.eventBus.annotations.EventTarget;

public class Timer extends Module {
    public Timer() {
        super(ModuleCategory.WORLD, "Timer", "Speeds up the world's timer");
        this.registerSetting(new NumberSetting<>("Timer", "Timer value", 0.1F, Float.class, 0.1F, 10.0F, 0.1F));
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            mc.timer.timerSpeed = this.getNumberValueBySettingName("Timer");
        }
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
    }
}