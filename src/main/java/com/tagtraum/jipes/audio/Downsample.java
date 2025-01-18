/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;

import javax.sound.sampled.AudioFormat;

/**
 * Drops samples in order to downsample the data. Note that proper downsampling (i.e. decimating) has to be
 * preceded by the application of a suitable low pass filter (see {@link com.tagtraum.jipes.math.Filters}
 * and {@link com.tagtraum.jipes.universal.Mapping}) to avoid aliasing.
 * <p/>
 * Date: Jul 22, 2010
 * Time: 2:38:54 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see Decimate
 */
public class Downsample extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private int nthFrameToKeep = 2;
    private float[] output;
    private RealAudioBuffer realAudioBuffer;

    /**
     * Keeps every nth frame, drops all other frames.
     *
     * @param nthFrameToKeep nth frame to keep
     */
    public Downsample(final int nthFrameToKeep) {
        this.nthFrameToKeep = nthFrameToKeep;
    }

    /**
     * Creates a downsample processor that drops/keeps every second frame.
     */
    public Downsample() {
        this(2);
    }

    /**
     * @see #setNthFrameToKeep(int)
     * @return frame to keep
     */
    public int getNthFrameToKeep() {
        return nthFrameToKeep;
    }

    /**
     * Every <code>nthFrameToKeep</code>th frame will be kept.
     * So if you specify 2, every 2nd frame will be kept.
     * If you specify 3, every third frame will be kept - leading to a sample rate reduction of,
     * e.g. 44100 to 44100/3 = 14700.
     *
     * @param nthFrameToKeep frame to keep
     */
    public void setNthFrameToKeep(final int nthFrameToKeep) {
        if (nthFrameToKeep < 1) throw new IllegalArgumentException("nthFrameToKeep must be greater than 0: " + nthFrameToKeep);
        this.nthFrameToKeep = nthFrameToKeep;
    }

    private AudioFormat getProcessedAudioFormat(final AudioBuffer buffer) {
        final AudioFormat sourceAudioFormat = buffer.getAudioFormat();
        return new AudioFormat(sourceAudioFormat.getSampleRate()/ nthFrameToKeep,
                sourceAudioFormat.getSampleSizeInBits(),
                sourceAudioFormat.getChannels(),
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) {
        final float[] floats = buffer.getData();
        final int outputLength = floats.length / nthFrameToKeep;
        if (output == null || outputLength != output.length) {
            output = new float[outputLength];
        }
        final int channels = buffer.getAudioFormat().getChannels();
        for (int i=0; i/nthFrameToKeep<output.length; i=i+nthFrameToKeep*channels) {
            try {
                System.arraycopy(floats, i, output, i / nthFrameToKeep, channels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer(buffer.getFrameNumber()/nthFrameToKeep, output, getProcessedAudioFormat(buffer));
        } else {
            realAudioBuffer.reuse(buffer.getFrameNumber()/nthFrameToKeep, output, realAudioBuffer.getAudioFormat());
        }
        return realAudioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Downsample that = (Downsample) o;

        if (nthFrameToKeep != that.nthFrameToKeep) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nthFrameToKeep;
    }

    @Override
    public String toString() {
        return "Downsample{" +
                "nthFrameToKeep=" + nthFrameToKeep +
                '}';
    }
}