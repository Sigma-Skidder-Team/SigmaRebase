package com.mentalfrostbyte.jello.gui.unmapped;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class InGameOptionsScreen extends Screen {
   public InGameOptionsScreen() {
      super(new StringTextComponent("Jello Options"));
   }

   @Override
   public boolean isPauseScreen() {
      return true;
   }
}
