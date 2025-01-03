package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.gui.base.QuadraticEasing;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.util.math.vector.Vector3i;

public class Class4355 extends UIBase {
   public int field21288;
   public final Animation field21289;
   public final Animation field21290;
   public String field21291;
   public Vector3i field21292;
   public int field21293;
   public int field21294;

   public Class4355(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String var7, Vector3i var8, int var9) {
      super(var1, var2, var3, var4, var5, var6, true);
      this.field21288 = var4;
      this.field21289 = new Animation(114, 114);
      this.field21290 = new Animation(200, 200);
      this.field21290.changeDirection(Direction.BACKWARDS);
      this.field21291 = var7;
      this.field21292 = var8;
      this.field21293 = var9;
      this.field21294 = var6;
      this.field20883 = true;
   }

   @Override
   public void method13028(int var1, int var2) {
      super.method13028(var1, var2);
      this.field21289.changeDirection(!this.method13216() ? Direction.BACKWARDS : Direction.FORWARDS);
      boolean var5 = this.method13216() || var1 > this.method13271() + this.getWidthA() - 62;
      this.method13215(var5);
      if (this.field21290.getDirection() == Direction.FORWARDS) {
         this.method13215(false);
         this.setXA(Math.round((float)this.getWidthA() * QuadraticEasing.easeInQuad(this.field21290.calcPercent(), 0.0F, 1.0F, 1.0F)));
         if (this.field21290.calcPercent() == 1.0F) {
            this.method13037();
         }
      }
   }

   public void method13608() {
      this.field21290.changeDirection(Direction.FORWARDS);
   }

   @Override
   public void draw(float var1) {
      RenderUtil.drawRoundedRect2(
         (float)this.xA,
         (float)this.yA,
         (float)this.widthA,
         (float)this.heightA,
              ColorUtils.applyAlpha(ColorUtils.method17691(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.03F), this.field21289.calcPercent())
      );
      RenderUtil.drawString(
         ResourceRegistry.JelloLightFont20,
         (float)(this.xA + 68),
         (float)(this.yA + 14),
         this.field21291,
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F)
      );
      RenderUtil.drawString(
         ResourceRegistry.JelloLightFont14,
         (float)(this.xA + 68),
         (float)(this.yA + 38),
         "x:" + this.field21292.getX() + " z:" + this.field21292.getZ(),
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F)
      );
      int var5 = this.widthA - 43;
      float var6 = !this.method13216() ? 0.2F : 0.4F;
      RenderUtil.drawRoundedRect2(
         (float)(this.xA + var5), (float)(this.yA + 27), 20.0F, 2.0F, ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var6)
      );
      RenderUtil.drawRoundedRect2(
         (float)(this.xA + var5), (float)(this.yA + 27 + 5), 20.0F, 2.0F, ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var6)
      );
      RenderUtil.drawRoundedRect2(
         (float)(this.xA + var5), (float)(this.yA + 27 + 10), 20.0F, 2.0F, ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var6)
      );
      RenderUtil.method11438(
         (float)(this.xA + 35),
         (float)(this.yA + this.heightA / 2),
         20.0F,
              ColorUtils.method17690(this.field21293, ClientColors.DEEP_TEAL.getColor(), 0.9F)
      );
      RenderUtil.method11438((float)(this.xA + 35), (float)(this.yA + this.heightA / 2), 17.0F, this.field21293);
      RenderUtil.drawRoundedRect(
         (float)this.xA, (float)this.yA, (float)this.widthA, (float)this.heightA, 14.0F, var1 * 0.2F * this.field21289.calcPercent()
      );
      super.draw(var1);
   }
}
