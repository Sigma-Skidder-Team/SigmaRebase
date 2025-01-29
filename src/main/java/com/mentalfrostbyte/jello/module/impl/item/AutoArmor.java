package com.mentalfrostbyte.jello.module.impl.item;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.TimerUtil;
import com.mentalfrostbyte.jello.util.player.InvManagerUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CCloseWindowPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class AutoArmor extends Module {
    public static boolean field23798;
    public boolean field23799 = false;
    public boolean field23800 = false;
    private final TimerUtil timer = new TimerUtil();
    private boolean isInventoryOpen;

    public AutoArmor() {
        super(ModuleCategory.ITEM, "AutoArmor", "Automaticly equips your armor");
        this.registerSetting(new BooleanSetting("Fake Items", "Bypass for fake items (AAC).", false));
        this.registerSetting(new NumberSetting<Float>("Delay", "Inventory clicks delay", 0.3F, Float.class, 0.0F, 1.0F, 0.01F));
        this.registerSetting(new ModeSetting("Mode", "The way it will move armor in your inventory", 0, "Basic", "OpenInv", "FakeInv"));
        this.registerSetting(new ModeSetting("Elytra", "Elytra Equip Mode", 0, "Ignore", "Equip", "On Use"));
    }

    @Override
    public void onEnable() {
        if (!this.timer.isEnabled()) {
            this.timer.start();
        }

        this.isInventoryOpen = mc.currentScreen instanceof InventoryScreen;
        field23798 = false;
    }

    @Override
    public void onDisable() {
        field23798 = false;
    }

    @EventTarget
    @HigherPriority
    public void method16615(EventPlayerTick var1) {
        if (this.isEnabled()) {
            if (!this.timer.isEnabled()) {
                this.timer.start();
            }

            if (!this.getStringSettingValueByName("Mode").equals("OpenInv") || mc.currentScreen instanceof InventoryScreen) {
                long var4 = (long) (this.getNumberValueBySettingName("Delay") * 1000.0F);
                String var6 = this.getStringSettingValueByName("Elytra");
                switch (var6) {
                    case "Ignore":
                        this.field23799 = false;
                        break;
                    case "Equip":
                        this.field23799 = true;
                        break;
                    case "On Use":
                        if (!mc.player.isOnGround() && mc.player.jumpTicks == 0 && mc.player.isJumping && !this.field23800) {
                            this.field23799 = true;
                        } else if (mc.player.isOnGround()) {
                            this.field23799 = false;
                        }
                }

                this.field23800 = mc.player.isJumping;
                if (mc.currentScreen instanceof InventoryScreen) {
                    this.isInventoryOpen = false;
                }

                if ((mc.currentScreen == null || mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof ChatScreen)
                        && this.timer.getElapsedTime() > var4
                        && (float) Client.getInstance().playerTracker.getMode() > (float) var4 / 50.0F) {
                    field23798 = false;
                    this.method16616(this.getStringSettingValueByName("Mode").equalsIgnoreCase("FakeInv"));
                }

                for (EquipmentSlotType var9 : EquipmentSlotType.values()) {
                    if (mc.player.container.getSlot(8 - var9.getIndex()).getHasStack()) {
                        if (InvManagerUtil.isArmor(8 - var9.getIndex())) {
                            return;
                        }
                    } else if (this.method16618(var9)) {
                        return;
                    }
                }

                if (!this.isInventoryOpen && !(mc.currentScreen instanceof InventoryScreen) && this.timer.getElapsedTime() > 0L) {
                    this.isInventoryOpen = true;
                    mc.getConnection().sendPacket(new CCloseWindowPacket(-1));
                }
            }
        }
    }

    private void method16616(boolean var1) {
        for (EquipmentSlotType type : EquipmentSlotType.values()) {
            if (mc.player.container.getSlot(8 - type.getIndex()).getHasStack()) {
                ItemStack var8 = mc.player.container.getSlot(8 - type.getIndex()).getStack();
                if (InvManagerUtil.isBestArmorPiece(var8) && (!this.field23799 || type != EquipmentSlotType.CHEST)) {
                    continue;
                }
            }

            for (int slot = 9; slot < 45; slot++) {
                if (mc.player.container.getSlot(slot).getHasStack()) {
                    ItemStack var9 = mc.player.container.getSlot(slot).getStack();
                    if (var9.getItem() instanceof ElytraItem && this.field23799) {
                        mc.player.inventory.getStackInSlot(36 + EquipmentSlotType.CHEST.getIndex());
                    }
                    if (var9.getItem() instanceof ArmorItem && !this.field23799) {
                        ArmorItem armorItem = (ArmorItem) var9.getItem();
                        if (armorItem.getEquipmentSlot() == type
                                && InvManagerUtil.isBestArmorPiece(var9)
                                && InvManagerUtil.getArmorProtectionValue(var9) > 0
                                && (
                                !Client.getInstance().moduleManager.getModuleByClass(AutoArmor.class).getBooleanValueFromSettingName("Fake Items")
                                        || Client.getInstance().slotChangeTracker.method33238(slot) >= 1500L
                        )) {
                            this.method16617(var1);
                            Item var11 = mc.player.inventory.getStackInSlot(36 + armorItem.getEquipmentSlot().getIndex()).getItem();
                            if (!(var11 instanceof ElytraItem)) {
                                if (!(var11 instanceof AirItem)) {
                                    InvManagerUtil.method25871(8 - armorItem.getEquipmentSlot().getIndex());
                                }
                            } else {
                                InvManagerUtil.click(8 - type.getIndex(), 0, true);
                            }

                            InvManagerUtil.fixedClick(mc.player.container.windowId, slot, 0, ClickType.QUICK_MOVE, mc.player, true);
                            this.timer.reset();
                            field23798 = true;
                            if (Client.getInstance().moduleManager.getModuleByClass(AutoArmor.class).getNumberValueBySettingName("Delay") > 0.0F) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void method16617(boolean var1) {
        if (var1 && this.isInventoryOpen && !(mc.currentScreen instanceof InventoryScreen)/* && JelloPortal.getCurrentVersionApplied() <= ViaVerList._1_11_1_or_2.getVersionNumber()*/) {
//            mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
            this.isInventoryOpen = false;
        }
    }

    private boolean method16618(EquipmentSlotType var1) {
        for (int var4 = 9; var4 < 45; var4++) {
            if (mc.player.container.getSlot(var4).getHasStack()) {
                ItemStack var5 = mc.player.container.getSlot(var4).getStack();
                Item var6 = var5.getItem();
                if (var6 instanceof ArmorItem) {
                    ArmorItem var7 = (ArmorItem) var6;
                    if (var1 == var7.getEquipmentSlot()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}