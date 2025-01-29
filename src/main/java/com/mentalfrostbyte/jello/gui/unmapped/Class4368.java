package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class Class4368 extends UIBase {
   public Module field21351;

   public Class4368(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Module var7) {
      super(var1, var2, var3, var4, var5, var6, false);
      this.field21351 = var7;
      Class4262 var10;
      this.addToList(var10 = new Class4262(this, "enable", 114, 9, 40, 18));
      var10.method13093(var7.isEnabled());
      var10.onPress(var2x -> var7.setEnabled(var10.method13092()));
      if (var7.getSettingMap().size() > 0) {
         Class4364 var11;
         this.addToList(var11 = new Class4364(this, "gear", 132, 32));
         var11.doThis((var2x, var3x) -> ((Class4335)this.getParent()).method13486(var7));
      }
   }

   @Override
   public void draw(float partialTicks) {
      RenderUtil.drawString(
              Resources.regular17,
         (float)(this.xA + 10),
         (float)(this.yA + 8),
         this.field21351.getSuffix(),
         ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), !this.field21351.isEnabled() ? 0.5F : 0.9F)
      );
      RenderUtil.drawString(
              Resources.regular15,
         (float)(this.xA + 15),
         (float)(this.yA + 33),
         "Bind",
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 1.0F)
      );
      RenderUtil.drawString(
              Resources.regular15,
         (float)(this.xA + 15),
         (float)(this.yA + 52),
              RenderUtil.getKeyName(this.field21351.parseSettingValueToIntBySettingName("Keybind")),
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.7F)
      );
      if (this.field21351.getSettingMap().size() > 1) {
         RenderUtil.drawString(
                 Resources.regular12,
            (float)(this.xA + 84),
            (float)(this.yA + 34),
            "Settings",
                 ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 1.0F)
         );
      }

      super.draw(partialTicks);
   }
}
