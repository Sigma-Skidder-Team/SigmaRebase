package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.EventRender;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MusicParticles extends Module {
    public long field23676 = 0L;
    private final List<Class5968> field23677 = new ArrayList<>();

    public MusicParticles() {
        super(ModuleCategory.GUI, "MusicParticles", "Shows nice particles when music is playing");
        this.setAvailableOnClassic(false);
    }

    @EventTarget
    public void onRender(EventRender event) {
        if (this.isEnabled() && mc.player != null) {
            if (Client.getInstance().musicManager.method24319() && !Client.getInstance().musicManager.field32163.isEmpty()) {
                long var4 = System.nanoTime() - this.field23676;
                float var6 = Math.min(10.0F, Math.max(0.0F, (float) var4 / 1.810361E7F));
                double var7 = 0.0;
                double var9 = 4750;
                if (Client.getInstance().musicManager.field32165.isEmpty()) {
                    return;
                }

                for (int var10 = 0; var10 < 3; var10++) {
                    var7 = Math.max(var7, Math.sqrt(Client.getInstance().musicManager.field32165.get(var10)) - 1000.0);
                }

                float var14 = 0.7F + (float) (var7 / (double) (var9 - 1000)) * 8.14F;
                var14 *= var6;
                int var11 = 0;

                while (this.field23677.size() < 40) {
                    this.method16464();
                    if ((float) (var11++) > var14) {
                        break;
                    }
                }

                this.method16465(var14);

                for (Class5968 var13 : this.field23677) {
                    var13.method18498();
                }
            }

            this.field23676 = System.nanoTime();
        }
    }

    @Override
    public void onEnable() {
        this.field23676 = System.nanoTime();
    }

    private void method16464() {
        this.field23677.add(new Class5968());
    }

    private void method16465(float var1) {
        Iterator<Class5968> var4 = this.field23677.iterator();

        while (var4.hasNext()) {
            Class5968 var5 = var4.next();
            var5.method18497(var1);
            if (var5.method18499()) {
                var4.remove();
            }
        }
    }

    public static class Class5968 {
        public final float field26020 = (float)(0.1F + Math.random() * 0.9F);
        public final float field26021 = (float)(0.5 + Math.random() * 0.5);
        public final int field26022 = (int)((double) mc.getMainWindow().getWidth() * Math.random());
        public final int field26023 = (int)((double) mc.getMainWindow().getHeight() * Math.random());
        public float field26024;

        public void method18497(float var1) {
            this.field26024 = this.field26024 + 0.02F * var1 * this.field26020;
        }

        public void method18498() {
            float var3 = 0.3F + this.field26024 * 0.7F;
            float var4 = 1.0F;
            if (!(this.field26024 < 0.1F)) {
                if (this.field26024 > 0.75F) {
                    var4 = 1.0F - (this.field26024 - 0.75F) / 0.25F;
                }
            } else {
                var4 = this.field26024 / 0.1F;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef((float)(mc.getMainWindow().getWidth() / 2), (float)(mc.getMainWindow().getHeight() / 2), 0.0F);
            GL11.glScalef(var3, var3, 1.0F);
            GL11.glTranslatef((float)(-mc.getMainWindow().getWidth() / 2), (float)(-mc.getMainWindow().getHeight() / 2), 0.0F);
            int var5 = Color.getHSBColor((float)(System.currentTimeMillis() % 4000L) / 4000.0F, 0.3F, 1.0F).getRGB();
            float var6 = 60.0F * this.field26021;
            RenderUtil.drawImage((float)this.field26022 - var6 / 2.0F, (float)this.field26023 - var6 / 2.0F, var6, var6, Resources.particlePNG, ColorUtils.applyAlpha(var5, var4 * 0.9F));
            GL11.glPopMatrix();
        }

        public boolean method18499() {
            return this.field26024 >= 1.0F;
        }
    }
}
