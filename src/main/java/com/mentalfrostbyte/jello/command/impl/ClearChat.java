package com.mentalfrostbyte.jello.command.impl;

import com.mentalfrostbyte.jello.command.Command;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandArguments;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandExecutor;
import com.mentalfrostbyte.jello.managers.util.command.CommandException;

public class ClearChat extends Command {
    public ClearChat() {
        super("clearchat", "Clears your chat client side", "cc", "chatclear");
    }

    @Override
    public void run(String var1, ChatCommandArguments[] args, ChatCommandExecutor executor) throws CommandException {
        if (args.length == 0) {
            mc.ingameGUI.getChatGUI().clearChatMessages(true);
        } else {
            throw new CommandException("Too many arguments");
        }
    }
}
