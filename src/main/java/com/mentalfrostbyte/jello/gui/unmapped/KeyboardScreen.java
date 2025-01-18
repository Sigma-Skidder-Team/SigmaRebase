package com.mentalfrostbyte.jello.gui.unmapped;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class KeyboardScreen extends Screen {
   private static String[] field6211;

   public KeyboardScreen(Text var1) {
      super(var1);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
