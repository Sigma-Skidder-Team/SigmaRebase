/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

/**
 * <p>{@link com.tagtraum.jipes.SignalProcessor} that splits the signal into multiple signals and as such has
 * multiple children - per channel.
 * This can be useful e.g. to split up a stereo signal into two distinct pipelines.
 * </p>
 * <p>In order to simply duplicate the signal, without actually splitting it into different parts,
 * use {@link NoopSignalProcessor}.
 * </p>
 *
 * @param <I> type of the input values
 * @param <O> type of the output values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.universal.Join
 * @see com.tagtraum.jipes.NoopSignalProcessor
 */
public interface SignalSplit<I, O> extends SignalProcessor<I, O> {

    /**
     * Registers a child processor for a specific channel.
     *
     * @param <O2> output type of the processor we want to connect to
     * @param channel channel
     * @param signalProcessor processor
     * @return the just added processor
     * @see #disconnectFrom(com.tagtraum.jipes.SignalProcessor)
     * @see #getConnectedProcessors()
     */
    <O2> SignalProcessor<O, O2> connectTo(int channel, SignalProcessor<O, O2> signalProcessor);

    /**
     * Removes a child processor from a specific channel.
     *
     * @param <O2> output type of the processor we want to disconnect from
     * @param channel channel
     * @param signalProcessor processor
     * @return the processor, if it was indeed removed
     * @see #connectTo(com.tagtraum.jipes.SignalProcessor)
     * @see #getConnectedProcessors()
     */
    <O2> SignalProcessor<O, O2> disconnectFrom(int channel, SignalProcessor<O, O2> signalProcessor);

    /**
     * Lists all registered processors for a specific channel or band.
     *
     * @param channel channel or band
     * @return array of registered processors, never <code>null</code>
     * @see #disconnectFrom(com.tagtraum.jipes.SignalProcessor)
     * @see #connectTo(com.tagtraum.jipes.SignalProcessor)
     */
    SignalProcessor<O, ?>[] getConnectedProcessors(int channel);

    /**
     * Number of channels the signal is split up into.
     *
     * @return number of channels the signal is split up into
     */
    int getChannelCount();
}
