package com.mentalfrostbyte.jello.module.impl.movement.phase;


import com.mentalfrostbyte.jello.event.impl.game.world.EventBlockCollision;
import com.mentalfrostbyte.jello.event.impl.game.world.EventPushBlock;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventWalkingUpdate;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import team.sdhq.eventBus.annotations.EventTarget;

public class UnfullPhase extends Module {
    public int currentYPosition;
    public double yOffset;

    public UnfullPhase() {
        super(ModuleCategory.MOVEMENT, "Unfull", "Weird Hypixel VClip for some blocks");
    }
    
    @EventTarget
    public void onWalkingUpdate(EventWalkingUpdate event) {
        if (this.isEnabled()) {
            if ((int) mc.player.getPosY() == currentYPosition && !mc.player.isJumping) {
                MultiUtilities.setPlayerYMotion(-2.0);
            }

            if (mc.player.getPosY() > (double) currentYPosition && mc.player.isJumping && !mc.player.isSneaking()) {
                currentYPosition++;
            }

            if (mc.player.getPosY() % 1.0 == 0.0 && mc.player.isSneaking()) {
                currentYPosition--;
            }
        }
    }
    
    @EventTarget
    public void onBlockCollision(EventBlockCollision event) {
        if (this.isEnabled()) {
            if (event.getVoxelShape() != null &&
                    (event.getBlockPos().getY() == currentYPosition - 1 ||
                            event.getBlockPos().getY() == currentYPosition ||
                            (event.getBlockPos().getY() == currentYPosition + 1 &&
                                    mc.world.getBlockState(event.getBlockPos()).getBlock() instanceof SlabBlock) ||
                            mc.world.getBlockState(event.getBlockPos()).getBlock() instanceof StairsBlock)) {

                event.setBoxelShape(null);
            }
        }
    }

    @EventTarget
    public void onPushBlock(EventPushBlock event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnable() {
        currentYPosition = (int) mc.player.getPosY();
        yOffset = (double) currentYPosition - mc.player.getPosY();
    }
}
