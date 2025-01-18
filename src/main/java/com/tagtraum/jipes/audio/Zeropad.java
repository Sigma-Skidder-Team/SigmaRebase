/*
 * =================================================
 * Copyright 2017 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;

import java.io.IOException;

/**
 * Zeropads {@link AudioBuffer}s at a given {@link Position},
 * so that a desired total size is reached.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class Zeropad<T extends AudioBuffer> implements SignalProcessor<T, T> {

    private final SignalProcessorSupport<T> support = new SignalProcessorSupport<T>();
    private final Position position;
    private final Object id;
    private final int sizeAfterPadding;
    private T out;

    public Zeropad(final Object id, final Position position, final int sizeAfterPadding) {
        this.position = position;
        this.id = id;
        this.sizeAfterPadding = sizeAfterPadding;
    }

    public Zeropad(final Position position, final int sizeAfterPadding) {
        this(null, position, sizeAfterPadding);
    }

    @Override
    public void process(final T buffer) throws IOException {
        final float[] paddedImaginary = position.pad(sizeAfterPadding, buffer.getImaginaryData());
        final float[] paddedReal = position.pad(sizeAfterPadding, buffer.getRealData());
        out = (T) buffer.derive(paddedReal, paddedImaginary);
        support.process(out);
    }

    @Override
    public void flush() throws IOException {
        support.flush();
    }

    @Override
    public T getOutput() throws IOException {
        return out;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public <O2> SignalProcessor<T, O2> connectTo(final SignalProcessor<T, O2> signalProcessor) {
        return support.connectTo(signalProcessor);
    }

    @Override
    public <O2> SignalProcessor<T, O2> disconnectFrom(final SignalProcessor<T, O2> signalProcessor) {
        return support.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<T, ?>[] getConnectedProcessors() {
        return support.getConnectedProcessors();
    }

    public enum Position {
        FRONT() {
            @Override
            float[] pad(final int size, final float[] data) {
                if (data == null) return null;
                final float[] paddedData = new float[size];
                System.arraycopy(data, 0, paddedData, size-data.length, data.length);
                return paddedData;
            }
        },
        BACK() {
            @Override
            float[] pad(final int size, final float[] data) {
                if (data == null) return null;
                final float[] paddedData = new float[size];
                System.arraycopy(data, 0, paddedData, 0, data.length);
                return paddedData;
            }
        },
        BOTH() {
            @Override
            float[] pad(final int size, final float[] data) {
                if (data == null) return null;
                final float[] paddedData = new float[size];
                System.arraycopy(data, 0, paddedData, size/4, data.length);
                return paddedData;
            }
        };

        abstract float[] pad(int size, float[] data);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Zeropad<?> zeropad = (Zeropad<?>) o;

        if (sizeAfterPadding != zeropad.sizeAfterPadding) return false;
        if (position != zeropad.position) return false;
        return id != null ? id.equals(zeropad.id) : zeropad.id == null;
    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + sizeAfterPadding;
        return result;
    }

    @Override
    public String toString() {
        return "Zeropad{" +
            "id=" + id +
            ", " + position +
            ", sizeAfterPadding=" + sizeAfterPadding +
            '}';
    }
}
