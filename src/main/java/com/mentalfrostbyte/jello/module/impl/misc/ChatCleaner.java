package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.server.SChatPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class ChatCleaner extends Module {
    public ChatCleaner() {
        super(ModuleCategory.MISC, "ChatCleaner", "Cleans chat in atempt to avoid spam on anarchy servers");
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (isEnabled()) {
            if (event.getPacket() instanceof SChatPacket packet) {
                if (isSpam(cleanChatMessage(packet.getChatComponent().getString()))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public String cleanChatMessage(String message) {
        if (!message.startsWith("<")) {
            message = message.replaceAll("^(.*?): ", "");
        } else {
            message = message.replaceAll("^(.*?)> ", "");
        }

        return message.toLowerCase();
    }

    public boolean isSpam(String input) {
        String[] spamKeywords = {
                "> ", "http://", "https://", "discord.gg", "www.", "ʳᵘˢʰᵉʳʰᵃᶜᵏ", "♿", "/ignore", "#TeamRusher",
                "Default Message", "wwe", "future", "iknowimez", "lol get gud"
        };

        String[] spamPhrases = {
                ": [", "] [", "!", "TPS: ", "Hey, ", "Hello, ", "Farewell, ", "Howdy, ", "Good evening, ",
                "Good bye, ", "Bye, ", "Later, ", "See you next time, ", "See you later, ",
                "Welcome to 2b2t.org, ", "Greetings, ", "Catch ya later, ", "Good to see you, ",
                "Hope you had a good time, ", "Aww, it's you ", "Well, It was nice to have you here, ",
                "Bye, Bye ", "I just "
        };

        for (String keyword : spamKeywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }

        for (String phrase : spamPhrases) {
            if (input.startsWith(phrase) && input.endsWith(".")) {
                return true;
            }
        }

        return false;
    }
}
