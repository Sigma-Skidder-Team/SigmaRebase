package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.MathHelper;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class CubecraftFly extends Module {
    public int field23845;
    public boolean field23846;
    public boolean field23847;
    public boolean field23848;
    public double field23849;

    public CubecraftFly() {
        super(ModuleCategory.MOVEMENT, "Cubecraft", "Fly for Cubecraft");
        this.registerSetting(new BooleanSetting("Damage", "Allows you to go infinitly up", false));
    }

    @Override
    public void onEnable() {
        this.field23845 = 0;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            this.field23846 = false;
        } else {
            mc.gameSettings.keyBindSneak.pressed = false;
            this.field23846 = true;
        }

        if (mc.player.onGround) {
            this.field23847 = this.field23848 = false;
        }

        if (!this.getBooleanValueFromSettingName("Damage")) {
            this.field23848 = true;
        }
    }

    @Override
    public void onDisable() {
        mc.player.setMotion(mc.player.getMotion().x, -0.078, mc.player.getMotion().z);
        MovementUtil.moveInDirection(0.2);
        mc.timer.timerSpeed = 1.0F;
        if (this.field23846) {
            mc.gameSettings.keyBindSneak.pressed = true;
        }
    }

    @EventTarget
    public void method16686(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                var1.cancelled = true;
                this.field23846 = true;
            }
        }
    }

    @EventTarget
    public void method16687(EventMouseHover var1) {
        if (this.isEnabled()) {
            if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                var1.cancelled = true;
                this.field23846 = false;
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16688(EventMove var1) {
        if (!this.isEnabled()) {
            if (mc.player.onGround) {
                this.field23847 = this.field23848 = false;
            }
        } else if (ServerUtil.isCubecraft()) {
            if (this.field23845 > 0) {
                var1.setY(0.0);
                MovementUtil.setMotion(var1, 0.0);
                this.field23845++;
            } else {
                if (this.field23845 != 0) {
                    if (this.field23847) {
                        var1.setY(0.0);
                        MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed());
                        if (this.field23845 != -4) {
                            if (this.field23845 != -1) {
                                if (JelloPortal.getVersion().newerThan(ProtocolVersion.v1_8)) {
                                    if (mc.gameSettings.keyBindJump.isKeyDown() && !this.field23846) {
                                        var1.setY(0.0625);
                                        MovementUtil.setMotion(var1, 0.0);
                                        this.field23845 = 1;
                                    } else if (this.field23846 && !mc.gameSettings.keyBindJump.isKeyDown()) {
                                        var1.setY(-0.225);
                                        MovementUtil.setMotion(var1, 0.0);
                                        this.field23845 = 1;
                                    }
                                }
                            } else {
                                if (!this.field23848) {
                                    MovementUtil.sendRandomizedPlayerPositionPackets(false);
                                    this.field23848 = true;
                                }

                                this.field23845--;
                            }
                        } else if (mc.gameSettings.keyBindJump.isKeyDown() && !this.field23846) {
                            var1.setY(0.0625);
                        }
                    }

                } else if (JelloPortal.getVersion() != ProtocolVersion.v1_8) {
                    if (!BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                        var1.setY(0.0);
                        MovementUtil.setMotion(var1, 0.0);
                        if (mc.player.fallDistance > 4.0F) {
                            mc.getConnection().sendPacket(new CPlayerPacket(true));
                            this.field23848 = true;
                        }
                    } else {
                        var1.setY(MovementUtil.getJumpValue());
                        MovementUtil.setMotion(var1, 0.615);
                    }

                    this.field23845 = 1;

                } else if (!BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                    var1.setY(0.0);
                    this.field23845 = -2;
                    Client.getInstance().notificationManager
                            .send(new Notification("Cubecraft Fly", "Please start on the ground."));
                } else {
                    MovementUtil.setMotion(var1, 0.0);
                    var1.setY(0.0);
                    long var14 = MathHelper.calculateTimeBasedValue() % 90L;
                    double var16 = mc.player.getPosX();
                    double var10 = mc.player.getPosY() + 0.022;
                    double var12 = mc.player.getPosZ();
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var16, var10, var12, false));
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var16, var10, var12 + 0.07, false));
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var16, var10, var12, false));
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var16, var10 + 3.0, var12, false));
                }

                mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
            }
        } else {
            MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed());
            var1.setY(0.0);
            if (mc.player.ticksExisted % 2 == 0) {
                double var4 = mc.player.getPosX();
                double var6 = mc.player.getPosY();
                double var8 = mc.player.getPosZ();
                mc.player.setPosition(var4, var6 + 1.0E-10, var8);
            }
        }
    }

    @EventTarget
    public void method16689(EventUpdateWalkingPlayer event) {
        if (event.isPre() && ServerUtil.isCubecraft()) {
            event.setMoving(true);
            if (JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8) && this.field23845 == 0 && BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                event.cancelled = true;
            }

            if (this.field23845 >= 2) {
                event.setY(275.0);
            }
        }
    }

    @EventTarget
    public void method16690(EventReceivePacket var1) {
        if (mc.world != null && mc.getConnection() != null && ServerUtil.isCubecraft()) {
            IPacket packet = var1.packet;
            if (!(packet instanceof SPlayerPositionLookPacket)) {
                if (packet instanceof SEntityVelocityPacket var5) {
                    if (var5.getEntityID() == mc.player.getEntityId()) {
                        this.field23845 = -4;
                    }
                }
            } else {
                SPlayerPositionLookPacket var20 = (SPlayerPositionLookPacket) packet;
                if (!JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8)) {
                    float[] var6 = MovementUtil.getDirectionArray();
                    float var7 = var6[1];
                    float var8 = var6[2];
                    float var9 = var6[0];
                    double var10 = Math.cos(Math.toRadians(var9));
                    double var12 = Math.sin(Math.toRadians(var9));
                    double var14 = MovementUtil.getSpeed();
                    double var16 = ((double) var7 * var10 + (double) var8 * var12) * var14;
                    double var18 = ((double) var7 * var12 - (double) var8 * var10) * var14;
                    this.field23849 = var20.y;
                    var20.x += var16;
                    var20.z += var18;
                }

                this.field23847 = true;
                this.field23845 = -1;
            }
        }
    }
}
