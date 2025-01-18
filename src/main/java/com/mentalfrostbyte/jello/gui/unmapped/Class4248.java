package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class Class4248 extends UIBase {
    public final Setting setting;

   public Class4248(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Setting var7) {
      super(var1, var2, var3, var4, var5, var6, false);
      this.setting = var7;
   }

   @Override
   public void draw(float var1) {
      RenderUtil.drawRoundedRect(
         (float)this.getXA(),
         (float)this.getYA(),
         (float)(this.getXA() + this.getWidthA()),
         (float)(this.getYA() + this.getHeightA()),
         -3618616
      );
      RenderUtil.drawString(Resources.regular17, (float)(this.getXA() + 5), (float)(this.getYA() - 2), this.setting.getDescription(), -14540254);
   }
}
