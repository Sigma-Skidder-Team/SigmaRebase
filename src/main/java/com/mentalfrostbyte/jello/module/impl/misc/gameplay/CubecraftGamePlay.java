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

public class CubecraftGamePlay extends Module {
    public GamePlay parentModule;

    public CubecraftGamePlay() {
        super(ModuleCategory.MISC, "Cubecraft", "Gameplay for Cubecraft");
    }

    @Override
    public void initialize() {
        this.parentModule = (GamePlay) this.access();
    }

    @EventTarget
    public void onPacket(EventReceivePacket event) {
        if (this.isEnabled() && mc.player != null) {
            IPacket<?> packet = event.packet;
            if (packet instanceof SChatPacket chatPacket) {
                String text = chatPacket.getChatComponent().getString();
                String playerName = mc.player.getName().getString().toLowerCase();
                if (this.parentModule.getBooleanValueFromSettingName("AutoL")
                        && (
                        text.toLowerCase().contains("was slain by " + playerName)
                                || text.toLowerCase().contains("burned to death while fighting " + playerName)
                                || text.toLowerCase().contains("was shot by " + playerName)
                                || text.toLowerCase().contains("burnt to a crisp while fighting " + playerName)
                                || text.toLowerCase().contains("couldn't fly while escaping " + playerName)
                                || text.toLowerCase().contains("thought they could survive in the void while escaping " + playerName)
                                || text.toLowerCase().contains("fell to their death while escaping " + playerName)
                                || text.toLowerCase().contains("died in the void while escaping " + playerName)
                )) {
                    this.parentModule.processAutoLMessage(text);
                }

                if (text.contains("§a§lPlay Again §r§8• §r§6§lAuto Mode §r§8• §r§c§lLeave") && this.parentModule.getBooleanValueFromSettingName("Auto Join")) {
                    for (ITextComponent textCom : chatPacket.getChatComponent().getSiblings()) {
                        ClickEvent clickEvent = textCom.getStyle().getClickEvent();
                        if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND && clickEvent.getValue().contains("playagain")) {
                        }
                        this.parentModule.updateTimedMessage(new TimedMessage(clickEvent.getValue(), (long) this.parentModule.getNumberValueBySettingName("Auto Join delay") * 1000L));
                        Client.getInstance()
                                .notificationManager
                                .send(
                                        new Notification("Auto Join", "Joining a new game in 3 seconds.", (int) (this.parentModule.getNumberValueBySettingName("Auto Join delay") - 1.0F) * 1000)
                                );
                        break;
                    }
                }

                if (this.parentModule.getBooleanValueFromSettingName("AutoGG") && text.equalsIgnoreCase("§e" + mc.player.getName().getString() + "§r§a won the game!§r")) {
                    this.parentModule.initializeAutoL();
                }
            }
        }
    }
}
