package com.mentalfrostbyte.jello.gui.unmapped;

// $VF: synthetic class
public class KeybindTypesWrapper {
   public static final int[] values = new int[KeybindTypes.values().length];

   static {
      try {
         values[KeybindTypes.MODULE.ordinal()] = 1;
      } catch (NoSuchFieldError var4) {
      }

      try {
         values[KeybindTypes.SCREEN.ordinal()] = 2;
      } catch (NoSuchFieldError var3) {
      }
   }
}
