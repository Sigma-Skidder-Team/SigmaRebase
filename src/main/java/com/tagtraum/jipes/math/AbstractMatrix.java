/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Base implementation for a {@link Matrix}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class AbstractMatrix implements Matrix {

    protected int rows;
    protected int columns;
    protected boolean zeroPadded;
    protected MatrixBackingBuffer buffer;

    @Override
    public Matrix add(final Matrix m) {
        return new AddMatrix(this, m);
    }

    @Override
    public Matrix subtract(final Matrix m) {
        return new SubtractMatrix(this, m);
    }

    @Override
    public Matrix multiply(final float scalar) {
        return new ScalarMultiplicationMatrix(this, scalar);
    }

    @Override
    public Matrix multiply(final Matrix m) {
        return new MultiplicationMatrix(this, m);
    }

    @Override
    public Matrix transpose() {
        return new TransposeMatrix(this);
    }

    @Override
    public Matrix translate(final int rows, final int columns) {
        return new TranslatedMatrix(this, rows, columns);
    }

    @Override
    public Matrix hadamardMultiply(final Matrix m) {
        return new HadamardProductMatrix(this, m);
    }

    @Override
    public Matrix enlarge(final Matrix m) {
        return new EnlargeMatrix(this, m);
    }

    @Override
    public int getNumberOfColumns() {
        return columns;
    }

    @Override
    public int getNumberOfRows() {
        return rows;
    }

    @Override
    public boolean isZeroPadded() {
        return zeroPadded;
    }

    @Override
    public float get(final int row, final int column) {
        if (isValidXORZeroPadded(row, column)) return 0f;
        return get(toIndex(row, column));
    }

    @Override
    public float sum() {
        float s = 0f;
        for (int row=0; row<getNumberOfRows(); row++) {
            for (int column=0; column<getNumberOfColumns(); column++) {
                s += get(row, column);
            }
        }
        return s;
    }

    @Override
    public float[] rowSum() {
        final int numberOfRows = getNumberOfRows();
        final int numberOfColumns = getNumberOfColumns();
        final float[] s = new float[numberOfRows];
        for (int row = 0; row< numberOfRows; row++) {
            for (int column = 0; column< numberOfColumns; column++) {
                s[row] += get(row, column);
            }
        }
        return s;
    }

    @Override
    public float[] columnSum() {
        final int numberOfColumns = getNumberOfColumns();
        final int numberOfRows = getNumberOfRows();
        final float[] s = new float[numberOfColumns];
        for (int row = 0; row< numberOfRows; row++) {
            for (int column = 0; column< numberOfColumns; column++) {
                s[column] += get(row, column);
            }
        }
        return s;
    }

    @Override
    public float[] getRow(final int row) {
        final float[] values = new float[getNumberOfColumns()];
        for (int column=0; column< values.length; column++) {
            values[column] = get(row, column);
        }
        return values;
    }

    @Override
    public float[] getColumn(final int column) {
        final float[] values = new float[getNumberOfRows()];
        for (int row=0; row< values.length; row++) {
            values[row] = get(row, column);
        }
        return values;
    }

    protected boolean isValidXORZeroPadded(final int row, final int column) {
        if (isInvalid(row, column)) {
            if (isZeroPadded()) return true;
            else throw new IndexOutOfBoundsException("Row: " + row + ", Column: " + column);
        }
        return false;
    }

    protected void checkBounds(final int row, final int column) {
        if (isInvalid(row, column)) {
            throw new IndexOutOfBoundsException("Row: " + row + ", Column: " + column);
        }
    }

    protected boolean isInvalid(final int row, final int column) {
        return row<0 || column<0 || row>=getNumberOfRows() || column>=getNumberOfColumns();
    }

    protected int toIndex(final int row, final int column) {
        return row * columns + column;
    }

    protected float get(final int index) {
        return buffer.get(index);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Matrix)) return false;
        final Matrix that = (Matrix)o;
        if (this.getNumberOfColumns() != that.getNumberOfColumns()) return false;
        if (this.getNumberOfRows() != that.getNumberOfRows()) return false;
        // Ui! This is potentially really expensive
        for (int row=0; row<getNumberOfRows(); row++) {
            for (int column=0; column<getNumberOfColumns(); column++) {
                if (this.get(row, column) != that.get(row, column)) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getNumberOfRows();
        result = 31 * result + getNumberOfColumns();
        final int max = Math.min(100, Math.min(getNumberOfRows(), getNumberOfColumns()));
        for (int i=0; i<max; i++) {
            result = 31 * result + Float.floatToIntBits(get(i,i));
        }
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "rows=" + getNumberOfRows() +
                ", columns=" + getNumberOfColumns() +
                ", zeroPadded=" + isZeroPadded() +
                '}';
    }

    private static class HadamardProductMatrix extends AbstractMatrix {

        private final Matrix m1;
        private final Matrix m2;

        public HadamardProductMatrix(final Matrix m1, final Matrix m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        @Override
        public float get(final int row, final int column) {
            return m1.get(row, column) * m2.get(row, column);
        }

        @Override
        public int getNumberOfRows() {
            return m1.getNumberOfRows() > m2.getNumberOfRows() ? m1.getNumberOfRows() : m2.getNumberOfRows();
        }

        @Override
        public int getNumberOfColumns() {
            return m1.getNumberOfColumns() > m2.getNumberOfColumns() ? m1.getNumberOfColumns() : m2.getNumberOfColumns();
        }

        @Override
        public boolean isZeroPadded() {
            return m1.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class TranslatedMatrix extends AbstractMatrix {

        private final Matrix m;

        public TranslatedMatrix(final Matrix m, final int rows, final int columns) {
            this.m = m;
            this.rows = rows;
            this.columns = columns;
        }

        @Override
        public float get(final int row, final int column) {
            return m.get(row-rows, column-columns);
        }

        @Override
        public int getNumberOfRows() {
            return Math.max(0, m.getNumberOfRows()+rows);
        }

        @Override
        public int getNumberOfColumns() {
            return Math.max(0, m.getNumberOfColumns()+columns);
        }

        @Override
        public boolean isZeroPadded() {
            return m.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class AddMatrix extends AbstractMatrix {
        private final Matrix m1;
        private final Matrix m2;

        public AddMatrix(final Matrix m1, final Matrix m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        @Override
        public float get(final int row, final int column) {
            return m1.get(row, column) + m2.get(row, column);
        }

        @Override
        public int getNumberOfRows() {
            return m1.getNumberOfRows() > m2.getNumberOfRows() ? m1.getNumberOfRows() : m2.getNumberOfRows();
        }

        @Override
        public int getNumberOfColumns() {
            return m1.getNumberOfColumns() > m2.getNumberOfColumns() ? m1.getNumberOfColumns() : m2.getNumberOfColumns();
        }

        @Override
        public boolean isZeroPadded() {
            return m1.isZeroPadded() && m2.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class SubtractMatrix extends AbstractMatrix {
        private final Matrix m1;
        private final Matrix m2;

        public SubtractMatrix(final Matrix m1, final Matrix m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        @Override
        public float get(final int row, final int column) {
            return m1.get(row, column) - m2.get(row, column);
        }

        @Override
        public int getNumberOfRows() {
            return m1.getNumberOfRows() > m2.getNumberOfRows() ? m1.getNumberOfRows() : m2.getNumberOfRows();
        }

        @Override
        public int getNumberOfColumns() {
            return m1.getNumberOfColumns() > m2.getNumberOfColumns() ? m1.getNumberOfColumns() : m2.getNumberOfColumns();
        }

        @Override
        public boolean isZeroPadded() {
            return m1.isZeroPadded() && m2.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class ScalarMultiplicationMatrix extends AbstractMatrix {
        private final Matrix m;
        private final float scalar;

        public ScalarMultiplicationMatrix(final Matrix m, final float scalar) {
            this.m = m;
            this.scalar = scalar;
        }

        @Override
        public float get(final int row, final int column) {
            return m.get(row, column) * scalar;
        }

        @Override
        public int getNumberOfRows() {
            return m.getNumberOfRows();
        }

        @Override
        public int getNumberOfColumns() {
            return m.getNumberOfColumns();
        }

        @Override
        public boolean isZeroPadded() {
            return m.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class MultiplicationMatrix extends AbstractMatrix {
        private final Matrix m1;
        private final Matrix m2;

        public MultiplicationMatrix(final Matrix m1, final Matrix m2) {
            if (m1.getNumberOfColumns() != m2.getNumberOfRows()) throw new IllegalArgumentException("Left columns != right rows");
            this.m1 = m1;
            this.m2 = m2;
        }

        @Override
        public float get(final int row, final int column) {
            float sum = 0f;
            for (int r=0;r<m1.getNumberOfColumns(); r++) {
                sum += m1.get(row, r) * m2.get(r, column);
            }
            return sum;
        }

        @Override
        public int getNumberOfRows() {
            return m1.getNumberOfRows();
        }

        @Override
        public int getNumberOfColumns() {
            return m2.getNumberOfColumns();
        }

        @Override
        public boolean isZeroPadded() {
            return m1.isZeroPadded() && m2.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class TransposeMatrix extends AbstractMatrix {
        private final Matrix m;

        public TransposeMatrix(final Matrix m) {
            this.m = m;
        }

        @Override
        public float get(final int row, final int column) {
            return m.get(column, row);
        }

        @Override
        public int getNumberOfRows() {
            return m.getNumberOfColumns();
        }

        @Override
        public int getNumberOfColumns() {
            return m.getNumberOfRows();
        }

        @Override
        public boolean isZeroPadded() {
            return m.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }

    private static class EnlargeMatrix extends AbstractMatrix {
        private final Matrix m1;
        private final Matrix m2;

        public EnlargeMatrix(final Matrix m1, final Matrix m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        @Override
        public float get(final int row, final int column) {
            if (row >= m1.getNumberOfRows() || column >= m1.getNumberOfColumns()) return m2.get(row, column);
            else return m1.get(row, column);
        }

        @Override
        public int getNumberOfRows() {
            return Math.max(m1.getNumberOfRows(), m2.getNumberOfRows());
        }

        @Override
        public int getNumberOfColumns() {
            return Math.max(m1.getNumberOfColumns(), m2.getNumberOfColumns());
        }

        @Override
        public boolean isZeroPadded() {
            return m2.isZeroPadded();
        }

        @Override
        protected float get(final int index) {
            throw new UnsupportedOperationException();
        }

    }
}
