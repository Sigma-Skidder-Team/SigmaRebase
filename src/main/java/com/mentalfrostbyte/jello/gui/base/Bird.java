package com.mentalfrostbyte.jello.gui.base;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class Bird extends Screen {

    public Bird(Text title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
