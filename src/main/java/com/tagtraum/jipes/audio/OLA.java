/*
 * =================================================
 * Copyright 2015 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;

import java.io.IOException;

/**
 * Overlap-Add combines multiple overlapping windows into non-overlapping ones by
 * adding the overlapping parts. This is the counterpart of {@link SlidingWindow}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see SlidingWindow
 */
public class OLA extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    private int hopSizeInFrames = 1024;
    private int sliceLengthInFrames = 2048;

    private ComplexAudioBuffer lastBuffer;
    private ComplexAudioBuffer currentBuffer;
    private int offset;

    /**
     * Creates a processor with a window length of 2048 frames and 1024 hop size.
     */
    public OLA() {
    }

    /**
     * Produces windows of frames that don't overlap.
     *
     * @param sliceLengthInFrames frames per window
     * @param hopSizeInFrames number of frames two consecutive <em>input</em> windows overlap
     */
    public OLA(final int sliceLengthInFrames, final int hopSizeInFrames) {
        setHopSizeInFrames(hopSizeInFrames);
        setSliceLengthInFrames(sliceLengthInFrames);
    }

    /**
     * Produces windows with a given frame length. The <em>input</em> windows overlap by half their length.
     *
     * @param sliceLengthInFrames frames per window
     */
    public OLA(final int sliceLengthInFrames) {
        this(sliceLengthInFrames, sliceLengthInFrames/2);
    }

    public int getHopSizeInFrames() {
        return hopSizeInFrames;
    }

    public void setHopSizeInFrames(final int hopSizeInFrames) {
        if (hopSizeInFrames <= 0) throw new IllegalArgumentException("Hop size must be positive: " + hopSizeInFrames);
        this.hopSizeInFrames = hopSizeInFrames;
    }

    public int getSliceLengthInFrames() {
        return sliceLengthInFrames;
    }

    public void setSliceLengthInFrames(final int sliceLengthInFrames) {
        if (sliceLengthInFrames <= 0) throw new IllegalArgumentException("Slice length must be positive: " + sliceLengthInFrames);
        this.sliceLengthInFrames = sliceLengthInFrames;
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer input) throws IOException {
        throw new RuntimeException("This method in not implemented");
    }

    @Override
    public void process(final AudioBuffer in) throws IOException {
        addToInternalBuffers(in);
        if (offset >= sliceLengthInFrames) {
            signalProcessorSupport.process(lastBuffer);
            lastBuffer = currentBuffer;
            currentBuffer = null;
            offset -= sliceLengthInFrames;
        }
    }
    
    @Override
    public void flush() throws IOException {
        if (lastBuffer != null) signalProcessorSupport.process(lastBuffer);
        if (currentBuffer != null) signalProcessorSupport.process(currentBuffer);
        lastBuffer = null;
        currentBuffer = null;
        super.flush();
    }

    @Override
    public AudioBuffer read() throws IOException {
        while (offset < sliceLengthInFrames) {
            final AudioBuffer in = getConnectedSource().read();
            if (in != null) {
                addToInternalBuffers(in);
            } else if (lastBuffer != null) {
                lastOut = lastBuffer;
                lastBuffer = null;
                return lastOut;
            } else if (currentBuffer != null) {
                lastOut = currentBuffer;
                currentBuffer = null;
                return lastOut;
            } else {
                return null;
            }
        }
        offset -= sliceLengthInFrames;
        lastOut = lastBuffer;
        lastBuffer = currentBuffer;
        currentBuffer = null;
        return lastOut;
    }

    private void addToInternalBuffers(final AudioBuffer in) {
        final float[] inReal = in.getRealData();
        final float[] inImaginary = in.getImaginaryData();
        // add to last
        if (lastBuffer == null) {
            lastBuffer = new ComplexAudioBuffer(in.getFrameNumber(), new float[sliceLengthInFrames], new float[sliceLengthInFrames], in.getAudioFormat());
        }
        final float[] lbReal = lastBuffer.getRealData();
        final float[] lbImaginary = lastBuffer.getImaginaryData();
        final int valuesToCopyToLastBuffer = Math.min(sliceLengthInFrames, inReal.length + offset) - offset;
        for (int i=0; i<valuesToCopyToLastBuffer; i++) {
            lbReal[i+offset] += inReal[i];
            lbImaginary[i+offset] += inImaginary[i];
        }
        if (inReal.length+offset > sliceLengthInFrames) {
            // add to current
            if (currentBuffer == null) {
                currentBuffer = new ComplexAudioBuffer(lastBuffer.getFrameNumber() + sliceLengthInFrames, new float[sliceLengthInFrames], new float[sliceLengthInFrames], in.getAudioFormat());
            }
            final float[] cReal = currentBuffer.getRealData();
            final float[] cImaginary = currentBuffer.getImaginaryData();
            final int valuesToCopyToCurrentBuffer = inReal.length - valuesToCopyToLastBuffer;
            for (int i = 0; i < valuesToCopyToCurrentBuffer; i++) {
                cReal[i] += inReal[i - offset + sliceLengthInFrames];
                cImaginary[i] += inImaginary[i - offset + sliceLengthInFrames];
            }
        }
        offset += hopSizeInFrames;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OLA that = (OLA) o;

        if (hopSizeInFrames != that.hopSizeInFrames) return false;
        if (sliceLengthInFrames != that.sliceLengthInFrames) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hopSizeInFrames;
        result = 31 * result + sliceLengthInFrames;
        return result;
    }

    @Override
    public String toString() {
        return "OLA{" +
                "window=" + sliceLengthInFrames +
                ", hop=" + hopSizeInFrames +
                '}';
    }
}
