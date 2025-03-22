package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import net.minecraft.network.play.client.CEntityActionPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Sneak extends Module {
    public Sneak() {
        super(ModuleCategory.PLAYER, "Sneak", "Always sneaks");
    }

    @Override
    public void onDisable() {
        mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
    }

    @EventTarget
    public void EventUpdate(EventUpdateWalkingPlayer eventUpdateWakkingPlayer) {
        if (this.isEnabled()) {
            if (!eventUpdateWakkingPlayer.isPre()) {
                mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
                mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
            } else {
                mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
                mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
            }
        }
    }
}
