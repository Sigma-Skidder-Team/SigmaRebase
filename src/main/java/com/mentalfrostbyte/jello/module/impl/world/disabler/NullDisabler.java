package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SKeepAlivePacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class NullDisabler extends Module {
    public NullDisabler() {
        super(ModuleCategory.EXPLOIT, "Null", "Makes you invisible for the anticheat.");
        this.registerSetting(new BooleanSetting("Inv Bypass", "Avoid inventory glitchs on some servers", false));
    }

    @EventTarget
    public void RecievePacketEvent(EventReceivePacket event) {
        if (this.isEnabled() && mc.getCurrentServerData() != null) {
            if (!(event.packet instanceof SKeepAlivePacket)) {
                if (event.packet instanceof SConfirmTransactionPacket) {
                    SConfirmTransactionPacket confirmTransactionPacket = (SConfirmTransactionPacket) event.packet;
                    if (confirmTransactionPacket.getActionNumber() < 0 || !this.getBooleanValueFromSettingName("Inv Bypass")) {
                        event.cancelled = true;
                    }
                }
            } else {
                event.cancelled = true;
            }
        }
    }
}
