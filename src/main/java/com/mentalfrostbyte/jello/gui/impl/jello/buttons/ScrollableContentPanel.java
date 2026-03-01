package com.mentalfrostbyte.jello.gui.impl.jello.buttons;

import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.VerticalScrollBar;
import com.mentalfrostbyte.jello.gui.combined.AnimatedIconPanel;
import com.mentalfrostbyte.jello.gui.combined.ContentSize;
import com.mentalfrostbyte.jello.gui.base.interfaces.IWidthSetter;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import org.newdawn.slick.TrueTypeFont;

public class ScrollableContentPanel extends AnimatedIconPanel {
   private boolean onlyUpdateWhenSelfVisible = false;
   public CustomGuiScreen buttonList;
   public VerticalScrollBar scrollBar;
   private boolean scissor = true;
   public int scrollStep = 35;
   public boolean scrollable = false;

   public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6) {
      super(var1, name, var3, var4, var5, var6, false);
      this.pack();
   }

   public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7) {
      super(var1, name, var3, var4, var5, var6, var7, false);
      this.pack();
   }

   public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
      super(var1, name, var3, var4, var5, var6, var7, var8, false);
      this.pack();
   }

   public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
      super(var1, name, var3, var4, var5, var6, var7, var8, var9, false);
      this.pack();
   }

   private void pack() {
      this.getChildren().add(this.buttonList = new CustomGuiScreen(this, "content", 0, 0, this.widthA, this.heightA));
      this.buttonList.setSize(new ContentSize());
      this.getChildren().add(this.scrollBar = new VerticalScrollBar(this, 11));
      this.scrollBar.setReAddChildren(true);
   }

   public void setScrollBarOffset(int var1) {
      this.scrollBar.offset = var1;
   }

   public int getScrollBarOffset() {
      return this.scrollBar != null ? this.scrollBar.offset : 0;
   }

   public void setOnlyUpdateWhenSelfVisible(boolean var1) {
      this.onlyUpdateWhenSelfVisible = var1;
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      if (!this.onlyUpdateWhenSelfVisible || this.isSelfVisible()) {
         super.updatePanelDimensions(newHeight, newWidth);
         this.buttonList.setYA(-1 * this.scrollBar.method13162());

         for (CustomGuiScreen var6 : this.getButtonList().getChildren()) {
            for (IWidthSetter var8 : var6.method13260()) {
               var8.setWidth(var6, this);
            }
         }
      }
   }

   public void setScissor(boolean var1) {
      this.scissor = var1;
   }

   public boolean isScissor() {
      return this.scissor;
   }

   @Override
   public void draw(float partialTicks) {
      this.method13224();
      if (!this.onlyUpdateWhenSelfVisible || this.isSelfVisible()) {
         if (this.scissor) {
            RenderUtil.scissorScreenArea(this);
         }

         super.draw(partialTicks);
         if (this.scissor) {
            RenderUtil.restoreScissor();
         }
      }
   }

   @Override
   public void addToList(CustomGuiScreen var1) {
      this.buttonList.addToList(var1);
   }

   @Override
   public boolean hasChild(CustomGuiScreen child) {
      return this.buttonList.hasChild(child);
   }

   @Override
   public boolean isntQueue(String var1) {
      return this.buttonList.isntQueue(var1);
   }

   public CustomGuiScreen getButtonList() {
      return this.buttonList;
   }

   public void setScrollable(boolean scrollable) {
      this.scrollable = scrollable;
   }
}
