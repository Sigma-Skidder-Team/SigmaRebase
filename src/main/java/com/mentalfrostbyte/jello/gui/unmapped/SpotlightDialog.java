package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;

public class SpotlightDialog extends UIBase {
   public UIInput query;
   public String field20640;

   public SpotlightDialog(CustomGuiScreen screen, String iconName, int var3, int var4, int width, int height,
         boolean var7) {
      super(screen, iconName, var3, var4, width, height, var7);
      this.addToList(this.query = new UIInput(this, "search", 50, 0, width - 60, height - 2, UIInput.field20741, "",
            "Search..."));
      this.query.method13156(false);
      this.query.method13151(var1x -> this.field20640 = this.query.getTypedText());
   }

   @Override
   public void draw(float var1) {
      this.query.method13145(true);
      int var4 = 10;
      RenderUtil.drawRoundedRect(
            (float) (this.xA + var4 / 2),
            (float) (this.yA + var4 / 2),
            (float) (this.widthA - var4),
            (float) (this.heightA - var4),
            9.0F,
            var1 * 0.9F);
      RenderUtil.drawRoundedRect(
            (float) (this.xA + var4 / 2),
            (float) (this.yA + var4 / 2),
            (float) (this.widthA - var4),
            (float) (this.heightA - var4),
            30.0F,
            var1 * 0.4F);
      RenderUtil.drawRoundedRect(
            (float) this.xA,
            (float) this.yA,
            (float) this.widthA,
            (float) this.heightA,
            (float) var4,
            ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.97F));
      RenderUtil.drawImage(
            (float) (this.xA + 20),
            (float) (this.yA + 20),
            20.0F,
            20.0F,
            Resources.searchPNG,
            ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F));
      ArrayList<Module> var5 = this.method13064();
      if (!var5.isEmpty() && this.method13067(this.field20640, ((Module) var5.get(0)).getName())) {
         String var6 = ((Module) var5.get(0)).getName();
         String var7 = this.field20640
               + ((Module) var5.get(0)).getName().substring(this.field20640.length(), var6.length())
               + (!((Module) var5.get(0)).isEnabled() ? " - Disabled" : " - Enabled");
         RenderUtil.drawString(
               this.query.getFont(),
               (float) (this.xA + 54),
               (float) (this.yA + 14),
               var7,
               ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.25F));
      }

      super.draw(var1);
   }

   public ArrayList<Module> method13064() {
      ArrayList<Module> var3 = new ArrayList<>();
      if (this.field20640 != null && !this.field20640.isEmpty()) {
         for (Module var5 : Client.getInstance().moduleManager.getModuleMap().values()) {
            if (this.method13067(this.field20640, var5.getName())) {
               var3.add(var5);
            }
         }

         return var3;
      } else {
         return var3;
      }
   }

   @Override
   public void keyPressed(int keyCode) {
      super.keyPressed(keyCode);
      if (keyCode == 257) {
         ArrayList var4 = this.method13064();
         if (var4.size() > 0) {
            ((Module) var4.get(0)).toggle();
         }

         MinecraftClient.getInstance().displayGuiScreen(null);
      }
   }

   private boolean method13066(String var1, String var2) {
      return var1 != null && var1 != "" && var2 != null ? var2.toLowerCase().contains(var1.toLowerCase()) : true;
   }

   private boolean method13067(String var1, String var2) {
      return var1 != null && var1 != "" && var2 != null ? var2.toLowerCase().startsWith(var1.toLowerCase()) : true;
   }
}
