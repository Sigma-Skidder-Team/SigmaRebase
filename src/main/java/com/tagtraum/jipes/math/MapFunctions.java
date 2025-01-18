/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Common map functions.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see MapFunction
 */
public final class MapFunctions {

    private MapFunctions() {}

    /**
     * @see <a href="http://www.mathworks.com/help/toolbox/signal/xcorr.html">Matlab&trade; function <code>xcorr</code></a>
     */
    public static final MapFunction<float[]> AUTO_CORRELATION = new MapFunction<float[]>() {
        public float[] map(final float[] samples) {
            return Floats.autoCorrelation(samples);
        }

        @Override
        public String toString() {
            return "AUTO_CORRELATION";
        }
    };

    /**
     * Auto correlation function normalized with the first coefficient (global maximum).
     * I.e. all values are between 0 and 1.
     *
     * @see <a href="http://www.mathworks.com/help/toolbox/signal/xcorr.html">Matlab&trade; function <code>xcorr(..., 'coeff')</code></a>
     */
    public static final MapFunction<float[]> AUTO_CORRELATION_COEFF = new MapFunction<float[]>() {
        public float[] map(final float[] samples) {
            final float[] floats = Floats.autoCorrelation(samples);
            final float zeroLag = floats[0];
            for (int i=0; i<floats.length; i++) {
                floats[i] = floats[i]/zeroLag;
            }
            return floats;
        }

        @Override
        public String toString() {
            return "AUTO_CORRELATION_COEFF";
        }
    };

    /**
     * Auto correlation function normalized with the number of samples, i.e. <code>biased(t) = r(t)/N</code>.
     *
     * @see <a href="http://www.mathworks.com/help/toolbox/signal/xcorr.html">Matlab&trade; function <code>xcorr(..., 'biased')</code></a>
     */
    public static final MapFunction<float[]> AUTO_CORRELATION_BIASED = new MapFunction<float[]>() {
        public float[] map(final float[] samples) {
            final float[] floats = Floats.autoCorrelation(samples);
            final float samplesLength = samples.length;
            for (int i=0; i<floats.length; i++) {
                floats[i] = floats[i]/samplesLength;
            }
            return floats;
        }

        @Override
        public String toString() {
            return "AUTO_CORRELATION_BIASED";
        }
    };

    /**
     * Auto correlation function normalized with the number of samples minus abs(t),
     * i.e. <code>unbiased(t) = r(t)/(N-|t|)</code>
     *
     * @see <a href="http://www.mathworks.com/help/toolbox/signal/xcorr.html">Matlab&trade; function <code>xcorr(..., 'unbiased')</code></a>
     */
    public static final MapFunction<float[]> AUTO_CORRELATION_UNBIASED = new MapFunction<float[]>() {
        public float[] map(final float[] samples) {
            final float[] floats = Floats.autoCorrelation(samples);
            final float samplesLength = samples.length;
            for (int i=0; i<floats.length; i++) {
                floats[i] = floats[i]/(samplesLength-i);
            }
            return floats;
        }

        @Override
        public String toString() {
            return "AUTO_CORRELATION_UNBIASED";
        }
    };

    /**
     * Removes sign.
     *
     * @return array with absolute values
     */
    public static MapFunction<float[]> createAbsFunction() {
        return new AbsFunction();
    }

    /**
     * Squares all values.
     *
     * @return arrays of squared values
     */
    public static MapFunction<float[]> createSquareFunction() {
        return new SquareFunction();
    }

    /**
     * Divides all values by the Euclidean norm of the the given buffer.
     * @return normalization
     */
    public static MapFunction<float[]> createEuclideanNormalization() {
        return new FloatNormalization() {
            public double getDivisor(final float[] buffer) {
                return Floats.euclideanNorm(buffer);
            }

            @Override
            public String toString() {
                return "EUCLIDEAN_NORMALIZATION";
            }
        };
    }

    /**
     * Divides all values by the max value in the given buffer.
     * @return normalization
     */
    public static MapFunction<float[]> createDivByMaxNormalization() {
        return new FloatNormalization() {
            public double getDivisor(final float[] buffer) {
                return Floats.max(buffer);
            }

            @Override
            public String toString() {
                return "DIV_BY_MAX_NORMALIZATION";
            }
        };
    }

    /**
     * Assumes that all values are signed 16bit values and divides them by {@link Short#MAX_VALUE}+1 in
     * order to map them to a range of -1 to 1.
     * @return normalization
     */
    public static MapFunction<float[]> createShortToOneNormalization() {
        return new ShortToOneNormalization();
    }

    /**
     * Reverses the order of the elements in <em>each</em> array.
     *
     * @return array with reversed order
     */
    public static MapFunction<float[]> createReverseFunction() {
        return new ReverseFunction();
    }

    /**
     * Creates wrap function.
     *
     * @param length length
     * @return wrap function
     */
    public static MapFunction<float[]> createWrapFunction(final int length) {
        return new WrapFunction(length);
    }

    /**
     * Creates a linear interpolation function.
     *
     * @param shift shift amount between -1 and 1
     * @param indicesPerOneShift number of indices the values need to be shifted, when the (normalized) shift is 1.
     * @return linear interpolation function
     */
    public static MapFunction<float[]> createInterpolateFunction(final float shift, final int indicesPerOneShift) {
        return new InterpolateFunction(shift, indicesPerOneShift);
    }

    /**
     * Maps consecutive samples to their temporal centroid.
     * This can e.g. be used in a {@link com.tagtraum.jipes.universal.Mapping}.
     *
     * @return mean function
     */
    public static StatefulMapFunction<Float> createTemporalCentroidFunction() {
        return new TemporalCentroidFunction();
    }

    /**
     * Maps consecutive samples to their variance.
     * This can e.g. be used in a {@link com.tagtraum.jipes.universal.Mapping}.
     *
     * @return variance function
     */
    public static StatefulMapFunction<Float> createVarianceFunction() {
        return new VarianceFunction();
    }

    /**
     * Maps consecutive samples to their standard deviation.
     * This can e.g. be used in a {@link com.tagtraum.jipes.universal.Mapping}.
     *
     * @return standard deviation function
     */
    public static StatefulMapFunction<Float> createStandardDeviationFunction() {
        return new StandardDeviationFunction();
    }

    /**
     * Maps consecutive samples to their arithmetic mean.
     * This can e.g. be used in a {@link com.tagtraum.jipes.universal.Mapping}.
     *
     * @return mean function
     */
    public static StatefulMapFunction<Float> createArithmeticMeanFunction() {
        return new ArithmeticMeanFunction();
    }

    /**
     * Keeps track of values that are below a given threshold and returns the fraction
     * of values that were below this threshold.
     *
     * @param threshold threshold (excl.)
     * @return fraction function
     */
    public static StatefulMapFunction<Float> createFractionFunction(final float threshold) {
        return new StatefulMapFunction<Float>() {

            private int count;
            private int below;

            public void reset() {
                count = 0;
                below = 0;
            }

            public Float map(final Float x) {
                count++;
                if (x < threshold) below++;
                return below / (float)count;
            }

            @Override
            public String toString() {
                return "FRACTION_BELOW_" + threshold;
            }
        };
    }

    private abstract static class FloatNormalization implements MapFunction<float[]> {

        private float[] normalizedData;

        /**
         * Divisor to normalize all elements of the given buffer.
         * Note that you are not supposed to manipulate the buffer itself in any way.
         *
         * @param buffer audio buffer with numbers we want to normalize
         * @return the number all elements of the buffer will be divided by.
         */
        public abstract double getDivisor(final float[] buffer);

        /**
         * Divides all elements of the audio buffer by {@link #getDivisor(float[])}
         *
         * @param buffer buffer
         * @return normalized buffer
         */
        public float[] map(final float[] buffer) {
            final double divisor = getDivisor(buffer);
            final int length = buffer.length;
            if (normalizedData == null || normalizedData.length != length) normalizedData = new float[length];
            for (int i=0; i<length; i++) {
                normalizedData[i] = (float)(buffer[i]/divisor);
            }
            return normalizedData;
        }
    }

    private static class VarianceFunction extends ArithmeticMeanFunction {

        protected double variance = 0;

        public void reset() {
            super.reset();
            variance = 0;
        }

        public Float map(final Float x) {
            // see http://boost-sandbox.sourceforge.net/libs/accumulators/doc/html/boost/accumulators/impl/variance_impl.html
            super.map(x);
            if (count == 1) variance = 0;
            else variance = (count - 1d) / count * variance + 1d/(count - 1d)*(x-average)*(x-average);
            return (float)variance;
        }

        @Override
        public String toString() {
            return "VARIANCE";
        }
    }

    private static class ArithmeticMeanFunction implements StatefulMapFunction<Float> {

        protected double average = 0;
        protected int count = 0;

        public void reset() {
            count = 0;
            average = 0;
        }

        public Float map(final Float x) {
            count++;
            average += (x - average) / count;
            //System.out.println(average);
            return (float)average;
        }

        @Override
        public String toString() {
            return "ARITHMETIC_MEAN";
        }
    }

    private static class StandardDeviationFunction extends VarianceFunction {

        @Override
        public Float map(final Float x) {
            super.map(x);
            return (float)Math.sqrt(variance);
        }

        @Override
        public String toString() {
            return "STANDARD_DEVIATION";
        }
    }

    private static class TemporalCentroidFunction implements StatefulMapFunction<Float> {

        private double numerator;
        private double denominator;
        private int count = 0;

        public void reset() {
            count = 0;
            numerator = 0;
            denominator = 0;
        }

        public Float map(final Float x) {
            count++;
            denominator += x;
            numerator += x * count;
            if (denominator == 0) return Float.NaN;
            return (float)(numerator/denominator);
        }

        @Override
        public String toString() {
            return "TEMPORAL_CENTROID";
        }
    }

    private static class InterpolateFunction implements MapFunction<float[]> {
        private float shift;
        private int indicesPerOneShift;

        private InterpolateFunction(final float shift, final int indicesPerOneShift) {
            this.shift = shift;
            this.indicesPerOneShift = indicesPerOneShift;
        }

        public float[] map(final float[] data) {
            return Floats.interpolate(data, shift, indicesPerOneShift);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final InterpolateFunction that = (InterpolateFunction) o;

            if (indicesPerOneShift != that.indicesPerOneShift) return false;
            if (Float.compare(that.shift, shift) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (shift != +0.0f ? Float.floatToIntBits(shift) : 0);
            result = 31 * result + indicesPerOneShift;
            return result;
        }

        @Override
        public String toString() {
            return "InterpolateFunction{" +
                    "indicesPerOneShift=" + indicesPerOneShift +
                    ", shift=" + shift +
                    '}';
        }
    }

    private static class WrapFunction implements MapFunction<float[]> {

        private final int length;

        public WrapFunction(final int length) {
            this.length = length;
        }

        public float[] map(final float[] data) {
            return Floats.wrap(data, length);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WrapFunction that = (WrapFunction) o;

            if (length != that.length) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return length;
        }

        @Override
        public String toString() {
            return "WRAP_" + length;
        }
    }

    private static class AbsFunction implements MapFunction<float[]> {
        
        private float[] out;
        
        public float[] map(final float[] data) {
            if (out == null || out.length != data.length) {
                out = new float[data.length];
            }
            for (int i=0; i<data.length; i++) {
                out[i] = Math.abs(data[i]);
            }
            return out;
        }

        @Override
        public String toString() {
            return "ABS";
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof AbsFunction;
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }

    private static class SquareFunction implements MapFunction<float[]> {

        private float[] out;

        public float[] map(final float[] data) {
            if (out == null || out.length != data.length) {
                out = new float[data.length];
            }
            for (int i=0; i<data.length; i++) {
                final float temp = data[i];
                out[i] = temp * temp;
            }
            return out;
        }

        @Override
        public String toString() {
            return "SQUARE";
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof SquareFunction;
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }

    private static class ReverseFunction implements MapFunction<float[]> {

        private float[] out;

        public float[] map(final float[] data) {
            if (out == null || out.length != data.length) {
                out = new float[data.length];
            }
            for (int i=0; i<data.length; i++) {
                out[data.length-1-i] = data[i];
            }
            return out;
        }

        @Override
        public String toString() {
            return "REVERSE";
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ReverseFunction;
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }

    private static final class ShortToOneNormalization extends FloatNormalization {

        public double getDivisor(final float[] buffer) {
            return Short.MAX_VALUE + 1;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ShortToOneNormalization;
        }

        @Override
        public String toString() {
            return "SHORT_TO_ONE_NORMALIZATION";
        }

    }

}
