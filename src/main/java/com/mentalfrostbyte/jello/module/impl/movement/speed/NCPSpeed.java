package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventStep;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import com.mentalfrostbyte.jello.module.impl.movement.Jesus;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class NCPSpeed extends Module {
    private int field23607;
    private int field23608;
    private double field23609;

    public NCPSpeed() {
        super(ModuleCategory.MOVEMENT, "NCP", "Speed for NCP");
        this.registerSetting(new BooleanSetting("Auto Jump", "Automatically jumps for you.", true));
    }

    @Override
    public void onEnable() {
        this.field23608 = 1;
        double mX = mc.player.getMotion().x;
        double mZ = mc.player.getMotion().z;
        this.field23609 = Math.sqrt(mX * mX + mZ * mZ);
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled()
                && mc.player != null
                && !Jesus.isWalkingOnLiquid()
                && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
            if (var1.isPre() && Speed.tickCounter > 1) {
                double var4 = mc.player.getPosX() - mc.player.lastReportedPosX;
                double var6 = mc.player.getPosZ() - mc.player.lastReportedPosZ;
                if (this.field23607 != 0) {
                    this.field23609 = Math.sqrt(var4 * var4 + var6 * var6);
                } else {
                    this.field23609 = this.field23609 * (0.67 + Math.random() * 1.0E-10);
                }
            }
        }
    }

    @EventTarget
    public void onMove(EventMove var1) {
        if (this.isEnabled() && mc.player != null) {
            if (!Jesus.isWalkingOnLiquid() && !mc.player.isInWater()) {
                if (this.field23608 < 2) {
                    this.field23608++;
                }

                if (!mc.player.isOnGround()) {
                    if (this.field23607 >= 0) {
                        this.field23607++;
                        double var4 = this.field23609;
                        if (this.field23607 > 1) {
                            var4 = Math.max(MovementUtil.method37076(), this.field23609 - (0.004 - MovementUtil.method37076() * 0.003) - Math.random() * 1.0E-10);
                        }

                        MovementUtil.setSpeed(var1, var4);
                        if (var1.getY() >= -0.008744698139753596 && var1.getY() <= -0.008724698139753597) {
                            var1.setY(0.001);
                        } else if (var1.getY() >= -0.07743000150680542 && var1.getY() <= -0.07741000150680542) {
                            var1.setY(var1.getY() - 0.01);
                        }
                    }
                } else if (this.field23608 > 1 && (this.getBooleanValueFromSettingName("Auto Jump") && MultiUtilities.isMoving() || mc.gameSettings.keyBindJump.isKeyDown())) {
                    this.field23607 = 0;
                    mc.player.jump();
                    var1.setX(mc.player.getMotion().x);
                    var1.setY(mc.player.getMotion().y);
                    var1.setZ(mc.player.getMotion().z);
                }
            } else {
                this.field23607 = -1;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onJump(EventJump event) {
        if (this.isEnabled() && !Jesus.isWalkingOnLiquid()) {
            if (this.field23607 != 0) {
                event.setCancelled(true);
            }

            if (!mc.gameSettings.keyBindJump.isKeyDown() || !Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).isEnabled()) {
                double strafeSpeed = 0.56 + (double) MovementUtil.getSpeedBoost() * 0.1;
                event.setY(0.407 + (double) MovementUtil.getJumpBoost() * 0.1 + Math.random() * 1.0E-5);
                if (Speed.tickCounter< 2) {
                    strafeSpeed /= 2.5;
                }

                strafeSpeed = Math.max(MovementUtil.method37076(), strafeSpeed);
                event.setStrafeSpeed(strafeSpeed);
                this.field23609 = strafeSpeed;
            }
        }
    }

    @EventTarget
    public void onStep(EventStep var1) {
        if (this.isEnabled() && !(var1.getHeight() < 0.9)) {
            this.field23608 = 0;
        }
    }
}