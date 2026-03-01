package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.EventGetFovModifier;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.SafeWalk;
import com.mentalfrostbyte.jello.module.impl.movement.speed.AACSpeed;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.world.PositionFacing;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import team.sdhq.eventBus.annotations.priority.LowestPriority;

import java.util.List;

public class BlockFlyAACMode extends Module {
    private float yaw;
    private float pitch;
    private int placeY = 0;
    private int previousSlot = 0;
    private int field23524;
    /** IDK, it's set to 0 when you get setback though. Speed value? **/
    private int speedValue;
    private double field23526;

    public BlockFlyAACMode() {
        super(ModuleCategory.MOVEMENT, "AAC", "Places block underneath if it is in hand");
        this.registerSetting(new BooleanSetting("Haphe (AACAP)", "Never let's you touch the ground.", false));
    }

    @Override
    public void onEnable() {
        this.previousSlot = mc.player.inventory.currentItem;
        this.yaw = mc.player.rotationYaw;
        this.pitch = mc.player.rotationPitch;
        this.placeY = (int) mc.player.getPosY();
        this.speedValue = -1;
        ((BlockFly) this.getParent()).lastSpoofedSlot = -1;
    }

    @Override
    public void onDisable() {
        if (this.previousSlot != -1 && this.getParent().getStringSettingValueByName("ItemSpoof").equals("Switch")) {
            mc.player.inventory.currentItem = this.previousSlot;
        }

        this.previousSlot = -1;
        if (((BlockFly) this.getParent()).lastSpoofedSlot >= 0) {
            mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            ((BlockFly) this.getParent()).lastSpoofedSlot = -1;
        }

        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    @LowerPriority
    public void method16202(EventSendPacket var1) {
        if (this.isEnabled() && mc.player != null) {
            if (var1.packet instanceof CHeldItemChangePacket && ((BlockFly) this.getParent()).lastSpoofedSlot >= 0) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16203(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SPlayerPositionLookPacket) {
                this.speedValue = 0;
            }
        }
    }

    @EventTarget
    public void method16204(EventSafeWalk var1) {
        if (this.isEnabled()) {
            if (mc.player.isOnGround() && Client.getInstance().moduleManager.getModuleByClass(SafeWalk.class).isEnabled()) {
                var1.setSafe(true);
            }
        }
    }

    @EventTarget
    public void method16205(EventMove var1) {
        if (this.isEnabled()) {
            if (this.getParent().getBooleanValueFromSettingName("No Sprint")) {
                mc.player.setSprinting(false);
            }

            if (!this.getBooleanValueFromSettingName("Haphe (AACAP)")) {
                mc.gameSettings.keyBindSprint.setPressed(false);
                mc.player.setSprinting(false);
            }

            ((BlockFly) this.getParent()).performTower(var1);
            if (this.getBooleanValueFromSettingName("Haphe (AACAP)")) {
                if (!mc.player.isOnGround() || mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F) {
                    if (this.field23524 >= 0) {
                        this.field23524++;
                    }
                } else {
                    this.field23524 = 0;
                    mc.player.jump();
                    var1.setY(0.419998 + (double) MovementUtil.getJumpBoost() * 0.1);
                    if (this.speedValue < 3) {
                        this.speedValue++;
                    }
                }

                if (mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F || mc.player.collidedHorizontally) {
                    this.speedValue = 0;
                }

                this.field23526 = AACSpeed.method16016(this.field23524, this.speedValue, () -> this.speedValue = 0);
                if (this.field23524 >= 0) {
                    MovementUtil.setMotion(var1, this.field23526);
                }
            }
        }
    }

    @EventTarget
    public void onFOV(EventGetFovModifier var1) {
        if (this.isEnabled() && mc.world != null && mc.player != null) {
            if (this.getBooleanValueFromSettingName("Haphe (AACAP)") && MovementUtil.isMoving() && !mc.player.isSprinting()) {
                var1.fovModifier *= 1.14F;
            }
        }
    }

    private boolean method16207() {
        BlockRayTraceResult var3 = (BlockRayTraceResult) BlockUtil.method34569(mc.player.lastReportedYaw, mc.player.lastReportedPitch, BlockUtil.getBlockReachDistance(), 0.0F);
        boolean var4 = false;
        if (var3 != null && var3.getType() == RayTraceResult.Type.BLOCK) {
            if (this.getParent().getStringSettingValueByName("ItemSpoof").equals("None")) {
                if (!InvManagerUtil.shouldPlaceItem(mc.player.getHeldItem(Hand.MAIN_HAND).getItem())) {
                    return false;
                }
            }

            if (this.getBooleanValueFromSettingName("Haphe (AACAP)") && !mc.player.isJumping && !mc.player.isOnGround()) {
                if (var3.getFace() == Direction.UP) {
                    return false;
                }

                if (var3.getPos().getY() != this.placeY - 1) {
                    return false;
                }
            }

            if (var3.getFace() == Direction.UP
                    && (double) (var3.getPos().getY() + 2) > mc.player.getPosY()
                    && BlockUtil.isValidBlockPosition(var3.getPos())) {
                return false;
            }

            if ((double) var3.getPos().getY() == mc.player.getPosY()) {
                return false;
            }

            ((BlockFly) this.getParent()).method16736();
            int var5 = mc.player.inventory.currentItem;
            if (!this.getParent().getStringSettingValueByName("ItemSpoof").equals("None")) {
                ((BlockFly) this.getParent()).switchToValidHotbarItem();
            }

            ActionResultType var6 = mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, var3);
            if (this.getParent().getStringSettingValueByName("ItemSpoof").equals("Spoof") || this.getParent().getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")) {
                mc.player.inventory.currentItem = var5;
            }

            if (var6 == ActionResultType.SUCCESS) {
                if (!this.getParent().getBooleanValueFromSettingName("NoSwing")) {
                    mc.player.swingArm(Hand.MAIN_HAND);
                } else {
                    mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                }

                if (var3.getFace() == Direction.UP) {
                    this.placeY = var3.getPos().getY() + 2;
                }

                var4 = true;
            }
        }

        return var4;
    }

    public List<PositionFacing> method16208(Block var1, BlockPos var2) {
        return BlockFlyHelper.method35030(var1, var2, (int) mc.playerController.getBlockReachDistance());
    }

    @EventTarget
    @LowestPriority
    public void onUpdate(EventMotion event) {
        if (this.isEnabled()) {
            if (!event.isPre()) {
                if (MovementUtil.isMoving() && mc.player.isOnGround() && this.getBooleanValueFromSettingName("Haphe (AACAP)") && !mc.player.isJumping) {
                    mc.player.jump();
                }

                if (!this.getBooleanValueFromSettingName("Haphe (AACAP)")) {
                    if (!this.method16207()) {
                        float var11 = 0.0F;

                        while (var11 < 0.7F && !this.method16207()) {
                            var11 += 0.1F;
                        }
                    }
                } else {
                    this.method16207();
                }
            } else {
                double placeY = mc.player.getPosY();
                if (!mc.player.isJumping && this.getBooleanValueFromSettingName("Haphe (AACAP)")) {
                    placeY = this.placeY;
                }

                BlockPos var6 = new BlockPos(mc.player.getPosX(), (double) Math.round(placeY - 1.0), mc.player.getPosZ());
                List var7 = this.method16208(Blocks.STONE, var6);
                if (!var7.isEmpty()) {
                    PositionFacing var8 = (PositionFacing) var7.get(var7.size() - 1);
                    BlockRayTraceResult var9 = BlockUtil.rayTrace(this.yaw, this.pitch, 5.0F);
                    if (!var9.getPos().equals(var8.blockPos()) || !var9.getFace().equals(var8.direction())) {
                        float[] var10 = BlockUtil.rotationsToBlock(var8.blockPos(), var8.direction());
                        this.yaw = var10[0];
                        this.pitch = var10[1];
                    }
                }

                event.setYaw(this.yaw);
                event.setPitch(this.pitch);
            }
        }
    }

    @EventTarget
    public void method16211(EventJump var1) {
        if (this.isEnabled()) {
            if (this.getParent().getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")
                    && (!MovementUtil.isMoving() || this.getParent().getBooleanValueFromSettingName("Tower while moving"))) {
                var1.cancelled = true;
            }
        }
    }
}
