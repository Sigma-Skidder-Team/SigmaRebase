/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.Arrays;

/**
 * Provides overlapping frame blocks (aka slices or windows or frames) of a defined size.
 * This is the counterpart of {@link SlidingWindow}.
 * <br/>
 * If no new data can be obtained the last few blocks/windows are zero padded until
 * all data disappeared from the window. Only then {@link #read()} returns <code>null</code>.
 * <br/>
 * Note that the <i>pull</i> API makes heavy object re-use. Do <em>not</em> rely on returned
 * buffers being immutable. If you need to keep a buffer around for longer than the call in which
 * it was given to you, {@link Object#clone()} it.
 * <p/>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see OLA
 */
public class SlidingWindow extends AbstractSignalProcessor<AudioBuffer, AudioBuffer> {

    // TODO: This class looks like hell and needs some refactoring love urgently! (hs)

    private int hopSizeInFrames = 1024;
    private int sliceLengthInFrames = 2048;
    private float[] lastOutput;
    private float[] lastInput;
    private int lastInputPosition;
    private int lastOutputPosition;
    private AudioFormat audioFormat;
    private float[] pushOutput;
    private float[] pushOutput2;
    private int outputPosition;
    private float[] pullOutput;
    private RealAudioBuffer pullRealAudioBuffer;
    private int readFrames;
    private int frameNumberOffset = -1;

    /**
     * Creates a processor with a window length of 2048 frames and 1024 hop size.
     */
    public SlidingWindow() {
    }

    /**
     * Produces windows of frames that overlap to a certain degree.
     *
     * @param sliceLengthInFrames frames per window
     * @param hopSizeInFrames number of frames two consecutive windows overlap
     */
    public SlidingWindow(final int sliceLengthInFrames, final int hopSizeInFrames) {
        setHopSizeInFrames(hopSizeInFrames);
        setSliceLengthInFrames(sliceLengthInFrames);
    }

    /**
     * Produces windows with a given frame length. The windows overlap by half their length.
     *
     * @param sliceLengthInFrames frames per window
     */
    public SlidingWindow(final int sliceLengthInFrames) {
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

    private int getCurrentFrameNumber() {
        return readFrames + frameNumberOffset;
    }

    public void reset() {
        super.reset();
        lastOutput = null;
        lastInput = null;
        lastInputPosition = 0;
        lastOutputPosition = 0;
        audioFormat = null;
        pushOutput = null;
        outputPosition = 0;
        pullRealAudioBuffer = null;
        readFrames = 0;
        frameNumberOffset = -1;
    }

    @Override
    protected AudioBuffer processNext(final AudioBuffer buffer) throws IOException {
        throw new RuntimeException("This method in not implemented");
    }

    @Override
    public void flush() throws IOException {
        while (outputPosition > 0) {
            if (pullRealAudioBuffer == null) {
                pullRealAudioBuffer = new RealAudioBuffer(getCurrentFrameNumber(), pushOutput, audioFormat);
            } else {
                pullRealAudioBuffer.reuse(getCurrentFrameNumber(), pushOutput, pullRealAudioBuffer.getAudioFormat());
            }
            signalProcessorSupport.process(pullRealAudioBuffer);
            readFrames += hopSizeInFrames;
            if (outputPosition > hopSizeInFrames) {
                final float[] newOutput = new float[pushOutput.length];
                System.arraycopy(pushOutput, hopSizeInFrames, newOutput, 0, pushOutput.length-hopSizeInFrames);
                pushOutput = newOutput;
                outputPosition -= hopSizeInFrames;
            } else {
                outputPosition = 0;
            }
        }
        super.flush();
    }

    @Override
    public void process(final AudioBuffer buffer) throws IOException {
        if (hopSizeInFrames > sliceLengthInFrames) {
            throw new IllegalArgumentException("hopSizeInFrames " + hopSizeInFrames
                    + " must not be greater than sliceLengthInFrames" + sliceLengthInFrames);
        }

        final float[] data = buffer.getData();
        audioFormat = buffer.getAudioFormat();
        if (frameNumberOffset == -1) frameNumberOffset = buffer.getFrameNumber();
        int dataPosition = 0;

        if (pushOutput == null) {
            pushOutput = new float[sliceLengthInFrames];
            outputPosition = 0;
        }

        if (lastOutput != null) {
            // copy stuff we already have in the old output
            outputPosition = lastOutput.length-hopSizeInFrames;
            System.arraycopy(lastOutput, hopSizeInFrames, pushOutput, 0, outputPosition);
        }

        while (outputPosition < pushOutput.length && dataPosition < data.length) {
            final int framesToCopy = Math.min(data.length - dataPosition, pushOutput.length - outputPosition);
            System.arraycopy(data, dataPosition, pushOutput, outputPosition, framesToCopy);
            dataPosition += framesToCopy;
            outputPosition += framesToCopy;
            if (outputPosition == pushOutput.length) {
                if (pullRealAudioBuffer == null) {
                    pullRealAudioBuffer = new RealAudioBuffer(getCurrentFrameNumber(), pushOutput, audioFormat);
                } else {
                    pullRealAudioBuffer.reuse(getCurrentFrameNumber(), pushOutput, pullRealAudioBuffer.getAudioFormat());
                }
                signalProcessorSupport.process(pullRealAudioBuffer);
                readFrames += hopSizeInFrames;

                // re-use already existing buffer
                lastOutput = pushOutput;
                if (pushOutput2 == null) {
                    pushOutput = new float[sliceLengthInFrames];
                } else {
                    pushOutput = pushOutput2;
                    Arrays.fill(pushOutput, 0);
                }
                pushOutput2 = lastOutput;

                //lastOutput = pushOutput;
                //pushOutput = new float[sliceLengthInFrames];
                outputPosition = lastOutput.length-hopSizeInFrames;
                System.arraycopy(lastOutput, hopSizeInFrames, pushOutput, 0, outputPosition);
            }
        }
        lastOutput = null;
    }

    /**
     * If no new data can be obtained the last few blocks/windows are zero padded until
     * all data disappeared from the window. Only then {@link #read()} returns <code>null</code>.
     *
     * @return sliding windowed view on the generator's data.
     * @throws IOException if something goes wrong
     */
    @Override
    public AudioBuffer read() throws IOException {
        if (hopSizeInFrames > sliceLengthInFrames) {
            throw new IllegalArgumentException("hopSizeInFrames " + hopSizeInFrames
                    + " must not be greater than sliceLengthInFrames" + sliceLengthInFrames);
        }
        if (pullOutput == null) {
            pullOutput = new float[sliceLengthInFrames];
        }

        int outputPosition = 0;
        // do we have some old values? then copy them..
        if (hopSizeInFrames == sliceLengthInFrames) {
            // do nothing, no overlap!
        } else if (lastOutput == null) {
            lastOutput = new float[sliceLengthInFrames];
        } else {
            // copy stuff we already have in the old output output
            outputPosition = Math.max(lastOutputPosition-hopSizeInFrames, 0);
            System.arraycopy(lastOutput, hopSizeInFrames, pullOutput, 0, outputPosition);
        }

        while (outputPosition < pullOutput.length) {
            if (lastInput == null || lastInputPosition == lastInput.length) {
                final AudioBuffer buffer = getConnectedSource().read();
                if (frameNumberOffset == -1 && buffer != null) frameNumberOffset = buffer.getFrameNumber();
                lastInput = buffer == null ? null : buffer.getData();
                if (buffer != null) {
                    audioFormat = buffer.getAudioFormat();
                    if (audioFormat != null && audioFormat.getChannels() != 1) {
                        throw new IOException("Source must be mono.");
                    }
                }
                lastInputPosition = 0;
            }
            if (lastInput == null) break;

            final int framesToCopy = Math.min(lastInput.length - lastInputPosition, pullOutput.length - outputPosition);
            System.arraycopy(lastInput, lastInputPosition, pullOutput, outputPosition, framesToCopy);
            lastInputPosition += framesToCopy;
            outputPosition += framesToCopy;
        }

        if (outputPosition == 0) {
            lastOutput = null;
            return null;
        //} else if (outputPosition < output.length) {
        //    lastOutput = null;
        //    new float[outputPosition];
        } else {
            lastOutput = pullOutput;
        }
        // since we are re-using the array, zero out stuff we didn't overwrite
        if (outputPosition < pullOutput.length) {
            for (int i=outputPosition; i<pullOutput.length; i++) {
                pullOutput[i] = 0;
            }
        }
        lastOutputPosition = outputPosition;
        if (pullRealAudioBuffer == null) {
            pullRealAudioBuffer = new RealAudioBuffer(getCurrentFrameNumber(), pullOutput, audioFormat);
        } else {
            pullRealAudioBuffer.reuse(getCurrentFrameNumber(), pullOutput, pullRealAudioBuffer.getAudioFormat());
        }
        readFrames += hopSizeInFrames;
        return pullRealAudioBuffer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlidingWindow that = (SlidingWindow) o;

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
        return "SlidingWindow{" +
                "window=" + sliceLengthInFrames +
                ", hop=" + hopSizeInFrames +
                '}';
    }
}
