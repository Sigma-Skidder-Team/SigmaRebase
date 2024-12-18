package com.mentalfrostbyte.jello.event.impl;


import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class EventBlockCollision extends CancellableEvent {
    private BlockPos blockPos;
    private VoxelShape voxelShape;

    public EventBlockCollision(BlockPos blockPos, VoxelShape voxelShape) {
        this.blockPos = blockPos;
        this.voxelShape = voxelShape;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public VoxelShape getVoxelShape() {
        return this.voxelShape;
    }

    public void setBlockPos(BlockPos to) {
        this.blockPos = to;
    }

    public void setVoxelShape(VoxelShape var1) {
        if (var1 == null) {
            var1 = VoxelShapes.create(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        this.voxelShape = var1;
    }
}