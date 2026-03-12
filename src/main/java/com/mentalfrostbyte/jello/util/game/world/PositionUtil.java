package com.mentalfrostbyte.jello.util.game.world;

import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class PositionUtil implements MinecraftUtil {
    public static double calculateDistanceSquared(Entity entity) {
        double deltaX = getEntityPosition(mc.player).x - getEntityPosition(entity).x;
        double deltaY = getEntityPosition(mc.player).y - getEntityPosition(entity).y;
        double deltaZ = getEntityPosition(mc.player).z - getEntityPosition(entity).z;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public static double calculateDistanceSquared(BlockPos blockPos) {
        double deltaX = getEntityPosition(mc.player).x - (double) blockPos.getX();
        double deltaY = getEntityPosition(mc.player).y - (double) blockPos.getY();
        double deltaZ = getEntityPosition(mc.player).z - (double) blockPos.getZ();
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public static Vector3d getEntityPosition(Entity entity) {
        return new Vector3d(
                entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * (double) mc.timer.renderPartialTicks,
                entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * (double) mc.timer.renderPartialTicks,
                entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * (double) mc.timer.renderPartialTicks
        );
    }

    public static Vector3d getRelativePosition(Entity entity) {
        Vector3d entityPos = getEntityPosition(entity);
        return new Vector3d(
                entityPos.x - mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                entityPos.y - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                entityPos.z - mc.gameRenderer.getActiveRenderInfo().getPos().getZ()
        );
    }

    public static Vector3d getRelativePosition(BlockPos blockPos) {
        return new Vector3d(
                (double) blockPos.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                (double) blockPos.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                (double) blockPos.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ()
        );
    }
}
