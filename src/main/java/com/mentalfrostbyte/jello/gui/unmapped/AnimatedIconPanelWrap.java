package com.mentalfrostbyte.jello.gui.unmapped;


import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ColorHelper;
import org.newdawn.slick.TrueTypeFont;

// not sure if this is an artifact of obfuscation or what.
// this is the same thing as AnimatedIconPanel
// as it just passes in the arguments to it and there's no extra methods
public class AnimatedIconPanelWrap extends AnimatedIconPanel {

    public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int x, int y, int width, int height, boolean var7) {
      super(screen, name, x, y, width, height, var7);
   }

   public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int x, int y, int width, int height, ColorHelper colorHelper, boolean var8) {
      super(screen, name, x, y, width, height, colorHelper, var8);
   }

   public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int x, int y, int width, int height, ColorHelper colorHelper, String var8, boolean var9) {
      super(screen, name, x, y, width, height, colorHelper, var8, var9);
   }

   public AnimatedIconPanelWrap(CustomGuiScreen screen, String name, int x, int y, int width, int height, ColorHelper colorHelper, String var8, TrueTypeFont font, boolean var10) {
      super(screen, name, x, y, width, height, colorHelper, var8, font, var10);
   }
}
