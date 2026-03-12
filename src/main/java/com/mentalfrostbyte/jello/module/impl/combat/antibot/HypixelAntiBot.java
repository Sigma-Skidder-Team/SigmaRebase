package com.mentalfrostbyte.jello.module.impl.combat.antibot;

import com.mentalfrostbyte.jello.managers.util.combat.AntiBotBase;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class HypixelAntiBot extends AntiBotBase {
    public HypixelAntiBot() {
        super("Hypixel", "Detects bots on Hypixel based on entity names");
    }

    @Override
    public boolean isBot(Entity entity) {
        if (entity == null) {
            return false;
        }

        String displayName = entity.getDisplayName().getString();
        String entityName = entity.getName().getString();

        String customName =
                entity.getCustomName() != null
                        ? entity.getDisplayName().getString()
                        : null;

        if (entity.isInvisible() && !displayName.startsWith("§c") && displayName.endsWith("§r") && (customName == null || customName.equals(entityName))) {
            double x = Math.abs(entity.getPosX() - mc.player.getPosX());
            double y = Math.abs(entity.getPosY() - mc.player.getPosY());
            double z = Math.abs(entity.getPosZ() - mc.player.getPosZ());
            double dist = Math.hypot(x, z);
            if (y < 13.0 && y > 10.0 && dist < 3.0) {
                return true;
            }
        }

        if (!displayName.startsWith("§") && displayName.endsWith("§r")) {
            return true;
        } else if (entity.isInvisible() && entityName.equals(displayName) && Objects.equals(customName, entityName + "§r")) {
            return true;
        } else if (customName != null && !customName.equalsIgnoreCase("") && displayName.toLowerCase().contains("§c") && displayName.toLowerCase().contains("§r")) {
            return true;
        } else {
            return displayName.contains("§8[NPC]") || !displayName.contains("§c") && customName != null && !customName.equalsIgnoreCase("");
        }
    }

    @Override
    public boolean isNotBot(Entity entity) {
        return true;
    }
}