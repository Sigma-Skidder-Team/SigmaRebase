package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.*;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Class4358 extends UIBase {
   public Animation field21302;
   public int field21303;
   public int field21304;
   public int field21305;
   public int field21306;
   public String field21307;
   public MusicTabs field21308;
   public Class6984 field21309;
   public boolean field21311 = false;
   private final List<Class7875> field21312 = new ArrayList<>();

   public Class4358(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
      super(var1, var2, var3, var4, var5, var6, false);
      this.field21305 = 500;
      this.field21306 = 600;
      this.field21304 = (var5 - this.field21305) / 2;
      this.field21303 = (var6 - this.field21306) / 2;
      UIInput var10;
      this.addToList(
         var10 = new UIInput(
            this, "search", this.field21304 + 30, this.field21303 + 30 + 50, this.field21305 - 30 * 2, 60, UIInput.field20741, "", "Search..."
         )
      );
      var10.method13151(var2x -> {
         this.field21307 = var10.getTypedText();
         this.field21308.method13512(0);
      });
      var10.method13242();
      this.addToList(
         this.field21308 = new MusicTabs(
            this, "mods", this.field21304 + 30, this.field21303 + 30 + 120, this.field21305 - 30 * 2, this.field21306 - 30 * 2 - 120
         )
      );
      int var11 = 10;

      for (Entry var13 : GuiManager.screenToScreenName.entrySet()) {
         Class6984 var14 = new Class6984((Class<? extends Screen>)var13.getKey());
         ColorHelper var15 = new ColorHelper(ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.02F), -986896)
            .method19410(ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F))
            .method19412(Class2218.field14492);
         ButtonPanel var16;
         this.field21308
            .addToList(
               var16 = new ButtonPanel(this.field21308, var14.method21596(), 0, var11++ * 55, this.field21308.getWidthA(), 55, var15, var14.method21596())
            );
         var16.doThis((var2x, var3x) -> {
            for (Entry var7 : GuiManager.screenToScreenName.entrySet()) {
               Class6984 var8 = new Class6984((Class<? extends Screen>)var7.getKey());
               if (var8.method21596().equals(var16.getName()) && !this.field21311) {
                  this.field21309 = var8;
                  this.field21311 = true;
                  break;
               }
            }
         });
      }

      var11 += 50;

      for (Module var19 : Client.getInstance().moduleManager.getModuleMap().values()) {
         ColorHelper var20 = new ColorHelper(16777215, -986896).method19410(ClientColors.DEEP_TEAL.getColor()).method19412(Class2218.field14488);
         ButtonPanel var21;
         this.field21308
            .addToList(
               var21 = new ButtonPanel(
                  this.field21308, var19.getName(), 0, var11++ * 40, this.field21308.getWidthA(), 40, var20, new Class6984(var19).method21596()
               )
            );
         var21.method13034(10);
         var21.doThis((var2x, var3x) -> {
            for (Module var7 : Client.getInstance().moduleManager.getModuleMap().values()) {
               if (var7.getName().equals(var21.getTypedText()) && !this.field21311) {
                  this.field21309 = new Class6984(var7);
                  this.field21311 = true;
                  break;
               }
            }
         });
      }

      this.field21302 = new Animation(200, 120);
      this.setListening(false);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      if (this.method13212()
         && (newHeight < this.field21304 || newWidth < this.field21303 || newHeight > this.field21304 + this.field21305 || newWidth > this.field21303 + this.field21306)) {
         this.field21311 = true;
      }

      this.field21302.changeDirection(this.field21311 ? Direction.BACKWARDS : Direction.FORWARDS);
      Map<String, ButtonPanel> var5 = new TreeMap();
      Map<String, ButtonPanel> var6 = new TreeMap();
      Map<String, ButtonPanel> var7 = new TreeMap();
      List<ButtonPanel> var8 = new ArrayList();

      for (CustomGuiScreen var10 : this.field21308.getChildren()) {
         if (!(var10 instanceof VerticalScrollBar)) {
            for (CustomGuiScreen var12 : var10.getChildren()) {
               if (var12 instanceof ButtonPanel) {
                  ButtonPanel var13 = (ButtonPanel)var12;
                  boolean var14 = var13.getHeightA() != 40;
                  if (!var14 || this.field21307 != null && (this.field21307 == null || this.field21307.length() != 0)) {
                     if (!var14 && this.method13622(this.field21307, var13.getTypedText())) {
                        var6.put(var13.getTypedText(), var13);
                     } else if (!var14 && this.method13621(this.field21307, var13.getTypedText())) {
                        var7.put(var13.getTypedText(), var13);
                     } else {
                        var8.add(var13);
                     }
                  } else {
                     var5.put(var13.getTypedText(), var13);
                  }
               }
            }
         }
      }

      int var15 = var5.size() <= 0 ? 0 : 10;

      for (ButtonPanel var20 : var5.values()) {
         var20.setEnabled(true);
         var20.setYA(var15);
         var15 += var20.getHeightA();
      }

      if (var5.size() > 0) {
         var15 += 10;
      }

      for (ButtonPanel var21 : var6.values()) {
         var21.setEnabled(true);
         var21.setYA(var15);
         var15 += var21.getHeightA();
      }

      for (ButtonPanel var22 : var7.values()) {
         var22.setEnabled(true);
         var22.setYA(var15);
         var15 += var22.getHeightA();
      }

      for (ButtonPanel var23 : var8) {
         var23.setEnabled(false);
      }

      super.updatePanelDimensions(newHeight, newWidth);
   }

   private boolean method13621(String var1, String var2) {
      return var1 != null && var1 != "" && var2 != null ? var2.toLowerCase().contains(var1.toLowerCase()) : true;
   }

   private boolean method13622(String var1, String var2) {
      return var1 != null && var1 != "" && var2 != null ? var2.toLowerCase().startsWith(var1.toLowerCase()) : true;
   }

   @Override
   public void draw(float partialTicks) {
      partialTicks = this.field21302.calcPercent();
      float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
      if (this.field21311) {
         var4 = QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F);
      }

      this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
      if (partialTicks == 0.0F && this.field21311) {
         this.method13624(this.field21309);
      }

      RenderUtil.drawRoundedRect(
         (float)this.xA,
         (float)this.yA,
         (float)this.widthA,
         (float)this.heightA,
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F * partialTicks)
      );
      super.method13224();
      RenderUtil.drawRoundedRect(
         (float)this.field21304,
         (float)this.field21303,
         (float)this.field21305,
         (float)this.field21306,
         10.0F,
              ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
      );
      RenderUtil.drawString(
         ResourceRegistry.JelloLightFont36,
         (float)(30 + this.field21304),
         (float)(30 + this.field21303),
         "Select mod to bind",
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.7F)
      );
      super.draw(partialTicks);
   }

   public final void method13623(Class7875 var1) {
      this.field21312.add(var1);
   }

   public final void method13624(Class6984 var1) {
      for (Class7875 var5 : this.field21312) {
         var5.method26411(this, var1);
      }
   }
}
