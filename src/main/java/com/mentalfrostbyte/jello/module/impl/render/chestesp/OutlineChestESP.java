package com.mentalfrostbyte.jello.module.impl.render.chestesp;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.render.jello.esp.util.Class2329;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
import com.mentalfrostbyte.jello.util.game.world.PositionUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TrappedChestTileEntity;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;
import team.sdhq.eventBus.annotations.EventTarget;

public class OutlineChestESP extends Module {
    public OutlineChestESP() {
        super(ModuleCategory.RENDER, "Outline", "Draws a line arround chests");
    }

    @EventTarget
    public void method16963(EventRender3D var1) {
        if (this.isEnabled()) {
            if (mc.player != null && mc.world != null) {
                this.method16965();
                RenderUtil.method11476();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.method16964(false);
                RenderUtil.method11477(Class2329.field15941);
                GL11.glLineWidth(3.0F);
                RenderSystem.alphaFunc(518, 0.0F);
                RenderSystem.enableAlphaTest();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.1F);
                GL11.glEnable(3042);
                GL11.glDisable(2896);
                this.method16964(true);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.method11478();
                this.method16966();
            }
        }
    }

    private void method16964(boolean var1) {
        int var4 = RenderUtil2.applyAlpha(this.getParent().parseSettingValueToIntBySettingName("Regular Color"), 0.7F);
        int var5 = RenderUtil2.applyAlpha(this.getParent().parseSettingValueToIntBySettingName("Ender Color"), 0.7F);
        int var6 = RenderUtil2.applyAlpha(this.getParent().parseSettingValueToIntBySettingName("Trapped Color"), 0.7F);

        for (TileEntity var8 : mc.world.loadedTileEntityList) {
            boolean var9 = var8 instanceof ChestTileEntity && !(var8 instanceof TrappedChestTileEntity)
                    && this.getParent().getBooleanValueFromSettingName("Show Regular Chests");
            boolean var10 = var8 instanceof EnderChestTileEntity && this.getParent().getBooleanValueFromSettingName("Show Ender Chests");
            boolean var11 = var8 instanceof TrappedChestTileEntity
                    && this.getParent().getBooleanValueFromSettingName("Show Trapped Chests");
            if (var9 || var10 || var11) {
                double var12 = PositionUtil.getRelativePosition(var8.getPos()).x;
                double var14 = PositionUtil.getRelativePosition(var8.getPos()).y;
                double var16 = PositionUtil.getRelativePosition(var8.getPos()).z;
                GL11.glDisable(2929);
                GL11.glEnable(3042);
                int var18 = var4;
                if (!(var8 instanceof EnderChestTileEntity)) {
                    if (var8 instanceof TrappedChestTileEntity) {
                        var18 = var6;
                    }
                } else {
                    var18 = var5;
                }

                BoundingBox var19 = new BoundingBox(
                        var8.getBlockState().getShape(mc.world, var8.getPos()).getBoundingBox().offset(var12, var14,
                                var16));
                if (var1) {
                    RenderUtil.renderWireframeBox(var19, 3.0F, var18);
                } else {
                    RenderUtil.render3DColoredBox(var19, ClientColors.LIGHT_GREYISH_BLUE.getColor());
                }

                GL11.glDisable(3042);
            }
        }
    }

    private void method16965() {
        GL11.glLineWidth(3.0F);
        GL11.glPointSize(3.0F);
        GL11.glEnable(2832);
        GL11.glEnable(2848);
        GL11.glEnable(3042);
        GL11.glDisable(2896);
        GL11.glEnable(3008);
        GL11.glDisable(3553);
        GL11.glDisable(2903);
        GL11.glDisable(2929);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.gameRenderer.lightmapTexture.disableLightmap();
    }

    private void method16966() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(2896);
        GL11.glEnable(3553);
        GL11.glEnable(2903);
        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
        TextureImpl.unbind();
        mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        mc.gameRenderer.lightmapTexture.enableLightmap();
    }
}
