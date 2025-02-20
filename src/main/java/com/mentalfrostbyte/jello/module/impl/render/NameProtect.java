package com.mentalfrostbyte.jello.module.impl.render;

import com.mentalfrostbyte.jello.event.impl.game.EventReplaceText;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.InputSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.text.StringTextComponent;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.List;

public class NameProtect extends Module {
    public NameProtect() {
        super(ModuleCategory.RENDER, "NameProtect", "Useful for recording/streaming");
        this.registerSetting(new InputSetting("Username", "The name which your username is replaced with", "Me"));
    }

    @EventTarget
    public void TextReplaceEvent(EventReplaceText event) {
        if (this.isEnabled()) {
            event.getText(event.getText().replaceAll(mc.getSession().getUsername(), this.getStringSettingValueByName("Username")));
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket receivePacketEvent) {
        if (this.isEnabled()) {
            IPacket<?> packet = receivePacketEvent.packet;

            if (packet instanceof SUpdateScorePacket scorePacket) {
                if (scorePacket.getAction() == ServerScoreboard.Action.CHANGE) {
                    String originalUsername = scorePacket.getPlayerName();
                    String replacementUsername = this.getStringSettingValueByName("Username");
                    if (originalUsername.contains(mc.getSession().getUsername())) {
                        originalUsername = originalUsername.replaceAll(mc.getSession().getUsername(), replacementUsername);
                        receivePacketEvent.setPacket(new SUpdateScorePacket(scorePacket.getAction(), scorePacket.getObjectiveName(), originalUsername, scorePacket.getScoreValue()));
                    }
                }
            }

            if (packet instanceof SPlayerListItemPacket playerListPacket) {
                List<SPlayerListItemPacket.AddPlayerData> playerEntries = playerListPacket.getEntries();

                for (SPlayerListItemPacket.AddPlayerData playerData : playerEntries) {
                    if (playerData.getDisplayName() != null) {
                        String displayName = playerData.getDisplayName().getString();
                        String replacementUsername = this.getStringSettingValueByName("Username");
                        if (displayName.contains(mc.getSession().getUsername())) {
                            displayName = displayName.replaceAll(mc.getSession().getUsername(), replacementUsername);
                            playerData.displayName = new StringTextComponent(displayName);
                        }
                    }
                }

                playerListPacket.players = playerEntries;
            }

            if (packet instanceof SUpdateBossInfoPacket bossInfoPacket) {
                if (bossInfoPacket.getName() == null) {
                    return;
                }

                String bossName = bossInfoPacket.getName().getString();
                String replacementUsername = this.getStringSettingValueByName("Username");
                if (bossName.contains(mc.getSession().getUsername())) {
                    bossName = bossName.replaceAll(mc.getSession().getUsername(), replacementUsername);
                    bossInfoPacket.setName(new StringTextComponent(bossName));
                }
            }

            if (packet instanceof STitlePacket titlePacket) {
                if (titlePacket.getMessage() == null) {
                    return;
                }

                String titleText = titlePacket.getMessage().getString();
                String replacementUsername = this.getStringSettingValueByName("Username");
                if (titleText.contains(mc.getSession().getUsername())) {
                    titleText = titleText.replaceAll(mc.getSession().getUsername(), replacementUsername);
                    receivePacketEvent.setPacket(new STitlePacket(titlePacket.getType(), new StringTextComponent(titleText), titlePacket.getFadeInTime(), titlePacket.getDisplayTime(), titlePacket.getFadeOutTime()));
                }
            }
        }
    }
}
