/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.universal;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.audio.AudioBuffer;
import com.tagtraum.jipes.math.AggregateFunction;

import java.io.IOException;

/**
 * <p>Applies an {@link com.tagtraum.jipes.math.AggregateFunction} like {@link com.tagtraum.jipes.math.AggregateFunctions#ARITHMETIC_MEAN}
 * to <em>each</em> provided collection (usually a {@link com.tagtraum.jipes.audio.RealAudioBuffer} or a float array).
 * </p>
 *
 * @param <C> collection of values
 * @param <E> the type of the aggregated values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.math.AggregateFunctions#ARITHMETIC_MEAN
 * @see com.tagtraum.jipes.math.AggregateFunctions#EUCLIDEAN_NORM
 * @see com.tagtraum.jipes.math.AggregateFunctions#ZERO_CROSSING_RATE
 * @see com.tagtraum.jipes.math.AggregateFunctions#VARIANCE
 * @see com.tagtraum.jipes.math.AggregateFunctions#STANDARD_DEVIATION
 * @see com.tagtraum.jipes.math.AggregateFunctions#ROOT_MEAN_SQUARE
 * @see com.tagtraum.jipes.audio.AudioBufferFunctions#createAggregateFunction(com.tagtraum.jipes.math.AggregateFunction)
 */
public class Aggregate<C, E> extends AbstractSignalProcessor<C, E> {

    private AggregateFunction<C,E> aggregateFunction = new AggregateFunction<C,E>() {
        public E aggregate(final C collection) {
            return null;
        }
    };

    public Aggregate() {
    }

    public Aggregate(final AggregateFunction<C, E> aggregateFunction) {
        this(aggregateFunction, null);
    }

    public Aggregate(final AggregateFunction<C, E> aggregateFunction, final Object id) {
        setAggregateFunction(aggregateFunction);
        setId(id);
    }

    public AggregateFunction<C,E> getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(final AggregateFunction<C,E> aggregateFunction) {
        if (aggregateFunction == null) throw new IllegalArgumentException("AggregateFunction must not be null");
        this.aggregateFunction = aggregateFunction;
    }

    protected E processNext(final C buffer) throws IOException {
        if (buffer instanceof AudioBuffer) {
            final AudioBuffer b = (AudioBuffer)buffer;
            if (b.getAudioFormat() != null && b.getAudioFormat().getChannels() != 1) {
                throw new IOException("Source must be mono.");
            }
        }
        return aggregateFunction.aggregate(buffer);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aggregate that = (Aggregate) o;

        if (aggregateFunction != null ? !aggregateFunction.equals(that.aggregateFunction) : that.aggregateFunction != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return aggregateFunction != null ? aggregateFunction.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Aggregate{" + aggregateFunction + '}';
    }

}