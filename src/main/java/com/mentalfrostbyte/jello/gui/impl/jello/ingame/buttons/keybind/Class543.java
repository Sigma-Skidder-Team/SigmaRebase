package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind;

import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.KeyboardScreen;

public class Class543 implements Runnable {
   private static String[] field2602;
   public final KeyboardScreen field2603;
   public final KeyboardScreen field2604;

   public Class543(KeyboardScreen var1, KeyboardScreen var2) {
      this.field2604 = var1;
      this.field2603 = var2;
   }

   @Override
   public void run() {
      for (CustomGuiScreen var4 : this.field2603.getChildren()) {
         if (var4 instanceof PopOver var5) {
			 var5.method13712();
            this.field2604.keyboard.method13104();
            var5.setReAddChildren(true);
            var5.method13242();
            this.field2603.method13234(this.field2604.modsPanel);
         }
      }
   }
}
