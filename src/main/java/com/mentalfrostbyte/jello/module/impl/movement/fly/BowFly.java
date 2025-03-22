package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.action.EventStopUseItem;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.item.InvManager;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import team.sdhq.eventBus.annotations.EventTarget;

public class BowFly extends Module {
    public int field23504;
    public final TimerUtil timer = new TimerUtil();

    public BowFly() {
        super(ModuleCategory.MOVEMENT, "Bow", "Fly for Bow");
    }

    @Override
    public void onDisable() {
        if (mc.timer.timerSpeed == 0.1F) {
            mc.timer.timerSpeed = 1.0F;
        }
    }

    @Override
    public void initialize() {
    }

    @EventTarget
    public void onStopuseItem(EventStopUseItem var1) {
        if (this.isEnabled()) {
            if (mc.player.getHeldItem(Hand.MAIN_HAND).getItem() == Items.BOW && this.field23504 >= 1) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16179(EventMove var1) {
        if (this.isEnabled()) {
            double var4 = var1.getVector().y;
            var1.getVector().y = 0.0;
            double var6 = var1.getVector().length();
            var1.getVector().y = var4;
            float var8 = MovementUtil.getDirectionArray()[1];
            float var9 = MovementUtil.getDirectionArray()[2];
            float var10 = MovementUtil.getDirectionArray()[0];
            System.out.println(var6);
            if ((var8 != 0.0F || var9 != 0.0F) && !(var1.getVector().y < -0.5)) {
                double var11 = Math.cos(Math.toRadians(var10));
                double var13 = Math.sin(Math.toRadians(var10));
                var1.setX((double) var8 * var6 * var11 + (double) var9 * var6 * var13);
                var1.setZ((double) var8 * var6 * var13 - (double) var9 * var6 * var11);
                mc.player.getMotion().x = var1.getX();
                mc.player.getMotion().y = var1.getY();
            } else {
                var1.setX(0.0);
                var1.setZ(0.0);
            }
        }
    }

    @EventTarget
    public void method16180(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && event.isPre()) {
            if (!this.timer.isEnabled()) {
                this.timer.start();
            }

            int var4 = this.method16181();
            if (var4 >= 0 || var4 <= 8) {
                if (mc.player.getHeldItem(Hand.MAIN_HAND).getItem() == Items.BOW) {
                    if (InvManager.method16437(Items.ARROW) == 0) {
                        if (this.timer.getElapsedTime() > 5000L) {
                            Client.getInstance().notificationManager
                                    .send(new Notification("BowFly", "You have no arrows"));
                            this.timer.reset();
                        }

                        if (mc.timer.timerSpeed == 0.1F) {
                            mc.timer.timerSpeed = 1.0F;
                        }

                        return;
                    }

                    float var5 = mc.player.rotationYaw;
                    float var6 = -90.0F;
                    if (mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F) {
                        var6 = -80.0F;
                    }

                    if (mc.player.moveForward < 0.0F) {
                        var5 -= 180.0F;
                    }

                    if (mc.player.getMotion().y < -0.1) {
                        var6 = 90.0F;
                    }

                    event.setPitch(var6);
                    event.setYaw(var5);
                    if (mc.player.isOnGround() && mc.player.collidedVertically) {
                        mc.player.jump();
                    } else if (!(mc.player.getMotion().y < 0.0)) {
                        if (mc.timer.timerSpeed == 0.1F) {
                            mc.timer.timerSpeed = 1.0F;
                        }
                    } else {
                        mc.timer.timerSpeed = 0.1F;
                    }

                    this.field23504++;
                    if (this.field23504 < 4) {
                        if (this.field23504 == 1) {
                            mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        }
                    } else {
                        mc.getConnection().sendPacket(new CPlayerDiggingPacket(
                                CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                        this.field23504 = 0;
                    }
                }
            }
        }
    }

    public int method16181() {
        for (int var3 = 36; var3 < 45; var3++) {
            if (mc.player.container.getSlot(var3).getHasStack()) {
                ItemStack var4 = mc.player.container.getSlot(var3).getStack();
                if (var4.getItem() == Items.BOW) {
                    return var3 - 36;
                }
            }
        }

        for (int var5 = 9; var5 < 36; var5++) {
            if (mc.player.container.getSlot(var5).getHasStack()) {
                ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                if (var6.getItem() == Items.BOW) {
                    InvManagerUtil.clickSlot(var5, 7);
                    return 7;
                }
            }
        }

        return -1;
    }
}
