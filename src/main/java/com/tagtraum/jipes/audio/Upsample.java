/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

/**
 * Adds zero samples (zero stuffing) in order to upsample the data. Note that proper upsampling (i.e. interpolating)
 * has to be followed by the application of a suitable low pass filter (see {@link com.tagtraum.jipes.math.Filters}
 * and {@link com.tagtraum.jipes.universal.Mapping}) to avoid aliasing.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Upsample extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private int factor = 2;
    private float[] output;
    private RealAudioBuffer realAudioBuffer;

    /**
     * Creates an upsample processor with the given upsampling factor.
     *
     * @param factor number of added zeros plus one (upsampling factor)
     */
    public Upsample(final int factor) {
        this.factor = factor;
    }

    /**
     * Creates an upsample processor that adds a zero for each sample.
     */
    public Upsample() {
        this(2);
    }

    /**
     * @see #setFactor(int)
     * @return upsampling factor
     */
    public int getFactor() {
        return factor;
    }

    /**
     * Add zeroes for every sample so that the sample rate is <code>x</code>-times higher
     * than before.
     *
     * So if you specify 2, 1 zero will be added for every sample, effectively doubling the sample rate.
     * If you specify 3, the number of samples (and thus also the samplerate) triples - for each
     * sample two zeros are added.
     *
     * @param factor upsampling factor
     */
    public void setFactor(final int factor) {
        if (factor < 1) throw new IllegalArgumentException("Upsampling factor must be greater than 0: " + factor);
        this.factor = factor;
    }

    private AudioFormat getProcessedAudioFormat(final AudioBuffer buffer) {
        final AudioFormat sourceAudioFormat = buffer.getAudioFormat();
        return new AudioFormat(sourceAudioFormat.getSampleRate() * factor,
                sourceAudioFormat.getSampleSizeInBits(),
                sourceAudioFormat.getChannels(),
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) {
        final float[] floats = buffer.getData();
        final int outputLength = floats.length * factor;
        if (output == null || outputLength != output.length) {
            output = new float[outputLength];
        }
        final int channels = buffer.getAudioFormat().getChannels();
        Arrays.fill(output, 0f);


        for (int i=0; i<floats.length; i+=channels) {
            try {
                System.arraycopy(floats, i, output, i*factor, channels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer(buffer.getFrameNumber() * factor, output, getProcessedAudioFormat(buffer));
        } else {
            realAudioBuffer.reuse(buffer.getFrameNumber() * factor, output, realAudioBuffer.getAudioFormat());
        }
        return realAudioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Upsample that = (Upsample) o;

        if (factor != that.factor) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return factor;
    }

    @Override
    public String toString() {
        return "Upsample{" +
                "factor=" + factor +
                '}';
    }
}