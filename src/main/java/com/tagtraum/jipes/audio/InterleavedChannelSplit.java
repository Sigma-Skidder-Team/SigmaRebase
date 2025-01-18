/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import com.tagtraum.jipes.SignalSplit;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

/**
 * Splits {@link AudioBuffer}s containing interleaved signals (for example {@code LRLRLR}) for multiple channels into
 * separate buffers containing the signal for one channel each.
 * To further process signals for a specific channel, use the method
 * {@link com.tagtraum.jipes.SignalSplit#connectTo(int, com.tagtraum.jipes.SignalProcessor)}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see Mono
 * @see AudioSignalSource
 * @see BandSplit
 * @see InterleavedChannelJoin
 */
public class InterleavedChannelSplit implements SignalSplit<AudioBuffer, AudioBuffer> {

    private SignalProcessorSupport<AudioBuffer> signalProcessorSupport = new SignalProcessorSupport<AudioBuffer>();

    private AudioFormat audioFormat;
    private RealAudioBuffer[] channelBuffers;
    private float[][] channels;


    public void process(final AudioBuffer buffer) throws IOException {
        if (audioFormat == null) {
            final AudioFormat inputFormat = buffer.getAudioFormat();
            audioFormat = new AudioFormat(
                    inputFormat.getEncoding(), inputFormat.getSampleRate(),
                    inputFormat.getSampleSizeInBits(),
                    1, inputFormat.getFrameSize(), inputFormat.getFrameRate(),
                    inputFormat.isBigEndian()
            );
        }
        final int numberOfSamples = buffer.getNumberOfSamples();
        final int numberOfChannels = buffer.getAudioFormat().getChannels();
        final int samplesPerChannel = numberOfSamples / numberOfChannels;

        if (channels == null || numberOfChannels != channels.length || samplesPerChannel != channels[0].length) {
            channels = new float[numberOfChannels][samplesPerChannel];
        }
        if (channelBuffers == null || numberOfChannels != channels.length ) {
            channelBuffers = new RealAudioBuffer[numberOfChannels];
        }

        // fill channels
        for (int sample=0, frame=0; sample<numberOfSamples; sample++) {
            final int channel = sample % channels.length;
            channels[channel][frame] = buffer.getRealData()[channel + frame*numberOfChannels];
            if (channel+1 == channels.length) frame++;
        }

        // fill channelBuffers
        for (int channel=0; channel<channels.length; channel++) {
            final float[] samples = channels[channel];
            if (channelBuffers[channel] == null) {
                channelBuffers[channel] = new RealAudioBuffer(buffer.getFrameNumber(), samples, audioFormat);
            } else {
                final RealAudioBuffer realAudioBuffer = channelBuffers[channel];
                realAudioBuffer.reuse(buffer.getFrameNumber(), samples, realAudioBuffer.getAudioFormat());
            }

        }
        this.signalProcessorSupport.process(channelBuffers);
    }

    public void flush() throws IOException {
        signalProcessorSupport.flush();
    }

    public AudioBuffer getOutput() throws IOException {
        return null;
    }

    public Object getId() {
        return toString();
    }

    public <O2> SignalProcessor<AudioBuffer, O2> connectTo(final int channel, final SignalProcessor<AudioBuffer, O2> audioBufferSignalProcessor) {
        return signalProcessorSupport.connectTo(channel, audioBufferSignalProcessor);
    }

    public <O2> SignalProcessor<AudioBuffer, O2> disconnectFrom(final int channel, final SignalProcessor<AudioBuffer, O2> audioBufferSignalProcessor) {
        return signalProcessorSupport.disconnectFrom(channel, audioBufferSignalProcessor);
    }

    public SignalProcessor<AudioBuffer, ?>[] getConnectedProcessors(final int channel) {
        return signalProcessorSupport.getConnectedProcessors(channel);
    }

    public int getChannelCount() {
        return signalProcessorSupport.getChannelCount();
    }

    public <O2> SignalProcessor<AudioBuffer, O2> connectTo(final SignalProcessor<AudioBuffer, O2> audioBufferSignalProcessor) {
        return signalProcessorSupport.connectTo(audioBufferSignalProcessor);
    }

    public <O2> SignalProcessor<AudioBuffer, O2> disconnectFrom(final SignalProcessor<AudioBuffer, O2> audioBufferSignalProcessor) {
        return signalProcessorSupport.disconnectFrom(audioBufferSignalProcessor);
    }

    public SignalProcessor<AudioBuffer, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    @Override
    public String toString() {
        return "InterleavedChannelSplit";
    }
}
