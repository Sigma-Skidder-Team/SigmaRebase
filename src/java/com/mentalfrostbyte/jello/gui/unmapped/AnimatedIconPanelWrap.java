package com.mentalfrostbyte.jello.gui.unmapped;


import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ColorHelper;
import org.newdawn.slick.TrueTypeFont;

// not sure if this is an artifact of obfuscation or what.
// this is the same thing as AnimatedIconPanel
// as it just passes in the arguments to it and there's no extra methods
public class AnimatedIconPanelWrap extends AnimatedIconPanel {

    public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int var3, int var4, int var5, int var6, boolean var7) {
      super(screen, name, var3, var4, var5, var6, var7);
   }

   public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int var3, int var4, int var5, int var6, ColorHelper var7, boolean var8) {
      super(screen, name, var3, var4, var5, var6, var7, var8);
   }

   public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, boolean var9) {
      super(screen, name, var3, var4, var5, var6, var7, var8, var9);
   }

   public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9, boolean var10) {
      super(screen, name, var3, var4, var5, var6, var7, var8, var9, var10);
   }
}
