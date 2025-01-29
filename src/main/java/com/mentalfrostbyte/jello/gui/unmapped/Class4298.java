package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.Account;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class Class4298 extends AnimatedIconPanelWrap {
   public static ColorHelper field20821 = new ColorHelper(
         ClientColors.DEEP_TEAL.getColor(),
         ClientColors.DEEP_TEAL.getColor(),
         ClientColors.DEEP_TEAL.getColor(),
         ClientColors.DEEP_TEAL.getColor(),
         Class2218.field14488,
         Class2218.field14492);
   public String skin = null;
   private AbstractClientPlayerEntity field20823;
   private MinecraftClient mc = MinecraftClient.getInstance();
   private static ClientWorld clientWorld;
   public Account account;
   private DynamicTexture field20827;
   private UIEntity entity;

   public Class4298(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String skinName) {
      super(var1, var2, var3, var4, var5, var6, field20821, false);
      this.skin = skinName;
   }

   public IProfiler method13180() {
      Class7991 var3 = new Class7991(this);
      return new Profiler(() -> 0L, var3, false);
   }

   @Override
   public void draw(float partialTicks) {
      if (this.account != null) {
         GL11.glEnable(2929);
         RenderHelper.enableStandardItemLighting();
         RenderSystem.disableDepthTest();
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float) (this.xA + this.widthA / 2), (float) (this.yA - this.heightA / 4), -200.0F);
         GL11.glColor3f(1.0F, 1.0F, 1.0F);
         RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
         RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
         float var4 = (float) (this.mc.getMainWindow().getHeight() - this.getWidthO()
               - this.mc.getMainWindow().getHeight() / 2);
         float var5 = (float) (this.mc.getMainWindow().getWidth() - this.getHeightO()
               - this.mc.getMainWindow().getWidth() / 2);
         float var6 = (float) Math.atan((double) (var4 / (float) (this.mc.getMainWindow().getHeight() / 2))) * 20.0F;
         float var7 = (float) Math.atan((double) (var5 / (float) (this.mc.getMainWindow().getWidth() / 2))) * 20.0F;
         RenderSystem.rotatef(-var6, 1.0F, 0.0F, 0.0F);
         RenderSystem.rotatef(-var7, 0.0F, 1.0F, 0.0F);
         UUID uid = UUID.fromString(this.account.getKnownUUID().equals("steve") ? "123e4567-e89b-12d3-a456-556642440000"
               : this.account.getKnownUUID());
         if (clientWorld == null) {
            ClientWorld.ClientWorldInfo var9 = new ClientWorld.ClientWorldInfo(Difficulty.NORMAL, false, false);
            clientWorld = new ClientWorld(this.mc.getConnection(), var9, World.OVERWORLD, DimensionType.OVERWORLD_TYPE,
                  1, this::method13180, null, false, 0L);
         }

         GameProfile prof = new GameProfile(uid, this.account.getKnownName());
         if (this.entity == null || !this.entity.getDisplayName().getString().equals(this.account.getKnownName())) {
            this.entity = new UIEntity(clientWorld, new GameProfile(uid, this.account.getKnownName()));
            this.entity.playerInfo = new NetworkPlayerInfo(
                  new SPlayerListItemPacket.AddPlayerData(prof, 0, GameType.CREATIVE, this.entity.getDisplayName()));
         }

         this.entity.setUniqueId(uid);
         float var10 = (float) (System.currentTimeMillis() % 1750L) / 278.52115F;
         var10 = (float) Math.sin((double) var10);
         float var11 = (float) (System.currentTimeMillis() % 14000L) / 2228.1692F;
         var11 = (float) Math.sin((double) var11);
         float var12 = (float) (System.currentTimeMillis() % 30000L) / 4774.648F;
         var12 = (float) Math.sin((double) var12);
         new PlayerModel(0.0F, false);
         new PlayerRenderer(this.mc.getRenderManager());
         new MatrixStack();
         RenderSystem.pushMatrix();
         this.mc.getRenderManager().setRenderShadow(false);
         RenderHelper.setupGui3DDiffuseLighting();
         RenderSystem.enableLighting();
         RenderSystem.enableDepthTest();
         GL11.glLightModelfv(2899, new float[] { 0.7F, 0.7F, 0.7F, 1.0F });
         if (this.mc.getRenderManager().info == null) {
            this.mc.getRenderManager().info = new ActiveRenderInfo();
         }

         this.entity.limbSwingAmount = var10 * 0.5F;
         InventoryScreen.drawEntityOnScreen(0, 390, 160, 0.0F, 0.0F, this.entity);
         this.mc.getRenderManager().setRenderShadow(true);
         RenderSystem.popMatrix();
         RenderSystem.popMatrix();
         RenderHelper.disableStandardItemLighting();
         RenderSystem.disableRescaleNormal();
         RenderSystem.activeTexture(33985);
         RenderSystem.disableTexture();
         RenderSystem.activeTexture(33984);
         GL11.glDisable(2929);
      }
   }

   public void handleSelectedAccount(Account var1) {
      this.account = var1;
      this.field20827 = null;
   }
}
