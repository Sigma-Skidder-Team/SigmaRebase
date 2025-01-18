package com.mentalfrostbyte.jello.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class MinecraftUtil {

    private static final Minecraft mc = MinecraftClient.getInstance();

    public static void addChatMessage(String text) {
        LiteralText textComp = new LiteralText(text);
        mc.ingameGUI.getChatGUI().printChatMessage(textComp);
    }

}
