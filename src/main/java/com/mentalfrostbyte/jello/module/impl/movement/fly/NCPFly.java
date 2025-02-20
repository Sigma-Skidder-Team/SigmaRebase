package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

public class NCPFly extends Module {
    private int field23919;
    private double field23920;

    public NCPFly() {
        super(ModuleCategory.MOVEMENT, "NCPSpigot", "A fly for NoCheatPlus");
    }

    @Override
    public void onEnable() {
        this.field23920 = mc.player.getPosY();
        this.field23919 = 2;
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
    public void method16800(EventMove var1) {
        if (this.isEnabled()) {
            if (this.field23919 <= 1) {
                if (this.field23919 != -1) {
                    if (this.field23919 != 0) {
                        if (this.field23919 == 1) {
                            var1.setY(-1.0E-7);
                            MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed());
                            mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                        }
                    } else {
                        var1.setY(-1.0E-7);
                        MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed());
                        mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                    }
                } else {
                    var1.setY(0.0);
                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                    MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed());
                }
            } else {
                MovementUtil.setMotion(var1, 0.0);
            }
        }
    }

    @EventTarget
    public void method16801(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.field23919++;
            if (this.field23919 != 3) {
                if (this.field23919 > 3 && this.field23919 >= 20 && this.field23919 % 20 == 0) {
                    var1.setY(-150.0 - Math.random() * 150.0);
                }
            } else {
                var1.setY(-150.0 - Math.random() * 150.0);
            }

            var1.setMoving(true);
        }
    }

    @EventTarget
    public void method16802(EventReceivePacket event) {
        if (this.isEnabled()) {
            IPacket packet = event.packet;
            if (packet instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var5 = (SPlayerPositionLookPacket) packet;
                if (this.field23919 >= 1) {
                    this.field23919 = -1;
                }

                this.field23920 = var5.getY();
                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
            }
        }
    }

    @EventTarget
    public void method16803(EventSendPacket event) {
        if (this.isEnabled()) {
            IPacket packet = event.packet;
            if (packet instanceof CPlayerPacket) {
                CPlayerPacket playerPacket = (CPlayerPacket) packet;
                if (this.field23919 == -1) {
                    playerPacket.onGround = true;
                }
            }
        }
    }

    @EventTarget
    public void method16804(EventRender2D var1) {
        if (this.isEnabled()) {
            double var4 = this.field23920;
            mc.player.setPosition(mc.player.getPosX(), var4, mc.player.getPosZ());
            mc.player.lastTickPosY = var4;
            mc.player.chasingPosY = var4;
            mc.player.prevPosY = var4;
        }
    }
}
