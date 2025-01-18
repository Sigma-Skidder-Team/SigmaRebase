/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;

/**
 * <p>Convenience super class for both {@link com.tagtraum.jipes.SignalPullProcessor} and
 * {@link com.tagtraum.jipes.SignalProcessor}.
 * <p/>
 * <p>This class is a good starting point for processors that do not change the flow of data,
 * i.e. don't collect data before they forward it. In other words, each processing step
 * has one input and one output value.
 * <p/>
 *
 * @param <I> type of the input values
 * @param <O> type of the output values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class AbstractSignalProcessor<I, O> implements SignalPullProcessor<I, O>, SignalProcessor<I, O> {

    protected O lastOut;
    protected SignalProcessorSupport<O> signalProcessorSupport = new SignalProcessorSupport<O>();
    private SignalSource<I> source;
    private Object id;

    protected AbstractSignalProcessor() {
    }

    /**
     * @param id id to retrieve the last output
     * @see com.tagtraum.jipes.SignalProcessor#getId()
     */
    protected AbstractSignalProcessor(final Object id) {
        this.id = id;
    }

    /**
     * Flushes this processor and its kids.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        signalProcessorSupport.flush();
    }

    /**
     * The last object created by this processor.
     *
     * @return last object
     * @throws IOException
     */
    public O getOutput() throws IOException {
        return lastOut;
    }

    /**
     * If no explicit id is set, this simply calls {@link #toString()}.
     *
     * @return id
     * @see #setId(Object) 
     */
    public Object getId() {
        if (id == null) return toString();
        return id;
    }

    /**
     * Lets you provide an id.
     *
     * @param id some identifier
     */
    public void setId(final Object id) {
        this.id = id;
    }

    public <O2> SignalProcessor<O, O2> connectTo(final SignalProcessor<O, O2> signalProcessor) {
        return signalProcessorSupport.connectTo(signalProcessor);
    }

    public <O2> SignalProcessor<O, O2> disconnectFrom(final SignalProcessor<O, O2> signalProcessor) {
        return signalProcessorSupport.disconnectFrom(signalProcessor);
    }

    public SignalProcessor<O, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    public <O2> SignalPullProcessor<O2, I> connectTo(final SignalPullProcessor<O2, I> source) {
        connectTo((SignalSource<I>) source);
        return source;
    }

    public void connectTo(final SignalSource<I> source) {
        this.source = source;
    }

    public SignalSource<I> getConnectedSource() {
        return source;
    }

    /**
     * Calls {@link com.tagtraum.jipes.SignalSource#reset()}, if a source is set.
     */
    public void reset() {
        if (source != null) source.reset();
    }

    public O read() throws IOException {
        final I buffer = source.read();
        if (buffer != null) {
            return processNext(buffer);
        }
        return null;
    }

    public void process(final I in) throws IOException {
        lastOut = processNext(in);
        signalProcessorSupport.process(lastOut);
    }

    /**
     * Processes the given input and returns some output.
     *
     * @param input input guaranteed not to be <code>null</code>.
     * @return output produced by this processor
     * @throws java.io.IOException if an IO error occurs
     */
    protected abstract O processNext(I input) throws IOException;
}
