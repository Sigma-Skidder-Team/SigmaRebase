package com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons;


import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.image.types.AnimatedImage;
import com.mentalfrostbyte.jello.gui.combined.AnimationMode;
import com.mentalfrostbyte.jello.util.client.render.Resources;

public class Checkbox extends AnimatedImageButton {
   private boolean field20675;
   private boolean field20676;

   public Checkbox(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
      super(var1, var2, var3, var4, var5, var6, false, new AnimatedImage(Resources.checkbox, 20, 40, 18, AnimationMode.TICK_BASED, 200, 1));
      this.method13088();
   }

   private void method13088() {
      this.getAnimatedImage().setLooping(false);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
   }

   @Override
   public void onClick3(int mouseX, int mouseY, int mouseButton) {
      this.getAnimatedImage().start();
      this.method13093(this.getAnimatedImage().isForwards());
   }

   public boolean method13090() {
      return this.field20675;
   }

   public void method13091(boolean var1) {
      this.field20675 = var1;
   }

   public boolean method13092() {
      return this.field20676;
   }

   public void method13093(boolean var1) {
      this.method13094(var1, true);
   }

   public void method13094(boolean var1, boolean var2) {
      if (var1 != this.method13092()) {
         if (var1 && !this.getAnimatedImage().isForwards() || !var1 && this.getAnimatedImage().isForwards()) {
            this.getAnimatedImage().start();
         }

         this.field20676 = var1;
         if (var2) {
            this.callUIHandlers();
         }
      }
   }
}
