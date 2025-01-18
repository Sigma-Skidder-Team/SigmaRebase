package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.*;
import com.mentalfrostbyte.jello.gui.unmapped.*;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.gui.jello.BrainFreeze;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import totalcross.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JelloClickGUI extends Screen {
   public ClickGUIBlurOverlay blurOverlay;
   private static Minecraft mc = MinecraftClient.getInstance();
   private static Animation field20942;
   private static boolean field20943;
   private static boolean field20944;
   private Map<ModuleCategory, JelloClickGUIPanels> categoryPanel = new HashMap<ModuleCategory, JelloClickGUIPanels>();
   public MusicPlayer musicPlayer;
   public BrainFreezeGui brainFreeze;
   public ConfigButtonOnClickGui configButton;
   public ModuleSettingUI moduleSettingUI;
   public AlertPanel dependenciesAlert;
   private static boolean field20951 = true;
   public JelloClickGUIPanels jelloClickGUIPanels = null;

   public JelloClickGUI() {
      super("JelloScreen");
      field20944 = field20944 | !field20943;
      int x = 30;
      int y = 30;
      this.addToList(this.brainFreeze = new BrainFreezeGui(this, "brainFreeze"));

      for (Module module : Client.getInstance().moduleManager.getModuleMap().values()) {
         if (!this.categoryPanel.containsKey(module.getAdjustedCategoryBasedOnClientMode())) {
            JelloClickGUIPanels clickGUIPanels = new JelloClickGUIPanels(this,
                  module.getAdjustedCategoryBasedOnClientMode().getName(), x, y,
                  module.getAdjustedCategoryBasedOnClientMode());
            this.categoryPanel.put(module.getAdjustedCategoryBasedOnClientMode(), clickGUIPanels);
            this.addToList(clickGUIPanels);

            x += clickGUIPanels.getWidthA() + 10;
            if (this.categoryPanel.size() == 4) {
               x = 30;
               y += clickGUIPanels.getHeightA() - 20;
            }

            clickGUIPanels.method13507(var2 -> this.runThisOnDimensionUpdate(() -> {
               this.addToList(this.moduleSettingUI = new ModuleSettingUI(this, "settings", 0, 0, this.widthA,
                     this.heightA, var2));
               this.moduleSettingUI.method13292(true);
            }));
         }
      }

      this.addToList(this.musicPlayer = new MusicPlayer(this, "musicPlayer"));
      this.musicPlayer.method13215(true);
      PNGIconButton var9;
      this.addToList(var9 = new PNGIconButton(this, "more", this.getWidthA() - 69, this.getHeightA() - 55, 55, 41,
            Resources.optionsPNG1));
      var9.getTextColor().method19406(ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F));
      var9.method13300(false);
      this.musicPlayer.setEnabled(field20951);
      var9.doThis((var1, var2) -> this.runThisOnDimensionUpdate(() -> {
         if (this.configButton != null && this.hasChild(this.configButton)) {
            this.method13234(this.configButton);
         } else {
            this.addToList(this.configButton = new ConfigButtonOnClickGui(this, "morepopover", this.getWidthA() - 14,
                  this.getHeightA() - 14));
            this.configButton.method13292(true);
         }
      }));
      field20942 = new Animation(450, 125);
      this.blurOverlay = new ClickGUIBlurOverlay(this, this, "overlay");
      ColorUtils.blur();
      ColorUtils.setShaderParamsRounded(field20942.calcPercent());
   }

   public boolean hasJelloMusicRequirements() {
      if (Client.getInstance().musicManager.hasPython() && Client.getInstance().musicManager.hasVCRedist()) {
         return false;
      } else if (this.dependenciesAlert == null) {
         this.runThisOnDimensionUpdate(() -> {
            List<MiniAlert> var3 = new ArrayList();
            var3.add(new MiniAlert(AlertType.HEADER, "Music", 40));
            var3.add(new MiniAlert(AlertType.FIRST_LINE, "Jello Music requires:", 20));
            if (!Client.getInstance().musicManager.hasPython()) {
               var3.add(new MiniAlert(AlertType.FIRST_LINE, "- Python 3.12.5", 30));
            }

            if (!Client.getInstance().musicManager.hasVCRedist()) {
               var3.add(new MiniAlert(AlertType.FIRST_LINE, "- Visual C++ 2010 x86", 30));
            }

            var3.add(new MiniAlert(AlertType.BUTTON, "Download", 55));
            this.method13233(this.dependenciesAlert = new AlertPanel(this, "music", true, "Dependencies.",
                  var3.toArray(new MiniAlert[0])));
            this.dependenciesAlert.addUIHandler(var0 -> {
               if (!Client.getInstance().musicManager.hasPython()) {
                  Util.getOSType().openLink("https://www.python.org/ftp/python/3.12.5/python-3.12.5-macos11.pkg");
               }

               if (!Client.getInstance().musicManager.hasVCRedist()) {
                  Util.getOSType().openLink("https://www.microsoft.com/en-US/Download/confirmation.aspx?id=26999");
               }
            });
            this.dependenciesAlert.method13604(var1 -> new Thread(() -> {
               this.runThisOnDimensionUpdate(() -> {
                  this.method13236(this.dependenciesAlert);
                  this.dependenciesAlert = null;
               });
            }).start());
            this.dependenciesAlert.method13603(true);
         });
         return true;
      } else {
         return true;
      }
   }

   public void method13315() {
      for (JelloClickGUIPanels panel : this.categoryPanel.values()) {
         panel.method13504();
      }
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      this.musicPlayer.setEnabled(
            this.musicPlayer.getWidthA() < this.getWidthA() && this.musicPlayer.getHeightA() < this.getHeightA());
      super.updatePanelDimensions(newHeight, newWidth);
      ColorUtils.setShaderParamsRounded(Math.min(1.0F, field20942.calcPercent() * 4.0F));
      this.brainFreeze.setEnabled(Client.getInstance().moduleManager.getModuleByClass(BrainFreeze.class).isEnabled());
      if (this.configButton != null) {
         int var5 = newHeight - this.configButton.method13271();
         int var6 = newWidth - this.configButton.method13272();
         boolean var7 = var5 >= -10 && var6 >= -10;
         if (!var7) {
            this.configButton.method13613();
         }
      }

      if (this.configButton != null && this.configButton.method13614()) {
         this.method13236(this.configButton);
         this.configButton = null;
      }

      if (field20942.getDirection() == Direction.BACKWARDS && this.moduleSettingUI != null
            && !this.moduleSettingUI.field20671) {
         this.moduleSettingUI.field20671 = true;
      }

      if (this.moduleSettingUI != null && this.moduleSettingUI.field20671
            && this.moduleSettingUI.animation1.calcPercent() == 0.0F) {
         this.runThisOnDimensionUpdate(() -> {
            this.method13236(this.moduleSettingUI);
            this.moduleSettingUI = null;
         });
      }

      if (field20944) {
         float var8 = (float) (0.03F * (60.0 / (double) this.method13313()));
         Direction var9 = field20942.getDirection();
         field20942.changeDirection(!field20943 ? Direction.FORWARDS : Direction.BACKWARDS);
         if (field20942.calcPercent() <= 0.0F && field20943) {
            field20943 = false;
            this.method13316(field20943);
         } else if (field20942.calcPercent() >= 1.0F && field20942.getDirection() == var9) {
            field20943 = true;
            this.method13316(field20943);
         }
      }

      if (field20944 && field20943) {
         ColorUtils.resetShaders();
      }
   }

   @Override
   public int method13313() {
      return Minecraft.getFps();
   }

   @Override
   public JSONObject toConfigWithExtra(JSONObject config) {
      ColorUtils.resetShaders();
      this.method13234(this.blurOverlay);
      return super.toConfigWithExtra(config);
   }

   @Override
   public void loadConfig(JSONObject config) {
      super.loadConfig(config);
   }

   private void method13316(boolean var1) {
      field20944 = false;
      if (!var1) {
         mc.displayGuiScreen(null);
      }
   }

   @Override
   public boolean onClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton <= 1) {
         return super.onClick(mouseX, mouseY, mouseButton);
      } else {
         this.keyPressed(mouseButton);
         return false;
      }
   }

   @Override
   public void keyPressed(int keyCode) {
      super.keyPressed(keyCode);
      int var4 = Client.getInstance().moduleManager.getMacOSTouchBar().getKeybindFor(ClickGui.class);
      if (keyCode == 256 || keyCode == var4 && this.moduleSettingUI == null && !this.method13227()) {
         if (field20944) {
            field20943 = !field20943;
         }

         field20944 = true;
      }
   }

   public float method13317(float var1, float var2) {
      return field20942.getDirection() != Direction.BACKWARDS
            ? (float) (Math.pow(2.0, (double) (-10.0F * var1))
                  * Math.sin((double) (var1 - var2 / 4.0F) * (Math.PI * 2) / (double) var2) + 1.0)
            : QuadraticEasing.easeOutQuad(var1, 0.0F, 1.0F, 1.0F);
   }

   @Override
   public void draw(float var1) {
      float var4 = field20944 && !field20943
            ? this.method13317(field20942.calcPercent(), 0.8F) * 0.5F + 0.5F
            : (!field20944 ? 1.0F : this.method13317(field20942.calcPercent(), 1.0F));
      float var5 = 0.2F * var1 * var4;
      RenderUtil.drawRoundedRect(
            (float) this.xA,
            (float) this.yA,
            (float) (this.xA + this.widthA),
            (float) (this.yA + this.heightA),
            ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var5));
      Object var6 = null;
      float var7 = 1.0F;
      if (this.moduleSettingUI != null) {
         float var8 = EasingFunctions.easeOutBack(this.moduleSettingUI.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
         if (this.moduleSettingUI.animation.getDirection() == Direction.BACKWARDS) {
            var8 = MathHelper.calculateBackwardTransition(this.moduleSettingUI.animation.calcPercent(), 0.0F, 1.0F,
                  1.0F);
         }

         var7 -= this.moduleSettingUI.animation.calcPercent() * 0.1F;
         var4 *= 1.0F + var8 * 0.2F;
      }

      if (Client.getInstance().moduleManager.getConfigurationManager().getCurrentConfig() != null) {
         String var12 = Client.getInstance().moduleManager.getConfigurationManager().getCurrentConfig().getName;
         RenderUtil.drawString(
               ResourceRegistry.JelloLightFont20,
               (float) (this.widthA - ResourceRegistry.JelloLightFont20.getWidth(var12) - 80),
               (float) (this.heightA - 47),
               var12,
               ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                     0.5F * Math.max(0.0F, Math.min(1.0F, var4))));
      }

      for (CustomGuiScreen var9 : this.getChildren()) {
         float var10 = (float) (var9.getYA() + var9.getHeightA() / 2 - mc.getWindow().getHeight() / 2)
               * (1.0F - var4) * 0.5F;
         float var11 = (float) (var9.getXA() + var9.getWidthA() / 2 - mc.getWindow().getWidth() / 2) * (1.0F - var4)
               * 0.5F;
         var9.method13286((int) var11, (int) var10);
         var9.method13279(1.5F - var4 * 0.5F, 1.5F - var4 * 0.5F);
      }

      super.draw(var1 * Math.min(1.0F, var4) * var7);
      if (this.jelloClickGUIPanels != null) {
         this.jelloClickGUIPanels.method13292(false);
      }

      this.blurOverlay.method13292(false);
      this.method13234(this.blurOverlay);
   }
}
