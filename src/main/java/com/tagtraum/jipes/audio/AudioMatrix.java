/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.Matrix;

/**
 * Read-only matrix that holds (audio) data and some meta information in the form of an {@link javax.sound.sampled.AudioFormat}
 * instance.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface AudioMatrix extends AudioBuffer {

    /**
     * Access to a value given its row and column.
     *
     * @param row row
     * @param column column
     * @return value
     */
    float getData(int row, int column);

    /**
     * Access to a value given its row and column.
     *
     * @param row row
     * @param column column
     * @return value
     */
    float getRealData(int row, int column);

    /**
     * Access to the first row.
     *
     * @return columns of first row
     */
    @Override
    float[] getData();

    /**
     * Access to the first row.
     *
     * @return columns of first row
     */
    @Override
    float[] getRealData();

    /**
     * Number of rows.
     *
     * @return rows
     */
    int getNumberOfRows();

    /**
     * Number of columns.
     *
     * @return columns
     */
    int getNumberOfColumns();

    /**
     * Derives a new matrix from this matrix using the same audio format etc, but
     * different data.
     *
     * @param real real data
     * @param imaginary imaginary data
     * @return derived matrix
     */
    AudioMatrix derive(Matrix real, Matrix imaginary);

}
