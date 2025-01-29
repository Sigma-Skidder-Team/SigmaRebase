package com.mentalfrostbyte.jello.util.world;

import com.google.common.collect.ImmutableList;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import com.mentalfrostbyte.jello.util.unmapped.PlacementPattern;
import com.mentalfrostbyte.jello.util.unmapped.BlockCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import org.apache.commons.lang3.RandomUtils;
import com.mentalfrostbyte.jello.misc.Class3583;

import java.util.Collections;
import java.util.List;

import java.util.ArrayList;

public class BlockUtil {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public static int method34573(BlockState var0) {
        Block var3 = var0.getBlock();
        StateContainer<Block, BlockState> var4 = var3.getStateContainer();
        ImmutableList<BlockState> var5 = var4.getValidStates();
        return var5.indexOf(var0);
    }

    public static boolean isValidBlockPosition(BlockPos blockPos) {
        if (blockPos != null) {
            Block var3 = mc.world.getBlockState(blockPos).getBlock();
            return (var3.getDefaultState().isSolid() || !var3.getDefaultState().getMaterial().isReplaceable())
                    && (!(var3 instanceof SnowBlock) || method34573(mc.world.getBlockState(blockPos)) != 0);
        } else {
            return false;
        }
    }

    public static float[] method34581(BlockPos var0, Direction var1) {
        double var4 = (double) var0.getX() + 0.5 - mc.player.getPosX() + (double) var1.getXOffset() / 2.0;
        double var6 = (double) var0.getZ() + 0.5 - mc.player.getPosZ() + (double) var1.getZOffset() / 2.0;
        double var8 = mc.player.getPosY() + (double) mc.player.getEyeHeight() - ((double) var0.getY() + 0.5);
        double var10 = (double) MathHelper.sqrt(var4 * var4 + var6 * var6);
        float var12 = (float) (Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0F;
        float var13 = (float) (Math.atan2(var8, var10) * 180.0 / Math.PI);
        if (var12 < 0.0F) {
            var12 += 360.0F;
        }

        return new float[] { var12, var13 };
    }

    public static boolean method34535(PlayerEntity var0, BlockPos var1) {
        return method34550(var0, var1) < method34560();
    }

    public static List<BlockPos> method34545(List<BlockPos> var0) {
        var0.sort((var0x, var1) -> {
            float var4 = method34550(mc.player, var0x);
            float var5 = method34550(mc.player, var1);
            if (!(var4 > var5)) {
                return var4 != var5 ? -1 : 0;
            } else {
                return 1;
            }
        });
        return var0;
    }

    public static float method34550(Entity var0, BlockPos var1) {
        return method34553(var0, (double) var1.getX(), (double) var1.getY(), (double) var1.getZ());
    }

    public static float method34553(Entity var0, double var1, double var3, double var5) {
        float var9 = (float) (var0.getPosX() - var1);
        float var10 = (float) (var0.getPosY() - var3);
        float var11 = (float) (var0.getPosZ() - var5);
        return method34558(var9, var10, var11);
    }

    public static float method34558(float var0, float var1, float var2) {
        return MathHelper
                .sqrt((var0 - 0.5F) * (var0 - 0.5F) + (var1 - 0.5F) * (var1 - 0.5F) + (var2 - 0.5F) * (var2 - 0.5F));
    }

    public static float method34560() {
        return mc.playerController.getBlockReachDistance();
    }

    public static Direction method34580(BlockPos var0) {
        Direction var3 = Direction.UP;
        float var4 = MathHelper.wrapDegrees(method34581(var0, Direction.UP)[0]);
        if (var4 >= 45.0F && var4 <= 135.0F) {
            var3 = Direction.EAST;
        } else if ((!(var4 >= 135.0F) || !(var4 <= 180.0F)) && (!(var4 <= -135.0F) || !(var4 >= -180.0F))) {
            if (var4 <= -45.0F && var4 >= -135.0F) {
                var3 = Direction.WEST;
            } else if (var4 >= -45.0F && var4 <= 0.0F || var4 <= 45.0F && var4 >= 0.0F) {
                var3 = Direction.NORTH;
            }
        } else {
            var3 = Direction.SOUTH;
        }

        if (MathHelper.wrapDegrees(method34581(var0, Direction.UP)[1]) > 75.0F
                || MathHelper.wrapDegrees(method34581(var0, Direction.UP)[1]) < -75.0F) {
            var3 = Direction.UP;
        }

        return var3;
    }

    public static List<PlayerEntity> method34549(List<PlayerEntity> var0) {
        Collections.sort(var0, new Class3583());
        return var0;
    }

    public static BlockCache findValidBlockCache(BlockPos basePos, boolean var1) {
        Vector3i[] relativePositions = new Vector3i[] {
                new Vector3i(0, 0, 0),
                new Vector3i(-1, 0, 0),
                new Vector3i(1, 0, 0),
                new Vector3i(0, 0, 1),
                new Vector3i(0, 0, -1)
        };
        PlacementPattern[] placementPatterns = new PlacementPattern[] {
                new PlacementPattern(1, 1, 1, false),
                new PlacementPattern(2, 1, 2, false),
                new PlacementPattern(3, 1, 3, false),
                new PlacementPattern(4, 1, 4, false),
                new PlacementPattern(0, -1, 0, true)
        };

        for (PlacementPattern pattern : placementPatterns) {
            for (Vector3i offset : relativePositions) {
                Vector3i positionToCheck = !pattern.isOffset
                        ? new Vector3i(offset.getX() * pattern.offsetX, offset.getY() * pattern.offsetY,
                                offset.getZ() * pattern.offsetZ)
                        : new Vector3i(offset.getX() + pattern.offsetX, offset.getY() + pattern.offsetY,
                                offset.getZ() + pattern.offsetZ);

                for (Direction direction : Direction.values()) {
                    if ((direction != Direction.DOWN || !var1)
                            && isValidBlockPosition(basePos.add(positionToCheck).offset(direction, -1))) {
                        return new BlockCache(basePos.add(positionToCheck).offset(direction, -1), direction);
                    }
                }
            }
        }

        return null;
    }

    public static BlockRayTraceResult rayTrace(float var0, float var1, float var2) {
        Vector3d var5 = new Vector3d(
                mc.player.lastReportedPosX, mc.player.lastReportedPosY + (double) mc.player.getEyeHeight(),
                mc.player.lastReportedPosZ);
        var0 = (float) Math.toRadians((double) var0);
        var1 = (float) Math.toRadians((double) var1);
        float var6 = -MathHelper.sin(var0) * MathHelper.cos(var1);
        float var7 = -MathHelper.sin(var1);
        float var8 = MathHelper.cos(var0) * MathHelper.cos(var1);
        if (var2 == 0.0F) {
            var2 = mc.playerController.getBlockReachDistance();
        }

        Vector3d var9 = new Vector3d(
                mc.player.lastReportedPosX + (double) (var6 * var2),
                mc.player.lastReportedPosY + (double) (var7 * var2) + (double) mc.player.getEyeHeight(),
                mc.player.lastReportedPosZ + (double) (var8 * var2));
        Entity var10 = mc.getRenderViewEntity();
        return mc.world.rayTraceBlocks(new RayTraceContext(var5, var9, RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE, var10));
    }

    public static BlockRayTraceResult rayTrace(float var0, float var1, float var2, EventUpdateWalkingPlayer var3) {
        Vector3d var6 = new Vector3d(var3.getX(), (double) mc.player.getEyeHeight() + var3.getY(), var3.getZ());
        var0 = (float) Math.toRadians((double) var0);
        var1 = (float) Math.toRadians((double) var1);
        float var7 = -MathHelper.sin(var0) * MathHelper.cos(var1);
        float var8 = -MathHelper.sin(var1);
        float var9 = MathHelper.cos(var0) * MathHelper.cos(var1);
        if (var2 == 0.0F) {
            var2 = mc.playerController.getBlockReachDistance();
        }

        Vector3d var10 = new Vector3d(
                mc.player.lastReportedPosX + (double) (var7 * var2),
                mc.player.lastReportedPosY + (double) (var8 * var2) + (double) mc.player.getEyeHeight(),
                mc.player.lastReportedPosZ + (double) (var9 * var2));
        Entity var11 = mc.getRenderViewEntity();
        return mc.world.rayTraceBlocks(new RayTraceContext(var6, var10, RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE, var11));
    }

    public static float[] method34543(BlockPos var0, Direction var1) {
        float var4 = 0.0F;
        float var5 = 0.0F;
        float var6 = (float) (0.4F + Math.random() * 0.1F);
        switch (var1) {
            case EAST:
                var4 += 0.49F;
                break;
            case NORTH:
                var5 -= 0.49F;
                break;
            case SOUTH:
                var5 += 0.49F;
                break;
            case WEST:
                var4 -= 0.49F;
                break;
            case UP:
                var6 = 0.0F;
                var4 = 0.26F - (float) (Math.random() * 0.2F);
                var5 = 0.26F - (float) (Math.random() * 0.2F);
            case DOWN:
                var6 = 1.0F;
                var4 = 0.26F - (float) (Math.random() * 0.2F);
                var5 = 0.26F - (float) (Math.random() * 0.2F);
        }

        if (var4 == 0.0F) {
            var4 = (float) (0.1F - Math.sin((double) (System.currentTimeMillis() - 500L) / 1200.0) * 0.2);
        }

        if (var5 == 0.0F) {
            var5 = (float) (0.1F - Math.sin((double) (System.currentTimeMillis() - 500L) / 1000.0) * 0.2);
        }

        if (var6 == 0.0F) {
            var6 = (float) (0.6F - Math.sin((double) (System.currentTimeMillis() - 500L) / 1600.0) * 0.2);
        }

        double var7 = (double) var0.getX() + 0.5 - MinecraftClient.getInstance().player.getPosX() + (double) var4;
        double var9 = (double) var0.getY()
                - 0.02
                - (MinecraftClient.getInstance().player.getPosY()
                        + (double) MinecraftClient.getInstance().player.getEyeHeight())
                + (double) var6;
        double var11 = (double) var0.getZ() + 0.5 - MinecraftClient.getInstance().player.getPosZ() + (double) var5;
        double var13 = (double) MathHelper.sqrt(var7 * var7 + var11 * var11);
        float var15 = (float) (Math.atan2(var11, var7) * 180.0 / Math.PI) - 90.0F;
        float var16 = (float) (-(Math.atan2(var9, var13) * 180.0 / Math.PI));
        return new float[] {
                MinecraftClient.getInstance().player.rotationYaw
                        + MathHelper.wrapDegrees(var15 - MinecraftClient.getInstance().player.rotationYaw),
                MinecraftClient.getInstance().player.rotationPitch
                        + MathHelper.wrapDegrees(var16 - MinecraftClient.getInstance().player.rotationPitch)
        };
    }

    public static RayTraceResult method34569(float var0, float var1, float var2, float var3) {
        double var6 = Math.cos((double) MovementUtil.method37086() * Math.PI / 180.0) * (double) var3;
        double var8 = Math.sin((double) MovementUtil.method37086() * Math.PI / 180.0) * (double) var3;
        Vector3d var10 = new Vector3d(
                mc.player.getPosX() + var6,
                mc.player.getPosY() + (double) mc.player.getEyeHeight(),
                mc.player.getPosZ() + var8);
        var0 = (float) Math.toRadians((double) var0);
        var1 = (float) Math.toRadians((double) var1);
        float var11 = -MathHelper.sin(var0) * MathHelper.cos(var1);
        float var12 = -MathHelper.sin(var1);
        float var13 = MathHelper.cos(var0) * MathHelper.cos(var1);
        if (var2 == 0.0F) {
            var2 = mc.playerController.getBlockReachDistance();
        }

        Vector3d var14 = new Vector3d(
                mc.player.lastReportedPosX + (double) (var11 * var2),
                mc.player.lastReportedPosY + (double) (var12 * var2) + (double) mc.player.getEyeHeight(),
                mc.player.lastReportedPosZ + (double) (var13 * var2));
        Entity var15 = mc.getRenderViewEntity();
        return mc.world.rayTraceBlocks(new RayTraceContext(var10, var14, RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE, var15));
    }

    public static RayTraceResult method34570(BlockPos var0) {
        Vector3d var3 = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ());
        Vector3d var4 = new Vector3d(
                (double) var0.getX() + 0.5 + RandomUtils.nextDouble(0.01, 0.04),
                (double) var0.getY(),
                (double) var0.getZ() + 0.5 + RandomUtils.nextDouble(0.01, 0.04));
        return mc.world.rayTraceBlocks(new RayTraceContext(var3, var4, RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE, mc.getRenderViewEntity()));
    }

    public static float[] method34542(BlockPos var0, Direction var1) {
        float var4 = 0.0F;
        float var5 = 0.0F;
        float var6 = 0.0F;
        switch (var1) {
            case EAST:
                var4 += 0.49F;
                break;
            case NORTH:
                var5 -= 0.49F;
                break;
            case SOUTH:
                var5 += 0.49F;
                break;
            case WEST:
                var4 -= 0.49F;
                break;
            case UP:
                var6 += 0.0F;
            case DOWN:
                var6++;
        }

        double var7 = (double) var0.getX() + 0.5 - MinecraftClient.getInstance().player.getPosX() + (double) var4;
        double var9 = (double) var0.getY()
                - 0.02
                - (MinecraftClient.getInstance().player.getPosY()
                        + (double) MinecraftClient.getInstance().player.getEyeHeight())
                + (double) var6;
        double var11 = (double) var0.getZ() + 0.5 - MinecraftClient.getInstance().player.getPosZ() + (double) var5;
        double var13 = (double) MathHelper.sqrt(var7 * var7 + var11 * var11);
        float var15 = (float) (Math.atan2(var11, var7) * 180.0 / Math.PI) - 90.0F;
        float var16 = (float) (-(Math.atan2(var9, var13) * 180.0 / Math.PI));
        return new float[] {
                MinecraftClient.getInstance().player.rotationYaw
                        + MathHelper.wrapDegrees(var15 - MinecraftClient.getInstance().player.rotationYaw),
                MinecraftClient.getInstance().player.rotationPitch
                        + MathHelper.wrapDegrees(var16 - MinecraftClient.getInstance().player.rotationPitch)
        };
    }

    public static boolean method34538(Block var0, BlockPos var1) {
        VoxelShape var4 = var0.getDefaultState().getCollisionShape(mc.world, var1);
        return !isValidBlockPosition(var1)
                && mc.world.checkNoEntityCollision(mc.player, var4)
                && var1.getY() <= mc.player.getPosition().getY();
    }

    public static final Block getBlockFromPosition(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock();
    }

    public static float getBlockReachDistance() {
        return mc.playerController.getBlockReachDistance();
    }

    public static BlockRayTraceResult method34566(float var0) {
        Vector3d var3 = new Vector3d(mc.player.lastReportedPosX, mc.player.lastReportedPosY - 0.8F,
                mc.player.lastReportedPosZ);
        var0 = (float) Math.toRadians((double) var0);
        float var4 = 0.0F;
        float var5 = -MathHelper.sin(var0) * MathHelper.cos(var4);
        float var6 = MathHelper.cos(var0) * MathHelper.cos(var4);
        float var7 = 2.3F;
        Vector3d var8 = new Vector3d(
                mc.player.lastReportedPosX + (double) (var5 * var7),
                mc.player.lastReportedPosY - 0.8F - (double) (!mc.player.isJumping ? 0.0F : 0.6F),
                mc.player.lastReportedPosZ + (double) (var6 * var7));
        Entity var9 = mc.getRenderViewEntity();
        return mc.world.rayTraceBlocks(new RayTraceContext(var3, var8, RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE, var9));
    }

    public static float[] getRotationsToBlock() {
        BlockRayTraceResult var2 = method34566(MovementUtil.method37086() - 270.0F);
        if (var2.getType() != RayTraceResult.Type.MISS) {
            double var3 = var2.getHitVec().x - (double) var2.getPos().getX();
            double var5 = var2.getHitVec().z - (double) var2.getPos().getZ();
            double var7 = var2.getHitVec().y - (double) var2.getPos().getY();
            double var9 = (double) var2.getPos().getX() - MinecraftClient.getInstance().player.getPosX() + var3;
            double var11 = (double) var2.getPos().getY()
                    - (MinecraftClient.getInstance().player.getPosY()
                            + (double) MinecraftClient.getInstance().player.getEyeHeight())
                    + var7;
            double var13 = (double) var2.getPos().getZ() - MinecraftClient.getInstance().player.getPosZ() + var5;
            double var15 = (double) MathHelper.sqrt(var9 * var9 + var13 * var13);
            float var17 = (float) (Math.atan2(var13, var9) * 180.0 / Math.PI) - 90.0F;
            float var18 = (float) (-(Math.atan2(var11, var15) * 180.0 / Math.PI));
            return new float[] {
                    MinecraftClient.getInstance().player.rotationYaw
                            + MathHelper.wrapDegrees(var17 - MinecraftClient.getInstance().player.rotationYaw),
                    MinecraftClient.getInstance().player.rotationPitch
                            + MathHelper.wrapDegrees(var18 - MinecraftClient.getInstance().player.rotationPitch)
            };
        } else {
            return null;
        }
    }

    public static List<BlockPos> method34561(float var0) {
        ArrayList<BlockPos> var3 = new ArrayList<>();

        for (float var4 = -var0; var4 <= var0; var4++) {
            for (float var5 = -var0; var5 <= var0; var5++) {
                for (float var6 = -var0; var6 <= var0; var6++) {
                    BlockPos var7 = new BlockPos(
                            mc.player.getPosX() + (double) var5,
                            mc.player.getPosY() + (double) var4,
                            mc.player.getPosZ() + (double) var6);
                    var3.add(var7);
                }
            }
        }

        return var3;
    }

}
