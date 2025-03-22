package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.clicktp.BasicClickTP;
import com.mentalfrostbyte.jello.module.impl.movement.clicktp.MinibloxClickTP;
import com.mentalfrostbyte.jello.module.impl.movement.clicktp.SpartanClickTP;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

public class ClickTP extends ModuleWithModuleSettings {
    private final NumberSetting<Float> maxRange;

    public ClickTP() {
        super(ModuleCategory.MOVEMENT, "ClickTP", "TP's you when you click", new BasicClickTP(), new MinibloxClickTP(), new SpartanClickTP());
        this.registerSetting(new BooleanSetting("Sneak", "Allows teleport only when sneaking", true));
        this.registerSetting(new BooleanSetting("Auto Disable", "Disable ClickTP after teleporting", true));
        this.registerSetting(this.maxRange = new NumberSetting<>("Maximum range", "Maximum range of the teleport", 100.0F, Float.class, 10.0F, 300.0F, 1.0F));
    }

    public BlockPos getRotationHit() {
        BlockRayTraceResult trace = BlockUtil.rayTrace(
                mc.player.rotationYaw, mc.player.rotationPitch,
                this.maxRange.currentValue);

		return trace.getPos();
    }

    @EventTarget
    public void method16752(EventRender3D var1) {
        if (this.isEnabled() && (mc.player.isSneaking() || !this.getBooleanValueFromSettingName("Sneak"))) {
            BlockRayTraceResult var4 = BlockUtil.rayTrace(mc.player.rotationYaw, mc.player.rotationPitch, this.getNumberValueBySettingName("Maximum range"));
            BlockPos var5 = null;
            if (var4 != null) {
                var5 = var4.getPos();
            }

            this.method16754(
                    this.method16753(),
                    (double) var5.getX() + 0.5 - Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPos().getX(),
                    (double) (var5.getY() + 1) - Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPos().getY(),
                    (double) var5.getZ() + 0.5 - Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPos().getZ()
            );
        }
    }

    private double method16753() {
        return MathHelper.sin((float) Math.toRadians(90.0F - mc.player.rotationPitch)) * 10.0F;
    }

    private void method16754(double var1, double var3, double var5, double var7) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        GL11.glTranslated(var3, var5, var7);
        GL11.glRotatef((float) (mc.player.ticksExisted % 90 * 4), 0.0F, 1.0F, 0.0F);
        this.method16756();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    private void method16755(float var1) {
        GL11.glBegin(6);

        for (int var4 = 0; var4 < 360; var4++) {
            double var5 = (double) var4 * Math.PI / 180.0;
            GL11.glVertex2d(Math.cos(var5) * (double) var1, Math.sin(var5) * (double) var1);
        }

        GL11.glEnd();
    }

    private void method16756() {
        for (int var3 = 0; var3 <= 270; var3 += 90) {
            GL11.glPushMatrix();
            GL11.glRotatef(var3, 0.0F, 1.0F, 0.0F);
            this.method16757(77.0F, (float) (177 + var3 / 10), (float) (206 + var3 / 10));
            GL11.glPopMatrix();
        }

        for (int var4 = 0; var4 <= 270; var4 += 90) {
            GL11.glPushMatrix();
            GL11.glRotatef(var4, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            this.method16757(77.0F, (float) (177 + var4 / 10), (float) (206 + var4 / 10));
            GL11.glPopMatrix();
        }
    }

    private void method16757(float var1, float var2, float var3) {
        GL11.glColor3f(var1 / 255.0F, var2 / 255.0F, var3 / 255.0F);
        rotationThingy();
    }

    public static void rotationThingy() {
        GL11.glTranslatef(0.0F, 0.0F, 0.3F);
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        GL11.glRotated(-37.0, 1.0, 0.0, 0.0);
        GL11.glBegin(6);
        GL11.glVertex2f(0.0F, 0.4985F);
        GL11.glVertex2f(-0.3F, 0.0F);
        GL11.glVertex2f(0.3F, 0.0F);
        GL11.glEnd();
    }
}