/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.universal;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import com.tagtraum.jipes.math.AggregateFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Joins input data from <code>N</code> consecutive calls to {@link #process(Object)} using an {@link com.tagtraum.jipes.math.AggregateFunction}.
 * By adding this joiner to multiple {@link com.tagtraum.jipes.SignalProcessor}s, which are children of the same {@link com.tagtraum.jipes.SignalSplit},
 * one can rejoin multiple bands, e.g. by adding their values.
 *
 * @param <I> type of the input values
 * @param <O> type of the output values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.SignalSplit
 */
public class Join<I, O> implements SignalProcessor<I, O> {

    private SignalProcessorSupport<O> signalProcessorSupport = new SignalProcessorSupport<O>();
    private AggregateFunction<List<I>, O> aggregateFunction;
    private int partsPerUnit;
    private List<I> parts = new ArrayList<I>();
    private O lastOut;
    private Object id;
    private boolean flushed;

    public Join(final int partsPerUnit, final AggregateFunction<List<I>, O> aggregateFunction) {
        this(partsPerUnit, aggregateFunction, null);
    }

    public Join(final int partsPerUnit, final AggregateFunction<List<I>, O> aggregateFunction, final Object id) {
        if (aggregateFunction == null) throw new IllegalArgumentException("AggregateFunction must not be null.");
        this.aggregateFunction = aggregateFunction;
        setPartsPerUnit(partsPerUnit);
        setId(id);
    }

    private void setPartsPerUnit(final int partsPerUnit) {
        if (partsPerUnit<1) throw new IllegalArgumentException("Parts per unit: " + partsPerUnit);
        this.partsPerUnit = partsPerUnit;
    }

    public int getPartsPerUnit() {
        return partsPerUnit;
    }

    /**
     * Only call kids process method, if we have enough parts.
     *
     * @param i data to process
     * @throws IOException
     */
    public void process(final I i) throws IOException {
        this.parts.add(i);
        if (parts.size() == partsPerUnit) {
            this.lastOut = aggregateFunction.aggregate(parts);
            this.signalProcessorSupport.process(lastOut);
            this.parts.clear();
        }
    }

    /**
     * Only flush, if we just aggregated some signal, but not, if we are still waiting for missing parts.
     * Also, flush at most once.
     * This effectively swallows flush calls, if they occur before we put all the pieces together.
     * We do this to avoid pre-mature/multiple flushing.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        if (parts.size() % partsPerUnit == 0 && !flushed) {
            signalProcessorSupport.flush();
            flushed = true;
        }
    }

    public O getOutput() throws IOException {
        return lastOut;
    }

    /**
     * By default this simply calls {@link #toString()}
     *
     * @return id
     * @see #setId(Object)
     */
    public Object getId() {
        if (id == null) return toString();
        return id;
    }

    /**
     * Let's you provide an id.
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Join that = (Join) o;

        if (partsPerUnit != that.partsPerUnit) return false;
        if (aggregateFunction != null ? !aggregateFunction.equals(that.aggregateFunction) : that.aggregateFunction != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = aggregateFunction != null ? aggregateFunction.hashCode() : 0;
        result = 31 * result + partsPerUnit;
        return result;
    }

    @Override
    public String toString() {
        return "Join{" +
                "parts=" + partsPerUnit +
                ", function=" + aggregateFunction +
                '}';
    }

}
