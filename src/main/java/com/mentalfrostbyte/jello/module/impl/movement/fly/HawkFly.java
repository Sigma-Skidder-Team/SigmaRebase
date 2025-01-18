package com.mentalfrostbyte.jello.module.impl.movement.fly;

import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.ReceivePacketEvent;
import com.mentalfrostbyte.jello.event.impl.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.Render2DEvent;
import com.mentalfrostbyte.jello.event.impl.EventMove;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.network.Packet;

public class HawkFly extends Module {
    private int field23424;
    private double field23425;
    private double field23426;
    private double field23427;

    public HawkFly() {
        super(ModuleCategory.MOVEMENT, "Hawk", "A fly for Hawk anticheat");
    }

    @Override
    public void onEnable() {
        this.field23425 = mc.player.getPosX();
        this.field23426 = mc.player.getPosY();
        this.field23427 = mc.player.getPosZ();
        this.field23424 = 0;
    }

    @Override
    public void onDisable() {
        MovementUtil.strafe(0.0);
        if (mc.player.getMotion().y > 0.0) {
            MovementUtil.setPlayerYMotion(-0.0789);
        }
    }

    @EventTarget
    @LowerPriority
    public void method16052(EventMove var1) {
        if (this.isEnabled()) {
            double var4 = 0.125;
            if (this.field23424 != -1) {
                if (this.field23424 == 0) {
                    MovementUtil.setSpeed(var1, 0.18);
                }
            } else {
                var1.setY(0.015);
                MovementUtil.setSpeed(var1, var4);
            }

            MovementUtil.setPlayerYMotion(var1.getY());
            MovementUtil.setPlayerXMotion(var1.getX());
            MovementUtil.setPlayerZMotion(var1.getZ());
        }
    }

    @EventTarget
    public void method16053(EventUpdate var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.field23424++;
            if (this.field23424 == 1) {
                var1.setY(0.1);
            }

            var1.method13908(true);
            var1.setGround(false);
        }
    }

    @EventTarget
    public void method16054(ReceivePacketEvent var1) {
        if (this.isEnabled()) {
            Packet var4 = var1.getPacket();
            if (var4 instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var5 = (SPlayerPositionLookPacket) var4;
                if (this.field23424 >= 1) {
                    this.field23424 = -1;
                }

                this.field23425 = var5.getX();
                this.field23426 = var5.getY();
                this.field23427 = var5.getZ();
                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
            }
        }
    }

    @EventTarget
    public void method16055(Render2DEvent var1) {
        if (this.isEnabled()) {
            double y = this.field23426;
            double x = this.field23425;
            double z = this.field23427;
            mc.player.setPosition(mc.player.getPosX(), y, mc.player.getPosZ());
            mc.player.lastTickPosY = y;
            mc.player.chasingPosY = y;
            mc.player.prevPosY = y;
            mc.player.setPosition(x, mc.player.getPosY(), mc.player.getPosZ());
            mc.player.lastTickPosX = x;
            mc.player.chasingPosX = x;
            mc.player.prevPosX = x;
            mc.player.setPosition(mc.player.getPosY(), mc.player.getPosY(), z);
            mc.player.lastTickPosZ = z;
            mc.player.chasingPosZ = z;
            mc.player.prevPosZ = z;
        }
    }
}
