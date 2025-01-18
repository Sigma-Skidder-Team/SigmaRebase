/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Maps a collection (typically an array or the floats in of a {@link com.tagtraum.jipes.audio.RealAudioBuffer})
 * of a given size to a new object of size 1, applying some sort of function aggregating all
 * elements of the collection. Typical examples are the sum, the statistical mean, standard deviation or
 * variance.
 * <p/>
 * Date: Aug 27, 2010
 * Time: 10:44:04 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://en.wikipedia.org/wiki/Map_(higher-order_function)">Wikipedia, Map Function</a>
 * @see com.tagtraum.jipes.math.MapFunction
 * @see DistanceFunction
 * @see FloatAggregateFunction
 * @see AggregateFunctions
 */
public interface AggregateFunction<C, E> {

    /**
     * Maps a collection (usually an array, an {@link com.tagtraum.jipes.audio.RealAudioBuffer}
     * or a {@link java.util.List}) to a single value.
     *
     * @param collection collection
     * @return aggregation value
     */
    E aggregate(C collection);
}
