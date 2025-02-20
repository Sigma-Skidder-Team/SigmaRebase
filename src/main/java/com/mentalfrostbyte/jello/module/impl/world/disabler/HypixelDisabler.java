package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class HypixelDisabler extends Module {
    private final List<IPacket<?>> field23983 = new ArrayList<IPacket<?>>();
    private boolean field23984;
    private final TimerUtil field23985 = new TimerUtil();

    public HypixelDisabler() {
        super(ModuleCategory.EXPLOIT, "Hypixel", "Disable watchdog.");
    }

    @Override
    public void onEnable() {
        if (!mc.player.isOnGround()) {
            this.field23984 = false;
        } else {
            double var3 = mc.player.getPosX();
            double var5 = mc.player.getPosY();
            double var7 = mc.player.getPosZ();
            this.field23984 = false;
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var3, var5 + 0.2, var7, false));
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var3, var5 + 0.1, var7, false));
            this.field23984 = true;
            this.field23985.reset();
            this.field23985.start();
            if (!this.getBooleanValueFromSettingName("Instant")) {
                Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Wait 5s..."));
            } else {
                Client.getInstance().notificationManager
                        .send(new Notification("Hypixel disabler", "Move where you want"));
            }
        }
    }

    @Override
    public void onDisable() {
        this.field23985.reset();
        this.field23985.start();
        if (this.field23984) {
            int var3 = this.field23983.size();

            for (int var4 = 0; var4 < var3; var4++) {
                mc.getConnection().sendPacket(this.field23983.get(var4));
            }

            Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Disabler canceled"));
        }

        this.field23983.clear();
    }

    @EventTarget
    public void method16898(EventUpdateWalkingPlayer var1) {
        if (mc.player != null) {
            if (!this.field23984 && mc.player.isOnGround()) {
                if (!this.getBooleanValueFromSettingName("Instant")) {
                    Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Wait 5s..."));
                } else {
                    Client.getInstance().notificationManager
                            .send(new Notification("Hypixel disabler", "Move where you want"));
                }

                this.field23985.reset();
                this.field23985.start();
                this.field23984 = true;
            }
        }
    }

    @EventTarget
    public void method16899(EventMove var1) {
        if (mc.player != null) {
            if (!this.getBooleanValueFromSettingName("Instant") && this.field23984) {
                var1.setX(0.0);
                var1.setY(0.0);
                var1.setZ(0.0);
            }

            if (this.field23985.getElapsedTime() > 10000L && this.field23984) {
                this.field23984 = false;
                int var4 = this.field23983.size();

                for (int var5 = 0; var5 < var4; var5++) {
                    mc.getConnection().sendPacket(this.field23983.get(var5));
                }

                this.field23983.clear();
                this.field23985.reset();
                this.field23985.stop();
                this.access().toggle();
                Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Disabler failed"));
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacket var1) {
        if (mc.getConnection() != null) {
            if (this.field23984) {
                if (var1.packet instanceof CEntityActionPacket
                        || var1.packet instanceof CPlayerPacket
                        || var1.packet instanceof CUseEntityPacket
                        || var1.packet instanceof CAnimateHandPacket
                        || var1.packet instanceof CPlayerTryUseItemPacket) {
                    if (this.getBooleanValueFromSettingName("Instant")) {
                        this.field23983.add(var1.packet);
                    }

                    var1.cancelled = true;
                }
            }
        }
    }

    @EventTarget
    public void method16901(EventReceivePacket var1) {
        if (mc.player != null && this.field23984) {
            if (this.isEnabled() || this.getBooleanValueFromSettingName("Instant")) {
                if (var1.packet instanceof SPlayerPositionLookPacket) {
                    this.access().toggle();
                    if (!this.getBooleanValueFromSettingName("Instant")) {
                        Client.getInstance().notificationManager
                                .send(new Notification("Hypixel disabler", "You can do what you want for 5s"));
                    } else {
                        SPlayerPositionLookPacket var4 = (SPlayerPositionLookPacket) var1.packet;
                        var1.cancelled = true;
                        mc.getConnection()
                                .sendPacket(new CPlayerPacket.PositionRotationPacket(var4.getX(), var4.getY(), var4.getZ(), var4.yaw,
                                        var4.pitch, false));
                        int var5 = this.field23983.size();

                        for (int var6 = 0; var6 < var5; var6++) {
                            mc.getConnection().sendPacket(this.field23983.get(var6));
                        }

                        this.field23983.clear();
                        Client.getInstance().notificationManager
                                .send(new Notification("Hypixel disabler", "Successfully sent packets"));
                    }

                    this.field23984 = false;
                }
            }
        }
    }
}
