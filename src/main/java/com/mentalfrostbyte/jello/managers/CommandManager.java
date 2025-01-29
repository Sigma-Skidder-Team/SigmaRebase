package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.command.impl.*;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandArguments;
import com.mentalfrostbyte.jello.command.Command;
import com.mentalfrostbyte.jello.managers.util.command.CommandException;
import com.mentalfrostbyte.jello.util.MinecraftUtil;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    public static final String CHAT_COMMAND_CHAR = ".";
    public static final String CHAT_PREFIX = "§f[§6Sigma§f]§7";
    private static final List<Runnable> runnableList = new ArrayList<>();
    public List<Command> commands = new ArrayList<>();
    private boolean field38298 = true;

    public static void runRunnable(Runnable runnable) {
        runnableList.add(runnable);
    }

    public void init() {
        EventBus.register(this);
        this.register(new VClip());
        this.register(new HClip());
        this.register(new Damage());
        this.register(new ClearChat());
        this.register(new EntityDesync());
        this.register(new Peek());
        this.register(new Insult());
        this.register(new Bind());
        this.register(new Help());
        this.register(new Friend());
        this.register(new Enemy());
        this.register(new Toggle());
        this.register(new Config());
        this.register(new Panic());
        this.register(new HighDPI());
        this.register(new KillPotion());
        this.register(new Enchant());
        this.register(new TP());
    }

    public Command getCommandByName(String var1) {
        for (Command var5 : this.commands) {
            if (var5.getName().equals(var1)) {
                return var5;
            }
        }

        for (Command command : this.commands) {
            for (String var9 : command.getAlias()) {
                if (var9.equals(var1)) {
                    return command;
                }
            }
        }

        return null;
    }

    public List<Command> getCommands() {
        return this.commands;
    }

    private void register(Command command) {
        this.commands.add(command);
    }

    public void invalidCommandMessage(String name) {
        MinecraftUtil.addChatMessage(this.getPrefix() + " Invalid command \"" + "." + name + "\"");
        MinecraftUtil.addChatMessage(this.getPrefix() + " Use \"" + "." + "help\" for a list of commands.");
    }

    public String getPrefix() {
        if (this.field38298) {
            this.field38298 = false;
            return "§f[§6Sigma§f]§7";
        } else {
            StringBuilder var3 = new StringBuilder();

            for (int var4 = 0; var4 < 8; var4++) {
                var3.append(" ");
            }

            return var3 + "§7";
        }
    }

    public void method30236() {
        this.field38298 = true;
    }

    @EventTarget
    public void method30237(EventPlayerTick var1) {
        for (Runnable var5 : runnableList) {
            var5.run();
        }

        runnableList.clear();
    }

    @EventTarget
    public void onSendPacket(EventSendPacket var1) {
        if (Client.getInstance().clientMode != ClientMode.NOADDONS) {
            if (var1.getPacket() instanceof CChatMessagePacket) {
                CChatMessagePacket var4 = (CChatMessagePacket) var1.getPacket();
                String var5 = var4.getMessage();
                if (var5.startsWith(".") && var5.substring(1).startsWith(".")) {
                    var4.message = var5.substring(1);
                    return;
                }

                if (var5.startsWith(".")) {
                    var1.setCancelled(true);
                    this.method30236();
                    String[] var6 = var5.substring(".".length()).split(" ");
                    Command var7 = this.getCommandByName(var6[0]);
                    if (var7 == null) {
                        this.invalidCommandMessage(var6[0]);
                        return;
                    }

                    ArrayList var8 = new ArrayList();

                    for (int var9 = 1; var9 < var6.length; var9++) {
                        var8.add(new ChatCommandArguments(var6[var9]));
                    }

                    MinecraftUtil.addChatMessage(" ");

                    try {
                        var7.run(var5, (ChatCommandArguments[]) var8.<ChatCommandArguments>toArray(new ChatCommandArguments[0]), var1x -> MinecraftUtil.addChatMessage(this.getPrefix() + " " + var1x));
                    } catch (CommandException exception) {
                        if (!exception.reason.isEmpty()) {
                            MinecraftUtil.addChatMessage(this.getPrefix() + " Error: " + exception.reason);
                        }

                        MinecraftUtil.addChatMessage(this.getPrefix() + " Usage: " + "." + var7.getName() + " " + var7.getOptions());
                    }

                    MinecraftUtil.addChatMessage(" ");
                }
            }

            if (var1.getPacket() instanceof CTabCompletePacket var11) {
                if (var11.getCommand().startsWith(".")) {
                    var1.setCancelled(true);
                }
            }
        }
    }
}