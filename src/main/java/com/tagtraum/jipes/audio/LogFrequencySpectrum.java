/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.Floats;

import javax.sound.sampled.AudioFormat;

/**
 * Log frequency spectrum - possibly created by {@link ConstantQTransform}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see ConstantQTransform
 * @see com.tagtraum.jipes.math.ConstantQTransformFactory
 */
public class LogFrequencySpectrum extends AbstractAudioSpectrum implements Cloneable {

    private static final double LOG2 = Math.log(2.0);
    private float[] frequencies;
    private float q;

    public LogFrequencySpectrum(final int frameNumber, final float[] real, final float[] imag, final AudioFormat audioFormat, final float q, final float[] frequencies) {
        super(frameNumber, real, imag, audioFormat);
        this.q = q;
        this.frequencies = frequencies;
        if (frequencies.length != real.length) {
            throw new IllegalArgumentException("Length of frequencies must match length of data: f.length=" + frequencies.length + ", r.length=" + real.length);
        }
    }

    public LogFrequencySpectrum(final LogFrequencySpectrum logFrequencySpectrum) {
        super(logFrequencySpectrum);
        this.frequencies = logFrequencySpectrum.frequencies;
        this.q = logFrequencySpectrum.q;
    }

    /**
     * <p>Number of fractions each semitone is divided into.</p>
     * <p>E.g. if <code>binsPerSemitone</code is 3, this spectrum contains three bins for each semitone - each
     * one a third of a semitone wide.</p>
     *
     * @return number of bins/indices each semitone is divided into
     * @see com.tagtraum.jipes.audio.LinearFrequencySpectrum#getBandwidth()
     * @see #getQ()
     */
    public float getBinsPerSemitone() {
        return (float)(1.0/(12.0 * log2(1.0 / getQ() + 1.0)));
    }

    /**
     * <p>Returns the ratio between a note's frequency and its bandwidth.</p>
     * <p>Q is directly related to {@link #getBinsPerSemitone()}, as it is defined
     * like this:</p>
     * <p><code>Q = (binsPerSemitone * 12) / ln 2</code></p>
     * <p>Instances of this spectrum have a constant Q value.</p>
     *
     * @return Q, i.e. the ratio between a note's frequency and its bandwidth
     * @see #getBinsPerSemitone()
     */
    public float getQ() {
        return q;
    }

    public LogFrequencySpectrum derive(final float[] real, final float[] imaginary) {
        return new LogFrequencySpectrum(getFrameNumber(), real, imaginary, getAudioFormat(), q, frequencies);
    }

    /**
     * Creates a copy of this spectrum with the bins' values shifted (interpolated) by shift*{@link #getBinsPerSemitone()}
     * bins. Note that the frequencies are <em>not</em> adjusted.
     * This is e.g. useful to map a tuning other than 440Hz into the regular 440Hz tuning.
     *
     * @param shift value between -1 and 1
     * @return spectrum with shifted (and if necessary interpolated) bins
     */
    public LogFrequencySpectrum derive(final float shift) {
        // interpolate bins
        final int binsPerOneShift = Math.round(this.getBinsPerSemitone());
        final float[] shiftedReal = Floats.interpolate(getRealData(), shift, binsPerOneShift);
        final float[] shiftedImag = getImaginaryData() == null ? null : Floats.interpolate(getImaginaryData(), shift, binsPerOneShift);
        return derive(shiftedReal, shiftedImag);
    }

    /**
     * Center frequencies for each bin.
     *
     * @return frequencies
     */
    public float[] getFrequencies() {
        return frequencies;
    }

    /**
     * MIDI notes for each bin.
     *
     * @return MIDI notes
     * @see <a href="http://en.wikipedia.org/wiki/Musical_Instrument_Digital_Interface">MIDI on Wikipedia</a>
     */
    public float[] getMidiNotes() {
        final float[] midiNotes = new float[getRealData().length];
        for (int i=0; i<midiNotes.length; i++) {
            midiNotes[i] = getMidiNote(i);
        }
        return midiNotes;
    }

    /**
     * MIDI note for a bin/index.
     *
     * @param index bin index
     * @return MIDI note
     * @see #getMidiNotes()
     */
    public float getMidiNote(final int index) {
        return (float) (69 + 12.0 * log2(getFrequency(index) / 440.0));
    }

    /**
     * Returns the bin that is closest to the given frequency.
     *
     * @param frequency frequency
     * @return (rounded) bin number
     */
    public int getBin(final float frequency) {
        int bestBin = -1;
        float bestDiff = Float.MAX_VALUE;

        for (int i=0; i<frequencies.length; i++) {
            final float freq = frequencies[i];
            final float diff = Math.abs(freq - frequency);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestBin = i;
            }
        }
        return bestBin;
    }

    /**
     * Base 2 logarithm.
     *
     * @param n n
     * @return base 2 logarithm
     */
    private static double log2(final double n) {
        return Math.log(n) / LOG2;
    }


    @Override
    public String toString() {
        return "LogFrequencySpectrum{" +
                "audioFormat=" + getAudioFormat() +
                ", Q=" + getQ() +
                ", binsPerSemitone=" + getBinsPerSemitone() +
                ", timestamp=" + getTimestamp() +
                ", frameNumber=" + getFrameNumber() +
                ", realData=" + getRealData() +
                ", imaginaryData=" + getImaginaryData() +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LogFrequencySpectrum clone = (LogFrequencySpectrum)super.clone();
        if (this.frequencies != null) clone.frequencies = this.frequencies.clone();
        clone.q = this.q;
        return clone;
    }
}
