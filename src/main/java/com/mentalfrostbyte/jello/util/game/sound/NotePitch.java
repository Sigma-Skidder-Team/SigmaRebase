package com.mentalfrostbyte.jello.util.game.sound;

public enum NotePitch {
    NOTE_0(0, 0.4920635F), // Math.pow(2.0, (note - 12) / 12.0), see NoteBlock#eventReceived
    NOTE_1(1, 0.52380955F),
    NOIE_2(2, 0.5555556F),
    NOTE_3(3, 0.5873016F),
    NOTE_4(4, 0.61904764F),
    NOTE_5(5, 0.6666667F),
    NOTE_6(6, 0.6984127F),
    NOTE_7(7, 0.74603176F),
    NOTE_8(8, 0.7936508F),
    NOTE_9(9, 0.82539684F),
    NOTE_10(10, 0.8888889F),
    NOTE_11(11, 0.93650794F),
    NOTE_12(12, 1.0F),
    NOTE_13(13, 1.0476191F),
    NOTE_14(14, 1.1111112F),
    NOTE_15(15, 1.1746032F),
    NOTE_16(16, 1.2539682F),
    NOTE_17(17, 1.3333334F),
    NOTE_18(18, 1.4126984F),
    NOTE_19(19, 1.4920635F),
    NOTE_20(20, 1.5873016F),
    NOTE_21(21, 1.6666666F),
    NOTE_22(22, 1.7777778F),
    NOTE_23(23, 1.8730159F),
    NOTE_24(24, 2.0F);

    public int note;
    public float pitch;

    NotePitch(int var3, float var4) {
        this.note = var3;
        this.pitch = var4;
    }

    public static float getNotePitch(int var0) {
        for (NotePitch var6 : values()) {
            if (var6.note == var0) {
                return var6.pitch;
            }
        }

        return 0.0F;
    }

    public static float getClosestNote(float pitch) {
        NotePitch[] var3 = values();
        int var4 = var3.length;
        int var5 = 0;
        int var6 = 0;

        for (float var7 = Math.abs(var3[0].pitch - pitch); var5 < var4; var5++) {
            NotePitch var8 = var3[var5];
            float var9 = Math.abs(var8.pitch - pitch);
            if (var9 < var7) {
                var6 = var5;
                var7 = var9;
            }
        }

        return (float)var3[var6].note;
    }
}
