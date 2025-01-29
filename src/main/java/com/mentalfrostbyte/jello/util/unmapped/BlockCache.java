package com.mentalfrostbyte.jello.util.unmapped;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BlockCache {
    public BlockPos position;
    public Direction direction;

    public BlockCache(BlockPos position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }
}
