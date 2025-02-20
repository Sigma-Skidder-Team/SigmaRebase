package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.action.EventInputOptions;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

public class LegitAntiKB extends Module {
    public static boolean s12_received;

    public LegitAntiKB() {
        super(ModuleCategory.COMBAT, "Legit", "Use jump-reset mechanism to reduce velocity.");
    }

    @EventTarget
    @HighestPriority
    public void onInputOptionEvent(EventInputOptions var1) {
        if(s12_received){
            var1.setJumping(true);
            s12_received = false;
        }

    }

    @EventTarget
    @HighestPriority
    public void onReceivePackett(EventReceivePacket event) {
        if (event.packet instanceof SEntityVelocityPacket) {

            SEntityVelocityPacket var4 = (SEntityVelocityPacket) event.packet;
            if (var4.getEntityID() == mc.player.getEntityId()) {
                s12_received = true;
            }
        }
    }
}