package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.client.CChatMessagePacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class ChatFilter extends Module {
    public ChatFilter() {
        super(ModuleCategory.MISC, "ChatFilter", "Bypasse chat filters");
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof CChatMessagePacket chatPacket) {
                String[] words = chatPacket.message.split(" ");
                if (chatPacket.message.length() + words.length <= 100) {
                    StringBuilder modifiedMessage = new StringBuilder();
                    boolean isCommand = false;

                    for (String word : words) {
                        if (!word.startsWith("/")) {
                            if (modifiedMessage.length() != 0) {
                                modifiedMessage.append(" ");
                            }

                            String firstChar = word.substring(0, 1);
                            String remainingChars = word.substring(1);
                            modifiedMessage.append(firstChar).append("\uf8ff").append(remainingChars);
                        } else {
                            modifiedMessage.append(word);
                            isCommand = !word.equals("/r") && !word.equals("/msg");
                        }
                    }

                    if (!isCommand) {
                        chatPacket.message = modifiedMessage.toString();
                    }
                }
            }
        }
    }
}
