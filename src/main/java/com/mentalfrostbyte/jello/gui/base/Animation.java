package com.mentalfrostbyte.jello.gui.base;

import java.util.Date;

public class Animation {
    public int duration;
    public int reverseDuration;
    public Direction direction = Direction.FORWARDS;
    public Date startTime;
    public Date reverseStartTime;

    public Animation(int duration, int reverseDuration) {
        this(duration, reverseDuration, Direction.FORWARDS);
    }

    public Animation(int duration, int reverseDuration, Direction direction) {
        this.duration = duration;
        this.reverseDuration = reverseDuration;
        this.startTime = new Date();
        this.reverseStartTime = new Date();
        this.changeDirection(direction);
    }
    /**
     * Calculates the progress of an animation with consideration for reverse timing.
     *
     * @param startForward The start time of the forward animation. If null, the current time is used.
     * @param startReverse The start time of the reverse animation. If null, the current time is used.
     * @param maxForward The maximum duration of the forward animation in milliseconds.
     * @param maxReverse The maximum duration of the reverse animation in milliseconds.
     * @return A float value representing the progress of the animation, ranging from 0.0 to 1.0.
     */
    public static float calculateProgressWithReverse(Date startForward, Date startReverse, float maxForward, float maxReverse) {
        float var6 = Math.min(maxForward, (float) (new Date().getTime() - (startForward != null ? startForward.getTime() : new Date().getTime())))
                / maxForward
                * (1.0F - Math.min(maxReverse, (float) (new Date().getTime() - (startReverse != null ? startReverse.getTime() : new Date().getTime()))) / maxReverse);
        return Math.max(0.0F, Math.min(1.0F, var6));
    }

    public static float calculateProgress(Date date, float max) {
        float var4 = Math.min(max, (float) (new Date().getTime() - (date != null ? date.getTime() : new Date().getTime()))) / max;
        return Math.max(0.0F, Math.min(1.0F, var4));
    }

    public static float calculateProgressWithReverse(Date startForward, Date startReverse, float max) {
        return calculateProgressWithReverse(startForward, startReverse, max, max);
    }

    public static boolean hasTimeElapsed(Date time, float elapsed) {
        return time != null && (float) (new Date().getTime() - time.getTime()) > elapsed;
    }

    public int getDuration() {
        return this.duration;
    }

    public void changeDirection(Direction newDirection) {
        if (this.direction != newDirection) {
            switch (direction) {
                case FORWARDS:
                    long var4 = (long) (this.calcPercent() * (float) this.duration);
                    this.startTime = new Date(new Date().getTime() - var4);
                    break;
                case BACKWARDS:
                    long var6 = (long) ((1.0F - this.calcPercent()) * (float) this.reverseDuration);
                    this.reverseStartTime = new Date(new Date().getTime() - var6);
            }

            this.direction = newDirection;
        }
    }

    /**
     * Updates the start time of the animation based on the given progress value.
     *
     * @param progress The progress value, ranging from 0.0 to 1.0, representing the current state of the animation.
     * @see #getDirection()
     * @see #getDuration()
     */
    public void updateStartTime(float progress) {
        switch (direction) {
            case FORWARDS:
                long timeElapsed = (long) (progress * (float) this.duration);
                this.startTime = new Date(new Date().getTime() - timeElapsed);
                break;
            case BACKWARDS:
                long timeRemaining = (long) ((1.0F - progress) * (float) this.reverseDuration);
                this.reverseStartTime = new Date(new Date().getTime() - timeRemaining);
        }
    }

    public Direction getDirection() {
        return this.direction;
    }

    public float calcPercent() {
        return this.direction != Direction.FORWARDS
                ? 1.0F - (float) Math.min(this.reverseDuration, new Date().getTime() - this.reverseStartTime.getTime()) / (float) this.reverseDuration
                : (float) Math.min(this.duration, new Date().getTime() - this.startTime.getTime()) / (float) this.duration;
    }
}
