/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Backing buffer using a {@link FloatBuffer} internally.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FloatBackingBuffer implements MatrixBackingBuffer, Cloneable {

    private final boolean direct;
    private FloatBuffer buffer;

    /**
     * Creates the backing buffer without actually creating the internal data structure.
     *
     * @param direct should the underlying {@link FloatBuffer} be native/direct or not?
     * @see #allocate(int)
     */
    public FloatBackingBuffer(final boolean direct) {
        this.direct = direct;
    }

    @Override
    public void allocate(final int size) {
        this.buffer = direct
                ? ByteBuffer.allocateDirect(size * 4).asFloatBuffer()
                : FloatBuffer.allocate(size);
    }

    @Override
    public boolean isAllocated() {
        return this.buffer != null;
    }

    @Override
    public void set(final int index, final float value) {
        this.buffer.put(index, value);
    }

    @Override
    public float get(final int index) {
        return this.buffer.get(index);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final FloatBackingBuffer clone = (FloatBackingBuffer)super.clone();
        if (this.isAllocated()) {
            clone.allocate(this.buffer.capacity());
            this.buffer.rewind();
            clone.buffer.put(this.buffer);
            this.buffer.rewind();
            clone.buffer.flip();
        }
        return clone;
    }
}
