package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import team.sdhq.eventBus.annotations.EventTarget;

public class PortalGui extends Module {
    public PortalGui() {
        super(ModuleCategory.MISC, "PortalGui", "Allows GUIs while in nether portal");
    }

    @EventTarget
    public void onTick(EventPlayerTick var1) {
        if (this.isEnabled()) {
            mc.player.inPortal = false;
        }
    }
}
