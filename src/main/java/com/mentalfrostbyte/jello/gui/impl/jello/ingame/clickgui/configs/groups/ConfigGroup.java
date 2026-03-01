package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.configs.groups;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.TextButton;
import com.mentalfrostbyte.jello.gui.base.elements.impl.dropdown.Class7262;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.LoadingIndicator;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.configs.ConfigScreen;
import com.mentalfrostbyte.jello.managers.OnlineProfilesManager;
import com.mentalfrostbyte.jello.managers.ProfileManager;
import com.mentalfrostbyte.jello.managers.util.profile.Profile;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.system.math.SmoothInterpolator;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;

public class ConfigGroup extends Element {
   public Animation field20703 = new Animation(300, 200, Animation.Direction.BACKWARDS);
   private final int field20704;
   private final ScrollableContentPanel field20705;
   public static OnlineProfilesManager onlineProfilesManager;
   private final LoadingIndicator field20707;

   public ConfigGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
      super(var1, var2, var3, var4, var5, 0, ColorHelper.field27961, false);
      TextButton blankButton;
      this.addToList(
         blankButton = new TextButton(this, "blankButton", 25, 0, ResourceRegistry.JelloLightFont20.getWidth("Blank"), 30, ColorHelper.field27961, "Blank", ResourceRegistry.JelloLightFont20)
      );
      blankButton.onClick((var1x, var2x) -> {
         ConfigScreen var5x = (ConfigScreen)this.getParent();
         var5x.method13612();
      });
      TextButton var10;
      this.addToList(
         var10 = new TextButton(
            this,
            "dupeButton",
            var5 - 25 - ResourceRegistry.JelloLightFont20.getWidth("Duplicate"),
            0,
            ResourceRegistry.JelloLightFont20.getWidth("Duplicate"),
            30,
            ColorHelper.field27961,
            "Duplicate",
            ResourceRegistry.JelloLightFont20
         )
      );
      var10.onClick((var1x, var2x) -> {
         ConfigScreen var5x = (ConfigScreen)this.getParent();
         var5x.method13610();
      });
      this.addToList(this.field20707 = new LoadingIndicator(this, "loading", (var5 - 30) / 2, 100, 30, 30));
      this.addToList(this.field20705 = new ScrollableContentPanel(this, "defaultProfiles", 0, 40, var5, var6 - 40));
      onlineProfilesManager = new OnlineProfilesManager(
         var2x -> {
            this.field20707.setSelfVisible(false);
            ConfigScreen screen = (ConfigScreen)this.getParent();

            for (String var7 : var2x) {
               Button var8;
               this.field20705
                  .addToList(
                     var8 = new Button(
                        this.field20705, "p_" + var7, 0, 0, var5, 30, new ColorHelper(-723724, -2039584, 0, -14671840), var7, ResourceRegistry.JelloLightFont18
                     )
                  );
               var8.onClick((var3x, var4x) -> {
                  this.method13118(true);
                  new Thread(() -> {
                     Client.getInstance();
                     ProfileManager var5xx = Client.getInstance().moduleManager.getConfigurationManager();
                     Profile var6x = var5xx.getCurrentConfig();
                     Profile var7x = onlineProfilesManager.createProfileFromOnlineConfig(var6x, var7);
                     screen.method13611(var7x);
                     this.method13118(false);
                  }).start();
               });
            }

            this.field20705.getButtonList().method13246(new Class7262(1));
         }
      );
      this.field20704 = var6;
   }

   public void method13118(boolean var1) {
      this.field20705.setSelfVisible(!var1);
      this.field20707.setSelfVisible(var1);
   }

   public void method13119(boolean var1) {
      this.field20703.changeDirection(!var1 ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
   }

   public boolean method13120() {
      return this.field20703.getDirection() == Animation.Direction.FORWARDS;
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
   }

   @Override
   public void draw(float partialTicks) {
      float var4 = SmoothInterpolator.interpolate(this.field20703.calcPercent(), 0.1, 0.81, 0.14, 1.0);
      if (this.field20703.getDirection() == Animation.Direction.BACKWARDS) {
         var4 = SmoothInterpolator.interpolate(this.field20703.calcPercent(), 0.61, 0.01, 0.87, 0.16);
      }

      this.setHeightA((int)((float)this.field20704 * var4));
      if (this.field20703.calcPercent() != 0.0F) {
         RenderUtil.drawImage(
            (float)this.xA,
            (float)(this.yA + this.heightA),
            (float)this.widthA,
            50.0F,
            Resources.shadowBottomPNG,
            RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.field20703.calcPercent() * partialTicks * 0.3F)
         );
         RenderUtil.scissorScreenArea(this);
         RenderUtil.drawRoundedRect2(
            (float)this.xA, (float)this.yA, (float)this.widthA, (float)this.heightA, RenderUtil2.applyAlpha(-723724, partialTicks)
         );
         if (onlineProfilesManager != null && OnlineProfilesManager.cachedOnlineProfiles != null && OnlineProfilesManager.cachedOnlineProfiles.isEmpty()) {
            RenderUtil.drawString(
               ResourceRegistry.JelloLightFont14,
               (float)(this.xA + 40),
               (float)(this.yA + 110),
               "No Default Profiles Available",
               ClientColors.MID_GREY.getColor()
            );
         }

         super.draw(partialTicks);
         RenderUtil.restoreScissor();
      }
   }
}
