/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

/**
 * <p>Represents an audio spectrum divided into bands as produced for example by
 * {@link #createLogarithmicBands(float, float, int)} or {@link #createMidiBands(int, int)}.</p>
 *
 * <p>The bands <em>do not</em> have to be logarithmically spaced.</p>
 *
 * <p>Even though instances are meant to be immutable, all methods return
 * the original internally used arrays to increase efficiency and reduce
 * memory consumption. As a consequence you must <em>never</em> modify any
 * of those arrays returned to you.</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MultiBandSpectrum extends AbstractAudioSpectrum implements Cloneable {

    private static final double LOG2 = Math.log(2.0);
    private int numberOfBands;
    private float[] bandBoundariesInHz;

    /**
     * Creates a multi band spectrum from another spectrum.
     *
     * @param frameNumber frame number
     * @param audioSpectrum audio spectrum to copy data from
     * @param bandBoundariesInHz band boundaries in Hz
     */
    public MultiBandSpectrum(int frameNumber, final AudioSpectrum audioSpectrum, final float[] bandBoundariesInHz) {
        super(frameNumber, null, null, audioSpectrum.getAudioFormat());
        this.bandBoundariesInHz = bandBoundariesInHz;
        this.numberOfBands = bandBoundariesInHz.length-1;
        final float[] frequencies = audioSpectrum.getFrequencies();
        this.powers = computeBins(frequencies, audioSpectrum.getPowers());
        this.realData = new float[powers.length];
        this.imaginaryData = new float[powers.length];
        this.magnitudes = new float[powers.length];
        for (int i=0; i<bandBoundariesInHz.length-1; i++) {
            final float m = (float) Math.sqrt(powers[i]);
            this.magnitudes[i] = m;
            this.realData[i] = m;
        }
    }

    /**
     * Constructs a new multi band spectrum. Important: Provided real/imaginary data must already
     * be in bins. The provided band boundaries merely describe those bins.
     *
     * @param frameNumber frame number
     * @param real real
     * @param imaginary imaginary
     * @param audioFormat audio format
     * @param bandBoundariesInHz band boundaries in Hz
     */
    public MultiBandSpectrum(int frameNumber, final float[] real, final float[] imaginary, final AudioFormat audioFormat, final float[] bandBoundariesInHz) {
        super(frameNumber, null, null, audioFormat);
        if (bandBoundariesInHz.length != real.length+1) {
            throw new IllegalArgumentException("Must provide samples+1 band boundaries: r.length=" + real.length + ", b.length=" + bandBoundariesInHz.length);
        }
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

        this.bandBoundariesInHz = bandBoundariesInHz;
        this.numberOfBands = bandBoundariesInHz.length-1;
    }


    public MultiBandSpectrum(final MultiBandSpectrum multiBandSpectrum) {
        super(multiBandSpectrum);
        this.bandBoundariesInHz = multiBandSpectrum.bandBoundariesInHz;
        this.numberOfBands = multiBandSpectrum.numberOfBands;
        if (multiBandSpectrum.magnitudes != null) this.magnitudes = multiBandSpectrum.magnitudes.clone();
        if (multiBandSpectrum.realData != null) this.realData = multiBandSpectrum.realData.clone();
        if (multiBandSpectrum.imaginaryData != null) this.imaginaryData = multiBandSpectrum.imaginaryData.clone();
        if (multiBandSpectrum.powers != null) this.powers = multiBandSpectrum.powers.clone();
    }

    /**
     * Creates logarithmically spaced frequency bands.
     *
     * @param lowFrequency lower boundary in Hz
     * @param highFrequency upper boundary in Hz
     * @param numberOfBands number of bands in between lower and upper boundary
     * @return array of frequency boundaries in Hz
     */
    public static float[] createLogarithmicBands(final float lowFrequency, final float highFrequency, final int numberOfBands) {
        if (numberOfBands == 1) return new float[] {lowFrequency, highFrequency};
        // for now, avoid div by zero simply be throwing an exception
        if (lowFrequency == 0) throw new IllegalArgumentException();
        final double factor = numberOfBands / (Math.log(highFrequency / (double) lowFrequency) / LOG2);
        final float scale[] = new float[numberOfBands+1];
        scale[0] = lowFrequency;
        scale[scale.length-1] = highFrequency;
        for (int i=1; i<numberOfBands; i++) {
            scale[i] = lowFrequency * (float)Math.pow(2, i/factor);
        }
        return scale;
    }

    /**
     * Creates linearly spaced frequency bands.
     *
     * @param lowFrequency lower boundary in Hz
     * @param highFrequency upper boundary in Hz
     * @param numberOfBands number of bands in between lower and upper boundary
     * @return array of frequency boundaries in Hz
     */
    public static float[] createLinearBands(final float lowFrequency, final float highFrequency, final int numberOfBands) {
        if (numberOfBands == 1) return new float[] {lowFrequency, highFrequency};
        final float bandwidth = highFrequency - lowFrequency;
        final float scale[] = new float[numberOfBands+1];
        scale[0] = lowFrequency;
        scale[scale.length-1] = highFrequency;
        for (int i=1; i<numberOfBands; i++) {
            scale[i] = lowFrequency  + i * (bandwidth/numberOfBands);
        }
        return scale;
    }

    /**
     * Creates and array of frequency boundaries exactly +-50 cents to the given MIDI
     * notes.
     *
     * @param lowMidi low MIDI note
     * @param highMidi high MIDI note
     * @return frequency boundaries
     */
    public static float[] createMidiBands(final int lowMidi, final int highMidi) {
        return createMidiBands(lowMidi, highMidi, 1, 0);
    }

    /**
     * Creates an array of frequency boundaries by dividing each note into the given number of bins.
     *
     * @param lowMidi low MIDI note
     * @param highMidi high MIDI note
     * @param binsPerSemitone bins/semitone
     * @return frequency boundaries
     */
    public static float[] createMidiBands(final int lowMidi, final int highMidi, final int binsPerSemitone) {
        return createMidiBands(lowMidi, highMidi, binsPerSemitone, 0);
    }

    /**
     * Creates an array of frequency boundaries by dividing each note into the given number of bins
     * and allowing for a different than the standard 440Hz tuning specified in cents.
     *
     * @param binsPerSemitone bins/semitone
     * @param lowMidi low MIDI note (incl.)
     * @param highMidi high MIDI note (excl.)
     * @param centDeviation deviation in cent from concert pitch (A4=440Hz)
     * @return frequency boundaries
     */
    public static float[] createMidiBands(final int lowMidi, final int highMidi, final int binsPerSemitone, final float centDeviation) {
        final int semitones = highMidi - lowMidi;
        final float[] bands = new float[semitones*binsPerSemitone+1];
        for (int i=0; i<semitones; i++) {
            for (int bin = 0; bin<binsPerSemitone; bin++) {
                // subtract x cents
                final double cents = 100.0/binsPerSemitone*bin - 50.0 + centDeviation;
                bands[i*binsPerSemitone+bin] = midiToFrequency(lowMidi+i, cents);
            }
        }
        bands[bands.length-1] = midiToFrequency(highMidi-1, 50.0 + centDeviation);
        return bands;
    }

    private static float midiToFrequency(final int midiNote, final double cents) {
        final double freq = (Math.pow(2.0, (midiNote -69.0)/12.0) * 440.0);
        return (float) (freq*Math.pow(2.0, cents /1200.0));
    }

    private float[] computeBins(final float[] frequencies, final float[] values) {
        final int[] valuesPerBin = new int[numberOfBands+2];
        int currentEntry = 0;
        int bin = 0;
        for (final float freq : frequencies) {
            while (bin < bandBoundariesInHz.length && freq >= bandBoundariesInHz[bin]) {
                valuesPerBin[bin] = currentEntry;
                currentEntry = 0;
                bin++;
            }
            currentEntry++;
        }
        valuesPerBin[bin] = currentEntry;

        final float[] bins = new float[numberOfBands+2];
        int binIndex = 0;
        int offset = 0;
        for (final int length : valuesPerBin) {
            for (int i = 0; i < length; i++) {
                bins[binIndex] += values[i+offset];
            }
            offset += length;
            binIndex++;
        }
        // remove outOfBand bins
        final float[] inBandBins = new float[numberOfBands];
        System.arraycopy(bins, 1, inBandBins, 0, inBandBins.length);
        return inBandBins;
    }

    public MultiBandSpectrum derive(final float[] real, final float[] imaginary) {
        return new MultiBandSpectrum(getFrameNumber(), real, imaginary, getAudioFormat(), bandBoundariesInHz);
    }

    /**
     * Array with frequency values in Hz corresponding to the band boundaries.
     *
     * @return array of length numberOfBands+1
     * @see #getNumberOfBands()
     */
    public float[] getBandBoundaries() {
        return bandBoundariesInHz;
    }

    /**
     * Number of bands.
     *
     * @return number of bands
     * @see #getBandBoundaries()
     */
    public int getNumberOfBands() {
        return numberOfBands;
    }

    /**
     * Computes a bin for the given frequency. Returns -1, if the bin is not found.
     *
     * @param frequency frequency
     * @return bin
     */
    public int getBin(final float frequency) {
        for (int bin=0; bin<bandBoundariesInHz.length-1; bin++) {
            final float lastFreq = bandBoundariesInHz[bin];
            final float freq = bandBoundariesInHz[bin+1];
            if (frequency >= lastFreq && frequency < freq) return bin;
        }
        return -1;
    }

    /**
     * Array with center frequency values in Hz corresponding to the bin numbers.<br/>
     * Note that these values are simply the means of the upper and lower boundaries for each bin.
     * Due to the logarithmic nature of the <a href="http://en.wikipedia.org/wiki/Equal_temperament">equal temperament</a>,
     * if one bin spans from 440hz to 880Hz, i.e. an octave, the mean frequency is not the same as the center semitone.
     *
     * @return array of length numberOfSamples/2 due to symmetry
     * @see #getFrequency(int)
     */
    public float[] getFrequencies() {
        final float[] frequencies = new float[numberOfBands];
        for (int i=0; i<frequencies.length; i++) {
            frequencies[i] = (bandBoundariesInHz[i]+bandBoundariesInHz[i+1]) / 2f;
        }
        return frequencies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiBandSpectrum that = (MultiBandSpectrum) o;

        if (this.frameNumber != that.frameNumber) return false;
        if (!Arrays.equals(bandBoundariesInHz, that.bandBoundariesInHz)) return false;
        if (!Arrays.equals(imaginaryData, that.imaginaryData)) return false;
        if (!Arrays.equals(realData, that.realData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = realData != null ? Arrays.hashCode(realData) : 0;
        result = 31 * result + (imaginaryData != null ? Arrays.hashCode(imaginaryData) : 0);
        result = 31 * result + (bandBoundariesInHz != null ? Arrays.hashCode(bandBoundariesInHz) : 0);
        result = 31 * result + frameNumber;
        return result;
    }

    @Override
    public String toString() {
        return "MultiBandSpectrum{" +
                "timestamp=" + getTimestamp() +
                ", frameNumber=" + getFrameNumber() +
                ", bandBoundariesInHz=" + bandBoundariesInHz +
                ", numberOfBands=" + numberOfBands +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final MultiBandSpectrum clone = (MultiBandSpectrum)super.clone();
        clone.numberOfBands = numberOfBands;
        clone.bandBoundariesInHz = bandBoundariesInHz.clone();
        return clone;
    }
}
