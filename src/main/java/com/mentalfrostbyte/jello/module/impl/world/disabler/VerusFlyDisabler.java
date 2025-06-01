package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;

import net.minecraft.network.play.client.CPlayerTryUseItemPacket;

import net.minecraft.util.Hand;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.Objects;

public class VerusFlyDisabler extends Module {
    public VerusFlyDisabler() {
        super(ModuleCategory.EXPLOIT, "Verus Fly", "Disable Verus fly checks.");
    }


    public void onEnable() {
        Client.getInstance().notificationManager.send(new Notification("Verus Fly Disabler", "Hold water bucket to bypass."));
    }

    @EventTarget
    public void TickEvent(EventUpdate event) {
        if (this.isEnabled() && mc.getCurrentServerData() != null) {
            Objects.requireNonNull(mc.getConnection()).sendPacket(
                    new CPlayerTryUseItemPacket(Hand.MAIN_HAND)
            );
        }
    }
}