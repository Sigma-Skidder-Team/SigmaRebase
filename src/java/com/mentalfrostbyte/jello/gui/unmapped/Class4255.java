package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.utils.ClientColors;
import com.mentalfrostbyte.jello.utils.ColorHelper;
import com.mentalfrostbyte.jello.utils.render.RenderUtil;
import com.mentalfrostbyte.jello.utils.render.Resources;
import com.mentalfrostbyte.jello.utils.render.Texture;
import org.lwjgl.opengl.GL11;

public class Class4255 extends Class4247 {
   public Texture field20633;
   public Texture field20634;

   public Class4255(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String var7, Texture var8, Texture var9) {
      super(var1, var2, var3, var4, var5, var6, ColorHelper.field27961, var7, false);
      this.field20633 = var8;
      this.field20634 = var9;
   }

   @Override
   public void draw(float var1) {
      GL11.glAlphaFunc(518, 0.1F);
      RenderUtil.drawImage(
         (float)(this.xA + (this.widthA - 64) / 2),
         (float)(this.yA + 10),
         64.0F,
         64.0F,
         !this.method13298() ? this.field20633 : this.field20634,
         ClientColors.LIGHT_GREYISH_BLUE.getColor
      );

      RenderUtil.drawString(
              Resources.regular25,
         (float)(this.xA + (this.getWidthA() - Resources.regular25.getStringWidth(this.field20912)) / 2),
         (float)(this.yA + this.getHeightA() - 50),
         this.field20912,
         !this.method13298() ? -14869219 : -319475
      );
      super.draw(var1);
   }
}