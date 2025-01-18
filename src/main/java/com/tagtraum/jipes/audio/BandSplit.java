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
 * Takes the real values of a sequence of input spectra and creates new buffers for each frequency bin.
 * These new buffers are then passed on to band-specific children. In other words, a number of real values
 * with the index {@code 0} are collected in one buffer and then sent to the child processor for band/channel 0.
 * The same happens with magnitudes at index {@code 1} and so on.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class BandSplit<T extends AudioSpectrum> implements SignalSplit<T, AudioBuffer> {

    private SignalProcessorSupport<AudioBuffer> signalProcessorSupport = new SignalProcessorSupport<AudioBuffer>();

    private AudioFormat audioFormat;
    private final int windowLength;
    private float[][] bands;

    private int frame;
    private int firstFrameNumber;
    private boolean flushed;

    /**
     * @param windowLength the number of samples/magnitudes collected in each band to form the new (per) band buffer
     */
    public BandSplit(final int windowLength) {
        this.windowLength = windowLength;
    }

    public void process(final T buffer) throws IOException {
        fillBands(buffer);
        flushed = false;
        frame++;
        if (frame % windowLength == 0) {
            flush();
        }
    }

    public void flush() throws IOException {
        if (flushed) return;
        flushed = true;
        topOffBands();
        final AudioBuffer[] buffers = createAudioBuffers();
        this.signalProcessorSupport.process(buffers);
        signalProcessorSupport.flush();
    }

    /**
     * Top off with zeroes, if necessary.
     */
    private void topOffBands() {
        for (; frame % windowLength != 0; frame++) {
            for (int band=0; band<bands.length; band++) {
                bands[band][(frame % windowLength)] = 0;
            }
        }
    }

    private void fillBands(final T buffer) {
        if (frame == 0) {
            firstFrameNumber = buffer.getFrameNumber();
        } else if (frame == 1) {
            final AudioFormat inputFormat = buffer.getAudioFormat();
            final float newSampleRate = inputFormat.getSampleRate() / (buffer.getFrameNumber() - firstFrameNumber);
            audioFormat = new AudioFormat(
                    inputFormat.getEncoding(), newSampleRate, inputFormat.getSampleSizeInBits(),
                    inputFormat.getChannels(), inputFormat.getFrameSize(), newSampleRate,
                    inputFormat.isBigEndian()
            );
        }
        final float[] realData = buffer.getRealData();
        if (bands == null) {
            bands = new float[realData.length][windowLength];
        }
        for (int band=0; band<bands.length; band++) {
            bands[band][(frame % windowLength)] = realData[band];
        }
    }

    private AudioBuffer[] createAudioBuffers() {
        final AudioBuffer[] buffers = new AudioBuffer[bands.length];
        for (int band=0; band<bands.length; band++) {
            final float[] samples = bands[band];
            buffers[band] = new RealAudioBuffer(frame - windowLength, samples, audioFormat);
        }
        return buffers;
    }

    public AudioBuffer getOutput() throws IOException {
        return null;
    }

    public Object getId() {
        return toString();
    }

    public int getChannelCount() {
        return signalProcessorSupport.getChannelCount();
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
        return "BandSplit{" +
                "windowLength=" + windowLength +
                '}';
    }
}
