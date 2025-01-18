/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import com.tagtraum.jipes.math.Floats;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes {@link AudioSpectrum} to compute an onset strength signal in the form of an {@link AudioBuffer}
 * produced on {@link #flush()}.<br/>
 *
 * This particular onset strength is defined as the bandwise difference
 * <code>log(power(k,t)+1)-log(power(k,t-1)+1)</code> for the band <code>k</code> and the spectrum <code>t</code>.
 * Differences for all bands in the valid frequency range are averaged.
 * Note, that the input is half-wave rectified, i.e. only <em>increases</em> in power are taken into account.
 * You can manipulate the <code>incFactor</code> to require a certain amount of increase, i.e.
 * <code>power(k,t)>incFactor*power(k,t-1)</code> is evaluated before a difference is computed.<p/>
 *
 * You may specify lower and upper frequency bounds, to only take a certain frequency range into account. Often
 * a lower bound of 30Hz is a good idea.<p/>
 *
 * <code>Hopsize</code> is a required parameter, if you want the resulting buffers to use the correct {@link AudioFormat}.
 * If not specified, hopsize will default to the number of samples of the first input spectrum.<p/>
 *
 * This processor produces output only upon {@link #flush()}. The produced buffer will contain all
 * onset strength values. The values are normalized, i.e. divided by their max.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class OnsetStrength implements SignalProcessor<AudioSpectrum, AudioBuffer> {

    private final Object id;
    private final int hopSize;
    private final int low;
    private final int high;
    private final float incFactor;
    private final SignalProcessorSupport<AudioBuffer> signalProcessorSupport = new SignalProcessorSupport<AudioBuffer>();
    private int effectiveHopSize;
    private AudioFormat audioFormat;
    private final List<Float> onsetValues = new ArrayList<Float>();
    private AudioBuffer lastOut;
    private float[] lastPowers;

    /**
     * Creates an onset strength signal with the given lower and upper frequency bounds, hopsize and incFactor.
     *
     * @param id {@link com.tagtraum.jipes.SignalPump} id
     * @param low lower freq boundary in Hz
     * @param high upper freq boundary in Hz
     * @param hopSize hop size
     * @param incFactor incFactor
     */
    public OnsetStrength(final Object id, final int low, final int high, final int hopSize, final float incFactor) {
        this.id = id;
        this.low = low;
        this.high = high;
        this.hopSize = hopSize;
        this.incFactor = incFactor;
        this.effectiveHopSize = hopSize;
    }

    /**
     * Creates an onset strength signal with the given lower and upper frequency bounds, hopsize and an incFactor
     * of <code>1f</code>.
     *
     * @param low lower freq boundary in Hz
     * @param high upper freq boundary in Hz
     * @param hopSize hop size
     */
    public OnsetStrength(final int low, final int high, final int hopSize) {
        this(null, low, high, hopSize, 1f);
    }

    /**
     * Creates an onset strength signal with no frequency boundaries a hopsize equal to
     * {@link com.tagtraum.jipes.audio.AudioSpectrum#getNumberOfSamples()}
     * of the first processed spectrum and an incFactor of <code>1f</code>.
     */
    public OnsetStrength() {
        this(0, Integer.MAX_VALUE, -1);
    }

    @Override
    public void process(final AudioSpectrum spectrum) throws IOException {

        double sum = 0;
        final float[] powers = spectrum.getPowers();
        final float[] frequencies = spectrum.getFrequencies();

        if (lastPowers != null) {
            int values = 0;
            for (int i=0; i<powers.length; i++) {
                final float power = powers[i];
                final float lastPower = lastPowers[i];
                if (useFrequency(frequencies[i])) {
                    if (usePower(power, lastPower)) {
                        sum += difference(power, lastPower);
                    }
                    values++;
                }
            }
            final float onset = (float)(sum/values);
            onsetValues.add(onset);
        }
        if (audioFormat == null) {
            deriveOutputFormat(spectrum);
        }
        lastPowers = powers.clone();
    }

    /**
     * Computes the difference between a power and its predecessor.
     * By default their logs are computed first.
     * Called from {@link #process(AudioSpectrum)}.
     *
     * @param power power
     * @param lastPower previous power (same band)
     * @return some difference
     */
    protected double difference(final float power, final float lastPower) {
        return Math.log(power+1) - Math.log(lastPower+1);
    }

    /**
     * Indicate, whether we should proceed calculating the difference in some way,
     * for the given power and its predecessor (for a given frequency).
     * Called from {@link #process(AudioSpectrum)}.
     *
     * @param power power
     * @param lastPower previous power (same band)
     * @return true or false
     */
    protected boolean usePower(final float power, final float lastPower) {
        return power > incFactor * lastPower;
    }

    /**
     * Indicate, whether the power for the given frequency should be taken into account.
     * Called from {@link #process(AudioSpectrum)}.
     *
     * @param frequency frequency (of a given power)
     * @return true or false
     */
    protected boolean useFrequency(final float frequency) {
        return frequency >= low && frequency <= high;
    }

    private void deriveOutputFormat(final AudioBuffer spectrum) {
        final AudioFormat inputFormat = spectrum.getAudioFormat();
        if (hopSize < 0) effectiveHopSize = spectrum.getNumberOfSamples();
        final float sampleRate = inputFormat.getSampleRate() / effectiveHopSize;
        audioFormat = new AudioFormat(
                inputFormat.getEncoding(), sampleRate, inputFormat.getSampleSizeInBits(),
                inputFormat.getChannels(), inputFormat.getFrameSize(), sampleRate,
                inputFormat.isBigEndian());
    }


    @Override
    public void flush() throws IOException {
        if (audioFormat == null) return;
        this.lastOut = finish();
        this.signalProcessorSupport.process(lastOut);
        this.signalProcessorSupport.flush();
    }

    private RealAudioBuffer finish() {
        final float[] real = new float[onsetValues.size()];
        for (int i=0; i<real.length; i++) {
            real[i] = this.onsetValues.get(i);
        }
        final float max = Floats.max(real);
        if (max != 0) {
            for (int i = 0; i < real.length; i++) {
                real[i] /= max;
            }
        }

        return new RealAudioBuffer(0, real, audioFormat);
    }

    @Override
    public AudioBuffer getOutput() throws IOException {
        return lastOut;
    }

    @Override
    public Object getId() {
        return id == null ? toString() : id;
    }

    @Override
    public <O2> SignalProcessor<AudioBuffer, O2> connectTo(final SignalProcessor<AudioBuffer, O2> floatO2SignalProcessor) {
        return signalProcessorSupport.connectTo(floatO2SignalProcessor);
    }

    @Override
    public <O2> SignalProcessor<AudioBuffer, O2> disconnectFrom(final SignalProcessor<AudioBuffer, O2> floatO2SignalProcessor) {
        return signalProcessorSupport.disconnectFrom(floatO2SignalProcessor);
    }

    @Override
    public SignalProcessor<AudioBuffer, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final OnsetStrength that = (OnsetStrength) o;

        if (high != that.high) return false;
        if (hopSize != that.hopSize) return false;
        if (low != that.low) return false;
        if (Float.compare(that.incFactor, incFactor) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hopSize;
        result = 31 * result + low;
        result = 31 * result + high;
        result = 31 * result + (incFactor != +0.0f ? Float.floatToIntBits(incFactor) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OnsetStrength{" +
                "low=" + low +
                "Hz, high=" + high +
                "Hz, hop=" + hopSize +
                ", effectiveHop=" + effectiveHopSize +
                ", incFactor=" + incFactor +
                '}';
    }
}