package com.mentalfrostbyte.jello.gui.unmapped;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGui extends Screen {

   public ClickGui(Text var1) {
      super(var1);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
