package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.TimerUtil;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import totalcross.json.CJsonUtils;
import totalcross.json.JSONObject;

public class VerticalScrollBar extends AnimatedIconPanelWrap implements Class4293 {
   public int field20793;
   public float field20794;
   public boolean field20795;
   public final Class4288 field20796;
   public TimerUtil field20797 = new TimerUtil();

   public VerticalScrollBar(CustomGuiScreen var1, int var2) {
      super(var1, "verticalScrollBar", var1.getWidthA() - var2 - 5, 5, var2, var1.getHeightA() - 10, false);
      this.setSize((var1x, var2x) -> {
         var1x.setXA(var2x.getWidthA() - var2 - 5);
         var1x.setYA(5);
         var1x.setWidthA(var2);
         var1x.setHeightA(var2x.getHeightA() - 10);
      });
      this.addToList(this.field20796 = new Class4288(this, this, var2));
   }

   @Override
   public void voidEvent3(float var1) {
      super.voidEvent3(var1);
      if (this.parent != null && this.parent.method13228(this.getHeightO(), this.getWidthO(), false) || ((Class4339)this.parent).field21208) {
         float var4 = (float)((Class4339)this.getParent()).getButton().getHeightA();
         float var5 = (float)this.getParent().getHeightA();
         float var6 = (float)this.getHeightA();
         if (var4 == 0.0F) {
            return;
         }

         float var7 = var5 / var4;
         if (var7 >= 1.0F) {
            return;
         }

         this.field20793 = this.field20793
            - Math.round(!(var1 < 0.0F) ? (float)((Class4339)this.parent).field21207 * var1 : 1.0F * (float)((Class4339)this.parent).field21207 * var1);
         this.field20797.reset();
         this.field20797.start();
      }
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
      this.field20908 = this.method13228(newHeight, newWidth, false);
      this.field20794 = this.field20794
         + (
            this.field20796.getHeightA() >= this.getHeightA()
               ? -1.0F
               : (
                  !this.method13298() && !this.field20796.method13216() && (!this.field20797.isEnabled() || this.field20797.getElapsedTime() >= 500L)
                     ? -0.05F
                     : 0.05F
               )
         );
      this.field20794 = Math.min(Math.max(0.0F, this.field20794), 1.0F);
      float var5 = (float)((Class4339)this.getParent()).getButton().getHeightA();
      float var6 = (float)this.getParent().getHeightA();
      float var7 = (float)this.getHeightA();
      float var8 = var6 / var5;
      boolean var9 = var8 < 1.0F && var5 > 0.0F && this.field20794 >= 0.0F;
      this.setEnabled(var9);
      this.method13296(var9);
   }

   @Override
   public void draw(float var1) {
      var1 *= this.field20794;
      int var4 = 5;
      int var5 = ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.2F * var1);
      int var6 = this.xA;
      int var7 = this.widthA;
      if (Client.getInstance().clientMode != ClientMode.JELLO) {
         var4 = 0;
         var7 -= 8;
         var6 += 8;
         RenderUtil.drawRoundedRect(
            (float)var6,
            (float)(this.yA + var4),
            (float)(var6 + var7),
            (float)(this.yA + this.heightA - var4),
            ColorUtils.applyAlpha(ClientColors.MID_GREY.getColor(), 0.1F * var1)
         );
      } else {
         RenderUtil.drawImage((float)var6, (float)this.yA, (float)var7, 5.0F, Resources.verticalScrollBarTopPNG, 0.45F * var1);
         RenderUtil.drawImage((float)var6, (float)(this.yA + this.heightA - var4), (float)var7, 5.0F, Resources.verticalScrollBarBottomPNG, 0.45F * var1);
         RenderUtil.drawRoundedRect((float)var6, (float)(this.yA + var4), (float)(var6 + var7), (float)(this.yA + this.heightA - var4), var5);
      }

      super.draw(var1);
   }

   @Override
   public boolean onClick(int mouseX, int mouseY, int probablyTimes) {
      if (!super.onClick(mouseX, mouseY, probablyTimes)) {
         this.field20908 = this.method13228(mouseX, mouseY, false);
         if (this.method13298()) {
            int var6 = mouseY - this.method13272();
            if (var6 <= this.field20796.getYA() + this.field20796.getHeightA()) {
               if (var6 < this.field20796.getYA()) {
                  this.field20793 = this.field20793 - (int)((float)((Class4339)this.parent).getButton().getHeightA() / 4.0F);
               }
            } else {
               this.field20793 = this.field20793 + (int)((float)((Class4339)this.parent).getButton().getHeightA() / 4.0F);
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public JSONObject toConfigWithExtra(JSONObject config) {
      config.put("offset", this.field20793);
      return super.toConfigWithExtra(config);
   }

   @Override
   public void loadConfig(JSONObject config) {
      super.loadConfig(config);
      this.field20793 = CJsonUtils.getIntOrDefault(config, "offset", this.field20793);
   }

   @Override
   public int method13162() {
      return this.field20793;
   }

   @Override
   public void method13163(int var1) {
      this.field20793 = var1;
   }

   @Override
   public boolean method13164() {
      return this.field20795;
   }
}
