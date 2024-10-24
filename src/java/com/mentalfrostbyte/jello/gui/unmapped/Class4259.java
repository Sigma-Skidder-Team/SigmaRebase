package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.utils.ClientColors;
import com.mentalfrostbyte.jello.utils.ResourceRegistry;
import com.mentalfrostbyte.jello.utils.render.ColorUtils;
import com.mentalfrostbyte.jello.utils.render.RenderUtil;
import com.mentalfrostbyte.jello.utils.render.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Class4259 extends Class4247 {
    public Class4266 field20647;
   public ChunkPos chunkPos;
   public int field20649 = 8;
   public float field20650 = 0.0F;
   public float field20651 = 0.0F;
   public int field20652;
   public int field20653;
   public Class7101 field20654;
   public int field20655;
   public float field20656;
   public float field20657;
   public ChunkPos field20658;
   private final List<Class8041> field20659 = new ArrayList<>();
   private final List<Class9693> field20660 = new ArrayList<>();

   public Class4259(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
      super(var1, var2, var3, var4, var5, var6, false);
      int var9 = 90;
      int var10 = 40;
      int var11 = var5 - var10 - 10;
      int var12 = var6 - var9 - 10;
      this.addToList(this.field20647 = new Class4266(this, "zoom", var11, var12, var10, var9));
      this.chunkPos = Minecraft.getInstance().world.getChunk(Minecraft.getInstance().player.getPosition()).getPos();
      this.method13300(false);
   }

   public void method13076(boolean var1) {
      this.field20649 = Math.max(3, Math.min(33, !var1 ? this.field20649 + 1 : this.field20649 - 1));
      this.method13083();
   }

   @Override
   public void method13028(int var1, int var2) {
      super.method13028(var1, var2);
      if (this.field20909) {
         int var5 = var1 - this.field20652;
         int var6 = var2 - this.field20653;
         float var7 = ((float)this.field20649 - 1.0F) / (float)this.field20649;
         float var8 = (float)this.widthA / ((float)this.field20649 * 2.0F * var7);
         this.field20651 += (float)var5 / var8;
         this.field20650 += (float)var6 / var8;
      }

      this.field20652 = var1;
      this.field20653 = var2;
   }

   public void method13077(int var1, int var2) {
      this.chunkPos = new ChunkPos(var1 / 16, var2 / 16);
      this.field20651 = -0.5F;
      this.field20650 = -0.5F;
      this.field20647.field20687 = true;
   }

   @Override
   public boolean method13078(int var1, int var2, int var3) {
      if (this.method13298() && var3 == 1) {
         int var6 = Math.max(this.widthA, this.heightA);
         float var7 = (float)(this.widthA - var6) / 2.0F;
         float var8 = (float)(this.heightA - var6) / 2.0F;
         float var9 = (float)var1 - ((float)this.method13271() + var8 + (float)(var6 / 2));
         float var10 = (float)(Minecraft.getInstance().getMainWindow().getHeight() - var2) - ((float)this.method13272() + var7 + (float)(var6 / 2));
         float var11 = (float)var6 / ((float)(this.field20649 - 1) * 2.0F);
         float var12 = (float)(this.chunkPos.x * 16) - this.field20651 * 16.0F;
         float var13 = (float)(this.chunkPos.z * 16) - this.field20650 * 16.0F;
         float var14 = var12 + var9 / var11 * 16.0F;
         float var15 = var13 - var10 / var11 * 16.0F;
         this.method13081(var1, var2, new Vector3i(Math.round(var14), 0, Math.round(var15)));
         return false;
      } else {
         this.method13083();
         return super.method13078(var1, var2, var3);
      }
   }

   @Override
   public void method13079(float var1) {
      super.method13079(var1);
      if (this.method13298()) {
         this.field20649 = Math.round(Math.max(3.0F, Math.min(33.0F, (float)this.field20649 + var1 / 10.0F)));
         this.method13083();
      }
   }

   @Override
   public void draw(float var1) {
      Minecraft var4 = Minecraft.getInstance();
      ChunkPos var5 = new ChunkPos(this.chunkPos.x, this.chunkPos.z);
      var5.x = (int)((double)var5.x - Math.floor((double)this.field20651));
      var5.z = (int)((double)var5.z - Math.floor((double)this.field20650));
      if (var1 != 1.0F) {
         this.field20647.field20687 = true;
      }

      if (this.field20654 == null || this.field20649 != this.field20655 || !this.field20658.equals(var5)) {
         this.field20654 = Client.getInstance().waypointsManager.method30003(var5, this.field20649 * 2);
      }

      if (this.field20654 == null || this.field20649 != this.field20655 || this.field20651 != this.field20657 || this.field20650 != this.field20656) {
         this.field20647.field20687 = true;
      }

      int var6 = Math.max(this.widthA, this.heightA);
      int var7 = (this.widthA - var6) / 2;
      int var8 = (this.heightA - var6) / 2;
      float var9 = (float)this.field20649 / ((float)this.field20649 - 1.0F);
      float var10 = (float)var6 / ((float)this.field20649 * 2.0F);
      double var11 = ((double)this.field20650 - Math.floor((double)this.field20650)) * (double)var10;
      double var13 = ((double)this.field20651 - Math.floor((double)this.field20651)) * (double)var10;
      TextureManager textureManager = var4.getTextureManager();
      textureManager.bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
      RenderUtil.method11421(this.xA, this.yA, this.xA + this.widthA, this.yA + this.heightA, true);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.xA + this.widthA / 2), (float)(this.yA + this.heightA / 2), 0.0F);
      GL11.glScalef(var9, var9, 0.0F);
      GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef((float)(-this.xA - this.widthA / 2), (float)(-this.yA - this.heightA / 2), 0.0F);
      GL11.glTranslated(-var11, var13, 0.0);
      RenderUtil.method11453(
         (float)(this.xA + var7),
         (float)(this.yA + var8),
         (float)var6,
         (float)var6,
         this.field20654.field30546,
         ClientColors.LIGHT_GREYISH_BLUE.getColor,
         0.0F,
         0.0F,
         (float)this.field20654.field30544,
         (float)this.field20654.field30545,
         true,
         false
      );
      GL11.glPopMatrix();

      for (Class8351 var16 : Client.getInstance().waypointsManager.method29989()) {
         float var17 = (float)(this.chunkPos.x * 16) - this.field20651 * 16.0F;
         float var18 = (float)(this.chunkPos.z * 16) - this.field20650 * 16.0F;
         float var19 = (float)var16.field35890 - var17 + 1.0F;
         float var20 = (float)var16.field35891 - var18 + 1.0F;
         float var21 = (float)var6 / ((float)(this.field20649 - 1) * 2.0F);
         RenderUtil.drawImage(
            (float)(this.xA + Math.round(var19 * var21 / 16.0F) + this.widthA / 2 - 16),
            (float)(this.yA + Math.round(var20 * var21 / 16.0F) + this.heightA / 2 - 42),
            32.0F,
            46.0F,
            Resources.waypointPNG,
            var16.field35892
         );
      }

      RenderUtil.endScissor();
      int var22 = Math.round((float)(this.chunkPos.x * 16) - this.field20651 * 16.0F);
      int var23 = Math.round((float)(this.chunkPos.z * 16) - this.field20650 * 16.0F);
      String var24 = var22 + "  " + var23;
      RenderUtil.drawString(
         ResourceRegistry.JelloLightFont14,
         (float)(this.xA - ResourceRegistry.JelloLightFont14.getStringWidth(var24) - 23),
         (float)(this.yA + 35),
         var24,
              ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor, 0.4F)
      );
      this.field20656 = this.field20650;
      this.field20657 = this.field20651;
      this.field20655 = this.field20649;
      this.field20658 = var5;
      super.draw(var1);
   }

   public final void method13080(Class8041 var1) {
      this.field20659.add(var1);
   }

   public final void method13081(int var1, int var2, Vector3i var3) {
      for (Class8041 var7 : this.field20659) {
         var7.method27609(this, var1, var2, var3);
      }
   }

   public final void method13082(Class9693 var1) {
      this.field20660.add(var1);
   }

   public final void method13083() {
      for (Class9693 var4 : this.field20660) {
         var4.method37947(this);
      }
   }
}