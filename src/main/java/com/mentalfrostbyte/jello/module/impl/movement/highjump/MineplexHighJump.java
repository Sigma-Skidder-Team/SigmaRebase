package com.mentalfrostbyte.jello.module.impl.movement.highjump;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;

import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.AxisAlignedBB;
import team.sdhq.eventBus.annotations.EventTarget;

public class MineplexHighJump extends Module {
   private boolean field24023;
   private double field24024;
   private double field24025;
   private double field24026;

   public MineplexHighJump() {
      super(ModuleCategory.MOVEMENT, "Mineplex", "Highjump for Mineplex");
      this.registerSetting(new NumberSetting<Float>("Motion", "Highjump motion", 1.1F, Float.class, 0.42F, 5.0F, 0.05F));
      this.registerSetting(new BooleanSetting("Disable", "Disable on landing.", true));
      this.registerSetting(new BooleanSetting("Fake fly", "Am i flying ?", false));
   }

   @Override
   public void onEnable() {
      this.field24023 = false;
   }

   @EventTarget
   public void method16970(EventSafeWalk var1) {
      if (this.isEnabled() && mc.player.isOnGround()) {
         var1.setSafe(true);
      }
   }

   @EventTarget
   public void method16971(EventMove var1) {
      if (this.isEnabled()) {
         if (this.field24023 && mc.player.getPosY() + 0.42 < this.field24026) {
            this.access().toggle();
         }

         if (!mc.player.isOnGround() && this.field24023) {
            this.field24024 = Math.max(this.field24024, 0.499);
            MovementUtil.setMotion(var1, this.field24024);
            this.field24024 -= 0.007;
            double var4 = 0.5;
            if (this.getNumberValueBySettingName("Motion") > 3.0F) {
               var4 -= 0.8;
            }

            if (Math.abs(var1.getY()) < var4 && Math.abs(this.field24025) < var4) {
               this.field24025 -= 0.04000000000001;
               var1.setY(this.field24025);
               mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
            } else {
               this.field24025 = var1.getY();
            }
         }
      }
   }

   @EventTarget
   public void method16972(EventPlayerTick var1) {
      if (this.isEnabled()) {
         if (mc.player.isOnGround()) {
            if (this.field24023) {
               this.field24023 = !this.field24023;
               MovementUtil.moveInDirection(0.0);
               if (this.getBooleanValueFromSettingName("Disable")) {
                  this.access().toggle();
               }

               return;
            }

            double var4 = mc.player.getPosX();
            double var6 = mc.player.getPosZ();
            double var8 = mc.player.getPosY();
            double var10 = (double) mc.player.movementInput.moveForward;
            double var12 = (double) mc.player.movementInput.moveForward;
            float var14 = mc.player.rotationYaw;
            double var15 = 0.1;
            double var17 = var4
               + (var10 * 0.45 * Math.cos(Math.toRadians((double)(var14 + 90.0F))) + var12 * 0.45 * Math.sin(Math.toRadians((double)(var14 + 90.0F)))) * var15;
            double var19 = var6
               + (var10 * 0.45 * Math.sin(Math.toRadians((double)(var14 + 90.0F))) - var12 * 0.45 * Math.cos(Math.toRadians((double)(var14 + 90.0F)))) * var15;
            AxisAlignedBB var21 = new AxisAlignedBB(var17 - 0.3, var8 - 1.0, var19 - 0.3, var17 + 0.3, var8 + 2.0, var19 + 0.3);
            if (mc.world.getCollisionShapes(mc.player, var21).count() == 0L) {
               double var22 = this.method16975(var21);
               if (var22 != 11.0) {
                  double var24 = mc.player.getPosY();
                  var19 = 312.7;
                  CPlayerPacket.PositionPacket var26 = new CPlayerPacket.PositionPacket(var17, var24 - var22, var19, true);
                  CPlayerPacket.PositionPacket var27 = new CPlayerPacket.PositionPacket(var17, var24, var19, true);
                  mc.getConnection().sendPacket(var27);
                  mc.getConnection().sendPacket(var26);
                  this.field24026 = var24 + 0.42;
                  mc.player.setPosition(var17, var24, var19);
                  this.field24025 = (double)this.getNumberValueBySettingName("Motion");
                  this.field24024 = 0.81;
               }
            }
         }
      }
   }

   @EventTarget
   public void method16973(EventReceivePacket var1) {
      if (this.isEnabled()) {
         IPacket var4 = var1.packet;
         if (var4 instanceof SPlayerPositionLookPacket) {
            this.access().toggle();
         }
      }
   }

   @EventTarget
   public void method16974(EventRender2D var1) {
      if (this.isEnabled() && this.field24023 && !(mc.player.getPosY() < this.field24026) && this.getBooleanValueFromSettingName("Fake fly")) {
         mc.player.getPositionVec().y = this.field24026;
         mc.player.lastTickPosY = this.field24026;
         mc.player.chasingPosY = this.field24026;
         mc.player.prevPosY = this.field24026;
         if (MovementUtil.isMoving()) {
            mc.player.cameraYaw = 0.099999994F;
         }
      }
   }

   public double method16975(AxisAlignedBB var1) {
      double var4 = 6.0;
      AxisAlignedBB var6 = var1.offset(0.0, -var4, 0.0);

      do {
         var6 = var1.offset(0.0, -var4, 0.0);
         if (mc.world.getCollisionShapes(mc.player, var6).count() == 0L) {
            return var4;
         }

         var4 -= 0.5;
      } while (mc.world.getCollisionShapes(mc.player, var6).count() != 0L && var4 > 3.0);

      return 11.0;
   }
}
