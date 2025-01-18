/*
 * =================================================
 * Copyright 2017 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.Floats;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

/**
 * <p>Represents a spectrum with instantaneous frequencies based on two subsequent {@link LinearFrequencySpectrum}s.
 * </p><p>
 * Even though instances are meant to be immutable, all methods return
 * the original, internally used arrays to increase efficiency and reduce
 * memory consumption. As a consequence you <em>must never</em> modify any
 * of those arrays returned to you.
 * </p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class InstantaneousFrequencySpectrum extends AbstractAudioSpectrum implements Cloneable {

    private static final float TWO_PI = (float)(2 * Math.PI);
    private float[] frequencies;

    public InstantaneousFrequencySpectrum(final LinearFrequencySpectrum spectrum1, final LinearFrequencySpectrum spectrum2) {
        this(spectrum1.getFrameNumber(), averageMagnitudes(spectrum1, spectrum2), null, spectrum1.getAudioFormat());
        this.frequencies = instantaneousFrequencies(spectrum1, spectrum2);
    }

    public InstantaneousFrequencySpectrum(final int frameNumber, final float[] magnitudes, final float[] frequencies, final AudioFormat audioFormat) {
        super(frameNumber, magnitudes, null, audioFormat);
        this.frequencies = frequencies;
        this.magnitudes = new float[magnitudes.length/2];
        System.arraycopy(magnitudes, 0, this.magnitudes, 0, this.magnitudes.length);
    }

    public InstantaneousFrequencySpectrum(final InstantaneousFrequencySpectrum instantaneousFrequencySpectrum) {
        super(instantaneousFrequencySpectrum);
        this.frequencies = instantaneousFrequencySpectrum.frequencies.clone();
    }

    private static float[] averageMagnitudes(final LinearFrequencySpectrum spectrum1, final LinearFrequencySpectrum spectrum2) {
        final float[] m1 = spectrum1.getMagnitudes(); // this call only returns the first half, dues to symmetry
        final float[] m2 = spectrum2.getMagnitudes();
        float[] average = new float[m1.length*2];
        for (int i=0; i<m1.length; i++) {
            final float avg = (m1[i] + m2[i]) / 2;
            average[i] = avg;
            // mirror magnitudes
            average[average.length-1-i] = avg;
        }
        return average;
    }

    private static float[] instantaneousFrequencies(final LinearFrequencySpectrum spectrum1, final LinearFrequencySpectrum spectrum2) {
        final int hop = spectrum2.getFrameNumber() - spectrum1.getFrameNumber();
        float[] phases1 = spectrum1.getPhases();  //phases
        float[] phases2 = spectrum2.getPhases();  //phases
        // number of samples
        final int numberOfSamples = spectrum1.getNumberOfSamples();
        float[] omega = new float[numberOfSamples/2];
        float[] dphi = new float[numberOfSamples/2];
        for (int i=0; i<omega.length; i++) {
            // expected phase increment per sample for every frequency bin in[rad]
            omega[i] = (float) ((2 * Math.PI * i) / numberOfSamples);
            // expected phase increment over 'hop' samples
            dphi[i] = omega[i] * hop;
        }
        // initialize instantaneous frequencies
        float[] instFreqs = new float[dphi.length];
        for (int i=0; i<dphi.length; i++) {
            // initialize heterodyned phase increment
            float hpi = (phases2[i] - phases1[i]) - dphi[i];
            // compute heterodyned phase increment in radians
            hpi = hpi - TWO_PI * Math.round(hpi / TWO_PI);
            // compute instantaneous frequencies in Hz
            instFreqs[i] = (omega[i] + hpi / hop) / TWO_PI * spectrum1.getAudioFormat().getSampleRate();
        }
        return instFreqs;
    }

    @Override
    public void reuse(final int frameNumber, final float[] magnitudes, final float[] frequencies, final AudioFormat audioFormat) {
        super.reuse(frameNumber, magnitudes, null, audioFormat);
        this.frequencies = frequencies;
        this.magnitudes = magnitudes;
    }

    /**
     * Returns the bandwidth (or frequency resolution) of this buffer. This the distance from one bin to the next in Hz or
     * in other words, the width of one bin in Hz.
     *
     * @return frequency resolution in Hz
     */
    public float getBandwidth() {
        return getAudioFormat().getSampleRate() / getRealData().length;
    }

    /**
     * Array with frequency values in Hz corresponding to the bin numbers.
     *
     * @return array of length <code>numberOfSamples/2</code> due to symmetry
     * @see #getFrequency(int)
     */
    public synchronized float[] getFrequencies() {
        return frequencies;
    }

    /**
     * Returns the frequency of a given bin. If you need the frequencies for the whole
     * spectrum you might rather use {@link #getFrequencies()}, as it is more efficient.
     *
     * @param bin bin number
     * @return frequency in Hz
     * @see #getFrequencies()
     */
    @Override
    public float getFrequency(final int bin) {
        if (bin < 0 || bin >= frequencies.length) return 0f;
        return frequencies[bin];
    }

    /**
     * Computes a bin/index for the given frequency.
     *
     * @param frequency frequency
     * @return bin
     */
    public int getBin(final float frequency) {
        return (int) ((frequency / getAudioFormat().getSampleRate()) * getRealData().length);
    }

    /**
     * Powers (sum of the squares of the real and imaginary part) of the spectrum.
     *
     * @return array of length numberOfSamples/2 due to symmetry
     * @see #getMagnitudes()
     */
    public synchronized float[] getPowers() {
        if (powers == null) {
            powers = Floats.square(getMagnitudes().clone());
        }
        return powers;
    }

    /**
     * Magnitudes (square root of the powers) of the spectrum.
     *
     * @return array of length numberOfSamples/2 due to symmetry
     * @see #getPowers()
     */
    public synchronized float[] getMagnitudes() {
        if (magnitudes == null) {
            magnitudes = computeMagnitudes(getNumberOfSamples() / 2, powers);
        }
        return magnitudes;
    }

    public InstantaneousFrequencySpectrum derive(final float[] magnitudes, final float[] frequencies) {
        return new InstantaneousFrequencySpectrum(getFrameNumber(), magnitudes, frequencies, getAudioFormat());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstantaneousFrequencySpectrum that = (InstantaneousFrequencySpectrum) o;

        if (this.frameNumber != that.frameNumber) return false;
        if (getAudioFormat() != null ? !getAudioFormat().equals(that.getAudioFormat()) : that.getAudioFormat() != null) return false;
        if (!Arrays.equals(getMagnitudes(), that.getMagnitudes())) return false;
        if (!Arrays.equals(getFrequencies(), that.getFrequencies())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getAudioFormat() != null ? getAudioFormat().hashCode() : 0;
        result = 31 * result + (getMagnitudes() != null ? Arrays.hashCode(getMagnitudes()) : 0);
        result = 31 * result + (getFrequencies() != null ? Arrays.hashCode(getFrequencies()) : 0);
        result = 31 * result + frameNumber;
        return result;
    }

    @Override
    public String toString() {
        return "InstantaneousFrequencySpectrum{" +
                "audioFormat=" + getAudioFormat() +
                ", numberOfSamples=" + getNumberOfSamples() +
                ", timestamp=" + getTimestamp() +
                ", frameNumber=" + getFrameNumber() +
                ", magnitudes=" + getMagnitudes() +
                ", frequencies=" + getFrequencies() +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final InstantaneousFrequencySpectrum clone = (InstantaneousFrequencySpectrum)super.clone();
        if (this.frequencies != null) clone.frequencies = this.frequencies.clone();
        return clone;
    }
}

