package com.mentalfrostbyte.jello.managers.impl.music;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.MusicManager;

public class Class8808 implements AudioProcessor {
    public final MusicManager musicManager;

    public Class8808(MusicManager var1) {
        this.musicManager = var1;
    }

    @Override
    public void processBuffer(byte[] var1, int var2, int var3) {
        Client.getInstance().getLogger().dummyMethod("DONE");
    }
}
