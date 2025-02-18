package com.mentalfrostbyte.jello.module.impl.movement.longjump;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.LongJump;
import com.mentalfrostbyte.jello.module.impl.movement.Step;
import com.mentalfrostbyte.jello.module.impl.player.NoFall;

import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.BlockPos;
import team.sdhq.eventBus.annotations.EventTarget;

public class NCPLongJump extends Module {
    private int groundTicks;
    private int airTicks;
    private boolean jumpEventTriggered;
    private double strafeSpeed;

    public NCPLongJump() {
        super(ModuleCategory.MOVEMENT, "NCP", "Longjump for NoCheatPlus.");
        this.registerSetting(new NumberSetting<>("Boost", "Longjump boost", 3.0F, Float.class, 1.0F, 5.0F, 0.01F));
        this.registerSetting(new NumberSetting<>("Duration", "Speed duration", 10.0F, Float.class, 7.0F, 200.0F, 1.0F));
        this.registerSetting(new ModeSetting("Glide Mode", "The way you will glide", 1, "None", "Basic", "High"));
        this.registerSetting(new ModeSetting("Speed Mode", "The way you will speed", 0, "Basic", "Funcraft", "Hypixel"));
    }

    @Override
    public void onDisable() {
        this.jumpEventTriggered = false;
        mc.timer.timerSpeed = 1.0F;
        MovementUtil.moveInDirection(MovementUtil.getDumberSpeed() * 0.7);
    }

    @Override
    public void onEnable() {
        this.jumpEventTriggered = false;
        this.groundTicks = 0;
    }

    @EventTarget
    public void onMove(EventMove e) {
        if (this.isEnabled() && mc.player != null) {
            if (mc.player.isOnGround()) {
                this.airTicks = 0;
                this.groundTicks++;
                if (this.jumpEventTriggered && e.getY() != 0.599 && this.access().getBooleanValueFromSettingName("Auto Disable")) {
                    this.access().toggle();
                    MovementUtil.setMotion(e, MovementUtil.getDumberSpeed() * 0.8);
                    return;
                }

                BlockPos bp = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.4, mc.player.getPosZ());
                if (Step.updateTicksBeforeStep > 1) {
                    if (this.access().getBooleanValueFromSettingName("BorderJump") && !BlockUtil.isValidBlockPosition(bp) && this.groundTicks > 0 && MovementUtil.isMoving()) {
                        mc.player.jump();
                        e.setX(mc.player.getMotion().x);
                        e.setY(mc.player.getMotion().y);
                        e.setZ(mc.player.getMotion().z);
                    } else if (this.access().getBooleanValueFromSettingName("Auto Jump") && this.groundTicks > (this.jumpEventTriggered ? 1 : 0) && MovementUtil.isMoving()) {
                        mc.player.jump();
                        e.setX(mc.player.getMotion().x);
                        e.setY(mc.player.getMotion().y);
                        e.setZ(mc.player.getMotion().z);
                    }
                }
            } else {
                this.airTicks++;
                this.groundTicks = 0;
                if (this.jumpEventTriggered) {
                    double newStrafeSpeed = MovementUtil.getDumberSpeed() * 0.95;
                    if (this.airTicks == 1) {
                        this.strafeSpeed = (double) this.getNumberValueBySettingName("Boost") * 0.4 + (double) MovementUtil.getSpeedBoost() * 0.05;
                    } else if ((float) this.airTicks > this.getNumberValueBySettingName("Duration") + (float) MovementUtil.getSpeedBoost()) {
                        this.strafeSpeed = newStrafeSpeed;
                    } else if (this.strafeSpeed > newStrafeSpeed) {
                        String var7 = this.getStringSettingValueByName("Speed Mode");
                        switch (var7) {
                            case "Basic":
                                this.strafeSpeed *= 0.987;
                                break;
                            case "Funcraft":
                                this.strafeSpeed -= 0.0075;
                                break;
                            case "Hypixel":
                                this.strafeSpeed -= 0.0079;
                        }

                        if (this.strafeSpeed < newStrafeSpeed) {
                            this.strafeSpeed = newStrafeSpeed;
                        }
                    }

                    if (mc.player.collidedHorizontally || !MovementUtil.isMoving()) {
                        this.strafeSpeed = newStrafeSpeed;
                    }

                    MovementUtil.setMotion(e, this.strafeSpeed);
                    if (MovementUtil.getJumpBoost() == 0) {
                        String glideMode = this.getStringSettingValueByName("Glide Mode");
                        switch (glideMode) {
                            case "Basic":
                                e.setY(((LongJump) this.access()).getNCPBasicY(this.airTicks));
                                break;
                            case "High":
                                e.setY(((LongJump) this.access()).getNCPHighY(this.airTicks));
                                if (ServerUtil.isHypixel()
                                        && Client.getInstance().moduleManager.getModuleByClass(NoFall.class).isEnabled()
                                        && (this.airTicks == 8 || this.airTicks == 21)) {
                                    double absoluteY = mc.player.getPosY() + e.getY();
                                    double downY = absoluteY - (double) ((int) (absoluteY + 0.001));
                                    if (Math.abs(downY) < 0.001) {
                                        e.setY(e.getY() - downY);
                                    } else {
                                        downY = absoluteY - (double) ((int) absoluteY) - 0.25;
                                        if (Math.abs(downY) < 0.007) {
                                            e.setY(e.getY() - downY);
                                        }
                                    }
                                }
                        }
                    }
                }

                if (this.groundTicks == 1 && mc.player.getMotion().y < 0.0 && this.access().getBooleanValueFromSettingName("Auto Jump")) {
                    MovementUtil.setMotion(e, MovementUtil.getDumberSpeed() * 0.2);
                }
            }

            mc.player.setMotion(mc.player.getMotion().x, e.getY(), mc.player.getMotion().z);
        }
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (this.isEnabled() && mc.player != null) {
            this.jumpEventTriggered = true;
            this.strafeSpeed = MovementUtil.getDumberSpeed();
            event.setStrafeSpeed(this.strafeSpeed);
            event.setY(0.425 + (double) MovementUtil.getJumpBoost() * 0.1);
            if (this.getStringSettingValueByName("Glide Mode").equals("High") && MovementUtil.getJumpBoost() == 0) {
                event.setY(0.599);
                event.setStrafeSpeed(0.0);
                if ((double) this.getNumberValueBySettingName("Boost") > 1.5) {
                    event.setStrafeSpeed(0.28 + (double) this.getNumberValueBySettingName("Boost") * 0.1 + (double) MovementUtil.getSmartSpeed() * 0.05);
                }

                if (this.getStringSettingValueByName("Speed Mode").equals("Hypixel") && (double) this.getNumberValueBySettingName("Boost") > 1.75) {
                    MovementUtil.sendRandomizedPlayerPositionPackets(true);
                }

                mc.getConnection()
                        .sendPacket(
                                new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 0.425, mc.player.getPosZ(), false)
                        );
                mc.getConnection()
                        .sendPacket(
                                new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 0.425 + 0.396, mc.player.getPosZ(), false)
                        );
                mc.getConnection()
                        .sendPacket(
                                new CPlayerPacket.PositionPacket(
                                        mc.player.getPosX(), mc.player.getPosY() + 0.425 + 0.396 - 0.122, mc.player.getPosZ(), false
                                )
                        );
            }
        }
    }

//    @EventTarget
//    public void method16124(EventSafeWalk var1) {
//        if (!this.isEnabled() || !this.getStringSettingValueByName("Glide Mode").equals("High")) {
//        }
//    }
}
