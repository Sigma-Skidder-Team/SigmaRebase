package com.mentalfrostbyte.jello.command.impl;

import com.mentalfrostbyte.jello.command.Command;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandArguments;
import com.mentalfrostbyte.jello.managers.util.command.ChatCommandExecutor;
import com.mentalfrostbyte.jello.managers.util.command.CommandException;
import com.mentalfrostbyte.jello.managers.util.command.CommandType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;


public class Enchant extends Command {
    public Enchant() {
        super("enchant", "Enchants items in creative mode");
    }

    @Override
    public void run(String var1, ChatCommandArguments[] var2, ChatCommandExecutor var3) throws CommandException {
        if (var2.length == 2) {
            if (var2[1].getCommandType() == CommandType.NUMBER) {
                if (!mc.playerController.isNotCreative()) {
                    Enchantment var6 = null;
                    ItemStack item = mc.player.getHeldItemMainhand().copy();

                    for (ResourceLocation var9 : Registry.ENCHANTMENT.keySet()) {
                        if (var2[0].getArguments().equals(var9.getPath())) {
                            var6 = Registry.ENCHANTMENT.getOrDefault(var9);
                        }
                    }

                    if (var6 != null) {
                        item.addEnchantment(var6, var2[1].getInt());
                        mc.getConnection().sendPacket(new CCreativeInventoryActionPacket(36 + mc.player.inventory.currentItem, item));
                        var3.send("Requested server to apply " + var2[0].getArguments() + " " + var2[1].getInt());
                    } else {
                        throw new CommandException("Unknown enchant '" + var2[0].getArguments() + "'");
                    }
                } else {
                    throw new CommandException("Creative mode only!");
                }
            } else {
                throw new CommandException("Enter a valid enchant value");
            }
        } else {
            throw new CommandException("Too many arguments");
        }
    }
}