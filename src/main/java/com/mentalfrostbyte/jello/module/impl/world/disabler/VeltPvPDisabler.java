package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class VeltPvPDisabler extends Module {
    private int tickCounter;

    public VeltPvPDisabler() {
        super(ModuleCategory.EXPLOIT, "VeltPvP", "Disabler for VeltPvP.");
    }

    @Override
    public void onEnable() {
        this.tickCounter = 0;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && mc.player != null && event.isPre()) {
            this.tickCounter++;
            double motionY = -0.1;
            if (this.tickCounter >= 20) {
                this.tickCounter = 0;
                event.setY(motionY);
                event.setGround(false);
            }
        }
    }

    @EventTarget
    public void RecievePacketEvent(EventReceivePacket event) {
        if (!this.isEnabled()) {
        }
    }
}
