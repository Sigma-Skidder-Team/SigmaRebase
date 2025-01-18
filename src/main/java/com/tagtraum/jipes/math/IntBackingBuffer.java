/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Backing buffer using a {@link java.nio.IntBuffer} internally.
 * All written <code>float</code> values are rounded to the nearest <code>int</code>.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class IntBackingBuffer implements MatrixBackingBuffer, Cloneable {

    private final boolean direct;
    private IntBuffer buffer;

    /**
     * Creates the backing buffer without actually creating the internal data structure.
     *
     * @param direct should the underlying {@link IntBuffer} be native/direct or not?
     * @see #allocate(int)
     */
    public IntBackingBuffer(final boolean direct) {
        this.direct = direct;
    }

    @Override
    public void allocate(final int size) {
        this.buffer = direct
                ? ByteBuffer.allocateDirect(size * 4).asIntBuffer()
                : IntBuffer.allocate(size);
    }

    @Override
    public boolean isAllocated() {
        return this.buffer != null;
    }

    @Override
    public void set(final int index, final float value) {
        this.buffer.put(index, Math.round(value));
    }

    @Override
    public float get(final int index) {
        return this.buffer.get(index);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final IntBackingBuffer clone = (IntBackingBuffer)super.clone();
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
