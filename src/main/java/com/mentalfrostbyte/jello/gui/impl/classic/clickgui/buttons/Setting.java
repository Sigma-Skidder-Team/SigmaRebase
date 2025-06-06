package com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons;

import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import org.lwjgl.opengl.GL11;

public class Setting extends Element {
   private static String[] field21332;
   public Animation field21333 = new Animation(1200, 1200, Animation.Direction.BACKWARDS);

   public Setting(CustomGuiScreen var1, String var2, int var3, int var4) {
      super(var1, var2, var3, var4, 20, 20, false);
   }

   @Override
   public void draw(float partialTicks) {
      this.field21333.changeDirection(!this.method13298() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
      if (this.field21333.calcPercent() == 1.0F && this.method13298()) {
         this.field21333 = new Animation(1200, 1200, Animation.Direction.FORWARDS);
      }

      int var4 = this.getXA() + 10;
      int var5 = this.getYA() + 10;
      GL11.glPushMatrix();
      GL11.glTranslatef((float)var4, (float)var5, 0.0F);
      GL11.glRotatef(this.field21333.calcPercent() * 360.0F, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef((float)(-var4), (float)(-var5), 0.0F);
      RenderUtil.drawImage((float)this.xA, (float)this.yA, 20.0F, 20.0F, !this.method13298() ? Resources.gear : Resources.gear2);
      GL11.glPopMatrix();
      super.draw(partialTicks);
   }
}
