package com.mentalfrostbyte.jello.module.impl.item;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.block.BarrierBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ChestStealer extends Module {
    public boolean field23621;
    private final ConcurrentHashMap<ChestTileEntity, Boolean> chests;
    private final TimerUtil field23623 = new TimerUtil();
    private final TimerUtil field23624 = new TimerUtil();
    private ChestTileEntity targetChest;

    public ChestStealer() {
        super(ModuleCategory.ITEM, "ChestStealer", "Steals items from chest");
        this.registerSetting(new BooleanSetting("Aura", "Automatically open chests near you.", false));
        this.registerSetting(new BooleanSetting("Ignore Junk", "Ignores useless items.", true));
        this.registerSetting(new BooleanSetting("Fix ViaVersion", "Fixes ViaVersion delay.", true));
        this.registerSetting(new BooleanSetting("Close", "Automatically closes the chest when done", true));
        this.registerSetting(new NumberSetting<>("Delay", "Click delay", 0.2F, Float.class, 0.0F, 1.0F, 0.01F));
        this.registerSetting(new NumberSetting<>("First Item", "Tick delay before grabbing first item", 0.2F, Float.class, 0.0F, 1.0F, 0.01F));
        this.chests = new ConcurrentHashMap<>();
    }

    @Override
    public void onEnable() {
        this.targetChest = null;
        this.field23621 = false;
        if (!this.chests.isEmpty()) {
            this.chests.clear();
        }
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            if (this.getBooleanValueFromSettingName("Aura")) {
                if (this.field23624.getElapsedTime() > 2000L && this.field23621) {
                    this.field23624.reset();
                    this.field23621 = false;
                }

                if (!this.field23624.isEnabled()) {
                    this.field23624.start();
                }

                this.method16370();
                if (this.targetChest != null && mc.currentScreen == null && this.field23624.getElapsedTime() > 1000L) {
                    BlockRayTraceResult var4 = (BlockRayTraceResult) BlockUtil.method34570(this.targetChest.getPos());
                    if (var4.getPos().getX() == this.targetChest.getPos().getX()
                            && var4.getPos().getY() == this.targetChest.getPos().getY()
                            && var4.getPos().getZ() == this.targetChest.getPos().getZ()) {
                        this.field23621 = true;
                        mc.getConnection().sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, var4));
                        mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                        this.field23624.reset();
                    }
                }

                boolean var14 = false;

                for (Entry var6 : this.chests.entrySet()) {
                    ChestTileEntity var7 = (ChestTileEntity) var6.getKey();
                    boolean var8 = (Boolean) var6.getValue();
                    float var9 = (float) var7.getPos().getX();
                    float var10 = (float) var7.getPos().getY() + 0.1F;
                    float var11 = (float) var7.getPos().getZ();
                    if (!this.field23621
                            && (
                            this.targetChest == null
                                    || mc.player.getDistanceSq(var9, var10, var11)
                                    > mc.player.getDistanceSq(var9, var10, var11)
                    )
                            && !var8
                            && Math.sqrt(mc.player.getDistanceSq(var9, var10, var11)) < 5.0
                            && this.field23624.getElapsedTime() > 1000L
                            && mc.currentScreen == null) {
                        BlockRayTraceResult var12 = (BlockRayTraceResult) BlockUtil.method34570(var7.getPos());
                        if (var12.getPos().getX() == var7.getPos().getX()
                                && var12.getPos().getY() == var7.getPos().getY()
                                && var12.getPos().getZ() == var7.getPos().getZ()) {
                            this.targetChest = var7;
                            float[] var13 = RotationUtil.rotationToPos((double) var9 + 0.5, (double) var11 + 0.5, (double) var10 + 0.35);
                            var1.setYaw(var13[0]);
                            var1.setPitch(var13[1]);
                            var14 = true;
                        }
                    }
                }

                if (!var14 && mc.currentScreen == null && this.targetChest != null) {
                    this.chests.put(this.targetChest, true);
                    this.targetChest = null;
                }
            }
        }
    }

    @EventTarget
    public void method16366(EventLoadWorld var1) {
        if (!this.chests.isEmpty()) {
            this.chests.clear();
        }
    }

    @EventTarget
    public void method16367(EventRender2DOffset var1) {
        if (this.isEnabled()) {
            if (!(mc.currentScreen instanceof ChestScreen)) {
                this.field23621 = false;
                this.field23623.stop();
                this.field23623.reset();
                if (mc.currentScreen == null && InvManagerUtil.hasAllSlotsFilled()) {
                    this.field23624.reset();
                }
            } else {
                if (!this.field23623.isEnabled()) {
                    this.field23623.start();
                }

                if (!((float) Client.getInstance().playerTracker.getMode() < this.getNumberValueBySettingName("Delay") * 20.0F)) {
                    if (InvManagerUtil.hasAllSlotsFilled()) {
                        if (this.getBooleanValueFromSettingName("Close")) {
                            mc.player.closeScreen();
                        }
                    } else {
                        ChestScreen chestScreen = (ChestScreen) mc.currentScreen;
                        if (!this.shouldSteal(chestScreen)) {
                            if (this.targetChest != null) {
                                this.chests.put(this.targetChest, true);
                            }
                        } else {
                            boolean var5 = true;

                            for (Slot slot : chestScreen.getContainer().inventorySlots) {
                                if (slot.getHasStack() && slot.slotNumber < chestScreen.getContainer().getNumRows() * 9) {
                                    ItemStack var8 = slot.getStack();
                                    if (!this.method16369(var8)) {
                                        if (!this.field23621) {
                                            if ((float) this.field23623.getElapsedTime() < this.getNumberValueBySettingName("First Item") * 1000.0F) {
                                                return;
                                            }

                                            this.field23621 = !this.field23621;
                                        }

                                        if (!this.getBooleanValueFromSettingName("Fix ViaVersion")) {
                                            InvManagerUtil.clickSlot(chestScreen.getContainer().windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, mc.player);
                                        } else {
                                            InvManagerUtil.clickSlot(chestScreen.getContainer().windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, mc.player, true);
                                        }

                                        this.field23623.reset();
                                        var5 = false;
                                        if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                                            break;
                                        }
                                    }
                                }
                            }

                            if (var5) {
                                if (this.field23621) {
                                    this.field23621 = !this.field23621;
                                }

                                if (this.getBooleanValueFromSettingName("Close")) {
                                    mc.player.closeScreen();
                                }

                                for (ChestTileEntity chest : this.chests.keySet()) {
                                    if (chest == this.targetChest) {
                                        this.targetChest = null;
                                        this.chests.put(chest, true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean shouldSteal(ChestScreen chest) {
        List<String> doNotStealKeywords = new ArrayList<>(
                Arrays.asList(
                        "menu",
                        "selector",
                        "game",
                        "gui",
                        "server",
                        "inventory",
                        "play",
                        "teleporter",
                        "shop",
                        "melee",
                        "armor",
                        "block",
                        "castle",
                        "mini",
                        "warp",
                        "teleport",
                        "user",
                        "team",
                        "tool",
                        "sure",
                        "trade",
                        "cancel",
                        "accept",
                        "soul",
                        "book",
                        "recipe",
                        "profile",
                        "tele",
                        "port",
                        "map",
                        "kit",
                        "select",
                        "lobby",
                        "vault",
                        "lock",
                        "anticheat",
                        "travel",
                        "settings",
                        "user",
                        "preference",
                        "compass",
                        "cake",
                        "wars",
                        "buy",
                        "upgrade",
                        "ranged",
                        "potions",
                        "utility",
                        "choose",
                        "modalidades"
                )
        );
        List<BlockPos> positions = BlockUtil.getBlockPositionsInRange(8.0F);
        String cleanName = chest.getNarrationMessage().replaceAll("§.", "").toLowerCase();

        for (String keyword : doNotStealKeywords) {
            int indexOf = cleanName.indexOf(keyword);
            if (indexOf > 0 && indexOf < 40) {
                return false;
            }
        }

        for (BlockPos pos : positions) {
            if (BlockUtil.getBlockFromPosition(pos) instanceof ChestBlock || BlockUtil.getBlockFromPosition(pos) instanceof BarrierBlock) {
                return true;
            }
        }

        return false;
    }

    private boolean method16369(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!this.getBooleanValueFromSettingName("Ignore Junk")) {
            return false;
        } else if (!(item instanceof SwordItem)) {
            if (item instanceof PickaxeItem) {
                return !InvManager.method16442(itemStack);
            } else if (!(item instanceof AxeItem)) {
                if (item instanceof HoeItem) {
                    return !InvManager.isHoe(itemStack);
                } else if (!(item instanceof PotionItem)) {
                    if (item instanceof BlockItem) {
                        return !InvManagerUtil.shouldPlaceItem(item);
                    } else if (!(item instanceof ArrowItem)
                            && (!(item instanceof BowItem) || !Client.getInstance().moduleManager.getModuleByClass(InvManager.class).getBooleanValueFromSettingName("Archery"))) {
                        if (item == Items.WATER_BUCKET && Client.getInstance().moduleManager.getModuleByClass(AutoMLG.class).isEnabled()) {
                            return false;
                        } else {
                            ArrayList var5 = new ArrayList<Item>(
                                    Arrays.asList(
                                            Items.COMPASS,
                                            Items.FEATHER,
                                            Items.FLINT,
                                            Items.EGG,
                                            Items.STRING,
                                            Items.STICK,
                                            Items.TNT,
                                            Items.BUCKET,
                                            Items.LAVA_BUCKET,
                                            Items.WATER_BUCKET,
                                            Items.SNOW,
                                            Items.ENCHANTED_BOOK,
                                            Items.EXPERIENCE_BOTTLE,
                                            Items.SHEARS,
                                            Items.ANVIL,
                                            Items.TORCH,
                                            Items.BEETROOT_SEEDS,
                                            Items.MELON_SEEDS,
                                            Items.PUMPKIN_SEEDS,
                                            Items.WHEAT_SEEDS,
                                            Items.LEATHER,
                                            Items.GLASS_BOTTLE,
                                            Items.PISTON,
                                            Items.SNOWBALL,
                                            Items.FISHING_ROD
                                    )
                            );
                            return var5.contains(item) || item.getName().getString().toLowerCase().contains("seed");
                        }
                    } else {
                        return true;
                    }
                } else {
                    return InvManagerUtil.hasNegativePotionEffects(itemStack);
                }
            } else {
                return !InvManager.method16444(itemStack);
            }
        } else {
            return !InvManager.method16431(itemStack);
        }
    }

    private void method16370() {
        List<TileEntity> var3 = mc.world.loadedTileEntityList;
        var3.removeIf(var0 -> !(var0 instanceof ChestTileEntity));

        for (TileEntity var5 : var3) {
            if (!this.chests.containsKey((ChestTileEntity) var5)) {
                this.chests.put((ChestTileEntity) var5, false);
            }
        }

        for (ChestTileEntity var7 : this.chests.keySet()) {
            if (!var3.contains(var7)) {
                this.chests.remove(var7);
            }
        }
    }
}
