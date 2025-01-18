package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import net.minecraft.client.MinecraftClient;

public class CustomGuiScreenWidthSetter implements IWidthSetter {

   @Override
   public void setWidth(CustomGuiScreen forScreen, CustomGuiScreen fromWidthOfThisScreen) {
      forScreen.setXA(0);
      if (fromWidthOfThisScreen == null) {
         forScreen.setWidthA(MinecraftClient.getInstance().getWindow().getWidth());
      } else {
         forScreen.setWidthA(fromWidthOfThisScreen.getWidthA());
      }
   }
}
