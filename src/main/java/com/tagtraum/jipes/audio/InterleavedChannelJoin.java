/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.AggregateFunction;
import com.tagtraum.jipes.universal.Join;

import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * Joins multichannel audio data in an interleaved format ({@code LRLRLR...}).
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see InterleavedChannelSplit
 */
public class InterleavedChannelJoin extends Join<AudioBuffer, AudioBuffer> {

    public InterleavedChannelJoin(final int channels) {
        this(channels, null);
    }

    public InterleavedChannelJoin(final int channels, final Object id) {
        super(channels, new InterleavedChannelAggregateFunction(), id);
    }

    private static class InterleavedChannelAggregateFunction implements AggregateFunction<List<AudioBuffer>, AudioBuffer> {

        private AudioFormat audioFormat;

        public AudioBuffer aggregate(final List<AudioBuffer> collection) {
            final AudioBuffer firstAudioBuffer = collection.get(0);
            final int channels = collection.size();
            if (audioFormat == null) {
                audioFormat = new AudioFormat(
                    firstAudioBuffer.getAudioFormat().getEncoding(),
                    firstAudioBuffer.getAudioFormat().getSampleRate(),
                    firstAudioBuffer.getAudioFormat().getSampleSizeInBits(),
                    channels,
                    firstAudioBuffer.getAudioFormat().getFrameSize() * channels,
                    firstAudioBuffer.getAudioFormat().getFrameRate(),
                    firstAudioBuffer.getAudioFormat().isBigEndian()
                    );
            }

            final float[] real = new float[firstAudioBuffer.getRealData().length*channels];
            for (int i=0; i< channels; i++) {
                final AudioBuffer buffer = collection.get(i);
                final float[] data = buffer.getRealData();
                for (int j=0; j< data.length; j++) {
                    real[j*channels+i] = data[j];
                }
            }
            return new RealAudioBuffer(firstAudioBuffer.getFrameNumber(), real, audioFormat);
        }

    }

    @Override
    public String toString() {
        return "InterleavedChannelJoin{" +
                "parts=" + getPartsPerUnit() + "}";
    }

}
