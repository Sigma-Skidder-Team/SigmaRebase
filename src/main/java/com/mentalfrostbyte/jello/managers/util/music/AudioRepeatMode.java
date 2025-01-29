package com.mentalfrostbyte.jello.managers.util.music;

public enum AudioRepeatMode {
    NO_REPEAT(0),
    REPEAT(1),
    LOOP_CURRENT(2);

    public final int type;

    AudioRepeatMode(int type) {
        this.type = type;
    }

    public AudioRepeatMode getNext() {
        for (AudioRepeatMode mode : values()) {
            if (mode.type == this.type + 1) {
                return mode;
            }
        }

        return NO_REPEAT;
    }

    public static AudioRepeatMode parseRepeat(int type) {
        for (AudioRepeatMode mode : values()) {
            if (mode.type == type) {
                return mode;
            }
        }

        return REPEAT;
    }
}
