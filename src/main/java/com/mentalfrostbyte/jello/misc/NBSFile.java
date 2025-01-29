package com.mentalfrostbyte.jello.misc;

import java.io.File;
import java.util.HashMap;

public class NBSFile implements Cloneable {
    private HashMap<Integer, Class9616> map = new HashMap<>();
    private final short short1;
    private final short short2;
    private final String songName;
    private final File file;
    private final String songAuthor;
    private final String string3;
    private final float originalTempo;
    private float tempo;
    private final Class8084[] field16347;
    private final int field16348;

    public NBSFile(NBSFile file) {
        this(
                file.method9957(),
                file.method9950(),
                file.getShort1(),
                file.getShort2(),
                file.getSongName(),
                file.getSongAuthor(),
                file.getString3(),
                file.getFile(),
                file.method9960(),
                file.method9959()
        );
    }

    /** @deprecated */
    public NBSFile(float var1, HashMap<Integer, Class9616> var2, short var3, short var4, String var5, String var6, String var7, File var8) {
        this(var1, var2, var3, var4, var5, var6, var7, var8, Class9705.maxId(), new Class8084[0]);
    }

    /** @deprecated */
    public NBSFile(float var1, HashMap<Integer, Class9616> var2, short var3, short var4, String var5, String var6, String var7, File var8, Class8084[] var9) {
        this(var1, var2, var3, var4, var5, var6, var7, var8, Class9705.maxId(), var9);
    }

    public NBSFile(float var1, HashMap<Integer, Class9616> var2, short var3, short var4, String var5, String var6, String var7, File var8, int var9) {
        this(var1, var2, var3, var4, var5, var6, var7, var8, var9, new Class8084[0]);
    }

    public NBSFile(
            float tempo, HashMap<Integer, Class9616> map, short var3, short var4, String songName, String songAuthor, String string3, File file, int var9, Class8084[] var10
    ) {
        this.originalTempo = tempo;
        this.setTempo(20.0F / tempo);
        this.map = map;
        this.short1 = var3;
        this.short2 = var4;
        this.songName = songName;
        this.songAuthor = songAuthor;
        this.string3 = string3;
        this.file = file;
        this.field16348 = var9;
        this.field16347 = var10;
    }

    public HashMap<Integer, Class9616> method9950() {
        return this.map;
    }

    public short getShort1() {
        return this.short1;
    }

    public short getShort2() {
        return this.short2;
    }

    public String getSongName() {
        return this.songName;
    }

    public String getSongAuthor() {
        return this.songAuthor;
    }

    public File getFile() {
        return this.file;
    }

    public String getString3() {
        return this.string3;
    }

    public float method9957() {
        return this.originalTempo;
    }

    public float getTempo() {
        return this.tempo;
    }

    public Class8084[] method9959() {
        return this.field16347;
    }

    @Override
    public NBSFile clone() {
        return new NBSFile(this);
    }

    public int method9960() {
        return this.field16348;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }
}
