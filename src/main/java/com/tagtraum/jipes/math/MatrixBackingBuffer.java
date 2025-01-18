/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

/**
 * Allows customization of how data is stored in a {@link Matrix}.
 */
public interface MatrixBackingBuffer {

    /**
     * Allocates the internal data structure.
     *
     * @param size size
     */
    void allocate(int size);

    /**
     * Indicates whether the internal data structure is already allocated.
     *
     * @return true or false
     */
    boolean isAllocated();

    /**
     * Write a value.
     *
     * @param index index
     * @param value value
     */
    void set(int index, float value);

    /**
     * Reads a value.
     *
     * @param index index
     * @return value
     */
    float get(int index);
}
