/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.SignalSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * <p>Encapsulates an {@link AudioInputStream} to serve as a {@link SignalSource} for
 * a {@link com.tagtraum.jipes.SignalPump} or a {@link com.tagtraum.jipes.SignalPullProcessor}.
 * </p>
 * <p>
 * The provided stream/file is converted to <code>float[]</code> based {@link RealAudioBuffer}s.
 * This also implies a normalization to values between -1 and 1 (signed) or 0 and 1 (unsigned).
 * Normalization can be turned off via {@link #setNormalize(boolean)}.
 * </p>
 * <p>
 * If the signal contains multiple channels, these channel signals are still in their original
 * (potentially interleaved) layout.
 * </p>
 * <p>
 * As of Java 6, mp3 is unfortunately not supported out of the box by Java. You might need
 * to install the free codec <a href="http://www.javazoom.net/javalayer/javalayer.html">JLayer</a>.
 * Platform dependent alternatives are <a href="http://www.tagtraum.com/casampledsp/">CASampledSP</a>
 * and <a href="http://www.tagtraum.com/mfsampledsp/">MFSampledSP</a>/
 * </p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see javax.sound.sampled.AudioSystem#getAudioInputStream(java.io.File)
 * @see <a href="http://www.javazoom.net/javalayer/javalayer.html">JLayer</a>
 * @see <a href="http://www.tagtraum.com/casampledsp/">CASampledSP</a>
 * @see <a href="http://www.tagtraum.com/mfsampledsp/">MFSampledSP</a>
 */
public class AudioSignalSource implements SignalSource<AudioBuffer>, Closeable {

    /**
     * A constant holding the minimum value a <code>signed24bit</code> can
     * have, -2<sup>22</sup>.
     */
    private static final int MIN_VALUE_24BIT = -2 << 22;

    /**
     * A constant holding the maximum value a <code>signed24bit</code> can
     * have, 2<sup>22</sup>-1.
     */
    private static final int MAX_VALUE_24BIT = -MIN_VALUE_24BIT-1;

    private static final int BITS_PER_BYTE = 8;
    private static final int FLOAT_SAMPLE_SIZE_IN_BITS = 32;
    private AudioInputStream in;
    private final byte[] buf;
    private RealAudioBuffer realAudioBuffer;
    private float[] block;
    private long readBytes;
    private boolean closed;
    private boolean normalize = true;

    /**
     * Creates a normalized (see {@link #isNormalize()}) {@link SignalSource} from the given stream.
     *
     * @param in audio input stream with an associated AudioFormat that has at least <code>sampleSizeInBits</code>, <code>channels</code>, and <code>frameSize</code> set
     * @param bufferSize buffer this must be a multiple of the audio format's framesize
     * @throws IllegalArgumentException if the audio format is not specific enough
     */
    public AudioSignalSource(final AudioInputStream in, final int bufferSize) throws IllegalArgumentException {
        this.in = in;
        this.buf = new byte[bufferSize];
        if (in.getFormat().getSampleSizeInBits() <= 0) {
            throw new IllegalArgumentException("AudioFormat is not specific enough. SampleSizeInBits must be greater than 0, but is " + in.getFormat().getSampleSizeInBits());
        }
        if (in.getFormat().getChannels() <= 0) {
            throw new IllegalArgumentException("AudioFormat is not specific enough. Channels must be greater than 0, but is " + in.getFormat().getChannels());
        }
        if (in.getFormat().getFrameSize() <= 0) {
            throw new IllegalArgumentException("AudioFormat is not specific enough. FrameSize must be greater than 0, but is " + in.getFormat().getFrameSize());
        }
    }

    /**
     * Creates a normalized (see {@link #isNormalize()}) {@link SignalSource} from the given stream,
     * using a 16kb internal buffer.
     *
     * @param in audio input stream
     * @throws IllegalArgumentException if the audio format is not specific enough
     */
    public AudioSignalSource(final AudioInputStream in) throws IllegalArgumentException  {
        this(in, 16 * 1024);
    }

    /**
     * Creates a normalized (see {@link #isNormalize()}) {@link SignalSource} from the given file,
     * using a 16kb internal buffer.
     *
     * @param file audio file
     * @throws UnsupportedAudioFileException if the file is not supported by {@link AudioSystem}.
     * @throws IOException if something goes wrong while opening the audio file
     * @throws IllegalArgumentException if the audio format extracted from the file is not specific enough
     */
    public AudioSignalSource(final File file) throws UnsupportedAudioFileException, IOException, IllegalArgumentException  {
        this(openStream(file));
    }

    /**
     * Convert signal to signed PCM for further processing.
     *
     * @param file audio file
     * @return PCM audio inputstream
     * @throws UnsupportedAudioFileException if the file is not supported by {@link AudioSystem}.
     * @throws IOException if something goes wrong while opening the audio file
     */
    private static AudioInputStream openStream(final File file) throws UnsupportedAudioFileException, IOException {
        final AudioInputStream origAudioInputStream = AudioSystem.getAudioInputStream(file);
        final AudioFormat origFormat = origAudioInputStream.getFormat();
        return AudioSystem.getAudioInputStream(
                new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        origFormat.getSampleRate(),
                        origFormat.getSampleSizeInBits() <= 0 ? 16 : origFormat.getSampleSizeInBits(),
                        origFormat.getChannels(),
                        origFormat.getFrameSize() <= 0 ? origFormat.getChannels() * 2 : origFormat.getFrameSize(),
                        origFormat.getSampleRate(),
                        false),
                origAudioInputStream
        );
    }

    /**
     * Indicates whether all values are normalized to -1 t0 1 (signed) or 0 to 1 (unsigned).
     *
     * @return true if this source is normalizing all values
     */
    public boolean isNormalize() {
        return normalize;
    }

    /**
     * Turns normalization to values between -1 and 1 (signed) or 0 and 1 (unsigned) on or off.
     *
     * @param normalize true or off
     */
    public void setNormalize(final boolean normalize) {
        this.normalize = normalize;
    }

    private int getCurrentFrameNumber() {
        return (int) (readBytes / in.getFormat().getFrameSize());
    }

    /**
     * Since this source is stream-based it cannot be properly reset.
     */
    public void reset() {
        readBytes = 0;
    }

    public AudioBuffer read() throws IOException {
        if (closed) return null;
        final int numberOfBytesRead = in.read(buf);
        if (numberOfBytesRead <= 0) {
            close();
            return null;
        }
        return toFloatBuffer(buf, numberOfBytesRead, in.getFormat());
    }

    /**
     * Convert the multibyte-multichannel buffer to a singlefloat-multichannel buffer.
     *
     * @param byteBuf     multibyte-multichannel byte array
     * @param length      number of bytes to read from {@code byteBuf}
     * @param audioFormat audioFormat
     * @return singlefloat-multichannel buffer
     * @throws java.io.IOException if the buffer cannot be computed
     */
    private AudioBuffer toFloatBuffer(final byte[] byteBuf, final int length, final AudioFormat audioFormat) throws IOException {
        final int bytesPerChannel = audioFormat.getSampleSizeInBits() / BITS_PER_BYTE;
        final boolean signed = AudioFormat.Encoding.PCM_SIGNED.equals(audioFormat.getEncoding());
        final float normalizationFactor = normalize ? normalizationFactor(bytesPerChannel, signed) : 1f;
        final int samplesToRead = length / bytesPerChannel;

        if (block == null || block.length != samplesToRead) {
            block = new float[samplesToRead];
        }

        // fast lane for the most common case: signed, 16 bit per channel
        if (signed && bytesPerChannel == 2) {
            if (audioFormat.isBigEndian()) bytesToSignedShortsBigEndian(byteBuf, samplesToRead, normalizationFactor, this.block);
            else bytesToSignedShortsLittleEndian(byteBuf, samplesToRead, normalizationFactor, this.block);
        } else {
            // convert to samples
            final int[] samples = audioFormat.isBigEndian()
                ? bytesToIntsBigEndian(byteBuf, samplesToRead, bytesPerChannel)
                : bytesToIntsLittleEndian(byteBuf, samplesToRead, bytesPerChannel);

            // cast and normalize
            for (int sampleNumber = 0; sampleNumber < samplesToRead; sampleNumber++) {
                final int sample = samples[sampleNumber];
                if (signed) {
                    switch (bytesPerChannel) {
                        case 1:
                            final byte byteSample = (byte) sample;
                            block[sampleNumber] = byteSample / normalizationFactor;
                            break;
//                        we handle 2 bytes extra for speed
//                        case 2:
//                            final short shortSample = (short) sample;
//                            block[sampleNumber] = shortSample / normalizationFactor;
//                            break;
                        case 3:
                            final int threeByteSample = sample > MAX_VALUE_24BIT ? sample + MIN_VALUE_24BIT + MIN_VALUE_24BIT : sample;
                            block[sampleNumber] = threeByteSample / normalizationFactor;
                            break;
                        case 4:
                            block[sampleNumber] = sample / normalizationFactor;
                            break;
                        default:
                            throw new IOException(bytesPerChannel + " bytes per channel not supported.");
                            //block[sampleNumber] = sample / (float) (2 << (bytesPerChannel*8-2));
                    }
                } else {
                    block[sampleNumber] = sample / normalizationFactor;
                }
            }
        }
        if (realAudioBuffer == null) {
            realAudioBuffer = new RealAudioBuffer(getCurrentFrameNumber(), block, toProcessedAudioFormat(audioFormat));
        } else {
            realAudioBuffer.reuse(getCurrentFrameNumber(), block, realAudioBuffer.getAudioFormat());
        }
        this.readBytes += length;
        return realAudioBuffer;
    }

    private static int[] bytesToIntsLittleEndian(final byte[] buf, final int samplesToRead, final int bytesPerSample) {
        final int[] samples = new int[samplesToRead];
        for (int sampleNumber = 0; sampleNumber < samplesToRead; sampleNumber++) {
            final int sampleOffset = sampleNumber * bytesPerSample;
            int sample = 0;
            for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
                final int aByte = buf[sampleOffset + byteIndex] & 0xff;
                sample += aByte << 8 * (byteIndex);
            }
            samples[sampleNumber] = sample;
        }
        return samples;
    }

    private static int[] bytesToIntsBigEndian(final byte[] buf, final int samplesToRead, final int bytesPerSample) {
        final int[] samples = new int[samplesToRead];
        for (int sampleNumber = 0; sampleNumber < samplesToRead; sampleNumber++) {
            final int sampleOffset = sampleNumber * bytesPerSample;
            int sample = 0;
            for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
                final int aByte = buf[sampleOffset + byteIndex] & 0xff;
                sample += aByte << (8 * (bytesPerSample - byteIndex - 1));
            }
            samples[sampleNumber] = sample;
        }
        return samples;
    }

    private static void bytesToSignedShortsLittleEndian(final byte[] inputBuf, final int samplesToRead, final float normalizationFactor, final float[] outputBuf) {
        for (int sampleNumber = 0; sampleNumber < samplesToRead; sampleNumber++) {
            final int sampleOffset = sampleNumber * 2;
            final short sample = (short) ((inputBuf[sampleOffset] & 0xff) + ((inputBuf[sampleOffset+1] & 0xff) << 8));
            outputBuf[sampleNumber] = sample / normalizationFactor;
        }
    }

    private static void bytesToSignedShortsBigEndian(final byte[] inputBuf, final int samplesToRead, final float normalizationFactor, final float[] outputBuf) {
        for (int sampleNumber = 0; sampleNumber < samplesToRead; sampleNumber++) {
            final int sampleOffset = sampleNumber * 2;
            final short sample = (short) (((inputBuf[sampleOffset] & 0xff) << 8) + (inputBuf[sampleOffset+1] & 0xff));
            outputBuf[sampleNumber] = sample / normalizationFactor;
        }
    }

    /**
     * Normalization factor appropriate for the given sample size and sign.
     *
     * @param bytesPerChannel bytes per sample
     * @param signed sign
     * @return normalization factor
     * @throws IOException if the parameter combination is not supported
     */
    private static float normalizationFactor(final int bytesPerChannel, final boolean signed) throws IOException {
        final float normalizationFactor;
        if (signed) {
            switch (bytesPerChannel) {
                case 1:
                    normalizationFactor = Byte.MAX_VALUE + 1.0f;
                    break;
                case 2:
                    normalizationFactor = Short.MAX_VALUE + 1.0f;
                    break;
                case 3:
                    normalizationFactor = MAX_VALUE_24BIT + 1.0f;
                    break;
                case 4:
                    normalizationFactor = Integer.MAX_VALUE + 1.0f;
                    break;
                default:
                    throw new IOException(bytesPerChannel + " bytes per channel not supported.");
                    //block[sampleNumber] = sample / (float) (2 << (bytesPerChannel*8-2));
            }
        } else {
            normalizationFactor = (2L<<(bytesPerChannel*8-1))-1.0f;
        }
        return normalizationFactor;
    }

    private AudioFormat toProcessedAudioFormat(final AudioFormat sourceAudioFormat) {
        return new AudioFormat(sourceAudioFormat.getSampleRate(), FLOAT_SAMPLE_SIZE_IN_BITS, sourceAudioFormat.getChannels(),
                AudioFormat.Encoding.PCM_SIGNED.equals(sourceAudioFormat.getEncoding()),
                sourceAudioFormat.isBigEndian());
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.finalize();
    }

    /**
     * Close this signal source and its underlying {@link AudioInputStream}.
     */
    public void close() throws IOException {
        if (in != null && !closed) {
            in.close();
            closed = true;
        }
        in = null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioSignalSource source = (AudioSignalSource) o;
        if (in != null ? !in.equals(source.in) : source.in != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return in != null ? in.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AudioSignalSource{" +
                "audioInputStream=" + in +
                '}';
    }
}
