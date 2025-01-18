package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.EasingFunctions;
import com.mentalfrostbyte.jello.gui.base.Screen;
import com.mentalfrostbyte.jello.gui.unmapped.Class4297;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.TimerUtil;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.MinecraftClient;

public class SnakeGameScreen extends Screen {
   public Minecraft field21044 = MinecraftClient.getInstance();
   public TimerUtil field21045 = new TimerUtil();
   public Class4297 field21046;
   public Animation field21047;
   public int field21048;
   public int field21049 = 14;

   public SnakeGameScreen() {
      super("SnakeGameScreen");
      this.method13300(false);
      this.field21045.start();
      this.field21047 = new Animation(200, 0);
      ColorUtils.blur();
      int var3 = 48;
      int var4 = 27;
      int var5 = 14;
      int var6 = var3 * var5;
      int var7 = var4 * var5;
      this.addToList(this.field21046 = new Class4297(this, "snake", (this.widthA - var6) / 2,
            (this.getHeightA() - var7) / 2 + 30, var3, 27, var5));
   }

   @Override
   public void draw(float var1) {
      var1 = this.field21047.calcPercent();
      float var4 = EasingFunctions.easeOutBack(var1, 0.0F, 1.0F, 1.0F);
      this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
      float var5 = 0.25F * var1;
      RenderUtil.drawRoundedRect(
            (float) this.xA,
            (float) this.yA,
            (float) (this.xA + this.widthA),
            (float) (this.yA + this.heightA),
            ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var5));
      super.method13224();
      RenderUtil.drawRoundedRect(
            (float) this.field21046.getXA(),
            (float) this.field21046.getYA(),
            (float) this.field21046.getWidthA(),
            (float) this.field21046.getHeightA(),
            40.0F,
            var1);
      RenderUtil.drawRoundedRect(
            (float) (this.field21046.getXA() - 20),
            (float) (this.field21046.getYA() - 20),
            (float) (this.field21046.getWidthA() + 40),
            (float) (this.field21046.getHeightA() + 40),
            14.0F,
            ClientColors.LIGHT_GREYISH_BLUE.getColor());
      super.draw(var1);
      int var6 = (this.widthA - this.field21046.getWidthA()) / 2;
      int var7 = (this.heightA - this.field21046.getHeightA()) / 2;
      RenderUtil.drawString(ResourceRegistry.JelloMediumFont40, (float) var6, (float) (var7 - 60), "Snake",
            ClientColors.LIGHT_GREYISH_BLUE.getColor());
      this.field21048 = Math.max(this.field21046.method13179(), this.field21048);
      String var8 = "Max: " + this.field21048 + "   |   Score: " + this.field21046.method13179();
      RenderUtil.drawString(
            ResourceRegistry.JelloLightFont20,
            (float) (var6 + this.field21046.getWidthA() - ResourceRegistry.JelloLightFont20.getWidth(var8)),
            (float) (var7 - 50),
            var8,
            ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F));
   }

   @Override
   public void keyPressed(int keyCode) {
      super.keyPressed(keyCode);
      if (keyCode == 256) {
         ColorUtils.resetShaders();
         MinecraftClient.getInstance().displayGuiScreen(null);
      }
   }
}
