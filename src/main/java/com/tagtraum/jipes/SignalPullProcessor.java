/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes;

/**
 * <p>Capable of processing a given object (most likely a data buffer of type <code>I</code> (input),
 * for example a <code>float[]</code>, and returning another object of type <code>O</code> (output).
 * This processor follows the <em>pull</em> model, i.e. the data flow is controlled from the <em>end</em>
 * of the processing pipeline. If your pipeline must contain forks/splits, to treat bands or channels separately,
 * this is <em>not</em> a suitable approach. Consider using {@link com.tagtraum.jipes.SignalProcessor}s in
 * combination with a {@link SignalSplit}.
 * </p>
 * <p>Typically you specify an {@link SignalSource} and set it as the generator for an {@link SignalPullProcessor}.
 * Then call {@link SignalSource#read()} to pull the data through the SignalPullProcessor from the SignalSource.
 * </p>
 * <p>Every pull processor is also a source. To apply multiple processing steps (e.g. stereo to mono,
 * low pass filtering, downsampling), you can chain multiple processors.
 * </p>
 *
 * @param <I> type of the input values
 * @param <O> type of the output values
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see com.tagtraum.jipes.SignalProcessor
 */
public interface SignalPullProcessor<I, O> extends SignalSource<O> {

    /**
     * Sets the source to use for this {@link SignalPullProcessor}.
     * <p/>
     * Because the method returns its parameter, it encourages
     * <a href="http://en.wikipedia.org/wiki/Method_chaining">method chaining</a>
     * to build processing chains/pipelines.
     *
     * @param source source
     * @param <I2> input type of the source processor
     * @return the just set source, if it is a {@link SignalPullProcessor}
     */
    public <I2> SignalPullProcessor<I2,I> connectTo(SignalPullProcessor<I2, I> source);

    /**
     * Sets the source to use for this {@link SignalPullProcessor}.
     *
     * @param source source
     */
    public void connectTo(SignalSource<I> source);

    /**
     * Get the current source/generator.
     *
     * @return source
     */
    public SignalSource<I> getConnectedSource();

}
