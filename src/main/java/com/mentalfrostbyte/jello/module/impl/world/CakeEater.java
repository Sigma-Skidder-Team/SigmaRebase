package com.mentalfrostbyte.jello.module.impl.world;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.misc.Class3593;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.fly.MineplexFly;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.player.RotationHelper;
import com.mentalfrostbyte.jello.util.player.Rots;
import net.minecraft.block.CakeBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CakeEater extends Module {
    public static BlockPos field23588;

    public CakeEater() {
        super(ModuleCategory.WORLD, "CakeEater", "Automatically eats cake");
        this.registerSetting(new BooleanSetting("No Swing", "Removes the swing animation.", true));
        this.registerSetting(new BooleanSetting("Mineplex", "Mineplex mode.", true));
    }

    // $VF: synthetic method
    public static MinecraftClient method16322() {
        return mc;
    }

    // $VF: synthetic method
    public static MinecraftClient method16323() {
        return mc;
    }

    @Override
    public void onDisable() {
        Rots.rotating = false;
        field23588 = null;
    }

    @EventTarget
    public void method16319(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (var1.getPacket() instanceof SChatPacket) {
                SChatPacket var4 = (SChatPacket) var1.getPacket();
                if (var4.getChatComponent().getString().equals("§9Game> §r§7You cannot eat your own cake!§r")) {
                    var1.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16320(EventUpdateWalkingPlayer event) {
        if (this.isEnabled()) {
            ModuleWithModuleSettings var4 = (ModuleWithModuleSettings) Client.getInstance().moduleManager
                    .getModuleByClass(Fly.class);
            if (var4.getModWithTypeSetToName() instanceof MineplexFly) {
                MineplexFly var5 = (MineplexFly) var4.getModWithTypeSetToName();
                if (var5.method16456()) {
                    return;
                }
            }

            if (!event.isPre()) {
                if (field23588 != null) {
                    if (this.getBooleanValueFromSettingName("No Swing")
                            && !this.getBooleanValueFromSettingName("Mineplex")) {
                        mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                    } else if (!this.getBooleanValueFromSettingName("No Swing")) {
                        mc.player.swingArm(Hand.MAIN_HAND);
                    }

                    BlockRayTraceResult var7 = new BlockRayTraceResult(
                            new Vector3d(
                                    (double) field23588.getX() + 0.4 + Math.random() * 0.2,
                                    (double) field23588.getY() + 0.5,
                                    (double) field23588.getZ() + 0.4 + Math.random() * 0.2),
                            Direction.UP,
                            field23588,
                            false);
                    mc.getConnection().sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, var7));
                }
            } else {
                List<BlockPos> var8 = this.method16321(
                        !this.getBooleanValueFromSettingName("Mineplex") ? mc.playerController.getBlockReachDistance()
                                : 6.0F);
                if (var8.isEmpty()) {
                    field23588 = null;
                    Rots.rotating = false;
                } else {
                    Collections.sort(var8, new Class3593(this));
                    field23588 = var8.get(0);
                    if (!this.getBooleanValueFromSettingName("Mineplex")) {
                        float[] rots = RotationHelper.method34144(
                                (double) field23588.getX() + 0.5, (double) field23588.getZ() + 0.5, field23588.getY());

                        Rots.rotating = true;
                        Rots.prevYaw = rots[0];
                        Rots.prevPitch = rots[1];
                        event.setYaw(rots[0]);
                        event.setPitch(rots[1]);
                        Rots.yaw = rots[0];
                        Rots.pitch = rots[1];

                        mc.player.rotationYawHead = event.getYaw();
                        mc.player.renderYawOffset = event.getYaw();
                    } else {
                        Rots.rotating = true;
                    }
                }
            }
        }
    }

    public List<BlockPos> method16321(float var1) {
        ArrayList<BlockPos> var4 = new ArrayList<>();

        for (float var5 = var1 + 2.0F; var5 >= -var1 + 1.0F; var5--) {
            for (float var6 = -var1; var6 <= var1; var6++) {
                for (float var7 = -var1; var7 <= var1; var7++) {
                    BlockPos var8 = new BlockPos(
                            mc.player.getPosX() + (double) var6,
                            mc.player.getPosY() + (double) var5,
                            mc.player.getPosZ() + (double) var7);
                    if (mc.world.getBlockState(var8).getBlock() instanceof CakeBlock
                            && Math.sqrt(
                                    mc.player.getDistanceSq((double) var8.getX() + 0.5, (double) var8.getY() + 0.5,
                                            (double) var8.getZ() + 0.5)) < (double) var1) {
                        var4.add(var8);
                    }
                }
            }
        }

        return var4;
    }
}