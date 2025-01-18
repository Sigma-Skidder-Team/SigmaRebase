/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Common distance functions.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see DistanceFunction
 */
public final class DistanceFunctions {

    private DistanceFunctions() {
    }

    /**
     * @see Floats#euclideanDistance(float[], float[])
     */
    public static final DistanceFunction<float[]> EUCLIDEAN_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.euclideanDistance(now, last);
        }

        @Override
        public String toString() {
            return "EUCLIDEAN_DISTANCE";
        }
    };
    /**
     * @see Floats#euclideanDistance(float[], float[], boolean)
     */
    public static final DistanceFunction<float[]> EUCLIDEAN_INCREASE_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.euclideanDistance(last, now, true);
        }

        @Override
        public String toString() {
            return "EUCLIDEAN_INCREASE_DISTANCE";
        }
    };
    /**
     * @see Floats#cityBlockDistance(float[], float[])
     */
    public static final DistanceFunction<float[]> CITY_BLOCK_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cityBlockDistance(now, last);
        }

        @Override
        public String toString() {
            return "CITY_BLOCK_DISTANCE";
        }
    };
    /**
     * @see Floats#cityBlockDistance(float[], float[], boolean)
     */
    public static final DistanceFunction<float[]> CITY_BLOCK_INCREASE_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cityBlockDistance(last, now, true);
        }

        @Override
        public String toString() {
            return "CITY_BLOCK_INCREASE_DISTANCE";
        }
    };
    /**
     * @see Floats#cosineDistance(float[], float[])
     */
    public static final DistanceFunction<float[]> COSINE_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cosineDistance(now, last);
        }

        @Override
        public String toString() {
            return "COSINE_DISTANCE";
        }
    };
    /**
     * @see Floats#cosineSimilarity(float[], float[])
     */
    public static final DistanceFunction<float[]> COSINE_SIMILARITY = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cosineSimilarity(now, last);
        }

        @Override
        public String toString() {
            return "COSINE_SIMILARITY";
        }
    };

    /**
     * <p>Creates a cosine distance function.
     * </p>
     * <p>Assuming the arrays to compare are fourier spectra, limiting the length and selecting an
     * offset can implicitly band-pass filter the spectra  before computing the distance.
     * </p>
     *
     * @param length length of the array to compare
     * @param offset offset into both arrays
     * @see Floats#cosineDistance(float[], float[], int, int)
     * @return distance function
     */
    public static DistanceFunction<float[]> createCosineDistanceFunction(final int offset, final int length) {
        return new DistanceFunction<float[]>() {
            public float distance(final float[] last, final float[] now) {
                return (float)Floats.cosineDistance(now, last, offset, length);
            }

            @Override
            public String toString() {
                return "CosineDistance{offset=" + offset + ", length=" + length + "}";
            }

        };
    }

    /**
     * Creates a cosine distance function that caches intermediate results.
     * That means it is <em>stateful</em>, consumes <em>memory</em>, and
     * therefore shouldn't be kept around longer than necessary.
     * It basically trades memory for speed.
     *
     * @see Floats#cosineDistance(float[], float[], int, int)
     * @return distance function
     */
    public static DistanceFunction<float[]> createCosineDistanceFunction() {
        return new DistanceFunction<float[]>() {
            private Map<float[], Double> cache = new HashMap<float[], Double>();

            @Override
            public float distance(final float[] a, final float[] b) {
                if (a == b) return 0;
                final double euclideanNormA = euclideanNorm(a);
                final double euclideanNormB = euclideanNorm(b);
                return 1-(float)Floats.cosineSimilarity(a, b, 0, a.length, euclideanNormA, euclideanNormB);
            }

            private double euclideanNorm(final float[] a) {
                Double norm = cache.get(a);
                if (norm == null) {
                    norm = Floats.euclideanNorm(a);
                    cache.put(a, norm);
                }
                return norm;
            }

            @Override
            public String toString() {
                return "CosineDistance{caching,stateful}";
            }
        };
    }

    /**
     * Creates stateful similarity function.
     *
     * @return similarity function
     * @see #createCosineDistanceFunction()
     */
    public static DistanceFunction<float[]> createCosineSimilarityFunction() {
        final DistanceFunction<float[]> cosineDistanceFunction = createCosineDistanceFunction();
        return new DistanceFunction<float[]>() {
            @Override
            public float distance(final float[] a, final float[] b) {
                return 1-cosineDistanceFunction.distance(a, b);
            }

            @Override
            public String toString() {
                return "CosineSimilarity{caching,stateful}";
            }
        };
    }

    /**
     * Creates a cosine similarity function.
     * <p/>
     * Assuming the arrays to compare are fourier spectra, limiting the length and selecting an
     * offset can implicitly band-pass filter the spectra before computing the similarity.
     *
     * @param length length of the array to compare
     * @param offset offset into both arrays
     * @see Floats#cosineSimilarity(float[], float[], int, int)
     * @return distance function
     */
    public static DistanceFunction<float[]> createCosineSimilarityFunction(final int offset, final int length) {
        return new DistanceFunction<float[]>() {
            public float distance(final float[] last, final float[] now) {
                return (float)Floats.cosineSimilarity(now, last, offset, length);
            }

            @Override
            public String toString() {
                return "CosineSimilarity{offset=" + offset + ", length=" + length + "}";
            }

        };
    }
}
