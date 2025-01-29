package com.mentalfrostbyte.jello.module.impl.item;


import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;

import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.player.InvManagerUtil;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;

public class AutoTools extends Module {
    public int previousSlot = -1;

    public AutoTools() {
        super(ModuleCategory.ITEM, "AutoTools", "Picks the best tool when breaking blocks");
        this.registerSetting(new ModeSetting("Inv Mode", "The way it will move tools in your inventory", 0, "Basic", "OpenInv", "FakeInv"));
    }

    @EventTarget
    public void onMouseHover(EventMouseHover event) {
        if (this.isEnabled() && mc.player != null && event.getMouseButton() == 0) {
            if (this.previousSlot != -1) {
                mc.player.inventory.currentItem = this.previousSlot;
                this.previousSlot = -1;
            }
        }
    }

    @EventTarget
    public void onKeyPress(EventKeyPress event) {
        if (this.isEnabled() && mc.player != null && event.getKey() == 0) {
            this.selectBestTool(event.getBlockPos());
        }
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled() && mc.player != null && mc.gameSettings.keyBindAttack.isKeyDown()) {
            this.selectBestTool(null);
        }
    }

    public void selectBestTool(BlockPos blockPos) {
        BlockPos targetBlockPos = blockPos == null
                ? (mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK ? null : ((BlockRayTraceResult) mc.objectMouseOver).getPos())
                : blockPos;

        if (targetBlockPos != null) {
            int bestToolSlot = InvManagerUtil.findBestToolFromHotbarSlotForBlock(mc.world.getBlockState(targetBlockPos));
            if (bestToolSlot != -1) {
                if (mc.player.inventory.currentItem != bestToolSlot % 9 && this.previousSlot == -1) {
                    this.previousSlot = mc.player.inventory.currentItem;
                }

                if (bestToolSlot >= 36 && bestToolSlot <= 44) {
                    mc.player.inventory.currentItem = bestToolSlot % 9;
                } /* else if  (Client.getInstance().getPlayerTracker().getMode() > 1) */ { // TODO
                    String invMode = this.getStringSettingValueByName("Inv Mode");
                    if (invMode.equals("OpenInv") && !(mc.currentScreen instanceof InventoryScreen)) {
                        return;
                    }

//                    if (invMode.equals("FakeInv")/* && JelloPortal.getCurrentVersionApplied() <= ViaVerList._1_11_1_or_2.getVersionNumber()*/) {
//                        mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
//                    }
//
//                    mc.player.inventory.currentItem = InvManagerUtil.swapToolToHotbar(bestToolSlot);
//                    if (invMode.equals("FakeInv")) {
//                        mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                    // TODO
                    }
                }
            }
        }
    }

