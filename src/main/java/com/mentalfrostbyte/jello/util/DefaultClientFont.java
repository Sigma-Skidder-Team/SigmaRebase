package com.mentalfrostbyte.jello.util;

import java.awt.Font;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class DefaultClientFont extends TrueTypeFont {
   public final int field31945;
   public Minecraft field31946 = Minecraft.getInstance();

   public DefaultClientFont(int var1) {
      super(new Font("Arial", 0, var1), false);
      this.field31945 = var1;
   }

   private int method23949(char var1) {
      return this.field31946.fontRenderer.getStringWidth(String.valueOf(var1)) * this.field31945;
   }

   @Override
   public int getWidth(String var1) {
      return this.field31946.fontRenderer.getStringWidth(var1) * this.field31945;
   }

   @Override
   public int getHeight() {
      return 9 * this.field31945;
   }

   @Override
   public int getHeight(String var1) {
      return 9 * this.field31945;
   }

   @Override
   public int getLineHeight() {
      return 9 * this.field31945;
   }

   @Override
   public void drawString(float x, float y, String string, Color color) {
      this.drawString(x, y, string, color, 0, string.length() - 1);
   }

   @Override
   public void drawString(float x, float y, String string, Color color, int startIndex, int endIndex) {
      GL11.glPushMatrix();
      GL11.glScalef((float)this.field31945, (float)this.field31945, 0.0F);
      GL11.glTranslatef(-x / (float)this.field31945, -y / (float)this.field31945 + 1.0F, 0.0F);
      this.field31946
         .fontRenderer
         .renderString(
                 string,
                 x,
                 y,
            new java.awt.Color(color.r, color.g, color.b, color.a).getRGB(),
            new MatrixStack().getLast().getMatrix(),
            false,
            false
         );
      GL11.glPopMatrix();
   }

   @Override
   public void drawString(float x, float y, String string) {
      this.drawString(x, y, string, Color.white);
   }
}
