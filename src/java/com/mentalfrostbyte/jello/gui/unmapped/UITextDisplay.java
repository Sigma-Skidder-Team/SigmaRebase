package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.newdawn.slick.TrueTypeFont;
import org.lwjgl.opengl.GL11;

public class UITextDisplay extends AnimatedIconPanelWrap {
   private static String[] field20736;
   public static ColorHelper field20778 = new ColorHelper(
      ClientColors.DEEP_TEAL.getColor(),
      ClientColors.DEEP_TEAL.getColor(),
      ClientColors.DEEP_TEAL.getColor(),
      ClientColors.DEEP_TEAL.getColor(),
      Class2218.field14488,
      Class2218.field14492
   );
   public boolean field20779 = false;

   public UITextDisplay(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, false);
   }

   public UITextDisplay(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var9, false);
   }

   @Override
   public void draw(float var1) {
      if (this.field20779) {
         GL11.glAlphaFunc(518, 0.01F);
         RenderUtil.drawString(
            ResourceRegistry.JelloLightFont18_1,
            (float)this.getXA(),
            (float)this.getYA(),
            this.getTypedText(),
            ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var1)
         );
         GL11.glAlphaFunc(519, 0.0F);
      }

      if (this.typedText != null) {
         RenderUtil.drawString(
            this.getFont(),
            (float)this.getXA(),
            (float)this.getYA(),
            this.getTypedText(),
            ColorUtils.applyAlpha(this.textColor.getTextColor(), var1 * ColorUtils.method17710(this.textColor.getTextColor()))
         );
      }
   }
}
