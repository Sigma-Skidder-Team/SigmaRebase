package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockFlyHelper {
    static int[][] xzoff = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    public static BlockCache check(BlockPos pos) {
        if (isOkBlock(pos.add(0.0, -0.5, 0.0))) {
            return new BlockCache(pos.add(0.0, -0.5, 0.0), Direction.UP);
        }
        if (isOkBlock(pos.add(-1, 0, 0))) {
            return new BlockCache(pos.add(-1, 0, 0), Direction.EAST);
        }
        if (isOkBlock(pos.add(1, 0, 0))) {
            return new BlockCache(pos.add(1, 0, 0), Direction.WEST);
        }
        if (isOkBlock(pos.add(0, 0, 1))) {
            return new BlockCache(pos.add(0, 0, 1), Direction.NORTH);
        }
        if (isOkBlock(pos.add(0, 0, -1))) {
            return new BlockCache(pos.add(0, 0, -1), Direction.SOUTH);
        }
        return null;
    }

    public static BlockCache check2(BlockPos pos) {
        if (isOkBlock(pos.add(0.0, -0.5, 0.0))) {
            return new BlockCache(pos.add(0.0, -0.5, 0.0), Direction.UP);
        }
        if (isOkBlock(pos.add(-1, 0, 0))) {
            return new BlockCache(pos.add(-1, 0, 0), Direction.EAST);
        }
        if (isOkBlock(pos.add(1, 0, 0))) {
            return new BlockCache(pos.add(1, 0, 0), Direction.WEST);
        }
        if (isOkBlock(pos.add(0, 0, 1))) {
            return new BlockCache(pos.add(0, 0, 1), Direction.NORTH);
        }
        if (isOkBlock(pos.add(0, 0, -1))) {
            return new BlockCache(pos.add(0, 0, -1), Direction.SOUTH);
        }
        return null;
    }


    public static boolean isOkBlock(BlockItem blocks) {
        Block block = blocks.getBlock();
        return !(block instanceof FlowingFluidBlock) && !(block instanceof AirBlock) && !(block instanceof ChestBlock) && !(block instanceof FurnaceBlock);
    }

    public static boolean isOkBlock(BlockPos blockPos) {
        Block block = Minecraft.getInstance().world.getBlockState(blockPos).getBlock();
        return !(block instanceof FlowingFluidBlock) && !(block instanceof AirBlock) && !(block instanceof ChestBlock) && !(block instanceof FurnaceBlock);
    }

    public static class BlockCache {
        private BlockPos position;
        private Direction facing;

        public BlockCache(BlockPos position, Direction facing) {
            this.position = position;
            this.facing = facing;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public Direction getFacing() {
            return this.facing;
        }

        public void setFacing(Direction facing) {
            this.facing = facing;
        }
    }
    public static Vector3d blockPosRedirection(BlockPos bp, Direction ef) {
        return new Vector3d((double)bp.getX() + 0.5 + (double)((float)ef.getXOffset() / 2.0f), (double)bp.getY() + 0.5 + (double)((float)ef.getYOffset() / 2.0f), (double)bp.getZ() + 0.5 + (double)((float)ef.getZOffset() / 2.0f));
    }

    public static BlockCache getBlockCache(BlockPos pos, int range) {

        BlockCache cache = check(pos);


        List<Vec3> possibilities = new ArrayList<>();

        // Consider possible positions around the given BlockPos
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    BlockPos currentPos = pos.add(x, y, z);
                    Block block = blockRelativeToPlayer(x, y, z);
                    BlockPos blockPos = new BlockPos(Minecraft.getInstance().player.getPosX() + x, Minecraft.getInstance().player.getPosY() + y, Minecraft.getInstance().player.getPosZ() + z);

                    if (block.getDefaultState().getMaterial().isReplaceable()) {
                        possibilities.add(new Vec3(currentPos.getX() + 1, currentPos.getY(), currentPos.getZ()));
                        possibilities.add(new Vec3(currentPos.getX() - 1, currentPos.getY(), currentPos.getZ()));
                        possibilities.add(new Vec3(currentPos.getX(), currentPos.getY() + 1, currentPos.getZ()));
                        possibilities.add(new Vec3(currentPos.getX(), currentPos.getY() - 1, currentPos.getZ()));
                        possibilities.add(new Vec3(currentPos.getX(), currentPos.getY(), currentPos.getZ() + 1));
                        possibilities.add(new Vec3(currentPos.getX(), currentPos.getY(), currentPos.getZ() - 1));
                    }
                }
            }
        }

        possibilities = possibilities.stream()
                .filter(vec3 -> Minecraft.getInstance().player.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) <= range)
                .filter(vec3 -> block(vec3.xCoord, vec3.yCoord, vec3.zCoord).getDefaultState().getMaterial().isReplaceable())
                .collect(Collectors.toList());

        if (possibilities.isEmpty()) {
            return null;
        }

        possibilities.sort(Comparator.comparingDouble(vec3 -> {
            double d0 = Minecraft.getInstance().player.getPosX() - vec3.xCoord;
            double d1 = Minecraft.getInstance().player.getPosY() - vec3.yCoord;
            double d2 = Minecraft.getInstance().player.getPosZ() - vec3.zCoord;
            return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        }));

        for (Vec3 possibility : possibilities) {
            BlockPos closestPos = new BlockPos(possibility.xCoord, possibility.yCoord, possibility.zCoord);
            cache = BlockUtils.findValidBlockData(closestPos,false, Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).getBooleanValueFromSettingName("Placement Priority"));
            if (cache != null) {
                return cache;
            }
        }


        return null;
    }

    public static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return block(Minecraft.getInstance().player.getPosX() + offsetX, Minecraft.getInstance().player.getPosY() + offsetY, Minecraft.getInstance().player.getPosZ() + offsetZ);
    }

    public static Block block(final double x, final double y, final double z) {
        return Minecraft.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

}

