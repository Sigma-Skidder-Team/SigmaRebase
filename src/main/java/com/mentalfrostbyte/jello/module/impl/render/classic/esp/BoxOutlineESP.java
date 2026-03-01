package com.mentalfrostbyte.jello.module.impl.render.classic.esp;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.render.jello.esp.util.Class2329;
import com.mentalfrostbyte.jello.module.settings.impl.ColorSetting;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
import com.mentalfrostbyte.jello.util.game.world.PositionUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;
import team.sdhq.eventBus.annotations.EventTarget;

public class BoxOutlineESP extends Module {
    public int color = RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F);

    public BoxOutlineESP() {
        super(ModuleCategory.RENDER, "Box Outline", "Draws a line arround players");
        this.registerSetting(
                new ColorSetting("Color", "The tracers color", ClientColors.LIGHT_GREYISH_BLUE.getColor()));
    }

    @EventTarget
    public void onRender(EventRender3D event) {
        if (this.isEnabled()) {
            if (mc.player != null && mc.world != null) {
                this.method16509();
                RenderUtil.method11476();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.method16508(false);
                RenderUtil.method11477(Class2329.field15941);
                GL11.glLineWidth(3.0F);
                RenderSystem.alphaFunc(518, 0.0F);
                RenderSystem.enableAlphaTest();
                this.method16507();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.1F);
                GL11.glEnable(3042);
                GL11.glDisable(2896);
                this.method16508(true);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.method11478();
                this.method16510();
            }
        }
    }

    private void method16507() {
        if (Client.getInstance().clientMode == ClientMode.JELLO) {
            mc.world.entitiesById
                    .forEach(
                            (var1, var2) -> {
                                boolean var5 = PlayerUtil.getEntityCategory(var2) == PlayerUtil.EntityTypeCategory.PLAYER
                                        && this.getParent().getBooleanValueFromSettingName("Show Players");
                                boolean var6 = !var2.isInvisible()
                                        || this.getParent().getBooleanValueFromSettingName("Show Invisibles");
                                if (!Client.getInstance().botManager.isBot(var2) && var5 && var6
                                        && var2 != mc.player) {
                                    double var7 = PositionUtil.getRelativePosition(var2).x;
                                    double var9 = PositionUtil.getRelativePosition(var2).y;
                                    double var11 = PositionUtil.getRelativePosition(var2).z;
                                    GL11.glPushMatrix();
                                    GL11.glAlphaFunc(519, 0.0F);
                                    GL11.glTranslated(var7, var9, var11);
                                    GL11.glTranslatef(0.0F, var2.getHeight(), 0.0F);
                                    GL11.glTranslatef(0.0F, 0.1F, 0.0F);
                                    GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
                                    GL11.glScalef(-0.11F, -0.11F, -0.11F);
                                    RenderUtil.drawImage2(
                                            -var2.getWidth() * 22.0F,
                                            -var2.getHeight() * 5.5F,
                                            var2.getWidth() * 44.0F,
                                            var2.getHeight() * 21.0F,
                                            Resources.shadowPNG,
                                            this.color,
                                            false);
                                    Resources.shoutIconPNG.bind();
                                    GL11.glPopMatrix();
                                }
                            });
        }
    }

    private void method16508(boolean var1) {
        for (Entity var5 : mc.world.getAllEntities()) {
            if (!Client.getInstance().botManager.isBot(var5)) {
                boolean var6 = PlayerUtil.getEntityCategory(var5) == PlayerUtil.EntityTypeCategory.PLAYER
                        && this.getParent().getBooleanValueFromSettingName("Show Players");
                boolean var7 = PlayerUtil.getEntityCategory(var5) == PlayerUtil.EntityTypeCategory.MONSTER
                        && this.getParent().getBooleanValueFromSettingName("Show Mobs");
                boolean var8 = PlayerUtil.getEntityCategory(var5) == PlayerUtil.EntityTypeCategory.NON_PLAYER
                        && this.getParent().getBooleanValueFromSettingName("Show Passives");
                boolean var9 = !var5.isInvisible() || this.getParent().getBooleanValueFromSettingName("Show Invisibles");
                if ((var7 || var6 || var8) && var9 && var5 != mc.player) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(
                            -mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                            -mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                            -mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
                    GL11.glDisable(2929);
                    GL11.glEnable(3042);
                    int var10 = this.parseSettingValueToIntBySettingName("Color");
                    double var11 = (var5.getPosX() - var5.lastTickPosX) * (double) mc.timer.renderPartialTicks
                            - (var5.getPosX() - var5.lastTickPosX);
                    double var13 = (var5.getPosY() - var5.lastTickPosY) * (double) mc.timer.renderPartialTicks
                            - (var5.getPosY() - var5.lastTickPosY);
                    double var15 = (var5.getPosZ() - var5.lastTickPosZ) * (double) mc.timer.renderPartialTicks
                            - (var5.getPosZ() - var5.lastTickPosZ);
                    BoundingBox var17 = new BoundingBox(var5.getBoundingBox().offset(var11, var13, var15)).expand(0.1F);
                    if (var1) {
                        RenderUtil.renderWireframeBox(var17, 3.0F, RenderUtil2.applyAlpha(var10,
                                Client.getInstance().clientMode != ClientMode.JELLO ? 0.8F : 0.35F));
                    } else {
                        RenderUtil.render3DColoredBox(var17, ClientColors.LIGHT_GREYISH_BLUE.getColor());
                    }

                    GL11.glDisable(3042);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    private void method16509() {
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

    private void method16510() {
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
