package com.mentalfrostbyte.jello.module.impl.item.autogapple;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.PremiumModule;
import com.mentalfrostbyte.jello.module.impl.item.AutoGapple;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effect;
import net.minecraft.util.Hand;
import team.sdhq.eventBus.annotations.EventTarget;

public class HypixelAutoGapple extends PremiumModule {
    private int currentTickCounter;
    private int currentGappleSlot;
    private int gappleCooldown;
    private int firePotionSlot;

    public HypixelAutoGapple() {
        super(ModuleCategory.PLAYER, "Hypixel", "Hypixel bypass");
        this.registerSetting(new BooleanSetting("Fire resistance potions", "Automatically drink fire pots", true));
        this.currentTickCounter = -1;
        this.firePotionSlot = -1;
    }

    @Override
    public void onEnable() {
        this.currentTickCounter = -1;
        this.gappleCooldown = 20;
        this.firePotionSlot = -1;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && event.isPre() && !(mc.currentScreen instanceof ChestScreen)) {
            if (this.gappleCooldown < 20) {
                this.gappleCooldown++;
            }

            if (this.firePotionSlot == -1 && this.gappleCooldown >= 20 && this.currentTickCounter == -1) {
                if (mc.player.getHealth() <= this.access().getNumberValueBySettingName("Health") * 2.0F && mc.player.getAbsorptionAmount() == 0.0F) {
                    this.firePotionSlot = ((AutoGapple) this.access()).findGappleSlot(false);
                    if (this.firePotionSlot >= 0) {
                        this.currentTickCounter = 0;
                        this.gappleCooldown = 0;
                    }
                }

                if (this.currentTickCounter == -1 && this.getBooleanValueFromSettingName("Fire resistance potions") && !mc.player.isPotionActive(Effect.get(12))) {
                    this.firePotionSlot = ((AutoGapple) this.access()).findGappleSlot(true);
                    if (this.firePotionSlot >= 0) {
                        this.currentTickCounter = 0;
                        this.gappleCooldown = 0;
                    }
                }
            }

            if (this.firePotionSlot >= 0 && this.currentTickCounter >= 0) {
                this.currentTickCounter++;
                if (this.currentTickCounter == 2) {
                    event.setPitch(event.getPitch() + 1.0F);
                }

                if (this.currentTickCounter != 1) {
                    if (this.currentTickCounter >= 3) {
                        mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        mc.getConnection().sendPacket(new CHeldItemChangePacket(this.firePotionSlot + (this.firePotionSlot != 8 ? 1 : -1)));
                        mc.getConnection().sendPacket(new CHeldItemChangePacket(this.firePotionSlot));
                        mc.player.inventory.currentItem = this.currentGappleSlot;
                        this.currentGappleSlot = -1;
                        this.currentTickCounter = -1;
                        this.firePotionSlot = -1;
                    }
                } else {
                    this.currentGappleSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = this.firePotionSlot;
                }
            }
        }
    }
}
