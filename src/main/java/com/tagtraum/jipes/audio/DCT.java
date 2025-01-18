/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.math.DCTFactory;
import com.tagtraum.jipes.math.Transform;

import java.io.IOException;

/**
 * Transforms samples obtained with {@link AudioBuffer#getData()}
 * using the DCT. The result will be a {@link LinearFrequencySpectrum}, containing
 * the real part and methods for accessing all kinds of other goodies.<br>
 * Should the number of samples fed into this processor not be a power of two, the sample array will
 * be zero padded at the end before applying the DCT.
 * <p/>
 * The returned {@link AudioSpectrum} object is re-used. If you need to hold on
 * to it for longer than the current method call, you must either {@link Object#clone()} it or
 * create a copy using a copy constructor like {@link LinearFrequencySpectrum#LinearFrequencySpectrum(LinearFrequencySpectrum)}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.math.DCTFactory
 * @see FFT
 */
public class DCT extends AbstractSignalProcessor<AudioBuffer, LinearFrequencySpectrum> {

    private Transform dct;
    private int length;
    private LinearFrequencySpectrum linearFrequencySpectrum;

    /**
     * @param length minimum size of the array to transform - shorter buffers will be zero padded
     */
    public DCT(final int length) {
        this.length = length;
    }

    public DCT() {
        this(0);
    }

    @Override
    protected LinearFrequencySpectrum processNext(final AudioBuffer buffer) throws IOException {
        if (buffer.getAudioFormat() != null && buffer.getAudioFormat().getChannels() != 1) {
            throw new IOException("Source must be mono.");
        }
        final float[] floats = buffer.getData();
        if (dct == null) {
            this.dct = DCTFactory.getInstance().create(floats.length);
            if (length == 0) length = floats.length;
        }
        final float[][] dctResult = dct.transform(floats);
        assert dctResult[0].length == floats.length : "DCT result has different length (" + dctResult[0].length + ") than input (" + floats.length + ").";
        if (linearFrequencySpectrum == null) {
            linearFrequencySpectrum = new LinearFrequencySpectrum(buffer.getFrameNumber(), dctResult[0], null, buffer.getAudioFormat());
        } else {
            linearFrequencySpectrum.reuse(buffer.getFrameNumber(), dctResult[0], null, buffer.getAudioFormat());
        }
        return linearFrequencySpectrum;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DCT that = (DCT) o;
        if (this.length > 0 && that.length > 0
                && this.length == that.length) return true;

        // This is kind of tricky, as it can result in two objects not being equal anymore,
        // once the actual length has been determined.
        if (this.length <= 0 && that.length <= 0) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        if (length > 0) {
            return "DCT{" +
                    "length=" + length +
                    '}';
        } else {
            return "DCT{" +
                    "length=equal to first input" +
                    '}';
        }
    }
}