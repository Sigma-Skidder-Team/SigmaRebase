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
 * Matrix that optimizes memory for matrices that contain sparse columns.
 * I.e. most columns have at least one value and the method {@link Matrix#getColumn(int)}
 * runs much more quickly than {@link Matrix#getRow(int)}.
 * The same is true for {@link MutableMatrix#setColumn(int, float[])} and its column
 * counterpart.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SparseColumnMatrix extends MutableAbstractMatrix {

    private final Map<Integer, Map<Integer,Float>> map = new HashMap<Integer, Map<Integer,Float>>();

    /**
     * Creates a sparse matrix with the given dimensions.
     *
     * @param rows number of rows
     * @param columns number of columns
     * @param zeroPadded zero-pad or not?
     */
    public SparseColumnMatrix(final int rows, final int columns, final boolean zeroPadded) {
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
    public SparseColumnMatrix(final int rows, final int columns) {
        this(rows, columns, false);
    }

    @Override
    public float get(final int row, final int column) {
        if (isValidXORZeroPadded(row, column)) return 0f;
        final Map<Integer, Float> columnMap = map.get(column);
        if (columnMap == null) {
            return 0f;
        } else {
            final Float f = columnMap.get(row);
            return f == null ? 0f : f;
        }
    }

    @Override
    public float[] getColumn(final int column) {
        final float[] values = new float[getNumberOfRows()];
        final Map<Integer, Float> columnMap = map.get(column);
        if (columnMap != null) {
            for (final Map.Entry<Integer, Float> e : columnMap.entrySet()) {
                values[e.getKey()] = e.getValue();
            }
        }
        return values;
    }

    @Override
    public void set(final int row, final int column, final float value) {
        checkBounds(row, column);
        // attempt to save memory by re-using certain often-used Float objects.
        Map<Integer, Float> columnMap = map.get(column);
        if (value != 0f) {
            if (columnMap == null) {
                columnMap = new HashMap<Integer, Float>(4, 0.75f);
                map.put(column, columnMap);
            }
            columnMap.put(row, Floats.toFloat(value));
        } else {
            if (columnMap != null) {
                columnMap.remove(row);
                if (columnMap.isEmpty()) {
                    map.remove(column);
                }
            }
        }
    }

    @Override
    public void setColumn(final int column, final float[] values) {
        if (values.length != getNumberOfRows()) throw new IllegalArgumentException("Array must have the same length as row: " + getNumberOfColumns());
        Map<Integer, Float> columnMap = map.get(column);
        if (columnMap == null) {
            columnMap = new HashMap<Integer, Float>(4, 0.75f);
            map.put(column, columnMap);
        }
        for (int i=0; i<values.length; i++) {
            if (values[i] != 0f) {
                columnMap.put(i, Floats.toFloat(values[i]));
            } else {
                columnMap.remove(i);
            }
        }
        if (columnMap.isEmpty()) {
            map.remove(column);
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
    public float[] columnSum() {
        final int numberOfColumns = getNumberOfColumns();
        final float[] s = new float[numberOfColumns];
        for (int column=0; column<numberOfColumns; column++) {
            final Map<Integer, Float> columnMap = map.get(column);
            if (columnMap != null) {
                for (final Float f : columnMap.values()) {
                    s[column] += f;
                }
            }
        }
        return s;
    }

    @Override
    public float[] rowSum() {
        final int numberOfRows = getNumberOfRows();
        final int numberOfColumns = getNumberOfColumns();
        final float[] s = new float[numberOfRows];
        for (int column=0; column<numberOfColumns; column++) {
            final Map<Integer, Float> columnMap = map.get(column);
            if (columnMap != null) {
                for (final Map.Entry<Integer, Float> e : columnMap.entrySet()) {
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
        return "SparseColumnMatrix{" +
            "rows=" + rows +
            ", columns=" + columns +
            ", zeroPadded=" + isZeroPadded() +
            '}';
    }
}
