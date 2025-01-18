/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.Arrays;

/**
 * Multirate filters for decimating/interpolating and resampling.
 * <p/>
 * Date: 10/2/11
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://www.dspguru.com/dsp/faqs/multirate">Multirate FAQ on dspguru.com</a>
 */
public final class MultirateFilters {

    private MultirateFilters() {
    }

    /**
     * Decimator based on coefficients of a given {@link com.tagtraum.jipes.math.Filters.FIRFilter}.
     * Only every <code>n</code>th sample is actually filtered, all others are dropped.
     * Thus this class combines downsampling <em>and</em> filtering (to remove aliasing).
     */
    public static class Decimator extends Filters.FIRFilter {

        private int factor = 2;
        private int pos;
        private float[] out;

        @Override
        public void reset() {
            super.reset();
            pos = 0;
        }

        /**
         * Creates a decimator using a simple fir1 16th order low pass filter.
         *
         * @param factor nth frame to keep (factor)
         * @throws IllegalArgumentException if the decimation factor is not supported
         * @see Filters#createFir1_16thOrderLowpass(int)
         */
        public Decimator(final int factor) throws IllegalArgumentException {
            super(Filters.createFir1_16thOrderLowpass(factor).getCoefficients());
            this.factor = factor;
        }

        /**
         * Creates a decimator with the given FIR coefficients.
         *
         * @param coefficients FIR coefficients
         * @param factor nth frame to keep (factor)
         */
        public Decimator(final double[] coefficients, final int factor) {
            super(coefficients);
            this.factor = factor;
        }

        /**
         * Creates a decimator with the given FIR (low pass) filter.
         *
         * @param filter FIR filter
         * @param factor nth frame to keep (factor)
         */
        public Decimator(final Filters.FIRFilter filter, final int factor) {
            super(filter.getCoefficients());
            this.factor = factor;
        }

        /**
         * Factor by which the signal is downsampled.
         *
         * @return factor
         */
        public int getFactor() {
            return factor;
        }

        public float[] map(final float[] data) {
            final int outLength = (int) Math.ceil(data.length / (float) factor);
            if (out == null || out.length != outLength) {
                out = new float[outLength];
            }
            int j = 0;
            for (final float sample : data) {
                addToDelayLine(sample);
                if ((pos % factor) == 0) {
                    out[j] = (float) filter();
                    j++;
                }
                pos = (pos + 1) % factor;
            }
            return out;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            final Decimator decimator = (Decimator) o;

            if (factor != decimator.factor) return false;
            return Arrays.equals(getCoefficients(), decimator.getCoefficients());
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + factor;
            return result;
        }

        @Override
        public String toString() {
            return "Decimator{" +
                    "factor=" + factor +
                    '}';
        }

    }

    /**
     * Interpolator based on coefficients of a given {@link com.tagtraum.jipes.math.Filters.FIRFilter}.
     * This class combines upsampling (i.e. zero stuffing) <em>and</em> filtering (to avoid aliasing).
     */
    public static class Interpolator implements StatefulMapFunction<float[]> {

        private int factor;
        private Filters.FIRFilter[] filters;
        private double[] originalCoefficients;
        private float[] out;

        /**
         * Creates an interpolator using a simple fir1 16th order low pass filter.
         *
         * @param factor upsample factor
         * @throws IllegalArgumentException if the upsample factor is not supported
         * @see Filters#createFir1_16thOrderLowpass(int)
         */
        public Interpolator(final int factor) {
            this(Filters.createFir1_16thOrderLowpass(factor).getCoefficients(), factor);
        }

        /**
         * Creates an interpolator with the given FIR coefficients for filtering after upsampling to avoid aliasing.
         * It's recommended to use a multiple of the upsample factor as the number of coefficients. If that's not the
         * case, zeros are added to the coefficients.
         *
         * @param coefficients FIR filter coefficients
         * @param factor upsample factor
         */
        public Interpolator(final double[] coefficients, final int factor) {
            this.factor = factor;
            this.originalCoefficients = coefficients.clone();
            setCoefficients(coefficients);
        }

        /**
         * Creates an interpolator with the given FIR filter for filtering after upsampling to avoid aliasing.
         * It's recommended to use a filter with a multiple of the upsample factor as the number of coefficients (taps).
         * If that's not the case, zeros are added to the filter's coefficients.
         *
         * @param filter FIR filter
         * @param factor upsample factor
         */
        public Interpolator(final Filters.FIRFilter filter, final int factor) {
            this(filter.getCoefficients(), factor);
        }

        private void setCoefficients(final double[] coefficients) {
            // make sure we have a multiple
            final double[] actualCoefficients;
            if (coefficients.length % factor == 0) {
                actualCoefficients = coefficients;
            } else {
                // append zeros, if coefficients.length isn't a multiple of factor
                actualCoefficients = new double[(coefficients.length/factor + 1)*factor];
                System.arraycopy(coefficients, 0, actualCoefficients, 0, coefficients.length);
            }
            // create multiple FIRFilters
            filters = new Filters.FIRFilter[factor];
            for (int i=0; i<factor; i++) {
                final double[] subCoefficients = new double[actualCoefficients.length / factor];
                for (int j=0; j<subCoefficients.length; j++) {
                    subCoefficients[j] = actualCoefficients[i + j*factor];
                }
                filters[i] = new Filters.FIRFilter(subCoefficients);
            }
        }

        public void reset() {
            setCoefficients(originalCoefficients);
        }

        /**
         * Get upsample factor.
         *
         * @return upsample factor
         */
        public int getFactor() {
            return factor;
        }

        public float[] map(final float[] data) {
            final int outLength = data.length * factor;
            if (out == null || out.length != outLength) {
                out = new float[outLength];
            } else {
                Arrays.fill(out, 0);
            }
            for (int i=0; i<data.length; i++) {
                final float sample = data[i];
                for (int j=0; j<factor; j++) {
                    final Filters.FIRFilter filter = filters[j];
                    filter.addToDelayLine(sample);
                    out[i*factor+j] = (float)filter.filter();
                }
            }
            return out;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Interpolator that = (Interpolator) o;

            if (factor != that.factor) return false;
            return Arrays.equals(originalCoefficients, that.originalCoefficients);

        }

        @Override
        public int hashCode() {
            int result = factor;
            result = 31 * result + (originalCoefficients != null ? Arrays.hashCode(originalCoefficients) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Interpolator{" +
                    "factor=" + factor +
                    '}';
        }
    }

    /**
     * Resamples the input by upsampling, low pass filtering and then downsampling by
     * the given factors. The implementation aims for efficiency by not computing samples
     * that are later dropped anyway.
     */
    public static class Resampler implements StatefulMapFunction<float[]> {

        private int upFactor;
        private int downFactor;
        private Filters.FIRFilter[] filters;
        private double[] originalCoefficients;

        /**
         * Creates a resampler using a simple fir1 16th order low pass filter and the given up- and down-sample
         * factors. The filter used is based on the maximum of the two factors. Internally the up- and down-factors
         * are divided by their greatest common divisor to increase efficiency.
         *
         * @param upFactor upsample factor
         * @param downFactor downsample factor
         * @throws IllegalArgumentException if a filter for the factor <code>max(upFactor,downFactor)</code> is not supported
         * @see Filters#createFir1_16thOrderLowpass(int)
         */
        public Resampler(final int upFactor, final int downFactor) {
            this(createFilter(upFactor, downFactor).getCoefficients(), upFactor, downFactor);
        }

        /**
         * Creates a resampler using a FIR filter based on the given coefficients and the given up- and down-sample
         * factors. The coefficients must be appropriate for the given factors. Internally the up- and down-factors
         * are divided by their greatest common divisor to increase efficiency.
         *
         * @param coefficients FIR filter coefficients
         * @param upFactor upsample factor
         * @param downFactor downsample factor
         */
        public Resampler(final double[] coefficients, final int upFactor, final int downFactor) {
            final int gcd = greatestCommonDivisor(upFactor, downFactor);
            this.upFactor = upFactor/gcd;
            this.downFactor = downFactor/gcd;
            this.originalCoefficients = coefficients.clone();
            setCoefficients(coefficients);
        }

        /**
         * Creates a resampler using a FIR low pass filter and the given up- and down-sample
         * factors. The filter must be appropriate for the given factors. Internally the up- and down-factors
         * are divided by their greatest common divisor to increase efficiency.
         *
         * @param filter FIR filter
         * @param upFactor upsample factor
         * @param downFactor downsample factor
         */
        public Resampler(final Filters.FIRFilter filter, final int upFactor, final int downFactor) {
            this(filter.getCoefficients(), upFactor, downFactor);
        }

        /**
         * Find the appropriate filter for the given up/down compbination.
         *
         * @param upFactor up sampling factor
         * @param downFactor down sampling factor
         * @return filter
         */
        private static Filters.FIRFilter createFilter(final int upFactor, final int downFactor) {
            final int gcd = greatestCommonDivisor(upFactor, downFactor);
            return Filters.createFir1_16thOrderLowpass(Math.max(upFactor/gcd, downFactor/gcd));
        }

        /**
         * Compute the greatest common divisor (gcd).
         *
         * @param x x
         * @param y y
         * @return greatest common divisor
         */
        private static int greatestCommonDivisor(final int x, final int y) {
            int a = x;
            int b = y;
            while (b != 0) {
                int h = a % b;
                a = b;
                b = h;
            }
            return a;
        }

        private void setCoefficients(final double[] coeff) {
            // make sure we have a multiple
            final double[] coefficients;
            if (coeff.length % this.upFactor == 0) {
                coefficients = coeff;
            } else {
                // append zeros, if coeff.length isn't a multiple of factor
                coefficients = new double[(coeff.length/this.upFactor + 1)*this.upFactor];
                System.arraycopy(coeff, 0, coefficients, 0, coeff.length);
            }
            // create multiple FIRFilters
            filters = new Filters.FIRFilter[upFactor];
            for (int i=0; i<upFactor; i++) {
                final double[] subCoefficients = new double[coefficients.length / upFactor];
                for (int j=0; j<subCoefficients.length; j++) {
                    subCoefficients[j] = coefficients[i + j*upFactor];
                }
                filters[i] = new Filters.FIRFilter(subCoefficients);
            }
        }

        public void reset() {
            setCoefficients(originalCoefficients);
        }

        /**
         * Up-sampling factor.
         *
         * @return factor
         */
        public int getUpFactor() {
            return upFactor;
        }

        /**
         * Down-sampling factor.
         *
         * @return factor
         */
        public int getDownFactor() {
            return downFactor;
        }

        /**
         * Resampling factor.
         *
         * @return factor
         */
        public float getFactor() {
            return upFactor/(float)downFactor;
        }

        public float[] map(final float[] data) {
            final float[] out = new float[data.length * upFactor / downFactor];
            for (int i=0; i<data.length; i++) {
                final float sample = data[i];
                for (int j=0; j<upFactor; j++) {
                    final Filters.FIRFilter filter = filters[j];
                    filter.addToDelayLine(sample);
                    final int upsampledIndex = i * upFactor + j;
                    if (upsampledIndex % downFactor == 0) {
                        out[upsampledIndex/downFactor] = (float)filter.filter();
                    }
                }
            }
            return out;
        }

        @Override
        public String toString() {
            return "Resampler{" +
                    "factor=" + upFactor + "/" + downFactor + " (" + getFactor() + ")" +
                    '}';
        }
    }
}
