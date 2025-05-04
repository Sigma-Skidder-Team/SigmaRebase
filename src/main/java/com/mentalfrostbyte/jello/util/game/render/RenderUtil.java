package com.mentalfrostbyte.jello.util.game.render;

import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind.Keys;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.render.jello.esp.util.Class2329;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
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
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class RenderUtil implements MinecraftUtil {
    public static boolean stencilOpInProgress = false;

    private static final Stack<IntBuffer> buffer = new Stack<>();
    public static boolean field18461 = false;

    public static void restoreScissor() {
        if (buffer.isEmpty()) {
            GL11.glDisable(GL_SCISSOR_TEST);
        } else {
            IntBuffer var2 = buffer.pop();
            GL11.glScissor(var2.get(0), var2.get(1), var2.get(2), var2.get(3));
        }
    }

    public static void drawBlurredBackground(int var0, int var1, int var2, int var3) {
        startScissor(var0, var1, var2, var3, false);
    }

    public static void method11436(float var0, float var1, float var2, int var3) {
        method11445(var0, var1, 0.0F, 360.0F, var2 - 1.0F, var3);
    }

    public static void method11445(float var0, float var1, float var2, float var3, float var4, int var5) {
        method11446(var0, var1, var2, var3, var4, var4, var5);
    }

    public static float getGuiScaleFactor() {
        return (float) mc.mainWindow.getGuiScaleFactor();
    }

    public static void drawRect(double var0, double var2, double var4, double var6, int var8) {
        drawRect((float) var0, (float) var2, (float) var4, (float) var6, var8);
    }

    public static void drawRect2(float var0, float var1, float var2, float var3, int var4) {
        drawRect(var0, var1, var0 + var2, var1 + var3, var4);
    }

    public static void drawImage2(float var0, float var1, float var2, float var3, Texture var4, int var5,
                                  boolean var6) {
        drawImage(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F, (float) var4.getImageWidth(),
                (float) var4.getImageHeight(), var6);
    }

    public static void renderWireframeBox(BoundingBox boxIn, int color) {
        renderWireframeBox(boxIn, 2.8F, color);
    }

    public static void renderWireframeBox(BoundingBox boxIn, float width, int color) {
        if (boxIn != null) {
            float var5 = (float) (color >> 24 & 0xFF) / 255.0F;
            float var6 = (float) (color >> 16 & 0xFF) / 255.0F;
            float var7 = (float) (color >> 8 & 0xFF) / 255.0F;
            float var8 = (float) (color & 0xFF) / 255.0F;
            GL11.glColor4f(var6, var7, var8, var5);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glLineWidth(width);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GL11.glBegin(3);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glEnd();
            GL11.glBegin(1);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glEnable(3553);
            GL11.glEnable(2896);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
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

    public static void method11477(Class2329 var0) {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0);
        GL11.glStencilFunc(var0 != Class2329.field15940 ? 517 : 514, 1, 1);
    }

    public static void method11478() {
        GL11.glStencilMask(-1);
        GL11.glDisable(2960);
        GL11.glPopMatrix();
        field18461 = false;
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
    }

    public static void drawItem(ItemStack itemStack, int posX, int posY, int width, int height) {
        if (itemStack != null) {
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
            GL11.glPushMatrix();
            GL11.glTranslatef((float) posX, (float) posY, 0.0F);
            GL11.glScalef((float) width / 16.0F, (float) height / 16.0F, 0.0F);
            ItemRenderer itemRenderer = mc.getItemRenderer();
            if (itemStack.count == 0) {
                itemStack = new ItemStack(itemStack.getItem());
            }

            RenderHelper.setupGuiFlatDiffuseLighting();
            GL11.glLightModelfv(2899, new float[]{0.4F, 0.4F, 0.4F, 1.0F});
            RenderSystem.enableColorMaterial();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDepthFunc(519);
            itemRenderer.renderItemIntoGUI(itemStack, 0, 0);
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

    public static void render3DColoredBox(BoundingBox boxIn, int color) {
        if (boxIn != null) {
            float var4 = (float) (color >> 24 & 0xFF) / 255.0F;
            float var5 = (float) (color >> 16 & 0xFF) / 255.0F;
            float var6 = (float) (color >> 8 & 0xFF) / 255.0F;
            float var7 = (float) (color & 0xFF) / 255.0F;
            GL11.glColor4f(var5, var6, var7, var4);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glLineWidth(1.8F * GuiManager.scaleFactor);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(2848);
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.maxY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.maxY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.minZ);
            GL11.glVertex3d(boxIn.minX, boxIn.minY, boxIn.maxZ);
            GL11.glVertex3d(boxIn.maxX, boxIn.minY, boxIn.maxZ);
            GL11.glEnd();
            GL11.glEnable(3553);
            GL11.glEnable(2896);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
        }
    }

    /**
     * Transforms 2D coordinates using the current OpenGL model view matrix and applies scaling.
     * This method is typically used for converting screen coordinates to scaled OpenGL coordinates.
     *
     * @param x The x-coordinate to transform.
     * @param y The y-coordinate to transform.
     * @return A float array containing two elements:
     * [0] The transformed and scaled x-coordinate.
     * [1] The transformed and scaled y-coordinate.
     */
    public static float[] screenCoordinatesToOpenGLCoordinates(int x, int y) {
        FloatBuffer var4 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, var4);
        float var5 = var4.get(0) * (float) x + var4.get(4) * (float) y + var4.get(8) * 0.0F + var4.get(12);
        float var6 = var4.get(1) * (float) x + var4.get(5) * (float) y + var4.get(9) * 0.0F + var4.get(13);
        float var7 = var4.get(3) * (float) x + var4.get(7) * (float) y + var4.get(11) * 0.0F + var4.get(15);
        var5 /= var7;
        var6 /= var7;
        return new float[]{(float) Math.round(var5 * getGuiScaleFactor()), (float) Math.round(var6 * getGuiScaleFactor())};
    }

    public static void method11430(double var0, double var2, double var4, double var6, double var8, int var10, int var11) {
        drawRect(var0 + var8, var2 + var8, var4 - var8, var6 - var8, var10);
        drawRect(var0 + var8, var2, var4 - var8, var2 + var8, var11);
        drawRect(var0, var2, var0 + var8, var6, var11);
        drawRect(var4 - var8, var2, var4, var6, var11);
        drawRect(var0 + var8, var6 - var8, var4 - var8, var6, var11);
    }

    public static void startScissor(int x, int y, int width, int height, boolean scale) {
        if (!scale) {
            x = (int) ((float) x * GuiManager.scaleFactor);
            y = (int) ((float) y * GuiManager.scaleFactor);
            width = (int) ((float) width * GuiManager.scaleFactor);
            height = (int) ((float) height * GuiManager.scaleFactor);
        } else {
            float[] var7 = screenCoordinatesToOpenGLCoordinates(x, y);
            x = (int) var7[0];
            y = (int) var7[1];
            float[] var8 = screenCoordinatesToOpenGLCoordinates(width, height);
            width = (int) var8[0];
            height = (int) var8[1];
        }

        if (GL11.glIsEnabled(GL_SCISSOR_TEST)) {
            IntBuffer var17 = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, var17);
            buffer.push(var17);
            int var18 = var17.get(0);
            int var9 = mc.getMainWindow().getFramebufferHeight() - var17.get(1) - var17.get(3);
            int var10 = var18 + var17.get(2);
            int var11 = var9 + var17.get(3);
            if (x < var18) {
                x = var18;
            }

            if (y < var9) {
                y = var9;
            }

            if (width > var10) {
                width = var10;
            }

            if (height > var11) {
                height = var11;
            }

            if (y > height) {
                height = y;
            }

            if (x > width) {
                width = x;
            }
        }

        int adjustedY = mc.getMainWindow().getFramebufferHeight() - height;
        int width2 = width - x;
        int height2 = height - y;
        GL11.glEnable(GL_SCISSOR_TEST);
        if (width2 >= 0 && height2 >= 0) {
            GL11.glScissor(x, adjustedY, width2, height2);
        }
    }

    public static void drawRect(float var0, float var1, float var2, float var3, int var4) {
        if (var0 < var2) {
            int var7 = (int) var0;
            var0 = var2;
            var2 = (float) var7;
        }

        if (var1 < var3) {
            int var13 = (int) var1;
            var1 = var3;
            var3 = (float) var13;
        }

        float var14 = (float) (var4 >> 24 & 0xFF) / 255.0F;
        float var8 = (float) (var4 >> 16 & 0xFF) / 255.0F;
        float var9 = (float) (var4 >> 8 & 0xFF) / 255.0F;
        float var10 = (float) (var4 & 0xFF) / 255.0F;
        Tessellator var11 = Tessellator.getInstance();
        BufferBuilder var12 = var11.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();

        // Corrected blend function with proper factors
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        RenderSystem.color4f(var8, var9, var10, var14);

        var12.begin(7, DefaultVertexFormats.POSITION);
        var12.pos(var0, var3, 0.0).endVertex();
        var12.pos(var2, var3, 0.0).endVertex();
        var12.pos(var2, var1, 0.0).endVertex();
        var12.pos(var0, var1, 0.0).endVertex();

        var11.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    public static void drawImage(float x, float y, float width, float height, Texture tex, float alphaValue) {
        drawImage(x, y, width, height, tex, RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alphaValue));
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture) {
        drawImage(x, y, width, height, texture, -1);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color) {
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), true);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, boolean linearFiltering) {
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), linearFiltering);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, float tlX, float tlY, float siW, float siH) {
        drawImage(x, y, width, height, texture, color, tlX, tlY, siW, siH, true);
    }

    /**
     * Draws a sub-image of a texture to the screen.
     *
     * @param x               The x-coordinate of the top-left corner of the image.
     * @param y               The y-coordinate of the top-left corner of the image.
     * @param width           The width of the image.
     * @param height          The height of the image.
     * @param texture         The texture to draw from.
     * @param color           The color to draw the image in, represented as an integer.
     * @param tlX             The x-coordinate of the top-left corner of the sub-image within the texture.
     * @param tlY             The y-coordinate of the top-left corner of the sub-image within the texture.
     * @param siW             The width of the sub-image.
     * @param siH             The height of the sub-image.
     * @param linearFiltering Whether to use linear filtering for the texture.
     */
    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, float tlX, float tlY, float siW, float siH, boolean linearFiltering) {
        if (texture != null) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
            x = (float) Math.round(x);
            width = (float) Math.round(width);
            y = (float) Math.round(y);
            height = (float) Math.round(height);
            float a = (float) (color >> 24 & 0xFF) / 255.0F;
            float r = (float) (color >> 16 & 0xFF) / 255.0F;
            float g = (float) (color >> 8 & 0xFF) / 255.0F;
            float b = (float) (color & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.color4f(r, g, b, a);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            texture.bind();
            float var17 = width / (float) texture.getTextureWidth() / (width / (float) texture.getImageWidth());
            float var18 = height / (float) texture.getTextureHeight() / (height / (float) texture.getImageHeight());
            float var19 = siW / (float) texture.getImageWidth() * var17;
            float var20 = siH / (float) texture.getImageHeight() * var18;
            float var21 = tlX / (float) texture.getImageWidth() * var17;
            float var22 = tlY / (float) texture.getImageHeight() * var18;
            if (!linearFiltering) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            } else {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }

            GL11.glBegin(7);
            GL11.glTexCoord2f(var21, var22);
            GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(var21, var22 + var20);
            GL11.glVertex2f(x, y + height);
            GL11.glTexCoord2f(var21 + var19, var22 + var20);
            GL11.glVertex2f(x + width, y + height);
            GL11.glTexCoord2f(var21 + var19, var22);
            GL11.glVertex2f(x + width, y);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void drawCircle(boolean isFadingOut, float circleHeight, float radius, float glowStrength, float glowOpacity) {
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_QUAD_STRIP);

        int angleStep = (int) (360.0F / (40.0F * radius));

        com.mentalfrostbyte.jello.module.Module moduleInstance = new Module(ModuleCategory.PLAYER, "ESP COLOR", "");
        java.awt.Color espColor = new java.awt.Color(moduleInstance.parseSettingValueToIntBySettingName("ESP Color"));
        float red = (float) espColor.getRed() / 255.0F;
        float green = (float) espColor.getGreen() / 255.0F;
        float blue = (float) espColor.getBlue() / 255.0F;

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int effectiveAngle = angle > 360 ? 0 : angle;

            double x = Math.sin(Math.toRadians(effectiveAngle)) * radius;
            double z = Math.cos(Math.toRadians(effectiveAngle)) * radius;

            GL11.glColor4f(red, green, blue, isFadingOut ? 0.0F : glowStrength * glowOpacity);
            GL11.glVertex3d(x, 0.0, z);

            GL11.glColor4f(red, green, blue, isFadingOut ? glowStrength * glowOpacity : 0.0F);
            GL11.glVertex3d(x, circleHeight, z);
        }

        GL11.glEnd();

        GL11.glLineWidth(2.2F);
        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int effectiveAngle = angle > 360 ? 0 : angle;

            double x = Math.sin(Math.toRadians(effectiveAngle)) * radius;
            double z = Math.cos(Math.toRadians(effectiveAngle)) * radius;

            GL11.glColor4f(red, green, blue, (0.5F + 0.5F * glowStrength) * glowOpacity);
            GL11.glVertex3d(x, isFadingOut ? 0.0 : circleHeight, z);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderSystem.shadeModel(GL11.GL_FLAT);
    }

    public static void drawRoundedRect2(float x, float y, float width, float height, int color) {
        drawRoundedRect(x, y, x + width, y + height, color);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float size, int color) {
        drawRoundedRect(x, y + size, x + width, y + height - size, color);
        drawRoundedRect(x + size, y, x + width - size, y + size, color);
        drawRoundedRect(x + size, y + height - size, x + width - size, y + height, color);
        drawBlurredBackground(x, y, x + size, y + size);
        drawCircle(x + size, y + size, size * 2.0F, color);
        restoreScissor();
        drawBlurredBackground(x + width - size, y, x + width, y + size);
        drawCircle(x - size + width, y + size, size * 2.0F, color);
        restoreScissor();
        drawBlurredBackground(x, y + height - size, x + size, y + height);
        drawCircle(x + size, y - size + height, size * 2.0F, color);
        restoreScissor();
        drawBlurredBackground(x + width - size, y + height - size, x + width, y + height);
        drawCircle(x - size + width, y - size + height, size * 2.0F, color);
        restoreScissor();
    }

    public static void drawBlurredBackground(float x, float y, float width, float height) {
        startScissor((int) x, (int) y, (int) width, (int) height, true);
    }

    public static void drawCircle(float centerX, float centerY, float size, int color) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        Tessellator var10 = Tessellator.getInstance();
        BufferBuilder var11 = var10.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(r, g, b, a);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPointSize(size * GuiManager.scaleFactor);
        GL11.glBegin(0);
        GL11.glVertex2f(centerX, centerY);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(float x, float y, float sizedX, float sizedY, int color) {
        if (x < sizedX) {
            int var7 = (int) x;
            x = sizedX;
            sizedX = (float) var7;
        }

        if (y < sizedY) {
            int var13 = (int) y;
            y = sizedY;
            sizedY = (float) var13;
        }

        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        Tessellator var11 = Tessellator.getInstance();
        BufferBuilder var12 = var11.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(r, g, b, a);
        var12.begin(7, DefaultVertexFormats.POSITION);
        var12.pos(x, sizedY, 0.0).endVertex();
        var12.pos(sizedX, sizedY, 0.0).endVertex();
        var12.pos(sizedX, y, 0.0).endVertex();
        var12.pos(x, y, 0.0).endVertex();
        var11.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawString(TrueTypeFont res, float var1, float var2, String string, int var4, FontSizeAdjust var5, FontSizeAdjust var6) {
        drawString(res, var1, var2, string, var4, var5, var6, false);
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color, FontSizeAdjust widthAdjust, FontSizeAdjust heightAdjust, boolean var7) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        int adjustedWidth = 0;
        int adjustedHeight = 0;
        adjustedWidth = switch (widthAdjust) {
            case NEGATE_AND_DIVIDE_BY_2 -> -font.getWidth(text) / 2;
            case WIDTH_NEGATE -> -font.getWidth(text);
            default -> adjustedWidth;
        };

        adjustedHeight = switch (heightAdjust) {
            case NEGATE_AND_DIVIDE_BY_2 -> -font.getHeight(text) / 2;
            case HEIGHT_NEGATE -> -font.getHeight(text);
            default -> adjustedHeight;
        };

        float var12 = (float) (color >> 24 & 0xFF) / 255.0F;
        float var13 = (float) (color >> 16 & 0xFF) / 255.0F;
        float var14 = (float) (color >> 8 & 0xFF) / 255.0F;
        float var15 = (float) (color & 0xFF) / 255.0F;
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
                GL11.glTranslatef(x, y, 0.0F);
                GL11.glScalef(1.0F / GuiManager.scaleFactor, 1.0F / GuiManager.scaleFactor, 1.0F / GuiManager.scaleFactor);
                GL11.glTranslatef(-x, -y, 0.0F);
                adjustedWidth = (int) ((float) adjustedWidth * GuiManager.scaleFactor);
                adjustedHeight = (int) ((float) adjustedHeight * GuiManager.scaleFactor);
            }
        }

        RenderSystem.enableBlend();
        GL11.glBlendFunc(770, 771);
        if (var7) {
            font.drawString((float) Math.round(x + (float) adjustedWidth), (float) (Math.round(y + (float) adjustedHeight) + 2), text, new Color(0.0F, 0.0F, 0.0F, 0.35F));
        }

        if (text != null) {
            font.drawString((float) Math.round(x + (float) adjustedWidth), (float) Math.round(y + (float) adjustedHeight), text, new Color(var13, var14, var15, var12));
        }

        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static void method11415(CustomGuiScreen var0) {
        startScissor(var0.getXA(), var0.getYA(), var0.getWidthA() + var0.getXA(), var0.getHeightA() + var0.getYA(), true);
    }

    public static void drawRoundedButton(float var0, float var1, float var2, float var3, float var4, int color) {
        drawRoundedRect(var0, var1 + var4, var0 + var2, var1 + var3 - var4, color);
        drawRoundedRect(var0 + var4, var1, var0 + var2 - var4, var1 + var3, color);
        FloatBuffer var8 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, var8);
        float var9 = 1.0F;
        drawCircle(var0 + var4, var1 + var4, var4 * 2.0F * var9, color);
        drawCircle(var0 - var4 + var2, var1 + var4, var4 * 2.0F * var9, color);
        drawCircle(var0 + var4, var1 - var4 + var3, var4 * 2.0F * var9, color);
        drawCircle(var0 - var4 + var2, var1 - var4 + var3, var4 * 2.0F * var9, color);
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color) {
        drawString(font, x, y, text, color, FontSizeAdjust.field14488, FontSizeAdjust.field14489, false);
    }

    public static void drawRoundedRect(float var0, float var1, float var2, float var3, float var4, float var5) {
        GL11.glAlphaFunc(519, 0.0F);
        int var8 = RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5);
        drawImage(var0 - var4, var1 - var4, var4, var4, Resources.shadowCorner1PNG, var8);
        drawImage(var0 + var2, var1 - var4, var4, var4, Resources.shadowCorner2PNG, var8);
        drawImage(var0 - var4, var1 + var3, var4, var4, Resources.shadowCorner3PNG, var8);
        drawImage(var0 + var2, var1 + var3, var4, var4, Resources.shadowCorner4PNG, var8);
        drawImage(var0 - var4, var1, var4, var3, Resources.shadowLeftPNG, var8, false);
        drawImage(var0 + var2, var1, var4, var3, Resources.shadowRightPNG, var8, false);
        drawImage(var0, var1 - var4, var2, var4, Resources.shadowTopPNG, var8, false);
        drawImage(var0, var1 + var3, var2, var4, Resources.shadowBottomPNG, var8, false);
    }

    public static void startScissor(float var0, float var1, float var2, float var3) {
        startScissor((int) var0, (int) var1, (int) var0 + (int) var2, (int) var1 + (int) var3, true);
    }

    public static void method11465(int var0, int var1, int var2, int var3, int var4) {
        method11466(var0, var1, var2, var3, var4, var0, var1);
    }

    public static void method11466(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        int var9 = 36;
        int var10 = 10;
        int var11 = var9 - var10;
        drawRoundedRect((float) (var0 + var10), (float) (var1 + var10), (float) (var0 + var2 - var10), (float) (var1 + var3 - var10), var4);
        drawImage((float) (var0 - var11), (float) (var1 - var11), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var9 / 2), (float) (var1 + var9 / 2), 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var11), (float) (var1 - var11), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var9 / 2), (float) (var1 + var3 + var9 / 2), 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var9 / 2), (float) (-var1 - var3 - var9 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var11), (float) (var1 + var10 + var3), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 - var9 / 2), (float) (var1 + var3 + var9 / 2), 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var3 - var9 / 2), 0.0F);
        drawImage((float) (var0 + var10), (float) (var1 + var10 + var3), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        drawBlurredBackground(var5 - var9, var6 + var10, var5 - var11 + var9, var6 - var10 + var3);

        for (int var12 = 0; var12 < var3; var12 += var9) {
            drawImage((float) (var0 - var11), (float) (var1 + var10 + var12), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
        }

        restoreScissor();
        drawBlurredBackground(var5, var6 - var11, var5 + var2 - var10, var6 + var10);

        for (int var13 = 0; var13 < var2; var13 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var9 / 2), (float) (var1 + var9 / 2), 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
            drawImage((float) (var0 - var11), (float) (var1 - var10 - var13), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        restoreScissor();
        drawBlurredBackground(var5 + var2 - var10, var6 - var11, var0 + var2 + var11, var6 + var3 - var10);

        for (int var14 = 0; var14 < var3; var14 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var9 / 2), (float) (var1 + var9 / 2), 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
            drawImage((float) (var0 - var2 + var10), (float) (var1 - var10 - var14), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        restoreScissor();
        drawBlurredBackground(var5 - var10, var6 - var11 + var3 - var9, var5 + var2 - var10, var6 + var3 + var10 * 2);

        for (int var15 = 0; var15 < var2; var15 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var9 / 2), (float) (var1 + var9 / 2), 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
            drawImage((float) (var0 - var3 + var10), (float) (var1 + var10 + var15), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        restoreScissor();
    }

    public static void drawTexture(float x, float y, float width, float height, Texture texture, int color) {
        if (texture == null) {
            return;
        }
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), true);
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), false);
    }

    public static Rectangle method11413(Rectangle var0, float var1, float var2) {
        float var5 = (float) var0.x;
        float var6 = (float) var0.y;
        float var7 = (float) var0.width;
        float var8 = (float) var0.height;
        int var9 = Math.round(var7 * var1);
        int var10 = Math.round(var8 * var2);
        float var11 = var7 - (float) var9;
        float var12 = var8 - (float) var10;
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
        var0 = (float) Math.round(var0);
        var2 = (float) Math.round(var2);
        var1 = (float) Math.round(var1);
        var3 = (float) Math.round(var3);
        float var11 = (float) (var4 >> 24 & 0xFF) / 255.0F;
        float var12 = (float) (var4 >> 16 & 0xFF) / 255.0F;
        float var13 = (float) (var4 >> 8 & 0xFF) / 255.0F;
        float var14 = (float) (var4 & 0xFF) / 255.0F;
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

        float var16 = 1.0f;
        float var17 = 1.0f;
        float var18 = var5 / var7;
        float var19 = var6 / var8;

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
        int var8 = RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5);
        drawImage(var0, var1, var4, var3, Resources.shadowRightPNG, var8, false);
        drawImage(var0 + var2 - var4, var1, var4, var3, Resources.shadowLeftPNG, var8, false);
        drawImage(var0, var1, var2, var4, Resources.shadowBottomPNG, var8, false);
        drawImage(var0, var1 + var3 - var4, var2, var4, Resources.shadowTopPNG, var8, false);
    }

    public static void method11467(int var0, int var1, int var2, int var3, int var4) {
        int var7 = 36;
        int var8 = 10;
        int var9 = var7 - var8;
        drawRoundedRect((float) (var0 + var8), (float) (var1 + var8), (float) (var0 + var2 - var8), (float) (var1 + var3 - var8), var4);
        drawImage((float) (var0 - var9), (float) (var1 - var9), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var7 / 2), (float) (var1 + var7 / 2), 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var9), (float) (var1 - var9), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var7 / 2), (float) (var1 + var3 + var7 / 2), 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var7 / 2), (float) (-var1 - var3 - var7 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var9), (float) (var1 + var8 + var3), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 - var7 / 2), (float) (var1 + var3 + var7 / 2), 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var3 - var7 / 2), 0.0F);
        drawImage((float) (var0 + var8), (float) (var1 + var8 + var3), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        startScissor(var0 - var7, var1 + var8, var0 - var9 + var7, var1 - var8 + var3, true);

        for (int var10 = 0; var10 < var3; var10 += var7) {
            drawImage((float) (var0 - var9), (float) (var1 + var8 + var10) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
        }

        restoreScissor();
        startScissor(var0, var1 - var9, var0 + var2 - var8, var1 + var8, true);

        for (int var11 = 0; var11 < var2; var11 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var7 / 2), (float) (var1 + var7 / 2), 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
            drawImage((float) (var0 - var9), (float) (var1 - var8 - var11) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        restoreScissor();
        startScissor(var0 + var2 - var8, var1 - var9, var0 + var2 + var9, var1 + var3 - var8, true);

        for (int var12 = 0; var12 < var3; var12 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var7 / 2), (float) (var1 + var7 / 2), 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
            drawImage((float) (var0 - var2 + var8), (float) (var1 - var8 - var12) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        restoreScissor();
        startScissor(var0 - var8, var1 - var9 + var3 - var7, var0 + var2 - var8, var1 + var3 + var8 * 2, true);

        for (int var13 = 0; var13 < var2; var13 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var7 / 2), (float) (var1 + var7 / 2), 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
            drawImage((float) (var0 - var3 + var8), (float) (var1 + var8 + var13) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        restoreScissor();
    }

    public static void drawFilledArc(float var0, float var1, float var2, int var3) {
        drawFilledArc(var0, var1, 0.0F, 360.0F, var2 - 1.0F, var3);
    }

    public static void drawFilledArc(float var0, float var1, float var2, float var3, float var4, int var5) {
        drawFilledArc(var0, var1, var2, var3, var4, var4, var5);
    }

    /**
     * Draws a filled arc with the specified center, radii, start and end angles, and color.
     *
     * @param x          The x-coordinate of the center of the arc.
     * @param y          The y-coordinate of the center of the arc.
     * @param startAngle The start angle of the arc in degrees.
     * @param endAngle   The end angle of the arc in degrees.
     * @param hRadius    The horizontal radius of the arc.
     * @param vRadius    The vertical radius of the arc.
     * @param color      The color of the arc in ARGB format.
     */
    public static void drawFilledArc(float x, float y, float startAngle, float endAngle, float hRadius, float vRadius, int color) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var9 = 0.0F;
        if (startAngle > endAngle) {
            var9 = endAngle;
            endAngle = startAngle;
            startAngle = var9;
        }

        float var10 = (float) (color >> 24 & 0xFF) / 255.0F;
        float var11 = (float) (color >> 16 & 0xFF) / 255.0F;
        float var12 = (float) (color >> 8 & 0xFF) / 255.0F;
        float var13 = (float) (color & 0xFF) / 255.0F;
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

            for (float var16 = endAngle; var16 >= startAngle; var16 -= 4.0F) {
                float var17 = (float) Math.cos((double) var16 * Math.PI / 180.0) * hRadius * 1.001F;
                float var18 = (float) Math.sin((double) var16 * Math.PI / 180.0) * vRadius * 1.001F;
                GL11.glVertex2f(x + var17, y + var18);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (float var20 = endAngle; var20 >= startAngle; var20 -= 4.0F) {
            float var21 = (float) Math.cos((double) var20 * Math.PI / 180.0) * hRadius;
            float var22 = (float) Math.sin((double) var20 * Math.PI / 180.0) * vRadius;
            GL11.glVertex2f(x + var21, y + var22);
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
        drawRoundedRect(var0, var3 - (float) var4, var2 - (float) var4, var3, var5);
        drawRoundedRect(var0, var1, var2 - (float) var4, var1 + (float) var4, var5);
        drawRoundedRect(var0, var1 + (float) var4, var0 + (float) var4, var3 - (float) var4, var5);
        drawRoundedRect(var2 - (float) var4, var1, var2, var3, var5);
    }

    public static void method11434(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var9 = (float) (var6 >> 24 & 0xFF) / 255.0F;
        float var10 = (float) (var6 >> 16 & 0xFF) / 255.0F;
        float var11 = (float) (var6 >> 8 & 0xFF) / 255.0F;
        float var12 = (float) (var6 & 0xFF) / 255.0F;
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
        float var8 = (float) (var4 >> 24 & 0xFF) / 255.0F;
        float var9 = (float) (var4 >> 16 & 0xFF) / 255.0F;
        float var10 = (float) (var4 >> 8 & 0xFF) / 255.0F;
        float var11 = (float) (var4 & 0xFF) / 255.0F;
        float var12 = (float) (var5 >> 24 & 0xFF) / 255.0F;
        float var13 = (float) (var5 >> 16 & 0xFF) / 255.0F;
        float var14 = (float) (var5 >> 8 & 0xFF) / 255.0F;
        float var15 = (float) (var5 & 0xFF) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator var16 = Tessellator.getInstance();
        BufferBuilder var17 = var16.getBuffer();
        var17.begin(7, DefaultVertexFormats.POSITION_COLOR);
        var17.pos(var2, var1, 0.0).color(var9, var10, var11, var8).endVertex();
        var17.pos(var0, var1, 0.0).color(var9, var10, var11, var8).endVertex();
        var17.pos(var0, var3, 0.0).color(var13, var14, var15, var12).endVertex();
        var17.pos(var2, var3, 0.0).color(var13, var14, var15, var12).endVertex();
        var16.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    /**
     * Renders a quad with the specified parameters.
     *
     * @param x1     The x-coordinate of the first vertex.
     * @param y1     The y-coordinate of the first vertex.
     * @param x2     The x-coordinate of the second vertex.
     * @param y2     The y-coordinate of the second vertex.
     * @param color3 The x-coordinate of the fourth vertex.
     * @param color  The color of the quad in integer format.
     */
    public static void drawQuad(int x1, int y1, int x2, int y2, int color, int color2, int color3, int color4) {
        float a1 = (float) (color >> 24 & 0xFF) / 255.0F;
        float r1 = (float) (color >> 16 & 0xFF) / 255.0F;
        float g1 = (float) (color >> 8 & 0xFF) / 255.0F;
        float b1 = (float) (color & 0xFF) / 255.0F;
        float a2 = (float) (color2 >> 24 & 0xFF) / 255.0F;
        float r2 = (float) (color2 >> 16 & 0xFF) / 255.0F;
        float g2 = (float) (color2 >> 8 & 0xFF) / 255.0F;
        float b2 = (float) (color2 & 0xFF) / 255.0F;
        float a3 = (float) (color3 >> 24 & 0xFF) / 255.0F;
        float r3 = (float) (color3 >> 16 & 0xFF) / 255.0F;
        float g3 = (float) (color3 >> 8 & 0xFF) / 255.0F;
        float b3 = (float) (color3 & 0xFF) / 255.0F;
        float a4 = (float) (color4 >> 24 & 0xFF) / 255.0F;
        float r4 = (float) (color4 >> 16 & 0xFF) / 255.0F;
        float g4 = (float) (color4 >> 8 & 0xFF) / 255.0F;
        float b4 = (float) (color4 & 0xFF) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder var27 = tessellator.getBuffer();
        var27.begin(7, DefaultVertexFormats.POSITION_COLOR);
        var27.pos(x2, y1, 0.0).color(r2, g2, b2, a2).endVertex();
        var27.pos(x1, y1, 0.0).color(r1, g1, b1, a1).endVertex();
        var27.pos(x1, y2, 0.0).color(r4, g4, b4, a4).endVertex();
        var27.pos(x2, y2, 0.0).color(r3, g3, b3, a3).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    /**
     * Renders a category box with the specified parameters.
     *
     * @param x            The x-coordinate of the top-left corner of the box.
     * @param y            The y-coordinate of the top-left corner of the box.
     * @param size         The size of the box.
     * @param color        The color of the box in integer format.
     * @param outlineColor The color of the box outline in integer format.
     */
    public static void renderCategoryBox(float x, float y, float size, int color, int outlineColor) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4fv(RenderUtil2.intColorToFloatArrayColor(color));
        GL11.glEnable(2881);
        GL11.glBegin(4);
        GL11.glVertex2f(x + size / 2.0F, y + size / 2.0F);
        GL11.glVertex2f(x + size / 2.0F, y - size / 2.0F);
        GL11.glVertex2f(x - size / 2.0F, y);
        GL11.glEnd();
        GL11.glLineWidth(2.0F);
        GL11.glColor4fv(RenderUtil2.intColorToFloatArrayColor(outlineColor));
        GL11.glBegin(3);
        GL11.glVertex2f(x + size / 2.0F, y + size / 2.0F);
        GL11.glVertex2f(x + size / 2.0F, y - size / 2.0F);
        GL11.glEnd();
        GL11.glBegin(3);
        GL11.glVertex2f(x - size / 2.0F, y);
        GL11.glVertex2f(x + size / 2.0F, y - size / 2.0F);
        GL11.glEnd();
        GL11.glBegin(3);
        GL11.glVertex2f(x + size / 2.0F, y + size / 2.0F);
        GL11.glVertex2f(x - size / 2.0F, y);
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
        Framebuffer currentFramebuffer = Minecraft.getInstance().getFramebuffer();

        if (currentFramebuffer != null && currentFramebuffer.depthBuffer > -1) {
            recreateDepthBuffer(currentFramebuffer);
            currentFramebuffer.depthBuffer = -1;
        }
    }

    /**
     * Initializes the stencil buffer for rendering operations.
     * This method sets up the OpenGL state for stencil testing, disables color and depth writing,
     * configures the stencil function and operation, and clears the stencil buffer.
     * It's typically used before performing stencil-based rendering techniques.
     * <p>
     * The method performs the following operations:
     * 1. Pushes the current matrix onto the stack.
     * 2. Resets the depth buffer.
     * 3. Enables stencil testing.
     * 4. Disables color and depth writing.
     * 5. Sets up the stencil function and operation.
     * 6. Clears the stencil buffer.
     * 7. Sets a flag indicating that stencil operations are in progress.
     * <p>
     * Note: This method doesn't take any parameters and doesn't return any value.
     * It modifies the OpenGL state and should be used in conjunction with method11478()
     * to restore the previous state after stencil operations are complete.
     */
    public static void initStencilBuffer() {
        GL11.glPushMatrix();
        resetDepthBuffer();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glStencilFunc(GL11.GL_ACCUM_BUFFER_BIT, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(1);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        stencilOpInProgress = true;
    }

    /**
     * Restores the previous stencil buffer state after stencil operations.
     * This method resets the OpenGL state modified by initStencilBuffer().
     * It disables stencil testing, restores the previous matrix state,
     * and marks stencil operations as no longer in progress.
     * <p>
     * The method performs the following operations:
     * 1. Resets the stencil mask to allow writing to all bits.
     * 2. Disables stencil testing.
     * 3. Pops the matrix stack, restoring the previous transformation state.
     * 4. Sets the stencil operation flag to false.
     * <p>
     * This method should be called after completing stencil-based rendering
     * to restore the OpenGL state to its previous condition.
     */
    public static void restorePreviousStencilBuffer() {
        GL11.glStencilMask(-1);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glPopMatrix();
        stencilOpInProgress = false;
    }

    public static void configureStencilTest() {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
    }

    public static void method11453(float var0, float var1, float var2, float var3, ByteBuffer var4, int color, float var6, float var7, float var8, float var9, boolean var10, boolean var11) {
        if (var4 != null) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
            var0 = (float) Math.round(var0);
            var2 = (float) Math.round(var2);
            var1 = (float) Math.round(var1);
            var3 = (float) Math.round(var3);
            float b = (float) (color >> 24 & 0xFF) / 255.0F;
            float a = (float) (color >> 16 & 0xFF) / 255.0F;
            float r = (float) (color >> 8 & 0xFF) / 255.0F;
            float g = (float) (color & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            RenderSystem.color4f(a, r, g, b);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, (int) var8, (int) var9, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, var4);
            float var19 = 1.0f;
            float var20 = 1.0f;
            float var21 = var6 / var8;
            float var22 = var7 / var9;
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
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void renderItem(ItemStack var0, int var1, int var2, int var3, int var4) {
        if (var0 != null) {
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
            GL11.glPushMatrix();
            GL11.glTranslatef((float) var1, (float) var2, 0.0F);
            GL11.glScalef((float) var3 / 16.0F, (float) var4 / 16.0F, 0.0F);
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

    public static double[] worldToScreen(double x, double y, double z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelViewMatrix = BufferUtils.createFloatBuffer(16);
        FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);

        // Get the current OpenGL matrices and viewport
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        // Project the world coordinates to screen coordinates
        boolean isSuccessful = projectToScreen((float) x, (float) y, (float) z, modelViewMatrix, projectionMatrix, viewport, screenCoords);

        // Return the screen coordinates if successful, otherwise return null
        return !isSuccessful
                ? null
                : new double[]{
                (double) (screenCoords.get(0) / GuiManager.scaleFactor),
                (double) (((float) mc.getFramebuffer().framebufferHeight - screenCoords.get(1)) / GuiManager.scaleFactor),
                (double) screenCoords.get(2)
        };
    }

    public static java.awt.Color getColorFromScreen(int mouseX, int mouseY, java.awt.Color var2) {
        mouseX = (int) ((float) mouseX * GuiManager.scaleFactor);
        mouseY = (int) ((float) mouseY * GuiManager.scaleFactor);
        ByteBuffer var5 = ByteBuffer.allocateDirect(3);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glReadPixels(mouseX, Minecraft.getInstance().getMainWindow().getFramebufferHeight() - mouseY, 1, 1, GL11.GL_RGB, GL11.GL_BYTE, var5);
        return new java.awt.Color(var5.get(0) * 2, var5.get(1) * 2, var5.get(2) * 2, 1);
    }

    public static int applyAlpha(int color, float alpha) {
        return (int) (alpha * 255.0F) << 24 | color & 16777215;
    }

    public static boolean projectToScreen(float x, float y, float z, FloatBuffer modelMatrix, FloatBuffer projectionMatrix, IntBuffer viewport, FloatBuffer screenCoords) {
        float[] inVector = PlayerUtil.field24951;
        float[] outVector = PlayerUtil.field24952;

        // Load input coordinates into the vector
        inVector[0] = x;
        inVector[1] = y;
        inVector[2] = z;
        inVector[3] = 1.0F;

        // Apply the model and projection transformations
        transformVector(modelMatrix, inVector, outVector);
        transformVector(projectionMatrix, outVector, inVector);

        // Perform perspective division if the w-component is non-zero
        if ((double) inVector[3] != 0.0) {
            inVector[3] = 1.0F / inVector[3] * 0.5F;
            inVector[0] = inVector[0] * inVector[3] + 0.5F;
            inVector[1] = inVector[1] * inVector[3] + 0.5F;
            inVector[2] = inVector[2] * inVector[3] + 0.5F;

            // Map to screen coordinates using the viewport
            screenCoords.put(0, inVector[0] * (float) viewport.get(viewport.position() + 2) + (float) viewport.get(viewport.position()));
            screenCoords.put(1, inVector[1] * (float) viewport.get(viewport.position() + 3) + (float) viewport.get(viewport.position() + 1));
            screenCoords.put(2, inVector[2]);

            return true;
        } else {
            return false;
        }
    }

    public static float[] method17709(int var0) {
        float var3 = (float) (var0 >> 24 & 0xFF) / 255.0F;
        float var4 = (float) (var0 >> 16 & 0xFF) / 255.0F;
        float var5 = (float) (var0 >> 8 & 0xFF) / 255.0F;
        float var6 = (float) (var0 & 0xFF) / 255.0F;
        return new float[]{var4, var5, var6, var3};
    }

    public static int method17691(int var0, float var1) {
        int var4 = var0 >> 24 & 0xFF;
        int var5 = var0 >> 16 & 0xFF;
        int var6 = var0 >> 8 & 0xFF;
        int var7 = var0 & 0xFF;
        int var8 = (int) ((float) var5 * (1.0F - var1));
        int var9 = (int) ((float) var6 * (1.0F - var1));
        int var10 = (int) ((float) var7 * (1.0F - var1));
        return var4 << 24 | (var8 & 0xFF) << 16 | (var9 & 0xFF) << 8 | var10 & 0xFF;
    }

    public static int method17690(int var0, int var1, float var2) {
        int var5 = var0 >> 24 & 0xFF;
        int var6 = var0 >> 16 & 0xFF;
        int var7 = var0 >> 8 & 0xFF;
        int var8 = var0 & 0xFF;
        int var9 = var1 >> 24 & 0xFF;
        int var10 = var1 >> 16 & 0xFF;
        int var11 = var1 >> 8 & 0xFF;
        int var12 = var1 & 0xFF;
        float var13 = 1.0F - var2;
        float var14 = (float) var5 * var2 + (float) var9 * var13;
        float var15 = (float) var6 * var2 + (float) var10 * var13;
        float var16 = (float) var7 * var2 + (float) var11 * var13;
        float var17 = (float) var8 * var2 + (float) var12 * var13;
        return (int) var14 << 24 | ((int) var15 & 0xFF) << 16 | ((int) var16 & 0xFF) << 8 | (int) var17 & 0xFF;
    }

    public static java.awt.Color method17682(java.awt.Color... var0) {
        if (var0 != null) {
            if (var0.length > 0) {
                float var3 = 1.0F / (float) var0.length;
                float var4 = 0.0F;
                float var5 = 0.0F;
                float var6 = 0.0F;
                float var7 = 0.0F;

                for (java.awt.Color var11 : var0) {
                    if (var11 == null) {
                        var11 = java.awt.Color.BLACK;
                    }

                    var4 += (float) var11.getRed() * var3;
                    var5 += (float) var11.getGreen() * var3;
                    var6 += (float) var11.getBlue() * var3;
                    var7 += (float) var11.getAlpha() * var3;
                }

                return new java.awt.Color(var4 / 255.0F, var5 / 255.0F, var6 / 255.0F, var7 / 255.0F);
            } else {
                return java.awt.Color.WHITE;
            }
        } else {
            return java.awt.Color.WHITE;
        }
    }

    public static java.awt.Color method17681(java.awt.Color var0, java.awt.Color var1, float var2) {
        float var5 = 1.0F - var2;
        float var6 = (float) var0.getRed() * var2 + (float) var1.getRed() * var5;
        float var7 = (float) var0.getGreen() * var2 + (float) var1.getGreen() * var5;
        float var8 = (float) var0.getBlue() * var2 + (float) var1.getBlue() * var5;
        return new java.awt.Color(var6 / 255.0F, var7 / 255.0F, var8 / 255.0F);
    }

    public static void transformVector(FloatBuffer matrixBuffer, float[] inputVector, float[] outputVector) {
        for (int i = 0; i < 4; i++) {
            outputVector[i] = inputVector[0] * matrixBuffer.get(matrixBuffer.position() + i)
                    + inputVector[1] * matrixBuffer.get(matrixBuffer.position() + 4 + i)
                    + inputVector[2] * matrixBuffer.get(matrixBuffer.position() + 8 + i)
                    + inputVector[3] * matrixBuffer.get(matrixBuffer.position() + 12 + i);
        }
    }
}



