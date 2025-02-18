package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

import com.google.common.collect.ImmutableList;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {
    public static Minecraft mc = Minecraft.getInstance();
    public static BlockFlyHelper.BlockCache findValidBlockData(BlockPos var0, boolean excludedown, boolean sortMethod) {

        for (BlockPos var8 : sortBlockPositionsWithMode(
                findAccessibleBlockPositions(var0), new Vector3d(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ()), sortMethod
        )) {
            for (Direction var12 : Direction.values()) {
                if ((var12 != Direction.DOWN || !excludedown) && !isAir(var8) && isAir(var8.offset(var12, -1))) {
                    return new BlockFlyHelper.BlockCache(var8.offset(var12, -1), var12);
                }
            }
        }

        return null;
    }

    public static int method34573(BlockState var0) {
        Block var3 = var0.getBlock();
        StateContainer var4 = var3.getStateContainer();
        ImmutableList var5 = var4.getValidStates();
        return var5.indexOf(var0);
    }

    public static boolean isAir(BlockPos var0) {
        if (var0 != null) {
            Block var3 = mc.world.getBlockState(var0).getBlock();
            return !var3.getDefaultState().isSolid() && var3.getDefaultState().getMaterial().isReplaceable()
                    ? false
                    : !(var3 instanceof SnowBlock) || method34573(mc.world.getBlockState(var0)) != 0;
        } else {
            return false;
        }
    }

    public static float[] rotationToBlock(BlockPos var0, Direction var1) {
        float var4 = 0.0F;
        float var5 = 0.0F;
        float var6 = 0.0F;
        switch (var1.ordinal()) {
            case 1:
                var4 += 0.49F;
                break;
            case 2:
                var5 -= 0.49F;
                break;
            case 3:
                var5 += 0.49F;
                break;
            case 4:
                var4 -= 0.49F;
                break;
            case 5:
                var6 += 0.0F;
            case 6:
                var6++;
        }

        double var7 = (double)var0.getX() + 0.5 - Minecraft.getInstance().player.getPosX() + (double)var4;
        double var9 = (double)var0.getY()
                - 0.02
                - (Minecraft.getInstance().player.getPosY() + (double) Minecraft.getInstance().player.getEyeHeight())
                + (double)var6;
        double var11 = (double)var0.getZ() + 0.5 - Minecraft.getInstance().player.getPosZ() + (double)var5;
        double var13 = (double) MathHelper.sqrt(var7 * var7 + var11 * var11);
        float var15 = (float)(Math.atan2(var11, var7) * 180.0 / Math.PI) - 90.0F;
        float var16 = (float)(-(Math.atan2(var9, var13) * 180.0 / Math.PI));
        return new float[]{
                Minecraft.getInstance().player.rotationYaw + MathHelper.wrapAngleTo180_float(var15 - Minecraft.getInstance().player.rotationYaw),
                Minecraft.getInstance().player.rotationPitch + MathHelper.wrapAngleTo180_float(var16 - Minecraft.getInstance().player.rotationPitch)
        };
    }

    public static float getNaturalReach() {
        return mc.playerController.getBlockReachDistance();
    }


    public static List<BlockPos> findAccessibleBlockPositions(BlockPos var0) {
        ArrayList var3 = new ArrayList();
        float var4 = getNaturalReach() - 3.0F;

        for (float var5 = -var4; var5 < 1.0F; var5++) {
            for (float var6 = -var4; var6 <= var4; var6++) {
                for (float var7 = -var4; var7 <= var4; var7++) {
                    BlockPos var8 = var0.add((double)var6, (double)var5, (double)var7);
                    if (mc.player.getPosY() >= (double)var8.getY()) {
                        if(Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).getBooleanValueFromSettingName("Place All Attempts")){
                            var3.add(var8);

                        }else{

                            if(mc.player.onGround){
                                if(mc.player.getPosY() - var8.getY() == 1){
                                    var3.add(var8);
                                }

                            }else{
                                var3.add(var8);
                            }
                        }


                    }
                }
            }
        }

        return var3;
    }


    public static List<BlockPos> sortBlockPositionsWithMode(List<BlockPos> var0, Vector3d var1, boolean var2) {
        if (!var2) {
            sortBlockPosByDistance(var0, var1);
        } else {
            var0.sort((var1x, var2x) -> !(horizontalDistanceToBlockPos(var1, var1x) >= horizontalDistanceToBlockPos(var1, var2x)) ? -1 : 1);
        }

        return var0;
    }

    public static float horizontalDistanceToBlockPos(Vector3d var0, BlockPos var1) {
        return distanceToPointXZ(var0, (double)var1.getX(), (double)var1.getZ());
    }

    private static float computeDistanceToCenter(float var0, float var1) {
        return MathHelper.sqrt((var0 - 0.5F) * (var0 - 0.5F) + (var1 - 0.5F) * (var1 - 0.5F));
    }

    public static float distanceToPointXZ(Vector3d var0, double var1, double var3) {
        float var7 = (float)(var0.getX() - var1);
        float var8 = (float)(var0.getZ() - var3);
        return computeDistanceToCenter(var7, var8);
    }

    public static float getDistanceToBlockPos(Vector3d var0, BlockPos var1) {
        return calculateDistanceToPoint(var0, (double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
    }

    public static float calculateDistanceToPoint(Vector3d var0, double var1, double var3, double var5) {
        float var9 = (float)(var0.getX() - var1);
        float var10 = (float)(var0.getY() - var3);
        float var11 = (float)(var0.getZ() - var5);
        return calculateDistanceFromCenter(var9, var10, var11);
    }

    private static float calculateDistanceFromCenter(float var0, float var1, float var2) {
        return MathHelper.sqrt((var0 - 0.5F) * (var0 - 0.5F) + (var1 - 0.5F) * (var1 - 0.5F) + (var2 - 0.5F) * (var2 - 0.5F));
    }

    public static List<BlockPos> sortBlockPosByDistance(List<BlockPos> var0, Vector3d var1) {
        var0.sort((var1x, var2) -> {
            float var5 = getDistanceToBlockPos(var1, var1x);
            float var6 = getDistanceToBlockPos(var1, var2);
            if (!(var5 > var6)) {
                return var5 != var6 ? -1 : 0;
            } else {
                return 1;
            }
        });
        return var0;
    }
}
