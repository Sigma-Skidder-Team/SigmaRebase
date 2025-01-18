package com.mentalfrostbyte.jello.gui.unmapped;


import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class Class4302 extends AnimatedIconPanelWrap {
   private static String[] field20844;

   public Class4302(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
      super(var1, var2, var3, var4, var5, var6, false);
   }

   @Override
   public void draw(float var1) {
      if (this.isVisible()) {
         RenderUtil.drawImage(
            (float)(this.xA + 30),
            (float)(this.yA + 30),
            187.0F,
            36.0F,
            Resources.gemPNG,
            ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
         );
      }
   }
}
