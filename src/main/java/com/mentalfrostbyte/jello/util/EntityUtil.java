package com.mentalfrostbyte.jello.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.mentalfrostbyte.jello.util.player.RotationHelper.getLookVector;

public class EntityUtil {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void swing(Entity target, boolean swing) {
        if (target == null) {
            return;
        }

        boolean isOnePointEight = false;

        if (isOnePointEight && swing) {
            mc.player.swingArm(Hand.MAIN_HAND);
        }

        mc.getConnection().getNetworkManager().sendNoEventPacket(new CUseEntityPacket(target, mc.player.isSneaking()));

        boolean canSwing = (double) mc.player.getCooledAttackStrength(0.5F) > 0.9 || isOnePointEight;

        mc.player.resetCooldown();
        if (!isOnePointEight && swing) {
            mc.player.swingArm(Hand.MAIN_HAND);
        }

        mc.playerController.attackEntity(mc.player, target);
    }

    public static Entity getEntityFromRayTrace(float yaw, float pitch, float reachDistanceModifier,
            double boundingBoxExpansion) {
        EntityRayTraceResult rayTraceResult = rayTraceFromPlayer(yaw, pitch, reachDistanceModifier,
                boundingBoxExpansion);
        return rayTraceResult == null ? null : rayTraceResult.getEntity();
    }

    public static EntityRayTraceResult method17714(Entity var0, float var1, float var2, Predicate<Entity> var3,
            double var4) {
        double var8 = var4 * var4;
        Entity var10 = null;
        Vector3d var11 = null;
        Vector3d var12 = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ());
        Vector3d var13 = getLookVector(var2, var1);
        Vector3d var14 = var12.add(var13.x * var8, var13.y * var8, var13.z * var8);

        assert mc.world != null;
        for (Entity var16 : mc.world
                .getEntitiesInAABBexcluding(mc.player,
                        mc.player.getBoundingBox().expand(var13.scale(var8)).grow(1.0, 1.0, 1.0), var3)) {
            AxisAlignedBB var17 = var16.getBoundingBox();
            Optional<Vector3d> var18 = var17.rayTrace(var12, var14);
            if (var18.isPresent()) {
                double var19 = var12.squareDistanceTo(var18.get());
                if (var19 < var8 && (var16 == var0 || var0 == null)) {
                    var11 = var18.get().subtract(var16.getPosX(), var16.getPosY(), var16.getPosZ());
                    var10 = var16;
                    var8 = var19;
                }
            }
        }

        return var10 != null && var11 != null ? new EntityRayTraceResult(var10, var11) : null;
    }

    public static <T extends Entity> List<T> getEntitesInWorld(Predicate<T> filter) {
        return StreamSupport.stream(mc.world.getAllEntities().spliterator(), true)
                .filter((Predicate<Entity>) filter).map(entity -> (T) entity).toList();
    }

    public static EntityRayTraceResult rayTraceFromPlayer(float yaw, float pitch, float reachDistanceModifier,
            double boundingBoxExpansion) {
        Vector3d playerEyesPos = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ());
        Entity renderViewEntity = mc.getRenderViewEntity();

        if (renderViewEntity != null && mc.world != null) {
            double reachDistance = (double) mc.playerController.getBlockReachDistance();
            if (reachDistanceModifier != 0.0F) {
                reachDistance = (double) reachDistanceModifier;
            }

            Vector3d lookVector = getLookVector(pitch, yaw);
            Vector3d rayEndPos = playerEyesPos.add(lookVector.x * reachDistance, lookVector.y * reachDistance,
                    lookVector.z * reachDistance);
            AxisAlignedBB searchBox = renderViewEntity.getBoundingBox().expand(lookVector.scale(reachDistance))
                    .grow(1.0, 1.0, 1.0);

            return traceEntityRay(
                    mc.world, renderViewEntity, playerEyesPos, rayEndPos, searchBox,
                    entity -> entity instanceof LivingEntity || entity instanceof FallingBlockEntity,
                    (double) (reachDistanceModifier * reachDistanceModifier), boundingBoxExpansion);
        } else {
            return null;
        }
    }

    public static boolean isVecWithinBox(Vector3d vec, AxisAlignedBB box) {
        return vec.x >= box.minX
                && vec.x <= box.maxX
                && vec.y >= box.minY
                && vec.y <= box.maxY
                && vec.z >= box.minZ
                && vec.z <= box.maxZ;
    }

    public static EntityRayTraceResult traceEntityRay(
            World world, Entity sourceEntity, Vector3d startPos, Vector3d endPos, AxisAlignedBB searchBox,
            Predicate<Entity> entityFilter, double maxDistance, double boundingBoxExpansion) {
        double closestDistance = maxDistance;
        Entity closestEntity = null;

        for (Entity entity : world.getEntitiesInAABBexcluding(sourceEntity, searchBox, entityFilter)) {
            AxisAlignedBB expandedBox = entity.getBoundingBox().grow(boundingBoxExpansion);
            Optional<Vector3d> hitResult = expandedBox.rayTrace(startPos, endPos);

            if (!hitResult.isPresent()) {
                if (isVecWithinBox(sourceEntity.getPositionVec(), expandedBox)) {
                    closestEntity = entity;
                    break;
                }
            } else {
                double distanceToHit = startPos.squareDistanceTo(hitResult.get());
                if (distanceToHit < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distanceToHit;
                }
            }
        }

        return closestEntity != null ? new EntityRayTraceResult(closestEntity) : null;
    }

    public static Vector3d getCenteredPosition(AxisAlignedBB var0) {
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

    public static boolean rayTraceEntity(PlayerEntity player, Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();

        Vector3d playerEyesPos = player.getEyePosition(1.0F);
        Vector3d lookDirection = player.getLook(1.0F);

        double reachDistance = mc.playerController.getBlockReachDistance();
        Vector3d endPos = playerEyesPos.add(lookDirection.x * reachDistance, lookDirection.y * reachDistance,
                lookDirection.z * reachDistance);

        AxisAlignedBB entityBoundingBox = entity.getBoundingBox().grow(0.3D);

        RayTraceContext context = new RayTraceContext(
                playerEyesPos,
                endPos,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                player);
        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(context);

        if (rayTraceResult.getType() == RayTraceResult.Type.MISS) {
            Optional<Vector3d> hitResult = entityBoundingBox.rayTrace(playerEyesPos, endPos);

            return hitResult.isPresent();
        }

        return false;
    }

    public static Vector3d getCenteredHitbox(Entity entity) {
        return getCenteredPosition(entity.getBoundingBox());
    }
}
