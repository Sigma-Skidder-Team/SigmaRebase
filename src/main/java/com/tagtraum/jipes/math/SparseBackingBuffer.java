/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.HashMap;
import java.util.Map;

/**
 * A sparse matrix backing buffer uses a {@link HashMap} as internal datastructure.
 * Only values that differ from the <code>defaultValue</code> (can be set via {@link #SparseBackingBuffer(float)})
 * are actually stored.
 * If you intend to place values in most possible matrix positions, this is not a suitable implementation.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SparseBackingBuffer implements MatrixBackingBuffer, Cloneable {

    private Map<Integer, Float> buffer;
    private final float defaultValue;

    /**
     * Creates a sparse backing buffer with the given default value.
     * That means, whenever no value can be found in the internal map, the default
     * value is returned.
     *
     * @param defaultValue default value
     */
    public SparseBackingBuffer(final float defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sparse backing buffer with default value <code>0f</code>.
     */
    public SparseBackingBuffer() {
        this(0f);
    }

    @Override
    public void allocate(final int size) {
        this.buffer = new HashMap<Integer, Float>();
    }

    @Override
    public boolean isAllocated() {
        return this.buffer != null;
    }

    @Override
    public void set(final int index, final float value) {
        if (value == defaultValue) this.buffer.remove(index);
        else this.buffer.put(index, value);
    }

    @Override
    public float get(final int index) {
        final Float value = this.buffer.get(index);
        return value == null ? defaultValue : value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final SparseBackingBuffer clone = (SparseBackingBuffer)super.clone();
        if (this.isAllocated()) {
            clone.allocate(0);
            clone.buffer.putAll(this.buffer);
        }
        return clone;
    }
}
