package com.mentalfrostbyte.jello.module.impl.render.jello.esp;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderEntity;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderNameTag;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.render.jello.esp.util.RenderShapeMode;
import com.mentalfrostbyte.jello.module.settings.impl.ColorSetting;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.world.PositionUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

public class ShadowESP extends Module {
    private static RenderState currentRenderMode = RenderState.DEFAULT;
    private final IRenderTypeBuffer.Impl renderBuffer = IRenderTypeBuffer.getImpl(mc.getRenderTypeBuffers().fixedBuffers, new BufferBuilder(256));

    public ShadowESP() {
        super(ModuleCategory.RENDER, "Shadow", "Draws a line arround entities");
        this.registerSetting(new ColorSetting("Color", "The tracers color", ClientColors.LIGHT_GREYISH_BLUE.getColor()));
    }

    @EventTarget
    public void onRenderEntity(EventRenderEntity event) {
        if (currentRenderMode != RenderState.DEFAULT) {
            event.setRender(false);
        }
    }

    @EventTarget
    public void onNametagRender(EventRenderNameTag event) {
        if (currentRenderMode != RenderState.DEFAULT && event.getEntity() instanceof PlayerEntity) {
            event.cancelled = true;
        }
    }

    @EventTarget
    public void onRender(EventRender3D event) {
        if (mc.player != null && mc.world != null) {
            this.setup();
            RenderUtil.beginStencilWrite();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.applyRenderMode(RenderState.PRE_RENDER);
            RenderUtil.configureStencilTest(RenderShapeMode.OUTLINE);
            GL11.glLineWidth(1.0F);
            this.renderShadowSprites();
            this.applyRenderMode(RenderState.OUTLINE);
            RenderSystem.alphaFunc(518, 0.0F);
            RenderSystem.enableAlphaTest();
            GL11.glColor4f(1.0F, 0.0F, 1.0F, 0.1F);
            GL11.glEnable(3042);
            GL11.glDisable(2896);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.endStencilWrite();
            this.reset();
            this.renderBuffer.finish();
        }
    }

    public void renderShadowSprites() {
        int color = RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F);
        mc.world.entitiesById.forEach((i, entity) -> {
            if (this.isValid(entity)) {
                double x = PositionUtil.getRelativePosition(entity).x;
                double y = PositionUtil.getRelativePosition(entity).y;
                double z = PositionUtil.getRelativePosition(entity).z;
                GL11.glPushMatrix();
                GL11.glAlphaFunc(519, 0.0F);
                GL11.glTranslated(x, y, z);
                GL11.glTranslatef(0.0F, entity.getHeight(), 0.0F);
                GL11.glTranslatef(0.0F, 0.1F, 0.0F);
                GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
                GL11.glScalef(-0.11F, -0.11F, -0.11F);
                RenderUtil.drawImage2(
                        -entity.getWidth() * 22.0F,
                        -entity.getHeight() * 5.5F,
                        entity.getWidth() * 44.0F,
                        entity.getHeight() * 21.0F,
                        Resources.shadowPNG,
                        color,
                        false);
                Resources.shoutIconPNG.bind();
                GL11.glPopMatrix();
            }
        });
    }

    public void applyRenderMode(RenderState renderState) {
        GL11.glDepthFunc(519);
        currentRenderMode = renderState;

        int colorValue = this.parseSettingValueToIntBySettingName("Color");
        float alpha = (float) (colorValue >> 24 & 0xFF) / 255.0F;
        float red = (float) (colorValue >> 16 & 0xFF) / 255.0F;
        float green = (float) (colorValue >> 8 & 0xFF) / 255.0F;
        float blue = (float) (colorValue & 0xFF) / 255.0F;

        GL11.glEnable(2896);
        GL11.glLightModelfv(2899, new float[]{red, green, blue, alpha});

        RenderSystem.enableLighting();

        if (currentRenderMode == RenderState.OUTLINE) {
            GL11.glEnable(10754);
            GL11.glLineWidth(2.0F);
            GL11.glPolygonMode(1032, 6913);
            GL11.glDisable(3553);
            GL11.glEnable(3008);
            GL11.glEnable(2896);
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (this.isValid(entity)) {
                GL11.glPushMatrix();

                Vector3d cameraPos = mc.gameRenderer.getActiveRenderInfo().getPos();
                double renderPosX = cameraPos.getX();
                double renderPosY = cameraPos.getY();
                double renderPosZ = cameraPos.getZ();

                MatrixStack matrixStack = new MatrixStack();
                boolean previousShadowState = mc.gameSettings.entityShadows;

                RenderSystem.disableLighting();
                RenderSystem.color4f(0.0F, 0.0F, 1.0F, 0.5F);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.enableBlend();

                mc.gameSettings.entityShadows = false;
                int fireTimer = entity.getFireTimer();
                boolean burning = entity.getFlag(0);

                entity.setFire(0);
                entity.setFlag(0, false);

                this.renderEntity(entity, renderPosX, renderPosY, renderPosZ, mc.timer.renderPartialTicks, matrixStack, this.renderBuffer);

                entity.setFire(fireTimer);
                entity.setFlag(0, burning);
                mc.gameSettings.entityShadows = previousShadowState;
                GL11.glPopMatrix();
            }
        }

        this.renderBuffer.finish(RenderType.getEntitySolid(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        this.renderBuffer.finish(RenderType.getEntityCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        this.renderBuffer.finish(RenderType.getEntityCutoutNoCull(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        this.renderBuffer.finish(RenderType.getEntitySmoothCutout(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
        this.renderBuffer.finish(RenderType.getLines());
        this.renderBuffer.finish();

        if (currentRenderMode == RenderState.OUTLINE) {
            GL11.glPolygonMode(1032, 6914);
            GL11.glDisable(10754);
        }

        currentRenderMode = RenderState.DEFAULT;
        GL11.glDepthFunc(515);
    }

    public void renderEntity(Entity entity, double offsetX, double offsetY, double offsetZ, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer typeBuffer) {
        double interpolatedX = MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX());
        double interpolatedY = MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY());
        double interpolatedZ = MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ());
        float interpolatedYaw = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);

        mc.worldRenderer.renderManager.renderEntityStatic(entity, interpolatedX - offsetX, interpolatedY - offsetY, interpolatedZ - offsetZ, interpolatedYaw, partialTicks, matrixStack, typeBuffer, 238);
    }

    public boolean isValid(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        if (entity instanceof ClientPlayerEntity) {
            return false;
        }

        return !entity.isInvisible()
                && !Client.getInstance().botManager.isBot(entity);
    }

    public void setup() {
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
        mc.gameRenderer.lightmapTexture.enableLightmap();
    }

    private void reset() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(2896);
        GL11.glEnable(3553);
        GL11.glEnable(2903);
        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
        mc.gameRenderer.lightmapTexture.enableLightmap();
        GL11.glLightModelfv(2899, new float[]{0.4F, 0.4F, 0.4F, 1.0F});
        currentRenderMode = RenderState.DEFAULT;
    }

    private enum RenderState {
        DEFAULT,
        PRE_RENDER,
        OUTLINE
    }
}