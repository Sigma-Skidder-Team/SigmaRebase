package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class HorizonFly extends Module {
    private int field23497;
    private double field23498;

    public HorizonFly() {
        super(ModuleCategory.MOVEMENT, "Horizon", "A fly for Horizon anticheat");
    }

    @Override
    public void onEnable() {
        this.field23498 = mc.player.getPosY();
        this.field23497 = 10;
        mc.timer.timerSpeed = 0.6F;
    }

    @Override
    public void onDisable() {
        MovementUtil.moveInDirection(0.0);
        if (mc.player.getMotion().y > 0.0) {
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        }

        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    @LowerPriority
    public void method16158(EventMove event) {
        if (this.isEnabled()) {
            double var4 = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
            if (this.field23497 <= 9) {
                if (this.field23497 != -1) {
                    if (this.field23497 != 0) {
                        if (this.field23497 >= 1) {
                            MovementUtil.setMotion(event, var4 + 5.0E-4);
                        }
                    } else {
                        MovementUtil.setMotion(event, var4 + 0.0015);
                    }
                } else {
                    event.setY(MovementUtil.getJumpValue());
                    mc.player.setMotion(mc.player.getMotion().x, event.getY(), mc.player.getMotion().z);
                    MovementUtil.setMotion(event, 0.125);
                }
            } else {
                MovementUtil.setMotion(event, 0.0);
            }
        }
    }

    @EventTarget
    public void method16159(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.field23497++;
            if (this.field23497 != 11) {
                if (this.field23497 > 11 && this.field23497 >= 20 && this.field23497 % 20 == 0) {
                    var1.setY(0.0);
                }
            } else {
                var1.setY(0.0);
            }

            var1.setMoving(true);
        }
    }

    @EventTarget
    public void method16160(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof SPlayerPositionLookPacket var5) {
				if (this.field23497 >= 1) {
                    this.field23497 = -1;
                }

                this.field23498 = var5.getY();
                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
            }
        }
    }

    @EventTarget
    public void method16161(EventSendPacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof CPlayerPacket var5) {
				if (this.field23497 == -1) {
                    var5.onGround = true;
                }
            }
        }
    }

    @EventTarget
    public void method16162(EventRender2D var1) {
        if (this.isEnabled()) {
            double y = this.field23498;
            mc.player.setPosition(mc.player.getPosX(), y, mc.player.getPosZ());
            mc.player.lastTickPosY = y;
            mc.player.chasingPosY = y;
            mc.player.prevPosY = y;
        }
    }
}
