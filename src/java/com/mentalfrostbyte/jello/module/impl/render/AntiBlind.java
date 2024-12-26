package com.mentalfrostbyte.jello.module.impl.render;

import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.Render2DEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.potion.Effects;

public class AntiBlind extends Module {
    public AntiBlind() {
        super(ModuleCategory.RENDER, "AntiBlind", "Disables bad visual potion effects");
    }

    @EventTarget
    private void Render2DEvent(Render2DEvent event) {
        if (this.isEnabled() && mc.player != null) {
            mc.player.removeActivePotionEffect(Effects.NAUSEA);
            mc.player.removeActivePotionEffect(Effects.BLINDNESS);
        }
    }
}
