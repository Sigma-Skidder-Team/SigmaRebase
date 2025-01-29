package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ColorHelper;
import org.newdawn.slick.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

// NOTE: thought of this being PanelBase because AlertPanel uses it,
//       but then I checked the usages and I saw UIButton & etc.
public class UIBase extends AnimatedIconPanelWrap {
    private final List<UIHandler> uiHandlers = new ArrayList<UIHandler>();

   public UIBase(CustomGuiScreen screen, String typeThingIdk, int x, int y, int width, int height, boolean var7) {
      super(screen, typeThingIdk, x, y, width, height, var7);
   }

   public UIBase(CustomGuiScreen screen, String typeThingIdk, int x, int y, int width, int height, ColorHelper var7, boolean var8) {
      super(screen, typeThingIdk, x, y, width, height, var7, var8);
   }

   public UIBase(CustomGuiScreen screen, String typeThingIdk, int x, int y, int width, int height, ColorHelper var7, String var8, boolean var9) {
      super(screen, typeThingIdk, x, y, width, height, var7, var8, var9);
   }

   public UIBase(CustomGuiScreen screen, String typeThingIdk, int x, int y, int width, int height, ColorHelper var7, String var8, TrueTypeFont font, boolean var10) {
      super(screen, typeThingIdk, x, y, width, height, var7, var8, font, var10);
   }

   public final void onPress(UIHandler uiHandler) {
      this.uiHandlers.add(uiHandler);
   }

   public final void callUIHandlers() {
      for (UIHandler handler : this.uiHandlers) {
         handler.handle(this);
      }
   }
}
