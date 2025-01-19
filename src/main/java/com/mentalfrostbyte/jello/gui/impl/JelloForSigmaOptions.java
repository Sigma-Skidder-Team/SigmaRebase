package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.gui.unmapped.InGameOptionsScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.text.LiteralText;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

public class JelloForSigmaOptions extends IngameMenuScreen {
   public static Date field4622 = new Date(0L);

   public JelloForSigmaOptions() {
      super(true);
      if (field4622.before(new Date(System.currentTimeMillis() - 3000L))) {
         field4622 = new Date();
         Client.getInstance().getLogger().info("Saving profiles...");

         try {
            Client.getInstance().moduleManager.getConfigurationManager().saveAndReplaceConfigs();
            Client.getInstance().saveClientData();
         } catch (IOException var4) {
            var4.printStackTrace();
            Client.getInstance().getLogger().warn("Unable to save mod profiles...");
         }
      }
   }

   @Override
   public void init() {
      if (Client.getInstance().clientMode == ClientMode.JELLO) {
         this.addButton(
               new Button(
                     this.width / 2 - 102,
                     this.height - 45,
                     204,
                     20,
                     new LiteralText("Jello for Sigma Options"),
                     var1 -> this.minecraft.displayGuiScreen(new InGameOptionsScreen())));
      }

      super.init();
      Iterator var3 = this.buttons.iterator();

      while (var3.hasNext()) {
         AbstractButtonWidget var4 = (AbstractButtonWidget) var3.next();
         if (var4.y == this.height / 4 + 72 + -16) {
            var3.remove();
         }
      }
   }

   @Override
   public void render(MatrixStack matrices, int var2, int var3, float delta) {
      super.render(matrices, var2, var3, delta);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
