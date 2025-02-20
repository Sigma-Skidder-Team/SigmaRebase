package com.mentalfrostbyte.jello.module.impl.combat.antibot;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SEntityPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import com.mentalfrostbyte.jello.managers.util.combat.BotRecognitionTechnique;
import com.mentalfrostbyte.jello.managers.util.combat.AntiBotBase;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MovementAntiBot extends AntiBotBase {
   public List<Integer> field31115 = new CopyOnWriteArrayList<Integer>();
   public HashMap<Entity, Integer> field31116 = new HashMap<Entity, Integer>();
   public HashMap<Entity, ArrayList<Integer>> field31117 = new HashMap<Entity, ArrayList<Integer>>();
   public int field31118 = 30;

   public MovementAntiBot() {
      super("Movement", "Detects bots based on movement patterns", BotRecognitionTechnique.PATTERNS);
   }

   @Override
   public boolean isBot(Entity entity) {
      return this.field31116.getOrDefault(entity, 0) < this.field31118;
   }

   @EventTarget
   public void onTick(EventPlayerTick var1) {
       assert mc.player != null;
       if (mc.player.ticksExisted < 10) {
         this.field31116.clear();
      }

      for (PlayerEntity var5 : CombatUtil.getPlayers()) {
         if (var5 != mc.player) {
            if (var5 == null
               || !BlockUtil.isAboveBounds(var5, 0.01F)
               || var5.isInvisible()
               || !(var5.getDistance(mc.player) > 5.0F)
                  && (var5.getPosX() != var5.lastTickPosX || var5.getPosZ() != var5.lastTickPosZ || var5.getPosY() != var5.lastTickPosY)) {
               if (this.field31116.getOrDefault(var5, 0) < this.field31118) {
                  this.field31116.put(var5, 0);
               }
            } else {
               this.field31116.put(var5, this.field31116.getOrDefault(var5, 0) + 1);
            }
         }
      }
   }

   @EventTarget
   public void onClientboundPacket(EventReceivePacket var1) {
      if (mc.player != null && this.field31116 != null) {
         if (mc.player.ticksExisted < 10) {
            this.field31116.clear();
         }

         if (var1.packet instanceof SEntityPacket.RelativeMovePacket) {
            SEntityPacket.RelativeMovePacket relativeMovePacket = (SEntityPacket.RelativeMovePacket)var1.packet;
            if (mc.world == null)
               return;

            if (!(relativeMovePacket.getEntity(mc.world) instanceof PlayerEntity)) {
               return;
            }

            Entity var5 = relativeMovePacket.getEntity(mc.world);
            boolean var6 = BlockUtil.isAboveBounds(var5, 0.5F);
            short var7 = relativeMovePacket.getPitch(); // prob wrong will fix
            if (!this.field31117.containsKey(var5)) {
               this.field31117.put(var5, new ArrayList<Integer>());
            }

            ArrayList var8 = this.field31117.get(var5);
            if (var6) {
               if (!var8.isEmpty()) {
                  int var9 = this.method22754(var8);
                  int var10 = this.method22755(var8);
                  if (var10 > 0 && var10 < 1300 && var9 < 3500 && var9 > 2900) {
                     this.field31116.put(var5, this.field31116.getOrDefault(var5, 0) + this.field31118);
                  }
               }

               var8.clear();
               this.field31117.put(var5, var8);
            }

            if (!var8.isEmpty() && (Integer)var8.get(var8.size() - 1) < 0 && var7 > 0) {
               var8.clear();
            }

            if (this.method22754(var8) > 3600) {
               var8.clear();
            }

            if ((var8.isEmpty() || !var6) && var7 != 0) {
               var8.add(Integer.valueOf(var7));
            }

            this.field31117.put(var5, var8);
         }
      }
   }

   public int method22754(ArrayList<Integer> var1) {
      int var4 = 0;

      for (int var6 : var1) {
         var4 += var6 <= 0 ? 0 : var6;
      }

      return var4;
   }

   public int method22755(ArrayList<Integer> var1) {
      int var4 = 0;

      for (int var6 : var1) {
         var4 += var6;
      }

      return var4;
   }

   public boolean method22756(Entity var1) {
      if (!mc.world.getBlockState(var1.getPosition()).isSolid()) {
         AxisAlignedBB var4 = new AxisAlignedBB(
            var1.getBoundingBox().minX,
            var1.getBoundingBox().minY - 0.5,
            var1.getBoundingBox().minZ,
            var1.getBoundingBox().maxX,
            var1.getBoundingBox().maxY,
            var1.getBoundingBox().maxZ
         );

//         for (BlockPos var6 : method22757(var1)) {
//            VoxelShape var7 = field31119.world.getBlockState(var6).getCollisionShape(field31119.world, var6);
//            if (!var7.isEmpty() && var4.intersects(var7.getBoundingBox().withSizeAtOrigin(var6))) {
//               return true;
//            }
//         }
         // will fix later

         return false;
      } else {
         return false;
      }
   }

   public static List<BlockPos> method22757(Entity var0) {
      ArrayList var3 = new ArrayList();
      int var4 = 1;

      for (float var5 = (float)(-var4); var5 <= (float)var4; var5++) {
         for (float var6 = (float)(-var4); var6 <= (float)var4; var6++) {
            BlockPos var7 = new BlockPos(var0.getPosX() + (double)var5, var0.getPosY() - 1.0, var0.getPosZ() + (double)var6);
            var3.add(var7);
         }
      }

      return var3;
   }

   @Override
   public boolean isNotBot(Entity entity) {
      return this.field31116.getOrDefault(entity, 0) >= this.field31118;
   }
}
