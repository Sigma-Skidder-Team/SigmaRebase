package com.mentalfrostbyte.jello.gui.unmapped;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class CreditToCreatorsScreen extends Screen {
   private static String[] field6188;

   public CreditToCreatorsScreen(ITextComponent var1) {
      super(var1);
   }

   @Override
   public boolean isPauseScreen() {
      return true;
   }
}
