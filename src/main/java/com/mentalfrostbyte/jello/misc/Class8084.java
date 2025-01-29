package com.mentalfrostbyte.jello.misc;


public class Class8084 {
    private final byte alwaysZero;
    private final String name;
    private final String soundFileNoExt;

    public Class8084(byte alwaysZero, String name, String soundFile) {
        this.alwaysZero = alwaysZero;
        this.name = name;
        this.soundFileNoExt = soundFile.replaceAll(".ogg", "");
    }

    public byte getAlwaysZero() {
        return this.alwaysZero;
    }

    public String getName() {
        return this.name;
    }

    public String getSoundFileNoExt() {
        return this.soundFileNoExt;
    }
}
