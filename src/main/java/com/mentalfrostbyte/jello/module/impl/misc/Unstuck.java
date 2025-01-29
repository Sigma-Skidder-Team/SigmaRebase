package com.mentalfrostbyte.jello.module.impl.misc;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.player.MovementUtil;

import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Unstuck extends Module {
    private int packetCancelled;
    private int packetsToCancelCount;

    public Unstuck() {
        super(ModuleCategory.MISC, "Unstuck", "Toggle this when an anticheat freeze you mid-air");
        this.registerSetting(new NumberSetting<>("Flags", "Maximum flag before trying to unstuck", 5.0F, Float.class, 2.0F, 20.0F, 1.0F));
    }

    @Override
    public void onEnable() {
        this.packetCancelled = 0;
    }

    @EventTarget
    public void onMove(EventMove event) {
        if (this.isEnabled()) {
            if ((float) this.packetCancelled >= this.getNumberValueBySettingName("Flags")) {
                MovementUtil.setSpeed(event, 0.0);
                event.setY(0.0);
                mc.player.setMotion(0.0, 0.0, 0.0);
            }
        }
    }

    @EventTarget
    public void onWorldLoad(EventLoadWorld event) {
        if (this.isEnabled()) {
            this.packetCancelled = 0;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && event.isPre()) {
            if (!mc.player.isOnGround() && !MultiUtilities.isAboveBounds(mc.player, 0.001F)) {
                if ((float) this.packetCancelled >= this.getNumberValueBySettingName("Flags") && this.packetsToCancelCount == 0) {
                    this.packetsToCancelCount = 60;
                    Client.getInstance().notificationManager.send(new Notification("Unstuck", "Trying to unstuck you.."));
                }

                if (this.packetsToCancelCount > 0) {
                    this.packetsToCancelCount--;
                    if (this.packetsToCancelCount == 0) {
                        this.packetCancelled = 0;
                    }

                    event.setCancelled(true);
                }
            } else {
                this.packetCancelled = 0;
            }
        }
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (mc.player != null) {
                if (event.getPacket() instanceof SPlayerPositionLookPacket && !MultiUtilities.isAboveBounds(mc.player, 0.3F) && mc.player.ticksExisted > 10) {
                    this.packetCancelled++;
                    if ((float) this.packetCancelled > this.getNumberValueBySettingName("Flags")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
