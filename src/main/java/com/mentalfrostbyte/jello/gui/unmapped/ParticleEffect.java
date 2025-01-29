package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;

import java.awt.Color;

public class ParticleEffect {
    private float xPosition;
    public float initialXPosition;
    public float yPosition;
    public float size;
    private float movementSpeed;
    private final RandomIntGenerator random = new RandomIntGenerator();
    public float direction;
    public Color color = new Color(1.0F, 1.0F, 1.0F, 0.5F);

    public ParticleEffect(float x, float y) {
        this.initialXPosition = this.xPosition = x;
        this.yPosition = y;
        this.size = (float) this.random.nextInt(1, 3) + this.random.nextFloat();
        this.initialize();
    }

    private void initialize() {
        float maxMovement = 1.0F;
        this.movementSpeed = this.random.nextFloat() % maxMovement;
        this.direction = this.random.nextFloat() / 2.0F;
        if (this.random.nextBoolean()) {
            this.direction *= -1.0F;
        }
    }

    public void render(float partialTicks) {
        RenderUtil.drawCircle(
                this.initialXPosition * 2.0F, this.yPosition * 2.0F, this.size * 2.0F, ColorUtils.applyAlpha(this.color.getRGB(), partialTicks * 0.7F)
        );
    }

    public void updatePosition(AnimationManager animationManager) {
        this.initialXPosition = this.initialXPosition + animationManager.getCurrentValue() + this.direction;
        this.xPosition = this.xPosition + animationManager.getCurrentValue() + this.direction;
        this.yPosition = this.yPosition + this.movementSpeed;
    }
}
