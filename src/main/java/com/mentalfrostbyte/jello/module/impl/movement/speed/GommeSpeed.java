package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class GommeSpeed extends Module {
    private int field23581;
    private int field23582;
    private double field23583;
    private float field23584;

    public GommeSpeed() {
        super(ModuleCategory.MOVEMENT, "Gomme", "Speed for GommeHD");
    }

    @Override
    public void onEnable() {
        this.field23581 = 0;
        this.field23582 = 0;
        this.field23584 = MovementUtil.getDirectionArray()[0];
    }

    @Override
    public void onDisable() {
        if (MovementUtil.isMoving()) {
            MovementUtil.setMotion(0.27, MovementUtil.getDirectionArray()[0], this.field23584, 45.0F);
        } else {
            MovementUtil.strafe(0.0);
        }
    }

    @EventTarget
    public void method16301(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SPlayerPositionLookPacket) {
                this.field23582 = 0;
            }
        }
    }

    @EventTarget
    public void method16302(EventMove event) {
        if (this.isEnabled()) {
            if (!mc.player.onGround) {
                if (this.field23583 > 0.0 && this.field23582 > 0) {
                    this.field23581++;
                    if (this.field23581 != 1) {
                        if (this.field23581 != 11) {
                            double[] var4 = new double[]{0.98, 0.98};
                            double var5 = 0.98;
                            if (this.field23582 - 1 < var4.length) {
                                var5 = var4[this.field23582 - 1];
                            }

                            this.field23583 *= var5;
                        } else {
                            double[] var7 = new double[]{0.999, 0.999};
                            double var10 = 0.98;
                            if (this.field23582 - 1 < var7.length) {
                                var10 = var7[this.field23582 - 1];
                            }

                            this.field23583 *= var10;
                        }
                    } else {
                        double[] var8 = new double[]{0.3686, 0.3688};
                        if (this.field23582 - 1 >= var8.length) {
                            this.field23583 = 0.28;
                        } else {
                            this.field23583 = var8[this.field23582 - 1];
                        }
                    }

                    if (MovementUtil.isMoving()) {
                        this.field23584 = MovementUtil.setMotion(event, Math.max(this.field23583, 0.23), MovementUtil.getDirectionArray()[0], this.field23584, 45.0F);
                    } else {
                        this.field23583 = 0.1;
                        MovementUtil.setMotion(event, 0.0);
                    }
                }
            } else if (MovementUtil.isMoving()) {
                this.field23581 = 0;
                event.setY(MovementUtil.getJumpValue());
                double[] var9 = new double[]{0.549, 0.625};
                this.field23583 = var9[Math.min(this.field23582, var9.length - 1)];
                if (this.field23582 < var9.length) {
                    this.field23582++;
                }

                MovementUtil.setMotion(event, this.field23583);
                this.field23584 = MovementUtil.getDirectionArray()[0];
            }
        }
    }
}
