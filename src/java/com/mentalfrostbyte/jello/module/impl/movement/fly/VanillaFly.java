package com.mentalfrostbyte.jello.module.impl.movement.fly;

import net.minecraft.util.math.shapes.VoxelShape;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.MouseHoverEvent;
import com.mentalfrostbyte.jello.event.impl.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VanillaFly extends Module {
    private boolean sneakCancelled;
    private int ticksInAir;

    public VanillaFly() {
        super(ModuleCategory.MOVEMENT, "Vanilla", "Regular vanilla fly");
        this.registerSetting(new NumberSetting<Float>("Speed", "Fly speed", 4.0F, Float.class, 0.28F, 10.0F, 0.01F));
        this.registerSetting(new BooleanSetting("Kick bypass", "Bypass vanilla kick for flying", true));
    }

    @Override
    public void onEnable() {
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                this.sneakCancelled = false;
            }
        } else {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.sneakCancelled = true;
        }
    }

    @Override
    public void onDisable() {
        MovementUtil.setPlayerYMotion(-0.08);
        double plrSpeed = MovementUtil.getSpeed();
        MovementUtil.strafe(plrSpeed);
        if (this.sneakCancelled) {
            mc.gameSettings.keyBindSneak.setPressed(true);
        }
    }

    @EventTarget
    public void onKeyPress(EventKeyPress event) {
        if (this.isEnabled()) {
            if (event.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                event.cancelled = true;
                this.sneakCancelled = true;
            }
        }
    }

    @EventTarget
    public void onMouseHover(MouseHoverEvent event) {
        if (this.isEnabled()) {
            if (event.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                event.cancelled = true;
                this.sneakCancelled = false;
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (this.isEnabled()) {
            if (!mc.player.isOnGround() && this.getBooleanValueFromSettingName("Kick bypass")) {
                if (this.ticksInAir > 0 && this.ticksInAir % 30 == 0
                        /*&& !MultiUtilities.isAboveBounds(mc.player, 0.01F)*/) {
                    /*
                     * if (JelloPortal.getCurrentVersionApplied() !=
                     * ViaVerList._1_8_x.getVersionNumber()) {
                     * event.setY(event.getY() - 0.04);
                     * } else {
                     * 
                     */
                    double collisionHeight = this.getGroundCollisionHeight();
                    if (collisionHeight < 0.0) {
                        return;
                    }

                    double yPosition = event.getY();
                    List<Double> yPositions = new ArrayList<>();
                    if (!(yPosition - collisionHeight > 9.0)) {
                        mc.getConnection().sendPacket(
                                new CPlayerPacket.PositionPacket(event.getX(), collisionHeight, event.getZ(), true));
                    } else {
                        while (yPosition > collisionHeight + 9.0) {
                            yPosition -= 9.0;
                            yPositions.add(yPosition);
                            mc.getConnection().sendPacket(
                                    new CPlayerPacket.PositionPacket(event.getX(), yPosition, event.getZ(), true));
                        }

                        for (Double intermediateY : yPositions) {
                            mc.getConnection().sendPacket(
                                    new CPlayerPacket.PositionPacket(event.getX(), intermediateY, event.getZ(), true));
                        }

                        mc.getConnection().sendPacket(
                                new CPlayerPacket.PositionPacket(event.getX(), collisionHeight, event.getZ(), true));
                        Collections.reverse(yPositions);

                        for (Double intermediateYReversed : yPositions) {
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(event.getX(),
                                    intermediateYReversed, event.getZ(), true));
                        }

                        mc.getConnection().sendPacket(
                                new CPlayerPacket.PositionPacket(event.getX(), event.getY(), event.getZ(), true));
                    }

                    this.ticksInAir = 0;
                    // }
                }
            }
        }
    }

    @EventTarget
    public void onMove(EventMove event) {
        if (this.isEnabled()) {
            if (!/*MultiUtilities.isAboveBounds(mc.player, 0.01F)*/mc.player.isOnGround()) {
                this.ticksInAir++;
            } else {
                this.ticksInAir = 0;
            }

            double speed = this.getNumberValueBySettingName("Speed");
            double verticalSpeed = !mc.gameSettings.keyBindJump.isPressed() ? 0.0 : speed / 2.0;
            if (mc.gameSettings.keyBindJump.isPressed() && mc.gameSettings.keyBindSneak.isPressed()) {
                verticalSpeed = 0.0;
            } else if (!this.sneakCancelled) {
                if (mc.gameSettings.keyBindJump.isPressed()) {
                    verticalSpeed = speed / 2.0;
                }
            } else {
                verticalSpeed = -speed / 2.0;
            }

            MovementUtil.setSpeed(event, speed);
            event.setY(verticalSpeed);
            MovementUtil.setPlayerYMotion(event.getY());
        }
    }

    private double getGroundCollisionHeight() {
        if (!(mc.player.getPositionVec().y < 1.0)) {
            if (!mc.player.isOnGround()) {
                AxisAlignedBB alignedBB = mc.player.getBoundingBox().expand(0.0, -mc.player.getPositionVec().y, 0.0);
                Iterator<VoxelShape> shapeIterator = mc.world.getCollisionShapes(mc.player, alignedBB).iterator();
                double maxCollisionHeight = -1.0;

                while (shapeIterator.hasNext()) {
                    VoxelShape voxelShape = shapeIterator.next();
                    if (voxelShape.getBoundingBox().maxY > maxCollisionHeight) {
                        maxCollisionHeight = voxelShape.getBoundingBox().maxY;
                    }
                }

                return maxCollisionHeight;
            } else {
                return mc.player.getPosY();
            }
        } else {
            return -1.0;
        }
    }
}
