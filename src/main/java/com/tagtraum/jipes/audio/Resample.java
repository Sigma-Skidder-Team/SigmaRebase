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
 * Resamples mono audio data.
 * Note that not all conversions are supported.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see MultirateFilters.Resampler
 */
public class Resample extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private RealAudioBuffer realAudioBuffer;
    private MultirateFilters.Resampler resampler;

    /**
     *
     * @param upFactor upsample factor
     * @param downFactor downsample factor
     * @throws IllegalArgumentException if the resample factor is not supported
     */
    public Resample(final int upFactor, final int downFactor) {
        setFactor(upFactor, downFactor);
    }

    /**
     *
     * @param upFactor up factor
     * @param downFactor down factor
     * @throws IllegalArgumentException if the resample factor is not supported
     */
    public void setFactor(final int upFactor, final int downFactor) throws IllegalArgumentException {
        if (upFactor < 1) throw new IllegalArgumentException("Up factor must be greater than 0: " + upFactor);
        if (downFactor < 1) throw new IllegalArgumentException("Down factor must be greater than 0: " + downFactor);
        this.resampler = new MultirateFilters.Resampler(upFactor, downFactor);
    }

    public float getFactor() {
        return resampler.getFactor();
    }

    private AudioFormat getProcessedAudioFormat(final AudioBuffer buffer) {
        final AudioFormat sourceAudioFormat = buffer.getAudioFormat();
        return new AudioFormat(sourceAudioFormat.getSampleRate() * resampler.getFactor(),
                sourceAudioFormat.getSampleSizeInBits(),
                sourceAudioFormat.getChannels(),
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) throws IOException {
        if (buffer.getAudioFormat().getChannels() != 1) throw new IOException("Resample only supports single channel buffers. Actual audio format: " + buffer.getAudioFormat());
        final float[] floats = buffer.getData();
        final float[] resampled = resampler.map(floats);
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer((int) (buffer.getFrameNumber() * getFactor()), resampled, getProcessedAudioFormat(buffer));
        } else {
            realAudioBuffer.reuse((int)(buffer.getFrameNumber() * getFactor()), resampled, realAudioBuffer.getAudioFormat());
        }
        return realAudioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resample that = (Resample) o;

        if (resampler.getFactor() != that.resampler.getFactor()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return resampler.getUpFactor() ^ resampler.getDownFactor() * 31;
    }

    @Override
    public String toString() {
        return "Resample{" +
                "factor=" + resampler.getUpFactor() + "/" + resampler.getDownFactor() +
                '}';
    }
}