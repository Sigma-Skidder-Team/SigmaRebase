package com.mentalfrostbyte.jello.util.client.rotation.util;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.player.AutoSprint;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class RotationUtils {
    private static final Minecraft mc = Minecraft. getInstance();

    public static Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, float horizontalSpeed, float verticalSpeed) {
        float yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw);
        float pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch);
        return new Rotation(currentRotation.yaw + (yawDifference > horizontalSpeed ? horizontalSpeed : Math.max(yawDifference, -horizontalSpeed)), currentRotation.pitch + (pitchDifference > verticalSpeed ? verticalSpeed : Math.max(pitchDifference, -verticalSpeed)));
    }

    public static float updateRotation(float current, float calc, float maxDelta) {
        float f = MathHelper.wrapAngleTo180_float(calc - current);
        if (f > maxDelta) {
            f = maxDelta;
        }
        if (f < -maxDelta) {
            f = -maxDelta;
        }
        return current + f;
    }

    public static float[] gcdFix(float[] currentRotation, float[] lastRotation) {
        float currentYaw = currentRotation[0];
        float currentPitch = currentRotation[1];
        float lastYaw = lastRotation[0];
        float lastPitch = lastRotation[1];
        boolean nigger = Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).isEnabled()
                && (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).getStringSettingValueByName("Mode").equals("Grim")
                || Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).getStringSettingValueByName("Mode").equals("Clutch"));
        float f = (float) ((nigger ? 0 : 0.6126761)  * 0.6F + 0.2F);
        float f5 = f * f * f;

        float gcd = f * f * f * (nigger ? 8.0F : Client.getInstance().moduleManager.getModuleByClass(AutoSprint.class).getBooleanValueFromSettingName("VulcanGCD") ? 1.2F : 8.0F);

        float deltaYaw = currentYaw - lastYaw;
        float deltaPitch = currentPitch - lastPitch;

        deltaYaw -= (deltaYaw % gcd);
        deltaPitch -= (deltaPitch % gcd);

        if(nigger){
            MouseSmoother filterVolkanX = new MouseSmoother();
            MouseSmoother filterVolkanY = new MouseSmoother();
            deltaYaw = (float) filterVolkanX.smooth(deltaYaw, 3800F * gcd);
            deltaPitch = (float) filterVolkanY.smooth(deltaPitch, 3800F * gcd);
            deltaYaw *= f5;
            deltaPitch *= f5;



        }


        float finalYaw = lastYaw + deltaYaw;
        float finalPitch = lastPitch + deltaPitch;
        finalPitch = MathHelper.clamp(finalPitch, -90.0F, 90.0F);


        return new float[]{finalYaw, finalPitch};
    }

    public static float getAngleDifference(float a, float b) {
        return ((a - b) % 360.0f + 540.0f) % 360.0f - 180.0f;
    }

    public static float[] scaffoldRots(double bx, double by, double bz, float lastYaw, float lastPitch, float yawSpeed, float pitchSpeed, boolean random) {
        double x = bx - Minecraft.getInstance().player.getPosX();
        double y = by - (Minecraft.getInstance().player.getPosY() + (double)Minecraft.getInstance().player.getEyeHeight());
        double z = bz - Minecraft.getInstance().player.getPosZ();
        float calcYaw = (float)(Math.toDegrees(MathHelper.atan2(z, x)) - 90.0);
        float calcPitch = (float)(-(MathHelper.atan2(y, MathHelper.sqrt(x * x + z * z)) * 180.0 / Math.PI));
        float pitch = RotationUtils.updateRotation(lastPitch, calcPitch, pitchSpeed + RandomUtil.nextFloat(0.0f, 15.0f));
        float yaw = RotationUtils.updateRotation(lastYaw, calcYaw, yawSpeed + RandomUtil.nextFloat(0.0f, 15.0f));
        if (random) {
            yaw = (float)((double)yaw + ThreadLocalRandom.current().nextDouble(-2.0, 2.0));
            pitch = (float)((double)pitch + ThreadLocalRandom.current().nextDouble(-0.2, 0.2));
        }
        return new float[]{yaw, pitch};
    }

    public static Rotation method34148(Vector3d var0) {
        float[] var3 = method34145(Minecraft.getInstance().player.getPositionVec().add(0.0, (double) Minecraft.getInstance().player.getEyeHeight(), 0.0), var0);
        return new Rotation(var3[0], var3[1]);
    }

    public static Vector3d getEntityPosition(Entity var0) {
        return calculateBoundingBoxPosition(var0.boundingBox);
    }

    public static float wrapAngleDifference(float var0, float var1) {
        return MathHelper.wrapAngleTo180_float(-(var0 - var1));
    }

    public static Vector3d calculateBoundingBoxPosition(AxisAlignedBB var0) {
        double var3 = var0.getCenter().x;
        double var5 = var0.minY;
        double var7 = var0.getCenter().z;
        double var9 = (var0.maxY - var5) * 0.95;
        double var11 = (var0.maxX - var0.minX) * 0.95;
        double var13 = (var0.maxZ - var0.minZ) * 0.95;
        double var15 = Math.max(var5, Math.min(var5 + var9, mc.player.getPosY() + (double) mc.player.getEyeHeight()));
        double var17 = Math.max(var3 - var11 / 2.0, Math.min(var3 + var11 / 2.0, mc.player.getPosX()));
        double var19 = Math.max(var7 - var13 / 2.0, Math.min(var7 + var13 / 2.0, mc.player.getPosZ()));
        return new Vector3d(var17, var15, var19);
    }

    public static float[] method34145(Vector3d var0, Vector3d var1) {
        double var4 = var1.x - var0.x;
        double var6 = var1.z - var0.z;
        double var8 = var1.y - var0.y;
        double var10 = (double) MathHelper.sqrt(var4 * var4 + var6 * var6);
        float var12 = adjustAngle(0.0F, (float)(Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0F, 360.0F);
        float var13 = adjustAngle(Minecraft.getInstance().player.rotationPitch, (float)(-(Math.atan2(var8, var10) * 180.0 / Math.PI)), 360.0F);
        return new float[]{var12, var13};
    }

    public static float adjustAngle(float var0, float var1, float var2) {
        float var5 = MathHelper.wrapAngleTo180_float(var1 - var0);
        if (var5 > var2) {
            var5 = var2;
        }

        if (var5 < -var2) {
            var5 = -var2;
        }

        return var0 + var5;
    }

    public static float getAngleDifference2(float target, float current) {
        target %= 360.0F;
        current %= 360.0F;
        if (target < 0.0F) {
            target += 360.0F;
        }

        if (current < 0.0F) {
            current += 360.0F;
        }

        float var4 = current - target;
        return !(var4 > 180.0F) ? (!(var4 < -180.0F) ? var4 : var4 + 360.0F) : var4 - 360.0F;
    }
    public static Rotation getAdvancedRotation(Entity target, boolean noVisible) {
        Vector3d entityPosition = getEntityPosition(target);
        if (noVisible && !isHovering(entityPosition)) {
            for (int heightLevel = -1; heightLevel < 2; heightLevel++) {
                double heightAdjustment = (double)heightLevel;
                if (heightLevel != -1) {
                    heightAdjustment *= target.boundingBox.getYSize();
                } else {
                    heightAdjustment = (double)(target.getEyeHeight() - 0.02F);
                }

                double entityPosX = target.getPosX();
                double entityPosZ = target.getPosZ();
                double entityPosY = target.getPosY() + heightAdjustment + 0.05;
                double deltaX = entityPosX - mc.player.getPosX();
                double deltaY = entityPosY - (double) mc.player.getEyeHeight() - 0.02F - mc.player.getPosY();
                double deltaZ = entityPosZ - mc.player.getPosZ();
                double horizontalDistance = (double) MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                float adjustedYaw = adjustAngle(mc.player.rotationYaw, (float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0F, 360.0F);
                float adjustedPitch = adjustAngle(mc.player.rotationPitch, (float)(-(Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI)), 360.0F);
                boolean isHoveringOverEntity = isHovering(new Vector3d(entityPosX, entityPosY, entityPosZ));
                if (isHoveringOverEntity) {
                    return new Rotation(adjustedYaw, adjustedPitch);
                }

                for (int sideAdjustment = -1; sideAdjustment < 2; sideAdjustment += 2) {
                    entityPosX = target.getPosX() + (target.getPosX() - target.lastTickPosX) * (double) mc.getRenderPartialTicks();
                    entityPosZ = target.getPosZ() + (target.getPosZ() - target.lastTickPosZ) * (double) mc.getRenderPartialTicks();
                    entityPosY = target.getPosY() + 0.05 + (target.getPosY() - target.lastTickPosY) * (double) mc.getRenderPartialTicks() + heightAdjustment;
                    double adjustmentX = target.boundingBox.getXSize() / 2.5 * (double)sideAdjustment;
                    double adjustmentZ = target.boundingBox.getZSize() / 2.5 * (double)sideAdjustment;
                    if (!(mc.player.getPosX() < entityPosX + adjustmentX)) {
                        if (mc.player.getPosX() > entityPosX + adjustmentX) {
                            if (!(mc.player.getPosZ() < entityPosZ - adjustmentZ)) {
                                entityPosX += adjustmentX;
                            } else {
                                entityPosX -= adjustmentX;
                            }

                            if (!(mc.player.getPosX() > entityPosX + adjustmentX)) {
                                entityPosZ += adjustmentZ;
                            } else {
                                entityPosZ -= adjustmentZ;
                            }
                        }
                    } else {
                        if (!(mc.player.getPosZ() > entityPosZ + adjustmentZ)) {
                            entityPosX -= adjustmentX;
                        } else {
                            entityPosX += adjustmentX;
                        }

                        if (!(mc.player.getPosX() < entityPosX - adjustmentX)) {
                            entityPosZ -= adjustmentZ;
                        } else {
                            entityPosZ += adjustmentZ;
                        }
                    }

                    deltaX = entityPosX - mc.player.getPosX();
                    deltaY = entityPosY - (double) mc.player.getEyeHeight() - 0.02 - mc.player.getPosY();
                    deltaZ = entityPosZ - mc.player.getPosZ();
                    horizontalDistance = (double) MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    adjustedYaw = adjustAngle(mc.player.rotationYaw, (float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0F, 360.0F);
                    adjustedPitch = adjustAngle(mc.player.rotationPitch, (float)(-(Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI)), 360.0F);
                    isHoveringOverEntity = isHovering(new Vector3d(entityPosX, entityPosY, entityPosZ));
                    if (isHoveringOverEntity) {
                        return new Rotation(adjustedYaw, adjustedPitch);
                    }
                }
            }

            return null;
        } else {
            return method34148(entityPosition);
        }
    }

    public static EntityRayTraceResult hoveringTarget(Entity var0, float var1, float var2, Predicate<Entity> var3, double var4) {
        double var8 = var4 * var4;
        Entity var10 = null;
        Vector3d var11 = null;
        Vector3d var12 = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ()
        );
        Vector3d var13 = method17721(var2, var1);
        Vector3d var14 = var12.add(var13.x * var8, var13.y * var8, var13.z * var8);

        for (Entity var16 : mc.world
                .getEntitiesInAABBexcluding(mc.player, mc.player.getBoundingBox().expand(var13.scale(var8)).grow(1.0, 1.0, 1.0), var3)) {
            AxisAlignedBB var17 = var16.getBoundingBox();
            Optional var18 = var17.rayTrace(var12, var14);
            if (var18.isPresent()) {
                double var19 = var12.squareDistanceTo((Vector3d)var18.get());
                if (var19 < var8 && (var16 == var0 || var0 == null)) {
                    var11 = ((Vector3d)var18.get()).subtract(var16.getPosX(), var16.getPosY(), var16.getPosZ());
                    var10 = var16;
                    var8 = var19;
                }
            }
        }

        return var10 != null && var11 != null ? new EntityRayTraceResult(var10, var11) : null;
    }

    public static Entity hoveringTarget(float var0, float var1, float var2, double var3) {
        EntityRayTraceResult var7 = getrayTraceResult(var0, var1, var2, var3);
        return var7 == null ? null : var7.getEntity();
    }

    public static EntityRayTraceResult getrayTraceResult(float var0, float var1, float var2, double var3) {
        Vector3d var7 = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ()
        );
        Entity var8 = mc.getRenderViewEntity();
        if (var8 != null && mc.world != null) {
            double var9 = (double) mc.playerController.getBlockReachDistance();
            if (var2 != 0.0F) {
                var9 = (double)var2;
            }

            Vector3d var11 = method17721(var1, var0);
            Vector3d var12 = var7.add(var11.x * var9, var11.y * var9, var11.z * var9);
            float var13 = 1.0F;
            AxisAlignedBB var14 = var8.getBoundingBox().expand(var11.scale(var9)).grow(1.0, 1.0, 1.0);
            return method17713(
                    mc.world, var8, var7, var12, var14, var0x -> var0x instanceof LivingEntity || var0x instanceof FallingBlockEntity, (double)(var2 * var2), var3
            );
        } else {
            return null;
        }
    }
    public static boolean isHovering(Vector3d var0) {
        Vector3d var3 = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ()
        );
        RayTraceContext var4 = new RayTraceContext(var3, var0, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, mc.player);
        BlockRayTraceResult var5 = mc.world.rayTraceBlocks(var4);
        boolean var6 = var5.getType() == RayTraceResult.Type.MISS || var5.getType() == RayTraceResult.Type.ENTITY;
        Block var7 = mc.world.getBlockState(var5.getPos()).getBlock();
        return var6;
    }
    public static EntityRayTraceResult method17713(
            World var0, Entity var1, Vector3d var2, Vector3d var3, AxisAlignedBB var4, Predicate<Entity> var5, double var6, double var8
    ) {
        double var12 = var6;
        Entity var14 = null;

        for (Entity var16 : var0.getEntitiesInAABBexcluding(var1, var4, var5)) {
            AxisAlignedBB var17 = var16.getBoundingBox().grow(var8);
            Optional var18 = var17.rayTrace(var2, var3);
            if (!var18.isPresent()) {
                if (method17715(var1.getPositionVec(), var17)) {
                    var14 = var16;
                    break;
                }
            } else {
                double var19 = var2.squareDistanceTo((Vector3d)var18.get());
                if (var19 < var12) {
                    var14 = var16;
                    var12 = var19;
                }
            }
        }

        return var14 != null ? new EntityRayTraceResult(var14) : null;
    }

    public static boolean method17715(Vector3d var0, AxisAlignedBB var1) {
        return var0.x >= var1.minX
                && var0.x <= var1.maxX
                && var0.y >= var1.minY
                && var0.y <= var1.maxY
                && var0.z >= var1.minZ
                && var0.z <= var1.maxZ;
    }

    public static Vector3d method17721(float var0, float var1) {
        float var4 = var0 * (float) (Math.PI / 180.0);
        float var5 = -var1 * (float) (Math.PI / 180.0);
        float var6 = MathHelper.cos(var5);
        float var7 = MathHelper.sin(var5);
        float var8 = MathHelper.cos(var4);
        float var9 = MathHelper.sin(var4);
        return new Vector3d((double)(var7 * var8), (double)(-var9), (double)(var6 * var8));
    }
}

