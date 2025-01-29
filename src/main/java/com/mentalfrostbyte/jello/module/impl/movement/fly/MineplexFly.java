package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
//import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
//import mapped.*;
//import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class MineplexFly extends Module {
    private int boostTicks;
    private int field23669;
    private int currentItem;
    private double speed;
    private double moveY;
    private double posY;
    private boolean field23674;
    private boolean failed;

    public MineplexFly() {
        super(ModuleCategory.MOVEMENT, "Mineplex", "Mineplex fly/longjump");
        this.registerSetting(new NumberSetting<Float>("Boost", "Boost value", 4.0F, Float.class, 1.0F, 8.0F, 0.01F));
        this.registerSetting(new BooleanSetting("Fake", "Simulate a real fly", false));
    }

    @Override
    public void onEnable() {
        this.boostTicks = -1;
        this.speed = MovementUtil.getSpeed();
        this.field23669 = 0;
        this.failed = false;
        this.currentItem = -1;
        this.posY = -1.0;
        this.method16461();
    }

    @Override
    public void onDisable() {
        double speed = MovementUtil.getSpeed() * 0.5;
        MovementUtil.strafe(speed);
        if (this.currentItem != -1) {
            mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            this.currentItem = mc.player.inventory.currentItem;
        }

//        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            var1.setMoving(true);
        }
    }

    @EventTarget
    public void onWorldLoad(EventLoadWorld var1) {
        if (this.isEnabled()) {
            this.posY = this.boostTicks = this.currentItem = -1;
            this.field23669 = 0;
            this.failed = false;
            this.speed = MovementUtil.getSpeed();
        }
    }

    public boolean method16456() {
        return this.isEnabled()
                && this.currentItem != -1
                && this.speed < (double) this.getNumberValueBySettingName("Boost")
                && (mc.player.isOnGround() /*|| MultiUtilities.isAboveBounds(mc.player, 0.001F)*/)
                && !this.failed;
    }

    @EventTarget
    public void onSafeWalk(EventSafeWalk var1) {
        if (this.isEnabled() && this.failed && mc.player != null) {
            if (mc.player.isOnGround()) {
                var1.setSafe(true);
            }
        }
    }

    @EventTarget
    public void onMove(EventMove var1) {
        if (this.isEnabled()) {
            if (this.failed) {
                MovementUtil.setSpeed(var1, 0.01);
            } else {
                float offsetRotationYaw = mc.player.rotationYaw + 90.0F;
                if (!mc.player.isOnGround()/* && !MultiUtilities.isAboveBounds(mc.player, 0.001F)*/) {
                    if (this.boostTicks != -1) {
                        if (this.field23674 /*&& !MultiUtilities.method17686()*/) {
                            this.field23674 = !this.field23674;
                            this.speed = 0.5;
                        }

                        this.field23669++;
                        this.speed *= 0.98;
                        this.moveY -= 0.04000000000001;
                        if (this.field23669 <= 22) {
                            if (this.field23669 == 10 && this.field23674) {
                                this.moveY = -0.005;
                            }
                        } else {
                            this.moveY -= 0.02;
                        }

                        if (this.field23669 > 6 /*&& !MultiUtilities.method17686()*/) {
                            this.moveY -= 0.05;
                        }

                        var1.setY(this.moveY);
                        if (mc.player.collidedHorizontally/* || !MultiUtilities.method17686()*/) {
                            this.speed = 0.35;
                        }

                        MovementUtil.setSpeed(var1, this.speed);
                    }
                } else {
                    if (this.field23669 > 0) {
                        MovementUtil.setSpeed(var1, 0.0);
                        this.access().toggle();
                        return;
                    }

                    if (this.boostTicks == -1) {
                        this.boostTicks = 0;
                        this.speed = 0.35;
                        return;
                    }

                    this.boostTicks++;
                    int var5 = this.method16461();
                    if (var5 == -1) {
                        return;
                    }

                    Vector3d rayHitVec = new Vector3d(0.475 + Math.random() * 0.05, 1.0, 0.475 + Math.random() * 0.05);
                    BlockPos standingOnBlock = new BlockPos(mc.player.getPosition()).add(0, -1, 0);
                    BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(rayHitVec, Direction.UP, standingOnBlock, false);
                    CPlayerTryUseItemOnBlockPacket usePacket = new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, rayTraceResult);
                    mc.getConnection().sendPacket(usePacket);
                    if (!(this.speed < (double) this.getNumberValueBySettingName("Boost"))) {
                        MovementUtil.setSpeed(var1, 0.0);
                        mc.player.jump();
                        this.moveY = 0.4299999;
                        this.field23669 = 0;
//                        this.field23674 = MultiUtilities.method17686();
                        var1.setY(this.moveY);
                        this.posY = mc.player.getPosY();
                        this.boostTicks++;
                        this.speed += 0.5;
                    } else {
//                        mc.timer.timerSpeed = Math.min(1.0F, Math.max(0.1F, (float) (1.2 - this.speed * 0.15)));
                        if (this.boostTicks > 2) {
                            this.speed += 0.05;
                        }

                        if (this.boostTicks % 2 != 0) {
                            var1.setX(Math.cos(Math.toRadians(offsetRotationYaw)) * this.speed);
                            var1.setZ(Math.sin(Math.toRadians(offsetRotationYaw)) * this.speed);
                        } else {
                            var1.setX(Math.cos(Math.toRadians(offsetRotationYaw + 180.0F)) * this.speed);
                            var1.setZ(Math.sin(Math.toRadians(offsetRotationYaw + 180.0F)) * this.speed);
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacketEvent(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (var1.getPacket() instanceof SPlayerPositionLookPacket) {
                this.failed = true;
                Client.getInstance().notificationManager
                        .send(new Notification("Mineplex fly", "Please try again"));
            }
        }
    }

    @EventTarget
    public void onSendPacketEvent(EventSendPacket var1) {
        if (this.isEnabled()) {
            if (var1.getPacket() instanceof CHeldItemChangePacket
                    && this.currentItem != -1
                    && this.speed < (double) this.getNumberValueBySettingName("Boost")
                    && (mc.player.isOnGround() /*|| MultiUtilities.isAboveBounds(mc.player, 0.001F)*/)
                    && !this.failed) {
                var1.cancelled = true;
            }
        }
    }

    private int method16461() {
        if (mc.player.getHeldItemMainhand().isEmpty()) {
            this.currentItem = mc.player.inventory.currentItem;
            return this.currentItem;
        } else {
            for (int slot = 36; slot < 45; slot++) {
                int slotId = slot - 36;
                if (mc.player.container.getSlot(slot).getStack().isEmpty()) {
                    if (mc.player.inventory.currentItem != slotId && this.currentItem != slotId) {
                        mc.getConnection().sendPacket(new CHeldItemChangePacket(slotId));
                        this.currentItem = slotId;
                    }

                    return slotId;
                }
            }

//            InvManagerUtil.fixedClick(mc.player.container.windowId, 42, 0, ClickType.QUICK_MOVE, mc.player, true);
            if (!mc.player.container.getSlot(42).getStack().isEmpty()) {
                Client.getInstance().notificationManager
                        .send(new Notification("Mineplex Fly", "Please empty a slot in your inventory"));
            } else if (mc.player.inventory.currentItem != 6 && this.currentItem != 6) {
                mc.getConnection().sendPacket(new CHeldItemChangePacket(6));
                this.currentItem = 6;
                return 6;
            }

            return -1;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Fake") && !(this.posY < 0.0)
                && !(mc.player.getPosY() < this.posY)) {
            mc.player.setPosition(mc.player.getPosX(), this.posY, mc.player.getPosZ());
            mc.player.lastTickPosY = this.posY;
            mc.player.chasingPosY = this.posY;
            mc.player.prevPosY = this.posY;
            if (MovementUtil.isMoving()) {
                mc.player.cameraYaw = 0.099999994F;
            }
        }
    }
}
