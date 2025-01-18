/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Useful for calculating the distance between two objects of the same type,
 * for example <code>float</code> or <code>double</code> arrays.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see Floats
 * @see MapFunction
 * @see AggregateFunction
 */
public interface DistanceFunction<C> {

    /**
     * Computes the distance between two arrays/vectors.
     *
     * @param a vector a
     * @param b vector b
     * @return distance
     * @see Floats
     */
    float distance(final C a, final C b);

}
