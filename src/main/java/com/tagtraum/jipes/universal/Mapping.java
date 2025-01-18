/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.universal;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.math.MapFunction;
import com.tagtraum.jipes.math.StatefulMapFunction;
import com.tagtraum.jipes.math.WindowFunction;

import java.io.IOException;

/**
 * Applies a {@link com.tagtraum.jipes.math.MapFunction} to <em>each</em>
 * provided object of type T (for example to all data of a {@link com.tagtraum.jipes.audio.RealAudioBuffer}}).
 * The function could be e.g. a {@link WindowFunction.Hamming} window or a
 * {@link com.tagtraum.jipes.math.Filters.FIRFilter}. Due to its universal nature, this is a central
 * {@link com.tagtraum.jipes.SignalProcessor} class.
 *
 * @param <T> type to map data from <em>and</em> to
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.math.Floats
 * @see com.tagtraum.jipes.audio.AudioBufferFunctions#createMapFunction(com.tagtraum.jipes.math.MapFunction)
 */
public class Mapping<T> extends AbstractSignalProcessor<T, T> {

    private MapFunction<T> mapFunction;

    public Mapping() {
    }

    public Mapping(final MapFunction<T> mapFunction) {
        setMapFunction(mapFunction);
    }

    public Mapping(final MapFunction<T> mapFunction, final Object id) {
        setMapFunction(mapFunction);
        setId(id);
    }

    public MapFunction<T> getMapFunction() {
        return mapFunction;
    }

    public void setMapFunction(final MapFunction<T> mappingFunction) {
        this.mapFunction = mappingFunction;
    }

    public void reset() {
        super.reset();
        if (mapFunction instanceof StatefulMapFunction) {
            ((StatefulMapFunction) mapFunction).reset();
        }
    }

    protected T processNext(final T buffer) throws IOException {
        if (mapFunction == null) return buffer;
        else return mapFunction.map(buffer);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mapping that = (Mapping) o;
        if (mapFunction != null ? !mapFunction.equals(that.mapFunction) : that.mapFunction != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return mapFunction != null ? mapFunction.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Mapping{" + mapFunction + '}';
    }
}