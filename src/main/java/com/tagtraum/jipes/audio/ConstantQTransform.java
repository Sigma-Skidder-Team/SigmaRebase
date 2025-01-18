/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.math.ConstantQTransformFactory;
import com.tagtraum.jipes.math.Transform;

import java.io.IOException;

/**
 * Processes input using a transform created by {@link com.tagtraum.jipes.math.ConstantQTransformFactory}.
 * Since constant Q transforms are often initialized with arguments from the {@link javax.sound.sampled.AudioFormat}
 * the transform is created at runtime with the first input {@link AudioBuffer} using a {@link ConstantQTransformFactory}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see ConstantQTransformFactory
 * @see Transform
 */
public class ConstantQTransform extends AbstractSignalProcessor<AudioBuffer, LogFrequencySpectrum> {

    private Transform constantQTransform;
    private int binsPerOctave;
    private float maxFrequency;
    private float minFrequency;
    private float Q;
    private float[] frequencies;
    private float threshold;

    /**
     * @param minFrequency min frequency
     * @param maxFrequency max frequency
     * @param binsPerOctave bins per octave
     */
    public ConstantQTransform(final float minFrequency, final float maxFrequency, final int binsPerOctave) {
        this(minFrequency, maxFrequency, binsPerOctave, ConstantQTransformFactory.DEFAULT_THRESHOLD);
    }

    public ConstantQTransform(final float minFrequency, final float maxFrequency, final int binsPerOctave, final float threshold) {
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.binsPerOctave = binsPerOctave;
        this.threshold = threshold;
        this.Q = (float) (1f / (Math.pow(2, 1.0f / binsPerOctave) - 1));
    }

    @Override
    public void reset() {
        constantQTransform = null;
        frequencies = null;
    }

    protected LogFrequencySpectrum processNext(final AudioBuffer buffer) throws IOException {
        if (buffer.getAudioFormat().getChannels() != 1) {
            throw new IOException("ConstantQTransform only supports single channel buffers. Actual audio format: " + buffer.getAudioFormat());
        }
        if (constantQTransform == null) {
            constantQTransform = ConstantQTransformFactory.getInstance().create(minFrequency, maxFrequency, binsPerOctave, buffer.getAudioFormat().getSampleRate(), threshold);
        }
        final float[][] transform = constantQTransform.transform(buffer.getRealData());
        if (frequencies == null) {
            frequencies = transform[2];
            for (int i=0; i<frequencies.length; i++) {
                frequencies[i] *= buffer.getAudioFormat().getSampleRate();
            }
        }
        return new LogFrequencySpectrum(buffer.getFrameNumber(),
                transform[0], transform[1],
                buffer.getAudioFormat(),
                Q,
                frequencies);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ConstantQTransform that = (ConstantQTransform) o;

        if (binsPerOctave != that.binsPerOctave) return false;
        if (Float.compare(that.maxFrequency, maxFrequency) != 0) return false;
        if (Float.compare(that.minFrequency, minFrequency) != 0) return false;
        if (Float.compare(that.threshold, threshold) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = binsPerOctave;
        result = 31 * result + (maxFrequency != +0.0f ? Float.floatToIntBits(maxFrequency) : 0);
        result = 31 * result + (minFrequency != +0.0f ? Float.floatToIntBits(minFrequency) : 0);
        result = 31 * result + (threshold != +0.0f ? Float.floatToIntBits(threshold) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConstantQTransform{" +
                "minFrequency=" + minFrequency +
                "Hz, maxFrequency=" + maxFrequency +
                "Hz, binsPerOctave=" + binsPerOctave +
                ", threshold=" + threshold +
                '}';
    }
}
