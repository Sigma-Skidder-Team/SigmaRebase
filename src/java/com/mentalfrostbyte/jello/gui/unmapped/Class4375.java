package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.EasingFunctions;
import com.mentalfrostbyte.jello.gui.base.QuadraticEasing;
import com.mentalfrostbyte.jello.gui.impl.JelloKeyboardScreen;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Class4375 extends UIBase {
   private final int field21376;
   private final Animation field21377;
   private boolean field21378 = false;
   private final List<Class6601> field21379 = new ArrayList<Class6601>();
   private final List<Class9342> field21380 = new ArrayList<Class9342>();

   public Class4375(CustomGuiScreen var1, String var2, int var3, int var4, int var5, String var6) {
      super(var1, var2, var3 - 125, var4, 250, 330, ColorHelper.field27961, var6, false);
      if (this.yA + this.heightA <= Minecraft.getInstance().getMainWindow().getHeight()) {
         this.yA += 10;
      } else {
         this.yA -= 400;
         this.field21378 = true;
      }

      this.field21376 = var5;
      this.field21377 = new Animation(250, 0);
      this.method13292(true);
      this.method13300(false);
      this.method13712();
      UIButton var9;
      this.addToList(
         var9 = new UIButton(
            this,
            "addButton",
            this.widthA - 70,
            this.heightA - 70,
            ResourceRegistry.JelloLightFont25.getWidth("Add"),
            70,
            ColorHelper.field27961,
            "Add",
            ResourceRegistry.JelloLightFont25
         )
      );
      var9.doThis((var1x, var2x) -> this.method13714());
   }

   public void method13712() {
      int var3 = 1;
      ArrayList var4 = new ArrayList();

      for (CustomGuiScreen var6 : this.getRenderObjects()) {
         if (var6.getHeightA() != 0) {
            var4.add(var6.getName());
         }
      }

      this.method13242();
      this.method13145(true);
      this.method13238();

      for (Class6984 var10 : JelloKeyboardScreen.method13328()) {
         int var7 = var10.method21599();
         if (var7 == this.field21376) {
            Class4253 var8;
            this.addToList(var8 = new Class4253(this, var10.method21596(), 0, 20 + 55 * var3, this.widthA, 55, var10, var3++));
            var8.method13036(var2 -> {
               var10.method21598(0);
               this.method13037();
            });
            if (var4.size() > 0 && !var4.contains(var10.method21596())) {
               var8.method13056();
            }
         }
      }
   }

   @Override
   public void method13028(int var1, int var2) {
      Map<Integer, Class4253> var5 = new HashMap();

      for (CustomGuiScreen var7 : this.getRenderObjects()) {
         if (var7 instanceof Class4253) {
             var5.put(((Class4253) var7).field20626, (Class4253) var7);
         }
      }

      int var9 = 75;

      for (Entry<Integer, Class4253> var11 : var5.entrySet()) {
         var11.getValue().setYA(var9);
         var9 += var11.getValue().getHeightA();
      }

      super.method13028(var1, var2);
   }

   @Override
   public void draw(float var1) {
      var1 = this.field21377.calcPercent();
      float var4 = EasingFunctions.easeOutBack(var1, 0.0F, 1.0F, 1.0F);
      this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
      this.method13284((int)((float)this.widthA * 0.2F * (1.0F - var4)) * (!this.field21378 ? 1 : -1));
      super.method13224();
      int var6 = ColorUtils.applyAlpha(-723724, QuadraticEasing.easeOutQuad(var1, 0.0F, 1.0F, 1.0F));
      RenderUtil.drawRoundedRect(
         (float)(this.xA + 10 / 2),
         (float)(this.yA + 10 / 2),
         (float)(this.widthA - 10),
         (float)(this.heightA - 10),
         35.0F,
         var1
      );
      RenderUtil.drawRoundedRect(
         (float)(this.xA + 10 / 2),
         (float)(this.yA + 10 / 2),
         (float)(this.xA - 10 / 2 + this.widthA),
         (float)(this.yA - 10 / 2 + this.heightA),
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var1 * 0.25F)
      );
      RenderUtil.drawRoundedRect((float)this.xA, (float)this.yA, (float)this.widthA, (float)this.heightA, (float)10, var6);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)this.xA, (float)this.yA, 0.0F);
      GL11.glRotatef(!this.field21378 ? -90.0F : 90.0F, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef((float)(-this.xA), (float)(-this.yA), 0.0F);
      RenderUtil.drawImage(
         (float)(this.xA + (!this.field21378 ? 0 : this.heightA)),
         (float)this.yA + (float)((this.widthA - 47) / 2) * (!this.field21378 ? 1.0F : -1.5F),
         18.0F,
         47.0F,
         Resources.selectPNG,
         var6
      );
      GL11.glPopMatrix();
      RenderUtil.drawString(
         ResourceRegistry.JelloLightFont25,
         (float)(this.xA + 25),
         (float)(this.yA + 20),
         this.typedText + " Key",
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * var1)
      );
      RenderUtil.drawRoundedRect(
         (float)(this.xA + 25),
         (float)(this.yA + 68),
         (float)(this.xA + this.widthA - 25),
         (float)(this.yA + 69),
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * var1)
      );
      super.draw(var1);
   }

   public final void method13713(Class6601 var1) {
      this.field21379.add(var1);
   }

   public final void method13714() {
      for (Class6601 var4 : this.field21379) {
         var4.method20001(this);
      }
   }
}
