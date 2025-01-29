package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class AutoRespawn extends Module {

    public AutoRespawn() {
        super(ModuleCategory.PLAYER, "AutoRespawn", "Respawns for you");
    }

    @EventTarget
    public void TickEvent(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (!mc.player.isAlive()) {
                mc.player.respawnPlayer();
            }
        }
    }
}
