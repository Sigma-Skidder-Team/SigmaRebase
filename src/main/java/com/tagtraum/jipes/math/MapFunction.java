/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Maps an object (typically an array or an {@link com.tagtraum.jipes.audio.RealAudioBuffer})
 * of a given size to a new object of the same size, applying some sort of function to all
 * elements of the object.
 * <p/>
 * Date: Aug 27, 2010
 * Time: 10:44:04 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://en.wikipedia.org/wiki/Map_(higher-order_function)">Wikipedia, Map Function</a>
 * @see DistanceFunction
 * @see AggregateFunction
 * @see FloatMapFunction
 * @see MapFunctions
 */
public interface MapFunction<T> {

    /**
     * Maps an object (usually an array, an {@link com.tagtraum.jipes.audio.RealAudioBuffer}
     * or a {@link java.util.List}) to another one of the same size.
     *
     * @param data data
     * @return mapped data
     */
    T map(T data);
}
