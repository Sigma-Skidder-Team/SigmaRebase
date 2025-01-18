/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.Matrix;

import javax.sound.sampled.AudioFormat;
import java.util.concurrent.TimeUnit;

/**
 * {@link AudioMatrix} representing exclusively real values.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RealAudioMatrix implements AudioMatrix {

    private final Matrix realData;
    private AudioFormat audioFormat;
    private int frameNumber;

    public RealAudioMatrix(final int frameNumber, final Matrix realData, final AudioFormat audioFormat) {
        if (realData == null) throw new IllegalArgumentException("Data must not be null");
        this.frameNumber = frameNumber;
        this.audioFormat = audioFormat;
        this.realData = realData;
    }

    @Override
    public float getData(final int row, final int column) {
        return getRealData(row, column);
    }

    @Override
    public float getRealData(final int row, final int column) {
        return realData.get(row, column);
    }

    @Override
    public long getTimestamp() {
        return getTimestamp(TimeUnit.MILLISECONDS);
    }

    @Override
    public long getTimestamp(final TimeUnit timeUnit) {
        if (audioFormat == null) return -1;
        else return timeUnit.convert((long) (frameNumber * 1000l * 1000l * 1000l / (double)audioFormat.getSampleRate()), TimeUnit.NANOSECONDS);
    }

    @Override
    public int getFrameNumber() {
        return frameNumber;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public float[] getData() {
        return getRealData();
    }

    @Override
    public float[] getRealData() {
        if (realData.getNumberOfRows() == 0) return null;
        final float[] firstRow = new float[realData.getNumberOfColumns()];
        for (int column=0; column<firstRow.length; column++) {
            firstRow[column] = realData.get(0, column);
        }
        return firstRow;
    }

    @Override
    public float[] getImaginaryData() {
        return new float[getRealData().length];
    }

    @Override
    public float[] getPowers() {
        final float[] array = new float[realData.getNumberOfColumns()];
        for (int i = 0; i < array.length; i++) {
            final float v = realData.get(0, i);
            array[i] = v * v;
        }
        return array;
    }

    @Override
    public float[] getMagnitudes() {
        final float[] array = new float[realData.getNumberOfColumns()];
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.abs(realData.get(0, i));
        }
        return array;
    }

    @Override
    public int getNumberOfSamples() {
        return getNumberOfRows();
    }

    @Override
    public int getNumberOfColumns() {
        return realData.getNumberOfColumns();
    }

    @Override
    public int getNumberOfRows() {
        return realData.getNumberOfRows();
    }

    @Override
    public AudioBuffer derive(final float[] real, final float[] imaginary) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioMatrix derive(final Matrix real, final Matrix imaginary) {
        return new RealAudioMatrix(frameNumber, real, audioFormat);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RealAudioMatrix that = (RealAudioMatrix) o;

        if (frameNumber != that.frameNumber) return false;
        if (audioFormat != null ? !audioFormat.equals(that.audioFormat) : that.audioFormat != null) return false;
        if (!realData.equals(that.realData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = audioFormat != null ? audioFormat.hashCode() : 0;
        result = 31 * result + frameNumber;
        result = 31 * result + realData.hashCode();
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
        return "RealAudioMatrix{" +
                "audioFormat=" + audioFormat +
                ", timestamp=" + getTimestamp() +
                ", frameNumber=" + frameNumber +
                ", rows=" +  getNumberOfRows() +
                ", columns=" + getNumberOfColumns() +
                '}';
    }


}
