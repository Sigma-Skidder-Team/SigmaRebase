package com.mentalfrostbyte.jello.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class MinecraftUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void addChatMessage(String text) {
        StringTextComponent textComp = new StringTextComponent(text);
        mc.ingameGUI.getChatGUI().printChatMessage(textComp);
    }

}
