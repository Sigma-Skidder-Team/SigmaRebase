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
 * <p>Only lets one specified channel through.</p>
 * <p>This processor assumes interleaved order, i.e. for the stereo case
 * each Left sample is followed by a Right sample like this: <code>LRLRLRLR...</code></p>
 * <p>More than two channels are supported.</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see Mono
 */
public class Channel extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private float[] block;
    private RealAudioBuffer realAudioBuffer;
    private int channel;

    /**
     *
     * @param channel channel to let through (zero-based)
     */
    public Channel(final int channel) {
        this.channel = channel;
    }

    private AudioFormat getProcessedAudioFormat(final AudioBuffer buffer) {
        final AudioFormat sourceAudioFormat = buffer.getAudioFormat();
        return new AudioFormat(sourceAudioFormat.getSampleRate(),
                sourceAudioFormat.getSampleSizeInBits(), 1,
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    protected AudioBuffer processNext(final AudioBuffer buffer) {
        final int numberOfChannels = buffer.getAudioFormat().getChannels();
        if (numberOfChannels == 1) return buffer;
        if (numberOfChannels <= channel) throw new IllegalArgumentException("Buffer does not have a channel " + channel + ": " + buffer.getAudioFormat());
        final float[] floats = buffer.getData();
        final int framesToRead = floats.length / numberOfChannels;
        if (block == null || block.length != framesToRead) {
            block = new float[framesToRead];
        }
        for (int frame=0; frame<framesToRead; frame++) {
            final int sampleOffset = frame * numberOfChannels;
            block[frame] = floats[sampleOffset + channel];
        }
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer(buffer.getFrameNumber(), block, getProcessedAudioFormat(buffer));
        } else {
            realAudioBuffer.reuse(buffer.getFrameNumber(), block, realAudioBuffer.getAudioFormat());
        }
        return realAudioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Channel channel1 = (Channel) o;
        return channel == channel1.channel;
    }

    @Override
    public int hashCode() {
        return channel;
    }

    @Override
    public String toString() {
        return "Channel[" + channel + "]";
    }
}
