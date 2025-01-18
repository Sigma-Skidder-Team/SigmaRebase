package com.mentalfrostbyte.jello.module.impl.combat.wtap;

import com.mentalfrostbyte.jello.event.impl.EventRayTraceResult;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.play.client.CEntityActionPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class NormalWTap extends Module {
   public NormalWTap() {
      super(ModuleCategory.COMBAT, "Normal", "Increase the knockback you give to players");
   }

   @EventTarget
   @HigherPriority
   private void EventRayTraceResult(EventRayTraceResult event) {
      if (this.isEnabled() && event.isHovering()) {
         mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
      }
   }
}
