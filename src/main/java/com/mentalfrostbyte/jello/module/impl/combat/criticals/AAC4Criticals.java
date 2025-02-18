package com.mentalfrostbyte.jello.module.impl.combat.criticals;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventStep;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class AAC4Criticals extends Module {
    private static int field23862;

    public AAC4Criticals() {
        super(ModuleCategory.COMBAT, "AAC 4", "Criticals for aac 4.*");
        this.registerSetting(new BooleanSetting("KillAura", "Criticals only if KillAura is enabled", false));
    }

    @Override
    public void onEnable() {
        field23862 = 0;
    }

    @EventTarget
    @LowerPriority
    public void method16705(EventStep var1) {
        if (this.isEnabled() && !(var1.getHeight() < 0.625)) {
            field23862 = 0;
        }
    }

    @EventTarget
    public void method16706(EventJump var1) {
        if (field23862 == 2) {
            var1.cancelled = true;
        }
    }

    @EventTarget
    @HigherPriority
    public void method16707(EventUpdateWalkingPlayer var1) {
        if (!this.isEnabled() || Client.getInstance().moduleManager.getModuleByClass(Speed.class).isEnabled()) {
            field23862 = 0;
        } else if (var1.isPre()) {
            if (mc.playerController.getIsHittingBlock()) {
                field23862 = 0;
            }

            boolean var4 = !this.getBooleanValueFromSettingName("KillAura") || KillAura.targetEntity != null || KillAura.targetData != null;
            if (mc.player.isOnGround() && mc.player.collidedVertically && var4) {
                field23862++;
                mc.player.lastReportedPosY = 0.0;
                if (field23862 != 2) {
                    if (field23862 >= 3) {
                        double var5 = 0.001;
                        var1.setY(var1.getY() + var5);
                        var1.setOnGround(false);
                    }
                } else {
                    var1.setY(var1.getY() + 0.00101);
                    var1.setOnGround(false);
                }
            } else {
                field23862 = 0;
            }
        }
    }
}
