package com.mentalfrostbyte.jello.gui.base;

import totalcross.json.JSONObject;
import net.minecraft.client.MinecraftClient;

public abstract class Screen
        extends CustomGuiScreen {

    public Screen(String var1) {
        super(null, var1, 0, 0, MinecraftClient.getInstance().getMainWindow().getWidth(),
                MinecraftClient.getInstance().getMainWindow().getHeight());
    }

    public int getFPS() {
        return 30;
    }

    @Override
    public void loadConfig(JSONObject config) {
        super.loadConfig(config);
        this.setWidthA(MinecraftClient.getInstance().getMainWindow().getWidth());
        this.setHeightA(MinecraftClient.getInstance().getMainWindow().getHeight());
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == MinecraftClient.getInstance().gameSettings.keyBindFullscreen.keyCode.getKeyCode()) {
            MinecraftClient.getInstance().getMainWindow().toggleFullscreen();
            MinecraftClient.getInstance().gameSettings.fullscreen = MinecraftClient.getInstance().getMainWindow()
                    .isFullscreen();
        }

        super.keyPressed(keyCode);
    }
}
