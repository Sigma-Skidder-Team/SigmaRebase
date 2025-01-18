package com.mentalfrostbyte.jello.gui.unmapped;


import com.mentalfrostbyte.jello.module.settings.SettingType;

// $VF: synthetic class
public class SettingTypeWrapper {
   public static final int[] values = new int[SettingType.values().length];

   static {
      try {
         values[SettingType.BOOLEAN.ordinal()] = 1;
      } catch (NoSuchFieldError var10) {
      }

      try {
         values[SettingType.NUMBER.ordinal()] = 2;
      } catch (NoSuchFieldError var9) {
      }

      try {
         values[SettingType.INPUT.ordinal()] = 3;
      } catch (NoSuchFieldError var8) {
      }

      try {
         values[SettingType.MODE.ordinal()] = 4;
      } catch (NoSuchFieldError var7) {
      }

      try {
         values[SettingType.TEXTBOX.ordinal()] = 5;
      } catch (NoSuchFieldError var6) {
      }

      try {
         values[SettingType.SUBOPTION.ordinal()] = 6;
      } catch (NoSuchFieldError var5) {
      }

      try {
         values[SettingType.BOOLEAN2.ordinal()] = 7;
      } catch (NoSuchFieldError var4) {
      }

      try {
         values[SettingType.UNUSED.ordinal()] = 8;
      } catch (NoSuchFieldError var3) {
      }
   }
}
