/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import javax.sound.sampled.AudioFormat;
import java.util.concurrent.TimeUnit;

/**
 * Skeleton implementation of an {@link AudioSpectrum}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class AbstractAudioSpectrum implements AudioSpectrum, Cloneable {

    protected float[] realData;
    protected float[] imaginaryData;
    protected int frameNumber;
    protected AudioFormat audioFormat;
    protected float[] magnitudes;
    protected float[] powers;

    public AbstractAudioSpectrum(final int frameNumber, final float[] realData, final float[] imaginaryData, final AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        this.imaginaryData = imaginaryData;
        this.realData = realData;
        this.frameNumber = frameNumber;
        if (imaginaryData != null && realData.length != imaginaryData.length) {
            throw new IllegalArgumentException("If imaginary data is given, it must have the same length as the real data: r.length=" + realData.length + ", i.length=" + imaginaryData.length);
        }
    }

    /**
     * Copy constructor for creating exact copies of a spectrum, if you know, what class it is.
     * If you don't know the exact class you might prefer {@link #clone()}.
     *
     * @param abstractAudioSpectrum spectrum to copy
     */
    protected AbstractAudioSpectrum(final AbstractAudioSpectrum abstractAudioSpectrum) {
        this.audioFormat = abstractAudioSpectrum.audioFormat;
        this.frameNumber = abstractAudioSpectrum.frameNumber;
        if (abstractAudioSpectrum.realData != null) this.realData = abstractAudioSpectrum.realData.clone();
        if (abstractAudioSpectrum.imaginaryData != null) this.imaginaryData = abstractAudioSpectrum.imaginaryData.clone();
        if (abstractAudioSpectrum.powers != null) this.powers = abstractAudioSpectrum.powers.clone();
        if (abstractAudioSpectrum.magnitudes != null) this.magnitudes = abstractAudioSpectrum.magnitudes.clone();
    }

    /**
     * Refills this spectrum with new data. This allows some object reuse. <em>Use with care!</em>
     *
     * @param frameNumber frame number
     * @param realData real data
     * @param imaginaryData imaginary data
     * @param audioFormat audio format
     */
    protected void reuse(final int frameNumber, final float[] realData, final float[] imaginaryData, final AudioFormat audioFormat) {
        this.frameNumber = frameNumber;
        this.realData = realData;
        this.imaginaryData = imaginaryData;
        this.audioFormat = audioFormat;
        this.magnitudes = null;
        this.powers = null;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public long getTimestamp() {
        return getTimestamp(TimeUnit.MILLISECONDS);
    }

    public long getTimestamp(final TimeUnit timeUnit) {
        if (audioFormat == null) return -1;
        else return timeUnit.convert((long) (frameNumber * 1000l * 1000l * 1000l / (double) audioFormat.getSampleRate()), TimeUnit.NANOSECONDS);
    }

    /**
     * Computes magnitude based on power.
     *
     * @param power the power of a signal
     * @return magnitude
     * @see #power(float, float)
     */
    private static double magnitude(final double power) {
        return Math.sqrt(power);
    }

    /**
     * Computes power based on a complex number.
     *
     * @param r real part
     * @param i imaginary part
     * @return power
     */
    private static double power(final float r, final float i) {
        return square(r) + square(i);
    }

    /**
     * Simple square of a number.
     *
     * @param f float
     * @return f*f
     */
    private static double square(final double f) {
        return f*f;
    }

    /**
     * AudioFormat of the data this spectrum was generated from.
     *
     * @return audio formal
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Real part of the spectrum (amplitudes).
     *
     * @return real part
     */
    public float[] getRealData() {
        return realData;
    }

    /**
     * Imaginary part of the spectrum (phase).
     *
     * @return imaginary part
     */
    public float[] getImaginaryData() {
        return imaginaryData;
    }

    /**
     * Synonymous with {@link #getMagnitudes()}
     *
     * @return magnitudes of this spectrum
     */
    public float[] getData() {
        return getMagnitudes();
    }

    /**
     * Frequency represented by bin/index <code>x</code>.
     *
     * @param bin bin number
     * @return frequency
     */
    public float getFrequency(final int bin) {
        final float[] freqs = getFrequencies();
        if (bin >= freqs.length) return 0;
        return freqs[bin];
    }

    /**
     * Number of samples.
     *
     * @return number of samples, usually <code>{@link #getRealData()}.length</code>.
     */
    public int getNumberOfSamples() {
        return getRealData().length;
    }

    protected float[] computePowers(final int length) {
        final float[] powers = new float[length];
        for (int i=0; i<length; i++) {
            final float r = realData[i];
            final float j = imaginaryData == null ? 0 : imaginaryData[i];
            powers[i] = (float)power(r, j);
        }
        return powers;
    }

    protected float[] computeMagnitudes(final int length, final float[] powers) {
        final float[] magnitudes = new float[length];
        if (powers == null || powers.length != length) {
            if (imaginaryData == null) {
                for (int i=0; i<length; i++) {
                    magnitudes[i] = Math.abs(realData[i]);
                }
            } else {
                for (int i=0; i<length; i++) {
                    if (imaginaryData[i] == 0) magnitudes[i] = Math.abs(realData[i]);
                    else magnitudes[i] = (float)magnitude(power(realData[i], imaginaryData[i]));
                }
            }
        } else {
            for (int i=0; i<length; i++) {
                if (imaginaryData[i] == 0) magnitudes[i] = Math.abs(realData[i]);
                else magnitudes[i] = (float)magnitude(powers[i]);
            }
        }
        return magnitudes;
    }

    /**
     * Powers (sum of the squares of the real and imaginary part) of the spectrum.
     *
     * @return array of length numberOfSamples
     * @see #getMagnitudes()
     */
    public synchronized float[] getPowers() {
        if (powers == null) {
            powers = computePowers(getNumberOfSamples());
        }
        return powers;
    }

    /**
     * Magnitudes (square root of the powers) of the spectrum.
     *
     * @return array of length numberOfSamples
     * @see #getPowers()
     */
    public synchronized float[] getMagnitudes() {
        if (magnitudes == null) {
            magnitudes = computeMagnitudes(getNumberOfSamples(), powers);
        }
        return magnitudes;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final AbstractAudioSpectrum clone = (AbstractAudioSpectrum)super.clone();
        if (realData != null) clone.realData = realData.clone();
        if (imaginaryData != null) clone.imaginaryData = imaginaryData.clone();
        if (powers != null) clone.powers = powers.clone();
        if (magnitudes != null) clone.magnitudes = magnitudes.clone();
        return clone;
    }

}
