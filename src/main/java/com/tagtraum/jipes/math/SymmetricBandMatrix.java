/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Matrix for data that is mirrored along the diagonal, but only in a diagonal band along the diagonal -
 * all other values are zero.
 * Any data set in one half of the matrix is automatically set in the other half
 * without doubling memory consumption.
 * The <em>bandwidth</em> of the matrix is defined as the number of diagonals with non-zero
 * entries. Since this implementation is symmetric, possible bandwidths are 1, 3, 5, etc.
 * In other words: the number of diagonals <code>k1</code> below the main diagonal,
 * the number of diagonals <code>k2</code> above the main diagonal, plus the main diagonal.
 * <code>bandwidth := k1 + k2 + 1</code>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://en.wikipedia.org/wiki/Band_matrix">Banded Matrix on Wikipedia</a>
 */
public class SymmetricBandMatrix extends SymmetricMatrix {

    private final int columnsPerRow;
    private float defaultValue;

    /**
     * Creates a square, symmetric band matrix with the given number of rows and columns.
     * Memory is allocated from the Java heap. The matrix is <em>not</em> zero-padded.
     *
     * @param length rows == columns (square)
     * @param bandwidth bandwidth == k1 + k2 + 1, with k1 and k2 being the number of diagonals above and below the main diagonal
     * @param defaultValue value to return for elements outside the band
     */
    public SymmetricBandMatrix(final int length, final int bandwidth, final float defaultValue) {
        this(length, bandwidth, new FloatBackingBuffer(false), defaultValue, false);
    }

    /**
     * Creates a square, symmetric band matrix with the given number of rows and columns.
     * Memory is allocated via the given backing buffer. The matrix may be zero-padded.
     *
     * @param length rows == columns (square)
     * @param bandwidth bandwidth == k1 + k2 + 1, with k1 and k2 being the number of diagonals above and below the main diagonal
     * @param buffer backing buffer
     * @param defaultValue value to return for elements outside the band
     * @param zeroPadded zero pad?
     */
    public SymmetricBandMatrix(final int length, final int bandwidth, final MatrixBackingBuffer buffer, final float defaultValue, final boolean zeroPadded) {
        this(length, bandwidth, buffer, zeroPadded, defaultValue, true);
    }

    /**
     * Creates a square, symmetric band matrix with the given number of rows and columns.
     * Memory is allocated via a {@link FloatBackingBuffer} either directly/natively or on the Java heap.
     * The matrix may be zero-padded.
     *
     * @param length rows == columns (square)
     * @param bandwidth bandwidth == k1 + k2 + 1, with k1 and k2 being the number of diagonals above and below the main diagonal
     * @param direct allocate memory natively or on the Java heap
     * @param zeroPadded zero pad?
     */
    public SymmetricBandMatrix(final int length, final int bandwidth, final boolean direct, final boolean zeroPadded) {
        this(length, bandwidth, new FloatBackingBuffer(direct), 0f, zeroPadded);
    }

    protected SymmetricBandMatrix(final int length, final int bandwidth, final MatrixBackingBuffer buffer,
                                  final boolean zeroPadded, final float defaultValue, final boolean allocate) {
        super(0, length, buffer, zeroPadded, false);
        if (bandwidth % 2 == 0) throw new IllegalArgumentException("Bandwidth must be odd because of symmetry: " + bandwidth);
        this.columnsPerRow = Math.min(((bandwidth - 1) / 2 + 1), length);
        this.defaultValue = defaultValue;
        // to make things easy, we allocate more than necessary. Example 5x5 matrix:
        //
        // ***..
        // .***.
        // ..***
        // ...**|*    <-- 1x extra
        // ....*|**   <-- 2x extra
        //
        // The last two rows are too long - but we handle this in set and get
        if (allocate) {
            allocate(buffer);
        }
    }

    @Override
    protected void allocate(final MatrixBackingBuffer buffer) {
        buffer.allocate(columnsPerRow * rows);
    }

    /**
     * Fills the matrix with the given values and adjusts the <code>defaultValue</code>
     * returned for elements outside the band.
     *
     * @param value value
     */
    @Override
    public void fill(final float value) {
        for (int row = 0; row<rows; row++) {
            for (int column = 0; column<columns; column++) {
                if (row < column && column>=columnsPerRow+row && column<this.columns) continue;
                if (column < row && row>=columnsPerRow+column && row<this.rows) continue;
                set(row, column, value);
            }
        }
        defaultValue = value;
    }

    @Override
    public void copy(final float[] values) {
        for (int row = 0, i = 0; row<rows; row++) {
            for (int column = 0; column<columns; column++) {
                if (row < column && column>=columnsPerRow+row && column<this.columns) continue;
                if (column < row && row>=columnsPerRow+column && row<this.rows) continue;
                set(row, column, values[i]);
                i++;
            }
        }
    }

    @Override
    public void copy(final Matrix fromMatrix) {
        if (fromMatrix instanceof SymmetricBandMatrix && this.columnsPerRow == ((SymmetricBandMatrix) fromMatrix).columnsPerRow) {
            // since we know that the internal structure is identical, we can make copying vastly less complicated...
            final SymmetricBandMatrix that = (SymmetricBandMatrix) fromMatrix;
            final int length = columnsPerRow * fromMatrix.getNumberOfColumns();
            for (int i=0; i<length; i++) {
                this.buffer.set(i, that.buffer.get(i));
            }
        }
        else {
            super.copy(fromMatrix);
        }
    }

    @Override
    public void copy(final Matrix fromMatrix, final int fromRow, final int fromColumn,
                     final int toRow, final int toColumn,
                     final int rows, final int columns) {

        for (int currentFromRow = fromRow, currentToRow = toRow; currentFromRow< fromRow + rows; currentFromRow++, currentToRow++) {
            for (int currentFromColumn = fromColumn, currentToColumn = toColumn; currentFromColumn< fromColumn + columns; currentFromColumn++, currentToColumn++) {
                // simply ignore values that can't be set
                if (currentToRow < currentToColumn && currentToColumn>=columnsPerRow+currentToRow && currentToColumn<this.columns) continue;
                if (currentToColumn < currentToRow && currentToRow>=columnsPerRow+currentToColumn && currentToRow<this.rows) continue;
                set(currentToRow, currentToColumn, fromMatrix.get(currentFromRow, currentFromColumn));
            }
        }
    }




    @Override
    public float get(final int row, final int column) {
        if (row < column && column>=columnsPerRow+row && column<columns) return defaultValue;
        return super.get(row, column);
    }

    @Override
    public void set(final int row, final int column, final float value) {
        if (row < column && column>=columnsPerRow+row && column<columns) throw new IndexOutOfBoundsException("Storage at row: " + row + ", column: " + column + " is not supported by this matrix implementation.");
        super.set(row, column, value);
    }

    protected int toIndex(final int row, final int column) {
        return row*columnsPerRow + column-row;
    }

    @Override
    public String toString() {
        return "SymmetricBandMatrix{" +
                "bandwidth=" + ((columnsPerRow-1)*2+1) +
                ", length=" + columns +
                ", offset=" + offset +
                ", zeroPad=" + isZeroPadded() +
                '}';
    }
}
