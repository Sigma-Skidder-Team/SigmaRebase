package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class OmegaCraftTestFly extends Module {
    private int field23854;

    public OmegaCraftTestFly() {
        super(ModuleCategory.MOVEMENT, "Test", "A fly for OmegaCraft");
    }

    @Override
    public void onEnable() {
        this.field23854 = 2;
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
    public void onMove(EventMove var1) {
        if (this.isEnabled()) {
            if (this.field23854 <= 1) {
                if (this.field23854 != -1) {
                    if (this.field23854 == 0) {
                        MovementUtil.setMotion(var1, 0.1);
                    }
                } else {
                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                    MovementUtil.setMotion(var1, 1.0);
                }
            } else {
                var1.setY(0.0);
                MovementUtil.setMotion(var1, 0.0);
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && event.isPre()) {
            this.field23854++;
            if (this.field23854 != 3) {
                if (this.field23854 > 3) {
                    if (this.field23854 >= 20 && this.field23854 % 20 == 0) {
                        event.setY(0.0);
                    } else {
                        event.cancelled = true;
                    }
                }
            } else {
                event.setY(1000.0);
            }

            event.setMoving(true);
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            IPacket<?> packet = event.packet;
            if (packet instanceof SPlayerPositionLookPacket sPL) {
                if (this.field23854 >= 1) {
                    this.field23854 = -1;
                }

                sPL.yaw = mc.player.rotationYaw;
                sPL.pitch = mc.player.rotationPitch;
            }
        }
    }

    @EventTarget
    public void method16703(EventSendPacket event) {
        if (this.isEnabled()) {
            IPacket<?> packet = event.packet;
            if (packet instanceof CPlayerPacket playerPacket) {
                if (this.field23854 == -1) {
                    playerPacket.onGround = true;
                }
            }
        }
    }
}
