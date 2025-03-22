package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;

public class InvadedSpeed extends Module {
    private int field24022;

    public InvadedSpeed() {
        super(ModuleCategory.MOVEMENT, "Invaded", "Speed for Invadedlands");
        this.registerSetting(new NumberSetting<Float>("Speed", "Speed value", 3.0F, Float.class, 0.5F, 9.5F, 0.1F));
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        MovementUtil.strafe(0.28F);
        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    public void method16969(EventMove var1) {
        if (this.isEnabled()) {
            this.field24022++;
            if (this.field24022 != 1) {
                if (this.field24022 != 2) {
                    if (this.field24022 >= 3) {
                        this.field24022 = 0;
                        mc.timer.timerSpeed = 0.25F;
                        MovementUtil.setMotion(var1, this.getNumberValueBySettingName("Speed"));
                    }
                } else {
                    mc.timer.timerSpeed = 2.0F;
                    MovementUtil.setMotion(var1, MovementUtil.getSpeed() + 0.05);
                }
            } else {
                mc.timer.timerSpeed = 2.0F;
                MovementUtil.setMotion(var1, MovementUtil.getSpeed() + 0.05);
            }
        }
    }
}
