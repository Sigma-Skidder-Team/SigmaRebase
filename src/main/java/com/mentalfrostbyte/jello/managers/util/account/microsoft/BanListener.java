package com.mentalfrostbyte.jello.managers.util.account.microsoft;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.login.server.SDisconnectLoginPacket;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.network.play.server.SChatPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanListener {
    public Minecraft mc = Minecraft.getInstance();

    @EventTarget
    public void onPacketReceive(EventReceivePacket event) {
        if (this.mc.getCurrentServerData() != null) {
            if (event.packet instanceof SChatPacket packet) {
                ArrayList<String> banMessages = new ArrayList<>(
                        Arrays.asList(
                                "You are permanently banned from MinemenClub. ",
                                "Your connection to the server leu-practice has been prevented due to you being associated to a blacklisted player.",
                                "You are blacklisted from MinemenClub. "
                        )
                );
                if (!packet.getChatComponent().getSiblings().isEmpty()
                        && banMessages.contains(packet.getChatComponent().getString())
                        && packet.getChatComponent().getSiblings().get(0).getStyle().getColor().toString().equalsIgnoreCase("red")) {
                    Account account = Client.getInstance().accountManager.containsAccount();
                    if (account != null) {
                        Ban ban = new Ban(this.mc.getCurrentServerData().serverIP, new Date(Long.MAX_VALUE));
                        account.registerBan(ban);
                        Client.getInstance().accountManager.updateAccount(account);
                        Client.getInstance().accountManager.saveAlts();
                    }
                }
            }

            if (!(event.packet instanceof SDisconnectLoginPacket packet)) {
                if (!(event.packet instanceof SDisconnectPacket packet)) {
                    if (event.packet instanceof SLoginSuccessPacket) {
                        long currentTimeMillis = System.currentTimeMillis();
                        if (this.mc.getCurrentServerData() == null) {
                            return;
                        }

                        Ban ban = new Ban(this.mc.getCurrentServerData().serverIP, new Date(currentTimeMillis));
                        Account account = Client.getInstance().accountManager.containsAccount();
                        if (account != null) {
                            account.registerBan(ban);
                            Client.getInstance().accountManager.updateAccount(account);
                            Client.getInstance().accountManager.saveAlts();
                        }
                    }
                } else {
                    long banDur = this.calculateBanDuration(packet.getReason().getString());
                    if (banDur == 0L) {
                        return;
                    }

                    Ban ban = new Ban(this.mc.getCurrentServerData().serverIP, new Date(banDur));
                    Account account = Client.getInstance().accountManager.containsAccount();
                    if (account != null) {
                        account.registerBan(ban);
                        Client.getInstance().accountManager.updateAccount(account);
                        Client.getInstance().accountManager.saveAlts();
                    }
                }
            } else {
                long banDur = this.calculateBanDuration(packet.getReason().getString());
                if (banDur == 0L) {
                    return;
                }

                Ban ban = new Ban(this.mc.getCurrentServerData().serverIP, new Date(banDur));
                Account account = Client.getInstance().accountManager.containsAccount();
                if (account != null) {
                    account.registerBan(ban);
                    Client.getInstance().accountManager.updateAccount(account);
                    Client.getInstance().accountManager.saveAlts();
                }
            }
        }
    }

    private long calculateBanDuration(String message) {
        message = message.toLowerCase();

        // Special cases with predefined return values
        if (message.contains("security") && message.contains("alert")) {
            return 9223372036854775806L;
        }

        if (message.contains("permanent") ||
                message.contains("your account has been suspended from") ||
                message.contains("tu cuenta ha sido suspendida. al reconectarte, tendr") ||
                message.contains("gebannt")) {
            return Long.MAX_VALUE;
        }

        if (message.contains("compromised")) {
            return 9223372036854775806L;
        }

        // Calculate duration in milliseconds
        long days = TimeUnit.DAYS.toMillis(this.extractDays(message));
        long hours = TimeUnit.HOURS.toMillis(this.extractHours(message));
        long minutes = TimeUnit.MINUTES.toMillis(this.extractMinutes(message));
        long seconds = TimeUnit.SECONDS.toMillis(this.extractSeconds(message));

        // Special case for a specific French message
        if (message.contains("vous avez été banni") && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return Long.MAX_VALUE;
        }

        // Return the calculated ban expiration time
        return System.currentTimeMillis() + days + hours + minutes + seconds;
    }

    private int extractDays(String var1) {
        String[] dayKeywords = new String[]{"day", "jour", "tage", "día", "dia"};

        for (String keyword : dayKeywords) {
            Pattern pattern = Pattern.compile("([0-9]+)(?:d| " + keyword + "s|" + keyword + "s| " + keyword + "|" + keyword + ")[ |\\n]");
            Matcher matcher = pattern.matcher(var1);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return 0;
    }

    private int extractHours(String input) {
        String[] hourKeywords = new String[]{"hour", "heure", "uhr", "hora"};

        for (String keyword : hourKeywords) {
            Pattern pattern = Pattern.compile("([0-9]+)(?:h| " + keyword + "s|" + keyword + "s| " + keyword + "|" + keyword + ")[ |\\n]");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return 0;
    }

    private int extractMinutes(String input) {
        String[] minuteKeywords = new String[]{"minute", "min", "minuto", "mínuto"};

        for (String keyword : minuteKeywords) {
            Pattern pattern = Pattern.compile("([0-9]+)(?:m| " + keyword + "s|" + keyword + "s| " + keyword + "|" + keyword + ")[ |\\n]");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return 0;
    }

    private int extractSeconds(String input) {
        String[] secondKeywords = new String[]{"second", "sec", "seconde", "sekunde", "segundo"};

        for (String keyword : secondKeywords) {
            Pattern pattern = Pattern.compile("([0-9]+)(?:s| " + keyword + "s|" + keyword + "s| " + keyword + "|" + keyword + ")[ |\\n]");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return 0;
    }
}
