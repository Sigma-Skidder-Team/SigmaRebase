package com.mentalfrostbyte.jello.module.impl.combat.criticals;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventStep;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
//import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
//import com.mentalfrostbyte.jello.settings.BooleanSetting;
//import com.mentalfrostbyte.jello.settings.ModeSetting;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class MinisCriticals extends Module {
    public MinisCriticals() {
        super(ModuleCategory.COMBAT, "Minis", "Minis criticals");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Basic", "Hypixel", "Cubecraft"));
        this.registerSetting(new BooleanSetting("Avoid Fall Damage", "Avoid fall damages", false));
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (this.isEnabled()) {
            if (KillAura.isActive) {
                mc.getConnection()
                        .sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), true));
            }
        }
    }

    @EventTarget
    public void onStep(EventStep event) {
        if (this.isEnabled()) {
            if (KillAura.isActive) {
                event.cancelled = true;
            }
        }
    }
}
