package com.mentalfrostbyte.jello.util;


import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public record PositionFacing(BlockPos blockPos, Direction direction) {
}
