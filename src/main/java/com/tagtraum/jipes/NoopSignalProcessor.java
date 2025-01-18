/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;

/**
 * No-op {@link SignalProcessor} which allows to add multiple child processors.
 * This is different to a {@link SignalSplit} as that the signal is in essence not split,
 * but duplicated.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see SignalSplit
 */
public final class NoopSignalProcessor<T> extends AbstractSignalProcessor<T, T> {

    public NoopSignalProcessor() {
    }

    @Override
    protected final T processNext(final T data) throws IOException {
        return data;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "Noop";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // if it's the same class, it's equal
        return true;
    }
}
