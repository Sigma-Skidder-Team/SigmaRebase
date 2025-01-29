package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.blockfly.*;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import com.mentalfrostbyte.jello.util.player.Rots;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.Arrays;
import java.util.List;

public class BlockFly extends ModuleWithModuleSettings {
    public static List<Block> blocksToNotPlace;
    public int lastSpoofedSlot;
    public Animation animation = new Animation(114, 114, Direction.BACKWARDS);
    public int blockCount = 0;

    public BlockFly() {
        super(ModuleCategory.MOVEMENT, "BlockFly", "Allows you to automatically bridge", new BlockFlyNCPMode(),
                new BlockFlyAACMode(), new BlockFlySmoothMode(), new BlockFlyHypixelMode());
        this.registerSetting(
                new ModeSetting("ItemSpoof", "Item spoofing mode", 2, "None", "Switch", "Spoof", "LiteSpoof"));
        this.registerSetting(new ModeSetting("Tower Mode", "Tower mode", 1, "None", "NCP", "AAC", "Vanilla"));
        this.registerSetting(new ModeSetting("Picking mode", "The way it will move blocks in your inventory.", 0,
                "Basic", "FakeInv", "OpenInv"));
        this.registerSetting(new BooleanSetting("Tower while moving", "Allows you to tower while moving.", false));
        this.registerSetting(
                new BooleanSetting("Show Block Amount", "Shows the amount of blocks in your inventory.", true));
        this.registerSetting(new BooleanSetting("NoSwing", "Removes the swing animation.", true));
        this.registerSetting(
                new BooleanSetting("Intelligent Block Picker", "Always get the biggest blocks stack.", true));
        this.registerSetting(new BooleanSetting("No Sprint", "Disable sprint.", false));
        blocksToNotPlace = Arrays.asList(
                Blocks.AIR,
                Blocks.WATER,
                Blocks.LAVA,
                Blocks.ENCHANTING_TABLE,
                Blocks.BLACK_CARPET,
                Blocks.GLASS_PANE,
                Blocks.IRON_BARS,
                Blocks.ICE,
                Blocks.PACKED_ICE,
                Blocks.CHEST,
                Blocks.TRAPPED_CHEST,
                Blocks.TORCH,
                Blocks.ANVIL,
                Blocks.TRAPPED_CHEST,
                Blocks.NOTE_BLOCK,
                Blocks.JUKEBOX,
                Blocks.TNT,
                Blocks.REDSTONE_WIRE,
                Blocks.LEVER,
                Blocks.COBBLESTONE_WALL,
                Blocks.OAK_FENCE,
                Blocks.TALL_GRASS,
                Blocks.TRIPWIRE,
                Blocks.TRIPWIRE_HOOK,
                Blocks.RAIL,
                Blocks.LILY_PAD,
                Blocks.RED_MUSHROOM,
                Blocks.BROWN_MUSHROOM,
                Blocks.VINE,
                Blocks.ACACIA_TRAPDOOR,
                Blocks.LADDER,
                Blocks.FURNACE,
                Blocks.SAND,
                Blocks.CACTUS,
                Blocks.DISPENSER,
                Blocks.DROPPER,
                Blocks.CRAFTING_TABLE,
                Blocks.COBWEB,
                Blocks.PUMPKIN,
                Blocks.ACACIA_SAPLING);
    }

    public static boolean shouldPlaceItem(Item item) {
        if (!(item instanceof BlockItem)) {
            return false;
        } else {
            Block var3 = ((BlockItem) item).getBlock();
            return !blocksToNotPlace.contains(var3)
                    && !(var3 instanceof AbstractButtonBlock)
                    && !(var3 instanceof BushBlock)
                    && !(var3 instanceof TrapDoorBlock)
                    && !(var3 instanceof AbstractPressurePlateBlock)
                    && !(var3 instanceof SandBlock)
                    && !(var3 instanceof OreBlock)
                    && !(var3 instanceof SkullBlock)
                    && !(var3 instanceof BedBlock)
                    && !(var3 instanceof BannerBlock)
                    && !(var3 instanceof ChestBlock)
                    && !(var3 instanceof DoorBlock);
        }
    }

    public boolean method16732() {
        return this.getBooleanValueFromSettingName("No Sprint") && this.isEnabled();
    }

    public void switchToValidHotbarItem() {
        try {
            for (int containerSlot = 36; containerSlot < 45; containerSlot++) {
                int hotbarSlot = containerSlot - 36;
                if (mc.player.container.getSlot(containerSlot).getHasStack()
                        && shouldPlaceItem(mc.player.container.getSlot(containerSlot).getStack().getItem())
                        && mc.player.container.getSlot(containerSlot).getStack().getCount() != 0) {
                    if (mc.player.inventory.currentItem == hotbarSlot) {
                        return;
                    }

                    mc.player.inventory.currentItem = hotbarSlot;
                    if (this.getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")
                            && (this.lastSpoofedSlot < 0 || this.lastSpoofedSlot != hotbarSlot)) {
                        mc.getConnection().getNetworkManager().sendPacket(new CHeldItemChangePacket(hotbarSlot));
                        this.lastSpoofedSlot = hotbarSlot;
                    }
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

    public int getValidItemCount() {
        int totalItemCount = 0;

        for (int containerSlot = 0; containerSlot < 45; containerSlot++) {
            if (mc.player.container.getSlot(containerSlot).getHasStack()) {
                ItemStack stack = mc.player.container.getSlot(containerSlot).getStack();
                Item item = stack.getItem();

                if (shouldPlaceItem(item)) {
                    totalItemCount += stack.getCount();
                }
            }
        }

        return totalItemCount;
    }

    public void method16736() {
        String var3 = this.getStringSettingValueByName("Picking mode");
        if ((!var3.equals("OpenInv") || mc.currentScreen instanceof InventoryScreen) && this.getValidItemCount() != 0) {
            int var4 = 43;
            if (!this.getBooleanValueFromSettingName("Intelligent Block Picker")) {
                if (!this.hasPlaceableItem()) {
                    int var5 = -1;

                    for (int var6 = 9; var6 < 36; var6++) {
                        if (mc.player.container.getSlot(var6).getHasStack()) {
                            Item var7 = mc.player.container.getSlot(var6).getStack().getItem();
                            if (shouldPlaceItem(var7)) {
                                var5 = var6;
                                break;
                            }
                        }
                    }

                    for (int var9 = 36; var9 < 45; var9++) {
                        if (!mc.player.container.getSlot(var9).getHasStack()) {
                            var4 = var9;
                            break;
                        }
                    }

                    if (var5 >= 0) {
                        if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")) {
//                            mc.getConnection()
//                                    .sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
                        }

                        this.windowClick(var5, var4 - 36);
                        if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")) {
                            mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                        }
                    }
                }
            } else {
                int var8 = this.getBiggestSlot();
                if (!this.hasPlaceableItem()) {
                    for (int var11 = 36; var11 < 45; var11++) {
                        if (!mc.player.container.getSlot(var11).getHasStack()) {
                            var4 = var11;
                            break;
                        }
                    }
                } else {
                    for (int var10 = 36; var10 < 45; var10++) {
                        if (mc.player.container.getSlot(var10).getHasStack()) {
                            Item var12 = mc.player.container.getSlot(var10).getStack().getItem();
                            if (shouldPlaceItem(var12)) {
                                var4 = var10;
                                if (mc.player.container.getSlot(var10).getStack().getCount() == mc.player.container
                                        .getSlot(var8).getStack().getCount()) {
                                    var4 = -1;
                                }
                                break;
                            }
                        }
                    }
                }

                if (var4 >= 0 && mc.player.container.getSlot(var4).slotNumber != var8) {
                    if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")/*
                                                                                                 * && JelloPortal.
                                                                                                 * getCurrentVersionApplied
                                                                                                 * () <= ViaVerList.
                                                                                                 * _1_11_1_or_2.
                                                                                                 * getVersionNumber()
                                                                                                 */) {
//                        mc.getConnection()
//                                .sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
                    }

                    this.windowClick(var8, var4 - 36);
                    if (!(mc.currentScreen instanceof InventoryScreen) && var3.equals("FakeInv")) {
                        mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                    }
                }
            }
        }
    }

    public int getBiggestSlot() {
        int biggestSlot = -1;
        int biggestSlotCount = 0;
        if (this.getValidItemCount() != 0) {
            for (int slot = 9; slot < 45; slot++) {
                if (mc.player.container.getSlot(slot).getHasStack()) {
                    Item var6 = mc.player.container.getSlot(slot).getStack().getItem();
                    ItemStack var7 = mc.player.container.getSlot(slot).getStack();
                    if (shouldPlaceItem(var6) && var7.getCount() > biggestSlotCount) {
                        biggestSlotCount = var7.getCount();
                        biggestSlot = slot;
                    }
                }
            }

            return biggestSlot;
        } else {
            return -1;
        }
    }

    public boolean hasPlaceableItem() {
        for (int slot = 36; slot < 45; slot++) {
            if (mc.player.container.getSlot(slot).getHasStack()) {
                Item item = mc.player.container.getSlot(slot).getStack().getItem();
                if (shouldPlaceItem(item)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canPlaceItem(Hand var1) {
        if (!this.access().getStringSettingValueByName("ItemSpoof").equals("None")) {
            return this.getValidItemCount() != 0;
        } else
            return shouldPlaceItem(mc.player.getHeldItem(var1).getItem());
    }

    public void windowClick(int slot, int mouseButton) {
        mc.playerController.windowClick(mc.player.container.windowId, slot, mouseButton, ClickType.SWAP, mc.player);
    }

    public void onMove(EventMove var1) {
        if (mc.timer.timerSpeed == 0.8038576F) {
            mc.timer.timerSpeed = 1.0F;
        }

        if (this.getValidItemCount() != 0 && (!mc.player.collidedVertically
                || this.getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla"))) {
            if (!MultiUtilities.isMoving() || this.getBooleanValueFromSettingName("Tower while moving")) {
                String var4 = this.getStringSettingValueByName("Tower Mode");
                switch (var4) {
                    case "NCP":
                        if (var1.getY() > 0.0) {
                            if (MovementUtil.getJumpBoost() == 0) {
                                if (var1.getY() > 0.247 && var1.getY() < 0.249) {
                                    var1.setY(
                                            (double) ((int) (mc.player.getPosY() + var1.getY())) - mc.player.getPosY());
                                }
                            } else {
                                double var6 = (int) (mc.player.getPosY() + var1.getY());
                                if (var6 != (double) ((int) mc.player.getPosY())
                                        && mc.player.getPosY() + var1.getY() - var6 < 0.15) {
                                    var1.setY(var6 - mc.player.getPosY());
                                }
                            }
                        }

                        if (mc.player.getPosY() == (double) ((int) mc.player.getPosY())
                                && MultiUtilities.isAboveBounds(mc.player, 0.001F)) {
                            if (mc.gameSettings.keyBindJump.isPressed()) {
                                if (!MultiUtilities.isMoving()) {
                                    MovementUtil.strafe(0.0);
                                    MovementUtil.setSpeed(var1, 0.0);
                                }

                                var1.setY(MovementUtil.getJumpValue());
                            } else {
                                var1.setY(-1.0E-5);
                            }
                        }
                        break;
                    case "AAC":
                        if (var1.getY() > 0.247 && var1.getY() < 0.249) {
                            var1.setY((double) ((int) (mc.player.getPosY() + var1.getY())) - mc.player.getPosY());
                            if (mc.gameSettings.keyBindJump.isPressed() && !MultiUtilities.isMoving()) {
                                MovementUtil.strafe(0.0);
                                MovementUtil.setSpeed(var1, 0.0);
                            }
                        } else if (mc.player.getPosY() == (double) ((int) mc.player.getPosY())
                                && MultiUtilities.isAboveBounds(mc.player, 0.001F)) {
                            var1.setY(-1.0E-10);
                        }
                        break;
                    case "Vanilla":
                        if (mc.gameSettings.keyBindJump.isPressed()
                                && MultiUtilities.isAboveBounds(mc.player, 0.001F)
                                && mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().offset(0.0, 1.0, 0.0))
                                        .count() == 0L) {
                            mc.player
                                    .setPosition(mc.player.getPosX(), mc.player.getPosY() + 1.0, mc.player.getPosZ());
                            var1.setY(0.0);
                            MovementUtil.setSpeed(var1, 0.0);
                            mc.timer.timerSpeed = 0.8038576F;
                        }
                }
            }
        } else if (!this.getStringSettingValueByName("Tower Mode").equals("AAC")
                || !MultiUtilities.isAboveBounds(mc.player, 0.001F)
                || !mc.gameSettings.keyBindJump.isPressed()) {
            if (!this.getStringSettingValueByName("Tower Mode").equals("NCP")
                    && !this.getStringSettingValueByName("Tower Mode").equals("Vanilla")
                    && MultiUtilities.isAboveBounds(mc.player, 0.001F)
                    && mc.gameSettings.keyBindJump.isPressed()) {
                mc.player.jumpTicks = 20;
                var1.setY(MovementUtil.getJumpValue());
            }
        } else if (!MultiUtilities.isMoving() || this.getBooleanValueFromSettingName("Tower while moving")) {
            mc.player.jumpTicks = 0;
            mc.player.jump();
            MovementUtil.setSpeed(var1, MovementUtil.getSpeed());
            MovementUtil.strafe(MovementUtil.getSpeed());
        }

        if (!this.getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")) {
            MultiUtilities.setPlayerYMotion(var1.getY());
        }
    }

    @Override
    public String getSuffix() {
        return Client.getInstance().clientMode != ClientMode.CLASSIC ? super.getSuffix() : "Scaffold";
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (this.getBooleanValueFromSettingName("Show Block Amount")) {
                this.blockCount = this.getValidItemCount();
            }
        }
    }

    @Override
    public void onDisable() {
        Rots.rotating = false;
        this.animation.changeDirection(Direction.BACKWARDS);
        super.onDisable();
    }

    @EventTarget
    public void onRender(EventRender2DOffset render) {
        this.animation.changeDirection(Direction.FORWARDS);
        if (this.animation.calcPercent() != 0.0F) {
            if (this.getBooleanValueFromSettingName("Show Block Amount")) {
                if (Client.getInstance().clientMode != ClientMode.JELLO) {
                    this.renderClassicBlockCount(
                            mc.getMainWindow().getWidth() / 2,
                            mc.getMainWindow().getHeight() / 2 + 15 - (int) (10.0F * this.animation.calcPercent()),
                            this.animation.calcPercent());
                } else {
                    this.renderJelloBlockCount(
                            mc.getMainWindow().getWidth() / 2,
                            mc.getMainWindow().getHeight() - 138
                                    - (int) (25.0F * MathHelper.calculateTransition(this.animation.calcPercent(), 0.0F,
                                            1.0F, 1.0F)),
                            this.animation.calcPercent());
                }
            }
        }
    }

    public void renderClassicBlockCount(int x, int y, float alpha) {
        alpha = (float) (0.5 + 0.5 * (double) alpha);
        GL11.glAlphaFunc(518, 0.1F);
        RenderUtil.drawString(
                Resources.medium17,
                (float) (x + 10),
                (float) (y + 5),
                this.blockCount + " Blocks",
                MultiUtilities.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha * 0.3F));
        RenderUtil.drawString(
                Resources.medium17,
                (float) (x + 10),
                (float) (y + 4),
                this.blockCount + " Blocks",
                MultiUtilities.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha * 0.8F));
        GL11.glAlphaFunc(519, 0.0F);
    }

    public void renderJelloBlockCount(int var1, int var2, float var3) {
        int var6 = 0;
        int var7 = ResourceRegistry.JelloLightFont18.getWidth(this.blockCount + "") + 3;
        var6 += var7;
        var6 += ResourceRegistry.JelloLightFont14.getWidth("Blocks");
        int var8 = var6 + 20;
        int var9 = 32;
        var1 -= var8 / 2;
        GL11.glPushMatrix();
        RenderUtil.method11465(var1, var2, var8, var9, MultiUtilities.applyAlpha(-15461356, 0.8F * var3));
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont18, (float) (var1 + 10), (float) (var2 + 4), this.blockCount + "",
                MultiUtilities.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var3));
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14, (float) (var1 + 10 + var7), (float) (var2 + 8), "Blocks",
                MultiUtilities.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F * var3));
        var1 += 11 + var8 / 2;
        var2 += var9;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var1, (float) var2, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var1), (float) (-var2), 0.0F);
        RenderUtil.drawImage((float) var1, (float) var2, 9.0F, 23.0F, Resources.selectPNG,
                MultiUtilities.applyAlpha(-15461356, 0.8F * var3));
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
