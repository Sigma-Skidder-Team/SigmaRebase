package com.mentalfrostbyte.jello.gui.impl.classic.mainmenu;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.combined.impl.SwitchScreen;
import com.mentalfrostbyte.jello.gui.impl.classic.altmanager.ClassicAltScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.viamcp.JelloPortalScreen;
import com.mentalfrostbyte.jello.gui.impl.classic.mainmenu.buttons.BoxedButton;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.LanguageScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.SoundEvents;

public class ClassicMainScreenGroup extends CustomGuiScreen {
   public final BoxedButton singlePlayerButton;
   public final BoxedButton exitButton;
   public final BoxedButton languageButton;
   public final BoxedButton optionsButton;
   public final BoxedButton accountsButton;
   public final BoxedButton multiplayerButton;
   public final BoxedButton agoraButton;

   public ClassicMainScreenGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
      super(var1, var2, var3, var4, var5, var6);
      int clicks = 0;
      this.addToList(this.singlePlayerButton = new BoxedButton(this, "Singleplayer", this.method13497(clicks++), this.method13496(), 114, 140, Resources.singlePlayer));
      this.addToList(this.multiplayerButton = new BoxedButton(this, "Multiplayer", this.method13497(clicks++), this.method13496(), 114, 140, Resources.multiplayer));
      this.addToList(this.optionsButton = new BoxedButton(this, "Options", this.method13497(clicks++), this.method13496(), 114, 140, Resources.options));
      this.addToList(this.languageButton = new BoxedButton(this, "Language", this.method13497(clicks++), this.method13496(), 114, 140, Resources.language));
      this.addToList(
         this.accountsButton = new BoxedButton(this, "Accounts", this.method13497(clicks++), this.method13496() + 140 + 10, 114, 140, Resources.accounts)
      );
      this.addToList(this.agoraButton = new BoxedButton(this, "Switch", this.method13497(clicks++), this.method13496() + 140 + 10, 114, 140, Resources.switchMode));
      this.addToList(this.exitButton = new BoxedButton(this, "Exit", this.method13497(clicks++), this.method13496() + 140 + 10, 114, 140, Resources.exit));
      this.singlePlayerButton.onClick((var1x, var2x) -> this.displayMcScreen(new WorldSelectionScreen(Minecraft.getInstance().currentScreen)));
      this.multiplayerButton.onClick((var1x, var2x) -> this.displayMcScreen(new JelloPortalScreen(Minecraft.getInstance().currentScreen)));
      this.optionsButton.onClick((var1x, var2x) -> this.displayMcScreen(new OptionsScreen(Minecraft.getInstance().currentScreen, Minecraft.getInstance().gameSettings)));
      this.accountsButton.onClick((var1x, var2x) -> this.handleScreenAndPlayClick(new ClassicAltScreen()));
      this.languageButton.onClick((var1x, var2x) -> this.displayMcScreen(new LanguageScreen(Minecraft.getInstance().currentScreen, Minecraft.getInstance().gameSettings, Minecraft.getInstance().getLanguageManager())));
      this.agoraButton.onClick((a, b) -> displayScreen(new SwitchScreen()));
      this.exitButton.onClick((var0, var1x) -> Minecraft.getInstance().shutdown());
   }

   public void displayMcScreen(net.minecraft.client.gui.screen.Screen screen) {
      Minecraft.getInstance().displayGuiScreen(screen);
      this.playClick();
   }

   public void displayScreen(Screen var1) {
      Client.getInstance().guiManager.handleScreen(var1);
      this.playClick();
   }

   public void handleScreenAndPlayClick(Screen screen) {
      Client.getInstance().guiManager.handleScreen(screen);
      this.playClick();
   }

   public void playClick() {
      Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
   public void draw(float partialTicks) {
      this.method13225();
      RenderUtil.drawImage((float)(this.xA + (this.getWidthA() - 300) / 2), (float)(this.yA + 30), 300.0F, 97.0F, Resources.big);
      super.draw(partialTicks);
   }
}
