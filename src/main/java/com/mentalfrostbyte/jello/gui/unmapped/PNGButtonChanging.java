package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.managers.impl.music.Class189;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class PNGButtonChanging extends UIBase {
   public Class189 field20607;

   public PNGButtonChanging(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Class189 var7) {
      super(var1, var2, var3, var4, var5, var6, false);
      this.field20607 = var7;
      this.doThis((var1x, var2x) -> {
         this.field20607 = this.field20607.method577();
         this.callUIHandlers();
      });
   }

   public Class189 method13038() {
      return this.field20607;
   }

   @Override
   public void draw(float var1) {
      RenderUtil.startScissor((float)this.xA, (float)this.yA, (float)this.widthA, (float)this.heightA);
      RenderUtil.drawImage(
         (float)(this.xA - this.field20607.field719 * this.widthA),
         (float)this.yA,
         (float)(this.widthA * 3),
         (float)this.heightA,
         Resources.repeatPNG,
         ColorUtils.applyAlpha(  ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.35F)
      );
      RenderUtil.endScissor();
      super.draw(var1);
   }
}
