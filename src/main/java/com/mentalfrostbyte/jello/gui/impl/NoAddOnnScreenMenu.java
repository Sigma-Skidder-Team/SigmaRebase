package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.MainMenuScreen;
import org.lwjgl.opengl.GL11;

public class NoAddOnnScreenMenu extends MainMenuScreen {
   @Override
   public void render(MatrixStack matrices, int var2, int var3, float delta) {
      super.render(matrices, var2, var3, delta);
      drawString(matrices, this.font, "No Addons - SIGMA", 87, this.height - 10, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F));
      GL11.glPushMatrix();
      GL11.glTranslatef(88.0F, (float)(this.height - 10), 0.0F);
      GL11.glScalef(0.5F, 0.5F, 1.0F);
      GL11.glTranslatef(-88.0F, (float)(-(this.height - 10)), 0.0F);
      drawString(matrices, this.font, "PROD", 280, this.height - 10, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F));
      GL11.glPopMatrix();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
