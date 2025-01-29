package com.mentalfrostbyte.jello.command.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.command.Command;
import com.mentalfrostbyte.jello.managers.util.command.*;

import java.util.List;

public class Help extends Command {
    public Help() {
        super("help", "Show this help dialog", "vc");
        this.registerSubCommands("page/command");
    }

    @Override
    public void run(String var1, ChatCommandArguments[] var2, ChatCommandExecutor var3) throws CommandException {
        List var6 = Client.getInstance().commandManager.getCommands();
        int var7 = (int) Math.ceil((double) ((float) var6.size() / 7.0F));
        int var8 = var2.length == 1 && var2[0].getCommandType() == CommandType.NUMBER ? var2[0].getInt() - 1 : 0;
        if (var2.length == 1 && var2[0].getCommandType() == CommandType.TEXT) {
            Command command = Client.getInstance().commandManager.getCommandByName(var2[0].getArguments());
            if (command == null) {
                throw new CommandException();
            } else {
                var3.send("§f" + command.getName() + "§8" + " > " + "§7" + command.getDescription());
                if (command.getOptions().length() <= 0) {
                    var3.send("   [no options]");
                } else {
                    var3.send("   " + command.getOptions());
                }
            }
        } else if (var2.length <= 1) {
            if (var8 + 1 <= var7 && var8 >= 0) {
                var3.send("§fHelp:§7 Page " + (var8 + 1) + "/" + var7);
                var3.send("");

                for (int var9 = 0; var9 < 7; var9++) {
                    int var10 = var9 + var8 * 7;
                    if (var6.size() > var10) {
                        Command var11 = (Command) var6.get(var10);
                        var3.send("§f" + var11.getName() + "§8" + " > " + "§7" + var11.getDescription());
                        if (var11.getOptions().isEmpty()) {
                            var3.send("   [no options]");
                        } else {
                            var3.send("   " + var11.getOptions());
                        }
                    }
                }
            } else {
                throw new CommandException("Page " + var8 + " does not exist!");
            }
        } else {
            throw new CommandException();
        }
    }
}