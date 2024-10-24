package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.utils.ClientColors;
import com.mentalfrostbyte.jello.utils.ColorHelper;
import com.mentalfrostbyte.jello.utils.render.ColorUtils;
import com.mentalfrostbyte.jello.utils.render.RenderUtil;
import com.mentalfrostbyte.jello.utils.render.Texture;
import com.mentalfrostbyte.jello.utils.unmapped.ClientResource;
import org.lwjgl.opengl.GL11;

public class PNGIconButton extends ButtonPanel {
   public static final ColorHelper field20574 = new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor, ColorUtils.method17691(ClientColors.LIGHT_GREYISH_BLUE.getColor, 0.1F));
   public Texture field20575;

   public PNGIconButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8, String var9, ClientResource var10) {
      super(var1, var2, var3, var4, var5, var6, var8, var9, var10);
      this.field20575 = var7;
   }

   public PNGIconButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8, String var9) {
      super(var1, var2, var3, var4, var5, var6, var8, var9);
      this.field20575 = var7;
   }

   public PNGIconButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Texture var7, ColorHelper var8) {
      super(var1, var2, var3, var4, var5, var6, var8);
      this.field20575 = var7;
   }

   public PNGIconButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Texture var7) {
      super(var1, var2, var3, var4, var5, var6, field20574);
      this.field20575 = var7;
   }

   public Texture method13025() {
      return this.field20575;
   }

   public void method13026(Texture var1) {
      this.field20575 = var1;
   }

   @Override
   public void draw(float var1) {
      float var4 = !this.isHovered() ? 0.3F : (!this.method13216() ? (!this.method13212() ? Math.max(var1 * this.field20584, 0.0F) : 1.5F) : 0.0F);
      RenderUtil.drawImage(
         (float)this.getXA(),
         (float)this.getYA(),
         (float)this.getWidthA(),
         (float)this.getHeightA(),
         this.method13025(),
         ColorUtils.applyAlpha(
                 ColorUtils.method17690(this.textColor.method19405(), this.textColor.method19403(), 1.0F - var4),
            (float)(this.textColor.method19405() >> 24 & 0xFF) / 255.0F * var1
         )
      );
      if (this.getTypedText() != null) {
         RenderUtil.method11440(
            this.getFont(),
            (float)(this.getXA() + this.getWidthA() / 2),
            (float)(this.getYA() + this.getHeightA() / 2),
            this.getTypedText(),
                 ColorUtils.applyAlpha(this.textColor.getTextColor(), var1),
            this.textColor.method19411(),
            this.textColor.method19413()
         );
      }

      GL11.glPushMatrix();
      super.method13226(var1);
      GL11.glPopMatrix();
   }
}