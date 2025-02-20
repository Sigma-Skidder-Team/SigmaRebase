package com.mentalfrostbyte.jello.util.game.render;

import com.google.gson.JsonSyntaxException;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.util.game.render.shader.SigmaBlurShader;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowestPriority;

import java.io.IOException;

public class BlurEngine {
    private static final Minecraft mc = Minecraft.getInstance();
    private static ShaderGroup blurShader;
    public static Framebuffer frameBuff;
    public static Framebuffer frameBuff2;
    public static int frameBuffWidth = mc.getFramebuffer().framebufferWidth;
    public static int frameBuffHeight = mc.getFramebuffer().framebufferHeight;
    public static int screenWidth = 0;
    public static int screenHeight = 0;

    public void init() {
        EventBus.register(this);
    }

    public static void drawBlur(int var0, int var1, int var2, int var3) {
        frameBuffWidth = Math.min(var0, frameBuffWidth);
        frameBuffHeight = Math.min(var1, frameBuffHeight);
        screenWidth = Math.max(var0 + var2, screenWidth);
        screenHeight = Math.max(var1 + var3, screenHeight);
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (event.packet instanceof SCloseWindowPacket) {
            RenderUtil2.resetShaders();
        }
    }

    @EventTarget
    @LowestPriority
    public void on3DRender(EventRender3D event) {
        if (Client.getInstance().guiManager.getHqIngameBlur() && frameBuffWidth < screenWidth && frameBuffHeight < screenHeight) {
            if (frameBuff == null) {
                try {
                    blurShader = new ShaderGroup(mc.getTextureManager(), new SigmaBlurShader(), mc.getFramebuffer(), new ResourceLocation("jelloblur"));
                    blurShader.createBindFramebuffers(mc.getFramebuffer().framebufferWidth, mc.getFramebuffer().framebufferHeight);
                    blurShader.listShaders.get(0).getShaderManager().getShaderUniform("Radius").set(35.0F);
                    blurShader.listShaders.get(1).getShaderManager().getShaderUniform("Radius").set(35.0F);
                    frameBuff = blurShader.getFramebuffer("jello");
                    frameBuff2 = blurShader.getFramebuffer("jelloswap");
                } catch (IOException | JsonSyntaxException var5) {
                    var5.printStackTrace();
                }
            }

            if (frameBuff.framebufferHeight != mc.getFramebuffer().framebufferHeight || frameBuff.framebufferWidth != mc.getFramebuffer().framebufferWidth) {
                blurShader.createBindFramebuffers(mc.getFramebuffer().framebufferWidth, mc.getFramebuffer().framebufferHeight);
            }

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.enableBlend();
            GL11.glDisable(2929);
            GL11.glDisable(3008);
            RenderSystem.disableBlend();
            frameBuff.framebufferClear(true);
            frameBuff2.framebufferClear(true);
            RenderSystem.clear(256, Minecraft.IS_RUNNING_ON_MAC);
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.ortho(
                    0.0,
                    (double) mc.mainWindow.getFramebufferWidth() / mc.mainWindow.getGuiScaleFactor(),
                    (double) mc.mainWindow.getFramebufferHeight() / mc.mainWindow.getGuiScaleFactor(),
                    0.0,
                    1000.0,
                    3000.0
            );
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
            GL11.glScaled(1.0 / mc.mainWindow.getGuiScaleFactor() * (double) GuiManager.scaleFactor, 1.0 / mc.mainWindow.getGuiScaleFactor() * (double) GuiManager.scaleFactor, 1.0);
            int var4 = 35;
            RenderUtil.drawBlurredBackground(frameBuffWidth, frameBuffHeight - var4, screenWidth, screenHeight + var4);
            blurShader.render(mc.timer.renderPartialTicks);
            RenderUtil.endScissor();
            GL11.glEnable(3008);
            frameBuff.bindFramebuffer(true);
            mc.getFramebuffer().bindFramebuffer(true);
        }

        frameBuffWidth = mc.getFramebuffer().framebufferWidth;
        frameBuffHeight = mc.getFramebuffer().framebufferHeight;
        screenWidth = 0;
        screenHeight = 0;
    }

    public static void endBlur() {
        if (frameBuff != null) {
            GL11.glPushMatrix();
            frameBuff.bindFramebufferTexture();
            frameBuff.framebufferRender(mc.getFramebuffer().framebufferWidth, mc.getFramebuffer().framebufferHeight);
            GL11.glPopMatrix();
            RenderSystem.clear(256, Minecraft.IS_RUNNING_ON_MAC);
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.ortho(
                    0.0,
                    (double) mc.mainWindow.getFramebufferWidth() / mc.mainWindow.getGuiScaleFactor(),
                    (double) mc.mainWindow.getFramebufferHeight() / mc.mainWindow.getGuiScaleFactor(),
                    0.0,
                    1000.0,
                    3000.0
            );
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
            GL11.glScaled(
                    1.0 / mc.mainWindow.getGuiScaleFactor() * (double) GuiManager.scaleFactor, 1.0 / mc.mainWindow.getGuiScaleFactor() * (double) GuiManager.scaleFactor, 1.0
            );
            mc.getFramebuffer().bindFramebuffer(true);
        }
    }
}
