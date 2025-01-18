package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.newdawn.slick.TrueTypeFont;

public class UIButton extends UIBase {
   private static String[] field20602;
   public Animation field20711 = new Animation(190, 190);

   public UIButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var9, false);
      int var12 = (int)(210.0 * Math.sqrt((double)((float)var5 / 242.0F)));
      this.field20711 = new Animation(var12, var12);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
      this.field20711.changeDirection(!this.method13298() ? Direction.BACKWARDS : Direction.FORWARDS);
   }

   @Override
   public void draw(float var1) {
      if (this.getTypedText() != null) {
         int var4 = this.textColor.method19405();
         int var5 = this.getXA()
            + (
               this.textColor.method19411() != Class2218.field14492
                  ? 0
                  : (this.textColor.method19411() != Class2218.field14490 ? this.getWidthA() / 2 : this.getWidthA())
            );
         int var6 = this.getYA()
            + (
               this.textColor.method19413() != Class2218.field14492
                  ? 0
                  : (this.textColor.method19413() != Class2218.field14491 ? this.getHeightA() / 2 : this.getHeightA())
            );
         int var7 = this.getFont().getWidth(this.getTypedText());
         float var8 = 18;
         float var9 = (float)Math.pow((double)this.field20711.calcPercent(), 3.0);
         RenderUtil.drawString(
            this.getFont(),
            (float)var5,
            (float)var6,
            this.getTypedText(),
                 ColorUtils.applyAlpha(var4, var1 * ColorUtils.getAlpha(var4)),
            this.textColor.method19411(),
            this.textColor.method19413()
         );
         RenderUtil.drawRoundedRect(
            (float)var5 - (float)(var7 / 2) * var9,
                 var6 + var8,
            (float)var5 + (float)(var7 / 2) * var9,
                 var6 + var8 + 2,
                 ColorUtils.applyAlpha(var4, var1 * ColorUtils.getAlpha(var4))
         );
         super.draw(var1);
      }
   }
}
