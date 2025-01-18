/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.Arrays;

/**
 * <p>
 * Offers methods to create a number of useful filters, including MIDI filterbanks.
 * Furthermore, you can create your own filters using the {@link IIRFilter} and {@link FIRFilter} classes.
 * </p>
 * <p>
 * Filters are {@link StatefulMapFunction}s and as such can be used in {@link com.tagtraum.jipes.universal.Mapping} to
 * be part of a {@link com.tagtraum.jipes.SignalPipeline}. To filter an {@link com.tagtraum.jipes.audio.AudioBuffer} create a Mapping like this:
 * <xmp>new Mapping<AudioBuffer>(AudioBufferFunctions.createMapFunction(Filters.createFir1_16thOrderLowpassCutoff(4)))</xmp>
 * </p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.audio.Downsample
 * @see MultirateFilters
 */
public final class Filters {

    private Filters() {
    }

    /**
     * <p>FIR (finite impulse response) filter.</p>
     * <p>
     * To compute the coefficients you might want to use Matlab or the free package
     * <a href="http://www.gnu.org/software/octave/">Octave</a> (with additional Octave-Forge packages needed for signals).
     * E.g. for a low pass filter you could execute:</p>
     * <xmp>
     * b = fir1(16,0.125)
     * </xmp>
     *
     * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
     * @see <a href="http://en.wikipedia.org/wiki/Finite_impulse_response">Wikipedia on FIR</a>
     * @see IIRFilter
     */
    public static class FIRFilter implements StatefulMapFunction<float[]> {

        /**
         * Efficient no-op FIR filter.
         */
        public static FIRFilter NOOP = new NoopFIRFilter();
        private int length;
        private double[] delayLine;
        private double[] impulseResponse;
        private int count = -1;
        private double[] coefficients;

        public FIRFilter() {
            this(new double[]{1.0});
        }

        public FIRFilter(double[] coefficients) {
            setCoefficients(coefficients);
        }

        private void setCoefficients(final double[] coefs) {
            if (coefs.length < 1) throw new IllegalArgumentException("At least one coefficient is required.");
            this.coefficients = coefs.clone();
            this.length = coefs.length;
            this.impulseResponse = coefs.clone();
            this.delayLine = new double[length];
            this.count = -1;
        }

        public double[] getCoefficients() {
            return coefficients.clone();
        }

        public void reset() {
            if (coefficients != null) setCoefficients(coefficients);
        }

        public float[] map(final float[] data) {
            final float[] out = new float[data.length];
            for (int i = 0; i < data.length; i++) {
                final float sample = data[i];
                addToDelayLine(sample);
                out[i] = (float) filter();
            }
            return out;
        }

        protected void addToDelayLine(final double sample) {
            this.count = (count + 1) % length;
            this.delayLine[count] = sample;
        }

        protected double filter() {
            double result = 0.0;
            int index = count;
            for (int i = 0; i < length; i++) {
                result += impulseResponse[i] * delayLine[index--];
                if (index < 0) index = length - 1;
            }
            return result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FIRFilter firFilter = (FIRFilter) o;

            if (!Arrays.equals(coefficients, firFilter.coefficients)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return coefficients != null ? Arrays.hashCode(coefficients) : 0;
        }

        @Override
        public String toString() {
            return "FIRFilter{" +
                    "coefficients=" + Arrays.toString(coefficients) +
                    '}';
        }
    }

    /**
     * Efficient no-op FIR filter.
     */
    private static class NoopFIRFilter extends FIRFilter {

        private double sample;

        public NoopFIRFilter() {
            super(new double[]{1.0});
        }

        @Override
        protected void addToDelayLine(final double sample) {
            this.sample = sample;
        }

        @Override
        protected double filter() {
            return sample;
        }

        @Override
        public float[] map(final float[] data) {
            return data.clone();
        }
    }

    /**
     * IIR (infinite impulse response) filter.
     * <p>
     * To compute the coefficients you might want to use Matlab or the free package
     * <a href="http://www.gnu.org/software/octave/">Octave</a> (with additional Octave-Forge packages needed for signals).
     * E.g. for a low pass filter you could execute:</p>
     * <xmp>
     * [a,b] = butter(3, .4, 'low')
     * </xmp>
     * <p>This will compute the coefficients for a third order low pass filter where the first argument (3) is the order
     * and the second (.4) the normalized cutoff frequency, which is defined as <code>f<sub>c</sub>*2/f<sub>s</sub></code>
     * (f<sub>c</sub> = cutoff frequency, f<sub>s</sub> = sample frequency).</p>
     * <p>Assuming your audio is sampled at 44.1kHz, and you wanted a cutoff frequency of 11.025kHz, the seconds parameters
     * would be <code>11025*2/44100 = 0.5</code>.</p>
     * <p/>
     * Date: Jul 20, 2010
     * Time: 2:13:51 PM
     *
     * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
     * @see <a href="http://en.wikipedia.org/wiki/Iir_filter">Wikipedia on IIR</a>
     * @see FIRFilter
     * @see FourthOrderIIRFilter
     */
    public static class IIRFilter implements StatefulMapFunction<float[]> {

        private double[] inputCoefficients;
        private double[] outputCoefficients;
        private double[] inputValue;
        private double[] outputValue;
        private int valuePosition;
        private int order;
        private float[] out;

        protected IIRFilter() {
        }

        public IIRFilter(final double[] inputCoefficients, final double[] outputCoefficients) {
            setInputCoefficients(inputCoefficients);
            setOutputCoefficients(outputCoefficients);
        }

        public void reset() {
            this.inputValue = null;
            this.outputValue = null;
            this.valuePosition = 0;
        }

        /**
         * @param inputCoefficients a (input) coefficients
         */
        public void setInputCoefficients(final double[] inputCoefficients) {
            this.inputCoefficients = inputCoefficients;
            this.order = inputCoefficients.length;
        }

        /**
         * First b coefficient must always be 1.
         *
         * @param outputCoefficients b (output) coefficients
         */
        public void setOutputCoefficients(final double[] outputCoefficients) {
            this.outputCoefficients = outputCoefficients;
        }

        public float[] map(final float[] data) {
            if (out == null || out.length != data.length) {
                out = new float[data.length];
            }
            int offset = 0;
            if (this.inputValue == null && this.outputValue == null && data.length > 0) {
                out[offset] = (float)firstFilter(data[offset]);
                offset++;
            }
            if (this.inputValue != null && this.outputValue != null) {
                followingFilters(data, offset);
            } else if (data.length > 0) {
                throw new IllegalArgumentException("Both inputValue and outputValue should either be null or not null.");
            }
            return out;
        }

        @Deprecated
        public double filter(final double currentInputValue) {
            final double result;
            if (this.inputValue != null && this.outputValue != null) {
                result = followingFilter(currentInputValue);
            } else if (this.inputValue == null && this.outputValue == null) {
                result = firstFilter(currentInputValue);
            } else {
                throw new IllegalArgumentException("Both inputValue and outputValue should either be null or not null.");
            }
            return result;
        }

        private double followingFilter(final double currentInputValue) {
            this.valuePosition = incrementLowOrderPosition(valuePosition);
            this.inputValue[valuePosition] = currentInputValue;
            this.outputValue[valuePosition] = 0;
            // use temp to avoid at least one array access in the loop
            double tempOutputValue = this.outputValue[valuePosition];
            int j = valuePosition;
            for (int i = 0; i < order; i++) {
                tempOutputValue += inputCoefficients[i] * this.inputValue[j] -
                        outputCoefficients[i] * this.outputValue[j];
                j = decrementLowOrderPosition(j);
            }
            this.outputValue[valuePosition] = tempOutputValue;
            return this.outputValue[valuePosition];
        }

        private void followingFilters(final float[] data, final int offset) {
            for (int k = offset, length = data.length; k < length; k++) {

                // incrementLowOrderPosition
                this.valuePosition = (valuePosition + 1) % order;

                this.inputValue[valuePosition] = data[k];
                this.outputValue[valuePosition] = 0;

                // use temp to avoid at least one array access in the loop
                double tempOutputValue = this.outputValue[valuePosition];
                int j = valuePosition;
                for (int i = 0; i < order; i++) {
                    tempOutputValue += inputCoefficients[i] * this.inputValue[j] - outputCoefficients[i] * this.outputValue[j];
                    
                    // decrementLowOrderPosition
                    if (--j < 0) j += this.order;
                }
                this.outputValue[valuePosition] = tempOutputValue;
                this.out[k] = (float)tempOutputValue;
            }
        }

        private double firstFilter(final double currentInputValue) {
            this.inputValue = new double[order];
            this.outputValue = new double[order];

            this.valuePosition = -1;

            for (int i = 0; i < order; i++) {
                this.inputValue[i] = currentInputValue;
                this.outputValue[i] = currentInputValue;
            }

            return currentInputValue;
        }

        private int decrementLowOrderPosition(int position) {
            if (--position < 0) {
                position += order;
            }
            return position;
        }

        private int incrementLowOrderPosition(final int position) {
            return ((position + 1) % order);
        }


        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IIRFilter iirFilter = (IIRFilter) o;

            if (!Arrays.equals(inputCoefficients, iirFilter.inputCoefficients)) return false;
            if (!Arrays.equals(outputCoefficients, iirFilter.outputCoefficients)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = inputCoefficients != null ? Arrays.hashCode(inputCoefficients) : 0;
            result = 31 * result + (outputCoefficients != null ? Arrays.hashCode(outputCoefficients) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "IIRFilter{" +
                    "order=" + order +
                    ", inputCoefficients=" + Arrays.toString(inputCoefficients) +
                    ", outputCoefficients=" + Arrays.toString(outputCoefficients) +
                    '}';
        }
    }

    /**
     * (Faster) implementation of a fourth order IIR (infinite impulse response) filter.
     *
     * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
     * @see <a href="http://en.wikipedia.org/wiki/Iir_filter">Wikipedia on IIR</a>
     * @see FIRFilter
     * @see IIRFilter
     */
    public static class FourthOrderIIRFilter extends IIRFilter {

        private float[] out;
        private boolean primed;

        private final float b0;
        private final float b1;
        private final float b2;
        private final float b3;
        private final float b4;

        private final float a0;
        private final float a1;
        private final float a2;
        private final float a3;
        private final float a4;

        private float in1;
        private float in2;
        private float in3;
        private float in4;

        private float out0;
        private float out1;
        private float out2;
        private float out3;
        private float out4;

        public FourthOrderIIRFilter(final double[] inputCoefficients, final double[] outputCoefficients) {
            super();
            b0 = (float)inputCoefficients[0];
            b1 = (float)inputCoefficients[1];
            b2 = (float)inputCoefficients[2];
            b3 = (float)inputCoefficients[3];
            b4 = (float)inputCoefficients[4];
            a0 = (float)outputCoefficients[0];
            a1 = (float)outputCoefficients[1];
            a2 = (float)outputCoefficients[2];
            a3 = (float)outputCoefficients[3];
            a4 = (float)outputCoefficients[4];
        }

        @Override
        public void reset() {
            this.primed = false;
        }

        @Override
        public float[] map(final float[] data) {
            final int length = data.length;
            if (length == 0) return new float[0];
            if (out == null || out.length != length) {
                out = new float[length];
            }
            int offset = 0;
            if (!primed) {
                in1 = in2 = in3 = in4 = out0 = out1 = out2 = out3 = out4 = data[0];
                primed = true;
                out[0] = in1;
                offset = 1;
            }
            for (int i = offset; i<length; i++) {
                final float in0 = data[i];
                out0 = b0*in0+b1*in1+b2*in2+b3*in3+b4*in4 -a1*out1-a2*out2-a3*out3-a4*out4;
                in4 = in3;
                in3 = in2;
                in2 = in1;
                in1 = in0;
                out4 = out3;
                out3 = out2;
                out2 = out1;
                out1 = out0;
                out[i] = out0;
            }
            return out;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final FourthOrderIIRFilter that = (FourthOrderIIRFilter) o;

            if (Float.compare(that.b0, b0) != 0) return false;
            if (Float.compare(that.b1, b1) != 0) return false;
            if (Float.compare(that.b2, b2) != 0) return false;
            if (Float.compare(that.b3, b3) != 0) return false;
            if (Float.compare(that.b4, b4) != 0) return false;
            if (Float.compare(that.a0, a0) != 0) return false;
            if (Float.compare(that.a1, a1) != 0) return false;
            if (Float.compare(that.a2, a2) != 0) return false;
            if (Float.compare(that.a3, a3) != 0) return false;
            return Float.compare(that.a4, a4) == 0;
        }

        @Override
        public int hashCode() {
            int result = (b0 != +0.0f ? Float.floatToIntBits(b0) : 0);
            result = 31 * result + (b1 != +0.0f ? Float.floatToIntBits(b1) : 0);
            result = 31 * result + (b2 != +0.0f ? Float.floatToIntBits(b2) : 0);
            result = 31 * result + (b3 != +0.0f ? Float.floatToIntBits(b3) : 0);
            result = 31 * result + (b4 != +0.0f ? Float.floatToIntBits(b4) : 0);
            result = 31 * result + (a0 != +0.0f ? Float.floatToIntBits(a0) : 0);
            result = 31 * result + (a1 != +0.0f ? Float.floatToIntBits(a1) : 0);
            result = 31 * result + (a2 != +0.0f ? Float.floatToIntBits(a2) : 0);
            result = 31 * result + (a3 != +0.0f ? Float.floatToIntBits(a3) : 0);
            result = 31 * result + (a4 != +0.0f ? Float.floatToIntBits(a4) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FourthOrderIIRFilter{" +
                "b0=" + b0 +
                ", b1=" + b1 +
                ", b2=" + b2 +
                ", b3=" + b3 +
                ", b4=" + b4 +
                ", a0=" + a0 +
                ", a1=" + a1 +
                ", a2=" + a2 +
                ", a3=" + a3 +
                ", a4=" + a4 +
                '}';
        }
    }


    /**
     * 16th order Fir1 (Matlab/Octave) lowpass filter that lets <code>factor</code>-th-Nyquist pass (&#x03C9;=1/factor).
     * Supported factors are 1, 2, 3, 4, 5, 7, 8 and 160.
     *
     * @param factor factor
     * @return filter
     * @throws IllegalArgumentException if the factor is not supported.
     */
    public static FIRFilter createFir1_16thOrderLowpass(final int factor) throws IllegalArgumentException {
        if (factor == 1) return FIRFilter.NOOP;
        if (factor == 2) return createFir1_16thOrderLowpassCutoffHalf();
        if (factor == 3) return createFir1_16thOrderLowpassCutoffThird();
        if (factor == 4) return createFir1_16thOrderLowpassCutoffQuarter();
        if (factor == 5) return createFir1_16thOrderLowpassCutoffFifth();
        if (factor == 7) return createFir1_16thOrderLowpassCutoffSeventh();
        if (factor == 8) return createFir1_16thOrderLowpassCutoffEighth();
        if (factor == 160) return createFir1_16thOrderLowpassCutoff160th();
        throw new IllegalArgumentException("Frequency factor " + factor + " is not supported.");
    }

    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets 160th-Nyquist pass (&#x03C9;=1/160).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoff160th() {
        return new FIRFilter(
                new double[]{0.0091559, 0.0131728, 0.0246085, 0.0417328,
                        0.0619435, 0.0821638, 0.0993117, 0.1107724,
                        0.1147973, 0.1107724, 0.0993117, 0.0821638,
                        0.0619435, 0.0417328, 0.0246085, 0.0131728,
                        0.0091559
                }
        );
    }

    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets seventh-Nyquist pass (&#x03C9;=1/7).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoffSeventh() {
        return new FIRFilter(
                new double[]{-1.6801e-03, 1.4401e-04, 6.5789e-03, 2.3510e-02,
                        5.3865e-02, 9.4771e-02, 1.3738e-01, 1.6980e-01,
                        1.8193e-01, 1.6980e-01, 1.3738e-01, 9.4771e-02,
                        5.3865e-02, 2.3510e-02, 6.5789e-03, 1.4401e-04,
                        -1.6801e-03
                }
        );
    }

    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets fifth-Nyquist pass (&#x03C9;=1/5).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoffFifth() {
        return new FIRFilter(
                new double[]{-3.5125e-03, -5.6864e-03, -7.5140e-03, 4.0930e-04,
                        2.9577e-02, 8.3457e-02, 1.5053e-01, 2.0705e-01,
                        2.2921e-01, 2.0705e-01, 1.5053e-01, 8.3457e-02,
                        2.9577e-02, 4.0930e-04, -7.5140e-03, -5.6864e-03,
                        -3.5125e-03
                }
        );
    }

    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets third-Nyquist pass (&#x03C9;=1/3).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoffThird() {
        return new FIRFilter(
                new double[]{ 2.8694e-03, 4.5917e-03, -2.1529e-04, -2.0784e-02,
                        -3.7939e-02, 7.1800e-04, 1.2289e-01, 2.7267e-01,
                        3.4128e-01, 2.7267e-01, 1.2289e-01, 7.1800e-04,
                        -3.7939e-02, -2.0784e-02, -2.1529e-04, 4.5917e-03,
                        2.8694e-03
                }
        );
    }


    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets half-Nyquist pass (&#x03C9;=0.5).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoffHalf() {
        return new FIRFilter(
                new double[]{-7.8001e-005, -5.2209e-003, 2.0936e-004, 2.3132e-002,
                        -5.2650e-004, -7.5850e-002, 8.4364e-004, 3.0667e-001,
                        4.9823e-001, 3.0667e-001, 8.4364e-004, -7.5850e-002,
                        -5.2650e-004, 2.3132e-002, 2.0936e-004, -5.2209e-003,
                        -7.8001e-005
                }
        );
    }

    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets quarter-Nyquist pass (&#x03C9;=0.25).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoffQuarter() {
        return new FIRFilter(
                new double[]{-8.4830e-005, -4.1013e-003, -1.2368e-002, -1.7516e-002,
                        5.7260e-004, 5.8867e-002, 1.4953e-001, 2.3512e-001,
                        2.7040e-001, 2.3512e-001, 1.4953e-001, 5.8867e-002,
                        5.7260e-004, -1.7516e-002, -1.2368e-002, -4.1013e-003,
                        -8.4830e-005
                }
        );
    }

    /**
     * 16th order Fir1 (Matlab) lowpass filter that lets eighth-Nyquist pass (&#x03C9;=0.125).
     *
     * @return filter
     * @deprecated use {@link #createFir1_16thOrderLowpass(int)}
     */
    public static FIRFilter createFir1_16thOrderLowpassCutoffEighth() {
        return new FIRFilter(
                new double[]{1.0560e-004, 2.8453e-003, 1.1088e-002, 2.9118e-002,
                        5.8084e-002, 9.4514e-002, 1.3082e-001, 1.5771e-001,
                        1.6765e-001, 1.5771e-001, 1.3082e-001, 9.4514e-002,
                        5.8084e-002, 2.9118e-002, 1.1088e-002, 2.8453e-003,
                        1.0560e-004
                }
        );
    }


    /**
     * Fourth order Butterworth lowpass filter that lets <code>factor</code>-th-Nyquist pass (&#x03C9;=1/factor).
     * Supported factors are 2, 4, and 8.
     *
     * @param factor factor
     * @return filter
     * @throws IllegalArgumentException if the factor is not supported.
     */
    public static FourthOrderIIRFilter createButterworth4thOrderLowpass(final int factor) throws IllegalArgumentException {
        if (factor == 2) return createButterworth4thOrderLowpassCutoffHalf();
        if (factor == 4) return createButterworth4thOrderLowpassCutoffQuarter();
        if (factor == 8) return createButterworth4thOrderLowpassCutoffEighth();
        throw new IllegalArgumentException("Frequency factor " + factor + " is not supported.");
    }

    /**
     * Fourth order Butterworth lowpass filter that lets half-Nyquist pass (&#x03C9;=0.5).
     *
     * @return filter
     * @deprecated use {@link #createButterworth4thOrderLowpass(int)}
     */
    public static FourthOrderIIRFilter createButterworth4thOrderLowpassCutoffHalf() {
        return new FourthOrderIIRFilter(
                new double[]{0.093981, 0.375923, 0.563885, 0.375923, 0.093981},
                new double[]{1, -3.0871e-016d, 4.8603e-001d, -4.7269e-017d, 1.7665e-002d}
        );
    }

    /**
     * Fourth order Butterworth lowpass filter that lets quarter-Nyquist pass (&#x03C9;=0.25).
     *
     * @return filter
     * @deprecated use {@link #createButterworth4thOrderLowpass(int)}
     */
    public static FourthOrderIIRFilter createButterworth4thOrderLowpassCutoffQuarter() {
        return new FourthOrderIIRFilter(
                new double[]{0.010209, 0.040838, 0.061257, 0.040838, 0.010209},
                new double[]{1, -1.96843, 1.73586, -0.72447, 0.12039}
        );
    }

    /**
     * Fourth order Butterworth lowpass filter that lets eighth-Nyquist pass (&#x03C9;=0.125).
     *
     * @return filter
     * @deprecated use {@link #createButterworth4thOrderLowpass(int)}
     */
    public static FourthOrderIIRFilter createButterworth4thOrderLowpassCutoffEighth() {
        return new FourthOrderIIRFilter(
                new double[]{9.3350e-004, 3.7340e-003, 5.6010e-003, 3.7340e-003, 9.3350e-004},
                new double[]{1, -2.97684, 3.42231, -1.78611, 0.35558}
        );
    }


    /**
     * Eigth order Elliptic lowpass filter that lets <code>factor</code>-th-Nyquist pass (&#x03C9;=1/factor).
     * Supported factors are 2, 4, and 8.
     *
     * @param factor factor
     * @return filter
     * @throws IllegalArgumentException if the factor is not supported.
     */
    public static IIRFilter createElliptic8thOrderLowpass(final int factor) throws IllegalArgumentException {
        if (factor == 2) return createElliptic8thOrderLowpassCutoffHalf();
        if (factor == 4) return createElliptic8thOrderLowpassCutoffQuarter();
        if (factor == 8) return createElliptic8thOrderLowpassCutoffEighth();
        throw new IllegalArgumentException("Frequency factor " + factor + " is not supported.");
    }

    /**
     * Eigth order Elliptic lowpass filter that lets half-Nyquist pass (&#x03C9;=0.5).
     * <br>
     * Created with Matlab/Octave: <code>format long e; ellip(8,1,50,0.5)</code>
     *
     * @return filter
     * @deprecated use {@link #createElliptic8thOrderLowpass(int)}
     */
    public static IIRFilter createElliptic8thOrderLowpassCutoffHalf() {
        return new IIRFilter(
                new double[]{4.11064388825708e-002, 1.08866737154535e-001, 2.41347240647923e-001, 3.43806135787190e-001, 4.01594253357415e-001, 3.43806135787190e-001, 2.41347240647923e-001, 1.08866737154535e-001, 4.11064388825708e-002},
                new double[]{1.00000000000000e+000, -1.41360699508507e+000, 3.47191779241568e+000, -3.54933041493939e+000, 4.11821126483047e+000, -2.89633972010838e+000, 1.88851000448182e+000, -7.60166584396562e-001, 2.41051932452500e-001}
        );
    }

    /**
     * Eigth order Elliptic lowpass filter that lets quarter-Nyquist pass (&#x03C9;=0.25).
     * <br>
     * Created with Matlab/Octave: <code>format long e; ellip(8,1,50,0.25)</code>
     *
     * @return filter
     * @deprecated use {@link #createElliptic8thOrderLowpass(int)}
     */
    public static IIRFilter createElliptic8thOrderLowpassCutoffQuarter() {
        return new IIRFilter(
                new double[]{8.32029221457793e-003, -2.50751120991713e-002, 5.05964798990251e-002, -6.64679305308505e-002, 7.52292333137131e-002, -6.64679305308505e-002, 5.05964798990251e-002, -2.50751120991713e-002, 8.32029221457793e-003},
                new double[]{1, -5.82407353677890e+000, 1.61042342351185e+001, -2.71659326029196e+001, 3.03867729535560e+001, -2.30023347251336e+001, 1.14999583000060e+001, -3.47627073026071e+000, 4.88840139264333e-001}
        );
    }

    /**
     * Eigth order Elliptic lowpass filter that lets eighth-Nyquist pass (&#x03C9;=0.125).
     * <br>
     * Created with Matlab/Octave: <code>format long e; ellip(8,1,50,0.125)</code>
     *
     * @return filter
     * @deprecated use {@link #createElliptic8thOrderLowpass(int)}
     */
    public static IIRFilter createElliptic8thOrderLowpassCutoffEighth() {
        return new IIRFilter(
                new double[]{4.08662908565832e-003, -2.50208555755834e-002, 7.21835404577325e-002, -1.28219206557494e-001, 1.53985767563535e-001, -1.28219206557494e-001, 7.21835404577325e-002, -2.50208555755834e-002, 4.08662908565832e-003},
                new double[]{1.00000000000000e+000, -7.26108011616103e+000, 2.34393963033145e+001, -4.38980736967991e+001, 5.21393396531836e+001, -4.02016581490597e+001, 1.96467661898711e+001, -5.56371542552930e+000, 6.99076834263625e-001}
        );
    }

    private static IIRFilter[] createMidiFilterBank(final int minMidiPitch, final int maxMidiPitch, final double[][][] coefficients) {
        if (maxMidiPitch<minMidiPitch) throw new IllegalArgumentException("maxMidiPitch must not be less than minMidiPitch");
        final IIRFilter[] filterBank = new IIRFilter[maxMidiPitch - minMidiPitch + 1];
        for (int p = minMidiPitch; p <= maxMidiPitch; p++) {
            filterBank[p - minMidiPitch] = new IIRFilter(
                    coefficients[p][0],
                    coefficients[p][1]
            );
        }
        return filterBank;
    }

    /**
     * Creates a new <a href="http://en.wikipedia.org/wiki/Musical_Instrument_Digital_Interface">MIDI</a>-pitch
     * filterbank, based on 8th order elliptic filters with 1dB passband ripple, 50dB rejection in the stopband and
     * a Q-factor of 25. Unless the sampling rate does not allow it, all pitches 0-127 are present.
     * <p>
     * If only a subset of the complete filterbank is desired, you can specify a minimum and a maximum pitch
     * (both incl.).
     * <p>
     * Note that the individual filters are <em>stateful</em> and can't simply ce re-used in a different context.
     *
     * @param sampleRate one of several supported sample rates (currently 44100Hz, 22050Hz, 11025Hz, 5512.5Hz, 2756.25Hz, 4410Hz, 882Hz)
     * @param minMidiPitch smallest pitch to include (inclusive)
     * @param maxMidiPitch greatest pitch to include (inclusive)
     * @return array of filters for the desired pitch range
     * @throws IllegalArgumentException if the desired sample rate is not supported
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    public static IIRFilter[] createMidiFilterBank(final float sampleRate, final int minMidiPitch, final int maxMidiPitch)  throws IllegalArgumentException {
        if (44100 == sampleRate) return createMidi44100HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        if (22050 == sampleRate) return createMidi22050HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        if (11025 == sampleRate) return createMidi11025HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        if (5512.5 == sampleRate) return createMidi5512_5HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        if (2756.25 == sampleRate) return createMidi2756_25HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        if (4410 == sampleRate) return createMidi4410HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        if (882 == sampleRate) return createMidi882HzElliptic8thOrderFilterBank(minMidiPitch, maxMidiPitch);
        throw new IllegalArgumentException("Sample rate " + sampleRate + "Hz is not supported.");
    }

    /**
     * Creates a new <a href="http://en.wikipedia.org/wiki/Musical_Instrument_Digital_Interface">MIDI</a>-pitch
     * filterbank, based on 8th order elliptic filters with 1dB passband ripple, 50dB rejection in the stopband and
     * a Q-factor of 25. The filters are suitable for a signal sampled at 44100Hz. Unless the sampling rate does not
     * allow it, all pitches 0-127 are present.
     * <p>
     * If only a subset of the complete filterbank is desired, you can specify a minimum and a maximum pitch
     * (both incl.).
     *
     * @param minMidiPitch smallest pitch to include (inclusive)
     * @param maxMidiPitch greatest pitch to include (inclusive)
     * @return array of filters for the desired pitch range
     */
    private static IIRFilter[] createMidi44100HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR44100HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi22050HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR22050HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi11025HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR11025HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi5512_5HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR5512_5HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi2756_25HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR2756_25HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi1378_125HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR1378_125HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi4410HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR4410HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    /**
     * @see #createMidi44100HzElliptic8thOrderFilterBank(int, int)
     */
    private static IIRFilter[] createMidi882HzElliptic8thOrderFilterBank(final int minMidiPitch, final int maxMidiPitch) {
        return createMidiFilterBank(minMidiPitch, maxMidiPitch, MIDI_COEFFICIENTS_SR882HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25.COEFFICIENTS);
    }

    private static class MIDI_COEFFICIENTS_SR44100HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.162216009652449e-003, -2.529771074302774e-002, 8.854194426515281e-002, -1.770838365278009e-001, 2.213547739920467e-001, -1.770838365278009e-001, 8.854194426515281e-002, -2.529771074302774e-002, 3.162216009652449e-003},
                        new double[]{1.000000000000000e+000, -7.999950553797846e+000, 2.799965930534010e+001, -5.599899420208452e+001, 6.999835081325685e+001, -5.599837795605944e+001, 2.799904305913583e+001, -7.999686448153742e+000, 9.999559823627847e-001}
                },
                new double[][]{
                        new double[]{3.162211876363730e-003, -2.529767555396305e-002, 8.854181579654832e-002, -1.770835732223455e-001, 2.213544422067930e-001, -1.770835732223455e-001, 8.854181579654832e-002, -2.529767555396305e-002, 3.162211876363729e-003},
                        new double[]{1.000000000000000e+000, -7.999947271642859e+000, 2.799963699508307e+001, -5.599892926575744e+001, 6.999824591005515e+001, -5.599827637680751e+001, 2.799898410592006e+001, -7.999667461849360e+000, 9.999533649989015e-001}
                },
                new double[][]{
                        new double[]{3.162207497632330e-003, -2.529763814140490e-002, 8.854167889583898e-002, -1.770832922728291e-001, 2.213540880415253e-001, -1.770832922728291e-001, 8.854167889583897e-002, -2.529763814140490e-002, 3.162207497632329e-003},
                        new double[]{1.000000000000000e+000, -7.999943752447514e+000, 2.799961310696197e+001, -5.599885984008552e+001, 6.999813393167864e+001, -5.599816812943570e+001, 2.799892139605873e+001, -7.999647304736554e+000, 9.999505920059388e-001}
                },
                new double[][]{
                        new double[]{3.162202858904541e-003, -2.529759835709026e-002, 8.854153296452155e-002, -1.770829923867465e-001, 2.213537098408213e-001, -1.770829923867465e-001, 8.854153296452154e-002, -2.529759835709027e-002, 3.162202858904541e-003},
                        new double[]{1.000000000000000e+000, -7.999939976988816e+000, 2.799958751638697e+001, -5.599878558121114e+001, 6.999801435488178e+001, -5.599805274054663e+001, 2.799885467542111e+001, -7.999625902073905e+000, 9.999476541306223e-001}
                },
                new double[][]{
                        new double[]{3.162197944766279e-003, -2.529755604197496e-002, 8.854137735661334e-002, -1.770826721649393e-001, 2.213533058110692e-001, -1.770826721649393e-001, 8.854137735661337e-002, -2.529755604197497e-002, 3.162197944766280e-003},
                        new double[]{1.000000000000000e+000, -7.999935924272773e+000, 2.799956008759241e+001, -5.599870611541219e+001, 6.999788661274967e+001, -5.599792969917795e+001, 2.799878367099981e+001, -7.999603174048534e+000, 9.999445415695646e-001}
                },
                new double[][]{
                        new double[]{3.162192738892543e-003, -2.529751102535189e-002, 8.854121137437283e-002, -1.770823300916050e-001, 2.213528740073831e-001, -1.770823300916050e-001, 8.854121137437282e-002, -2.529751102535189e-002, 3.162192738892542e-003},
                        new double[]{1.000000000000000e+000, -7.999931571352159e+000, 2.799953067251077e+001, -5.599862103617296e+001, 6.999775009056390e+001, -5.599779845341580e+001, 2.799870808932742e+001, -7.999579035397737e+000, 9.999412439365664e-001}
                },
                new double[][]{
                        new double[]{3.162187223993926e-003, -2.529746312388740e-002, 8.854103426358742e-002, -1.770819645232660e-001, 2.213524123191440e-001, -1.770819645232659e-001, 8.854103426358745e-002, -2.529746312388740e-002, 3.162187223993926e-003},
                        new double[]{1.000000000000000e+000, -7.999926893124075e+000, 2.799949910952342e+001, -5.599852990093974e+001, 6.999760412123427e+001, -5.599765840666537e+001, 2.799862761474223e+001, -7.999553394998698e+000, 9.999377502279682e-001}
                },
                new double[][]{
                        new double[]{3.162181381760068e-003, -2.529741214056743e-002, 8.854084520838157e-002, -1.770815736765805e-001, 2.213519184540126e-001, -1.770815736765805e-001, 8.854084520838157e-002, -2.529741214056743e-002, 3.162181381760067e-003},
                        new double[]{1.000000000000000e+000, -7.999921862104869e+000, 2.799946522207355e+001, -5.599843222752502e+001, 6.999744798024790e+001, -5.599750891354203e+001, 2.799854190748787e+001, -7.999526155423358e+000, 9.999340487859640e-001}
                },
                new double[][]{
                        new double[]{3.162175192799788e-003, -2.529735786354358e-002, 8.854064332549043e-002, -1.770811556148661e-001, 2.213513899202389e-001, -1.770811556148661e-001, 8.854064332549043e-002, -2.529735786354358e-002, 3.162175192799787e-003},
                        new double[]{1.000000000000000e+000, -7.999916448179821e+000, 2.799942881712553e+001, -5.599832749011988e+001, 6.999728088008038e+001, -5.599734927533957e+001, 2.799845060162848e+001, -7.999497212454843e+000, 9.999301272597133e-001}
                },
                new double[][]{
                        new double[]{3.162168636577810e-003, -2.529730006486919e-002, 8.854042765794205e-002, -1.770807082331904e-001, 2.213508240070796e-001, -1.770807082331905e-001, 8.854042765794205e-002, -2.529730006486919e-002, 3.162168636577811e-003},
                        new double[]{1.000000000000000e+000, -7.999910618324636e+000, 2.799938968345269e+001, -5.599821511486969e+001, 6.999710196400878e+001, -5.599717873502961e+001, 2.799835330276025e+001, -7.999466454561915e+000, 9.999259725641548e-001}
                },
                new double[][]{
                        new double[]{3.162161691347837e-003, -2.529723849911344e-002, 8.854019716808061e-002, -1.770802292418669e-001, 2.213502177631037e-001, -1.770802292418669e-001, 8.854019716808061e-002, -2.529723849911345e-002, 3.162161691347838e-003},
                        new double[]{1.000000000000000e+000, -7.999904336295398e+000, 2.799934758973372e+001, -5.599809447496267e+001, 6.999691029925782e+001, -5.599699647174003e+001, 2.799824958549746e+001, -7.999433762327270e+000, 9.999215708363755e-001}
                },
                new double[][]{
                        new double[]{3.162154334081750e-003, -2.529717290184038e-002, 8.853995072985670e-002, -1.770797161481713e-001, 2.213495679721465e-001, -1.770797161481713e-001, 8.853995072985670e-002, -2.529717290184038e-002, 3.162154334081750e-003},
                        new double[]{1.000000000000000e+000, -7.999897562283346e+000, 2.799930228243514e+001, -5.599796488517461e+001, 6.999670486940335e+001, -5.599680159465296e+001, 2.799813899070809e+001, -7.999399007825036e+000, 9.999169073893813e-001}
                },
                new double[][]{
                        new double[]{3.162146540394787e-003, -2.529710298793881e-002, 8.853968712030330e-002, -1.770791662360805e-001, 2.213488711266425e-001, -1.770791662360805e-001, 8.853968712030330e-002, -2.529710298793881e-002, 3.162146540394789e-003},
                        new double[]{1.000000000000000e+000, -7.999890252530271e+000, 2.799925348345496e+001, -5.599782559580713e+001, 6.999648456594744e+001, -5.599659313625823e+001, 2.799802102247260e+001, -7.999362053942496e+000, 9.999119666631262e-001}
                },
                new double[][]{
                        new double[]{3.162138284466419e-003, -2.529702844978658e-002, 8.853940501010447e-002, -1.770785765438014e-001, 2.213481233980341e-001, -1.770785765438014e-001, 8.853940501010449e-002, -2.529702844978658e-002, 3.162138284466419e-003},
                        new double[]{1.000000000000000e+000, -7.999882358899898e+000, 2.799920088749939e+001, -5.599767578594843e+001, 6.999624817897040e+001, -5.599637004488871e+001, 2.799789514473500e+001, -7.999322753640388e+000, 9.999067321726334e-001}
                },
                new double[][]{
                        new double[]{3.162129538956708e-003, -2.529694895523159e-002, 8.853910295315416e-002, -1.770779438388397e-001, 2.213473206039208e-001, -1.770779438388397e-001, 8.853910295315416e-002, -2.529694895523159e-002, 3.162129538956707e-003},
                        new double[]{1.000000000000000e+000, -7.999873828400071e+000, 2.799914415916124e+001, -5.599751455597762e+001, 6.999599438675247e+001, -5.599613117645615e+001, 2.799776077761258e+001, -7.999280949145493e+000, 9.999011864530334e-001}
                },
                new double[][]{
                        new double[]{3.162120274917934e-003, -2.529686414536947e-002, 8.853877937499056e-002, -1.770772645903239e-001, 2.213464581715698e-001, -1.770772645903239e-001, 8.853877937499055e-002, -2.529686414536947e-002, 3.162120274917934e-003},
                        new double[]{1.000000000000000e+000, -7.999864602649883e+000, 2.799908292966491e+001, -5.599734091922413e+001, 6.999572174424645e+001, -5.599587528529676e+001, 2.799761729332678e+001, -7.999236471068709e+000, 9.998953110013412e-001}
                },
                new double[][]{
                        new double[]{3.162110461701167e-003, -2.529677363209544e-002, 8.853843255997608e-002, -1.770765349382640e-001, 2.213455310973644e-001, -1.770765349382640e-001, 8.853843255997609e-002, -2.529677363209545e-002, 3.162110461701166e-003},
                        new double[]{1.000000000000000e+000, -7.999854617285235e+000, 2.799901679323846e+001, -5.599715379268315e+001, 6.999542867026750e+001, -5.599560101402449e+001, 2.799746401171292e+001, -7.999189137440797e+000, 9.998890862147759e-001}
                },
                new double[][]{
                        new double[]{3.162100066857578e-003, -2.529667699540583e-002, 8.853806063708099e-002, -1.770757506593927e-001, 2.213445339017200e-001, -1.770757506593927e-001, 8.853806063708099e-002, -2.529667699540583e-002, 3.162100066857579e-003},
                        new double[]{1.000000000000000e+000, -7.999843801295476e+000, 2.799894530306873e+001, -5.599695198667576e+001, 6.999511343325074e+001, -5.599530688227819e+001, 2.799730019526186e+001, -7.999138752657308e+000, 9.998824913254165e-001}
                },
                new double[][]{
                        new double[]{3.162089056034176e-003, -2.529657378042117e-002, 8.853766156410828e-002, -1.770749071291894e-001, 2.213434605789362e-001, -1.770749071291894e-001, 8.853766156410828e-002, -2.529657378042117e-002, 3.162089056034177e-003},
                        new double[]{1.000000000000000e+000, -7.999832076282965e+000, 2.799886796679014e+001, -5.599673419333016e+001, 6.999477413540964e+001, -5.599499127423578e+001, 2.799712504364141e+001, -7.999085106323258e+000, 9.998755043309840e-001}
                },
                new double[][]{
                        new double[]{3.162077392863688e-003, -2.529646349410025e-002, 8.853723311018119e-002, -1.770739992796415e-001, 2.213423045413937e-001, -1.770739992796415e-001, 8.853723311018120e-002, -2.529646349410026e-002, 3.162077392863687e-003},
                        new double[]{1.000000000000000e+000, -7.999819355636307e+000, 2.799878424145155e+001, -5.599649897374390e+001, 6.999440869510775e+001, -5.599465242475232e+001, 2.799693768763854e+001, -7.999027971986831e+000, 9.998681019215062e-001}
                },
                new double[][]{
                        new double[]{3.162065038848255e-003, -2.529634560161019e-002, 8.853677283629112e-002, -1.770730215522448e-001, 2.213410585574313e-001, -1.770730215522448e-001, 8.853677283629112e-002, -2.529634560161019e-002, 3.162065038848254e-003},
                        new double[]{1.000000000000000e+000, -7.999805543606985e+000, 2.799869352789952e+001, -5.599624474367214e+001, 6.999401482723479e+001, -5.599428840396362e+001, 2.799673718245736e+001, -7.998967105750577e+000, 9.998602594016441e-001}
                },
                new double[][]{
                        new double[]{3.162051953236663e-003, -2.529621952231421e-002, 8.853627807368147e-002, -1.770719678456846e-001, 2.213397146821615e-001, -1.770719678456847e-001, 8.853627807368147e-002, -2.529621952231421e-002, 3.162051953236664e-003},
                        new double[]{1.000000000000000e+000, -7.999790534277842e+000, 2.799859516450793e+001, -5.599596975756637e+001, 6.999359002135154e+001, -5.599389710017644e+001, 2.799652250029955e+001, -7.998902244746753e+000, 9.998519506083922e-001}
                },
                new double[][]{
                        new double[]{3.162038092894738e-003, -2.529608462533390e-002, 8.853574589981543e-002, -1.770708314575724e-001, 2.213382641803922e-001, -1.770708314575724e-001, 8.853574589981543e-002, -2.529608462533390e-002, 3.162038092894737e-003},
                        new double[]{1.000000000000000e+000, -7.999774210410424e+000, 2.799848842017653e+001, -5.599567209076860e+001, 6.999313151734168e+001, -5.599347620084627e+001, 2.799629252214570e+001, -7.998833105462527e+000, 9.998431478239104e-001}
                },
                new double[][]{
                        new double[]{3.162023412168557e-003, -2.529594022463760e-002, 8.853517311164454e-002, -1.770696050195366e-001, 2.213366974407221e-001, -1.770696050195366e-001, 8.853517311164454e-002, -2.529594022463760e-002, 3.162023412168557e-003},
                        new double[]{1.000000000000000e+000, -7.999756442156698e+000, 2.799837248651063e+001, -5.599534961964117e+001, 6.999263627827578e+001, -5.599302317141927e+001, 2.799604602864611e+001, -7.998759381898553e+000, 9.998338216831745e-001}
                },
                new double[][]{
                        new double[]{3.162007862740170e-003, -2.529578557360153e-002, 8.853455619586464e-002, -1.770682804248874e-001, 2.213350038797683e-001, -1.770682804248875e-001, 8.853455619586466e-002, -2.529578557360154e-002, 3.162007862740171e-003},
                        new double[]{1.000000000000000e+000, -7.999737085618857e+000, 2.799824646908436e+001, -5.599499999938700e+001, 6.999210096015798e+001, -5.599253523178885e+001, 2.799578169001924e+001, -7.998680743543029e+000, 9.998239410761565e-001}
                },
                new double[][]{
                        new double[]{3.161991393475375e-003, -2.529561985898286e-002, 8.853389129580302e-002, -1.770668487479723e-001, 2.213331718353534e-001, -1.770668487479723e-001, 8.853389129580305e-002, -2.529561985898286e-002, 3.161991393475375e-003},
                        new double[]{1.000000000000000e+000, -7.999715981238941e+000, 2.799810937767752e+001, -5.599462063928445e+001, 6.999152187818606e+001, -5.599200933008674e+001, 2.799549805484331e+001, -7.998596833140963e+000, 9.998134730441964e-001}
                },
                new double[][]{
                        new double[]{3.161973950263232e-003, -2.529544219423797e-002, 8.853317417454307e-002, -1.770653001542420e-001, 2.213311884473474e-001, -1.770653001542420e-001, 8.853317417454308e-002, -2.529544219423796e-002, 3.161973950263232e-003},
                        new double[]{1.000000000000000e+000, -7.999692951997833e+000, 2.799796011536304e+001, -5.599420867502855e+001, 6.999089496911128e+001, -5.599144211349563e+001, 2.799519353761363e+001, -7.998507264236177e+000, 9.998023826702345e-001}
                },
                new double[][]{
                        new double[]{3.161955475846854e-003, -2.529525161211021e-002, 8.853240017384159e-002, -1.770636237999238e-001, 2.213290395246911e-001, -1.770636237999238e-001, 8.853240017384160e-002, -2.529525161211020e-002, 3.161955475846854e-003},
                        new double[]{1.000000000000000e+000, -7.999667801400651e+000, 2.799779746630722e+001, -5.599376093783255e+001, 6.999021574923438e+001, -5.599082989573267e+001, 2.799486640492275e+001, -7.998411618461006e+000, 9.997906329625295e-001}
                },
                new double[][]{
                        new double[]{3.161935909645136e-003, -2.529504705640347e-002, 8.853156416834446e-002, -1.770618077200683e-001, 2.213267093969643e-001, -1.770618077200683e-001, 8.853156416834446e-002, -2.529504705640347e-002, 3.161935909645137e-003},
                        new double[]{1.000000000000000e+000, -7.999640311222830e+000, 2.799762008212798e+001, -5.599327391990241e+001, 6.998947926751902e+001, -5.599016862081130e+001, 2.799451476010375e+001, -7.998309442545697e+000, 9.997781847314912e-001}
                },
                new double[][]{
                        new double[]{3.161915187564930e-003, -2.529482737284698e-002, 8.853066051454253e-002, -1.770598387035854e-001, 2.213241807486499e-001, -1.770598387035854e-001, 8.853066051454254e-002, -2.529482737284698e-002, 3.161915187564931e-003},
                        new double[]{1.000000000000000e+000, -7.999610238987999e+000, 2.799742646663783e+001, -5.599274373584947e+001, 6.998868005324024e+001, -5.598945382264122e+001, 2.799413652615765e+001, -7.998200245016235e+000, 9.997649964592074e-001}
                },
                new double[][]{
                        new double[]{3.161893241803242e-003, -2.529459129894610e-002, 8.852968299384582e-002, -1.770577021537192e-001, 2.213214344340326e-001, -1.770577021537192e-001, 8.852968299384582e-002, -2.529459129894610e-002, 3.161893241803241e-003},
                        new double[]{1.000000000000000e+000, -7.999577315145299e+000, 2.799721495877710e+001, -5.599216607955446e+001, 6.998781205751611e+001, -5.598868057997400e+001, 2.799372942676505e+001, -7.998083492545739e+000, 9.997510241612407e-001}
                },
                new double[][]{
                        new double[]{3.161870000639062e-003, -2.529433745270150e-002, 8.852862474907945e-002, -1.770553819322278e-001, 2.213184492704216e-001, -1.770553819322278e-001, 8.852862474907944e-002, -2.529433745270150e-002, 3.161870000639062e-003},
                        new double[]{1.000000000000000e+000, -7.999541239909840e+000, 2.799698371351953e+001, -5.599153617593690e+001, 6.998686858799225e+001, -5.598784346614271e+001, 2.799329096515795e+001, -7.997958605920512e+000, 9.997362212402372e-001}
                },
                new double[][]{
                        new double[]{3.161845388214309e-003, -2.529406432006455e-002, 8.852747821361813e-002, -1.770528601853185e-001, 2.213152018071011e-001, -1.770528601853185e-001, 8.852747821361814e-002, -2.529406432006455e-002, 3.161845388214308e-003},
                        new double[]{1.000000000000000e+000, -7.999501679725586e+000, 2.799673068050587e+001, -5.599084872701820e+001, 6.998584223586057e+001, -5.598693649297755e+001, 2.799281840060127e+001, -7.997824955577232e+000, 9.997205383308672e-001}
                },
                new double[][]{
                        new double[]{3.161819324303432e-003, -2.529377024098110e-002, 8.852623503228417e-002, -1.770501171491587e-001, 2.213116660671045e-001, -1.770501171491587e-001, 8.852623503228417e-002, -2.529377024098109e-002, 3.161819324303432e-003},
                        new double[]{1.000000000000000e+000, -7.999458263304954e+000, 2.799645358013133e+001, -5.599009785159184e+001, 6.998472479429390e+001, -5.598595304820432e+001, 2.799230872220294e+001, -7.997681856662638e+000, 9.997039231355781e-001}
                },
                new double[][]{
                        new double[]{3.161791724071281e-003, -2.529345339385895e-002, 8.852488597302886e-002, -1.770471309325213e-001, 2.213078132585601e-001, -1.770471309325213e-001, 8.852488597302888e-002, -2.529345339385895e-002, 3.161791724071281e-003},
                        new double[]{1.000000000000000e+000, -7.999410577193965e+000, 2.799614987677971e+001, -5.598927701773240e+001, 6.998350716726949e+001, -5.598488582555106e+001, 2.799175861973908e+001, -7.997528563561497e+000, 9.996863202506293e-001}
                },
                new double[][]{
                        new double[]{3.161762497818671e-003, -2.529311177827321e-002, 8.852342082829638e-002, -1.770438772738195e-001, 2.213036114519553e-001, -1.770438772738195e-001, 8.852342082829640e-002, -2.529311177827320e-002, 3.161762497818670e-003},
                        new double[]{1.000000000000000e+000, -7.999358160805437e+000, 2.799581674885958e+001, -5.598837896728089e+001, 6.998217926762800e+001, -5.598372674669284e+001, 2.799116445114182e+001, -7.997364263832050e+000, 9.996676709818243e-001}
                },
                new double[][]{
                        new double[]{3.161731550715255e-003, -2.529274319570281e-002, 8.852182830483955e-002, -1.770403292694686e-001, 2.212990252192333e-001, -1.770403292694686e-001, 8.852182830483955e-002, -2.529274319570281e-002, 3.161731550715255e-003},
                        new double[]{1.000000000000000e+000, -7.999300500855798e+000, 2.799545105525624e+001, -5.598739563134019e+001, 6.998072990307688e+001, -5.598246687406163e+001, 2.799052220625634e+001, -7.997188071481178e+000, 9.996479131493456e-001}
                },
                new double[][]{
                        new double[]{3.161698782519206e-003, -2.529234522806594e-002, 8.852009590060686e-002, -1.770364570701295e-001, 2.212940152301387e-001, -1.770364570701295e-001, 8.852009590060689e-002, -2.529234522806595e-002, 3.161698782519207e-003},
                        new double[]{1.000000000000000e+000, -7.999237025133197e+000, 2.799504929776608e+001, -5.598631803569721e+001, 6.997914664869103e+001, -5.598109631343003e+001, 2.798982746642563e+001, -7.996999019503344e+000, 9.996269808810403e-001}
                },
                new double[][]{
                        new double[]{3.161664087283275e-003, -2.529191521379445e-002, 8.851820976715390e-002, -1.770322275409812e-001, 2.212885378006770e-001, -1.770322275409812e-001, 8.851820976715390e-002, -2.529191521379445e-002, 3.161664087283275e-003},
                        new double[]{1.000000000000000e+000, -7.999167095515828e+000, 2.799460757902766e+001, -5.598513619495752e+001, 6.997741570428816e+001, -5.597960410504683e+001, 2.798907535940938e+001, -7.996796051598514e+000, 9.996048043934835e-001}
                },
                new double[][]{
                        new double[]{3.161627353046769e-003, -2.529145022115624e-002, 8.851615455584626e-002, -1.770276038817035e-001, 2.212825443879335e-001, -1.770276038817035e-001, 8.851615455584626e-002, -2.529145022115624e-002, 3.161627353046770e-003},
                        new double[]{1.000000000000000e+000, -7.999090000149478e+000, 2.799412155540460e+001, -5.598383899403104e+001, 6.997552173486156e+001, -5.597797810195495e+001, 2.798826050908336e+001, -7.996578012974147e+000, 9.995813097600974e-001}
                },
                new double[][]{
                        new double[]{3.161588461513018e-003, -2.529094701849926e-002, 8.851391324591054e-002, -1.770225452013237e-001, 2.212759810247989e-001, -1.770225452013237e-001, 8.851391324591053e-002, -2.529094701849925e-002, 3.161588461513017e-003},
                        new double[]{1.000000000000000e+000, -7.999004944682265e+000, 2.799358638420972e+001, -5.598241405544334e+001, 6.997344769203320e+001, -5.597620483395794e+001, 2.798737697930015e+001, -7.996343640125112e+000, 9.995564186655692e-001}
                },
                new double[][]{
                        new double[]{3.161547287711997e-003, -2.529040204105239e-002, 8.851146695215922e-002, -1.770170060425100e-001, 2.212687876873824e-001, -1.770170060425100e-001, 8.851146695215921e-002, -2.529040204105239e-002, 3.161547287711996e-003},
                        new double[]{1.000000000000000e+000, -7.998911042442045e+000, 2.799299666458529e+001, -5.598084759076193e+001, 6.997117461424480e+001, -5.597426935551604e+001, 2.798641821121696e+001, -7.996091549472803e+000, 9.995300481457619e-001}
                },
                new double[][]{
                        new double[]{3.161503699647645e-003, -2.528981135387320e-002, 8.850879470994689e-002, -1.770109358492243e-001, 2.212608975870059e-001, -1.770109358492243e-001, 8.850879470994692e-002, -2.528981135387320e-002, 3.161503699647646e-003},
                        new double[]{1.000000000000000e+000, -7.998807303428078e+000, 2.799234637127157e+001, -5.597912423422096e+001, 6.996868140312893e+001, -5.597215507564681e+001, 2.798537695331360e+001, -7.995820224730498e+000, 9.995021103122610e-001}
                },
                new double[][]{
                        new double[]{3.161457557929669e-003, -2.528917061048478e-002, 8.850587323462855e-002, -1.770042783709314e-001, 2.212522363777158e-001, -1.770042783709314e-001, 8.850587323462855e-002, -2.528917061048478e-002, 3.161457557929670e-003},
                        new double[]{1.000000000000000e+000, -7.998692621972898e+000, 2.799162878040291e+001, -5.597722685639667e+001, 6.996594457319563e+001, -5.596984356767348e+001, 2.798424518323031e+001, -7.995528002846465e+000, 9.994725120606737e-001}
                },
                new double[][]{
                        new double[]{3.161408715389472e-003, -2.528847500668750e-002, 8.850267665245455e-002, -1.769969709957286e-001, 2.212427212691440e-001, -1.769969709957285e-001, 8.850267665245455e-002, -2.528847500668750e-002, 3.161408715389472e-003},
                        new double[]{1.000000000000000e+000, -7.998565762912708e+000, 2.799083638636589e+001, -5.597513635552572e+001, 6.996293797162383e+001, -5.596731435640531e+001, 2.798301402045050e+001, -7.995213058358191e+000, 9.994411547617077e-001}
                },
                new double[][]{
                        new double[]{3.161357016680064e-003, -2.528771922897007e-002, 8.849917619947209e-002, -1.769889440038497e-001, 2.212322600333352e-001, -1.769889440038497e-001, 8.849917619947209e-002, -2.528771922897008e-002, 3.161357016680064e-003},
                        new double[]{1.000000000000000e+000, -7.998425346085041e+000, 2.798996080863779e+001, -5.597283142376993e+001, 6.995963246456225e+001, -5.596454468004423e+001, 2.798167362873678e+001, -7.994873385971648e+000, 9.994079339340317e-001}
                },
                new double[][]{
                        new double[]{3.161302297859827e-003, -2.528689739687535e-002, 8.849533988458978e-002, -1.769801197319709e-001, 2.212207498927932e-001, -1.769801197319709e-001, 8.849533988458978e-002, -2.528689739687534e-002, 3.161302297859828e-003},
                        new double[]{1.000000000000000e+000, -7.998269828950293e+000, 2.798899268739245e+001, -5.597028828540589e+001, 6.995599558591326e+001, -5.596150922378865e+001, 2.798021310709847e+001, -7.994506781157211e+000, 9.993727388978670e-001}
                },
                new double[][]{
                        new double[]{3.161244385960086e-003, -2.528600299859753e-002, 8.849113211249675e-002, -1.769704116375925e-001, 2.212080762754665e-001, -1.769704116375925e-001, 8.849113211249676e-002, -2.528600299859753e-002, 3.161244385960087e-003},
                        new double[]{1.000000000000000e+000, -7.998097487108931e+000, 2.798792156651412e+001, -5.596748040355397e+001, 6.995199114408922e+001, -5.595817982174073e+001, 2.797862036792086e+001, -7.994110818528716e+000, 9.993354524081550e-001}
                },
                new double[][]{
                        new double[]{3.161183098536588e-003, -2.528502881900131e-002, 8.848651326161339e-002, -1.769597232514924e-001, 2.211941114206876e-001, -1.769597232514925e-001, 8.848651326161340e-002, -2.528502881900131e-002, 3.161183098536589e-003},
                        new double[]{1.000000000000000e+000, -7.997906392458485e+000, 2.798673576249600e+001, -5.596437815165698e+001, 6.994757878169354e+001, -5.595452512332000e+001, 2.797688200072444e+001, -7.993682827744629e+000, 9.992959502661386e-001}
                },
                new double[][]{
                        new double[]{3.161118243205082e-003, -2.528396685915505e-002, 8.848143921166912e-002, -1.769479470048061e-001, 2.211787128181739e-001, -1.769479470048060e-001, 8.848143921166909e-002, -2.528396685915505e-002, 3.161118243205083e-003},
                        new double[]{1.000000000000000e+000, -7.997694388703194e+000, 2.798542221751621e+001, -5.596094844546255e+001, 6.994271348247383e+001, -5.595051021993152e+001, 2.797498311983799e+001, -7.993219866638839e+000, 9.992541009080764e-001}
                },
                new double[][]{
                        new double[]{3.161049617161414e-003, -2.528280824636124e-002, 8.847586081485707e-002, -1.769349629156903e-001, 2.211617214600661e-001, -1.769349629156903e-001, 8.847586081485709e-002, -2.528280824636124e-002, 3.161049617161414e-003},
                        new double[]{1.000000000000000e+000, -7.997459063894291e+000, 2.798396633477872e+001, -5.595715433075780e+001, 6.993734501922353e+001, -5.594609622713119e+001, 2.797290719406510e+001, -7.992718691253814e+000, 9.992097649697828e-001}
                },
                new double[][]{
                        new double[]{3.160977006686676e-003, -2.528154313354447e-002, 8.846972330378967e-002, -1.769206371187328e-001, 2.211429598836020e-001, -1.769206371187328e-001, 8.846972330378969e-002, -2.528154313354447e-002, 3.160977006686676e-003},
                        new double[]{1.000000000000000e+000, -7.997197719639722e+000, 2.798235179397666e+001, -5.595295452153719e+001, 6.993141733555554e+001, -5.594123981696578e+001, 2.797063585619458e+001, -7.992175722409599e+000, 9.991627948255754e-001}
                },
                new double[][]{
                        new double[]{3.160900186638132e-003, -2.528016058671942e-002, 8.846296562866780e-002, -1.769048202182677e-001, 2.211222299793627e-001, -1.769048202182677e-001, 8.846296562866780e-002, -2.528016058671942e-002, 3.160900186638132e-003},
                        new double[]{1.000000000000000e+000, -7.996907336578110e+000, 2.798056034447828e+001, -5.594830288265121e+001, 6.992486785363307e+001, -5.593589269453279e+001, 2.796814868994899e+001, -7.991587008398287e+000, 9.991130341001322e-001}
                },
                new double[][]{
                        new double[]{3.160818919927040e-003, -2.527864845910844e-002, 8.845551971517662e-002, -1.768873454445385e-001, 2.210993105370871e-001, -1.768873454445385e-001, 8.845551971517662e-002, -2.527864845910844e-002, 3.160818919927039e-003},
                        new double[]{1.000000000000000e+000, -7.996584535662630e+000, 2.797857157354876e+001, -5.594314785027997e+001, 6.991762669901105e+001, -5.593000101210501e+001, 2.796542299168110e+001, -7.990948183344891e+000, 9.990603171517305e-001}
                },
                new double[][]{
                        new double[]{3.160732956984562e-003, -2.527699325030442e-002, 8.844730963360412e-002, -1.768680265891523e-001, 2.210739544977368e-001, -1.768680265891523e-001, 8.844730963360412e-002, -2.527699325030443e-002, 3.160732956984563e-003},
                        new double[]{1.000000000000000e+000, -7.996225534745101e+000, 2.797636264659958e+001, -5.593743178278633e+001, 6.990961583269734e+001, -5.592350471337751e+001, 2.796243350380717e+001, -7.990254420720088e+000, 9.990044685251582e-001}
                },
                new double[][]{
                        new double[]{3.160642035217503e-003, -2.527517994868275e-002, 8.843825066855632e-002, -1.768466556935119e-001, 2.210458858768428e-001, -1.768466556935119e-001, 8.843825066855633e-002, -2.527517994868275e-002, 3.160642035217504e-003},
                        new double[]{1.000000000000000e+000, -7.995826099888900e+000, 2.797390801609919e+001, -5.593109023362921e+001, 6.990074807938849e+001, -5.591633679952461e+001, 2.795915211661225e+001, -7.989500381429437e+000, 9.989453023725972e-001}
                },
                new double[][]{
                        new double[]{3.160545878455811e-003, -2.527319185504862e-002, 8.842824827738170e-002, -1.768230004608285e-001, 2.210147963200810e-001, -1.768230004608285e-001, 8.842824827738169e-002, -2.527319185504861e-002, 3.160545878455811e-003},
                        new double[]{1.000000000000000e+000, -7.995381490769717e+000, 2.797117909537814e+001, -5.592405113704149e+001, 6.989092603954595e+001, -5.590842250778280e+001, 2.795554753466443e+001, -7.988680155834519e+000, 9.988826218405983e-001}
                },
                new double[][]{
                        new double[]{3.160444196394361e-003, -2.527101038526481e-002, 8.841719692401759e-002, -1.767968013589008e-001, 2.209803412475103e-001, -1.767968013589008e-001, 8.841719692401759e-002, -2.527101038526481e-002, 3.160444196394361e-003},
                        new double[]{1.000000000000000e+000, -7.994886399445699e+000, 2.796814389311626e+001, -5.591623389609305e+001, 6.988004087155034e+001, -5.589967839219742e+001, 2.795158490363400e+001, -7.987787198984727e+000, 9.988162184212104e-001}
                },
                new double[][]{
                        new double[]{3.160336684031966e-003, -2.526861484933287e-002, 8.840497877340901e-002, -1.767677683770464e-001, 2.209421355378812e-001, -1.767677683770464e-001, 8.840497877340901e-002, -2.526861484933288e-002, 3.160336684031967e-003},
                        new double[]{1.000000000000000e+000, -7.994334881691234e+000, 2.796476660380225e+001, -5.590754836155735e+001, 6.986797092858842e+001, -5.589001129497501e+001, 2.794722539282159e+001, -7.986814258252410e+000, 9.987458712652104e-001}
                },
                new double[][]{
                        new double[]{3.160223021111152e-003, -2.526598220409768e-002, 8.839146222991390e-002, -1.767355773963661e-001, 2.208997486988848e-001, -1.767355773963661e-001, 8.839146222991388e-002, -2.526598220409768e-002, 3.160223021111152e-003},
                        new double[]{1.000000000000000e+000, -7.993720279991338e+000, 2.796100714890299e+001, -5.589789368866482e+001, 6.985458023317315e+001, -5.587931719555724e+001, 2.794242572815215e+001, -7.985753292467819e+000, 9.986713464552553e-001}
                },
                new double[][]{
                        new double[]{3.160102871562913e-003, -2.526308677640546e-002, 8.837650030117852e-002, -1.766998661278661e-001, 2.208526994630720e-001, -1.766998661278661e-001, 8.837650030117855e-002, -2.526308677640547e-002, 3.160102871562914e-003},
                        new double[]{1.000000000000000e+000, -7.993035137184169e+000, 2.795682066286448e+001, -5.588715705735085e+001, 6.983971677026145e+001, -5.586747992306563e+001, 2.793713766978453e+001, -7.984595381542750e+000, 9.985923962366661e-001}
                },
                new double[][]{
                        new double[]{3.159975882961421e-003, -2.525989995316554e-002, 8.835992876682405e-002, -1.766602295678194e-001, 2.208004497424176e-001, -1.766602295678194e-001, 8.835992876682405e-002, -2.525989995316553e-002, 3.159975882961421e-003},
                        new double[]{1.000000000000000e+000, -7.992271099617168e+000, 2.795215691738355e+001, -5.587521223997506e+001, 6.982321057779913e+001, -5.585436971614900e+001, 2.793130742781282e+001, -7.983330625451835e+000, 9.985087582034402e-001}
                },
                new double[][]{
                        new double[]{3.159841685994492e-003, -2.525638983434093e-002, 8.834156412890981e-002, -1.766162149140839e-001, 2.207423978670707e-001, -1.766162149140839e-001, 8.834156412890981e-002, -2.525638983434093e-002, 3.159841685994491e-003},
                        new double[]{1.000000000000000e+000, -7.991418808545359e+000, 2.794695967663140e+001, -5.586191799869053e+001, 6.980487161117404e+001, -5.583984161248014e+001, 2.792487500878889e+001, -7.981948031305878e+000, 9.984201544369321e-001}
                },
                new double[][]{
                        new double[]{3.159699893956621e-003, -2.525252084441813e-002, 8.832120131851101e-002, -1.765673158808659e-001, 2.206778710256798e-001, -1.765673158808659e-001, 8.832120131851101e-002, -2.525252084441813e-002, 3.159699893956621e-003},
                        new double[]{1.000000000000000e+000, -7.990467778347355e+000, 2.794116597527122e+001, -5.584711629266529e+001, 6.978448735548403e+001, -5.582373364819085e+001, 2.791777348497057e+001, -7.980435387101997e+000, 9.983262905945257e-001}
                },
                new double[][]{
                        new double[]{3.159550102272552e-003, -2.524825329737595e-002, 8.829861112984655e-002, -1.765129663425945e-001, 2.206061168157774e-001, -1.765129663425945e-001, 8.829861112984654e-002, -2.524825329737595e-002, 3.159550102272551e-003},
                        new double[]{1.000000000000000e+000, -7.989406259963432e+000, 2.793470531018452e+001, -5.583063027319029e+001, 6.976182015671301e+001, -5.580586484538701e+001, 2.790992817728934e+001, -7.978779120568047e+000, 9.982268549454829e-001}
                },
                new double[][]{
                        new double[]{3.159391888060492e-003, -2.524354290957952e-002, 8.827353735018950e-002, -1.764525332301130e-001, 2.205262938030033e-001, -1.764525332301129e-001, 8.827353735018949e-002, -2.524354290957952e-002, 3.159391888060493e-003},
                        new double[]{1.000000000000000e+000, -7.988221087768674e+000, 2.792749873579812e+001, -5.581226204234034e+001, 6.973660423984350e+001, -5.578603296354181e+001, 2.790125574202765e+001, -7.976964141331972e+000, 9.981215173509664e-001}
                },
                new double[][]{
                        new double[]{3.159224809745750e-003, -2.523834025436573e-002, 8.824569355028043e-002, -1.763853085942930e-001, 2.204374609774531e-001, -1.763853085942930e-001, 8.824569355028043e-002, -2.523834025436573e-002, 3.159224809745750e-003},
                        new double[]{1.000000000000000e+000, -7.986897507880478e+000, 2.791945785177781e+001, -5.579179014827696e+001, 6.970854237861690e+001, -5.576401198801909e+001, 2.789166315009525e+001, -7.974973664439123e+000, 9.980099281851373e-001}
                },
                new double[][]{
                        new double[]{3.159048406736869e-003, -2.523259015134536e-002, 8.821475949609868e-002, -1.763105007433883e-001, 2.203385659840945e-001, -1.763105007433883e-001, 8.821475949609867e-002, -2.523259015134535e-002, 3.159048406736868e-003},
                        new double[]{1.000000000000000e+000, -7.985418985660877e+000, 2.791048367061966e+001, -5.576896678748984e+001, 6.967730217807400e+001, -5.573954932622059e+001, 2.788104654658634e+001, -7.972789013008361e+000, 9.978917171939963e-001}
                },
                new double[][]{
                        new double[]{3.158862199178640e-003, -2.522623098262760e-002, 8.818037713864389e-002, -1.762272243510122e-001, 2.202284319921079e-001, -1.762272243510122e-001, 8.818037713864388e-002, -2.522623098262760e-002, 3.158862199178640e-003},
                        new double[]{1.000000000000000e+000, -7.983766989907910e+000, 2.790046535131966e+001, -5.574351468126776e+001, 6.964251192718245e+001, -5.571236267888894e+001, 2.786928997698394e+001, -7.970389397560299e+000, 9.977664922885320e-001}
                },
                new double[][]{
                        new double[]{3.158665687797979e-003, -2.521919392725215e-002, 8.814214613378270e-002, -1.761344894215090e-001, 2.201057430551121e-001, -1.761344894215089e-001, 8.814214613378270e-002, -2.521919392725215e-002, 3.158665687797979e-003},
                        new double[]{1.000000000000000e+000, -7.981920750933047e+000, 2.788927878383032e+001, -5.571512359047303e+001, 6.960375597479234e+001, -5.568213655092985e+001, 2.785626396494602e+001, -7.967751669263686e+000, 9.976338382685047e-001}
                },
                new double[][]{
                        new double[]{3.158458353861474e-003, -2.521140210409663e-002, 8.809961883926078e-002, -1.760311889888082e-001, 2.199690278007572e-001, -1.760311889888082e-001, 8.809961883926079e-002, -2.521140210409664e-002, 3.158458353861475e-003},
                        new double[]{1.000000000000000e+000, -7.979856989390820e+000, 2.787678500741772e+001, -5.568344642928296e+001, 6.956056957789367e+001, -5.564851836276790e+001, 2.784182392506172e+001, -7.964850044027480e+000, 9.974933154730581e-001}
                },
                new double[][]{
                        new double[]{3.158239659265769e-003, -2.520276961238962e-002, 8.805229473062350e-002, -1.759160854136171e-001, 2.198166412741269e-001, -1.759160854136170e-001, 8.805229473062347e-002, -2.520276961238962e-002, 3.158239659265770e-003},
                        new double[]{1.000000000000000e+000, -7.977549612358271e+000, 2.786282844431199e+001, -5.564809493498185e+001, 6.951243316672097e+001, -5.561111411973872e+001, 2.782580839230226e+001, -7.961655794012175e+000, 9.973444583541184e-001}
                },
                new double[][]{
                        new double[]{3.158009046785217e-003, -2.519320045770075e-002, 8.799961417207584e-002, -1.757877951322226e-001, 2.196467447451271e-001, -1.757877951322225e-001, 8.799961417207582e-002, -2.519320045770074e-002, 3.158009046785216e-003},
                        new double[]{1.000000000000000e+000, -7.974969372752160e+000, 2.784723492820081e+001, -5.560863484718004e+001, 6.945876596673548e+001, -5.556948359339760e+001, 2.780803704811647e+001, -7.958136902741812e+000, 9.971867739683715e-001}
                },
                new double[][]{
                        new double[]{3.157765940504518e-003, -2.518258734987953e-002, 8.794095147224382e-002, -1.756447716983805e-001, 2.194572832757883e-001, -1.756447716983806e-001, 8.794095147224382e-002, -2.518258734987952e-002, 3.157765940504517e-003},
                        new double[]{1.000000000000000e+000, -7.972083487716799e+000, 2.782980950515790e+001, -5.556458054606449e+001, 6.939891891301646e+001, -5.552313496494917e+001, 2.778830852124351e+001, -7.954257679564011e+000, 9.970197403833869e-001}
                },
                new double[][]{
                        new double[]{3.157509746467995e-003, -2.517081035786543e-002, 8.787560714843677e-002, -1.754852869480970e-001, 2.192459608296765e-001, -1.754852869480970e-001, 8.787560714843674e-002, -2.517081035786543e-002, 3.157509746467995e-003},
                        new double[]{1.000000000000000e+000, -7.968855211110624e+000, 2.781033398253884e+001, -5.551538909553598e+001, 6.933216678824905e+001, -5.547151887736864e+001, 2.776639793935085e+001, -7.949978328725671e+000, 9.968428049932396e-001}
                },
                new double[][]{
                        new double[]{3.157239853581603e-003, -2.515773540457894e-002, 8.780279931642641e-002, -1.753074101059224e-001, 2.190102126929847e-001, -1.753074101059224e-001, 8.780279931642640e-002, -2.515773540457894e-002, 3.157239853581603e-003},
                        new double[]{1.000000000000000e+000, -7.965243354660791e+000, 2.778856419923966e+001, -5.546045363348811e+001, 6.925769951150495e+001, -5.541402183931861e+001, 2.774205420558135e+001, -7.945254467803112e+000, 9.966553827387890e-001}
                },
                new double[][]{
                        new double[]{3.156955634808492e-003, -2.514321258320968e-002, 8.772165411605427e-002, -1.751089846412184e-001, 2.187471749661684e-001, -1.751089846412184e-001, 8.772165411605427e-002, -2.514321258320967e-002, 3.156955634808492e-003},
                        new double[]{1.000000000000000e+000, -7.961201751737143e+000, 2.776422698853192e+001, -5.539909604819387e+001, 6.917461250162503e+001, -5.534995892083294e+001, 2.771499697204265e+001, -7.940036589642881e+000, 9.964568542275056e-001}
                },
                new double[][]{
                        new double[]{3.156656448704776e-003, -2.512707427413613e-002, 8.763119507633681e-002, -1.748876026744579e-001, 2.184536508773114e-001, -1.748876026744579e-001, 8.763119507633681e-002, -2.512707427413613e-002, 3.156656448704775e-003},
                        new double[]{1.000000000000000e+000, -7.956678657015536e+000, 2.773701680251237e+001, -5.533055887701768e+001, 6.908189603657752e+001, -5.527856567817344e+001, 2.768491328025209e+001, -7.934269461330747e+000, 9.962465637475458e-001}
                },
                new double[][]{
                        new double[]{3.156341641348188e-003, -2.510913303942006e-002, 8.753033131735499e-002, -1.746405767278798e-001, 2.181260736651187e-001, -1.746405767278799e-001, 8.753033131735499e-002, -2.510913303942007e-002, 3.156341641348189e-003},
                        new double[]{1.000000000000000e+000, -7.951616074550573e+000, 2.770659196510449e+001, -5.525399636175364e+001, 6.897842352911954e+001, -5.519898924355911e+001, 2.765145383663062e+001, -7.927891453006585e+000, 9.960238171705083e-001}
                },
                new double[][]{
                        new double[]{3.156010548719496e-003, -2.508917926931127e-002, 8.741784448041817e-002, -1.743649086130051e-001, 2.177604657823905e-001, -1.743649086130051e-001, 8.741784448041816e-002, -2.508917926931127e-002, 3.156010548719496e-003},
                        new double[]{1.000000000000000e+000, -7.945949005955032e+000, 2.767257051861824e+001, -5.516846459419217e+001, 6.886293864000967e+001, -5.511027851501318e+001, 2.761422888941914e+001, -7.920833788580041e+000, 9.957878797370805e-001}
                },
                new double[][]{
                        new double[]{3.155662499604399e-003, -2.506697855245887e-002, 8.729237427317103e-002, -1.740572552511763e-001, 2.173523941823380e-001, -1.740572552511763e-001, 8.729237427317105e-002, -2.506697855245887e-002, 3.155662499604400e-003},
                        new double[]{1.000000000000000e+000, -7.939604609483376e+000, 2.763452562724007e+001, -5.507291068653348e+001, 6.873404115363863e+001, -5.501137338286942e+001, 2.757280367200363e+001, -7.913019709572767e+000, 9.955379737195335e-001}
                },
                new double[][]{
                        new double[]{3.155296819092648e-003, -2.504226873854748e-002, 8.715240251302218e-002, -1.737138912344610e-001, 2.168969214731223e-001, -1.737138912344610e-001, 8.715240251302220e-002, -2.504226873854748e-002, 3.155296819092648e-003},
                        new double[]{1.000000000000000e+000, -7.932501259836564e+000, 2.759198049965839e+001, -5.496616090463505e+001, 6.859017154819686e+001, -5.490109293316875e+001, 2.752669337675260e+001, -7.904363542416965e+000, 9.952732759547681e-001}
                },
                new double[][]{
                        new double[]{3.154912832761072e-003, -2.501475665885260e-002, 8.699623555120342e-002, -1.733306679554957e-001, 2.163885527642557e-001, -1.733306679554957e-001, 8.699623555120342e-002, -2.501475665885260e-002, 3.154912832761072e-003},
                        new double[]{1.000000000000000e+000, -7.924547497442289e+000, 2.754440279254992e+001, -5.484690770861077e+001, 6.842959420455813e+001, -5.477812257507029e+001, 2.747535762332123e+001, -7.894769658576227e+000, 9.949929152413864e-001}
                },
                new double[][]{
                        new double[]{3.154509871638553e-003, -2.498411446674099e-002, 8.682198496185380e-002, -1.729029691694235e-001, 2.158211780866830e-001, -1.729029691694236e-001, 8.682198496185381e-002, -2.498411446674100e-002, 3.154509871638554e-003},
                        new double[]{1.000000000000000e+000, -7.915640854818626e+000, 2.749119845715203e+001, -5.471369565601653e+001, 6.825037921640217e+001, -5.464100005052808e+001, 2.741819438625329e+001, -7.884131315825985e+000, 9.946959695939834e-001}
                },
                new double[][]{
                        new double[]{3.154087278063668e-003, -2.494997555643428e-002, 8.662754638693286e-002, -1.724256629030175e-001, 2.151880103522501e-001, -1.724256629030176e-001, 8.662754638693286e-002, -2.494997555643428e-002, 3.154087278063668e-003},
                        new double[]{1.000000000000000e+000, -7.905666546401251e+000, 2.743170499299610e+001, -5.456490613904224e+001, 6.805038279056032e+001, -5.448810030112487e+001, 2.735453334899452e+001, -7.872329367941698e+000, 9.943814633476313e-001}
                },
                new double[][]{
                        new double[]{3.153644412559933e-003, -2.491193001442541e-002, 8.641057644002366e-002, -1.718930497004701e-001, 2.144815189350482e-001, -1.718930497004701e-001, 8.641057644002366e-002, -2.491193001442541e-002, 3.153644412559932e-003},
                        new double[]{1.000000000000000e+000, -7.894496006909852e+000, 2.736518407655722e+001, -5.439874095038135e+001, 6.782722626340617e+001, -5.431761919070840e+001, 2.728362865560162e+001, -7.859230828901897e+000, 9.940483641052797e-001}
                },
                new double[][]{
                        new double[]{3.153180661869607e-003, -2.486951955381822e-002, 8.616846759211466e-002, -1.712988072983833e-001, 2.136933591155672e-001, -1.712988072983833e-001, 8.616846759211465e-002, -2.486951955381822e-002, 3.153180661869607e-003},
                        new double[]{1.000000000000000e+000, -7.881985261957821e+000, 2.729081353869786e+001, -5.421320470473966e+001, 6.757827380910089e+001, -5.412755611530962e+001, 2.720465103810294e+001, -7.844687276533637e+000, 9.936955795205367e-001}
                },
                new double[][]{
                        new double[]{3.152695448305012e-003, -2.482223187759939e-002, 8.589832099269115e-002, -1.706359319618829e-001, 2.128142978393528e-001, -1.706359319618829e-001, 8.589832099269117e-002, -2.482223187759939e-002, 3.152695448305013e-003},
                        new double[]{1.000000000000000e+000, -7.867973113185387e+000, 2.720767867411994e+001, -5.400608618669720e+001, 6.730060898200142e+001, -5.391569557615249e+001, 2.711667930741871e+001, -7.828533079330270e+000, 9.933219539081104e-001}
                },
                new double[][]{
                        new double[]{3.152188240596396e-003, -2.476949441252039e-002, 8.559691722298893e-002, -1.698966768987765e-001, 2.118341365177477e-001, -1.698966768987765e-001, 8.559691722298890e-002, -2.476949441252038e-002, 3.152188240596395e-003},
                        new double[]{1.000000000000000e+000, -7.852279118743454e+000, 2.711476287956804e+001, -5.377493875379191e+001, 6.699101032259173e+001, -5.367958785031539e+001, 2.701869120994906e+001, -7.810583428982896e+000, 9.929262646739174e-001}
                },
                new double[][]{
                        new double[]{3.151658566438113e-003, -2.471066735097116e-002, 8.526068503891726e-002, -1.690724884104834e-001, 2.107416319545301e-001, -1.690724884104833e-001, 8.526068503891726e-002, -2.471066735097116e-002, 3.151658566438114e-003},
                        new double[]{1.000000000000000e+000, -7.834701348506457e+000, 2.701093763646996e+001, -5.351705999979735e+001, 6.664592636870657e+001, -5.341652897030031e+001, 2.690955367161049e+001, -7.790632160028131e+000, 9.925072185567274e-001}
                },
                new double[][]{
                        new double[]{3.151106026960063e-003, -2.464503593410573e-002, 8.488566824372117e-002, -1.681539407503443e-001, 2.095244169366758e-001, -1.681539407503443e-001, 8.488566824372115e-002, -2.464503593410573e-002, 3.151106026960062e-003},
                        new double[]{1.000000000000000e+000, -7.815013891993500e+000, 2.689495187958396e+001, -5.322947098143593e+001, 6.626145055721739e+001, -5.312354032258413e+001, 2.678801247775242e+001, -7.768449336977040e+000, 9.920634476729724e-001}
                },
                new double[][]{
                        new double[]{3.150530313380003e-003, -2.457180190573428e-002, 8.446749094086682e-002, -1.671306710575339e-001, 2.081689226006989e-001, -1.671306710575339e-001, 8.446749094086682e-002, -2.457180190573428e-002, 3.150530313380004e-003},
                        new double[]{1.000000000000000e+000, -7.792964095691631e+000, 2.676542082793782e+001, -5.290889543716165e+001, 6.583329668224586e+001, -5.279734830107660e+001, 2.665268147288917e+001, -7.743778588425860e+000, 9.915935053562469e-001}
                },
                new double[][]{
                        new double[]{3.149931226123974e-003, -2.449007406337997e-002, 8.400132156317211e-002, -1.659913162360803e-001, 2.066603054012899e-001, -1.659913162360803e-001, 8.400132156317211e-002, -2.449007406337997e-002, 3.149931226123974e-003},
                        new double[]{1.000000000000000e+000, -7.768269505387938e+000, 2.662081440022254e+001, -5.255173958488378e+001, 6.535677580170375e+001, -5.243436460993682e+001, 2.650203141082893e+001, -7.716334167049936e+000, 9.910958617828615e-001}
                },
                new double[][]{
                        new double[]{3.149308696737955e-003, -2.439885783076391e-002, 8.348183626380033e-002, -1.647234542738494e-001, 2.049823823919444e-001, -1.647234542738494e-001, 8.348183626380033e-002, -2.439885783076391e-002, 3.149308696737955e-003},
                        new double[]{1.000000000000000e+000, -7.740614488341203e+000, 2.645944539669780e+001, -5.215407328305949e+001, 6.482677576262216e+001, -5.203066800777763e+001, 2.633437864637221e+001, -7.685797714171228e+000, 9.905688993747558e-001}
                },
                new double[][]{
                        new double[]{3.148662812953755e-003, -2.429704377527410e-002, 8.290318249918893e-002, -1.633135532680249e-001, 2.031175796050522e-001, -1.633135532680249e-001, 8.290318249918893e-002, -2.429704377527410e-002, 3.148662812953756e-003},
                        new double[]{1.000000000000000e+000, -7.709646509807524e+000, 2.627945770704299e+001, -5.171161358357100e+001, 6.423774485589274e+001, -5.158198852888927e+001, 2.614787393767223e+001, -7.651814707922908e+000, 9.900109079710157e-001}
                },
                new double[][]{
                        new double[]{3.147993847316972e-003, -2.418339499528223e-002, 8.225894394631153e-002, -1.617469323651304e-001, 2.010468996187549e-001, -1.617469323651304e-001, 8.225894394631153e-002, -2.418339499528223e-002, 3.147993847316972e-003},
                        new double[]{1.000000000000000e+000, -7.674972038770115e+000, 2.607881490262433e+001, -5.121971200272805e+001, 6.358368152129840e+001, -5.108369551406377e+001, 2.594049172766093e+001, -7.613990575114853e+000, 9.894200797592629e-001}
                },
                new double[][]{
                        new double[]{3.147302289834950e-003, -2.405653330624902e-002, 8.154210829236015e-002, -1.600077399582097e-001, 1.987499159446394e-001, -1.600077399582097e-001, 8.154210829236014e-002, -2.405653330624902e-002, 3.147302289834948e-003},
                        new double[]{1.000000000000000e+000, -7.636152058960868e+000, 2.585528969732743e+001, -5.067334719604580e+001, 6.285813251191995e+001, -5.053079114110794e+001, 2.571002039846987e+001, -7.571886448988433e+000, 9.887945039582496e-001}
                },
                new double[][]{
                        new double[]{3.146588885158769e-003, -2.391492416236799e-002, 8.074503993219968e-002, -1.580789558318979e-001, 1.962048036828320e-001, -1.580789558318979e-001, 8.074503993219971e-002, -2.391492416236799e-002, 3.146588885158770e-003},
                        new double[]{1.000000000000000e+000, -7.592697163719579e+000, 2.560645491929913e+001, -5.006712514976279e+001, 6.205420249911512e+001, -4.991791156951130e+001, 2.545405415016516e+001, -7.525014558465190e+000, 9.881321612431455e-001}
                },
                new double[][]{
                        new double[]{3.145854674876142e-003, -2.375686026326680e-002, 7.985946022891163e-002, -1.559424255217798e-001, 1.933884179764724e-001, -1.559424255217798e-001, 7.985946022891163e-002, -2.375686026326680e-002, 3.145854674876142e-003},
                        new double[]{1.000000000000000e+000, -7.544062217323155e+000, 2.532967683347243e+001, -4.939528950196758e+001, 6.116457875756177e+001, -4.923933830918798e+001, 2.516998735089173e+001, -7.472833239653872e+000, 9.874309179052380e-001}
                },
                new double[][]{
                        new double[]{3.145101045562206e-003, -2.358044381456427e-002, 7.887643875940420e-002, -1.535789369592204e-001, 1.902764341435763e-001, -1.535789369592204e-001, 7.887643875940420e-002, -2.358044381456427e-002, 3.145101045562206e-003},
                        new double[]{1.000000000000000e+000, -7.489640571648333e+000, 2.502211189939137e+001, -4.865174517941539e+001, 6.018157531047837e+001, -4.848902298999204e+001, 2.485501244705817e+001, -7.414741567800956e+000, 9.866885197380821e-001}
                },
                new double[][]{
                        new double[]{3.144329783313850e-003, -2.338356743882254e-002, 7.778639990704114e-002, -1.509683514875424e-001, 1.868435659282270e-001, -1.509683514875424e-001, 7.778639990704114e-002, -2.338356743882254e-002, 3.144329783313851e-003},
                        new double[]{1.000000000000000e+000, -7.428757836057749e+000, 2.468070834925526e+001, -4.783009917679499e+001, 5.909720172464564e+001, -4.766062934041341e+001, 2.450612281760468e+001, -7.350073618197181e+000, 9.859025856426016e-001}
                },
                new double[][]{
                        new double[]{3.143543135581006e-003, -2.316389377207868e-002, 7.657915030499854e-002, -1.480898035077080e-001, 1.830638810073308e-001, -1.480898035077080e-001, 7.657915030499855e-002, -2.316389377207868e-002, 3.143543135581006e-003},
                        new double[]{1.000000000000000e+000, -7.360665211036957e+000, 2.430221433655941e+001, -4.692372299709454e+001, 5.790326259567788e+001, -4.674759686361156e+001, 2.412012231428596e+001, -7.278092378587070e+000, 9.850706009441995e-001}
                },
                new double[][]{
                        new double[]{3.142743881206541e-003, -2.291883383372955e-002, 7.524393400650334e-002, -1.449219852393075e-001, 1.789112355627868e-001, -1.449219852393075e-001, 7.524393400650334e-002, -2.291883383372955e-002, 3.142743881206540e-003},
                        new double[]{1.000000000000000e+000, -7.284532413376133e+000, 2.388319484571587e+001, -4.592584198278260e+001, 5.659149460855977e+001, -4.574323139402281e+001, 2.369364365886558e+001, -7.197983354359358e+000, 9.841899104156673e-001}
                },
                new double[][]{
                        new double[]{3.141935409696230e-003, -2.264552432784142e-002, 7.376952388809109e-002, -1.414435351904935e-001, 1.743598521445707e-001, -1.414435351904935e-001, 7.376952388809109e-002, -2.264552432784142e-002, 3.141935409696230e-003},
                        new double[]{1.000000000000000e+000, -7.199440243846447e+000, 2.342006007650916e+001, -4.482965744266117e+001, 5.515374882202251e+001, -4.464082837240964e+001, 2.322317837594225e+001, -7.108847932436513e+000, 9.832577110005878e-001}
                },
                new double[][]{
                        new double[]{3.141121810862988e-003, -2.234080412659672e-002, 7.214435970035693e-002, -1.376335506400123e-001, 1.693850668579320e-001, -1.376335506400123e-001, 7.214435970035690e-002, -2.234080412659671e-002, 3.141121810862987e-003},
                        new double[]{1.000000000000000e+000, -7.104372878921088e+000, 2.290910863125306e+001, -4.362850803060598e+001, 5.358222639763521e+001, -4.343383520301292e+001, 2.270512153246824e+001, -7.009696601809882e+000, 9.822710442329992e-001}
                },
                new double[][]{
                        new double[]{3.140307976126783e-003, -2.200119030724153e-002, 7.035674537599862e-002, -1.334722453248443e-001, 1.639642726170729e-001, -1.334722453248443e-001, 7.035674537599862e-002, -2.200119030724154e-002, 3.140307976126782e-003},
                        new double[]{1.000000000000000e+000, -6.998210008037751e+000, 2.234658954035024e+001, -4.231607712696974e+001, 5.186977622103262e+001, -4.211605932017192e+001, 2.213583523342565e+001, -6.899442169873026e+000, 9.812267883503994e-001}
                },
                new double[][]{
                        new double[]{3.139499712905021e-003, -2.162285426944518e-002, 6.839512067458584e-002, -1.289417729867781e-001, 1.580780841620924e-001, -1.289417729867781e-001, 6.839512067458588e-002, -2.162285426944519e-002, 3.139499712905022e-003},
                        new double[]{1.000000000000000e+000, -6.879718989504162e+000, 2.172878796019500e+001, -4.088665281760313e+001, 5.001026253686516e+001, -4.068192840019685e+001, 2.151173558210775e+001, -6.776893166237469e+000, 9.801216500987595e-001}
                },
                new double[][]{
                        new double[]{3.138703873699729e-003, -2.120159865872217e-002, 6.624842494020339e-002, -1.240272346018628e-001, 1.517117468996463e-001, -1.240272346018628e-001, 6.624842494020337e-002, -2.120159865872217e-002, 3.138703873699729e-003},
                        new double[]{1.000000000000000e+000, -6.747547264196248e+000, 2.105214025163815e+001, -3.933544618694712e+001, 4.799900957772635e+001, -3.912680823655984e+001, 2.082940863624978e+001, -6.640747692124122e+000, 9.789521562300232e-001}
                },
                new double[][]{
                        new double[]{3.137928501679498e-003, -2.073283607339804e-002, 6.390657357142512e-002, -1.187178808303045e-001, 1.448568043951183e-001, -1.187178808303045e-001, 6.390657357142512e-002, -2.073283607339804e-002, 3.137928501679498e-003},
                        new double[]{1.000000000000000e+000, -6.600215350011449e+000, 2.031338506624802e+001, -3.765897166957116e+001, 4.583332789420625e+001, -3.744738178488232e+001, 2.008576174391972e+001, -6.489588056709191e+000, 9.777146446947530e-001}
                },
                new double[][]{
                        new double[]{3.137182994768761e-003, -2.021157084921369e-002, 6.136107057341432e-002, -1.130085099447135e-001, 1.375130273952083e-001, -1.130085099447135e-001, 6.136107057341432e-002, -2.021157084921369e-002, 3.137182994768761e-003},
                        new double[]{1.000000000000000e+000, -6.436110845532803e+000, 1.950975797479131e+001, -3.585548965015296e+001, 4.351312333645753e+001, -3.564208929582135e+001, 1.927821746633142e+001, -6.321876645437641e+000, 9.764052555351180e-001}
                },
                new double[][]{
                        new double[]{3.136478290496714e-003, -1.963238561066010e-002, 5.860578298576107e-002, -1.069010432752794e-001, 1.296905894905115e-001, -1.069010432752794e-001, 5.860578298576107e-002, -1.963238561066010e-002, 3.136478290496713e-003},
                        new double[]{1.000000000000000e+000, -6.253484003079926e+000, 1.863923797465455e+001, -3.392550574495159e+001, 4.104158405102900e+001, -3.371162367818436e+001, 1.840495799303932e+001, -6.135953593248777e+000, 9.750199214863040e-001}
                },
                new double[][]{
                        new double[]{3.135827074126856e-003, -1.898943476709113e-002, 5.563790460357721e-002, -1.004062326978215e-001, 1.214124500942118e-001, -1.004062326978215e-001, 5.563790460357720e-002, -1.898943476709113e-002, 3.135827074126855e-003},
                        new double[]{1.000000000000000e+000, -6.050445594454034e+000, 1.770085476591669e+001, -3.187231248663630e+001, 3.842593317714272e+001, -3.165946653481059e+001, 1.746522838225852e+001, -5.930036992274631e+000, 9.735543582978614e-001}
                },
                new double[][]{
                        new double[]{3.135244012888753e-003, -1.827644773199040e-002, 5.245913656145098e-002, -9.354541521470149e-002, 1.127168741389934e-001, -9.354541521470149e-002, 5.245913656145097e-002, -1.827644773199040e-002, 3.135244012888753e-003},
                        new double[]{1.000000000000000e+000, -5.824967992989347e+000, 1.669506577467719e+001, -2.970254661371650e+001, 3.567822510278449e+001, -2.949243785701340e+001, 1.645970690065039e+001, -5.702226553585581e+000, 9.720040547903410e-001}
                },
                new double[][]{
                        new double[]{3.134746019470630e-003, -1.748674537436416e-002, 4.907711006745098e-002, -8.635217549983100e-002, 1.036599808864124e-001, -8.635217549983101e-002, 4.907711006745098e-002, -1.748674537436416e-002, 3.134746019470632e-003},
                        new double[]{1.000000000000000e+000, -5.574890639196458e+000, 1.562421121838405e+001, -2.742671795429572e+001, 3.281615152807541e+001, -2.722121529947888e+001, 1.539094988747847e+001, -5.450511870800915e+000, 9.703642626670224e-001}
                },
                new double[][]{
                        new double[]{3.134352548309350e-003, -1.661327409169017e-002, 4.550707051926735e-002, -7.887370609052574e-002, 9.431817474222426e-002, -7.887370609052574e-002, 4.550707051926735e-002, -1.661327409169017e-002, 3.134352548309349e-003},
                        new double[]{1.000000000000000e+000, -5.297931350649583e+000, 1.449305362107864e+001, -2.505964324185187e+001, 2.986381121167740e+001, -2.486075657853854e+001, 1.426391650604211e+001, -5.172786704431734e+000, 9.686299861056348e-001}
                },
                new double[][]{
                        new double[]{3.134085928638540e-003, -1.564866294308467e-002, 4.177383060073082e-002, -7.117156573103166e-002, 8.479027540628278e-002, -7.117156573103163e-002, 4.177383060073081e-002, -1.564866294308467e-002, 3.134085928638538e-003},
                        new double[]{1.000000000000000e+000, -4.991705285766048e+000, 1.330940451460967e+001, -2.262068977353927e+001, 2.685238618316034e+001, -2.243053056001658e+001, 1.308657490580105e+001, -4.866871023409204e+000, 9.667959711606842e-001}
                },
                new double[][]{
                        new double[]{3.133971738732764e-003, -1.458531051532196e-002, 3.791398038204636e-002, -6.332143101523267e-002, 7.519914376860126e-002, -6.332143101523267e-002, 3.791398038204637e-002, -1.458531051532196e-002, 3.133971738732764e-003},
                        new double[]{1.000000000000000e+000, -4.653753780459771e+000, 1.208483484175470e+001, -2.013370016357715e+001, 2.382066078021547e+001, -1.995442963213826e+001, 1.187058496830032e+001, -4.530542906340166e+000, 9.648566950135764e-001}
                },
                new double[][]{
                        new double[]{3.134039226322169e-003, -1.341550962251499e-002, 3.397831195038829e-002, -5.541132177031712e-002, 6.569260943266918e-002, -5.541132177031712e-002, 3.397831195038829e-002, -1.341550962251499e-002, 3.134039226322169e-003},
                        new double[]{1.000000000000000e+000, -4.281585748495832e+000, 1.083545584338935e+001, -1.762643268940588e+001, 2.081532308871231e+001, -1.746020018575418e+001, 1.063204303178750e+001, -4.161582818466576e+000, 9.628063551150658e-001}
                },
                new double[][]{
                        new double[]{3.134321780757537e-003, -1.213161956026688e-002, 3.003437108339710e-002, -4.753767160414881e-002, 5.644356598658096e-002, -4.753767160414879e-002, 3.003437108339709e-002, -1.213161956026688e-002, 3.134321780757537e-003},
                        new double[]{1.000000000000000e+000, -3.872734868428340e+000, 9.582742796500902e+000, -1.512931661567666e+001, 1.789100765938705e+001, -1.497819422522096e+001, 9.392259689330881e+000, -3.757833234835760e+000, 9.606388582728882e-001}
                },
                new double[][]{
                        new double[]{3.134857463189034e-003, -1.072629742487928e-002, 2.616898486943640e-002, -3.979853915444663e-002, 4.764923335280723e-002, -3.979853915444661e-002, 2.616898486943639e-002, -1.072629742487927e-002, 3.134857463189034e-003},
                        new double[]{1.000000000000000e+000, -3.424836362665021e+000, 8.354353544174984e+000, -1.267329710343470e+001, 1.511008073912570e+001, -1.253922198523031e+001, 8.178521712285489e+000, -3.317277061692935e+000, 9.583478097467500e-001}
                },
                new double[][]{
                        new double[]{3.135689601795952e-003, -9.192801914856020e-003, 2.249052777972339e-002, -3.228325770744059e-002, 3.952980949329304e-002, -3.228325770744058e-002, 2.249052777972340e-002, -9.192801914856018e-003, 3.135689601795953e-003},
                        new double[]{1.000000000000000e+000, -2.935727786560088e+000, 7.184865948619916e+000, -1.028654413040799e+001, 1.254223993346572e+001, -1.017128706803972e+001, 7.024762141100510e+000, -2.838138790755699e+000, 9.559265024233198e-001}
                },
                new double[][]{
                        new double[]{3.136867459982495e-003, -7.525384956076330e-003, 1.913057593393776e-002, -2.505796833460762e-002, 3.232704353501763e-002, -2.505796833460763e-002, 1.913057593393775e-002, -7.525384956076330e-003, 3.136867459982495e-003},
                        new double[]{1.000000000000000e+000, -2.403578849731980e+000, 6.116322021836488e+000, -7.989845724149395e+000, 1.026409819970081e+001, -7.895033186180221e+000, 5.972027625440275e+000, -2.319012762106196e+000, 9.533679061555159e-001}
                },
                new double[][]{
                        new double[]{3.138446986453599e-003, -5.719788266387029e-003, 1.624445853637538e-002, -1.814686749013245e-002, 2.630361280892667e-002, -1.814686749013246e-002, 1.624445853637540e-002, -5.719788266387029e-003, 3.138446986453599e-003},
                        new double[]{1.000000000000000e+000, -1.827055825922343e+000, 5.198420980989063e+000, -5.790627461845995e+000, 8.359032593241103e+000, -5.717854195620120e+000, 5.068588868120155e+000, -1.759023244522341e+000, 9.506646573634051e-001}
                },
                new double[][]{
                        new double[]{3.140491657224234e-003, -3.773863336298222e-003, 1.401004915216966e-002, -1.150974045018174e-002, 2.174445408345166e-002, -1.150974045018174e-002, 1.401004915216966e-002, -3.773863336298222e-003, 3.140491657224234e-003},
                        new double[]{1.000000000000000e+000, -1.205526475294007e+000, 4.488149587584013e+000, -3.675774949486338e+000, 6.917662040320396e+000, -3.626853005615643e+000, 4.369489565598928e+000, -1.158021168308530e+000, 9.478090490084538e-001}
                },
                new double[][]{
                        new double[]{3.143073420920189e-003, -1.688333852644997e-003, 1.262395748797918e-002, -5.017573087974836e-003, 1.896114476128465e-002, -5.017573087974833e-003, 1.262395748797917e-002, -1.688333852644996e-003, 3.143073420920189e-003},
                        new double[]{1.000000000000000e+000, -5.393114608165123e-001, 4.048578996469638e+000, -1.603824537959893e+000, 6.039299478305788e+000, -1.581219217193783e+000, 3.935274955307954e+000, -5.168221229728411e-001, 9.447930210688752e-001}
                },
                new double[][]{
                        new double[]{3.146273760226500e-003, 5.322812464931087e-004, 1.229410658041627e-002, 1.570162041369536e-003, 1.829966777695046e-002, 1.570162041369536e-003, 1.229410658041628e-002, 5.322812464931094e-004, 3.146273760226500e-003},
                        new double[]{1.000000000000000e+000, 1.700122175818937e-001, 3.946500014377161e+000, 5.023368561645172e-001, 5.834479755741242e+000, 4.948389524665681e-001, 3.829590220830911e+000, 1.625105377327566e-001, 9.416081516614653e-001}
                },
                new double[][]{
                        new double[]{3.150184884067147e-003, 2.878568940453699e-003, 1.322755106586022e-002, 8.620552071421581e-003, 2.014990932533000e-002, 8.620552071421581e-003, 1.322755106586022e-002, 2.878568940453698e-003, 3.150184884067147e-003},
                        new double[]{1.000000000000000e+000, 9.192515080342589e-001, 4.248526154251194e+000, 2.760354359206612e+000, 6.428032354219372e+000, 2.716723422282723e+000, 4.115304532346304e+000, 8.763352262803313e-001, 9.382456489747480e-001}
                },
                new double[][]{
                        new double[]{3.154911067098878e-003, 5.334938740144918e-003, 1.561237564818605e-002, 1.662582664159492e-002, 2.495150819282764e-002, 1.662582664159493e-002, 1.561237564818605e-002, 5.334938740144917e-003, 3.154911067098878e-003},
                        new double[]{1.000000000000000e+000, 1.703179499277949e+000, 5.015287582907513e+000, 5.328060497664982e+000, 7.960981372090174e+000, 5.238878049409704e+000, 4.848819300199692e+000, 1.619054784052110e+000, 9.346963441995914e-001}
                }
        };
    }

    private static class MIDI_COEFFICIENTS_SR22050HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.162146540394787e-003, -2.529710298793881e-002, 8.853968712030330e-002, -1.770791662360805e-001, 2.213488711266425e-001, -1.770791662360805e-001, 8.853968712030330e-002, -2.529710298793881e-002, 3.162146540394789e-003},
                        new double[]{1.000000000000000e+000, -7.999890252530271e+000, 2.799925348345496e+001, -5.599782559580713e+001, 6.999648456594744e+001, -5.599659313625823e+001, 2.799802102247260e+001, -7.999362053942496e+000, 9.999119666631262e-001}
                },
                new double[][]{
                        new double[]{3.162138284466419e-003, -2.529702844978658e-002, 8.853940501010447e-002, -1.770785765438014e-001, 2.213481233980341e-001, -1.770785765438014e-001, 8.853940501010449e-002, -2.529702844978658e-002, 3.162138284466419e-003},
                        new double[]{1.000000000000000e+000, -7.999882358899898e+000, 2.799920088749939e+001, -5.599767578594843e+001, 6.999624817897040e+001, -5.599637004488871e+001, 2.799789514473500e+001, -7.999322753640388e+000, 9.999067321726334e-001}
                },
                new double[][]{
                        new double[]{3.162129538956708e-003, -2.529694895523159e-002, 8.853910295315416e-002, -1.770779438388397e-001, 2.213473206039208e-001, -1.770779438388397e-001, 8.853910295315416e-002, -2.529694895523159e-002, 3.162129538956707e-003},
                        new double[]{1.000000000000000e+000, -7.999873828400071e+000, 2.799914415916124e+001, -5.599751455597762e+001, 6.999599438675247e+001, -5.599613117645615e+001, 2.799776077761258e+001, -7.999280949145493e+000, 9.999011864530334e-001}
                },
                new double[][]{
                        new double[]{3.162120274917934e-003, -2.529686414536947e-002, 8.853877937499056e-002, -1.770772645903239e-001, 2.213464581715698e-001, -1.770772645903239e-001, 8.853877937499055e-002, -2.529686414536947e-002, 3.162120274917934e-003},
                        new double[]{1.000000000000000e+000, -7.999864602649883e+000, 2.799908292966491e+001, -5.599734091922413e+001, 6.999572174424645e+001, -5.599587528529676e+001, 2.799761729332678e+001, -7.999236471068709e+000, 9.998953110013412e-001}
                },
                new double[][]{
                        new double[]{3.162110461701167e-003, -2.529677363209544e-002, 8.853843255997608e-002, -1.770765349382640e-001, 2.213455310973644e-001, -1.770765349382640e-001, 8.853843255997609e-002, -2.529677363209545e-002, 3.162110461701166e-003},
                        new double[]{1.000000000000000e+000, -7.999854617285235e+000, 2.799901679323846e+001, -5.599715379268315e+001, 6.999542867026750e+001, -5.599560101402449e+001, 2.799746401171292e+001, -7.999189137440797e+000, 9.998890862147759e-001}
                },
                new double[][]{
                        new double[]{3.162100066857578e-003, -2.529667699540583e-002, 8.853806063708099e-002, -1.770757506593927e-001, 2.213445339017200e-001, -1.770757506593927e-001, 8.853806063708099e-002, -2.529667699540583e-002, 3.162100066857579e-003},
                        new double[]{1.000000000000000e+000, -7.999843801295476e+000, 2.799894530306873e+001, -5.599695198667576e+001, 6.999511343325074e+001, -5.599530688227819e+001, 2.799730019526186e+001, -7.999138752657308e+000, 9.998824913254165e-001}
                },
                new double[][]{
                        new double[]{3.162089056034176e-003, -2.529657378042117e-002, 8.853766156410828e-002, -1.770749071291894e-001, 2.213434605789362e-001, -1.770749071291894e-001, 8.853766156410828e-002, -2.529657378042117e-002, 3.162089056034177e-003},
                        new double[]{1.000000000000000e+000, -7.999832076282965e+000, 2.799886796679014e+001, -5.599673419333016e+001, 6.999477413540964e+001, -5.599499127423578e+001, 2.799712504364141e+001, -7.999085106323258e+000, 9.998755043309840e-001}
                },
                new double[][]{
                        new double[]{3.162077392863688e-003, -2.529646349410025e-002, 8.853723311018119e-002, -1.770739992796415e-001, 2.213423045413937e-001, -1.770739992796415e-001, 8.853723311018120e-002, -2.529646349410026e-002, 3.162077392863687e-003},
                        new double[]{1.000000000000000e+000, -7.999819355636307e+000, 2.799878424145155e+001, -5.599649897374390e+001, 6.999440869510775e+001, -5.599465242475232e+001, 2.799693768763854e+001, -7.999027971986831e+000, 9.998681019215062e-001}
                },
                new double[][]{
                        new double[]{3.162065038848255e-003, -2.529634560161019e-002, 8.853677283629112e-002, -1.770730215522448e-001, 2.213410585574313e-001, -1.770730215522448e-001, 8.853677283629112e-002, -2.529634560161019e-002, 3.162065038848254e-003},
                        new double[]{1.000000000000000e+000, -7.999805543606985e+000, 2.799869352789952e+001, -5.599624474367214e+001, 6.999401482723479e+001, -5.599428840396362e+001, 2.799673718245736e+001, -7.998967105750577e+000, 9.998602594016441e-001}
                },
                new double[][]{
                        new double[]{3.162051953236663e-003, -2.529621952231421e-002, 8.853627807368147e-002, -1.770719678456846e-001, 2.213397146821615e-001, -1.770719678456847e-001, 8.853627807368147e-002, -2.529621952231421e-002, 3.162051953236664e-003},
                        new double[]{1.000000000000000e+000, -7.999790534277842e+000, 2.799859516450793e+001, -5.599596975756637e+001, 6.999359002135154e+001, -5.599389710017644e+001, 2.799652250029955e+001, -7.998902244746753e+000, 9.998519506083922e-001}
                },
                new double[][]{
                        new double[]{3.162038092894738e-003, -2.529608462533390e-002, 8.853574589981543e-002, -1.770708314575724e-001, 2.213382641803922e-001, -1.770708314575724e-001, 8.853574589981543e-002, -2.529608462533390e-002, 3.162038092894737e-003},
                        new double[]{1.000000000000000e+000, -7.999774210410424e+000, 2.799848842017653e+001, -5.599567209076860e+001, 6.999313151734168e+001, -5.599347620084627e+001, 2.799629252214570e+001, -7.998833105462527e+000, 9.998431478239104e-001}
                },
                new double[][]{
                        new double[]{3.162023412168557e-003, -2.529594022463760e-002, 8.853517311164454e-002, -1.770696050195366e-001, 2.213366974407221e-001, -1.770696050195366e-001, 8.853517311164454e-002, -2.529594022463760e-002, 3.162023412168557e-003},
                        new double[]{1.000000000000000e+000, -7.999756442156698e+000, 2.799837248651063e+001, -5.599534961964117e+001, 6.999263627827578e+001, -5.599302317141927e+001, 2.799604602864611e+001, -7.998759381898553e+000, 9.998338216831745e-001}
                },
                new double[][]{
                        new double[]{3.162007862740170e-003, -2.529578557360153e-002, 8.853455619586464e-002, -1.770682804248874e-001, 2.213350038797683e-001, -1.770682804248875e-001, 8.853455619586466e-002, -2.529578557360154e-002, 3.162007862740171e-003},
                        new double[]{1.000000000000000e+000, -7.999737085618857e+000, 2.799824646908436e+001, -5.599499999938700e+001, 6.999210096015798e+001, -5.599253523178885e+001, 2.799578169001924e+001, -7.998680743543029e+000, 9.998239410761565e-001}
                },
                new double[][]{
                        new double[]{3.161991393475375e-003, -2.529561985898286e-002, 8.853389129580302e-002, -1.770668487479723e-001, 2.213331718353534e-001, -1.770668487479723e-001, 8.853389129580305e-002, -2.529561985898286e-002, 3.161991393475375e-003},
                        new double[]{1.000000000000000e+000, -7.999715981238941e+000, 2.799810937767752e+001, -5.599462063928445e+001, 6.999152187818606e+001, -5.599200933008674e+001, 2.799549805484331e+001, -7.998596833140963e+000, 9.998134730441964e-001}
                },
                new double[][]{
                        new double[]{3.161973950263232e-003, -2.529544219423797e-002, 8.853317417454307e-002, -1.770653001542420e-001, 2.213311884473474e-001, -1.770653001542420e-001, 8.853317417454308e-002, -2.529544219423796e-002, 3.161973950263232e-003},
                        new double[]{1.000000000000000e+000, -7.999692951997833e+000, 2.799796011536304e+001, -5.599420867502855e+001, 6.999089496911128e+001, -5.599144211349563e+001, 2.799519353761363e+001, -7.998507264236177e+000, 9.998023826702345e-001}
                },
                new double[][]{
                        new double[]{3.161955475846854e-003, -2.529525161211021e-002, 8.853240017384159e-002, -1.770636237999238e-001, 2.213290395246911e-001, -1.770636237999238e-001, 8.853240017384160e-002, -2.529525161211020e-002, 3.161955475846854e-003},
                        new double[]{1.000000000000000e+000, -7.999667801400651e+000, 2.799779746630722e+001, -5.599376093783255e+001, 6.999021574923438e+001, -5.599082989573267e+001, 2.799486640492275e+001, -7.998411618461006e+000, 9.997906329625295e-001}
                },
                new double[][]{
                        new double[]{3.161935909645136e-003, -2.529504705640347e-002, 8.853156416834446e-002, -1.770618077200683e-001, 2.213267093969643e-001, -1.770618077200683e-001, 8.853156416834446e-002, -2.529504705640347e-002, 3.161935909645137e-003},
                        new double[]{1.000000000000000e+000, -7.999640311222830e+000, 2.799762008212798e+001, -5.599327391990241e+001, 6.998947926751902e+001, -5.599016862081130e+001, 2.799451476010375e+001, -7.998309442545697e+000, 9.997781847314912e-001}
                },
                new double[][]{
                        new double[]{3.161915187564930e-003, -2.529482737284698e-002, 8.853066051454253e-002, -1.770598387035854e-001, 2.213241807486499e-001, -1.770598387035854e-001, 8.853066051454254e-002, -2.529482737284698e-002, 3.161915187564931e-003},
                        new double[]{1.000000000000000e+000, -7.999610238987999e+000, 2.799742646663783e+001, -5.599274373584947e+001, 6.998868005324024e+001, -5.598945382264122e+001, 2.799413652615765e+001, -7.998200245016235e+000, 9.997649964592074e-001}
                },
                new double[][]{
                        new double[]{3.161893241803242e-003, -2.529459129894610e-002, 8.852968299384582e-002, -1.770577021537192e-001, 2.213214344340326e-001, -1.770577021537192e-001, 8.852968299384582e-002, -2.529459129894610e-002, 3.161893241803241e-003},
                        new double[]{1.000000000000000e+000, -7.999577315145299e+000, 2.799721495877710e+001, -5.599216607955446e+001, 6.998781205751611e+001, -5.598868057997400e+001, 2.799372942676505e+001, -7.998083492545739e+000, 9.997510241612407e-001}
                },
                new double[][]{
                        new double[]{3.161870000639062e-003, -2.529433745270150e-002, 8.852862474907945e-002, -1.770553819322278e-001, 2.213184492704216e-001, -1.770553819322278e-001, 8.852862474907944e-002, -2.529433745270150e-002, 3.161870000639062e-003},
                        new double[]{1.000000000000000e+000, -7.999541239909840e+000, 2.799698371351953e+001, -5.599153617593690e+001, 6.998686858799225e+001, -5.598784346614271e+001, 2.799329096515795e+001, -7.997958605920512e+000, 9.997362212402372e-001}
                },
                new double[][]{
                        new double[]{3.161845388214309e-003, -2.529406432006455e-002, 8.852747821361813e-002, -1.770528601853185e-001, 2.213152018071011e-001, -1.770528601853185e-001, 8.852747821361814e-002, -2.529406432006455e-002, 3.161845388214308e-003},
                        new double[]{1.000000000000000e+000, -7.999501679725586e+000, 2.799673068050587e+001, -5.599084872701820e+001, 6.998584223586057e+001, -5.598693649297755e+001, 2.799281840060127e+001, -7.997824955577232e+000, 9.997205383308672e-001}
                },
                new double[][]{
                        new double[]{3.161819324303432e-003, -2.529377024098110e-002, 8.852623503228417e-002, -1.770501171491587e-001, 2.213116660671045e-001, -1.770501171491587e-001, 8.852623503228417e-002, -2.529377024098109e-002, 3.161819324303432e-003},
                        new double[]{1.000000000000000e+000, -7.999458263304954e+000, 2.799645358013133e+001, -5.599009785159184e+001, 6.998472479429390e+001, -5.598595304820432e+001, 2.799230872220294e+001, -7.997681856662638e+000, 9.997039231355781e-001}
                },
                new double[][]{
                        new double[]{3.161791724071281e-003, -2.529345339385895e-002, 8.852488597302886e-002, -1.770471309325213e-001, 2.213078132585601e-001, -1.770471309325213e-001, 8.852488597302888e-002, -2.529345339385895e-002, 3.161791724071281e-003},
                        new double[]{1.000000000000000e+000, -7.999410577193965e+000, 2.799614987677971e+001, -5.598927701773240e+001, 6.998350716726949e+001, -5.598488582555106e+001, 2.799175861973908e+001, -7.997528563561497e+000, 9.996863202506293e-001}
                },
                new double[][]{
                        new double[]{3.161762497818671e-003, -2.529311177827321e-002, 8.852342082829638e-002, -1.770438772738195e-001, 2.213036114519553e-001, -1.770438772738195e-001, 8.852342082829640e-002, -2.529311177827320e-002, 3.161762497818670e-003},
                        new double[]{1.000000000000000e+000, -7.999358160805437e+000, 2.799581674885958e+001, -5.598837896728089e+001, 6.998217926762800e+001, -5.598372674669284e+001, 2.799116445114182e+001, -7.997364263832050e+000, 9.996676709818243e-001}
                },
                new double[][]{
                        new double[]{3.161731550715255e-003, -2.529274319570281e-002, 8.852182830483955e-002, -1.770403292694686e-001, 2.212990252192333e-001, -1.770403292694686e-001, 8.852182830483955e-002, -2.529274319570281e-002, 3.161731550715255e-003},
                        new double[]{1.000000000000000e+000, -7.999300500855798e+000, 2.799545105525624e+001, -5.598739563134019e+001, 6.998072990307688e+001, -5.598246687406163e+001, 2.799052220625634e+001, -7.997188071481178e+000, 9.996479131493456e-001}
                },
                new double[][]{
                        new double[]{3.161698782519206e-003, -2.529234522806594e-002, 8.852009590060686e-002, -1.770364570701295e-001, 2.212940152301387e-001, -1.770364570701295e-001, 8.852009590060689e-002, -2.529234522806595e-002, 3.161698782519207e-003},
                        new double[]{1.000000000000000e+000, -7.999237025133197e+000, 2.799504929776608e+001, -5.598631803569721e+001, 6.997914664869103e+001, -5.598109631343003e+001, 2.798982746642563e+001, -7.996999019503344e+000, 9.996269808810403e-001}
                },
                new double[][]{
                        new double[]{3.161664087283275e-003, -2.529191521379445e-002, 8.851820976715390e-002, -1.770322275409812e-001, 2.212885378006770e-001, -1.770322275409812e-001, 8.851820976715390e-002, -2.529191521379445e-002, 3.161664087283275e-003},
                        new double[]{1.000000000000000e+000, -7.999167095515828e+000, 2.799460757902766e+001, -5.598513619495752e+001, 6.997741570428816e+001, -5.597960410504683e+001, 2.798907535940938e+001, -7.996796051598514e+000, 9.996048043934835e-001}
                },
                new double[][]{
                        new double[]{3.161627353046769e-003, -2.529145022115624e-002, 8.851615455584626e-002, -1.770276038817035e-001, 2.212825443879335e-001, -1.770276038817035e-001, 8.851615455584626e-002, -2.529145022115624e-002, 3.161627353046770e-003},
                        new double[]{1.000000000000000e+000, -7.999090000149478e+000, 2.799412155540460e+001, -5.598383899403104e+001, 6.997552173486156e+001, -5.597797810195495e+001, 2.798826050908336e+001, -7.996578012974147e+000, 9.995813097600974e-001}
                },
                new double[][]{
                        new double[]{3.161588461513018e-003, -2.529094701849926e-002, 8.851391324591054e-002, -1.770225452013237e-001, 2.212759810247989e-001, -1.770225452013237e-001, 8.851391324591053e-002, -2.529094701849925e-002, 3.161588461513017e-003},
                        new double[]{1.000000000000000e+000, -7.999004944682265e+000, 2.799358638420972e+001, -5.598241405544334e+001, 6.997344769203320e+001, -5.597620483395794e+001, 2.798737697930015e+001, -7.996343640125112e+000, 9.995564186655692e-001}
                },
                new double[][]{
                        new double[]{3.161547287711997e-003, -2.529040204105239e-002, 8.851146695215922e-002, -1.770170060425100e-001, 2.212687876873824e-001, -1.770170060425100e-001, 8.851146695215921e-002, -2.529040204105239e-002, 3.161547287711996e-003},
                        new double[]{1.000000000000000e+000, -7.998911042442045e+000, 2.799299666458529e+001, -5.598084759076193e+001, 6.997117461424480e+001, -5.597426935551604e+001, 2.798641821121696e+001, -7.996091549472803e+000, 9.995300481457619e-001}
                },
                new double[][]{
                        new double[]{3.161503699647645e-003, -2.528981135387320e-002, 8.850879470994689e-002, -1.770109358492243e-001, 2.212608975870059e-001, -1.770109358492243e-001, 8.850879470994692e-002, -2.528981135387320e-002, 3.161503699647646e-003},
                        new double[]{1.000000000000000e+000, -7.998807303428078e+000, 2.799234637127157e+001, -5.597912423422096e+001, 6.996868140312893e+001, -5.597215507564681e+001, 2.798537695331360e+001, -7.995820224730498e+000, 9.995021103122610e-001}
                },
                new double[][]{
                        new double[]{3.161457557929669e-003, -2.528917061048478e-002, 8.850587323462855e-002, -1.770042783709314e-001, 2.212522363777158e-001, -1.770042783709314e-001, 8.850587323462855e-002, -2.528917061048478e-002, 3.161457557929670e-003},
                        new double[]{1.000000000000000e+000, -7.998692621972898e+000, 2.799162878040291e+001, -5.597722685639667e+001, 6.996594457319563e+001, -5.596984356767348e+001, 2.798424518323031e+001, -7.995528002846465e+000, 9.994725120606737e-001}
                },
                new double[][]{
                        new double[]{3.161408715389472e-003, -2.528847500668750e-002, 8.850267665245455e-002, -1.769969709957286e-001, 2.212427212691440e-001, -1.769969709957285e-001, 8.850267665245455e-002, -2.528847500668750e-002, 3.161408715389472e-003},
                        new double[]{1.000000000000000e+000, -7.998565762912708e+000, 2.799083638636589e+001, -5.597513635552572e+001, 6.996293797162383e+001, -5.596731435640531e+001, 2.798301402045050e+001, -7.995213058358191e+000, 9.994411547617077e-001}
                },
                new double[][]{
                        new double[]{3.161357016680064e-003, -2.528771922897007e-002, 8.849917619947209e-002, -1.769889440038497e-001, 2.212322600333352e-001, -1.769889440038497e-001, 8.849917619947209e-002, -2.528771922897008e-002, 3.161357016680064e-003},
                        new double[]{1.000000000000000e+000, -7.998425346085041e+000, 2.798996080863779e+001, -5.597283142376993e+001, 6.995963246456225e+001, -5.596454468004423e+001, 2.798167362873678e+001, -7.994873385971648e+000, 9.994079339340317e-001}
                },
                new double[][]{
                        new double[]{3.161302297859827e-003, -2.528689739687535e-002, 8.849533988458978e-002, -1.769801197319709e-001, 2.212207498927932e-001, -1.769801197319709e-001, 8.849533988458978e-002, -2.528689739687534e-002, 3.161302297859828e-003},
                        new double[]{1.000000000000000e+000, -7.998269828950293e+000, 2.798899268739245e+001, -5.597028828540589e+001, 6.995599558591326e+001, -5.596150922378865e+001, 2.798021310709847e+001, -7.994506781157211e+000, 9.993727388978670e-001}
                },
                new double[][]{
                        new double[]{3.161244385960086e-003, -2.528600299859753e-002, 8.849113211249675e-002, -1.769704116375925e-001, 2.212080762754665e-001, -1.769704116375925e-001, 8.849113211249675e-002, -2.528600299859753e-002, 3.161244385960087e-003},
                        new double[]{1.000000000000000e+000, -7.998097487108931e+000, 2.798792156651412e+001, -5.596748040355397e+001, 6.995199114408922e+001, -5.595817982174073e+001, 2.797862036792086e+001, -7.994110818528716e+000, 9.993354524081550e-001}
                },
                new double[][]{
                        new double[]{3.161183098536588e-003, -2.528502881900131e-002, 8.848651326161339e-002, -1.769597232514924e-001, 2.211941114206876e-001, -1.769597232514925e-001, 8.848651326161340e-002, -2.528502881900131e-002, 3.161183098536589e-003},
                        new double[]{1.000000000000000e+000, -7.997906392458485e+000, 2.798673576249600e+001, -5.596437815165698e+001, 6.994757878169354e+001, -5.595452512332000e+001, 2.797688200072444e+001, -7.993682827744629e+000, 9.992959502661386e-001}
                },
                new double[][]{
                        new double[]{3.161118243205082e-003, -2.528396685915505e-002, 8.848143921166912e-002, -1.769479470048061e-001, 2.211787128181739e-001, -1.769479470048060e-001, 8.848143921166909e-002, -2.528396685915505e-002, 3.161118243205083e-003},
                        new double[]{1.000000000000000e+000, -7.997694388703194e+000, 2.798542221751621e+001, -5.596094844546255e+001, 6.994271348247383e+001, -5.595051021993152e+001, 2.797498311983799e+001, -7.993219866638839e+000, 9.992541009080764e-001}
                },
                new double[][]{
                        new double[]{3.161049617161414e-003, -2.528280824636124e-002, 8.847586081485707e-002, -1.769349629156903e-001, 2.211617214600661e-001, -1.769349629156903e-001, 8.847586081485709e-002, -2.528280824636124e-002, 3.161049617161414e-003},
                        new double[]{1.000000000000000e+000, -7.997459063894291e+000, 2.798396633477872e+001, -5.595715433075780e+001, 6.993734501922353e+001, -5.594609622713119e+001, 2.797290719406510e+001, -7.992718691253814e+000, 9.992097649697828e-001}
                },
                new double[][]{
                        new double[]{3.160977006686676e-003, -2.528154313354447e-002, 8.846972330378967e-002, -1.769206371187328e-001, 2.211429598836020e-001, -1.769206371187328e-001, 8.846972330378969e-002, -2.528154313354447e-002, 3.160977006686676e-003},
                        new double[]{1.000000000000000e+000, -7.997197719639722e+000, 2.798235179397666e+001, -5.595295452153719e+001, 6.993141733555554e+001, -5.594123981696578e+001, 2.797063585619458e+001, -7.992175722409599e+000, 9.991627948255754e-001}
                },
                new double[][]{
                        new double[]{3.160900186638132e-003, -2.528016058671942e-002, 8.846296562866780e-002, -1.769048202182677e-001, 2.211222299793627e-001, -1.769048202182677e-001, 8.846296562866780e-002, -2.528016058671942e-002, 3.160900186638132e-003},
                        new double[]{1.000000000000000e+000, -7.996907336578110e+000, 2.798056034447828e+001, -5.594830288265121e+001, 6.992486785363307e+001, -5.593589269453279e+001, 2.796814868994899e+001, -7.991587008398287e+000, 9.991130341001322e-001}
                },
                new double[][]{
                        new double[]{3.160818919927040e-003, -2.527864845910844e-002, 8.845551971517662e-002, -1.768873454445385e-001, 2.210993105370871e-001, -1.768873454445385e-001, 8.845551971517662e-002, -2.527864845910844e-002, 3.160818919927039e-003},
                        new double[]{1.000000000000000e+000, -7.996584535662630e+000, 2.797857157354876e+001, -5.594314785027997e+001, 6.991762669901105e+001, -5.593000101210501e+001, 2.796542299168110e+001, -7.990948183344891e+000, 9.990603171517305e-001}
                },
                new double[][]{
                        new double[]{3.160732956984562e-003, -2.527699325030442e-002, 8.844730963360412e-002, -1.768680265891523e-001, 2.210739544977368e-001, -1.768680265891523e-001, 8.844730963360412e-002, -2.527699325030443e-002, 3.160732956984563e-003},
                        new double[]{1.000000000000000e+000, -7.996225534745101e+000, 2.797636264659958e+001, -5.593743178278633e+001, 6.990961583269734e+001, -5.592350471337751e+001, 2.796243350380717e+001, -7.990254420720088e+000, 9.990044685251582e-001}
                },
                new double[][]{
                        new double[]{3.160642035217503e-003, -2.527517994868275e-002, 8.843825066855632e-002, -1.768466556935119e-001, 2.210458858768428e-001, -1.768466556935119e-001, 8.843825066855633e-002, -2.527517994868275e-002, 3.160642035217504e-003},
                        new double[]{1.000000000000000e+000, -7.995826099888900e+000, 2.797390801609919e+001, -5.593109023362921e+001, 6.990074807938849e+001, -5.591633679952461e+001, 2.795915211661225e+001, -7.989500381429437e+000, 9.989453023725972e-001}
                },
                new double[][]{
                        new double[]{3.160545878455811e-003, -2.527319185504862e-002, 8.842824827738170e-002, -1.768230004608285e-001, 2.210147963200810e-001, -1.768230004608285e-001, 8.842824827738169e-002, -2.527319185504861e-002, 3.160545878455811e-003},
                        new double[]{1.000000000000000e+000, -7.995381490769717e+000, 2.797117909537814e+001, -5.592405113704149e+001, 6.989092603954595e+001, -5.590842250778280e+001, 2.795554753466443e+001, -7.988680155834519e+000, 9.988826218405983e-001}
                },
                new double[][]{
                        new double[]{3.160444196394361e-003, -2.527101038526481e-002, 8.841719692401759e-002, -1.767968013589008e-001, 2.209803412475103e-001, -1.767968013589008e-001, 8.841719692401759e-002, -2.527101038526481e-002, 3.160444196394361e-003},
                        new double[]{1.000000000000000e+000, -7.994886399445699e+000, 2.796814389311626e+001, -5.591623389609305e+001, 6.988004087155034e+001, -5.589967839219742e+001, 2.795158490363400e+001, -7.987787198984727e+000, 9.988162184212104e-001}
                },
                new double[][]{
                        new double[]{3.160336684031966e-003, -2.526861484933287e-002, 8.840497877340901e-002, -1.767677683770464e-001, 2.209421355378812e-001, -1.767677683770464e-001, 8.840497877340901e-002, -2.526861484933288e-002, 3.160336684031967e-003},
                        new double[]{1.000000000000000e+000, -7.994334881691234e+000, 2.796476660380225e+001, -5.590754836155735e+001, 6.986797092858842e+001, -5.589001129497501e+001, 2.794722539282159e+001, -7.986814258252410e+000, 9.987458712652104e-001}
                },
                new double[][]{
                        new double[]{3.160223021111152e-003, -2.526598220409768e-002, 8.839146222991390e-002, -1.767355773963661e-001, 2.208997486988848e-001, -1.767355773963661e-001, 8.839146222991388e-002, -2.526598220409768e-002, 3.160223021111152e-003},
                        new double[]{1.000000000000000e+000, -7.993720279991338e+000, 2.796100714890299e+001, -5.589789368866482e+001, 6.985458023317315e+001, -5.587931719555724e+001, 2.794242572815215e+001, -7.985753292467819e+000, 9.986713464552553e-001}
                },
                new double[][]{
                        new double[]{3.160102871562913e-003, -2.526308677640546e-002, 8.837650030117852e-002, -1.766998661278661e-001, 2.208526994630720e-001, -1.766998661278661e-001, 8.837650030117855e-002, -2.526308677640547e-002, 3.160102871562914e-003},
                        new double[]{1.000000000000000e+000, -7.993035137184169e+000, 2.795682066286448e+001, -5.588715705735085e+001, 6.983971677026145e+001, -5.586747992306563e+001, 2.793713766978453e+001, -7.984595381542750e+000, 9.985923962366661e-001}
                },
                new double[][]{
                        new double[]{3.159975882961421e-003, -2.525989995316554e-002, 8.835992876682405e-002, -1.766602295678194e-001, 2.208004497424176e-001, -1.766602295678194e-001, 8.835992876682405e-002, -2.525989995316553e-002, 3.159975882961421e-003},
                        new double[]{1.000000000000000e+000, -7.992271099617168e+000, 2.795215691738355e+001, -5.587521223997506e+001, 6.982321057779913e+001, -5.585436971614900e+001, 2.793130742781282e+001, -7.983330625451835e+000, 9.985087582034402e-001}
                },
                new double[][]{
                        new double[]{3.159841685994492e-003, -2.525638983434093e-002, 8.834156412890981e-002, -1.766162149140839e-001, 2.207423978670707e-001, -1.766162149140839e-001, 8.834156412890981e-002, -2.525638983434093e-002, 3.159841685994491e-003},
                        new double[]{1.000000000000000e+000, -7.991418808545359e+000, 2.794695967663140e+001, -5.586191799869053e+001, 6.980487161117404e+001, -5.583984161248014e+001, 2.792487500878889e+001, -7.981948031305878e+000, 9.984201544369321e-001}
                },
                new double[][]{
                        new double[]{3.159699893956621e-003, -2.525252084441813e-002, 8.832120131851101e-002, -1.765673158808659e-001, 2.206778710256798e-001, -1.765673158808659e-001, 8.832120131851101e-002, -2.525252084441813e-002, 3.159699893956621e-003},
                        new double[]{1.000000000000000e+000, -7.990467778347355e+000, 2.794116597527122e+001, -5.584711629266529e+001, 6.978448735548403e+001, -5.582373364819085e+001, 2.791777348497057e+001, -7.980435387101997e+000, 9.983262905945257e-001}
                },
                new double[][]{
                        new double[]{3.159550102272552e-003, -2.524825329737595e-002, 8.829861112984655e-002, -1.765129663425945e-001, 2.206061168157774e-001, -1.765129663425945e-001, 8.829861112984654e-002, -2.524825329737595e-002, 3.159550102272551e-003},
                        new double[]{1.000000000000000e+000, -7.989406259963432e+000, 2.793470531018452e+001, -5.583063027319029e+001, 6.976182015671301e+001, -5.580586484538701e+001, 2.790992817728934e+001, -7.978779120568047e+000, 9.982268549454829e-001}
                },
                new double[][]{
                        new double[]{3.159391888060492e-003, -2.524354290957952e-002, 8.827353735018950e-002, -1.764525332301130e-001, 2.205262938030033e-001, -1.764525332301129e-001, 8.827353735018949e-002, -2.524354290957952e-002, 3.159391888060493e-003},
                        new double[]{1.000000000000000e+000, -7.988221087768674e+000, 2.792749873579812e+001, -5.581226204234034e+001, 6.973660423984350e+001, -5.578603296354181e+001, 2.790125574202765e+001, -7.976964141331972e+000, 9.981215173509664e-001}
                },
                new double[][]{
                        new double[]{3.159224809745750e-003, -2.523834025436573e-002, 8.824569355028043e-002, -1.763853085942930e-001, 2.204374609774531e-001, -1.763853085942930e-001, 8.824569355028043e-002, -2.523834025436573e-002, 3.159224809745750e-003},
                        new double[]{1.000000000000000e+000, -7.986897507880478e+000, 2.791945785177781e+001, -5.579179014827696e+001, 6.970854237861690e+001, -5.576401198801909e+001, 2.789166315009525e+001, -7.974973664439123e+000, 9.980099281851373e-001}
                },
                new double[][]{
                        new double[]{3.159048406736869e-003, -2.523259015134536e-002, 8.821475949609868e-002, -1.763105007433883e-001, 2.203385659840945e-001, -1.763105007433883e-001, 8.821475949609867e-002, -2.523259015134535e-002, 3.159048406736868e-003},
                        new double[]{1.000000000000000e+000, -7.985418985660877e+000, 2.791048367061966e+001, -5.576896678748984e+001, 6.967730217807400e+001, -5.573954932622059e+001, 2.788104654658634e+001, -7.972789013008361e+000, 9.978917171939963e-001}
                },
                new double[][]{
                        new double[]{3.158862199178640e-003, -2.522623098262760e-002, 8.818037713864389e-002, -1.762272243510122e-001, 2.202284319921079e-001, -1.762272243510122e-001, 8.818037713864388e-002, -2.522623098262760e-002, 3.158862199178640e-003},
                        new double[]{1.000000000000000e+000, -7.983766989907910e+000, 2.790046535131966e+001, -5.574351468126776e+001, 6.964251192718245e+001, -5.571236267888894e+001, 2.786928997698394e+001, -7.970389397560299e+000, 9.977664922885320e-001}
                },
                new double[][]{
                        new double[]{3.158665687797979e-003, -2.521919392725215e-002, 8.814214613378270e-002, -1.761344894215090e-001, 2.201057430551121e-001, -1.761344894215089e-001, 8.814214613378270e-002, -2.521919392725215e-002, 3.158665687797979e-003},
                        new double[]{1.000000000000000e+000, -7.981920750933047e+000, 2.788927878383032e+001, -5.571512359047303e+001, 6.960375597479234e+001, -5.568213655092985e+001, 2.785626396494602e+001, -7.967751669263686e+000, 9.976338382685047e-001}
                },
                new double[][]{
                        new double[]{3.158458353861474e-003, -2.521140210409663e-002, 8.809961883926078e-002, -1.760311889888082e-001, 2.199690278007572e-001, -1.760311889888082e-001, 8.809961883926079e-002, -2.521140210409664e-002, 3.158458353861475e-003},
                        new double[]{1.000000000000000e+000, -7.979856989390820e+000, 2.787678500741772e+001, -5.568344642928296e+001, 6.956056957789367e+001, -5.564851836276790e+001, 2.784182392506172e+001, -7.964850044027480e+000, 9.974933154730581e-001}
                },
                new double[][]{
                        new double[]{3.158239659265769e-003, -2.520276961238962e-002, 8.805229473062350e-002, -1.759160854136171e-001, 2.198166412741269e-001, -1.759160854136170e-001, 8.805229473062347e-002, -2.520276961238962e-002, 3.158239659265770e-003},
                        new double[]{1.000000000000000e+000, -7.977549612358271e+000, 2.786282844431199e+001, -5.564809493498185e+001, 6.951243316672097e+001, -5.561111411973872e+001, 2.782580839230226e+001, -7.961655794012175e+000, 9.973444583541184e-001}
                },
                new double[][]{
                        new double[]{3.158009046785217e-003, -2.519320045770075e-002, 8.799961417207584e-002, -1.757877951322226e-001, 2.196467447451271e-001, -1.757877951322225e-001, 8.799961417207582e-002, -2.519320045770074e-002, 3.158009046785216e-003},
                        new double[]{1.000000000000000e+000, -7.974969372752160e+000, 2.784723492820081e+001, -5.560863484718004e+001, 6.945876596673548e+001, -5.556948359339760e+001, 2.780803704811647e+001, -7.958136902741812e+000, 9.971867739683715e-001}
                },
                new double[][]{
                        new double[]{3.157765940504518e-003, -2.518258734987953e-002, 8.794095147224382e-002, -1.756447716983805e-001, 2.194572832757883e-001, -1.756447716983806e-001, 8.794095147224382e-002, -2.518258734987952e-002, 3.157765940504517e-003},
                        new double[]{1.000000000000000e+000, -7.972083487716799e+000, 2.782980950515790e+001, -5.556458054606449e+001, 6.939891891301646e+001, -5.552313496494917e+001, 2.778830852124351e+001, -7.954257679564011e+000, 9.970197403833869e-001}
                },
                new double[][]{
                        new double[]{3.157509746467995e-003, -2.517081035786543e-002, 8.787560714843677e-002, -1.754852869480970e-001, 2.192459608296765e-001, -1.754852869480970e-001, 8.787560714843674e-002, -2.517081035786543e-002, 3.157509746467995e-003},
                        new double[]{1.000000000000000e+000, -7.968855211110624e+000, 2.781033398253884e+001, -5.551538909553598e+001, 6.933216678824905e+001, -5.547151887736864e+001, 2.776639793935085e+001, -7.949978328725671e+000, 9.968428049932396e-001}
                },
                new double[][]{
                        new double[]{3.157239853581603e-003, -2.515773540457894e-002, 8.780279931642641e-002, -1.753074101059224e-001, 2.190102126929847e-001, -1.753074101059224e-001, 8.780279931642640e-002, -2.515773540457894e-002, 3.157239853581603e-003},
                        new double[]{1.000000000000000e+000, -7.965243354660791e+000, 2.778856419923966e+001, -5.546045363348811e+001, 6.925769951150495e+001, -5.541402183931861e+001, 2.774205420558135e+001, -7.945254467803112e+000, 9.966553827387890e-001}
                },
                new double[][]{
                        new double[]{3.156955634808492e-003, -2.514321258320968e-002, 8.772165411605427e-002, -1.751089846412184e-001, 2.187471749661684e-001, -1.751089846412184e-001, 8.772165411605427e-002, -2.514321258320967e-002, 3.156955634808492e-003},
                        new double[]{1.000000000000000e+000, -7.961201751737143e+000, 2.776422698853192e+001, -5.539909604819387e+001, 6.917461250162503e+001, -5.534995892083294e+001, 2.771499697204265e+001, -7.940036589642881e+000, 9.964568542275056e-001}
                },
                new double[][]{
                        new double[]{3.156656448704776e-003, -2.512707427413613e-002, 8.763119507633681e-002, -1.748876026744579e-001, 2.184536508773114e-001, -1.748876026744579e-001, 8.763119507633681e-002, -2.512707427413613e-002, 3.156656448704775e-003},
                        new double[]{1.000000000000000e+000, -7.956678657015536e+000, 2.773701680251237e+001, -5.533055887701768e+001, 6.908189603657752e+001, -5.527856567817344e+001, 2.768491328025209e+001, -7.934269461330747e+000, 9.962465637475458e-001}
                },
                new double[][]{
                        new double[]{3.156341641348188e-003, -2.510913303942006e-002, 8.753033131735499e-002, -1.746405767278798e-001, 2.181260736651187e-001, -1.746405767278799e-001, 8.753033131735499e-002, -2.510913303942007e-002, 3.156341641348189e-003},
                        new double[]{1.000000000000000e+000, -7.951616074550573e+000, 2.770659196510449e+001, -5.525399636175364e+001, 6.897842352911954e+001, -5.519898924355911e+001, 2.765145383663062e+001, -7.927891453006585e+000, 9.960238171705083e-001}
                },
                new double[][]{
                        new double[]{3.156010548719496e-003, -2.508917926931127e-002, 8.741784448041817e-002, -1.743649086130051e-001, 2.177604657823905e-001, -1.743649086130051e-001, 8.741784448041816e-002, -2.508917926931127e-002, 3.156010548719496e-003},
                        new double[]{1.000000000000000e+000, -7.945949005955032e+000, 2.767257051861824e+001, -5.516846459419217e+001, 6.886293864000967e+001, -5.511027851501318e+001, 2.761422888941914e+001, -7.920833788580041e+000, 9.957878797370805e-001}
                },
                new double[][]{
                        new double[]{3.155662499604399e-003, -2.506697855245887e-002, 8.729237427317103e-002, -1.740572552511763e-001, 2.173523941823380e-001, -1.740572552511763e-001, 8.729237427317105e-002, -2.506697855245887e-002, 3.155662499604400e-003},
                        new double[]{1.000000000000000e+000, -7.939604609483376e+000, 2.763452562724007e+001, -5.507291068653348e+001, 6.873404115363863e+001, -5.501137338286942e+001, 2.757280367200363e+001, -7.913019709572767e+000, 9.955379737195335e-001}
                },
                new double[][]{
                        new double[]{3.155296819092648e-003, -2.504226873854748e-002, 8.715240251302218e-002, -1.737138912344610e-001, 2.168969214731223e-001, -1.737138912344610e-001, 8.715240251302220e-002, -2.504226873854748e-002, 3.155296819092648e-003},
                        new double[]{1.000000000000000e+000, -7.932501259836564e+000, 2.759198049965839e+001, -5.496616090463505e+001, 6.859017154819686e+001, -5.490109293316875e+001, 2.752669337675260e+001, -7.904363542416965e+000, 9.952732759547681e-001}
                },
                new double[][]{
                        new double[]{3.154912832761072e-003, -2.501475665885260e-002, 8.699623555120342e-002, -1.733306679554957e-001, 2.163885527642557e-001, -1.733306679554957e-001, 8.699623555120342e-002, -2.501475665885260e-002, 3.154912832761072e-003},
                        new double[]{1.000000000000000e+000, -7.924547497442289e+000, 2.754440279254992e+001, -5.484690770861077e+001, 6.842959420455813e+001, -5.477812257507029e+001, 2.747535762332123e+001, -7.894769658576227e+000, 9.949929152413864e-001}
                },
                new double[][]{
                        new double[]{3.154509871638553e-003, -2.498411446674099e-002, 8.682198496185380e-002, -1.729029691694235e-001, 2.158211780866830e-001, -1.729029691694236e-001, 8.682198496185381e-002, -2.498411446674100e-002, 3.154509871638554e-003},
                        new double[]{1.000000000000000e+000, -7.915640854818626e+000, 2.749119845715203e+001, -5.471369565601653e+001, 6.825037921640217e+001, -5.464100005052808e+001, 2.741819438625329e+001, -7.884131315825985e+000, 9.946959695939834e-001}
                },
                new double[][]{
                        new double[]{3.154087278063668e-003, -2.494997555643428e-002, 8.662754638693286e-002, -1.724256629030175e-001, 2.151880103522501e-001, -1.724256629030176e-001, 8.662754638693286e-002, -2.494997555643428e-002, 3.154087278063668e-003},
                        new double[]{1.000000000000000e+000, -7.905666546401251e+000, 2.743170499299610e+001, -5.456490613904224e+001, 6.805038279056032e+001, -5.448810030112487e+001, 2.735453334899452e+001, -7.872329367941698e+000, 9.943814633476313e-001}
                },
                new double[][]{
                        new double[]{3.153644412559933e-003, -2.491193001442541e-002, 8.641057644002366e-002, -1.718930497004701e-001, 2.144815189350482e-001, -1.718930497004701e-001, 8.641057644002366e-002, -2.491193001442541e-002, 3.153644412559932e-003},
                        new double[]{1.000000000000000e+000, -7.894496006909852e+000, 2.736518407655722e+001, -5.439874095038135e+001, 6.782722626340617e+001, -5.431761919070840e+001, 2.728362865560162e+001, -7.859230828901897e+000, 9.940483641052797e-001}
                },
                new double[][]{
                        new double[]{3.153180661869607e-003, -2.486951955381822e-002, 8.616846759211466e-002, -1.712988072983833e-001, 2.136933591155672e-001, -1.712988072983833e-001, 8.616846759211465e-002, -2.486951955381822e-002, 3.153180661869607e-003},
                        new double[]{1.000000000000000e+000, -7.881985261957821e+000, 2.729081353869786e+001, -5.421320470473966e+001, 6.757827380910089e+001, -5.412755611530962e+001, 2.720465103810294e+001, -7.844687276533637e+000, 9.936955795205367e-001}
                },
                new double[][]{
                        new double[]{3.152695448305012e-003, -2.482223187759939e-002, 8.589832099269115e-002, -1.706359319618829e-001, 2.128142978393528e-001, -1.706359319618829e-001, 8.589832099269117e-002, -2.482223187759939e-002, 3.152695448305013e-003},
                        new double[]{1.000000000000000e+000, -7.867973113185387e+000, 2.720767867411994e+001, -5.400608618669720e+001, 6.730060898200142e+001, -5.391569557615249e+001, 2.711667930741871e+001, -7.828533079330270e+000, 9.933219539081104e-001}
                },
                new double[][]{
                        new double[]{3.152188240596396e-003, -2.476949441252039e-002, 8.559691722298893e-002, -1.698966768987765e-001, 2.118341365177477e-001, -1.698966768987765e-001, 8.559691722298890e-002, -2.476949441252038e-002, 3.152188240596395e-003},
                        new double[]{1.000000000000000e+000, -7.852279118743454e+000, 2.711476287956804e+001, -5.377493875379191e+001, 6.699101032259173e+001, -5.367958785031539e+001, 2.701869120994906e+001, -7.810583428982896e+000, 9.929262646739174e-001}
                },
                new double[][]{
                        new double[]{3.151658566438113e-003, -2.471066735097116e-002, 8.526068503891726e-002, -1.690724884104834e-001, 2.107416319545301e-001, -1.690724884104833e-001, 8.526068503891726e-002, -2.471066735097116e-002, 3.151658566438114e-003},
                        new double[]{1.000000000000000e+000, -7.834701348506457e+000, 2.701093763646996e+001, -5.351705999979735e+001, 6.664592636870657e+001, -5.341652897030031e+001, 2.690955367161049e+001, -7.790632160028131e+000, 9.925072185567274e-001}
                },
                new double[][]{
                        new double[]{3.151106026960063e-003, -2.464503593410573e-002, 8.488566824372117e-002, -1.681539407503443e-001, 2.095244169366758e-001, -1.681539407503443e-001, 8.488566824372115e-002, -2.464503593410573e-002, 3.151106026960062e-003},
                        new double[]{1.000000000000000e+000, -7.815013891993500e+000, 2.689495187958396e+001, -5.322947098143593e+001, 6.626145055721739e+001, -5.312354032258413e+001, 2.678801247775242e+001, -7.768449336977040e+000, 9.920634476729724e-001}
                },
                new double[][]{
                        new double[]{3.150530313380003e-003, -2.457180190573428e-002, 8.446749094086682e-002, -1.671306710575339e-001, 2.081689226006989e-001, -1.671306710575339e-001, 8.446749094086682e-002, -2.457180190573428e-002, 3.150530313380004e-003},
                        new double[]{1.000000000000000e+000, -7.792964095691631e+000, 2.676542082793782e+001, -5.290889543716165e+001, 6.583329668224586e+001, -5.279734830107660e+001, 2.665268147288917e+001, -7.743778588425860e+000, 9.915935053562469e-001}
                },
                new double[][]{
                        new double[]{3.149931226123974e-003, -2.449007406337997e-002, 8.400132156317211e-002, -1.659913162360803e-001, 2.066603054012899e-001, -1.659913162360803e-001, 8.400132156317211e-002, -2.449007406337997e-002, 3.149931226123974e-003},
                        new double[]{1.000000000000000e+000, -7.768269505387938e+000, 2.662081440022254e+001, -5.255173958488378e+001, 6.535677580170375e+001, -5.243436460993682e+001, 2.650203141082893e+001, -7.716334167049936e+000, 9.910958617828615e-001}
                },
                new double[][]{
                        new double[]{3.149308696737955e-003, -2.439885783076391e-002, 8.348183626380033e-002, -1.647234542738494e-001, 2.049823823919444e-001, -1.647234542738494e-001, 8.348183626380033e-002, -2.439885783076391e-002, 3.149308696737955e-003},
                        new double[]{1.000000000000000e+000, -7.740614488341203e+000, 2.645944539669780e+001, -5.215407328305949e+001, 6.482677576262216e+001, -5.203066800777763e+001, 2.633437864637221e+001, -7.685797714171228e+000, 9.905688993747558e-001}
                },
                new double[][]{
                        new double[]{3.148662812953755e-003, -2.429704377527410e-002, 8.290318249918893e-002, -1.633135532680249e-001, 2.031175796050522e-001, -1.633135532680249e-001, 8.290318249918893e-002, -2.429704377527410e-002, 3.148662812953756e-003},
                        new double[]{1.000000000000000e+000, -7.709646509807524e+000, 2.627945770704299e+001, -5.171161358357100e+001, 6.423774485589274e+001, -5.158198852888927e+001, 2.614787393767223e+001, -7.651814707922908e+000, 9.900109079710157e-001}
                },
                new double[][]{
                        new double[]{3.147993847316972e-003, -2.418339499528223e-002, 8.225894394631153e-002, -1.617469323651304e-001, 2.010468996187548e-001, -1.617469323651304e-001, 8.225894394631153e-002, -2.418339499528222e-002, 3.147993847316972e-003},
                        new double[]{1.000000000000000e+000, -7.674972038770115e+000, 2.607881490262433e+001, -5.121971200272804e+001, 6.358368152129840e+001, -5.108369551406376e+001, 2.594049172766094e+001, -7.613990575114853e+000, 9.894200797592629e-001}
                },
                new double[][]{
                        new double[]{3.147302289834950e-003, -2.405653330624902e-002, 8.154210829236015e-002, -1.600077399582097e-001, 1.987499159446394e-001, -1.600077399582097e-001, 8.154210829236014e-002, -2.405653330624902e-002, 3.147302289834948e-003},
                        new double[]{1.000000000000000e+000, -7.636152058960868e+000, 2.585528969732743e+001, -5.067334719604580e+001, 6.285813251191995e+001, -5.053079114110794e+001, 2.571002039846987e+001, -7.571886448988433e+000, 9.887945039582496e-001}
                },
                new double[][]{
                        new double[]{3.146588885158769e-003, -2.391492416236799e-002, 8.074503993219968e-002, -1.580789558318979e-001, 1.962048036828320e-001, -1.580789558318979e-001, 8.074503993219971e-002, -2.391492416236799e-002, 3.146588885158770e-003},
                        new double[]{1.000000000000000e+000, -7.592697163719579e+000, 2.560645491929913e+001, -5.006712514976279e+001, 6.205420249911512e+001, -4.991791156951130e+001, 2.545405415016516e+001, -7.525014558465190e+000, 9.881321612431455e-001}
                },
                new double[][]{
                        new double[]{3.145854674876141e-003, -2.375686026326679e-002, 7.985946022891161e-002, -1.559424255217798e-001, 1.933884179764724e-001, -1.559424255217798e-001, 7.985946022891161e-002, -2.375686026326679e-002, 3.145854674876141e-003},
                        new double[]{1.000000000000000e+000, -7.544062217323155e+000, 2.532967683347243e+001, -4.939528950196758e+001, 6.116457875756176e+001, -4.923933830918797e+001, 2.516998735089173e+001, -7.472833239653871e+000, 9.874309179052381e-001}
                },
                new double[][]{
                        new double[]{3.145101045562206e-003, -2.358044381456427e-002, 7.887643875940420e-002, -1.535789369592204e-001, 1.902764341435763e-001, -1.535789369592204e-001, 7.887643875940420e-002, -2.358044381456427e-002, 3.145101045562206e-003},
                        new double[]{1.000000000000000e+000, -7.489640571648333e+000, 2.502211189939137e+001, -4.865174517941539e+001, 6.018157531047837e+001, -4.848902298999204e+001, 2.485501244705817e+001, -7.414741567800956e+000, 9.866885197380821e-001}
                },
                new double[][]{
                        new double[]{3.144329783313850e-003, -2.338356743882253e-002, 7.778639990704111e-002, -1.509683514875423e-001, 1.868435659282269e-001, -1.509683514875423e-001, 7.778639990704111e-002, -2.338356743882253e-002, 3.144329783313850e-003},
                        new double[]{1.000000000000000e+000, -7.428757836057749e+000, 2.468070834925526e+001, -4.783009917679500e+001, 5.909720172464564e+001, -4.766062934041341e+001, 2.450612281760468e+001, -7.350073618197181e+000, 9.859025856426020e-001}
                },
                new double[][]{
                        new double[]{3.143543135581006e-003, -2.316389377207868e-002, 7.657915030499854e-002, -1.480898035077080e-001, 1.830638810073308e-001, -1.480898035077080e-001, 7.657915030499855e-002, -2.316389377207868e-002, 3.143543135581006e-003},
                        new double[]{1.000000000000000e+000, -7.360665211036957e+000, 2.430221433655941e+001, -4.692372299709454e+001, 5.790326259567788e+001, -4.674759686361156e+001, 2.412012231428596e+001, -7.278092378587070e+000, 9.850706009441995e-001}
                },
                new double[][]{
                        new double[]{3.142743881206541e-003, -2.291883383372955e-002, 7.524393400650334e-002, -1.449219852393075e-001, 1.789112355627868e-001, -1.449219852393075e-001, 7.524393400650334e-002, -2.291883383372955e-002, 3.142743881206540e-003},
                        new double[]{1.000000000000000e+000, -7.284532413376133e+000, 2.388319484571587e+001, -4.592584198278260e+001, 5.659149460855977e+001, -4.574323139402281e+001, 2.369364365886558e+001, -7.197983354359358e+000, 9.841899104156673e-001}
                },
                new double[][]{
                        new double[]{3.141935409696230e-003, -2.264552432784142e-002, 7.376952388809109e-002, -1.414435351904935e-001, 1.743598521445707e-001, -1.414435351904935e-001, 7.376952388809108e-002, -2.264552432784141e-002, 3.141935409696229e-003},
                        new double[]{1.000000000000000e+000, -7.199440243846447e+000, 2.342006007650916e+001, -4.482965744266117e+001, 5.515374882202251e+001, -4.464082837240964e+001, 2.322317837594224e+001, -7.108847932436513e+000, 9.832577110005875e-001}
                },
                new double[][]{
                        new double[]{3.141121810862988e-003, -2.234080412659672e-002, 7.214435970035693e-002, -1.376335506400123e-001, 1.693850668579320e-001, -1.376335506400123e-001, 7.214435970035690e-002, -2.234080412659671e-002, 3.141121810862987e-003},
                        new double[]{1.000000000000000e+000, -7.104372878921088e+000, 2.290910863125306e+001, -4.362850803060598e+001, 5.358222639763521e+001, -4.343383520301292e+001, 2.270512153246824e+001, -7.009696601809882e+000, 9.822710442329992e-001}
                },
                new double[][]{
                        new double[]{3.140307976126783e-003, -2.200119030724153e-002, 7.035674537599862e-002, -1.334722453248443e-001, 1.639642726170729e-001, -1.334722453248443e-001, 7.035674537599862e-002, -2.200119030724154e-002, 3.140307976126782e-003},
                        new double[]{1.000000000000000e+000, -6.998210008037751e+000, 2.234658954035024e+001, -4.231607712696974e+001, 5.186977622103262e+001, -4.211605932017192e+001, 2.213583523342565e+001, -6.899442169873026e+000, 9.812267883503994e-001}
                },
                new double[][]{
                        new double[]{3.139499712905021e-003, -2.162285426944518e-002, 6.839512067458584e-002, -1.289417729867781e-001, 1.580780841620924e-001, -1.289417729867781e-001, 6.839512067458588e-002, -2.162285426944519e-002, 3.139499712905022e-003},
                        new double[]{1.000000000000000e+000, -6.879718989504162e+000, 2.172878796019500e+001, -4.088665281760313e+001, 5.001026253686516e+001, -4.068192840019685e+001, 2.151173558210775e+001, -6.776893166237469e+000, 9.801216500987595e-001}
                },
                new double[][]{
                        new double[]{3.138703873699729e-003, -2.120159865872217e-002, 6.624842494020339e-002, -1.240272346018628e-001, 1.517117468996463e-001, -1.240272346018628e-001, 6.624842494020337e-002, -2.120159865872217e-002, 3.138703873699729e-003},
                        new double[]{1.000000000000000e+000, -6.747547264196248e+000, 2.105214025163815e+001, -3.933544618694712e+001, 4.799900957772635e+001, -3.912680823655984e+001, 2.082940863624978e+001, -6.640747692124122e+000, 9.789521562300232e-001}
                },
                new double[][]{
                        new double[]{3.137928501679498e-003, -2.073283607339804e-002, 6.390657357142512e-002, -1.187178808303045e-001, 1.448568043951183e-001, -1.187178808303045e-001, 6.390657357142512e-002, -2.073283607339804e-002, 3.137928501679498e-003},
                        new double[]{1.000000000000000e+000, -6.600215350011449e+000, 2.031338506624802e+001, -3.765897166957116e+001, 4.583332789420625e+001, -3.744738178488232e+001, 2.008576174391972e+001, -6.489588056709191e+000, 9.777146446947530e-001}
                },
                new double[][]{
                        new double[]{3.137182994768761e-003, -2.021157084921369e-002, 6.136107057341432e-002, -1.130085099447135e-001, 1.375130273952083e-001, -1.130085099447135e-001, 6.136107057341432e-002, -2.021157084921369e-002, 3.137182994768761e-003},
                        new double[]{1.000000000000000e+000, -6.436110845532803e+000, 1.950975797479131e+001, -3.585548965015296e+001, 4.351312333645753e+001, -3.564208929582135e+001, 1.927821746633142e+001, -6.321876645437641e+000, 9.764052555351180e-001}
                },
                new double[][]{
                        new double[]{3.136478290496714e-003, -1.963238561066010e-002, 5.860578298576107e-002, -1.069010432752794e-001, 1.296905894905115e-001, -1.069010432752794e-001, 5.860578298576107e-002, -1.963238561066010e-002, 3.136478290496713e-003},
                        new double[]{1.000000000000000e+000, -6.253484003079926e+000, 1.863923797465455e+001, -3.392550574495159e+001, 4.104158405102900e+001, -3.371162367818436e+001, 1.840495799303932e+001, -6.135953593248777e+000, 9.750199214863040e-001}
                },
                new double[][]{
                        new double[]{3.135827074126856e-003, -1.898943476709113e-002, 5.563790460357721e-002, -1.004062326978215e-001, 1.214124500942118e-001, -1.004062326978215e-001, 5.563790460357720e-002, -1.898943476709113e-002, 3.135827074126855e-003},
                        new double[]{1.000000000000000e+000, -6.050445594454034e+000, 1.770085476591669e+001, -3.187231248663630e+001, 3.842593317714272e+001, -3.165946653481059e+001, 1.746522838225852e+001, -5.930036992274631e+000, 9.735543582978614e-001}
                },
                new double[][]{
                        new double[]{3.135244012888753e-003, -1.827644773199040e-002, 5.245913656145098e-002, -9.354541521470149e-002, 1.127168741389934e-001, -9.354541521470149e-002, 5.245913656145097e-002, -1.827644773199040e-002, 3.135244012888753e-003},
                        new double[]{1.000000000000000e+000, -5.824967992989347e+000, 1.669506577467719e+001, -2.970254661371650e+001, 3.567822510278449e+001, -2.949243785701340e+001, 1.645970690065039e+001, -5.702226553585581e+000, 9.720040547903410e-001}
                },
                new double[][]{
                        new double[]{3.134746019470630e-003, -1.748674537436416e-002, 4.907711006745098e-002, -8.635217549983100e-002, 1.036599808864124e-001, -8.635217549983101e-002, 4.907711006745098e-002, -1.748674537436416e-002, 3.134746019470632e-003},
                        new double[]{1.000000000000000e+000, -5.574890639196458e+000, 1.562421121838405e+001, -2.742671795429572e+001, 3.281615152807541e+001, -2.722121529947888e+001, 1.539094988747847e+001, -5.450511870800915e+000, 9.703642626670224e-001}
                },
                new double[][]{
                        new double[]{3.134352548309350e-003, -1.661327409169017e-002, 4.550707051926735e-002, -7.887370609052574e-002, 9.431817474222426e-002, -7.887370609052574e-002, 4.550707051926735e-002, -1.661327409169017e-002, 3.134352548309349e-003},
                        new double[]{1.000000000000000e+000, -5.297931350649583e+000, 1.449305362107864e+001, -2.505964324185187e+001, 2.986381121167740e+001, -2.486075657853854e+001, 1.426391650604211e+001, -5.172786704431734e+000, 9.686299861056348e-001}
                },
                new double[][]{
                        new double[]{3.134085928638540e-003, -1.564866294308467e-002, 4.177383060073082e-002, -7.117156573103166e-002, 8.479027540628278e-002, -7.117156573103163e-002, 4.177383060073081e-002, -1.564866294308467e-002, 3.134085928638538e-003},
                        new double[]{1.000000000000000e+000, -4.991705285766048e+000, 1.330940451460967e+001, -2.262068977353927e+001, 2.685238618316034e+001, -2.243053056001658e+001, 1.308657490580105e+001, -4.866871023409204e+000, 9.667959711606842e-001}
                },
                new double[][]{
                        new double[]{3.133971738732764e-003, -1.458531051532196e-002, 3.791398038204636e-002, -6.332143101523267e-002, 7.519914376860126e-002, -6.332143101523267e-002, 3.791398038204637e-002, -1.458531051532196e-002, 3.133971738732764e-003},
                        new double[]{1.000000000000000e+000, -4.653753780459771e+000, 1.208483484175470e+001, -2.013370016357715e+001, 2.382066078021547e+001, -1.995442963213826e+001, 1.187058496830032e+001, -4.530542906340166e+000, 9.648566950135764e-001}
                },
                new double[][]{
                        new double[]{3.134039226322169e-003, -1.341550962251499e-002, 3.397831195038829e-002, -5.541132177031712e-002, 6.569260943266918e-002, -5.541132177031712e-002, 3.397831195038829e-002, -1.341550962251499e-002, 3.134039226322169e-003},
                        new double[]{1.000000000000000e+000, -4.281585748495832e+000, 1.083545584338935e+001, -1.762643268940588e+001, 2.081532308871231e+001, -1.746020018575418e+001, 1.063204303178750e+001, -4.161582818466576e+000, 9.628063551150658e-001}
                },
                new double[][]{
                        new double[]{3.134321780757536e-003, -1.213161956026687e-002, 3.003437108339706e-002, -4.753767160414873e-002, 5.644356598658089e-002, -4.753767160414873e-002, 3.003437108339706e-002, -1.213161956026687e-002, 3.134321780757536e-003},
                        new double[]{1.000000000000000e+000, -3.872734868428338e+000, 9.582742796500897e+000, -1.512931661567665e+001, 1.789100765938704e+001, -1.497819422522095e+001, 9.392259689330880e+000, -3.757833234835759e+000, 9.606388582728883e-001}
                },
                new double[][]{
                        new double[]{3.134857463189034e-003, -1.072629742487929e-002, 2.616898486943644e-002, -3.979853915444668e-002, 4.764923335280728e-002, -3.979853915444668e-002, 2.616898486943644e-002, -1.072629742487929e-002, 3.134857463189034e-003},
                        new double[]{1.000000000000000e+000, -3.424836362665025e+000, 8.354353544174995e+000, -1.267329710343472e+001, 1.511008073912572e+001, -1.253922198523033e+001, 8.178521712285502e+000, -3.317277061692939e+000, 9.583478097467506e-001}
                },
                new double[][]{
                        new double[]{3.135689601795952e-003, -9.192801914856020e-003, 2.249052777972339e-002, -3.228325770744059e-002, 3.952980949329304e-002, -3.228325770744058e-002, 2.249052777972340e-002, -9.192801914856018e-003, 3.135689601795953e-003},
                        new double[]{1.000000000000000e+000, -2.935727786560088e+000, 7.184865948619916e+000, -1.028654413040799e+001, 1.254223993346572e+001, -1.017128706803972e+001, 7.024762141100510e+000, -2.838138790755699e+000, 9.559265024233198e-001}
                },
                new double[][]{
                        new double[]{3.136867459982495e-003, -7.525384956076330e-003, 1.913057593393776e-002, -2.505796833460762e-002, 3.232704353501763e-002, -2.505796833460763e-002, 1.913057593393775e-002, -7.525384956076330e-003, 3.136867459982495e-003},
                        new double[]{1.000000000000000e+000, -2.403578849731980e+000, 6.116322021836488e+000, -7.989845724149395e+000, 1.026409819970081e+001, -7.895033186180221e+000, 5.972027625440275e+000, -2.319012762106196e+000, 9.533679061555159e-001}
                },
                new double[][]{
                        new double[]{3.138446986453600e-003, -5.719788266387046e-003, 1.624445853637542e-002, -1.814686749013252e-002, 2.630361280892670e-002, -1.814686749013250e-002, 1.624445853637542e-002, -5.719788266387046e-003, 3.138446986453601e-003},
                        new double[]{1.000000000000000e+000, -1.827055825922349e+000, 5.198420980989070e+000, -5.790627461846015e+000, 8.359032593241123e+000, -5.717854195620141e+000, 5.068588868120163e+000, -1.759023244522346e+000, 9.506646573634053e-001}
                },
                new double[][]{
                        new double[]{3.140491657224234e-003, -3.773863336298222e-003, 1.401004915216966e-002, -1.150974045018174e-002, 2.174445408345166e-002, -1.150974045018174e-002, 1.401004915216966e-002, -3.773863336298222e-003, 3.140491657224234e-003},
                        new double[]{1.000000000000000e+000, -1.205526475294007e+000, 4.488149587584013e+000, -3.675774949486338e+000, 6.917662040320396e+000, -3.626853005615643e+000, 4.369489565598928e+000, -1.158021168308530e+000, 9.478090490084538e-001}
                },
                new double[][]{
                        new double[]{3.143073420920190e-003, -1.688333852644986e-003, 1.262395748797918e-002, -5.017573087974801e-003, 1.896114476128463e-002, -5.017573087974800e-003, 1.262395748797918e-002, -1.688333852644986e-003, 3.143073420920191e-003},
                        new double[]{1.000000000000000e+000, -5.393114608165095e-001, 4.048578996469637e+000, -1.603824537959884e+000, 6.039299478305785e+000, -1.581219217193774e+000, 3.935274955307952e+000, -5.168221229728384e-001, 9.447930210688752e-001}
                },
                new double[][]{
                        new double[]{3.146273760226500e-003, 5.322812464931028e-004, 1.229410658041627e-002, 1.570162041369520e-003, 1.829966777695044e-002, 1.570162041369518e-003, 1.229410658041627e-002, 5.322812464931027e-004, 3.146273760226500e-003},
                        new double[]{1.000000000000000e+000, 1.700122175818912e-001, 3.946500014377160e+000, 5.023368561645096e-001, 5.834479755741241e+000, 4.948389524665608e-001, 3.829590220830910e+000, 1.625105377327541e-001, 9.416081516614648e-001}
                },
                new double[][]{
                        new double[]{3.150184884067147e-003, 2.878568940453699e-003, 1.322755106586022e-002, 8.620552071421581e-003, 2.014990932533000e-002, 8.620552071421581e-003, 1.322755106586022e-002, 2.878568940453698e-003, 3.150184884067147e-003},
                        new double[]{1.000000000000000e+000, 9.192515080342589e-001, 4.248526154251194e+000, 2.760354359206612e+000, 6.428032354219372e+000, 2.716723422282723e+000, 4.115304532346304e+000, 8.763352262803313e-001, 9.382456489747480e-001}
                },
                new double[][]{
                        new double[]{3.154911067098877e-003, 5.334938740144926e-003, 1.561237564818607e-002, 1.662582664159496e-002, 2.495150819282766e-002, 1.662582664159495e-002, 1.561237564818606e-002, 5.334938740144928e-003, 3.154911067098877e-003},
                        new double[]{1.000000000000000e+000, 1.703179499277953e+000, 5.015287582907517e+000, 5.328060497664996e+000, 7.960981372090182e+000, 5.238878049409717e+000, 4.848819300199697e+000, 1.619054784052114e+000, 9.346963441995912e-001}
                },
                new double[][]{
                        new double[]{3.160570155425528e-003, 7.878157153029112e-003, 1.959270862731381e-002, 2.617401829906347e-002, 3.318498160501834e-002, 2.617401829906346e-002, 1.959270862731381e-002, 7.878157153029110e-003, 3.160570155425528e-003},
                        new double[]{1.000000000000000e+000, 2.514061860132557e+000, 6.293401312113454e+000, 8.394231003661758e+000, 1.058774518680232e+001, 8.245442893665601e+000, 6.072290240693297e+000, 2.382697143855915e+000, 9.309506856667303e-001}
                },
                new double[][]{
                        new double[]{3.167295260151064e-003, 1.047572033190279e-002, 2.523643163272042e-002, 3.787881035026627e-002, 4.533007101187137e-002, 3.787881035026630e-002, 2.523643163272043e-002, 1.047572033190280e-002, 3.167295260151064e-003},
                        new double[]{1.000000000000000e+000, 3.341136359577790e+000, 8.105078676150034e+000, 1.215629189647473e+001, 1.446288885419975e+001, 1.192812032225852e+001, 7.803674281773004e+000, 3.156465395399920e+000, 9.269987344260360e-001}
                },
                new double[][]{
                        new double[]{3.175236663568254e-003, 1.308411231056349e-002, 3.249623781842036e-002, 5.225727921334366e-002, 6.176741363408540e-002, 5.225727921334363e-002, 3.249623781842036e-002, 1.308411231056349e-002, 3.175236663568254e-003},
                        new double[]{1.000000000000000e+000, 4.170063916809946e+000, 1.043557693581983e+001, 1.678131188069652e+001, 1.970982407256148e+001, 1.644776743866159e+001, 1.002485619533123e+001, 3.926277990238656e+000, 9.228301615299098e-001}
                },
                new double[][]{
                        new double[]{3.184563966522064e-003, 1.564703528332047e-002, 4.116647822650339e-002, 6.955135546761333e-002, 8.260002997525130e-002, 6.955135546761335e-002, 4.116647822650339e-002, 1.564703528332047e-002, 3.184563966522064e-003},
                        new double[]{1.000000000000000e+000, 4.982383252986072e+000, 1.321928002312588e+001, 2.234885788249898e+001, 2.636396393156670e+001, 2.187848507504307e+001, 1.266867131040729e+001, 4.674332629971482e+000, 9.184342473129029e-001}
                },
                new double[][]{
                        new double[]{3.195468509911748e-003, 1.809375255989474e-002, 5.084090030653590e-002, 8.950422298444746e-002, 1.073856458036988e-001, 8.950422298444746e-002, 5.084090030653590e-002, 1.809375255989474e-002, 3.195468509911749e-003},
                        new double[]{1.000000000000000e+000, 5.755019760051603e+000, 1.632605611212203e+001, 2.877918880332384e+001, 3.428743475883073e+001, 2.813781188877264e+001, 1.560646217523939e+001, 5.378744289396370e+000, 9.137998829916308e-001}
                },
                new double[][]{
                        new double[]{3.208166108549520e-003, 2.033775720013014e-002, 6.087995852729569e-002, 1.111283766985333e-001, 1.348084233730102e-001, 1.111283766985333e-001, 6.087995852729569e-002, 2.033775720013014e-002, 3.208166108549520e-003},
                        new double[]{1.000000000000000e+000, 6.459923452852331e+000, 1.955069789760037e+001, 3.575841878007016e+001, 4.306434589484458e+001, 3.491460431644380e+001, 1.863888825236724e+001, 6.013329244005268e+000, 9.089155749432362e-001}
                },
                new double[][]{
                        new double[]{3.222900141858363e-003, 2.227607222988251e-002, 7.040061526828763e-002, 1.325362521214650e-001, 1.623833078994802e-001, 1.325362521214650e-001, 7.040061526828763e-002, 2.227607222988251e-002, 3.222900141858363e-003},
                        new double[]{1.000000000000000e+000, 7.063941201586088e+000, 2.260962332507322e+001, 4.268327468011347e+001, 5.190611606085587e+001, 4.161681749209132e+001, 2.149395668247146e+001, 6.547632809776029e+000, 9.037694520569337e-001}
                },
                new double[][]{
                        new double[]{3.239945053390167e-003, 2.378960212591439e-002, 7.830568646607240e-002, 1.509391118013942e-001, 1.863663521383706e-001, 1.509391118013942e-001, 7.830568646607242e-002, 2.378960212591439e-002, 3.239945053390167e-003},
                        new double[]{1.000000000000000e+000, 7.529064346833643e+000, 2.515035008465618e+001, 4.865934114163024e+001, 5.962181888765429e+001, 4.737213222877233e+001, 2.383740644607056e+001, 6.947327741252297e+000, 8.983492765915317e-001}
                },
                new double[][]{
                        new double[]{3.259610320154737e-003, 2.474508638385787e-002, 8.337226016441435e-002, 1.629367425557869e-001, 2.020999170550658e-001, 1.629367425557869e-001, 8.337226016441435e-002, 2.474508638385787e-002, 3.259610320154737e-003},
                        new double[]{1.000000000000000e+000, 7.813232181445080e+000, 2.678004704072203e+001, 5.259232996155322e+001, 6.472625974952638e+001, 5.111941179551022e+001, 2.530116502103030e+001, 7.175140285956255e+000, 8.926424590119200e-001}
                }
        };
    }

    private static class MIDI_COEFFICIENTS_SR11025HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.162007862740170e-003, -2.529578557360153e-002, 8.853455619586464e-002, -1.770682804248874e-001, 2.213350038797683e-001, -1.770682804248875e-001, 8.853455619586466e-002, -2.529578557360154e-002, 3.162007862740171e-003},
                        new double[]{1.000000000000000e+000, -7.999737085618857e+000, 2.799824646908436e+001, -5.599499999938700e+001, 6.999210096015798e+001, -5.599253523178885e+001, 2.799578169001924e+001, -7.998680743543029e+000, 9.998239410761565e-001}
                },
                new double[][]{
                        new double[]{3.161991393475375e-003, -2.529561985898286e-002, 8.853389129580302e-002, -1.770668487479723e-001, 2.213331718353534e-001, -1.770668487479723e-001, 8.853389129580305e-002, -2.529561985898286e-002, 3.161991393475375e-003},
                        new double[]{1.000000000000000e+000, -7.999715981238941e+000, 2.799810937767752e+001, -5.599462063928445e+001, 6.999152187818606e+001, -5.599200933008674e+001, 2.799549805484331e+001, -7.998596833140963e+000, 9.998134730441964e-001}
                },
                new double[][]{
                        new double[]{3.161973950263232e-003, -2.529544219423797e-002, 8.853317417454307e-002, -1.770653001542420e-001, 2.213311884473474e-001, -1.770653001542420e-001, 8.853317417454308e-002, -2.529544219423796e-002, 3.161973950263232e-003},
                        new double[]{1.000000000000000e+000, -7.999692951997833e+000, 2.799796011536304e+001, -5.599420867502855e+001, 6.999089496911128e+001, -5.599144211349563e+001, 2.799519353761363e+001, -7.998507264236177e+000, 9.998023826702345e-001}
                },
                new double[][]{
                        new double[]{3.161955475846854e-003, -2.529525161211021e-002, 8.853240017384159e-002, -1.770636237999238e-001, 2.213290395246911e-001, -1.770636237999238e-001, 8.853240017384160e-002, -2.529525161211020e-002, 3.161955475846854e-003},
                        new double[]{1.000000000000000e+000, -7.999667801400651e+000, 2.799779746630722e+001, -5.599376093783255e+001, 6.999021574923438e+001, -5.599082989573267e+001, 2.799486640492275e+001, -7.998411618461006e+000, 9.997906329625295e-001}
                },
                new double[][]{
                        new double[]{3.161935909645136e-003, -2.529504705640347e-002, 8.853156416834446e-002, -1.770618077200683e-001, 2.213267093969643e-001, -1.770618077200683e-001, 8.853156416834446e-002, -2.529504705640347e-002, 3.161935909645137e-003},
                        new double[]{1.000000000000000e+000, -7.999640311222830e+000, 2.799762008212798e+001, -5.599327391990241e+001, 6.998947926751902e+001, -5.599016862081130e+001, 2.799451476010375e+001, -7.998309442545697e+000, 9.997781847314912e-001}
                },
                new double[][]{
                        new double[]{3.161915187564930e-003, -2.529482737284698e-002, 8.853066051454253e-002, -1.770598387035854e-001, 2.213241807486499e-001, -1.770598387035854e-001, 8.853066051454254e-002, -2.529482737284698e-002, 3.161915187564931e-003},
                        new double[]{1.000000000000000e+000, -7.999610238987999e+000, 2.799742646663783e+001, -5.599274373584947e+001, 6.998868005324024e+001, -5.598945382264122e+001, 2.799413652615765e+001, -7.998200245016235e+000, 9.997649964592074e-001}
                },
                new double[][]{
                        new double[]{3.161893241803242e-003, -2.529459129894610e-002, 8.852968299384582e-002, -1.770577021537192e-001, 2.213214344340326e-001, -1.770577021537192e-001, 8.852968299384582e-002, -2.529459129894610e-002, 3.161893241803241e-003},
                        new double[]{1.000000000000000e+000, -7.999577315145299e+000, 2.799721495877710e+001, -5.599216607955446e+001, 6.998781205751611e+001, -5.598868057997400e+001, 2.799372942676505e+001, -7.998083492545739e+000, 9.997510241612407e-001}
                },
                new double[][]{
                        new double[]{3.161870000639062e-003, -2.529433745270150e-002, 8.852862474907945e-002, -1.770553819322278e-001, 2.213184492704216e-001, -1.770553819322278e-001, 8.852862474907944e-002, -2.529433745270150e-002, 3.161870000639062e-003},
                        new double[]{1.000000000000000e+000, -7.999541239909840e+000, 2.799698371351953e+001, -5.599153617593690e+001, 6.998686858799225e+001, -5.598784346614271e+001, 2.799329096515795e+001, -7.997958605920512e+000, 9.997362212402372e-001}
                },
                new double[][]{
                        new double[]{3.161845388214309e-003, -2.529406432006455e-002, 8.852747821361813e-002, -1.770528601853185e-001, 2.213152018071011e-001, -1.770528601853185e-001, 8.852747821361814e-002, -2.529406432006455e-002, 3.161845388214308e-003},
                        new double[]{1.000000000000000e+000, -7.999501679725586e+000, 2.799673068050587e+001, -5.599084872701820e+001, 6.998584223586057e+001, -5.598693649297755e+001, 2.799281840060127e+001, -7.997824955577232e+000, 9.997205383308672e-001}
                },
                new double[][]{
                        new double[]{3.161819324303432e-003, -2.529377024098110e-002, 8.852623503228417e-002, -1.770501171491587e-001, 2.213116660671045e-001, -1.770501171491587e-001, 8.852623503228417e-002, -2.529377024098109e-002, 3.161819324303432e-003},
                        new double[]{1.000000000000000e+000, -7.999458263304954e+000, 2.799645358013133e+001, -5.599009785159184e+001, 6.998472479429390e+001, -5.598595304820432e+001, 2.799230872220294e+001, -7.997681856662638e+000, 9.997039231355781e-001}
                },
                new double[][]{
                        new double[]{3.161791724071281e-003, -2.529345339385895e-002, 8.852488597302886e-002, -1.770471309325213e-001, 2.213078132585601e-001, -1.770471309325213e-001, 8.852488597302888e-002, -2.529345339385895e-002, 3.161791724071281e-003},
                        new double[]{1.000000000000000e+000, -7.999410577193965e+000, 2.799614987677971e+001, -5.598927701773240e+001, 6.998350716726949e+001, -5.598488582555106e+001, 2.799175861973908e+001, -7.997528563561497e+000, 9.996863202506293e-001}
                },
                new double[][]{
                        new double[]{3.161762497818671e-003, -2.529311177827321e-002, 8.852342082829638e-002, -1.770438772738195e-001, 2.213036114519553e-001, -1.770438772738195e-001, 8.852342082829640e-002, -2.529311177827320e-002, 3.161762497818670e-003},
                        new double[]{1.000000000000000e+000, -7.999358160805437e+000, 2.799581674885958e+001, -5.598837896728089e+001, 6.998217926762800e+001, -5.598372674669284e+001, 2.799116445114182e+001, -7.997364263832050e+000, 9.996676709818243e-001}
                },
                new double[][]{
                        new double[]{3.161731550715255e-003, -2.529274319570281e-002, 8.852182830483955e-002, -1.770403292694686e-001, 2.212990252192333e-001, -1.770403292694686e-001, 8.852182830483955e-002, -2.529274319570281e-002, 3.161731550715255e-003},
                        new double[]{1.000000000000000e+000, -7.999300500855798e+000, 2.799545105525624e+001, -5.598739563134019e+001, 6.998072990307688e+001, -5.598246687406163e+001, 2.799052220625634e+001, -7.997188071481178e+000, 9.996479131493456e-001}
                },
                new double[][]{
                        new double[]{3.161698782519206e-003, -2.529234522806594e-002, 8.852009590060686e-002, -1.770364570701295e-001, 2.212940152301387e-001, -1.770364570701295e-001, 8.852009590060689e-002, -2.529234522806595e-002, 3.161698782519207e-003},
                        new double[]{1.000000000000000e+000, -7.999237025133197e+000, 2.799504929776608e+001, -5.598631803569721e+001, 6.997914664869103e+001, -5.598109631343003e+001, 2.798982746642563e+001, -7.996999019503344e+000, 9.996269808810403e-001}
                },
                new double[][]{
                        new double[]{3.161664087283275e-003, -2.529191521379445e-002, 8.851820976715390e-002, -1.770322275409812e-001, 2.212885378006770e-001, -1.770322275409812e-001, 8.851820976715390e-002, -2.529191521379445e-002, 3.161664087283275e-003},
                        new double[]{1.000000000000000e+000, -7.999167095515828e+000, 2.799460757902766e+001, -5.598513619495752e+001, 6.997741570428816e+001, -5.597960410504683e+001, 2.798907535940938e+001, -7.996796051598514e+000, 9.996048043934835e-001}
                },
                new double[][]{
                        new double[]{3.161627353046769e-003, -2.529145022115624e-002, 8.851615455584626e-002, -1.770276038817035e-001, 2.212825443879335e-001, -1.770276038817035e-001, 8.851615455584626e-002, -2.529145022115624e-002, 3.161627353046770e-003},
                        new double[]{1.000000000000000e+000, -7.999090000149478e+000, 2.799412155540460e+001, -5.598383899403104e+001, 6.997552173486156e+001, -5.597797810195495e+001, 2.798826050908336e+001, -7.996578012974147e+000, 9.995813097600974e-001}
                },
                new double[][]{
                        new double[]{3.161588461513018e-003, -2.529094701849926e-002, 8.851391324591054e-002, -1.770225452013237e-001, 2.212759810247989e-001, -1.770225452013237e-001, 8.851391324591053e-002, -2.529094701849925e-002, 3.161588461513017e-003},
                        new double[]{1.000000000000000e+000, -7.999004944682265e+000, 2.799358638420972e+001, -5.598241405544334e+001, 6.997344769203320e+001, -5.597620483395794e+001, 2.798737697930015e+001, -7.996343640125112e+000, 9.995564186655692e-001}
                },
                new double[][]{
                        new double[]{3.161547287711997e-003, -2.529040204105239e-002, 8.851146695215922e-002, -1.770170060425100e-001, 2.212687876873824e-001, -1.770170060425100e-001, 8.851146695215921e-002, -2.529040204105239e-002, 3.161547287711996e-003},
                        new double[]{1.000000000000000e+000, -7.998911042442045e+000, 2.799299666458529e+001, -5.598084759076193e+001, 6.997117461424480e+001, -5.597426935551604e+001, 2.798641821121696e+001, -7.996091549472803e+000, 9.995300481457619e-001}
                },
                new double[][]{
                        new double[]{3.161503699647645e-003, -2.528981135387320e-002, 8.850879470994689e-002, -1.770109358492243e-001, 2.212608975870059e-001, -1.770109358492243e-001, 8.850879470994692e-002, -2.528981135387320e-002, 3.161503699647646e-003},
                        new double[]{1.000000000000000e+000, -7.998807303428078e+000, 2.799234637127157e+001, -5.597912423422096e+001, 6.996868140312893e+001, -5.597215507564681e+001, 2.798537695331360e+001, -7.995820224730498e+000, 9.995021103122610e-001}
                },
                new double[][]{
                        new double[]{3.161457557929669e-003, -2.528917061048478e-002, 8.850587323462855e-002, -1.770042783709314e-001, 2.212522363777158e-001, -1.770042783709314e-001, 8.850587323462855e-002, -2.528917061048478e-002, 3.161457557929670e-003},
                        new double[]{1.000000000000000e+000, -7.998692621972898e+000, 2.799162878040291e+001, -5.597722685639667e+001, 6.996594457319563e+001, -5.596984356767348e+001, 2.798424518323031e+001, -7.995528002846465e+000, 9.994725120606737e-001}
                },
                new double[][]{
                        new double[]{3.161408715389472e-003, -2.528847500668750e-002, 8.850267665245455e-002, -1.769969709957286e-001, 2.212427212691440e-001, -1.769969709957285e-001, 8.850267665245455e-002, -2.528847500668750e-002, 3.161408715389472e-003},
                        new double[]{1.000000000000000e+000, -7.998565762912708e+000, 2.799083638636589e+001, -5.597513635552572e+001, 6.996293797162383e+001, -5.596731435640531e+001, 2.798301402045050e+001, -7.995213058358191e+000, 9.994411547617077e-001}
                },
                new double[][]{
                        new double[]{3.161357016680064e-003, -2.528771922897007e-002, 8.849917619947209e-002, -1.769889440038497e-001, 2.212322600333352e-001, -1.769889440038497e-001, 8.849917619947209e-002, -2.528771922897008e-002, 3.161357016680064e-003},
                        new double[]{1.000000000000000e+000, -7.998425346085041e+000, 2.798996080863779e+001, -5.597283142376993e+001, 6.995963246456225e+001, -5.596454468004423e+001, 2.798167362873678e+001, -7.994873385971648e+000, 9.994079339340317e-001}
                },
                new double[][]{
                        new double[]{3.161302297859827e-003, -2.528689739687535e-002, 8.849533988458978e-002, -1.769801197319709e-001, 2.212207498927932e-001, -1.769801197319709e-001, 8.849533988458978e-002, -2.528689739687534e-002, 3.161302297859828e-003},
                        new double[]{1.000000000000000e+000, -7.998269828950293e+000, 2.798899268739245e+001, -5.597028828540589e+001, 6.995599558591326e+001, -5.596150922378865e+001, 2.798021310709847e+001, -7.994506781157211e+000, 9.993727388978670e-001}
                },
                new double[][]{
                        new double[]{3.161244385960086e-003, -2.528600299859753e-002, 8.849113211249675e-002, -1.769704116375925e-001, 2.212080762754665e-001, -1.769704116375925e-001, 8.849113211249675e-002, -2.528600299859753e-002, 3.161244385960087e-003},
                        new double[]{1.000000000000000e+000, -7.998097487108931e+000, 2.798792156651412e+001, -5.596748040355397e+001, 6.995199114408922e+001, -5.595817982174073e+001, 2.797862036792086e+001, -7.994110818528716e+000, 9.993354524081550e-001}
                },
                new double[][]{
                        new double[]{3.161183098536588e-003, -2.528502881900131e-002, 8.848651326161339e-002, -1.769597232514924e-001, 2.211941114206876e-001, -1.769597232514925e-001, 8.848651326161340e-002, -2.528502881900131e-002, 3.161183098536589e-003},
                        new double[]{1.000000000000000e+000, -7.997906392458485e+000, 2.798673576249600e+001, -5.596437815165698e+001, 6.994757878169354e+001, -5.595452512332000e+001, 2.797688200072444e+001, -7.993682827744629e+000, 9.992959502661386e-001}
                },
                new double[][]{
                        new double[]{3.161118243205082e-003, -2.528396685915505e-002, 8.848143921166912e-002, -1.769479470048061e-001, 2.211787128181739e-001, -1.769479470048060e-001, 8.848143921166909e-002, -2.528396685915505e-002, 3.161118243205083e-003},
                        new double[]{1.000000000000000e+000, -7.997694388703194e+000, 2.798542221751621e+001, -5.596094844546255e+001, 6.994271348247383e+001, -5.595051021993152e+001, 2.797498311983799e+001, -7.993219866638839e+000, 9.992541009080764e-001}
                },
                new double[][]{
                        new double[]{3.161049617161414e-003, -2.528280824636124e-002, 8.847586081485707e-002, -1.769349629156903e-001, 2.211617214600661e-001, -1.769349629156903e-001, 8.847586081485709e-002, -2.528280824636124e-002, 3.161049617161414e-003},
                        new double[]{1.000000000000000e+000, -7.997459063894291e+000, 2.798396633477872e+001, -5.595715433075780e+001, 6.993734501922353e+001, -5.594609622713119e+001, 2.797290719406510e+001, -7.992718691253814e+000, 9.992097649697828e-001}
                },
                new double[][]{
                        new double[]{3.160977006686676e-003, -2.528154313354447e-002, 8.846972330378967e-002, -1.769206371187328e-001, 2.211429598836020e-001, -1.769206371187328e-001, 8.846972330378969e-002, -2.528154313354447e-002, 3.160977006686676e-003},
                        new double[]{1.000000000000000e+000, -7.997197719639722e+000, 2.798235179397666e+001, -5.595295452153719e+001, 6.993141733555554e+001, -5.594123981696578e+001, 2.797063585619458e+001, -7.992175722409599e+000, 9.991627948255754e-001}
                },
                new double[][]{
                        new double[]{3.160900186638132e-003, -2.528016058671942e-002, 8.846296562866780e-002, -1.769048202182677e-001, 2.211222299793627e-001, -1.769048202182677e-001, 8.846296562866780e-002, -2.528016058671942e-002, 3.160900186638132e-003},
                        new double[]{1.000000000000000e+000, -7.996907336578110e+000, 2.798056034447828e+001, -5.594830288265121e+001, 6.992486785363307e+001, -5.593589269453279e+001, 2.796814868994899e+001, -7.991587008398287e+000, 9.991130341001322e-001}
                },
                new double[][]{
                        new double[]{3.160818919927040e-003, -2.527864845910844e-002, 8.845551971517662e-002, -1.768873454445385e-001, 2.210993105370871e-001, -1.768873454445385e-001, 8.845551971517662e-002, -2.527864845910844e-002, 3.160818919927039e-003},
                        new double[]{1.000000000000000e+000, -7.996584535662630e+000, 2.797857157354876e+001, -5.594314785027997e+001, 6.991762669901105e+001, -5.593000101210501e+001, 2.796542299168110e+001, -7.990948183344891e+000, 9.990603171517305e-001}
                },
                new double[][]{
                        new double[]{3.160732956984562e-003, -2.527699325030442e-002, 8.844730963360412e-002, -1.768680265891523e-001, 2.210739544977368e-001, -1.768680265891523e-001, 8.844730963360412e-002, -2.527699325030443e-002, 3.160732956984563e-003},
                        new double[]{1.000000000000000e+000, -7.996225534745101e+000, 2.797636264659958e+001, -5.593743178278633e+001, 6.990961583269734e+001, -5.592350471337751e+001, 2.796243350380717e+001, -7.990254420720088e+000, 9.990044685251582e-001}
                },
                new double[][]{
                        new double[]{3.160642035217503e-003, -2.527517994868275e-002, 8.843825066855632e-002, -1.768466556935119e-001, 2.210458858768428e-001, -1.768466556935119e-001, 8.843825066855633e-002, -2.527517994868275e-002, 3.160642035217504e-003},
                        new double[]{1.000000000000000e+000, -7.995826099888900e+000, 2.797390801609919e+001, -5.593109023362921e+001, 6.990074807938849e+001, -5.591633679952461e+001, 2.795915211661225e+001, -7.989500381429437e+000, 9.989453023725972e-001}
                },
                new double[][]{
                        new double[]{3.160545878455811e-003, -2.527319185504862e-002, 8.842824827738170e-002, -1.768230004608285e-001, 2.210147963200810e-001, -1.768230004608285e-001, 8.842824827738169e-002, -2.527319185504861e-002, 3.160545878455811e-003},
                        new double[]{1.000000000000000e+000, -7.995381490769717e+000, 2.797117909537814e+001, -5.592405113704149e+001, 6.989092603954595e+001, -5.590842250778280e+001, 2.795554753466443e+001, -7.988680155834519e+000, 9.988826218405983e-001}
                },
                new double[][]{
                        new double[]{3.160444196394361e-003, -2.527101038526481e-002, 8.841719692401759e-002, -1.767968013589008e-001, 2.209803412475103e-001, -1.767968013589008e-001, 8.841719692401759e-002, -2.527101038526481e-002, 3.160444196394361e-003},
                        new double[]{1.000000000000000e+000, -7.994886399445699e+000, 2.796814389311626e+001, -5.591623389609305e+001, 6.988004087155034e+001, -5.589967839219742e+001, 2.795158490363400e+001, -7.987787198984727e+000, 9.988162184212104e-001}
                },
                new double[][]{
                        new double[]{3.160336684031966e-003, -2.526861484933287e-002, 8.840497877340901e-002, -1.767677683770464e-001, 2.209421355378812e-001, -1.767677683770464e-001, 8.840497877340901e-002, -2.526861484933288e-002, 3.160336684031967e-003},
                        new double[]{1.000000000000000e+000, -7.994334881691234e+000, 2.796476660380225e+001, -5.590754836155735e+001, 6.986797092858842e+001, -5.589001129497501e+001, 2.794722539282159e+001, -7.986814258252410e+000, 9.987458712652104e-001}
                },
                new double[][]{
                        new double[]{3.160223021111152e-003, -2.526598220409768e-002, 8.839146222991390e-002, -1.767355773963661e-001, 2.208997486988848e-001, -1.767355773963661e-001, 8.839146222991388e-002, -2.526598220409768e-002, 3.160223021111152e-003},
                        new double[]{1.000000000000000e+000, -7.993720279991338e+000, 2.796100714890299e+001, -5.589789368866482e+001, 6.985458023317315e+001, -5.587931719555724e+001, 2.794242572815215e+001, -7.985753292467819e+000, 9.986713464552553e-001}
                },
                new double[][]{
                        new double[]{3.160102871562913e-003, -2.526308677640546e-002, 8.837650030117852e-002, -1.766998661278661e-001, 2.208526994630720e-001, -1.766998661278661e-001, 8.837650030117855e-002, -2.526308677640547e-002, 3.160102871562914e-003},
                        new double[]{1.000000000000000e+000, -7.993035137184169e+000, 2.795682066286448e+001, -5.588715705735085e+001, 6.983971677026145e+001, -5.586747992306563e+001, 2.793713766978453e+001, -7.984595381542750e+000, 9.985923962366661e-001}
                },
                new double[][]{
                        new double[]{3.159975882961421e-003, -2.525989995316554e-002, 8.835992876682405e-002, -1.766602295678194e-001, 2.208004497424176e-001, -1.766602295678194e-001, 8.835992876682405e-002, -2.525989995316553e-002, 3.159975882961421e-003},
                        new double[]{1.000000000000000e+000, -7.992271099617168e+000, 2.795215691738355e+001, -5.587521223997506e+001, 6.982321057779913e+001, -5.585436971614900e+001, 2.793130742781282e+001, -7.983330625451835e+000, 9.985087582034402e-001}
                },
                new double[][]{
                        new double[]{3.159841685994492e-003, -2.525638983434093e-002, 8.834156412890981e-002, -1.766162149140839e-001, 2.207423978670707e-001, -1.766162149140839e-001, 8.834156412890981e-002, -2.525638983434093e-002, 3.159841685994491e-003},
                        new double[]{1.000000000000000e+000, -7.991418808545359e+000, 2.794695967663140e+001, -5.586191799869053e+001, 6.980487161117404e+001, -5.583984161248014e+001, 2.792487500878889e+001, -7.981948031305878e+000, 9.984201544369321e-001}
                },
                new double[][]{
                        new double[]{3.159699893956621e-003, -2.525252084441813e-002, 8.832120131851101e-002, -1.765673158808659e-001, 2.206778710256798e-001, -1.765673158808659e-001, 8.832120131851101e-002, -2.525252084441813e-002, 3.159699893956621e-003},
                        new double[]{1.000000000000000e+000, -7.990467778347355e+000, 2.794116597527122e+001, -5.584711629266529e+001, 6.978448735548403e+001, -5.582373364819085e+001, 2.791777348497057e+001, -7.980435387101997e+000, 9.983262905945257e-001}
                },
                new double[][]{
                        new double[]{3.159550102272552e-003, -2.524825329737595e-002, 8.829861112984655e-002, -1.765129663425945e-001, 2.206061168157774e-001, -1.765129663425945e-001, 8.829861112984654e-002, -2.524825329737595e-002, 3.159550102272551e-003},
                        new double[]{1.000000000000000e+000, -7.989406259963432e+000, 2.793470531018452e+001, -5.583063027319029e+001, 6.976182015671301e+001, -5.580586484538701e+001, 2.790992817728934e+001, -7.978779120568047e+000, 9.982268549454829e-001}
                },
                new double[][]{
                        new double[]{3.159391888060492e-003, -2.524354290957952e-002, 8.827353735018950e-002, -1.764525332301130e-001, 2.205262938030033e-001, -1.764525332301129e-001, 8.827353735018949e-002, -2.524354290957952e-002, 3.159391888060493e-003},
                        new double[]{1.000000000000000e+000, -7.988221087768674e+000, 2.792749873579812e+001, -5.581226204234034e+001, 6.973660423984350e+001, -5.578603296354181e+001, 2.790125574202765e+001, -7.976964141331972e+000, 9.981215173509664e-001}
                },
                new double[][]{
                        new double[]{3.159224809745750e-003, -2.523834025436573e-002, 8.824569355028043e-002, -1.763853085942930e-001, 2.204374609774531e-001, -1.763853085942930e-001, 8.824569355028043e-002, -2.523834025436573e-002, 3.159224809745750e-003},
                        new double[]{1.000000000000000e+000, -7.986897507880478e+000, 2.791945785177781e+001, -5.579179014827696e+001, 6.970854237861690e+001, -5.576401198801909e+001, 2.789166315009525e+001, -7.974973664439123e+000, 9.980099281851373e-001}
                },
                new double[][]{
                        new double[]{3.159048406736869e-003, -2.523259015134536e-002, 8.821475949609868e-002, -1.763105007433883e-001, 2.203385659840945e-001, -1.763105007433883e-001, 8.821475949609867e-002, -2.523259015134535e-002, 3.159048406736868e-003},
                        new double[]{1.000000000000000e+000, -7.985418985660877e+000, 2.791048367061966e+001, -5.576896678748984e+001, 6.967730217807400e+001, -5.573954932622059e+001, 2.788104654658634e+001, -7.972789013008361e+000, 9.978917171939963e-001}
                },
                new double[][]{
                        new double[]{3.158862199178640e-003, -2.522623098262760e-002, 8.818037713864389e-002, -1.762272243510122e-001, 2.202284319921079e-001, -1.762272243510122e-001, 8.818037713864388e-002, -2.522623098262760e-002, 3.158862199178640e-003},
                        new double[]{1.000000000000000e+000, -7.983766989907910e+000, 2.790046535131966e+001, -5.574351468126776e+001, 6.964251192718245e+001, -5.571236267888894e+001, 2.786928997698394e+001, -7.970389397560299e+000, 9.977664922885320e-001}
                },
                new double[][]{
                        new double[]{3.158665687797979e-003, -2.521919392725215e-002, 8.814214613378270e-002, -1.761344894215090e-001, 2.201057430551121e-001, -1.761344894215089e-001, 8.814214613378270e-002, -2.521919392725215e-002, 3.158665687797979e-003},
                        new double[]{1.000000000000000e+000, -7.981920750933047e+000, 2.788927878383032e+001, -5.571512359047303e+001, 6.960375597479234e+001, -5.568213655092985e+001, 2.785626396494602e+001, -7.967751669263686e+000, 9.976338382685047e-001}
                },
                new double[][]{
                        new double[]{3.158458353861474e-003, -2.521140210409663e-002, 8.809961883926078e-002, -1.760311889888082e-001, 2.199690278007572e-001, -1.760311889888082e-001, 8.809961883926079e-002, -2.521140210409664e-002, 3.158458353861475e-003},
                        new double[]{1.000000000000000e+000, -7.979856989390820e+000, 2.787678500741772e+001, -5.568344642928296e+001, 6.956056957789367e+001, -5.564851836276790e+001, 2.784182392506172e+001, -7.964850044027480e+000, 9.974933154730581e-001}
                },
                new double[][]{
                        new double[]{3.158239659265769e-003, -2.520276961238962e-002, 8.805229473062350e-002, -1.759160854136171e-001, 2.198166412741269e-001, -1.759160854136170e-001, 8.805229473062347e-002, -2.520276961238962e-002, 3.158239659265770e-003},
                        new double[]{1.000000000000000e+000, -7.977549612358271e+000, 2.786282844431199e+001, -5.564809493498185e+001, 6.951243316672097e+001, -5.561111411973872e+001, 2.782580839230226e+001, -7.961655794012175e+000, 9.973444583541184e-001}
                },
                new double[][]{
                        new double[]{3.158009046785217e-003, -2.519320045770075e-002, 8.799961417207584e-002, -1.757877951322226e-001, 2.196467447451271e-001, -1.757877951322225e-001, 8.799961417207582e-002, -2.519320045770074e-002, 3.158009046785216e-003},
                        new double[]{1.000000000000000e+000, -7.974969372752160e+000, 2.784723492820081e+001, -5.560863484718004e+001, 6.945876596673548e+001, -5.556948359339760e+001, 2.780803704811647e+001, -7.958136902741812e+000, 9.971867739683715e-001}
                },
                new double[][]{
                        new double[]{3.157765940504518e-003, -2.518258734987953e-002, 8.794095147224382e-002, -1.756447716983805e-001, 2.194572832757883e-001, -1.756447716983806e-001, 8.794095147224382e-002, -2.518258734987952e-002, 3.157765940504517e-003},
                        new double[]{1.000000000000000e+000, -7.972083487716799e+000, 2.782980950515790e+001, -5.556458054606449e+001, 6.939891891301646e+001, -5.552313496494917e+001, 2.778830852124351e+001, -7.954257679564011e+000, 9.970197403833869e-001}
                },
                new double[][]{
                        new double[]{3.157509746467995e-003, -2.517081035786543e-002, 8.787560714843677e-002, -1.754852869480970e-001, 2.192459608296765e-001, -1.754852869480970e-001, 8.787560714843674e-002, -2.517081035786543e-002, 3.157509746467995e-003},
                        new double[]{1.000000000000000e+000, -7.968855211110624e+000, 2.781033398253884e+001, -5.551538909553598e+001, 6.933216678824905e+001, -5.547151887736864e+001, 2.776639793935085e+001, -7.949978328725671e+000, 9.968428049932396e-001}
                },
                new double[][]{
                        new double[]{3.157239853581603e-003, -2.515773540457894e-002, 8.780279931642641e-002, -1.753074101059224e-001, 2.190102126929847e-001, -1.753074101059224e-001, 8.780279931642640e-002, -2.515773540457894e-002, 3.157239853581603e-003},
                        new double[]{1.000000000000000e+000, -7.965243354660791e+000, 2.778856419923966e+001, -5.546045363348811e+001, 6.925769951150495e+001, -5.541402183931861e+001, 2.774205420558135e+001, -7.945254467803112e+000, 9.966553827387890e-001}
                },
                new double[][]{
                        new double[]{3.156955634808492e-003, -2.514321258320968e-002, 8.772165411605427e-002, -1.751089846412184e-001, 2.187471749661684e-001, -1.751089846412184e-001, 8.772165411605427e-002, -2.514321258320967e-002, 3.156955634808492e-003},
                        new double[]{1.000000000000000e+000, -7.961201751737143e+000, 2.776422698853192e+001, -5.539909604819387e+001, 6.917461250162503e+001, -5.534995892083294e+001, 2.771499697204265e+001, -7.940036589642881e+000, 9.964568542275056e-001}
                },
                new double[][]{
                        new double[]{3.156656448704776e-003, -2.512707427413613e-002, 8.763119507633681e-002, -1.748876026744579e-001, 2.184536508773114e-001, -1.748876026744579e-001, 8.763119507633681e-002, -2.512707427413613e-002, 3.156656448704775e-003},
                        new double[]{1.000000000000000e+000, -7.956678657015536e+000, 2.773701680251237e+001, -5.533055887701768e+001, 6.908189603657752e+001, -5.527856567817344e+001, 2.768491328025209e+001, -7.934269461330747e+000, 9.962465637475458e-001}
                },
                new double[][]{
                        new double[]{3.156341641348188e-003, -2.510913303942006e-002, 8.753033131735499e-002, -1.746405767278798e-001, 2.181260736651187e-001, -1.746405767278799e-001, 8.753033131735499e-002, -2.510913303942007e-002, 3.156341641348189e-003},
                        new double[]{1.000000000000000e+000, -7.951616074550573e+000, 2.770659196510449e+001, -5.525399636175364e+001, 6.897842352911954e+001, -5.519898924355911e+001, 2.765145383663062e+001, -7.927891453006585e+000, 9.960238171705083e-001}
                },
                new double[][]{
                        new double[]{3.156010548719496e-003, -2.508917926931127e-002, 8.741784448041817e-002, -1.743649086130051e-001, 2.177604657823905e-001, -1.743649086130051e-001, 8.741784448041816e-002, -2.508917926931127e-002, 3.156010548719496e-003},
                        new double[]{1.000000000000000e+000, -7.945949005955032e+000, 2.767257051861824e+001, -5.516846459419217e+001, 6.886293864000967e+001, -5.511027851501318e+001, 2.761422888941914e+001, -7.920833788580041e+000, 9.957878797370805e-001}
                },
                new double[][]{
                        new double[]{3.155662499604399e-003, -2.506697855245887e-002, 8.729237427317103e-002, -1.740572552511763e-001, 2.173523941823380e-001, -1.740572552511763e-001, 8.729237427317105e-002, -2.506697855245887e-002, 3.155662499604400e-003},
                        new double[]{1.000000000000000e+000, -7.939604609483376e+000, 2.763452562724007e+001, -5.507291068653348e+001, 6.873404115363863e+001, -5.501137338286942e+001, 2.757280367200363e+001, -7.913019709572767e+000, 9.955379737195335e-001}
                },
                new double[][]{
                        new double[]{3.155296819092648e-003, -2.504226873854748e-002, 8.715240251302218e-002, -1.737138912344610e-001, 2.168969214731223e-001, -1.737138912344610e-001, 8.715240251302220e-002, -2.504226873854748e-002, 3.155296819092648e-003},
                        new double[]{1.000000000000000e+000, -7.932501259836564e+000, 2.759198049965839e+001, -5.496616090463505e+001, 6.859017154819686e+001, -5.490109293316875e+001, 2.752669337675260e+001, -7.904363542416965e+000, 9.952732759547681e-001}
                },
                new double[][]{
                        new double[]{3.154912832761072e-003, -2.501475665885260e-002, 8.699623555120342e-002, -1.733306679554957e-001, 2.163885527642557e-001, -1.733306679554957e-001, 8.699623555120342e-002, -2.501475665885260e-002, 3.154912832761072e-003},
                        new double[]{1.000000000000000e+000, -7.924547497442289e+000, 2.754440279254992e+001, -5.484690770861077e+001, 6.842959420455813e+001, -5.477812257507029e+001, 2.747535762332123e+001, -7.894769658576227e+000, 9.949929152413864e-001}
                },
                new double[][]{
                        new double[]{3.154509871638553e-003, -2.498411446674099e-002, 8.682198496185380e-002, -1.729029691694235e-001, 2.158211780866830e-001, -1.729029691694236e-001, 8.682198496185381e-002, -2.498411446674100e-002, 3.154509871638554e-003},
                        new double[]{1.000000000000000e+000, -7.915640854818626e+000, 2.749119845715203e+001, -5.471369565601653e+001, 6.825037921640217e+001, -5.464100005052808e+001, 2.741819438625329e+001, -7.884131315825985e+000, 9.946959695939834e-001}
                },
                new double[][]{
                        new double[]{3.154087278063668e-003, -2.494997555643428e-002, 8.662754638693286e-002, -1.724256629030175e-001, 2.151880103522501e-001, -1.724256629030176e-001, 8.662754638693286e-002, -2.494997555643428e-002, 3.154087278063668e-003},
                        new double[]{1.000000000000000e+000, -7.905666546401251e+000, 2.743170499299610e+001, -5.456490613904224e+001, 6.805038279056032e+001, -5.448810030112487e+001, 2.735453334899452e+001, -7.872329367941698e+000, 9.943814633476313e-001}
                },
                new double[][]{
                        new double[]{3.153644412559933e-003, -2.491193001442541e-002, 8.641057644002366e-002, -1.718930497004701e-001, 2.144815189350482e-001, -1.718930497004701e-001, 8.641057644002366e-002, -2.491193001442541e-002, 3.153644412559932e-003},
                        new double[]{1.000000000000000e+000, -7.894496006909852e+000, 2.736518407655722e+001, -5.439874095038135e+001, 6.782722626340617e+001, -5.431761919070840e+001, 2.728362865560162e+001, -7.859230828901897e+000, 9.940483641052797e-001}
                },
                new double[][]{
                        new double[]{3.153180661869607e-003, -2.486951955381822e-002, 8.616846759211466e-002, -1.712988072983833e-001, 2.136933591155672e-001, -1.712988072983833e-001, 8.616846759211465e-002, -2.486951955381822e-002, 3.153180661869607e-003},
                        new double[]{1.000000000000000e+000, -7.881985261957821e+000, 2.729081353869786e+001, -5.421320470473966e+001, 6.757827380910089e+001, -5.412755611530962e+001, 2.720465103810294e+001, -7.844687276533637e+000, 9.936955795205367e-001}
                },
                new double[][]{
                        new double[]{3.152695448305012e-003, -2.482223187759939e-002, 8.589832099269115e-002, -1.706359319618829e-001, 2.128142978393528e-001, -1.706359319618829e-001, 8.589832099269117e-002, -2.482223187759939e-002, 3.152695448305013e-003},
                        new double[]{1.000000000000000e+000, -7.867973113185387e+000, 2.720767867411994e+001, -5.400608618669720e+001, 6.730060898200142e+001, -5.391569557615249e+001, 2.711667930741871e+001, -7.828533079330270e+000, 9.933219539081104e-001}
                },
                new double[][]{
                        new double[]{3.152188240596396e-003, -2.476949441252039e-002, 8.559691722298893e-002, -1.698966768987765e-001, 2.118341365177477e-001, -1.698966768987765e-001, 8.559691722298890e-002, -2.476949441252038e-002, 3.152188240596395e-003},
                        new double[]{1.000000000000000e+000, -7.852279118743454e+000, 2.711476287956804e+001, -5.377493875379191e+001, 6.699101032259173e+001, -5.367958785031539e+001, 2.701869120994906e+001, -7.810583428982896e+000, 9.929262646739174e-001}
                },
                new double[][]{
                        new double[]{3.151658566438113e-003, -2.471066735097116e-002, 8.526068503891726e-002, -1.690724884104834e-001, 2.107416319545301e-001, -1.690724884104833e-001, 8.526068503891726e-002, -2.471066735097116e-002, 3.151658566438114e-003},
                        new double[]{1.000000000000000e+000, -7.834701348506457e+000, 2.701093763646996e+001, -5.351705999979735e+001, 6.664592636870657e+001, -5.341652897030031e+001, 2.690955367161049e+001, -7.790632160028131e+000, 9.925072185567274e-001}
                },
                new double[][]{
                        new double[]{3.151106026960063e-003, -2.464503593410573e-002, 8.488566824372117e-002, -1.681539407503443e-001, 2.095244169366758e-001, -1.681539407503443e-001, 8.488566824372115e-002, -2.464503593410573e-002, 3.151106026960062e-003},
                        new double[]{1.000000000000000e+000, -7.815013891993500e+000, 2.689495187958396e+001, -5.322947098143593e+001, 6.626145055721739e+001, -5.312354032258413e+001, 2.678801247775242e+001, -7.768449336977040e+000, 9.920634476729724e-001}
                },
                new double[][]{
                        new double[]{3.150530313380003e-003, -2.457180190573428e-002, 8.446749094086682e-002, -1.671306710575339e-001, 2.081689226006989e-001, -1.671306710575339e-001, 8.446749094086682e-002, -2.457180190573428e-002, 3.150530313380004e-003},
                        new double[]{1.000000000000000e+000, -7.792964095691631e+000, 2.676542082793782e+001, -5.290889543716165e+001, 6.583329668224586e+001, -5.279734830107660e+001, 2.665268147288917e+001, -7.743778588425860e+000, 9.915935053562469e-001}
                },
                new double[][]{
                        new double[]{3.149931226123974e-003, -2.449007406337997e-002, 8.400132156317211e-002, -1.659913162360803e-001, 2.066603054012899e-001, -1.659913162360803e-001, 8.400132156317211e-002, -2.449007406337997e-002, 3.149931226123974e-003},
                        new double[]{1.000000000000000e+000, -7.768269505387938e+000, 2.662081440022254e+001, -5.255173958488378e+001, 6.535677580170375e+001, -5.243436460993682e+001, 2.650203141082893e+001, -7.716334167049936e+000, 9.910958617828615e-001}
                },
                new double[][]{
                        new double[]{3.149308696737955e-003, -2.439885783076391e-002, 8.348183626380033e-002, -1.647234542738494e-001, 2.049823823919444e-001, -1.647234542738494e-001, 8.348183626380033e-002, -2.439885783076391e-002, 3.149308696737955e-003},
                        new double[]{1.000000000000000e+000, -7.740614488341203e+000, 2.645944539669780e+001, -5.215407328305949e+001, 6.482677576262216e+001, -5.203066800777763e+001, 2.633437864637221e+001, -7.685797714171228e+000, 9.905688993747558e-001}
                },
                new double[][]{
                        new double[]{3.148662812953755e-003, -2.429704377527410e-002, 8.290318249918893e-002, -1.633135532680249e-001, 2.031175796050522e-001, -1.633135532680249e-001, 8.290318249918893e-002, -2.429704377527410e-002, 3.148662812953756e-003},
                        new double[]{1.000000000000000e+000, -7.709646509807524e+000, 2.627945770704299e+001, -5.171161358357100e+001, 6.423774485589274e+001, -5.158198852888927e+001, 2.614787393767223e+001, -7.651814707922908e+000, 9.900109079710157e-001}
                },
                new double[][]{
                        new double[]{3.147993847316972e-003, -2.418339499528223e-002, 8.225894394631153e-002, -1.617469323651304e-001, 2.010468996187548e-001, -1.617469323651304e-001, 8.225894394631153e-002, -2.418339499528222e-002, 3.147993847316972e-003},
                        new double[]{1.000000000000000e+000, -7.674972038770115e+000, 2.607881490262433e+001, -5.121971200272804e+001, 6.358368152129840e+001, -5.108369551406376e+001, 2.594049172766094e+001, -7.613990575114853e+000, 9.894200797592629e-001}
                },
                new double[][]{
                        new double[]{3.147302289834950e-003, -2.405653330624902e-002, 8.154210829236015e-002, -1.600077399582097e-001, 1.987499159446394e-001, -1.600077399582097e-001, 8.154210829236014e-002, -2.405653330624902e-002, 3.147302289834948e-003},
                        new double[]{1.000000000000000e+000, -7.636152058960868e+000, 2.585528969732743e+001, -5.067334719604580e+001, 6.285813251191995e+001, -5.053079114110794e+001, 2.571002039846987e+001, -7.571886448988433e+000, 9.887945039582496e-001}
                },
                new double[][]{
                        new double[]{3.146588885158769e-003, -2.391492416236799e-002, 8.074503993219968e-002, -1.580789558318979e-001, 1.962048036828320e-001, -1.580789558318979e-001, 8.074503993219971e-002, -2.391492416236799e-002, 3.146588885158770e-003},
                        new double[]{1.000000000000000e+000, -7.592697163719579e+000, 2.560645491929913e+001, -5.006712514976279e+001, 6.205420249911512e+001, -4.991791156951130e+001, 2.545405415016516e+001, -7.525014558465190e+000, 9.881321612431455e-001}
                },
                new double[][]{
                        new double[]{3.145854674876141e-003, -2.375686026326679e-002, 7.985946022891161e-002, -1.559424255217798e-001, 1.933884179764724e-001, -1.559424255217798e-001, 7.985946022891161e-002, -2.375686026326679e-002, 3.145854674876141e-003},
                        new double[]{1.000000000000000e+000, -7.544062217323155e+000, 2.532967683347243e+001, -4.939528950196758e+001, 6.116457875756176e+001, -4.923933830918797e+001, 2.516998735089173e+001, -7.472833239653871e+000, 9.874309179052381e-001}
                },
                new double[][]{
                        new double[]{3.145101045562206e-003, -2.358044381456427e-002, 7.887643875940420e-002, -1.535789369592204e-001, 1.902764341435763e-001, -1.535789369592204e-001, 7.887643875940420e-002, -2.358044381456427e-002, 3.145101045562206e-003},
                        new double[]{1.000000000000000e+000, -7.489640571648333e+000, 2.502211189939137e+001, -4.865174517941539e+001, 6.018157531047837e+001, -4.848902298999204e+001, 2.485501244705817e+001, -7.414741567800956e+000, 9.866885197380821e-001}
                },
                new double[][]{
                        new double[]{3.144329783313850e-003, -2.338356743882253e-002, 7.778639990704111e-002, -1.509683514875423e-001, 1.868435659282269e-001, -1.509683514875423e-001, 7.778639990704111e-002, -2.338356743882253e-002, 3.144329783313850e-003},
                        new double[]{1.000000000000000e+000, -7.428757836057749e+000, 2.468070834925526e+001, -4.783009917679500e+001, 5.909720172464564e+001, -4.766062934041341e+001, 2.450612281760468e+001, -7.350073618197181e+000, 9.859025856426020e-001}
                },
                new double[][]{
                        new double[]{3.143543135581006e-003, -2.316389377207868e-002, 7.657915030499854e-002, -1.480898035077080e-001, 1.830638810073308e-001, -1.480898035077080e-001, 7.657915030499855e-002, -2.316389377207868e-002, 3.143543135581006e-003},
                        new double[]{1.000000000000000e+000, -7.360665211036957e+000, 2.430221433655941e+001, -4.692372299709454e+001, 5.790326259567788e+001, -4.674759686361156e+001, 2.412012231428596e+001, -7.278092378587070e+000, 9.850706009441995e-001}
                },
                new double[][]{
                        new double[]{3.142743881206541e-003, -2.291883383372955e-002, 7.524393400650334e-002, -1.449219852393075e-001, 1.789112355627868e-001, -1.449219852393075e-001, 7.524393400650334e-002, -2.291883383372955e-002, 3.142743881206540e-003},
                        new double[]{1.000000000000000e+000, -7.284532413376133e+000, 2.388319484571587e+001, -4.592584198278260e+001, 5.659149460855977e+001, -4.574323139402281e+001, 2.369364365886558e+001, -7.197983354359358e+000, 9.841899104156673e-001}
                },
                new double[][]{
                        new double[]{3.141935409696230e-003, -2.264552432784142e-002, 7.376952388809109e-002, -1.414435351904935e-001, 1.743598521445707e-001, -1.414435351904935e-001, 7.376952388809108e-002, -2.264552432784141e-002, 3.141935409696229e-003},
                        new double[]{1.000000000000000e+000, -7.199440243846447e+000, 2.342006007650916e+001, -4.482965744266117e+001, 5.515374882202251e+001, -4.464082837240964e+001, 2.322317837594224e+001, -7.108847932436513e+000, 9.832577110005875e-001}
                },
                new double[][]{
                        new double[]{3.141121810862988e-003, -2.234080412659672e-002, 7.214435970035693e-002, -1.376335506400123e-001, 1.693850668579320e-001, -1.376335506400123e-001, 7.214435970035690e-002, -2.234080412659671e-002, 3.141121810862987e-003},
                        new double[]{1.000000000000000e+000, -7.104372878921088e+000, 2.290910863125306e+001, -4.362850803060598e+001, 5.358222639763521e+001, -4.343383520301292e+001, 2.270512153246824e+001, -7.009696601809882e+000, 9.822710442329992e-001}
                },
                new double[][]{
                        new double[]{3.140307976126783e-003, -2.200119030724153e-002, 7.035674537599862e-002, -1.334722453248443e-001, 1.639642726170729e-001, -1.334722453248443e-001, 7.035674537599862e-002, -2.200119030724154e-002, 3.140307976126782e-003},
                        new double[]{1.000000000000000e+000, -6.998210008037751e+000, 2.234658954035024e+001, -4.231607712696974e+001, 5.186977622103262e+001, -4.211605932017192e+001, 2.213583523342565e+001, -6.899442169873026e+000, 9.812267883503994e-001}
                },
                new double[][]{
                        new double[]{3.139499712905021e-003, -2.162285426944518e-002, 6.839512067458584e-002, -1.289417729867781e-001, 1.580780841620924e-001, -1.289417729867781e-001, 6.839512067458588e-002, -2.162285426944519e-002, 3.139499712905022e-003},
                        new double[]{1.000000000000000e+000, -6.879718989504162e+000, 2.172878796019500e+001, -4.088665281760313e+001, 5.001026253686516e+001, -4.068192840019685e+001, 2.151173558210775e+001, -6.776893166237469e+000, 9.801216500987595e-001}
                },
                new double[][]{
                        new double[]{3.138703873699729e-003, -2.120159865872217e-002, 6.624842494020337e-002, -1.240272346018628e-001, 1.517117468996463e-001, -1.240272346018628e-001, 6.624842494020337e-002, -2.120159865872217e-002, 3.138703873699729e-003},
                        new double[]{1.000000000000000e+000, -6.747547264196246e+000, 2.105214025163815e+001, -3.933544618694712e+001, 4.799900957772635e+001, -3.912680823655984e+001, 2.082940863624978e+001, -6.640747692124121e+000, 9.789521562300233e-001}
                },
                new double[][]{
                        new double[]{3.137928501679498e-003, -2.073283607339804e-002, 6.390657357142512e-002, -1.187178808303045e-001, 1.448568043951183e-001, -1.187178808303045e-001, 6.390657357142512e-002, -2.073283607339804e-002, 3.137928501679498e-003},
                        new double[]{1.000000000000000e+000, -6.600215350011449e+000, 2.031338506624802e+001, -3.765897166957116e+001, 4.583332789420625e+001, -3.744738178488232e+001, 2.008576174391972e+001, -6.489588056709191e+000, 9.777146446947530e-001}
                },
                new double[][]{
                        new double[]{3.137182994768761e-003, -2.021157084921369e-002, 6.136107057341432e-002, -1.130085099447135e-001, 1.375130273952083e-001, -1.130085099447135e-001, 6.136107057341432e-002, -2.021157084921369e-002, 3.137182994768761e-003},
                        new double[]{1.000000000000000e+000, -6.436110845532803e+000, 1.950975797479131e+001, -3.585548965015296e+001, 4.351312333645753e+001, -3.564208929582135e+001, 1.927821746633142e+001, -6.321876645437641e+000, 9.764052555351180e-001}
                },
                new double[][]{
                        new double[]{3.136478290496715e-003, -1.963238561066010e-002, 5.860578298576107e-002, -1.069010432752794e-001, 1.296905894905115e-001, -1.069010432752794e-001, 5.860578298576107e-002, -1.963238561066010e-002, 3.136478290496715e-003},
                        new double[]{1.000000000000000e+000, -6.253484003079926e+000, 1.863923797465455e+001, -3.392550574495158e+001, 4.104158405102899e+001, -3.371162367818436e+001, 1.840495799303931e+001, -6.135953593248776e+000, 9.750199214863040e-001}
                },
                new double[][]{
                        new double[]{3.135827074126856e-003, -1.898943476709113e-002, 5.563790460357721e-002, -1.004062326978215e-001, 1.214124500942118e-001, -1.004062326978215e-001, 5.563790460357720e-002, -1.898943476709113e-002, 3.135827074126855e-003},
                        new double[]{1.000000000000000e+000, -6.050445594454034e+000, 1.770085476591669e+001, -3.187231248663630e+001, 3.842593317714272e+001, -3.165946653481059e+001, 1.746522838225852e+001, -5.930036992274631e+000, 9.735543582978614e-001}
                },
                new double[][]{
                        new double[]{3.135244012888754e-003, -1.827644773199041e-002, 5.245913656145099e-002, -9.354541521470155e-002, 1.127168741389934e-001, -9.354541521470155e-002, 5.245913656145100e-002, -1.827644773199041e-002, 3.135244012888755e-003},
                        new double[]{1.000000000000000e+000, -5.824967992989349e+000, 1.669506577467719e+001, -2.970254661371651e+001, 3.567822510278451e+001, -2.949243785701341e+001, 1.645970690065039e+001, -5.702226553585582e+000, 9.720040547903410e-001}
                },
                new double[][]{
                        new double[]{3.134746019470630e-003, -1.748674537436416e-002, 4.907711006745098e-002, -8.635217549983100e-002, 1.036599808864124e-001, -8.635217549983101e-002, 4.907711006745098e-002, -1.748674537436416e-002, 3.134746019470632e-003},
                        new double[]{1.000000000000000e+000, -5.574890639196458e+000, 1.562421121838405e+001, -2.742671795429572e+001, 3.281615152807541e+001, -2.722121529947888e+001, 1.539094988747847e+001, -5.450511870800915e+000, 9.703642626670224e-001}
                },
                new double[][]{
                        new double[]{3.134352548309350e-003, -1.661327409169017e-002, 4.550707051926735e-002, -7.887370609052574e-002, 9.431817474222426e-002, -7.887370609052574e-002, 4.550707051926735e-002, -1.661327409169017e-002, 3.134352548309349e-003},
                        new double[]{1.000000000000000e+000, -5.297931350649583e+000, 1.449305362107864e+001, -2.505964324185187e+001, 2.986381121167740e+001, -2.486075657853854e+001, 1.426391650604211e+001, -5.172786704431734e+000, 9.686299861056348e-001}
                },
                new double[][]{
                        new double[]{3.134085928638541e-003, -1.564866294308468e-002, 4.177383060073083e-002, -7.117156573103167e-002, 8.479027540628280e-002, -7.117156573103167e-002, 4.177383060073084e-002, -1.564866294308468e-002, 3.134085928638541e-003},
                        new double[]{1.000000000000000e+000, -4.991705285766049e+000, 1.330940451460967e+001, -2.262068977353928e+001, 2.685238618316034e+001, -2.243053056001659e+001, 1.308657490580105e+001, -4.866871023409204e+000, 9.667959711606842e-001}
                },
                new double[][]{
                        new double[]{3.133971738732764e-003, -1.458531051532196e-002, 3.791398038204636e-002, -6.332143101523267e-002, 7.519914376860126e-002, -6.332143101523267e-002, 3.791398038204637e-002, -1.458531051532196e-002, 3.133971738732764e-003},
                        new double[]{1.000000000000000e+000, -4.653753780459771e+000, 1.208483484175470e+001, -2.013370016357715e+001, 2.382066078021547e+001, -1.995442963213826e+001, 1.187058496830032e+001, -4.530542906340166e+000, 9.648566950135764e-001}
                },
                new double[][]{
                        new double[]{3.134039226322169e-003, -1.341550962251499e-002, 3.397831195038829e-002, -5.541132177031712e-002, 6.569260943266918e-002, -5.541132177031712e-002, 3.397831195038829e-002, -1.341550962251499e-002, 3.134039226322169e-003},
                        new double[]{1.000000000000000e+000, -4.281585748495832e+000, 1.083545584338935e+001, -1.762643268940588e+001, 2.081532308871231e+001, -1.746020018575418e+001, 1.063204303178750e+001, -4.161582818466576e+000, 9.628063551150658e-001}
                },
                new double[][]{
                        new double[]{3.134321780757536e-003, -1.213161956026687e-002, 3.003437108339706e-002, -4.753767160414873e-002, 5.644356598658089e-002, -4.753767160414873e-002, 3.003437108339706e-002, -1.213161956026687e-002, 3.134321780757536e-003},
                        new double[]{1.000000000000000e+000, -3.872734868428338e+000, 9.582742796500897e+000, -1.512931661567665e+001, 1.789100765938704e+001, -1.497819422522095e+001, 9.392259689330880e+000, -3.757833234835759e+000, 9.606388582728883e-001}
                },
                new double[][]{
                        new double[]{3.134857463189034e-003, -1.072629742487929e-002, 2.616898486943644e-002, -3.979853915444668e-002, 4.764923335280728e-002, -3.979853915444668e-002, 2.616898486943644e-002, -1.072629742487929e-002, 3.134857463189034e-003},
                        new double[]{1.000000000000000e+000, -3.424836362665025e+000, 8.354353544174995e+000, -1.267329710343472e+001, 1.511008073912572e+001, -1.253922198523033e+001, 8.178521712285502e+000, -3.317277061692939e+000, 9.583478097467506e-001}
                },
                new double[][]{
                        new double[]{3.135689601795952e-003, -9.192801914856020e-003, 2.249052777972339e-002, -3.228325770744059e-002, 3.952980949329304e-002, -3.228325770744058e-002, 2.249052777972340e-002, -9.192801914856018e-003, 3.135689601795953e-003},
                        new double[]{1.000000000000000e+000, -2.935727786560088e+000, 7.184865948619916e+000, -1.028654413040799e+001, 1.254223993346572e+001, -1.017128706803972e+001, 7.024762141100510e+000, -2.838138790755699e+000, 9.559265024233198e-001}
                },
                new double[][]{
                        new double[]{3.136867459982495e-003, -7.525384956076330e-003, 1.913057593393776e-002, -2.505796833460762e-002, 3.232704353501763e-002, -2.505796833460763e-002, 1.913057593393775e-002, -7.525384956076330e-003, 3.136867459982495e-003},
                        new double[]{1.000000000000000e+000, -2.403578849731980e+000, 6.116322021836488e+000, -7.989845724149395e+000, 1.026409819970081e+001, -7.895033186180221e+000, 5.972027625440275e+000, -2.319012762106196e+000, 9.533679061555159e-001}
                },
                new double[][]{
                        new double[]{3.138446986453600e-003, -5.719788266387046e-003, 1.624445853637542e-002, -1.814686749013252e-002, 2.630361280892670e-002, -1.814686749013250e-002, 1.624445853637542e-002, -5.719788266387046e-003, 3.138446986453601e-003},
                        new double[]{1.000000000000000e+000, -1.827055825922349e+000, 5.198420980989070e+000, -5.790627461846015e+000, 8.359032593241123e+000, -5.717854195620141e+000, 5.068588868120163e+000, -1.759023244522346e+000, 9.506646573634053e-001}
                },
                new double[][]{
                        new double[]{3.140491657224234e-003, -3.773863336298222e-003, 1.401004915216966e-002, -1.150974045018174e-002, 2.174445408345166e-002, -1.150974045018174e-002, 1.401004915216966e-002, -3.773863336298222e-003, 3.140491657224234e-003},
                        new double[]{1.000000000000000e+000, -1.205526475294007e+000, 4.488149587584013e+000, -3.675774949486338e+000, 6.917662040320396e+000, -3.626853005615643e+000, 4.369489565598928e+000, -1.158021168308530e+000, 9.478090490084538e-001}
                },
                new double[][]{
                        new double[]{3.143073420920190e-003, -1.688333852644986e-003, 1.262395748797918e-002, -5.017573087974801e-003, 1.896114476128463e-002, -5.017573087974800e-003, 1.262395748797918e-002, -1.688333852644986e-003, 3.143073420920191e-003},
                        new double[]{1.000000000000000e+000, -5.393114608165095e-001, 4.048578996469637e+000, -1.603824537959884e+000, 6.039299478305785e+000, -1.581219217193774e+000, 3.935274955307952e+000, -5.168221229728384e-001, 9.447930210688752e-001}
                },
                new double[][]{
                        new double[]{3.146273760226500e-003, 5.322812464931028e-004, 1.229410658041627e-002, 1.570162041369520e-003, 1.829966777695044e-002, 1.570162041369518e-003, 1.229410658041627e-002, 5.322812464931027e-004, 3.146273760226500e-003},
                        new double[]{1.000000000000000e+000, 1.700122175818912e-001, 3.946500014377160e+000, 5.023368561645096e-001, 5.834479755741241e+000, 4.948389524665608e-001, 3.829590220830910e+000, 1.625105377327541e-001, 9.416081516614648e-001}
                },
                new double[][]{
                        new double[]{3.150184884067147e-003, 2.878568940453699e-003, 1.322755106586022e-002, 8.620552071421581e-003, 2.014990932533000e-002, 8.620552071421581e-003, 1.322755106586022e-002, 2.878568940453698e-003, 3.150184884067147e-003},
                        new double[]{1.000000000000000e+000, 9.192515080342589e-001, 4.248526154251194e+000, 2.760354359206612e+000, 6.428032354219372e+000, 2.716723422282723e+000, 4.115304532346304e+000, 8.763352262803313e-001, 9.382456489747480e-001}
                },
                new double[][]{
                        new double[]{3.154911067098877e-003, 5.334938740144926e-003, 1.561237564818607e-002, 1.662582664159496e-002, 2.495150819282766e-002, 1.662582664159495e-002, 1.561237564818606e-002, 5.334938740144928e-003, 3.154911067098877e-003},
                        new double[]{1.000000000000000e+000, 1.703179499277953e+000, 5.015287582907517e+000, 5.328060497664996e+000, 7.960981372090182e+000, 5.238878049409717e+000, 4.848819300199697e+000, 1.619054784052114e+000, 9.346963441995912e-001}
                },
                new double[][]{
                        new double[]{3.160570155425528e-003, 7.878157153029112e-003, 1.959270862731381e-002, 2.617401829906347e-002, 3.318498160501834e-002, 2.617401829906346e-002, 1.959270862731381e-002, 7.878157153029110e-003, 3.160570155425528e-003},
                        new double[]{1.000000000000000e+000, 2.514061860132557e+000, 6.293401312113454e+000, 8.394231003661758e+000, 1.058774518680232e+001, 8.245442893665601e+000, 6.072290240693297e+000, 2.382697143855915e+000, 9.309506856667303e-001}
                },
                new double[][]{
                        new double[]{3.167295260151064e-003, 1.047572033190279e-002, 2.523643163272042e-002, 3.787881035026627e-002, 4.533007101187137e-002, 3.787881035026630e-002, 2.523643163272043e-002, 1.047572033190280e-002, 3.167295260151064e-003},
                        new double[]{1.000000000000000e+000, 3.341136359577790e+000, 8.105078676150034e+000, 1.215629189647473e+001, 1.446288885419975e+001, 1.192812032225852e+001, 7.803674281773004e+000, 3.156465395399920e+000, 9.269987344260360e-001}
                },
                new double[][]{
                        new double[]{3.175236663568253e-003, 1.308411231056350e-002, 3.249623781842040e-002, 5.225727921334373e-002, 6.176741363408551e-002, 5.225727921334372e-002, 3.249623781842039e-002, 1.308411231056350e-002, 3.175236663568252e-003},
                        new double[]{1.000000000000000e+000, 4.170063916809950e+000, 1.043557693581984e+001, 1.678131188069654e+001, 1.970982407256151e+001, 1.644776743866162e+001, 1.002485619533124e+001, 3.926277990238661e+000, 9.228301615299103e-001}
                },
                new double[][]{
                        new double[]{3.184563966522064e-003, 1.564703528332045e-002, 4.116647822650334e-002, 6.955135546761322e-002, 8.260002997525118e-002, 6.955135546761322e-002, 4.116647822650334e-002, 1.564703528332045e-002, 3.184563966522064e-003},
                        new double[]{1.000000000000000e+000, 4.982383252986065e+000, 1.321928002312586e+001, 2.234885788249893e+001, 2.636396393156664e+001, 2.187848507504302e+001, 1.266867131040726e+001, 4.674332629971476e+000, 9.184342473129035e-001}
                },
                new double[][]{
                        new double[]{3.195468509911748e-003, 1.809375255989474e-002, 5.084090030653590e-002, 8.950422298444746e-002, 1.073856458036988e-001, 8.950422298444746e-002, 5.084090030653590e-002, 1.809375255989474e-002, 3.195468509911749e-003},
                        new double[]{1.000000000000000e+000, 5.755019760051603e+000, 1.632605611212203e+001, 2.877918880332384e+001, 3.428743475883073e+001, 2.813781188877264e+001, 1.560646217523939e+001, 5.378744289396370e+000, 9.137998829916308e-001}
                },
                new double[][]{
                        new double[]{3.208166108549520e-003, 2.033775720013014e-002, 6.087995852729569e-002, 1.111283766985333e-001, 1.348084233730102e-001, 1.111283766985333e-001, 6.087995852729569e-002, 2.033775720013014e-002, 3.208166108549520e-003},
                        new double[]{1.000000000000000e+000, 6.459923452852331e+000, 1.955069789760037e+001, 3.575841878007016e+001, 4.306434589484458e+001, 3.491460431644380e+001, 1.863888825236724e+001, 6.013329244005268e+000, 9.089155749432362e-001}
                },
                new double[][]{
                        new double[]{3.222900141858363e-003, 2.227607222988249e-002, 7.040061526828752e-002, 1.325362521214648e-001, 1.623833078994799e-001, 1.325362521214648e-001, 7.040061526828752e-002, 2.227607222988248e-002, 3.222900141858363e-003},
                        new double[]{1.000000000000000e+000, 7.063941201586084e+000, 2.260962332507320e+001, 4.268327468011343e+001, 5.190611606085580e+001, 4.161681749209127e+001, 2.149395668247144e+001, 6.547632809776026e+000, 9.037694520569338e-001}
                },
                new double[][]{
                        new double[]{3.239945053390167e-003, 2.378960212591439e-002, 7.830568646607240e-002, 1.509391118013942e-001, 1.863663521383706e-001, 1.509391118013942e-001, 7.830568646607242e-002, 2.378960212591439e-002, 3.239945053390167e-003},
                        new double[]{1.000000000000000e+000, 7.529064346833643e+000, 2.515035008465618e+001, 4.865934114163024e+001, 5.962181888765429e+001, 4.737213222877233e+001, 2.383740644607056e+001, 6.947327741252297e+000, 8.983492765915317e-001}
                },
                new double[][]{
                        new double[]{3.259610320154745e-003, 2.474508638385794e-002, 8.337226016441461e-002, 1.629367425557874e-001, 2.020999170550664e-001, 1.629367425557874e-001, 8.337226016441456e-002, 2.474508638385793e-002, 3.259610320154744e-003},
                        new double[]{1.000000000000000e+000, 7.813232181445080e+000, 2.678004704072203e+001, 5.259232996155323e+001, 6.472625974952639e+001, 5.111941179551023e+001, 2.530116502103031e+001, 7.175140285956257e+000, 8.926424590119200e-001}
                }
        };
    }

    private static class MIDI_COEFFICIENTS_SR5512_5HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {


        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.161731550715255e-003, -2.529274319570281e-002, 8.852182830483955e-002, -1.770403292694686e-001, 2.212990252192333e-001, -1.770403292694686e-001, 8.852182830483955e-002, -2.529274319570281e-002, 3.161731550715255e-003},
                        new double[]{1.000000000000000e+000, -7.999300500855798e+000, 2.799545105525624e+001, -5.598739563134019e+001, 6.998072990307688e+001, -5.598246687406163e+001, 2.799052220625634e+001, -7.997188071481178e+000, 9.996479131493456e-001}
                },
                new double[][]{
                        new double[]{3.161698782519206e-003, -2.529234522806594e-002, 8.852009590060686e-002, -1.770364570701295e-001, 2.212940152301387e-001, -1.770364570701295e-001, 8.852009590060689e-002, -2.529234522806595e-002, 3.161698782519207e-003},
                        new double[]{1.000000000000000e+000, -7.999237025133197e+000, 2.799504929776608e+001, -5.598631803569721e+001, 6.997914664869103e+001, -5.598109631343003e+001, 2.798982746642563e+001, -7.996999019503344e+000, 9.996269808810403e-001}
                },
                new double[][]{
                        new double[]{3.161664087283275e-003, -2.529191521379445e-002, 8.851820976715390e-002, -1.770322275409812e-001, 2.212885378006770e-001, -1.770322275409812e-001, 8.851820976715390e-002, -2.529191521379445e-002, 3.161664087283275e-003},
                        new double[]{1.000000000000000e+000, -7.999167095515828e+000, 2.799460757902766e+001, -5.598513619495752e+001, 6.997741570428816e+001, -5.597960410504683e+001, 2.798907535940938e+001, -7.996796051598514e+000, 9.996048043934835e-001}
                },
                new double[][]{
                        new double[]{3.161627353046769e-003, -2.529145022115624e-002, 8.851615455584626e-002, -1.770276038817035e-001, 2.212825443879335e-001, -1.770276038817035e-001, 8.851615455584626e-002, -2.529145022115624e-002, 3.161627353046770e-003},
                        new double[]{1.000000000000000e+000, -7.999090000149478e+000, 2.799412155540460e+001, -5.598383899403104e+001, 6.997552173486156e+001, -5.597797810195495e+001, 2.798826050908336e+001, -7.996578012974147e+000, 9.995813097600974e-001}
                },
                new double[][]{
                        new double[]{3.161588461513018e-003, -2.529094701849926e-002, 8.851391324591054e-002, -1.770225452013237e-001, 2.212759810247989e-001, -1.770225452013237e-001, 8.851391324591053e-002, -2.529094701849925e-002, 3.161588461513017e-003},
                        new double[]{1.000000000000000e+000, -7.999004944682265e+000, 2.799358638420972e+001, -5.598241405544334e+001, 6.997344769203320e+001, -5.597620483395794e+001, 2.798737697930015e+001, -7.996343640125112e+000, 9.995564186655692e-001}
                },
                new double[][]{
                        new double[]{3.161547287711997e-003, -2.529040204105239e-002, 8.851146695215922e-002, -1.770170060425100e-001, 2.212687876873824e-001, -1.770170060425100e-001, 8.851146695215921e-002, -2.529040204105239e-002, 3.161547287711996e-003},
                        new double[]{1.000000000000000e+000, -7.998911042442045e+000, 2.799299666458529e+001, -5.598084759076193e+001, 6.997117461424480e+001, -5.597426935551604e+001, 2.798641821121696e+001, -7.996091549472803e+000, 9.995300481457619e-001}
                },
                new double[][]{
                        new double[]{3.161503699647645e-003, -2.528981135387320e-002, 8.850879470994689e-002, -1.770109358492243e-001, 2.212608975870059e-001, -1.770109358492243e-001, 8.850879470994692e-002, -2.528981135387320e-002, 3.161503699647646e-003},
                        new double[]{1.000000000000000e+000, -7.998807303428078e+000, 2.799234637127157e+001, -5.597912423422096e+001, 6.996868140312893e+001, -5.597215507564681e+001, 2.798537695331360e+001, -7.995820224730498e+000, 9.995021103122610e-001}
                },
                new double[][]{
                        new double[]{3.161457557929669e-003, -2.528917061048478e-002, 8.850587323462855e-002, -1.770042783709314e-001, 2.212522363777158e-001, -1.770042783709314e-001, 8.850587323462855e-002, -2.528917061048478e-002, 3.161457557929670e-003},
                        new double[]{1.000000000000000e+000, -7.998692621972898e+000, 2.799162878040291e+001, -5.597722685639667e+001, 6.996594457319563e+001, -5.596984356767348e+001, 2.798424518323031e+001, -7.995528002846465e+000, 9.994725120606737e-001}
                },
                new double[][]{
                        new double[]{3.161408715389472e-003, -2.528847500668750e-002, 8.850267665245455e-002, -1.769969709957286e-001, 2.212427212691440e-001, -1.769969709957285e-001, 8.850267665245455e-002, -2.528847500668750e-002, 3.161408715389472e-003},
                        new double[]{1.000000000000000e+000, -7.998565762912708e+000, 2.799083638636589e+001, -5.597513635552572e+001, 6.996293797162383e+001, -5.596731435640531e+001, 2.798301402045050e+001, -7.995213058358191e+000, 9.994411547617077e-001}
                },
                new double[][]{
                        new double[]{3.161357016680064e-003, -2.528771922897007e-002, 8.849917619947209e-002, -1.769889440038497e-001, 2.212322600333352e-001, -1.769889440038497e-001, 8.849917619947209e-002, -2.528771922897008e-002, 3.161357016680064e-003},
                        new double[]{1.000000000000000e+000, -7.998425346085041e+000, 2.798996080863779e+001, -5.597283142376993e+001, 6.995963246456225e+001, -5.596454468004423e+001, 2.798167362873678e+001, -7.994873385971648e+000, 9.994079339340317e-001}
                },
                new double[][]{
                        new double[]{3.161302297859827e-003, -2.528689739687535e-002, 8.849533988458978e-002, -1.769801197319709e-001, 2.212207498927932e-001, -1.769801197319709e-001, 8.849533988458978e-002, -2.528689739687534e-002, 3.161302297859828e-003},
                        new double[]{1.000000000000000e+000, -7.998269828950293e+000, 2.798899268739245e+001, -5.597028828540589e+001, 6.995599558591326e+001, -5.596150922378865e+001, 2.798021310709847e+001, -7.994506781157211e+000, 9.993727388978670e-001}
                },
                new double[][]{
                        new double[]{3.161244385960086e-003, -2.528600299859753e-002, 8.849113211249675e-002, -1.769704116375925e-001, 2.212080762754665e-001, -1.769704116375925e-001, 8.849113211249676e-002, -2.528600299859753e-002, 3.161244385960087e-003},
                        new double[]{1.000000000000000e+000, -7.998097487108931e+000, 2.798792156651412e+001, -5.596748040355397e+001, 6.995199114408922e+001, -5.595817982174073e+001, 2.797862036792086e+001, -7.994110818528716e+000, 9.993354524081550e-001}
                },
                new double[][]{
                        new double[]{3.161183098536588e-003, -2.528502881900131e-002, 8.848651326161339e-002, -1.769597232514924e-001, 2.211941114206876e-001, -1.769597232514925e-001, 8.848651326161340e-002, -2.528502881900131e-002, 3.161183098536589e-003},
                        new double[]{1.000000000000000e+000, -7.997906392458485e+000, 2.798673576249600e+001, -5.596437815165698e+001, 6.994757878169354e+001, -5.595452512332000e+001, 2.797688200072444e+001, -7.993682827744629e+000, 9.992959502661386e-001}
                },
                new double[][]{
                        new double[]{3.161118243205082e-003, -2.528396685915505e-002, 8.848143921166912e-002, -1.769479470048061e-001, 2.211787128181739e-001, -1.769479470048060e-001, 8.848143921166909e-002, -2.528396685915505e-002, 3.161118243205083e-003},
                        new double[]{1.000000000000000e+000, -7.997694388703194e+000, 2.798542221751621e+001, -5.596094844546255e+001, 6.994271348247383e+001, -5.595051021993152e+001, 2.797498311983799e+001, -7.993219866638839e+000, 9.992541009080764e-001}
                },
                new double[][]{
                        new double[]{3.161049617161414e-003, -2.528280824636124e-002, 8.847586081485707e-002, -1.769349629156903e-001, 2.211617214600661e-001, -1.769349629156903e-001, 8.847586081485709e-002, -2.528280824636124e-002, 3.161049617161414e-003},
                        new double[]{1.000000000000000e+000, -7.997459063894291e+000, 2.798396633477872e+001, -5.595715433075780e+001, 6.993734501922353e+001, -5.594609622713119e+001, 2.797290719406510e+001, -7.992718691253814e+000, 9.992097649697828e-001}
                },
                new double[][]{
                        new double[]{3.160977006686676e-003, -2.528154313354447e-002, 8.846972330378967e-002, -1.769206371187328e-001, 2.211429598836020e-001, -1.769206371187328e-001, 8.846972330378969e-002, -2.528154313354447e-002, 3.160977006686676e-003},
                        new double[]{1.000000000000000e+000, -7.997197719639722e+000, 2.798235179397666e+001, -5.595295452153719e+001, 6.993141733555554e+001, -5.594123981696578e+001, 2.797063585619458e+001, -7.992175722409599e+000, 9.991627948255754e-001}
                },
                new double[][]{
                        new double[]{3.160900186638132e-003, -2.528016058671942e-002, 8.846296562866780e-002, -1.769048202182677e-001, 2.211222299793627e-001, -1.769048202182677e-001, 8.846296562866780e-002, -2.528016058671942e-002, 3.160900186638132e-003},
                        new double[]{1.000000000000000e+000, -7.996907336578110e+000, 2.798056034447828e+001, -5.594830288265121e+001, 6.992486785363307e+001, -5.593589269453279e+001, 2.796814868994899e+001, -7.991587008398287e+000, 9.991130341001322e-001}
                },
                new double[][]{
                        new double[]{3.160818919927040e-003, -2.527864845910844e-002, 8.845551971517662e-002, -1.768873454445385e-001, 2.210993105370871e-001, -1.768873454445385e-001, 8.845551971517662e-002, -2.527864845910844e-002, 3.160818919927039e-003},
                        new double[]{1.000000000000000e+000, -7.996584535662630e+000, 2.797857157354876e+001, -5.594314785027997e+001, 6.991762669901105e+001, -5.593000101210501e+001, 2.796542299168110e+001, -7.990948183344893e+000, 9.990603171517307e-001}
                },
                new double[][]{
                        new double[]{3.160732956984562e-003, -2.527699325030442e-002, 8.844730963360412e-002, -1.768680265891523e-001, 2.210739544977368e-001, -1.768680265891523e-001, 8.844730963360412e-002, -2.527699325030443e-002, 3.160732956984563e-003},
                        new double[]{1.000000000000000e+000, -7.996225534745101e+000, 2.797636264659958e+001, -5.593743178278633e+001, 6.990961583269734e+001, -5.592350471337751e+001, 2.796243350380717e+001, -7.990254420720088e+000, 9.990044685251582e-001}
                },
                new double[][]{
                        new double[]{3.160642035217503e-003, -2.527517994868275e-002, 8.843825066855632e-002, -1.768466556935119e-001, 2.210458858768428e-001, -1.768466556935119e-001, 8.843825066855633e-002, -2.527517994868275e-002, 3.160642035217504e-003},
                        new double[]{1.000000000000000e+000, -7.995826099888900e+000, 2.797390801609919e+001, -5.593109023362921e+001, 6.990074807938849e+001, -5.591633679952461e+001, 2.795915211661225e+001, -7.989500381429437e+000, 9.989453023725972e-001}
                },
                new double[][]{
                        new double[]{3.160545878455811e-003, -2.527319185504862e-002, 8.842824827738170e-002, -1.768230004608285e-001, 2.210147963200810e-001, -1.768230004608285e-001, 8.842824827738169e-002, -2.527319185504861e-002, 3.160545878455811e-003},
                        new double[]{1.000000000000000e+000, -7.995381490769717e+000, 2.797117909537814e+001, -5.592405113704149e+001, 6.989092603954595e+001, -5.590842250778280e+001, 2.795554753466443e+001, -7.988680155834519e+000, 9.988826218405983e-001}
                },
                new double[][]{
                        new double[]{3.160444196394361e-003, -2.527101038526481e-002, 8.841719692401759e-002, -1.767968013589008e-001, 2.209803412475103e-001, -1.767968013589008e-001, 8.841719692401759e-002, -2.527101038526481e-002, 3.160444196394361e-003},
                        new double[]{1.000000000000000e+000, -7.994886399445699e+000, 2.796814389311626e+001, -5.591623389609305e+001, 6.988004087155034e+001, -5.589967839219742e+001, 2.795158490363400e+001, -7.987787198984727e+000, 9.988162184212104e-001}
                },
                new double[][]{
                        new double[]{3.160336684031966e-003, -2.526861484933287e-002, 8.840497877340901e-002, -1.767677683770464e-001, 2.209421355378812e-001, -1.767677683770464e-001, 8.840497877340901e-002, -2.526861484933288e-002, 3.160336684031967e-003},
                        new double[]{1.000000000000000e+000, -7.994334881691234e+000, 2.796476660380225e+001, -5.590754836155735e+001, 6.986797092858842e+001, -5.589001129497501e+001, 2.794722539282159e+001, -7.986814258252410e+000, 9.987458712652104e-001}
                },
                new double[][]{
                        new double[]{3.160223021111152e-003, -2.526598220409768e-002, 8.839146222991390e-002, -1.767355773963661e-001, 2.208997486988848e-001, -1.767355773963661e-001, 8.839146222991388e-002, -2.526598220409768e-002, 3.160223021111152e-003},
                        new double[]{1.000000000000000e+000, -7.993720279991338e+000, 2.796100714890299e+001, -5.589789368866482e+001, 6.985458023317315e+001, -5.587931719555724e+001, 2.794242572815215e+001, -7.985753292467819e+000, 9.986713464552553e-001}
                },
                new double[][]{
                        new double[]{3.160102871562913e-003, -2.526308677640546e-002, 8.837650030117852e-002, -1.766998661278661e-001, 2.208526994630720e-001, -1.766998661278661e-001, 8.837650030117855e-002, -2.526308677640547e-002, 3.160102871562914e-003},
                        new double[]{1.000000000000000e+000, -7.993035137184169e+000, 2.795682066286448e+001, -5.588715705735085e+001, 6.983971677026145e+001, -5.586747992306563e+001, 2.793713766978453e+001, -7.984595381542750e+000, 9.985923962366661e-001}
                },
                new double[][]{
                        new double[]{3.159975882961421e-003, -2.525989995316554e-002, 8.835992876682405e-002, -1.766602295678194e-001, 2.208004497424176e-001, -1.766602295678194e-001, 8.835992876682405e-002, -2.525989995316553e-002, 3.159975882961421e-003},
                        new double[]{1.000000000000000e+000, -7.992271099617168e+000, 2.795215691738355e+001, -5.587521223997506e+001, 6.982321057779913e+001, -5.585436971614900e+001, 2.793130742781282e+001, -7.983330625451835e+000, 9.985087582034402e-001}
                },
                new double[][]{
                        new double[]{3.159841685994492e-003, -2.525638983434093e-002, 8.834156412890981e-002, -1.766162149140839e-001, 2.207423978670707e-001, -1.766162149140839e-001, 8.834156412890981e-002, -2.525638983434093e-002, 3.159841685994491e-003},
                        new double[]{1.000000000000000e+000, -7.991418808545359e+000, 2.794695967663140e+001, -5.586191799869053e+001, 6.980487161117404e+001, -5.583984161248014e+001, 2.792487500878889e+001, -7.981948031305878e+000, 9.984201544369321e-001}
                },
                new double[][]{
                        new double[]{3.159699893956621e-003, -2.525252084441813e-002, 8.832120131851101e-002, -1.765673158808659e-001, 2.206778710256798e-001, -1.765673158808659e-001, 8.832120131851101e-002, -2.525252084441813e-002, 3.159699893956621e-003},
                        new double[]{1.000000000000000e+000, -7.990467778347355e+000, 2.794116597527122e+001, -5.584711629266529e+001, 6.978448735548403e+001, -5.582373364819085e+001, 2.791777348497057e+001, -7.980435387101997e+000, 9.983262905945257e-001}
                },
                new double[][]{
                        new double[]{3.159550102272552e-003, -2.524825329737595e-002, 8.829861112984655e-002, -1.765129663425945e-001, 2.206061168157774e-001, -1.765129663425945e-001, 8.829861112984654e-002, -2.524825329737595e-002, 3.159550102272551e-003},
                        new double[]{1.000000000000000e+000, -7.989406259963432e+000, 2.793470531018452e+001, -5.583063027319029e+001, 6.976182015671301e+001, -5.580586484538701e+001, 2.790992817728934e+001, -7.978779120568047e+000, 9.982268549454829e-001}
                },
                new double[][]{
                        new double[]{3.159391888060492e-003, -2.524354290957952e-002, 8.827353735018950e-002, -1.764525332301130e-001, 2.205262938030033e-001, -1.764525332301129e-001, 8.827353735018949e-002, -2.524354290957952e-002, 3.159391888060493e-003},
                        new double[]{1.000000000000000e+000, -7.988221087768674e+000, 2.792749873579812e+001, -5.581226204234034e+001, 6.973660423984350e+001, -5.578603296354181e+001, 2.790125574202765e+001, -7.976964141331972e+000, 9.981215173509664e-001}
                },
                new double[][]{
                        new double[]{3.159224809745750e-003, -2.523834025436573e-002, 8.824569355028043e-002, -1.763853085942930e-001, 2.204374609774531e-001, -1.763853085942930e-001, 8.824569355028043e-002, -2.523834025436573e-002, 3.159224809745750e-003},
                        new double[]{1.000000000000000e+000, -7.986897507880478e+000, 2.791945785177781e+001, -5.579179014827696e+001, 6.970854237861690e+001, -5.576401198801909e+001, 2.789166315009525e+001, -7.974973664439123e+000, 9.980099281851373e-001}
                },
                new double[][]{
                        new double[]{3.159048406736869e-003, -2.523259015134536e-002, 8.821475949609868e-002, -1.763105007433883e-001, 2.203385659840945e-001, -1.763105007433883e-001, 8.821475949609867e-002, -2.523259015134535e-002, 3.159048406736868e-003},
                        new double[]{1.000000000000000e+000, -7.985418985660877e+000, 2.791048367061966e+001, -5.576896678748984e+001, 6.967730217807400e+001, -5.573954932622059e+001, 2.788104654658634e+001, -7.972789013008361e+000, 9.978917171939963e-001}
                },
                new double[][]{
                        new double[]{3.158862199178640e-003, -2.522623098262760e-002, 8.818037713864389e-002, -1.762272243510122e-001, 2.202284319921079e-001, -1.762272243510122e-001, 8.818037713864388e-002, -2.522623098262760e-002, 3.158862199178640e-003},
                        new double[]{1.000000000000000e+000, -7.983766989907910e+000, 2.790046535131966e+001, -5.574351468126776e+001, 6.964251192718245e+001, -5.571236267888894e+001, 2.786928997698394e+001, -7.970389397560299e+000, 9.977664922885320e-001}
                },
                new double[][]{
                        new double[]{3.158665687797979e-003, -2.521919392725215e-002, 8.814214613378270e-002, -1.761344894215090e-001, 2.201057430551121e-001, -1.761344894215089e-001, 8.814214613378270e-002, -2.521919392725215e-002, 3.158665687797979e-003},
                        new double[]{1.000000000000000e+000, -7.981920750933047e+000, 2.788927878383032e+001, -5.571512359047303e+001, 6.960375597479234e+001, -5.568213655092985e+001, 2.785626396494602e+001, -7.967751669263686e+000, 9.976338382685047e-001}
                },
                new double[][]{
                        new double[]{3.158458353861474e-003, -2.521140210409663e-002, 8.809961883926078e-002, -1.760311889888082e-001, 2.199690278007572e-001, -1.760311889888082e-001, 8.809961883926079e-002, -2.521140210409664e-002, 3.158458353861475e-003},
                        new double[]{1.000000000000000e+000, -7.979856989390820e+000, 2.787678500741772e+001, -5.568344642928296e+001, 6.956056957789367e+001, -5.564851836276790e+001, 2.784182392506172e+001, -7.964850044027480e+000, 9.974933154730581e-001}
                },
                new double[][]{
                        new double[]{3.158239659265769e-003, -2.520276961238962e-002, 8.805229473062350e-002, -1.759160854136171e-001, 2.198166412741269e-001, -1.759160854136170e-001, 8.805229473062347e-002, -2.520276961238962e-002, 3.158239659265769e-003},
                        new double[]{1.000000000000000e+000, -7.977549612358271e+000, 2.786282844431199e+001, -5.564809493498185e+001, 6.951243316672097e+001, -5.561111411973872e+001, 2.782580839230226e+001, -7.961655794012175e+000, 9.973444583541183e-001}
                },
                new double[][]{
                        new double[]{3.158009046785217e-003, -2.519320045770075e-002, 8.799961417207584e-002, -1.757877951322226e-001, 2.196467447451271e-001, -1.757877951322225e-001, 8.799961417207582e-002, -2.519320045770074e-002, 3.158009046785216e-003},
                        new double[]{1.000000000000000e+000, -7.974969372752160e+000, 2.784723492820081e+001, -5.560863484718004e+001, 6.945876596673548e+001, -5.556948359339760e+001, 2.780803704811647e+001, -7.958136902741812e+000, 9.971867739683715e-001}
                },
                new double[][]{
                        new double[]{3.157765940504518e-003, -2.518258734987953e-002, 8.794095147224382e-002, -1.756447716983805e-001, 2.194572832757883e-001, -1.756447716983806e-001, 8.794095147224382e-002, -2.518258734987952e-002, 3.157765940504517e-003},
                        new double[]{1.000000000000000e+000, -7.972083487716799e+000, 2.782980950515790e+001, -5.556458054606449e+001, 6.939891891301646e+001, -5.552313496494917e+001, 2.778830852124351e+001, -7.954257679564011e+000, 9.970197403833869e-001}
                },
                new double[][]{
                        new double[]{3.157509746467995e-003, -2.517081035786543e-002, 8.787560714843677e-002, -1.754852869480970e-001, 2.192459608296765e-001, -1.754852869480970e-001, 8.787560714843674e-002, -2.517081035786543e-002, 3.157509746467995e-003},
                        new double[]{1.000000000000000e+000, -7.968855211110624e+000, 2.781033398253884e+001, -5.551538909553598e+001, 6.933216678824905e+001, -5.547151887736864e+001, 2.776639793935085e+001, -7.949978328725670e+000, 9.968428049932395e-001}
                },
                new double[][]{
                        new double[]{3.157239853581603e-003, -2.515773540457894e-002, 8.780279931642641e-002, -1.753074101059224e-001, 2.190102126929847e-001, -1.753074101059224e-001, 8.780279931642640e-002, -2.515773540457894e-002, 3.157239853581603e-003},
                        new double[]{1.000000000000000e+000, -7.965243354660791e+000, 2.778856419923966e+001, -5.546045363348811e+001, 6.925769951150495e+001, -5.541402183931861e+001, 2.774205420558135e+001, -7.945254467803112e+000, 9.966553827387890e-001}
                },
                new double[][]{
                        new double[]{3.156955634808492e-003, -2.514321258320968e-002, 8.772165411605427e-002, -1.751089846412184e-001, 2.187471749661684e-001, -1.751089846412184e-001, 8.772165411605427e-002, -2.514321258320967e-002, 3.156955634808492e-003},
                        new double[]{1.000000000000000e+000, -7.961201751737143e+000, 2.776422698853192e+001, -5.539909604819387e+001, 6.917461250162503e+001, -5.534995892083294e+001, 2.771499697204265e+001, -7.940036589642881e+000, 9.964568542275056e-001}
                },
                new double[][]{
                        new double[]{3.156656448704776e-003, -2.512707427413613e-002, 8.763119507633681e-002, -1.748876026744579e-001, 2.184536508773114e-001, -1.748876026744579e-001, 8.763119507633681e-002, -2.512707427413613e-002, 3.156656448704775e-003},
                        new double[]{1.000000000000000e+000, -7.956678657015536e+000, 2.773701680251237e+001, -5.533055887701768e+001, 6.908189603657752e+001, -5.527856567817344e+001, 2.768491328025209e+001, -7.934269461330747e+000, 9.962465637475458e-001}
                },
                new double[][]{
                        new double[]{3.156341641348188e-003, -2.510913303942006e-002, 8.753033131735499e-002, -1.746405767278798e-001, 2.181260736651187e-001, -1.746405767278799e-001, 8.753033131735499e-002, -2.510913303942007e-002, 3.156341641348189e-003},
                        new double[]{1.000000000000000e+000, -7.951616074550573e+000, 2.770659196510449e+001, -5.525399636175364e+001, 6.897842352911954e+001, -5.519898924355911e+001, 2.765145383663062e+001, -7.927891453006585e+000, 9.960238171705083e-001}
                },
                new double[][]{
                        new double[]{3.156010548719496e-003, -2.508917926931127e-002, 8.741784448041817e-002, -1.743649086130051e-001, 2.177604657823905e-001, -1.743649086130051e-001, 8.741784448041816e-002, -2.508917926931127e-002, 3.156010548719496e-003},
                        new double[]{1.000000000000000e+000, -7.945949005955032e+000, 2.767257051861824e+001, -5.516846459419217e+001, 6.886293864000967e+001, -5.511027851501318e+001, 2.761422888941914e+001, -7.920833788580041e+000, 9.957878797370805e-001}
                },
                new double[][]{
                        new double[]{3.155662499604399e-003, -2.506697855245887e-002, 8.729237427317103e-002, -1.740572552511763e-001, 2.173523941823380e-001, -1.740572552511763e-001, 8.729237427317105e-002, -2.506697855245887e-002, 3.155662499604400e-003},
                        new double[]{1.000000000000000e+000, -7.939604609483376e+000, 2.763452562724007e+001, -5.507291068653348e+001, 6.873404115363863e+001, -5.501137338286942e+001, 2.757280367200363e+001, -7.913019709572767e+000, 9.955379737195335e-001}
                },
                new double[][]{
                        new double[]{3.155296819092648e-003, -2.504226873854748e-002, 8.715240251302218e-002, -1.737138912344610e-001, 2.168969214731223e-001, -1.737138912344610e-001, 8.715240251302220e-002, -2.504226873854748e-002, 3.155296819092648e-003},
                        new double[]{1.000000000000000e+000, -7.932501259836564e+000, 2.759198049965839e+001, -5.496616090463505e+001, 6.859017154819686e+001, -5.490109293316875e+001, 2.752669337675260e+001, -7.904363542416965e+000, 9.952732759547681e-001}
                },
                new double[][]{
                        new double[]{3.154912832761072e-003, -2.501475665885260e-002, 8.699623555120342e-002, -1.733306679554957e-001, 2.163885527642557e-001, -1.733306679554957e-001, 8.699623555120342e-002, -2.501475665885260e-002, 3.154912832761072e-003},
                        new double[]{1.000000000000000e+000, -7.924547497442289e+000, 2.754440279254992e+001, -5.484690770861077e+001, 6.842959420455813e+001, -5.477812257507029e+001, 2.747535762332123e+001, -7.894769658576227e+000, 9.949929152413864e-001}
                },
                new double[][]{
                        new double[]{3.154509871638553e-003, -2.498411446674099e-002, 8.682198496185380e-002, -1.729029691694235e-001, 2.158211780866830e-001, -1.729029691694236e-001, 8.682198496185381e-002, -2.498411446674100e-002, 3.154509871638554e-003},
                        new double[]{1.000000000000000e+000, -7.915640854818626e+000, 2.749119845715203e+001, -5.471369565601653e+001, 6.825037921640217e+001, -5.464100005052808e+001, 2.741819438625329e+001, -7.884131315825985e+000, 9.946959695939834e-001}
                },
                new double[][]{
                        new double[]{3.154087278063668e-003, -2.494997555643428e-002, 8.662754638693286e-002, -1.724256629030175e-001, 2.151880103522501e-001, -1.724256629030176e-001, 8.662754638693286e-002, -2.494997555643428e-002, 3.154087278063668e-003},
                        new double[]{1.000000000000000e+000, -7.905666546401251e+000, 2.743170499299610e+001, -5.456490613904224e+001, 6.805038279056032e+001, -5.448810030112487e+001, 2.735453334899452e+001, -7.872329367941698e+000, 9.943814633476313e-001}
                },
                new double[][]{
                        new double[]{3.153644412559933e-003, -2.491193001442541e-002, 8.641057644002366e-002, -1.718930497004701e-001, 2.144815189350482e-001, -1.718930497004701e-001, 8.641057644002366e-002, -2.491193001442541e-002, 3.153644412559932e-003},
                        new double[]{1.000000000000000e+000, -7.894496006909852e+000, 2.736518407655722e+001, -5.439874095038135e+001, 6.782722626340617e+001, -5.431761919070840e+001, 2.728362865560162e+001, -7.859230828901897e+000, 9.940483641052797e-001}
                },
                new double[][]{
                        new double[]{3.153180661869607e-003, -2.486951955381822e-002, 8.616846759211466e-002, -1.712988072983833e-001, 2.136933591155672e-001, -1.712988072983833e-001, 8.616846759211465e-002, -2.486951955381822e-002, 3.153180661869607e-003},
                        new double[]{1.000000000000000e+000, -7.881985261957821e+000, 2.729081353869786e+001, -5.421320470473966e+001, 6.757827380910089e+001, -5.412755611530962e+001, 2.720465103810294e+001, -7.844687276533637e+000, 9.936955795205367e-001}
                },
                new double[][]{
                        new double[]{3.152695448305012e-003, -2.482223187759939e-002, 8.589832099269115e-002, -1.706359319618829e-001, 2.128142978393528e-001, -1.706359319618829e-001, 8.589832099269117e-002, -2.482223187759939e-002, 3.152695448305013e-003},
                        new double[]{1.000000000000000e+000, -7.867973113185387e+000, 2.720767867411994e+001, -5.400608618669720e+001, 6.730060898200142e+001, -5.391569557615249e+001, 2.711667930741871e+001, -7.828533079330270e+000, 9.933219539081104e-001}
                },
                new double[][]{
                        new double[]{3.152188240596396e-003, -2.476949441252039e-002, 8.559691722298893e-002, -1.698966768987765e-001, 2.118341365177477e-001, -1.698966768987765e-001, 8.559691722298890e-002, -2.476949441252038e-002, 3.152188240596395e-003},
                        new double[]{1.000000000000000e+000, -7.852279118743454e+000, 2.711476287956804e+001, -5.377493875379191e+001, 6.699101032259173e+001, -5.367958785031539e+001, 2.701869120994906e+001, -7.810583428982896e+000, 9.929262646739174e-001}
                },
                new double[][]{
                        new double[]{3.151658566438113e-003, -2.471066735097116e-002, 8.526068503891726e-002, -1.690724884104834e-001, 2.107416319545301e-001, -1.690724884104833e-001, 8.526068503891726e-002, -2.471066735097116e-002, 3.151658566438114e-003},
                        new double[]{1.000000000000000e+000, -7.834701348506457e+000, 2.701093763646996e+001, -5.351705999979735e+001, 6.664592636870657e+001, -5.341652897030031e+001, 2.690955367161049e+001, -7.790632160028131e+000, 9.925072185567274e-001}
                },
                new double[][]{
                        new double[]{3.151106026960063e-003, -2.464503593410573e-002, 8.488566824372117e-002, -1.681539407503443e-001, 2.095244169366758e-001, -1.681539407503443e-001, 8.488566824372115e-002, -2.464503593410573e-002, 3.151106026960062e-003},
                        new double[]{1.000000000000000e+000, -7.815013891993500e+000, 2.689495187958396e+001, -5.322947098143593e+001, 6.626145055721739e+001, -5.312354032258413e+001, 2.678801247775242e+001, -7.768449336977040e+000, 9.920634476729724e-001}
                },
                new double[][]{
                        new double[]{3.150530313380003e-003, -2.457180190573428e-002, 8.446749094086682e-002, -1.671306710575339e-001, 2.081689226006989e-001, -1.671306710575339e-001, 8.446749094086682e-002, -2.457180190573428e-002, 3.150530313380004e-003},
                        new double[]{1.000000000000000e+000, -7.792964095691631e+000, 2.676542082793782e+001, -5.290889543716165e+001, 6.583329668224586e+001, -5.279734830107660e+001, 2.665268147288917e+001, -7.743778588425860e+000, 9.915935053562469e-001}
                },
                new double[][]{
                        new double[]{3.149931226123974e-003, -2.449007406337997e-002, 8.400132156317211e-002, -1.659913162360803e-001, 2.066603054012899e-001, -1.659913162360803e-001, 8.400132156317211e-002, -2.449007406337997e-002, 3.149931226123974e-003},
                        new double[]{1.000000000000000e+000, -7.768269505387938e+000, 2.662081440022254e+001, -5.255173958488378e+001, 6.535677580170375e+001, -5.243436460993682e+001, 2.650203141082893e+001, -7.716334167049936e+000, 9.910958617828615e-001}
                },
                new double[][]{
                        new double[]{3.149308696737955e-003, -2.439885783076391e-002, 8.348183626380033e-002, -1.647234542738494e-001, 2.049823823919444e-001, -1.647234542738494e-001, 8.348183626380033e-002, -2.439885783076391e-002, 3.149308696737955e-003},
                        new double[]{1.000000000000000e+000, -7.740614488341203e+000, 2.645944539669780e+001, -5.215407328305949e+001, 6.482677576262216e+001, -5.203066800777763e+001, 2.633437864637221e+001, -7.685797714171228e+000, 9.905688993747558e-001}
                },
                new double[][]{
                        new double[]{3.148662812953755e-003, -2.429704377527410e-002, 8.290318249918893e-002, -1.633135532680249e-001, 2.031175796050522e-001, -1.633135532680249e-001, 8.290318249918893e-002, -2.429704377527410e-002, 3.148662812953756e-003},
                        new double[]{1.000000000000000e+000, -7.709646509807524e+000, 2.627945770704299e+001, -5.171161358357100e+001, 6.423774485589274e+001, -5.158198852888927e+001, 2.614787393767223e+001, -7.651814707922908e+000, 9.900109079710157e-001}
                },
                new double[][]{
                        new double[]{3.147993847316972e-003, -2.418339499528223e-002, 8.225894394631153e-002, -1.617469323651304e-001, 2.010468996187548e-001, -1.617469323651304e-001, 8.225894394631153e-002, -2.418339499528222e-002, 3.147993847316972e-003},
                        new double[]{1.000000000000000e+000, -7.674972038770115e+000, 2.607881490262433e+001, -5.121971200272804e+001, 6.358368152129840e+001, -5.108369551406376e+001, 2.594049172766094e+001, -7.613990575114853e+000, 9.894200797592629e-001}
                },
                new double[][]{
                        new double[]{3.147302289834950e-003, -2.405653330624902e-002, 8.154210829236015e-002, -1.600077399582097e-001, 1.987499159446394e-001, -1.600077399582097e-001, 8.154210829236014e-002, -2.405653330624902e-002, 3.147302289834948e-003},
                        new double[]{1.000000000000000e+000, -7.636152058960868e+000, 2.585528969732743e+001, -5.067334719604580e+001, 6.285813251191995e+001, -5.053079114110794e+001, 2.571002039846987e+001, -7.571886448988433e+000, 9.887945039582496e-001}
                },
                new double[][]{
                        new double[]{3.146588885158769e-003, -2.391492416236799e-002, 8.074503993219968e-002, -1.580789558318979e-001, 1.962048036828320e-001, -1.580789558318979e-001, 8.074503993219971e-002, -2.391492416236799e-002, 3.146588885158770e-003},
                        new double[]{1.000000000000000e+000, -7.592697163719579e+000, 2.560645491929913e+001, -5.006712514976279e+001, 6.205420249911512e+001, -4.991791156951130e+001, 2.545405415016516e+001, -7.525014558465190e+000, 9.881321612431455e-001}
                },
                new double[][]{
                        new double[]{3.145854674876141e-003, -2.375686026326679e-002, 7.985946022891161e-002, -1.559424255217798e-001, 1.933884179764724e-001, -1.559424255217798e-001, 7.985946022891161e-002, -2.375686026326679e-002, 3.145854674876141e-003},
                        new double[]{1.000000000000000e+000, -7.544062217323155e+000, 2.532967683347243e+001, -4.939528950196758e+001, 6.116457875756176e+001, -4.923933830918797e+001, 2.516998735089173e+001, -7.472833239653871e+000, 9.874309179052381e-001}
                },
                new double[][]{
                        new double[]{3.145101045562206e-003, -2.358044381456427e-002, 7.887643875940420e-002, -1.535789369592204e-001, 1.902764341435763e-001, -1.535789369592204e-001, 7.887643875940420e-002, -2.358044381456427e-002, 3.145101045562206e-003},
                        new double[]{1.000000000000000e+000, -7.489640571648333e+000, 2.502211189939137e+001, -4.865174517941539e+001, 6.018157531047837e+001, -4.848902298999204e+001, 2.485501244705817e+001, -7.414741567800956e+000, 9.866885197380821e-001}
                },
                new double[][]{
                        new double[]{3.144329783313850e-003, -2.338356743882253e-002, 7.778639990704111e-002, -1.509683514875423e-001, 1.868435659282269e-001, -1.509683514875423e-001, 7.778639990704111e-002, -2.338356743882253e-002, 3.144329783313850e-003},
                        new double[]{1.000000000000000e+000, -7.428757836057749e+000, 2.468070834925526e+001, -4.783009917679500e+001, 5.909720172464564e+001, -4.766062934041341e+001, 2.450612281760468e+001, -7.350073618197181e+000, 9.859025856426020e-001}
                },
                new double[][]{
                        new double[]{3.143543135581006e-003, -2.316389377207868e-002, 7.657915030499854e-002, -1.480898035077080e-001, 1.830638810073308e-001, -1.480898035077080e-001, 7.657915030499855e-002, -2.316389377207868e-002, 3.143543135581006e-003},
                        new double[]{1.000000000000000e+000, -7.360665211036957e+000, 2.430221433655941e+001, -4.692372299709454e+001, 5.790326259567788e+001, -4.674759686361156e+001, 2.412012231428596e+001, -7.278092378587070e+000, 9.850706009441995e-001}
                },
                new double[][]{
                        new double[]{3.142743881206541e-003, -2.291883383372955e-002, 7.524393400650334e-002, -1.449219852393075e-001, 1.789112355627868e-001, -1.449219852393075e-001, 7.524393400650334e-002, -2.291883383372955e-002, 3.142743881206540e-003},
                        new double[]{1.000000000000000e+000, -7.284532413376133e+000, 2.388319484571587e+001, -4.592584198278260e+001, 5.659149460855977e+001, -4.574323139402281e+001, 2.369364365886558e+001, -7.197983354359358e+000, 9.841899104156673e-001}
                },
                new double[][]{
                        new double[]{3.141935409696230e-003, -2.264552432784142e-002, 7.376952388809109e-002, -1.414435351904935e-001, 1.743598521445707e-001, -1.414435351904935e-001, 7.376952388809108e-002, -2.264552432784141e-002, 3.141935409696229e-003},
                        new double[]{1.000000000000000e+000, -7.199440243846447e+000, 2.342006007650916e+001, -4.482965744266117e+001, 5.515374882202251e+001, -4.464082837240964e+001, 2.322317837594224e+001, -7.108847932436513e+000, 9.832577110005875e-001}
                },
                new double[][]{
                        new double[]{3.141121810862988e-003, -2.234080412659672e-002, 7.214435970035693e-002, -1.376335506400123e-001, 1.693850668579320e-001, -1.376335506400123e-001, 7.214435970035690e-002, -2.234080412659671e-002, 3.141121810862987e-003},
                        new double[]{1.000000000000000e+000, -7.104372878921088e+000, 2.290910863125306e+001, -4.362850803060598e+001, 5.358222639763521e+001, -4.343383520301292e+001, 2.270512153246824e+001, -7.009696601809882e+000, 9.822710442329992e-001}
                },
                new double[][]{
                        new double[]{3.140307976126783e-003, -2.200119030724153e-002, 7.035674537599862e-002, -1.334722453248443e-001, 1.639642726170729e-001, -1.334722453248443e-001, 7.035674537599862e-002, -2.200119030724154e-002, 3.140307976126782e-003},
                        new double[]{1.000000000000000e+000, -6.998210008037751e+000, 2.234658954035024e+001, -4.231607712696974e+001, 5.186977622103262e+001, -4.211605932017192e+001, 2.213583523342565e+001, -6.899442169873026e+000, 9.812267883503994e-001}
                },
                new double[][]{
                        new double[]{3.139499712905021e-003, -2.162285426944518e-002, 6.839512067458584e-002, -1.289417729867781e-001, 1.580780841620924e-001, -1.289417729867781e-001, 6.839512067458588e-002, -2.162285426944519e-002, 3.139499712905022e-003},
                        new double[]{1.000000000000000e+000, -6.879718989504162e+000, 2.172878796019500e+001, -4.088665281760313e+001, 5.001026253686516e+001, -4.068192840019685e+001, 2.151173558210775e+001, -6.776893166237469e+000, 9.801216500987595e-001}
                },
                new double[][]{
                        new double[]{3.138703873699729e-003, -2.120159865872217e-002, 6.624842494020337e-002, -1.240272346018628e-001, 1.517117468996463e-001, -1.240272346018628e-001, 6.624842494020337e-002, -2.120159865872217e-002, 3.138703873699729e-003},
                        new double[]{1.000000000000000e+000, -6.747547264196246e+000, 2.105214025163815e+001, -3.933544618694712e+001, 4.799900957772635e+001, -3.912680823655984e+001, 2.082940863624978e+001, -6.640747692124121e+000, 9.789521562300233e-001}
                },
                new double[][]{
                        new double[]{3.137928501679498e-003, -2.073283607339804e-002, 6.390657357142512e-002, -1.187178808303045e-001, 1.448568043951183e-001, -1.187178808303045e-001, 6.390657357142512e-002, -2.073283607339804e-002, 3.137928501679498e-003},
                        new double[]{1.000000000000000e+000, -6.600215350011449e+000, 2.031338506624802e+001, -3.765897166957116e+001, 4.583332789420625e+001, -3.744738178488232e+001, 2.008576174391972e+001, -6.489588056709191e+000, 9.777146446947530e-001}
                },
                new double[][]{
                        new double[]{3.137182994768761e-003, -2.021157084921369e-002, 6.136107057341432e-002, -1.130085099447135e-001, 1.375130273952083e-001, -1.130085099447135e-001, 6.136107057341432e-002, -2.021157084921369e-002, 3.137182994768761e-003},
                        new double[]{1.000000000000000e+000, -6.436110845532803e+000, 1.950975797479131e+001, -3.585548965015296e+001, 4.351312333645753e+001, -3.564208929582135e+001, 1.927821746633142e+001, -6.321876645437641e+000, 9.764052555351180e-001}
                },
                new double[][]{
                        new double[]{3.136478290496715e-003, -1.963238561066010e-002, 5.860578298576107e-002, -1.069010432752794e-001, 1.296905894905115e-001, -1.069010432752794e-001, 5.860578298576107e-002, -1.963238561066010e-002, 3.136478290496715e-003},
                        new double[]{1.000000000000000e+000, -6.253484003079926e+000, 1.863923797465455e+001, -3.392550574495158e+001, 4.104158405102899e+001, -3.371162367818436e+001, 1.840495799303931e+001, -6.135953593248776e+000, 9.750199214863040e-001}
                },
                new double[][]{
                        new double[]{3.135827074126856e-003, -1.898943476709113e-002, 5.563790460357721e-002, -1.004062326978215e-001, 1.214124500942118e-001, -1.004062326978215e-001, 5.563790460357720e-002, -1.898943476709113e-002, 3.135827074126855e-003},
                        new double[]{1.000000000000000e+000, -6.050445594454034e+000, 1.770085476591669e+001, -3.187231248663630e+001, 3.842593317714272e+001, -3.165946653481059e+001, 1.746522838225852e+001, -5.930036992274631e+000, 9.735543582978614e-001}
                },
                new double[][]{
                        new double[]{3.135244012888754e-003, -1.827644773199041e-002, 5.245913656145099e-002, -9.354541521470155e-002, 1.127168741389934e-001, -9.354541521470155e-002, 5.245913656145100e-002, -1.827644773199041e-002, 3.135244012888755e-003},
                        new double[]{1.000000000000000e+000, -5.824967992989349e+000, 1.669506577467719e+001, -2.970254661371651e+001, 3.567822510278451e+001, -2.949243785701341e+001, 1.645970690065039e+001, -5.702226553585582e+000, 9.720040547903410e-001}
                },
                new double[][]{
                        new double[]{3.134746019470630e-003, -1.748674537436416e-002, 4.907711006745098e-002, -8.635217549983100e-002, 1.036599808864124e-001, -8.635217549983101e-002, 4.907711006745098e-002, -1.748674537436416e-002, 3.134746019470632e-003},
                        new double[]{1.000000000000000e+000, -5.574890639196458e+000, 1.562421121838405e+001, -2.742671795429572e+001, 3.281615152807541e+001, -2.722121529947888e+001, 1.539094988747847e+001, -5.450511870800915e+000, 9.703642626670224e-001}
                },
                new double[][]{
                        new double[]{3.134352548309350e-003, -1.661327409169017e-002, 4.550707051926735e-002, -7.887370609052574e-002, 9.431817474222426e-002, -7.887370609052574e-002, 4.550707051926735e-002, -1.661327409169017e-002, 3.134352548309349e-003},
                        new double[]{1.000000000000000e+000, -5.297931350649583e+000, 1.449305362107864e+001, -2.505964324185187e+001, 2.986381121167740e+001, -2.486075657853854e+001, 1.426391650604211e+001, -5.172786704431734e+000, 9.686299861056348e-001}
                },
                new double[][]{
                        new double[]{3.134085928638541e-003, -1.564866294308468e-002, 4.177383060073083e-002, -7.117156573103167e-002, 8.479027540628280e-002, -7.117156573103167e-002, 4.177383060073084e-002, -1.564866294308468e-002, 3.134085928638541e-003},
                        new double[]{1.000000000000000e+000, -4.991705285766049e+000, 1.330940451460967e+001, -2.262068977353928e+001, 2.685238618316034e+001, -2.243053056001659e+001, 1.308657490580105e+001, -4.866871023409204e+000, 9.667959711606842e-001}
                },
                new double[][]{
                        new double[]{3.133971738732764e-003, -1.458531051532196e-002, 3.791398038204636e-002, -6.332143101523267e-002, 7.519914376860126e-002, -6.332143101523267e-002, 3.791398038204637e-002, -1.458531051532196e-002, 3.133971738732764e-003},
                        new double[]{1.000000000000000e+000, -4.653753780459771e+000, 1.208483484175470e+001, -2.013370016357715e+001, 2.382066078021547e+001, -1.995442963213826e+001, 1.187058496830032e+001, -4.530542906340166e+000, 9.648566950135764e-001}
                },
                new double[][]{
                        new double[]{3.134039226322169e-003, -1.341550962251499e-002, 3.397831195038829e-002, -5.541132177031712e-002, 6.569260943266918e-002, -5.541132177031712e-002, 3.397831195038829e-002, -1.341550962251499e-002, 3.134039226322169e-003},
                        new double[]{1.000000000000000e+000, -4.281585748495832e+000, 1.083545584338935e+001, -1.762643268940588e+001, 2.081532308871231e+001, -1.746020018575418e+001, 1.063204303178750e+001, -4.161582818466576e+000, 9.628063551150658e-001}
                },
                new double[][]{
                        new double[]{3.134321780757536e-003, -1.213161956026687e-002, 3.003437108339706e-002, -4.753767160414873e-002, 5.644356598658089e-002, -4.753767160414873e-002, 3.003437108339706e-002, -1.213161956026687e-002, 3.134321780757536e-003},
                        new double[]{1.000000000000000e+000, -3.872734868428338e+000, 9.582742796500897e+000, -1.512931661567665e+001, 1.789100765938704e+001, -1.497819422522095e+001, 9.392259689330880e+000, -3.757833234835759e+000, 9.606388582728883e-001}
                },
                new double[][]{
                        new double[]{3.134857463189034e-003, -1.072629742487928e-002, 2.616898486943642e-002, -3.979853915444665e-002, 4.764923335280726e-002, -3.979853915444665e-002, 2.616898486943642e-002, -1.072629742487929e-002, 3.134857463189036e-003},
                        new double[]{1.000000000000000e+000, -3.424836362665022e+000, 8.354353544174989e+000, -1.267329710343471e+001, 1.511008073912571e+001, -1.253922198523032e+001, 8.178521712285495e+000, -3.317277061692937e+000, 9.583478097467505e-001}
                },
                new double[][]{
                        new double[]{3.135689601795952e-003, -9.192801914856020e-003, 2.249052777972339e-002, -3.228325770744059e-002, 3.952980949329304e-002, -3.228325770744058e-002, 2.249052777972340e-002, -9.192801914856018e-003, 3.135689601795953e-003},
                        new double[]{1.000000000000000e+000, -2.935727786560088e+000, 7.184865948619916e+000, -1.028654413040799e+001, 1.254223993346572e+001, -1.017128706803972e+001, 7.024762141100510e+000, -2.838138790755699e+000, 9.559265024233198e-001}
                },
                new double[][]{
                        new double[]{3.136867459982495e-003, -7.525384956076330e-003, 1.913057593393776e-002, -2.505796833460762e-002, 3.232704353501763e-002, -2.505796833460763e-002, 1.913057593393775e-002, -7.525384956076330e-003, 3.136867459982495e-003},
                        new double[]{1.000000000000000e+000, -2.403578849731980e+000, 6.116322021836488e+000, -7.989845724149395e+000, 1.026409819970081e+001, -7.895033186180221e+000, 5.972027625440275e+000, -2.319012762106196e+000, 9.533679061555159e-001}
                },
                new double[][]{
                        new double[]{3.138446986453598e-003, -5.719788266387037e-003, 1.624445853637540e-002, -1.814686749013248e-002, 2.630361280892668e-002, -1.814686749013247e-002, 1.624445853637540e-002, -5.719788266387037e-003, 3.138446986453599e-003},
                        new double[]{1.000000000000000e+000, -1.827055825922347e+000, 5.198420980989066e+000, -5.790627461846007e+000, 8.359032593241114e+000, -5.717854195620133e+000, 5.068588868120159e+000, -1.759023244522344e+000, 9.506646573634056e-001}
                },
                new double[][]{
                        new double[]{3.140491657224234e-003, -3.773863336298222e-003, 1.401004915216966e-002, -1.150974045018174e-002, 2.174445408345166e-002, -1.150974045018174e-002, 1.401004915216966e-002, -3.773863336298222e-003, 3.140491657224234e-003},
                        new double[]{1.000000000000000e+000, -1.205526475294007e+000, 4.488149587584013e+000, -3.675774949486338e+000, 6.917662040320396e+000, -3.626853005615643e+000, 4.369489565598928e+000, -1.158021168308530e+000, 9.478090490084538e-001}
                },
                new double[][]{
                        new double[]{3.143073420920189e-003, -1.688333852644991e-003, 1.262395748797917e-002, -5.017573087974817e-003, 1.896114476128464e-002, -5.017573087974814e-003, 1.262395748797918e-002, -1.688333852644991e-003, 3.143073420920189e-003},
                        new double[]{1.000000000000000e+000, -5.393114608165109e-001, 4.048578996469637e+000, -1.603824537959889e+000, 6.039299478305786e+000, -1.581219217193778e+000, 3.935274955307953e+000, -5.168221229728396e-001, 9.447930210688751e-001}
                },
                new double[][]{
                        new double[]{3.146273760226500e-003, 5.322812464931028e-004, 1.229410658041627e-002, 1.570162041369520e-003, 1.829966777695044e-002, 1.570162041369518e-003, 1.229410658041627e-002, 5.322812464931027e-004, 3.146273760226500e-003},
                        new double[]{1.000000000000000e+000, 1.700122175818912e-001, 3.946500014377160e+000, 5.023368561645096e-001, 5.834479755741241e+000, 4.948389524665608e-001, 3.829590220830910e+000, 1.625105377327541e-001, 9.416081516614648e-001}
                },
                new double[][]{
                        new double[]{3.150184884067147e-003, 2.878568940453699e-003, 1.322755106586022e-002, 8.620552071421581e-003, 2.014990932533000e-002, 8.620552071421581e-003, 1.322755106586022e-002, 2.878568940453698e-003, 3.150184884067147e-003},
                        new double[]{1.000000000000000e+000, 9.192515080342589e-001, 4.248526154251194e+000, 2.760354359206612e+000, 6.428032354219372e+000, 2.716723422282723e+000, 4.115304532346304e+000, 8.763352262803313e-001, 9.382456489747480e-001}
                },
                new double[][]{
                        new double[]{3.154911067098878e-003, 5.334938740144927e-003, 1.561237564818607e-002, 1.662582664159495e-002, 2.495150819282767e-002, 1.662582664159496e-002, 1.561237564818607e-002, 5.334938740144928e-003, 3.154911067098878e-003},
                        new double[]{1.000000000000000e+000, 1.703179499277951e+000, 5.015287582907516e+000, 5.328060497664993e+000, 7.960981372090181e+000, 5.238878049409713e+000, 4.848819300199696e+000, 1.619054784052113e+000, 9.346963441995914e-001}
                },
                new double[][]{
                        new double[]{3.160570155425528e-003, 7.878157153029112e-003, 1.959270862731381e-002, 2.617401829906347e-002, 3.318498160501834e-002, 2.617401829906346e-002, 1.959270862731381e-002, 7.878157153029110e-003, 3.160570155425528e-003},
                        new double[]{1.000000000000000e+000, 2.514061860132557e+000, 6.293401312113454e+000, 8.394231003661758e+000, 1.058774518680232e+001, 8.245442893665601e+000, 6.072290240693297e+000, 2.382697143855915e+000, 9.309506856667303e-001}
                },
                new double[][]{
                        new double[]{3.167295260151064e-003, 1.047572033190279e-002, 2.523643163272042e-002, 3.787881035026627e-002, 4.533007101187137e-002, 3.787881035026630e-002, 2.523643163272043e-002, 1.047572033190280e-002, 3.167295260151064e-003},
                        new double[]{1.000000000000000e+000, 3.341136359577790e+000, 8.105078676150034e+000, 1.215629189647473e+001, 1.446288885419975e+001, 1.192812032225852e+001, 7.803674281773004e+000, 3.156465395399920e+000, 9.269987344260360e-001}
                },
                new double[][]{
                        new double[]{3.175236663568253e-003, 1.308411231056350e-002, 3.249623781842040e-002, 5.225727921334373e-002, 6.176741363408551e-002, 5.225727921334372e-002, 3.249623781842039e-002, 1.308411231056350e-002, 3.175236663568252e-003},
                        new double[]{1.000000000000000e+000, 4.170063916809950e+000, 1.043557693581984e+001, 1.678131188069654e+001, 1.970982407256151e+001, 1.644776743866162e+001, 1.002485619533124e+001, 3.926277990238661e+000, 9.228301615299103e-001}
                },
                new double[][]{
                        new double[]{3.184563966522064e-003, 1.564703528332045e-002, 4.116647822650334e-002, 6.955135546761322e-002, 8.260002997525118e-002, 6.955135546761322e-002, 4.116647822650334e-002, 1.564703528332045e-002, 3.184563966522064e-003},
                        new double[]{1.000000000000000e+000, 4.982383252986065e+000, 1.321928002312586e+001, 2.234885788249893e+001, 2.636396393156664e+001, 2.187848507504302e+001, 1.266867131040726e+001, 4.674332629971476e+000, 9.184342473129035e-001}
                },
                new double[][]{
                        new double[]{3.195468509911748e-003, 1.809375255989474e-002, 5.084090030653590e-002, 8.950422298444746e-002, 1.073856458036988e-001, 8.950422298444746e-002, 5.084090030653590e-002, 1.809375255989474e-002, 3.195468509911749e-003},
                        new double[]{1.000000000000000e+000, 5.755019760051603e+000, 1.632605611212203e+001, 2.877918880332384e+001, 3.428743475883073e+001, 2.813781188877264e+001, 1.560646217523939e+001, 5.378744289396370e+000, 9.137998829916308e-001}
                },
                new double[][]{
                        new double[]{3.208166108549520e-003, 2.033775720013014e-002, 6.087995852729569e-002, 1.111283766985333e-001, 1.348084233730102e-001, 1.111283766985333e-001, 6.087995852729569e-002, 2.033775720013014e-002, 3.208166108549520e-003},
                        new double[]{1.000000000000000e+000, 6.459923452852331e+000, 1.955069789760037e+001, 3.575841878007016e+001, 4.306434589484458e+001, 3.491460431644380e+001, 1.863888825236724e+001, 6.013329244005268e+000, 9.089155749432362e-001}
                },
                new double[][]{
                        new double[]{3.222900141858363e-003, 2.227607222988249e-002, 7.040061526828752e-002, 1.325362521214648e-001, 1.623833078994799e-001, 1.325362521214648e-001, 7.040061526828752e-002, 2.227607222988248e-002, 3.222900141858363e-003},
                        new double[]{1.000000000000000e+000, 7.063941201586084e+000, 2.260962332507320e+001, 4.268327468011343e+001, 5.190611606085580e+001, 4.161681749209127e+001, 2.149395668247144e+001, 6.547632809776026e+000, 9.037694520569338e-001}
                },
                new double[][]{
                        new double[]{3.239945053390167e-003, 2.378960212591439e-002, 7.830568646607240e-002, 1.509391118013942e-001, 1.863663521383706e-001, 1.509391118013942e-001, 7.830568646607242e-002, 2.378960212591439e-002, 3.239945053390167e-003},
                        new double[]{1.000000000000000e+000, 7.529064346833643e+000, 2.515035008465618e+001, 4.865934114163024e+001, 5.962181888765429e+001, 4.737213222877233e+001, 2.383740644607056e+001, 6.947327741252297e+000, 8.983492765915317e-001}
                },
                new double[][]{
                        new double[]{3.259610320154745e-003, 2.474508638385794e-002, 8.337226016441461e-002, 1.629367425557874e-001, 2.020999170550664e-001, 1.629367425557874e-001, 8.337226016441456e-002, 2.474508638385793e-002, 3.259610320154744e-003},
                        new double[]{1.000000000000000e+000, 7.813232181445080e+000, 2.678004704072203e+001, 5.259232996155323e+001, 6.472625974952639e+001, 5.111941179551023e+001, 2.530116502103031e+001, 7.175140285956257e+000, 8.926424590119200e-001}
                }
        };
    }

    private static class MIDI_COEFFICIENTS_SR2756_25HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.161183098536588e-003, -2.528502881900131e-002, 8.848651326161339e-002, -1.769597232514924e-001, 2.211941114206876e-001, -1.769597232514925e-001, 8.848651326161340e-002, -2.528502881900131e-002, 3.161183098536589e-003},
                        new double[]{1.000000000000000e+000, -7.997906392458485e+000, 2.798673576249600e+001, -5.596437815165698e+001, 6.994757878169354e+001, -5.595452512332000e+001, 2.797688200072444e+001, -7.993682827744629e+000, 9.992959502661386e-001}
                },
                new double[][]{
                        new double[]{3.161118243205082e-003, -2.528396685915505e-002, 8.848143921166912e-002, -1.769479470048061e-001, 2.211787128181739e-001, -1.769479470048060e-001, 8.848143921166909e-002, -2.528396685915505e-002, 3.161118243205083e-003},
                        new double[]{1.000000000000000e+000, -7.997694388703194e+000, 2.798542221751621e+001, -5.596094844546255e+001, 6.994271348247383e+001, -5.595051021993152e+001, 2.797498311983799e+001, -7.993219866638839e+000, 9.992541009080764e-001}
                },
                new double[][]{
                        new double[]{3.161049617161414e-003, -2.528280824636124e-002, 8.847586081485707e-002, -1.769349629156903e-001, 2.211617214600661e-001, -1.769349629156903e-001, 8.847586081485709e-002, -2.528280824636124e-002, 3.161049617161414e-003},
                        new double[]{1.000000000000000e+000, -7.997459063894291e+000, 2.798396633477872e+001, -5.595715433075780e+001, 6.993734501922353e+001, -5.594609622713119e+001, 2.797290719406510e+001, -7.992718691253814e+000, 9.992097649697828e-001}
                },
                new double[][]{
                        new double[]{3.160977006686676e-003, -2.528154313354447e-002, 8.846972330378967e-002, -1.769206371187328e-001, 2.211429598836020e-001, -1.769206371187328e-001, 8.846972330378969e-002, -2.528154313354447e-002, 3.160977006686676e-003},
                        new double[]{1.000000000000000e+000, -7.997197719639722e+000, 2.798235179397666e+001, -5.595295452153719e+001, 6.993141733555554e+001, -5.594123981696578e+001, 2.797063585619458e+001, -7.992175722409599e+000, 9.991627948255754e-001}
                },
                new double[][]{
                        new double[]{3.160900186638132e-003, -2.528016058671942e-002, 8.846296562866780e-002, -1.769048202182677e-001, 2.211222299793627e-001, -1.769048202182677e-001, 8.846296562866780e-002, -2.528016058671942e-002, 3.160900186638132e-003},
                        new double[]{1.000000000000000e+000, -7.996907336578110e+000, 2.798056034447828e+001, -5.594830288265121e+001, 6.992486785363307e+001, -5.593589269453279e+001, 2.796814868994899e+001, -7.991587008398287e+000, 9.991130341001322e-001}
                },
                new double[][]{
                        new double[]{3.160818919927040e-003, -2.527864845910844e-002, 8.845551971517662e-002, -1.768873454445385e-001, 2.210993105370871e-001, -1.768873454445385e-001, 8.845551971517662e-002, -2.527864845910844e-002, 3.160818919927039e-003},
                        new double[]{1.000000000000000e+000, -7.996584535662630e+000, 2.797857157354876e+001, -5.594314785027997e+001, 6.991762669901105e+001, -5.593000101210501e+001, 2.796542299168110e+001, -7.990948183344893e+000, 9.990603171517307e-001}
                },
                new double[][]{
                        new double[]{3.160732956984562e-003, -2.527699325030442e-002, 8.844730963360412e-002, -1.768680265891523e-001, 2.210739544977368e-001, -1.768680265891523e-001, 8.844730963360412e-002, -2.527699325030443e-002, 3.160732956984563e-003},
                        new double[]{1.000000000000000e+000, -7.996225534745101e+000, 2.797636264659958e+001, -5.593743178278633e+001, 6.990961583269734e+001, -5.592350471337751e+001, 2.796243350380717e+001, -7.990254420720088e+000, 9.990044685251582e-001}
                },
                new double[][]{
                        new double[]{3.160642035217503e-003, -2.527517994868275e-002, 8.843825066855632e-002, -1.768466556935119e-001, 2.210458858768428e-001, -1.768466556935119e-001, 8.843825066855633e-002, -2.527517994868275e-002, 3.160642035217504e-003},
                        new double[]{1.000000000000000e+000, -7.995826099888900e+000, 2.797390801609919e+001, -5.593109023362921e+001, 6.990074807938849e+001, -5.591633679952461e+001, 2.795915211661225e+001, -7.989500381429437e+000, 9.989453023725972e-001}
                },
                new double[][]{
                        new double[]{3.160545878455811e-003, -2.527319185504862e-002, 8.842824827738170e-002, -1.768230004608285e-001, 2.210147963200810e-001, -1.768230004608285e-001, 8.842824827738169e-002, -2.527319185504861e-002, 3.160545878455811e-003},
                        new double[]{1.000000000000000e+000, -7.995381490769717e+000, 2.797117909537814e+001, -5.592405113704149e+001, 6.989092603954595e+001, -5.590842250778280e+001, 2.795554753466443e+001, -7.988680155834519e+000, 9.988826218405983e-001}
                },
                new double[][]{
                        new double[]{3.160444196394361e-003, -2.527101038526481e-002, 8.841719692401759e-002, -1.767968013589008e-001, 2.209803412475103e-001, -1.767968013589008e-001, 8.841719692401759e-002, -2.527101038526481e-002, 3.160444196394361e-003},
                        new double[]{1.000000000000000e+000, -7.994886399445699e+000, 2.796814389311626e+001, -5.591623389609305e+001, 6.988004087155034e+001, -5.589967839219742e+001, 2.795158490363400e+001, -7.987787198984727e+000, 9.988162184212104e-001}
                },
                new double[][]{
                        new double[]{3.160336684031966e-003, -2.526861484933287e-002, 8.840497877340901e-002, -1.767677683770464e-001, 2.209421355378812e-001, -1.767677683770464e-001, 8.840497877340901e-002, -2.526861484933288e-002, 3.160336684031967e-003},
                        new double[]{1.000000000000000e+000, -7.994334881691234e+000, 2.796476660380225e+001, -5.590754836155735e+001, 6.986797092858842e+001, -5.589001129497501e+001, 2.794722539282159e+001, -7.986814258252410e+000, 9.987458712652103e-001}
                },
                new double[][]{
                        new double[]{3.160223021111152e-003, -2.526598220409768e-002, 8.839146222991390e-002, -1.767355773963661e-001, 2.208997486988848e-001, -1.767355773963661e-001, 8.839146222991388e-002, -2.526598220409768e-002, 3.160223021111152e-003},
                        new double[]{1.000000000000000e+000, -7.993720279991338e+000, 2.796100714890299e+001, -5.589789368866482e+001, 6.985458023317315e+001, -5.587931719555724e+001, 2.794242572815215e+001, -7.985753292467819e+000, 9.986713464552553e-001}
                },
                new double[][]{
                        new double[]{3.160102871562913e-003, -2.526308677640546e-002, 8.837650030117852e-002, -1.766998661278661e-001, 2.208526994630720e-001, -1.766998661278661e-001, 8.837650030117855e-002, -2.526308677640547e-002, 3.160102871562914e-003},
                        new double[]{1.000000000000000e+000, -7.993035137184169e+000, 2.795682066286448e+001, -5.588715705735085e+001, 6.983971677026145e+001, -5.586747992306563e+001, 2.793713766978453e+001, -7.984595381542750e+000, 9.985923962366661e-001}
                },
                new double[][]{
                        new double[]{3.159975882961421e-003, -2.525989995316554e-002, 8.835992876682405e-002, -1.766602295678194e-001, 2.208004497424176e-001, -1.766602295678194e-001, 8.835992876682405e-002, -2.525989995316553e-002, 3.159975882961421e-003},
                        new double[]{1.000000000000000e+000, -7.992271099617168e+000, 2.795215691738355e+001, -5.587521223997506e+001, 6.982321057779913e+001, -5.585436971614900e+001, 2.793130742781282e+001, -7.983330625451835e+000, 9.985087582034402e-001}
                },
                new double[][]{
                        new double[]{3.159841685994492e-003, -2.525638983434093e-002, 8.834156412890981e-002, -1.766162149140839e-001, 2.207423978670707e-001, -1.766162149140839e-001, 8.834156412890981e-002, -2.525638983434093e-002, 3.159841685994491e-003},
                        new double[]{1.000000000000000e+000, -7.991418808545359e+000, 2.794695967663140e+001, -5.586191799869053e+001, 6.980487161117404e+001, -5.583984161248014e+001, 2.792487500878889e+001, -7.981948031305878e+000, 9.984201544369321e-001}
                },
                new double[][]{
                        new double[]{3.159699893956621e-003, -2.525252084441813e-002, 8.832120131851101e-002, -1.765673158808659e-001, 2.206778710256798e-001, -1.765673158808659e-001, 8.832120131851101e-002, -2.525252084441813e-002, 3.159699893956621e-003},
                        new double[]{1.000000000000000e+000, -7.990467778347355e+000, 2.794116597527122e+001, -5.584711629266529e+001, 6.978448735548403e+001, -5.582373364819085e+001, 2.791777348497057e+001, -7.980435387101997e+000, 9.983262905945257e-001}
                },
                new double[][]{
                        new double[]{3.159550102272552e-003, -2.524825329737595e-002, 8.829861112984655e-002, -1.765129663425945e-001, 2.206061168157774e-001, -1.765129663425945e-001, 8.829861112984654e-002, -2.524825329737595e-002, 3.159550102272551e-003},
                        new double[]{1.000000000000000e+000, -7.989406259963432e+000, 2.793470531018452e+001, -5.583063027319029e+001, 6.976182015671301e+001, -5.580586484538701e+001, 2.790992817728934e+001, -7.978779120568047e+000, 9.982268549454829e-001}
                },
                new double[][]{
                        new double[]{3.159391888060492e-003, -2.524354290957952e-002, 8.827353735018950e-002, -1.764525332301130e-001, 2.205262938030033e-001, -1.764525332301129e-001, 8.827353735018949e-002, -2.524354290957952e-002, 3.159391888060493e-003},
                        new double[]{1.000000000000000e+000, -7.988221087768674e+000, 2.792749873579812e+001, -5.581226204234034e+001, 6.973660423984350e+001, -5.578603296354181e+001, 2.790125574202765e+001, -7.976964141331972e+000, 9.981215173509664e-001}
                },
                new double[][]{
                        new double[]{3.159224809745750e-003, -2.523834025436573e-002, 8.824569355028043e-002, -1.763853085942930e-001, 2.204374609774531e-001, -1.763853085942930e-001, 8.824569355028043e-002, -2.523834025436573e-002, 3.159224809745750e-003},
                        new double[]{1.000000000000000e+000, -7.986897507880478e+000, 2.791945785177781e+001, -5.579179014827696e+001, 6.970854237861690e+001, -5.576401198801909e+001, 2.789166315009525e+001, -7.974973664439123e+000, 9.980099281851373e-001}
                },
                new double[][]{
                        new double[]{3.159048406736869e-003, -2.523259015134536e-002, 8.821475949609869e-002, -1.763105007433883e-001, 2.203385659840945e-001, -1.763105007433883e-001, 8.821475949609869e-002, -2.523259015134536e-002, 3.159048406736869e-003},
                        new double[]{1.000000000000000e+000, -7.985418985660877e+000, 2.791048367061966e+001, -5.576896678748984e+001, 6.967730217807400e+001, -5.573954932622059e+001, 2.788104654658634e+001, -7.972789013008361e+000, 9.978917171939963e-001}
                },
                new double[][]{
                        new double[]{3.158862199178640e-003, -2.522623098262760e-002, 8.818037713864389e-002, -1.762272243510122e-001, 2.202284319921079e-001, -1.762272243510122e-001, 8.818037713864388e-002, -2.522623098262760e-002, 3.158862199178640e-003},
                        new double[]{1.000000000000000e+000, -7.983766989907910e+000, 2.790046535131966e+001, -5.574351468126776e+001, 6.964251192718245e+001, -5.571236267888894e+001, 2.786928997698394e+001, -7.970389397560299e+000, 9.977664922885320e-001}
                },
                new double[][]{
                        new double[]{3.158665687797979e-003, -2.521919392725215e-002, 8.814214613378270e-002, -1.761344894215090e-001, 2.201057430551121e-001, -1.761344894215089e-001, 8.814214613378270e-002, -2.521919392725215e-002, 3.158665687797979e-003},
                        new double[]{1.000000000000000e+000, -7.981920750933047e+000, 2.788927878383032e+001, -5.571512359047303e+001, 6.960375597479234e+001, -5.568213655092985e+001, 2.785626396494602e+001, -7.967751669263686e+000, 9.976338382685047e-001}
                },
                new double[][]{
                        new double[]{3.158458353861474e-003, -2.521140210409663e-002, 8.809961883926078e-002, -1.760311889888082e-001, 2.199690278007572e-001, -1.760311889888082e-001, 8.809961883926079e-002, -2.521140210409664e-002, 3.158458353861475e-003},
                        new double[]{1.000000000000000e+000, -7.979856989390820e+000, 2.787678500741772e+001, -5.568344642928296e+001, 6.956056957789367e+001, -5.564851836276790e+001, 2.784182392506172e+001, -7.964850044027480e+000, 9.974933154730581e-001}
                },
                new double[][]{
                        new double[]{3.158239659265769e-003, -2.520276961238962e-002, 8.805229473062350e-002, -1.759160854136171e-001, 2.198166412741269e-001, -1.759160854136170e-001, 8.805229473062347e-002, -2.520276961238962e-002, 3.158239659265769e-003},
                        new double[]{1.000000000000000e+000, -7.977549612358271e+000, 2.786282844431199e+001, -5.564809493498185e+001, 6.951243316672097e+001, -5.561111411973872e+001, 2.782580839230226e+001, -7.961655794012175e+000, 9.973444583541183e-001}
                },
                new double[][]{
                        new double[]{3.158009046785217e-003, -2.519320045770075e-002, 8.799961417207584e-002, -1.757877951322226e-001, 2.196467447451271e-001, -1.757877951322225e-001, 8.799961417207582e-002, -2.519320045770074e-002, 3.158009046785216e-003},
                        new double[]{1.000000000000000e+000, -7.974969372752160e+000, 2.784723492820081e+001, -5.560863484718004e+001, 6.945876596673548e+001, -5.556948359339760e+001, 2.780803704811647e+001, -7.958136902741812e+000, 9.971867739683715e-001}
                },
                new double[][]{
                        new double[]{3.157765940504518e-003, -2.518258734987953e-002, 8.794095147224382e-002, -1.756447716983805e-001, 2.194572832757883e-001, -1.756447716983806e-001, 8.794095147224382e-002, -2.518258734987952e-002, 3.157765940504517e-003},
                        new double[]{1.000000000000000e+000, -7.972083487716799e+000, 2.782980950515790e+001, -5.556458054606449e+001, 6.939891891301646e+001, -5.552313496494917e+001, 2.778830852124351e+001, -7.954257679564011e+000, 9.970197403833869e-001}
                },
                new double[][]{
                        new double[]{3.157509746467995e-003, -2.517081035786543e-002, 8.787560714843677e-002, -1.754852869480970e-001, 2.192459608296765e-001, -1.754852869480970e-001, 8.787560714843674e-002, -2.517081035786543e-002, 3.157509746467995e-003},
                        new double[]{1.000000000000000e+000, -7.968855211110624e+000, 2.781033398253884e+001, -5.551538909553598e+001, 6.933216678824905e+001, -5.547151887736864e+001, 2.776639793935085e+001, -7.949978328725670e+000, 9.968428049932395e-001}
                },
                new double[][]{
                        new double[]{3.157239853581603e-003, -2.515773540457894e-002, 8.780279931642641e-002, -1.753074101059224e-001, 2.190102126929847e-001, -1.753074101059224e-001, 8.780279931642640e-002, -2.515773540457894e-002, 3.157239853581603e-003},
                        new double[]{1.000000000000000e+000, -7.965243354660791e+000, 2.778856419923966e+001, -5.546045363348811e+001, 6.925769951150495e+001, -5.541402183931861e+001, 2.774205420558135e+001, -7.945254467803112e+000, 9.966553827387890e-001}
                },
                new double[][]{
                        new double[]{3.156955634808492e-003, -2.514321258320968e-002, 8.772165411605427e-002, -1.751089846412184e-001, 2.187471749661684e-001, -1.751089846412184e-001, 8.772165411605427e-002, -2.514321258320967e-002, 3.156955634808492e-003},
                        new double[]{1.000000000000000e+000, -7.961201751737143e+000, 2.776422698853192e+001, -5.539909604819387e+001, 6.917461250162503e+001, -5.534995892083294e+001, 2.771499697204265e+001, -7.940036589642881e+000, 9.964568542275056e-001}
                },
                new double[][]{
                        new double[]{3.156656448704776e-003, -2.512707427413613e-002, 8.763119507633681e-002, -1.748876026744579e-001, 2.184536508773114e-001, -1.748876026744579e-001, 8.763119507633681e-002, -2.512707427413613e-002, 3.156656448704775e-003},
                        new double[]{1.000000000000000e+000, -7.956678657015536e+000, 2.773701680251237e+001, -5.533055887701768e+001, 6.908189603657752e+001, -5.527856567817344e+001, 2.768491328025209e+001, -7.934269461330747e+000, 9.962465637475458e-001}
                },
                new double[][]{
                        new double[]{3.156341641348188e-003, -2.510913303942006e-002, 8.753033131735499e-002, -1.746405767278798e-001, 2.181260736651187e-001, -1.746405767278799e-001, 8.753033131735499e-002, -2.510913303942007e-002, 3.156341641348189e-003},
                        new double[]{1.000000000000000e+000, -7.951616074550573e+000, 2.770659196510449e+001, -5.525399636175364e+001, 6.897842352911954e+001, -5.519898924355911e+001, 2.765145383663062e+001, -7.927891453006585e+000, 9.960238171705083e-001}
                },
                new double[][]{
                        new double[]{3.156010548719496e-003, -2.508917926931127e-002, 8.741784448041817e-002, -1.743649086130051e-001, 2.177604657823905e-001, -1.743649086130051e-001, 8.741784448041816e-002, -2.508917926931127e-002, 3.156010548719496e-003},
                        new double[]{1.000000000000000e+000, -7.945949005955032e+000, 2.767257051861824e+001, -5.516846459419217e+001, 6.886293864000967e+001, -5.511027851501318e+001, 2.761422888941914e+001, -7.920833788580041e+000, 9.957878797370805e-001}
                },
                new double[][]{
                        new double[]{3.155662499604399e-003, -2.506697855245887e-002, 8.729237427317103e-002, -1.740572552511763e-001, 2.173523941823380e-001, -1.740572552511763e-001, 8.729237427317105e-002, -2.506697855245887e-002, 3.155662499604400e-003},
                        new double[]{1.000000000000000e+000, -7.939604609483376e+000, 2.763452562724007e+001, -5.507291068653348e+001, 6.873404115363863e+001, -5.501137338286942e+001, 2.757280367200363e+001, -7.913019709572767e+000, 9.955379737195335e-001}
                },
                new double[][]{
                        new double[]{3.155296819092648e-003, -2.504226873854748e-002, 8.715240251302218e-002, -1.737138912344610e-001, 2.168969214731223e-001, -1.737138912344610e-001, 8.715240251302220e-002, -2.504226873854748e-002, 3.155296819092648e-003},
                        new double[]{1.000000000000000e+000, -7.932501259836564e+000, 2.759198049965839e+001, -5.496616090463505e+001, 6.859017154819686e+001, -5.490109293316875e+001, 2.752669337675260e+001, -7.904363542416965e+000, 9.952732759547681e-001}
                },
                new double[][]{
                        new double[]{3.154912832761072e-003, -2.501475665885260e-002, 8.699623555120342e-002, -1.733306679554957e-001, 2.163885527642557e-001, -1.733306679554957e-001, 8.699623555120342e-002, -2.501475665885260e-002, 3.154912832761072e-003},
                        new double[]{1.000000000000000e+000, -7.924547497442289e+000, 2.754440279254992e+001, -5.484690770861077e+001, 6.842959420455813e+001, -5.477812257507029e+001, 2.747535762332123e+001, -7.894769658576227e+000, 9.949929152413864e-001}
                },
                new double[][]{
                        new double[]{3.154509871638553e-003, -2.498411446674099e-002, 8.682198496185380e-002, -1.729029691694235e-001, 2.158211780866830e-001, -1.729029691694236e-001, 8.682198496185380e-002, -2.498411446674099e-002, 3.154509871638554e-003},
                        new double[]{1.000000000000000e+000, -7.915640854818626e+000, 2.749119845715203e+001, -5.471369565601653e+001, 6.825037921640217e+001, -5.464100005052808e+001, 2.741819438625328e+001, -7.884131315825984e+000, 9.946959695939833e-001}
                },
                new double[][]{
                        new double[]{3.154087278063668e-003, -2.494997555643428e-002, 8.662754638693286e-002, -1.724256629030175e-001, 2.151880103522501e-001, -1.724256629030176e-001, 8.662754638693286e-002, -2.494997555643428e-002, 3.154087278063668e-003},
                        new double[]{1.000000000000000e+000, -7.905666546401251e+000, 2.743170499299610e+001, -5.456490613904224e+001, 6.805038279056032e+001, -5.448810030112487e+001, 2.735453334899452e+001, -7.872329367941698e+000, 9.943814633476313e-001}
                },
                new double[][]{
                        new double[]{3.153644412559933e-003, -2.491193001442541e-002, 8.641057644002366e-002, -1.718930497004701e-001, 2.144815189350482e-001, -1.718930497004701e-001, 8.641057644002366e-002, -2.491193001442541e-002, 3.153644412559932e-003},
                        new double[]{1.000000000000000e+000, -7.894496006909852e+000, 2.736518407655722e+001, -5.439874095038135e+001, 6.782722626340617e+001, -5.431761919070840e+001, 2.728362865560162e+001, -7.859230828901897e+000, 9.940483641052797e-001}
                },
                new double[][]{
                        new double[]{3.153180661869607e-003, -2.486951955381822e-002, 8.616846759211466e-002, -1.712988072983833e-001, 2.136933591155672e-001, -1.712988072983833e-001, 8.616846759211465e-002, -2.486951955381822e-002, 3.153180661869608e-003},
                        new double[]{1.000000000000000e+000, -7.881985261957821e+000, 2.729081353869786e+001, -5.421320470473966e+001, 6.757827380910089e+001, -5.412755611530962e+001, 2.720465103810294e+001, -7.844687276533637e+000, 9.936955795205367e-001}
                },
                new double[][]{
                        new double[]{3.152695448305012e-003, -2.482223187759939e-002, 8.589832099269115e-002, -1.706359319618829e-001, 2.128142978393528e-001, -1.706359319618829e-001, 8.589832099269117e-002, -2.482223187759939e-002, 3.152695448305013e-003},
                        new double[]{1.000000000000000e+000, -7.867973113185387e+000, 2.720767867411994e+001, -5.400608618669720e+001, 6.730060898200142e+001, -5.391569557615249e+001, 2.711667930741871e+001, -7.828533079330270e+000, 9.933219539081104e-001}
                },
                new double[][]{
                        new double[]{3.152188240596396e-003, -2.476949441252039e-002, 8.559691722298893e-002, -1.698966768987765e-001, 2.118341365177477e-001, -1.698966768987765e-001, 8.559691722298893e-002, -2.476949441252038e-002, 3.152188240596396e-003},
                        new double[]{1.000000000000000e+000, -7.852279118743454e+000, 2.711476287956804e+001, -5.377493875379191e+001, 6.699101032259173e+001, -5.367958785031539e+001, 2.701869120994906e+001, -7.810583428982896e+000, 9.929262646739174e-001}
                },
                new double[][]{
                        new double[]{3.151658566438113e-003, -2.471066735097116e-002, 8.526068503891726e-002, -1.690724884104834e-001, 2.107416319545301e-001, -1.690724884104833e-001, 8.526068503891726e-002, -2.471066735097116e-002, 3.151658566438114e-003},
                        new double[]{1.000000000000000e+000, -7.834701348506457e+000, 2.701093763646996e+001, -5.351705999979735e+001, 6.664592636870657e+001, -5.341652897030031e+001, 2.690955367161049e+001, -7.790632160028131e+000, 9.925072185567274e-001}
                },
                new double[][]{
                        new double[]{3.151106026960063e-003, -2.464503593410573e-002, 8.488566824372117e-002, -1.681539407503443e-001, 2.095244169366758e-001, -1.681539407503443e-001, 8.488566824372115e-002, -2.464503593410573e-002, 3.151106026960062e-003},
                        new double[]{1.000000000000000e+000, -7.815013891993500e+000, 2.689495187958396e+001, -5.322947098143593e+001, 6.626145055721739e+001, -5.312354032258413e+001, 2.678801247775242e+001, -7.768449336977040e+000, 9.920634476729724e-001}
                },
                new double[][]{
                        new double[]{3.150530313380003e-003, -2.457180190573428e-002, 8.446749094086684e-002, -1.671306710575339e-001, 2.081689226006989e-001, -1.671306710575339e-001, 8.446749094086682e-002, -2.457180190573428e-002, 3.150530313380004e-003},
                        new double[]{1.000000000000000e+000, -7.792964095691631e+000, 2.676542082793782e+001, -5.290889543716165e+001, 6.583329668224586e+001, -5.279734830107660e+001, 2.665268147288917e+001, -7.743778588425861e+000, 9.915935053562470e-001}
                },
                new double[][]{
                        new double[]{3.149931226123974e-003, -2.449007406337997e-002, 8.400132156317211e-002, -1.659913162360803e-001, 2.066603054012899e-001, -1.659913162360803e-001, 8.400132156317211e-002, -2.449007406337997e-002, 3.149931226123974e-003},
                        new double[]{1.000000000000000e+000, -7.768269505387938e+000, 2.662081440022254e+001, -5.255173958488378e+001, 6.535677580170375e+001, -5.243436460993682e+001, 2.650203141082893e+001, -7.716334167049936e+000, 9.910958617828615e-001}
                },
                new double[][]{
                        new double[]{3.149308696737955e-003, -2.439885783076391e-002, 8.348183626380033e-002, -1.647234542738494e-001, 2.049823823919444e-001, -1.647234542738494e-001, 8.348183626380033e-002, -2.439885783076391e-002, 3.149308696737955e-003},
                        new double[]{1.000000000000000e+000, -7.740614488341203e+000, 2.645944539669780e+001, -5.215407328305949e+001, 6.482677576262216e+001, -5.203066800777763e+001, 2.633437864637221e+001, -7.685797714171228e+000, 9.905688993747558e-001}
                },
                new double[][]{
                        new double[]{3.148662812953755e-003, -2.429704377527410e-002, 8.290318249918893e-002, -1.633135532680249e-001, 2.031175796050522e-001, -1.633135532680249e-001, 8.290318249918893e-002, -2.429704377527410e-002, 3.148662812953756e-003},
                        new double[]{1.000000000000000e+000, -7.709646509807524e+000, 2.627945770704299e+001, -5.171161358357100e+001, 6.423774485589274e+001, -5.158198852888927e+001, 2.614787393767223e+001, -7.651814707922908e+000, 9.900109079710157e-001}
                },
                new double[][]{
                        new double[]{3.147993847316972e-003, -2.418339499528223e-002, 8.225894394631153e-002, -1.617469323651304e-001, 2.010468996187548e-001, -1.617469323651304e-001, 8.225894394631153e-002, -2.418339499528222e-002, 3.147993847316972e-003},
                        new double[]{1.000000000000000e+000, -7.674972038770115e+000, 2.607881490262433e+001, -5.121971200272804e+001, 6.358368152129840e+001, -5.108369551406376e+001, 2.594049172766094e+001, -7.613990575114853e+000, 9.894200797592629e-001}
                },
                new double[][]{
                        new double[]{3.147302289834950e-003, -2.405653330624902e-002, 8.154210829236015e-002, -1.600077399582097e-001, 1.987499159446394e-001, -1.600077399582097e-001, 8.154210829236014e-002, -2.405653330624902e-002, 3.147302289834948e-003},
                        new double[]{1.000000000000000e+000, -7.636152058960868e+000, 2.585528969732743e+001, -5.067334719604580e+001, 6.285813251191995e+001, -5.053079114110794e+001, 2.571002039846987e+001, -7.571886448988433e+000, 9.887945039582496e-001}
                },
                new double[][]{
                        new double[]{3.146588885158769e-003, -2.391492416236799e-002, 8.074503993219968e-002, -1.580789558318979e-001, 1.962048036828320e-001, -1.580789558318979e-001, 8.074503993219971e-002, -2.391492416236799e-002, 3.146588885158770e-003},
                        new double[]{1.000000000000000e+000, -7.592697163719579e+000, 2.560645491929913e+001, -5.006712514976279e+001, 6.205420249911512e+001, -4.991791156951130e+001, 2.545405415016516e+001, -7.525014558465190e+000, 9.881321612431455e-001}
                },
                new double[][]{
                        new double[]{3.145854674876141e-003, -2.375686026326679e-002, 7.985946022891161e-002, -1.559424255217798e-001, 1.933884179764724e-001, -1.559424255217798e-001, 7.985946022891161e-002, -2.375686026326679e-002, 3.145854674876141e-003},
                        new double[]{1.000000000000000e+000, -7.544062217323155e+000, 2.532967683347243e+001, -4.939528950196758e+001, 6.116457875756176e+001, -4.923933830918797e+001, 2.516998735089173e+001, -7.472833239653871e+000, 9.874309179052381e-001}
                },
                new double[][]{
                        new double[]{3.145101045562206e-003, -2.358044381456427e-002, 7.887643875940420e-002, -1.535789369592204e-001, 1.902764341435763e-001, -1.535789369592204e-001, 7.887643875940420e-002, -2.358044381456427e-002, 3.145101045562206e-003},
                        new double[]{1.000000000000000e+000, -7.489640571648333e+000, 2.502211189939137e+001, -4.865174517941539e+001, 6.018157531047837e+001, -4.848902298999204e+001, 2.485501244705817e+001, -7.414741567800956e+000, 9.866885197380821e-001}
                },
                new double[][]{
                        new double[]{3.144329783313850e-003, -2.338356743882253e-002, 7.778639990704111e-002, -1.509683514875423e-001, 1.868435659282269e-001, -1.509683514875423e-001, 7.778639990704111e-002, -2.338356743882253e-002, 3.144329783313850e-003},
                        new double[]{1.000000000000000e+000, -7.428757836057749e+000, 2.468070834925526e+001, -4.783009917679500e+001, 5.909720172464564e+001, -4.766062934041341e+001, 2.450612281760468e+001, -7.350073618197181e+000, 9.859025856426020e-001}
                },
                new double[][]{
                        new double[]{3.143543135581006e-003, -2.316389377207868e-002, 7.657915030499854e-002, -1.480898035077080e-001, 1.830638810073308e-001, -1.480898035077080e-001, 7.657915030499855e-002, -2.316389377207868e-002, 3.143543135581006e-003},
                        new double[]{1.000000000000000e+000, -7.360665211036957e+000, 2.430221433655941e+001, -4.692372299709454e+001, 5.790326259567788e+001, -4.674759686361156e+001, 2.412012231428596e+001, -7.278092378587070e+000, 9.850706009441995e-001}
                },
                new double[][]{
                        new double[]{3.142743881206541e-003, -2.291883383372955e-002, 7.524393400650334e-002, -1.449219852393075e-001, 1.789112355627868e-001, -1.449219852393075e-001, 7.524393400650334e-002, -2.291883383372955e-002, 3.142743881206540e-003},
                        new double[]{1.000000000000000e+000, -7.284532413376133e+000, 2.388319484571587e+001, -4.592584198278260e+001, 5.659149460855977e+001, -4.574323139402281e+001, 2.369364365886558e+001, -7.197983354359358e+000, 9.841899104156673e-001}
                },
                new double[][]{
                        new double[]{3.141935409696230e-003, -2.264552432784142e-002, 7.376952388809109e-002, -1.414435351904935e-001, 1.743598521445707e-001, -1.414435351904935e-001, 7.376952388809108e-002, -2.264552432784141e-002, 3.141935409696229e-003},
                        new double[]{1.000000000000000e+000, -7.199440243846447e+000, 2.342006007650916e+001, -4.482965744266117e+001, 5.515374882202251e+001, -4.464082837240964e+001, 2.322317837594224e+001, -7.108847932436513e+000, 9.832577110005875e-001}
                },
                new double[][]{
                        new double[]{3.141121810862988e-003, -2.234080412659672e-002, 7.214435970035693e-002, -1.376335506400123e-001, 1.693850668579320e-001, -1.376335506400123e-001, 7.214435970035690e-002, -2.234080412659671e-002, 3.141121810862987e-003},
                        new double[]{1.000000000000000e+000, -7.104372878921088e+000, 2.290910863125306e+001, -4.362850803060598e+001, 5.358222639763521e+001, -4.343383520301292e+001, 2.270512153246824e+001, -7.009696601809882e+000, 9.822710442329992e-001}
                },
                new double[][]{
                        new double[]{3.140307976126783e-003, -2.200119030724153e-002, 7.035674537599862e-002, -1.334722453248443e-001, 1.639642726170729e-001, -1.334722453248443e-001, 7.035674537599862e-002, -2.200119030724154e-002, 3.140307976126782e-003},
                        new double[]{1.000000000000000e+000, -6.998210008037751e+000, 2.234658954035024e+001, -4.231607712696974e+001, 5.186977622103262e+001, -4.211605932017192e+001, 2.213583523342565e+001, -6.899442169873026e+000, 9.812267883503994e-001}
                },
                new double[][]{
                        new double[]{3.139499712905021e-003, -2.162285426944518e-002, 6.839512067458584e-002, -1.289417729867781e-001, 1.580780841620924e-001, -1.289417729867781e-001, 6.839512067458588e-002, -2.162285426944519e-002, 3.139499712905022e-003},
                        new double[]{1.000000000000000e+000, -6.879718989504162e+000, 2.172878796019500e+001, -4.088665281760313e+001, 5.001026253686516e+001, -4.068192840019685e+001, 2.151173558210775e+001, -6.776893166237469e+000, 9.801216500987595e-001}
                },
                new double[][]{
                        new double[]{3.138703873699729e-003, -2.120159865872217e-002, 6.624842494020337e-002, -1.240272346018628e-001, 1.517117468996463e-001, -1.240272346018628e-001, 6.624842494020337e-002, -2.120159865872217e-002, 3.138703873699729e-003},
                        new double[]{1.000000000000000e+000, -6.747547264196246e+000, 2.105214025163815e+001, -3.933544618694712e+001, 4.799900957772635e+001, -3.912680823655984e+001, 2.082940863624978e+001, -6.640747692124121e+000, 9.789521562300233e-001}
                },
                new double[][]{
                        new double[]{3.137928501679498e-003, -2.073283607339804e-002, 6.390657357142512e-002, -1.187178808303045e-001, 1.448568043951183e-001, -1.187178808303045e-001, 6.390657357142512e-002, -2.073283607339804e-002, 3.137928501679498e-003},
                        new double[]{1.000000000000000e+000, -6.600215350011449e+000, 2.031338506624802e+001, -3.765897166957116e+001, 4.583332789420625e+001, -3.744738178488232e+001, 2.008576174391972e+001, -6.489588056709191e+000, 9.777146446947530e-001}
                },
                new double[][]{
                        new double[]{3.137182994768761e-003, -2.021157084921369e-002, 6.136107057341432e-002, -1.130085099447135e-001, 1.375130273952083e-001, -1.130085099447135e-001, 6.136107057341432e-002, -2.021157084921369e-002, 3.137182994768761e-003},
                        new double[]{1.000000000000000e+000, -6.436110845532803e+000, 1.950975797479131e+001, -3.585548965015296e+001, 4.351312333645753e+001, -3.564208929582135e+001, 1.927821746633142e+001, -6.321876645437641e+000, 9.764052555351180e-001}
                },
                new double[][]{
                        new double[]{3.136478290496715e-003, -1.963238561066010e-002, 5.860578298576107e-002, -1.069010432752794e-001, 1.296905894905115e-001, -1.069010432752794e-001, 5.860578298576107e-002, -1.963238561066010e-002, 3.136478290496715e-003},
                        new double[]{1.000000000000000e+000, -6.253484003079926e+000, 1.863923797465455e+001, -3.392550574495158e+001, 4.104158405102899e+001, -3.371162367818436e+001, 1.840495799303931e+001, -6.135953593248776e+000, 9.750199214863040e-001}
                },
                new double[][]{
                        new double[]{3.135827074126856e-003, -1.898943476709113e-002, 5.563790460357721e-002, -1.004062326978215e-001, 1.214124500942118e-001, -1.004062326978215e-001, 5.563790460357720e-002, -1.898943476709113e-002, 3.135827074126855e-003},
                        new double[]{1.000000000000000e+000, -6.050445594454034e+000, 1.770085476591669e+001, -3.187231248663630e+001, 3.842593317714272e+001, -3.165946653481059e+001, 1.746522838225852e+001, -5.930036992274631e+000, 9.735543582978614e-001}
                },
                new double[][]{
                        new double[]{3.135244012888754e-003, -1.827644773199041e-002, 5.245913656145099e-002, -9.354541521470155e-002, 1.127168741389934e-001, -9.354541521470155e-002, 5.245913656145100e-002, -1.827644773199041e-002, 3.135244012888755e-003},
                        new double[]{1.000000000000000e+000, -5.824967992989349e+000, 1.669506577467719e+001, -2.970254661371651e+001, 3.567822510278451e+001, -2.949243785701341e+001, 1.645970690065039e+001, -5.702226553585582e+000, 9.720040547903410e-001}
                },
                new double[][]{
                        new double[]{3.134746019470630e-003, -1.748674537436416e-002, 4.907711006745098e-002, -8.635217549983100e-002, 1.036599808864124e-001, -8.635217549983101e-002, 4.907711006745098e-002, -1.748674537436416e-002, 3.134746019470632e-003},
                        new double[]{1.000000000000000e+000, -5.574890639196458e+000, 1.562421121838405e+001, -2.742671795429572e+001, 3.281615152807541e+001, -2.722121529947888e+001, 1.539094988747847e+001, -5.450511870800915e+000, 9.703642626670224e-001}
                },
                new double[][]{
                        new double[]{3.134352548309350e-003, -1.661327409169017e-002, 4.550707051926735e-002, -7.887370609052574e-002, 9.431817474222426e-002, -7.887370609052574e-002, 4.550707051926735e-002, -1.661327409169017e-002, 3.134352548309349e-003},
                        new double[]{1.000000000000000e+000, -5.297931350649583e+000, 1.449305362107864e+001, -2.505964324185187e+001, 2.986381121167740e+001, -2.486075657853854e+001, 1.426391650604211e+001, -5.172786704431734e+000, 9.686299861056348e-001}
                },
                new double[][]{
                        new double[]{3.134085928638541e-003, -1.564866294308468e-002, 4.177383060073083e-002, -7.117156573103167e-002, 8.479027540628280e-002, -7.117156573103167e-002, 4.177383060073084e-002, -1.564866294308468e-002, 3.134085928638541e-003},
                        new double[]{1.000000000000000e+000, -4.991705285766049e+000, 1.330940451460967e+001, -2.262068977353928e+001, 2.685238618316034e+001, -2.243053056001659e+001, 1.308657490580105e+001, -4.866871023409204e+000, 9.667959711606842e-001}
                },
                new double[][]{
                        new double[]{3.133971738732764e-003, -1.458531051532196e-002, 3.791398038204636e-002, -6.332143101523267e-002, 7.519914376860126e-002, -6.332143101523267e-002, 3.791398038204637e-002, -1.458531051532196e-002, 3.133971738732764e-003},
                        new double[]{1.000000000000000e+000, -4.653753780459771e+000, 1.208483484175470e+001, -2.013370016357715e+001, 2.382066078021547e+001, -1.995442963213826e+001, 1.187058496830032e+001, -4.530542906340166e+000, 9.648566950135764e-001}
                },
                new double[][]{
                        new double[]{3.134039226322169e-003, -1.341550962251499e-002, 3.397831195038829e-002, -5.541132177031712e-002, 6.569260943266918e-002, -5.541132177031712e-002, 3.397831195038829e-002, -1.341550962251499e-002, 3.134039226322169e-003},
                        new double[]{1.000000000000000e+000, -4.281585748495832e+000, 1.083545584338935e+001, -1.762643268940588e+001, 2.081532308871231e+001, -1.746020018575418e+001, 1.063204303178750e+001, -4.161582818466576e+000, 9.628063551150658e-001}
                },
                new double[][]{
                        new double[]{3.134321780757536e-003, -1.213161956026687e-002, 3.003437108339706e-002, -4.753767160414873e-002, 5.644356598658089e-002, -4.753767160414873e-002, 3.003437108339706e-002, -1.213161956026687e-002, 3.134321780757536e-003},
                        new double[]{1.000000000000000e+000, -3.872734868428338e+000, 9.582742796500897e+000, -1.512931661567665e+001, 1.789100765938704e+001, -1.497819422522095e+001, 9.392259689330880e+000, -3.757833234835759e+000, 9.606388582728883e-001}
                },
                new double[][]{
                        new double[]{3.134857463189034e-003, -1.072629742487928e-002, 2.616898486943642e-002, -3.979853915444665e-002, 4.764923335280726e-002, -3.979853915444665e-002, 2.616898486943642e-002, -1.072629742487929e-002, 3.134857463189036e-003},
                        new double[]{1.000000000000000e+000, -3.424836362665022e+000, 8.354353544174989e+000, -1.267329710343471e+001, 1.511008073912571e+001, -1.253922198523032e+001, 8.178521712285495e+000, -3.317277061692937e+000, 9.583478097467505e-001}
                },
                new double[][]{
                        new double[]{3.135689601795952e-003, -9.192801914856020e-003, 2.249052777972339e-002, -3.228325770744059e-002, 3.952980949329304e-002, -3.228325770744058e-002, 2.249052777972340e-002, -9.192801914856018e-003, 3.135689601795953e-003},
                        new double[]{1.000000000000000e+000, -2.935727786560088e+000, 7.184865948619916e+000, -1.028654413040799e+001, 1.254223993346572e+001, -1.017128706803972e+001, 7.024762141100510e+000, -2.838138790755699e+000, 9.559265024233198e-001}
                },
                new double[][]{
                        new double[]{3.136867459982495e-003, -7.525384956076330e-003, 1.913057593393776e-002, -2.505796833460762e-002, 3.232704353501763e-002, -2.505796833460763e-002, 1.913057593393775e-002, -7.525384956076330e-003, 3.136867459982495e-003},
                        new double[]{1.000000000000000e+000, -2.403578849731980e+000, 6.116322021836488e+000, -7.989845724149395e+000, 1.026409819970081e+001, -7.895033186180221e+000, 5.972027625440275e+000, -2.319012762106196e+000, 9.533679061555159e-001}
                },
                new double[][]{
                        new double[]{3.138446986453598e-003, -5.719788266387037e-003, 1.624445853637540e-002, -1.814686749013248e-002, 2.630361280892668e-002, -1.814686749013247e-002, 1.624445853637540e-002, -5.719788266387037e-003, 3.138446986453599e-003},
                        new double[]{1.000000000000000e+000, -1.827055825922347e+000, 5.198420980989066e+000, -5.790627461846007e+000, 8.359032593241114e+000, -5.717854195620133e+000, 5.068588868120159e+000, -1.759023244522344e+000, 9.506646573634056e-001}
                },
                new double[][]{
                        new double[]{3.140491657224234e-003, -3.773863336298222e-003, 1.401004915216966e-002, -1.150974045018174e-002, 2.174445408345166e-002, -1.150974045018174e-002, 1.401004915216966e-002, -3.773863336298222e-003, 3.140491657224234e-003},
                        new double[]{1.000000000000000e+000, -1.205526475294007e+000, 4.488149587584013e+000, -3.675774949486338e+000, 6.917662040320396e+000, -3.626853005615643e+000, 4.369489565598928e+000, -1.158021168308530e+000, 9.478090490084538e-001}
                },
                new double[][]{
                        new double[]{3.143073420920189e-003, -1.688333852644991e-003, 1.262395748797917e-002, -5.017573087974817e-003, 1.896114476128464e-002, -5.017573087974814e-003, 1.262395748797918e-002, -1.688333852644991e-003, 3.143073420920189e-003},
                        new double[]{1.000000000000000e+000, -5.393114608165109e-001, 4.048578996469637e+000, -1.603824537959889e+000, 6.039299478305786e+000, -1.581219217193778e+000, 3.935274955307953e+000, -5.168221229728396e-001, 9.447930210688751e-001}
                },
                new double[][]{
                        new double[]{3.146273760226500e-003, 5.322812464931028e-004, 1.229410658041627e-002, 1.570162041369520e-003, 1.829966777695044e-002, 1.570162041369518e-003, 1.229410658041627e-002, 5.322812464931027e-004, 3.146273760226500e-003},
                        new double[]{1.000000000000000e+000, 1.700122175818912e-001, 3.946500014377160e+000, 5.023368561645096e-001, 5.834479755741241e+000, 4.948389524665608e-001, 3.829590220830910e+000, 1.625105377327541e-001, 9.416081516614648e-001}
                },
                new double[][]{
                        new double[]{3.150184884067147e-003, 2.878568940453699e-003, 1.322755106586022e-002, 8.620552071421581e-003, 2.014990932533000e-002, 8.620552071421581e-003, 1.322755106586022e-002, 2.878568940453698e-003, 3.150184884067147e-003},
                        new double[]{1.000000000000000e+000, 9.192515080342589e-001, 4.248526154251194e+000, 2.760354359206612e+000, 6.428032354219372e+000, 2.716723422282723e+000, 4.115304532346304e+000, 8.763352262803313e-001, 9.382456489747480e-001}
                },
                new double[][]{
                        new double[]{3.154911067098878e-003, 5.334938740144927e-003, 1.561237564818607e-002, 1.662582664159495e-002, 2.495150819282767e-002, 1.662582664159496e-002, 1.561237564818607e-002, 5.334938740144928e-003, 3.154911067098878e-003},
                        new double[]{1.000000000000000e+000, 1.703179499277951e+000, 5.015287582907516e+000, 5.328060497664993e+000, 7.960981372090181e+000, 5.238878049409713e+000, 4.848819300199696e+000, 1.619054784052113e+000, 9.346963441995914e-001}
                },
                new double[][]{
                        new double[]{3.160570155425528e-003, 7.878157153029112e-003, 1.959270862731381e-002, 2.617401829906347e-002, 3.318498160501834e-002, 2.617401829906346e-002, 1.959270862731381e-002, 7.878157153029110e-003, 3.160570155425528e-003},
                        new double[]{1.000000000000000e+000, 2.514061860132557e+000, 6.293401312113454e+000, 8.394231003661758e+000, 1.058774518680232e+001, 8.245442893665601e+000, 6.072290240693297e+000, 2.382697143855915e+000, 9.309506856667303e-001}
                },
                new double[][]{
                        new double[]{3.167295260151064e-003, 1.047572033190279e-002, 2.523643163272042e-002, 3.787881035026627e-002, 4.533007101187137e-002, 3.787881035026630e-002, 2.523643163272043e-002, 1.047572033190280e-002, 3.167295260151064e-003},
                        new double[]{1.000000000000000e+000, 3.341136359577790e+000, 8.105078676150034e+000, 1.215629189647473e+001, 1.446288885419975e+001, 1.192812032225852e+001, 7.803674281773004e+000, 3.156465395399920e+000, 9.269987344260360e-001}
                },
                new double[][]{
                        new double[]{3.175236663568253e-003, 1.308411231056350e-002, 3.249623781842040e-002, 5.225727921334373e-002, 6.176741363408551e-002, 5.225727921334372e-002, 3.249623781842039e-002, 1.308411231056350e-002, 3.175236663568252e-003},
                        new double[]{1.000000000000000e+000, 4.170063916809950e+000, 1.043557693581984e+001, 1.678131188069654e+001, 1.970982407256151e+001, 1.644776743866162e+001, 1.002485619533124e+001, 3.926277990238661e+000, 9.228301615299103e-001}
                },
                new double[][]{
                        new double[]{3.184563966522060e-003, 1.564703528332044e-002, 4.116647822650329e-002, 6.955135546761315e-002, 8.260002997525108e-002, 6.955135546761315e-002, 4.116647822650329e-002, 1.564703528332044e-002, 3.184563966522059e-003},
                        new double[]{1.000000000000000e+000, 4.982383252986069e+000, 1.321928002312587e+001, 2.234885788249895e+001, 2.636396393156668e+001, 2.187848507504305e+001, 1.266867131040727e+001, 4.674332629971478e+000, 9.184342473129028e-001}
                },
                new double[][]{
                        new double[]{3.195468509911748e-003, 1.809375255989474e-002, 5.084090030653590e-002, 8.950422298444746e-002, 1.073856458036988e-001, 8.950422298444746e-002, 5.084090030653590e-002, 1.809375255989474e-002, 3.195468509911749e-003},
                        new double[]{1.000000000000000e+000, 5.755019760051603e+000, 1.632605611212203e+001, 2.877918880332384e+001, 3.428743475883073e+001, 2.813781188877264e+001, 1.560646217523939e+001, 5.378744289396370e+000, 9.137998829916308e-001}
                },
                new double[][]{
                        new double[]{3.208166108549520e-003, 2.033775720013014e-002, 6.087995852729569e-002, 1.111283766985333e-001, 1.348084233730102e-001, 1.111283766985333e-001, 6.087995852729569e-002, 2.033775720013014e-002, 3.208166108549520e-003},
                        new double[]{1.000000000000000e+000, 6.459923452852331e+000, 1.955069789760037e+001, 3.575841878007016e+001, 4.306434589484458e+001, 3.491460431644380e+001, 1.863888825236724e+001, 6.013329244005268e+000, 9.089155749432362e-001}
                },
                new double[][]{
                        new double[]{3.222900141858363e-003, 2.227607222988250e-002, 7.040061526828756e-002, 1.325362521214649e-001, 1.623833078994801e-001, 1.325362521214649e-001, 7.040061526828756e-002, 2.227607222988250e-002, 3.222900141858363e-003},
                        new double[]{1.000000000000000e+000, 7.063941201586085e+000, 2.260962332507321e+001, 4.268327468011344e+001, 5.190611606085582e+001, 4.161681749209129e+001, 2.149395668247145e+001, 6.547632809776028e+000, 9.037694520569339e-001}
                },
                new double[][]{
                        new double[]{3.239945053390167e-003, 2.378960212591439e-002, 7.830568646607240e-002, 1.509391118013942e-001, 1.863663521383706e-001, 1.509391118013942e-001, 7.830568646607242e-002, 2.378960212591439e-002, 3.239945053390167e-003},
                        new double[]{1.000000000000000e+000, 7.529064346833643e+000, 2.515035008465618e+001, 4.865934114163024e+001, 5.962181888765429e+001, 4.737213222877233e+001, 2.383740644607056e+001, 6.947327741252297e+000, 8.983492765915317e-001}
                },
                new double[][]{
                        new double[]{3.259610320154749e-003, 2.474508638385797e-002, 8.337226016441471e-002, 1.629367425557876e-001, 2.020999170550666e-001, 1.629367425557876e-001, 8.337226016441469e-002, 2.474508638385797e-002, 3.259610320154748e-003},
                        new double[]{1.000000000000000e+000, 7.813232181445080e+000, 2.678004704072203e+001, 5.259232996155322e+001, 6.472625974952638e+001, 5.111941179551022e+001, 2.530116502103031e+001, 7.175140285956256e+000, 8.926424590119200e-001}
                }
        };
    }

    private static class MIDI_COEFFICIENTS_SR1378_125HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.160102871562913e-003, -2.526308677640546e-002, 8.837650030117852e-002, -1.766998661278661e-001, 2.208526994630720e-001, -1.766998661278661e-001, 8.837650030117855e-002, -2.526308677640547e-002, 3.160102871562914e-003},
                        new double[]{1.000000000000000e+000, -7.993035137184169e+000, 2.795682066286448e+001, -5.588715705735085e+001, 6.983971677026145e+001, -5.586747992306563e+001, 2.793713766978453e+001, -7.984595381542750e+000, 9.985923962366661e-001}
                },
                new double[][]{
                        new double[]{3.159975882961421e-003, -2.525989995316554e-002, 8.835992876682405e-002, -1.766602295678194e-001, 2.208004497424176e-001, -1.766602295678194e-001, 8.835992876682405e-002, -2.525989995316553e-002, 3.159975882961421e-003},
                        new double[]{1.000000000000000e+000, -7.992271099617168e+000, 2.795215691738355e+001, -5.587521223997506e+001, 6.982321057779913e+001, -5.585436971614900e+001, 2.793130742781282e+001, -7.983330625451835e+000, 9.985087582034402e-001}
                },
                new double[][]{
                        new double[]{3.159841685994492e-003, -2.525638983434093e-002, 8.834156412890981e-002, -1.766162149140839e-001, 2.207423978670707e-001, -1.766162149140839e-001, 8.834156412890981e-002, -2.525638983434093e-002, 3.159841685994491e-003},
                        new double[]{1.000000000000000e+000, -7.991418808545359e+000, 2.794695967663140e+001, -5.586191799869053e+001, 6.980487161117404e+001, -5.583984161248014e+001, 2.792487500878889e+001, -7.981948031305878e+000, 9.984201544369321e-001}
                },
                new double[][]{
                        new double[]{3.159699893956621e-003, -2.525252084441813e-002, 8.832120131851101e-002, -1.765673158808659e-001, 2.206778710256798e-001, -1.765673158808659e-001, 8.832120131851101e-002, -2.525252084441813e-002, 3.159699893956621e-003},
                        new double[]{1.000000000000000e+000, -7.990467778347355e+000, 2.794116597527122e+001, -5.584711629266529e+001, 6.978448735548403e+001, -5.582373364819085e+001, 2.791777348497057e+001, -7.980435387101997e+000, 9.983262905945257e-001}
                },
                new double[][]{
                        new double[]{3.159550102272552e-003, -2.524825329737595e-002, 8.829861112984655e-002, -1.765129663425945e-001, 2.206061168157774e-001, -1.765129663425945e-001, 8.829861112984654e-002, -2.524825329737595e-002, 3.159550102272551e-003},
                        new double[]{1.000000000000000e+000, -7.989406259963432e+000, 2.793470531018452e+001, -5.583063027319029e+001, 6.976182015671301e+001, -5.580586484538701e+001, 2.790992817728934e+001, -7.978779120568047e+000, 9.982268549454829e-001}
                },
                new double[][]{
                        new double[]{3.159391888060492e-003, -2.524354290957952e-002, 8.827353735018950e-002, -1.764525332301130e-001, 2.205262938030033e-001, -1.764525332301129e-001, 8.827353735018949e-002, -2.524354290957952e-002, 3.159391888060493e-003},
                        new double[]{1.000000000000000e+000, -7.988221087768674e+000, 2.792749873579812e+001, -5.581226204234034e+001, 6.973660423984350e+001, -5.578603296354181e+001, 2.790125574202765e+001, -7.976964141331972e+000, 9.981215173509664e-001}
                },
                new double[][]{
                        new double[]{3.159224809745750e-003, -2.523834025436573e-002, 8.824569355028043e-002, -1.763853085942930e-001, 2.204374609774531e-001, -1.763853085942930e-001, 8.824569355028043e-002, -2.523834025436573e-002, 3.159224809745750e-003},
                        new double[]{1.000000000000000e+000, -7.986897507880478e+000, 2.791945785177781e+001, -5.579179014827696e+001, 6.970854237861690e+001, -5.576401198801909e+001, 2.789166315009525e+001, -7.974973664439123e+000, 9.980099281851373e-001}
                },
                new double[][]{
                        new double[]{3.159048406736869e-003, -2.523259015134536e-002, 8.821475949609869e-002, -1.763105007433883e-001, 2.203385659840945e-001, -1.763105007433883e-001, 8.821475949609869e-002, -2.523259015134536e-002, 3.159048406736869e-003},
                        new double[]{1.000000000000000e+000, -7.985418985660877e+000, 2.791048367061966e+001, -5.576896678748984e+001, 6.967730217807400e+001, -5.573954932622059e+001, 2.788104654658634e+001, -7.972789013008361e+000, 9.978917171939963e-001}
                },
                new double[][]{
                        new double[]{3.158862199178640e-003, -2.522623098262760e-002, 8.818037713864389e-002, -1.762272243510122e-001, 2.202284319921079e-001, -1.762272243510122e-001, 8.818037713864388e-002, -2.522623098262760e-002, 3.158862199178640e-003},
                        new double[]{1.000000000000000e+000, -7.983766989907910e+000, 2.790046535131966e+001, -5.574351468126776e+001, 6.964251192718245e+001, -5.571236267888894e+001, 2.786928997698394e+001, -7.970389397560299e+000, 9.977664922885320e-001}
                },
                new double[][]{
                        new double[]{3.158665687797979e-003, -2.521919392725215e-002, 8.814214613378270e-002, -1.761344894215090e-001, 2.201057430551121e-001, -1.761344894215089e-001, 8.814214613378270e-002, -2.521919392725215e-002, 3.158665687797979e-003},
                        new double[]{1.000000000000000e+000, -7.981920750933047e+000, 2.788927878383032e+001, -5.571512359047303e+001, 6.960375597479234e+001, -5.568213655092985e+001, 2.785626396494602e+001, -7.967751669263686e+000, 9.976338382685047e-001}
                },
                new double[][]{
                        new double[]{3.158458353861473e-003, -2.521140210409662e-002, 8.809961883926073e-002, -1.760311889888081e-001, 2.199690278007571e-001, -1.760311889888081e-001, 8.809961883926075e-002, -2.521140210409663e-002, 3.158458353861473e-003},
                        new double[]{1.000000000000000e+000, -7.979856989390820e+000, 2.787678500741772e+001, -5.568344642928296e+001, 6.956056957789366e+001, -5.564851836276789e+001, 2.784182392506171e+001, -7.964850044027476e+000, 9.974933154730576e-001}
                },
                new double[][]{
                        new double[]{3.158239659265769e-003, -2.520276961238962e-002, 8.805229473062350e-002, -1.759160854136171e-001, 2.198166412741269e-001, -1.759160854136170e-001, 8.805229473062347e-002, -2.520276961238962e-002, 3.158239659265770e-003},
                        new double[]{1.000000000000000e+000, -7.977549612358271e+000, 2.786282844431199e+001, -5.564809493498185e+001, 6.951243316672097e+001, -5.561111411973872e+001, 2.782580839230226e+001, -7.961655794012175e+000, 9.973444583541184e-001}
                },
                new double[][]{
                        new double[]{3.158009046785217e-003, -2.519320045770075e-002, 8.799961417207584e-002, -1.757877951322226e-001, 2.196467447451271e-001, -1.757877951322225e-001, 8.799961417207582e-002, -2.519320045770074e-002, 3.158009046785216e-003},
                        new double[]{1.000000000000000e+000, -7.974969372752160e+000, 2.784723492820081e+001, -5.560863484718004e+001, 6.945876596673548e+001, -5.556948359339760e+001, 2.780803704811647e+001, -7.958136902741812e+000, 9.971867739683715e-001}
                },
                new double[][]{
                        new double[]{3.157765940504518e-003, -2.518258734987953e-002, 8.794095147224382e-002, -1.756447716983805e-001, 2.194572832757883e-001, -1.756447716983806e-001, 8.794095147224382e-002, -2.518258734987952e-002, 3.157765940504517e-003},
                        new double[]{1.000000000000000e+000, -7.972083487716799e+000, 2.782980950515790e+001, -5.556458054606449e+001, 6.939891891301646e+001, -5.552313496494917e+001, 2.778830852124351e+001, -7.954257679564011e+000, 9.970197403833869e-001}
                },
                new double[][]{
                        new double[]{3.157509746467995e-003, -2.517081035786543e-002, 8.787560714843676e-002, -1.754852869480970e-001, 2.192459608296765e-001, -1.754852869480970e-001, 8.787560714843676e-002, -2.517081035786543e-002, 3.157509746467995e-003},
                        new double[]{1.000000000000000e+000, -7.968855211110624e+000, 2.781033398253884e+001, -5.551538909553598e+001, 6.933216678824905e+001, -5.547151887736864e+001, 2.776639793935085e+001, -7.949978328725671e+000, 9.968428049932396e-001}
                },
                new double[][]{
                        new double[]{3.157239853581603e-003, -2.515773540457894e-002, 8.780279931642641e-002, -1.753074101059224e-001, 2.190102126929847e-001, -1.753074101059224e-001, 8.780279931642640e-002, -2.515773540457894e-002, 3.157239853581603e-003},
                        new double[]{1.000000000000000e+000, -7.965243354660791e+000, 2.778856419923966e+001, -5.546045363348811e+001, 6.925769951150495e+001, -5.541402183931861e+001, 2.774205420558135e+001, -7.945254467803112e+000, 9.966553827387890e-001}
                },
                new double[][]{
                        new double[]{3.156955634808493e-003, -2.514321258320968e-002, 8.772165411605430e-002, -1.751089846412184e-001, 2.187471749661684e-001, -1.751089846412184e-001, 8.772165411605430e-002, -2.514321258320968e-002, 3.156955634808493e-003},
                        new double[]{1.000000000000000e+000, -7.961201751737143e+000, 2.776422698853192e+001, -5.539909604819387e+001, 6.917461250162503e+001, -5.534995892083294e+001, 2.771499697204265e+001, -7.940036589642882e+000, 9.964568542275057e-001}
                },
                new double[][]{
                        new double[]{3.156656448704776e-003, -2.512707427413613e-002, 8.763119507633681e-002, -1.748876026744579e-001, 2.184536508773114e-001, -1.748876026744579e-001, 8.763119507633679e-002, -2.512707427413613e-002, 3.156656448704776e-003},
                        new double[]{1.000000000000000e+000, -7.956678657015536e+000, 2.773701680251237e+001, -5.533055887701768e+001, 6.908189603657752e+001, -5.527856567817344e+001, 2.768491328025209e+001, -7.934269461330747e+000, 9.962465637475458e-001}
                },
                new double[][]{
                        new double[]{3.156341641348188e-003, -2.510913303942006e-002, 8.753033131735499e-002, -1.746405767278798e-001, 2.181260736651187e-001, -1.746405767278799e-001, 8.753033131735499e-002, -2.510913303942007e-002, 3.156341641348189e-003},
                        new double[]{1.000000000000000e+000, -7.951616074550573e+000, 2.770659196510449e+001, -5.525399636175364e+001, 6.897842352911954e+001, -5.519898924355911e+001, 2.765145383663062e+001, -7.927891453006585e+000, 9.960238171705083e-001}
                },
                new double[][]{
                        new double[]{3.156010548719496e-003, -2.508917926931127e-002, 8.741784448041817e-002, -1.743649086130051e-001, 2.177604657823905e-001, -1.743649086130051e-001, 8.741784448041816e-002, -2.508917926931127e-002, 3.156010548719496e-003},
                        new double[]{1.000000000000000e+000, -7.945949005955032e+000, 2.767257051861824e+001, -5.516846459419217e+001, 6.886293864000967e+001, -5.511027851501318e+001, 2.761422888941914e+001, -7.920833788580041e+000, 9.957878797370805e-001}
                },
                new double[][]{
                        new double[]{3.155662499604399e-003, -2.506697855245887e-002, 8.729237427317103e-002, -1.740572552511763e-001, 2.173523941823380e-001, -1.740572552511763e-001, 8.729237427317103e-002, -2.506697855245886e-002, 3.155662499604398e-003},
                        new double[]{1.000000000000000e+000, -7.939604609483376e+000, 2.763452562724007e+001, -5.507291068653348e+001, 6.873404115363863e+001, -5.501137338286942e+001, 2.757280367200363e+001, -7.913019709572767e+000, 9.955379737195335e-001}
                },
                new double[][]{
                        new double[]{3.155296819092648e-003, -2.504226873854748e-002, 8.715240251302218e-002, -1.737138912344610e-001, 2.168969214731223e-001, -1.737138912344610e-001, 8.715240251302220e-002, -2.504226873854748e-002, 3.155296819092648e-003},
                        new double[]{1.000000000000000e+000, -7.932501259836564e+000, 2.759198049965839e+001, -5.496616090463505e+001, 6.859017154819686e+001, -5.490109293316875e+001, 2.752669337675260e+001, -7.904363542416965e+000, 9.952732759547681e-001}
                },
                new double[][]{
                        new double[]{3.154912832761072e-003, -2.501475665885260e-002, 8.699623555120342e-002, -1.733306679554957e-001, 2.163885527642557e-001, -1.733306679554957e-001, 8.699623555120342e-002, -2.501475665885260e-002, 3.154912832761072e-003},
                        new double[]{1.000000000000000e+000, -7.924547497442289e+000, 2.754440279254992e+001, -5.484690770861077e+001, 6.842959420455813e+001, -5.477812257507029e+001, 2.747535762332123e+001, -7.894769658576227e+000, 9.949929152413864e-001}
                },
                new double[][]{
                        new double[]{3.154509871638553e-003, -2.498411446674099e-002, 8.682198496185380e-002, -1.729029691694235e-001, 2.158211780866830e-001, -1.729029691694236e-001, 8.682198496185380e-002, -2.498411446674099e-002, 3.154509871638554e-003},
                        new double[]{1.000000000000000e+000, -7.915640854818626e+000, 2.749119845715203e+001, -5.471369565601653e+001, 6.825037921640217e+001, -5.464100005052808e+001, 2.741819438625328e+001, -7.884131315825984e+000, 9.946959695939833e-001}
                },
                new double[][]{
                        new double[]{3.154087278063668e-003, -2.494997555643428e-002, 8.662754638693286e-002, -1.724256629030175e-001, 2.151880103522501e-001, -1.724256629030176e-001, 8.662754638693286e-002, -2.494997555643428e-002, 3.154087278063668e-003},
                        new double[]{1.000000000000000e+000, -7.905666546401251e+000, 2.743170499299610e+001, -5.456490613904224e+001, 6.805038279056032e+001, -5.448810030112487e+001, 2.735453334899452e+001, -7.872329367941698e+000, 9.943814633476313e-001}
                },
                new double[][]{
                        new double[]{3.153644412559933e-003, -2.491193001442541e-002, 8.641057644002366e-002, -1.718930497004701e-001, 2.144815189350482e-001, -1.718930497004701e-001, 8.641057644002366e-002, -2.491193001442541e-002, 3.153644412559932e-003},
                        new double[]{1.000000000000000e+000, -7.894496006909852e+000, 2.736518407655722e+001, -5.439874095038135e+001, 6.782722626340617e+001, -5.431761919070840e+001, 2.728362865560162e+001, -7.859230828901897e+000, 9.940483641052797e-001}
                },
                new double[][]{
                        new double[]{3.153180661869607e-003, -2.486951955381822e-002, 8.616846759211466e-002, -1.712988072983833e-001, 2.136933591155672e-001, -1.712988072983833e-001, 8.616846759211465e-002, -2.486951955381822e-002, 3.153180661869608e-003},
                        new double[]{1.000000000000000e+000, -7.881985261957821e+000, 2.729081353869786e+001, -5.421320470473966e+001, 6.757827380910089e+001, -5.412755611530962e+001, 2.720465103810294e+001, -7.844687276533637e+000, 9.936955795205367e-001}
                },
                new double[][]{
                        new double[]{3.152695448305012e-003, -2.482223187759939e-002, 8.589832099269115e-002, -1.706359319618829e-001, 2.128142978393528e-001, -1.706359319618829e-001, 8.589832099269117e-002, -2.482223187759939e-002, 3.152695448305013e-003},
                        new double[]{1.000000000000000e+000, -7.867973113185387e+000, 2.720767867411994e+001, -5.400608618669720e+001, 6.730060898200142e+001, -5.391569557615249e+001, 2.711667930741871e+001, -7.828533079330270e+000, 9.933219539081104e-001}
                },
                new double[][]{
                        new double[]{3.152188240596396e-003, -2.476949441252039e-002, 8.559691722298893e-002, -1.698966768987765e-001, 2.118341365177477e-001, -1.698966768987765e-001, 8.559691722298893e-002, -2.476949441252038e-002, 3.152188240596396e-003},
                        new double[]{1.000000000000000e+000, -7.852279118743454e+000, 2.711476287956804e+001, -5.377493875379191e+001, 6.699101032259173e+001, -5.367958785031539e+001, 2.701869120994906e+001, -7.810583428982896e+000, 9.929262646739174e-001}
                },
                new double[][]{
                        new double[]{3.151658566438113e-003, -2.471066735097116e-002, 8.526068503891726e-002, -1.690724884104834e-001, 2.107416319545301e-001, -1.690724884104833e-001, 8.526068503891726e-002, -2.471066735097116e-002, 3.151658566438114e-003},
                        new double[]{1.000000000000000e+000, -7.834701348506457e+000, 2.701093763646996e+001, -5.351705999979735e+001, 6.664592636870657e+001, -5.341652897030031e+001, 2.690955367161049e+001, -7.790632160028131e+000, 9.925072185567274e-001}
                },
                new double[][]{
                        new double[]{3.151106026960063e-003, -2.464503593410573e-002, 8.488566824372117e-002, -1.681539407503443e-001, 2.095244169366758e-001, -1.681539407503443e-001, 8.488566824372115e-002, -2.464503593410573e-002, 3.151106026960062e-003},
                        new double[]{1.000000000000000e+000, -7.815013891993500e+000, 2.689495187958396e+001, -5.322947098143593e+001, 6.626145055721739e+001, -5.312354032258413e+001, 2.678801247775242e+001, -7.768449336977040e+000, 9.920634476729724e-001}
                },
                new double[][]{
                        new double[]{3.150530313380003e-003, -2.457180190573428e-002, 8.446749094086684e-002, -1.671306710575339e-001, 2.081689226006989e-001, -1.671306710575339e-001, 8.446749094086682e-002, -2.457180190573428e-002, 3.150530313380004e-003},
                        new double[]{1.000000000000000e+000, -7.792964095691631e+000, 2.676542082793782e+001, -5.290889543716165e+001, 6.583329668224586e+001, -5.279734830107660e+001, 2.665268147288917e+001, -7.743778588425861e+000, 9.915935053562470e-001}
                },
                new double[][]{
                        new double[]{3.149931226123974e-003, -2.449007406337997e-002, 8.400132156317211e-002, -1.659913162360803e-001, 2.066603054012899e-001, -1.659913162360803e-001, 8.400132156317211e-002, -2.449007406337997e-002, 3.149931226123974e-003},
                        new double[]{1.000000000000000e+000, -7.768269505387938e+000, 2.662081440022254e+001, -5.255173958488378e+001, 6.535677580170375e+001, -5.243436460993682e+001, 2.650203141082893e+001, -7.716334167049936e+000, 9.910958617828615e-001}
                },
                new double[][]{
                        new double[]{3.149308696737955e-003, -2.439885783076391e-002, 8.348183626380033e-002, -1.647234542738494e-001, 2.049823823919444e-001, -1.647234542738494e-001, 8.348183626380033e-002, -2.439885783076391e-002, 3.149308696737955e-003},
                        new double[]{1.000000000000000e+000, -7.740614488341203e+000, 2.645944539669780e+001, -5.215407328305949e+001, 6.482677576262216e+001, -5.203066800777763e+001, 2.633437864637221e+001, -7.685797714171228e+000, 9.905688993747558e-001}
                },
                new double[][]{
                        new double[]{3.148662812953755e-003, -2.429704377527410e-002, 8.290318249918893e-002, -1.633135532680249e-001, 2.031175796050522e-001, -1.633135532680249e-001, 8.290318249918893e-002, -2.429704377527410e-002, 3.148662812953756e-003},
                        new double[]{1.000000000000000e+000, -7.709646509807524e+000, 2.627945770704299e+001, -5.171161358357100e+001, 6.423774485589274e+001, -5.158198852888927e+001, 2.614787393767223e+001, -7.651814707922908e+000, 9.900109079710157e-001}
                },
                new double[][]{
                        new double[]{3.147993847316972e-003, -2.418339499528223e-002, 8.225894394631153e-002, -1.617469323651304e-001, 2.010468996187549e-001, -1.617469323651304e-001, 8.225894394631153e-002, -2.418339499528223e-002, 3.147993847316972e-003},
                        new double[]{1.000000000000000e+000, -7.674972038770115e+000, 2.607881490262433e+001, -5.121971200272805e+001, 6.358368152129840e+001, -5.108369551406377e+001, 2.594049172766093e+001, -7.613990575114853e+000, 9.894200797592629e-001}
                },
                new double[][]{
                        new double[]{3.147302289834950e-003, -2.405653330624902e-002, 8.154210829236015e-002, -1.600077399582097e-001, 1.987499159446394e-001, -1.600077399582097e-001, 8.154210829236014e-002, -2.405653330624902e-002, 3.147302289834948e-003},
                        new double[]{1.000000000000000e+000, -7.636152058960868e+000, 2.585528969732743e+001, -5.067334719604580e+001, 6.285813251191995e+001, -5.053079114110794e+001, 2.571002039846987e+001, -7.571886448988433e+000, 9.887945039582496e-001}
                },
                new double[][]{
                        new double[]{3.146588885158769e-003, -2.391492416236799e-002, 8.074503993219968e-002, -1.580789558318979e-001, 1.962048036828320e-001, -1.580789558318979e-001, 8.074503993219971e-002, -2.391492416236799e-002, 3.146588885158770e-003},
                        new double[]{1.000000000000000e+000, -7.592697163719579e+000, 2.560645491929913e+001, -5.006712514976279e+001, 6.205420249911512e+001, -4.991791156951130e+001, 2.545405415016516e+001, -7.525014558465190e+000, 9.881321612431455e-001}
                },
                new double[][]{
                        new double[]{3.145854674876142e-003, -2.375686026326680e-002, 7.985946022891163e-002, -1.559424255217798e-001, 1.933884179764724e-001, -1.559424255217798e-001, 7.985946022891163e-002, -2.375686026326680e-002, 3.145854674876142e-003},
                        new double[]{1.000000000000000e+000, -7.544062217323155e+000, 2.532967683347243e+001, -4.939528950196758e+001, 6.116457875756177e+001, -4.923933830918798e+001, 2.516998735089173e+001, -7.472833239653872e+000, 9.874309179052380e-001}
                },
                new double[][]{
                        new double[]{3.145101045562206e-003, -2.358044381456427e-002, 7.887643875940420e-002, -1.535789369592204e-001, 1.902764341435763e-001, -1.535789369592204e-001, 7.887643875940420e-002, -2.358044381456427e-002, 3.145101045562206e-003},
                        new double[]{1.000000000000000e+000, -7.489640571648333e+000, 2.502211189939137e+001, -4.865174517941539e+001, 6.018157531047837e+001, -4.848902298999204e+001, 2.485501244705817e+001, -7.414741567800956e+000, 9.866885197380821e-001}
                },
                new double[][]{
                        new double[]{3.144329783313850e-003, -2.338356743882254e-002, 7.778639990704114e-002, -1.509683514875424e-001, 1.868435659282270e-001, -1.509683514875424e-001, 7.778639990704114e-002, -2.338356743882254e-002, 3.144329783313851e-003},
                        new double[]{1.000000000000000e+000, -7.428757836057749e+000, 2.468070834925526e+001, -4.783009917679499e+001, 5.909720172464564e+001, -4.766062934041341e+001, 2.450612281760468e+001, -7.350073618197181e+000, 9.859025856426016e-001}
                },
                new double[][]{
                        new double[]{3.143543135581006e-003, -2.316389377207868e-002, 7.657915030499854e-002, -1.480898035077080e-001, 1.830638810073308e-001, -1.480898035077080e-001, 7.657915030499855e-002, -2.316389377207868e-002, 3.143543135581006e-003},
                        new double[]{1.000000000000000e+000, -7.360665211036957e+000, 2.430221433655941e+001, -4.692372299709454e+001, 5.790326259567788e+001, -4.674759686361156e+001, 2.412012231428596e+001, -7.278092378587070e+000, 9.850706009441995e-001}
                },
                new double[][]{
                        new double[]{3.142743881206541e-003, -2.291883383372955e-002, 7.524393400650334e-002, -1.449219852393075e-001, 1.789112355627868e-001, -1.449219852393075e-001, 7.524393400650334e-002, -2.291883383372955e-002, 3.142743881206540e-003},
                        new double[]{1.000000000000000e+000, -7.284532413376133e+000, 2.388319484571587e+001, -4.592584198278260e+001, 5.659149460855977e+001, -4.574323139402281e+001, 2.369364365886558e+001, -7.197983354359358e+000, 9.841899104156673e-001}
                },
                new double[][]{
                        new double[]{3.141935409696230e-003, -2.264552432784142e-002, 7.376952388809109e-002, -1.414435351904935e-001, 1.743598521445707e-001, -1.414435351904935e-001, 7.376952388809109e-002, -2.264552432784142e-002, 3.141935409696230e-003},
                        new double[]{1.000000000000000e+000, -7.199440243846447e+000, 2.342006007650916e+001, -4.482965744266117e+001, 5.515374882202251e+001, -4.464082837240964e+001, 2.322317837594225e+001, -7.108847932436513e+000, 9.832577110005878e-001}
                },
                new double[][]{
                        new double[]{3.141121810862988e-003, -2.234080412659672e-002, 7.214435970035693e-002, -1.376335506400123e-001, 1.693850668579320e-001, -1.376335506400123e-001, 7.214435970035690e-002, -2.234080412659671e-002, 3.141121810862987e-003},
                        new double[]{1.000000000000000e+000, -7.104372878921088e+000, 2.290910863125306e+001, -4.362850803060598e+001, 5.358222639763521e+001, -4.343383520301292e+001, 2.270512153246824e+001, -7.009696601809882e+000, 9.822710442329992e-001}
                },
                new double[][]{
                        new double[]{3.140307976126783e-003, -2.200119030724153e-002, 7.035674537599862e-002, -1.334722453248443e-001, 1.639642726170729e-001, -1.334722453248443e-001, 7.035674537599862e-002, -2.200119030724154e-002, 3.140307976126782e-003},
                        new double[]{1.000000000000000e+000, -6.998210008037751e+000, 2.234658954035024e+001, -4.231607712696974e+001, 5.186977622103262e+001, -4.211605932017192e+001, 2.213583523342565e+001, -6.899442169873026e+000, 9.812267883503994e-001}
                },
                new double[][]{
                        new double[]{3.139499712905021e-003, -2.162285426944518e-002, 6.839512067458584e-002, -1.289417729867781e-001, 1.580780841620924e-001, -1.289417729867781e-001, 6.839512067458588e-002, -2.162285426944519e-002, 3.139499712905022e-003},
                        new double[]{1.000000000000000e+000, -6.879718989504162e+000, 2.172878796019500e+001, -4.088665281760313e+001, 5.001026253686516e+001, -4.068192840019685e+001, 2.151173558210775e+001, -6.776893166237469e+000, 9.801216500987595e-001}
                },
                new double[][]{
                        new double[]{3.138703873699729e-003, -2.120159865872217e-002, 6.624842494020337e-002, -1.240272346018628e-001, 1.517117468996463e-001, -1.240272346018628e-001, 6.624842494020337e-002, -2.120159865872217e-002, 3.138703873699729e-003},
                        new double[]{1.000000000000000e+000, -6.747547264196246e+000, 2.105214025163815e+001, -3.933544618694712e+001, 4.799900957772635e+001, -3.912680823655984e+001, 2.082940863624978e+001, -6.640747692124121e+000, 9.789521562300233e-001}
                },
                new double[][]{
                        new double[]{3.137928501679498e-003, -2.073283607339804e-002, 6.390657357142512e-002, -1.187178808303045e-001, 1.448568043951183e-001, -1.187178808303045e-001, 6.390657357142512e-002, -2.073283607339804e-002, 3.137928501679498e-003},
                        new double[]{1.000000000000000e+000, -6.600215350011449e+000, 2.031338506624802e+001, -3.765897166957116e+001, 4.583332789420625e+001, -3.744738178488232e+001, 2.008576174391972e+001, -6.489588056709191e+000, 9.777146446947530e-001}
                },
                new double[][]{
                        new double[]{3.137182994768761e-003, -2.021157084921369e-002, 6.136107057341432e-002, -1.130085099447135e-001, 1.375130273952083e-001, -1.130085099447135e-001, 6.136107057341432e-002, -2.021157084921369e-002, 3.137182994768761e-003},
                        new double[]{1.000000000000000e+000, -6.436110845532803e+000, 1.950975797479131e+001, -3.585548965015296e+001, 4.351312333645753e+001, -3.564208929582135e+001, 1.927821746633142e+001, -6.321876645437641e+000, 9.764052555351180e-001}
                },
                new double[][]{
                        new double[]{3.136478290496715e-003, -1.963238561066010e-002, 5.860578298576107e-002, -1.069010432752794e-001, 1.296905894905115e-001, -1.069010432752794e-001, 5.860578298576107e-002, -1.963238561066010e-002, 3.136478290496715e-003},
                        new double[]{1.000000000000000e+000, -6.253484003079926e+000, 1.863923797465455e+001, -3.392550574495158e+001, 4.104158405102899e+001, -3.371162367818436e+001, 1.840495799303931e+001, -6.135953593248776e+000, 9.750199214863040e-001}
                },
                new double[][]{
                        new double[]{3.135827074126856e-003, -1.898943476709113e-002, 5.563790460357721e-002, -1.004062326978215e-001, 1.214124500942118e-001, -1.004062326978215e-001, 5.563790460357720e-002, -1.898943476709113e-002, 3.135827074126855e-003},
                        new double[]{1.000000000000000e+000, -6.050445594454034e+000, 1.770085476591669e+001, -3.187231248663630e+001, 3.842593317714272e+001, -3.165946653481059e+001, 1.746522838225852e+001, -5.930036992274631e+000, 9.735543582978614e-001}
                },
                new double[][]{
                        new double[]{3.135244012888754e-003, -1.827644773199041e-002, 5.245913656145099e-002, -9.354541521470155e-002, 1.127168741389934e-001, -9.354541521470155e-002, 5.245913656145100e-002, -1.827644773199041e-002, 3.135244012888755e-003},
                        new double[]{1.000000000000000e+000, -5.824967992989349e+000, 1.669506577467719e+001, -2.970254661371651e+001, 3.567822510278451e+001, -2.949243785701341e+001, 1.645970690065039e+001, -5.702226553585582e+000, 9.720040547903410e-001}
                },
                new double[][]{
                        new double[]{3.134746019470630e-003, -1.748674537436416e-002, 4.907711006745098e-002, -8.635217549983100e-002, 1.036599808864124e-001, -8.635217549983101e-002, 4.907711006745098e-002, -1.748674537436416e-002, 3.134746019470632e-003},
                        new double[]{1.000000000000000e+000, -5.574890639196458e+000, 1.562421121838405e+001, -2.742671795429572e+001, 3.281615152807541e+001, -2.722121529947888e+001, 1.539094988747847e+001, -5.450511870800915e+000, 9.703642626670224e-001}
                },
                new double[][]{
                        new double[]{3.134352548309350e-003, -1.661327409169017e-002, 4.550707051926735e-002, -7.887370609052574e-002, 9.431817474222426e-002, -7.887370609052574e-002, 4.550707051926735e-002, -1.661327409169017e-002, 3.134352548309349e-003},
                        new double[]{1.000000000000000e+000, -5.297931350649583e+000, 1.449305362107864e+001, -2.505964324185187e+001, 2.986381121167740e+001, -2.486075657853854e+001, 1.426391650604211e+001, -5.172786704431734e+000, 9.686299861056348e-001}
                },
                new double[][]{
                        new double[]{3.134085928638541e-003, -1.564866294308468e-002, 4.177383060073083e-002, -7.117156573103167e-002, 8.479027540628280e-002, -7.117156573103167e-002, 4.177383060073084e-002, -1.564866294308468e-002, 3.134085928638541e-003},
                        new double[]{1.000000000000000e+000, -4.991705285766049e+000, 1.330940451460967e+001, -2.262068977353928e+001, 2.685238618316034e+001, -2.243053056001659e+001, 1.308657490580105e+001, -4.866871023409204e+000, 9.667959711606842e-001}
                },
                new double[][]{
                        new double[]{3.133971738732764e-003, -1.458531051532196e-002, 3.791398038204636e-002, -6.332143101523267e-002, 7.519914376860126e-002, -6.332143101523267e-002, 3.791398038204637e-002, -1.458531051532196e-002, 3.133971738732764e-003},
                        new double[]{1.000000000000000e+000, -4.653753780459771e+000, 1.208483484175470e+001, -2.013370016357715e+001, 2.382066078021547e+001, -1.995442963213826e+001, 1.187058496830032e+001, -4.530542906340166e+000, 9.648566950135764e-001}
                },
                new double[][]{
                        new double[]{3.134039226322169e-003, -1.341550962251499e-002, 3.397831195038829e-002, -5.541132177031712e-002, 6.569260943266918e-002, -5.541132177031712e-002, 3.397831195038829e-002, -1.341550962251499e-002, 3.134039226322169e-003},
                        new double[]{1.000000000000000e+000, -4.281585748495832e+000, 1.083545584338935e+001, -1.762643268940588e+001, 2.081532308871231e+001, -1.746020018575418e+001, 1.063204303178750e+001, -4.161582818466576e+000, 9.628063551150658e-001}
                },
                new double[][]{
                        new double[]{3.134321780757536e-003, -1.213161956026687e-002, 3.003437108339706e-002, -4.753767160414873e-002, 5.644356598658089e-002, -4.753767160414873e-002, 3.003437108339706e-002, -1.213161956026687e-002, 3.134321780757536e-003},
                        new double[]{1.000000000000000e+000, -3.872734868428338e+000, 9.582742796500897e+000, -1.512931661567665e+001, 1.789100765938704e+001, -1.497819422522095e+001, 9.392259689330880e+000, -3.757833234835759e+000, 9.606388582728883e-001}
                },
                new double[][]{
                        new double[]{3.134857463189034e-003, -1.072629742487928e-002, 2.616898486943642e-002, -3.979853915444665e-002, 4.764923335280726e-002, -3.979853915444665e-002, 2.616898486943642e-002, -1.072629742487929e-002, 3.134857463189036e-003},
                        new double[]{1.000000000000000e+000, -3.424836362665022e+000, 8.354353544174989e+000, -1.267329710343471e+001, 1.511008073912571e+001, -1.253922198523032e+001, 8.178521712285495e+000, -3.317277061692937e+000, 9.583478097467505e-001}
                },
                new double[][]{
                        new double[]{3.135689601795952e-003, -9.192801914856020e-003, 2.249052777972339e-002, -3.228325770744059e-002, 3.952980949329304e-002, -3.228325770744058e-002, 2.249052777972340e-002, -9.192801914856018e-003, 3.135689601795953e-003},
                        new double[]{1.000000000000000e+000, -2.935727786560088e+000, 7.184865948619916e+000, -1.028654413040799e+001, 1.254223993346572e+001, -1.017128706803972e+001, 7.024762141100510e+000, -2.838138790755699e+000, 9.559265024233198e-001}
                },
                new double[][]{
                        new double[]{3.136867459982495e-003, -7.525384956076330e-003, 1.913057593393776e-002, -2.505796833460762e-002, 3.232704353501763e-002, -2.505796833460763e-002, 1.913057593393775e-002, -7.525384956076330e-003, 3.136867459982495e-003},
                        new double[]{1.000000000000000e+000, -2.403578849731980e+000, 6.116322021836488e+000, -7.989845724149395e+000, 1.026409819970081e+001, -7.895033186180221e+000, 5.972027625440275e+000, -2.319012762106196e+000, 9.533679061555159e-001}
                },
                new double[][]{
                        new double[]{3.138446986453598e-003, -5.719788266387037e-003, 1.624445853637540e-002, -1.814686749013248e-002, 2.630361280892668e-002, -1.814686749013247e-002, 1.624445853637540e-002, -5.719788266387037e-003, 3.138446986453599e-003},
                        new double[]{1.000000000000000e+000, -1.827055825922347e+000, 5.198420980989066e+000, -5.790627461846007e+000, 8.359032593241114e+000, -5.717854195620133e+000, 5.068588868120159e+000, -1.759023244522344e+000, 9.506646573634056e-001}
                },
                new double[][]{
                        new double[]{3.140491657224234e-003, -3.773863336298222e-003, 1.401004915216966e-002, -1.150974045018174e-002, 2.174445408345166e-002, -1.150974045018174e-002, 1.401004915216966e-002, -3.773863336298222e-003, 3.140491657224234e-003},
                        new double[]{1.000000000000000e+000, -1.205526475294007e+000, 4.488149587584013e+000, -3.675774949486338e+000, 6.917662040320396e+000, -3.626853005615643e+000, 4.369489565598928e+000, -1.158021168308530e+000, 9.478090490084538e-001}
                },
                new double[][]{
                        new double[]{3.143073420920189e-003, -1.688333852644991e-003, 1.262395748797917e-002, -5.017573087974817e-003, 1.896114476128464e-002, -5.017573087974814e-003, 1.262395748797918e-002, -1.688333852644991e-003, 3.143073420920189e-003},
                        new double[]{1.000000000000000e+000, -5.393114608165109e-001, 4.048578996469637e+000, -1.603824537959889e+000, 6.039299478305786e+000, -1.581219217193778e+000, 3.935274955307953e+000, -5.168221229728396e-001, 9.447930210688751e-001}
                },
                new double[][]{
                        new double[]{3.146273760226500e-003, 5.322812464931028e-004, 1.229410658041627e-002, 1.570162041369520e-003, 1.829966777695044e-002, 1.570162041369518e-003, 1.229410658041627e-002, 5.322812464931027e-004, 3.146273760226500e-003},
                        new double[]{1.000000000000000e+000, 1.700122175818912e-001, 3.946500014377160e+000, 5.023368561645096e-001, 5.834479755741241e+000, 4.948389524665608e-001, 3.829590220830910e+000, 1.625105377327541e-001, 9.416081516614648e-001}
                },
                new double[][]{
                        new double[]{3.150184884067147e-003, 2.878568940453699e-003, 1.322755106586022e-002, 8.620552071421581e-003, 2.014990932533000e-002, 8.620552071421581e-003, 1.322755106586022e-002, 2.878568940453698e-003, 3.150184884067147e-003},
                        new double[]{1.000000000000000e+000, 9.192515080342589e-001, 4.248526154251194e+000, 2.760354359206612e+000, 6.428032354219372e+000, 2.716723422282723e+000, 4.115304532346304e+000, 8.763352262803313e-001, 9.382456489747480e-001}
                },
                new double[][]{
                        new double[]{3.154911067098878e-003, 5.334938740144927e-003, 1.561237564818607e-002, 1.662582664159495e-002, 2.495150819282767e-002, 1.662582664159496e-002, 1.561237564818607e-002, 5.334938740144928e-003, 3.154911067098878e-003},
                        new double[]{1.000000000000000e+000, 1.703179499277951e+000, 5.015287582907516e+000, 5.328060497664993e+000, 7.960981372090181e+000, 5.238878049409713e+000, 4.848819300199696e+000, 1.619054784052113e+000, 9.346963441995914e-001}
                },
                new double[][]{
                        new double[]{3.160570155425528e-003, 7.878157153029112e-003, 1.959270862731381e-002, 2.617401829906347e-002, 3.318498160501834e-002, 2.617401829906346e-002, 1.959270862731381e-002, 7.878157153029110e-003, 3.160570155425528e-003},
                        new double[]{1.000000000000000e+000, 2.514061860132557e+000, 6.293401312113454e+000, 8.394231003661758e+000, 1.058774518680232e+001, 8.245442893665601e+000, 6.072290240693297e+000, 2.382697143855915e+000, 9.309506856667303e-001}
                },
                new double[][]{
                        new double[]{3.167295260151064e-003, 1.047572033190279e-002, 2.523643163272042e-002, 3.787881035026627e-002, 4.533007101187137e-002, 3.787881035026630e-002, 2.523643163272043e-002, 1.047572033190280e-002, 3.167295260151064e-003},
                        new double[]{1.000000000000000e+000, 3.341136359577790e+000, 8.105078676150034e+000, 1.215629189647473e+001, 1.446288885419975e+001, 1.192812032225852e+001, 7.803674281773004e+000, 3.156465395399920e+000, 9.269987344260360e-001}
                },
                new double[][]{
                        new double[]{3.175236663568253e-003, 1.308411231056350e-002, 3.249623781842040e-002, 5.225727921334373e-002, 6.176741363408551e-002, 5.225727921334372e-002, 3.249623781842039e-002, 1.308411231056350e-002, 3.175236663568252e-003},
                        new double[]{1.000000000000000e+000, 4.170063916809950e+000, 1.043557693581984e+001, 1.678131188069654e+001, 1.970982407256151e+001, 1.644776743866162e+001, 1.002485619533124e+001, 3.926277990238661e+000, 9.228301615299103e-001}
                },
                new double[][]{
                        new double[]{3.184563966522060e-003, 1.564703528332044e-002, 4.116647822650329e-002, 6.955135546761315e-002, 8.260002997525108e-002, 6.955135546761315e-002, 4.116647822650329e-002, 1.564703528332044e-002, 3.184563966522059e-003},
                        new double[]{1.000000000000000e+000, 4.982383252986069e+000, 1.321928002312587e+001, 2.234885788249895e+001, 2.636396393156668e+001, 2.187848507504305e+001, 1.266867131040727e+001, 4.674332629971478e+000, 9.184342473129028e-001}
                },
                new double[][]{
                        new double[]{3.195468509911748e-003, 1.809375255989474e-002, 5.084090030653590e-002, 8.950422298444746e-002, 1.073856458036988e-001, 8.950422298444746e-002, 5.084090030653590e-002, 1.809375255989474e-002, 3.195468509911749e-003},
                        new double[]{1.000000000000000e+000, 5.755019760051603e+000, 1.632605611212203e+001, 2.877918880332384e+001, 3.428743475883073e+001, 2.813781188877264e+001, 1.560646217523939e+001, 5.378744289396370e+000, 9.137998829916308e-001}
                },
                new double[][]{
                        new double[]{3.208166108549520e-003, 2.033775720013014e-002, 6.087995852729569e-002, 1.111283766985333e-001, 1.348084233730102e-001, 1.111283766985333e-001, 6.087995852729569e-002, 2.033775720013014e-002, 3.208166108549520e-003},
                        new double[]{1.000000000000000e+000, 6.459923452852331e+000, 1.955069789760037e+001, 3.575841878007016e+001, 4.306434589484458e+001, 3.491460431644380e+001, 1.863888825236724e+001, 6.013329244005268e+000, 9.089155749432362e-001}
                },
                new double[][]{
                        new double[]{3.222900141858363e-003, 2.227607222988250e-002, 7.040061526828756e-002, 1.325362521214649e-001, 1.623833078994801e-001, 1.325362521214649e-001, 7.040061526828756e-002, 2.227607222988250e-002, 3.222900141858363e-003},
                        new double[]{1.000000000000000e+000, 7.063941201586085e+000, 2.260962332507321e+001, 4.268327468011344e+001, 5.190611606085582e+001, 4.161681749209129e+001, 2.149395668247145e+001, 6.547632809776028e+000, 9.037694520569339e-001}
                },
                new double[][]{
                        new double[]{3.239945053390167e-003, 2.378960212591439e-002, 7.830568646607240e-002, 1.509391118013942e-001, 1.863663521383706e-001, 1.509391118013942e-001, 7.830568646607242e-002, 2.378960212591439e-002, 3.239945053390167e-003},
                        new double[]{1.000000000000000e+000, 7.529064346833643e+000, 2.515035008465618e+001, 4.865934114163024e+001, 5.962181888765429e+001, 4.737213222877233e+001, 2.383740644607056e+001, 6.947327741252297e+000, 8.983492765915317e-001}
                },
                new double[][]{
                        new double[]{3.259610320154749e-003, 2.474508638385797e-002, 8.337226016441471e-002, 1.629367425557876e-001, 2.020999170550666e-001, 1.629367425557876e-001, 8.337226016441469e-002, 2.474508638385797e-002, 3.259610320154748e-003},
                        new double[]{1.000000000000000e+000, 7.813232181445080e+000, 2.678004704072203e+001, 5.259232996155322e+001, 6.472625974952638e+001, 5.111941179551022e+001, 2.530116502103031e+001, 7.175140285956256e+000, 8.926424590119200e-001}
                },
        };
    }

    private static class MIDI_COEFFICIENTS_SR4410HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.161593916254468e-003, -2.529101827223581e-002, 8.851423166213471e-002, -1.770232648584561e-001, 2.212769151046056e-001, -1.770232648584561e-001, 8.851423166213471e-002, -2.529101827223581e-002, 3.161593916254467e-003},
                        new double[]{1.000000000000000e+000, -7.999017087285694e+000, 2.799366272412529e+001, -5.598261711166223e+001, 6.997374286196239e+001, -5.597645675867950e+001, 2.798750219201668e+001, -7.996376731285144e+000, 9.995599108082113e-001}
                },
                new double[][]{
                        new double[]{3.161553062446479e-003, -2.529047923592975e-002, 8.851181461241352e-002, -1.770177943253849e-001, 2.212698117729093e-001, -1.770177943253849e-001, 8.851181461241352e-002, -2.529047923592974e-002, 3.161553062446478e-003},
                        new double[]{1.000000000000000e+000, -7.998924451880028e+000, 2.799308081132896e+001, -5.598107088708218e+001, 6.997149822054516e+001, -5.597454442206223e+001, 2.798655413329785e+001, -7.996127151992318e+000, 9.995337478447833e-001}
                },
                new double[][]{
                        new double[]{3.161509812868865e-003, -2.528989505177274e-002, 8.850917461487780e-002, -1.770117999993947e-001, 2.212620212468415e-001, -1.770117999993947e-001, 8.850917461487781e-002, -2.528989505177274e-002, 3.161509812868865e-003},
                        new double[]{1.000000000000000e+000, -7.998822121735933e+000, 2.799243918899172e+001, -5.597936997342470e+001, 6.996903646848112e+001, -5.597245565865120e+001, 2.798552462091247e+001, -7.995858554471205e+000, 9.995060298977045e-001}
                },
                new double[][]{
                        new double[]{3.161464029165647e-003, -2.528926143222603e-002, 8.850628871762561e-002, -1.770052264470284e-001, 2.212534702649263e-001, -1.770052264470284e-001, 8.850628871762561e-002, -2.528926143222603e-002, 3.161464029165648e-003},
                        new double[]{1.000000000000000e+000, -7.998709007798092e+000, 2.799173123356117e+001, -5.597749749444398e+001, 6.996633446283460e+001, -5.597017230504635e+001, 2.798440574294180e+001, -7.995569296643458e+000, 9.994766645943051e-001}
                },
                new double[][]{
                        new double[]{3.161415565256061e-003, -2.528857363865507e-002, 8.850313141828986e-002, -1.769980119692333e-001, 2.212440772486848e-001, -1.769980119692333e-001, 8.850313141828987e-002, -2.528857363865507e-002, 3.161415565256062e-003},
                        new double[]{1.000000000000000e+000, -7.998583893502143e+000, 2.799094955168246e+001, -5.597543463153167e+001, 6.996336643358228e+001, -5.596767417988971e+001, 2.798318874184015e+001, -7.995257576261720e+000, 9.994455540803483e-001}
                },
                new double[][]{
                        new double[]{3.161364266936658e-003, -2.528782643051617e-002, 8.849967436753572e-002, -1.769900878663036e-001, 2.212337513246947e-001, -1.769900878663036e-001, 8.849967436753575e-002, -2.528782643051617e-002, 3.161364266936657e-003},
                        new double[]{1.000000000000000e+000, -7.998445419512772e+000, 2.799008588847668e+001, -5.597316039358141e+001, 6.996010367472567e+001, -5.596493884940269e+001, 2.798186391834275e+001, -7.994921413743657e+000, 9.994125946954514e-001}
                },
                new double[][]{
                        new double[]{3.161309971467286e-003, -2.528701400859047e-002, 8.849588603727337e-002, -1.769813776150009e-001, 2.212223912297014e-001, -1.769813776150009e-001, 8.849588603727339e-002, -2.528701400859047e-002, 3.161309971467286e-003},
                        new double[]{1.000000000000000e+000, -7.998292066614896e+000, 2.798913102476083e+001, -5.597065135921200e+001, 6.995651419846419e+001, -5.596194136505488e+001, 2.798042052407982e+001, -7.994558633052427e+000, 9.993776766293693e-001}
                },
                new double[][]{
                        new double[]{3.161252507140881e-003, -2.528612995155339e-002, 8.849173134934443e-002, -1.769717959472906e-001, 2.212098840847174e-001, -1.769717959472906e-001, 8.849173134934443e-002, -2.528612995155340e-002, 3.161252507140881e-003},
                        new double[]{1.000000000000000e+000, -7.998122136533355e+000, 2.798807466187124e+001, -5.596788138801683e+001, 6.995256234798947e+001, -5.595865397002451e+001, 2.797884664154993e+001, -7.994166840393976e+000, 9.993406835580333e-001}
                },
                new double[][]{
                        new double[]{3.161191692837110e-003, -2.528516714508202e-002, 8.848717125993194e-002, -1.769612478188733e-001, 2.211961040223725e-001, -1.769612478188732e-001, 8.848717125993193e-002, -2.528516714508202e-002, 3.161191692837109e-003},
                        new double[]{1.000000000000000e+000, -7.997933730429132e+000, 2.798690529259037e+001, -5.596482129710677e+001, 6.994820836392856e+001, -5.595504577071011e+001, 2.797712904994279e+001, -7.993743400473845e+000, 9.993014922581553e-001}
                },
                new double[][]{
                        new double[]{3.161127337560148e-003, -2.528411770260749e-002, 8.848216229437571e-002, -1.769496272542766e-001, 2.211809106498965e-001, -1.769496272542766e-001, 8.848216229437571e-002, -2.528411770260749e-002, 3.161127337560148e-003},
                        new double[]{1.000000000000000e+000, -7.997724724789534e+000, 2.798561005649652e+001, -5.596143849876702e+001, 6.994340789887144e+001, -5.595108236910750e+001, 2.797525307512261e+001, -7.993285410025651e+000, 9.992599721991516e-001}
                },
                new double[][]{
                        new double[]{3.161059239960843e-003, -2.528297287671021e-002, 8.847665602643158e-002, -1.769368160536879e-001, 2.211641473280116e-001, -1.769368160536879e-001, 8.847665602643158e-002, -2.528297287671022e-002, 3.161059239960843e-003},
                        new double[]{1.000000000000000e+000, -7.997492744395283e+000, 2.798417457785312e+001, -5.595769659454857e+001, 6.993811147375503e+001, -5.594672545136710e+001, 2.797320242188069e+001, -7.992789668288936e+000, 9.992159851110762e-001}
                },
                new double[][]{
                        new double[]{3.160987187843867e-003, -2.528172296003670e-002, 8.847059849530557e-002, -1.769226823449516e-001, 2.211456392436778e-001, -1.769226823449516e-001, 8.847059849530559e-002, -2.528172296003670e-002, 3.160987187843867e-003},
                        new double[]{1.000000000000000e+000, -7.997235132008951e+000, 2.798258278392826e+001, -5.595355492055626e+001, 6.993226386913460e+001, -5.594193232728965e+001, 2.797095898634032e+001, -7.992252644075421e+000, 9.991693845271448e-001}
                },
                new double[][]{
                        new double[]{3.160910957660547e-003, -2.528035717447984e-002, 8.846392955299175e-002, -1.769070789621774e-001, 2.211251912520102e-001, -1.769070789621774e-001, 8.846392955299175e-002, -2.528035717447984e-002, 3.160910957660547e-003},
                        new double[]{1.000000000000000e+000, -7.996948914385899e+000, 2.798081670138147e+001, -5.594896803807287e+001, 6.992580344354946e+001, -5.593665541489734e+001, 2.796850264614566e+001, -7.991670439019794e+000, 9.991200152994048e-001}
                },
                new double[][]{
                        new double[]{3.160830313988396e-003, -2.527886354721426e-002, 8.845658213355438e-002, -1.768898416302190e-001, 2.211025854597813e-001, -1.768898416302190e-001, 8.845658213355440e-002, -2.527886354721427e-002, 3.160830313988396e-003},
                        new double[]{1.000000000000000e+000, -7.996630764160401e+000, 2.797885622807235e+001, -5.594388516296320e+001, 6.991866137026807e+001, -5.593084166352424e+001, 2.796581102578429e+001, -7.991038746562714e+000, 9.990677130859730e-001}
                },
                new double[][]{
                        new double[]{3.160745008998582e-003, -2.527722877200765e-002, 8.844848143499869e-002, -1.768707869318240e-001, 2.210775785196694e-001, -1.768707869318240e-001, 8.844848143499867e-002, -2.527722877200766e-002, 3.160745008998582e-003},
                        new double[]{1.000000000000000e+000, -7.996276957105287e+000, 2.797667887732879e+001, -5.593824952652658e+001, 6.991076078267308e+001, -5.592443190809767e+001, 2.796285923407937e+001, -7.990352806159700e+000, 9.990123038082130e-001}
                },
                new double[][]{
                        new double[]{3.160654781912930e-003, -2.527543805403891e-002, 8.843954400326257e-002, -1.768497100315384e-001, 2.210498986008047e-001, -1.768497100315383e-001, 8.843954400326257e-002, -2.527543805403891e-002, 3.160654781912930e-003},
                        new double[]{1.000000000000000e+000, -7.995883324202469e+000, 2.797425949135969e+001, -5.593199765960337e+001, 6.990201581740493e+001, -5.591736014642182e+001, 2.795961957053631e+001, -7.989607352149015e+000, 9.989536030760980e-001}
                },
                new double[][]{
                        new double[]{3.160559358452429e-003, -2.527347493624133e-002, 8.842967670662490e-002, -1.768263821274089e-001, 2.210192419971476e-001, -1.768263821274089e-001, 8.842967670662488e-002, -2.527347493624133e-002, 3.160559358452428e-003},
                        new double[]{1.000000000000000e+000, -7.995445197893488e+000, 2.797156992010315e+001, -5.592505859077960e+001, 6.989233054311535e+001, -5.590955273031947e+001, 2.795606119683879e+001, -7.988796556644120e+000, 9.988914155799424e-001}
                },
                new double[][]{
                        new double[]{3.160458450279628e-003, -2.527132110494989e-002, 8.841877558744213e-002, -1.768005475981578e-001, 2.209852693307746e-001, -1.768005475981578e-001, 8.841877558744214e-002, -2.527132110494989e-002, 3.160458450279627e-003},
                        new double[]{1.000000000000000e+000, -7.994957351802661e+000, 2.796857866136127e+001, -5.591735294846595e+001, 6.988159776127253e+001, -5.590092746042342e+001, 2.795214976935288e+001, -7.987913965740336e+000, 9.988255344465596e-001}
                },
                new double[][]{
                        new double[]{3.160351754437832e-003, -2.526895617236514e-002, 8.840672457658853e-002, -1.767719208097601e-001, 2.209476013022021e-001, -1.767719208097601e-001, 8.840672457658852e-002, -2.526895617236515e-002, 3.160351754437832e-003},
                        new double[]{1.000000000000000e+000, -7.994413933139685e+000, 2.796525045758338e+001, -5.590879195544071e+001, 6.986969766389852e+001, -5.589139257323082e+001, 2.794784702801366e+001, -7.986952428240766e+000, 9.987557405577985e-001}
                },
                new double[][]{
                        new double[]{3.160238952790540e-003, -2.526635743304703e-002, 8.839339405426234e-002, -1.767401825412063e-001, 2.209058139344079e-001, -1.767401825412063e-001, 8.839339405426234e-002, -2.526635743304703e-002, 3.160238952790540e-003},
                        new double[]{1.000000000000000e+000, -7.993808386892582e+000, 2.796154584411398e+001, -5.589927630313153e+001, 6.985649633139039e+001, -5.588084560772559e+001, 2.794311033643001e+001, -7.985904016011802e+000, 9.986818018293174e-001}
                },
                new double[][]{
                        new double[]{3.160119711465250e-003, -2.526349959131741e-002, 8.837863923891780e-002, -1.767049759846373e-001, 2.208594332511544e-001, -1.767049759846373e-001, 8.837863923891778e-002, -2.526349959131740e-002, 3.160119711465249e-003},
                        new double[]{1.000000000000000e+000, -7.993133370814285e+000, 2.795742064311580e+001, -5.588869489145412e+001, 6.984184405166565e+001, -5.586917213742809e+001, 2.793789216744373e+001, -7.984759934972510e+000, 9.986034724473059e-001}
                },
                new double[][]{
                        new double[]{3.159993680306473e-003, -2.526035445607623e-002, 8.836229838397044e-002, -1.766659022699698e-001, 2.208079293235557e-001, -1.766659022699698e-001, 8.836229838397044e-002, -2.526035445607623e-002, 3.159993680306474e-003},
                        new double[]{1.000000000000000e+000, -7.992380660085850e+000, 2.795282539670441e+001, -5.587692341841763e+001, 6.982557343976634e+001, -5.585624435213430e+001, 2.793213952771580e+001, -7.983510425604034e+000, 9.985204920607722e-001}
                },
                new double[][]{
                        new double[]{3.159860492343657e-003, -2.525689059911731e-002, 8.834419075958098e-002, -1.766225154585356e-001, 2.207507096114842e-001, -1.766225154585356e-001, 8.834419075958097e-002, -2.525689059911731e-002, 3.159860492343657e-003},
                        new double[]{1.000000000000000e+000, -7.991541040404488e+000, 2.794770473208404e+001, -5.586382280193163e+001, 6.980749733473664e+001, -5.584191947184556e+001, 2.792579331417567e+001, -7.982144651732744e+000, 9.984325849268532e-001}
                },
                new double[][]{
                        new double[]{3.159719763280717e-003, -2.525307297256270e-002, 8.832411439423189e-002, -1.765743169441132e-001, 2.206871115183708e-001, -1.765743169441133e-001, 8.832411439423192e-002, -2.525307297256270e-002, 3.159719763280717e-003},
                        new double[]{1.000000000000000e+000, -7.990604188093956e+000, 2.794199665064674e+001, -5.584923741429866e+001, 6.978740644804773e+001, -5.582603797345720e+001, 2.791878759435619e+001, -7.980650576193400e+000, 9.983394590065021e-001}
                },
                new double[][]{
                        new double[]{3.159571091014891e-003, -2.524886248051078e-002, 8.830184354794433e-002, -1.765207491929835e-001, 2.206163940691402e-001, -1.765207491929835e-001, 8.830184354794433e-002, -2.524886248051078e-002, 3.159571091014891e-003},
                        new double[]{1.000000000000000e+000, -7.989558535666231e+000, 2.793563173208197e+001, -5.583299310773469e+001, 6.976506673506189e+001, -5.580842160865240e+001, 2.791104880173756e+001, -7.979014821813688e+000, 9.982408050077310e-001}
                },
                new double[][]{
                        new double[]{3.159414055193948e-003, -2.524421549941024e-002, 8.827712588582676e-002, -1.764611887472768e-001, 2.205377286114439e-001, -1.764611887472768e-001, 8.827712588582676e-002, -2.524421549941025e-002, 3.159414055193949e-003},
                        new double[]{1.000000000000000e+000, -7.988391121074918e+000, 2.792853224353422e+001, -5.581489500692074e+001, 6.974021645799945e+001, -5.578887118913256e+001, 2.790249483623393e+001, -7.977222515977711e+000, 9.981362953734644e-001}
                },
                new double[][]{
                        new double[]{3.159248216822215e-003, -2.523908334102008e-002, 8.824967931717416e-002, -1.763949384078660e-001, 2.204501884299558e-001, -1.763949384078660e-001, 8.824967931717419e-002, -2.523908334102009e-002, 3.159248216822216e-003},
                        new double[]{1.000000000000000e+000, -7.987087418690350e+000, 2.792061114273534e+001, -5.579472504203896e+001, 6.971256290558816e+001, -5.576716411279639e+001, 2.789303405886813e+001, -7.975257116821023e+000, 9.980255832108814e-001}
                },
                new double[][]{
                        new double[]{3.159073117927404e-003, -2.523341165108801e-002, 8.821918846152486e-002, -1.763212185043638e-001, 2.203527371522792e-001, -1.763212185043639e-001, 8.821918846152486e-002, -2.523341165108799e-002, 3.159073117927403e-003},
                        new double[]{1.000000000000000e+000, -7.985631149791122e+000, 2.791177096281825e+001, -5.577223919298618e+001, 6.968177873103369e+001, -5.574305160175234e+001, 2.788256416848884e+001, -7.973100218881590e+000, 9.979083011589901e-001}
                },
                new double[][]{
                        new double[]{3.158888281302154e-003, -2.522713973606934e-002, 8.818530069892069e-002, -1.762391571504379e-001, 2.202442158130131e-001, -1.762391571504379e-001, 8.818530069892069e-002, -2.522713973606934e-002, 3.158888281302154e-003},
                        new double[]{1.000000000000000e+000, -7.984004070104426e+000, 2.790190256518441e+001, -5.574716441247836e+001, 6.964749786615431e+001, -5.571625562011219e+001, 2.787097094708239e+001, -7.970731335776597e+000, 9.977840601909688e-001}
                },
                new double[][]{
                        new double[]{3.158693210336190e-003, -2.522019980930472e-002, 8.814762175708106e-002, -1.761477793726378e-001, 2.201233284297557e-001, -1.761477793726378e-001, 8.814762175708105e-002, -2.522019980930472e-002, 3.158693210336189e-003},
                        new double[]{1.000000000000000e+000, -7.982185731634832e+000, 2.789088374534256e+001, -5.571919519257318e+001, 6.960931096549196e+001, -5.568646543637601e+001, 2.785812685881734e+001, -7.968127657192499e+000, 9.976524483476704e-001}
                },
                new double[][]{
                        new double[]{3.158487388957364e-003, -2.521251614706886e-002, 8.810571078328787e-002, -1.760459949903133e-001, 2.199886259313930e-001, -1.760459949903132e-001, 8.810571078328785e-002, -2.521251614706886e-002, 3.158487388957364e-003},
                        new double[]{1.000000000000000e+000, -7.980153215695061e+000, 2.787857767505782e+001, -5.568798973575105e+001, 6.956676032996681e+001, -5.565333379188986e+001, 2.784388948642809e+001, -7.965263777161316e+000, 9.975130293984810e-001}
                },
                new double[][]{
                        new double[]{3.158270281702517e-003, -2.520400414378463e-002, 8.805907484348342e-002, -1.759325851130478e-001, 2.198384882650691e-001, -1.759325851130477e-001, 8.805907484348341e-002, -2.520400414378463e-002, 3.158270281702517e-003},
                        new double[]{1.000000000000000e+000, -7.977880833689168e+000, 2.786483116244711e+001, -5.565316568813741e+001, 6.951933425523487e+001, -5.561647263336882e+001, 2.782809978689696e+001, -7.962111390247397e+000, 9.973653414255297e-001}
                },
                new double[][]{
                        new double[]{3.158041333942031e-003, -2.519456925445314e-002, 8.800716278541246e-002, -1.758061871104754e-001, 2.196711044939669e-001, -1.758061871104754e-001, 8.800716278541246e-002, -2.519456925445314e-002, 3.158041333942031e-003},
                        new double[]{1.000000000000000e+000, -7.975339791794704e+000, 2.784947270982959e+001, -5.561429538875487e+001, 6.946646074539751e+001, -5.557544836386473e+001, 2.781058014663365e+001, -7.958638951882147e+000, 9.972088953270691e-001}
                },
                new double[][]{
                        new double[]{3.157799972285370e-003, -2.518410581097227e-002, 8.794935839662138e-002, -1.756652778975781e-001, 2.194844506837603e-001, -1.756652778975781e-001, 8.794935839662137e-002, -2.518410581097226e-002, 3.157799972285370e-003},
                        new double[]{1.000000000000000e+000, -7.972497815241868e+000, 2.783231034719847e+001, -5.557090058492724e+001, 6.940750052820565e+001, -5.552977656288718e+001, 2.779113221448716e+001, -7.954811298656081e+000, 9.970431732356129e-001}
                },
                new double[][]{
                        new double[]{3.157545605198718e-003, -2.517249569748762e-002, 8.788497278180067e-002, -1.755081553668454e-001, 2.192762653617654e-001, -1.755081553668454e-001, 8.788497278180069e-002, -2.517249569748763e-002, 3.157545605198719e-003},
                        new double[]{1.000000000000000e+000, -7.969318727389940e+000, 2.781312921713376e+001, -5.552244656019855e+001, 6.934173930352458e+001, -5.547891612273882e+001, 2.776953448897178e+001, -7.950589223904405e+000, 9.968676268462359e-001}
                },
                new double[][]{
                        new double[]{3.157277623870033e-003, -2.515960686822934e-002, 8.781323587739391e-002, -1.753329177873968e-001, 2.190440223199878e-001, -1.753329177873968e-001, 8.781323587739394e-002, -2.515960686822935e-002, 3.157277623870033e-003},
                        new double[]{1.000000000000000e+000, -7.965761978250091e+000, 2.779168888484717e+001, -5.546833561749963e+001, 6.926837915277176e+001, -5.542226274462711e+001, 2.774553963406962e+001, -7.945929003400094e+000, 9.966816756501863e-001}
                },
                new double[][]{
                        new double[]{3.156995403361866e-003, -2.514529168942255e-002, 8.773328701470844e-002, -1.751374409808414e-001, 2.187849005222595e-001, -1.751374409808415e-001, 8.773328701470844e-002, -2.514529168942255e-002, 3.156995403361866e-003},
                        new double[]{1.000000000000000e+000, -7.961782116494210e+000, 2.776772034487254e+001, -5.540789985695334e+001, 6.918652903355432e+001, -5.535914173492657e+001, 2.771887149592420e+001, -7.940781865394065e+000, 9.964847050687767e-001}
                },
                new double[][]{
                        new double[]{3.156698304097594e-003, -2.512938508480151e-002, 8.764416443609210e-002, -1.749193530749411e-001, 2.184957508674631e-001, -1.749193530749411e-001, 8.764416443609212e-002, -2.512938508480151e-002, 3.156698304097594e-003},
                        new double[]{1.000000000000000e+000, -7.957328198316873e+000, 2.774092269373505e+001, -5.534039318486957e+001, 6.909519428114079e+001, -5.528880003930368e+001, 2.768922179070990e+001, -7.935093398610544e+000, 9.962760644822535e-001}
                },
                new double[][]{
                        new double[]{3.156385673732920e-003, -2.511170246199721e-002, 8.754479366231135e-002, -1.746760066299887e-001, 2.181730595568746e-001, -1.746760066299887e-001, 8.754479366231135e-002, -2.511170246199721e-002, 3.156385673732920e-003},
                        new double[]{1.000000000000000e+000, -7.952343125777372e+000, 2.771095943581639e+001, -5.526498248843188e+001, 6.899326503713729e+001, -5.521039745057466e+001, 2.765624643202461e+001, -7.928802891114996e+000, 9.960550651481253e-001}
                },
                new double[][]{
                        new double[]{3.156056849471511e-003, -2.509203739459069e-002, 8.743397460336610e-002, -1.744044479303707e-001, 2.178129078156267e-001, -1.744044479303707e-001, 8.743397460336609e-002, -2.509203739459069e-002, 3.156056849471511e-003},
                        new double[]{1.000000000000000e+000, -7.946762906436165e+000, 2.767745438767212e+001, -5.518073790968374e+001, 6.887950352634795e+001, -5.512299692551164e+001, 2.761956146439795e+001, -7.921842592217633e+000, 9.958209780031876e-001}
                },
                new double[][]{
                        new double[]{3.155711160891354e-003, -2.507015903191721e-002, 8.731036729999446e-002, -1.741013832365857e-001, 2.174109277284708e-001, -1.741013832365857e-001, 8.731036729999449e-002, -2.507015903191722e-002, 3.155711160891355e-003},
                        new double[]{1.000000000000000e+000, -7.940515825210965e+000, 2.763998714437387e+001, -5.508662215317161e+001, 6.875253010600088e+001, -5.502555394683704e+001, 2.757873856806594e+001, -7.914136888755846e+000, 9.955730313432369e-001}
                },
                new double[][]{
                        new double[]{3.155347933357245e-003, -2.504580920576140e-002, 8.717247617958086e-002, -1.737631418029930e-001, 2.169622539712598e-001, -1.737631418029930e-001, 8.717247617958084e-002, -2.504580920576139e-002, 3.155347933357244e-003},
                        new double[]{1.000000000000000e+000, -7.933521518408702e+000, 2.759808807020598e+001, -5.498147876460376e+001, 6.861080801821052e+001, -5.491690486996239e+001, 2.753330009919890e+001, -7.905601386211856e+000, 9.953104083742229e-001}
                },
                new double[][]{
                        new double[]{3.154966492104676e-003, -2.501869919989346e-002, 8.701863270876069e-002, -1.733856354862595e-001, 2.164614712554956e-001, -1.733856354862595e-001, 8.701863270876067e-002, -2.501869919989346e-002, 3.154966492104676e-003},
                        new double[]{1.000000000000000e+000, -7.925689938837876e+000, 2.755123277545729e+001, -5.486401932392693e+001, 6.845262678787009e+001, -5.479575420042791e+001, 2.748271362950386e+001, -7.896141884167316e+000, 9.950322446283145e-001}
                },
                new double[][]{
                        new double[]{3.154566167091586e-003, -2.498850614496175e-002, 8.684697632660542e-002, -1.729643148019982e-001, 2.159025573584216e-001, -1.729643148019982e-001, 8.684697632660540e-002, -2.498850614496174e-002, 3.154566167091585e-003},
                        new double[]{1.000000000000000e+000, -7.916920199771112e+000, 2.749883604139448e+001, -5.473280950629787e+001, 6.827608422555267e+001, -5.466066075847723e+001, 2.742638594983942e+001, -7.885653234576883e+000, 9.947376252381477e-001}
                },
                new double[][]{
                        new double[]{3.154146298726754e-003, -2.495486899758022e-002, 8.665543354800535e-002, -1.724941213363811e-001, 2.152788216911153e-001, -1.724941213363811e-001, 8.665543354800535e-002, -2.495486899758022e-002, 3.154146298726754e-003},
                        new double[]{1.000000000000000e+000, -7.907099284310671e+000, 2.744024515714226e+001, -5.458625397977642e+001, 6.807906702022626e+001, -5.451002270300110e+001, 2.736365650454474e+001, -7.874018070261074e+000, 9.944255820622409e-001}
                },
                new double[][]{
                        new double[]{3.153706244597830e-003, -2.491738405855074e-002, 8.644169513822392e-002, -1.719694364904847e-001, 2.145828394688449e-001, -1.719694364904847e-001, 8.644169513822393e-002, -2.491738405855074e-002, 3.153706244597829e-003},
                        new double[]{1.000000000000000e+000, -7.896100605415455e+000, 2.737473263559358e+001, -5.442258013074329e+001, 6.785922994185293e+001, -5.434206140975489e+001, 2.729379022706303e+001, -7.861105389884345e+000, 9.940950906543238e-001}
                },
                new double[][]{
                        new double[]{3.153245387337630e-003, -2.487559998106400e-002, 8.620319127842917e-002, -1.713840266338428e-001, 2.138063817001059e-001, -1.713840266338428e-001, 8.620319127842917e-002, -2.487559998106400e-002, 3.153245387337629e-003},
                        new double[]{1.000000000000000e+000, -7.883782400484689e+000, 2.730148828121852e+001, -5.423982063896797e+001, 6.761397372194872e+001, -5.415480423021781e+001, 2.721596975372922e+001, -7.846768984509151e+000, 9.937450670691137e-001}
                },
                new double[][]{
                        new double[]{3.152763143785002e-003, -2.482901221547677e-002, 8.593706467068338e-002, -1.707309848775805e-001, 2.129403414136488e-001, -1.707309848775804e-001, 8.593706467068339e-002, -2.482901221547678e-002, 3.152763143785002e-003},
                        new double[]{1.000000000000000e+000, -7.869985942975693e+000, 2.721961059148515e+001, -5.403579496627272e+001, 6.734042174419361e+001, -5.394606620005350e+001, 2.712928700203926e+001, -7.830845689619673e+000, 9.933743644967961e-001}
                },
                new double[][]{
                        new double[]{3.152258975616527e-003, -2.477705683293791e-002, 8.564014157237369e-002, -1.700026698556523e-001, 2.119746568089470e-001, -1.700026698556523e-001, 8.564014157237367e-002, -2.477705683293791e-002, 3.152258975616527e-003},
                        new double[]{1.000000000000000e+000, -7.854533552082533e+000, 2.712809748649418e+001, -5.380808987872892e+001, 6.703539576115764e+001, -5.371343082271932e+001, 2.703273411327409e+001, -7.813153445317708e+000, 9.929817697183038e-001}
                },
                new double[][]{
                        new double[]{3.151732401647415e-003, -2.471910366582464e-002, 8.530890080810991e-002, -1.691906421359926e-001, 2.108982323597847e-001, -1.691906421359926e-001, 8.530890080810992e-002, -2.471910366582464e-002, 3.151732401647415e-003},
                        new double[]{1.000000000000000e+000, -7.837226380050281e+000, 2.702583637954851e+001, -5.355403919573403e+001, 6.669539096183048e+001, -5.345423012777539e+001, 2.692519377819050e+001, -7.793489146244224e+000, 9.925659993732361e-001}
                },
                new double[][]{
                        new double[]{3.151183012025049e-003, -2.465444869879744e-002, 8.493944088636336e-002, -1.682855991848184e-001, 2.096988593404761e-001, -1.682855991848184e-001, 8.493944088636336e-002, -2.465444869879745e-002, 3.151183012025050e-003},
                        new double[]{1.000000000000000e+000, -7.817841955290685e+000, 2.691159362620951e+001, -5.327070305427880e+001, 6.631655085343974e+001, -5.316552429896600e+001, 2.680542899008685e+001, -7.771626261725299e+000, 9.921256960321099e-001}
                },
                new double[][]{
                        new double[]{3.150610484566584e-003, -2.458230564046940e-002, 8.452744545432143e-002, -1.672773101921885e-001, 2.083631377998730e-001, -1.672773101921885e-001, 8.452744545432143e-002, -2.458230564046940e-002, 3.150610484566584e-003},
                        new double[]{1.000000000000000e+000, -7.796131458159389e+000, 2.678400342277183e+001, -5.295484709807280e+001, 6.589464259638874e+001, -5.284408128897667e+001, 2.667207230371726e+001, -7.747312205747816e+000, 9.916594240644637e-001}
                },
                new double[][]{
                        new double[]{3.150014603523351e-003, -2.450179660246599e-002, 8.406814746467828e-002, -1.661545525528713e-001, 2.068764027026764e-001, -1.661545525528713e-001, 8.406814746467828e-002, -2.450179660246598e-002, 3.150014603523351e-003},
                        new double[]{1.000000000000000e+000, -7.771816705134682e+000, 2.664155626928255e+001, -5.260292215457298e+001, 6.542503365027984e+001, -5.248635699146607e+001, 2.652361472349708e+001, -7.720265435728679e+000, 9.911656652942074e-001}
                },
                new double[][]{
                        new double[]{3.149395281089990e-003, -2.441194181035762e-002, 8.355629261105317e-002, -1.649050524036589e-001, 2.052226578160066e-001, -1.649050524036589e-001, 8.355629261105317e-002, -2.441194181035762e-002, 3.149395281089989e-003},
                        new double[]{1.000000000000000e+000, -7.744586816311307e+000, 2.648258717001638e+001, -5.221104515472743e+001, 6.490267085990986e+001, -5.208847673280339e+001, 2.635839440293795e+001, -7.690172258768459e+000, 9.906428144335239e-001}
                },
                new double[][]{
                        new double[]{3.148752582015607e-003, -2.431164827001342e-002, 8.298610282495890e-002, -1.635154323685492e-001, 2.033845219686780e-001, -1.635154323685492e-001, 8.298610282495890e-002, -2.431164827001342e-002, 3.148752582015607e-003},
                        new double[]{1.000000000000000e+000, -7.714094540733340e+000, 2.630526381907285e+001, -5.177498228744085e+001, 6.432206344135321e+001, -5.164621908281710e+001, 2.617458541257556e+001, -7.656683324328320e+000, 9.900891742865688e-001}
                },
                new double[][]{
                        new double[]{3.148086751718359e-003, -2.419969731391642e-002, 8.235124092942395e-002, -1.619711705800846e-001, 2.013431935785155e-001, -1.619711705800846e-001, 8.235124092942393e-002, -2.419969731391643e-002, 3.148086751718360e-003},
                        new double[]{1.000000000000000e+000, -7.679952214323401e+000, 2.610757511455849e+001, -5.129013567086449e+001, 6.367727172841996e+001, -5.115500327307440e+001, 2.597018692977003e+001, -7.619409783233348e+000, 9.895029507142387e-001}
                },
                new double[][]{
                        new double[]{3.147398248354175e-003, -2.407473095562651e-002, 8.164477792763654e-002, -1.602565761519800e-001, 1.990784408547200e-001, -1.602565761519799e-001, 8.164477792763650e-002, -2.407473095562651e-002, 3.147398248354174e-003},
                        new double[]{1.000000000000000e+000, -7.641727326262320e+000, 2.588732046652314e+001, -5.075153517310149e+001, 6.296190401690616e+001, -5.060988186002639e+001, 2.574301332538145e+001, -7.577919094834855e+000, 9.888822473513279e-001}
                },
                new double[][]{
                        new double[]{3.146687779345620e-003, -2.393523698780969e-002, 8.085916488716266e-002, -1.583547875960414e-001, 1.965686268576598e-001, -1.583547875960414e-001, 8.085916488716267e-002, -2.393523698780969e-002, 3.146687779345621e-003},
                        new double[]{1.000000000000000e+000, -7.598937671942595e+000, 2.564210051718254e+001, -5.015383743259277e+001, 6.216912440444099e+001, -5.000554068526758e+001, 2.549068577498689e+001, -7.531730467375851e+000, 9.882250600675234e-001}
                },
                new double[][]{
                        new double[]{3.145956342938733e-003, -2.377953277125930e-002, 7.998621198210600e-002, -1.562478022204508e-001, 1.937907806467246e-001, -1.562478022204509e-001, 7.998621198210601e-002, -2.377953277125930e-002, 3.145956342938734e-003},
                        new double[]{1.000000000000000e+000, -7.551046074454964e+000, 2.536931008366765e+001, -4.949133461826578e+001, 6.129167517029540e+001, -4.933630867075759e+001, 2.521062621241026e+001, -7.480309921497300e+000, 9.875292711639343e-001}
                },
                new double[][]{
                        new double[]{3.145205275424770e-003, -2.360574768061716e-002, 7.901707800064237e-002, -1.539165463204845e-001, 1.907207280589422e-001, -1.539165463204846e-001, 7.901707800064237e-002, -2.360574768061716e-002, 3.145205275424770e-003},
                        new double[]{1.000000000000000e+000, -7.497454662469748e+000, 2.506613437124312e+001, -4.875797603287194e+001, 6.032191796940771e+001, -4.859618054406560e+001, 2.490005467814501e+001, -7.423064973898923e+000, 9.867926432971337e-001}
                },
                new double[][]{
                        new double[]{3.144436304741189e-003, -2.341180419895367e-002, 7.794227453996487e-002, -1.513409979608888e-001, 1.873332981991638e-001, -1.513409979608888e-001, 7.794227453996487e-002, -2.341180419895367e-002, 3.144436304741189e-003},
                        new double[]{1.000000000000000e+000, -7.437498700974345e+000, 2.472954979717061e+001, -4.794740629506200e+001, 5.925189891622068e+001, -4.777885621223402e+001, 2.455599140278651e+001, -7.359338948054349e+000, 9.860128131231478e-001}
                },
                new double[][]{
                        new double[]{3.143651611252740e-003, -2.319539769035377e-002, 7.675169022309922e-002, -1.485003763045669e-001, 1.836026244063562e-001, -1.485003763045669e-001, 7.675169022309922e-002, -2.319539769035376e-002, 3.143651611252740e-003},
                        new double[]{1.000000000000000e+000, -7.370439983416567e+000, 2.435633112138391e+001, -4.705302452210093e+001, 5.807344348138889e+001, -4.687780117754493e+001, 2.417526531441434e+001, -7.288404932353783e+000, 9.851872846543684e-001}
                },
                new double[][]{
                        new double[]{3.142853896610144e-003, -2.295397493013118e-002, 7.543464161125285e-002, -1.453734136707255e-001, 1.795025611488048e-001, -1.453734136707255e-001, 7.543464161125285e-002, -2.295397493013118e-002, 3.142853896610143e-003},
                        new double[]{1.000000000000000e+000, -7.295459810367642e+000, 2.394306700939642e+001, -4.606806964621647e+001, 5.677828797356281e+001, -4.588633308414332e+001, 2.375453107761304e+001, -7.209459424069180e+000, 9.843134223230665e-001}
                },
                new double[][]{
                        new double[]{3.142046461692133e-003, -2.268471153991593e-002, 7.397995907868350e-002, -1.419387286436412e-001, 1.750072407698501e-001, -1.419387286436412e-001, 7.397995907868350e-002, -2.268471153991593e-002, 3.142046461692133e-003},
                        new double[]{1.000000000000000e+000, -7.211651602084042e+000, 2.348618666443336e+001, -4.498573768197065e+001, 5.535825515872661e+001, -4.479774014901439e+001, 2.329029726845187e+001, -7.121615721287476e+000, 9.833884437460559e-001}
                },
                new double[][]{
                        new double[]{3.141233294757360e-003, -2.238448856413199e-002, 7.237611778644681e-002, -1.381753203384370e-001, 1.700917959222239e-001, -1.381753203384370e-001, 7.237611778644681e-002, -2.238448856413199e-002, 3.141233294757361e-003},
                        new double[]{1.000000000000000e+000, -7.118013221824705e+000, 2.298200076709687e+001, -4.379933733704434e+001, 5.380548217609095e+001, -4.360543778096117e+001, 2.277896887030747e+001, -7.023897155876496e+000, 9.824094121861331e-001}
                },
                new double[][]{
                        new double[]{3.140419171067952e-003, -2.204986854082340e-002, 7.061142604758854e-002, -1.340632049640309e-001, 1.647332744234073e-001, -1.340632049640310e-001, 7.061142604758854e-002, -2.204986854082340e-002, 3.140419171067952e-003},
                        new double[]{1.000000000000000e+000, -7.013439125320950e+000, 2.242676065588108e+001, -4.250249069843015e+001, 5.211270919437676e+001, -4.230317000069753e+001, 2.221690794216950e+001, -6.915230300379629e+000, 9.813732287071725e-001}
                },
                new double[][]{
                        new double[]{3.139609765397473e-003, -2.167707157010167e-002, 6.867428581028465e-002, -1.295842155235599e-001, 1.589117725043193e-001, -1.295842155235599e-001, 6.867428581028465e-002, -2.167707157010167e-002, 3.139609765397474e-003},
                        new double[]{1.000000000000000e+000, -6.896712501652490e+000, 2.181674046826984e+001, -4.108938564596743e+001, 5.027363701635703e+001, -4.088526215996502e+001, 2.160051705970665e+001, -6.794438332575813e+000, 9.802766240213092e-001}
                },
                new double[][]{
                        new double[]{3.138811779004953e-003, -2.126195207576799e-002, 6.655354265557797e-002, -1.247229830379156e-001, 1.526118091978146e-001, -1.247229830379157e-001, 6.655354265557797e-002, -2.126195207576798e-002, 3.138811779004952e-003},
                        new double[]{1.000000000000000e+000, -6.766497634670634e+000, 2.114834782741617e+001, -3.955508588505013e+001, 4.828336082739847e+001, -3.934693065859516e+001, 2.092635094498021e+001, -6.660234805784205e+000, 9.791161500284138e-001}
                },
                new double[][]{
                        new double[]{3.138033082845515e-003, -2.079997719958097e-002, 6.423894552086180e-002, -1.194681119467234e-001, 1.458239579375513e-001, -1.194681119467234e-001, 6.423894552086180e-002, -2.079997719958096e-002, 3.138033082845514e-003},
                        new double[]{1.000000000000000e+000, -6.621332795273823e+000, 2.041826957496731e+001, -3.789590269397691e+001, 4.613888518582353e+001, -3.768465352233575e+001, 2.019126255210583e+001, -6.511218153774687e+000, 9.778881710502232e-001}
                },
                new double[][]{
                        new double[]{3.137282878999282e-003, -2.028620807462637e-002, 6.172173915220864e-002, -1.138135518503707e-001, 1.385467402018924e-001, -1.138135518503707e-001, 6.172173915220863e-002, -2.028620807462637e-002, 3.137282878999281e-003},
                        new double[]{1.000000000000000e+000, -6.459624077112426e+000, 1.962365996395001e+001, -3.610982917720077e+001, 4.383972182040158e+001, -3.589660236482700e+001, 1.939259070813909e+001, -6.345867359771473e+000, 9.765888547639235e-001}
                },
                new double[][]{
                        new double[]{3.136571882536522e-003, -1.971528560763997e-002, 5.899541478253631e-002, -1.077601506174919e-001, 1.307887691238586e-001, -1.077601506174919e-001, 5.899541478253631e-002, -1.971528560763997e-002, 3.136571882536522e-003},
                        new double[]{1.000000000000000e+000, -6.279640716138481e+000, 1.876237954948621e+001, -3.419703240182812e+001, 4.138856648256758e+001, -3.398313082155067e+001, 1.852839713118599e+001, -6.162539343393573e+000, 9.752141628428805e-001}
                },
                new double[][]{
                        new double[]{3.135912526301347e-003, -1.908142287524003e-002, 5.605664630009245e-002, -1.013173477939091e-001, 1.225711074975170e-001, -1.013173477939091e-001, 5.605664630009245e-002, -1.908142287524003e-002, 3.135912526301347e-003},
                        new double[]{1.000000000000000e+000, -6.079512592919580e+000, 1.783329361310513e+001, -3.216039053657737e+001, 3.879204371730951e+001, -3.194730629156258e+001, 1.759776112304569e+001, -5.959468771792851e+000, 9.737598413154906e-001}
                },
                new double[][]{
                        new double[]{3.135319191392441e-003, -1.837840682302365e-002, 5.290643957477879e-002, -9.450492957451884e-002, 1.139297742445691e-001, -9.450492957451885e-002, 5.290643957477877e-002, -1.837840682302364e-002, 3.135319191392442e-003},
                        new double[]{1.000000000000000e+000, -5.857230811495141e+000, 1.683663912468907e+001, -3.000605016584413e+001, 3.606149887691670e+001, -2.979545993132036e+001, 1.660114025751611e+001, -5.734771186408036e+000, 9.722214106569864e-001}
                },
                new double[][]{
                        new double[]{3.134808466450974e-003, -1.759961266809466e-002, 4.955152073030654e-002, -8.735471458629229e-002, 1.049182970483839e-001, -8.735471458629231e-002, 4.955152073030654e-002, -1.759961266809466e-002, 3.134808466450974e-003},
                        new double[]{1.000000000000000e+000, -5.610652485916029e+000, 1.577446869037080e+001, -2.774396243152127e+001, 3.321380529123364e+001, -2.753771345886001e+001, 1.554080466633551e+001, -5.486450559482707e+000, 9.705941556333387e-001}
                },
                new double[][]{
                        new double[]{3.134399439237867e-003, -1.673803526438719e-002, 4.600598370664111e-002, -7.991197096996051e-002, 9.561016940658601e-002, -7.991197096996053e-002, 4.600598370664109e-002, -1.673803526438718e-002, 3.134399439237866e-003},
                        new double[]{1.000000000000000e+000, -5.337511151807351e+000, 1.465117824317552e+001, -2.538833478075808e+001, 3.027214214610962e+001, -2.518841970513400e+001, 1.442135065856619e+001, -5.212412659439286e+000, 9.688731149214057e-001}
                },
                new double[][]{
                        new double[]{3.134114024400282e-003, -1.578634271524339e-002, 4.229320671312595e-002, -7.223627850705502e-002, 8.610103372344589e-002, -7.223627850705502e-002, 4.229320671312595e-002, -1.578634271524339e-002, 3.134114024400283e-003},
                        new double[]{1.000000000000000e+000, -5.035434561360168e+000, 1.347412184638902e+001, -2.295790745473116e+001, 2.726668718722200e+001, -2.276642662901645e+001, 1.325029585232740e+001, -4.910485915250239e+000, 9.670530705351497e-001}
                },
                new double[][]{
                        new double[]{3.133977331796415e-003, -1.473695872564023e-002, 3.844802884645435e-002, -6.440144613353772e-002, 7.651038832767883e-002, -6.440144613353772e-002, 3.844802884645435e-002, -1.473695872564023e-002, 3.133977331796415e-003},
                        new double[]{1.000000000000000e+000, -4.701972021031336e+000, 1.225431116064779e+001, -2.047593083012582e+001, 2.423516104606333e+001, -2.029504214034185e+001, 1.203875204110981e+001, -4.578451829379190e+000, 9.651285370941288e-001}
                },
                new double[][]{
                        new double[]{3.134018080275880e-003, -1.358218158786286e-002, 3.451914934501335e-002, -5.649398108994028e-002, 6.698262049696595e-002, -5.649398108994028e-002, 3.451914934501335e-002, -1.358218158786286e-002, 3.134018080275880e-003},
                        new double[]{1.000000000000000e+000, -4.334633894531272e+000, 1.100718793748222e+001, -1.796968318498563e+001, 2.122316153205437e+001, -1.780154147371507e+001, 1.080216276877055e+001, -4.214087396049710e+000, 9.630937509777602e-001}
                },
                new double[][]{
                        new double[]{3.134269062408259e-003, -1.231434928893086e-002, 3.057166915645161e-002, -4.860949486690044e-002, 5.768721953991623e-002, -4.860949486690043e-002, 3.057166915645161e-002, -1.231434928893085e-002, 3.134269062408258e-003},
                        new double[]{1.000000000000000e+000, -3.930946417862069e+000, 9.753444203103115e+000, -1.546933271420957e+001, 1.828424291357617e+001, -1.531602437699200e+001, 9.561078976256924e+000, -3.815222431128634e+000, 9.609426594169974e-001}
                },
                new double[][]{
                        new double[]{3.134767666324647e-003, -1.092606199691836e-002, 2.668963368236989e-002, -4.084634847987040e-002, 4.881814598615966e-002, -4.084634847987040e-002, 2.668963368236989e-002, -1.092606199691836e-002, 3.134767666324647e-003},
                        new double[]{1.000000000000000e+000, -3.488525549835425e+000, 8.519845275102083e+000, -1.300592072787505e+001, 1.547973403338864e+001, -1.286940407685908e+001, 8.341926900254542e+000, -3.379815198939852e+000, 9.586689095842752e-001}
                },
                new double[][]{
                        new double[]{3.135556461597618e-003, -9.410475078600419e-003, 2.297835252009366e-002, -3.329582763135507e-002, 4.059254313022706e-002, -3.329582763135507e-002, 2.297835252009366e-002, -9.410475078600416e-003, 3.135556461597617e-003},
                        new double[]{1.000000000000000e+000, -3.005174191578075e+000, 7.339984032868136e+000, -1.060823832670953e+001, 1.287835576350105e+001, -1.049030715293154e+001, 7.177696414550543e+000, -2.906050205898013e+000, 9.562658377528447e-001}
                },
                new double[][]{
                        new double[]{3.136683856947902e-003, -7.761677730698712e-003, 1.956616302450336e-002, -2.602826100023193e-002, 3.324927652787478e-002, -2.602826100023192e-002, 1.956616302450336e-002, -7.761677730698711e-003, 3.136683856947902e-003},
                        new double[]{1.000000000000000e+000, -2.479007716963163e+000, 6.254859711520669e+000, -8.298406854876264e+000, 1.055579301650579e+001, -8.200703198179212e+000, 6.108444172411899e+000, -2.392462480928022e+000, 9.537264586081307e-001}
                },
                new double[][]{
                        new double[]{3.138204838547156e-003, -5.975174114940367e-003, 1.660516755572724e-002, -1.907484165185080e-002, 2.704813706109233e-002, -1.907484165185080e-002, 1.660516755572724e-002, -5.975174114940366e-003, 3.138204838547157e-003},
                        new double[]{1.000000000000000e+000, -1.908613304435944e+000, 5.313130225694978e+000, -6.086081653584211e+000, 8.594486978103532e+000, -6.010193468209465e+000, 5.181463790532223e+000, -1.838093015511091e+000, 9.510434548065663e-001}
                },
                new double[][]{
                        new double[]{3.140181798805204e-003, -4.048485324695409e-003, 1.427031109296301e-002, -1.240559599494189e-002, 2.227083343634230e-002, -1.240559599494188e-002, 1.427031109296301e-002, -4.048485324695407e-003, 3.140181798805204e-003},
                        new double[]{1.000000000000000e+000, -1.293248958339949e+000, 4.570834170465846e+000, -3.961415514095723e+000, 7.084004269172130e+000, -3.909104068503228e+000, 4.450925708226952e+000, -1.242680198047218e+000, 9.482091668915741e-001}
                },
                new double[][]{
                        new double[]{3.142685466810443e-003, -1.981891201642856e-003, 1.275598507109051e-002, -5.905092967702340e-003, 1.922490893558348e-002, -5.905092967702338e-003, 1.275598507109050e-002, -1.981891201642856e-003, 3.142685466810443e-003},
                        new double[]{1.000000000000000e+000, -6.330882185837364e-001, 4.090319921613351e+000, -1.887283430297775e+000, 6.122341024691120e+000, -1.860890737108303e+000, 3.976735271682492e+000, -6.068919125751182e-001, 9.452155836921591e-001}
                },
                new double[][]{
                        new double[]{3.145795953061868e-003, 2.206695853779799e-004, 1.226916478607727e-002, 6.507937097332545e-004, 1.825108019042818e-002, 6.507937097332541e-004, 1.226916478607727e-002, 2.206695853779804e-004, 3.145795953061868e-003},
                        new double[]{1.000000000000000e+000, 7.048381497110752e-002, 3.938061073067260e+000, 2.081809615620352e-001, 5.818315653524580e+000, 2.050979185919241e-001, 3.822305649740473e+000, 6.739770892980335e-002, 9.420543333469807e-001}
                },
                new double[][]{
                        new double[]{3.149603922824920e-003, 2.550525469983221e-003, 1.301794887198749e-002, 7.611436168154661e-003, 1.973271140029778e-002, 7.611436168154664e-003, 1.301794887198750e-002, 2.550525469983220e-003, 3.149603922824919e-003},
                        new double[]{1.000000000000000e+000, 8.145186473375130e-001, 4.180991859095494e+000, 2.436943974143494e+000, 6.294635311736420e+000, 2.398725819011656e+000, 4.050904438982940e+000, 7.767843464120088e-001, 9.387166751159667e-001}
                },
                new double[][]{
                        new double[]{3.154211914402492e-003, 4.993014030040840e-003, 1.519433231202710e-002, 1.545291226764864e-002, 2.410267045907083e-002, 1.545291226764864e-002, 1.519433231202710e-002, 4.993014030040840e-003, 3.154211914402491e-003},
                        new double[]{1.000000000000000e+000, 1.594094507650048e+000, 4.880983321331306e+000, 4.951642271452251e+000, 7.690118123907030e+000, 4.869407688492093e+000, 4.720228931318529e+000, 1.515962399129426e+000, 9.351934921625812e-001}
                },
                new double[][]{
                        new double[]{3.159735820888123e-003, 7.526040666030606e-003, 1.895020385742257e-002, 2.475323882294724e-002, 3.183737636762435e-002, 2.475323882294725e-002, 1.895020385742258e-002, 7.526040666030604e-003, 3.159735820888123e-003},
                        new double[]{1.000000000000000e+000, 2.401850071567135e+000, 6.087131489535002e+000, 7.937798020754480e+000, 1.015782470066915e+001, 7.798198857209872e+000, 5.874924462876592e+000, 2.277310895382985e+000, 9.314752855128813e-001}
                },
                new double[][]{
                        new double[]{3.166306556622805e-003, 1.011847131390913e-002, 2.436604744578619e-002, 3.612855315613932e-002, 4.342094784718614e-002, 3.612855315613931e-002, 2.436604744578618e-002, 1.011847131390912e-002, 3.166306556622805e-003},
                        new double[]{1.000000000000000e+000, 3.227469566039094e+000, 7.825685895886635e+000, 1.159355358628246e+001, 1.385365932048307e+001, 1.137764342811520e+001, 7.536923689351754e+000, 3.050446762878393e+000, 9.275521694226179e-001}
                },
                new double[][]{
                        new double[]{3.174071932686341e-003, 1.272840225052417e-002, 3.141281522803153e-002, 5.011846710987071e-002, 5.925613563402907e-002, 5.011846710987070e-002, 3.141281522803152e-002, 1.272840225052417e-002, 3.174071932686341e-003},
                        new double[]{1.000000000000000e+000, 4.057136173735536e+000, 1.008776559524817e+001, 1.609310956200188e+001, 1.890801956075611e+001, 1.577574036394405e+001, 9.693802957928598e+000, 3.821764728811681e+000, 9.234138684108242e-001}
                },
                new double[][]{
                        new double[]{3.183198769412820e-003, 1.530138728214639e-002, 3.990911830930127e-002, 6.701501415941384e-002, 7.949784289495179e-002, 6.701501415941387e-002, 3.990911830930128e-002, 1.530138728214639e-002, 3.183198769412820e-003},
                        new double[]{1.000000000000000e+000, 4.872983673821737e+000, 1.281555268646203e+001, 2.153199949973063e+001, 2.537278560026198e+001, 2.108235473066811e+001, 1.228588060012277e+001, 4.573995411122747e+000, 9.190497162478279e-001}
                },
                new double[][]{
                        new double[]{3.193875278243156e-003, 1.776875452331544e-002, 4.947841937856833e-002, 8.664372514461426e-002, 1.037970884360256e-001, 8.664372514461426e-002, 4.947841937856833e-002, 1.776875452331544e-002, 3.193875278243156e-003},
                        new double[]{1.000000000000000e+000, 5.652594074318859e+000, 1.588847505635949e+001, 2.785682067184561e+001, 3.313974994333205e+001, 2.724084106509572e+001, 1.519356653809370e+001, 5.285829477680879e+000, 9.144486572172310e-001}
                },
                new double[][]{
                        new double[]{3.206313750358361e-003, 2.004621469134501e-002, 5.951436885640767e-002, 1.081294655509232e-001, 1.309772342434988e-001, 1.081294655509231e-001, 5.951436885640767e-002, 2.004621469134501e-002, 3.206313750358361e-003},
                        new double[]{1.000000000000000e+000, 6.368612425815518e+000, 1.911200989217115e+001, 3.478974215906980e+001, 4.183734799560661e+001, 3.397518425227058e+001, 1.822752101726196e+001, 5.931676540984903e+000, 9.095992500053052e-001}
                },
                new double[][]{
                        new double[]{3.220753595651257e-003, 2.203305284781968e-002, 6.916660460442711e-002, 1.297129584989178e-001, 1.587254189019787e-001, 1.297129584989178e-001, 6.916660460442711e-002, 2.203305284781968e-002, 3.220753595651257e-003},
                        new double[]{1.000000000000000e+000, 6.988579509033998e+000, 2.221309221390521e+001, 4.176880682873050e+001, 5.073193117747918e+001, 4.073332534804013e+001, 2.112541676882233e+001, 6.481652723309696e+000, 9.044896746072624e-001}
                },
                new double[][]{
                        new double[]{3.237464782909127e-003, 2.361230654207196e-002, 7.736355682136441e-002, 1.487194381535389e-001, 1.834617816779504e-001, 1.487194381535389e-001, 7.736355682136441e-002, 2.361230654207196e-002, 3.237464782909128e-003},
                        new double[]{1.000000000000000e+000, 7.475118310113817e+000, 2.484747652136641e+001, 4.793653796099584e+001, 5.868511693499194e+001, 4.667831681680477e+001, 2.356029769134266e+001, 6.901919709808110e+000, 8.991077426779072e-001}
                },
                new double[][]{
                        new double[]{3.256751740863823e-003, 2.465246248878586e-002, 8.289162964500996e-002, 1.618017313704435e-001, 2.006116256305381e-001, 1.618017313704435e-001, 8.289162964500996e-002, 2.465246248878586e-002, 3.256751740863822e-003},
                        new double[]{1.000000000000000e+000, 7.786649310740569e+000, 2.662530614778131e+001, 5.221626639111764e+001, 6.423871190719274e+001, 5.076524758680607e+001, 2.516623281707262e+001, 7.155528278931967e+000, 8.934409117941060e-001}
                }
        };
    }

    private static class MIDI_COEFFICIENTS_SR882HZ_ELLIPTIC_8THORDER_1DBRPASS_50DBRSTOP_Q25 {

        private static final double[][][] COEFFICIENTS = {
                new double[][]{
                        new double[]{3.158914171751535e-003, -2.522803598882411e-002, 8.819015331617208e-002, -1.762509156159864e-001, 2.202597682341909e-001, -1.762509156159864e-001, 8.819015331617210e-002, -2.522803598882412e-002, 3.158914171751536e-003},
                        new double[]{1.000000000000000e+000, -7.984237571525840e+000, 2.790331825234382e+001, -5.575075998215929e+001, 6.965241071338740e+001, -5.572009253147075e+001, 2.787262850939722e+001, -7.971068879904086e+000, 9.978014900634370e-001}
                },
                new double[][]{
                        new double[]{3.158720531533387e-003, -2.522119177370039e-002, 8.815301818795945e-002, -1.761608745898854e-001, 2.201406552888479e-001, -1.761608745898854e-001, 8.815301818795948e-002, -2.522119177370039e-002, 3.158720531533387e-003},
                        new double[]{1.000000000000000e+000, -7.982446704718968e+000, 2.789246461982013e+001, -5.572320623730904e+001, 6.961478429973324e+001, -5.569073179382911e+001, 2.785996367989195e+001, -7.968498775606662e+000, 9.976709122278069e-001}
                },
                new double[][]{
                        new double[]{3.158516212681905e-003, -2.521361468921687e-002, 8.811171431091479e-002, -1.760605834493512e-001, 2.200079352309933e-001, -1.760605834493511e-001, 8.811171431091479e-002, -2.521361468921687e-002, 3.158516212681906e-003},
                        new double[]{1.000000000000000e+000, -7.980444953709927e+000, 2.788034338912858e+001, -5.569246533008179e+001, 6.957285985023684e+001, -5.565807943148504e+001, 2.784592599886708e+001, -7.965672111098276e+000, 9.975325884747779e-001}
                },
                new double[][]{
                        new double[]{3.158300682378288e-003, -2.520522140520122e-002, 8.806575622653086e-002, -1.759488417715463e-001, 2.198600125373441e-001, -1.759488417715463e-001, 8.806575622653086e-002, -2.520522140520121e-002, 3.158300682378288e-003},
                        new double[]{1.000000000000000e+000, -7.978207027955372e+000, 2.786680371792747e+001, -5.565816072653209e+001, 6.952613340832201e+001, -5.562175324365218e+001, 2.783035880891988e+001, -7.962560996991118e+000, 9.973860604886563e-001}
                },
                new double[][]{
                        new double[]{3.158073388596494e-003, -2.519591880393670e-002, 8.801460113804725e-002, -1.758243075466201e-001, 2.196951036504722e-001, -1.758243075466201e-001, 8.801460113804724e-002, -2.519591880393669e-002, 3.158073388596494e-003},
                        new double[]{1.000000000000000e+000, -7.975704579863196e+000, 2.785167673662227e+001, -5.561987126618882e+001, 6.947404162599960e+001, -5.558132616606035e+001, 2.781308716086624e+001, -7.959134353518608e+000, 9.972308429796352e-001}
                },
                new double[][]{
                        new double[]{3.157833760482284e-003, -2.518560281458240e-002, 8.795764216927342e-002, -1.756854807032544e-001, 2.195112151803615e-001, -1.756854807032545e-001, 8.795764216927343e-002, -2.518560281458240e-002, 3.157833760482284e-003},
                        new double[]{1.000000000000000e+000, -7.972905834873171e+000, 2.783477341392792e+001, -5.557712595360805e+001, 6.941595487860573e+001, -5.553632106677584e+001, 2.779391567850966e+001, -7.955357536572231e+000, 9.970664221138720e-001}
                },
                new double[][]{
                        new double[]{3.157581208939811e-003, -2.517415710875493e-002, 8.789420085347263e-002, -1.755306848048284e-001, 2.193061197090053e-001, -1.755306848048284e-001, 8.789420085347263e-002, -2.517415710875493e-002, 3.157581208939812e-003},
                        new double[]{1.000000000000000e+000, -7.969775177023080e+000, 2.781588217730575e+001, -5.552939817000009e+001, 6.935116962238817e+001, -5.548620496569271e+001, 2.777262618097458e+001, -7.951191919699798e+000, 9.968922538543546e-001}
                },
                new double[][]{
                        new double[]{3.157315127460584e-003, -2.516145164096717e-002, 8.782351877114050e-002, -1.753580467378082e-001, 2.190773289709222e-001, -1.753580467378081e-001, 8.782351877114050e-002, -2.516145164096717e-002, 3.157315127460584e-003},
                        new double[]{1.000000000000000e+000, -7.966272684728221e+000, 2.779476626226109e+001, -5.547609924814837e+001, 6.927889992318151e+001, -5.543038262172264e+001, 2.774897503724640e+001, -7.946594426953851e+000, 9.967077622077782e-001}
                },
                new double[][]{
                        new double[]{3.157034893234318e-003, -2.514734101577731e-002, 8.774474824882923e-002, -1.751654742034645e-001, 2.188220641711343e-001, -1.751654742034645e-001, 8.774474824882923e-002, -2.514734101577731e-002, 3.157034893234318e-003},
                        new double[]{1.000000000000000e+000, -7.962353610900939e+000, 2.777116076231463e+001, -5.541657135044301e+001, 6.919826808082883e+001, -5.536818942846689e+001, 2.772269022551200e+001, -7.941517010911404e+000, 9.965123373723843e-001}
                },
                new double[][]{
                        new double[]{3.156739868586608e-003, -2.513166266147215e-002, 8.765694202448739e-002, -1.749506308150537e-001, 2.185372231935248e-001, -1.749506308150537e-001, 8.765694202448739e-002, -2.513166266147215e-002, 3.156739868586608e-003},
                        new double[]{1.000000000000000e+000, -7.957967800873241e+000, 2.774476934927678e+001, -5.535007958693070e+001, 6.910829427127300e+001, -5.529888355640671e+001, 2.769346806786082e+001, -7.935906069563170e+000, 9.963053337815235e-001}
                },
                new double[][]{
                        new double[]{3.156429402794399e-003, -2.511423478787611e-002, 8.755904177830662e-002, -1.747109085960238e-001, 2.182193444478351e-001, -1.747109085960238e-001, 8.755904177830660e-002, -2.511423478787611e-002, 3.156429402794398e-003},
                        new double[]{1.000000000000000e+000, -7.953059040854923e+000, 2.771526063132524e+001, -5.527580330810300e+001, 6.900788512676303e+001, -5.522163727767330e+001, 2.766096960894986e+001, -7.929701795087816e+000, 9.960860680374509e-001}
                },
                new double[][]{
                        new double[]{3.156102834337093e-003, -2.509485410343064e-002, 8.744986542207409e-002, -1.744433976717799e-001, 2.178645671046087e-001, -1.744433976717799e-001, 8.744986542207410e-002, -2.509485410343064e-002, 3.156102834337092e-003},
                        new double[]{1.000000000000000e+000, -7.947564328857152e+000, 2.768226411439709e+001, -5.519282650606984e+001, 6.889582117493741e+001, -5.513552740861584e+001, 2.762481660545044e+001, -7.922837446781333e+000, 9.958538167296183e-001}
                },
                new double[][]{
                        new double[]{3.155759493648747e-003, -2.507329326401345e-002, 8.732809303489251e-002, -1.741448529495153e-001, 2.174685874762813e-001, -1.741448529495153e-001, 8.732809303489251e-002, -2.507329326401345e-002, 3.155759493648747e-003},
                        new double[]{1.000000000000000e+000, -7.941413059133155e+000, 2.764536573067644e+001, -5.510012725825377e+001, 6.877074306034311e+001, -5.503952480614666e+001, 2.758458709160039e+001, -7.915238539600926e+000, 9.956078141314901e-001}
                },
                new double[][]{
                        new double[]{3.155398706445551e-003, -2.504929802305526e-002, 8.719225132935901e-002, -1.738116575895111e-001, 2.170266113221589e-001, -1.738116575895110e-001, 8.719225132935898e-002, -2.504929802305526e-002, 3.155398706445551e-003},
                        new double[]{1.000000000000000e+000, -7.934526110229607e+000, 2.760410289664421e+001, -5.499656615039397e+001, 6.863113647810052e+001, -5.493248285680369e+001, 2.753981048514512e+001, -7.906821938905227e+000, 9.953472497696708e-001}
                },
                new double[][]{
                        new double[]{3.155019797712390e-003, -2.502258404935523e-002, 8.704069653053413e-002, -1.734397830894891e-001, 2.165333018886840e-001, -1.734397830894891e-001, 8.704069653053413e-002, -2.502258404935523e-002, 3.155019797712390e-003},
                        new double[]{1.000000000000000e+000, -7.926814825702158e+000, 2.755795906246331e+001, -5.488087362122109e+001, 6.847531576003593e+001, -5.481312490339467e+001, 2.748996219757397e+001, -7.897494851027085e+000, 9.950712658588513e-001}
                },
                new double[][]{
                        new double[]{3.154622096443360e-003, -2.499283337559471e-002, 8.687159555115100e-002, -1.730247458341730e-001, 2.159827235485997e-001, -1.730247458341730e-001, 8.687159555115100e-002, -2.499283337559471e-002, 3.154622096443359e-003},
                        new double[]{1.000000000000000e+000, -7.918179875425090e+000, 2.750635771467066e+001, -5.475163618062035e+001, 6.830140607006138e+001, -5.468003056393944e+001, 2.743445771312146e+001, -7.887153698304234e+000, 9.947789545958755e-001}
                },
                new double[][]{
                        new double[]{3.154204941243252e-003, -2.495969043690507e-002, 8.668290535160178e-002, -1.725615600089308e-001, 2.153682809788094e-001, -1.725615600089307e-001, 8.668290535160177e-002, -2.495969043690506e-002, 3.154204941243251e-003},
                        new double[]{1.000000000000000e+000, -7.908509984219141e+000, 2.744865579558501e+001, -5.460728146764365e+001, 6.810732418961489e+001, -5.453162091261407e+001, 2.737264610285698e+001, -7.875682866119663e+000, 9.944693553059467e-001}
                },
                new double[][]{
                        new double[]{3.153767686910969e-003, -2.492275765497536e-002, 8.647235038372664e-002, -1.720446868440570e-001, 2.146826539237228e-001, -1.720446868440570e-001, 8.647235038372665e-002, -2.492275765497536e-002, 3.153767686910969e-003},
                        new double[]{1.000000000000000e+000, -7.897680513237495e+000, 2.738413650596462e+001, -5.444606213586980e+001, 6.789075790771993e+001, -5.436614251402762e+001, 2.730380294378525e+001, -7.862953308375554e+000, 9.941414514337604e-001}
                },
                new double[][]{
                        new double[]{3.153309712141198e-003, -2.488159051911407e-002, 8.623739803510923e-002, -1.714679802507598e-001, 2.139177277369322e-001, -1.714679802507598e-001, 8.623739803510923e-002, -2.488159051911407e-002, 3.153309712141199e-003},
                        new double[]{1.000000000000000e+000, -7.885551878195282e+000, 2.731200146283265e+001, -5.426603858320769e+001, 6.764914407630269e+001, -5.418165033229153e+001, 2.722712261879769e+001, -7.848820996654095e+000, 9.937941673721074e-001}
                },
                new double[][]{
                        new double[]{3.152830428498169e-003, -2.483569211145087e-002, 8.597523201775284e-002, -1.708246290385094e-001, 2.130645200893329e-001, -1.708246290385094e-001, 8.597523201775285e-002, -2.483569211145087e-002, 3.152830428498169e-003},
                        new double[]{1.000000000000000e+000, -7.871967787116638e+000, 2.723136219275792e+001, -5.406506058358273e+001, 6.737964545301448e+001, -5.397598957725740e+001, 2.714170998228106e+001, -7.833125197123277e+000, 9.934263651202707e-001}
                },
                new double[][]{
                        new double[]{3.152329290834861e-003, -2.478450701914385e-002, 8.568272368455455e-002, -1.701070960748993e-001, 2.121131044884582e-001, -1.701070960748993e-001, 8.568272368455455e-002, -2.478450701914385e-002, 3.152329290834861e-003},
                        new double[]{1.000000000000000e+000, -7.856753278825433e+000, 2.714123095314888e+001, -5.384074793183437e+001, 6.707912653478749e+001, -5.374677660477805e+001, 2.704657138911713e+001, -7.815686558051402e+000, 9.930368407643015e-001}
                },
                new double[][]{
                        new double[]{3.151805809352821e-003, -2.472741457213834e-002, 8.535640131252108e-002, -1.693070549741827e-001, 2.110525315863649e-001, -1.693070549741827e-001, 8.535640131252105e-002, -2.472741457213833e-002, 3.151805809352821e-003},
                        new double[]{1.000000000000000e+000, -7.839712541951882e+000, 2.704051089144268e+001, -5.359047028395283e+001, 6.674412869029703e+001, -5.349137905924430e+001, 2.694060510284518e+001, -7.796304989636994e+000, 9.926243207710703e-001}
                },
                new double[][]{
                        new double[]{3.151259563522510e-003, -2.466372134083882e-002, 8.499241746770576e-002, -1.684153251919183e-001, 2.098707496790346e-001, -1.684153251919183e-001, 8.499241746770575e-002, -2.466372134083882e-002, 3.151259563522509e-003},
                        new double[]{1.000000000000000e+000, -7.820626492808971e+000, 2.692798557588263e+001, -5.331132646652480e+001, 6.637084503381098e+001, -5.320689553895649e+001, 2.682259112333003e+001, -7.774757316789275e+000, 9.921874580877952e-001}
                },
                new double[][]{
                        new double[]{3.150690218111486e-003, -2.459265282418264e-002, 8.458651466896791e-002, -1.674218067755961e-001, 2.085545263387358e-001, -1.674218067755961e-001, 8.458651466896790e-002, -2.459265282418264e-002, 3.150690218111486e-003},
                        new double[]{1.000000000000000e+000, -7.799250089168770e+000, 2.680230796366454e+001, -5.300012364668996e+001, 6.595509565284378e+001, -5.289013518281094e+001, 2.669118050715046e+001, -7.750794684574297e+000, 9.917248280385919e-001}
                },
                new double[][]{
                        new double[]{3.150097541598711e-003, -2.451334425528590e-002, 8.413398970276897e-002, -1.663154164921086e-001, 2.070893737947360e-001, -1.663154164921087e-001, 8.413398970276900e-002, -2.451334425528591e-002, 3.150097541598710e-003},
                        new double[]{1.000000000000000e+000, -7.775309355817265e+000, 2.666198891479433e+001, -5.265335690254354e+001, 6.549230401471652e+001, -5.253759772575928e+001, 2.654488429726324e+001, -7.724139695359455e+000, 9.912349240094542e-001}
                },
                new double[][]{
                        new double[]{3.149481427287878e-003, -2.442483044938895e-002, 8.362965711776890e-002, -1.650840276421737e-001, 2.054594815121588e-001, -1.650840276421737e-001, 8.362965711776890e-002, -2.442483044938895e-002, 3.149481427287878e-003},
                        new double[]{1.000000000000000e+000, -7.748498096892178e+000, 2.650538541574005e+001, -5.226718992003175e+001, 6.497747564046264e+001, -5.214545475671754e+001, 2.638206223496205e+001, -7.694483256357655e+000, 9.907161529129768e-001}
                },
                new double[][]{
                        new double[]{3.148841917471334e-003, -2.432603461767724e-002, 8.306781265622283e-002, -1.637144166011302e-001, 2.036476604404971e-001, -1.637144166011302e-001, 8.306781265622282e-002, -2.432603461767724e-002, 3.148841917471333e-003},
                        new double[]{1.000000000000000e+000, -7.718474269548964e+000, 2.633068874916934e+001, -5.183743777292881e+001, 6.440518045698101e+001, -5.170953314288412e+001, 2.620091149995314e+001, -7.661481116438743e+000, 9.901668304240812e-001}
                },
                new double[][]{
                        new double[]{3.148179231039704e-003, -2.421575607124442e-002, 8.244219767150425e-002, -1.621922200182072e-001, 2.016353046397266e-001, -1.621922200182072e-001, 8.244219767150426e-002, -2.421575607124442e-002, 3.148179231039704e-003},
                        new double[]{1.000000000000000e+000, -7.684855993635379e+000, 2.613591293871163e+001, -5.135955302489201e+001, 6.376954062857635e+001, -5.122530186604266e+001, 2.599945581738010e+001, -7.624750071928372e+000, 9.895851759779999e-001}
                },
                new double[][]{
                        new double[]{3.147493794980878e-003, -2.409265674269596e-002, 8.174596595225277e-002, -1.605019076858741e-001, 1.994023774689206e-001, -1.605019076858741e-001, 8.174596595225278e-002, -2.409265674269596e-002, 3.147493794980878e-003},
                        new double[]{1.000000000000000e+000, -7.647217173017054e+000, 2.591888391552564e+001, -5.082861673438728e+001, 6.306422613519982e+001, -5.068786385667163e+001, 2.577553538845406e+001, -7.583863822895104e+000, 9.889693075218270e-001}
                },
                new double[][]{
                        new double[]{3.146786280266355e-003, -2.395524645956951e-002, 8.097165484113340e-002, -1.586267773787971e-001, 1.969274312606955e-001, -1.586267773787970e-001, 8.097165484113339e-002, -2.395524645956951e-002, 3.146786280266355e-003},
                        new double[]{1.000000000000000e+000, -7.605082706284155e+000, 2.567723000210943e+001, -5.023933635140887e+001, 6.228246091342612e+001, -5.009195481722219e+001, 2.552679824940238e+001, -7.538348464440915e+000, 9.883172360110722e-001}
                },
                new double[][]{
                        new double[]{3.146057642683932e-003, -2.380186691502118e-002, 8.011116312030590e-002, -1.565489794744911e-001, 1.941876714157629e-001, -1.565489794744911e-001, 8.011116312030590e-002, -2.380186691502118e-002, 3.146057642683932e-003},
                        new double[]{1.000000000000000e+000, -7.557923268163840e+000, 2.540837449482019e+001, -4.958605297467446e+001, 6.141704301097847e+001, -4.943195150169110e+001, 2.525069384801983e+001, -7.487677602146427e+000, 9.876268596428651e-001}
                },
                new double[][]{
                        new double[]{3.145309169243783e-003, -2.363067429854809e-002, 7.915573885966637e-002, -1.542495809106769e-001, 1.911590781292836e-001, -1.542495809106769e-001, 7.915573885966637e-002, -2.363067429854808e-002, 3.145309169243782e-003},
                        new double[]{1.000000000000000e+000, -7.505149648548621e+000, 2.510953135749222e+001, -4.886276099136969e+001, 6.046038292455793e+001, -4.870189246616761e+001, 2.494446985533765e+001, -7.431267087565798e+000, 9.868959578177501e-001}
                },
                new double[][]{
                        new double[]{3.144542530861090e-003, -2.343962057472181e-002, 7.809598131368309e-002, -1.517086799951212e-001, 1.878166014715096e-001, -1.517086799951213e-001, 7.809598131368312e-002, -2.343962057472182e-002, 3.144542530861090e-003},
                        new double[]{1.000000000000000e+000, -7.446106644238659e+000, 2.477770532277857e+001, -4.806314374481819e+001, 5.940456508343567e+001, -4.789551492012099e+001, 2.460517350974951e+001, -7.368469379125207e+000, 9.861221848224336e-001}
                },
                new double[][]{
                        new double[]{3.143759842103694e-003, -2.322643343329385e-002, 7.692186203628669e-002, -1.489055857205794e-001, 1.841344482157672e-001, -1.489055857205794e-001, 7.692186203628669e-002, -2.322643343329385e-002, 3.143759842103694e-003},
                        new double[]{1.000000000000000e+000, -7.380066510053140e+000, 2.440969804454650e+001, -4.718062955572877e+001, 5.824143828454788e+001, -4.700631197726452e+001, 2.422965913085338e+001, -7.298567546709092e+000, 9.853030632263737e-001}
                },
                new double[][]{
                        new double[]{3.142963728888423e-003, -2.298859498246471e-002, 7.562277168975848e-002, -1.458190774637965e-001, 1.800864815056005e-001, -1.458190774637965e-001, 7.562277168975851e-002, -2.298859498246472e-002, 3.142963728888423e-003},
                        new double[]{1.000000000000000e+000, -7.306221991851069e+000, 2.400212236332807e+001, -4.620847313283798e+001, 5.696274173725337e+001, -4.602761530013240e+001, 2.381460384895219e+001, -7.220768955553241e+000, 9.844359769857969e-001}
                },
                new double[][]{
                        new double[]{3.142157405116242e-003, -2.272331932213791e-002, 7.418760058063407e-002, -1.424277631108395e-001, 1.756467569686751e-001, -1.424277631108395e-001, 7.418760058063407e-002, -2.272331932213791e-002, 3.142157405116241e-003},
                        new double[]{1.000000000000000e+000, -7.223678985404405e+000, 2.355142724664240e+001, -4.513986810088812e+001, 5.556027417094533e+001, -4.495270880541869e+001, 2.335653408171116e+001, -7.134198687951414e+000, 9.835181642495450e-001}
                },
                new double[][]{
                        new double[]{3.141344759355474e-003, -2.242752921998866e-002, 7.260486278627447e-002, -1.387105555046015e-001, 1.707902209014889e-001, -1.387105555046015e-001, 7.260486278627448e-002, -2.242752921998867e-002, 3.141344759355476e-003},
                        new double[]{1.000000000000000e+000, -7.131448893455791e+000, 2.305393655470365e+001, -4.396809697018314e+001, 5.402611409669658e+001, -4.377497966928874e+001, 2.285186585830404e+001, -7.037892791124552e+000, 9.825467098622310e-001}
                },
                new double[][]{
                        new double[]{3.140530452815434e-003, -2.209783222553157e-002, 7.086287585268358e-002, -1.346472882766651e-001, 1.654935972166466e-001, -1.346472882766651e-001, 7.086287585268358e-002, -2.209783222553157e-002, 3.140530452815434e-003},
                        new double[]{1.000000000000000e+000, -7.028440790469957e+000, 2.250590546417507e+001, -4.268672525319380e+001, 5.235289965352209e+001, -4.248811322368841e+001, 2.229696274641211e+001, -6.930791478097861e+000, 9.815185375613915e-001}
                },
                new double[][]{
                        new double[]{3.139720030001532e-003, -2.173049670260564e-002, 6.895001044164724e-002, -1.302194920509112e-001, 1.597364893546253e-001, -1.302194920509112e-001, 6.895001044164724e-002, -2.173049670260564e-002, 3.139720030001532e-003},
                        new double[]{1.000000000000000e+000, -6.913453552746011e+000, 2.190359915699494e+001, -4.128984642453281e+001, 5.053417631895373e+001, -4.108633829101993e+001, 2.168821587546242e+001, -6.811732457626789e+000, 9.804304018668419e-001}
                },
                new double[][]{
                        new double[]{3.138920043609318e-003, -2.132142844669853e-002, 6.685502694716070e-002, -1.254113499053784e-001, 1.535027205281139e-001, -1.254113499053784e-001, 6.685502694716070e-002, -2.132142844669852e-002, 3.138920043609317e-003},
                        new double[]{1.000000000000000e+000, -6.785168173345928e+000, 2.124339923703190e+001, -3.977238377227491e+001, 4.856481987488134e+001, -3.956472881770807e+001, 2.102215135734287e+001, -6.679444631544746e+000, 9.792788796621767e-001}
                },
                new double[][]{
                        new double[]{3.138138195400846e-003, -2.086614878974718e-002, 6.456751890372857e-002, -1.202108457749498e-001, 1.467819295268732e-001, -1.202108457749498e-001, 6.456751890372854e-002, -2.086614878974718e-002, 3.138138195400846e-003},
                        new double[]{1.000000000000000e+000, -6.642140559881460e+000, 2.052194424847472e+001, -3.813045355914415e+001, 4.644154009029114e+001, -3.791956599785321e+001, 2.029557125497202e+001, -6.532542476246826e+000, 9.780603614704660e-001}
                },
                new double[][]{
                        new double[]{3.137383495014738e-003, -2.035977539266257e-002, 6.207848581758801e-002, -1.146111097402831e-001, 1.395714287668851e-001, -1.146111097402831e-001, 6.207848581758804e-002, -2.035977539266258e-002, 3.137383495014739e-003},
                        new double[]{1.000000000000000e+000, -6.482795212335559e+000, 1.973631158775978e+001, -3.636179084677729e+001, 4.416346726759517e+001, -3.614876197657686e+001, 1.950573508814042e+001, -6.369521522715017e+000, 9.767710424285894e-001}
                },
                new double[][]{
                        new double[]{3.136666438893973e-003, -1.979700729790284e-002, 5.938106060794007e-002, -1.086119480461299e-001, 1.318783151921704e-001, -1.086119480461299e-001, 5.938106060794007e-002, -1.979700729790284e-002, 3.136666438893971e-003},
                        new double[]{1.000000000000000e+000, -6.305420302147028e+000, 1.888424894372728e+001, -3.446623425161173e+001, 4.173281874501650e+001, -3.425234111378631e+001, 1.865058961063090e+001, -6.188755470352579e+000, 9.754069129674712e-001}
                },
                new double[][]{
                        new double[]{3.135999211775756e-003, -1.917211627599845e-002, 5.647141874449895e-002, -1.022215209783066e-001, 1.237218020833705e-001, -1.022215209783066e-001, 5.647141874449895e-002, -1.917211627599845e-002, 3.135999211775755e-003},
                        new double[]{1.000000000000000e+000, -6.108164827770005e+000, 1.796446403991698e+001, -3.244625807994454e+001, 3.915563535779206e+001, -3.223296696933333e+001, 1.772905511216643e+001, -5.988495618201883e+000, 9.739637492087323e-001}
                },
                new double[][]{
                        new double[]{3.135395913479415e-003, -1.847894706794991e-002, 5.334989680459917e-002, -9.545809580325507e-002, 1.151357103832799e-001, -9.545809580325507e-002, 5.334989680459918e-002, -1.847894706794991e-002, 3.135395913479414e-003},
                        new double[]{1.000000000000000e+000, -5.889038711448119e+000, 1.697698169509040e+001, -3.030752890040785e+001, 3.644256861020583e+001, -3.009649182805379e+001, 1.674137659936002e+001, -5.766873477511554e+000, 9.724371030920150e-001}
                },
                new double[][]{
                        new double[]{3.134872814054742e-003, -1.771092981841938e-002, 5.002234667258992e-002, -8.835175207991430e-002, 1.061710224968992e-001, -8.835175207991428e-002, 5.002234667258991e-002, -1.771092981841938e-002, 3.134872814054743e-003},
                        new double[]{1.000000000000000e+000, -5.645916933114961e+000, 1.592357678282255e+001, -2.805944776861024e+001, 3.360968811899140e+001, -2.785248986069071e+001, 1.568954761343741e+001, -5.521907646923291e+000, 9.708222922514992e-001}
                },
                new double[][]{
                        new double[]{3.134448640719596e-003, -1.686110883213353e-002, 4.650174673990105e-002, -8.094585029574346e-002, 9.689836222128355e-002, -8.094585029574346e-002, 4.650174673990105e-002, -1.686110883213353e-002, 3.134448640719596e-003},
                        new double[]{1.000000000000000e+000, -5.376549076477819e+000, 1.480829016434980e+001, -2.571561821337495e+001, 3.067926657108625e+001, -2.551471414533917e+001, 1.457781276202342e+001, -5.251516290461129e+000, 9.691143896650616e-001}
                },
                new double[][]{
                        new double[]{3.134144900426430e-003, -1.592219278718520e-002, 4.281008156724019e-002, -7.329799049630324e-002, 8.741022695278995e-002, -7.329799049630321e-002, 4.281008156724018e-002, -1.592219278718520e-002, 3.134144900426431e-003},
                        new double[]{1.000000000000000e+000, -5.078575995571596e+000, 1.363803155333677e+001, -2.329415324124543e+001, 2.768048772176990e+001, -2.310139131752218e+001, 1.341325175472367e+001, -4.953536863062881e+000, 9.673082131050332e-001}
                },
                new double[][]{
                        new double[]{3.133986242359381e-003, -1.488663272103037e-002, 3.898048433866225e-002, -6.548008623161181e-002, 7.782277204675940e-002, -6.548008623161179e-002, 3.898048433866224e-002, -1.488663272103037e-002, 3.133986242359382e-003},
                        new double[]{1.000000000000000e+000, -4.749555702535110e+000, 1.242326783582474e+001, -2.081770226054185e+001, 2.465001483153426e+001, -2.063523587799613e+001, 1.220644215248738e+001, -4.625755081159084e+000, 9.653983144258902e-001}
                },
                new double[][]{
                        new double[]{3.134000865183136e-003, -1.374673547961106e-002, 3.505960926264279e-002, -5.757706591567998e-002, 6.827694650151442e-002, -5.757706591567997e-002, 3.505960926264277e-002, -1.374673547961106e-002, 3.134000865183137e-003},
                        new double[]{1.000000000000000e+000, -4.387001032092933e+000, 1.117878670446554e+001, -1.831304257978743e+001, 2.163235693364632e+001, -1.814303083692603e+001, 1.097218930255730e+001, -4.265945536642653e+000, 9.633789687312899e-001}
                },
                new double[][]{
                        new double[]{3.134220974450349e-003, -1.249482188871643e-002, 3.111016042416856e-002, -4.968360100192681e-002, 5.893882326426997e-002, -4.968360100192679e-002, 3.111016042416856e-002, -1.249482188871642e-002, 3.134220974450349e-003},
                        new double[]{1.000000000000000e+000, -3.988432154384610e+000, 9.924512458893030e+000, -1.581004390369762e+001, 1.867998452058553e+001, -1.565458636278783e+001, 9.730298985988352e+000, -3.871926796865431e+000, 9.612441634709374e-001}
                },
                new double[][]{
                        new double[]{3.134683296235498e-003, -1.112344065371278e-002, 2.721344549472061e-002, -4.189817179360247e-002, 4.999907860596907e-002, -4.189817179360249e-002, 2.721344549472063e-002, -1.112344065371278e-002, 3.134683296235499e-003},
                        new double[]{1.000000000000000e+000, -3.551447578106645e+000, 8.686332181368766e+000, -1.333978549177546e+001, 1.585318163154243e+001, -1.320086089594506e+001, 8.506349958513926e+000, -3.441634309410114e+000, 9.589875875268065e-001}
                },
                new double[][]{
                        new double[]{3.135429653810842e-003, -9.625650876013817e-003, 2.347174294635508e-002, -3.431375682076393e-002, 4.167177250795569e-002, -3.431375682076396e-002, 2.347174294635509e-002, -9.625650876013815e-003, 3.135429653810841e-003},
                        new double[]{1.000000000000000e+000, -3.073817893085362e+000, 7.496864839570460e+000, -1.093159692822955e+001, 1.321968397844113e+001, -1.081102226044384e+001, 7.332398576575025e+000, -2.973214916145051e+000, 9.566026203583706e-001}
                },
                new double[][]{
                        new double[]{3.136507615027994e-003, -7.995388007318339e-003, 2.001016614498467e-002, -2.700453127243610e-002, 3.419287025020119e-002, -2.700453127243610e-002, 2.001016614498467e-002, -7.995388007318338e-003, 3.136507615027994e-003},
                        new double[]{1.000000000000000e+000, -2.553607112888302e+000, 6.396071929059573e+000, -8.608824123123766e+000, 1.085424397430703e+001, -8.508258754562579e+000, 6.247514986116061e+000, -2.465147238984247e+000, 9.540823212878158e-001}
                },
                new double[][]{
                        new double[]{3.137971219032647e-003, -6.227929899550367e-003, 1.697757457589259e-002, -2.000826983095059e-002, 2.781929299210386e-002, -2.000826983095058e-002, 1.697757457589259e-002, -6.227929899550366e-003, 3.137971219032647e-003},
                        new double[]{1.000000000000000e+000, -1.989327039336724e+000, 5.431565256741408e+000, -6.383219646747632e+000, 8.838373261538344e+000, -6.304248919297201e+000, 5.298009357162090e+000, -1.916392570698153e+000, 9.514194190188600e-001}
                },
                new double[][]{
                        new double[]{3.139881792038660e-003, -4.320481115621655e-003, 1.454592254502238e-002, -1.330478008909641e-002, 2.282959375103136e-002, -1.330478008909641e-002, 1.454592254502238e-002, -4.320481115621655e-003, 3.139881792038661e-003},
                        new double[]{1.000000000000000e+000, -1.380130495951165e+000, 4.658415685376785e+000, -4.248056266378205e+000, 7.260610273994775e+000, -4.192398223125966e+000, 4.537157949505544e+000, -1.326581099061283e+000, 9.486063014968165e-001}
                },
                new double[][]{
                        new double[]{3.142308863144464e-003, -2.272894487109254e-003, 1.290725642193201e-002, -6.791770245505154e-003, 1.952742077619976e-002, -6.791770245505154e-003, 1.290725642193201e-002, -2.272894487109255e-003, 3.142308863144466e-003},
                        new double[]{1.000000000000000e+000, -7.260494374303685e-001, 4.138208459115812e+000, -2.170405102117195e+000, 6.217679689066231e+000, -2.140290301644085e+000, 4.024185030680213e+000, -6.962381825075767e-001, 9.456350062331020e-001}
                },
                new double[][]{
                        new double[]{3.145331192616787e-003, -8.854851642353578e-005, 1.226739091833158e-002, -2.611923055065791e-004, 1.824840871860560e-002, -2.611923055065795e-004, 1.226739091833159e-002, -8.854851642353585e-005, 3.145331192616786e-003},
                        new double[]{1.000000000000000e+000, -2.828365020127248e-002, 3.937031909613621e+000, -8.354203000899364e-002, 5.816743105726006e+000, -8.231448556477422e-002, 3.822203862406808e+000, -2.705479884464273e-002, 9.424972112345817e-001}
                },
                new double[][]{
                        new double[]{3.149037926727280e-003, 2.224603121450900e-003, 1.283514759095196e-002, 6.618655450406833e-003, 1.936954346716353e-002, 6.618655450406834e-003, 1.283514759095196e-002, 2.224603121450899e-003, 3.149037926727279e-003},
                        new double[]{1.000000000000000e+000, 7.104552478978243e-001, 4.122038047857780e+000, 2.118836655876266e+000, 6.178431841603045e+000, 2.085866876500583e+000, 3.994779312809874e+000, 6.777950423010539e-001, 9.391842266970702e-001}
                },
                new double[][]{
                        new double[]{3.153529895147938e-003, 4.652799048430014e-003, 1.480598299712125e-002, 1.430725536113852e-002, 2.331683675105064e-002, 1.430725536113852e-002, 1.480598299712125e-002, 4.652799048430014e-003, 3.153529895147938e-003},
                        new double[]{1.000000000000000e+000, 1.485541881038429e+000, 4.756194632377531e+000, 4.584032422313927e+000, 7.439331396054892e+000, 4.508497490965125e+000, 4.600764631638667e+000, 1.413289526237363e+000, 9.356869876431149e-001}
                },
                new double[][]{
                        new double[]{3.158921069139991e-003, 7.175053387591945e-003, 1.833895949636017e-002, 2.337143216507759e-002, 3.056197449687965e-002, 2.337143216507757e-002, 1.833895949636016e-002, 7.175053387591944e-003, 3.158921069139990e-003},
                        new double[]{1.000000000000000e+000, 2.289978261861833e+000, 5.890887877721747e+000, 7.493940446802735e+000, 9.750942774959203e+000, 7.363176385100306e+000, 5.687113558059197e+000, 2.172150380040946e+000, 9.319960477069255e-001}
                },
                new double[][]{
                        new double[]{3.165340201369383e-003, 9.761569329013144e-003, 2.352644605190533e-002, 3.442763988738570e-002, 4.159182395176630e-002, 3.442763988738570e-002, 2.352644605190533e-002, 9.761569329013143e-003, 3.165340201369383e-003},
                        new double[]{1.000000000000000e+000, 3.113884791888940e+000, 7.556174418169761e+000, 1.104673795985765e+001, 1.326999105509784e+001, 1.084261750062165e+001, 7.279515316476489e+000, 2.944399703559979e+000, 9.281015742941231e-001}
                },
                new double[][]{
                        new double[]{3.172932671224081e-003, 1.237202198640762e-002, 3.035684495242341e-002, 4.803481076836110e-002, 5.682815411349611e-002, 4.803481076836112e-002, 3.035684495242342e-002, 1.237202198640762e-002, 3.172932671224080e-003},
                        new double[]{1.000000000000000e+000, 3.943956573708311e+000, 9.748774235052908e+000, 1.542272903007664e+001, 1.813287413992946e+001, 1.512095428351280e+001, 9.370993906056189e+000, 3.716900149633642e+000, 9.239933453709293e-001}
                },
                new double[][]{
                        new double[]{3.181862563085667e-003, 1.495378494373177e-002, 3.867221321343667e-002, 6.453093249328780e-002, 7.647292739513274e-002, 6.453093249328780e-002, 3.867221321343666e-002, 1.495378494373176e-002, 3.181862563085666e-003},
                        new double[]{1.000000000000000e+000, 4.762912926638595e+000, 1.241840479845202e+001, 2.073208245271043e+001, 2.440640274739815e+001, 2.030252009137090e+001, 1.190910982584758e+001, 4.472908108259482e+000, 9.196607481665466e-001}
                },
                new double[][]{
                        new double[]{3.192315009232230e-003, 1.744022460892024e-002, 4.812506062201077e-002, 8.381939647366198e-002, 1.002642685413352e-001, 8.381939647366200e-002, 4.812506062201079e-002, 1.744022460892025e-002, 3.192315009232230e-003},
                        new double[]{1.000000000000000e+000, 5.548985106658352e+000, 1.545383788918091e+001, 2.694628787646489e+001, 3.201006246588864e+001, 2.635508915586734e+001, 1.478315021886033e+001, 5.191685281027697e+000, 9.150927801038186e-001}
                },
                new double[][]{
                        new double[]{3.204498834060272e-003, 1.974925360358840e-002, 5.814194197277289e-002, 1.051338440083434e-001, 1.271587081073587e-001, 1.051338440083434e-001, 5.814194197277289e-002, 1.974925360358840e-002, 3.204498834060272e-003},
                        new double[]{1.000000000000000e+000, 6.275509858783591e+000, 1.867114090846784e+001, 3.382240050104177e+001, 4.061468584186788e+001, 3.303666392836884e+001, 1.781370772584422e+001, 5.848234196735308e+000, 9.102780523068603e-001}
                },
                new double[][]{
                        new double[]{3.218649542281032e-003, 2.178242154899741e-002, 6.790556736316532e-002, 1.268425028388355e-001, 1.550127667839744e-001, 1.268425028388355e-001, 6.790556736316533e-002, 2.178242154899741e-002, 3.218649542281033e-003},
                        new double[]{1.000000000000000e+000, 6.910725099162502e+000, 2.180789402179193e+001, 4.083950270457974e+001, 4.954064329731128e+001, 3.983494492711604e+001, 2.074826826714032e+001, 6.413247900003434e+000, 9.052047960700427e-001}
                },
                new double[][]{
                        new double[]{3.235032700882341e-003, 2.342492982003226e-002, 7.637143484746153e-002, 1.463889627666626e-001, 1.804154239490834e-001, 1.463889627666626e-001, 7.637143484746153e-002, 2.342492982003226e-002, 3.235032700882341e-003},
                        new double[]{1.000000000000000e+000, 7.417898528962155e+000, 2.452855796450741e+001, 4.717843106493590e+001, 5.770357708737241e+001, 4.594974516022988e+001, 2.326765283300114e+001, 6.853392529536081e+000, 8.998608727105467e-001}
                },
                new double[][]{
                        new double[]{3.253947773212191e-003, 2.454708389953875e-002, 8.233813009766450e-002, 1.604906352150141e-001, 1.988912848896281e-001, 1.604906352150141e-001, 8.233813009766448e-002, 2.454708389953874e-002, 3.253947773212191e-003},
                        new double[]{1.000000000000000e+000, 7.755958748455693e+000, 2.644717555897957e+001, 5.178381016979742e+001, 6.367745906451024e+001, 5.035599589126372e+001, 2.500896886788771e+001, 7.132071709462643e+000, 8.942337872663337e-001}
                }
        };
    }

}
