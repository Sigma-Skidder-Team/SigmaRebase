package com.mentalfrostbyte.jello.module.impl.movement.spider;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import team.sdhq.eventBus.annotations.EventTarget;

public class VanillaSpider extends Module {
    public VanillaSpider() {
        super(ModuleCategory.MOVEMENT, "Vanilla", "Spider for Vanilla");
        this.registerSetting(new NumberSetting<Float>("Motion", "Spider motion", 0.35F, Float.class, 0.2F, 1.0F, 0.05F));
    }

    @EventTarget
    public void EventMove(EventMove event) {
        if (this.isEnabled()) {
            if (mc.player.collidedHorizontally) {
                event.setY((double)this.getNumberValueBySettingName("Motion"));
            }
        }
    }
}