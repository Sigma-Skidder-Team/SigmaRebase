package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.module.Module;
import net.minecraft.client.gui.screen.Screen;

public class Class6984 {
    public Module field30212;
    public Class<? extends Screen> field30213;

    public Class6984(Module var1) {
        this.field30212 = var1;
    }

    public Class6984(Class<? extends Screen> var1) {
        this.field30213 = var1;
    }

    public String method21596() {
        return this.field30212 == null ? GuiManager.screenToScreenName.get(this.field30213) : this.field30212.getName();
    }

    public String method21597() {
        return this.field30212 == null ? "Screen" : this.field30212.getCategoryBasedOnMode().name();
    }

    public void method21598(int var1) {
        if (this.field30212 == null) {
            Client.getInstance().moduleManager.getKeyManager().method13726(var1, this.field30213);
        } else {
            Client.getInstance().moduleManager.getKeyManager().method13725(var1, this.field30212);
        }
    }

    public int method21599() {
        return this.field30212 == null
                ? Client.getInstance().moduleManager.getKeyManager().getKeybindFor(this.field30213)
                : Client.getInstance().moduleManager.getKeyManager().method13729(this.field30212);
    }
}
