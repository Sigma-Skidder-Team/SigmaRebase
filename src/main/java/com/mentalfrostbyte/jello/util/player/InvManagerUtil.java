package com.mentalfrostbyte.jello.util.player;

import com.mentalfrostbyte.jello.misc.Class3280;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;

import java.util.List;
import java.util.Objects;

import static com.mentalfrostbyte.jello.module.Module.mc;

public class InvManagerUtil {
    public static boolean method25819(int var0) {
        ItemStack var3 = mc.player.container.getSlot(var0).getStack();
        return var3 == null || var3.getItem() instanceof Class3280;
    }

    public static void method25871(int var0) {
        mc.playerController.windowClick(mc.player.container.windowId, var0, 1, ClickType.THROW, mc.player);
    }
    public static boolean method25859(ItemStack var0) {
        return var0 != null ? var0.getItem() instanceof PotionItem : false;
    }
    public static ItemStack method25866(int var0) {
        return mc.player.container.getSlot(var0).getStack();
    }

    public static int isHotbarEmpty() {
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(hotbarSlot);
            if (itemStack == null) {
                return hotbarSlot;
            }
        }

        return 0;
    }

    public static int findBestToolFromHotbarSlotForBlock(BlockState state) {
        int slot = -1;
        float dmg = 1.0F;

        for (int hotbarSlot = 44; hotbarSlot >= 9; hotbarSlot--) {
            ItemStack item = mc.player.container.getSlot(hotbarSlot).getStack();
            if (item != null) {
                float damage;
                if (state == null) {
                    if (!(item.getItem() instanceof SwordItem)) {
                        continue;
                    }

                    damage = ((SwordItem)item.getItem()).getAttackDamage();
                } else {
                    damage = item.getDestroySpeed(state);
                }

                if (damage > dmg) {
                    slot = hotbarSlot;
                    dmg = damage;
                }
            }
        }

        return slot;
    }

    public static int method25843(Item... var0) {
        int var3 = 0;
        int var4 = -1;

        for (int var5 = 0; var5 < 9; var5++) {
            ItemStack var6 = mc.player.inventory.getStackInSlot(var5);
            if (var6 != null) {
                for (Item var10 : var0) {
                    if (var6.getItem() == var10) {
                        int var11 = var6.getCount();
                        if (var11 > var3) {
                            var4 = var5;
                            var3 = var11;
                        }
                    }
                }
            }
        }

        return var4;
    }


    public static int findItemSlot(Item var0) {
        int var3 = 0;
        int var4 = -1;

        for (int var5 = 44; var5 >= 9; var5--) {
            ItemStack var6 = mc.player.container.getSlot(var5).getStack();
            if (!method25819(var5) && var6.getItem() == var0) {
                int var7 = var6.getCount();
                if (var7 > var3) {
                    var4 = var5;
                    var3 = var7;
                }
            }
        }

        return var4;
    }
    public static int getArmorProtectionValue(ItemStack itemStack) {
        if (itemStack != null) {
            return itemStack.getItem() instanceof ArmorItem ? ((ArmorItem)itemStack.getItem()).getDamageReduceAmount() + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, itemStack) : 0;
        } else {
            return 0;
        }
    }

    public static int getTotalArmorProtection(PlayerEntity player) {
        int totalProtection = 0;

        for (int slot = 5; slot <= 8; slot++) {
            totalProtection += getArmorProtectionValue(player.inventory.getStackInSlot(slot));
        }

        return totalProtection;
    }

    public static boolean method25875() {
        for (Slot var3 : mc.player.container.inventorySlots) {
            if (!var3.getHasStack() && var3.slotNumber > 8 && var3.slotNumber < 45) {
                return false;
            }
        }

        return true;
    }

    public static boolean isItemStackValid(ItemStack var0) {
        return var0 != null && !var0.getItem().equals(Items.AIR);
    }

    public static List<EffectInstance> getPotionEffects(ItemStack itemStack) {
        if (itemStack == null)
            return null;
        return itemStack.getItem() instanceof PotionItem ? PotionUtils.getEffectsFromStack(itemStack) : null;
    }

    public static boolean method25874(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof PotionItem) {
            for (EffectInstance var4 : getPotionEffects(itemStack)) {
                if (var4.getPotion() == Effects.POISON
                        || var4.getPotion() == Effects.INSTANT_DAMAGE
                        || var4.getPotion() == Effects.SLOWNESS
                        || var4.getPotion() == Effects.WEAKNESS) {
                    return true;
                }
            }
        }

        return false;
    }

    public static ItemStack method25869(int windowId, int slotId, int usedButton, ClickType mode, PlayerEntity entity) {
        return fixedClick(windowId, slotId, usedButton, mode, entity, false);
    }

    public static ItemStack fixedClick(int windowId, int slotId, int usedButton, ClickType mode, PlayerEntity entity, boolean fixed) {
        ItemStack clickedItem = null;
        if (slotId >= 0) {
            clickedItem = entity.openContainer.getSlot(slotId).getStack().copy();
        }

        short actionNumber = entity.openContainer.getNextTransactionID(mc.player.inventory);
        ItemStack item = entity.openContainer.slotClick(slotId, usedButton, mode, entity);
        if (mode == null) {
            return item;
        }
        if (clickedItem == null || /*JelloPortal.getCurrentVersion().getVersionNumber() > ViaVerList._1_12.getVersionNumber() && */!fixed || mode == ClickType.SWAP) {
            clickedItem = item;
        }

        mc.getConnection().sendPacket(new CClickWindowPacket(windowId, slotId, usedButton, mode, clickedItem, actionNumber));
        return item;
    }

    public static void moveItemToHotbar(int slot, int mouseButton) {
        mc.playerController.windowClick(mc.player.container.windowId, slot, mouseButton, ClickType.SWAP, mc.player);
    }

    public static boolean isBestArmorPiece(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ArmorItem) {
            float prot = (float) getArmorProtectionValue(stack);

            for (int var4 = 5; var4 < 45; var4++) {
                if (mc.player.container.getSlot(var4).getHasStack()) {
                    ItemStack st = mc.player.container.getSlot(var4).getStack();
                    Item i = st.getItem();
                    if (i instanceof ArmorItem it) {
                        float armorProtection = (float) getArmorProtectionValue(st);
                        if (armorProtection > prot && it.getEquipmentSlot() == ((ArmorItem)stack.getItem()).getEquipmentSlot()) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean isArmor(int slot) {
        return method25848(mc.player.container.getSlot(slot).getStack());
    }

    public static boolean isHead(ArmorItem armorPiece) {
        return armorPiece.getEquipmentSlot() == EquipmentSlotType.HEAD;
    }

    public static boolean isChestplate(ArmorItem armorPiece) {
        return armorPiece.getEquipmentSlot() == EquipmentSlotType.CHEST;
    }

    public static boolean isLeggings(ArmorItem armorPiece) {
        return armorPiece.getEquipmentSlot() == EquipmentSlotType.LEGS;
    }

    public static boolean isBoots(ArmorItem armorPiece) {
        return armorPiece.getEquipmentSlot() == EquipmentSlotType.FEET;
    }

    public static int getDamageReduceFromSlot(int slot) {
        assert mc.player != null;
        ItemStack stack = mc.player.container.getSlot(slot).getStack();
        if (stack.getItem() instanceof ArmorItem item) {
            Enchantment en = Objects.requireNonNull(Enchantment.getEnchantmentByID(0));
            return item.getDamageReduceAmount() + EnchantmentHelper.getEnchantmentLevel(en, stack);
        } else {
            return 0;
        }
    }

    public static boolean method25848(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem)itemStack.getItem();
            int var4 = getArmorProtectionValue(itemStack);
            if (!isHead(armorItem)) {
                if (!isChestplate(armorItem)) {
                    if (!isLeggings(armorItem)) {
                        return !isBoots(armorItem) ? false : var4 > getDamageReduceFromSlot(8);
                    } else {
                        return var4 > getDamageReduceFromSlot(7);
                    }
                } else {
                    return var4 > getDamageReduceFromSlot(6);
                }
            } else {
                return var4 > getDamageReduceFromSlot(5);
            }
        } else {
            return false;
        }
    }

    public static void click(int slotId, int mouseButton, boolean type) {
        mc.playerController
                .windowClick(mc.player.container.windowId, slotId, mouseButton, !type ? ClickType.THROW : ClickType.QUICK_MOVE, mc.player);
    }
}
