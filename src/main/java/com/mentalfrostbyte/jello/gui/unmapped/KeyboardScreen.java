package com.mentalfrostbyte.jello.gui.unmapped;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class KeyboardScreen extends Screen {
   private static String[] field6211;

   public KeyboardScreen(ITextComponent var1) {
      super(var1);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
