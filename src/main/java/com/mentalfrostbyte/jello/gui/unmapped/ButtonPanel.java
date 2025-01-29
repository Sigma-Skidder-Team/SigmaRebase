package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.newdawn.slick.TrueTypeFont;

public class ButtonPanel extends UIBase {
   public float field20584;
   private int field20585 = 0;
   public int field20586 = 0;

   public ButtonPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int height) {
      super(screen, iconName, x, y, width, height, false);
   }

   public ButtonPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int var6, ColorHelper var7) {
      super(screen, iconName, x, y, width, var6, var7, false);
   }

   public ButtonPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int var6, ColorHelper var7, String var8) {
      super(screen, iconName, x, y, width, var6, var7, var8, false);
   }

   public ButtonPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, ColorHelper var7, String var8, TrueTypeFont font) {
      super(screen, iconName, x, y, width, height, var7, var8, font, false);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
      this.field20584 = this.field20584 + (!this.method13298() ? -0.1F : 0.1F);
      this.field20584 = Math.min(Math.max(0.0F, this.field20584), 1.0F);
   }

   @Override
   public void draw(float partialTicks) {
      float var4 = !this.isHovered() ? 0.3F : (!this.method13216() ? (!this.method13212() ? Math.max(partialTicks * this.field20584, 0.0F) : 1.5F) : 0.0F);
      int var5 = ColorUtils.applyAlpha(
              ColorUtils.method17690(this.textColor.method19405(), this.textColor.method19403(), 1.0F - var4),
         (float)(this.textColor.method19405() >> 24 & 0xFF) / 255.0F * partialTicks
      );
      int var6 = var5 >> 24 & 0xFF;
      int var7 = var5 >> 16 & 0xFF;
      int var8 = var5 >> 8 & 0xFF;
      int var9 = var5 & 0xFF;
      if (this.field20586 <= 0) {
         RenderUtil.drawRoundedRect(
            (float)this.getXA(),
            (float)this.getYA(),
            (float)(this.getXA() + this.getWidthA()),
            (float)(this.getYA() + this.getHeightA()),
            var5
         );
      } else {
         RenderUtil.method11474(
            (float)this.getXA(), (float)this.getYA(), (float)this.getWidthA(), (float)this.getHeightA(), (float)this.field20586, var5
         );
      }

      int var10 = this.getXA()
         + (
            this.textColor.method19411() != Class2218.field14492
               ? 0
               : (this.textColor.method19411() != Class2218.field14490 ? this.getWidthA() / 2 : this.getWidthA())
         );
      int var11 = this.getYA()
         + (
            this.textColor.method19413() != Class2218.field14492
               ? 0
               : (this.textColor.method19413() != Class2218.field14491 ? this.getHeightA() / 2 : this.getHeightA())
         );
      if (this.getTypedText() != null) {
         RenderUtil.drawString(
            this.getFont(),
            (float)(this.field20585 + var10),
            (float)var11,
            this.getTypedText(),
            ColorUtils.applyAlpha(this.textColor.getTextColor(), partialTicks),
            this.textColor.method19411(),
            this.textColor.method19413()
         );
      }

      super.draw(partialTicks);
   }

   public void method13034(int var1) {
      this.field20585 = var1;
   }

   public int method13035() {
      return this.field20585;
   }
}
