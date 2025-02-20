package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.IPacket;

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
        MovementUtil.moveInDirection(0.0);
        if (mc.player.getMotion().y > 0.0) {
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        }
    }

    @EventTarget
    @LowerPriority
    public void method16052(EventMove var1) {
        if (this.isEnabled()) {
            double var4 = 0.125;
            if (this.field23424 != -1) {
                if (this.field23424 == 0) {
                    MovementUtil.setMotion(var1, 0.18);
                }
            } else {
                var1.setY(0.015);
                MovementUtil.setMotion(var1, var4);
            }

            mc.player.setMotion(var1.getX(), var1.getY(), var1.getZ());
        }
    }

    @EventTarget
    public void method16053(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.field23424++;
            if (this.field23424 == 1) {
                var1.setY(0.1);
            }

            var1.setMoving(true);
            var1.setOnGround(false);
        }
    }

    @EventTarget
    public void method16054(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
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
    public void method16055(EventRender2D var1) {
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
