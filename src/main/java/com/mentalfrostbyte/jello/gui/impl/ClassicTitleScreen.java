package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Screen;
import com.mentalfrostbyte.jello.gui.unmapped.BoxedButton;
import com.mentalfrostbyte.jello.managers.impl.sound.CustomSoundPlayer;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.options.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.sound.SoundEvents;

public class ClassicTitleScreen extends CustomGuiScreen {
      public final BoxedButton singlePlayerButton;
      public final BoxedButton exitButton;
      public final BoxedButton languageButton;
      public final BoxedButton optionsButton;
      public final BoxedButton accountsButton;
      public final BoxedButton multiplayerButton;
      public final BoxedButton agoraButton;

      public ClassicTitleScreen(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
            super(var1, var2, var3, var4, var5, var6);
            int clicks = 0;
            this.addToList(this.singlePlayerButton = new BoxedButton(this, "Singleplayer", this.method13497(clicks++),
                        this.method13496(), 114, 140, Resources.singlePlayer));
            this.addToList(this.multiplayerButton = new BoxedButton(this, "Multiplayer", this.method13497(clicks++),
                        this.method13496(), 114, 140, Resources.multiplayer));
            this.addToList(this.optionsButton = new BoxedButton(this, "Options", this.method13497(clicks++),
                        this.method13496(), 114, 140, Resources.options));
            this.addToList(this.languageButton = new BoxedButton(this, "Language", this.method13497(clicks++),
                        this.method13496(), 114, 140, Resources.language));
            this.addToList(
                        this.accountsButton = new BoxedButton(this, "Accounts", this.method13497(clicks++),
                                    this.method13496() + 140 + 10, 114, 140, Resources.accounts));
            this.addToList(this.agoraButton = new BoxedButton(this, "Agora", this.method13497(clicks++),
                        this.method13496() + 140 + 10, 114, 140, Resources.agora));
            this.addToList(this.exitButton = new BoxedButton(this, "Exit", this.method13497(clicks++),
                        this.method13496() + 140 + 10, 114, 140, Resources.exit));
            this.singlePlayerButton.doThis((var1x, var2x) -> this
                        .displayMcScreen(new SelectWorldScreen(MinecraftClient.getInstance().currentScreen)));
            this.multiplayerButton.doThis(
                        (var1x, var2x) -> this.displayMcScreen(
                                    new JelloPortalScreen(MinecraftClient.getInstance().currentScreen)));
            this.optionsButton.doThis(
                        (var1x, var2x) -> this
                                    .displayMcScreen(new OptionsScreen(MinecraftClient.getInstance().currentScreen,
                                                MinecraftClient.getInstance().options)));
            this.accountsButton.doThis((var1x, var2x) -> this.handleScreenAndPlayClick(new SigmaClassicAltManager()));
            this.languageButton
                        .doThis(
                                    (var1x, var2x) -> this.displayMcScreen(
                                                new LanguageOptionsScreen(MinecraftClient.getInstance().currentScreen,
                                                            MinecraftClient.getInstance().options,
                                                            MinecraftClient.getInstance().getLanguageManager())));
            this.exitButton.doThis((var0, var1x) -> MinecraftClient.getInstance().stop());
      }

      public void displayMcScreen(net.minecraft.client.gui.screen.Screen screen) {
            MinecraftClient.getInstance().openScreen(screen);
            this.playClick();
      }

      public void handleScreenAndPlayClick(Screen screen) {
            Client.getInstance().guiManager.handleScreen(screen);
            this.playClick();
      }

      public void playClick() {
            MinecraftClient.getInstance().getSoundManager()
                        .play(CustomSoundPlayer.playSoundWithCustomPitch(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }

      private int method13496() {
            return 150;
      }

      private int method13497(int var1) {
            int var6 = 4;
            int var7 = -6;
            int var8 = 122 * var6 + var6 * var7;
            if (var1 < var6) {
                  return this.getWidthA() / 2 - var8 / 2 + var1 * 122 + var1 * var7 - 12;
            } else {
                  var1 -= var6;
                  var6 = 3;
                  var7 = 6;
                  var8 = 122 * var6 + var6 * var7;
                  return this.getWidthA() / 2 - var8 / 2 + var1 * 122 + var1 * var7 - 12;
            }
      }

      @Override
      public void draw(float var1) {
            this.method13225();
            RenderUtil.drawImage((float) (this.xA + (this.getWidthA() - 300) / 2), (float) (this.yA + 30), 300.0F,
                        97.0F,
                        Resources.big);
            super.draw(var1);
      }
}
