/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.AbstractSignalProcessor;
import com.tagtraum.jipes.SignalSource;
import com.tagtraum.jipes.math.*;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.*;

/**
 * Processes AudioBuffer features to create a novelty curve using a kernel and a distance function.
 * <p/>
 * By default this processor does not pad the self-similarity matrix with zeroes, but only calculates
 * novelty values for regions of the matrix that are completely covered by the used kernel. To
 * change this behavior, call {@link #setPadSimilarityMatrixWithZeros(boolean)}.
 * <p/>
 * The similarity matrix is created using a {@link DistanceFunction}, which is then turned into a
 * similarity function via a {@link FloatMapFunction}. Both functions can be set via their respective setters.
 * <p/>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://www.fxpal.com/publications/FXPAL-PR-03-186.pdf">Foote, J., and M. Cooper. 2003. Media segmentation using self-similarity decomposition. In M. Yeung, R. Lienhart, and C.-S. Li (Eds.), Proceedings of the SPIE: Storage and Retrieval for Media Databases, Volume 5021, Santa Clara, CA, USA, 167-75. SPIE.</a>
 * @see <a href="http://rotorbrain.com/foote/papers/footeICME00.pdf">J. Foote, "Automatic Audio Segmentation using a Measure of Audio Novelty." In Proceedings of IEEE International Conference on Multimedia and Expo, vol. I, pp. 452-455, 2000.</a>
 */
public class Novelty<I extends AudioBuffer> extends AbstractSignalProcessor<I, AudioBuffer> {

    /**
     * Converts a distance value into a similarity value by calculating <code>1 - distance</code>.
     */
    public static final FloatMapFunction ONE_MINUS_DISTANCE = new FloatMapFunction() {
        public float map(final float distance) {
            return 1-distance;
        }
    };
    /**
     * Converts a distance value into a similarity value by calculating <code>exp(-distance)</code>.
     */
    public static final FloatMapFunction EXP_NEG_DISTANCE = new FloatMapFunction() {
        public float map(final float distance) {
            return (float)Math.exp(-distance);
        }
    };
    private DistanceFunction<I> distanceFunction = com.tagtraum.jipes.audio.AudioBufferFunctions.createDistanceFunction(DistanceFunctions.COSINE_DISTANCE);
    private FloatMapFunction distanceToSimilarityFunction = EXP_NEG_DISTANCE;
    private Kernel kernel;
    private LinkedList<I> input = new LinkedList<I>();
    private int[] frameNumbers;
    private MutableMatrix similarityMatrix;
    private List<Float> noveltyValues = new ArrayList<Float>();
    private int firstFrameNumber = -1;
    private AudioFormat audioFormat;
    private boolean padSimilarityMatrixWithZeros;

    /**
     * Processor with {@link com.tagtraum.jipes.math.DistanceFunctions#COSINE_DISTANCE} and {@link #EXP_NEG_DISTANCE} mapping to similarity.
     *
     * @param id id
     * @param kernel kernel
     * @param distanceFunction distance function
     * @param padSimilarityMatrixWithZeros true or false
     */
    public Novelty(final Object id, final Kernel kernel, final DistanceFunction<I> distanceFunction, final boolean padSimilarityMatrixWithZeros) {
        setKernel(kernel);
        setId(id);
        setPadSimilarityMatrixWithZeros(padSimilarityMatrixWithZeros);
        setDistanceFunction(distanceFunction);
    }

    /**
     * Default processor with a 64 frames normalized Gaussian checkerboard kernel.
     *
     * @param id id
     * @param distanceFunction distance function
     * @param padSimilarityMatrixWithZeros true or false
     * @see NormGaussianCheckerboardKernel
     */
    public Novelty(final Object id, final DistanceFunction<I> distanceFunction, final boolean padSimilarityMatrixWithZeros) {
        this(id, NormGaussianCheckerboardKernel.getInstance(64), distanceFunction, padSimilarityMatrixWithZeros);
    }

    /**
     * Default processor with a 64 frames normalized Gaussian kernel and no padding.
     *
     * @param id id
     */
    public Novelty(final Object id) {
        this(id, com.tagtraum.jipes.audio.AudioBufferFunctions.<I>createDistanceFunction(DistanceFunctions.COSINE_DISTANCE), false);
    }

    /**
     * Default processor with a 64 frames normalized Gaussian kernel and no padding.
     */
    public Novelty() {
        this(null);
    }

    /**
     * Indicates whether the self-similarity matrix is zero padded. Zero padding will lead to results that have the same
     * length as the input, but are somewhat skewed (too high) at the beginning and the end.
     *
     * @return true or false
     */
    public boolean isPadSimilarityMatrixWithZeros() {
        return padSimilarityMatrixWithZeros;
    }

    /**
     * Configures the processor to zero pad the internal self-similarity matrix, so that novelty values
     * can be computed for the beginning and the end of the matrix' diagonal, where the kernel only partially overlaps
     * with the matrix.
     *
     * @param padSimilarityMatrixWithZeros true or false
     */
    public void setPadSimilarityMatrixWithZeros(final boolean padSimilarityMatrixWithZeros) {
        this.padSimilarityMatrixWithZeros = padSimilarityMatrixWithZeros;
    }

    /**
     * Returns the Kernel, a specialized {@link com.tagtraum.jipes.math.AggregateFunction}.
     *
     * @return kernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Sets the kernel.
     *
     * @param kernel kernel
     */
    public void setKernel(final Kernel kernel) {
        this.kernel = kernel;
        reset();
    }

    /**
     * Returns the distance function used to compute the self similarity matrix.
     * <p/>
     * The default value is {@link com.tagtraum.jipes.math.DistanceFunctions#COSINE_DISTANCE}
     *
     * @return distance function
     */
    public DistanceFunction<I> getDistanceFunction() {
        return distanceFunction;
    }

    /**
     * Sets the distance function used to compute the self similarity matrix.
     *
     * @param distanceFunction distance function
     * @see #createCosineDistanceFunction(int, int, int)
     */
    public void setDistanceFunction(final DistanceFunction<I> distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    /**
     * Returns the function used to map a distance value (the result of {@link #getDistanceFunction()}})
     * to a similarity value.
     * <p/>
     * The default value is {@link #EXP_NEG_DISTANCE}
     *
     * @return map function to map distance to similarity
     */
    public FloatMapFunction getDistanceToSimilarityFunction() {
        return distanceToSimilarityFunction;
    }

    /**
     * Sets the mapping function from distance values to similarity values.
     *
     * @param distanceToSimilarityFunction map function
     */
    public void setDistanceToSimilarityFunction(final FloatMapFunction distanceToSimilarityFunction) {
        this.distanceToSimilarityFunction = distanceToSimilarityFunction;
    }

    @Override
    protected AudioBuffer processNext(final I buffer) throws IOException {
        throw new RuntimeException("This method is not used by the implementation");
    }

    @Override
    public void reset() {
        this.noveltyValues = new ArrayList<Float>();
        this.firstFrameNumber = -1;
        this.audioFormat = null;
        this.input.clear();
        this.similarityMatrix = new SymmetricMatrix(kernel.size());
        this.frameNumbers = new int[kernel.size()];
    }

    @Override
    public AudioBuffer read() throws IOException {
        final SignalSource<I> source = getConnectedSource();
        I in;
        while ((in = source.read()) != null) {
            process(in);
        }
        if (this.noveltyValues.isEmpty()) return null;
        return finish();
    }

    @Override
    public void process(final I buffer) throws IOException {
        try {
            // since we keep the thing around...
            final I in = (I)buffer.clone();
            input.add(in);
            final int kernelDim = kernel.size();
            // shift up and left
            shiftSimilarityMatrixByOne();
            if (input.size() > kernelDim) {
                input.removeFirst();
            }
            System.arraycopy(frameNumbers, 1, frameNumbers, 0, frameNumbers.length-1);
            frameNumbers[frameNumbers.length - 1] = in.getFrameNumber();
            // fill in similarity matrix, start in the lower right corner
            final int indexI = kernelDim-1;
            int indexJ = kernelDim - input.size();
            for (final I i : input) {
                final float distance = distanceFunction.distance(in, i);
                final float similarity = distanceToSimilarityFunction == null ? distance : distanceToSimilarityFunction.map(distance);
                similarityMatrix.set(indexI, indexJ, similarity);
                indexJ++;
            }
            if (isMatrixFilled()) {
                // apply kernel and remember result
                noveltyValues.add(kernel.aggregate(similarityMatrix));
                if (audioFormat == null) {
                    deriveOutputFormat(in.getAudioFormat());
                }
            }
        } catch (CloneNotSupportedException e) {
            throw new IOException(e);
        }
    }

    private void deriveOutputFormat(final AudioFormat inputFormat) {
        firstFrameNumber = input.size() - kernel.size()/2;
        final int frameDiff = frameNumbers[frameNumbers.length/2+1] - frameNumbers[frameNumbers.length/2];
        final float sampleRate = inputFormat.getSampleRate() / frameDiff;
        audioFormat = new AudioFormat(
                inputFormat.getEncoding(), sampleRate, inputFormat.getSampleSizeInBits(),
                inputFormat.getChannels(), inputFormat.getFrameSize(), sampleRate,
                inputFormat.isBigEndian());
    }

    private boolean isMatrixFilled() {
        return isPadSimilarityMatrixWithZeros() ? input.size() >= kernel.size()/2 : input.size() == kernel.size();
    }

    @Override
    public void flush() throws IOException {
        if (audioFormat == null) return;
        this.lastOut = finish();
        this.signalProcessorSupport.process(lastOut);
        super.flush();
    }

    private RealAudioBuffer finish() {
        if (isPadSimilarityMatrixWithZeros()) {
            final int kernelDim = this.kernel.size();
            for (int k=0; k<kernelDim/2; k++) {
                shiftSimilarityMatrixByOne();
                this.noveltyValues.add(kernel.aggregate(similarityMatrix));
            }
        }
        final float[] real = new float[noveltyValues.size()];
        for (int i=0; i<real.length; i++) {
            real[i] = this.noveltyValues.get(i);
        }
        final RealAudioBuffer buffer = new RealAudioBuffer(firstFrameNumber, real, audioFormat);
        reset();
        return buffer;
    }

    /**
     * Shifts the content of the whole matrix by (-1,-1), i.e. up and left, filling the new fields with zeros.
     */
    private void shiftSimilarityMatrixByOne() {
        final SymmetricMatrix newMatrix = new SymmetricMatrix(similarityMatrix.getNumberOfRows());
        newMatrix.copy(similarityMatrix.translate(-1, -1));
        similarityMatrix = newMatrix;
    }

    @Override
    public String toString() {
        return "Novelty{" +
                "kernel=" + kernel +
                ", distanceFunction=" + distanceFunction +
                ", padSimilarityMatrixWithZeros=" + padSimilarityMatrixWithZeros +
                '}';
    }

    /**
     * Kernel.
     */
    public static interface Kernel extends FloatAggregateFunction<Matrix> {
        /**
         * Size of the (square) kernel.
         *
         * @return size
         */
        int size();
    }

    /**
     * Normalized Gaussian checkerboard kernel.<br/>
     * Aggregate values are divided by the maximal possible value a {@link GaussianCheckerboardKernel} would deliver
     * before they are returned, assuming the input values are between -1 and 1.
     */
    public static class NormGaussianCheckerboardKernel extends GaussianCheckerboardKernel {

        private static Map<Integer, NormGaussianCheckerboardKernel> instances = new HashMap<Integer, NormGaussianCheckerboardKernel>();
        private float maxAggregate;

        public NormGaussianCheckerboardKernel(final int size) {
            super(size);
            final Matrix k = this.getKernel();
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    this.maxAggregate += Math.abs(k.get(i,j));
                }
            }
            if (this.maxAggregate == 0) this.maxAggregate = 1;
        }

        /**
         * Creates a normalized Gaussian checkerboard kernel with the given dimension.
         *
         * @param dimension dimension, preferably even
         * @return kernel
         */
        public synchronized static NormGaussianCheckerboardKernel getInstance(final int dimension) {
            NormGaussianCheckerboardKernel instance = instances.get(dimension);
            if (instance == null) {
                instance = new NormGaussianCheckerboardKernel(dimension);
                instances.put(dimension, instance);
            }
            return instance;
        }

        @Override
        public float aggregate(final Matrix distances) {
            return super.aggregate(distances) / this.maxAggregate;
        }
    }

    /**
     * Gaussian checkboard kernel.
     */
    public static class GaussianCheckerboardKernel implements Kernel {

        private static Map<Integer, GaussianCheckerboardKernel> instances = new HashMap<Integer, GaussianCheckerboardKernel>();

        private int size;
        private MutableMatrix kernel;

        public GaussianCheckerboardKernel(final int size) {
            if (size < 0) throw new IllegalArgumentException("GaussianCheckerboardKernel size can't be less than zero: " + size);
            this.size = size;
            // create kernel
            this.kernel = new SymmetricMatrix(size);
            if (size % 2 != 0)  {
                // odd size
                final int half = size/2 + 1;
                for (int i=0; i<size; i++) {
                    for (int j=i; j<size; j++) {
                        final float a = ((i+1f-half)/half);
                        final float b = ((j+1f-half)/half);
                        final float g = (float)Math.exp(-(a*a+b*b)*4);
                        /*
                        boolean c = j > i;
                        boolean d = j+1 > size - i - 1;
                        boolean bool = c ^ d;
                        */
                        final boolean negative = i+1 > half && j < half
                                || j+1 > half && i < half;
                        if (negative) {
                            this.kernel.set(i, j, -g);
                        } else {
                            this.kernel.set(i,j,g);
                        }
                    }
                }
            } else {
                // even size
                final int half = size/2;
                for (int i=0; i<size; i++) {
                    for (int j=i; j<size; j++) {
                        final float a = ((i+0.5f-half)/half);
                        final float b = ((j+0.5f-half)/half);
                        final float g = (float)Math.exp(-(a*a+b*b)*4);
                        /*
                        boolean c = j > i;
                        boolean d = j+1 > size - i - 1;
                        boolean bool = c ^ d;
                        */
                        final boolean negative = i+1 > half && j < half
                                || j+1 > half && i < half;
                        if (negative) {
                            this.kernel.set(i,j,-g);
                        } else {
                            this.kernel.set(i,j,g);
                        }
                    }
                }
            }
        }

        public Matrix getKernel() {
            return kernel;
        }

        /**
         * Creates a Gaussian checkerboard kernel with the given dimension.
         *
         * @param dimension dimension, preferably even
         * @return kernel
         */
        public synchronized static GaussianCheckerboardKernel getInstance(final int dimension) {
            GaussianCheckerboardKernel instance = instances.get(dimension);
            if (instance == null) {
                instance = new GaussianCheckerboardKernel(dimension);
                instances.put(dimension, instance);
            }
            return instance;
        }

        public int size() {
            return size;
        }

        public float aggregate(final Matrix distances) {
            float score = 0;
            final Matrix product = kernel.hadamardMultiply(distances);
            // exploiting symmetry in both kernel and distances matrix
            for (int row=0; row<size; row++) {
                for (int column=row+1; column<size; column++) {
                    score += product.get(row, column) * 2;
                }
            }
            // add diagonal once
            for (int i=0; i<size; i++) {
                score += product.get(i, i);
            }
            return score;
        }

        @Override
        public String toString() {
            return "GaussianCheckerboardKernel{" +
                    "size=" + size +
                    '}';
        }
    }

    /**
     * Creates a special cosine distance function implementation specifically for how it is called from a
     * {@link Novelty} with a given kernel size.
     *
     *
     * @param kernelSize kernel size
     * @param minBin min bin
     * @param maxBin max bin
     * @return specially optimized cosine distance function
     */
    public static DistanceFunction<float[]> createCosineDistanceFunction(final int kernelSize, final int minBin, final int maxBin) {
        return new DistanceFunction<float[]>() {

            private float[] lastA;
            private double lastEuclideanNormA;
            private final LimitedCache limitedCache = new LimitedCache(kernelSize);

            public float distance(final float[] a, final float[] b) {
                if (a == b) return 0;
                // don't cache A, as it stays constant for long periods
                final double euclideanNormA;
                if (a == lastA) {
                    euclideanNormA = lastEuclideanNormA;
                } else {
                    euclideanNormA = Floats.euclideanNorm(a, minBin, maxBin - minBin);
                }
                lastA = a;
                lastEuclideanNormA = euclideanNormA;

                final double euclideanNormB;
                final int index = limitedCache.getIndex(b);
                if (index == -1) {
                    euclideanNormB = Floats.euclideanNorm(b, minBin, maxBin-minBin);
                    limitedCache.put(b, euclideanNormB);
                } else {
                    euclideanNormB = limitedCache.get(index);
                }


                return 1-(float)Floats.cosineSimilarity(a, b, minBin, maxBin-minBin, euclideanNormA, euclideanNormB);
            }

            @Override
            public String toString() {
                return "CosineDistance{kernelSize=" + + kernelSize + ", minBin=" + minBin + ", maxBin=" + maxBin + "}";
            }
        };
    }

    /**
     * Simple cache that heavily relies on items always being requested in the same order.
     */
    private static class LimitedCache {
        private float[][] a;
        private double[] eu;
        private int pos;
        private int capacity;

        private LimitedCache(final int capacity) {
            this.a = new float[capacity][];
            this.eu = new double[capacity];
            this.capacity = capacity;
            this.pos = 0;
        }

        public void put(final float[] a, final double eu) {
            this.a[pos] = a;
            this.eu[pos] = eu;
            pos = (pos+1) % capacity;
        }

        public int getIndex(final float[] a) {
            for (int i=0; i<capacity; i++) {
                final int p = (pos + i) % capacity;
                if (this.a[p] == a) {
                    pos = (p+1) % capacity;
                    return p;
                }
            }
            return -1;
        }

        public double get(final int i) {
            return eu[i];
        }

    }
}
