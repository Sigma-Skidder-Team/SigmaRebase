package com.mentalfrostbyte.jello.module.impl.item;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.PremiumModule;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

import java.util.Iterator;
import java.util.stream.Stream;

public class AutoMLG extends PremiumModule {
    private static int preTicks;
    private int field23647;
    private int field23648;
    private BlockPos field23650;

    public AutoMLG() {
        super(ModuleCategory.ITEM, "AutoMLG", "Automatically places water when falling");
        this.registerSetting(new BooleanSetting("Cubecraft", "Cubecraft bypass", true));
        preTicks = -1;
    }

    public static boolean method16421() {
        return preTicks >= 0;
    }

    @Override
    public void onEnable() {
        preTicks = -1;
    }

    @Override
    public void onDisable() {
        preTicks = -1;
    }

    @EventTarget
    public void onMove(EventMove var1) {
        if (this.isEnabled()) {
            if (preTicks > 0 && !mc.player.isOnGround()) {
                MovementUtil.setMotion(var1, 0.0);
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && mc.playerController.gameIsSurvivalOrAdventure()) {
            if (var1.isPre() && preTicks >= 0) {
                preTicks++;
                float[] var4 = RotationUtil.rotationToPos(
                        (double) this.field23650.getX() + 0.5, (double) this.field23650.getZ() + 0.5, (double) this.field23650.getY() + 0.5
                );
                var1.setYaw(var4[0]);
                var1.setPitch(var4[1]);
            }

            if (preTicks == (!this.getBooleanValueFromSettingName("Cubecraft") ? 3 : 5)) {
                if (mc.player.inventory.currentItem != this.field23647) {
                    this.field23648 = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = this.field23647;
                    mc.playerController.syncCurrentPlayItem();
                }

                mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                preTicks = -1;
                this.field23650 = null;
                mc.player.inventory.currentItem = this.field23648;
            }

            int var7 = this.method16424();
            if (!Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()
                    && var7 != -1
                    && !mc.player.isOnGround()
                    && mc.player.fallDistance > 3.0F) {
                BlockPos var5 = this.method16425();
                if (var5 != null) {
                    if (var1.isPre() && preTicks == -1) {
                        float[] var6 = RotationUtil.rotationToPos((double) var5.getX() + 0.5, (double) var5.getZ() + 0.5, (double) var5.getY() + 0.5);
                        var1.setYaw(var6[0]);
                        var1.setPitch(var6[1]);
                        if (var7 != mc.player.inventory.currentItem) {
                            this.field23648 = mc.player.inventory.currentItem;
                            mc.player.inventory.currentItem = var7;
                            mc.playerController.syncCurrentPlayItem();
                        }

                        this.field23647 = var7;
                        this.field23650 = var5;
                        preTicks = 0;
                        return;
                    }

                    if (this.field23650 != null) {
                        mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                        mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    }
                }
            }
        }
    }

    public int method16424() {
        for (int var3 = 36; var3 < 45; var3++) {
            if (mc.player.container.getSlot(var3).getHasStack()) {
                ItemStack var4 = mc.player.container.getSlot(var3).getStack();
                if (var4.getItem() == Items.WATER_BUCKET) {
                    return var3 - 36;
                }
            }
        }

        if (Client.getInstance().playerTracker.getMode() > 1) { // TODO
            for (int var5 = 9; var5 < 36; var5++) {
                if (mc.player.container.getSlot(var5).getHasStack()) {
                    ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                    if (var6.getItem() == Items.WATER_BUCKET) {

                        if (JelloPortal.getVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
                            mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
                        }

                        InvManagerUtil.clickSlot(var5, 6);
                        mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                        return 6;
                    }
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    private BlockPos method16425() {
        double var3 = mc.player.getMotion().x;
        double var5 = mc.player.getMotion().y;
        double var7 = mc.player.getMotion().z;
        AxisAlignedBB var9 = mc.player.getBoundingBox().expand(var3, 0.0, var7).offset(0.0, var5, 0.0);
        Stream var10 = mc.world.getCollisionShapes(mc.player, var9);
        Iterator var11 = var10.iterator();
        BlockPos var12 = null;

        while (var11.hasNext()) {
            VoxelShape var13 = (VoxelShape) var11.next();
            AxisAlignedBB var14 = var13.getBoundingBox();
            BlockPos var15 = new BlockPos(var14.getCenter());
            if (BlockUtil.isValidBlockPosition(var15)
                    && (double) (var15.getY() + 1) < mc.player.getPosY()
                    && (
                    var12 == null
                            || mc.player
                            .getDistanceSq((double) var12.getX() + 0.5 - var3, var12.getY() + 1, (double) var12.getZ() + 0.5 - var7)
                            > mc.player
                            .getDistanceSq((double) var15.getX() + 0.5 - var3, var15.getY() + 1, (double) var15.getZ() + 0.5 - var7)
            )) {
                var12 = var15;
            }
        }

        if (var12 != null) {
            return var12;
        } else {
            var5 = mc.player.getMotion().y - 1.0;
            var9 = mc.player.getBoundingBox().expand(var3, 0.0, var7).offset(0.0, var5, 0.0);
            var10 = mc.world.getCollisionShapes(mc.player, var9);
            var11 = var10.iterator();

            while (var11.hasNext()) {
                VoxelShape var20 = (VoxelShape) var11.next();
                AxisAlignedBB var21 = var20.getBoundingBox();
                BlockPos var22 = new BlockPos(var21.getCenter());
                if (BlockUtil.isValidBlockPosition(var22)
                        && (double) (var22.getY() + 1) < mc.player.getPosY()
                        && (
                        var12 == null
                                || mc.player
                                .getDistanceSq((double) var12.getX() + 0.5 - var3, var12.getY() + 1, (double) var12.getZ() + 0.5 - var7)
                                > mc.player
                                .getDistanceSq((double) var22.getX() + 0.5 - var3, var22.getY() + 1, (double) var22.getZ() + 0.5 - var7)
                )) {
                    var12 = var22;
                }
            }

            return var12;
        }
    }
}