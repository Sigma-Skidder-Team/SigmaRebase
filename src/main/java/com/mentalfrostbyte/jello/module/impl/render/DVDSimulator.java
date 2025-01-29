package com.mentalfrostbyte.jello.module.impl.render;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.gui.unmapped.Dimension;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.util.math.vector.Vector2f;
import team.sdhq.eventBus.annotations.EventTarget;

import java.awt.Color;
import java.io.IOException;

public class DVDSimulator extends Module {
    public Vector2f dvdPosition = new Vector2f(1.0F, 1.0F);
    public float dvdX;
    public float dvdY = 0.0F;
    public float xDirection = 1.0F;
    public float yDirection = 1.0F;
    public Dimension dvdDimensions = new Dimension(201, 90);
    public int dvdColor = 0;

    public DVDSimulator() {
        super(ModuleCategory.RENDER, "DVD Simulator", "wtf");
        this.changeColor();
    }

    @Override
    public void onEnable() {
        this.dvdX = (float) ((double) (mc.getMainWindow().getWidth() - this.dvdDimensions.width) * Math.random());
        this.dvdY = (float) ((double) (mc.getMainWindow().getHeight() - this.dvdDimensions.height) * Math.random());
        this.changeColor();
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) throws IOException {
        if (this.isEnabled() && mc.player != null && mc.world != null) {
            int windowHeight = mc.getMainWindow().getHeight();
            int windowWidth = mc.getMainWindow().getWidth();
            float speed = 2;

            if (!(this.dvdY <= speed)) {
                if (this.dvdY + (float) this.dvdDimensions.height > (float) windowHeight) {
                    this.yDirection = -1.0F;
                    this.changeColor();
                }
            } else {
                this.yDirection = 1.0F;
                this.changeColor();
            }

            if (!(this.dvdX <= speed)) {
                if (this.dvdX + (float) this.dvdDimensions.width > (float) windowWidth) {
                    this.xDirection = -1.0F;
                    this.changeColor();
                }
            } else {
                this.xDirection = 1.0F;
                this.changeColor();
            }

            this.dvdX += this.xDirection * speed;
            this.dvdY += this.yDirection * speed;

            RenderUtil.drawImage(
                    this.dvdX,
                    this.dvdY,
                    (float) this.dvdDimensions.width,
                    (float) this.dvdDimensions.height,
                    Resources.dvdPNG,
                    MultiUtilities.applyAlpha(this.dvdColor, 0.8F)
            );
        }
    }

    public void changeColor() {
        this.dvdColor = Color.getHSBColor((float) Math.random(), 0.6F, 1.0F).getRGB();
    }
}
