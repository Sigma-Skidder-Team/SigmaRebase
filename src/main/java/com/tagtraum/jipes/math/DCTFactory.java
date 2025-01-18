/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.*;

/**
 * <p>Factory for DC{@link com.tagtraum.jipes.math.Transform}s. Using the factory allows for sliding-in a faster, perhaps
 * native implementation.
 * </p>
 * <p>In order to use a factory other than the default factory, you need to specify
 * its classname with the system property <code>com.tagtraum.jipes.math.DCTFactory</code>.
 * I.e.
 * <xmp>-Dcom.tagtraum.jipes.math.DCTFactory=YOUR.CLASSNAME.HERE</xmp>
 * </p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class DCTFactory {

    public static final String FACTORYCLASS_PROPERTY_NAME = DCTFactory.class.getName();
    private static Logger LOG = Logger.getLogger(DCTFactory.class.getName());
    private static DCTFactory instance;

    protected DCTFactory() {
    }

    /**
     * Creates a factory for DCT {@link com.tagtraum.jipes.math.Transform} objects.
     *
     * @return factory instance
     */
    public synchronized static DCTFactory getInstance() {
        if (instance == null) {
            final String factoryClassname = System.getProperty(FACTORYCLASS_PROPERTY_NAME);
            if (factoryClassname != null) {
                try {
                    instance = (DCTFactory) Class.forName(factoryClassname, true, Thread.currentThread().getContextClassLoader()).newInstance();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Unable to instantiate configured DCTFactory \"" + factoryClassname + "\". Will fall back to standard implementation. Problem: " + e, e);
                }
            }
            if (instance == null) {
                // fall back to built-in factory
                instance = new BasicDCTFactory();
            }
        }
        return instance;
    }

    /**
     * Creates an instance of the discrete cosine transform (DCT).
     * By specifying the number of samples implementations can pre-create lookup tables to speed
     * up the actual transform.
     *
     * @param numberOfSamples number of samples the DCT instance should be able to process
     * @return DCT instance
     */
    public abstract Transform create(int numberOfSamples);

    /**
     * Default implementation for a DCT factory.
     */
    private static class BasicDCTFactory extends DCTFactory {

        private Transform last;
        private int lastNumberOfSamples;

        @Override
        public synchronized Transform create(final int numberOfSamples) {
            if (last != null && lastNumberOfSamples == numberOfSamples) return last;
            if (DCTFactory.isPowerOfTwo(numberOfSamples)) {
                last = new FFTBasedDCT(numberOfSamples);
            } else {
                last = new MatrixBasedDCT(numberOfSamples);
            }
            lastNumberOfSamples = numberOfSamples;
            return last;
        }
    }

    private static boolean isPowerOfTwo(final int number) {
        return (number & (number - 1)) == 0;
    }

    private static class MatrixBasedDCT implements Transform {

        private final int numberOfSamples;
        private final float[] matrix;

        public MatrixBasedDCT(final int numberOfSamples) {
            if (numberOfSamples <=0) throw new IllegalArgumentException("N must be greater than 0");
            this.numberOfSamples = numberOfSamples;
            this.matrix = createMatrix(numberOfSamples);
        }

        private float[] createMatrix(final int length) {
            final double k = Math.PI/length;
            final double w1 = 1; //1/Math.sqrt(2);
            final double w2 = 1; //Math.sqrt(2f/length);
            final float[] matrix = new float[length*length];

            for (int i=0; i<length; i++){
                for (int j=0; j<length; j++) {
                    final int idx = i + (j*length);
                    // we scale with factor 2, so that the results are identical to the ones of FFTBasedDCT
                    matrix[idx] = (float) (w1 * Math.cos(k * i * (j+0.5))) * 2;
                    /*
                    if (i == 0) {
                        matrix[idx] = (float) (w1 * Math.cos(k * (i+1) * (j+0.5)));
                    } else {
                        matrix[idx] = (float) (w2 * Math.cos(k * (i+1) * (j+0.5)));
                    }
                    */
                }
            }
            return matrix;
        }

        @Override
        public float[][] transform(final float[] real) throws UnsupportedOperationException {
            final float[] result = new float[numberOfSamples];
            for (int k=0; k<numberOfSamples; k++) {
                float v = 0;
                for (int n=0; n<numberOfSamples; n++) {
                    final int idx = k + (n*numberOfSamples);
                    v += (matrix[idx] * real[n]);
                }
                result[k] = v;
            }
            return new float[][] {result};
        }

        @Override
        public float[][] transform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
            return transform(real);
        }

        @Override
        public float[][] inverseTransform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final MatrixBasedDCT matrixBasedDCT = (MatrixBasedDCT) o;
            if (numberOfSamples != matrixBasedDCT.numberOfSamples) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return numberOfSamples;
        }

        @Override
        public String toString() {
            return "MatrixBasedDCT{" +
                "N=" + numberOfSamples +
                '}';
        }    }

    /**
     * Default implementation for a FFT based DCT using
     * the <a href="http://dsp.stackexchange.com/questions/2807/fast-cosine-transform-via-fft#10606">N FFT approach</a>.
     */
    public static class FFTBasedDCT implements Transform {

        private static final Map<Integer, float[][]> FACTORS = new HashMap<Integer, float[][]>();
        private final int numberOfSamples;
        private final Transform fft;
        private final float[][] factors;
        private float[] frequencies;

        public FFTBasedDCT(final int numberOfSamples) {
            if (!isPowerOfTwo(numberOfSamples)) throw new IllegalArgumentException("N is not a power of 2");
            if (numberOfSamples <=0) throw new IllegalArgumentException("N must be greater than 0");
            this.numberOfSamples = numberOfSamples;
            this.factors = getFactors(numberOfSamples);
            this.fft = FFTFactory.getInstance().create(numberOfSamples);

            this.frequencies = new float[numberOfSamples];
            for (int index=0; index<numberOfSamples; index++) {
                if (index <= numberOfSamples / 2) {
                    this.frequencies[index] = index / numberOfSamples;
                } else {
                    this.frequencies[index] = -((numberOfSamples - index) / (float) numberOfSamples);
                }
            }
        }

        private synchronized static float[][] getFactors(final int numberOfSamples) {
            float[][] factors = FACTORS.get(numberOfSamples);
            if (factors == null) {
                factors = halfSampleShift(numberOfSamples);
                FACTORS.put(numberOfSamples, factors);
            }
            return factors;
        }

        public float[][] inverseTransform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public float[][] transform(final float[] real) throws UnsupportedOperationException {
            // reorder [a, b, c, d, e, f] to [a, c, e, f, d, b]
            final float[] reordered = new float[real.length];
            for (int i=0; i<real.length/2; i++) {
                final int a = i * 2;
                reordered[i] = real[a];
                reordered[real.length-1-i] = real[a+1];
            }
            final float[][] fftOut = fft.transform(reordered);
            // for now we return only a single array of real values, no frequency array
            final float[][] out = new float[1][real.length];

            final float[] re = fftOut[0];
            final float[] im = fftOut[1];
            for (int k=0; k<real.length; k++) {
                out[0][k] = 2 * multiplyIgnoreImaginary(re[k], im[k], factors[0][k], factors[1][k]);
            }
            return out;
        }

        private static float[][] halfSampleShift(final int length) {
            final float[][] factors = new float[2][];
            factors[0] = new float[length];
            factors[1] = new float[length];
            final float[] real = factors[0];
            final float[] imag = factors[1];
            final int N2 = 2 * length;
            for (int k=0; k<length; k++) {
                final double x = -PI * k / N2;
                real[k] = (float) cos(x);
                imag[k] = (float) sin(x);
            }
            return factors;
        }

        private static float multiplyIgnoreImaginary(final float re1, final float im1, final float re2, final float im2) {
            return re1 * re2 - im1 * im2;
        }

        public float[][] transform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
            return transform(real);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final FFTBasedDCT fftBasedDCT = (FFTBasedDCT) o;
            if (numberOfSamples != fftBasedDCT.numberOfSamples) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return numberOfSamples;
        }

        @Override
        public String toString() {
            return "FFTBasedDCT{" +
                    "N=" + numberOfSamples +
                    '}';
        }
    }


}
