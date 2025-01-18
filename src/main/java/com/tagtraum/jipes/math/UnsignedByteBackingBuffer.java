/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.nio.ByteBuffer;

/**
 * Special backing buffer that uses a byte array and has a resolution of 8 bit.
 * In the unsigned variant, float values between 0 and 1 are internally mapped
 * to a byte each. Precision may be lost.
 * Any attempt to write invalid values (e.g. &gt; 1) lead to {@link IllegalArgumentException}s.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see SignedByteBackingBuffer
 */
public class UnsignedByteBackingBuffer implements MatrixBackingBuffer, Cloneable {

    private final boolean direct;
    private ByteBuffer buffer;

    /**
     * Creates the backing buffer without actually creating the internal data structure.
     *
     * @param direct should the underlying {@link java.nio.ByteBuffer} be native/direct or not?
     * @see #allocate(int)
     */
    public UnsignedByteBackingBuffer(final boolean direct) {
        this.direct = direct;
    }

    @Override
    public void allocate(final int size) {
        this.buffer = direct
                ? ByteBuffer.allocateDirect(size)
                : ByteBuffer.allocate(size);
    }

    @Override
    public boolean isAllocated() {
        return this.buffer != null;
    }

    @Override
    public float get(final int index) {
        return (buffer.get(index) - Byte.MIN_VALUE) / 255f;
    }

    @Override
    public void set(final int index, final float value) {
        if (value > 1f) throw new IllegalArgumentException("UnsignedByteBackingBuffer only supports values <= 1f: " + value);
        if (value < 0f) throw new IllegalArgumentException("UnsignedByteBackingBuffer only supports values >= 0f: " + value);
        buffer.put(index, (byte) (value * 255f + Byte.MIN_VALUE));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final UnsignedByteBackingBuffer clone = (UnsignedByteBackingBuffer)super.clone();
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
