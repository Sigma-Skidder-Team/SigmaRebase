package com.mentalfrostbyte.jello.gui.unmapped;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class InGameOptionsScreen extends Screen {
   public InGameOptionsScreen() {
      super(new LiteralText("Jello Options"));
   }

   @Override
   public boolean isPauseScreen() {
      return true;
   }
}
