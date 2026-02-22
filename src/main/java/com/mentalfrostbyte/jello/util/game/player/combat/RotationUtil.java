package com.mentalfrostbyte.jello.util.game.player.combat;


import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static float calculate(float current, float var1, float max) {
        float wrapped = MathHelper.wrapDegrees(var1 - current);

        return current + wrapped;
    }

    public static float[] method34144(double var0, double var2, double var4) {
        double var8 = var0 - mc.player.getPosX();
        double var10 = var2 - mc.player.getPosZ();
        double var12 = var4 - mc.player.getPosY() - 1.2;
        double var14 = MathHelper.sqrt(var8 * var8 + var10 * var10);
        float var16 = (float)(Math.atan2(var10, var8) * 180.0 / Math.PI) - 90.0F;
        float var17 = (float)(-(Math.atan2(var12, var14) * 180.0 / Math.PI));
        return new float[]{var16, var17};
    }

    public static float[] getYawPitchToEntity(Entity entity, double playerX, double playerZ, double playerY) {
        // Calculate the interpolated position of the entity based on render ticks
        double interpolatedPosX = entity.getPosX() + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks();
        double interpolatedPosZ = entity.getPosZ() + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks();
        double interpolatedPosY = entity.getPosY() + (entity.getPosY() - entity.lastTickPosY) * mc.getRenderPartialTicks();

        // Calculate the differences in position
        double deltaX = interpolatedPosX - playerX;
        double deltaY = interpolatedPosY - mc.player.getEyeHeight() - 0.02F + entity.getEyeHeight() - playerY;
        double deltaZ = interpolatedPosZ - playerZ;

        // Calculate horizontal distance and yaw/pitch angles
        double horizontalDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = calculate(mc.player.rotationYaw, (float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0F, 360.0F);
        float pitch = calculate(mc.player.rotationPitch, (float)(-(Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI)), 360.0F);

        // Return the calculated yaw and pitch
        return new float[]{yaw, pitch};
    }

    public static Vector3d getLookVector(float var0, float var1) {
        float var4 = var0 * (float) (Math.PI / 180.0);
        float var5 = -var1 * (float) (Math.PI / 180.0);
        float var6 = MathHelper.cos(var5);
        float var7 = MathHelper.sin(var5);
        float var8 = MathHelper.cos(var4);
        float var9 = MathHelper.sin(var4);
        return new Vector3d(var7 * var8, -var9, var6 * var8);
    }

    public static float getWrappedAngleDifference(float angleA, float angleB) {
        float diff = Math.abs(angleA - angleB) % 360.0F;
        if (diff > 180.0F) {
            diff = 360.0F - diff;
        }
        return diff;
    }

    public static float[] rotationToPos(double x, double y, double z) {
        double dX = x - mc.player.getPosX();
        double dY = y - mc.player.getPosZ();
        double adjustedDZ = z - mc.player.getPosY() - 1.2;
        double var14 = MathHelper.sqrt(dX * dX + dY * dY);
        float yaw = (float)(Math.atan2(dY, dX) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float)(-(Math.atan2(adjustedDZ, var14) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] method34145(Vector3d var0, Vector3d var1) {
        double var4 = var1.x - var0.x;
        double var6 = var1.z - var0.z;
        double var8 = var1.y - var0.y;
        double var10 = MathHelper.sqrt(var4 * var4 + var6 * var6);
        float var12 = calculate(0.0F, (float)(Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0F, 360.0F);
        float var13 = calculate(mc.player.rotationPitch, (float)(-(Math.atan2(var8, var10) * 180.0 / Math.PI)), 360.0F);
        return new float[]{var12, var13};
    }

    public static float[] method34146(LivingEntity var0) {
        double var3 = mc.player.getPosX() + (mc.player.getPosX() - mc.player.lastTickPosX) * (double) mc.getRenderPartialTicks();
        double var5 = mc.player.getPosZ() + (mc.player.getPosZ() - mc.player.lastTickPosZ) * (double) mc.getRenderPartialTicks();
        double var7 = mc.player.getPosY() + (mc.player.getPosY() - mc.player.lastTickPosY) * (double) mc.getRenderPartialTicks();
        double var9 = var0.getPosX() + (var0.getPosX() - var0.lastTickPosX) * (double) mc.getRenderPartialTicks();
        double var11 = var0.getPosZ() + (var0.getPosZ() - var0.lastTickPosZ) * (double) mc.getRenderPartialTicks();
        double var13 = var0.getPosY() + (var0.getPosY() - var0.lastTickPosY) * (double) mc.getRenderPartialTicks();
        double var15 = (var9 - var0.lastTickPosX) * 0.4;
        double var17 = (var11 - var0.lastTickPosZ) * 0.4;
        double var19 = mc.player.getDistance(var0);
        var19 -= var19 % 0.8;
        double var21 = 1.0;
        double var23 = 1.0;
        boolean var25 = var0.isSprinting();
        var21 = var19 / 0.8 * var15 * (!var25 ? 1.0 : 1.25);
        var23 = var19 / 0.8 * var17 * (!var25 ? 1.0 : 1.25);
        double var26 = var9 + var21 - var3;
        double var28 = var11 + var23 - var5;
        double var30 = var7 + (double) mc.player.getEyeHeight() - (var13 + (double)var0.getHeight());
        double var32 = mc.player.getDistance(var0);
        float var34 = (float)Math.toDegrees(Math.atan2(var28, var26)) - 90.0F;
        double var35 = MathHelper.sqrt(var26 * var26 + var28 * var28);
        float var37 = (float)(-(Math.atan2(var30, var35) * 180.0 / Math.PI)) + (float)var32 * 0.14F;
        return new float[]{var34, -var37};
    }

    public static Rotation getRotationsToEntity(Entity entity) {
        float[] yawPitch = getYawPitchToEntity(entity, mc.player.getPosX(), mc.player.getPosZ(), mc.player.getPosY());
        return new Rotation(yawPitch[0], yawPitch[1]);
    }

    public static float getShortestYawDifference(float yaw1, float yaw2) {
        yaw1 %= 360.0F;
        yaw2 %= 360.0F;

        if (yaw1 < 0.0F) {
            yaw1 += 360.0F;
        }

        if (yaw2 < 0.0F) {
            yaw2 += 360.0F;
        }

        float difference = yaw2 - yaw1;
        return (difference > 180.0F) ? difference - 360.0F : (difference < -180.0F ? difference + 360.0F : difference);
    }
}