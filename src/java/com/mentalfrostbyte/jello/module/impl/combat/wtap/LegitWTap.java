package com.mentalfrostbyte.jello.module.impl.combat.wtap;

import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.misc.InputMappings;
import net.minecraft.client.Minecraft;

public class LegitWTap extends Module {
    public LegitWTap() {
        super(ModuleCategory.COMBAT, "Legit", "Increase the knockback you give to players");
    }

    @EventTarget
    private void TickEvent(TickEvent event) {
        if (this.isEnabled()) {
            mc.gameSettings.keyBindSprint.setPressed(true);
            if (mc.player.swingProgressInt != 1) {
                if (mc.player.swingProgressInt == 0) {
                    mc.gameSettings.keyBindForward.setPressed(InputMappings.isKeyDown(
                            Minecraft.getInstance().getMainWindow().getHandle(),
                            mc.gameSettings.keyBindForward.keyCode.getKeyCode()
                    ));
                }
            } else {
                mc.gameSettings.keyBindForward.setPressed(false);
            }
        }
    }
}