package com.mentalfrostbyte.jello.module.impl.misc.gameplay;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.LivingDeathEvent;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.impl.misc.gameplay.miniblox.AutoBuy;
import com.mentalfrostbyte.jello.util.client.logger.TimedMessage;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.misc.GamePlay;
import com.mentalfrostbyte.jello.module.settings.impl.*;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.Nullable;
import team.sdhq.eventBus.annotations.EventTarget;

public class MinibloxGamePlay extends Module {
//    private final ModeSetting skywarsKit;
    private final ModeSetting autoVoteMode;
    private final BooleanSetting autoVote;
    private final BooleanSetting autoBuy;
    private GamePlay parentModule;
    private @Nullable String minibloxName;
    public static final ResourceLocation NAME_C2S_CHANNEL
            = new ResourceLocation("layer", "name_c2s");
    public static final ResourceLocation NAME_S2C_CHANNEL
            = new ResourceLocation("layer", "name_s2c");
    public static final PacketBuffer EMPTY_BUFFER = new PacketBuffer(Unpooled.buffer());
//    private final ModeSetting kitPvPKit;
    public boolean sent = false;

    public MinibloxGamePlay() {
        super(ModuleCategory.MISC, "Miniblox", "Gameplay for Miniblox");
        registerSetting(new BooleanSetting("FriendAccept", "Automatically accept friend requests", false));
        registerSetting(
                this.autoVote = new BooleanSetting(
                        "AutoVote",
                        "Automatically vote on the skywars gamemode poll",
                        true
                )
        );
        registerSetting(
                this.autoVoteMode = new ModeSetting(
                        "Mode",
                        "Mode to vote on Skywars",
                        "Insane (2)",
                        "Normal (1)",
                        "Insane (2)"
                )
        );
//        registerSetting(new BooleanSetting("AutoKit", "Automatically select kits", false));
        registerSetting(this.autoBuy = new BooleanSetting("AutoBuy", "Automatically buys stuff", false));
        registerSetting(AutoBuy.armor, AutoBuy.chainmailArmor, AutoBuy.ironArmor, AutoBuy.diamondArmor);
        registerSetting(AutoBuy.sword, AutoBuy.stoneSword, AutoBuy.ironSword, AutoBuy.diamondSword);
        registerSetting(AutoBuy.doUpgrades, AutoBuy.sharpnessUpgrade, AutoBuy.protectionUpgrade,
                AutoBuy.hasteUpgrade, AutoBuy.healPoolUpgrade,
                AutoBuy.forgeUpgrade
        );
    }

    public String getMinibloxName() {
        if (minibloxName != null) {
            return minibloxName;
        }
        assert mc.player != null;
        return mc.player.getName().getString();
    }

    @Override
    public void initialize() {
        parentModule = (GamePlay) access();
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onWorldEvent(EventLoadWorld e) {
        if (autoBuy.currentValue) AutoBuy.onWorldEvent(e);
        sent = false;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdateEvent(EventMotion e) {
        if (!autoBuy.currentValue) return;
        AutoBuy.onUpdateEvent(e);
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onLivingDeath(LivingDeathEvent e) {
        if (!autoBuy.currentValue) return;
        AutoBuy.onLivingDeathEvent(e);
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onReceive(EventReceivePacket event) {
        IPacket<?> packet = event.packet;
        if (mc.player != null) {
            if (packet instanceof SCustomPayloadPlayPacket pl) {
                if (!pl.getChannelName().equals(NAME_S2C_CHANNEL)) return;
                final var pb = pl.getBufferData();
                minibloxName = pb.readString();
                Client.getInstance().notificationManager.send(new Notification(
                        "GamePlay",
                        "Your miniblox username is " + minibloxName
                ));
            }
            if (!sent) {
                mc.getConnection().sendPacket(new CCustomPayloadPacket(
                    NAME_C2S_CHANNEL, EMPTY_BUFFER
                ));
                sent = true;
            }
            if (packet instanceof SChatPacket chatPacket) {
                String text = chatPacket.getChatComponent().getString().replaceAll("§.", "");
                if (chatPacket.getType() != ChatType.SYSTEM) {
                    return;
                }

                String playerName = getMinibloxName();

                if (autoVote.currentValue && text.equals("Poll started: Choose a gamemode")) {
                    switch (autoVoteMode.currentValue) {
                        case "Normal (1)":
                            MinecraftUtil.sendChatMessage("/vote 1");
                            break;
                        case "Insane (2)":
                            MinecraftUtil.sendChatMessage("/vote 2");
                            break;
                    }
                }

                if (parentModule.getBooleanValueFromSettingName("AutoL")) {
//                    boolean confirmedKill = false;

//                    if (text.startsWith("KILL! ")) {
//                        confirmedKill = true;
//                    }

//                    if (confirmedKill) {
//                        String[] splitText = text.split(" ");
//                        if (splitText.length > 3) {
//                            parentModule.processAutoLMessage(splitText[3]);
//                        }
//                    }

                    String lowerCaseText = text.toLowerCase();
                    if (lowerCaseText.contains("was slain by " + playerName)
                            || (lowerCaseText.contains("has been eliminated by") && lowerCaseText.endsWith(playerName + "!"))
                            || lowerCaseText.contains("burned to death while fighting " + playerName)
                            || lowerCaseText.contains("was shot by " + playerName)
                            || lowerCaseText.contains("burnt to a crisp while fighting " + playerName)
                            || lowerCaseText.contains("couldn't fly while escaping " + playerName)
                            || lowerCaseText.contains("thought they could survive in the void while escaping " + playerName)
                            || lowerCaseText.contains("fell to their death while escaping " + playerName)
                            || lowerCaseText.contains("died in the void while escaping " + playerName)) {
                        this.parentModule.processAutoLMessage(text);
                    }
                }

                if (getBooleanValueFromSettingName("FriendAccept") && text.contains("[ACCEPT] - [DENY] - [IGNORE]")) {
                    for (ITextComponent textCom : chatPacket.getChatComponent().getSiblings()) {
                        ClickEvent clickEvent = textCom.getStyle().getClickEvent();
                        if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND && clickEvent.getValue().contains("/f accept")) {
                            MinecraftUtil.sendChatMessage(clickEvent.getValue());
                        }
                    }
                }

                if (text.contains("The game is currently in progress. Please wait for the next game to start.")) {
                    if (parentModule.getBooleanValueFromSettingName("Auto Join")) {
                        // TODO: get the gamemode from the first line of the scoreboard
                    }
                }

                if (text.contains("Click here to play again")) {
                    if (parentModule.getBooleanValueFromSettingName("Auto Join")) {
                        for (ITextComponent textCom : chatPacket.getChatComponent().getSiblings()) {
                            ClickEvent clickEvent = textCom.getStyle().getClickEvent();

                            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                                parentModule.updateTimedMessage(
                                        new TimedMessage(
                                                clickEvent.getValue(),
                                                (long)
                                                        parentModule.
                                                                getNumberValueBySettingName("Auto Join delay") * 1000L));
                            }
                        }
                    }

                    if (parentModule.getBooleanValueFromSettingName("AutoGG")) {
                        parentModule.initializeAutoL();
                    }
                }
            }
        }
    }
}
