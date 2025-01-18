package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.RenderUtil;

public class Class4270 extends UIBase {
   public int field20696;

   public Class4270(CustomGuiScreen var1, String var2, int var3, int var4) {
      super(var1, var2, var3, var4, 1060, 357, false);

      for (Keys key : Keys.values()) {
         Class4268 var11;
         this.addToList(
            var11 = new Class4268(
               this,
               "KEY_" + key.row + this.getChildren().size(),
               key.getX(),
               key.method9026(),
               key.getY(),
               key.method9029(),
               key.name,
               key.row
            )
         );
         var11.doThis((var2x, var3x) -> {
            this.field20696 = var11.field20690;
            this.callUIHandlers();
         });
      }

      this.method13300(false);
   }

   @Override
   public boolean onClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton <= 1) {
         return super.onClick(mouseX, mouseY, mouseButton);
      } else {
         this.field20696 = mouseButton;
         this.callUIHandlers();
         return false;
      }
   }

   @Override
   public void keyPressed(int keyCode) {
      for (Keys var7 : Keys.values()) {
         if (var7.row == keyCode) {
            super.keyPressed(keyCode);
            return;
         }
      }

      this.field20696 = keyCode;
      this.callUIHandlers();
      super.keyPressed(keyCode);
   }

   public void method13104() {
      for (CustomGuiScreen var4 : this.getChildren()) {
         if (var4 instanceof Class4268) {
            Class4268 var5 = (Class4268)var4;
            var5.method13102();
         }
      }
   }

   public int[] method13105(int keycode) {
      for (Keys var7 : Keys.values()) {
         if (var7.row == keycode) {
            return new int[]{var7.getX() + var7.getY() / 2, var7.method9026() + var7.method9029()};
         }
      }

      return new int[]{this.getWidthA() / 2, 20};
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
   }

   @Override
   public void draw(float var1) {
      int var6 = this.xA - 20;
      int var7 = this.yA - 20;
      int var8 = this.widthA + 20 * 2;
      int var9 = this.heightA + 5 + 20 * 2;
      RenderUtil.drawRoundedRect((float)(var6 + 14 / 2), (float)(var7 + 14 / 2), (float)(var8 - 14), (float)(var9 - 14), 20.0F, var1 * 0.5F);
      RenderUtil.method11474((float)var6, (float)var7, (float)var8, (float)var9, 14.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor());
      super.draw(var1);
   }
}
