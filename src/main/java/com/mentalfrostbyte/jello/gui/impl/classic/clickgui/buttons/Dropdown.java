package com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons;

import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.dropdown.Class7262;
import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;

import java.util.ArrayList;
import java.util.List;

public class Dropdown extends Element {
   public static final ColorHelper field21342 = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.DEEP_TEAL.getColor()).method19414(FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2);
   public List<String> field21343 = new ArrayList<String>();
   public int field21344 = 0;
   public boolean field21345;
   public boolean field21346;

   public Dropdown(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, List<String> var7, int var8) {
      super(var1, var2, var3, var4, var5, var6, field21342, false);
      this.field21343 = var7;
      this.field21344 = var8;
      this.method13663();
   }

   private void method13663() {
      this.getChildren().clear();
      this.font = Resources.regular15;
      Button var3;
      this.addToList(var3 = new Button(this, "dropdownButton", 0, 0, this.getHeightA(), this.getHeightA(), this.textColor));
      var3.setSize((var1, var2) -> {
         var1.setXA(0);
         var1.setYA(0);
         var1.setWidthA(this.getWidthA());
         var1.setHeightA(this.getHeightA());
      });
      var3.onClick((var1, var2) -> this.method13674(!this.method13673()));

      for (String var5 : this.field21343) {
         Button var6;
         this.addToList(
            var6 = new Button(
               this,
               var5,
               0,
               this.getHeightA(),
               this.getWidthA(),
               17,
               new ColorHelper(
                  -14540254,
                  this.textColor.getPrimaryColor(),
                  this.textColor.getPrimaryColor(),
                  ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                  FontSizeAdjust.field14488,
                  FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2
               ),
               var5,
                    Resources.regular12
            )
         );
         var6.method13034(8);
         var6.onClick((var2, var3x) -> {
            int var6x = this.method13671();
            this.method13672(this.field21343.indexOf(var5));
            this.method13674(false);
            if (var6x != this.method13671()) {
               this.callUIHandlers();
            }
         });
      }

      this.method13246(new Class7262(1));
   }

   private int method13664() {
      return this.method13665();
   }

   private int method13665() {
      return this.method13673() ? this.getHeightA() * (this.field21343.size() + 1) : this.getHeightA();
   }

   private int method13666() {
      return 0;
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      if (!this.isFocused() && this.method13673()) {
         this.method13674(false);
      }

      super.updatePanelDimensions(newHeight, newWidth);
   }

   @Override
   public void draw(float partialTicks) {
      RenderUtil.drawRoundedRect(
         (float)this.getXA(),
         (float)this.getYA(),
         (float)(this.getXA() + this.getWidthA()),
         (float)(this.getYA() + this.getHeightA()),
         -14540254
      );
      RenderUtil.method11428(
         (float)this.getXA(),
         (float)this.getYA(),
         (float)(this.getXA() + this.getWidthA()),
         (float)(this.getYA() + this.getHeightA()),
         ClientColors.DEEP_TEAL.getColor()
      );
      if (this.method13114(this.getHeightO(), this.getWidthO()) && this.getWidthO() - this.method13272() < this.getHeightA()) {
         RenderUtil.method11428(
            (float)(this.getXA() + 1),
            (float)(this.getYA() + 1),
            (float)(this.getXA() + this.getWidthA() - 1),
            (float)(this.getYA() + this.getHeightA() - 1),
            RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.25F)
         );
      }

      int var4 = this.getXA() + this.getWidthA() - 11;
      int var5 = this.getYA() + this.getHeightA() - 12;
      if (!this.method13673()) {
         RenderUtil.method11434((float)var4, (float)var5, (float)(var4 + 6), (float)var5, (float)(var4 + 3), (float)(var5 + 3), ClientColors.MID_GREY.getColor());
      } else {
         RenderUtil.method11434(
            (float)var4, (float)(var5 + 3), (float)(var4 + 6), (float)(var5 + 3), (float)(var4 + 3), (float)var5, ClientColors.MID_GREY.getColor()
         );
      }

      for (CustomGuiScreen var7 : this.getChildren()) {
         if (!var7.getName().equals("dropdownButton")) {
            var7.setSelfVisible(this.field21345);
         }
      }

      if (this.getText() != null) {
         RenderUtil.scissorScreenArea(this);
         RenderUtil.drawString(
            this.getFont(),
            (float)(this.getXA() + 7),
            (float)(this.getYA() + (this.getHeightA() - this.getFont().getHeight()) / 2),
            this.getText(),
            RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * 0.5F)
         );
         RenderUtil.restoreScissor();
      }

      if (!this.method13673()) {
         RenderUtil.scissorScreenArea(this);
      }

      super.draw(partialTicks);
      if (!this.method13673()) {
         RenderUtil.restoreScissor();
      }
   }

   public List<String> method13667() {
      return this.field21343;
   }

   public void method13668(String var1, int var2) {
      this.method13667().add(var2, var1);
      this.method13663();
   }

   public void method13669(String var1) {
      this.method13668(var1, this.field21343.size());
   }

   public <E extends Enum<E>> void method13670(Class<E> var1) {
      this.field21343.clear();

      for (Enum var7 : var1.getEnumConstants()) {
         String var8 = var7.toString().substring(0, 1).toUpperCase() + var7.toString().substring(1).toLowerCase();
         this.method13668(var8, var7.ordinal());
      }
   }

   public int method13671() {
      return this.field21344;
   }

   public void method13672(int var1) {
      this.field21344 = var1;
   }

   public boolean method13673() {
      return this.field21345;
   }

   public void method13674(boolean var1) {
      this.field21345 = var1;
   }

   @Override
   public String getText() {
      return this.method13667().size() <= 0 ? null : this.method13667().get(this.method13671());
   }

   @Override
   public boolean method13114(int var1, int var2) {
      var1 -= this.method13271();
      var2 -= this.method13272();
      return var1 >= 0 && var1 <= this.getWidthA() && var2 >= 0 && var2 <= this.method13665();
   }
}
