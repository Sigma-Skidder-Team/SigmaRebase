/*
 * =================================================
 * Copyright 2018 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.HashMap;
import java.util.Map;

import static com.tagtraum.jipes.math.Floats.toFloat;

/**
 * Matrix that optimizes memory in case <em>both</em> axes are sparse.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SparseMatrix extends MutableAbstractMatrix {

    private final Map<Key, Float> map = new HashMap<Key, Float>();

    /**
     * Creates a sparse matrix with the given dimensions.
     *
     * @param rows number of rows
     * @param columns number of columns
     * @param zeroPadded zero-pad or not?
     */
    public SparseMatrix(final int rows, final int columns, final boolean zeroPadded) {
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
    public SparseMatrix(final int rows, final int columns) {
        this(rows, columns, false);
    }

    @Override
    public float get(final int row, final int column) {
        if (isValidXORZeroPadded(row, column)) return 0f;
        final Float f = map.get(new Key(row, column));
        return f == null ? 0f : f;
    }

    @Override
    public void set(final int row, final int column, final float value) {
        checkBounds(row, column);
        final Key key = new Key(row, column);
        if (value == 0f) {
            map.remove(key);
        } else {
            map.put(key, toFloat(value));
        }
    }

    @Override
    protected void allocate(final MatrixBackingBuffer buffer) {
        throw new RuntimeException(getClass().getSimpleName() + " does not use a regular MatrixBackingBuffer.");
    }

    @Override
    public float sum() {
        float s = 0f;
        for (final Float f : map.values()) {
            s += f;
        }
        return s;
    }

    @Override
    public String toString() {
        return "SparseMatrix{" +
            "rows=" + rows +
            ", columns=" + columns +
            ", zeroPadded=" + isZeroPadded() +
            '}';
    }


    private final static class Key {
        private final int row;
        private final int column;

        private Key(final int row, final int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public boolean equals(final Object o) {
            // we take some shortcuts here, which works, because we ALWAYS
            // create a new Key object (i.e. never encounter object identity)
            // and this class is private and final, which means we can skip the
            // class check.
            return column == ((Key) o).column && row == ((Key) o).row;
        }

        @Override
        public int hashCode() {
            return 31 * row + column;
        }
    }
}
