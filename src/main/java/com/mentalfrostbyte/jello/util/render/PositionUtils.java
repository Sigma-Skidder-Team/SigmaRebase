package com.mentalfrostbyte.jello.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class PositionUtils {
    public static final MinecraftClient mc = MinecraftClient.getInstance();

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

    public static Vector3D_ getEntityPosition(Entity entity) {
        return new Vector3D_(
                entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * (double) mc.timer.renderPartialTicks,
                entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * (double) mc.timer.renderPartialTicks,
                entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * (double) mc.timer.renderPartialTicks);
    }

    public static Vector3D_ getRelativePosition(Entity entity) {
        Vector3D_ entityPos = getEntityPosition(entity);
        return new Vector3D_(
                entityPos.x - MinecraftClient.getInstance().gameRenderer.getActiveRenderInfo().getPos().getX(),
                entityPos.y - MinecraftClient.getInstance().gameRenderer.getActiveRenderInfo().getPos().getY(),
                entityPos.z - MinecraftClient.getInstance().gameRenderer.getActiveRenderInfo().getPos().getZ());
    }

    public static Vector3D_ getRelativePosition(BlockPos blockPos) {
        return new Vector3D_(
                (double) blockPos.getX()
                        - MinecraftClient.getInstance().gameRenderer.getActiveRenderInfo().getPos().getX(),
                (double) blockPos.getY()
                        - MinecraftClient.getInstance().gameRenderer.getActiveRenderInfo().getPos().getY(),
                (double) blockPos.getZ()
                        - MinecraftClient.getInstance().gameRenderer.getActiveRenderInfo().getPos().getZ());
    }
}
