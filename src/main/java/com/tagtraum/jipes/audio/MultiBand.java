/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;

import java.io.IOException;
import java.util.Arrays;

/**
 * Assumes that the input is spectral data produced by some {@link com.tagtraum.jipes.math.Transform}
 * and sums up the powers into bins, which are spaced according to the
 * provided frequency boundaries. Magnitudes are computed as square roots of the powers,
 * the sum of the powers stays constant.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.audio.MultiBandSpectrum#createLogarithmicBands(float, float, int)
 */
public class MultiBand<T extends AudioSpectrum> extends AbstractSignalProcessor<T, MultiBandSpectrum> {

    private float[] bandBoundaries;

    public MultiBand(final float[] bandBoundaries) {
        this.bandBoundaries = bandBoundaries;
    }

    public MultiBand() {
    }

    public float[] getBandBoundaries() {
        return bandBoundaries;
    }

    public void setBandBoundaries(final float[] bandBoundaries) {
        this.bandBoundaries = bandBoundaries;
    }

    protected MultiBandSpectrum processNext(final T audioSpectrum) throws IOException {
        if (bandBoundaries == null) {
            throw new IllegalStateException("No boundaries set.");
        }
        if (audioSpectrum.getAudioFormat() != null && audioSpectrum.getAudioFormat().getChannels() != 1) {
            throw new IOException("Source must be mono.");
        }
        return new MultiBandSpectrum(audioSpectrum.getFrameNumber(), audioSpectrum, bandBoundaries);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MultiBand that = (MultiBand) o;
        if (!Arrays.equals(bandBoundaries, that.bandBoundaries)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bandBoundaries);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (bandBoundaries != null && bandBoundaries.length>0) {
            for (final float boundary : bandBoundaries) {
                sb.append(boundary).append(',');
            }
            if (sb.length()>0) sb.deleteCharAt(sb.length()-1);
        } else {
            sb.append("none");
        }
        return "MultiBand{" +
                "bandBoundaries=" + sb +
                '}';
    }
}
