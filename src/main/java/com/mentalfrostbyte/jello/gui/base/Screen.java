package com.mentalfrostbyte.jello.gui.base;

import totalcross.json.JSONObject;
import net.minecraft.client.Minecraft;

public abstract class Screen
        extends CustomGuiScreen {

    public Screen(String var1) {
        super(null, var1, 0, 0, Minecraft.getInstance().getMainWindow().getWidth(), Minecraft.getInstance().getMainWindow().getHeight());
    }

    public int getFPS() {
        return 30;
    }

    @Override
    public void loadConfig(JSONObject config) {
        super.loadConfig(config);
        this.setWidthA(Minecraft.getInstance().getMainWindow().getWidth());
        this.setHeightA(Minecraft.getInstance().getMainWindow().getHeight());
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == Minecraft.getInstance().gameSettings.keyBindFullscreen.keyCode.getKeyCode()) {
            Minecraft.getInstance().getMainWindow().toggleFullscreen();
            Minecraft.getInstance().gameSettings.fullscreen = Minecraft.getInstance().getMainWindow().isFullscreen();
        }

        super.keyPressed(keyCode);
    }
}
