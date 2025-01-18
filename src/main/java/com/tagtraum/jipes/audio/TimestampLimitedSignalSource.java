/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalSource;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Timestamp limited {@link SignalSource}.
 * Delivers {@link AudioBuffer}s as long as the source delivers them and
 * {@link com.tagtraum.jipes.audio.AudioBuffer#getTimestamp(TimeUnit)} &lt;
 * {@link #getMaxTimestamp(java.util.concurrent.TimeUnit)}.
 *
 * @param <T> subtype of {@link AudioBuffer}
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TimestampLimitedSignalSource<T extends AudioBuffer> implements SignalSource<T>, Closeable {

    private final SignalSource<T> signalSource;
    private final long maxTimestamp;
    private final TimeUnit timeUnit;
    private boolean closed;

    /**
     * Creates a timestamp limited signal source.
     *
     * @param signalSource signal source to read from
     * @param maxTimestampInMS max timestamp up to which to read in ms
     */
    public TimestampLimitedSignalSource(final SignalSource<T> signalSource, final long maxTimestampInMS) {
        this(signalSource, maxTimestampInMS, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a timestamp limited signal source.
     *
     * @param signalSource signal source to read from
     * @param timeUnit time unit for the timestamp
     * @param maxTimestamp max timestamp up to which to read
     */
    public TimestampLimitedSignalSource(final SignalSource<T> signalSource, final long maxTimestamp, final TimeUnit timeUnit) {
        this.signalSource = signalSource;
        this.timeUnit = timeUnit;
        this.maxTimestamp = maxTimestamp;
    }

    /**
     * Max timestamp up to which to read.
     *
     * @param timeUnit desired unit
     * @return timestamp in ms
     */
    public long getMaxTimestamp(final TimeUnit timeUnit) {
        return timeUnit.convert(maxTimestamp, this.timeUnit);
    }

    /**
     * Warpped signal source.
     *
     * @return signal source
     */
    public SignalSource<T> getSignalSource() {
        return signalSource;
    }

    @Override
    public T read() throws IOException {
        final T buffer = signalSource.read();
        final T result = buffer != null && buffer.getTimestamp(timeUnit) < maxTimestamp ? buffer : null;
        // auto close, when we're done
        if (result == null && !closed) {
            close();
        }
        return result;
    }

    public void reset() {
        signalSource.reset();
    }

    /**
     * Closes the underlying {@link SignalSource}, if it implements {@link Closeable}.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (signalSource != null && signalSource instanceof Closeable && !closed) {
            closed = true;
            ((Closeable) signalSource).close();
        }
    }

    @Override
    public String toString() {
        return "TimestampLimitedSignalSource{" +
                "maxTimestamp=" + maxTimestamp +
                " " + timeUnit.toString().toLowerCase() +
                ", signalSource=" + signalSource +
                '}';
    }
}
