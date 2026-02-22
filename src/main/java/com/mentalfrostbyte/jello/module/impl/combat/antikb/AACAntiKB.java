package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class AACAntiKB extends Module {
    public static int ticks;
    public float field23908;
    public float field23909;

    public AACAntiKB() {
        super(ModuleCategory.COMBAT, "AAC", "Places block underneath");
        this.registerSetting(new NumberSetting<>("Strength", "Boost strength", 0.7F, 0.0F, 1.0F, 0.01F));
        ticks = 20;
    }

    @Override
    public void onDisable() {
        ticks = 20;
    }

    @EventTarget
    public void method16788(EventMove var1) {
        if (this.isEnabled() && mc.player != null) {
            if (this.noStrength()) {
                if (ticks == 0 && !mc.player.isOnGround() && mc.player.hurtTime > 0 && mc.player.fallDistance < 2.0F) {
                    mc.player.addVelocity(0.0, -1.0, 0.0);
                    mc.player.setMotion(mc.player.getMotion().x, mc.player.getMotion().getY(), mc.player.getMotion().z);
                    mc.player.setOnGround(true);
                    ticks = 20;
                }
            } else {
                if (ticks < 7) {
                    ticks++;
                    if (ticks > 1) {
                        float var4 = MovementUtil.getDirectionArray()[1];
                        float var5 = MovementUtil.getDirectionArray()[2];
                        float var6 = MovementUtil.getDirectionArray()[0];
                        double var7 = Math.cos(Math.toRadians(var6));
                        double var9 = Math.sin(Math.toRadians(var6));
                        double var11 = (double) ((float) (7 - ticks) * this.getNumberValueBySettingName("Strength")) * 0.04 * (double) this.field23909 * 0.2;
                        double var13 = ((double) var4 * var7 + (double) var5 * var9) * var11;
                        double var15 = ((double) var4 * var9 - (double) var5 * var7) * var11;
                        float var17 = (float) (Math.atan2(var13, var15) * 180.0 / Math.PI) - 90.0F;
                        float var18 = RotationUtil.getWrappedAngleDifference(this.field23908, var17);
                        if (var18 > 100.0F && MovementUtil.isMoving()) {
                            var1.setX(var1.getX() + var13);
                            var1.setZ(var1.getZ() + var15);
                        } else {
                            var1.setX(var1.getX() * 0.8);
                            var1.setZ(var1.getZ() * 0.8);
                        }

                        mc.player.setMotion(var1.getX(), mc.player.getMotion().y, var1.getZ());
                    }
                }
            }
        }
    }

    @EventTarget
    public void method16789(EventReceivePacket var1) {
        if (this.isEnabled() && mc.player != null) {
            IPacket packet = var1.packet;
            if (packet instanceof SEntityVelocityPacket kb) {
                if (this.noStrength()) {
                    ticks = 0;
                    return;
                }

				if (kb.getEntityID() == mc.player.getEntityId() && (kb.getMotionX() != 0 || kb.getMotionZ() != 0)) {
                    this.field23909 = (float) (Math.sqrt(kb.getMotionX() * kb.getMotionX() + kb.getMotionZ() * kb.getMotionZ()) / 1000.0);
                    this.field23908 = (float) (Math.atan2((double) kb.getMotionX() / 1000, (double) kb.getMotionZ() / 1000) * 180.0 / Math.PI) - 90.0F;
                    ticks = 0;
                }
            }
//            unused
//            if (var1.packet instanceof SExplosionPacket) {
//            }
        }
    }

    public boolean noStrength() {
        return this.getNumberValueBySettingName("Strength") == 0.0F;
    }
}
