package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.JelloClickGUI;
import net.minecraft.client.MinecraftClient;

public class ClickGUIBlurOverlay extends AnimatedIconPanelWrap {
   public final JelloClickGUI field21278;

   public ClickGUIBlurOverlay(JelloClickGUI var1, CustomGuiScreen var2, String var3) {
      super(var2, var3, 0, 0, MinecraftClient.getInstance().getWindow().getWidth(),
            MinecraftClient.getInstance().getWindow().getHeight(), false);
      this.field21278 = var1;
   }

   @Override
   public void onClick3(int mouseX, int mouseY, int mouseButton) {
      super.onClick3(mouseX, mouseY, mouseButton);
   }
}
