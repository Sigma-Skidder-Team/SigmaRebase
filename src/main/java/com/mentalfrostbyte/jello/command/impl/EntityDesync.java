package com.mentalfrostbyte.jello.command.impl;

import com.mentalfrostbyte.jello.command.Command;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandArguments;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandExecutor;
import com.mentalfrostbyte.jello.managers.util.command.CommandException;
import com.mentalfrostbyte.jello.managers.util.command.CommandType;
import net.minecraft.entity.Entity;


public class EntityDesync extends Command {
    private Entity entityToRide = null;

    public EntityDesync() {
        super("entitydesync", "Forces a client side entity dismount", "vanish", "riderdesync");
        this.registerSubCommands("remount/dismount");
    }

    @Override
    public void run(String var1, ChatCommandArguments[] args, ChatCommandExecutor executor) throws CommandException {
        if (args.length != 0) {
            if (args.length <= 1) {
                if (args[0].getCommandType() != CommandType.TEXT) {
                    throw new CommandException();
                } else {
                    if (!args[0].getArguments().startsWith("d")) {
                        if (!args[0].getArguments().startsWith("m") && !args[0].getArguments().startsWith("r")) {
                            throw new CommandException();
                        }

                        if (this.entityToRide == null) {
                            throw new CommandException("No entity to remount");
                        }

                        mc.player.startRiding(this.entityToRide);
                        this.entityToRide.addedToChunk = true;
                        executor.send("Remounted entity " + this.entityToRide.getType().getName().getUnformattedComponentText());
                        this.entityToRide = null;
                    } else {
                        Entity var6 = mc.player.getRidingEntity();
                        if (var6 == null) {
                            throw new CommandException("You must be riding an entity to use this command");
                        }

                        this.entityToRide = mc.player.getRidingEntity();
                        this.entityToRide.addedToChunk = true;
                        mc.player.stopRiding();
                        executor.send("Dismounted entity " + this.entityToRide.getType().getName().getUnformattedComponentText());
                    }
                }
            } else {
                throw new CommandException("Too many arguments");
            }
        } else {
            throw new CommandException();
        }
    }
}