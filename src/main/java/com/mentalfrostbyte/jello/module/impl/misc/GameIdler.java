package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import team.sdhq.eventBus.annotations.EventTarget;

public class GameIdler extends Module {
    public GameIdler() {
        super(ModuleCategory.MISC, "GameIdler", "Lowers your fps when the game is idle to increase performance");
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (this.isEnabled()) {
            boolean focused = GLFW.glfwGetWindowAttrib(mc.getMainWindow().getHandle(), GLFW.GLFW_FOCUSED) == 1;
            if (focused) {
                Minecraft.getInstance().getMainWindow().setFramerateLimit(mc.gameSettings.framerateLimit);
            } else {
                Minecraft.getInstance().getMainWindow().setFramerateLimit(5);
            }
        }
    }

    @Override
    public void onDisable() {
        Minecraft.getInstance().getMainWindow().setFramerateLimit(mc.gameSettings.framerateLimit);
    }
}
