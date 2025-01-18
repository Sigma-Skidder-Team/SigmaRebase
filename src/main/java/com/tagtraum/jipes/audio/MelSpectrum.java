/*
 * =================================================
 * Copyright 2014 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

/**
 * Mel spectrum typically constructed from a {@link com.tagtraum.jipes.audio.LinearFrequencySpectrum} created
 * by {@link FFT}.
 * Each mel channel is computed by applying a triangular filter to the <em>magnitudes or powers</em> of the input spectrum and adding up
 * the result. The filters for consecutive channels overlap by 0.5 of their length.
 * <br>
 * Computing the mel spectrum from the magnitudes happens similar to the specification in
 * European Telecommunications Standards Institute (2003),
 * <a href="http://www.etsi.org/deliver/etsi_es/201100_201199/201108/01.01.03_60/es_201108v010103p.pdf">Speech Processing, Transmission and Quality Aspects (STQ); Distributed speech recognition; Front-end feature extraction algorithm; Compression algorithms</a>.
 * Technical standard ES 201 108, v1.1.3.
 * <br>
 * The <em>European Telecommunications Standards Institute</em> requires the filtering of the <em>magnitudes</em>.
 * However, this implementation lets you choose between magnitude and power filtering.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MelSpectrum extends AbstractAudioSpectrum implements Cloneable {

    private float[][] filterBank;
    private float[] channelBoundariesInHz;

    /**
     * Creates a mel spectrum from a linear spectrum.
     * <br/>
     * Note that the upper and lower boundaries are <em>not</em> the central frequencies of the
     * mel channels, but rather their edges.
     *
     * @param frameNumber frame number
     * @param audioSpectrum audio spectrum to copy data from
     * @param lowerFrequency lower frequency boundary for the lowest triangular filter in Hz
     * @param upperFrequency upper frequency boundary for the highest triangular filter in Hz
     * @param channels number of mel channels
     * @param filterPowers if true, the power spectrum is filtered, if false, the magnitude spectrum is filtered
     */
    public MelSpectrum(int frameNumber, final LinearFrequencySpectrum audioSpectrum, final float lowerFrequency, final float upperFrequency, final int channels, final boolean filterPowers) {
        this(frameNumber, audioSpectrum,
                createFilterBank(audioSpectrum.getFrequencies(), channelBoundaries(lowerFrequency, upperFrequency, channels + 2)),
                channelBoundaries(lowerFrequency, upperFrequency, channels + 2), filterPowers);
    }

    /**
     * Creates a mel spectrum from a linear spectrum.
     *
     * @param frameNumber frame number
     * @param audioSpectrum audio spectrum to copy data from
     * @param filterBank filterbank with coefficients for each channel
     * @param channelBoundariesInHz channel boundaries in Hz
     * @param filterPowers if true, the power spectrum is filtered, if false, the magnitude spectrum is filtered
     * @see #createFilterBank(float[], float[])
     * @see #channelBoundaries(float, float, int)
     */
    public MelSpectrum(int frameNumber, final LinearFrequencySpectrum audioSpectrum, final float[][] filterBank, final float[] channelBoundariesInHz, final boolean filterPowers) {
        super(frameNumber, null, null, audioSpectrum.getAudioFormat());
        this.channelBoundariesInHz = channelBoundariesInHz;
        this.filterBank = filterBank;
        if (filterPowers) {
            this.powers = computeBins(filterBank, audioSpectrum.getPowers());
            this.magnitudes = new float[powers.length];
            for (int i=0; i<powers.length; i++) {
                this.magnitudes[i] = (float)Math.sqrt(powers[i]);
            }
            this.realData = magnitudes.clone();
            this.imaginaryData = new float[magnitudes.length];
        } else {
            this.magnitudes = computeBins(filterBank, audioSpectrum.getMagnitudes());
            this.realData = magnitudes.clone();
            this.imaginaryData = new float[magnitudes.length];
            this.powers = new float[magnitudes.length];
            for (int i=0; i<magnitudes.length; i++) {
                final float m = magnitudes[i];
                this.powers[i] = m * m;
            }
        }
    }

    public MelSpectrum(final MelSpectrum melSpectrum) {
        super(melSpectrum);
        this.filterBank = melSpectrum.filterBank;
        this.channelBoundariesInHz = melSpectrum.channelBoundariesInHz;
        if (melSpectrum.magnitudes != null) this.magnitudes = melSpectrum.magnitudes.clone();
        if (melSpectrum.realData != null) this.realData = melSpectrum.realData.clone();
        if (melSpectrum.imaginaryData != null) this.imaginaryData = melSpectrum.imaginaryData.clone();
        if (melSpectrum.powers != null) this.powers = melSpectrum.powers.clone();
    }

    public MelSpectrum(final int frameNumber, final float[] real, final float[] imaginary, final AudioFormat audioFormat, final float[][] filterBank, final float[] channelBoundariesInHz) {
        super(frameNumber, null, null, audioFormat);
        this.realData = real;
        this.imaginaryData = imaginary;
        this.magnitudes = new float[real.length];
        this.powers = new float[real.length];

        for (int i=0; i<real.length; i++) {
            if (imaginary == null || imaginary[i] == 0) {
                this.magnitudes[i] = Math.abs(real[i]);
                this.powers[i] = real[i]*real[i];
            } else {
                this.powers[i] = real[i]*real[i] + imaginary[i]*imaginary[i];
                this.magnitudes[i] = (float)Math.sqrt(powers[i]);
            }
        }

        this.channelBoundariesInHz = channelBoundariesInHz;
        this.filterBank = filterBank;
    }

    /**
     * Convert a frequency in Hertz to mel.
     *
     * @param frequency frequency
     * @return mel
     * @see <a href="http://en.wikipedia.org/wiki/Mel_scale">Mel scale</a>
     */
    private static float hertzToMel(final float frequency) {
        return (float)(2595.0 * log10(1 + (frequency / 700.0)));
    }

    /**
     * Convert a frequency in mel to Hertz.
     *
     * @param mel mel
     * @return frequency in Hz
     * @see <a href="http://en.wikipedia.org/wiki/Mel_scale">Mel scale</a>
     */
    private static float melToHertz(final float mel) {
        return (float) (700.0 * (pow(10.0, mel / 2595.0)-1.0));
    }

    /**
     * Creates an array with {@code boundaryCount} frequencies in Hz, dividing the given
     * frequency range into {@code boundaryCount - 1} bins with equal mel-length.
     *
     * @param lowerFrequency lowerFrequency in Hz
     * @param upperFrequency upperFrequency in Hz
     * @param boundaryCount number of boundaries
     * @return array with boundary frequencies
     */
    public static float[] channelBoundaries(final float lowerFrequency, final float upperFrequency, final int boundaryCount) {
        if (boundaryCount < 2) throw new IllegalArgumentException("Count must be 2 or greater: " + boundaryCount);
        final float lowerMel = hertzToMel(lowerFrequency);
        final float upperMel = hertzToMel(upperFrequency);
        final float melBinWidth = (upperMel - lowerMel) / (boundaryCount - 1);
        final float[] boundaries = new float[boundaryCount];
        boundaries[0] = lowerFrequency;
        boundaries[boundaries.length-1] = upperFrequency;
        for (int boundary = 1; boundary<boundaries.length-1; boundary++) {
            boundaries[boundary] = melToHertz(lowerMel + boundary * melBinWidth);
        }
        return boundaries;
    }

    /**
     * Creates an array of filter coefficients.
     *
     * @param frequencies frequencies for a value input array
     * @param channelBoundariesInHz channel boundaries
     * @return filter bank
     */
    public static float[][] createFilterBank(final float[] frequencies, float[] channelBoundariesInHz) {
        final int channels = channelBoundariesInHz.length-2;
        final float[][] filterBank = new float[channels][frequencies.length];
        for (int channel=0; channel<channels; channel++) {
            final float low = channelBoundariesInHz[channel];
            final float center = channelBoundariesInHz[channel + 1];
            final float high = channelBoundariesInHz[channel + 2];
            final float[] channelFilter = filterBank[channel];
            for (int i = 0; i<frequencies.length; i++) {
                final float frequency = frequencies[i];
                if (frequency < low || frequency > high) {
                    continue;
                }
                if (frequency < center) {
                    channelFilter[i] = (frequency - low) / (center - low);
                } else {
                    channelFilter[i] = (high - frequency) / (high - center);
                }
            }
        }
        return filterBank;
    }

    private float[] computeBins(final float[][] filterBank, final float[] values) {
        final int channels = filterBank.length;
        final float[] melValues = new float[channels];
        for (int channel=0; channel<channels; channel++) {
            final float[] coefficients = filterBank[channel];
            for (int i=0; i<values.length; i++) {
                final float coefficient = coefficients[i];
                melValues[channel] += coefficient * values[i];
            }
        }
        return melValues;
    }

    public MelSpectrum derive(final float[] real, final float[] imaginary) {
        return new MelSpectrum(getFrameNumber(), real, imaginary, getAudioFormat(), filterBank, channelBoundariesInHz);
    }

    /**
     * Number of channels/bands.
     *
     * @return number of channels/bands
     */
    public int getNumberOfBands() {
        return filterBank.length;
    }

    /**
     * Computes a bin for the given frequency. Returns -1, if the bin is not found.
     *
     * @param frequency frequency
     * @return bin
     */
    public int getBin(final float frequency) {
        for (int bin=0; bin<channelBoundariesInHz.length-1; bin++) {
            final float lastFreq = channelBoundariesInHz[bin];
            final float freq = channelBoundariesInHz[bin+1];
            if (frequency >= lastFreq && frequency < freq) {
                if (Math.abs(lastFreq-frequency) < Math.abs(freq-frequency)) {
                    return Math.max(0, bin - 1);
                } else {
                    return Math.min(channelBoundariesInHz.length-3, bin);
                }
            }
        }
        return -1;
    }

    public float[] getFrequencies() {
        final float[] frequencies = new float[channelBoundariesInHz.length-2];
        System.arraycopy(channelBoundariesInHz, 1, frequencies, 0, frequencies.length);
        return frequencies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MelSpectrum that = (MelSpectrum) o;

        if (this.frameNumber != that.frameNumber) return false;
        if (!Arrays.equals(channelBoundariesInHz, that.channelBoundariesInHz)) return false;
        if (!Arrays.equals(imaginaryData, that.imaginaryData)) return false;
        if (!Arrays.equals(realData, that.realData)) return false;

        return true;
    }


    @Override
    public int hashCode() {
        int result = realData != null ? Arrays.hashCode(realData) : 0;
        result = 31 * result + (imaginaryData != null ? Arrays.hashCode(imaginaryData) : 0);
        result = 31 * result + (channelBoundariesInHz != null ? Arrays.hashCode(channelBoundariesInHz) : 0);
        result = 31 * result + frameNumber;
        return result;
    }

    @Override
    public String toString() {
        return "MelSpectrum{" +
                "timestamp=" + getTimestamp() +
                ", frameNumber=" + getFrameNumber() +
                ", channels=" + (channelBoundariesInHz.length-2) +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final MelSpectrum clone = (MelSpectrum)super.clone();
        clone.channelBoundariesInHz = this.channelBoundariesInHz.clone();
        clone.filterBank = this.filterBank.clone();
        return clone;
    }
}
