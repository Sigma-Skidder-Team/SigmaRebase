package com.mentalfrostbyte.jello.module.impl.combat.aimbot;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.Aimbot;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.player.RotationHelper;
import net.minecraft.entity.Entity;
import team.sdhq.eventBus.annotations.EventTarget;

public class BasicAimbot extends Module {
   public BasicAimbot() {
      super(ModuleCategory.COMBAT, "Basic", "Automatically aims at players");
      this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F, Float.class, 2.8F, 8.0F, 0.01F));
   }

   @EventTarget
   public void Render3DEvent(EventRender3D event) {
      if (this.isEnabled()) {
         Entity range = ((Aimbot)this.access()).getTarget(this.getNumberValueBySettingName("Range"));
         if (range != null) {
            float[] rotation = RotationHelper.doBasicRotation(range);
            mc.player.rotationYaw = rotation[0];
            mc.player.rotationPitch = rotation[1];
         }
      }
   }
}
