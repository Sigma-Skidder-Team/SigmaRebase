package com.mentalfrostbyte.jello.module.impl.combat.antibot;

import com.mentalfrostbyte.jello.managers.util.combat.BotRecognitionTechnique;
import com.mentalfrostbyte.jello.managers.util.combat.AntiBotBase;
import net.minecraft.entity.Entity;
import java.util.Objects;

public class HypixelAntiBot extends AntiBotBase {
   public HypixelAntiBot() {
      super("Hypixel", "Detects bots on Hypixel based on entity names", BotRecognitionTechnique.SERVER);
   }

   @Override
   public boolean isBot(Entity entity) {
      if (entity != null) {
         String displayName = entity.getDisplayName().getString();
         String customName = entity.getCustomName() != null ? entity.getDisplayName().getString() : null;
         String name = entity.getName().getString();
         if (entity.isInvisible() && !displayName.startsWith("§c") && displayName.endsWith("§r") && (customName == null || customName.equals(name))) {
             assert mc.player != null;
             double var7 = Math.abs(entity.getPosX() - mc.player.getPosX());
            double var9 = Math.abs(entity.getPosY() - mc.player.getPosY());
            double var11 = Math.abs(entity.getPosZ() - mc.player.getPosZ());
            double var13 = Math.sqrt(var7 * var7 + var11 * var11);
            if (var9 < 13.0 && var9 > 10.0 && var13 < 3.0) {
               return true;
            }
         }

         if (!displayName.startsWith("§") && displayName.endsWith("§r")) {
            return true;
         } else if (entity.isInvisible() && name.equals(displayName) && Objects.equals(customName, name + "§r")) {
            return true;
         } else if (customName != null && !customName.equalsIgnoreCase("") && displayName.toLowerCase().contains("§c") && displayName.toLowerCase().contains("§r")) {
            return true;
         } else {
            return displayName.contains("§8[NPC]") || !displayName.contains("§c") && customName != null && !customName.equalsIgnoreCase("");
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isNotBot(Entity entity) {
      return true;
   }
}
