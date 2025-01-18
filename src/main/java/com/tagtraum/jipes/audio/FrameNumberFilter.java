/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;

import java.io.IOException;

/**
 * <p>Allows to ignore {@link AudioBuffer}s, if their <em>first</em> frame number as returned by
 * {@link com.tagtraum.jipes.audio.AudioBuffer#getFrameNumber()} does not fall into a specified
 * range. Note, that the max frame number is exclusive.
 * </p><p>
 * If the end of the buffer is higher than max frames, it is still let through, if
 * the beginning is lower.
 * </p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FrameNumberFilter extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private int minFrameNumber = 0;
    private int maxFrameNumber = Integer.MAX_VALUE;

    /**
     * Creates a 'gatekeeper' for buffers with certain frame numbers.
     *
     * @param minFrameNumber each buffer's first frame number has to be at least as big as this
     * @param maxFrameNumber if a buffer's first frame number is equal or greater, the whole buffer will be ignored
     */
    public FrameNumberFilter(final int minFrameNumber, final int maxFrameNumber) {
        setMinFrameNumber(minFrameNumber);
        setMaxFrameNumber(maxFrameNumber);
    }

    public int getMinFrameNumber() {
        return minFrameNumber;
    }

    public void setMinFrameNumber(final int minFrameNumber) {
        if (minFrameNumber > this.maxFrameNumber) throw new IllegalArgumentException("Min frame number cannot be greater than max frame number.");
        this.minFrameNumber = minFrameNumber;
    }

    public int getMaxFrameNumber() {
        return maxFrameNumber;
    }

    public void setMaxFrameNumber(final int maxFrameNumber) {
        if (maxFrameNumber < this.minFrameNumber) throw new IllegalArgumentException("Min frame number cannot be greater than max frame number.");
        this.maxFrameNumber = maxFrameNumber;
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) throws IOException {
        throw new RuntimeException("This method is not used by the implementation");
    }

    @Override
    public AudioBuffer read() throws IOException {
        AudioBuffer buffer;
        if ((buffer = getConnectedSource().read()) != null && isValidBuffer(buffer)) {
            return buffer;
        }
        return null;
    }

    @Override
    public void process(final AudioBuffer buffer) throws IOException {
        if (isValidBuffer(buffer)) {
            signalProcessorSupport.process(buffer);
        }
    }

    private boolean isValidBuffer(final AudioBuffer buffer) {
        return buffer.getFrameNumber() >= minFrameNumber && buffer.getFrameNumber() < maxFrameNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FrameNumberFilter that = (FrameNumberFilter) o;

        if (maxFrameNumber != that.maxFrameNumber) return false;
        if (minFrameNumber != that.minFrameNumber) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = minFrameNumber;
        result = 31 * result + maxFrameNumber;
        return result;
    }

    @Override
    public String toString() {
        return "FrameNumberFilter{" +
                "minFrameNumber=" + minFrameNumber +
                ", maxFrameNumber=" + maxFrameNumber +
                '}';
    }
}

