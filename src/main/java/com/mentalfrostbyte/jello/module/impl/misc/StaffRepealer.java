package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class StaffRepealer extends Module {

    public StaffRepealer() {
        super(ModuleCategory.MISC, "StaffRepealer", "Repeals hypixel's staff ban laws with a simple rage quit!");
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (ServerUtil.isHypixel()) {
                mc.gameSettings.sendSettingsToServer();
            }
        }
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SPlayerListItemPacket listItemPacket) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }

                    for (SPlayerListItemPacket.AddPlayerData entity : listItemPacket.getEntries()) {
                        PlayerEntity player = mc.world.getPlayerByUuid(entity.getProfile().getId());
                        if (player == null && entity.getGameMode() != null) {
                            MinecraftUtil.addChatMessage("Detected an anomaly " + entity + entity.getProfile());
                        } else {
                            System.out.println("all seems good " + entity + entity.getProfile());
                        }
                    }
                }).start();
            }
        }
    }
}
