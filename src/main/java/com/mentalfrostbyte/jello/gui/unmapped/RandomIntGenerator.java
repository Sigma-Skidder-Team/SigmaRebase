package com.mentalfrostbyte.jello.gui.unmapped;

import java.util.Random;

public class RandomIntGenerator extends Random {
   @Override
   public int nextInt(int min, int max) {
      return this.nextInt(max - min) + min;
   }
}
