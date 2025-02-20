package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.Client;

import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.PremiumModule;

import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class Cubecraft2Fly extends PremiumModule {
    public int field23696;
    public final TimerUtil field23697 = new TimerUtil();
    public final TimerUtil field23698 = new TimerUtil();
    public boolean field23699;

    public Cubecraft2Fly() {
        super(ModuleCategory.MOVEMENT, "Cubecraft2", "A fly for 1.9+ cubecraft");
    }

    @Override
    public void onEnable() {
        this.field23696 = 0;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            this.field23699 = false;
        } else {
            mc.gameSettings.keyBindSneak.pressed = false;
            this.field23699 = true;
        }

        if (ServerUtil.isCubecraft()/*
                                         * && JelloPortal.getCurrentVersionApplied() ==
                                         * ViaVerList._1_8_x.getVersionNumber()
                                         */) {
            Client.getInstance().notificationManager
                    .send(new Notification("Cubecraft2 fly", "This fly was made for 1.9+ only"));
        }

        this.field23698.stop();
        this.field23698.reset();
    }

    @Override
    public void onDisable() {
        MovementUtil.moveInDirection(0.2);
        mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        if (BlockUtil.isAboveBounds(mc.player, 0.001F)) {
            MovementUtil.moveInDirection(0.0);
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        } else {
            double var3 = mc.player.getPosX();
            double var5 = mc.player.getPosY();
            double var7 = mc.player.getPosZ();
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var3, -150.0, var7, false));
            MovementUtil.moveInDirection(0.0);
            mc.player.setMotion(mc.player.getMotion().x, 0.0, mc.player.getMotion().z);
            this.field23696 = -3;
            this.field23697.reset();
            this.field23697.start();
        }
    }

    @EventTarget
    public void method16483(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                var1.cancelled = true;
                this.field23699 = true;
            }
        }
    }

    @EventTarget
    public void method16484(EventMouseHover var1) {
        if (this.isEnabled()) {
            if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                var1.cancelled = true;
                this.field23699 = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void method16485(EventMove var1) {
        if (this.isEnabled()) {
            this.field23696++;
            if (this.field23696 != 1) {
                if (this.field23696 != 2) {
                    var1.setY(0.0);
                    MovementUtil.setMotion(var1, 0.0);
                } else {
                    var1.setY(-9.999999999E-5);
                    MovementUtil.setMotion(var1, 0.28);
                }
            } else {
                var1.setY(!mc.gameSettings.keyBindJump.isKeyDown() ? (!this.field23699 ? 1.0E-4 : -0.99)
                        : (!this.field23699 ? 0.99 : 1.0E-4));
                MovementUtil.setMotion(var1, !mc.gameSettings.keyBindJump.isKeyDown() ? (!this.field23699 ? 3.7 : 2.8)
                        : (!this.field23699 ? 2.8 : 3.7));
            }

            mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
        } else {
            if (this.field23696 < 0) {
                if (this.field23696 != -3) {
                    if (this.field23696 != -2) {
                        if (this.field23696 == -1) {
                            this.field23696++;
                            var1.setY(-0.4);
                            MovementUtil.setMotion(var1, 0.0);
                        }
                    } else {
                        var1.setY(0.4);
                        this.field23696++;
                        MovementUtil.setMotion(var1, 0.0);
                    }
                } else {
                    if (this.field23697.getElapsedTime() > 1000L) {
                        this.field23696++;
                        this.field23697.reset();
                        this.field23697.stop();
                    }

                    var1.setY(0.0);
                    MovementUtil.setMotion(var1, 0.0);
                }
            }
        }
    }

    @EventTarget
    public void method16486(EventUpdateWalkingPlayer var1) {
        if (this.field23696 == -3) {
            var1.cancelled = true;
        }

        if (this.isEnabled() && var1.isPre()) {
            var1.setMoving(true);
            var1.setOnGround(true);
            if (this.field23696 != 3) {
                if (this.field23696 > 3) {
                    if (this.field23698.isEnabled() && this.field23698.getElapsedTime() > 2000L) {
                        var1.setY(-150.0);
                        this.field23698.reset();
                    } else {
                        var1.cancelled = true;
                    }
                }
            } else {
                var1.setY(-150.0);
                this.field23698.start();
            }
        }
    }

    @EventTarget
    public void method16487(EventReceivePacket var1) {
        if (this.isEnabled() || this.field23696 < 0) {
            IPacket var4 = var1.packet;
            if (var4 instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var5 = (SPlayerPositionLookPacket) var4;
                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
                this.field23698.reset();
                this.field23698.stop();
                if (this.field23696 != -3) {
                    this.field23696 = 0;
                } else {
                    this.field23696++;
                }
            }
        }
    }
}
