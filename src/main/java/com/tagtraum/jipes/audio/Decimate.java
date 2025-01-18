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
 * Applies an appropriate low pass filter before dropping samples.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see MultirateFilters.Decimator
 * @see Interpolate
 */
public class Decimate extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private RealAudioBuffer realAudioBuffer;
    private MultirateFilters.Decimator decimator;
    private float targetSampleRate;

    /**
     * Keeps every nth frame, drops all other frames.
     *
     * @param factor nth frame to keep
     */
    public Decimate(final int factor) {
        setFactor(factor);
    }

    /**
     * Creates a decimate processor that drops/keeps every second frame.
     */
    public Decimate() {
        setFactor(2);
    }

    /**
     * Creates a decimate processor that attempts to decimate {@link AudioBuffer}s
     * to the specified target sample rate.
     * Note that not all sample rates are supported. The ratio between sample rates
     * should be an integer.
     * If the target sample rate is not supported, an {@link IOException} is thrown
     * when processing the first buffer.
     *
     * @param targetSampleRate target sample rate
     */
    public Decimate(final float targetSampleRate) {
        setTargetSampleRate(targetSampleRate);
    }

    /**
     * @see #setFactor(int)
     * @return frame to keep or 0, if only a target sample rate was set
     */
    public int getFactor() {
        if (decimator == null) return 0;
        return decimator.getFactor();
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
        this.decimator = null;
    }

    /**
     * Every <code>factor</code>th frame will be kept.
     * So if you specify 2, every 2nd frame will be kept.
     * If you specify 3, every third frame will be kept - leading to a sample rate reduction of,
     * e.g. 44100 to 44100/3 = 14700.
     *
     * @param factor frame to keep
     * @throws IllegalArgumentException not all factors are supported
     * @see com.tagtraum.jipes.math.MultirateFilters.Decimator#Decimator(int)
     */
    public void setFactor(final int factor) {
        if (factor < 1) throw new IllegalArgumentException("Decimation factor must be greater than 0: " + factor);
        this.decimator = new MultirateFilters.Decimator(factor);
    }

    private AudioFormat getProcessedAudioFormat(final AudioBuffer buffer) {
        final AudioFormat sourceAudioFormat = buffer.getAudioFormat();
        return new AudioFormat(sourceAudioFormat.getSampleRate()/decimator.getFactor(),
                sourceAudioFormat.getSampleSizeInBits(),
                sourceAudioFormat.getChannels(),
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) throws IOException {
        if (decimator == null) {
            final float sourceSampleRate = buffer.getAudioFormat().getSampleRate();
            final float floatFactor = sourceSampleRate / targetSampleRate;
            final int intFactor = Math.round(floatFactor);
            if (Math.abs(floatFactor - intFactor) > 0.001f) throw new IOException("Decimation factor " + floatFactor + " from " + sourceSampleRate + "Hz to " + targetSampleRate + "Hz is not supported");
            try {
                setFactor(intFactor);
            } catch (IllegalArgumentException e) {
                throw new IOException("Decimation factor " + intFactor + " is not supported", e);
            }
        }
        if (buffer.getAudioFormat().getChannels() != 1) throw new IOException("Decimate only supports single channel buffers. Actual audio format: " + buffer.getAudioFormat());
        final float[] floats = buffer.getData();
        final float[] decimated = decimator.map(floats);
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer(buffer.getFrameNumber() / getFactor(), decimated, getProcessedAudioFormat(buffer));
        } else {
            realAudioBuffer.reuse(buffer.getFrameNumber() / getFactor(), decimated, realAudioBuffer.getAudioFormat());
        }
        return realAudioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Decimate decimate = (Decimate) o;

        if (targetSampleRate != 0f) {
            if (Float.compare(decimate.targetSampleRate, targetSampleRate) != 0) return false;
        } else {
            if (decimator != null ? !decimator.equals(decimate.decimator) : decimate.decimator != null) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (targetSampleRate != 0f) {
            return (targetSampleRate != +0.0f ? Float.floatToIntBits(targetSampleRate) : 0);
        } else {
            return decimator != null ? decimator.hashCode() : 0;
        }
    }

    @Override
    public String toString() {
        if (decimator != null)
            return "Decimate{" +
                "factor=" + decimator.getFactor() +
                '}';
        else
            return "Decimate{" +
                    "target=" + targetSampleRate +
                    "Hz}";
    }
}