package com.mentalfrostbyte.jello.module.impl.combat.aimbot;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.Aimbot;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import com.mentalfrostbyte.jello.util.game.player.rotation.util.RotationUtils;
import net.minecraft.entity.Entity;
import team.sdhq.eventBus.annotations.EventTarget;

public class BasicAimbot extends Module {
   public BasicAimbot() {
      super(ModuleCategory.COMBAT, "Basic", "Automatically aims at players");
      this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F, 2.8F, 8.0F, 0.01F));
   }

   @EventTarget
   public void Render3DEvent(EventRender3D event) {
      if (this.isEnabled()) {
         Entity target = ((Aimbot)this.getParent()).getTarget(this.getNumberValueBySettingName("Range"));
         if (target != null) {
            Rotation rotation = RotationUtils.getAdvancedRotation(target, false);
            mc.player.rotationYaw = rotation.yaw;
            mc.player.rotationPitch = rotation.pitch;
         }
      }
   }
}
