package com.mentalfrostbyte.jello.util.client;

import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MovementHelper implements MinecraftUtil {
    public static double x;
    public static double y;
    public static double z;

    public static void applyMotion(float motionX, float motionY, float motionZ, float speed) {
        float magnitude = motionX * motionX + motionY * motionY + motionZ * motionZ;

        if (magnitude >= 1.0E-4F) {
            magnitude = MathHelper.sqrt(magnitude);
            if (magnitude < 1.0F) {
                magnitude = 1.0F;
            }

            float scale = speed / magnitude;
            motionX *= scale;
            motionY *= scale;
            motionZ *= scale;

            float yawSin = MathHelper.sin(mc.player.rotationYaw * (float) (Math.PI / 180.0));
            float yawCos = MathHelper.cos(mc.player.rotationYaw * (float) (Math.PI / 180.0));

            x += (double) (motionX * yawCos - motionZ * yawSin);
            y += (double) motionY;
            z += (double) (motionZ * yawCos + motionX * yawSin);
        }
    }

    public static boolean isMaterialInBoundingBox(AxisAlignedBB boundingBox, Material targetMaterial) {
        int minX = MathHelper.floor(boundingBox.minX);
        int maxX = MathHelper.ceil(boundingBox.maxX);
        int minY = MathHelper.floor(boundingBox.minY);
        int maxY = MathHelper.ceil(boundingBox.maxY);
        int minZ = MathHelper.floor(boundingBox.minZ);
        int maxZ = MathHelper.ceil(boundingBox.maxZ);

        if (!mc.world.isAreaLoaded(minX, minY, minZ, maxX, maxY, maxZ)) {
            return false;
        }

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockState blockState = mc.world.getBlockState(new BlockPos(x, y, z));
                    if (blockState.getMaterial() == targetMaterial) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void method34126(double var0, double var2, double var4) {
    }

    public static void jump() {
        y += 0.04F;
    }

    public static float method34128() {
        return 0.8F;
    }

    public static boolean isPlayerInWater() {
        return isMaterialInBoundingBox(mc.player.getBoundingBox().expand(0.0, -0.4F, 0.0).contract(0.001, 0.001, 0.001), Material.WATER);
    }
}
