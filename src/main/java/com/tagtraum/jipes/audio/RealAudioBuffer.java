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
 * Buffer that holds real, time domain audio data and some meta information in the form of
 * an {@link javax.sound.sampled.AudioFormat} instance. The imaginary part of the represented data
 * is always zero.
 * <p/>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.SignalPullProcessor
 * @see com.tagtraum.jipes.SignalProcessor
 * @see ComplexAudioBuffer
 */
public class RealAudioBuffer implements AudioBuffer, Cloneable {

    private AudioFormat audioFormat;
    private float[] realData;
    private float[] imaginaryData;
    private int frameNumber;
    private static float[] cachedImaginaryData;

    public RealAudioBuffer(final int frameNumber, final float[] realData, final AudioFormat audioFormat) {
        if (realData == null) throw new IllegalArgumentException("Data must not be null");
        this.audioFormat = audioFormat;
        this.realData = realData;
        this.frameNumber = frameNumber;
    }

    /**
     * Copy constructor that creates a deep copy of the data, but a shallow (reference) copy of the format.
     *
     * @param buffer buffer to copy
     */
    public RealAudioBuffer(final RealAudioBuffer buffer) {
        this.realData = buffer.realData.clone();
        this.audioFormat = buffer.audioFormat;
        this.frameNumber = buffer.frameNumber;
    }

    /**
     * Refills this buffer with new data. This allows some object reuse. <em>Use with care!</em>
     *
     * @param frameNumber frameNumber
     * @param realData realData
     * @param audioFormat audioFormat
     */
    public void reuse(final int frameNumber, final float[] realData, final AudioFormat audioFormat) {
        this.frameNumber = frameNumber;
        this.realData = realData;
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
     * Because this is a real-only buffer, the result is the same as calling {@link #getRealData()}.
     * Note that this is different from calling {@link #getMagnitudes()} in that the result contains positive
     * and negative numbers, while the magnitudes are positive-only.
     *
     * @return real data
     */
    public float[] getData() {
        return realData;
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
     * Imaginary part of the data in this buffer. Since this buffer is real-only,
     * this returns an array of the same size as the real part, but with only zeros as data.
     *
     * @return zero initialized array of the same length as the real part
     */
    public synchronized float[] getImaginaryData() {
        if (imaginaryData == null) {
            if (cachedImaginaryData != null && cachedImaginaryData.length == realData.length) {
                imaginaryData = cachedImaginaryData;
            }
            else {
                imaginaryData = new float[realData.length];
                if (imaginaryData.length < 1024*64) cachedImaginaryData = imaginaryData;
                else cachedImaginaryData = null;
            }
        }
        return imaginaryData;
    }

    /**
     * Powers for this buffer - since this a real-only buffer, this is equivalent to
     * <code>square(getRealData())</code>.
     *
     * @return squares of all real values
     */
    public float[] getPowers() {
        if (realData == null) return null;
        final float[] array = new float[realData.length];
        for (int i = 0; i < array.length; i++) {
            final float v = realData[i];
            array[i] = v * v;
        }
        return array;
    }

    /**
     * Magnitudes for this buffer - since this is a real-only buffer, this is equivalent to
     * <code>abs(getRealData())</code>.
     *
     * @return absolute data values
     */
    public float[] getMagnitudes() {
        if (realData == null) return null;
        final float[] array = new float[realData.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.abs(realData[i]);
        }
        return array;
    }

    public int getNumberOfSamples() {
        return realData.length;
    }

    public RealAudioBuffer derive(final float[] real, final float[] imaginary) {
        return new RealAudioBuffer(frameNumber, real, audioFormat);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealAudioBuffer that = (RealAudioBuffer) o;

        if (frameNumber != that.frameNumber) return false;
        if (audioFormat != null ? !audioFormat.equals(that.audioFormat) : that.audioFormat != null) return false;
        if (!Arrays.equals(realData, that.realData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = audioFormat != null ? audioFormat.hashCode() : 0;
        result = 31 * result + (realData != null ? Arrays.hashCode(realData) : 0);
        result = 31 * result + frameNumber;
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final RealAudioBuffer clone = (RealAudioBuffer)super.clone();
        clone.realData = realData.clone();
        return clone;
    }

    @Override
    public String toString() {
        return "RealAudioBuffer{" +
                "audioFormat=" + audioFormat +
                ", timestamp=" + getTimestamp() +
                ", frameNumber=" + frameNumber +
                ", data=" + realData +
                '}';
    }

}
