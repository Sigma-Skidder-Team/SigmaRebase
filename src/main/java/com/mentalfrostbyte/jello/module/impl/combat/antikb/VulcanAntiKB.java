package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class VulcanAntiKB extends Module {
    public VulcanAntiKB() {
        super(ModuleCategory.COMBAT, "Vulcan", "Tries to reduce your knockback.");
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (mc.player == null)
            return;

        if (event.packet instanceof SEntityVelocityPacket packet) {
            if (packet.getEntityID() == mc.player.getEntityId()) {
                packet.motionX = 0;
                packet.motionZ = 1;
                packet.motionY = 0;
            }
        }

        if (event.packet instanceof CConfirmTransactionPacket) {
            event.cancelled = true;
        }
    }
}
