package com.mentalfrostbyte.jello.module.impl.movement;

//import com.mentalfrostbyte.jello.misc.unmapped.StepEnum;
//import com.mentalfrostbyte.jello.misc.unmapped.Class5631;
import com.mentalfrostbyte.jello.misc.StepEnum;
import net.minecraft.util.shape.VoxelShape;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.EventStep;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.step.*;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

public class Step extends ModuleWithModuleSettings {
    public static int updateTicksBeforeStep;

    public Step() {
        super(ModuleCategory.MOVEMENT,
                "Step",
                "Allows you to step up more than 0.5 block",
                new VanillaStep()
        // new HypixelStep(),
        // new NCPStep(),
        // new AACStep(),
        // new SpiderStep()
        );

    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.player.stepHeight = 0.6F;
    }

    @EventTarget
    // @Class5631
    public void onStep(EventStep var1) {
        if (!(var1.getHeight() < 0.1)) {
            updateTicksBeforeStep = 0;
        }
    }

    @EventTarget
    // @Class5631
    public void onUpdate(EventUpdate var1) {
        if (var1.isPre()) {
            updateTicksBeforeStep++;
        }
    }

    public StepEnum method16748(EventStep var1) {
        double var4 = mc.player.getPosX() + var1.getVector().x;
        double var6 = mc.player.getPosZ() + var1.getVector().z;
        double var8 = 0.41;
        double var10 = var1.getHeight() - var1.getY();
        Box var12 = new Box(
                var4 - var8, mc.player.getBoundingBox().minY, var6 - var8, var4 + var8,
                mc.player.getBoundingBox().minY + var10, var6 + var8);
        Object[] var13 = mc.world.getCollisionShapes(mc.player, var12).toArray();
        int var14 = var13.length;
        BlockState var15 = null;
        BlockPos var16 = null;
        double var17 = 0.0;

        for (int var19 = 0; var19 < var14; var19++) {
            VoxelShape var20 = (VoxelShape) var13[var19];
            BlockPos var21 = new BlockPos(var20.getStart(Direction.Axis.X), var20.getStart(Direction.Axis.Y),
                    var20.getStart(Direction.Axis.Z));
            BlockState var22 = mc.world.getBlockState(var21);
            if (var15 == null || var20.getBoundingBox().maxY > var17) {
                var15 = var22;
                var16 = var21;
                var17 = var20.getBoundingBox().maxY;
            }
        }

        if (!mc.player.isInWater() && !mc.player.isInLava()) {
            if (var15 != null) {
                if (!mc.player.isOnGround()) {
                    if (var15.getBlock() instanceof SlabBlock) {
                        VoxelShape var24 = var15.getShape(mc.world, var16);
                        if (var24.getBoundingBox().maxY == 1.0) {
                            return StepEnum.NORMAL_BLOCK;
                        }
                    }

                    if (var15.getBlock() instanceof StairsBlock) {
                        return StepEnum.STAIRS;
                    }

                    return StepEnum.NORMAL_BLOCK;
                }

                if (var10 != var1.getHeight() && var10 < 0.5) {
                    if (!(var15.getBlock() instanceof SlabBlock)) {
                        if (var15.getBlock() instanceof StairsBlock) {
                            return StepEnum.STAIRS;
                        }
                    } else {
                        VoxelShape var23 = var15.getShape(mc.world, var16);
                        if (var23.getBoundingBox().maxY == 1.0) {
                            return StepEnum.HALF_BLOCK;
                        }
                    }

                    return StepEnum.HALF_BLOCK;
                }
            }

            return StepEnum.HALF_BLOCK;
        } else {
            return StepEnum.NORMAL_BLOCK;
        }
    }
}