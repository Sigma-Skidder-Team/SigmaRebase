package com.mentalfrostbyte.jello.gui.base.elements.impl;

import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind.Keys;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;

public class Keyboard extends Element {
   public int keyCode;

   public Keyboard(CustomGuiScreen var1, String var2, int var3, int var4) {
      super(var1, var2, var3, var4, 1060, 357, false);

      for (Keys key : Keys.values()) {
         Child var11;
         this.addToList(
            var11 = new Child(
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
         var11.onClick((var2x, var3x) -> {
            this.keyCode = var11.field20690;
            this.callUIHandlers();
         });
      }

      this.setListening(false);
   }

   @Override
   public boolean onClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton <= 1) {
         return super.onClick(mouseX, mouseY, mouseButton);
      } else {
         this.keyCode = mouseButton;
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

      this.keyCode = keyCode;
      this.callUIHandlers();
      super.keyPressed(keyCode);
   }

   public void method13104() {
      for (CustomGuiScreen var4 : this.getChildren()) {
         if (var4 instanceof Child var5) {
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
   public void draw(float partialTicks) {
      int var6 = this.xA - 20;
      int var7 = this.yA - 20;
      int var8 = this.widthA + 20 * 2;
      int var9 = this.heightA + 5 + 20 * 2;
      RenderUtil.drawRoundedRect((float)(var6 + 14 / 2), (float)(var7 + 14 / 2), (float)(var8 - 14), (float)(var9 - 14), 20.0F, partialTicks * 0.5F);
      RenderUtil.drawRoundedButton((float)var6, (float)var7, (float)var8, (float)var9, 14.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor());
      super.draw(partialTicks);
   }
}
