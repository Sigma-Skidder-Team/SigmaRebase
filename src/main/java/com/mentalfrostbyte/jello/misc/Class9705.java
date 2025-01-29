package com.mentalfrostbyte.jello.misc;



public class Class9705 {
    public static String method38022(byte var0) {
        return switch (var0) {
            case 1 -> "BLOCK_NOTE_BLOCK_BASS";
            case 2 -> "BLOCK_NOTE_BLOCK_BASEDRUM";
            case 3 -> "BLOCK_NOTE_BLOCK_SNARE";
            case 4 -> "BLOCK_NOTE_BLOCK_HAT";
            case 5 -> "BLOCK_NOTE_BLOCK_GUITAR";
            case 6 -> "BLOCK_NOTE_BLOCK_FLUTE";
            case 7 -> "BLOCK_NOTE_BLOCK_BELL";
            case 8 -> "BLOCK_NOTE_BLOCK_CHIME";
            case 9 -> "BLOCK_NOTE_BLOCK_XYLOPHONE";
            case 10 -> "BLOCK_NOTE_BLOCK_IRON_XYLOPHONE";
            case 11 -> "BLOCK_NOTE_BLOCK_COW_BELL";
            case 12 -> "BLOCK_NOTE_BLOCK_DIDGERIDOO";
            case 13 -> "BLOCK_NOTE_BLOCK_BIT";
            case 14 -> "BLOCK_NOTE_BLOCK_BANJO";
            case 15 -> "BLOCK_NOTE_BLOCK_PLING";
            default -> "BLOCK_NOTE_BLOCK_HARP";
        };
    }

    public static boolean isValidId(byte n) {
        return n >= maxId();
    }

    public static byte maxId() {
        return 16;
    }
}
