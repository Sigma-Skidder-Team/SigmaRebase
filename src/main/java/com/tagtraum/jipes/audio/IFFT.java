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
 * <p>
 * Transforms samples obtained from a given {@link LinearFrequencySpectrum}
 * using the inverse FFT. The result will be an {@link AudioBuffer}, containing
 * the real part, the imaginary part and methods for accessing all kinds of other goodies.<br>
 * Should the number of samples fed into this processor not be a power of two, the sample array will
 * be zero padded at the end before applying the inverse FFT.
 * <p/>
 * <p>
 * The returned {@link AudioBuffer} object is re-used. If you need to hold on
 * to it for longer than the current method call, you must either {@link Object#clone()} it or
 * create a copy using a copy constructor.
 * </p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see FFTFactory
 * @see DCT
 * @see FFT
 */
public class IFFT extends AbstractSignalProcessor<LinearFrequencySpectrum, AudioBuffer> {

    private Transform fft;
    private int length;
    private float requiredResolutionInHz;
    private ComplexAudioBuffer audioBuffer;

    /**
     * @param length minimum size of the array to transform - shorter buffers will be zero padded
     * @param requiredResolutionInHz min res in Hz
     */
    private IFFT(final int length, final float requiredResolutionInHz) {
        if (length > 0 && requiredResolutionInHz > 0) throw new IllegalArgumentException("Either length or requiredResolutionInHz must be 0");
        this.length = length;
        this.requiredResolutionInHz = requiredResolutionInHz;
    }

    /**
     * @param length minimum size of the array to transform - shorter buffers will be zero padded
     */
    public IFFT(final int length) {
        this(length, 0);
    }

    /**
     *
     * @param requiredResolutionInHz min res in Hz (achieved through zero padding)
     */
    public IFFT(final float requiredResolutionInHz) {
        this(0, requiredResolutionInHz);
    }

    public IFFT() {
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
    protected AudioBuffer processNext(final LinearFrequencySpectrum buffer) throws IOException {
        if (buffer.getAudioFormat() != null && buffer.getAudioFormat().getChannels() != 1) {
            throw new IOException("Source must be mono.");
        }
        if (requiredResolutionInHz > 0 && length == 0) {
            length = (int)Math.ceil(buffer.getAudioFormat().getSampleRate()/requiredResolutionInHz);
        }

        final float[] realFloats = Floats.zeroPadAtEnd(length, buffer.getRealData());
        final float[] imaginaryFloats = Floats.zeroPadAtEnd(length, buffer.getImaginaryData());
        if (fft == null) {
            this.fft = FFTFactory.getInstance().create(realFloats.length);
            if (length == 0) length = realFloats.length;
        }
        final float[][] inverseFftResult = fft.inverseTransform(realFloats, imaginaryFloats);
        if (audioBuffer == null) {
            audioBuffer = new ComplexAudioBuffer(buffer.getFrameNumber(), inverseFftResult[0], inverseFftResult[1], buffer.getAudioFormat());
        } else {
            audioBuffer.reuse(buffer.getFrameNumber(), inverseFftResult[0], inverseFftResult[1], buffer.getAudioFormat());
        }
        return audioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final IFFT that = (IFFT) o;
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
            return "IFFT{" +
                    "length=" + length +
                    '}';
        } else if (requiredResolutionInHz > 0) {
            return "IFFT{" +
                    "requiredResolutionInHz=" + requiredResolutionInHz +
                    "Hz}";
        } else {
            return "IFFT{" +
                    "length=equal to first input" +
                    '}';
        }
    }
}