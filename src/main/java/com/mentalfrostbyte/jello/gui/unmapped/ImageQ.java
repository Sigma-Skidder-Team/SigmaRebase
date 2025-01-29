package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import org.newdawn.slick.opengl.Texture;

public class ImageQ extends AnimatedIconPanelWrap {
   private static String[] field20736;
   private Texture field20792;

   public ImageQ(CustomGuiScreen screen, String text, int var3, int var4, int var5, int var6, Texture image) {
      super(screen, text, var3, var4, var5, var6, false);
      this.field20792 = image;
   }

   @Override
   public void draw(float partialTicks) {
      RenderUtil.drawImage(
         (float)this.xA,
         (float)this.yA,
         (float)this.widthA,
         (float)this.heightA,
         this.field20792,
         ColorUtils.method17690(
            ClientColors.DEEP_TEAL.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), !this.method13298() ? 0.0F : (!this.method13212() ? 0.15F : 0.3F)
         )
      );
      super.draw(partialTicks);
   }
}
