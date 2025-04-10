package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.google.common.collect.Lists;
import com.mentalfrostbyte.jello.command.impl.Peek;

import com.mentalfrostbyte.jello.event.impl.game.action.EventMouse;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderShulker;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import com.mentalfrostbyte.jello.managers.GuiManager;

import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
import com.mentalfrostbyte.jello.util.game.world.PositionUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.TextureImpl;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class ShulkerInfo extends Module {
    public int field23841 = -1;
    public double field23842;

    public ShulkerInfo() {
        super(ModuleCategory.GUI, "ShulkerInfo", "Shows shulker information");
        this.setAvailableOnClassic(false);
    }

    @EventTarget
    public void onMouse(EventMouse var1) {
        this.field23842 = this.field23842 - var1.method13980();
    }

    public int method16670() {
        int var3 = !(this.field23842 > 0.0) ? (int) Math.floor(this.field23842 / 5.0)
                : (int) Math.ceil(this.field23842 / 5.0);
        this.field23842 = 0.0;
        return var3;
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (this.isEnabled()) {
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);

            for (Entity var5 : PlayerUtil.getEntitesInWorld()) {
                if (var5 instanceof ItemEntity var6) {
                    if (!(var6.getItem().getItem() instanceof BlockItem)
                            || !(((BlockItem) var6.getItem().getItem()).getBlock() instanceof ShulkerBoxBlock)) {
                        return;
                    }

                    this.method16674(
                            PositionUtil.getEntityPosition(var5).x,
                            PositionUtil.getEntityPosition(var5).y + (double) var5.getHeight(),
                            PositionUtil.getEntityPosition(var5).z,
                            var5,
                            0.8F);
                    if (this.method16672(var6)) {
                        double var7 = PositionUtil.getEntityPosition(var5).x
                                - mc.gameRenderer.getActiveRenderInfo().getPos().getX();
                        double var9 = PositionUtil.getEntityPosition(var5).y
                                - mc.gameRenderer.getActiveRenderInfo().getPos().getY();
                        double var11 = PositionUtil.getEntityPosition(var5).z
                                - mc.gameRenderer.getActiveRenderInfo().getPos().getZ();
                        float var13 = 0.3F;
                        GL11.glEnable(3042);
                        GL11.glAlphaFunc(516, 0.0F);
                        GL11.glEnable(2848);
                        RenderUtil.render3DColoredBox(
                                new BoundingBox(
                                        var7 - (double) var13, var9 + 0.01F, var11 - (double) var13,
                                        var7 + (double) var13, var9 + (double) (var13 * 2.0F), var11 + (double) var13),
                                RenderUtil.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F));
                        RenderUtil.renderWireframeBox(
                                new BoundingBox(
                                        var7 - (double) var13, var9 + 0.01F, var11 - (double) var13,
                                        var7 + (double) var13, var9 + (double) (var13 * 2.0F), var11 + (double) var13),
                                3.0F,
                                RenderUtil.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F));
                        GL11.glDisable(3042);
                        if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                            mc.gameSettings.keyBindUseItem.pressed = false;
                            Peek.peekContainerItem(var6.getItem());
                        }
                    }

                    GL11.glColor3f(1.0F, 1.0F, 1.0F);
                }
            }

            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            TextureImpl.unbind();
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        }
    }

    public boolean method16672(ItemEntity var1) {
        if (mc.player.getDistance(var1) > 5.0F) {
            return false;
        } else {
            float var4 = (float) Math.sqrt(6.0 / PositionUtil.calculateDistanceSquared(var1));
            float var5 = 10.0F * var4;
            double var6 = var1.getPosX() - mc.player.getPosX();
            double var8 = var1.getPosY() - mc.player.getPosY() - (double) mc.player.getHeight() + 0.4F;
            double var10 = var1.getPosZ() - mc.player.getPosZ();
            double var12 = MathHelper.sqrt(var6 * var6 + var10 * var10);
            float var14 = RotationUtil.calculate(mc.player.rotationYaw,
                    (float) (Math.atan2(var10, var6) * 180.0 / Math.PI) - 90.0F, 360.0F);
            float var15 = RotationUtil.calculate(mc.player.rotationPitch,
                    (float) (-(Math.atan2(var8, var12) * 180.0 / Math.PI)), 360.0F);
            return this.method16673(mc.player.rotationYaw, var14) <= var5
                    && this.method16673(mc.player.rotationPitch, var15) <= var5;
        }
    }

    public float method16673(float var1, float var2) {
        float var5 = Math.abs(var2 - var1) % 360.0F;
        return !(var5 > 180.0F) ? var5 : 360.0F - var5;
    }

    public void method16674(double var1, double var3, double var5, Entity var7, float var8) {
        TrueTypeFont var11 = ResourceRegistry.JelloLightFont25;
        String var12 = var7.getName().getUnformattedComponentText();
        float var13 = (float) (var1 - mc.gameRenderer.getActiveRenderInfo().getPos().getX());
        float var14 = (float) (var3 - mc.gameRenderer.getActiveRenderInfo().getPos().getY());
        float var15 = (float) (var5 - mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDisable(2896);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslated(var13, var14 + 0.6F - 0.33333334F * (1.0F - var8), var15);
        GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getPitch(), 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.009F * var8, -0.009F * var8, -0.009F * var8);
        GL11.glTranslated(-var11.getWidth(var12) / 2, 0.0, 0.0);
        List var16 = this.method16678(((ItemEntity) var7).getItem());
        this.method16676(-87, -70, var16, ((ItemEntity) var7).getItem().getDisplayName().getString(), false);
        GL11.glPopMatrix();
        GL11.glEnable(2929);
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    @EventTarget
    public void onRenderShulker(EventRenderShulker event) {
        if (this.isEnabled()) {
            if (mc.currentScreen instanceof ContainerScreen var4) {
                Slot var5 = ((ContainerScreen<?>) var4).hoveredSlot;
                if (var5 != null
                        && var5.getHasStack()
                        && var5.getStack().getItem() instanceof BlockItem
                        && ((BlockItem) var5.getStack().getItem()).getBlock() instanceof ShulkerBoxBlock) {
                    ItemStack var6 = var5.getStack();
                    List var7 = this.method16678(var6);
                    int var8 = Math.max(-1, Math.min(1, this.method16670()));
                    if (var8 != 0 || this.field23841 != -1) {
                        this.field23841 = Math.max(0, Math.min(var7.size() - 1, this.field23841 - var8));
                    }

                    GL11.glPushMatrix();
                    GL11.glTranslatef(0.0F, 0.0F, 1000.0F);
                    GL11.glScalef(1.0F / RenderUtil.getGuiScaleFactor(), 1.0F / RenderUtil.getGuiScaleFactor(), 0.0F);
                    int var9 = Math.round(16.0F * RenderUtil.getGuiScaleFactor());
                    int var10 = 1;
                    int var11 = 12;
                    int var13 = (int) (mc.mouseHelper.getMouseX() * (double) GuiManager.scaleFactor
                            - (double) (9 * (var9 + var10)) - (double) (var11 * 3));
                    int var14 = (int) (mc.mouseHelper.getMouseY() * (double) GuiManager.scaleFactor - 33.0);
                    this.method16676(var13, var14, var7, var6.getDisplayName().getString(), true);
                    GL11.glPopMatrix();
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableBlend();
                    RenderSystem.alphaFunc(518, 0.1F);
                } else {
                    this.field23841 = -1;
                    this.field23842 = 0.0;
                }
            }
        }
    }

    public void method16676(int var1, int var2, List<ItemStack> var3, String var4, boolean var5) {
        int var8 = 12;
        int var9 = ResourceRegistry.JelloLightFont25.getHeight();
        int var10 = Math.round(16.0F * RenderUtil.getGuiScaleFactor());
        int var11 = 1;
        int var12 = (int) Math.ceil((float) var3.size() / 9.0F) * (var10 + var11) + var8 * 2 + var9;
        int var13 = 9 * (var10 + var11) + var8 * 2;
        RenderSystem.disableLighting();
        GL11.glAlphaFunc(519, 0.0F);
        if (!var5) {
            RenderUtil.drawRect2(
                    (float) var1,
                    (float) var2,
                    (float) var13,
                    (float) var12,
                    RenderUtil.applyAlpha(RenderUtil.method17690(ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                            ClientColors.DEEP_TEAL.getColor(), 75.0F), 0.7F));
            RenderUtil.drawRoundedRect((float) var1, (float) var2, (float) var13, (float) var12, 10.0F, 0.5F);
        } else {
            RenderUtil.method11467(var1, var2, var13, var12,
                    RenderUtil.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.94F));
        }

        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont25, (float) (var1 + var8), (float) (var2 + var8 - 3), var4,
                RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F));
        RenderSystem.enableLighting();

        for (int var14 = 0; var14 < var3.size(); var14++) {
            ItemStack var15 = var3.get(var14);
            int var16 = var2 + var9 + var8 + var14 / 9 * (var10 + var11);
            int var17 = var1 + var8 + var14 % 9 * (var10 + var11);
            RenderSystem.disableLighting();
            if (var14 == this.field23841 && var5) {
                RenderUtil.drawRect2((float) var17, (float) var16, (float) var10, (float) var10,
                        RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.15F));
            }

            RenderUtil.renderItem(var15, var17, var16, var10, var10);
            if (var15.getCount() > 1) {
                int var18 = var10 - ResourceRegistry.JelloLightFont20.getWidth("" + var15.getCount());
                int var19 = ResourceRegistry.JelloLightFont20.getWidth("" + var15.getCount());
                GL11.glAlphaFunc(519, 0.0F);
                RenderSystem.disableLighting();
                RenderUtil.drawImage2(
                        (float) (var17 + var18 - 17 - var19 / 4),
                        (float) (var16 + 7),
                        (float) (40 + var19),
                        40.0F,
                        Resources.shadowPNG,
                        RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F),
                        false);
                RenderUtil.drawString(ResourceRegistry.JelloLightFont20, (float) (var17 + var18), (float) (var16 + 13),
                        "" + var15.getCount(), ClientColors.LIGHT_GREYISH_BLUE.getColor());
                RenderSystem.enableLighting();
            }

            RenderSystem.enableLighting();
        }

        for (int var23 = 0; var23 < var3.size(); var23++) {
            ItemStack var24 = var3.get(var23);
            int var25 = var2 + var9 + var8 + var23 / 9 * (var10 + var11);
            int var27 = var1 + var8 + var23 % 9 * (var10 + var11);
            if (var23 == this.field23841 && var5) {
                RenderSystem.disableLighting();
                int var29 = mc.fontRenderer.getStringWidth(var24.getDisplayName().getUnformattedComponentText());
                List var32 = this.method16677(var24);

                for (int var20 = 0; var20 < var32.size(); var20++) {
                    var29 = Math.max(var29, mc.fontRenderer.getStringWidth((String) var32.get(var20)));
                }

                var29 = (int) ((float) var29 * RenderUtil.getGuiScaleFactor());
                int var33 = var32.size();
                RenderUtil.drawRect2(
                        (float) var27,
                        (float) (var25 + var10),
                        (float) var29 + 9.0F * RenderUtil.getGuiScaleFactor(),
                        10.0F * RenderUtil.getGuiScaleFactor() * (float) var33 + 7.0F * RenderUtil.getGuiScaleFactor(),
                        RenderUtil.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F));
                GL11.glPushMatrix();
                GL11.glScalef(RenderUtil.getGuiScaleFactor(), RenderUtil.getGuiScaleFactor(), 0.0F);
                var25 = (int) ((float) var25 * (1.0F / RenderUtil.getGuiScaleFactor()));
                var27 = (int) ((float) var27 * (1.0F / RenderUtil.getGuiScaleFactor()));
                var10 = (int) ((float) var10 * (1.0F / RenderUtil.getGuiScaleFactor()));
                var29 = (int) ((float) var29 * (1.0F / RenderUtil.getGuiScaleFactor()));

                for (int var21 = 0; var21 < var32.size(); var21++) {
                    String var22 = (String) var32.get(var21);
                    mc.fontRenderer.renderString(
                            var22,
                            (float) (var27 + 5),
                            5.3F + (float) var25 + (float) var10 + (float) (var21 * 10),
                            ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                            new MatrixStack().getLast().getMatrix(),
                            false,
                            false);
                }

                GL11.glPopMatrix();
                RenderSystem.enableLighting();
            }
        }
    }

    public List<String> method16677(ItemStack var1) {
        List<ITextComponent> var4 = var1.getTooltip(mc.player,
                mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

        List<String> var5 = Lists.newArrayList();

        for (ITextComponent var7 : var4) {
            var5.add(var7.getString());
        }

        return var5;
    }


    public List<ItemStack> method16678(ItemStack var1) {
        List<ItemStack> var4 = new ArrayList();
        CompoundNBT var5 = var1.getTag();
        if (var5 != null && var5.contains("BlockEntityTag", 10)) {
            CompoundNBT var6 = var5.getCompound("BlockEntityTag");
            Peek.method18338(var6);
            if (var6.contains("Items", 9)) {
                NonNullList<ItemStack> var7 = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(var6, var7);

                for (ItemStack var9 : var7) {
                    if (!var9.isEmpty()) {
                        boolean var10 = true;

                        for (ItemStack var12 : var4) {
                            if (var9.isItemEqual(var12) && ItemStack.areItemStacksEqual(var12, var9)) {
                                var12.setCount(var9.getCount() + var12.getCount());
                                var10 = false;
                            }
                        }

                        if (var10) {
                            var4.add(var9);
                        }
                    }
                }
            }
        }

        return var4;
    }
}
