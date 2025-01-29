package com.mentalfrostbyte.jello.module.impl.world;

import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.*;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.render.Box3D;
import com.mentalfrostbyte.jello.util.world.BlockUtil;
import com.mentalfrostbyte.jello.util.player.RotationHelper;
import com.mentalfrostbyte.jello.util.render.RenderUtil;

import net.minecraft.block.*;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class Nuker extends Module {
    public BlockPos targetPos;
    public List<BlockPos> blocksToDestroy;

    public Nuker() {
        super(ModuleCategory.WORLD, "Nuker", "Destroys blocks around you");
        this.registerSetting(new NumberSetting<>("Range", "Range value for nuker", 6.0F, Float.class, 2.0F, 10.0F, 1.0F));
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "All", "One hit", "Bed", "Egg"));
        this.registerSetting(new BooleanSetting("NoSwing", "Removes the swing animation.", false));
        this.registerSetting(new BooleanListSetting("Blocks", "Blocks to destroy", true));
        this.registerSetting(new ColorSetting("Color", "The rendered block color", ClientColors.MID_GREY.getColor(), true));
    }

    public static void destroyBlock(BlockPos block) {
        mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, block, Direction.UP));
        mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, block, Direction.UP));
        mc.world.setBlockState(block, Blocks.AIR.getDefaultState());
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && event.isPre()) {
            this.blocksToDestroy = this.getBlocksToDestroy(this.getNumberValueBySettingName("Range") / 2.0F);
            if (this.blocksToDestroy.isEmpty()) {
                this.targetPos = null;
            } else if (mc.playerController.getCurrentGameType() != GameType.CREATIVE) {
                if (this.targetPos != null) {
                    if (mc.world.getBlockState(this.targetPos).isAir()
                            || Math.sqrt(
                            mc.player
                                    .getDistanceSq(
                                            (double) this.targetPos.getX() + 0.5,
                                            (double) this.targetPos.getY() + 0.5,
                                            (double) this.targetPos.getZ() + 0.5
                                    )
                    )
                            > 6.0) {
                        this.targetPos = this.blocksToDestroy.get(0);
                    }

                    float[] rotations = RotationHelper.rotationToPos(
                            (double) this.targetPos.getX(), (double) this.targetPos.getZ(), (double) this.targetPos.getY()
                    );
                    event.setYaw(rotations[0]);
                    event.setPitch(rotations[1]);
                    EventKeyPress keyPress = new EventKeyPress(0, false, this.targetPos);
                    EventBus.call(keyPress);
                } else {
                    this.targetPos = this.blocksToDestroy.get(0);
                    float[] var6 = RotationHelper.rotationToPos(
                            (double) this.targetPos.getX() + 0.5, (double) this.targetPos.getZ(), (double) this.targetPos.getY() + 0.5
                    );
                    event.setYaw(var6[0]);
                    event.setPitch(var6[1]);
                    EventKeyPress keyPress = new EventKeyPress(0, false, this.targetPos);
                    EventBus.call(keyPress);
                }
                mc.playerController.onPlayerDamageBlock(this.targetPos, BlockUtil.method34580(this.targetPos));
                if (!this.getBooleanValueFromSettingName("NoSwing")) {
                    mc.player.swingArm(Hand.MAIN_HAND);
                } else {
                    mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                }
            } else {
                for (BlockPos var9 : this.blocksToDestroy) {
                    mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, var9, BlockUtil.method34580(var9)));
                    if (!this.getBooleanValueFromSettingName("NoSwing")) {
                        mc.player.swingArm(Hand.MAIN_HAND);
                    } else {
                        mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender(EventRender3D var1) {
        if (this.targetPos != null && !mc.world.getBlockState(this.targetPos).isAir()) {
            int var4 = MultiUtilities.applyAlpha(this.parseSettingValueToIntBySettingName("Color"), 0.4F);
            GL11.glPushMatrix();
            GL11.glDisable(2929);
            double var5 = (double) this.targetPos.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX();
            double var7 = (double) this.targetPos.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY();
            double var9 = (double) this.targetPos.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ();
            AxisAlignedBB var11 = mc.world.getBlockState(this.targetPos).getCollisionShape(mc.world, this.targetPos).getBoundingBox();
            Box3D var12 = new Box3D(
                    var5 + var11.minX,
                    var7 + var11.minY,
                    var9 + var11.minZ,
                    var5 + var11.maxX,
                    var7 + var11.maxY,
                    var9 + var11.maxZ
            );
            RenderUtil.render3DColoredBox(var12, var4);
            GL11.glEnable(2929);
            GL11.glPopMatrix();
        }
    }

    public boolean isReplaceable(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return mc.world.getBlockState(pos).getMaterial().isReplaceable() || block instanceof BushBlock;
    }

    public List<BlockPos> getBlocksToDestroy(float range) {
        ArrayList<BlockPos> blocksToDestroy = new ArrayList<>();

        for (float y = range + 2.0F; y >= -range + 1.0F; y--) {
            for (float x = -range; x <= range; x++) {
                for (float z = -range; z <= range; z++) {
                    BlockPos pos = new BlockPos(
                            mc.player.getPosX() + (double) x,
                            mc.player.getPosY() + (double) y,
                            mc.player.getPosZ() + (double) z
                    );
                    if (!mc.world.getBlockState(pos).isAir()
                            && mc.world.getBlockState(pos).getFluidState().isEmpty()
                            && Math.sqrt(
                            mc.player.getDistanceSq((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5)
                    )
                            < (double) range) {
                        String mode = this.getStringSettingValueByName("Mode");
                        switch (mode) {
                            case "One hit":
                                if (!this.isReplaceable(pos)) {
                                    continue;
                                }
                                break;
                            case "Bed":
                                if (!(mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) {
                                    continue;
                                }
                                break;
                            case "Egg":
                                if (!(mc.world.getBlockState(pos).getBlock() instanceof DragonEggBlock)) {
                                    continue;
                                }
                        }

                        blocksToDestroy.add(pos);
                    }
                }
            }
        }

        return blocksToDestroy;
    }
}
