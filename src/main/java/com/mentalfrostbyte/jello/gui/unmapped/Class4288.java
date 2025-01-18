package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class Class4288 extends AnimatedIconPanelWrap {
   private final Class4339 field20780;
   public final VerticalScrollBar field20781;

   public Class4288(VerticalScrollBar var1, VerticalScrollBar var2, int var3) {
      super(var2, "verticalScrollBarButton", 0, 0, var3, 10, true);
      this.field20781 = var1;
      this.field20886 = true;
      this.field20780 = (Class4339)var2.getParent();
      this.method13300(false);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
      float var5 = (float)this.field20780.getButton().getHeightA();
      float var6 = (float)this.parent.getParent().getHeightA();
      float var7 = (float)this.parent.getHeightA();
      float var8 = var6 / var5;
      float var9 = var7 * var8;
      float var10 = 20.0F;
      if (!(var9 < 20.0F)) {
         if (var9 > var7) {
            var9 = var7;
         }
      } else {
         var9 = 20.0F;
      }

      this.setHeightA((int)var9);
      if (!this.field20877 && this.getHeightA() != this.parent.getHeightA()) {
         if (this.field20781.field20793 >= 0) {
            if (this.field20781.field20793 + this.parent.getParent().getHeightA() > this.field20780.getButton().getHeightA()) {
               this.field20781.field20793 = this.field20780.getButton().getHeightA() - this.parent.getParent().getHeightA();
            }
         } else {
            this.field20781.field20793 = 0;
         }

         float var16 = var5 - var6;
         float var13 = (float)this.field20781.field20793 / var16;
         float var14 = (float)(this.parent.getHeightA() - this.getHeightA());
         float var15 = var14 * var13 + 0.5F;
         this.setYA((int)var15);
      } else if (this.method13216()) {
         float var12 = (float)this.getYA() / (float)this.parent.getHeightA();
         this.field20781.field20793 = (int)(var12 * (float)this.field20780.getButton().getHeightA());
         if (this.field20781.field20793 >= 0) {
            if (this.field20781.field20793 + this.parent.getParent().getHeightA() > this.field20780.getButton().getHeightA()) {
               this.field20781.field20793 = this.field20780.getButton().getHeightA() - this.parent.getParent().getHeightA();
            }
         } else {
            this.field20781.field20793 = 0;
         }

         this.field20781.field20797.reset();
         this.field20781.field20797.start();
      }
   }

   @Override
   public void draw(float var1) {
      int var4 = 5;
      var1 *= !this.field20877 ? (!this.field20908 ? 0.3F : 0.7F) : 0.75F;
      int var5 = this.xA;
      int var6 = this.widthA;
      if (Client.getInstance().clientMode != ClientMode.JELLO) {
         var4 = 0;
         var6 -= 7;
         var5 += 7;
         RenderUtil.drawRoundedRect(
            (float)var5,
            (float)(this.yA + var4),
            (float)(var5 + var6),
            (float)(this.yA + this.getHeightA() - var4),
            ColorUtils.applyAlpha(ClientColors.MID_GREY.getColor(), 1.0F * var1)
         );
      } else {
         RenderUtil.drawImage((float)var5, (float)this.yA, (float)var6, (float)var4, Resources.verticalScrollBarTopPNG, var1);
         RenderUtil.drawImage((float)var5, (float)(this.yA + this.heightA - var4), (float)var6, (float)var4, Resources.verticalScrollBarBottomPNG, var1);
         RenderUtil.drawRoundedRect(
            (float)var5,
            (float)(this.yA + var4),
            (float)(var5 + var6),
            (float)(this.yA + this.getHeightA() - var4),
                 ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * var1)
         );
      }

      super.draw(var1);
   }
}
