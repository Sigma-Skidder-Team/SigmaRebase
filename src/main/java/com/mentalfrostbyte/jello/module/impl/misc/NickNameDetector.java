package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.entity.Entity;
import team.sdhq.eventBus.annotations.EventTarget;

public class NickNameDetector extends Module {
    public NickNameDetector() {
        super(ModuleCategory.MISC, "NickNameDetector", "Detect if a player has a custom name");
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            for (Entity entity : MultiUtilities.getPlayers()) {
                if (!Client.getInstance().combatManager.isTargetABot(entity) && entity.ticksExisted > 30 && entity.hasCustomName()) {
                    MultiUtilities.sendChatMessage(entity.getName().getUnformattedComponentText() + " might have a custom nametag");
                }
            }
        }
    }
}
