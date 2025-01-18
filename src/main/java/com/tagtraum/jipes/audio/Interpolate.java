/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.math.MultirateFilters;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

/**
 * Adds zero samples followed by an appropriate low pass filter.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see MultirateFilters.Interpolator
 * @see Decimate
 */
public class Interpolate extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private RealAudioBuffer realAudioBuffer;
    private MultirateFilters.Interpolator interpolator;
    private float targetSampleRate;

    /**
     * Increases the sample rate by a given factor.
     *
     * @param factor sample rate increase factor
     */
    public Interpolate(final int factor) {
        setFactor(factor);
    }

    /**
     * Doubles the sample rate.
     */
    public Interpolate() {
        setFactor(2);
    }

    /**
     * Creates a interpolate processor that attempts to interpolate {@link AudioBuffer}s
     * to the specified target sample rate.
     * Note that not all sample rates are supported. The ratio between sample rates
     * should be an integer.
     * If the target sample rate is not supported, an {@link IOException} is thrown
     * when processing the first buffer.
     *
     * @param targetSampleRate target sample rate
     */
    public Interpolate(final float targetSampleRate) {
        setTargetSampleRate(targetSampleRate);
    }

    /**
     * @see #setFactor(int)
     * @return sample rate factor or 0, if only a target sample rate was specified
     */
    public int getFactor() {
        if (interpolator == null) return 0;
        return interpolator.getFactor();
    }

    /**
     * @return target sample rate or 0, if only a factor was set
     */
    public float getTargetSampleRate() {
        return targetSampleRate;
    }

    /**
     * @param targetSampleRate target sample rate
     */
    public void setTargetSampleRate(final float targetSampleRate) {
        if (targetSampleRate <= 0f) throw new IllegalArgumentException("Target sample rate must be greater than 0Hz: " + targetSampleRate);
        this.targetSampleRate = targetSampleRate;
        this.interpolator = null;
    }

    /**
     * Increases the sample rate by the given factor.
     *
     * @param factor sample rate factor
     * @throws IllegalArgumentException not all factors are supported
     * @see com.tagtraum.jipes.math.MultirateFilters.Interpolator#Interpolator(int)
     */
    public void setFactor(final int factor) {
        if (factor < 1) throw new IllegalArgumentException("Interpolation factor must be greater than 0: " + factor);
        this.interpolator = new MultirateFilters.Interpolator(factor);
    }

    private AudioFormat getProcessedAudioFormat(final AudioBuffer buffer) {
        final AudioFormat sourceAudioFormat = buffer.getAudioFormat();
        return new AudioFormat(sourceAudioFormat.getSampleRate() * interpolator.getFactor(),
                sourceAudioFormat.getSampleSizeInBits(),
                sourceAudioFormat.getChannels(),
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) throws IOException {
        if (interpolator == null) {
            try {
                verifyTargetSampleRate(buffer.getAudioFormat().getSampleRate());
            } catch (IllegalArgumentException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        if (buffer.getAudioFormat().getChannels() != 1) throw new IOException("Interpolate only supports single channel buffers. Actual audio format: " + buffer.getAudioFormat());
        final float[] floats = buffer.getData();
        final float[] interpolated = interpolator.map(floats);
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer(buffer.getFrameNumber() * getFactor(), interpolated, getProcessedAudioFormat(buffer));
        } else {
            realAudioBuffer.reuse(buffer.getFrameNumber() * getFactor(), interpolated, realAudioBuffer.getAudioFormat());
        }
        return realAudioBuffer;
    }

    private void verifyTargetSampleRate(final float sourceSampleRate) throws IllegalArgumentException {
        final float floatFactor = targetSampleRate / sourceSampleRate;
        final int intFactor = Math.round(floatFactor);
        if (Math.abs(floatFactor - intFactor) > 0.001f) throw new IllegalArgumentException("Interpolation factor " + floatFactor + " from " + sourceSampleRate + "Hz to " + targetSampleRate + "Hz is not supported");
        setFactor(intFactor);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Interpolate interpolate = (Interpolate) o;

        if (targetSampleRate != 0f) {
            if (Float.compare(interpolate.targetSampleRate, targetSampleRate) != 0) return false;
        } else {
            if (interpolator != null
                    ? !interpolator.equals(interpolate.interpolator)
                    : interpolate.getFactor() != this.getFactor()) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (targetSampleRate != 0f) {
            return (targetSampleRate != +0.0f ? Float.floatToIntBits(targetSampleRate) : 0);
        } else {
            return interpolator != null ? interpolator.hashCode() : 0;
        }
    }

    @Override
    public String toString() {
        if (interpolator != null)
            return "Interpolate{" +
                    "factor=" + interpolator.getFactor() +
                    '}';
        else
            return "Interpolate{" +
                    "target=" + targetSampleRate +
                    "Hz}";
    }
}
