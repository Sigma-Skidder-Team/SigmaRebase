package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import team.sdhq.eventBus.annotations.EventTarget;

public class FastLadder extends Module {
    public FastLadder() {
        super(ModuleCategory.MOVEMENT, "FastLadder", "Allows you to climp ladders faster");
        this.registerSetting(new NumberSetting<Float>("Motion", "Climbing motion", 0.35F, Float.class, 0.2F, 1.0F, 0.05F));
        this.registerSetting(new ModeSetting("Down mode", "The way you will go down on ladders", 0, "None", "OnSneak", "Always"));
    }

    @EventTarget
    public void EventMove(EventMove event) {
        if (this.isEnabled() && mc.player.isOnLadder()) {
            boolean ViaVer = JelloPortal.getVersion().newerThanOrEqualTo(ProtocolVersion.v1_9) || mc.getCurrentServerData() == null;
            if (!mc.player.collidedHorizontally && (!mc.player.isJumping || !ViaVer)) {
                if (this.getStringSettingValueByName("Down mode").equals("OnSneak") && mc.gameSettings.keyBindSneak.isKeyDown()
                        || this.getStringSettingValueByName("Down mode").equals("Always")) {
                    event.setY(-this.getNumberValueBySettingName("Motion"));
                }
            } else {
                event.setY(this.getNumberValueBySettingName("Motion"));
            }
        }
    }
}