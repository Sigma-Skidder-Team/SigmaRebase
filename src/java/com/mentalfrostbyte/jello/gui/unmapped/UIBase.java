package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ColorHelper;
import org.newdawn.slick.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

// TODO: thought of this being PanelBase because AlertPanel uses it,
//       but then I checked the usages and I saw UIButton and etc.
public class UIBase extends AnimatedIconPanelWrap {
    private final List<Class8435> field20603 = new ArrayList<Class8435>();

   public UIBase(CustomGuiScreen screen, String iconName, int var3, int var4, int width, int height, boolean var7) {
      super(screen, iconName, var3, var4, width, height, var7);
   }

   public UIBase(CustomGuiScreen var1, String var2, int var3, int var4, int width, int height, ColorHelper var7, boolean var8) {
      super(var1, var2, var3, var4, width, height, var7, var8);
   }

   public UIBase(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, boolean var9) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var9);
   }

   public UIBase(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9, boolean var10) {
      super(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
   }

   public final void method13036(Class8435 var1) {
      this.field20603.add(var1);
   }

   public final void method13037() {
      for (Class8435 var4 : this.field20603) {
         var4.method29648(this);
      }
   }
}
