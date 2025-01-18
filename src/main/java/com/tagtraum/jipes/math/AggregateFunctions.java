/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.List;

/**
 * Pre-defined aggregate functions.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see AggregateFunction
 */
public final class AggregateFunctions {

    private AggregateFunctions() {
    }

    public static final AggregateFunction<float[], Float> ARITHMETIC_MEAN = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.arithmeticMean(collection);
        }

        @Override
        public String toString() {
            return "ARITHMETIC_MEAN";
        }
    };

    public static final AggregateFunction<float[], Float> EUCLIDEAN_NORM = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return (float)Floats.euclideanNorm(collection);
        }

        @Override
        public String toString() {
            return "EUCLIDEAN_NORM";
        }
    };

    public static final AggregateFunction<float[], Float> SUM = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.sum(collection);
        }

        @Override
        public String toString() {
            return "SUM";
        }
    };

    public static final AggregateFunction<List<float[]>, float[]> ELEMENT_WISE_SUM = new AggregateFunction<List<float[]>, float[]>() {
        public float[] aggregate(final List<float[]> collection) {
            return Floats.sum(collection);
        }

        @Override
        public String toString() {
            return "ELEMENT_WISE_SUM";
        }
    };

    public static final AggregateFunction<float[], Float> MINIMUM = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.min(collection);
        }

        @Override
        public String toString() {
            return "MINIMUM";
        }
    };

    public static final AggregateFunction<float[], Float> MAXIMUM = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.max(collection);
        }

        @Override
        public String toString() {
            return "MAXIMUM";
        }
    };

    public static final AggregateFunction<float[], Float> CORRECTED_STANDARD_DEVIATION = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.correctedStandardDeviation(collection);
        }

        @Override
        public String toString() {
            return "CORRECTED_STANDARD_DEVIATION";
        }
    };

    public static final AggregateFunction<float[], Float> STANDARD_DEVIATION = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.standardDeviation(collection);
        }

        @Override
        public String toString() {
            return "STANDARD_DEVIATION";
        }
    };

    public static final AggregateFunction<float[], Float> VARIANCE = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.variance(collection);
        }

        @Override
        public String toString() {
            return "VARIANCE";
        }
    };

    public static final AggregateFunction<float[], Float> UNBIASED_VARIANCE = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.unbiasedVariance(collection);
        }

        @Override
        public String toString() {
            return "UNBIASED_VARIANCE";
        }
    };

    public static final AggregateFunction<float[], Float> ROOT_MEAN_SQUARE = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.rootMeanSquare(collection);
        }

        @Override
        public String toString() {
            return "ROOT_MEAN_SQUARE";
        }
    };

    public static final AggregateFunction<float[], Float> ZERO_CROSSING_RATE = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] collection) {
            return Floats.zeroCrossingRate(collection);
        }

        @Override
        public String toString() {
            return "ZERO_CROSSING_RATE";
        }
    };

    /**
     * Computes the percentage of values that lie below the average of a collection.
     *
     * @see <a href="http://webhome.cs.uvic.ca/~gtzan/work/pubs/tsap02gtzan.pdf">G. Tzanetakis and P. Cook.
     * Musical Genre Classification of Audio Signals, IEEE Transactions on Speech and Audio Processing , 10(5), July 2002</a>
     */
    public static final AggregateFunction<float[], Float> PERCENTAGE_BELOW_AVERAGE = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] array) {
            return Floats.percentageBelowAverage(array);
        }

        @Override
        public String toString() {
            return "PERCENTAGE_BELOW_AVERAGE";
        }
    };

    /**
     * Computes the relative entropy of the given data. Values &lt;0 are set to 0.
     * <p/>
     * <code>H(p)=-sum(p.*log(p)) /log(length(p));</code>
     *
     * @see <a href="http://en.wikipedia.org/wiki/Entropy_(information_theory)">Shannon's entropy</a>
     */
    public static final AggregateFunction<float[], Float> RELATIVE_ENTROPY = new AggregateFunction<float[], Float>() {
        public Float aggregate(final float[] floats) {
            final float[] data = floats.clone();
            // remove negative values
            double sum = 0;
            for (int i=0; i<floats.length; i++) {
                float d = data[i];
                if (d < 0) {
                    data[i] = 0;
                } else {
                    sum += d;
                }
            }
            if (sum == 0) sum = 1e-12;
            for (int i=0; i<floats.length; i++) {
                data[i] = (float) (data[i]/sum);
            }
            double entropy = 0;
            for (final float d : data) {
                entropy += d * Math.log(d+1e-12);
            }
            return (float)(-entropy / Math.log(data.length));
        }

        @Override
        public String toString() {
            return "RELATIVE_ENTROPY";
        }
    };

}
