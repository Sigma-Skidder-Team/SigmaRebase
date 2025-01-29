package com.mentalfrostbyte.jello.module.impl.movement.speed;

import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.player.MovementUtil;

public class SlowHopSpeed extends Module {
    private int onGroundTicks;
    private double speed;
    private boolean field23601;

    public SlowHopSpeed() {
        super(ModuleCategory.MOVEMENT, "SlowHop", "SlowHop speed");
        this.registerSetting(new BooleanSetting("AutoJump", "Automatically jumps for you.", true));
    }

    @Override
    public void onEnable() {
        this.speed = MovementUtil.getSpeed();
        this.onGroundTicks = 2;
    }

    @Override
    public void onDisable() {
        MovementUtil.strafe(MovementUtil.getSpeed());
    }

    @EventTarget
    public void onMove(EventMove var1) {
        if (this.isEnabled()) {
            boolean autoJump = this.getBooleanValueFromSettingName("AutoJump");
            double var5 = MovementUtil.getSpeed();
            if (!mc.player.isOnGround()) {
                this.onGroundTicks++;
                this.speed = 0.36 - (double) this.onGroundTicks / 250.0;
                if (this.speed < var5) {
                    this.speed = var5;
                }

                MovementUtil.setSpeed(var1, this.speed);
            } else {
                this.onGroundTicks = 0;
                mc.player.jump();
                var1.setY(mc.player.getMotion().y);
            }
        }
    }

    @EventTarget
    public void onJump(EventJump var1) {
        if (this.isEnabled()) {
             var1.setY(0.407 + 0.1 * (double) MovementUtil.getJumpBoost());
            this.onGroundTicks = 0;
             var1.setStrafeSpeed(1.8);
        }
    }
}
