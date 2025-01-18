package com.mentalfrostbyte.jello.managers.impl.music;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class MusicStream extends InputStream {
    private final AudioProcessor audioProcessor;
    private MusicByteStream byteStream;
    private volatile int bufferEnd;
    private volatile int bufferPosition;
    private volatile boolean endOfStream;
    private Thread streamingThread;

    public MusicStream(InputStream inputStream, AudioProcessor audioProcessor) {
        this.audioProcessor = audioProcessor;
        this.byteStream = new MusicByteStream(this);
        this.bufferEnd = 0;
        this.bufferPosition = 0;
        this.endOfStream = false;
        this.streamingThread = new Thread(new AudioStreamer(this, inputStream));
        this.streamingThread.start();
    }

    public int getBufferEnd() {
        return this.bufferEnd;
    }

    public void setBufferEnd(int bufferEnd) {
        this.bufferEnd = bufferEnd;
    }

    public void incrementBufferEnd(int increment) {
        this.bufferEnd += increment;
    }

    public int getBufferPosition() {
        return this.bufferPosition;
    }

    public void setBufferPosition(int bufferPosition) {
        this.bufferPosition = bufferPosition;
    }

    public boolean isEndOfStream() {
        return this.endOfStream;
    }

    public void setEndOfStream(boolean endOfStream) {
        this.endOfStream = endOfStream;
    }

    public AudioProcessor getAudioProcessor() {
        return this.audioProcessor;
    }

    public MusicByteStream getByteStream() {
        return this.byteStream;
    }

    @Override
    public void close() throws IOException {
        this.byteStream.close();
        this.streamingThread.interrupt();
        this.streamingThread = null;
        this.byteStream = null;
        this.bufferEnd = 0;
        this.bufferPosition = 0;
        this.endOfStream = false;
        super.close();
    }

    @Override
    public int available() throws IOException {
        return this.bufferEnd - this.bufferPosition;
    }

    @Override
    public int read() throws IOException {
        if (this.endOfStream && this.bufferPosition >= this.bufferEnd) {
            return -1;
        } else {
            while (this.bufferEnd <= this.bufferPosition || this.byteStream.getBuffer().length <= this.bufferPosition) {
                if (this.endOfStream) {
                    return -1;
                }
            }
            return this.byteStream.getBuffer()[this.bufferPosition++] & 0xFF;
        }
    }

    @Override
    public int read(byte @NotNull [] data) throws IOException {
        return this.read(data, 0, data.length);
    }

    @Override
    public void reset() throws IOException {
        this.bufferPosition = 0;
    }

    @Override
    public int read(byte @NotNull [] data, int offset, int length) throws IOException {
        while (this.bufferEnd < this.bufferPosition + length) {
            if (this.endOfStream) {
                return -1;
            }
        }

        byte[] buffer = this.byteStream.getBuffer();
        System.arraycopy(buffer, this.bufferPosition, data, offset, length);
        this.bufferPosition += length;
        return length;
    }

    @Override
    public long skip(long bytes) throws IOException {
        this.bufferPosition += (int) bytes;
        return bytes;
    }
}
