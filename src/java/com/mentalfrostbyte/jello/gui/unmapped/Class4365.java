package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.managers.impl.account.Account;
import com.mentalfrostbyte.jello.utils.ClientColors;
import com.mentalfrostbyte.jello.utils.ColorHelper;
import com.mentalfrostbyte.jello.utils.ResourceRegistry;
import com.mentalfrostbyte.jello.utils.render.ColorUtils;
import com.mentalfrostbyte.jello.utils.render.RenderUtil;
import com.mentalfrostbyte.jello.utils.render.Resources;
import com.mentalfrostbyte.jello.utils.render.Texture;
import net.minecraft.client.Minecraft;

public class Class4365 extends Class4247 {
   public float field21334 = 0.0F;
   public int field21337;
   public int field21338;
   private Account account;

   public Class4365(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String var7) {
      super(var1, var2, var3, var4, var5, var6, ColorHelper.field27961, var7, false);
      this.font = ResourceRegistry.JelloLightFont20;
   }

   @Override
   public void method13028(int var1, int var2) {
      this.field21337 = var1;
      this.field21338 = var2;
   }

   @Override
   public void draw(float var1) {
      this.account = Client.getInstance().networkManager.account;
      String var4 = "Log in";
      Texture var5 = Resources.accountPNG;
      if (this.account != null) {
         var4 = this.account.username;
      }

      this.setWidthA(this.font.getStringWidth(var4) + 50 + 60);
      this.setXA(Minecraft.getInstance().getMainWindow().getWidth() - this.widthA - 20);
      boolean var6 = this.field21337 >= this.xA && this.field21338 <= this.yA + this.getHeightA();
      this.field21334 = Math.max(0.0F, Math.min(1.0F, this.field21334 + (!var6 ? -0.1F : 0.1F)));
      RenderUtil.drawRoundedRect(
         (float)this.xA, (float)this.yA, (float)this.getWidthA(), (float)this.getHeightA(), 20.0F, this.field21334 * 0.2F * var1
      );
      RenderUtil.drawRect(
         (float)this.xA,
         (float)this.yA,
         (float)(this.xA + this.getWidthA()),
         (float)(this.yA + this.getHeightA()),
         ColorUtils.applyAlpha(ClientColors.DULL_GREEN.getColor, (0.2F * this.field21334 + (!this.method13212() ? 0.0F : 0.2F)) * var1)
      );
      RenderUtil.drawImage(
         (float)(this.xA + this.widthA - 60 - 10),
         (float)(this.yA + 10),
         60.0F,
         60.0F,
         var5,
         ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor, (0.5F + 0.5F * this.field21334) * var1)
      );
      RenderUtil.drawString(
         this.font,
         (float)(this.xA + this.widthA - 90 - this.font.getStringWidth(var4)),
         (float)(this.yA + 27),
         var4,
              ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor, (0.5F + 0.5F * this.field21334) * var1)
      );
      super.draw(var1);
   }
}