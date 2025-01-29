package com.mentalfrostbyte.jello.module.impl.combat.wtap;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputMappings;
import team.sdhq.eventBus.annotations.EventTarget;

public class LegitWTap extends Module {
    public LegitWTap() {
        super(ModuleCategory.COMBAT, "Legit", "Increase the knockback you give to players");
    }

    @EventTarget
    public void TickEvent(EventPlayerTick event) {
        if (this.isEnabled()) {
            mc.gameSettings.keyBindSprint.setPressed(true);
            if (mc.player.swingProgressInt != 1) {
                if (mc.player.swingProgressInt == 0) {
                    mc.gameSettings.keyBindForward.setPressed(InputMappings.isKeyDown(
                            MinecraftClient.getInstance().getMainWindow().getHandle(),
                            mc.gameSettings.keyBindForward.keyCode.getKeyCode()));
                }
            } else {
                mc.gameSettings.keyBindForward.setPressed(false);
            }
        }
    }
}
