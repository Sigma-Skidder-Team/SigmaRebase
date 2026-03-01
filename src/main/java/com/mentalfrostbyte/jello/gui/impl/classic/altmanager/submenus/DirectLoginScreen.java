package com.mentalfrostbyte.jello.gui.impl.classic.altmanager.submenus;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.impl.classic.altmanager.ClassicAltScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.AltManagerButton;
import com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Input;
import com.mentalfrostbyte.jello.managers.AccountManager;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.Account;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class DirectLoginScreen extends Screen {
   public Input emailOrUsername;
   public Input password;
   public AltManagerButton loginButton;
   public AltManagerButton tokenLoginButton;
   public AltManagerButton backButton;
   public AltManagerButton importButton;
   public AccountManager accountManager = Client.getInstance().accountManager;
   private String status = "§7Idle...";

   public DirectLoginScreen() {
      super("Alt Manager");
      this.setListening(false);
      int var3 = 400;
      int var4 = 114;
      int var5 = (this.getWidthA() - var3) / 2;
      this.addToList(
         this.emailOrUsername = new Input(this, "username", var5, var4, var3, 45, Input.field20741, "", "Username / E-Mail", ResourceRegistry.DefaultClientFont)
      );
      var4 += 80;
      this.addToList(this.password = new Input(this, "password", var5, var4, var3, 45, Input.field20741, "", "Password", ResourceRegistry.DefaultClientFont));
      var4 += 190;
      this.addToList(this.loginButton = new AltManagerButton(this, "login", var5, var4, var3, 40, "Login", ClientColors.MID_GREY.getColor()));
      var4 += 50;
      this.addToList(this.tokenLoginButton = new AltManagerButton(this, "token_login", var5, var4, var3, 40, "Token Login", ClientColors.MID_GREY.getColor()));
      var4 += 50;
      this.addToList(this.backButton = new AltManagerButton(this, "back", var5, var4, var3, 40, "Back", ClientColors.MID_GREY.getColor()));
      var4 += 50;
      this.addToList(this.importButton = new AltManagerButton(this, "import", var5, var4, var3, 40, "Import user:pass", ClientColors.MID_GREY.getColor()));
      this.password.setCensorText(true);
      this.password.method13147("*");
      this.loginButton.onClick((var1, var2) -> {
         this.status = "§bLogging in...";
         new Thread(() -> {
            Account account = new Account(this.emailOrUsername.getText(), this.password.getText());
            if (!this.accountManager.login(account)) {
               this.status = "§cLogin failed!";
            } else {
               this.status = "Logged in. (" + account.getEmail() + (!account.isEmailAValidEmailFormat() ? "" : " - offline name") + ")";
            }
         }).start();
      });
      this.tokenLoginButton.onClick((var1, var2) -> {
         this.status = "§bLogging in with token...";
         new Thread(() -> {
            String tokenInput = this.emailOrUsername.getText();
            if (tokenInput.startsWith("mctoken:")) {
               tokenInput = tokenInput.substring(8);
            }
            Account account = new Account("Token Account", "Token ID", tokenInput);
            if (!this.accountManager.login(account)) {
               this.status = "§cToken failed!";
            } else {
               this.status = "Logged in. (" + account.getKnownName() + ")";
            }
         }).start();
      });
      this.backButton.onClick((var0, var1) -> Client.getInstance().guiManager.handleScreen(new ClassicAltScreen()));
      this.importButton.onClick((var1, var2) -> {
         String var5x = "";

         var5x = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle()) == null ? "" : GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());

         if (var5x.equalsIgnoreCase("")) {
            return;
         }

         if (var5x.contains(":")) {
            if (var5x.toLowerCase().startsWith("mctoken:")) {
               this.emailOrUsername.setText(var5x.replace("\n", ""));
               this.password.setText("");
            } else {
               String[] var6x = var5x.split(":");
               this.emailOrUsername.setText(var6x[0]);
               this.password.setText(var6x[1]);
            }
         } else this.status = "§cPlease copy a valid username:password or mctoken:token format to clipboard";
      });
   }

   @Override
   public void draw(float partialTicks) {
      RenderUtil.drawImage(0.0F, 0.0F, (float)this.getWidthA(), (float)this.getHeightA(), Resources.mainmenubackground);
      RenderUtil.drawRoundedRect(0.0F, 0.0F, (float)this.getWidthA(), (float)this.getHeightA(), RenderUtil2.applyAlpha(ClientColors.PALE_RED.getColor(), 0.1F));
      RenderUtil.drawRoundedRect(0.0F, 0.0F, (float)this.getWidthA(), (float)this.getHeightA(), RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.95F));
      RenderUtil.drawString(
         ResourceRegistry.DefaultClientFont, (float)(this.getWidthA() / 2), 38.0F, "Add Login", ClientColors.LIGHT_GREYISH_BLUE.getColor(), FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2, FontSizeAdjust.field14488
      );
      RenderUtil.drawString(
         ResourceRegistry.DefaultClientFont,
         (float)(this.getWidthA() / 2),
         58.0F,
         this.status,
         ClientColors.LIGHT_GREYISH_BLUE.getColor(),
         FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2,
         FontSizeAdjust.field14488,
         true
      );
      super.draw(partialTicks);
   }

   @Override
   public void keyPressed(int keyCode) {
      super.keyPressed(keyCode);
      if (keyCode == 256) {
         Client.getInstance().guiManager.handleScreen(new ClassicAltScreen());
      }
   }
}
