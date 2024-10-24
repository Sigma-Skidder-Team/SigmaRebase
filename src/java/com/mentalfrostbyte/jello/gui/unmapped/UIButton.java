package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.utils.ColorHelper;
import com.mentalfrostbyte.jello.utils.render.ColorUtils;
import com.mentalfrostbyte.jello.utils.render.RenderUtil;
import com.mentalfrostbyte.jello.utils.unmapped.Class2218;
import com.mentalfrostbyte.jello.utils.unmapped.ClientResource;

public class UIButton extends Class4247 {
   private static String[] field20602;
   public Animation field20711 = new Animation(190, 190);

   public UIButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, ClientResource var9) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var9, false);
      int var12 = (int)(210.0 * Math.sqrt((double)((float)var5 / 242.0F)));
      this.field20711 = new Animation(var12, var12);
   }

   @Override
   public void method13028(int var1, int var2) {
      super.method13028(var1, var2);
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
         int var7 = this.getFont().getStringWidth(this.getTypedText());
         float var8 = 18;
         float var9 = (float)Math.pow((double)this.field20711.calcPercent(), 3.0);
         RenderUtil.method11440(
            this.getFont(),
            (float)var5,
            (float)var6,
            this.getTypedText(),
                 ColorUtils.applyAlpha(var4, var1 * ColorUtils.method17710(var4)),
            this.textColor.method19411(),
            this.textColor.method19413()
         );
         RenderUtil.drawRect(
            (float)var5 - (float)(var7 / 2) * var9,
                 var6 + var8,
            (float)var5 + (float)(var7 / 2) * var9,
                 var6 + var8 + 2,
                 ColorUtils.applyAlpha(var4, var1 * ColorUtils.method17710(var4))
         );
         super.draw(var1);
      }
   }
}
