package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.TrueTypeFont;

public class Class4377 extends PNGIconButton {
   private String[] field21384;
   private int field21385;

   public Class4377(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String[] var7, int var8, ColorHelper var9, String var10, TrueTypeFont var11) {
      super(var1, var2, var3, var4, var5, var6, Resources.skinPNG, var9, var10, var11);
      this.field21384 = var7;
      this.field21385 = var8;
      this.method13719();
   }

   public Class4377(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String[] var7, int var8, ColorHelper var9, String var10) {
      super(var1, var2, var3, var4, var5, var6, Resources.skinPNG, var9, var10);
      this.field21384 = var7;
      this.field21385 = var8;
      this.method13719();
   }

   public Class4377(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String[] var7, int var8, ColorHelper var9) {
      super(var1, var2, var3, var4, var5, var6, Resources.skinPNG, var9);
      this.field21384 = var7;
      this.field21385 = var8;
      this.method13719();
   }

   public Class4377(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String[] var7, int var8) {
      super(var1, var2, var3, var4, var5, var6, Resources.skinPNG);
      this.field21384 = var7;
      this.field21385 = var8;
      this.method13719();
   }

   private void method13719() {
      if (this.field21385 >= 0 && this.field21385 < this.field21384.length) {
         this.setFont(ResourceRegistry.JelloLightFont20);
         this.setTypedText(this.field21384[this.field21385]);
      } else {
         throw new RuntimeException("Invalid index for TypeButton");
      }
   }

   @Override
   public void onClick3(int mouseX, int mouseY, int mouseButton) {
      this.method13721(this.method13720() + 1);
      super.onClick3(mouseX, mouseY, mouseButton);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
   }

   @Override
   public void draw(float partialTicks) {
      super.draw(partialTicks);
   }

   public int method13720() {
      return this.field21385;
   }

   public void method13721(int var1) {
      this.method13722(var1, true);
   }

   public void method13722(int var1, boolean var2) {
      var1 %= this.field21384.length;
      if (var1 != this.field21385) {
         this.field21385 = var1;
         this.setTypedText(this.field21384[var1]);
         if (var2) {
            this.callUIHandlers();
         }
      }
   }

   public String[] method13723() {
      return this.field21384;
   }
}
