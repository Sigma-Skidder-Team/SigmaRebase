package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.event.impl.*;
import com.mentalfrostbyte.jello.misc.Class116;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.item.AutoMLG;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

import java.util.Iterator;
import java.util.stream.Stream;

public class Jesus extends Module {
    public int liquidTicks;
    public int field24016;
    public int field24017;

    public Jesus() {
        super(ModuleCategory.MOVEMENT, "Jesus", "Where's the scientific proof?");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Basic", "Dolphin")/* .setPremiumModes("Dolphin") */);
        this.registerSetting(new BooleanSetting("Swim up", "Automatically swim up", true));
    }

    @Override
    public void onDisable() {
    }

    @EventTarget
    public void method16945(EventBlockCollision var1) {
        if (this.isEnabled() && mc.world != null && !AutoMLG.method16421()) {
            if (mc.world.getBlockState(var1.getBlockPos()).getMaterial() == Material.SEA_GRASS
                    || mc.world.getBlockState(var1.getBlockPos()).getMaterial() == Material.WATER
                    || mc.world.getBlockState(var1.getBlockPos()).getMaterial() == Material.LAVA) {
                if (!((double) var1.getBlockPos().getY() >= mc.player.getPosY()
                        - (double) (mc.player.getPosY() % 0.5 != 0.0 ? 0.0F : 0.5F))) {
                    if (!this.isOnLiquid(mc.player.getBoundingBox())) {
                        if (!mc.player.isSneaking()) {
                            if (!(mc.player.fallDistance > 10.0F)) {
                                BlockState var4 = mc.world.getBlockState(var1.getBlockPos());
                                int var5 = var4.getFluidState().getLevel();
                                float var6 = 0.0F;
                                if (var5 > 3) {
                                    var6++;
                                }

                                if (mc.player.fallDistance > 10.0F) {
                                    var6 -= 0.8F;
                                }

                                VoxelShape var7 = VoxelShapes.create(0.0, 0.0, 0.0, 1.0, (double) var6, 1.0);
                                var1.setBoxelShape(var7);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate var1) {
        if (this.isEnabled() && mc.world != null && var1.isPre() && mc.getCurrentServerData() != null) {
            if (isWalkingOnLiquid() && !this.isOnLiquid(mc.player.getBoundingBox())) {
                this.field24017++;
            } else {
                this.field24017 = 0;
            }

            if (isWalkingOnLiquid() && !this.isOnLiquid(mc.player.getBoundingBox())) {
                mc.player.jumpTicks = 0;
                var1.method13908(true);
                this.liquidTicks++;
                if (this.liquidTicks % 2 == 0) {
                    var1.setY(var1.getY() - 0.001);
                }
            } else {
                this.liquidTicks = !mc.player.isOnGround() ? 1 : 0;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onMove(EventMove var1) {
        if (this.isEnabled() && mc.world != null && !AutoMLG.method16421()) {
            if (this.isOnLiquid(mc.player.getBoundingBox()) && !mc.player.isSneaking()) {
                BlockState var4 = mc.world.getBlockState(mc.player.getPosition());
                if (!var4.getFluidState().isEmpty()) {
                    double var5 = var4.getFluidState().getHeight();
                    if (var5 > 0.4) {
                        if (this.getBooleanValueFromSettingName("Swim up")) {
                            var1.setY(0.13);
                        }

                        boolean onLiquid = this.isOnLiquid(mc.player.getBoundingBox().offset(0.0, var1.getY(), 0.0));
                        if (!onLiquid) { // TODO: is it correct for it to only work when swim up is active & still bob?
                            double var8 = (double) ((int) mc.player.getPosY() + 1);
                            double var10 = var8 - mc.player.getPosY();
                            var1.setY(var10);
                            mc.player.setOnGround(true);
                            this.liquidTicks = 1;
                        }
                    }
                }
            } else {
                if (isWalkingOnLiquid() && var1.getY() != -0.0784000015258789
                        && var1.getY() != MovementUtil.method37080()) {
                    var1.setY(-0.078);
                }

                if (this.getStringSettingValueByName("Mode").equals("Dolphin")) {
                    if (this.field24016 > 0) {
                        if (/* MultiUtilities.isAboveBounds(mc.player, 0.001F) */false) {
                            this.field24016 = 0;
                        } else {
                            if (mc.player.isSneaking() || mc.player.collidedVertically) {
                                this.field24016 = 0;
                                return;
                            }

                            if (this.field24016 > 0) {
                                MovementUtil.setSpeed(var1, 0.25 + (double) MovementUtil.getSpeed() * 0.05);
                                this.field24016++;
                            }

                            double var14 = this.method16954((double) this.field24016);
                            if (var14 != -999.0) {
                                mc.player.lastReportedPosY = 0.0;
                                var1.setY(var14);
                            }
                        }
                    } else if (isWalkingOnLiquid() && this.liquidTicks % 2 == 0) {
                        this.field24016++;
                        double var12 = this.method16954((double) this.field24016);
                        MovementUtil.setSpeed(var1, 0.25);
                        if (var12 != -999.0) {
                            var1.setY(var12);
                        }
                    }
                }

                MovementUtil.setPlayerYMotion(var1.getY());
            }
        }
    }

    @EventTarget
    public void onJump(JumpEvent var1) {
        if (this.isEnabled() && mc.world != null && mc.getCurrentServerData() != null) {
            if (isWalkingOnLiquid()) {
                if (this.liquidTicks % 2 != 0) {
                    var1.cancelled = true;
                }

                var1.setStrafeSpeed(0.2);
            }
        }
    }

    @EventTarget
    public void onStep(EventStep var1) {
        if (this.isEnabled() && !(var1.getHeight() < 0.2)) {
            if (isWalkingOnLiquid()) {
                var1.cancelled = true;
            }
        }
    }

    @Override
    public boolean isEnabled2() {
        return this.isEnabled() && isWalkingOnLiquid() && !this.method16950();
    }

    public boolean method16950() {
        return MovementUtil.isInWater();
    }

    public boolean isOnLiquid(AxisAlignedBB on) {
        return this.method16952(on, Material.WATER) || this.method16952(on, Material.LAVA);
    }

    public boolean method16952(AxisAlignedBB var1, Material var2) {
        int var5 = MathHelper.floor(var1.minX);
        int var6 = MathHelper.ceil(var1.maxX);
        int var7 = MathHelper.floor(var1.minY);
        int var8 = MathHelper.ceil(var1.maxY);
        int var9 = MathHelper.floor(var1.minZ);
        int var10 = MathHelper.ceil(var1.maxZ);
        Class116 var11 = Class116.fromMaterial(var2);
        return BlockPos.getAllInBox(var5, var7, var9, var6 - 1, var8 - 1, var10 - 1).anyMatch(var1x -> {
            assert mc.world != null;
            return var11.test(mc.world.getBlockState(var1x));
        });
    }

    public static boolean isWalkingOnLiquid() {
        AxisAlignedBB box = mc.player.getBoundingBox().offset(0.0, -0.001, 0.0);
        Stream<VoxelShape> collisionShapes = mc.world.getCollisionShapes(mc.player, box);
        Iterator<VoxelShape> shapeIterator = collisionShapes.iterator();
        boolean isLiquid = true;
        if (shapeIterator.hasNext()) {
            while (shapeIterator.hasNext()) {
                VoxelShape shape = shapeIterator.next();
                AxisAlignedBB bb = shape.getBoundingBox();
                BlockPos pos = new BlockPos(bb.getCenter());
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.WATER
                        && block != Blocks.LAVA
                        && block != Blocks.AIR
                        && block != Blocks.SEAGRASS
                        && block != Blocks.TALL_SEAGRASS) {
                    isLiquid = false;
                }
            }

            return isLiquid;
        } else {
            return false;
        }
    }

    public double method16954(double var1) {
        var1--;
        double[] var5 = new double[] {
                0.499,
                0.484,
                0.468,
                0.436,
                0.404,
                0.372,
                0.34,
                0.308,
                0.276,
                0.244,
                0.212,
                0.18,
                0.166,
                0.166,
                0.156,
                0.123,
                0.135,
                0.111,
                0.086,
                0.098,
                0.073,
                0.048,
                0.06,
                0.036,
                0.0106,
                0.015,
                0.0,
                0.0,
                0.0,
                -0.013,
                -0.045,
                -0.077,
                -0.109
        };
        return var1 < (double) var5.length && var1 >= 0.0 ? var5[(int) var1] : -999.0;
    }
}