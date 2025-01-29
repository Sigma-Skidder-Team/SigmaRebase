package com.mentalfrostbyte.jello.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.text.StringTextComponent;

public class MinecraftUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void addChatMessage(String text) {
        StringTextComponent textComp = new StringTextComponent(text);
        mc.ingameGUI.getChatGUI().printChatMessage(textComp);
    }

}
