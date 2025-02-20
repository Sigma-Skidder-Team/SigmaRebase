package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import net.minecraft.network.play.client.CChatMessagePacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class ChatFilter extends Module {
    private final BooleanSetting miniblox;
    public ChatFilter() {
        super(ModuleCategory.MISC, "ChatFilter", "Bypasses chat filters");
        registerSetting(
                this.miniblox = new BooleanSetting("Miniblox mode", "\\ bypass mode", false)
        );
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof CChatMessagePacket chatPacket) {
                String[] words = chatPacket.message.split(" ");
                if (chatPacket.message.length() + words.length <= 100) {
                    StringBuilder modifiedMessage = new StringBuilder();
                    boolean isCommand = false;

                    for (String word : words) {
                        if (!word.startsWith("/")) {
                            if (!modifiedMessage.isEmpty()) {
                                modifiedMessage.append(" ");
                            }

                            String firstChar = word.substring(0, 1);
                            String remainingChars = word.substring(1);
                            // `\`s are used as color escape sequences,
                            // an example of a color escape sequence is `\\red\\`
                            modifiedMessage.append(firstChar).append(miniblox.currentValue ? "\\" : "\uf8ff").append(remainingChars);
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
