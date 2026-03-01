package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind;

import com.mentalfrostbyte.jello.gui.impl.jello.ingame.KeyboardScreen;

public class Class1376 implements Runnable {
   public final KeyboardScreen keyboardScreen2;
   public final KeyboardScreen keyboardScreen;

   public Class1376(KeyboardScreen var1, KeyboardScreen var2) { // only useage is of the same instance
      this.keyboardScreen = var1;
      this.keyboardScreen2 = var2;
   }

   @Override
   public void run() {
      this.keyboardScreen.keyboard.method13242();
      this.keyboardScreen2.clearChildren();
      this.keyboardScreen.keyCode = 0;
   }
}
