package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.EventMove;
import com.mentalfrostbyte.jello.event.impl.EventUpdate;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;

public class NoFall extends Module {
    private boolean field23507 = false;
    private boolean field23508;
    private double field23509;
    private boolean field23510;

    public NoFall() {
        super(ModuleCategory.PLAYER, "NoFall", "Avoid you from getting fall damages");
        this.registerSetting(
                new ModeSetting("Mode", "Nofall mode", 0, "Vanilla", "Hypixel", "Hypixel2", "AAC", "NCPSpigot", "OldHypixel", "Vanilla Legit")
//                        .setPremiumModes("Hypixel", "Hypixel2")
        );
    }

    @Override
    public void onEnable() {
        this.field23507 = false;
        this.field23508 = false;
        this.field23509 = 0.0;
    }

    @EventTarget
    @LowerPriority
    public void onMove(EventMove event) {
        if (this.isEnabled()) {
            if (event.getY() < -0.5
                    && (double) mc.player.fallDistance > 2.0 + (double) MovementUtil.getJumpBoost() * 0.5
                    && !mc.player.isOnGround()
                    && this.getStringSettingValueByName("Mode").equals("Hypixel")
                    /*&& MultiUtilities.isHypixel()*/) {
                double[] var4 = /*MultiUtilities.method17747()*/new double[0]; // TODO
                double var6 = Double.MAX_VALUE;

                for (double var9 : var4) {
                    double var11 = mc.player.getPosY();
                    double var13 = (double) ((int) (var11 + event.getY())) - var11 - event.getY() + var9;
                    double var15 = 0.02;
                    double var17 = -0.05;
                    // TODO: is this correct?
                    if (event.getY() > -0.5 + (double) (MovementUtil.getJumpBoost())) {
                        var15 = 0.0;
                    }

                    if (var13 > var17 && var13 < var15) {
                        AxisAlignedBB var19 = mc.player.getBoundingBox().offset(event.getX(), event.getY() + var13 + var17, event.getZ());
                        if (mc.world.getCollisionShapes(mc.player, var19).count() != 0L) {
                            var13 -= 1.0E-5;
                            event.setY(event.getY() + var13);
                            MovementUtil.setPlayerYMotion(event.getY());
                            var6 = Double.MAX_VALUE;
                            break;
                        }

                        if (Math.abs(var13) < var6) {
                            var6 = var13;
                        }
                    }
                }

                if (Math.abs(var6) < 0.1) {
                    event.setY(event.getY() + var6);
                    MovementUtil.setPlayerYMotion(event.getY());
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate packet) {
        if (this.isEnabled() && mc.player != null) {
            if (!(mc.player.getPosY() < 2.0)) {
                String var4 = this.getStringSettingValueByName("Mode");
                if (var4.equals("Hypixel")) {
                    var4 = "OldHypixel";
                }

                switch (var4) {
                    case "OldHypixel":
                        if (packet.isPre()) {
//                            if (MultiUtilities.isAboveBounds(mc.player, 1.0E-4F)) {
//                                this.field23509 = 0.0;
//                                return;
//                            }

                            if (mc.player.getMotion().y < -0.1) {
                                this.field23509 = this.field23509 - mc.player.getMotion().y;
                            }

                            if (this.field23509 > 3.0) {
                                this.field23509 = 1.0E-14;
                                packet.setGround(true);
                            }
                        }
                        break;
                    case "Hypixel":
                        if (packet.isPre() && mc.player.getMotion().y < 0.0 && !mc.player.isOnGround()/* && MultiUtilities.isHypixel()*/) {
//                            TODO
//                            for (double var10 : MovementUtil.method17747()) {
//                                if ((double) ((int) packet.getY()) - packet.getY() + var10 == 0.0) {
//                                    packet.setGround(true);
//                                    break;
//                                }
//                            }
                        }
                        break;
                    case "Hypixel2":
                        if (packet.isPre()) {
//                            if (MultiUtilities.isAboveBounds(mc.player, 1.0E-4F)) {
//                                this.field23509 = 0.0;
//                                return;
//                            }

                            if (mc.player.getMotion().y < -0.1 && mc.player.fallDistance > 3.0F) {
                                this.field23509++;
                                if (this.field23509 == 1.0) {
                                    mc.getConnection().sendPacket(new CPlayerPacket(true));
                                } else if (this.field23509 > 1.0) {
                                    this.field23509 = 0.0;
                                }
                            }
                        }
                        break;
                    case "AAC":
                        if (packet.isPre()) {
                            if (mc.player.ticksExisted == 1) {
                                this.field23507 = false;
                            }

                            if (!this.field23507 && mc.player.fallDistance > 3.0F && this.getStringSettingValueByName("Mode").equals("AAC")) {
                                this.field23507 = !this.field23507;
                                CPlayerPacket.PositionPacket var7 = new CPlayerPacket.PositionPacket(mc.player.getPosX(), Double.NaN, mc.player.getPosZ(), true);
                                mc.getConnection().sendPacket(var7);
                            }
                        }
                        break;
                    case "Vanilla":
                        if (mc.player.getMotion().y < -0.1) {
                            packet.setGround(true);
                        }
                        break;
                    case "Vanilla Legit":
                        if (mc.player.getMotion().y < -0.1) {
                            packet.setGround(true);
                        }

                        if (mc.player.fallDistance > 3.0F) {
                            this.field23510 = true;
                        }

                        if (this.field23510 && mc.player.isOnGround() && !mc.player.isInWater()) {
                            double var12 = mc.player.getPosX();
                            double var14 = mc.player.getPosY();
                            double var16 = mc.player.getPosZ();
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var12, var14 + 3.01, var16, false));
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var12, var14, var16, false));
                            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var12, var14, var16, true));
                            this.field23510 = false;
                        }
                        break;
                    case "NCPSpigot":
                        if (packet.isPre()) {
                            if (mc.player.fallDistance > 3.0F) {
                                this.field23508 = true;
                            }

                            if (this.field23508 /*&& Client.getInstance().playerTracker.getgroundTicks() == 0*/ && mc.player.isOnGround()) {
                                packet.setY(packet.getY() - 11.0);
                                this.field23508 = false;
                            }
                        }
                }
            }
        }
    }
}