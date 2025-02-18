package com.mentalfrostbyte.jello.module.impl.player;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventHandAnimation;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.managers.ViaManager;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;

import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.*;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3f;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

import java.util.ArrayList;
import java.util.Arrays;

public class OldHitting extends Module {
    public static boolean field23408 = false;
    private boolean field23409;

    public OldHitting() {
        super(ModuleCategory.PLAYER, "OldHitting", "Reverts to 1.7/1.8 hitting");
        this.registerSetting(new ModeSetting("Animation", "Animation mode", 0, "Vanilla", "Tap", "Tap2", "Slide", "Slide2", "Scale", "Leaked", "Ninja", "Down"));
        this.registerSetting(new BooleanSetting("Enhancements", "Fix some 1.8 server ViaVersion issues.", true));
        this.registerSetting(new BooleanSetting("Always", "Fake autoblock.", true));
        this.registerSetting(new NumberSetting<Float>("XPos", "Default X position of the main hand", 0, Float.class, -1, 1, 0.01F));
        this.registerSetting(new NumberSetting<Float>("YPos", "Default Y position of the main hand", 0, Float.class, -1, 1, 0.01F));
        this.registerSetting(new NumberSetting<Float>("ZPos", "Default Z position of the main hand", 0, Float.class, -1, 1, 0.01F));
        this.setAvailableOnClassic(true);
    }

    @EventTarget
    @HigherPriority
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() || ViaLoadingBase.getInstance().getTargetVersion().equalTo(ProtocolVersion.v1_8)) {
            if (var1.isPre()) {
                boolean holdingSword = mc.player.getHeldItemMainhand() != null && mc.player.getHeldItemMainhand().getItem() instanceof SwordItem;
                boolean killauraEnabled = Client.getInstance().moduleManager.getModuleByClass(KillAura.class).isEnabled();
                boolean var6 = true;
                if (!mc.player.isSneaking()
                        && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK
                        && !Client.getInstance().moduleManager.getModuleByClass(KillAura.class).isEnabled()) {
                    BlockRayTraceResult var7 = (BlockRayTraceResult) mc.objectMouseOver;
                    BlockPos var8 = var7.getPos();
                    Block var9 = mc.world.getBlockState(var8).getBlock();
                    ArrayList var10 = new ArrayList<Block>(
                            Arrays.asList(
                                    Blocks.CHEST,
                                    Blocks.ENDER_CHEST,
                                    Blocks.TRAPPED_CHEST,
                                    Blocks.CRAFTING_TABLE,
                                    Blocks.BEACON,
                                    Blocks.FURNACE,
                                    Blocks.BLAST_FURNACE,
                                    Blocks.ENCHANTING_TABLE,
                                    Blocks.ANVIL,
                                    Blocks.CHIPPED_ANVIL,
                                    Blocks.DAMAGED_ANVIL,
                                    Blocks.DISPENSER,
                                    Blocks.NOTE_BLOCK,
                                    Blocks.LEVER,
                                    Blocks.HOPPER,
                                    Blocks.DROPPER,
                                    Blocks.REPEATER,
                                    Blocks.COMPARATOR
                            )
                    );
                    if (var10.contains(var9)
                            || var9 instanceof WoodButtonBlock
                            || var9 instanceof StoneButtonBlock
                            || var9 instanceof FenceGateBlock
                            || var9 instanceof DoorBlock && var9 != Blocks.IRON_DOOR) {
                        var6 = false;
                    }
                }

                field23408 = mc.player.getItemInUseCount() > 0 && holdingSword && var6 || killauraEnabled && KillAura.targetEntity != null && getBooleanValueFromSettingName("Always");
                //JelloPortalFixes.doBlock
                if (!field23408) {
                    if (ViaManager.entities.contains(mc.player)) {
                        ViaManager.entities.remove(mc.player);
                    }
                } else if (!ViaManager.entities.contains(mc.player)) {
                    ViaManager.entities.add(mc.player);
                }

                if (field23408 && !this.field23409) {
                    this.field23409 = !this.field23409;

                } else if (!field23408 && this.field23409) {
                    this.field23409 = !this.field23409;
                }
            }
        }
    }


    @EventTarget
    @LowerPriority
    public void method16022(EventHandAnimation var1) {
        if (this.isEnabled() || ViaLoadingBase.getInstance().getTargetVersion().equalTo(ProtocolVersion.v1_8)) {
            float getSwingProgress = var1.method13924();;
            var1.getMatrix().translate(getNumberValueBySettingName("XPos"), getNumberValueBySettingName("YPos"), getNumberValueBySettingName("ZPos"));
            if (var1.method13926() && var1.getHand() == HandSide.LEFT && var1.getItemStack().getItem() instanceof ShieldItem) {
                var1.renderBlocking(false);
            } else if (var1.getHand() != HandSide.LEFT || !field23408) {
                if (field23408 && var1.method13926()) {
                    var1.cancelled = true;
                    String var5 = this.getStringSettingValueByName("Animation");
                    switch (var5) {
                        case "Vanilla":
                            this.method16026(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Tap":
                            this.method16027(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Tap2":
                            this.method16028(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Slide":
                            this.method16029(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Slide2":
                            this.method16030(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Scale":
                            this.method16031(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Leaked":
                            this.method16032(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Ninja":
                            this.method16025(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Tomy":
                            this.method16024(0.0F, getSwingProgress, var1.getMatrix());
                            break;
                        case "Down":
                            this.method16033(0.0F, getSwingProgress, var1.getMatrix());
                    }
                }
            }
        }
    }

    private void rotate(float var1, float var2, float var3, float var4, MatrixStack var5) {
        var5.rotate(new Vector3f(var2, var3, var4).rotationDegrees(var1));
    }

    private void method16024(float var1, float var2, MatrixStack var3) {
        var3.translate(0.48F, -0.55F, -0.71999997F);
        var3.translate(0.0, (double) (var1 * -0.6F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        float var6 = MathHelper.sin(var2 * var2 * (float) Math.PI);
        float var7 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(var6 * -20.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(var7 * -20.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(var7 * -69.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        float var8 = 1.2F;
        var3.scale(var8, var8, var8);
    }

    private void method16025(float var1, float var2, MatrixStack var3) {
        var3.translate(0.48F, -0.39F, -0.71999997F);
        var3.translate(0.0, (double) (var1 * -0.6F), 0.0);
        this.rotate(100.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-50.0F, 0.0F, 0.0F, 1.0F, var3);
        float var6 = MathHelper.sin(var2 * (float) Math.PI);
        float var7 = MathHelper.sin(var2 * (float) Math.PI);
        this.rotate(var6 * -10.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(var7 * -30.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(var7 * 109.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(-90.0F, 1.0F, 0.0F, 0.0F, var3);
        float var8 = 1.2F;
        var3.scale(var8, var8, var8);
    }

    private void method16026(float var1, float var2, MatrixStack var3) {
        var3.translate(0.48F, -0.55F, -0.71999997F);
        var3.translate(0.0, (double) (var1 * -0.6F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        float var6 = MathHelper.sin(var2 * var2 * (float) Math.PI);
        float var7 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(var6 * -20.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(var7 * -20.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(var7 * -69.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        float var8 = 1.2F;
        var3.scale(var8, var8, var8);
    }

    private void method16027(float var1, float var2, MatrixStack var3) {
        var3.translate(0.0, -3.5, 0.0);
        var3.translate(0.56F, -0.52F, -0.72F);
        var3.translate(0.56F, -0.22F, -0.71999997F);
        this.rotate(45.0F, 0.0F, 1.0F, 0.0F, var3);
        float var6 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(0.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(var6 * -9.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(-9.0F, 0.0F, 0.0F, 1.0F, var3);
        var3.translate(0.0, 3.2F, 0.0);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        var3.scale(2.7F, 2.7F, 2.7F);
    }

    private void method16028(float var1, float swingProgress, MatrixStack matrixStack) {
        matrixStack.translate(0.648F, -0.55F, -0.71999997F);
        matrixStack.translate(0.0, (double) (var1 * -0.6F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, matrixStack);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, matrixStack);
        float var6 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, matrixStack);
        this.rotate(-var6 * 10.0F, 1.0F, -2.0F, 3.0F, matrixStack);
        float var7 = 1.2F;
        matrixStack.scale(var7, var7, var7);
    }

    private void method16029(float var1, float var2, MatrixStack var3) {
        var3.translate(0.648F, -0.55F, -0.71999997F);
        var3.translate(0.0, (double) (var1 * -0.6F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        float var6 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(-var6 * 20.0F, 1.0F, 0.0F, 0.0F, var3);
        float var7 = 1.2F;
        var3.scale(var7, var7, var7);
    }

    private void method16030(float var1, float var2, MatrixStack var3) {
        var3.translate(0.48F, -0.55F, -0.71999997F);
        var3.translate(0.0, (double) (var1 * -0.6F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        float var6 = MathHelper.sin(var2 * var2 * (float) Math.PI);
        float var7 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(var6 * -20.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(var7 * -20.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(var7 * -69.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        float var8 = 1.2F;
        var3.scale(var8, var8, var8);
    }

    private void method16031(float var1, float var2, MatrixStack var3) {
        var3.translate(0.48F, -0.55F, -0.71999997F);
        var3.translate(0.0, (double) (var1 * -0.2F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        float var6 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        float var7 = 1.2F - var6 * 0.3F;
        var3.scale(var7, var7, var7);
    }

    private void method16032(float var1, float var2, MatrixStack var3) {
        var3.translate(0.56, -0.52, -0.72);
        float var6 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        this.rotate(var6 * 10.0F, -4.0F, -2.0F, 5.0F, var3);
        this.rotate(var6 * 30.0F, 1.0F, -0.0F, -1.0F, var3);
    }

    private void method16033(float var1, float var2, MatrixStack var3) {
        float var6 = MathHelper.sin(MathHelper.sqrt(var2) * (float) Math.PI);
        var3.translate(0.48F, -0.55F, -0.71999997F);
        var3.translate(0.0, (double) (var6 * -0.2F), 0.0);
        this.rotate(77.0F, 0.0F, 1.0F, 0.0F, var3);
        this.rotate(-10.0F, 0.0F, 0.0F, 1.0F, var3);
        this.rotate(-80.0F, 1.0F, 0.0F, 0.0F, var3);
        float var7 = 1.2F;
        var3.scale(var7, var7, var7);
    }
}
