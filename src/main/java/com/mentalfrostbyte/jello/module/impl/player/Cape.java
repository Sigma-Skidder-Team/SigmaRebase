package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import team.sdhq.eventBus.annotations.EventTarget;

public class Cape extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Cape() {
        super(ModuleCategory.PLAYER, "Cape", "gives you a cape (wow)");
    }

    public Identifier getCape() {
        return new Identifier("minecraft", "com/mentalfrostbyte/gui/resources/jello/capes/2016.png");
    }

    public void onDisable() {
        if (mc.player != null) {
            mc.player.setLocationOfCape(null);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player != null) {
            mc.player.setLocationOfCape(getCape());
        }
    }
}