package com.mentalfrostbyte.jello.misc;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.module.impl.gui.classic.TabGUI;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.TrueTypeFont;

public class   CategoryDrawPartBackground {
   public final TrueTypeFont font;
   public final int priority;
   private boolean expanded = false;
   public Animation animation = new Animation(300, 300);

   public CategoryDrawPartBackground(int priority) {
      this.font = Resources.bold16;
      this.priority = priority;
   }

   public int getWidth() {
      return 106;
   }

   public int getStartX() {
      return TabGUI.calculateStartX(this.priority);
   }

   public int getStartY() {
      return 30;
   }

   public void expand() {
      this.expanded = true;
      this.animation.changeDirection(Direction.BACKWARDS);
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public boolean isFullyCollapsed() {
      return this.expanded && this.animation.calcPercent() == 0.0F;
   }

   public int getHeight() {
      return 100;
   }

   public void render(float partialTicks) {
      float transitionFactor = MathHelper.calculateTransition(this.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
      if (this.animation.getDirection() == Direction.BACKWARDS) {
         transitionFactor = MathHelper.calculateBackwardTransition(this.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
      }

      RenderUtil.startScissor((float)this.getStartX(), (float)this.getStartY(), (float)this.getWidth() * transitionFactor, (float)this.getHeight());
      drawContent(partialTicks);
      RenderUtil.endScissor();
   }

   public void drawContent(float partialTicks) {
   }
}
