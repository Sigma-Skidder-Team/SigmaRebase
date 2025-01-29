package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
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
    public long lastRenderTime = 0L;
    private final List<MusicParticle> particles = new ArrayList<>();

    public MusicParticles() {
        super(ModuleCategory.GUI, "MusicParticles", "Shows nice particles when music is playing");
        this.setAvailableOnClassic(false);
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (this.isEnabled() && mc.player != null) {
            if (Client.getInstance().musicManager.isPlayingSong() && !Client.getInstance().musicManager.visualizerData.isEmpty()) {
                long timeElapsed = System.nanoTime() - this.lastRenderTime;
                float particleIntensity = Math.min(10.0F, Math.max(0.0F, (float) timeElapsed / 1.810361E7F));
                double maxAmplitude = 0.0;
                double maxThreshold = 4750;
                if (Client.getInstance().musicManager.amplitudes.isEmpty()) {
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    maxAmplitude = Math.max(maxAmplitude, Math.sqrt(Client.getInstance().musicManager.amplitudes.get(i)) - 1000.0);
                }

                float particleCount = 0.7F + (float) (maxAmplitude / (double) (maxThreshold - 1000)) * 8.14F;
                particleCount *= particleIntensity;
                int particleIndex = 0;

                while (this.particles.size() < 40) {
                    this.addParticle();
                    if ((float) (particleIndex++) > particleCount) {
                        break;
                    }
                }

                this.updateParticles(particleCount);

                for (MusicParticle particle : this.particles) {
                    particle.render();
                }
            }

            this.lastRenderTime = System.nanoTime();
        }
    }

    @Override
    public void onEnable() {
        this.lastRenderTime = System.nanoTime();
    }

    private void addParticle() {
        this.particles.add(new MusicParticle());
    }

    private void updateParticles(float var1) {
        Iterator<MusicParticle> iterator = this.particles.iterator();

        while (iterator.hasNext()) {
            MusicParticle particle = iterator.next();
            particle.update(var1);
            if (particle.isExpired()) {
                iterator.remove();
            }
        }
    }

    public static class MusicParticle {
        public final float speedFactor = (float)(0.1F + Math.random() * 0.9F);
        public final float sizeFactor = (float)(0.5 + Math.random() * 0.5);
        public final int startX = (int)((double) mc.getMainWindow().getWidth() * Math.random());
        public final int startY = (int)((double) mc.getMainWindow().getHeight() * Math.random());
        public float lifeProgress;

        public void update(float var1) {
            this.lifeProgress = this.lifeProgress + 0.02F * var1 * this.speedFactor;
        }

        public void render() {
            float scale = 0.3F + this.lifeProgress * 0.7F;
            float alpha = 1.0F;
            if (!(this.lifeProgress < 0.1F)) {
                if (this.lifeProgress > 0.75F) {
                    alpha = 1.0F - (this.lifeProgress - 0.75F) / 0.25F;
                }
            } else {
                alpha = this.lifeProgress / 0.1F;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef((float)(mc.getMainWindow().getWidth() / 2), (float)(mc.getMainWindow().getHeight() / 2), 0.0F);
            GL11.glScalef(scale, scale, 1.0F);
            GL11.glTranslatef((float)(-mc.getMainWindow().getWidth() / 2), (float)(-mc.getMainWindow().getHeight() / 2), 0.0F);
            int color = Color.getHSBColor((float)(System.currentTimeMillis() % 4000L) / 4000.0F, 0.3F, 1.0F).getRGB();
            float size = 60.0F * this.sizeFactor;
            RenderUtil.drawImage((float)this.startX - size / 2.0F, (float)this.startY - size / 2.0F, size, size, Resources.particlePNG, ColorUtils.applyAlpha(color, alpha * 0.9F));
            GL11.glPopMatrix();
        }

        public boolean isExpired() {
            return this.lifeProgress >= 1.0F;
        }
    }
}
