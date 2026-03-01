package com.mentalfrostbyte.jello.gui.base.elements.impl.image.types;

import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.AnimationMode;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import org.newdawn.slick.opengl.Texture;

public class AnimatedImage {
    private final Texture texture;
    private int currentFrameIndex;
    private final int totalFrameCount;
    private final int frameCount;
    private final int individualImageHeight;
    private int ticksPerFrame;
    private final int individualImageWidth;
    private boolean playing;
    private boolean looping = true;
    private FrameDirection frameDirection;
    private final AnimationMode animationMode;
    private final TimerUtil timer = new TimerUtil();
    private int index;
    private int frameStep = 1;
    private final Animation animation;

    public AnimatedImage(Texture texture, int var2, int var3, int var4, AnimationMode var5, int var6, int var7) {
        this.texture = texture;
        this.totalFrameCount = var2;
        this.frameCount = var3;
        this.individualImageHeight = var4;
        this.animationMode = var5;
        this.ticksPerFrame = var6;
        this.frameStep = var7;
        this.animation = new Animation(var6, var6, Animation.Direction.BACKWARDS);
        this.individualImageWidth = (int) ((float) texture.getTextureWidth() / (float) var3);
        this.frameDirection = FrameDirection.FORWARDS;
    }

    public void start() {
        this.timer.start();
        this.playing = true;
        if (this.animation.getDirection() != Animation.Direction.BACKWARDS) {
            this.animation.changeDirection(Animation.Direction.BACKWARDS);
        } else {
            this.animation.changeDirection(Animation.Direction.FORWARDS);
        }
    }

    public boolean isForwards() {
        return this.animation.getDirection() != Animation.Direction.BACKWARDS;
    }

    public void stop() {
        this.timer.stop();
        this.playing = false;
    }

    public void reset() {
        this.timer.reset();
    }

    public void tick() {
        long var3 = this.timer.getElapsedTime();
        if (this.playing) {
            this.index++;
        }

        int var5 = 0;
        switch (this.animationMode.ordinal()) {
            case 1:
                this.currentFrameIndex = Math.round((float) (this.totalFrameCount - 1) * this.animation.calcPercent());
                break;
            case 2:
                if (this.index >= this.ticksPerFrame) {
                    var5 = this.frameStep;
                    this.index = 0;
                }

                if (this.frameDirection == FrameDirection.FORWARDS) {
                    this.currentFrameIndex += var5;
                    if (!this.looping && this.currentFrameIndex >= this.totalFrameCount - 1) {
                        this.currentFrameIndex = this.totalFrameCount - 1;
                        this.stop();
                        this.reset();
                    } else if (this.currentFrameIndex > this.totalFrameCount) {
                    }

                    this.currentFrameIndex = this.currentFrameIndex % this.totalFrameCount;
                } else {
                    this.currentFrameIndex -= var5;
                    if (!this.looping && this.currentFrameIndex <= 0) {
                        this.currentFrameIndex = 0;
                        this.stop();
                        this.reset();
                    } else if (this.currentFrameIndex < 0) {
                        this.currentFrameIndex = this.totalFrameCount - 1;
                    }
                }
        }
    }

    public void drawImage(int x, int y, int width, int height, float var5) {
        int var8 = this.currentFrameIndex % this.individualImageWidth * this.frameCount;
        int var9 = this.currentFrameIndex / this.individualImageWidth * this.individualImageHeight;
        RenderUtil.drawImage(
                (float) x,
                (float) y,
                (float) width,
                (float) height,
                this.texture,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5),
                (float) var8,
                (float) var9,
                (float) this.frameCount,
                (float) this.individualImageHeight
        );
    }

    public Texture getTexture() {
        return this.texture;
    }

    public int getCurrentFrameIndex() {
        return this.currentFrameIndex;
    }

    public void validateFrameIndex(int var1) {
        if (var1 < 0 || var1 > this.getTotalFrameCount() - 1) {
            throw new IllegalArgumentException("Invalid frame count");
        }
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public int getIndividualImageHeight() {
        return this.individualImageHeight;
    }

    public int getTotalFrameCount() {
        return this.totalFrameCount;
    }

    public int getTicksPerFrame() {
        return this.ticksPerFrame;
    }

    public void setTicksPerFrame(int var1) {
        this.ticksPerFrame = var1;
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public boolean isLooping() {
        return this.looping;
    }

    public void setLooping(boolean var1) {
        this.looping = var1;
    }

    public FrameDirection getFrameDirection() {
        return this.frameDirection;
    }

    public void setFrameDirection(FrameDirection var1) {
        this.frameDirection = var1;
    }

    public int getFrameStep() {
        return this.frameStep;
    }

    public void setFrameStep(int var1) {
        this.frameStep = var1;
    }

    public enum FrameDirection {
        FORWARDS,
        BACKWARDS
	}
}
