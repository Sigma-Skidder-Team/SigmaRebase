package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.managers.impl.sound.CustomSoundPlayer;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.SoundEvents;

public class Class4300 extends AnimatedIconPanelWrap {
   private static String[] field20838;
   public int field20839;

   public Class4300(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String var7, int var8) {
      super(var1, var2, var3, var4, var5, var6, false);
      this.setTypedText(var7);
      this.doThis((var0, var1x) -> MinecraftClient.getInstance().getSoundHandler()
            .play(CustomSoundPlayer.playSoundWithCustomPitch(SoundEvents.UI_BUTTON_CLICK, 1.0F)));
      this.field20839 = var8;
   }

   @Override
   public void draw(float var1) {
      this.setFont(ResourceRegistry.DefaultClientFont);
      RenderUtil.drawRoundedRect(
            (float) this.xA,
            (float) this.yA,
            (float) (this.xA + this.widthA),
            (float) (this.yA + this.heightA),
            ColorUtils.applyAlpha(this.field20839,
                  !this.isHovered() ? 0.25F : (!this.method13298() ? 0.4F : (!this.method13212() ? 0.5F : 0.6F))));
      RenderUtil.method11429(
            (float) this.xA,
            (float) this.yA,
            (float) (this.xA + this.widthA),
            (float) (this.yA + this.heightA),
            2,
            ColorUtils.applyAlpha(this.field20839, 0.2F));
      RenderUtil.drawString(
            ResourceRegistry.DefaultClientFont,
            (float) (this.getXA() + this.getWidthA() / 2),
            (float) (this.getYA() + this.getHeightA() / 2),
            this.typedText,
            ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), !this.isHovered() ? 0.5F : 1.0F),
            Class2218.field14492,
            Class2218.field14492);
      super.draw(var1);
   }
}
