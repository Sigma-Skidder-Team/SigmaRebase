package com.mentalfrostbyte.jello.module.impl.item;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.Criticals;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;

import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowestPriority;

import java.util.ArrayList;
import java.util.List;

public class AutoPotion extends Module {
    public int field23808;
    public int field23809;
    public int field23810;
    public int field23811;

    public AutoPotion() {
        super(ModuleCategory.ITEM, "AutoPotion", "Automatically throws potion to regen or speed up");
        this.registerSetting(new NumberSetting<Float>("Health", "Maximum health before healing.", 6.0F, Float.class, 0.5F, 10.0F, 0.5F));
        this.registerSetting(new BooleanSetting("Predict", "Predicts where to pot when moving.", true));
        this.registerSetting(new BooleanSetting("Instant", "Instant potting (more packets).", false));
        this.registerSetting(new BooleanSetting("Speed", "Uses Speed pots.", true));
        this.registerSetting(new BooleanSetting("Regen", "Uses Regen pots.", true));
        this.registerSetting(new BooleanSetting("Custom potion", "Allow the use of custom potions", false));
        this.registerSetting(new BooleanSetting("In fight", "Allows using autopot with killaura", true));
    }

    @Override
    public void onEnable() {
        this.field23811 = 0;
    }

    @EventTarget
    @LowestPriority
    public void method16629(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            if (this.getBooleanValueFromSettingName("In fight") || KillAura.targetData == null && KillAura.targetEntity == null) {
                int var4 = this.method16631();
                this.field23808++;
                int[] var5 = new int[]{6, -1, -1};
                if (this.getBooleanValueFromSettingName("Regen")) {
                    var5[1] = 10;
                }

                if (this.getBooleanValueFromSettingName("Speed")) {
                    var5[2] = 1;
                }

                if (!mc.player.isOnGround()) {
                    this.field23809 = 0;
                } else {
                    this.field23809++;
                }

                if (this.field23811 != 1) {
                    if (this.field23811 >= 2) {
                        mc.player.inventory.currentItem = this.field23810;
                        mc.playerController.syncCurrentPlayItem();
                        this.field23811 = 0;
                    }

                    if (this.field23809 > 1) {
                        for (int var6 = 0; var6 < var5.length; var6++) {
                            if (var5[var6] != -1) {
                                if (var5[var6] != 6 && var5[var6] != 10) {
                                    if (this.field23808 > 60) {
                                        this.method16634(var1, var4, var5[var6]);
                                    }
                                } else if (this.field23808 > 18
                                        && !mc.player.isPotionActive(Effect.get(var5[var6]))
                                        && mc.player.getHealth() < this.getNumberValueBySettingName("Health") * 2.0F) {
                                    this.method16634(var1, var4, var5[var6]);
                                }
                            }
                        }
                    }
                } else {
                    this.field23811++;
                    mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                }
            }
        }
    }

    public float[] method16630() {
        double var3 = mc.player.getPosX() + mc.player.getMotion().x * 26.0;
        double var5 = mc.player.getBoundingBox().minY - 3.6;
        double var7 = mc.player.getPosZ() + mc.player.getMotion().z * 26.0;
        return !this.getBooleanValueFromSettingName("Predict") ? new float[]{mc.player.rotationYaw, 90.0F} : RotationUtil.rotationToPos(var3, var7, var5);
    }

    public int method16631() {
        int var3 = 5;

        for (int var4 = 36; var4 < 45; var4++) {
            if (!mc.player.container.getSlot(var4).getHasStack()) {
                var3 = var4 - 36;
                break;
            }
        }

        return var3;
    }

    public int method16632(int var1) {
        int var4 = 0;
        int var5 = 0;
        int var6 = -1;
        int var7 = 0;

        for (int var8 = 9; var8 < 45; var8++) {
            if (mc.player.container.getSlot(var8).getHasStack()) {
                ItemStack var9 = mc.player.container.getSlot(var8).getStack();
                if (var9.getItem() instanceof PotionItem) {
                    List<EffectInstance> var10 = InvManagerUtil.getPotionEffects(var9);
                    int var11 = this.method16633(var10);
                    if (var10 != null && !var10.isEmpty() && (this.getBooleanValueFromSettingName("Custom potion") || var11 == 1)) {
                        for (EffectInstance var13 : var10) {
                            int var14 = Effect.getId(var13.getPotion());
                            int var15 = var13.getAmplifier();
                            int var16 = var13.getDuration();
                            if (var14 == var1 && InvManagerUtil.isPotionItem(var9)) {
                                if (var15 <= var4) {
                                    if (var15 == var4 && var16 > var5) {
                                        var6 = var8;
                                        var7 = var11;
                                    } else if (var7 > var11 && var15 >= var4) {
                                        var7 = var11;
                                    }
                                } else {
                                    var6 = var8;
                                    var7 = var11;
                                }
                            }
                        }
                    }
                }
            }
        }

        return mc.player.isPotionActive(Effect.get(var1)) && mc.player.getActivePotionEffect(Effect.get(var1)).getAmplifier() >= var4
                ? -1
                : var6;
    }

    public int method16633(List<EffectInstance> var1) {
        ArrayList var4 = new ArrayList();
        int var5 = 0;

        for (EffectInstance var7 : var1) {
            int var8 = Effect.getId(var7.getPotion());
            if (!var4.contains(var8)) {
                var5++;
                var4.add(var8);
            }
        }

        return var5;
    }

    public void method16634(EventUpdateWalkingPlayer var1, int var2, int var3) {
        int var6 = this.method16632(var3);
        if (var6 != -1) {
            if (var6 < 36) {
                if (Client.getInstance().playerTracker.getMode() > 2) {
                    InvManagerUtil.clickSlot(var6, var2);
                }
            } else {
                this.field23808 = 0;
                int var7 = mc.player.inventory.currentItem;
                boolean var8 = Client.getInstance().moduleManager.getModuleByClass(Criticals.class).isEnabled()
                        && Client.getInstance().moduleManager.getModuleByClass(Criticals.class).getStringSettingValueByName("Type").equalsIgnoreCase("NoGround");
                float[] var9 = this.method16630();
                mc.player.inventory.currentItem = var6 - 36;
                mc.playerController.syncCurrentPlayItem();
                if (!this.getBooleanValueFromSettingName("Instant")) {
                    this.field23811 = 1;
                    var1.setYaw(var9[0]);
                    var1.setPitch(var9[1]);
                } else {
                    mc.getConnection().sendPacket(new CPlayerPacket.RotationPacket(var9[0], var9[1], !var8 && mc.player.isOnGround()));
                    mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.getConnection().sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                    mc.player.inventory.currentItem = var7;
                    mc.playerController.syncCurrentPlayItem();
                    KillAura.attackCooldown = 1;
                }

                this.field23810 = var7;
            }
        }
    }
}
