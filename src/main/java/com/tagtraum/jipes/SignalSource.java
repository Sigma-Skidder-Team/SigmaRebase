/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

import java.io.IOException;

/**
 * Signal source, for example for audio data.
 *
 * @param <O> type of the provided values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface SignalSource<O> {

    /**
     * Reset the internal state of this AudioGenerator.
     * Implementing methods must call the source's {@link SignalSource#reset()} method.
     */
    void reset();

    /**
     * Provide the next chunk of output data.
     * This method is called by a consumer of {@link SignalSource} data, typically an {@link com.tagtraum.jipes.SignalPullProcessor}.
     *
     * @return output data - perhaps a float[] or even a more complex object like a {@link com.tagtraum.jipes.audio.AudioBuffer}, <code>null</code> if no more data is available
     * @throws java.io.IOException if something goes wrong
     */
    O read() throws IOException;
}
