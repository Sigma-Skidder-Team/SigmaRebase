package com.mentalfrostbyte.jello.module.impl.combat.killaura;

import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class AuraESP {
    public static Minecraft mc = Minecraft.getInstance();
    public final KillAura killauraMod;

    public AuraESP(KillAura var1) {
        this.killauraMod = var1;
    }


    public void renderEsp(Entity var1) {
        GL11.glPushMatrix();
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glEnable(32925);
        GL11.glEnable(2929);
        GL11.glLineWidth(1.4F);
        double var4 = Minecraft.getInstance().timer.renderPartialTicks;
        if (!var1.isAlive()) {
            var4 = 0.0;
        }

        GL11.glTranslated(
                var1.lastTickPosX + (var1.getPosX() - var1.lastTickPosX) * var4,
                var1.lastTickPosY + (var1.getPosY() - var1.lastTickPosY) * var4,
                var1.lastTickPosZ + (var1.getPosZ() - var1.lastTickPosZ) * var4
        );
        GL11.glTranslated(
                -mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                -mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                -mc.gameRenderer.getActiveRenderInfo().getPos().getZ()
        );
        GL11.glEnable(32823);
        GL11.glEnable(3008);
        GL11.glEnable(3042);
        GL11.glAlphaFunc(519, 0.0F);
        short var6 = 1800;
        float var7 = (float) (System.currentTimeMillis() % (long) var6) / (float) var6;
        boolean var8 = var7 > 0.5F;
        var7 = !var8 ? var7 * 2.0F : 1.0F - var7 * 2.0F % 1.0F;
        GL11.glTranslatef(0.0F, (var1.getHeight() + 0.4F) * var7, 0.0F);
        float var9 = (float) Math.sin((double) var7 * Math.PI);
        this.esppp(var8, 0.45F * var9, 0.6F, 0.35F * var9, killauraMod.entityAnimation.get(var1).getDuration());
        GL11.glPushMatrix();
        GL11.glTranslated(
                mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                mc.gameRenderer.getActiveRenderInfo().getPos().getZ()
        );
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(32925);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public void esppp(boolean var1, float var2, float var3, float var4, float var5) {
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(32823);
        GL11.glDisable(2929);
        GL11.glBegin(5);
        int var8 = (int) (360.0F / (40.0F * var3));
        Color var9 = new Color(killauraMod.parseSettingValueToIntBySettingName("ESP Color"));
        float var10 = (float) var9.getRed() / 255.0F;
        float var11 = (float) var9.getGreen() / 255.0F;
        float var12 = (float) var9.getBlue() / 255.0F;

        for (int var13 = 0; var13 <= 360 + var8; var13 += var8) {
            int var14 = var13;
            if (var13 > 360) {
                var14 = 0;
            }

            double var15 = Math.sin((double) var14 * Math.PI / 180.0) * (double) var3;
            double var17 = Math.cos((double) var14 * Math.PI / 180.0) * (double) var3;
            GL11.glColor4f(var10, var11, var12, !var1 ? 0.0F : var4 * var5);
            GL11.glVertex3d(var15, 0.0, var17);
            GL11.glColor4f(var10, var11, var12, var1 ? 0.0F : var4 * var5);
            GL11.glVertex3d(var15, var2, var17);
        }

        GL11.glEnd();
        GL11.glLineWidth(2.2F);
        GL11.glBegin(3);

        for (int var19 = 0; var19 <= 360 + var8; var19 += var8) {
            int var20 = var19;
            if (var19 > 360) {
                var20 = 0;
            }

            double var21 = Math.sin((double) var20 * Math.PI / 180.0) * (double) var3;
            double var22 = Math.cos((double) var20 * Math.PI / 180.0) * (double) var3;
            GL11.glColor4f(var10, var11, var12, (0.5F + 0.5F * var4) * var5);
            GL11.glVertex3d(var21, !var1 ? (double) var2 : 0.0, var22);
        }

        GL11.glEnd();
        GL11.glEnable(2929);
        RenderSystem.shadeModel(GL11.GL_FLAT);
    }
}
