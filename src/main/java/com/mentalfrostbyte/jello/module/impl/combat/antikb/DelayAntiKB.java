package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class DelayAntiKB extends Module {
    public int delay = 0;
    private final List<IPacket<?>> packets = new ArrayList<>();

    public DelayAntiKB() {
        super(ModuleCategory.COMBAT, "Delay", "For anticheats with \"good\" velocity checks");
        this.registerSetting(new NumberSetting<>("Delay", "Ticks delay", 7.0F, Float.class, 1.0F, 20.0F, 1.0F));
        this.registerSetting(new NumberSetting<>("H-Multiplier", "Horizontal velocity multiplier", 0.0F, Float.class, 0.0F, 1.0F, 0.01F));
        this.registerSetting(new NumberSetting<>("V-Multiplier", "Vertical velocity multiplier", 0.0F, Float.class, 0.0F, 1.0F, 0.01F));
    }

    /** handles receiving packets **/
    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SExplosionPacket packet) {
				this.packets.add(packet);
                event.cancelled = true;
            }

            if (mc.player != null && event.packet instanceof SEntityVelocityPacket packet) {
				if (packet.getEntityID() == mc.player.getEntityId()) {
                    this.packets.add(packet);
                    event.cancelled = true;
                    if (this.delay == 0) {
                        this.delay = (int) this.getNumberValueBySettingName("Delay");
                    }
                }
            }
        }
    }

//    unused
//    @EventTarget
//    public void onSendPacket(SendPacketEvent event) {
////        if (this.isEnabled()) {
//////            if (event.packet instanceof CUseEntityPacket) {
//////                CUseEntityPacket usePacket = (CUseEntityPacket) event.packet;
//////                CUseEntityPacket.Action var5 = usePacket.getAction();
////////                if (var5 != CUseEntityPacket.Action.ATTACK) {
////////                }
//////            }
////        }
//    }


    /** handles tick events **/
    @EventTarget
    public void onTick(EventPlayerTick _event) {
        if (this.delay != 0) {
            if (this.delay > 0) {
                this.delay--;
            }
        } else {
            this.handlePackets();
        }
    }

    /** handles packets **/
    private void handlePackets() {
        for (IPacket base : this.packets) {
            if (!(base instanceof SEntityVelocityPacket packet)) {
                if (base instanceof SExplosionPacket packet) {
					packet.motionX = packet.motionX * this.getNumberValueBySettingName("H-Multiplier");
                    packet.motionZ = packet.motionZ * this.getNumberValueBySettingName("H-Multiplier");
                    packet.motionY = packet.motionY * this.getNumberValueBySettingName("V-Multiplier");
                    mc.getConnection().handleExplosion(packet);
                }
            } else {
				packet.motionX = (int) ((float) packet.motionX * this.getNumberValueBySettingName("H-Multiplier"));
                packet.motionZ = (int) ((float) packet.motionZ * this.getNumberValueBySettingName("H-Multiplier"));
                packet.motionY = (int) ((float) packet.motionY * this.getNumberValueBySettingName("V-Multiplier"));
                mc.getConnection().handleEntityVelocity(packet);
            }
        }

        this.packets.clear();
    }
}
