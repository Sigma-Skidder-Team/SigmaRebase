package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SEntityStatusPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class HitSounds extends Module {
    public HitSounds() {
        super(ModuleCategory.COMBAT, "HitSounds", "Changes the player hurting sounds client side.");
    }

    @EventTarget
    @LowerPriority
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof SEntityStatusPacket sEntityStatusPacket) {
                if (mc.world == null) return;
                // it can be null
                try {
                    //noinspection ConstantConditions
                    if (sEntityStatusPacket.getEntity(mc.world) == null) return;
                } // // https://medium.com/madhash/how-null-references-became-the-billion-dollar-mistake-bcf0c0cc72ef
                catch (NullPointerException billionDollarMistake) {
                    System.err.println(
                            "[HitSounds] Warning: ignored null pointer exception, probably doesn't matter: " + billionDollarMistake
                    );
                    return;
                }
                if (mc.player == null) return;
                if (sEntityStatusPacket.getEntity(mc.world).isAlive() && !(sEntityStatusPacket.getEntity(mc.world).getDistance(mc.player) > 5.0F)) {
                    sEntityStatusPacket.getEntity(mc.world);
                }
            }
        }
    }
}
