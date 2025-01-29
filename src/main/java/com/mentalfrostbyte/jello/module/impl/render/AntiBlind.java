package com.mentalfrostbyte.jello.module.impl.render;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import team.sdhq.eventBus.annotations.EventTarget;

public class AntiBlind extends Module {
    public AntiBlind() {
        super(ModuleCategory.RENDER, "AntiBlind", "Disables bad visual potion effects");
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (this.isEnabled() && mc.player != null) {
            PlayerEntity player = mc.player;
            if (player.isPotionActive(Effects.BLINDNESS)) {
                player.removePotionEffect(Effects.BLINDNESS);
            }
            if (player.isPotionActive(Effects.NAUSEA)) {
                player.removePotionEffect(Effects.NAUSEA);
            }
        }
    }
}
