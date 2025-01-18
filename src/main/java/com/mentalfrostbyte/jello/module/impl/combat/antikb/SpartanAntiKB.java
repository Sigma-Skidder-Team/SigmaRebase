package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.ReceivePacketEvent;
import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class SpartanAntiKB extends Module {
    private int ticksSinceKb;

    public SpartanAntiKB() {
        super(ModuleCategory.COMBAT, "Spartan", "AntiKB for spartan antichet");
        this.registerSetting(new NumberSetting<Float>("Ticks", "Ticks delay", 1.0F, Float.class, 1.0F, 6.0F, 1.0F));
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            if (this.ticksSinceKb < 10) {
                this.ticksSinceKb++;
                if (this.ticksSinceKb == (int) this.getNumberValueBySettingName("Ticks")) {
                    MovementUtil.strafe(0.0);
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        if (this.isEnabled()) {
            if (mc.player != null && event.getPacket() instanceof SEntityVelocityPacket) {
                SEntityVelocityPacket var4 = (SEntityVelocityPacket) event.getPacket();
                if (var4.getEntityID() == mc.player.getEntityId()) {
                    this.ticksSinceKb = 0;
                }
            }
        }
    }
}
