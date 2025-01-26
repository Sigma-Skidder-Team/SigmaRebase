package com.mentalfrostbyte.jello.module.impl.movement;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSlowDown;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.item.*;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;

public class NoSlow extends Module {
    public boolean isBlocking;

    public NoSlow() {
        super(ModuleCategory.MOVEMENT, "NoSlow", "Stops slowdown when using an item");
        this.registerSetting(new ModeSetting("Mode", "NoSlow mode", 0, "Vanilla", "NCP"));
        this.registerSetting(new BooleanSetting("Sword", "Cancels blocking slowdown", true));
        this.registerSetting(new BooleanSetting("Consume", "Cancels consuming slowdown", true));
        this.registerSetting(new BooleanSetting("Bow", "Cancels bow's slowdown", true));
        // See net.minecraft.util.MovementInputFromOptions
        this.registerSetting(new BooleanSetting("Sneak", "Cancels sneaking slowdown", false));
        // See net.minecraft.entity.player.PlayerEntity/Entity and net.minecraft.block.SlimeBlock
        this.registerSetting(new BooleanSetting("Blocks", "Cancels some blocks slowdown like honey, slime & soul sand", false));
    }

    @EventTarget
    public void onSlowDown(EventSlowDown event) {
        if (this.isEnabled()) {
            if (mc.player.getHeldItemMainhand() == null) return;
            Item mainHandItem = mc.player.getHeldItemMainhand().getItem();
            Item offHandItem = mc.player.getHeldItemOffhand().getItem();
            boolean isUsingItem = mc.gameSettings.keyBindUseItem.isKeyDown();
            boolean shouldCancel =
                    (mainHandItem instanceof SwordItem && isUsingItem && getBooleanValueFromSettingName("Sword")) ||
                            ((mainHandItem.isFood() || mainHandItem instanceof PotionItem || mainHandItem instanceof MilkBucketItem) && isUsingItem && getBooleanValueFromSettingName("Consume")) ||
                            ((offHandItem.isFood() || offHandItem instanceof PotionItem || offHandItem instanceof MilkBucketItem) && isUsingItem && getBooleanValueFromSettingName("Consume")) ||
                            (mainHandItem instanceof BowItem && isUsingItem && getBooleanValueFromSettingName("Bow")) ||
                            (offHandItem instanceof BowItem && isUsingItem && getBooleanValueFromSettingName("Bow"));

            if (shouldCancel)
                event.setCancelled(true);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;

        boolean auraEnabled = Client.getInstance().moduleManager.getModuleByClass(KillAura.class).isEnabled2();
        boolean isSwordEquipped = mc.player.getHeldItemMainhand() != null && mc.player.getHeldItemMainhand().getItem() instanceof SwordItem;

        if (!event.isPre()) {
            if (isSwordEquipped && mc.gameSettings.keyBindUseItem.isKeyDown() && !isBlocking && !auraEnabled && isModeNCP()) {
                MultiUtilities.block();
                isBlocking = true;
            } else if (!isSwordEquipped && isBlocking) {
                isBlocking = false;
            }
        } else {
            handlePreEvent(isSwordEquipped, auraEnabled);
        }
    }

    public boolean isModeNCP() {
        return this.getStringSettingValueByName("Mode").equals("NCP");
    }

    public void handlePreEvent(boolean isSwordEquipped, boolean auraEnabled) {
        if (!isModeNCP()) {
            if (isBlocking && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
                isBlocking = false;
            }
        } else if (isBlocking && mc.gameSettings.keyBindUseItem.isKeyDown() && !auraEnabled) {
            if (isSwordEquipped) {
                MultiUtilities.unblock();
            }
            isBlocking = false;
        }
    }
}