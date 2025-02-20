package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class YPortSpeed extends Module {
    private boolean field23541;
    private double field23542;
    private double field23543;
    private double field23544;
    private int field23545;

    public YPortSpeed() {
        super(ModuleCategory.MOVEMENT, "YPort", "YPort speed");
        this.registerSetting(new ModeSetting("Mode", "YPort mode", 0, "NCP", "OldNCP"));
        this.registerSetting(new BooleanSetting("OnGround", "See yourself on ground", true));
    }

    @Override
    public void onEnable() {
        this.field23541 = false;
        this.field23545 = 0;
        this.field23543 = mc.player != null ? MovementUtil.getSpeed() : 0.2873;
        this.field23542 = mc.player.getPosY();
    }

    @Override
    public void onDisable() {
        this.field23541 = false;
        if (mc.player.getMotion().y > 0.33) {
            mc.player.setMotion(mc.player.getMotion().x, -0.43 + (double) MovementUtil.getJumpBoost() * 0.1, mc.player.getMotion().z);
            MovementUtil.strafe(MovementUtil.getSpeed());
        }
    }

    @EventTarget
    public void onMove(EventMove event) {
        if (this.isEnabled() && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
            if (!mc.player.isJumping) {
                String var4 = this.getStringSettingValueByName("Mode");
                switch (var4) {
                    case "NCP":
                        if (MovementUtil.isMoving() && mc.player.onGround) {
                            mc.player.jump();
                            event.setY(mc.player.getMotion().y);
                            MovementUtil.setMotion(event, 0.461);
                            this.field23541 = true;
                            mc.player.stepHeight = 0.5F;
                        } else if (this.field23541 && BlockUtil.isAboveBounds(mc.player, (float) (MovementUtil.getJumpValue() + (double) MovementUtil.getJumpBoost() * 0.1 + 0.001F))) {
                            this.field23541 = !this.field23541;
                            MovementUtil.setMotion(event, 0.312);
                            event.setY(-0.43 + (double) MovementUtil.getJumpBoost() * 0.1);

                            mc.player.setMotion(mc.player.getMotion().x, event.getY(), mc.player.getMotion().z);
                            mc.player.stepHeight = 0.0F;
                        } else if (this.field23541) {
                            event.setY(-0.1);
                            this.field23541 = !this.field23541;
                        }
                        break;
                    case "OldNCP":
                        if (mc.player.onGround && MovementUtil.isMoving()) {
                            this.field23545 = 2;
                        }

                        if (this.field23545 == 1 && MovementUtil.isMoving()) {
                            this.field23545 = 2;
                            this.field23543 = 1.38 * MovementUtil.getSpeed() - 0.01;
                        } else if (this.field23545 == 2) {
                            this.field23545 = 3;
                            double var8 = 0.401448482 + 0.002 * Math.random();
                            var8 *= 1.0 + Math.sqrt((float) MovementUtil.getJumpBoost() / 2.0F) / 2.0;
                            event.setY(var8);
                            this.field23543 *= 2.149;
                        } else if (this.field23545 == 3) {
                            this.field23545 = 4;
                            double var6 = 0.66 * (this.field23544 - MovementUtil.getSpeed());
                            this.field23543 = this.field23544 - var6;
                        } else {
                            if (mc.world.getCollisionShapes(mc.player, mc.player.boundingBox.offset(0.0, mc.player.getMotion().y, 0.0)).count() > 0L || mc.player.collidedVertically) {
                                this.field23545 = 1;
                            }

                            this.field23543 = this.field23544 - this.field23544 / 159.0;
                        }

                        this.field23543 = Math.max(this.field23543, MovementUtil.getSpeed());
                        MovementUtil.setMotion(event, this.field23543);
                        mc.player.stepHeight = 0.6F;

                        mc.player.setMotion(mc.player.getMotion().x, event.getY(), mc.player.getMotion().z);
                        break;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && mc.player != null && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
            if (mc.player.onGround && event.isPre() && ServerUtil.isHypixel()) {
                event.setY(event.getY() + 1.0E-14);
            }
        }

        if (this.isEnabled()
                && !this.getStringSettingValueByName("Mode").equalsIgnoreCase("NCP")
                && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
            if (!mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isOnLadder()) {
                if (!mc.gameSettings.keyBindJump.pressed
                        && !mc.player.isOnLadder()
                        && !mc.player.isInWater()
                        && BlockUtil.isAboveBounds(mc.player, 1.0F)
                        && !mc.player.onGround
                        && this.field23545 == 3) {

                    mc.player.setMotion(mc.player.getMotion().x, -0.3994, mc.player.getMotion().z);
                }

                double var4 = mc.player.getPosX() - mc.player.prevPosX;
                double var6 = mc.player.getPosZ() - mc.player.prevPosZ;
                this.field23544 = Math.sqrt(var4 * var4 + var6 * var6);
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2D event) {
        if (this.isEnabled()
                && BlockUtil.isAboveBounds(mc.player, 0.43F)
                && !((double) mc.player.fallDistance > 0.09)
                && this.getBooleanValueFromSettingName("OnGround")
                && !mc.gameSettings.keyBindJump.pressed
                && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
            if (mc.player.onGround && BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                this.field23542 = mc.player.getPosY();
            }

            mc.player.positionVec.y = this.field23542;
            mc.player.lastTickPosY = this.field23542;
            mc.player.chasingPosY = this.field23542;
            mc.player.prevPosY = this.field23542;
            if (MovementUtil.isMoving()) {
                mc.player.cameraYaw = 0.099999994F;
            }
        }
    }

    @EventTarget
    public void onPacketReceive(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SPlayerPositionLookPacket) {
                this.field23544 = 0.0;
            }
        }
    }
}
