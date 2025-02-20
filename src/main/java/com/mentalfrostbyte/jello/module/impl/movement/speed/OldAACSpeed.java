package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.antikb.AACAntiKB;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class OldAACSpeed extends Module {
    private double field23534;
    private float field23535;
    private int field23536;
    private int field23537;

    public OldAACSpeed() {
        super(ModuleCategory.MOVEMENT, "OldAAC", "Speed for old version of AAC");
        this.registerSetting(new BooleanSetting("Auto Jump", "Automatically jumps for you.", true));
    }

    @Override
    public void onEnable() {
        this.field23537 = Client.getInstance().playerTracker.getgroundTicks() <= 0 ? 1 : 0;
        this.field23534 = MovementUtil.getSpeed();
        this.field23535 = MovementUtil.getDirectionArray()[0];
    }

    @EventTarget
    public void onMove(EventMove event) {
        if (this.isEnabled()) {
            if (!mc.player.onGround) {
                if (MovementUtil.isMoving() && AACAntiKB.ticks >= 7) {
                    this.field23536++;
                    if (this.field23536 == 1) {
                        if (this.field23537 != 1) {
                            if (this.field23537 == 2) {
                                this.field23534 = 0.362;
                            }
                        } else {
                            this.field23534 = 0.306;
                        }
                    }

                    if (mc.player.collidedHorizontally) {
                        this.field23534 = MovementUtil.getSpeed();
                    }

                    this.field23535 = MovementUtil.setMotion(event, this.field23534, MovementUtil.getDirectionArray()[0], this.field23535, 45.0F);
                }
            } else if (this.getBooleanValueFromSettingName("Auto Jump") && MovementUtil.isMoving()) {
                this.field23536 = 0;
                mc.player.jump();
                event.setX(mc.player.getMotion().x);
                event.setY(mc.player.getMotion().y);
                event.setZ(mc.player.getMotion().z);
            } else if (event.getY() != 0.4 + (double) MovementUtil.getJumpBoost() * 0.1) {
                this.field23537 = 0;
            } else {
                MovementUtil.setMotion(event, this.field23534);
            }
        }
    }

    @EventTarget
    public void onJump(EventJump var1) {
        if (this.isEnabled()) {
            if (this.field23537 < 2) {
                this.field23537++;
            }

            if (this.field23537 != 1) {
                if (this.field23537 == 2) {
                    this.field23534 = 0.6;
                }
            } else {
                this.field23534 = 0.5;
            }

            this.field23535 = MovementUtil.getDirectionArray()[0];
            var1.setStrafeSpeed(this.field23534);
            var1.setY(0.4 + (double) MovementUtil.getJumpBoost() * 0.1);
            this.field23536 = 0;
        }
    }

    @EventTarget
    public void onPacketReceive(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SPlayerPositionLookPacket) {
                this.field23537 = 0;
                this.field23534 = MovementUtil.getSpeed();
            }
        }
    }
}
