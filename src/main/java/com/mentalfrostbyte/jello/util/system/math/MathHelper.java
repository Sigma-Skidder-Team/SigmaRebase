package com.mentalfrostbyte.jello.util.system.math;

import javax.sound.sampled.AudioFormat;

public class MathHelper {
    public static float easeInCubic(float elapsedTime, float startValue, float change, float duration) {
        elapsedTime /= duration;
        return change * elapsedTime * elapsedTime * elapsedTime + startValue;
    }

    public static float easeOutCubic(float elapsedTime, float startValue, float change, float duration) {
        elapsedTime /= duration;
        elapsedTime--; // equivalent to --t in classic easing formula
        return change * (elapsedTime * elapsedTime * elapsedTime + 1.0F) + startValue;
    }

    public static double getRandomValue() {
        return Math.random() * 1.0E-8;
    }

    public static float[] convertToPCMFloatArray(byte[] audioBytes, AudioFormat audioFormat) {
        float[] pcmValues = new float[audioBytes.length / audioFormat.getFrameSize()];

        for (int i = 0; i < audioBytes.length; i += audioFormat.getFrameSize()) {
            int sample = !audioFormat.isBigEndian() ? bytesToIntLE(audioBytes, i, audioFormat.getFrameSize()) : bytesToIntBE(audioBytes, i, audioFormat.getFrameSize());
            pcmValues[i / audioFormat.getFrameSize()] = (float) sample / 32768.0F;
        }

        return pcmValues;
    }

    public static double[] calculateAmplitudes(float[] realPart, float[] imaginaryPart) {
        double[] amplitudes = new double[realPart.length / 2];

        for (int i = 0; i < amplitudes.length; i++) {
            // Calculate magnitude using the Pythagorean theorem
            amplitudes[i] = Math.sqrt(realPart[i] * realPart[i] + imaginaryPart[i] * imaginaryPart[i]);
        }

        return amplitudes;
    }

    public static int bytesToIntLE(byte[] byteArray, int startIndex, int length) {
        int result = 0;

        for (int i = 0; i < length; i++) {
            // Extract the byte and shift it to its correct position
            int currentByte = byteArray[startIndex + i] & 0xFF;
            result += currentByte << (8 * i);
        }

        return result;
    }

    public static int bytesToIntBE(byte[] byteArray, int startIndex, int length) {
        int result = 0;

        for (int i = 0; i < length; i++) {
            // Extract the byte and shift it to its correct position
            int currentByte = byteArray[startIndex + i] & 0xFF;
            result += currentByte << (8 * (length - i - 1));
        }

        return result;
    }

    public static double generateRandomSmallValue() {
        return Math.random() * 1.0E-8;
    }

    public static double round(float number,float roundTo) {
        double rounded = Math.round(number / roundTo);

        return rounded * roundTo;
    }
}
