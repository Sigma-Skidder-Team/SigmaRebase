package com.mentalfrostbyte.jello.module.impl.movement.clicktp;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventClick;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;

public class SpartanClickTP extends Module {
    private int field23464;
    private BlockPos field23465;

    public SpartanClickTP() {
        super(ModuleCategory.MOVEMENT, "Spartan", "ClickTP for spartan anticheat");
    }

    @Override
    public void onEnable() {
        this.field23464 = -1;
        this.field23465 = null;
    }

    @Override
    public void onDisable() {
        mc.player.setMotion(mc.player.getMotion().x, -0.08, mc.player.getMotion().z);
        double var3 = MovementUtil.getSmartSpeed();
        MovementUtil.moveInDirection(var3);
        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    public void method16104(EventClick var1) {
        if (this.isEnabled() && (mc.player.isSneaking() || !this.access().getBooleanValueFromSettingName("Sneak"))) {
            if (var1.getButton() == EventClick.Button.RIGHT) {
                BlockRayTraceResult var4 = BlockUtil.rayTrace(
                        mc.player.rotationYaw, mc.player.rotationPitch,
                        this.access().getNumberValueBySettingName("Maximum range"));
                BlockPos var5 = null;
                if (var4 != null) {
                    var5 = var4.getPos();
                }

                if (var5 == null) {
                    return;
                }

                this.field23465 = var5;
                mc.getConnection()
                        .sendPacket(
                                new CPlayerPacket.PositionPacket(
                                        (double) this.field23465.getX() + 0.5, this.field23465.getY() + 1,
                                        (double) this.field23465.getZ() + 0.5, true));
                this.field23464 = 0;
            }
        }
    }

    @EventTarget
    public void method16105(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (var1.packet instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var4 = (SPlayerPositionLookPacket) var1.packet;
                if (var4.x == (double) this.field23465.getX() + 0.5
                        && var4.y == (double) (this.field23465.getY() + 1)
                        && var4.z == (double) this.field23465.getZ() + 0.5) {
                    Client.getInstance().notificationManager
                            .send(new Notification("ClickTP", "Successfully teleported"));
                    if (!this.access().getBooleanValueFromSettingName("Auto Disable")) {
                        this.field23464 = -1;
                        this.field23465 = null;
                        mc.player.setMotion(mc.player.getMotion().x, -0.08, mc.player.getMotion().z);
                        double var5 = MovementUtil.getSmartSpeed();
                        MovementUtil.moveInDirection(var5);
                        mc.timer.timerSpeed = 1.0F;
                    } else {
                        this.access().toggle();
                    }
                }
            }
        }
    }

    @EventTarget
    public void method16106(EventMove var1) {
        if (this.isEnabled()) {
            if (this.field23464 > -1 && this.field23465 != null) {
                var1.setY(0.01);
                this.field23464++;
                if (this.field23464 >= 20) {
                    mc.timer.timerSpeed = 1.4F;
                } else {
                    mc.timer.timerSpeed = 2.0F;
                }

                mc.getConnection()
                        .sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(),
                                mc.player.getPosZ(), true));
                mc.getConnection()
                        .sendPacket(
                                new CPlayerPacket.PositionPacket(
                                        (double) this.field23465.getX() + 0.5, this.field23465.getY() + 1,
                                        (double) this.field23465.getZ() + 0.5, true));
            }
        }
    }
}