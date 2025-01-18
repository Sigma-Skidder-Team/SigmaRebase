/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
* Mutable {@link Matrix}.
*
* @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
*/
public interface MutableMatrix extends Matrix {

    /**
     * Sets the given value.
     *
     * @param row row
     * @param column column
     * @param value value
     * @throws IndexOutOfBoundsException row, column or both are out of bounds
     * @throws IllegalArgumentException if the given value cannot be stored in the matrix, e.g. because it is
     * not in the valid range
     */
    void set(int row, int column, float value);

    /**
     * Sets the given values.
     *
     * @param row row
     * @param values values
     * @throws IndexOutOfBoundsException row is out of bounds
     * @throws IllegalArgumentException if the given value cannot be stored in the matrix, e.g. because it is
     * not in the valid range
     */
    void setRow(int row, float[] values);

    /**
     * Sets the given values.
     *
     * @param column column
     * @param values values
     * @throws IndexOutOfBoundsException column is out of bounds
     * @throws IllegalArgumentException if the given value cannot be stored in the matrix, e.g. because it is
     * not in the valid range
     */
    void setColumn(int column, float[] values);

    /**
     * Fills all writable elements of this matrix with the given value.
     *
     * @param value value
     */
    void fill(float value);

    /**
     * Copies values from the provided array row by row to this matrix.
     *
     * @param values values
     */
    void copy(float[] values);

    /**
     * Copies values from another {@link Matrix} to this matrix.
     * If this matrix does not support the setting of certain elements (e.g. {@link SymmetricBandMatrix}),
     * the method fails silently, i.e. no values are copied, and no exception is raised.
     *
     * @param fromMatrix other matrix
     */
    void copy(Matrix fromMatrix);

    /**
     * Copies values from a defined offset from another matrix to a defined offset of this matrix.
     * If this matrix does not support the setting of certain elements (e.g. {@link SymmetricBandMatrix}),
     * the method fails silently, i.e. no values are copied, and no exception is raised.
     *
     * @param fromMatrix other matrix
     * @param fromRow row start offset of from matrix
     * @param fromColumn column start offset of from matrix
     * @param toRow column start offset of this matrix
     * @param toColumn column start offset of this matrix
     * @param rows number of rows to copy
     * @param columns number of column to copy
     */
    void copy(Matrix fromMatrix, int fromRow, int fromColumn,
              int toRow, int toColumn,
              int rows, int columns);
}
