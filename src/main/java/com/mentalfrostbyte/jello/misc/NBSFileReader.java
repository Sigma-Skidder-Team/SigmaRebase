package com.mentalfrostbyte.jello.misc;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NBSFileReader {
    /**
     * Reads an NBS file from the specified input stream or file.
     *
     * @param file         The file to read the NBS file from, if inputStream is null.
     * @return             An instance of {@link NBSFile} representing the read NBS file, or null if an error occurs.
     */
    public static NBSFile fromFile(File file) {
        try {
            return read(new FileInputStream(file), file);
        } catch (FileNotFoundException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    /**
     * Reads an NBS file from the specified input stream.
     *
     * @param inputStream  The input stream to read the NBS file from.
     * @return              An instance of {@link NBSFile} representing the read NBS file, or null if an error occurs.
     */
    public static NBSFile fromInputStream(InputStream inputStream) {
        return read(inputStream, null);
    }

    /**
     * Reads an NBS file from the specified input stream or file, and returns an instance of {@link NBSFile} representing the read NBS file.
     *
     * @param inputStream  The input stream to read the NBS file from.
     * @param file         The file to read the NBS file from, if inputStream is null.
     * @return              An instance of {@link NBSFile} representing the read NBS file, or null if an error occurs.
     */
    private static NBSFile read(InputStream inputStream, File file) {
        HashMap var4 = new HashMap();

        try {
            DataInputStream stream = new DataInputStream(inputStream);
            short var25 = readSigned16(stream);
            int var8 = 10;
            byte var9 = 0;
            if (var25 == 0) {
                var9 = stream.readByte();
                var8 = stream.readByte();
                if (var9 >= 3) {
                    var25 = readSigned16(stream);
                }
            }

            int var10 = Class9705.maxId() - var8;
            short var11 = readSigned16(stream);
            String author = readString(stream);
            String name = readString(stream);
            readString(stream);
            String var14 = readString(stream);
            float var15 = (float) readSigned16(stream) / 100.0F;
            stream.readBoolean();
            stream.readByte();
            stream.readByte();
            readUnsignedI(stream);
            readUnsignedI(stream);
            readUnsignedI(stream);
            readUnsignedI(stream);
            readUnsignedI(stream);
            readString(stream);
            short var16 = -1;

            while (true) {
                short var17 = readSigned16(stream);
                if (var17 == 0) {
                    if (var9 > 0 && var9 < 3) {
                        var25 = var16;
                    }

                    for (int var26 = 0; var26 < var11; var26++) {
                        Class9616 var28 = (Class9616)var4.get(var26);
                        String var30 = readString(stream);
                        byte var33 = stream.readByte();
                        if (var9 >= 2) {
                            stream.readByte();
                        }

                        if (var28 != null) {
                            var28.method37432(var30);
                            var28.method37436(var33);
                        }
                    }

                    byte var27 = stream.readByte();
                    Class8084[] var29 = new Class8084[var27];

                    for (int var31 = 0; var31 < var27; var31++) {
                        var29[var31] = new Class8084((byte)var31, readString(stream), readString(stream));
                        stream.readByte();
                        stream.readByte();
                    }

                    if (var10 < 0) {
                        ArrayList<Class8084> var32 = Class7179.method22534(var8);
                        var32.addAll(Arrays.asList(var29));
                        var29 = var32.toArray(var29);
                    } else {
                        var8 += var10;
                    }

                    NBSFile result = new NBSFile(var15, var4, var11, var25, author, name, var14, file, var8, var29);;

                    return result;
                }

                var16 += var17;
                short var18 = -1;

                while (true) {
                    short var19 = readSigned16(stream);
                    if (var19 == 0) {
                        break;
                    }

                    var18 += var19;
                    byte var20 = stream.readByte();
                    if (var10 > 0 && var20 >= var8) {
                        var20 = (byte)(var20 + var10);
                    }

                    method29872(var18, var16, var20, stream.readByte(), var4);
                }
            }
        } catch (FileNotFoundException var21) {
            var21.printStackTrace();
        } catch (EOFException var22) {
            String var7 = "";
            if (file != null) {
                var7 = file.getName();
            }
        } catch (IOException var23) {
            var23.printStackTrace();
        }

        return null;
    }

    private static void method29872(int var0, int var1, byte var2, byte var3, HashMap<Integer, Class9616> var4) {
        Class9616 var7 = (Class9616)var4.get(var0);
        if (var7 == null) {
            var7 = new Class9616();
            var4.put(var0, var7);
        }

        var7.method37434(var1, new Class8255(var2, var3));
    }
    /**
     * Reads two bytes from the input stream and returns them as a 16-bit signed integer.
     *
     * @param stream The input stream to read the bytes from.
     * @return A 16-bit signed integer formed by combining the two bytes read from the input stream.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    private static short readSigned16(DataInputStream stream) throws IOException {
        int a = stream.readUnsignedByte();
        int b = stream.readUnsignedByte();
        return (short)(a + (b << 8));
    }
    /**
     * Reads four bytes from the input stream and returns them as a 32-bit unsigned integer.
     *
     * @param stream The input stream to read the bytes from.
     * @return A 32-bit unsigned integer formed by combining the four bytes read from the input stream.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    private static int readUnsignedI(DataInputStream stream) throws IOException {
        int var3 = stream.readUnsignedByte();
        int var4 = stream.readUnsignedByte();
        int var5 = stream.readUnsignedByte();
        int var6 = stream.readUnsignedByte();
        return var3 + (var4 << 8) + (var5 << 16) + (var6 << 24);
    }

    /**
     * Reads a string from the given DataInputStream.
     * <p>
     * This method first reads an unsigned integer to determine the length of the string,
     * then reads that many bytes from the stream, converting them to characters.
     * Carriage return characters ('\r') are replaced with spaces.
     *
     * @param stream The DataInputStream from which to read the string.
     * @return The string read from the stream.
     * @throws IOException If an I/O error occurs while reading from the stream.
     */
    private static String readString(DataInputStream stream) throws IOException {
        int var3 = readUnsignedI(stream);

        StringBuilder var4;
        for (var4 = new StringBuilder(var3); var3 > 0; var3--) {
            char var5 = (char)stream.readByte();
            if (var5 == '\r') {
                var5 = ' ';
            }

            var4.append(var5);
        }

        return var4.toString();
    }
}