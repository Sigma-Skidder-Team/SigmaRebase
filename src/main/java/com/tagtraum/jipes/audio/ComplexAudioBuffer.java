/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Buffer that holds complex, time domain audio data and some meta information in the form of
 * an {@link AudioFormat} instance.
 * <p/>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.SignalPullProcessor
 * @see com.tagtraum.jipes.SignalProcessor
 * @see RealAudioBuffer
 */
public class ComplexAudioBuffer implements AudioBuffer, Cloneable {

    private AudioFormat audioFormat;
    private float[] realData;
    private float[] imaginaryData;
    private float[] powers;
    private float[] magnitudes;
    private int frameNumber;

    public ComplexAudioBuffer(final int frameNumber, final float[] realData, final float[] imaginaryData, final AudioFormat audioFormat) {
        if (realData == null) throw new IllegalArgumentException("Real data must not be null");
        if (imaginaryData == null) throw new IllegalArgumentException("Imaginary data must not be null");
        if (realData.length != imaginaryData.length) throw new IllegalArgumentException("Length of real and imaginary data must be equal");
        this.audioFormat = audioFormat;
        this.realData = realData;
        this.imaginaryData = imaginaryData;
        this.frameNumber = frameNumber;
    }

    /**
     * Copy constructor that creates a deep copy of the data, but a shallow (reference) copy of the format.
     *
     * @param buffer buffer to copy
     */
    public ComplexAudioBuffer(final ComplexAudioBuffer buffer) {
        this.realData = buffer.realData.clone();
        this.imaginaryData = buffer.imaginaryData.clone();
        this.audioFormat = buffer.audioFormat;
        this.frameNumber = buffer.frameNumber;
    }

    /**
     * Refills this buffer with new data. This allows some object reuse. <em>Use with care!</em>
     *
     * @param frameNumber frameNumber
     * @param realData realData
     * @param imaginaryData imaginaryData
     * @param audioFormat audioFormat
     */
    public void reuse(final int frameNumber, final float[] realData, final float[] imaginaryData, final AudioFormat audioFormat) {
        this.frameNumber = frameNumber;
        this.realData = realData;
        this.imaginaryData = imaginaryData;
        this.audioFormat = audioFormat;
    }

    public long getTimestamp() {
        return getTimestamp(TimeUnit.MILLISECONDS);
    }

    public long getTimestamp(final TimeUnit timeUnit) {
        if (audioFormat == null) return -1;
        else return timeUnit.convert((long) (frameNumber * 1000l * 1000l * 1000l / (double)audioFormat.getSampleRate()), TimeUnit.NANOSECONDS);
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * This is the same as {@link #getMagnitudes()}.
     *
     * @return magnitudes
     */
    public float[] getData() {
        return getMagnitudes();
    }

    /**
     * Real part of this buffer's data. Typically, time domain, real-only data.
     *
     * @return real part
     */
    public float[] getRealData() {
        return realData;
    }

    /**
     * Imaginary part of this buffer's data.
     *
     * @return imaginary part
     */
    public synchronized float[] getImaginaryData() {
        return imaginaryData;
    }

    /**
     * Powers for this buffer - since this a real-only buffer, this is equivalent to
     * <code>square(getRealData())</code>.
     *
     * @return squares of all real values
     */
    public synchronized float[] getPowers() {
        if (powers == null) {
            powers = computePowers(getNumberOfSamples());
        }
        return powers;
    }

    /**
     * Magnitudes for this buffer - since this is a real-only buffer, this is equivalent to
     * <code>abs(getRealData())</code>.
     *
     * @return absolute data values
     */
    public synchronized float[] getMagnitudes() {
        if (magnitudes == null) {
            magnitudes = computeMagnitudes(getNumberOfSamples(), powers);
        }
        return magnitudes;
    }

    public int getNumberOfSamples() {
        return realData.length;
    }

    protected float[] computePowers(final int length) {
        final float[] powers = new float[length];
        for (int i=0; i<length; i++) {
            final float r = realData[i];
            final float j = imaginaryData[i];
            powers[i] = (float)power(r, j);
        }
        return powers;
    }

    protected float[] computeMagnitudes(final int length, final float[] powers) {
        final float[] magnitudes = new float[length];
        if (powers == null || powers.length != length) {
            for (int i=0; i<length; i++) {
                if (imaginaryData[i] == 0) magnitudes[i] = Math.abs(realData[i]);
                else magnitudes[i] = (float)magnitude(power(realData[i], imaginaryData[i]));
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

    public ComplexAudioBuffer derive(final float[] real, final float[] imaginary) {
        return new ComplexAudioBuffer(frameNumber, real, imaginary, audioFormat);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplexAudioBuffer that = (ComplexAudioBuffer) o;

        if (frameNumber != that.frameNumber) return false;
        if (audioFormat != null ? !audioFormat.equals(that.audioFormat) : that.audioFormat != null) return false;
        if (!Arrays.equals(realData, that.realData)) return false;
        if (!Arrays.equals(imaginaryData, that.imaginaryData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = audioFormat != null ? audioFormat.hashCode() : 0;
        result = 31 * result + (realData != null ? Arrays.hashCode(realData) : 0);
        result = 31 * result + (imaginaryData != null ? Arrays.hashCode(imaginaryData) : 0);
        result = 31 * result + frameNumber;
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final ComplexAudioBuffer clone = (ComplexAudioBuffer)super.clone();
        clone.realData = realData.clone();
        return clone;
    }

    @Override
    public String toString() {
        return "ComplexAudioBuffer{" +
                "audioFormat=" + audioFormat +
                ", timestamp=" + getTimestamp() +
                ", frameNumber=" + frameNumber +
                ", realData=" + realData +
                ", imaginaryData=" + imaginaryData +
                '}';
    }

}
