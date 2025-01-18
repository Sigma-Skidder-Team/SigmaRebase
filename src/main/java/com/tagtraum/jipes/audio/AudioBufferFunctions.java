/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.AggregateFunction;
import com.tagtraum.jipes.math.DistanceFunction;
import com.tagtraum.jipes.math.MapFunction;
import com.tagtraum.jipes.math.StatefulMapFunction;

/**
 * Factory methods for turning <code>float[]</code> functions into {@link AudioBuffer} functions.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see MapFunction
 * @see AggregateFunction
 * @see DistanceFunction
 * @see StatefulMapFunction
 */
public final class AudioBufferFunctions {

    private static final FloatArrayAccessor<AudioBuffer> DATA_ACCESSOR = new DataAccessor<AudioBuffer>();
    private static final FloatArrayAccessor<AudioBuffer> MAGNITUDE_ACCESSOR = new MagnitudeAccessor<AudioBuffer>();
    private static final FloatArrayAccessor<AudioBuffer> POWER_ACCESSOR = new PowerAccessor<AudioBuffer>();

    private AudioBufferFunctions() {
    }

    /**
     * Creates an {@link AudioBuffer} map function that maps both the real and the imaginary part
     * of the buffer using the provided <code>float[]</code> map function.
     *
     * @param function <code>float[]</code> map function
     * @param <T> actual type of the resulting map function
     * @return map function typed to process audio buffers
     */
    public static <T extends AudioBuffer> MapFunction<T> createMapFunction(final MapFunction<float[]> function) {
        return new AudioBufferMapFunction<T>(function);
    }

    /**
     * Creates an {@link AudioBuffer} map function that maps the <em>magnitudes</em>
     * of the buffer using the provided <code>float[]</code> map function.
     * This implies that the imaginary part of the data becomes <code>null</code>.
     *
     * @param function <code>float[]</code> map function
     * @param <T> actual type of the resulting map function
     * @return map function typed to process audio buffers
     */
    public static <T extends AudioBuffer> MapFunction<T> createMagnitudeMapFunction(final MapFunction<float[]> function) {
        return new AudioBufferMagnitudeMapFunction<T>(function);
    }

    /**
     * Creates an {@link AudioBuffer} map function that maps the <em>powers</em>
     * of the buffer using the provided <code>float[]</code> map function.
     * This implies that the imaginary part of the data becomes <code>null</code>.
     * After the given function has been applied to the powers, the square root is
     * set as the new real part of the data.
     *
     * @param function <code>float[]</code> map function
     * @param <T> actual type of the resulting map function
     * @return map function typed to process audio buffers
     */
    public static <T extends AudioBuffer> MapFunction<T> createPowerMapFunction(final MapFunction<float[]> function) {
        return new AudioBufferPowerMapFunction<T>(function);
    }

    /**
     * Creates a {@link AudioBuffer} stateful map function that maps both the real and the imaginary part
     * of the buffer using the provided <code>float[]</code> map function.
     *
     * @param function <code>float[]</code> map function
     * @param <T> actual type of the resulting map function
     * @return map function typed to process audio buffers
     */
    public static <T extends AudioBuffer> StatefulMapFunction<T> createStatefulMapFunction(final StatefulMapFunction<float[]> function) {
        return new AudioBufferStatefulMapFunction<T>(function);
    }

    /**
     * Creates an {@link AudioBuffer} distance function that computes the distance between the data
     * (as in {@link com.tagtraum.jipes.audio.AudioBuffer#getData()}) of two buffers using the
     * provided <code>float[]</code> distance function.
     *
     * @param function <code>float[]</code> distance function
     * @param <T> actual type of the resulting distance function
     * @return distance function typed to process audio buffers
     */
    public static <T extends AudioBuffer> DistanceFunction<T> createDistanceFunction(final DistanceFunction<float[]> function) {
        return new AudioBufferDistanceFunction<T>(function);
    }

    /**
     * Creates an {@link AudioBuffer} distance function that computes the distance between the data
     * (as in {@link AudioBuffer#getPowers()}) of two buffers using the
     * provided <code>float[]</code> distance function.
     *
     * @param function <code>float[]</code> distance function
     * @param <T> actual type of the resulting distance function
     * @return distance function typed to process audio buffers
     */
    public static <T extends AudioBuffer> DistanceFunction<T> createPowerDistanceFunction(final DistanceFunction<float[]> function) {
        return new AudioBufferDistanceFunction<T>(function, POWER_ACCESSOR);
    }

    /**
     * Creates an {@link AudioBuffer} aggregate function that computes a <code>Float</code> for the
     * buffers using the provided <code>float[]</code> aggregate function.
     *
     * @param function <code>float[]</code> aggregate function
     * @param <T> actual type of the resulting aggregate function
     * @return aggregate function typed to process audio buffers
     */
    public static <T extends AudioBuffer> AggregateFunction<T, Float> createAggregateFunction(final AggregateFunction<float[], Float> function) {
        return new AudioBufferAggregateFunction<T>(function);
    }

    private static class AudioBufferMagnitudeMapFunction<T extends AudioBuffer> implements MapFunction<T> {

        private final MapFunction<float[]> function;
        private RealAudioBuffer realAudioBuffer;

        public AudioBufferMagnitudeMapFunction(final MapFunction<float[]> function) {
            this.function = function;
        }

        public T map(final T buffer) {
            // there may be fewer magnitudes than samples!! fourier.
            final float[] data = new float[buffer.getNumberOfSamples()];
            final float[] mags = function.map(buffer.getMagnitudes());
            System.arraycopy(mags, 0, data, 0, mags.length);

            final AudioBuffer out;
            if (buffer instanceof RealAudioBuffer) {
                if (realAudioBuffer == null) {
                    realAudioBuffer = (RealAudioBuffer)buffer.derive(data, null);
                } else {
                    realAudioBuffer.reuse(buffer.getFrameNumber(), data, buffer.getAudioFormat());
                }
                out = realAudioBuffer;
            } else {
                out = buffer.derive(data, null);
            }
            return (T)out;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AudioBufferMagnitudeMapFunction that = (AudioBufferMagnitudeMapFunction) o;

            if (function != null ? !function.equals(that.function) : that.function != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return function != null ? function.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "<AudioBuffer.Magnitude>{" + function + '}';
        }
    }

    private static class AudioBufferPowerMapFunction<T extends AudioBuffer> implements MapFunction<T> {

        private final MapFunction<float[]> function;
        private RealAudioBuffer realAudioBuffer;

        public AudioBufferPowerMapFunction(final MapFunction<float[]> function) {
            this.function = function;
        }

        public T map(final T buffer) {
            // there may be fewer magnitudes than samples!! fourier.
            final float[] data = new float[buffer.getNumberOfSamples()];
            final float[] powers = function.map(buffer.getPowers());
            System.arraycopy(powers, 0, data, 0, powers.length);

            for (int i=0; i<data.length; i++) {
                data[i] = (float)Math.sqrt(data[i]);
            }
            final AudioBuffer out;
            if (buffer instanceof RealAudioBuffer) {
                if (realAudioBuffer == null) {
                    realAudioBuffer = (RealAudioBuffer)buffer.derive(data, null);
                } else {
                    realAudioBuffer.reuse(buffer.getFrameNumber(), data, buffer.getAudioFormat());
                }
                out = realAudioBuffer;
            } else {
                out = buffer.derive(data, null);
            }
            return (T)out;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AudioBufferPowerMapFunction that = (AudioBufferPowerMapFunction) o;

            if (function != null ? !function.equals(that.function) : that.function != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return function != null ? function.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "<AudioBuffer.Power>{" + function + '}';
        }
    }

    private static class AudioBufferMapFunction<T extends AudioBuffer> implements MapFunction<T> {

        private final MapFunction<float[]> function;
        private RealAudioBuffer realAudioBuffer;

        public AudioBufferMapFunction(final MapFunction<float[]> function) {
            this.function = function;
        }


        public T map(final T buffer) {
            float[] unscaled = buffer.getRealData();
            final float[] real = function.map(unscaled).clone();
            final AudioBuffer out;
            if (buffer instanceof RealAudioBuffer) {
                if (realAudioBuffer == null) {
                    realAudioBuffer = (RealAudioBuffer)buffer.derive(real, null);
                } else {
                    realAudioBuffer.reuse(buffer.getFrameNumber(), real, buffer.getAudioFormat());
                }
                out = realAudioBuffer;
            } else {
                final float[] imaginary = buffer.getImaginaryData() != null ? function.map(buffer.getImaginaryData()).clone() : null;
                out = buffer.derive(real, imaginary);
            }
            return (T)out;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AudioBufferMapFunction that = (AudioBufferMapFunction) o;

            if (function != null ? !function.equals(that.function) : that.function != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return function != null ? function.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "<AudioBuffer>{" + function + '}';
        }
    }

    private static class AudioBufferStatefulMapFunction<T extends AudioBuffer> implements StatefulMapFunction<T> {
        private final StatefulMapFunction<float[]> function;
        private RealAudioBuffer realAudioBuffer;

        public AudioBufferStatefulMapFunction(final StatefulMapFunction<float[]> function) {
            this.function = function;
        }

        public T map(final T buffer) {
            final float[] real = function.map(buffer.getRealData()).clone();
            final AudioBuffer out;
            if (buffer instanceof RealAudioBuffer) {
                if (realAudioBuffer == null) {
                    realAudioBuffer = (RealAudioBuffer)buffer.derive(real, null);
                } else {
                    realAudioBuffer.reuse(buffer.getFrameNumber(), real, buffer.getAudioFormat());
                }
                out = realAudioBuffer;
            } else {
                final float[] imaginary = buffer.getImaginaryData() != null ? function.map(buffer.getImaginaryData()).clone() : null;
                out = buffer.derive(real, imaginary);
            }
            return (T)out;
        }

        public void reset() {
            if (function != null) function.reset();
            realAudioBuffer = null;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AudioBufferMapFunction that = (AudioBufferMapFunction) o;

            if (function != null ? !function.equals(that.function) : that.function != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return function != null ? function.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "<AudioBuffer>{" + function + '}';
        }
    }

    private static class AudioBufferDistanceFunction<T extends AudioBuffer> implements DistanceFunction<T> {

        private final DistanceFunction<float[]> function;
        private final FloatArrayAccessor<AudioBuffer> accessor;

        public AudioBufferDistanceFunction(final DistanceFunction<float[]> function) {
            this(function, DATA_ACCESSOR);
        }

        public AudioBufferDistanceFunction(final DistanceFunction<float[]> function, final FloatArrayAccessor<AudioBuffer> accessor) {
            this.function = function;
            this.accessor = accessor;
        }

        public float distance(final T a, final T b) {
            return function.distance(accessor.getFloatArray(a), accessor.getFloatArray(b));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final AudioBufferDistanceFunction that = (AudioBufferDistanceFunction) o;

            if (accessor != null ? !accessor.equals(that.accessor) : that.accessor != null) return false;
            if (function != null ? !function.equals(that.function) : that.function != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return function != null ? function.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "<AudioBuffer>{" + function + "(" + accessor + ")}";
        }
    }

    private static class AudioBufferAggregateFunction<T extends AudioBuffer> implements AggregateFunction<T, Float> {

        private final AggregateFunction<float[], Float> function;
        private final FloatArrayAccessor<AudioBuffer> accessor;

        private AudioBufferAggregateFunction(final AggregateFunction<float[], Float> function) {
            this(function, DATA_ACCESSOR);
        }

        private AudioBufferAggregateFunction(final AggregateFunction<float[], Float> function, final FloatArrayAccessor<AudioBuffer> accessor) {
            this.function = function;
            this.accessor = accessor;
        }

        public Float aggregate(final T buffer) {
            return function.aggregate(accessor.getFloatArray(buffer));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AudioBufferAggregateFunction that = (AudioBufferAggregateFunction) o;

            if (accessor != null ? !accessor.equals(that.accessor) : that.accessor != null) return false;
            if (function != null ? !function.equals(that.function) : that.function != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return function != null ? function.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "<AudioBuffer>{" + function + "(" + accessor + ")}";
        }
    }


    private interface FloatArrayAccessor<T> {
        float[] getFloatArray(T t);
    }

    private static class DataAccessor<T extends AudioBuffer> implements FloatArrayAccessor<T> {
        @Override
        public float[] getFloatArray(final T t) {
            return t.getData();
        }
        @Override
        public String toString() {
            return "data";
        }
    }

    private static class PowerAccessor<T extends AudioBuffer> implements FloatArrayAccessor<T> {

        @Override
        public float[] getFloatArray(final T t) {
            return t.getPowers();
        }
        @Override
        public String toString() {
            return "powers";
        }
    }

    private static class MagnitudeAccessor<T extends AudioBuffer> implements FloatArrayAccessor<T> {
        @Override
        public float[] getFloatArray(final T t) {
            return t.getMagnitudes();
        }
        @Override
        public String toString() {
            return "magnitudes";
        }
    }
}
