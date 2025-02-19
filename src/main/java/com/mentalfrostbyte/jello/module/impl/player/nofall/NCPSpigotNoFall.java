package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class NCPSpigotNoFall extends Module {
    private boolean falling;

    public NCPSpigotNoFall() {
        super(ModuleCategory.PLAYER, "NCPSpigot", "NoFall for NCP (Spigot)");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.falling = false;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;
        if (event.isPre()) {
            if (mc.player.fallDistance > 3.0F) {
                this.falling = true;
            }

            if (this.falling && Client.getInstance().playerTracker.getgroundTicks() == 0 && mc.player.isOnGround()) {
                event.setY(event.getY() - 11.0);
                this.falling = false;
            }
        }
    }

}
