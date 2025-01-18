package com.mentalfrostbyte.jello.gui.base;

import totalcross.json.JSONObject;
import net.minecraft.client.MinecraftClient;

public abstract class Screen
        extends CustomGuiScreen {

    public Screen(String var1) {
        super(null, var1, 0, 0, MinecraftClient.getInstance().getWindow().getWidth(),
                MinecraftClient.getInstance().getWindow().getHeight());
    }

    public int method13313() {
        return 30;
    }

    @Override
    public void loadConfig(JSONObject config) {
        super.loadConfig(config);
        this.setWidthA(MinecraftClient.getInstance().getWindow().getWidth());
        this.setHeightA(MinecraftClient.getInstance().getWindow().getHeight());
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == MinecraftClient.getInstance().gameSettings.keyBindFullscreen.keyCode.getKeyCode()) {
            MinecraftClient.getInstance().getWindow().toggleFullscreen();
            MinecraftClient.getInstance().gameSettings.fullscreen = MinecraftClient.getInstance().getWindow()
                    .isFullscreen();
        }

        super.keyPressed(keyCode);
    }
}
