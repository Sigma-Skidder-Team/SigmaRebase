/*
 * =================================================
 * Copyright 2013 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalProcessor;
import com.tagtraum.jipes.SignalProcessorSupport;
import com.tagtraum.jipes.math.*;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.tagtraum.jipes.audio.AudioBufferFunctions.createDistanceFunction;

/**
 * Self similarity processor computes a self-similarity matrix from {@link AudioBuffer} features.
 * The actual value is produced on {@link #flush()}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class SelfSimilarity<I extends AudioBuffer> implements SignalProcessor<I, AudioMatrix> {

    public static final int FULL_MATRIX = -1;
    private SignalProcessorSupport<AudioMatrix> signalProcessorSupport = new SignalProcessorSupport<AudioMatrix>();
    private LinkedList<I> spectra = new LinkedList<I>();
    private AudioFormat audioFormat;
    private DistanceFunction<I> distanceFunction = createDistanceFunction(DistanceFunctions.createCosineSimilarityFunction());
    private AudioMatrix matrix;
    private Matrix combinedChunksMatrix;
    private int firstFrame = -1;
    private int secondFrame = -1;
    private Object id;
    private int bandwidth = FULL_MATRIX;
    private MutableMatrix similarityMatrix;
    private int chunkOffset = 0;
    private int spectraOffset;
    private boolean copyOnMatrixEnlargement;


    /**
     * Self similarity processor with a limited bandwidth around the main diagonal.
     *
     * @param bandwidth bandwidth
     */
    public SelfSimilarity(final int bandwidth) {
        this("SelfSimilarity", bandwidth, null);
    }

    /**
     * Self similarity processor with a limited bandwidth around the main diagonal.
     *
     * @param id               id
     * @param distanceFunction distance function
     */
    public SelfSimilarity(final Object id, final int bandwidth, final DistanceFunction<I> distanceFunction) {
        setId(id);
        if (distanceFunction != null) setDistanceFunction(distanceFunction);
        setBandwidth(bandwidth);
    }

    /**
     * Self similarity processor using a full matrix (i.e. {@link #getBandwidth()} {@code == } {@link #FULL_MATRIX}).
     *
     * @param id id
     */
    public SelfSimilarity(final Object id) {
        this(id, FULL_MATRIX, null);
    }

    /**
     * Self similarity processor using a full matrix (i.e. {@link #getBandwidth()} {@code == } {@link #FULL_MATRIX}).
     */
    public SelfSimilarity() {
        this("SelfSimilarity");
    }

    public void setId(final Object id) {
        this.id = id;
    }

    /**
     * Sets the maximal distance between two {@link AudioBuffer}s that is still compared and
     * computed for the {@link AudioMatrix}. If this is set to a positive value, a
     * {@link com.tagtraum.jipes.math.SymmetricBandMatrix} is used internally to preserve memory
     * even when the input is long.
     *
     * @param bandwidth bandwidth, must be odd. Values 0 or less indicate full bandwidth
     * @see com.tagtraum.jipes.math.SymmetricBandMatrix
     * @see #createMatrix(int, int, int)
     */
    public void setBandwidth(final int bandwidth) {
        if (bandwidth > 0 && bandwidth % 2 == 0) throw new IllegalArgumentException("Bandwidth most be odd because of symmetry: " + bandwidth);
        this.bandwidth = bandwidth;
    }

    /**
     * Matrix bandwidth for matrices created by {@link #createMatrix(int, int, int)}.
     *
     * @return bandwidth or 0 or less for full bandwidth
     * @see com.tagtraum.jipes.math.SymmetricBandMatrix
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     * Sets the distance function used to compute the self similarity matrix.
     *
     * @param distanceFunction distance function
     * @see DistanceFunctions#createCosineDistanceFunction()
     */
    public void setDistanceFunction(final DistanceFunction<I> distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    /**
     * Indicates whether the similarity matrix should be copied or enlarged
     * (see {@link com.tagtraum.jipes.math.Matrix#enlarge(com.tagtraum.jipes.math.Matrix)})
     * with a new matrix.
     *
     * @return true or false
     */
    public boolean isCopyOnMatrixEnlargement() {
        return copyOnMatrixEnlargement;
    }

    /**
     * Copy the whole matrix, if we need to increase its size?
     *
     * @param copyOnMatrixEnlargement if true, the similarity matrix will be copied, if we need to increase its size
     * @see #isCopyOnMatrixEnlargement()
     */
    public void setCopyOnMatrixEnlargement(final boolean copyOnMatrixEnlargement) {
        this.copyOnMatrixEnlargement = copyOnMatrixEnlargement;
    }

    @Override
    public void process(final I input) throws IOException {
        try {
            spectra.add((I)input.clone());
            final int chunk = bandwidth / 2 + 1;
            if (bandwidth > 0 && (spectra.size()+spectraOffset) % chunk == 0) {
                calculateMatrixChunk();
            }
            if (firstFrame < 0) firstFrame = input.getFrameNumber();
            else if (secondFrame < 0) secondFrame = input.getFrameNumber();
            else if (audioFormat == null) deriveOutputFormat(input.getAudioFormat());
        } catch (CloneNotSupportedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        calculateMatrixChunk();
        this.matrix = new RealAudioMatrix(firstFrame, combinedChunksMatrix, audioFormat);
        this.signalProcessorSupport.process(matrix);
        this.signalProcessorSupport.flush();
    }

    private void calculateMatrixChunk() {
        if (similarityMatrix == null) {
            similarityMatrix = createMatrix(0, spectra.size()+spectraOffset, bandwidth);
            combinedChunksMatrix = similarityMatrix;
        } else if (similarityMatrix.getNumberOfRows()<spectra.size()+spectraOffset) {
            final MutableMatrix similarityMatrix = createMatrix(this.similarityMatrix.getNumberOfRows(), spectra.size()+spectraOffset, bandwidth);
            if (copyOnMatrixEnlargement) {
                similarityMatrix.copy(this.similarityMatrix);
                combinedChunksMatrix = similarityMatrix;
            } else {
                combinedChunksMatrix = combinedChunksMatrix.enlarge(similarityMatrix);
            }
            chunkOffset = this.similarityMatrix.getNumberOfRows();
            this.similarityMatrix = similarityMatrix;
        }

        final int chunk = bandwidth / 2 + 1;
        final int startRow = Math.max(0, chunkOffset - chunk + 1);
        final int length = similarityMatrix.getNumberOfRows();

        // because the following loop is critical, we want RandomAccess to the spectra list
        final List<I> randomAccessSpectra = new ArrayList<I>(spectra);

        for (int row=startRow; row<length; row++) {
            final I spectrumA = randomAccessSpectra.get(row - spectraOffset);
            final int maxColumn = row + bandwidth / 2;
            // max of row/chunkOffset to exploit symmetry
            for (int column=Math.max(row, chunkOffset); column<length; column++) {
                if (bandwidth > 0 && column > maxColumn) break;
                final float d = distanceFunction.distance(spectrumA, randomAccessSpectra.get(column - spectraOffset));
                similarityMatrix.set(row, column, d);
            }
        }
        // remove spectra we don't need any more to preserve memory
        while (spectra.size() > 2*chunk-1) {
            spectra.removeFirst();
            spectraOffset++;
        }
    }

    /**
     * By default this creates a symmetric, <code>float</code>-backed, zero-padded {@link com.tagtraum.jipes.math.Matrix}.
     * If a positive bandwidth (see {@link #getBandwidth()}) is set, the matrix may be banded.
     * You may override this method to use a different matrix implementation,
     * e.g. one that is backed by an {@link com.tagtraum.jipes.math.UnsignedByteBackingBuffer}
     * to preserve memory.
     *
     * @param previousLength the length of the previously used matrix, in essence information about the part
     *                       that does not need to be covered by the new matrix (may be ignored).
     * @param length length
     * @param bandwidth bandwidth
     * @return mutable matrix
     */
    protected MutableMatrix createMatrix(final int previousLength, final int length, final int bandwidth) {
        final int memoryBanded = Math.min(((bandwidth-1)/2 + 1), length) * length;
        final int memorySymmetric = (length*(length+1))/2;
        final boolean banded = bandwidth > 0 && memoryBanded < memorySymmetric;
        setCopyOnMatrixEnlargement(banded);
        return banded
                ? new SymmetricBandMatrix(length, bandwidth, false, true)
                : new SymmetricMatrix(previousLength, length, false, true);
    }

    private void deriveOutputFormat(final AudioFormat inputFormat) {
        final int frameDiff = secondFrame - firstFrame;
        final float sampleRate = inputFormat.getSampleRate() / frameDiff;
        audioFormat = new AudioFormat(
                inputFormat.getEncoding(), sampleRate, inputFormat.getSampleSizeInBits(),
                inputFormat.getChannels(), inputFormat.getFrameSize(), sampleRate,
                inputFormat.isBigEndian());
    }


    @Override
    public AudioMatrix getOutput() throws IOException {
        return matrix;
    }

    @Override
    public Object getId() {
        return id == null ? toString() : id;
    }

    @Override
    public <O2> SignalProcessor<AudioMatrix, O2> disconnectFrom(final SignalProcessor<AudioMatrix, O2> signalProcessor) {
        return signalProcessorSupport.disconnectFrom(signalProcessor);
    }

    @Override
    public SignalProcessor<AudioMatrix, ?>[] getConnectedProcessors() {
        return signalProcessorSupport.getConnectedProcessors();
    }

    @Override
    public <O2> SignalProcessor<AudioMatrix, O2> connectTo(final SignalProcessor<AudioMatrix, O2> signalProcessor) {
        return signalProcessorSupport.connectTo(signalProcessor);
    }

}
