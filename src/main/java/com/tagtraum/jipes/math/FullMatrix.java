/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.io.*;
import java.nio.FloatBuffer;

/**
* Fully-backed full-float-precision {@link Matrix}.
*
* @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
*/
public class FullMatrix extends MutableAbstractMatrix implements MutableMatrix, Cloneable {

    /**
     * Constructs fully backed matrix with values from a CSV file.
     *
     * @param csvFile file
     * @param charset charset
     * @param separator seperator between values, usually ','
     * @param ignoreFirstLine whether the first line should be ignored (non-numerical headers)
     * @throws IOException
     */
    public FullMatrix(final File csvFile, final String charset, final char separator, final boolean ignoreFirstLine) throws IOException {
        final CSVReader csvReader = new CSVReader();
        csvReader.read(csvFile, charset, separator, ignoreFirstLine);
        this.rows = csvReader.getRows();
        this.columns = csvReader.getColumns();
        final FloatBuffer floats = csvReader.getValues();
        this.buffer = new MatrixBackingBuffer() {
            @Override
            public void allocate(final int size) {
            }

            @Override
            public boolean isAllocated() {
                return true;
            }

            @Override
            public void set(final int index, final float value) {
                floats.put(index, value);
            }

            @Override
            public float get(final int index) {
                return floats.get(index);
            }
        };
    }

    /**
     * Creates a fully backed matrix with the given number of rows and columns.
     * Memory is allocated from the Java heap. The matrix is <em>not</em> zero-padded.
     * Initial values are taken from the provided array, continuously row by row.
     * The values are copied, not referenced.
     *
     * @param rows rows
     * @param columns columns
     * @param values initial values
     * @see MutableMatrix#copy(float[])
     */
    public FullMatrix(final int rows, final int columns, final float[] values) {
        this(rows, columns, new FloatBackingBuffer(false), false);
        copy(values);
    }

    /**
     * Creates a fully backed matrix with the given number of rows and columns.
     * Memory is allocated from the Java heap. The matrix is <em>not</em> zero-padded.
     *
     * @param rows rows
     * @param columns columns
     */
    public FullMatrix(final int rows, final int columns) {
        this(rows, columns, new FloatBackingBuffer(false), false);
    }

    /**
     * Creates a fully backed matrix with the given number of rows and columns.
     * Memory is allocated through the given backing buffer. The matrix may be zero-padded.
     *
     * @param rows rows
     * @param columns columns
     * @param buffer backing buffer
     * @param zeroPadded shall the matrix be zero padded?
     */
    public FullMatrix(final int rows, final int columns, final MatrixBackingBuffer buffer, final boolean zeroPadded) {
        this(rows, columns, buffer, zeroPadded, true);
    }

    /**
     * Creates a fully backed matrix, filled with the values from the given matrix.
     * Memory is allocated through the given backing buffer. The matrix may be zero-padded.
     *
     * @param matrix matrix to copy
     * @param buffer backing buffer
     * @param zeroPadded shall the matrix be zero padded?
     */
    public FullMatrix(final Matrix matrix, final MatrixBackingBuffer buffer, final boolean zeroPadded) {
        this(matrix.getNumberOfRows(), matrix.getNumberOfColumns(), buffer, zeroPadded, true);
        copy(matrix);
    }

    /**
     * Creates a fully backed matrix with the given number of rows and columns.
     * Memory is allocated through a {@link FloatBackingBuffer}, either on the heap or natively.
     * The matrix may be zero-padded.
     *
     * @param rows rows
     * @param columns columns
     * @param direct shall the memory be allocated directly?
     * @param zeroPadded shall the matrix be zero padded?
     */
    public FullMatrix(final int rows, final int columns, final boolean direct, final boolean zeroPadded) {
        this(rows, columns, new FloatBackingBuffer(direct), zeroPadded);
    }

    protected FullMatrix(final int rows, final int columns, final MatrixBackingBuffer buffer, final boolean zeroPadded, final boolean allocate) {
        this.buffer = buffer;
        this.rows = rows;
        this.columns = columns;
        this.zeroPadded = zeroPadded;
        if (allocate) {
            allocate(buffer);
        }
    }

    @Override
    protected void allocate(final MatrixBackingBuffer buffer) {
        buffer.allocate(this.rows*this.columns);
    }

    /**
     * Reads values from CSV file.
     */
    private static class CSVReader {
        private int rows;
        private int columns;
        private FloatBuffer floats;

        private int getColumns() {
            return columns;
        }

        private FloatBuffer getValues() {
            return floats;
        }

        private int getRows() {
            return rows;
        }

        private void read(final File file, final String charset, final char separator, final boolean ignoreFirstLine) throws IOException {
            floats = FloatBuffer.allocate(1024);
            final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            try {
                String line;
                final StringBuilder sb = new StringBuilder();
                int state = 0;
                if (ignoreFirstLine) {
                    in.readLine();
                }
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() == 0) continue;
                    rows++;
                    for (int i=0; i<line.length(); i++) {
                        final char c=line.charAt(i);
                        switch(state) {
                            case 0: // regular value
                                if (c==separator) {
                                    floats = put(floats, Float.valueOf(sb.toString().trim()));
                                    sb.setLength(0);
                                }
                                else if (c=='"') {
                                    state = 1;
                                }
                                else {
                                    sb.append(c);
                                }
                                break;
                            case 1: // in a string
                                if (c=='"') {
                                    state = 2;
                                } else {
                                    sb.append(c);
                                }
                                break;
                            case 2: // we are in a string and just saw a quote
                                if (c=='"') {
                                    state = 1;
                                    sb.append(c);
                                } else {
                                    // just read a string...
                                    floats = put(floats, Float.valueOf(sb.toString().trim()));
                                    sb.setLength(0);
                                    if (c==separator){
                                        state = 0;
                                    } else {
                                        state = 3;
                                    }
                                }
                                break;
                            case 3: // string is over, we just wait for the next separator
                                if (c==separator) {
                                    state = 0;
                                }
                                break;
                        }
                    }
                    if (sb.length() != 0) {
                        floats = put(floats, Float.valueOf(sb.toString().trim()));
                        sb.setLength(0);
                    }
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            floats.flip();
            columns = floats.limit()/rows;
        }

        private static FloatBuffer put(final FloatBuffer buffer, final float value) {
            final FloatBuffer floatBuffer = ensureCapacity(buffer);
            floatBuffer.put(value);
            return floatBuffer;
        }

        private static FloatBuffer ensureCapacity(final FloatBuffer buffer) {
            if (buffer.position() == buffer.capacity()) {
                final FloatBuffer newBuffer = FloatBuffer.allocate(buffer.capacity()*2);
                buffer.flip();
                newBuffer.put(buffer);
                return newBuffer;
            }
            return buffer;
        }

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
