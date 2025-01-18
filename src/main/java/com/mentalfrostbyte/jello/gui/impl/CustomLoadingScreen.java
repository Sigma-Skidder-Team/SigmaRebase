package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.opengl.Texture;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.function.Consumer;

public class CustomLoadingScreen extends LoadingGui {

        private final Minecraft client;
        private final IAsyncReloader reloadMonitor;
        private final Consumer<Optional<Throwable>> exceptionHandler;
        private final boolean reloading;

        public static Texture sigmaLogo;
        public static Texture back;
        public static Texture background;

        private float progress;
        private long applyCompleteTime = -1L;
        private long prepareCompleteTime = -1L;

        public CustomLoadingScreen(Minecraft client, IAsyncReloader monitor,
                        Consumer<Optional<Throwable>> exceptionHandler,
                        boolean reloading) {
                this.client = client;
                this.reloadMonitor = monitor;
                this.exceptionHandler = exceptionHandler;
                this.reloading = reloading;

                sigmaLogo = Resources.loadTexture("com/mentalfrostbyte/gui/resources/sigma/logo.png");
                back = Resources.loadTexture("com/mentalfrostbyte/gui/resources/loading/back.png");
                background = Resources.createScaledAndProcessedTexture2(
                                "com/mentalfrostbyte/gui/resources/loading/back.png",
                                0.25F, 25);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                long var9 = Util.milliTime();
                if (this.reloading && (this.reloadMonitor.asyncPartDone() || this.client.currentScreen != null)
                                && this.prepareCompleteTime == -1L) {
                        this.prepareCompleteTime = var9;
                }

                float var11 = this.applyCompleteTime > -1L ? (float) (var9 - this.applyCompleteTime) / 200.0F : -1.0F;
                float var12 = this.prepareCompleteTime > -1L ? (float) (var9 - this.prepareCompleteTime) / 100.0F
                                : -1.0F;
                float var13 = 1.0F;
                float var16 = this.reloadMonitor.estimateExecutionSpeed();
                this.progress = this.progress * 0.95F + var16 * 0.050000012F;
                GL11.glPushMatrix();
                float var17 = 1111.0F;
                if (this.client.getWindow().getWidth() != 0) {
                        var17 = (float) (this.client.getWindow().getFramebufferWidth()
                                        / this.client.getWindow().getWidth());
                }

                float var18 = (float) this.client.getWindow().calcGuiScale(this.client.gameSettings.guiScale,
                                this.client.getForceUnicodeFont()) * var17;
                GL11.glScalef(1.0F / var18, 1.0F / var18, 0.0F);
                xd(var13, this.progress);
                GL11.glPopMatrix();
                if (var11 >= 2.0F) {
                        this.client.setLoadingGui(null);
                }

                if (this.applyCompleteTime == -1L && this.reloadMonitor.fullyDone()
                                && (!this.reloading || var12 >= 2.0F)) {
                        try {
                                this.reloadMonitor.join();
                                this.exceptionHandler.accept(Optional.empty());
                        } catch (Throwable var20) {
                                this.exceptionHandler.accept(Optional.of(var20));
                        }

                        this.applyCompleteTime = Util.milliTime();
                        if (this.client.currentScreen != null) {
                                this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(),
                                                this.client.getWindow().getScaledHeight());
                        }
                }
        }

        public static void xd(float var0, float var1) {
                GL11.glEnable(3008);
                GL11.glEnable(3042);
                RenderUtil.drawImage(0.0F, 0.0F, (float) MinecraftClient.getInstance().getWindow().getWidth(),
                                (float) MinecraftClient.getInstance().getWindow().getHeight(), background, var0);
                RenderUtil.drawRoundedRect2(0.0F, 0.0F, (float) MinecraftClient.getInstance().getWindow().getWidth(),
                                (float) MinecraftClient.getInstance().getWindow().getHeight(),
                                ColorUtils.applyAlpha(0, 0.75F));
                int var4 = 455;
                int var5 = 78;
                int var6 = (MinecraftClient.getInstance().getWindow().getWidth() - var4) / 2;
                int var7 = Math
                                .round((float) ((MinecraftClient.getInstance().getWindow().getHeight() - var5) / 2)
                                                - 14.0F * var0);
                float var8 = 0.75F + var0 * var0 * var0 * var0 * 0.25F;
                GL11.glPushMatrix();
                GL11.glTranslatef((float) (MinecraftClient.getInstance().getWindow().getWidth() / 2),
                                (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2), 0.0F);
                GL11.glScalef(var8, var8, 0.0F);
                GL11.glTranslatef((float) (-MinecraftClient.getInstance().getWindow().getWidth() / 2),
                                (float) (-MinecraftClient.getInstance().getWindow().getHeight() / 2), 0.0F);
                RenderUtil.drawImage((float) var6, (float) var7, (float) var4, (float) var5, sigmaLogo,
                                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var0));
                float var9 = Math.min(1.0F, var1 * 1.02F);
                float var11 = 80;
                if (var0 == 1.0F) {
                        RenderUtil.drawRoundedRect(
                                        (float) var6, (float) (var7 + var5 + var11), (float) var4, 20.0F, 10.0F,
                                        ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F * var0));
                        RenderUtil.drawRoundedRect(
                                        (float) (var6 + 1),
                                        (float) (var7 + var5 + var11 + 1),
                                        (float) (var4 - 2),
                                        18.0F,
                                        9.0F,
                                        ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 1.0F * var0));
                }

                RenderUtil.drawRoundedRect(
                                (float) (var6 + 2),
                                (float) (var7 + var5 + var11 + 2),
                                (float) ((int) ((float) (var4 - 4) * var9)),
                                16.0F,
                                8.0F,
                                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.9F * var0));
                GL11.glPopMatrix();
        }
}
