package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.module.Module;
import net.minecraft.client.gui.screen.Screen;

public class Class6984 {
    public Module moduel;
    public Class<? extends Screen> screen;

    public Class6984(Module var1) {
        this.moduel = var1;
    }

    public Class6984(Class<? extends Screen> var1) {
        this.screen = var1;
    }

    public String method21596() {
        return this.moduel == null ? GuiManager.screenToScreenName.get(this.screen) : this.moduel.getName();
    }

    public String method21597() {
        return this.moduel == null ? "Screen" : this.moduel.getCategoryBasedOnMode().name();
    }

    public void method21598(int var1) {
        if (this.moduel == null) {
            Client.getInstance().moduleManager.getKeyManager().method13726(var1, this.screen);
        } else {
            Client.getInstance().moduleManager.getKeyManager().method13725(var1, this.moduel);
        }
    }

    public int method21599() {
        return this.moduel == null
                ? Client.getInstance().moduleManager.getKeyManager().getKeybindFor(this.screen)
                : Client.getInstance().moduleManager.getKeyManager().method13729(this.moduel);
    }
}
