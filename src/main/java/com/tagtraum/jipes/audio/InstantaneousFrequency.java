/*
 * =================================================
 * Copyright 2017 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;

import java.io.IOException;

/**
 * Combines subsequent {@link LinearFrequencySpectrum}s into a {@link InstantaneousFrequencySpectrum},
 * potentially providing better frequency resolution.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class InstantaneousFrequency implements SignalProcessor<LinearFrequencySpectrum, InstantaneousFrequencySpectrum> {

    private final SignalProcessorSupport<InstantaneousFrequencySpectrum> support = new SignalProcessorSupport<InstantaneousFrequencySpectrum>();
    private final Object id;
    private LinearFrequencySpectrum lastLinearSpectrum;
    private InstantaneousFrequencySpectrum out;

    public InstantaneousFrequency() {
        this(null);
    }

    public InstantaneousFrequency(final Object id) {
        this.id = id;
    }

    @Override
    public void process(final LinearFrequencySpectrum spectrum) throws IOException {
        if (this.lastLinearSpectrum != null) {
            out = new InstantaneousFrequencySpectrum(this.lastLinearSpectrum, spectrum);
            support.process(out);
        }
        this.lastLinearSpectrum = new LinearFrequencySpectrum(spectrum);
    }

    @Override
    public void flush() throws IOException {
        support.flush();
    }

    @Override
    public InstantaneousFrequencySpectrum getOutput() throws IOException {
        return out;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public <O2> SignalProcessor<InstantaneousFrequencySpectrum, O2> connectTo(final SignalProcessor<InstantaneousFrequencySpectrum, O2> signalProcessor) {
        return support.connectTo(signalProcessor);
    }

    @Override
    public <O2> SignalProcessor<InstantaneousFrequencySpectrum, O2> disconnectFrom(final SignalProcessor<InstantaneousFrequencySpectrum, O2> signalProcessor) {
        return support.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<InstantaneousFrequencySpectrum, ?>[] getConnectedProcessors() {
        return support.getConnectedProcessors();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InstantaneousFrequency that = (InstantaneousFrequency) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InstantaneousFrequency{" +
            "id=" + id +
            '}';
    }
}
