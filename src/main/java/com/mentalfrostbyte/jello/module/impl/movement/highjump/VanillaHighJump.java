package com.mentalfrostbyte.jello.module.impl.movement.highjump;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import team.sdhq.eventBus.annotations.EventTarget;

public class VanillaHighJump extends Module {
    public VanillaHighJump() {
        super(ModuleCategory.MOVEMENT, "Vanilla", "Highjump for minecraft vanilla");
        this.registerSetting(new NumberSetting<>("Motion", "Highjump motion", 0.75F, Float.class, 0.42F, 5.0F, 0.05F));
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (this.isEnabled()) {
            event.setY(this.getNumberValueBySettingName("Motion"));
        }
    }
}
