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
               "KEY_" + key.row + this.getRenderObjects().size(),
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
            this.method13037();
         });
      }

      this.method13300(false);
   }

   @Override
   public boolean method13078(int var1, int var2, int var3) {
      if (var3 <= 1) {
         return super.method13078(var1, var2, var3);
      } else {
         this.field20696 = var3;
         this.method13037();
         return false;
      }
   }

   @Override
   public void keyPressed(int keycode) {
      for (Keys var7 : Keys.values()) {
         if (var7.row == keycode) {
            super.keyPressed(keycode);
            return;
         }
      }

      this.field20696 = keycode;
      this.method13037();
      super.keyPressed(keycode);
   }

   public void method13104() {
      for (CustomGuiScreen var4 : this.getRenderObjects()) {
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
   public void method13028(int var1, int var2) {
      super.method13028(var1, var2);
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
