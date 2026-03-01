package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class HypixelDisabler extends Module {
    private final List<IPacket<?>> heldPackets = new ArrayList<>();
    private boolean doStuff;
    private final TimerUtil timer = new TimerUtil();

    public HypixelDisabler() {
        super(ModuleCategory.EXPLOIT, "Hypixel", "Disable watchdog.");
    }

    @Override
    public void onEnable() {
        if (!mc.player.isOnGround()) {
            this.doStuff = false;
        } else {
            double x = mc.player.getPosX();
            double y = mc.player.getPosY();
            double z = mc.player.getPosZ();
            this.doStuff = false;
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.2, z, false));
            mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.1, z, false));
            this.doStuff = true;
            this.timer.reset();
            this.timer.start();
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
        this.timer.reset();
        this.timer.start();
        if (this.doStuff) {
            for (IPacket<?> heldPacket : this.heldPackets) {
                mc.getConnection().sendPacket(heldPacket);
            }

            Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Disabler canceled"));
        }

        this.heldPackets.clear();
    }

    @EventTarget
    public void onMotion(EventMotion ignored) {
        if (mc.player != null) {
            if (!this.doStuff && mc.player.isOnGround()) {
                if (!this.getBooleanValueFromSettingName("Instant")) {
                    Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Wait 5s..."));
                } else {
                    Client.getInstance().notificationManager
                            .send(new Notification("Hypixel disabler", "Move where you want"));
                }

                this.timer.reset();
                this.timer.start();
                this.doStuff = true;
            }
        }
    }

    @EventTarget
    public void onMove(EventMove e) {
        if (mc.player != null) {
            if (!this.getBooleanValueFromSettingName("Instant") && this.doStuff) {
                e.setX(0.0);
                e.setY(0.0);
                e.setZ(0.0);
            }

            if (this.timer.getElapsedTime() > 10000L && this.doStuff) {
                this.doStuff = false;

                for (IPacket<?> heldPacket : this.heldPackets) {
                    mc.getConnection().sendPacket(heldPacket);
                }

                this.heldPackets.clear();
                this.timer.reset();
                this.timer.stop();
                this.getParent().toggle();
                Client.getInstance().notificationManager.send(new Notification("Hypixel disabler", "Disabler failed"));
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacket e) {
        if (mc.getConnection() != null) {
            if (this.doStuff) {
                if (e.packet instanceof CEntityActionPacket
                        || e.packet instanceof CPlayerPacket
                        || e.packet instanceof CUseEntityPacket
                        || e.packet instanceof CAnimateHandPacket
                        || e.packet instanceof CPlayerTryUseItemPacket) {
                    if (this.getBooleanValueFromSettingName("Instant")) {
                        this.heldPackets.add(e.packet);
                    }

                    e.cancelled = true;
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket e) {
        if (mc.player != null && this.doStuff) {
            if (this.isEnabled() || this.getBooleanValueFromSettingName("Instant")) {
                if (e.packet instanceof SPlayerPositionLookPacket pkt) {
                    this.getParent().toggle();
                    if (!this.getBooleanValueFromSettingName("Instant")) {
                        Client.getInstance().notificationManager
                                .send(new Notification("Hypixel disabler", "You can do what you want for 5s"));
                    } else {
                        e.cancelled = true;
                        mc.getConnection()
                                .sendPacket(new CPlayerPacket.PositionRotationPacket(pkt.getX(), pkt.getY(), pkt.getZ(), pkt.yaw,
                                        pkt.pitch, false));

                        for (IPacket<?> heldPacket : this.heldPackets) {
                            mc.getConnection().sendPacket(heldPacket);
                        }

                        this.heldPackets.clear();
                        Client.getInstance().notificationManager
                                .send(new Notification("Hypixel disabler", "Successfully sent packets"));
                    }

                    this.doStuff = false;
                }
            }
        }
    }
}