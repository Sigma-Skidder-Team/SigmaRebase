package com.mentalfrostbyte.jello.util.render;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.unmapped.Keys;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.managers.impl.music.Class2329;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.unmapped.Class7820;
import org.newdawn.slick.opengl.TextureImpl;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class RenderUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean field18461 = false;

    private static final Stack<IntBuffer> buffer = new Stack<>();

    public static void endScissor() {
        if (buffer.isEmpty()) {
            GL11.glDisable(GL_SCISSOR_TEST);
        } else {
            IntBuffer var2 = buffer.pop();
            GL11.glScissor(var2.get(0), var2.get(1), var2.get(2), var2.get(3));
        }
    }

    public static void drawPortalBackground(int var0, int var1, int var2, int var3) {
        method11421(var0, var1, var2, var3, false);
    }

    public static float method11417() {
        return (float) mc.getMainWindow().getGuiScaleFactor();
    }

    public static float[] method11416(int var0, int var1) {
        FloatBuffer var4 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(2982, var4);
        float var5 = var4.get(0) * (float)var0 + var4.get(4) * (float)var1 + var4.get(8) * 0.0F + var4.get(12);
        float var6 = var4.get(1) * (float)var0 + var4.get(5) * (float)var1 + var4.get(9) * 0.0F + var4.get(13);
        float var7 = var4.get(3) * (float)var0 + var4.get(7) * (float)var1 + var4.get(11) * 0.0F + var4.get(15);
        var5 /= var7;
        var6 /= var7;
        return new float[]{(float)Math.round(var5 * method11417()), (float)Math.round(var6 * method11417())};
    }

    public static void method11421(int var0, int var1, int var2, int var3, boolean var4) {
        if (!var4) {
            var0 = (int)((float)var0 * GuiManager.scaleFactor);
            var1 = (int)((float)var1 * GuiManager.scaleFactor);
            var2 = (int)((float)var2 * GuiManager.scaleFactor);
            var3 = (int)((float)var3 * GuiManager.scaleFactor);
        } else {
            float[] var7 = method11416(var0, var1);
            var0 = (int)var7[0];
            var1 = (int)var7[1];
            float[] var8 = method11416(var2, var3);
            var2 = (int)var8[0];
            var3 = (int)var8[1];
        }

        if (GL11.glIsEnabled(3089)) {
            IntBuffer var17 = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(3088, var17);
            buffer.push(var17);
            int var18 = var17.get(0);
            int var9 = mc.getMainWindow().getFramebufferHeight() - var17.get(1) - var17.get(3);
            int var10 = var18 + var17.get(2);
            int var11 = var9 + var17.get(3);
            if (var0 < var18) {
                var0 = var18;
            }

            if (var1 < var9) {
                var1 = var9;
            }

            if (var2 > var10) {
                var2 = var10;
            }

            if (var3 > var11) {
                var3 = var11;
            }

            if (var1 > var3) {
                var3 = var1;
            }

            if (var0 > var2) {
                var2 = var0;
            }
        }

        int var19 = mc.getMainWindow().getFramebufferHeight() - var3;
        int var20 = var2 - var0;
        int var21 = var3 - var1;
        GL11.glEnable(3089);
        if (var20 >= 0 && var21 >= 0) {
            GL11.glScissor(var0, var19, var20, var21);
        }
    }

    public static void drawImage(float x, float y, float var2, float var3, Texture tex, float alphaValue) {
        drawImage(x, y, var2, var3, tex, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alphaValue));
    }

    public static void drawImage(float var0, float var1, float var2, float var3, Texture var4) {
        drawImage(var0, var1, var2, var3, var4, -1);
    }

    public static void drawImage(float var0, float var1, float var2, float var3, Texture var4, int var5) {
        drawImage(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F, (float)var4.getImageWidth(), (float)var4.getImageHeight(), true);
    }

    public static void method11450(float var0, float var1, float var2, float var3, Texture var4, int var5, boolean var6) {
        drawImage(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F, (float)var4.getImageWidth(), (float)var4.getImageHeight(), var6);
    }

    public static void method11451(float var0, float var1, float var2, float var3, Texture var4, int var5, float var6, float var7, float var8, float var9) {
        drawImage(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, true);
    }

    public static void drawImage(float var0, float var1, float var2, float var3, Texture var4, int var5, float var6, float var7, float var8, float var9, boolean var10) {
        if (var4 != null) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
            var0 = (float)Math.round(var0);
            var2 = (float)Math.round(var2);
            var1 = (float)Math.round(var1);
            var3 = (float)Math.round(var3);
            float var13 = (float)(var5 >> 24 & 0xFF) / 255.0F;
            float var14 = (float)(var5 >> 16 & 0xFF) / 255.0F;
            float var15 = (float)(var5 >> 8 & 0xFF) / 255.0F;
            float var16 = (float)(var5 & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.color4f(var14, var15, var16, var13);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            var4.bind();
            float var17 = var2 / (float)var4.getTextureWidth() / (var2 / (float)var4.getImageWidth());
            float var18 = var3 / (float)var4.getTextureHeight() / (var3 / (float)var4.getImageHeight());
            float var19 = var8 / (float)var4.getImageWidth() * var17;
            float var20 = var9 / (float)var4.getImageHeight() * var18;
            float var21 = var6 / (float)var4.getImageWidth() * var17;
            float var22 = var7 / (float)var4.getImageHeight() * var18;
            if (!var10) {
                GL11.glTexParameteri(3553, 10240, 9729);
            } else {
                GL11.glTexParameteri(3553, 10240, 9728);
            }

            GL11.glBegin(7);
            GL11.glTexCoord2f(var21, var22);
            GL11.glVertex2f(var0, var1);
            GL11.glTexCoord2f(var21, var22 + var20);
            GL11.glVertex2f(var0, var1 + var3);
            GL11.glTexCoord2f(var21 + var19, var22 + var20);
            GL11.glVertex2f(var0 + var2, var1 + var3);
            GL11.glTexCoord2f(var21 + var19, var22);
            GL11.glVertex2f(var0 + var2, var1);
            GL11.glEnd();
            GL11.glDisable(3553);
            GL11.glDisable(3042);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void drawRoundedRect2(float var0, float var1, float var2, float var3, int var4) {
        drawRoundedRect(var0, var1, var0 + var2, var1 + var3, var4);
    }

    public static void drawRoundedRect(float var0, float var1, float var2, float var3, float var4, int var5) {
        drawRoundedRect(var0, var1 + var4, var0 + var2, var1 + var3 - var4, var5);
        drawRoundedRect(var0 + var4, var1, var0 + var2 - var4, var1 + var4, var5);
        drawRoundedRect(var0 + var4, var1 + var3 - var4, var0 + var2 - var4, var1 + var3, var5);
        method11418(var0, var1, var0 + var4, var1 + var4);
        method11438(var0 + var4, var1 + var4, var4 * 2.0F, var5);
        endScissor();
        method11418(var0 + var2 - var4, var1, var0 + var2, var1 + var4);
        method11438(var0 - var4 + var2, var1 + var4, var4 * 2.0F, var5);
        endScissor();
        method11418(var0, var1 + var3 - var4, var0 + var4, var1 + var3);
        method11438(var0 + var4, var1 - var4 + var3, var4 * 2.0F, var5);
        endScissor();
        method11418(var0 + var2 - var4, var1 + var3 - var4, var0 + var2, var1 + var3);
        method11438(var0 - var4 + var2, var1 - var4 + var3, var4 * 2.0F, var5);
        endScissor();
    }

    public static void method11418(float var0, float var1, float var2, float var3) {
        method11421((int)var0, (int)var1, (int)var2, (int)var3, true);
    }

    public static void method11438(float var0, float var1, float var2, int var3) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var6 = (float)(var3 >> 24 & 0xFF) / 255.0F;
        float var7 = (float)(var3 >> 16 & 0xFF) / 255.0F;
        float var8 = (float)(var3 >> 8 & 0xFF) / 255.0F;
        float var9 = (float)(var3 & 0xFF) / 255.0F;
        Tessellator var10 = Tessellator.getInstance();
        BufferBuilder var11 = var10.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var7, var8, var9, var6);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glPointSize(var2 * GuiManager.scaleFactor);
        GL11.glBegin(0);
        GL11.glVertex2f(var0, var1);
        GL11.glEnd();
        GL11.glDisable(2832);
        GL11.glDisable(3042);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(float var0, float var1, float var2, float var3, int var4) {
        if (var0 < var2) {
            int var7 = (int)var0;
            var0 = var2;
            var2 = (float)var7;
        }

        if (var1 < var3) {
            int var13 = (int)var1;
            var1 = var3;
            var3 = (float)var13;
        }

        float var14 = (float)(var4 >> 24 & 0xFF) / 255.0F;
        float var8 = (float)(var4 >> 16 & 0xFF) / 255.0F;
        float var9 = (float)(var4 >> 8 & 0xFF) / 255.0F;
        float var10 = (float)(var4 & 0xFF) / 255.0F;
        Tessellator var11 = Tessellator.getInstance();
        BufferBuilder var12 = var11.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(var8, var9, var10, var14);
        var12.begin(7, DefaultVertexFormats.POSITION);
        var12.pos((double)var0, (double)var3, 0.0).endVertex();
        var12.pos((double)var2, (double)var3, 0.0).endVertex();
        var12.pos((double)var2, (double)var1, 0.0).endVertex();
        var12.pos((double)var0, (double)var1, 0.0).endVertex();
        var11.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    public static void drawString(TrueTypeFont res, float var1, float var2, String string, int var4, Class2218 var5, Class2218 var6) {
        drawString(res, var1, var2, string, var4, var5, var6, false);
    }

    public static void drawString(TrueTypeFont font, float var1, float var2, String string, int color, Class2218 var5, Class2218 var6, boolean var7) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        int var10 = 0;
        int var11 = 0;
        switch (Class7820.field33554[var5.ordinal()]) {
            case 1:
                var10 = -font.getWidth(string) / 2;
                break;
            case 2:
                var10 = -font.getWidth(string);
        }

        switch (Class7820.field33554[var6.ordinal()]) {
            case 1:
                var11 = -font.getHeight(string) / 2;
                break;
            case 3:
                var11 = -font.getHeight(string);
        }

        float var12 = (float)(color >> 24 & 0xFF) / 255.0F;
        float var13 = (float)(color >> 16 & 0xFF) / 255.0F;
        float var14 = (float)(color >> 8 & 0xFF) / 255.0F;
        float var15 = (float)(color & 0xFF) / 255.0F;
        GL11.glPushMatrix();
        boolean var16 = false;
        if ((double) GuiManager.scaleFactor == 2.0) {
            if (font == ResourceRegistry.JelloLightFont20) {
                font = ResourceRegistry.JelloLightFont40;
            } else if (font == ResourceRegistry.JelloLightFont25) {
                font = ResourceRegistry.JelloLightFont50;
            } else if (font == ResourceRegistry.JelloLightFont12) {
                font = ResourceRegistry.JelloLightFont24;
            } else if (font == ResourceRegistry.JelloLightFont14) {
                font = ResourceRegistry.JelloLightFont28;
            } else if (font == ResourceRegistry.JelloLightFont18) {
                font = ResourceRegistry.JelloLightFont36;
            } else if (font == ResourceRegistry.RegularFont20) {
                font = ResourceRegistry.RegularFont40;
            } else if (font == ResourceRegistry.JelloMediumFont20) {
                font = ResourceRegistry.JelloMediumFont40;
            } else if (font == ResourceRegistry.JelloMediumFont25) {
                font = ResourceRegistry.JelloMediumFont50;
            } else {
                var16 = true;
            }

            if (!var16) {
                float[] var17 = method11416((int)var1, (int)var2);
                int var18 = (int)var17[0];
                int var19 = (int)var17[1];
                GL11.glTranslatef(var1, var2, 0.0F);
                GL11.glScalef(1.0F / GuiManager.scaleFactor, 1.0F / GuiManager.scaleFactor, 1.0F / GuiManager.scaleFactor);
                GL11.glTranslatef(-var1, -var2, 0.0F);
                var10 = (int)((float)var10 * GuiManager.scaleFactor);
                var11 = (int)((float)var11 * GuiManager.scaleFactor);
            }
        }

        RenderSystem.enableBlend();
        GL11.glBlendFunc(770, 771);
        if (var7) {
            font.drawString((float)Math.round(var1 + (float)var10), (float)(Math.round(var2 + (float)var11) + 2), string, new Color(0.0F, 0.0F, 0.0F, 0.35F));
        }

        if (string != null) {
            font.drawString((float)Math.round(var1 + (float)var10), (float)Math.round(var2 + (float)var11), string, new Color(var13, var14, var15, var12));
        }

        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static void method11415(CustomGuiScreen var0) {
        method11421(var0.getXA(), var0.getYA(), var0.getWidthA() + var0.getXA(), var0.getHeightA() + var0.getYA(), true);
    }

    public static void method11474(float var0, float var1, float var2, float var3, float var4, int var5) {
        drawRoundedRect(var0, var1 + var4, var0 + var2, var1 + var3 - var4, var5);
        drawRoundedRect(var0 + var4, var1, var0 + var2 - var4, var1 + var3, var5);
        FloatBuffer var8 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(2982, var8);
        float var9 = 1.0F;
        method11438(var0 + var4, var1 + var4, var4 * 2.0F * var9, var5);
        method11438(var0 - var4 + var2, var1 + var4, var4 * 2.0F * var9, var5);
        method11438(var0 + var4, var1 - var4 + var3, var4 * 2.0F * var9, var5);
        method11438(var0 - var4 + var2, var1 - var4 + var3, var4 * 2.0F * var9, var5);
    }

    public static void drawString(TrueTypeFont font, float var1, float var2, String string, int color) {
        drawString(font, var1, var2, string, color, Class2218.field14488, Class2218.field14489, false);
    }

    public static void drawRoundedRect(float var0, float var1, float var2, float var3, float var4, float var5) {
        GL11.glAlphaFunc(519, 0.0F);
        int var8 = ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5);
        drawImage(var0 - var4, var1 - var4, var4, var4, Resources.shadowCorner1PNG, var8);
        drawImage(var0 + var2, var1 - var4, var4, var4, Resources.shadowCorner2PNG, var8);
        drawImage(var0 - var4, var1 + var3, var4, var4, Resources.shadowCorner3PNG, var8);
        drawImage(var0 + var2, var1 + var3, var4, var4, Resources.shadowCorner4PNG, var8);
        method11450(var0 - var4, var1, var4, var3, Resources.shadowLeftPNG, var8, false);
        method11450(var0 + var2, var1, var4, var3, Resources.shadowRightPNG, var8, false);
        method11450(var0, var1 - var4, var2, var4, Resources.shadowTopPNG, var8, false);
        method11450(var0, var1 + var3, var2, var4, Resources.shadowBottomPNG, var8, false);
    }

    public static void startScissor(float var0, float var1, float var2, float var3) {
        method11421((int)var0, (int)var1, (int)var0 + (int)var2, (int)var1 + (int)var3, true);
    }

    public static void method11465(int var0, int var1, int var2, int var3, int var4) {
        method11466(var0, var1, var2, var3, var4, var0, var1);
    }

    public static void method11466(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        int var9 = 36;
        int var10 = 10;
        int var11 = var9 - var10;
        drawRoundedRect((float)(var0 + var10), (float)(var1 + var10), (float)(var0 + var2 - var10), (float)(var1 + var3 - var10), var4);
        drawImage((float)(var0 - var11), (float)(var1 - var11), (float)var9, (float)var9, Resources.floatingCornerPNG, var4);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(var0 + var2 - var9 / 2), (float)(var1 + var9 / 2), 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float)(-var0 - var2 - var9 / 2), (float)(-var1 - var9 / 2), 0.0F);
        drawImage((float)(var0 + var2 - var11), (float)(var1 - var11), (float)var9, (float)var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(var0 + var2 - var9 / 2), (float)(var1 + var3 + var9 / 2), 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float)(-var0 - var2 - var9 / 2), (float)(-var1 - var3 - var9 / 2), 0.0F);
        drawImage((float)(var0 + var2 - var11), (float)(var1 + var10 + var3), (float)var9, (float)var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(var0 - var9 / 2), (float)(var1 + var3 + var9 / 2), 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float)(-var0 - var9 / 2), (float)(-var1 - var3 - var9 / 2), 0.0F);
        drawImage((float)(var0 + var10), (float)(var1 + var10 + var3), (float)var9, (float)var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        drawPortalBackground(var5 - var9, var6 + var10, var5 - var11 + var9, var6 - var10 + var3);

        for (int var12 = 0; var12 < var3; var12 += var9) {
            drawImage((float)(var0 - var11), (float)(var1 + var10 + var12), (float)var9, (float)var9, Resources.floatingBorderPNG, var4);
        }

        endScissor();
        drawPortalBackground(var5, var6 - var11, var5 + var2 - var10, var6 + var10);

        for (int var13 = 0; var13 < var2; var13 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var0 + var9 / 2), (float)(var1 + var9 / 2), 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float)(-var0 - var9 / 2), (float)(-var1 - var9 / 2), 0.0F);
            drawImage((float)(var0 - var11), (float)(var1 - var10 - var13), (float)var9, (float)var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        endScissor();
        drawPortalBackground(var5 + var2 - var10, var6 - var11, var0 + var2 + var11, var6 + var3 - var10);

        for (int var14 = 0; var14 < var3; var14 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var0 + var9 / 2), (float)(var1 + var9 / 2), 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float)(-var0 - var9 / 2), (float)(-var1 - var9 / 2), 0.0F);
            drawImage((float)(var0 - var2 + var10), (float)(var1 - var10 - var14), (float)var9, (float)var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        endScissor();
        drawPortalBackground(var5 - var10, var6 - var11 + var3 - var9, var5 + var2 - var10, var6 + var3 + var10 * 2);

        for (int var15 = 0; var15 < var2; var15 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var0 + var9 / 2), (float)(var1 + var9 / 2), 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float)(-var0 - var9 / 2), (float)(-var1 - var9 / 2), 0.0F);
            drawImage((float)(var0 - var3 + var10), (float)(var1 + var10 + var15), (float)var9, (float)var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        endScissor();
    }

    public static void method11448(float var0, float var1, float var2, float var3, Texture var4, int var5) {
        if (var4 == null) {
            return;
        }
        drawImage(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F, (float)var4.getImageWidth(), (float)var4.getImageHeight(), true);
        drawImage(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F, (float)var4.getImageWidth(), (float)var4.getImageHeight(), false);
    }

    public static Rectangle method11413(Rectangle var0, float var1, float var2) {
        float var5 = (float)var0.x;
        float var6 = (float)var0.y;
        float var7 = (float)var0.width;
        float var8 = (float)var0.height;
        int var9 = Math.round(var7 * var1);
        int var10 = Math.round(var8 * var2);
        float var11 = var7 - (float)var9;
        float var12 = var8 - (float)var10;
        int var13 = Math.round(var5 + var11 / 4.0F);
        int var14 = Math.round(var6 + var12 / 6.0F);
        return new Rectangle(var13, var14, var9, var10);
    }

    public static Rectangle method11414(CustomGuiScreen var0) {
        return new Rectangle(var0.getXA(), var0.getYA(), var0.getWidthA(), var0.getHeightA());
    }

    public static void method11457(float var0, float var1, float var2, float var3, int var4, float var5, float var6, float var7, float var8) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        var0 = (float)Math.round(var0);
        var2 = (float)Math.round(var2);
        var1 = (float)Math.round(var1);
        var3 = (float)Math.round(var3);
        float var11 = (float)(var4 >> 24 & 0xFF) / 255.0F;
        float var12 = (float)(var4 >> 16 & 0xFF) / 255.0F;
        float var13 = (float)(var4 >> 8 & 0xFF) / 255.0F;
        float var14 = (float)(var4 & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var12, var13, var14, var11);
        GL11.glEnable(3042);
        GL11.glEnable(3553);
        GL11.glPixelStorei(3312, 0);
        GL11.glPixelStorei(3313, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3317, 4);
        int var15 = GL11.glGenTextures();
        float var16 = var7 / var7 * 1.0F;
        float var17 = var8 / var8 * 1.0F;
        float var18 = var5 / var7 * 1.0F;
        float var19 = var6 / var8 * 1.0F;
        GL11.glBegin(7);
        GL11.glTexCoord2f(var18, var19);
        GL11.glVertex2f(var0, var1);
        GL11.glTexCoord2f(var18, var19 + var17);
        GL11.glVertex2f(var0, var1 + var3);
        GL11.glTexCoord2f(var18 + var16, var19 + var17);
        GL11.glVertex2f(var0 + var2, var1 + var3);
        GL11.glTexCoord2f(var18 + var16, var19);
        GL11.glVertex2f(var0 + var2, var1);
        GL11.glEnd();
        GL11.glDisable(3553);
        GL11.glDisable(3042);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void method11464(float var0, float var1, float var2, float var3, float var4, float var5) {
        int var8 = ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5);
        method11450(var0, var1, var4, var3, Resources.shadowRightPNG, var8, false);
        method11450(var0 + var2 - var4, var1, var4, var3, Resources.shadowLeftPNG, var8, false);
        method11450(var0, var1, var2, var4, Resources.shadowBottomPNG, var8, false);
        method11450(var0, var1 + var3 - var4, var2, var4, Resources.shadowTopPNG, var8, false);
    }

    public static void method11467(int var0, int var1, int var2, int var3, int var4) {
        int var7 = 36;
        int var8 = 10;
        int var9 = var7 - var8;
        drawRoundedRect((float)(var0 + var8), (float)(var1 + var8), (float)(var0 + var2 - var8), (float)(var1 + var3 - var8), var4);
        drawImage((float)(var0 - var9), (float)(var1 - var9), (float)var7, (float)var7, Resources.floatingCornerPNG, var4);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(var0 + var2 - var7 / 2), (float)(var1 + var7 / 2), 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float)(-var0 - var2 - var7 / 2), (float)(-var1 - var7 / 2), 0.0F);
        drawImage((float)(var0 + var2 - var9), (float)(var1 - var9), (float)var7, (float)var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(var0 + var2 - var7 / 2), (float)(var1 + var3 + var7 / 2), 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float)(-var0 - var2 - var7 / 2), (float)(-var1 - var3 - var7 / 2), 0.0F);
        drawImage((float)(var0 + var2 - var9), (float)(var1 + var8 + var3), (float)var7, (float)var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(var0 - var7 / 2), (float)(var1 + var3 + var7 / 2), 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float)(-var0 - var7 / 2), (float)(-var1 - var3 - var7 / 2), 0.0F);
        drawImage((float)(var0 + var8), (float)(var1 + var8 + var3), (float)var7, (float)var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        method11421(var0 - var7, var1 + var8, var0 - var9 + var7, var1 - var8 + var3, true);

        for (int var10 = 0; var10 < var3; var10 += var7) {
            drawImage((float)(var0 - var9), (float)(var1 + var8 + var10) - 0.4F, (float)var7, (float)var7 + 0.4F, Resources.floatingBorderPNG, var4);
        }

        endScissor();
        method11421(var0, var1 - var9, var0 + var2 - var8, var1 + var8, true);

        for (int var11 = 0; var11 < var2; var11 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var0 + var7 / 2), (float)(var1 + var7 / 2), 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float)(-var0 - var7 / 2), (float)(-var1 - var7 / 2), 0.0F);
            drawImage((float)(var0 - var9), (float)(var1 - var8 - var11) - 0.4F, (float)var7, (float)var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        endScissor();
        method11421(var0 + var2 - var8, var1 - var9, var0 + var2 + var9, var1 + var3 - var8, true);

        for (int var12 = 0; var12 < var3; var12 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var0 + var7 / 2), (float)(var1 + var7 / 2), 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float)(-var0 - var7 / 2), (float)(-var1 - var7 / 2), 0.0F);
            drawImage((float)(var0 - var2 + var8), (float)(var1 - var8 - var12) - 0.4F, (float)var7, (float)var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        endScissor();
        method11421(var0 - var8, var1 - var9 + var3 - var7, var0 + var2 - var8, var1 + var3 + var8 * 2, true);

        for (int var13 = 0; var13 < var2; var13 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var0 + var7 / 2), (float)(var1 + var7 / 2), 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float)(-var0 - var7 / 2), (float)(-var1 - var7 / 2), 0.0F);
            drawImage((float)(var0 - var3 + var8), (float)(var1 + var8 + var13) - 0.4F, (float)var7, (float)var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        endScissor();
    }

    public static void method11436(float var0, float var1, float var2, int var3) {
        method11445(var0, var1, 0.0F, 360.0F, var2 - 1.0F, var3);
    }

    public static void method11445(float var0, float var1, float var2, float var3, float var4, int var5) {
        method11446(var0, var1, var2, var3, var4, var4, var5);
    }


    public static void method11446(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var9 = 0.0F;
        if (var2 > var3) {
            var9 = var3;
            var3 = var2;
            var2 = var9;
        }

        float var10 = (float)(var6 >> 24 & 0xFF) / 255.0F;
        float var11 = (float)(var6 >> 16 & 0xFF) / 255.0F;
        float var12 = (float)(var6 >> 8 & 0xFF) / 255.0F;
        float var13 = (float)(var6 & 0xFF) / 255.0F;
        Tessellator var14 = Tessellator.getInstance();
        BufferBuilder var15 = var14.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var11, var12, var13, var10);
        if (var10 > 0.5F) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            for (float var16 = var3; var16 >= var2; var16 -= 4.0F) {
                float var17 = (float)Math.cos((double)var16 * Math.PI / 180.0) * var4 * 1.001F;
                float var18 = (float)Math.sin((double)var16 * Math.PI / 180.0) * var5 * 1.001F;
                GL11.glVertex2f(var0 + var17, var1 + var18);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (float var20 = var3; var20 >= var2; var20 -= 4.0F) {
            float var21 = (float)Math.cos((double)var20 * Math.PI / 180.0) * var4;
            float var22 = (float)Math.sin((double)var20 * Math.PI / 180.0) * var5;
            GL11.glVertex2f(var0 + var21, var1 + var22);
        }

        GL11.glEnd();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static String getKeyName(int var0) {
        for (Keys var6 : Keys.values()) {
            if (var6.row == var0) {
                return var6.name;
            }
        }

        InputMappings.Input var7 = InputMappings.getInputByCode(var0, 0);
        String[] var8 = var7.getTranslationKey().split("\\.");
        if (var8.length != 0) {
            String var9 = var8[var8.length - 1];
            if (!var9.isEmpty()) {
                String var10 = "";
                if (var0 <= 4) {
                    var10 = "Mouse ";
                }

                return var10 + var9.substring(0, 1).toUpperCase() + var9.substring(1);
            } else {
                return "Unknown";
            }
        } else {
            return "Unknown";
        }
    }

    public static void method11429(float var0, float var1, float var2, float var3, int var4, int var5) {
        drawRoundedRect(var0, var3 - (float)var4, var2 - (float)var4, var3, var5);
        drawRoundedRect(var0, var1, var2 - (float)var4, var1 + (float)var4, var5);
        drawRoundedRect(var0, var1 + (float)var4, var0 + (float)var4, var3 - (float)var4, var5);
        drawRoundedRect(var2 - (float)var4, var1, var2, var3, var5);
    }

    public static void method11434(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var9 = (float)(var6 >> 24 & 0xFF) / 255.0F;
        float var10 = (float)(var6 >> 16 & 0xFF) / 255.0F;
        float var11 = (float)(var6 >> 8 & 0xFF) / 255.0F;
        float var12 = (float)(var6 & 0xFF) / 255.0F;
        Tessellator var13 = Tessellator.getInstance();
        BufferBuilder var14 = var13.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var10, var11, var12, var9);
        GL11.glBegin(6);
        GL11.glVertex2f(var0, var1);
        GL11.glVertex2f(var4, var5);
        GL11.glVertex2f(var2, var3);
        GL11.glVertex2f(var0, var1);
        GL11.glEnd();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void method11428(float var0, float var1, float var2, float var3, int var4) {
        method11429(var0, var1, var2, var3, 1, var4);
    }

    public static void method11431(int var0, int var1, int var2, int var3, int var4, int var5) {
        float var8 = (float)(var4 >> 24 & 0xFF) / 255.0F;
        float var9 = (float)(var4 >> 16 & 0xFF) / 255.0F;
        float var10 = (float)(var4 >> 8 & 0xFF) / 255.0F;
        float var11 = (float)(var4 & 0xFF) / 255.0F;
        float var12 = (float)(var5 >> 24 & 0xFF) / 255.0F;
        float var13 = (float)(var5 >> 16 & 0xFF) / 255.0F;
        float var14 = (float)(var5 >> 8 & 0xFF) / 255.0F;
        float var15 = (float)(var5 & 0xFF) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(7425);
        Tessellator var16 = Tessellator.getInstance();
        BufferBuilder var17 = var16.getBuffer();
        var17.begin(7, DefaultVertexFormats.POSITION_COLOR);
        var17.pos((double)var2, (double)var1, 0.0).color(var9, var10, var11, var8).endVertex();
        var17.pos((double)var0, (double)var1, 0.0).color(var9, var10, var11, var8).endVertex();
        var17.pos((double)var0, (double)var3, 0.0).color(var13, var14, var15, var12).endVertex();
        var17.pos((double)var2, (double)var3, 0.0).color(var13, var14, var15, var12).endVertex();
        var16.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void method11432(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        float var10 = (float)(var4 >> 24 & 0xFF) / 255.0F;
        float var11 = (float)(var4 >> 16 & 0xFF) / 255.0F;
        float var12 = (float)(var4 >> 8 & 0xFF) / 255.0F;
        float var13 = (float)(var4 & 0xFF) / 255.0F;
        float var14 = (float)(var5 >> 24 & 0xFF) / 255.0F;
        float var15 = (float)(var5 >> 16 & 0xFF) / 255.0F;
        float var16 = (float)(var5 >> 8 & 0xFF) / 255.0F;
        float var17 = (float)(var5 & 0xFF) / 255.0F;
        float var18 = (float)(var6 >> 24 & 0xFF) / 255.0F;
        float var19 = (float)(var6 >> 16 & 0xFF) / 255.0F;
        float var20 = (float)(var6 >> 8 & 0xFF) / 255.0F;
        float var21 = (float)(var6 & 0xFF) / 255.0F;
        float var22 = (float)(var7 >> 24 & 0xFF) / 255.0F;
        float var23 = (float)(var7 >> 16 & 0xFF) / 255.0F;
        float var24 = (float)(var7 >> 8 & 0xFF) / 255.0F;
        float var25 = (float)(var7 & 0xFF) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(7425);
        Tessellator var26 = Tessellator.getInstance();
        BufferBuilder var27 = var26.getBuffer();
        var27.begin(7, DefaultVertexFormats.POSITION_COLOR);
        var27.pos((double)var2, (double)var1, 0.0).color(var15, var16, var17, var14).endVertex();
        var27.pos((double)var0, (double)var1, 0.0).color(var11, var12, var13, var10).endVertex();
        var27.pos((double)var0, (double)var3, 0.0).color(var23, var24, var25, var22).endVertex();
        var27.pos((double)var2, (double)var3, 0.0).color(var19, var20, var21, var18).endVertex();
        var26.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void renderCategoryBox(float var0, float var1, float var2, int var3, int var4) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        Tessellator var7 = Tessellator.getInstance();
        BufferBuilder var8 = var7.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4fv(ColorUtils.method17709(var3));
        GL11.glEnable(2881);
        GL11.glBegin(4);
        GL11.glVertex2f(var0 + var2 / 2.0F, var1 + var2 / 2.0F);
        GL11.glVertex2f(var0 + var2 / 2.0F, var1 - var2 / 2.0F);
        GL11.glVertex2f(var0 - var2 / 2.0F, var1);
        GL11.glEnd();
        GL11.glLineWidth(2.0F);
        GL11.glColor4fv(ColorUtils.method17709(var4));
        GL11.glBegin(3);
        GL11.glVertex2f(var0 + var2 / 2.0F, var1 + var2 / 2.0F);
        GL11.glVertex2f(var0 + var2 / 2.0F, var1 - var2 / 2.0F);
        GL11.glEnd();
        GL11.glBegin(3);
        GL11.glVertex2f(var0 - var2 / 2.0F, var1);
        GL11.glVertex2f(var0 + var2 / 2.0F, var1 - var2 / 2.0F);
        GL11.glEnd();
        GL11.glBegin(3);
        GL11.glVertex2f(var0 + var2 / 2.0F, var1 + var2 / 2.0F);
        GL11.glVertex2f(var0 - var2 / 2.0F, var1);
        GL11.glEnd();
        GL11.glDisable(2881);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void recreateDepthBuffer(Framebuffer frameBuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(frameBuffer.depthBuffer);
        int newDepthBuffer = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, newDepthBuffer);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, Minecraft.getInstance().getMainWindow().getFramebufferWidth(), Minecraft.getInstance().getMainWindow().getFramebufferHeight());
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, newDepthBuffer);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, newDepthBuffer);
    }

    public static void resetDepthBuffer() {
        // Retrieve the current framebuffer instance
        Framebuffer currentFramebuffer = Minecraft.getInstance().getFramebuffer();

        // Check if the framebuffer exists and has a valid depth buffer
        if (currentFramebuffer != null && currentFramebuffer.depthBuffer > -1) {
            // Recreate the depth buffer
            recreateDepthBuffer(currentFramebuffer);
            // Set the depth buffer to an invalid state
            currentFramebuffer.depthBuffer = -1;
        }
    }

    public static void method11476() {
        GL11.glPushMatrix();
        resetDepthBuffer();
        GL11.glEnable(2960);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glStencilFunc(512, 1, 1);
        GL11.glStencilOp(7681, 7680, 7680);
        GL11.glStencilMask(1);
        GL11.glClear(1024);
        field18461 = true;
    }

    public static void method11478() {
        GL11.glStencilMask(-1);
        GL11.glDisable(2960);
        GL11.glPopMatrix();
        field18461 = false;
    }

    public static void method11477(Class2329 var0) {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0);
        GL11.glStencilFunc(var0 != Class2329.field15940 ? 517 : 514, 1, 1);
    }

    public static void method11453(float var0, float var1, float var2, float var3, ByteBuffer var4, int var5, float var6, float var7, float var8, float var9, boolean var10, boolean var11) {
        if (var4 != null) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
            var0 = (float)Math.round(var0);
            var2 = (float)Math.round(var2);
            var1 = (float)Math.round(var1);
            var3 = (float)Math.round(var3);
            float var14 = (float)(var5 >> 24 & 0xFF) / 255.0F;
            float var15 = (float)(var5 >> 16 & 0xFF) / 255.0F;
            float var16 = (float)(var5 >> 8 & 0xFF) / 255.0F;
            float var17 = (float)(var5 & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.color4f(var15, var16, var17, var14);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            GL11.glPixelStorei(3312, 0);
            GL11.glPixelStorei(3313, 0);
            GL11.glPixelStorei(3314, 0);
            GL11.glPixelStorei(3315, 0);
            GL11.glPixelStorei(3316, 0);
            GL11.glPixelStorei(3317, 4);
            GL11.glTexParameteri(3553, 10240, 9728);
            int var18 = GL11.glGenTextures();
            GL11.glTexImage2D(3553, 0, 6407, (int)var8, (int)var9, 0, 6407, 5121, var4);
            float var19 = var8 / var8 * 1.0F;
            float var20 = var9 / var9 * 1.0F;
            float var21 = var6 / var8 * 1.0F;
            float var22 = var7 / var9 * 1.0F;
            GL11.glBegin(7);
            GL11.glTexCoord2f(var21 + (!var10 ? 0.0F : var19), var22 + (!var11 ? 0.0F : var20));
            GL11.glVertex2f(var0, var1);
            GL11.glTexCoord2f(var21 + (!var10 ? 0.0F : var19), var22 + (!var11 ? var20 : 0.0F));
            GL11.glVertex2f(var0, var1 + var3);
            GL11.glTexCoord2f(var21 + (!var10 ? var19 : 0.0F), var22 + (!var11 ? var20 : 0.0F));
            GL11.glVertex2f(var0 + var2, var1 + var3);
            GL11.glTexCoord2f(var21 + (!var10 ? var19 : 0.0F), var22 + (!var11 ? 0.0F : var20));
            GL11.glVertex2f(var0 + var2, var1);
            GL11.glEnd();
            GL11.glDisable(3553);
            GL11.glDisable(3042);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void method11479(ItemStack var0, int var1, int var2, int var3, int var4) {
        if (var0 != null) {
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
            GL11.glPushMatrix();
            GL11.glTranslatef((float)var1, (float)var2, 0.0F);
            GL11.glScalef((float)var3 / 16.0F, (float)var4 / 16.0F, 0.0F);
            ItemRenderer var7 = mc.getItemRenderer();
            if (var0.getCount() == 0) {
                var0 = new ItemStack(var0.getItem());
            }

            RenderHelper.setupGuiFlatDiffuseLighting();
            GL11.glLightModelfv(2899, new float[]{0.4F, 0.4F, 0.4F, 1.0F});
            RenderSystem.enableColorMaterial();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDepthFunc(519);
            var7.renderItemIntoGUI(var0, 0, 0);
            GL11.glDepthFunc(515);
            RenderSystem.popMatrix();
            GL11.glAlphaFunc(519, 0.0F);
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            RenderSystem.disableDepthTest();
            TextureImpl.unbind();
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
            RenderHelper.setupGui3DDiffuseLighting();
        }
    }

    public static java.awt.Color getColorFromScreen(int mouseX, int mouseY, java.awt.Color var2) {
        mouseX = (int)((float)mouseX * GuiManager.scaleFactor);
        mouseY = (int)((float)mouseY * GuiManager.scaleFactor);
        ByteBuffer var5 = ByteBuffer.allocateDirect(3);
        GL11.glPixelStorei(3317, 1);
        GL11.glReadPixels(mouseX, Minecraft.getInstance().getMainWindow().getFramebufferHeight() - mouseY, 1, 1, 6407, 5120, var5);
        return new java.awt.Color(var5.get(0) * 2, var5.get(1) * 2, var5.get(2) * 2, 1);
    }
}
