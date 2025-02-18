package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.AxisAlignedBB;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class NoFall extends Module {
    private boolean AACFalling = false;
    public static boolean falling = false;
    private boolean falling2;
    private double hypixel2Stage;
    private boolean vanillaLegitFalling;

    public NoFall() {
        super(ModuleCategory.PLAYER, "NoFall", "Avoid you from getting fall damages");
        this.registerSetting(
                new ModeSetting("Mode", "Nofall mode", 0, "Vanilla", "Cancel", "Hypixel", "Hypixel2", "AAC", "NCPSpigot", "OldHypixel", "Vanilla Legit", "Verus")
        );
    }

    @Override
    public void onEnable() {
        this.AACFalling = false;
        this.falling2 = false;
        this.hypixel2Stage = 0.0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        falling = false;
        mc.player.fallDistance = 0F;
    }

    @EventTarget
    @LowerPriority
    public void onMove(EventMove event) {
        if (this.isEnabled()) {
            if (event.getY() < -0.5
                    && (double) mc.player.fallDistance > 2.0 + (double) MovementUtil.getJumpBoost() * 0.5
                    && !mc.player.isOnGround()
                    && this.getStringSettingValueByName("Mode").equals("Hypixel")
                && ServerUtil.isHypixel()) {
                double[] verticalOffsets = MovementUtil.getVerticalOffsets();
                double otherYOffset = Double.MAX_VALUE;

                for (double verticalOffset : verticalOffsets) {
                    double curPosY = mc.player.getPosY();
                    double yOffset = (double) ((int) (curPosY + event.getY())) - curPosY - event.getY() + verticalOffset;
                    double var15 = 0.02;
                    double var17 = -0.05;

                    if (event.getY() > -0.5 + (double) (MovementUtil.getJumpBoost())) {
                        var15 = 0.0;
                    }

                    if (yOffset > var17 && yOffset < var15) {
                        AxisAlignedBB aabb = mc.player.getBoundingBox().offset(event.getX(), event.getY() + yOffset + var17, event.getZ());
                        if (mc.world.getCollisionShapes(mc.player, aabb).findAny().isPresent()) {
                            yOffset -= 1.0E-5;
                            event.setY(event.getY() + yOffset);
                            mc.player.setMotion(mc.player.getMotion().x, event.getY(), mc.player.getMotion().z);
                            otherYOffset = Double.MAX_VALUE;
                            break;
                        }

                        if (Math.abs(yOffset) < otherYOffset) {
                            otherYOffset = yOffset;
                        }
                    }
                }

                if (Math.abs(otherYOffset) < 0.1) {
                    event.setY(event.getY() + otherYOffset);
                    mc.player.setMotion(mc.player.getMotion().x, event.getY(), mc.player.getMotion().z);
                }
            }
        }
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onSendPacket(EventSendPacket event) {
        if (
                !this.isEnabled() ||
                        !this.getStringSettingValueByName("Mode").equals("Cancel") ||
                        event.cancelled
        ) return;
        if (event.getPacket() instanceof CPlayerPacket) {
            if (mc.player.fallDistance > 3f) {
                falling = true;
                event.cancelled = true;
                return;
            }
            falling = false;
        }
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getPacket() instanceof SPlayerPositionLookPacket packet && falling) {
            mc.getConnection().sendPacket(
                    new CPlayerPacket.PositionRotationPacket(
                            packet.getX(), packet.getY(),
                            packet.getZ(), packet.getYaw(),
                            packet.getPitch(), true
                    )
            );
            falling = false;
        }

    }

    @EventTarget
    public void onTick(EventPlayerTick eventPlayerTick) {
        if (getStringSettingValueByName("Mode").equals("Verus")) {
            // thanks @alarmingly_good
            if (!mc.player.onGround && mc.player.getMotion().y < 0 && mc.player.fallDistance > 2) {
                mc.player.onGround = true;
                mc.player.setMotion(mc.player.getMotion().x, 0.0, mc.player.getMotion().z);
                mc.player.fallDistance = 0;
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer packet) {
        if (this.isEnabled() && mc.player != null) {
            if (!(mc.player.getPosY() < 2.0)) {
                String mode = this.getStringSettingValueByName("Mode");

                switch (mode) {
                    case "OldHypixel":
                        if (packet.isPre()) {
                            if (BlockUtil.isAboveBounds(mc.player, 1.0E-4F)) {
                                this.hypixel2Stage = 0.0;
                                return;
                            }

                            if (mc.player.getMotion().y < -0.1) {
                                this.hypixel2Stage = this.hypixel2Stage - mc.player.getMotion().y;
                            }

                            if (this.hypixel2Stage > 3.0) {
                                this.hypixel2Stage = 1.0E-14;
                                packet.setOnGround(true);
                            }
                        }
                        break;
                    case "Hypixel":
                        if (packet.isPre() && mc.player.getMotion().y < 0.0 && !mc.player.isOnGround() && ServerUtil.isHypixel()) {
                            for (double verticalOffset : MovementUtil.getVerticalOffsets()) {
                                if ((double) ((int) packet.getY()) - packet.getY() + verticalOffset == 0.0) {
                                    packet.setOnGround(true);
                                    break;
                                }
                            }
                        }
                        break;
                    case "Hypixel2":
                        if (packet.isPre()) {
                            if (BlockUtil.isAboveBounds(mc.player, 1.0E-4F)) {
                                this.hypixel2Stage = 0.0;
                                return;
                            }

                            if (mc.player.getMotion().y < -0.1 && mc.player.fallDistance > 3.0F) {
                                this.hypixel2Stage++;
                                if (this.hypixel2Stage == 1.0) {
                                    mc.getConnection().sendPacket(new CPlayerPacket(true));
                                } else if (this.hypixel2Stage > 1.0) {
                                    this.hypixel2Stage = 0.0;
                                }
                            }
                        }
                        break;
                    case "AAC":
                        if (packet.isPre()) {
                            if (mc.player.ticksExisted == 1) {
                                this.AACFalling = false;
                            }

                            if (!this.AACFalling && mc.player.fallDistance > 3.0F && this.getStringSettingValueByName("Mode").equals("AAC")) {
                                this.AACFalling = !this.AACFalling;
                                CPlayerPacket.PositionPacket pos = new CPlayerPacket.PositionPacket(
                                        mc.player.getPosX(),
                                        Double.NaN,
                                        mc.player.getPosZ(),
                                        true
                                );
                                mc.getConnection().sendPacket(pos);
                            }
                        }
                        break;
                    case "Vanilla":
                        if (mc.player.getMotion().y < -0.1) {
                            packet.setOnGround(true);
                        }
                        break;
                    case "Vanilla Legit":
                        if (mc.player.getMotion().y < -0.1) {
                            packet.setOnGround(true);
                        }

                        if (mc.player.fallDistance > 3.0F) {
                            this.vanillaLegitFalling = true;
                        }

                        if (this.vanillaLegitFalling && mc.player.isOnGround() && !mc.player.isInWater()) {
                            double cX = mc.player.getPosX();
                            double cY = mc.player.getPosY();
                            double cZ = mc.player.getPosZ();
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(cX, cY + 3.01, cZ, false));
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(cX, cY, cZ, false));
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(cX, cY, cZ, true));
                            this.vanillaLegitFalling = false;
                        }
                        break;
                    case "NCPSpigot":
                        if (packet.isPre()) {
                            if (mc.player.fallDistance > 3.0F) {
                                this.falling2 = true;
                            }

                            if (this.falling2 && Client.getInstance().playerTracker.getgroundTicks() == 0 && mc.player.isOnGround()) {
                                packet.setY(packet.getY() - 11.0);
                                this.falling2 = false;
                            }
                        }
                }
            }
        }
    }
}