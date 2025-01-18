/*
 * =================================================
 * Copyright 2018 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Matrix that optimizes memory for matrices that contain sparse rows.
 * I.e. most rows have at least one value and the method {@link Matrix#getRow(int)}
 * runs much more quickly than {@link Matrix#getColumn(int)}.
 * The same is true for {@link MutableMatrix#setRow(int, float[])} and its column
 * counterpart.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SparseRowMatrix extends MutableAbstractMatrix {

    private final Map<Integer, Map<Integer,Float>> map = new HashMap<Integer, Map<Integer,Float>>();

    /**
     * Creates a sparse matrix with the given dimensions.
     *
     * @param rows number of rows
     * @param columns number of columns
     * @param zeroPadded zero-pad or not?
     */
    public SparseRowMatrix(final int rows, final int columns, final boolean zeroPadded) {
        this.rows = rows;
        this.columns = columns;
        this.zeroPadded = zeroPadded;
    }

    /**
     * Creates a sparse matrix with the given dimensions.
     * The matrix is not zero-padded.
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    public SparseRowMatrix(final int rows, final int columns) {
        this(rows, columns, false);
    }

    @Override
    public float get(final int row, final int column) {
        if (isValidXORZeroPadded(row, column)) return 0f;
        final Map<Integer, Float> rowMap = map.get(row);
        if (rowMap == null) {
            return 0f;
        } else {
            final Float f = rowMap.get(column);
            return f == null ? 0f : f;
        }
    }

    @Override
    public float[] getRow(final int row) {
        final float[] values = new float[getNumberOfColumns()];
        final Map<Integer, Float> rowMap = map.get(row);
        if (rowMap != null) {
            for (final Map.Entry<Integer, Float> e : rowMap.entrySet()) {
                values[e.getKey()] = e.getValue();
            }
        }
        return values;
    }

    @Override
    public void set(final int row, final int column, final float value) {
        checkBounds(row, column);
        // attempt to save memory by re-using certain often-used Float objects.
        Map<Integer, Float> rowMap = map.get(row);
        if (value != 0f) {
            if (rowMap == null) {
                rowMap = new HashMap<Integer, Float>(4, 0.75f);
                map.put(row, rowMap);
            }
            rowMap.put(column, Floats.toFloat(value));
        } else {
            if (rowMap != null) {
                rowMap.remove(column);
                if (rowMap.isEmpty()) {
                    map.remove(row);
                }
            }
        }
    }

    @Override
    public void setRow(final int row, final float[] values) {
        if (values.length != getNumberOfColumns()) throw new IllegalArgumentException("Array must have the same length as row: " + getNumberOfColumns());
        Map<Integer, Float> rowMap = map.get(row);
        if (rowMap == null) {
            rowMap = new HashMap<Integer, Float>(4, 0.75f);
            map.put(row, rowMap);
        }
        for (int i=0; i<values.length; i++) {
            if (values[i] != 0f) {
                rowMap.put(i, Floats.toFloat(values[i]));
            } else {
                rowMap.remove(i);
            }
        }
        if (rowMap.isEmpty()) {
            map.remove(row);
        }
    }

    @Override
    public float sum() {
        float s = 0f;
        for (final Map<Integer, Float> m : map.values()) {
            for (final Float f : m.values()) {
                s += f;
            }
        }
        return s;
    }

    @Override
    public float[] rowSum() {
        final int numberOfRows = getNumberOfRows();
        final float[] s = new float[numberOfRows];
        for (int row=0; row<numberOfRows; row++) {
            final Map<Integer, Float> rowMap = map.get(row);
            if (rowMap != null) {
                for (final Float f : rowMap.values()) {
                    s[row] += f;
                }
            }
        }
        return s;
    }

    @Override
    public float[] columnSum() {
        final int numberOfRows = getNumberOfRows();
        final int numberOfColumns = getNumberOfColumns();
        final float[] s = new float[numberOfColumns];
        for (int row=0; row<numberOfRows; row++) {
            final Map<Integer, Float> rowMap = map.get(row);
            if (rowMap != null) {
                for (final Map.Entry<Integer, Float> e : rowMap.entrySet()) {
                    s[e.getKey()] += e.getValue();
                }
            }
        }
        return s;
    }


    @Override
    protected void allocate(final MatrixBackingBuffer buffer) {
        throw new RuntimeException(getClass().getSimpleName() + " does not use a regular MatrixBackingBuffer.");
    }

    @Override
    public String toString() {
        return "SparseRowMatrix{" +
            "rows=" + rows +
            ", columns=" + columns +
            ", zeroPadded=" + isZeroPadded() +
            '}';
    }
}
