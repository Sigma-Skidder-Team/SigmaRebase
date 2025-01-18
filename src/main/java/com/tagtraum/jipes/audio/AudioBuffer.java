/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import javax.sound.sampled.AudioFormat;
import java.util.concurrent.TimeUnit;

/**
 * Read-only buffer that holds (audio) data and some meta information in the form of an {@link javax.sound.sampled.AudioFormat}
 * instance. The data might be in the form of complex numbers and/or magnitudes.
 * <p/>
 * Note that even though you could, you <em>must never</em> change any of the float arrays contained in this buffer.
 * If you need to change one, copy/clone it first. This contract is not enforced programmatically, but relies on
 * the user adhering to it.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.SignalPullProcessor
 * @see com.tagtraum.jipes.SignalProcessor
 */
public interface AudioBuffer {

    /**
     * Time from the beginning of the track to the beginning of this buffer in ms.
     * For more accurate timestamps, use {@link #getTimestamp(java.util.concurrent.TimeUnit)} with a
     * finer unit.
     * <p/>
     * If we didn't start reading at the beginning of the file (i.e. timestamp == 0),
     * the offset may not be 100% precise.
     *
     * @return time in ms or -1, if it cannot be determined
     * @see #getFrameNumber()
     * @see #getTimestamp(java.util.concurrent.TimeUnit)
     */
    long getTimestamp();

    /**
     * Time from the beginning of the track to the beginning of this buffer in the requested unit.
     * If we didn't start reading at the beginning of the file (i.e. timestamp == 0),
     * the offset may not be 100% precise.
     *
     * @param timeUnit time unit
     * @return time in the given unit or -1, if it cannot be determined
     * @see #getTimestamp()
     */
    long getTimestamp(TimeUnit timeUnit);

    /**
     * Number of the first frame at the beginning of this buffer. In this context a <em>frame</em>
     * consists of one or more <em>samples</em>, depending on the number of <em>channels</em>.
     * E.g. one stereo frame contains two samples.
     * <p/>
     * Just like {@link #getTimestamp()}, this method may not be the most accurate, if you didn't
     * start reading at the beginning of the file.
     *
     * @return frame number
     * @see #getTimestamp()
     * @see AudioFormat
     */
    int getFrameNumber();
    
    /**
     * Describes the audio data contained in this buffer.
     *
     * @return audio format
     */
    AudioFormat getAudioFormat();

    /**
     * Returns a real representation of the data held by this buffer. Note that this is not necessarily the same
     * as <em>samples</em>. In fact this could be anything, e.g. magnitudes. More info might be found in
     * {@link #getAudioFormat()}.
     * <p/>
     * <em>This array is meant for reading only. Do not manipulate it!</em>
     *
     * @return audio data
     * @see com.tagtraum.jipes.audio.LinearFrequencySpectrum#getData()
     */
    float[] getData();

    /**
     * Real part of the data contained in this buffer.
     * If the imaginary part is always zero, this returns the same as {@link #getData()}
     * <p/>
     * <em>This array is meant for reading only. Do not manipulate it!</em>
     *
     * @return real part
     */
    float[] getRealData();

    /**
     * Imaginary part of the data contained in this buffer.
     * <p/>
     * <em>This array is meant for reading only. Do not manipulate it!</em>
     *
     * @return imaginary part
     */
    float[] getImaginaryData();

    /**
     * Powers (sum of the squares of the real and imaginary part).
     * <p/>
     * <em>This array is meant for reading only. Do not manipulate it!</em>
     *
     * @return array
     * @see #getMagnitudes()
     */
    float[] getPowers();

    /**
     * Magnitudes (square root of the powers).
     * <p/>
     * <em>This array is meant for reading only. Do not manipulate it!</em>
     *
     * @return array
     * @see #getPowers()
     */
    float[] getMagnitudes();

    /**
     * Returns the number of samples this buffer was built from.
     *
     * @return number of samples
     */
    int getNumberOfSamples();

    /**
     * Creates a copy of this buffer, but replaces its values with the given real and imaginary data.
     * The original buffer remains unchanged.
     *
     * @param real real data
     * @param imaginary imaginary data
     * @return derived copy
     */
    public AudioBuffer derive(float[] real, float[] imaginary);

    /**
     * Creates an exact copy of this buffer.
     * All AudioBuffers must be cloneable.
     *
     * @return clone
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;

}
