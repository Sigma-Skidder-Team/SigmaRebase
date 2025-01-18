/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.math.FFTFactory;
import com.tagtraum.jipes.math.Floats;
import com.tagtraum.jipes.math.Transform;

import java.io.IOException;

/**
 * Transforms samples obtained with {@link com.tagtraum.jipes.audio.AudioBuffer#getData()}
 * using the FFT. The result will be a {@link com.tagtraum.jipes.audio.LinearFrequencySpectrum}, containing
 * the real part, the imaginary part and methods for accessing all kinds of other goodies.<br>
 * Should the number of samples fed into this processor not be a power of two, the sample array will
 * be zero padded at the end before applying the FFT.
 * <p/>
 * The returned {@link com.tagtraum.jipes.audio.AudioSpectrum} object is re-used. If you need to hold on
 * to it for longer than the current method call, you must either {@link Object#clone()} it or
 * create a copy using a copy constructor like {@link com.tagtraum.jipes.audio.LinearFrequencySpectrum#LinearFrequencySpectrum(com.tagtraum.jipes.audio.LinearFrequencySpectrum)}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see FFTFactory
 * @see DCT
 * @see IFFT
 */
public class FFT extends AbstractSignalProcessor<AudioBuffer, LinearFrequencySpectrum> {

    private Transform fft;
    private int length;
    private float requiredResolutionInHz;
    private LinearFrequencySpectrum linearFrequencySpectrum;

    /**
     * @param length minimum size of the array to transform - shorter buffers will be zero padded
     * @param requiredResolutionInHz min res in Hz
     */
    private FFT(final int length, final float requiredResolutionInHz) {
        if (length > 0 && requiredResolutionInHz > 0) throw new IllegalArgumentException("Either length or requiredResolutionInHz must be 0");
        this.length = length;
        this.requiredResolutionInHz = requiredResolutionInHz;
    }

    /**
     * @param length minimum size of the array to transform - shorter buffers will be zero padded
     */
    public FFT(final int length) {
        this(length, 0);
    }

    /**
     *
     * @param requiredResolutionInHz min res in Hz (achieved through zero padding)
     */
    public FFT(final float requiredResolutionInHz) {
        this(0, requiredResolutionInHz);
    }

    public FFT() {
        this(0);
    }

    /**
     * FFT length.
     *
     * @return values &lt= 0, if the length is still unknown.
     */
    public int getLength() {
        return length;
    }

    /**
     * Desired resolution in Hz.
     *
     * @return values &lt= 0, if the required resolution was not set.
     */
    public float getRequiredResolutionInHz() {
        return requiredResolutionInHz;
    }

    @Override
    protected LinearFrequencySpectrum processNext(final AudioBuffer buffer) throws IOException {
        if (buffer.getAudioFormat() != null && buffer.getAudioFormat().getChannels() != 1) {
            throw new IOException("Source must be mono.");
        }
        if (requiredResolutionInHz > 0 && length == 0) {
            length = (int)Math.ceil(buffer.getAudioFormat().getSampleRate()/requiredResolutionInHz);
        }

        final float[] floats = Floats.zeroPadAtEnd(length, buffer.getData());
        if (fft == null) {
            this.fft = FFTFactory.getInstance().create(floats.length);
            if (length == 0) length = floats.length;
        }
        final float[][] realFftResult = fft.transform(floats);
        assert realFftResult[0].length == floats.length;
        if (linearFrequencySpectrum == null) {
            linearFrequencySpectrum = new LinearFrequencySpectrum(buffer.getFrameNumber(), realFftResult[0], realFftResult[1], buffer.getAudioFormat());
        } else {
            linearFrequencySpectrum.reuse(buffer.getFrameNumber(), realFftResult[0], realFftResult[1], buffer.getAudioFormat());
        }
        return linearFrequencySpectrum;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FFT that = (FFT) o;
        if (this.length > 0 && that.length > 0
                && this.length == that.length) return true;

        // This is kind of tricky, as it can result in two objects not being equal anymore,
        // once the actual length has been determined.
        if (this.requiredResolutionInHz > 0 && that.requiredResolutionInHz > 0
                && this.requiredResolutionInHz == that.requiredResolutionInHz) return true;
        if (this.length <= 0 && that.length <= 0
                && this.requiredResolutionInHz <=0 && that.requiredResolutionInHz <=0) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        if (length > 0) {
            return "FFT{" +
                    "length=" + length +
                    '}';
        } else if (requiredResolutionInHz > 0) {
            return "FFT{" +
                    "requiredResolutionInHz=" + requiredResolutionInHz +
                    "Hz}";
        } else {
            return "FFT{" +
                    "length=equal to first input" +
                    '}';
        }
    }
}