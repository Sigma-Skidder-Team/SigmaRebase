package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.keybind;

import com.mentalfrostbyte.jello.gui.impl.jello.ingame.KeyboardScreen;

public class Class1376 implements Runnable {
   private static String[] field7351;
   public final KeyboardScreen field7352;
   public final KeyboardScreen field7353;

   public Class1376(KeyboardScreen var1, KeyboardScreen var2) {
      this.field7353 = var1;
      this.field7352 = var2;
   }

   @Override
   public void run() {
      this.field7353.field20957.method13242();
      this.field7352.clearChildren();
      this.field7353.field20961 = 0;
   }
}
