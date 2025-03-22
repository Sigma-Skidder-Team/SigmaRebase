package com.mentalfrostbyte.jello.module.impl.combat.criticals;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.combat.AntiKnockback;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class MinemenCriticals extends Module {
    private int airTicks;

    public MinemenCriticals() {
        super(ModuleCategory.COMBAT, "Minemen", "Criticals for Anti Gaming Chair");
    }

    @Override
    public void onEnable() {
        this.airTicks = 0;
    }

    @EventTarget
    @HigherPriority
    public void method16861(EventUpdateWalkingPlayer var1) {
        if (var1.isPre()) {
            ModuleWithModuleSettings var4 = (ModuleWithModuleSettings) Client.getInstance().moduleManager.getModuleByClass(AntiKnockback.class);
            if (!var4.isEnabled() || !var4.getStringSettingValueByName("Type").equalsIgnoreCase("Minemen")) {
                if (!mc.player.isOnGround()) {
                    this.airTicks = 0;
                } else {
                    if (this.airTicks > 0) {
                        if (this.airTicks % 2 != 0) {
                            var1.setY(var1.getY() - 1.0E-14);
                        }

                        var1.setOnGround(false);
                    }

                    this.airTicks++;
                    var1.setMoving(true);
                }

                if (mc.playerController.getIsHittingBlock() && mc.player.isOnGround()) {
                    this.airTicks = 0;
                    var1.setY(mc.player.getPosY());
                    var1.setOnGround(true);
                }
            }
        }
    }
}
