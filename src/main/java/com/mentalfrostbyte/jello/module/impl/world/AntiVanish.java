package com.mentalfrostbyte.jello.module.impl.world;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.util.text.ITextComponent;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class AntiVanish extends Module {
    public final List<UUID> field23967 = new CopyOnWriteArrayList<UUID>();
    public int field23968 = -3200;

    public AntiVanish() {
        super(ModuleCategory.WORLD, "AntiVanish", "Detects if there are vanished players");
    }

    @EventTarget
    public void method16862(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled()) {
            if (var1.isPre() && mc.getCurrentServerData() != null) {
                if (!this.field23967.isEmpty()) {
                    if (this.field23968 > 3200) {
                        this.field23967.clear();
                        Client.getInstance().notificationManager.send(new Notification("Vanished Cleared", "Vanish List has been Cleared.", 5500));
                        this.field23968 = -3200;
                    } else {
                        this.field23968++;
                    }
                }

                if (this.field23967 != null) {
                    try {
                        for (UUID var5 : this.field23967) {
                            NetworkPlayerInfo var6 = mc.getConnection().getPlayerInfo(var5);
                            ITextComponent var7 = var6 == null ? null : var6.getDisplayName();
                            if (var6 != null && this.field23967.contains(var5)) {
                                if (var7 == null) {
                                    Client.getInstance()
                                            .notificationManager
                                            .send(
                                                    new Notification("Vanished Warning", "A player is vanished !!" + var6.getDisplayName().getUnformattedComponentText(), 5500)
                                            );
                                } else {
                                    Client.getInstance()
                                            .notificationManager
                                            .send(new Notification("Vanish Warning", var6.getDisplayName().getString() + " is no longer Vanished.", 5500));
                                }
                            }

                            this.field23967.remove(var5);
                        }
                    } catch (Exception var8) {
                        Client.getInstance().notificationManager.send(new Notification("Vanished Error", "Something bad happened.", 5500));
                    }
                }
            }
        }
    }

    @EventTarget
    public void method16863(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (mc.getConnection() != null && var1.packet instanceof SPlayerListItemPacket var4) {
				if (var4.getAction() == SPlayerListItemPacket.Action.UPDATE_LATENCY) {
                    for (SPlayerListItemPacket.AddPlayerData var6 : var4.getEntries()) {
                        NetworkPlayerInfo var7 = mc.getConnection().getPlayerInfo(var6.getProfile().getId());
                        if (var7 == null && !this.method16864(var6.getProfile().getId())) {
                            System.out.println(var6.getProfile().getId());
                            Client.getInstance().notificationManager.send(new Notification("Vanished Warning", "A player is vanished ! ", 5500));
                            this.field23968 = -3200;
                        }
                    }
                }
            }
        }
    }

    public boolean method16864(UUID var1) {
        if (!this.field23967.contains(var1)) {
            this.field23967.add(var1);
            return false;
        } else {
            return true;
        }
    }
}
