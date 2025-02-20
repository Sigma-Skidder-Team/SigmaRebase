package com.mentalfrostbyte.jello.module.impl.misc.gameplay;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.util.client.logger.TimedMessage;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.misc.GamePlay;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.STeamsPacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.commons.lang3.StringUtils;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.Arrays;
import java.util.Collection;

public class HypixelGamePlay extends Module {
    private GamePlay parentModule;

    public HypixelGamePlay() {
        super(ModuleCategory.MISC, "Hypixel", "Gameplay for Hypixel");
        registerSetting(new BooleanSetting("FriendAccept", "Automatically accept friend requests", false));
        registerSetting(new BooleanSetting("Hide infos", "Hide scoreboard server informations & date when ingame", false));
    }

    @Override
    public void initialize() {
        parentModule = (GamePlay) access();
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (mc.player != null) {
            IPacket<?> packet = event.packet;
            if (packet instanceof SChatPacket chatPacket) {
                String text = chatPacket.getChatComponent().getString().replaceAll("§.", "");
                if (chatPacket.getType() != ChatType.SYSTEM && chatPacket.getType() != ChatType.CHAT) {
                    return;
                }

                String playerName = mc.player.getName().getString().toLowerCase();

                if (parentModule.getBooleanValueFromSettingName("AutoL")) {
                    String[] killTypes = new String[]{"MULTI ", "PENTA ", "QUADRA ", "TRIPLE ", "DOUBLE ", ""};
                    boolean confirmedKill = false;

                    for (String killType : killTypes) {
                        if (text.startsWith(killType + "KILL! ")) {
                            confirmedKill = true;
                            break;
                        }
                    }

                    if (confirmedKill) {
                        String[] splitText = text.split(" ");
                        if (splitText.length > 3) {
                            parentModule.processAutoLMessage(splitText[3]);
                        }
                    }

                    if (text.toLowerCase().contains("was killed by " + playerName)
                            || text.toLowerCase().contains("was thrown into the void by " + playerName + ".")
                            || text.toLowerCase().contains("was thrown off a cliff by " + playerName + ".")
                            || text.toLowerCase().contains("was struck down by " + playerName + ".")
                            || text.toLowerCase().contains("be sent to davy jones' locker by " + playerName + ".")) {

                        Scoreboard scoreboard = mc.world.getScoreboard();
                        ScoreObjective scoreobjective = null;
                        ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(mc.player.getScoreboardName());

                        if (playerTeam != null) {
                            int colorIndex = playerTeam.getColor().getColorIndex();
                            if (colorIndex >= 0) {
                                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + colorIndex);
                            }
                        }

                        ScoreObjective objective = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
                        Collection<Score> sortedScores = scoreboard.getSortedScores(objective);
                        int playersLeft = -1;

                        for (Score score : sortedScores) {
                            ScorePlayerTeam scoreTeam = scoreboard.getPlayersTeam(score.getPlayerName());
                            String formattedName = ScorePlayerTeam.func_237500_a_(scoreTeam, new StringTextComponent(score.getPlayerName())).getString().replaceAll("§t", "");

                            for (String playersLeftKeyword : Arrays.asList("players left", "joueurs restants", "spieler verbleibend")) {
                                if (formattedName.toLowerCase().contains(playersLeftKeyword + ":")) {
                                    String[] splitName = formattedName.split(" ");
                                    if (splitName.length > 2) {
                                        try {
                                            playersLeft = Integer.parseInt(splitName[2]);
                                        } catch (NumberFormatException ignored) {
                                        }
                                        break;
                                    }
                                }
                            }

                            if (playersLeft > 0) {
                                break;
                            }
                        }
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

                if (text.contains("Want to play again? Click here! ") || text.contains("coins! (Win)")) {
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
            } else if (packet instanceof STeamsPacket teamsPacket && getBooleanValueFromSettingName("Hide infos")) {
                if (teamsPacket.getAction() == 2 && teamsPacket.getName().startsWith("team_")) {
                    String teamPrefixSuffixCombined = teamsPacket.getPrefix().getString() + teamsPacket.getSuffix().getString();
                    String[] splitPrefixSuffix = teamPrefixSuffixCombined.split(" ");
                    if (splitPrefixSuffix != null && splitPrefixSuffix.length > 1 && StringUtils.countMatches(splitPrefixSuffix[0], "/") == 2) {
                        event.cancelled = true;
                    }
                }
            }
        }
    }
}
