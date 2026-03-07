package com.mentalfrostbyte.jello.module.impl.render.jello.esp;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.awt.*;

public class SimsESP extends Module {
    public SimsESP() {
        super(ModuleCategory.RENDER, "Sims", "You know what it is");
    }

    public static void renderDirectionalTriangleRing() {
        Color[] triangleColors = new Color[]{
                new Color(136, 217, 72),
                new Color(124, 189, 72),
                new Color(103, 181, 75),
                new Color(136, 217, 72),
                new Color(124, 189, 72),
                new Color(103, 181, 75),
                new Color(136, 217, 72),
                new Color(103, 181, 75)
        };

        for (int rotation = 0; rotation <= 315; rotation += 45) {
            GL11.glPushMatrix();
            GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
            int colorIndex = rotation / 45;
            renderTriangleIndicator((float) triangleColors[colorIndex].getRed() / 255.0F, (float) triangleColors[colorIndex].getGreen() / 255.0F, (float) triangleColors[colorIndex].getBlue() / 255.0F);
            GL11.glPopMatrix();
        }

        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

        for (int rotation = 0; rotation <= 315; rotation += 45) {
            GL11.glPushMatrix();
            GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            int colorIndex = rotation / 45;
            Color color = new Color(RenderUtil.darkenColor(triangleColors[colorIndex].getRGB(), 0.2F), false);
            renderTriangleIndicator((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F);
            GL11.glPopMatrix();
        }
    }

    public static void renderTriangleIndicator(float red, float green, float blue) {
        GL11.glColor3f(red, green, blue);

        GL11.glTranslatef(0.0F, 0.0F, 0.25F);
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);

        GL11.glRotated(-30.0, 1.0, 0.0, 0.0);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(0.0F, 0.5F);
        GL11.glVertex2f(-0.105F, 0.0F);
        GL11.glVertex2f(0.105F, 0.0F);
        GL11.glEnd();
    }

    public static void renderEntityTriangleIndicator(double x, double y, double z, Entity entity) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glDepthMask(false);
        GL11.glPushMatrix();

        GL11.glTranslated(
                x - mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                y - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                z - mc.gameRenderer.getActiveRenderInfo().getPos().getZ()
        );

        GL11.glRotated(entity.ticksExisted % 180 * 2, 0.0, -1.0, 0.0);

        float bobOffset = (float) (entity.ticksExisted % 100 - 50);
        if (bobOffset < 0.0F) {
            bobOffset *= -1.0F;
        }

        GL11.glTranslated(0.0, 0.7F + bobOffset / 500.0F, 0.0);
        renderDirectionalTriangleRing();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @EventTarget
    public void onRender(EventRender3D event) {
        for (Entity player : BlockUtil.sortPlayers(CombatUtil.getAllPlayersInWorld())) {
            if (player != mc.player && !Client.getInstance().botManager.isBot(player)) {
                renderEntityTriangleIndicator(
                        player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double) Minecraft.getInstance().timer.renderPartialTicks,
                        player.lastTickPosY + (double) player.getHeight() + (player.getPosY() - player.lastTickPosY) * (double) Minecraft.getInstance().timer.renderPartialTicks,
                        player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double) Minecraft.getInstance().timer.renderPartialTicks,
                        player
                );
            }
        }
    }
}
