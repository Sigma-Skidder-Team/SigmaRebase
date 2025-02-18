package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import team.sdhq.eventBus.annotations.EventTarget;

public class ElytraFly extends Module {
    public int field23528 = 0;
    public int field23533 = 0;
    private float field23529;
    private float field23530;
    private float field23531;
    private boolean field23532;

    public ElytraFly() {
        super(ModuleCategory.MOVEMENT, "ElytraFly", "Better elytra flying");
        this.registerSetting(new BooleanSetting("NCP", "Bypass NCP", true));
    }

    @EventTarget
    public void method16220(EventPlayerTick var1) {
        if (this.isEnabled()) {
            mc.gameSettings.keyBindSneak.pressed = false;
            if (!(mc.player.getMotion().y < 0.08) || mc.player.onGround) {
                mc.player.setFlag(7, false);
                if (mc.player.isSneaking()) {
                    this.setEnabled(false);
                }
            } else if (!mc.player.isElytraFlying()) {
                mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                mc.player.setFlag(7, true);
            }
        }
    }

    @EventTarget
    public void method16221(EventMove var1) {
        if (this.isEnabled()) {
            double var4 = MovementUtil.getSmartSpeed();
            if (!this.getBooleanValueFromSettingName("NCP") && mc.player.isSneaking()) {
                var4 *= 2.5;
            }

            MovementUtil.setMotion(var1, 0.0);
            if (!mc.player.isElytraFlying()) {
                this.field23528 = 0;
            } else {
                if (this.field23528 > 0) {
                    if (this.field23528 > 7) {
                        MovementUtil.setMotion(var1, var4 * 6.3F);
                    }

                    mc.player.setMotion(mc.player.getMotion().x, -0.071, mc.player.getMotion().z);
                    var1.setY(-1.0001E-4F);
                }

                this.field23528++;
            }

            if (this.field23530 > 1.0001E-4F && mc.player.isJumping) {
                MovementUtil.setMotion(var1, var4 * 6.3F);
                var1.setY(this.field23530);
            }

            int var7 = GLFW.glfwGetKey(mc.mainWindow.getHandle(), mc.gameSettings.keyBindSneak.keyCode.getKeyCode());
            if (var7 == 1 && this.getBooleanValueFromSettingName("NCP")) {
                var1.setY(-0.9F);
            } else if (!mc.player.isSneaking()) {
                if (mc.player.isJumping && !this.getBooleanValueFromSettingName("NCP")) {
                    var1.setY(1.4F);
                }
            } else {
                var1.setY(1.4F);
            }

            this.field23530 = (float) ((double) this.field23530 * 0.85);
        }
    }

    @EventTarget
    public void method16222(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (mc.player != null && var1.getPacket() instanceof SEntityVelocityPacket) {
                SEntityVelocityPacket var4 = (SEntityVelocityPacket) var1.getPacket();
                Entity var5 = mc.world.getEntityByID(var4.getEntityID());
                if (var5 instanceof FireworkRocketEntity) {
                    FireworkRocketEntity var6 = (FireworkRocketEntity) var5;
                    if (var6.getBoundingBox() != null && var6.getBoostedEntity().getEntityId() == mc.player.getEntityId()) {
                        this.field23529 = this.field23529 + (float) var4.motionX / 8000.0F;
                        this.field23531 = this.field23531 + (float) var4.motionZ / 8000.0F;
                        this.field23530 = this.field23530 + (float) var4.motionY / 8000.0F;
                        this.field23532 = true;
                    }
                }
            }
        }
    }

    @EventTarget
    public void method16223(EventUpdateWalkingPlayer event) {
        if (this.isEnabled()) {
            int var4 = 65;
            if (this.field23533 != var4 - 1) {
                if (this.field23533 <= 0 && mc.player.isJumping) {
                    this.field23533 = var4;
                }
            } else {
                int var5 = InvManagerUtil.getSlotWithMaxItemCount(Items.FIREWORK_ROCKET);
                if (var5 >= 0) {
                    if (var5 != mc.player.inventory.currentItem) {
                        mc.getConnection().sendPacket(new CHeldItemChangePacket(var5));
                    }

                    mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    if (var5 != mc.player.inventory.currentItem) {
                        mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    }
                }
            }

            if (this.field23533 > 0) {
                event.setPitch(-90.0F);
            }

            if (!mc.player.isJumping) {
                this.field23533 = 0;
            }

            this.field23533--;
        }
    }



    @Override
    public void onEnable() {
        if (mc.player.onGround) {
            mc.player.setMotion(mc.player.getMotion().x, 0.3994F, mc.player.getMotion().z);
        }
    }


}