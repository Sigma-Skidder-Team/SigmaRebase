package com.mentalfrostbyte.jello.module.impl.combat.antibot;

import com.mentalfrostbyte.jello.managers.impl.combat.Class2124;
import net.minecraft.entity.Entity;

import java.util.HashMap;

import com.mentalfrostbyte.jello.managers.impl.combat.Class7249;

public class HypixelAntiBot extends Class7249 {
   public HashMap<Entity, Boolean> field31124 = new HashMap<Entity, Boolean>();

   public HypixelAntiBot() {
      super("Hypixel", "Detects bots on Hypixel based on entity names", Class2124.field13865);
   }

   @Override
   public boolean method22751(Entity entity) {
      if (entity != null) {
         String displayName = entity.getDisplayName().getString();
         String customName = entity.getCustomName() != null ? entity.getCustomName().getString() : null;
         String name = entity.getName().getString();
         if (entity.isInvisible() && !displayName.startsWith("§c") && displayName.endsWith("§r") && (customName == null || customName.equals(name))) {
            double absX = Math.abs(entity.getPosX() - mc.player.getPosX());
            double absY = Math.abs(entity.getPosY() - mc.player.getPosY());
            double absZ = Math.abs(entity.getPosZ() - mc.player.getPosZ());
            double thing = Math.sqrt(absX * absX + absZ * absZ);
            if (absY < 13.0 && absY > 10.0 && thing < 3.0) {
               return true;
            }
         }

         if (!displayName.startsWith("§") && displayName.endsWith("§r")) {
            return true;
         } else if (entity.isInvisible() && name.equals(displayName) && customName.equals(name + "§r")) {
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
   public boolean method22758(Entity var1) {
      return true;
   }
}
