package com.mentalfrostbyte.jello.module.impl.item;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.PremiumModule;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CCloseWindowPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;

public class InvManager extends PremiumModule {
    public static int field23654 = 36;
    public static int field23655 = 37;
    public static int field23656 = 38;
    public static int field23657 = 39;
    public ArrayList<Integer> field23661 = new ArrayList<Integer>();
    private final TimerUtil timer = new TimerUtil();
    private boolean field23659;
    private boolean fakingInventory;

    public InvManager() {
        super(ModuleCategory.ITEM, "InvManager", "Drops all useless items from your inventory");
        this.registerSetting(new ModeSetting("Mode", "The way it will move items in your inventory", 0, "Basic", "OpenInv", "FakeInv"));
        this.registerSetting(new NumberSetting<Float>("Delay", "Inventory clicks delay", 0.3F, Float.class, 0.01F, 1.0F, 0.01F));
        this.registerSetting(new NumberSetting<Float>("Block Cap", "Maximum blocks.", 150.0F, Float.class, 0.0F, 256.0F, 10.0F));
        this.registerSetting(new ModeSetting("Clean Type", "Clean type", 0, "Skywars", "All"));
        this.registerSetting(new BooleanSetting("Fake Items", "Bypass for fake items (AAC).", false));
        this.registerSetting(new BooleanSetting("Cleaner", "Cleans your inventory.", true));
        this.registerSetting(new BooleanSetting("Sword", "Keeps only sword as weapon.", true));
        this.registerSetting(new ModeSetting("Tools", "How tools are handled.", 0, "Keep", "Organize", "Throw"));
        this.registerSetting(new BooleanSetting("Archery", "Cleans bows and arrows.", true));
        this.registerSetting(new BooleanSetting("Food", "Cleans food. Keeps Golden Apples.", false));
        this.registerSetting(new BooleanSetting("Heads", "Cleans Heads.", false));
        this.registerSetting(new BooleanSetting("Auto Shield", "Automatically equip shields for 1.9+.", false));
    }

    public static boolean method16431(ItemStack var0) {
        float var3 = calculateItemDamageBonus(var0);
        Module var4 = Client.getInstance().moduleManager.getModuleByClass(InvManager.class);

        for (int var5 = 9; var5 < 45; var5++) {
            if (mc.player.container.getSlot(var5).getHasStack()) {
                ItemStack stack = mc.player.container.getSlot(var5).getStack();
                if (calculateItemDamageBonus(stack) > var3 && (stack.getItem() instanceof SwordItem || !var4.getBooleanValueFromSettingName("Sword"))) {
                    return false;
                }
            }
        }

        return var0.getItem() instanceof SwordItem || !var4.getBooleanValueFromSettingName("Sword");
    }

    public static float calculateItemDamageBonus(ItemStack itemStack) {
        float dmg = 0.0F;
        Item item = itemStack.getItem();
        if (item instanceof ToolItem tool) {
			dmg += (float) tool.getMaxDamage();
        }

        if (item instanceof SwordItem sword) {
			dmg += sword.getAttackDamage();
        }

        return dmg + (float) EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, itemStack) * 1.25F + (float) EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, itemStack) * 0.01F;
    }

    public static int method16437(Item itemIn) {
        int count = 0;

        for (int slot = 0; slot < 45; slot++) {
            if (mc.player.container.getSlot(slot).getHasStack()) {
                ItemStack itemStack = mc.player.container.getSlot(slot).getStack();
                if (itemStack.getItem() == itemIn) {
                    count += itemStack.getCount();
                }
            }
        }

        return count;
    }

    public static boolean method16442(ItemStack var0) {
        Item var3 = var0.getItem();
        if (var3 instanceof PickaxeItem) {
            float var4 = method16445(var0);

            for (int var5 = 9; var5 < 45; var5++) {
                if (mc.player.container.getSlot(var5).getHasStack()) {
                    ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                    if (method16445(var6) > var4 && var6.getItem() instanceof PickaxeItem) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean isHoe(ItemStack itemStackIn) {
        Item item = itemStackIn.getItem();
        if (item instanceof HoeItem) {
            float var4 = method16445(itemStackIn);

            for (int slot = 9; slot < 45; slot++) {
                if (mc.player.container.getSlot(slot).getHasStack()) {
                    ItemStack itemStack = mc.player.container.getSlot(slot).getStack();
                    if (method16445(itemStack) > var4 && itemStack.getItem() instanceof HoeItem) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean method16444(ItemStack var0) {
        Item var3 = var0.getItem();
        if (var3 instanceof AxeItem) {
            float var4 = method16445(var0);

            for (int var5 = 9; var5 < 45; var5++) {
                if (mc.player.container.getSlot(var5).getHasStack()) {
                    ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                    if (method16445(var6) > var4 && var6.getItem() instanceof AxeItem && !method16431(var0)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static float method16445(ItemStack var0) {
        Item var3 = var0.getItem();
        if (var3 instanceof ToolItem var5) {
            String var4 = var3.getName().getString().toLowerCase();
			float var6 = 1.0F;
            if (!(var3 instanceof PickaxeItem)) {
                if (!(var3 instanceof HoeItem)) {
                    if (!(var3 instanceof AxeItem)) {
                        return 1.0F;
                    }

                    var6 = var5.getDestroySpeed(var0, Blocks.OAK_LOG.getDefaultState());
                    if (var4.toLowerCase().contains("gold")) {
                        var6 -= 5.0F;
                    }
                } else {
                    var6 = var5.getDestroySpeed(var0, Blocks.DIRT.getDefaultState());
                    if (var4.toLowerCase().contains("gold")) {
                        var6 -= 5.0F;
                    }
                }
            } else {
                var6 = var5.getDestroySpeed(var0, Blocks.STONE.getDefaultState());
                if (var4.toLowerCase().contains("gold")) {
                    var6 -= 5.0F;
                }
            }

            var6 = (float) ((double) var6 + (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, var0) * 0.0075);
            return (float) ((double) var6 + (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, var0) / 100.0);
        } else {
            return 0.0F;
        }
    }

    @Override
    public void onEnable() {
        this.field23659 = mc.currentScreen instanceof InventoryScreen;
        this.fakingInventory = false;
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (!this.timer.isEnabled()) {
            this.timer.start();
        }

        if (this.isEnabled() && !AutoArmor.field23798) {
            String mode = this.getStringSettingValueByName("Mode");
            if (!this.getStringSettingValueByName("Mode").equals("OpenInv") || mc.currentScreen instanceof InventoryScreen) {
                long delayValue = (long) (this.getNumberValueBySettingName("Delay") * 20.0F);
                if (mc.currentScreen instanceof InventoryScreen) {
                    this.field23659 = false;
                }

                if (this.fakingInventory && (long) Client.getInstance().playerTracker.getMode() >= delayValue) {
                    this.fakingInventory = !this.fakingInventory;
                    this.sendOpenInventoryPacket(this.field23659);
                    InvManagerUtil.clickSlot(mc.player.container.windowId, 45, 0, ClickType.PICKUP, mc.player, true);
                    this.timer.reset();
                } else {
                    if (mc.currentScreen == null || mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof ChatScreen) {
                        if (this.timer.getElapsedTime() > delayValue && field23654 >= 36) {
                            if (mc.player.container.getSlot(field23654).getHasStack()) {
                                if (!method16431(mc.player.container.getSlot(field23654).getStack())) {
                                    this.method16432(field23654, mode.equals("FakeInv"));
                                }
                            } else {
                                this.method16432(field23654, mode.equals("FakeInv"));
                            }
                        }

                        boolean var7 = this.getStringSettingValueByName("Tools").equals("Organize");
                        if ((long) Client.getInstance().playerTracker.getMode() >= delayValue && field23655 >= 36 && var7) {
                            this.method16438(field23655, mode.equals("FakeInv"));
                        }

                        if ((long) Client.getInstance().playerTracker.getMode() >= delayValue && field23657 >= 36 && var7) {
                            this.method16439(field23657, mode.equals("FakeInv"));
                        }

                        if ((long) Client.getInstance().playerTracker.getMode() >= delayValue && field23656 >= 36 && var7) {
                            this.method16440(field23656, mode.equals("FakeInv"));
                        }

                        if ((long) Client.getInstance().playerTracker.getMode() >= delayValue && this.getBooleanValueFromSettingName("Auto Shield")) {
                            this.fakeInventory(mode.equals("FakeInv"));
                        }

                        if ((long) Client.getInstance().playerTracker.getMode() >= delayValue && this.getBooleanValueFromSettingName("Cleaner")) {
                            for (int var8 = 9; var8 < 45; var8++) {
                                if (mc.player.container.getSlot(var8).getHasStack()) {
                                    ItemStack var9 = mc.player.container.getSlot(var8).getStack();
                                    if (this.method16434(var9, var8)) {
                                        this.sendOpenInventoryPacket(mode.equals("FakeInv"));
                                        InvManagerUtil.clickSlot(var8);
                                        this.timer.reset();
                                        if (delayValue > 0L) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!this.field23659 && !(mc.currentScreen instanceof InventoryScreen) && this.timer.getElapsedTime() > 0L && !this.fakingInventory) {
                        this.field23659 = true;
                        mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                    }
                }
            }
        }
    }

    public void method16432(int hotbarSlot, boolean var2) {
        for (int var5 = 9; var5 < 45; var5++) {
            if (mc.player.container.getSlot(var5).getHasStack()) {
                ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                if (method16431(var6) && calculateItemDamageBonus(var6) > 0.0F && (var6.getItem() instanceof SwordItem || !this.getBooleanValueFromSettingName("Sword"))) {
                    this.sendOpenInventoryPacket(var2);
                    InvManagerUtil.clickSlot(var5, hotbarSlot - 36);
                    this.timer.reset();
                    break;
                }
            }
        }
    }

    public boolean method16434(ItemStack var1, int slot) {
        Item var5 = var1.getItem();
        if (var1.getDisplayName().getString().toLowerCase().contains("(right click)")
                || var1.getDisplayName().getString().toLowerCase().contains("(clique direito)")
                || var1.getDisplayName().getString().toLowerCase().contains("(click derecho)")) {
            return false;
        } else if (var1.getDisplayName().getString().toLowerCase().contains("§k||")) {
            return false;
        } else if (slot == field23654 && method16431(mc.player.container.getSlot(slot).getStack())) {
            return false;
        } else if (var5 instanceof ShieldItem && this.getBooleanValueFromSettingName("Auto Shield")) {
            return false;
        } else if (this.getStringSettingValueByName("Tools").equals("Throw")
                || (
                slot != field23655 && !this.getStringSettingValueByName("Tools").equals("Keep")
                        || !method16442(mc.player.container.getSlot(slot).getStack())
                        || field23655 < 0
        )
                && (
                slot != field23656 && !this.getStringSettingValueByName("Tools").equals("Keep")
                        || !method16444(mc.player.container.getSlot(slot).getStack())
                        || field23656 < 0
        )
                && (
                slot != field23657 && !this.getStringSettingValueByName("Tools").equals("Keep")
                        || !isHoe(mc.player.container.getSlot(slot).getStack())
                        || field23657 < 0
        )) {
            if (var5 instanceof ArmorItem) {
                if (InvManagerUtil.isArmor(slot)) {
                    return false;
                }

                for (EquipmentSlotType var9 : EquipmentSlotType.values()) {
                    if (mc.player.container.getSlot(8 - var9.getIndex()).getHasStack()) {
                        ItemStack var10 = mc.player.container.getSlot(8 - var9.getIndex()).getStack();
                        if (!InvManagerUtil.isBestArmorPiece(var10)) {
                            return false;
                        }
                    }
                }
            }

            if (var5 instanceof BlockItem
                    && (this.method16436() > (int) this.getNumberValueBySettingName("Block Cap") || BlockUtil.blocksToNotPlace.contains(((BlockItem) var5).getBlock()))) {
                return true;
            } else if (var5 == Items.WATER_BUCKET && Client.getInstance().moduleManager.getModuleByClass(AutoMLG.class).isEnabled()) {
                return method16437(Items.WATER_BUCKET) > 1;
            } else if (var5 == Items.BUCKET && Client.getInstance().moduleManager.getModuleByClass(AutoMLG.class).isEnabled()) {
                return method16437(Items.BUCKET) > 1;
            } else if (var5 instanceof PotionItem && InvManagerUtil.hasNegativePotionEffects(var1)) {
                return true;
            } else if (var5 == Items.ENCHANTED_GOLDEN_APPLE) {
                return false;
            } else if (var5.isFood() && this.getBooleanValueFromSettingName("Food") && var5.getFood() != Foods.GOLDEN_APPLE) {
                return true;
            } else if (var5 instanceof HoeItem || var5 instanceof ToolItem || var5 instanceof SwordItem || var5 instanceof ArmorItem) {
                return true;
            } else if ((var5 instanceof BowItem || var5 instanceof ArrowItem) && this.getBooleanValueFromSettingName("Archery")) {
                return true;
            } else {
                return var5 instanceof SkullItem && this.getBooleanValueFromSettingName("Heads") || var5.getName().getString().toLowerCase().contains("tnt")
                        || var5.getName().getString().toLowerCase().contains("stick")
                        || var5.getName().getString().toLowerCase().contains("egg")
                        || var5.getName().getString().toLowerCase().contains("string")
                        || var5.getName().getString().toLowerCase().contains("cake")
                        || var5.getName().getString().toLowerCase().contains("mushroom")
                        || var5.getName().getString().toLowerCase().contains("flint")
                        || var5.getName().getString().toLowerCase().contains("dyePowder")
                        || var5.getName().getString().toLowerCase().contains("feather")
                        || var5.getName().getString().toLowerCase().contains("bucket")
                        || var5.getName().getString().toLowerCase().contains("chest") && !var1.getDisplayName().getString().toLowerCase().contains("collect")
                        || var5.getName().getString().toLowerCase().contains("snow")
                        || var5.getName().getString().toLowerCase().contains("fish")
                        || var5.getName().getString().toLowerCase().contains("enchant")
                        || var5.getName().getString().toLowerCase().contains("exp")
                        || var5.getName().getString().toLowerCase().contains("shears")
                        || var5.getName().getString().toLowerCase().contains("anvil")
                        || var5.getName().getString().toLowerCase().contains("torch")
                        || var5.getName().getString().toLowerCase().contains("seeds")
                        || var5.getName().getString().toLowerCase().contains("leather")
                        || var5.getName().getString().toLowerCase().contains("reeds")
                        || var5.getName().getString().toLowerCase().contains("record")
                        || var5.getName().getString().toLowerCase().contains("snowball")
                        || var5 instanceof GlassBottleItem
                        || var5 == Items.SLIME_BALL
                        || var5 == Items.GUNPOWDER
                        || var5.getName().getString().toLowerCase().contains("piston");
            }
        } else {
            return false;
        }
    }

    public ArrayList<Integer> method16435() {
        return this.field23661;
    }

    private int method16436() {
        int var3 = 0;

        for (int var4 = 0; var4 < 45; var4++) {
            if (mc.player.container.getSlot(var4).getHasStack()) {
                ItemStack var5 = mc.player.container.getSlot(var4).getStack();
                Item var6 = var5.getItem();
                if (var5.getItem() instanceof BlockItem && !BlockUtil.blocksToNotPlace.contains(((BlockItem) var6).getBlock())) {
                    var3 += var5.getCount();
                }
            }
        }

        return var3;
    }

    private void method16438(int var1, boolean var2) {
        for (int var5 = 9; var5 < 45; var5++) {
            if (mc.player.container.getSlot(var5).getHasStack()) {
                ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                if (method16442(var6) && field23655 != var5 && !method16431(var6)) {
                    if (mc.player.container.getSlot(field23655).getHasStack()) {
                        if (!method16442(mc.player.container.getSlot(field23655).getStack())) {
                            this.sendOpenInventoryPacket(var2);
                            InvManagerUtil.clickSlot(var5, field23655 - 36);
                            this.timer.reset();
                            if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                                return;
                            }
                        }
                    } else {
                        this.sendOpenInventoryPacket(var2);
                        InvManagerUtil.clickSlot(var5, field23655 - 36);
                        this.timer.reset();
                        if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void method16439(int var1, boolean var2) {
        for (int var5 = 9; var5 < 45; var5++) {
            if (mc.player.container.getSlot(var5).getHasStack()) {
                ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                if (isHoe(var6) && field23657 != var5 && !method16431(var6)) {
                    if (mc.player.container.getSlot(field23657).getHasStack()) {
                        if (!isHoe(mc.player.container.getSlot(field23657).getStack())) {
                            this.sendOpenInventoryPacket(var2);
                            InvManagerUtil.clickSlot(var5, field23657 - 36);
                            this.timer.reset();
                            if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                                return;
                            }
                        }
                    } else {
                        this.sendOpenInventoryPacket(var2);
                        InvManagerUtil.clickSlot(var5, field23657 - 36);
                        this.timer.reset();
                        if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void method16440(int var1, boolean var2) {
        for (int var5 = 9; var5 < 45; var5++) {
            if (mc.player.container.getSlot(var5).getHasStack()) {
                ItemStack var6 = mc.player.container.getSlot(var5).getStack();
                if (method16444(var6) && field23656 != var5 && !method16431(var6)) {
                    if (mc.player.container.getSlot(field23656).getHasStack()) {
                        if (!method16444(mc.player.container.getSlot(field23656).getStack())) {
                            this.sendOpenInventoryPacket(var2);
                            InvManagerUtil.clickSlot(var5, field23656 - 36);
                            this.timer.reset();
                            if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                                return;
                            }
                        }
                    } else {
                        this.sendOpenInventoryPacket(var2);
                        InvManagerUtil.clickSlot(var5, field23656 - 36);
                        this.timer.reset();
                        if (this.getNumberValueBySettingName("Delay") > 0.0F) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void fakeInventory(boolean fakeInvSetting) {
        if (!mc.player.container.getSlot(45).getHasStack()) {
            for (int hotbarSlot = 9; hotbarSlot < 45; hotbarSlot++) {
                ItemStack var5 = mc.player.container.getSlot(hotbarSlot).getStack();
                if (var5.getItem() instanceof ShieldItem) {
                    this.sendOpenInventoryPacket(fakeInvSetting);
                    this.timer.reset();
                    InvManagerUtil.clickSlot(mc.player.container.windowId, hotbarSlot, 0, ClickType.PICKUP, mc.player, true);
                    this.fakingInventory = true;
                    return;
                }
            }
        }
    }

    private void sendOpenInventoryPacket(boolean var1) {
        if (var1 && this.field23659 && !(mc.currentScreen instanceof InventoryScreen)/* && JelloPortal.getCurrentVersionApplied() <= ViaVerList._1_11_1_or_2.getVersionNumber()*/) {
//            mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
            this.field23659 = false;
        }
    }
}