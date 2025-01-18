/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for Constant-Q-{@link com.tagtraum.jipes.math.Transform}s. Using the factory allows for sliding-in a faster, perhaps
 * native implementation.
 * <p/>
 * In order to use a factory other than the default factory, you need to specify
 * its classname with the system property <code>com.tagtraum.jipes.math.ConstantQTransformFactory</code>.
 * <p/>
 * The default implementation is equivalent to the one described by Brown and Puckette in their 1992 paper
 * <em>An Efficient Algorithm for the Calculation of a Constant Q Transform</em>.
 * <p/>
 * Date: 5/16/11
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://www.wellesley.edu/Physics/brown/pubs/effalgV92P2698-P2701.pdf">Brown, J.C. and Puckette, M.S. (1992). "An Efficient Algorithm for the Calculation of a Constant Q Transform", J. Acoust. Soc. Am. 92, 2698-2701. (1992)</a>
 * @see <a href="http://wwwmath.uni-muenster.de/logik/Personen/blankertz/constQ/constQ.html">Matlab implementation by Benjamin Blankertz</a>
 */
public abstract class ConstantQTransformFactory {

    public static final String FACTORYCLASS_PROPERTY_NAME = ConstantQTransformFactory.class.getName();
    public static final float DEFAULT_THRESHOLD = 0.0054f;
    private static Logger LOG = Logger.getLogger(ConstantQTransformFactory.class.getName());
    private static ConstantQTransformFactory instance;

    /**
     * Creates a factory for Constant Q {@link com.tagtraum.jipes.math.Transform} objects.
     *
     * @return factory instance
     */
    public synchronized static ConstantQTransformFactory getInstance() {
        if (instance == null) {
            final String factoryClassname = System.getProperty(FACTORYCLASS_PROPERTY_NAME);
            if (factoryClassname != null) {
                try {
                    instance = (ConstantQTransformFactory) Class.forName(factoryClassname, true, Thread.currentThread().getContextClassLoader()).newInstance();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Unable to instantiate configured ConstantQTransformFactory \"" + factoryClassname + "\". Will fall back to standard implementation. Problem: " + e, e);
                }
            }
            if (instance == null) {
                // fall back to built-in factory
                instance = new JavaConstantQTransformFactory();
            }
        }
        return instance;
    }

    /**
     * Creates an instance of the constant-Q-transform.
     *
     * @param minFreq min frequency
     * @param maxFreq max frequency
     * @param binsPerOctave total bins per octave
     * @param sampleRate sample rate
     * @param threshold magnitude threshold used for creating the sparse matrix used in the kernel - see {@link #DEFAULT_THRESHOLD}
     * @return Constant Q transform instance
     */
    public abstract Transform create(final float minFreq, final float maxFreq, final int binsPerOctave, final float sampleRate, final float threshold);

    /**
     * Default implementation for a Java Constant-Q-Transform factory.
     * Since creating the transform can be pretty expensive, we cache a certain number of instances, once they are
     * created. Also, every <em>new</em> instance created is serialized to disk (tmp-directory).
     */
    private static class JavaConstantQTransformFactory extends ConstantQTransformFactory {

        private Map<ConstantQKernelKey, JavaConstantQTransform> kernels = new LinkedHashMap<ConstantQKernelKey, JavaConstantQTransform>() {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<ConstantQKernelKey, JavaConstantQTransform> eldest) {
                // just to make sure that the caching is limited somehow - in this case to 8 kernels
                return size() > 8;
            }
        };


        @Override
        public synchronized Transform create(final float minFreq, final float maxFreq, final int binsPerOctave, final float sampleRate, final float threshold) {
            final ConstantQKernelKey key = new ConstantQKernelKey(minFreq, maxFreq, binsPerOctave, sampleRate, threshold);
            JavaConstantQTransform kernel = kernels.get(key);
            if (kernel == null) {
                kernel = loadFromFile(minFreq, maxFreq, binsPerOctave, sampleRate, threshold, kernel);
                if (kernel == null) {
                    kernel = new JavaConstantQTransform(minFreq, maxFreq, binsPerOctave, sampleRate, threshold);
                    saveToFile(minFreq, maxFreq, binsPerOctave, sampleRate, threshold, kernel);
                }
                kernels.put(key, kernel);
            }
            return kernel;
        }

        private void saveToFile(final float minFreq, final float maxFreq, final int binsPerOctave, final float sampleRate, final float threshold, final JavaConstantQTransform kernel) {
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(new FileOutputStream(toFileName(minFreq, maxFreq, binsPerOctave, sampleRate, threshold)));
                objectOutputStream.writeObject(kernel);
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e1) {
                        // ignore
                    }
                }
            }
        }

        private JavaConstantQTransform loadFromFile(final float minFreq, final float maxFreq, final int binsPerOctave, final float sampleRate, final float threshold, JavaConstantQTransform kernel) {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(toFileName(minFreq, maxFreq, binsPerOctave, sampleRate, threshold)));
                kernel = (JavaConstantQTransform) objectInputStream.readObject();
            } catch (Exception e) {
                // ignore
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            return kernel;
        }

        private static File toFileName(final float minFreq, final float maxFreq, final int binsPerOctave, final float sampleRate, final float threshold) {
            final File file = new File(System.getProperty("java.io.tmpdir") + "/JavaConstantQTransform-" + minFreq + "-" + maxFreq + "-" + binsPerOctave + "-" + sampleRate + "-" + threshold + ".ser");
            return file;
        }

    }

    private static class ConstantQKernelKey {

        private final float minFreq;
        private final float maxFreq;
        private final int binsPerOctave;
        private final float sampleRate;
        private final float threshold;

        private ConstantQKernelKey(final float minFreq, final float maxFreq, final int binsPerOctave, final float sampleRate, final float threshold) {
            this.minFreq = minFreq;
            this.maxFreq = maxFreq;
            this.binsPerOctave = binsPerOctave;
            this.sampleRate = sampleRate;
            this.threshold = threshold;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConstantQKernelKey that = (ConstantQKernelKey) o;

            if (binsPerOctave != that.binsPerOctave) return false;
            if (Float.compare(that.maxFreq, maxFreq) != 0) return false;
            if (Float.compare(that.minFreq, minFreq) != 0) return false;
            if (Float.compare(that.sampleRate, sampleRate) != 0) return false;
            if (Float.compare(that.threshold, threshold) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (minFreq != +0.0f ? Float.floatToIntBits(minFreq) : 0);
            result = 31 * result + (maxFreq != +0.0f ? Float.floatToIntBits(maxFreq) : 0);
            result = 31 * result + binsPerOctave;
            result = 31 * result + (sampleRate != +0.0f ? Float.floatToIntBits(sampleRate) : 0);
            result = 31 * result + (threshold != +0.0f ? Float.floatToIntBits(threshold) : 0);
            return result;
        }
    }

    /**
     * Kernel for the efficient constant Q transform.
     */
    private static class JavaConstantQTransform implements Serializable, Transform {


        private SparseList<Complex>[] kernel;
        private int sparseLength;
        private float Q;
        private float[] frequencies;
        private float minFrequency;
        private float maxFrequency;
        private int binsPerOctave;
        private float sampleRate;
        private float threshold;

        /**
         * "Kernel" for performing constant Q transforms. This kernel is not a purely mathematical
         * kernel, i.e. it's not just a matrix. Instead it offers the whole transformation operation.
         * It is stateless and re-entrant and can therefore be cached, re-used and also used in parallel by multiple
         * threads.
         *
         * @param minFrequency lower frequency edge
         * @param maxFrequency upper frequency edge
         * @param binsPerOctave number of bins between lower and upper edge
         * @param sampleRate sample rate
         * @param threshold magnitude threshold, typically 0.0054f, for creating the sparse matrix (kernel)
         */
        private JavaConstantQTransform(final float minFrequency, final float maxFrequency, final int binsPerOctave, final float sampleRate, final float threshold) {
            this.minFrequency = minFrequency;
            this.maxFrequency = maxFrequency;
            this.binsPerOctave = binsPerOctave;
            this.sampleRate = sampleRate;
            this.threshold = threshold;
            this.Q = (float) (1f / (Math.pow(2, 1.0f / binsPerOctave) - 1));
            final int K = (int) Math.ceil(binsPerOctave * Floats.log2(maxFrequency / minFrequency));
            final int fftLen = (int) Math.round(Math.pow(2, nextpow2((int) Math.ceil((Q * sampleRate) / minFrequency))));
            final float[] tempKernelReal = new float[fftLen];
            final float[] tempKernelImag = new float[fftLen];

            final Transform fft = FFTFactory.getInstance().create(fftLen);
            this.kernel = new SparseList[K];
            int maxIndex = 0;

            for (int k = K; k >= 1; k--) {
                final int len = (int) Math.ceil(Q * sampleRate / (minFrequency * Math.pow(2, ((k - 1) / (float) binsPerOctave))));
                final float[] hamming = createHammingCoefficients(len);
                final float u = (float) (2 * Math.PI * Q / len);
                for (int n = 0; n < len; n++) {
                    final float hl = hamming[n] / len;
                    final float un = u * n;
                    tempKernelReal[n] = (float) (hl * Math.cos(un));
                    tempKernelImag[n] = (float) (hl * Math.sin(un));
                }

                final float[][] transform = fft.transform(tempKernelReal, tempKernelImag);

                final SparseList<Complex> sparseList = new SparseList<Complex>();
                this.kernel[k - 1] = sparseList;
                for (int z = 0; z < transform[0].length; z++) {
                    final float real = transform[0][z];
                    final float imag = transform[1][z];
                    final float magnitude = (float) Math.sqrt(real * real + imag * imag);
                    if (magnitude >= threshold) {
                        sparseList.add(new Complex(real / fftLen, imag / -fftLen), z);
                    }
                }
                maxIndex = Math.max(maxIndex, sparseList.getMaxIndex());

            }
            this.sparseLength = (int) Math.round(Math.pow(2, nextpow2(maxIndex))) * 2;
            this.frequencies = createFrequencies(minFrequency, maxFrequency, binsPerOctave);
        }

        private float[] createFrequencies(final float minFreq, final float maxFreq, final int binsPerOctave) {
            final int K = (int) Math.ceil(binsPerOctave * Floats.log2(maxFreq / minFreq));
            final float[] frequencies = new float[K];
            for (int i=0; i<K; i++) {
                frequencies[i] = (float) (minFreq * Math.pow(2, i / (float)binsPerOctave)) / sampleRate;
            }
            return frequencies;
        }

        public float[] getFrequencies() {
            return frequencies;
        }

        public float getQ() {
            return Q;
        }

        public float getMinFrequency() {
            return minFrequency;
        }

        public float getMaxFrequency() {
            return maxFrequency;
        }

        public int getBinsPerOctave() {
            return binsPerOctave;
        }

        public float getSampleRate() {
            return sampleRate;
        }

        public float getThreshold() {
            return threshold;
        }

        public float[][] transform(final float[] input) {
            final float[] values;
            if (input.length == sparseLength) {
                values = input;
            } else if (input.length > sparseLength) {
                values = new float[sparseLength];
                System.arraycopy(input, 0, values, 0, values.length);
            } else {
                values = new float[sparseLength];
                System.arraycopy(input, 0, values, 0, input.length);
            }
            //System.out.println("Calling FFT with length " + sparseLength + " and " + values.length + " values.");
            final float[][] transform = FFTFactory.getInstance().create(sparseLength).transform(values);
            float[][] result = new float[3][kernel.length];

            for (int i = 0; i < kernel.length; i++) {
                final SparseList<Complex> sparseList = kernel[i];
                for (int j = 0, max = sparseList.size(); j < max; j++) {
                    final int index = sparseList.getIndex(j);
                    final Complex scaleFactor = sparseList.get(j);
                    final Complex fftResult = new Complex(transform[0][index], transform[1][index]);
                    final Complex complexResult = fftResult.times(scaleFactor);

                    result[0][i] += complexResult.getR();
                    result[1][i] += complexResult.getI();
                }
            }
            result[2] = frequencies.clone();
            return result;
        }

        public float[][] inverseTransform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public float[][] transform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
            if (imaginary == null) return transform(real);
            throw new UnsupportedOperationException();
        }

        private float[] createHammingCoefficients(final int len) {
            final float[] hamm = new float[len];
            for (int i = 0; i < len; i++) {
                hamm[i] = (float) (0.54 - (0.46 * Math.cos(Math.PI * 2 * i / (len - 1))));
            }
            return hamm;
        }

        private static int nextpow2(final int in) {
            int i = 1;
            while (true) {
                i++;
                if (Math.pow(2, i) >= in) {
                    return i;
                }
            }
        }

        private static class SparseList<T> implements Serializable {
            private final List<Holder<T>> objects = new ArrayList<Holder<T>>();
            private int maxIndex = 0;
            private int size = 0;

            public void add(final T t, final int index) {
                objects.add(new Holder<T>(t, index));
                maxIndex = Math.max(maxIndex, index);
                size = objects.size();
            }

            public T get(final int index) {
                return objects.get(index).getObject();
            }

            public int getIndex(final int index) {
                return objects.get(index).getIndex();
            }

            public int getMaxIndex() {
                return maxIndex;
            }

            public int size() {
                return size;
            }

            private static class Holder<A>  implements Serializable {
                private A object;
                private int index;

                public Holder(final A object, final int index) {
                    this.object = object;
                    this.index = index;
                }

                public A getObject() {
                    return object;
                }

                public int getIndex() {
                    return index;
                }
            }
        }

        private static class Complex implements Serializable {
            private final float r, i;

            public Complex(final float r, final float i) {
                this.r = r;
                this.i = i;
            }

            public float getR() {
                return r;
            }

            public float getI() {
                return i;
            }

            public Complex times(final float r, final float i) {
                //(a + bi)(c + di) = (ac ? bd) + (bc + ad)i
                return new Complex(this.r*r - this.i*i, this.i*r + this.r*i);
            }

            public Complex times(final Complex that) {
                //(a + bi)(c + di) = (ac ? bd) + (bc + ad)i
                return new Complex(this.r*that.r - this.i*that.i, this.i*that.r + this.r*that.i);
            }

            @Override
            public String toString() {
                return r + ", " + i + "i";
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            JavaConstantQTransform that = (JavaConstantQTransform) o;

            if (binsPerOctave != that.binsPerOctave) return false;
            if (Float.compare(that.maxFrequency, maxFrequency) != 0) return false;
            if (Float.compare(that.minFrequency, minFrequency) != 0) return false;
            if (Float.compare(that.sampleRate, sampleRate) != 0) return false;
            if (Float.compare(that.threshold, threshold) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (minFrequency != +0.0f ? Float.floatToIntBits(minFrequency) : 0);
            result = 31 * result + (maxFrequency != +0.0f ? Float.floatToIntBits(maxFrequency) : 0);
            result = 31 * result + binsPerOctave;
            result = 31 * result + (sampleRate != +0.0f ? Float.floatToIntBits(sampleRate) : 0);
            result = 31 * result + (threshold != +0.0f ? Float.floatToIntBits(threshold) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "JavaConstantQTransform{" +
                    "Q=" + Q +
                    ", minFrequency=" + minFrequency +
                    ", maxFrequency=" + maxFrequency +
                    ", binsPerOctave=" + binsPerOctave +
                    ", sampleRate=" + sampleRate +
                    ", threshold=" + threshold +
                    '}';
        }
    }
}
