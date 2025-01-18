/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.Arrays;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

/**
 * <p>Classic window functions like triangle, Hamming and Hann.</p>
 * <p><em>Note that some implementations may re-use their output buffer!</em></p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://en.wikipedia.org/wiki/Window_function">Window functions on Wikipedia</a>
 */
public abstract class WindowFunction implements MapFunction<float[]> {

    private static final double DOUBLE_PI = 2.0 * PI;
    private float[] coefficients;
    private float[] out;
    private int length;

    public WindowFunction(final float[] coefficients) {
        this.coefficients = coefficients;
        this.length = coefficients.length;
        this.out = new float[length];
    }

    public float[] getCoefficients() {
        return coefficients.clone();
    }

    public int getLength() {
        return length;
    }

    public float[] map(final float[] data) {
        if (data.length != coefficients.length) throw new IllegalArgumentException("Data length must equal coefficients length.");
        for (int i=0; i<data.length; i++) {
            out[i] = data[i] * coefficients[i];
        }
        return out;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof WindowFunction)) return false;
        final WindowFunction that = (WindowFunction) o;
        return Arrays.equals(coefficients, that.coefficients);
    }

    @Override
    public int hashCode() {
        return coefficients != null ? Arrays.hashCode(coefficients) : 0;
    }

    /**
     * Inverts this window function. I.e. each coefficient is replaced with {@code 1/x}.
     *
     * @return inverse window function
     */
    public WindowFunction invert() {
        return new InverseWindowFunction(this);
    }

    /**
     * Triangle function that always uses a triangle that is as long as the provided data.
     */
    public static MapFunction<float[]> TRIANGLE = new MapFunction<float[]>() {
        public float[] map(final float[] array) {
            return new Triangle(array.length).map(array);
        }

        @Override
        public String toString() {
            return "TRIANGLE_WINDOW";
        }
    };

    /**
     * Function that always uses a Hann window that is as long as the provided data.
     */
    public static MapFunction<float[]> HANN = new MapFunction<float[]>() {
        public float[] map(final float[] array) {
            return new Hann(array.length).map(array);
        }

        @Override
        public String toString() {
            return "HANN_WINDOW";
        }
    };

    /**
     * Function that always uses a Hamming window that is as long as the provided data.
     */
    public static MapFunction<float[]> HAMMING = new MapFunction<float[]>() {
        public float[] map(final float[] array) {
            return new Hamming(array.length).map(array);
        }

        @Override
        public String toString() {
            return "HAMMING_WINDOW";
        }
    };

    /**
     * Function that always uses a Welch window that is as long as the provided data.
     */
    public static MapFunction<float[]> WELCH = new MapFunction<float[]>() {
        public float[] map(final float[] array) {
            return new Welch(array.length).map(array);
        }

        @Override
        public String toString() {
            return "WELCH_WINDOW";
        }
    };

    /**
     * Helper class to invert (1/x) window functions.
     * <p/>
     * This implementation re-uses its output buffer.
     * Do not hold on to it or rely on it staying unchanged. If you must hold on to it,
     * create a copy using {@link Object#clone()}.
     *
     * @see #invert()
     */
    public static class InverseWindowFunction extends WindowFunction {

        private final WindowFunction function;

        public InverseWindowFunction(final WindowFunction function) {
            super(function instanceof InverseWindowFunction
                    ? ((InverseWindowFunction) function).function.coefficients
                    : invert(function.coefficients));
            this.function = function;
        }

        private static float[] invert(final float[] coefficients) {
            final float[] inverse = new float[coefficients.length];
            for (int n = 0; n < coefficients.length; n++) {
                inverse[n] = 1f/coefficients[n];
            }
            return inverse;
        }

        @Override
        public String toString() {
            return "InverseWindowFunction{" +
                    "function=" + function +
                    '}';
        }
    }

    /**
     * Triangle window function.
     * <p/>
     * This implementation re-uses its output buffer.
     * Do not hold on to it or rely on it staying unchanged. If you must hold on to it,
     * create a copy using {@link Object#clone()}.
     */
    public static class Triangle extends WindowFunction {

        public Triangle(final int length) {
            super(coefficients(length));
        }

        private static float[] coefficients(final int length) {
            float[] coefficients = new float[length];
            final float halfLength = length/2;
            for (int n = 0; n < length; n++) {
                if (n <= length / 2) {
                    coefficients[n] = (float) n/halfLength;
                } else {
                    coefficients[n] = 2.0f - (float) n/halfLength;
                }
            }
            return coefficients;
        }

        @Override
        public String toString() {
            return "Triangle{" +
                    "length=" + getLength() +
                    '}';
        }
    }


    /**
     * Hann window function.
     * <p/>
     * This implementation re-uses its output buffer.
     * Do not hold on to it or rely on it staying unchanged. If you must hold on to it,
     * create a copy using {@link Object#clone()}.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Window_function#Hann_window">Hann Window</a>
     */
    public static class Hann extends WindowFunction {

        public Hann(final int length) {
            super(coefficients(length));
        }

        private static float[] coefficients(final int length) {
            final float[] coefficients = new float[length];
            final int lengthMinus1 = length - 1;
            if (lengthMinus1 == 0) {
                coefficients[0] = 1;
            } else {
                for (int n = 0; n < length; n++) {
                    final double cosArg = (DOUBLE_PI * n) / lengthMinus1;
                    coefficients[n] = (float) (0.5 - 0.5 * cos(cosArg));
                }
            }
            return coefficients;
        }

        @Override
        public String toString() {
            return "Hann{" +
                    "length=" + getLength() +
                    '}';
        }
    }

    /**
     * Hamming window function.
     * <p/>
     * This implementation re-uses its output buffer.
     * Do not hold on to it or rely on it staying unchanged. If you must hold on to it,
     * create a copy using {@link Object#clone()}.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Window_function#Hamming_window">Hamming Window</a>
     */
    public static class Hamming extends WindowFunction {

        public Hamming(final int length) {
            super(coefficients(length));
        }

        private static float[] coefficients(final int length) {
            final float[] coefficients = new float[length];
            final int lengthMinus1 = length - 1;
            if (lengthMinus1 == 0) {
                coefficients[0] = 1;
            } else {
                for (int n = 0; n < length; n++) {
                    final double cosArg = (DOUBLE_PI * n) / lengthMinus1;
                    coefficients[n] = (float) (0.54 - 0.46 * cos(cosArg));
                }
            }
            return coefficients;
        }

        @Override
        public String toString() {
            return "Hamming{" +
                    "length=" + getLength() +
                    '}';
        }
    }

    /**
     * Welch window function.
     * <p/>
     * This implementation re-uses its output buffer.
     * Do not hold on to it or rely on it staying unchanged. If you must hold on to it,
     * create a copy using {@link Object#clone()}.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Window_function#Welch_window">Welch Window</a>
     */
    public static class Welch extends WindowFunction {

        public Welch(final int length) {
            super(coefficients(length));
        }

        private static float[] coefficients(final int length) {
            final float[] coefficients = new float[length];
            final float a = (length - 1f) / 2f;
            for (int i=0; i<length; i++) {
                final float c = (i - a) / a;
                coefficients[i] = (1-c*c);
            }
            return coefficients;
        }

        @Override
        public String toString() {
            return "Welch{" +
                    "length=" + getLength() +
                    '}';
        }
    }
}
