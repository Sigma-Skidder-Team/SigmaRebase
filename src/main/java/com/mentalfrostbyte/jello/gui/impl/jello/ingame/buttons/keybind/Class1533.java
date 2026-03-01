package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind;

import com.mentalfrostbyte.jello.gui.impl.jello.ingame.KeyboardScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.panels.ModsPanel;

public class Class1533 implements Runnable {
   public final KeyboardScreen field8318;
   public final KeyboardScreen field8319;

   public Class1533(KeyboardScreen var1, KeyboardScreen var2) {
      this.field8319 = var1;
      this.field8318 = var2;
   }

   @Override
   public void run() {
      this.field8318
         .addToList(
            this.field8319.modsPanel = new ModsPanel(
               this.field8318, "mods", 0, 0, KeyboardScreen.method13337(this.field8319), KeyboardScreen.method13338(this.field8319)
            )
         );
      this.field8319.modsPanel.method13623((var1, var2) -> {
         if (var2 != null) {
            var2.method21598(this.field8319.keyboard.keyCode);
         }

         KeyboardScreen.method13339(this.field8319);
      });
      this.field8319.modsPanel.setReAddChildren(true);
   }
}
