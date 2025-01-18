/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Maps a <code>float</code> to another <code>float</code>, applying some sort of function to it.
 * Because this function does not need to box primitive float values into {@link Float} values
 * it may be much faster than {@link MapFunction}&lt;Float&gt;.
 * <p/>
 * Date: Aug 27, 2010
 * Time: 10:44:04 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://en.wikipedia.org/wiki/Map_(higher-order_function)">Wikipedia, Map Function</a>
 * @see com.tagtraum.jipes.math.DistanceFunction
 * @see AggregateFunction
 * @see MapFunction
 */
public interface FloatMapFunction {

    /**
     * Maps a float to a new float.
     *
     * @param data data
     * @return mapped data
     */
    float map(float data);
}
