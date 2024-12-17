package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.opengl.Texture;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.function.Consumer;

public class CustomLoadingScreen extends LoadingGui {

    private Minecraft mc;
    private final IAsyncReloader reloader;
    private final Consumer<Optional<Throwable>> throwable;
    private final boolean b;

    public static Texture sigmaLogo;
    public static Texture back;
    public static Texture background;

    private float field6771;
    private long field6772 = -1L;
    private long field6773 = -1L;

    public CustomLoadingScreen(Minecraft mc, IAsyncReloader r, Consumer<Optional<Throwable>> t, boolean b) {
        this.mc = mc;
        this.reloader = r;
        this.throwable = t;
        this.b = b;

        sigmaLogo = Resources.loadTexture("com/mentalfrostbyte/gui/resources/sigma/logo.png");
        back = Resources.loadTexture("com/mentalfrostbyte/gui/resources/loading/back.png");
        background = Resources.createScaledAndProcessedTexture2("com/mentalfrostbyte/gui/resources/loading/back.png", 0.25F, 25);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        long var9 = Util.milliTime();
        if (this.b && (this.reloader.asyncPartDone() || this.mc.currentScreen != null) && this.field6773 == -1L) {
            this.field6773 = var9;
        }

        float var11 = this.field6772 > -1L ? (float)(var9 - this.field6772) / 200.0F : -1.0F;
        float var12 = this.field6773 > -1L ? (float)(var9 - this.field6773) / 100.0F : -1.0F;
        float var13 = 1.0F;
        float var16 = this.reloader.estimateExecutionSpeed();
        this.field6771 = this.field6771 * 0.95F + var16 * 0.050000012F;
        GL11.glPushMatrix();
        float var17 = 1111.0F;
        if (this.mc.getMainWindow().getWidth() != 0) {
            var17 = (float)(this.mc.getMainWindow().getFramebufferWidth() / this.mc.getMainWindow().getWidth());
        }

        float var18 = (float)this.mc.getMainWindow().calcGuiScale(this.mc.gameSettings.guiScale, this.mc.getForceUnicodeFont()) * var17;
        GL11.glScalef(1.0F / var18, 1.0F / var18, 0.0F);
        xd(var13, this.field6771);
        GL11.glPopMatrix();
        if (var11 >= 2.0F) {
            this.mc.setLoadingGui(null);
        }

        if (this.field6772 == -1L && this.reloader.fullyDone() && (!this.b || var12 >= 2.0F)) {
            try {
                this.reloader.join();
                this.throwable.accept(Optional.empty());
            } catch (Throwable var20) {
                this.throwable.accept(Optional.of(var20));
            }

            this.field6772 = Util.milliTime();
            if (this.mc.currentScreen != null) {
                this.mc.currentScreen.init(this.mc, this.mc.getMainWindow().getScaledWidth(), this.mc.getMainWindow().getScaledHeight());
            }
        }
    }

    public static void xd(float var0, float var1) {
        GL11.glEnable(3008);
        GL11.glEnable(3042);
        RenderUtil.drawImage(0.0F, 0.0F, (float) Minecraft.getInstance().getMainWindow().getWidth(), (float) Minecraft.getInstance().getMainWindow().getHeight(), background, var0);
        RenderUtil.renderBackgroundBox(0.0F, 0.0F, (float) Minecraft.getInstance().getMainWindow().getWidth(), (float) Minecraft.getInstance().getMainWindow().getHeight(), ColorUtils.applyAlpha(0, 0.75F));
        int var4 = 455;
        int var5 = 78;
        int var6 = (Minecraft.getInstance().getMainWindow().getWidth() - var4) / 2;
        int var7 = Math.round((float)((Minecraft.getInstance().getMainWindow().getHeight() - var5) / 2) - 14.0F * var0);
        float var8 = 0.75F + var0 * var0 * var0 * var0 * 0.25F;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(Minecraft.getInstance().getMainWindow().getWidth() / 2), (float)(Minecraft.getInstance().getMainWindow().getHeight() / 2), 0.0F);
        GL11.glScalef(var8, var8, 0.0F);
        GL11.glTranslatef((float)(-Minecraft.getInstance().getMainWindow().getWidth() / 2), (float)(-Minecraft.getInstance().getMainWindow().getHeight() / 2), 0.0F);
        RenderUtil.drawImage((float)var6, (float)var7, (float)var4, (float)var5, sigmaLogo, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var0));
        float var9 = Math.min(1.0F, var1 * 1.02F);
        float var11 = 80;
        if (var0 == 1.0F) {
            RenderUtil.drawRect(
                    (float)var6, (float)(var7 + var5 + var11), (float)var4, 20.0F, 10.0F, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F * var0)
            );
            RenderUtil.drawRect(
                    (float)(var6 + 1),
                    (float)(var7 + var5 + var11 + 1),
                    (float)(var4 - 2),
                    18.0F,
                    9.0F,
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 1.0F * var0)
            );
        }

        RenderUtil.drawRect(
                (float)(var6 + 2),
                (float)(var7 + var5 + var11 + 2),
                (float)((int)((float)(var4 - 4) * var9)),
                16.0F,
                8.0F,
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.9F * var0)
        );
        GL11.glPopMatrix();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
