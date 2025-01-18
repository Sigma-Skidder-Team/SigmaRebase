/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>A signal pipeline is a <em>straight chain</em> (that is every chain element has exactly one child)
 * of multiple {@link com.tagtraum.jipes.SignalProcessor}s.
 * <p/>
 * <p>It has a <em>head</em> and a <em>tail</em> and is created using these two processors.
 * Building pipelines is useful for creating blackbox building blocks that in turn can be used
 * for building more complex processing graphs.
 * <p/>
 * <p>The structure of a pipeline is meant to be immutable, however, since you hand the processor chain
 * to it when constructing the pipeline, this is not enforced programmatically.
 * <p/>
 *
 * @param <I> type of the input values
 * @param <O> type of the output values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SignalPipeline<I, O> implements SignalProcessor<I, O> {

    private static Logger LOG = Logger.getLogger(SignalPipeline.class.getName());
    private SignalProcessor<I, ?> first;
    private SignalProcessor<?, O> last;

    /**
     * Creates a pipeline with the given head and tail. Note that there must be a <em>straight chain</em>
     * of parent/children relationships between the head and the tail <em>before</em> constructing this
     * pipeline. I.e. it is the caller's responsibility to create the connections.
     *
     * @param first first/head processor
     * @param last last/tail processor
     * @param verifyStraightChain indicates whether you want to make sure that every chain element has exactly one child and that there is a connection between head and tail
     * @see SignalPipeline#SignalPipeline(com.tagtraum.jipes.SignalProcessor[])
     * @throws IllegalArgumentException if their is no straight connection between head and tail, i.e. one processor along the way has more than old child (only thrown when verifyStraightChain == true)
     */
    public SignalPipeline(final SignalProcessor<I, ?> first, final SignalProcessor<?, O> last, final boolean verifyStraightChain)  throws IllegalArgumentException{
        if (verifyStraightChain) verifyStraightChain(first, last);
        this.first = first;
        this.last = last;
    }

    /**
     * Creates a signal processor pipeline from the given processors (in that same order).
     * This constructor not only creates the pipeline object, but also creates the links between its processors
     * by calling their {@link com.tagtraum.jipes.SignalProcessor#connectTo(com.tagtraum.jipes.SignalProcessor)} methods.
     * Note that this way of creating a processing chain works around type checking mechanisms. I.e. if
     * one processor's output type does not match the next processor's input type, most likely a
     * {@link ClassCastException} will be thrown at runtime.
     * </p>
     * If you want to wrap a series of processors that are already connected, use
     * {@link SignalPipeline#SignalPipeline(SignalProcessor, SignalProcessor, boolean)}.
     *
     * @param processors array of processors that are supposed to make a pipeline (starting with the head, ending with the tail)
     * @see com.tagtraum.jipes.SignalProcessor#connectTo(com.tagtraum.jipes.SignalProcessor)
     */
    public SignalPipeline(final SignalProcessor<?, ?>... processors) {
        SignalProcessor last = null;
        for (final SignalProcessor<?,?> processor : processors) {
            if (last == null) {
                last = processor;
            } else {
                last = last.connectTo(processor);
            }
        }
        this.first = (SignalProcessor<I, ?>)processors[0];
        this.last = (SignalProcessor<?, O>)last;
    }

    /**
     * First processor of this pipeline.
     *
     * @return first processor
     */
    public SignalProcessor<I, ?> getFirstProcessor() {
        return first;
    }

    /**
     * Returns the first processor with the given id.
     *
     * @param id id must not be <code>null</null>
     * @return the found processor or <code>null</null>
     * @exception IllegalStateException if this pipeline is not a line, but a tree or graph
     */
    public SignalProcessor<?, ?> getProcessorWithId(final Object id) throws IllegalStateException {
        SignalProcessor<?, ?> processor = first;
        while (true) {
            if (id.equals(processor.getId())) return processor;
            if (processor == last) break;
            SignalProcessor<?, ?>[] connectedProcessors = processor.getConnectedProcessors();
            if (connectedProcessors.length != 1) throw new IllegalStateException("Found a processor that is connected to more than one sub-processors: " + processor);
            processor = connectedProcessors[0];
        }
        return null;
    }

    /**
     * Returns the first processor that is an instance of the given {@link Class}.
     *
     * @param klass klass must not be <code>null</null>
     * @return the found processor or <code>null</null>
     * @exception IllegalStateException if this pipeline is not a line, but a tree or graph
     */
    public <T extends SignalProcessor> T getProcessorWithClass(final Class<T> klass) throws IllegalStateException {
        SignalProcessor<?, ?> processor = first;
        while (true) {
            if (klass.isInstance(processor)) return (T)processor;
            if (processor == last) break;
            SignalProcessor<?, ?>[] connectedProcessors = processor.getConnectedProcessors();
            if (connectedProcessors.length != 1) throw new IllegalStateException("Found a processor that is connected to more than one sub-processors: " + processor);
            processor = connectedProcessors[0];
        }
        return null;
    }

    /**
     * Creates a new pipeline from this pipeline's head to the given processor by connecting
     * this pipeline's tail to the given processor and using the current head and the newly
     * added processor as tail.
     * <p/>
     * The original pipeline remains unchanged.
     *
     * @param processor processor to append
     * @param <O2> new output type
     * @return new pipeline
     */
    public <O2> SignalPipeline<I, O2> joinWith(final SignalProcessor<O, O2> processor) {
        final SignalProcessor<?, O2> newLast;
        if (processor instanceof SignalPipeline) {
            SignalPipeline<O, O2> pipeline = (SignalPipeline<O, O2>) processor;
            last.connectTo(pipeline.first);
            newLast = pipeline.last;
        } else {
            newLast = processor;
            last.connectTo(processor);
        }
        return new SignalPipeline<I, O2>(first, newLast, false);
    }

    /**
     * Verifies that there is a straight connection between the head and the tail of a pipeline.
     *
     * @param head head
     * @param tail tail
     *
     * @throws IllegalArgumentException if their is no straight connection between head and tail.
     */
    private void verifyStraightChain(final SignalProcessor<I, ?> head, final SignalProcessor<?, O> tail) throws IllegalArgumentException {
        SignalProcessor<?,?> processor = head;
        while (processor != tail) {
            final SignalProcessor<?, ?>[] children = processor.getConnectedProcessors();
            if (children.length > 1) {
                throw new IllegalArgumentException("Pipeline from head " + head + " to tail " + tail
                        + " has an illegal fork at " + processor + ". The fork leads to "
                        + new ArrayList<SignalProcessor<?, ?>>(Arrays.asList(children)));
            }
            if (children.length == 0) {
                throw new IllegalArgumentException("Pipeline from head " + head + " to tail " + tail
                        + " does not have a straight connection from said head to tail. It ends at " + processor);
            }
            processor = children[0];
        }
    }

    public void process(final I i) throws IOException {
        first.process(i);
    }

    public void flush() throws IOException {
        first.flush();
    }

    public O getOutput() throws IOException {
        return last.getOutput();
    }

    public Object getId() {
        return last.getId();
    }

    public <O2> SignalProcessor<O, O2> connectTo(final SignalProcessor<O, O2> oSignalProcessor) {
        return last.connectTo(oSignalProcessor);
    }

    public <O2> SignalProcessor<O, O2> disconnectFrom(final SignalProcessor<O, O2> oSignalProcessor) {
        return last.disconnectFrom(oSignalProcessor);
    }

    public SignalProcessor<O, ?>[] getConnectedProcessors() {
        return last.getConnectedProcessors();
    }

    @Override
    public String toString() {
        final List<SignalProcessor<?, ?>> path = getPath();
        return "SignalPipeline" + path;
    }

    private List<SignalProcessor<?, ?>> getPath() {
        final List<SignalProcessor<?,?>> path = new ArrayList<SignalProcessor<?,?>>();
        SignalProcessor<?,?> processor = first;
        while (processor != last) {
            path.add(processor);
            if (processor.getConnectedProcessors().length == 1) {
                processor = processor.getConnectedProcessors()[0];
            }
            else if (processor.getConnectedProcessors().length > 1) {
                processor = processor.getConnectedProcessors()[0];
                // can't use toString() here, because this method is called from toString()
                LOG.warning("Pipeline with System.identityHashCode=" + System.identityHashCode(this) + " contains a fork.");
            }
            else break;
        }
        path.add(last);
        return path;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SignalPipeline that = (SignalPipeline) o;
        if (!getPath().equals(that.getPath())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return first != null ? first.hashCode() : 0;
    }
}
