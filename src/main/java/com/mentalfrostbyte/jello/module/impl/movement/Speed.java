package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.speed.*;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Speed extends ModuleWithModuleSettings {
    public static int tickCounter;

    public Speed() {
        super(
                ModuleCategory.MOVEMENT,
                "Speed",
                "Vroom vroom",
                new VanillaSpeed(),
                new HypixelSpeed(),
                new HypixelNewSpeed(),
                new AACSpeed(),
                new OldAACSpeed(),
                new ViperMCSpeed(),
                new SlowHopSpeed(),
                new NCPSpeed(),
                new LegitSpeed(),
                new CubecraftSpeed(),
                new YPortSpeed(),
                new MinemenSpeed(),
                new BoostSpeed(),
                new VerusSpeed(), //AUTHOR - alarmingly_good (on discord)
                new VulcanSpeed(),
                new LowHopSpeed(),
                new InvadedSpeed(),
                new MineplexSpeed(),
                new GommeSpeed(),
                new TestSpeed()
        );
        this.registerSetting(new BooleanSetting("Lag back checker", "Disable speed when you get lag back", true));
        tickCounter = 0;
    }

    @EventTarget    
    public void TickEvent(EventPlayerTick event) {
        tickCounter++;
    }

    @EventTarget
    public void RecievePacketEvent(EventReceivePacket event) {
        if (event.packet instanceof SPlayerPositionLookPacket && mc.player != null) {
            tickCounter = 0;
            if (this.getBooleanValueFromSettingName("Lag back checker") && this.isEnabled() && mc.player.ticksExisted > 2) {
                Client.getInstance().notificationManager.send(new Notification("Speed", "Disabled speed due to lagback."));
                this.toggle();
            }
        }
    }

    public void callHypixelSpeedMethod() {
        if (this.parentModule instanceof HypixelSpeed) {
            HypixelSpeed hypixelSpeed = (HypixelSpeed) this.parentModule;
            hypixelSpeed.method16044();
        }
    }
}
