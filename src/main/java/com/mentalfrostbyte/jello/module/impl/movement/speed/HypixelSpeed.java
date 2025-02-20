package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.Criticals;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.Jesus;
import com.mentalfrostbyte.jello.module.impl.movement.Step;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.world.Timer;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class HypixelSpeed extends Module {
    private int field23414;
    private double field23415;
    private double field23416;
    private double field23417;
    private Class2094 field23418 = Class2094.field13640;

    public HypixelSpeed() {
        super(ModuleCategory.MOVEMENT, "Hypixel", "Speed for Hypixel");
        this.registerSetting(new BooleanSetting("AutoJump", "Automatically jumps for you.", true));
        this.registerSetting(new BooleanSetting("Timer", "Use timer", true));
        this.registerSetting(new BooleanSetting("GroundSpeed", "Move faster on ground", true));
        this.registerSetting(new BooleanSetting("BorderJump", "Automatically jumps off edges with speed", true));
    }

    @Override
    public void onEnable() {
        this.field23415 = MovementUtil.getDumberSpeed();
        this.field23414 = 6;
        this.field23418 = Class2094.field13640;
        this.field23416 = -1.0;
        this.field23417 = 0.0;
    }

    @Override
    public void onDisable() {
        if (this.field23418 == Class2094.field13641 && mc.player.getMotion().y > 0.0 && this.field23414 == 0) {
            mc.player.setMotion(mc.player.getMotion().x, -MovementUtil.getJumpValue() - 1.0E-5 - 0.0625, mc.player.getMotion().z);
        }

        if (Math.abs((double) mc.timer.timerSpeed - 1.4123) < 0.001
                && !Client.getInstance().moduleManager.getModuleByClass(Timer.class).isEnabled()) {
            mc.timer.timerSpeed = 1.0F;
        }
    }

    @EventTarget
    @LowerPriority
    public void method16037(EventUpdateWalkingPlayer var1) {
        if (mc.player.isOnGround()) {
            if (!Client.getInstance().moduleManager.getModuleByClass(Criticals.class).isEnabled2()
                    || KillAura.targetEntity == null && KillAura.targetData == null
                    || this.field23418 != Class2094.field13641) {
                this.field23417 = 0.0;
            } else if (var1.isPre()) {
                if (this.field23417 > 3.0) {
                    this.field23417 = 0.0;
                    mc.getConnection().sendPacket(new CPlayerPacket(true));
                }

                var1.setOnGround(false);
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16038(EventMove var1) {
        if (!this.isEnabled()) {
            if (mc.player.isOnGround() || BlockUtil.isAboveBounds(mc.player, 0.001F) || mc.player.getPosY() < this.field23416) {
                this.field23416 = -1.0;
            }
        } else {
            mc.player.jumpTicks = 0;
            if (mc.player.isOnGround()) {
                this.field23416 = mc.player.getPosY();
                if (!Client.getInstance().moduleManager.getModuleByClass(Timer.class).isEnabled()) {
                    mc.timer.timerSpeed = 1.0F;
                }

                if (this.field23414 >= 0 && Step.updateTicksBeforeStep >= 2) {
                    if ((var1.getY() > 0.0 || this.getBooleanValueFromSettingName("AutoJump") && MovementUtil.isMoving()) && !PlayerUtil.inLiquid(mc.player)) {
                        mc.player.jump();
                        var1.setY(MovementUtil.getJumpValue());
                        MovementUtil.setMotion(var1, 0.644348756324588 + Math.random() * 1.0E-6 + (double) MovementUtil.getSpeedBoost() * 0.13);
                        if (this.getBooleanValueFromSettingName("Timer") && !Client.getInstance().moduleManager.getModuleByClass(Timer.class).isEnabled()) {
                            mc.timer.timerSpeed = 1.4123F;
                        }

                        this.field23414 = 0;
                        this.field23418 = Class2094.field13640;
                    } else if (MovementUtil.isMoving() && this.getBooleanValueFromSettingName("GroundSpeed") && !PlayerUtil.inLiquid(mc.player)) {
                        mc.player.stepHeight = 0.5F;
                        mc.player.jump();
                        var1.setY(0.399 + (double) MovementUtil.getJumpBoost() * 0.1 + 1.0E-14);
                        MovementUtil.setMotion(var1, 0.51 + Math.random() * 1.0E-6 + (double) MovementUtil.getSpeedBoost() * 0.098);
                        this.field23414 = 0;
                        if (this.getBooleanValueFromSettingName("Timer") && !Client.getInstance().moduleManager.getModuleByClass(Timer.class).isEnabled()) {
                            mc.timer.timerSpeed = 1.1123F;
                        }

                        this.field23418 = Class2094.field13641;
                    } else {
                        this.field23417 = 0.0;
                    }
                } else {
                    MovementUtil.setMotion(var1, 0.25);
                    if (this.field23414 < 0) {
                        this.field23414++;
                    }
                }
            } else if (this.field23414 >= 0) {
                double var4 = MovementUtil.getSmartSpeed();
                if (!Client.getInstance().moduleManager.getModuleByClass(Timer.class).isEnabled()) {
                    mc.timer.timerSpeed = 1.0F;
                }

                switch (this.field23418) {
                    case field13640:
                        if (this.field23414 == 0) {
                            this.field23415 = 0.3893478969348657 + Math.random() * 1.0E-6 + (double) MovementUtil.getSpeedBoost() * 0.077;
                        } else {
                            double var8 = 0.99375 - (double) this.field23414 * 1.0E-13;
                            this.field23415 *= var8;
                        }

                        if (MovementUtil.getJumpBoost() == 0 && !Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).isEnabled()) {
                            this.method16043(var1, this.field23414);
                        }

                        if (this.field23417 > 3.0) {
                            this.field23417 = 0.0;
                            mc.getConnection().sendPacket(new CPlayerPacket(true));
                        }
                        break;
                    case field13641:
                        if (this.field23414 == 0) {
                            double var6 = 0.399 + (double) MovementUtil.getJumpBoost() * 0.1 + 1.0E-5;
                            if (this.getBooleanValueFromSettingName("BorderJump")
                                    && mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.0, -var6 - 0.0625, 0.0)).count()
                                    == 0L) {
                                this.field23415 = 0.4103345672948576 + Math.random() * 1.0E-6 + (double) MovementUtil.getSpeedBoost() * 0.085;
                                this.field23416 = -1.0;
                            } else {
                                var1.setY(-var6 - 0.0625);
                                this.field23417 = this.field23417 - var1.getY();
                                this.field23415 = 0.3 + Math.random() * 1.0E-6 + (double) MovementUtil.getSpeedBoost() * 0.067;
                            }
                        } else if (this.field23414 == 1 && var1.getY() < 0.0) {
                            this.field23415 *= 0.7;
                        } else {
                            this.field23415 *= 0.981;
                        }
                }

                if (this.field23415 < var4 || mc.player.collidedHorizontally || !MovementUtil.isMoving() || PlayerUtil.inLiquid(mc.player)) {
                    this.field23415 = var4;
                }

                MovementUtil.setMotion(var1, this.field23415);
                this.field23414++;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void method16039(EventJump var1) {
        if (!Jesus.isWalkingOnLiquid() && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
            if (this.getBooleanValueFromSettingName("Auto Jump") || mc.player.isJumping) {
                if (this.field23414 < 0) {
                    var1.cancelled = true;
                }
            }
        }
    }

    @EventTarget
    public void method16040(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (var1.packet instanceof SPlayerPositionLookPacket) {
                this.field23414 = -2;
            }
        }
    }

    @EventTarget
    public void method16041(EventLoadWorld var1) {
        this.field23416 = -1.0;
    }

    @EventTarget
    public void method16042(EventRender2D var1) {
        if (!mc.player.isOnGround()
                && !BlockUtil.isAboveBounds(mc.player, 1.0E-4F)
                && BlockUtil.isAboveBounds(mc.player, (float) (MovementUtil.getJumpValue() + 1.0E-5 + 0.0625))
                && Step.updateTicksBeforeStep >= 2
                && !(this.field23416 < 0.0)
                && this.field23418 == Class2094.field13641
                && !(mc.player.getPosY() < this.field23416)) {
            mc.player.setPosition(mc.player.getPosX(), this.field23416, mc.player.getPosZ());
            mc.player.lastTickPosY = this.field23416;
            mc.player.chasingPosY = this.field23416;
            mc.player.prevPosY = this.field23416;
            if (MovementUtil.isMoving()) {
                mc.player.cameraYaw = 0.099999994F;
            }
        }
    }

    @Override
    public boolean isEnabled2() {
        return this.isEnabled()
                && (!mc.player.isOnGround() || mc.player.isJumping || this.getBooleanValueFromSettingName("AutoJump") || this.field23418 == Class2094.field13641);
    }

    private void method16043(EventMove var1, int var2) {
        if (var2 != 0) {
            if (var2 != 1) {
                if (var2 != 2) {
                    if (var2 == 3 && Math.abs(var1.getY()) < 0.1 && JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8)) {
                        var1.setY(0.0300011120129438);
                    }
                } else {
                    var1.setY(var1.getY() * 0.967);
                }
            } else {
                var1.setY(var1.getY() * 0.98);
            }
        } else {
            var1.setY(var1.getY() * 0.985);
        }
    }

    public void method16044() {
        this.field23414 = 0;
    }

    public enum Class2094 {
        field13640,
        field13641;
    }
}