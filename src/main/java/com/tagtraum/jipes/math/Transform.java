/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Transform that can be obtained for example through a {@link FFTFactory} or a {@link ConstantQTransformFactory}.
 * <p/>
 * The forward transform is typically from the time domain to the frequency domain,
 * the inverse the other way around. Note that not all methods of this interface are necessarily implemented
 * by its implementing classes. Unsupported methods throw {@link UnsupportedOperationException}s.
 * <p/>
 * Date: 1/15/11
 * Time: 8:03 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface Transform {

    /**
     * Performs transform on real values.
     *
     * @param real input array of real numbers
     * @return array with the real and the imaginary part of the transform, the third float array contains
     * the normalized frequencies, i.e. 1.0 is equal to the sample rate of the input
     * @throws UnsupportedOperationException should the implementation not support this operation
     */
    public float[][] transform(float[] real) throws UnsupportedOperationException;

    /**
     * Performs a complex transform.
     *
     * @param real input array of floats
     * @param imaginary input array of floats
     * @return array with the real and the imaginary part of the transform, the third float array contains
     * the normalized frequencies, i.e. 1.0 is equal to the sample rate of the input
     * @throws UnsupportedOperationException should the implementation not support this operation
     */
    public float[][] transform(float[] real, float[] imaginary) throws UnsupportedOperationException;


    /**
     * Performs an inverse transform.
     *
     * @param real input array of floats
     * @param imaginary input array of floats
     * @return array with the real and the imaginary part of the transform
     * @throws UnsupportedOperationException should the implementation not support this operation
     */
    public float[][] inverseTransform(float[] real, float[] imaginary) throws UnsupportedOperationException;

}
