package com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons;

import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.image.types.AnimatedImage;
import com.mentalfrostbyte.jello.gui.base.elements.Element;

public class AnimatedImageButton extends Element {
   public AnimatedImage image;

   public AnimatedImageButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, boolean var7, AnimatedImage var8) {
      super(var1, var2, var3, var4, var5, var6, var7);
      this.image = var8;
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      this.image.tick();
   }

   @Override
   public void draw(float partialTicks) {
      this.getAnimatedImage().drawImage(this.getXA(), this.getYA(), this.getWidthA(), this.getHeightA(), partialTicks);
      super.draw(partialTicks);
   }

   public AnimatedImage getAnimatedImage() {
      return this.image;
   }

   public void setImage(AnimatedImage var1) {
      this.image = var1;
   }
}
