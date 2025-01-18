/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;

/**
 * <p>Lets you process data using a <em>push</em> approach, that is controlling the flow of data
 * from the <em>beginning</em> of a pipeline (preferably with a {@link SignalPump}).
 * This means that you can push data into the {@link #process(Object)}
 * method without expecting a result as return value. Instead, a graph of registered SignalProcessors
 * is processing the provided data. Only when you call {@link #flush()} the final processing step is done and
 * final results have to be computed. Those results may be collected from the tree's leafs
 * with the leaf object's {@link #getOutput()} method.
 * <p/>
 * <p>If you have a rather simple pipeline, that does not contain any forks, you might want to use the
 * <em>pull</em> model, offered by {@link SignalPullProcessor}.
 * </p>
 *
 * Date: Aug 25, 2010
 * Time: 3:36:06 PM
 *
 * @param <I> type of the input values
 * @param <O> type of the output values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see SignalPullProcessor
 * @see SignalPump
 */
public interface SignalProcessor<I, O> {

    /**
     * Asks this processor to work on the provided data and pipe the
     * output to the connected processors' own {@link #process(Object)} method.<br>
     * Implementing methods <em>must not modify or hold on to the provided data</em>.
     * In other words: the provided object does not belong to you, its values are only
     * guaranteed to be stable for the duration of the call.
     * <p/>
     * If you need to hold on to data, create a copy of the provided data via
     * a copy constructor or the {@link Object#clone()} method.
     * <p/>
     * It is the responsibility of implementing classes to call the {@link #process(Object)}
     * method of any connected processors. To manage connected processors classes may want
     * to use a {@link SignalProcessorSupport} instance.
     *
     * @param i data to process
     * @throws IOException if a processing error occurs
     * @see #getConnectedProcessors()
     */
    void process(I i) throws IOException;

    /**
     * Asks to complete all pending operations.
     * This method may be called multiple times without any adverse effects.
     * It's the implementation's responsibility to call its kids' {@link #flush()} method.
     *
     * @throws IOException if a processing error occurs
     */
    void flush() throws IOException;

    /**
     * First calls {@link #flush()}, then returns the pending output, should there be any.
     * Note that this call may only succeed once to preserve memory. 
     *
     * @return output, if available. If not, <code>null</code> is returned
     * @throws IOException if a processing error occurs
     */
    O getOutput() throws IOException;

    /**
     * Arbitrary object to identify a processor. This is useful for associating with results provided by
     * {@link #getOutput()}.
     *
     * @return arbitrary non-null object. If this method returns null, potential results will not be associated
     * with an identifier and potentially dropped
     * @see SignalPump#pump()
     */
    Object getId();

    /**
     * Connects this processor to another processor to forward processed data to.
     * This method returns its parameter and therefore encourages
     * <a href="http://en.wikipedia.org/wiki/Method_chaining">method chaining</a>
     * to build processing chains/pipelines.
     *
     * @param <O2> output type of the processor we want to connect to
     * @param signalProcessor processor
     * @see #disconnectFrom(SignalProcessor)
     * @see #getConnectedProcessors()
     * @return the just added processor
     * @see SignalPipeline
     */
    <O2> SignalProcessor<O, O2> connectTo(SignalProcessor<O, O2> signalProcessor);

    /**
     * Disconnects a child processor.
     *
     * @param <O2> output type of the processor we want to disconnect from
     * @param signalProcessor processor
     * @return the processor, if it was indeed removed
     * @see #connectTo(SignalProcessor)
     * @see #getConnectedProcessors()
     */
    <O2> SignalProcessor<O, O2> disconnectFrom(SignalProcessor<O, O2> signalProcessor);

    /**
     * Lists all connected processors.
     *
     * @return array of registered processors, never <code>null</code>
     * @see #disconnectFrom(SignalProcessor)
     * @see #connectTo(SignalProcessor)
     */
    SignalProcessor<O, ?>[] getConnectedProcessors();

}
