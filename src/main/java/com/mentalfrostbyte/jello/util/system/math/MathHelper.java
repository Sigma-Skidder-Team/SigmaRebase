package com.mentalfrostbyte.jello.util.system.math;

import javax.sound.sampled.AudioFormat;

public class MathHelper {
    public static float calculateBackwardTransition(float var0, float var1, float var2, float var3) {
        var0 /= var3;
        return var2 * var0 * var0 * var0 + var1;
    }

    public static float calculateTransition(float var0, float var1, float var2, float var3) {
        var0 /= var3;
        return var2 * (var0 * var0 * --var0 + 1.0F) + var1;
    }

    public static double getRandomValue() {
        return Math.random() * 1.0E-8;
    }

    public static float method27665(float var0, float var1, float var2, float var3) {
        var0 /= var3 / 2.0F;
        if (!(var0 < 1.0F)) {
            var0 -= 2.0F;
            return var2 / 2.0F * (var0 * var0 * var0 + 2.0F) + var1;
        } else {
            return var2 / 2.0F * var0 * var0 * var0 + var1;
        }
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

    public static float wrapAngleTo180_float(float var0) {
        float var3 = var0 % 360.0F;
        if (var3 >= 180.0F) {
            var3 -= 360.0F;
        }

        if (var3 < -180.0F) {
            var3 += 360.0F;
        }

        return var3;
    }
}
