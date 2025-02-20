package com.mentalfrostbyte.jello.module.impl.misc.gameplay;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.util.client.logger.TimedMessage;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.misc.GamePlay;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import team.sdhq.eventBus.annotations.EventTarget;

public class JartexGamePlay extends Module {
    private GamePlay parentModule;

    public JartexGamePlay() {
        super(ModuleCategory.MISC, "Jartex", "Gameplay for Jartex network");
    }

    @Override
    public void initialize() {
        this.parentModule = (GamePlay) this.access();
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (this.isEnabled() && mc.player != null) {
            IPacket<?> packet = event.packet;
            if (packet instanceof SChatPacket chatPacket) {
                String text = chatPacket.getChatComponent().getString();
                String playerName = mc.player.getName().getString().toLowerCase();
                if (this.parentModule.getBooleanValueFromSettingName("AutoL")
                        && (text.toLowerCase().contains("§r§7 has been killed by §r§a§l" + playerName)
                        || text.toLowerCase().contains("§r§7 was shot by §r§a§l" + playerName)
                        || text.toLowerCase().contains("§r§7 was killed with dynamite by §r§a§l" + playerName))) {
                    this.parentModule.processAutoLMessage(text);
                }

                if (text.contains("§e§lPlay Again? §r§7Click here!§r")) {
                    if (this.parentModule.getBooleanValueFromSettingName("AutoGG")) {
                        this.parentModule.initializeAutoL();
                    }

                    if (this.parentModule.getBooleanValueFromSettingName("Auto Join")) {
                        for (ITextComponent textCom : chatPacket.getChatComponent().getSiblings()) {
                            ClickEvent clickEvent = textCom.getStyle().getClickEvent();
                            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                                this.parentModule.updateTimedMessage(new TimedMessage(clickEvent.getValue(),
                                        (long) this.parentModule.getNumberValueBySettingName("Auto Join delay") * 1000L));
                                Client.getInstance().notificationManager
                                        .send(
                                                new Notification(
                                                        "Auto Join", "Joining a new game in 3 seconds.",
                                                        (int) (this.parentModule
                                                                .getNumberValueBySettingName("Auto Join delay") - 1.0F)
                                                                * 1000));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
