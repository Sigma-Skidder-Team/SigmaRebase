package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

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
//        mc.timer.timerSpeed = 0.6F;
    }

    @Override
    public void onDisable() {
        MovementUtil.strafe(0.0);
        if (mc.player.getMotion().y > 0.0) {
            MovementUtil.setPlayerYMotion(-0.0789);
        }

//        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    @LowerPriority
    public void method16158(EventMove var1) {
        if (this.isEnabled()) {
            double var4 = Math.sqrt(var1.getX() * var1.getX() + var1.getZ() * var1.getZ());
            if (this.field23497 <= 9) {
                if (this.field23497 != -1) {
                    if (this.field23497 != 0) {
                        if (this.field23497 >= 1) {
                            MovementUtil.setSpeed(var1, var4 + 5.0E-4);
                        }
                    } else {
                        MovementUtil.setSpeed(var1, var4 + 0.0015);
                    }
                } else {
//                    var1.setY(MovementUtil.getJumpValue());
                    MovementUtil.setPlayerYMotion(var1.getY());
                    MovementUtil.setSpeed(var1, 0.125);
                }
            } else {
                MovementUtil.setSpeed(var1, 0.0);
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
            IPacket var4 = var1.getPacket();
            if (var4 instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var5 = (SPlayerPositionLookPacket) var4;
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
            IPacket var4 = var1.getPacket();
            if (var4 instanceof CPlayerPacket) {
                CPlayerPacket var5 = (CPlayerPacket) var4;
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
