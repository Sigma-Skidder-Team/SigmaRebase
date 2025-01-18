/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * <p>Stateful {@link MapFunction}. Typically the mapping of one value to another depends on
 * values that were mapped before.
 * </p>
 * <p>Examples are the mapping of a value to the mean of all the
 * prior values combined with the current value
 * ({@link com.tagtraum.jipes.math.AggregateFunctions#ARITHMETIC_MEAN})
 * or a signal {@link com.tagtraum.jipes.math.Filters.FIRFilter}.
 * </p>
 * <p/>
 * Date: Aug 27, 2010
 * Time: 10:53:55 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface StatefulMapFunction<T> extends MapFunction<T> {

    /**
     * Resets the function's state.
     */
    void reset();

}
