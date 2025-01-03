package com.mentalfrostbyte.jello.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

import static com.mentalfrostbyte.jello.util.player.RotationHelper.getLookVector;

public class EntityUtil {
    private static Minecraft mc = Minecraft.getInstance();
    public static Entity getEntityFromRayTrace(float yaw, float pitch, float reachDistanceModifier, double boundingBoxExpansion) {
        EntityRayTraceResult rayTraceResult = rayTraceFromPlayer(yaw, pitch, reachDistanceModifier, boundingBoxExpansion);
        return rayTraceResult == null ? null : rayTraceResult.getEntity();
    }

    public static EntityRayTraceResult rayTraceFromPlayer(float yaw, float pitch, float reachDistanceModifier, double boundingBoxExpansion) {
        Vector3d playerEyesPos = new Vector3d(
                mc.player.getPosX(), mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ()
        );
        Entity renderViewEntity = mc.getRenderViewEntity();

        if (renderViewEntity != null && mc.world != null) {
            double reachDistance = (double) mc.playerController.getBlockReachDistance();
            if (reachDistanceModifier != 0.0F) {
                reachDistance = (double) reachDistanceModifier;
            }

            Vector3d lookVector = getLookVector(pitch, yaw);
            Vector3d rayEndPos = playerEyesPos.add(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);
            AxisAlignedBB searchBox = renderViewEntity.getBoundingBox().expand(lookVector.scale(reachDistance)).grow(1.0, 1.0, 1.0);

            return traceEntityRay(
                    mc.world, renderViewEntity, playerEyesPos, rayEndPos, searchBox,
                    entity -> entity instanceof LivingEntity || entity instanceof FallingBlockEntity,
                    (double) (reachDistanceModifier * reachDistanceModifier), boundingBoxExpansion
            );
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
            Predicate<Entity> entityFilter, double maxDistance, double boundingBoxExpansion
    ) {
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

    public static Vector3d getCenteredHitbox(Entity entity) {
        return getCenteredPosition(entity.getBoundingBox());
    }
}
