package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.unmapped.RandomIntGenerator;
import com.mentalfrostbyte.jello.gui.unmapped.AnimatedIconPanelWrap;
import com.mentalfrostbyte.jello.gui.unmapped.ParticleEffect;
import com.mentalfrostbyte.jello.gui.unmapped.AnimationManager;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class BrainFreezeGui extends AnimatedIconPanelWrap {
    private final List<ParticleEffect> particles = new ArrayList<>();
    private final AnimationManager animationManager = new AnimationManager();
    public RandomIntGenerator random = new RandomIntGenerator();

    public BrainFreezeGui(CustomGuiScreen parentScreen, String name) {
        super(parentScreen, name, 0, 0, Minecraft.getInstance().getMainWindow().getWidth(), Minecraft.getInstance().getMainWindow().getHeight(), false);
        this.method13145(false);
        this.method13296(false);
        this.method13292(false);
        this.method13294(true);
        this.setListening(false);
    }

    @Override
    public void method13145(boolean var1) {
        super.method13145(false);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float partialTicks) {
        int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
        int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
        int halfScreenWidth = scaledWidth / 2;

        boolean shouldUpdate;
        for (shouldUpdate = false; this.particles.size() < halfScreenWidth; shouldUpdate = true) {
            this.particles.add(new ParticleEffect((float) this.random.nextInt(scaledWidth), (float) this.random.nextInt(scaledHeight)));
        }

        while (this.particles.size() > halfScreenWidth) {
            this.particles.remove(0);
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            for (ParticleEffect particle : this.particles) {
                particle.initialXPosition = (float) this.random.nextInt(scaledWidth);
                particle.yPosition = (float) this.random.nextInt(scaledHeight);
            }
        }

        this.animationManager.update();

        for (ParticleEffect particle : this.particles) {
            particle.updatePosition(this.animationManager);
            if (!(particle.initialXPosition < 0.0F)) {
                if (particle.initialXPosition > (float) scaledWidth) {
                    particle.initialXPosition = 0.0F;
                }
            } else {
                particle.initialXPosition = (float) scaledWidth;
            }

            if (!(particle.yPosition < 0.0F)) {
                if (particle.yPosition > (float) scaledHeight) {
                    particle.yPosition = 0.0F;
                }
            } else {
                particle.yPosition = (float) scaledHeight;
            }

            particle.render(partialTicks);
        }

        super.draw(partialTicks);
    }
}
