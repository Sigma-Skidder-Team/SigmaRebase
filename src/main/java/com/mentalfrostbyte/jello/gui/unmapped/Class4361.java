package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class Class4361 extends UIBase {
   public Class4361(CustomGuiScreen var1, String var2, int var3, int var4) {
      super(var1, var2, var3, var4, 30, 30, false);
   }

   @Override
   public void draw(float partialTicks) {
      RenderUtil.drawImage((float)this.xA, (float)this.yA, 30.0F, 30.0F, !this.method13298() ? Resources.xmark : Resources.xmark2);
      super.draw(partialTicks);
   }
}
