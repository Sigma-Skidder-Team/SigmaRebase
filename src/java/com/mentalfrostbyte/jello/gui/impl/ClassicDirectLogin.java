package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.Screen;
import com.mentalfrostbyte.jello.gui.unmapped.Class4300;
import com.mentalfrostbyte.jello.gui.unmapped.SigmaClassicTextBox;
import com.mentalfrostbyte.jello.managers.AccountManager;
import com.mentalfrostbyte.jello.managers.impl.account.microsoft.Account;
import com.mentalfrostbyte.jello.utils.ClientColors;
import com.mentalfrostbyte.jello.utils.ResourceRegistry;
import com.mentalfrostbyte.jello.utils.render.ColorUtils;
import com.mentalfrostbyte.jello.utils.render.RenderUtil;
import com.mentalfrostbyte.jello.utils.render.Resources;
import com.mentalfrostbyte.jello.utils.unmapped.Class2218;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ClassicDirectLogin extends Screen {
   public SigmaClassicTextBox field20985;
   public SigmaClassicTextBox field20986;
   public Class4300 field20987;
   public Class4300 field20988;
   public Class4300 field20989;
   public AccountManager field20990 = Client.getInstance().accountManager;
   private String field20991 = "§7Idle...";

   public ClassicDirectLogin() {
      super("Alt Manager");
      this.method13300(false);
      int var3 = 400;
      int var4 = 114;
      int var5 = (this.getWidthA() - var3) / 2;
      this.addToList(
         this.field20985 = new SigmaClassicTextBox(this, "username", var5, var4, var3, 45, SigmaClassicTextBox.field20741, "", "Username / E-Mail", ResourceRegistry.DefaultClientFont)
      );
      var4 += 80;
      this.addToList(this.field20986 = new SigmaClassicTextBox(this, "password", var5, var4, var3, 45, SigmaClassicTextBox.field20741, "", "Password", ResourceRegistry.DefaultClientFont));
      var4 += 190;
      this.addToList(this.field20987 = new Class4300(this, "login", var5, var4, var3, 40, "Login", ClientColors.MID_GREY.color));
      var4 += 50;
      this.addToList(this.field20988 = new Class4300(this, "back", var5, var4, var3, 40, "Back", ClientColors.MID_GREY.color));
      var4 += 50;
      this.addToList(this.field20989 = new Class4300(this, "import", var5, var4, var3, 40, "Import user:pass", ClientColors.MID_GREY.color));
      this.field20986.method13155(true);
      this.field20986.method13147("*");
      this.field20987.doThis((var1, var2) -> {
         this.field20991 = "§bLogging in...";
         new Thread(() -> {
            Account var3x = new Account(this.field20985.getTypedText(), this.field20986.getTypedText());
            if (!this.field20990.login(var3x)) {
               this.field20991 = "§cLogin failed!";
            } else {
               this.field20991 = "Logged in. (" + var3x.getEmail() + (!var3x.isEmailAValidEmailFormat() ? "" : " - offline name") + ")";
            }
         }).start();
      });
      this.field20988.doThis((var0, var1) -> Client.getInstance().guiManager.method33482(new SigmaClassicAltManager()));
      this.field20989.doThis((var1, var2) -> {
         String var5x = "";

         var5x = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle()) == null ? "" : GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());

         if (var5x.equalsIgnoreCase("")) {
            return;
         }

         if (var5x.contains(":")) {
            String[] var6x = var5x.split(":");
            this.field20985.method13304(var6x[0]);
            this.field20986.method13304(var6x[1]);
         } else this.field20991 = "§cPlease copy a valid username:password format to clipboard";
      });
   }

   @Override
   public void draw(float var1) {
      RenderUtil.method11455(0.0F, 0.0F, (float)this.getWidthA(), (float)this.getHeightA(), Resources.mainmenubackground);
      RenderUtil.drawRect(0.0F, 0.0F, (float)this.getWidthA(), (float)this.getHeightA(), ColorUtils.applyAlpha(ClientColors.PALE_RED.color, 0.1F));
      RenderUtil.drawRect(0.0F, 0.0F, (float)this.getWidthA(), (float)this.getHeightA(), ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.color, 0.95F));
      RenderUtil.method11440(
         ResourceRegistry.DefaultClientFont, (float)(this.getWidthA() / 2), 38.0F, "Add Login", ClientColors.LIGHT_GREYISH_BLUE.color, Class2218.field14492, Class2218.field14488
      );
      RenderUtil.method11441(
         ResourceRegistry.DefaultClientFont,
         (float)(this.getWidthA() / 2),
         58.0F,
         this.field20991,
         ClientColors.LIGHT_GREYISH_BLUE.color,
         Class2218.field14492,
         Class2218.field14488,
         true
      );
      super.draw(var1);
   }

   @Override
   public void keyPressed(int var1) {
      super.keyPressed(var1);
      if (var1 == 256) {
         Client.getInstance().guiManager.method33482(new SigmaClassicAltManager());
      }
   }
}
