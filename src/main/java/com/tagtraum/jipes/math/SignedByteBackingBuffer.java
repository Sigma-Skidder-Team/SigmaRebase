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
 * In the signed variant, float values between -1 and 1 are internally mapped to a single byte value.
 * Precision may be lost.
 * Any attempt to write invalid values (e.g. &gt; 1) lead to {@link IllegalArgumentException}s.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see UnsignedByteBackingBuffer
 */
public class SignedByteBackingBuffer implements MatrixBackingBuffer, Cloneable {

    private final boolean direct;
    private ByteBuffer buffer;

    /**
     * Creates the backing buffer without actually creating the internal data structure.
     *
     * @param direct should the underlying {@link java.nio.ByteBuffer} be native/direct or not?
     * @see #allocate(int)
     */
    public SignedByteBackingBuffer(final boolean direct) {
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
        return buffer.get(index) /(float)Byte.MAX_VALUE;
    }

    @Override
    public void set(final int index, final float value) {
        if (value > 1f) throw new IllegalArgumentException("SignedByteBackingBuffer only supports values <= 1f: " + value);
        if (value < -1f) throw new IllegalArgumentException("SignedByteBackingBuffer matrix only supports values >= -1f: " + value);
        buffer.put(index, (byte) (value * Byte.MAX_VALUE));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final SignedByteBackingBuffer clone = (SignedByteBackingBuffer)super.clone();
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
