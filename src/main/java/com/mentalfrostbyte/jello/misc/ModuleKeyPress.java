package com.mentalfrostbyte.jello.misc;

import java.lang.reflect.InvocationTargetException;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.gui.unmapped.Bound;
import com.mentalfrostbyte.jello.managers.GuiManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import team.sdhq.eventBus.EventBus;

public class ModuleKeyPress {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void press(int key) {
        if (Client.getInstance().clientMode != ClientMode.NOADDONS) {
            if (key != -1) {
                for (Bound bindType : Client.getInstance().moduleManager.getMacOSTouchBar().getBindedObjects(key)) {
                    if (bindType != null && bindType.hasTarget()) {
                        switch (bindType.getKeybindTypes()) {
                            case MODULE -> bindType.getModuleTarget().toggle();
                            case SCREEN -> {
                                try {
                                    Screen sigmaScreen = bindType.getScreenTarget()
                                            .getDeclaredConstructor(ITextComponent.class)
                                            .newInstance(new StringTextComponent(
                                                    GuiManager.screenToScreenName.get(bindType.getScreenTarget())));
                                    if (Client.getInstance().guiManager.hasReplacement(sigmaScreen)) {
                                        mc.displayGuiScreen(sigmaScreen);
                                    }
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                        | NoSuchMethodException | SecurityException | InstantiationException exc) {
                                    exc.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void listen(int button) {
        EventMouseHover eventMouseHover = new EventMouseHover(button);
        EventBus.call(eventMouseHover);
    }
}
