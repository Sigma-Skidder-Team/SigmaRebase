/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

/**
 * Read-only representation of an (audio) spectrum.
 * <p/>
 * Date: 1/13/11
 * Time: 2:19 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface AudioSpectrum extends AudioBuffer {

    /**
     * Array with frequency values in Hz corresponding to the bin numbers.
     * <p/>
     * <em>This array is meant for reading only. Do not manipulate it!</em>
     *
     * @return array with frequencies - length may be numberOfSamples/2 due to symmetry
     * @see #getFrequency(int)
     */
    float[] getFrequencies();

    /**
     * Returns the frequency of a given bin. If you need the frequencies for the whole
     * spectrum you might rather use {@link #getFrequencies()}, as it is more efficient.
     *
     * @param bin bin number
     * @return frequency in Hz
     * @see #getFrequencies()
     */
    float getFrequency(int bin);

    /**
     * Computes a bin/index number for the given frequency.
     *
     * @param frequency frequency
     * @return bin
     */
    int getBin(float frequency);

    /**
     * Creates a copy of this spectrum, but replaces its values with the given real and imaginary data.
     * The original object remains unchanged.
     *
     * @param real real data
     * @param imaginary imaginary data
     * @return derived copy
     */
    public AudioSpectrum derive(float[] real, float[] imaginary);

}
