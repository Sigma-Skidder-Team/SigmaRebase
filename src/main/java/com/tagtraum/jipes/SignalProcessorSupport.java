/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for implementing {@link com.tagtraum.jipes.SignalProcessor}s.
 *
 * @param <IO> type of the output values of the corresponding {@link SignalProcessor}
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SignalProcessorSupport<IO> {

    private List<SignalProcessor<IO, ?>>[] channelAudioPushProcessors = new List[] {new ArrayList<SignalProcessor<IO, ?>>()};

    /**
     * Registers a child processor.
     *
     * @param signalProcessor processor
     * @see #disconnectFrom(com.tagtraum.jipes.SignalProcessor)
     * @see #getConnectedProcessors()
     * @return the just added processor
     */
    public <O2> SignalProcessor<IO, O2> connectTo(final SignalProcessor<IO, O2> signalProcessor) {
        connectTo(0, signalProcessor);
        return signalProcessor;
    }

    /**
     * Registers a child processor for a specific channel.
     *
     * @param <O2> output type of the processor we want to connect to
     * @param channel channel or band
     * @param signalProcessor processor
     * @see #disconnectFrom(com.tagtraum.jipes.SignalProcessor)
     * @see #getConnectedProcessors()
     * @return the just added processor
     */
    public <O2> SignalProcessor<IO, O2> connectTo(final int channel, final SignalProcessor<IO, O2> signalProcessor) {
        if (channel >= channelAudioPushProcessors.length) {
            expandChannelAudioPushProcessors(channel+1);
        }
        channelAudioPushProcessors[channel].remove(signalProcessor);
        channelAudioPushProcessors[channel].add(signalProcessor);
        return signalProcessor;
    }

    /**
     * Removes a child processor.
     *
     * @param <O> output type of the processor we want to disconnect from
     * @param signalProcessor processor
     * @return the processor, if it was indeed removed
     * @see #connectTo(com.tagtraum.jipes.SignalProcessor)
     * @see #getConnectedProcessors()
     */
    public <O> SignalProcessor<IO, O> disconnectFrom(final SignalProcessor<IO, O> signalProcessor) {
        return disconnectFrom(0, signalProcessor);
    }

    /**
     * Removes a child processor.
     *
     * @param <O2> output type of the processor we want to disconnect from
     * @param channel channel or band
     * @param signalProcessor processor
     * @return the processor, if it was indeed removed
     * @see #connectTo(com.tagtraum.jipes.SignalProcessor)
     * @see #getConnectedProcessors()
     */
    public <O2> SignalProcessor<IO, O2> disconnectFrom(final int channel, final SignalProcessor<IO, O2> signalProcessor) {
        final List<SignalProcessor<IO, ?>> channelSignalProcessors = getChannelAudioPushProcessors(channel);
        final boolean success = channelSignalProcessors.remove(signalProcessor);
        return success ? signalProcessor : null;
    }

    /**
     * Lists all registered processors.
     *
     * @return array of registered processors, never <code>null</code>
     * @see #disconnectFrom(com.tagtraum.jipes.SignalProcessor)
     * @see #connectTo(com.tagtraum.jipes.SignalProcessor)
     */
    public SignalProcessor<IO, ?>[] getConnectedProcessors() {
        return getConnectedProcessors(0);
    }

    /**
     * Lists all registered processors.
     *
     * @param channel channel or band
     * @return array of registered processors, never <code>null</code>
     * @see #disconnectFrom(com.tagtraum.jipes.SignalProcessor)
     * @see #connectTo(com.tagtraum.jipes.SignalProcessor)
     */
    public SignalProcessor<IO, ?>[] getConnectedProcessors(final int channel) {
        final List<SignalProcessor<IO,?>> signalProcessors = getChannelAudioPushProcessors(channel);
        return signalProcessors.toArray(new SignalProcessor[signalProcessors.size()]);
    }


    /**
     * Propagate the output <code>i</code> to all children by calling their
     * {@link com.tagtraum.jipes.SignalProcessor#process(Object)} method.
     *
     * @param i output
     * @throws IOException if something goes wrong
     */
    public void process(final IO i) throws IOException {
        process(0, i);
    }

    /**
     * Propagate the signal to all children by calling their
     * {@link com.tagtraum.jipes.SignalProcessor#process(Object)} method.
     *
     * @param signal array of signals for all channels
     * @throws IOException if something goes wrong
     */
    public void process(final IO[] signal) throws IOException {
        for (int i=0; i<signal.length; i++) {
            process(i, signal[i]);
        }
    }

    /**
     * Propagate the output <code>i</code> to all children by calling their
     * {@link com.tagtraum.jipes.SignalProcessor#process(Object)} method.
     *
     * @param channel channel or band
     * @param i output
     * @throws IOException if something goes wrong
     */
    public void process(final int channel, final IO i) throws IOException {
        final List<SignalProcessor<IO, ?>> processors = getChannelAudioPushProcessors(channel);
        for (int j=0, max = processors.size(); j<max; j++) {
            processors.get(j).process(i);
        }
    }

    /**
     * Flush all kids.
     *
     * @throws IOException if something goes wrong
     */
    public void flush() throws IOException {
        for (int i=0; i<getChannelCount(); i++) {
            final List<SignalProcessor<IO, ?>> processors = getChannelAudioPushProcessors(i);
            for (int j=0, max = processors.size(); j<max; j++) {
                processors.get(j).flush();
            }
        }
    }

    /**
     * Number of the channel with the highest channel number which also has children.
     *
     * @return channel number
     */
    public int getChannelCount() {
        for (int i=channelAudioPushProcessors.length; i>0; i--) {
            if (!channelAudioPushProcessors[i-1].isEmpty()) return i;
        }
        return 0;
    }

    private List<SignalProcessor<IO,?>> getChannelAudioPushProcessors(final int channel) {
        if (channelAudioPushProcessors.length > channel) {
            return channelAudioPushProcessors[channel];
        } else {
            return new ArrayList<SignalProcessor<IO, ?>>();
        }
    }

    private void expandChannelAudioPushProcessors(final int newSize) {
        final int oldSize = channelAudioPushProcessors.length;
        final List[] newChannelAudioPushProcessors = new List[newSize];
        System.arraycopy(channelAudioPushProcessors, 0, newChannelAudioPushProcessors, 0, channelAudioPushProcessors.length);
        channelAudioPushProcessors = newChannelAudioPushProcessors;
        for (int i=oldSize; i<newSize; i++) {
            channelAudioPushProcessors[i] = new ArrayList<SignalProcessor<IO, ?>>();
        }
    }

}
