package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.ReceivePacketEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class BasicAntiKB extends Module {
   public BasicAntiKB() {
      super(ModuleCategory.COMBAT, "Basic", "Places block underneath");
      this.registerSetting(new BooleanSetting("Explosions", "Cancels Explosions knockback", true));
      this.registerSetting(new NumberSetting<Float>("H-Multiplier", "Horizontal velocity multiplier", 0.0F, Float.class, 0.0F, 1.0F, 0.01F));
      this.registerSetting(new NumberSetting<Float>("V-Multiplier", "Vertical velocity multiplier", 0.0F, Float.class, 0.0F, 1.0F, 0.01F));
   }

   @EventTarget
   public void method16068(ReceivePacketEvent event) {
      if (this.isEnabled()) {
         if (mc.player != null && event.getPacket() instanceof SEntityVelocityPacket) {
            SEntityVelocityPacket packet = (SEntityVelocityPacket)event.getPacket();
            if (packet.getEntityID() == mc.player.getEntityId()) {
               if (this.getNumberValueBySettingName("H-Multiplier") == 0.0F && this.getNumberValueBySettingName("V-Multiplier") == 0.0F) {
                  event.cancelled = true;
               }

               packet.motionX = (int)((float)packet.motionX * this.getNumberValueBySettingName("H-Multiplier"));
               packet.motionZ = (int)((float)packet.motionZ * this.getNumberValueBySettingName("H-Multiplier"));
               packet.motionY = (int)((float)packet.motionY * this.getNumberValueBySettingName("V-Multiplier"));
            }
         }

         if (event.getPacket() instanceof SExplosionPacket && this.getBooleanValueFromSettingName("Explosions")) {
            SExplosionPacket packet = (SExplosionPacket)event.getPacket();
            packet.motionX = packet.motionX * this.getNumberValueBySettingName("H-Multiplier");
            packet.motionZ = packet.motionZ * this.getNumberValueBySettingName("H-Multiplier");
            packet.motionY = packet.motionY * this.getNumberValueBySettingName("V-Multiplier");
         }
      }
   }
}
