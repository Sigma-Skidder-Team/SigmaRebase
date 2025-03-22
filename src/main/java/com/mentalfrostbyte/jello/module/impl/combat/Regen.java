package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Regen extends Module {
    public Regen() {
        super(ModuleCategory.COMBAT, "Regen", "Regenerates hearts faster than ever (1.8 only)");
        this.registerSetting(new NumberSetting<Float>("Packet amount", "Number of packets sent", 50.0F, Float.class, 1.0F, 100.0F, 1.0F));
        this.registerSetting(new BooleanSetting("Only OnGround", "Regen only when on ground", false));
    }

    @EventTarget
    public void method16007(EventPlayerTick var1) {
        if (this.isEnabled()) {
            if (!mc.player.abilities.isCreativeMode
                    && mc.player.getFoodStats().getFoodLevel() > 17
                    && mc.player.getHealth() < 20.0F
                    && mc.player.getHealth() != 0.0F
                    && (mc.player.isOnGround() || !this.getBooleanValueFromSettingName("Only OnGround"))) {
                for (int var4 = 0; (float)var4 < this.getNumberValueBySettingName("Packet amount"); var4++) {
                    mc.getConnection().sendPacket(new CPlayerPacket(mc.player.isOnGround()));
                }
            }
        }
    }
}
