package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.managers.util.music.YoutubeVideoData;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.*;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class YoutubeVideoThumbnail extends AnimatedIconPanelWrap {
   public static ColorHelper field20771 = new ColorHelper(
      ClientColors.DEEP_TEAL.getColor(),
      ClientColors.DEEP_TEAL.getColor(),
      ClientColors.DEEP_TEAL.getColor(),
      ClientColors.DEEP_TEAL.getColor(),
      Class2218.field14488,
      Class2218.field14492
   );
   public URL videoUrl = null;
   public BufferedImage field20773;
   public boolean field20774 = false;
   private Texture field20775;
   private Texture field20776;
   private Animation animation;

   @Override
   public void finalize() throws Throwable {
      try {
         if (this.field20775 != null) {
            Client.getInstance().method19927(this.field20775);
         }

         if (this.field20776 != null) {
            Client.getInstance().method19927(this.field20776);
         }
      } finally {
         super.finalize();
      }
   }

   public YoutubeVideoThumbnail(CustomGuiScreen var1, int x, int y, int width, int height, YoutubeVideoData video) {
      super(var1, video.videoId, x, y, width, height, field20771, video.title, false);
      URL videoUrl = null;

      try {
         videoUrl = new URL(video.fullUrl);
      } catch (MalformedURLException excep) {
         excep.printStackTrace();
      }

      this.videoUrl = videoUrl;
      this.animation = new Animation(125, 125);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      boolean var5 = this.method13298() && this.getParent().getParent().method13114(newHeight, newWidth);
      this.animation.changeDirection(!var5 ? Direction.BACKWARDS : Direction.FORWARDS);

      super.updatePanelDimensions(newHeight, newWidth);
   }

   public boolean method13157() {
      if (this.getParent() != null && this.getParent().getParent() != null) {
         CustomGuiScreen var3 = this.getParent().getParent();
         if (var3 instanceof MusicTabs) {
            MusicTabs var4 = (MusicTabs)var3;
            int var5 = var4.method13513() + var4.getHeightA() + this.getHeightA();
            int var6 = var4.method13513() - this.getHeightA();
            return this.getYA() <= var5 && this.getYA() >= var6;
         }
      }

      return true;
   }

   @Override
   public void draw(float partialTicks) {
      if (!this.method13157()) {
         if (this.field20775 != null) {
            this.field20775.release();
            this.field20775 = null;
         }

         if (this.field20776 != null) {
            this.field20776.release();
            this.field20776 = null;
         }
      } else {
         if (this.method13157() && !this.field20774) {
            this.field20774 = true;
            new Thread(() -> {
               try {
                  BufferedImage var3 = ImageIO.read(this.videoUrl);
                  if (var3.getHeight() != var3.getWidth()) {
                     if (this.getTypedText().contains("[NCS Release]")) {
                        this.field20773 = var3.getSubimage(1, 3, 170, 170);
                     } else {
                        this.field20773 = var3.getSubimage(70, 0, 180, 180);
                     }
                  } else {
                     this.field20773 = var3;
                  }
               } catch (IOException | NumberFormatException var5x) {
                  var5x.printStackTrace();
               }
            }).start();
         }

         float var4 = this.animation.calcPercent();
         float var5 = (float)Math.round((float)(this.getXA() + 15) - 5.0F * var4);
         float var6 = (float)Math.round((float)(this.getYA() + 15) - 5.0F * var4);
         float var7 = (float)Math.round((float)(this.getWidthA() - 30) + 10.0F * var4);
         float var8 = (float)Math.round((float)(this.getWidthA() - 30) + 10.0F * var4);
         RenderUtil.drawRoundedRect(
            (float)(this.getXA() + 15) - 5.0F * var4,
            (float)(this.getYA() + 15) - 5.0F * var4,
            (float)(this.getWidthA() - 30) + 10.0F * var4,
            (float)(this.getWidthA() - 30) + 10.0F * var4,
            20.0F,
                 partialTicks
         );
         if (this.field20775 == null && this.field20773 == null) {
            RenderUtil.drawImage(var5, var6, var7, var8, Resources.artworkPNG, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * (1.0F - var4)));
            if (this.field20776 != null) {
               RenderUtil.drawImage(var5, var6, var7, var8, Resources.artworkPNG, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks));
            }
         } else {
            if (this.field20775 == null) {
               try {
                  if (this.field20775 != null) {
                     this.field20775.release();
                  }

                  this.field20775 = BufferedImageUtil.getTexture("picture", this.field20773);
               } catch (IOException var14) {
                  var14.printStackTrace();
               }
            }

            if (this.field20776 == null && var4 > 0.0F) {
               try {
                  if (this.field20776 != null) {
                     this.field20776.release();
                  }

                  this.field20776 = BufferedImageUtil.getTexture("picture", ImageUtil.applyBlur(this.field20773, 14));
               } catch (IOException var13) {
                  var13.printStackTrace();
               }
            } else if (var4 == 0.0F && this.field20776 != null) {
               this.field20776 = null;
            }

            RenderUtil.drawImage(var5, var6, var7, var8, this.field20775, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * (1.0F - var4)));
            if (this.field20776 != null) {
               RenderUtil.drawImage(var5, var6, var7, var8, this.field20776, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks));
            }
         }

         float var9 = 50;
         if (this.method13212()) {
            var9 = 40;
         }

         float var10 = 0.5F + var4 / 2.0F;
         RenderUtil.drawImage(
            (float)(this.getXA() + this.getWidthA() / 2) - (var9 / 2) * var10,
            (float)(this.getYA() + this.getWidthA() / 2) - (var9 / 2) * var10,
            var9 * var10,
            var9 * var10,
            Resources.playIconPNG,
                 ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks)
         );
         TrueTypeFont var11 = ResourceRegistry.JelloLightFont12;
         if (this.typedText != null) {
            RenderUtil.method11415(this);
            String[] var12 = this.getTypedText().replaceAll("\\(.*\\)", "").replaceAll("\\[.*\\]", "").split(" - ");
            if (var12.length > 1) {
               RenderUtil.drawString(
                  var11,
                  (float)(this.getXA() + (this.getWidthA() - var11.getWidth(var12[1])) / 2),
                  (float)(this.getYA() + this.getWidthA() - 2),
                  var12[1],
                       ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
               );
               RenderUtil.drawString(
                  var11,
                  (float)(this.getXA() + (this.getWidthA() - var11.getWidth(var12[0])) / 2),
                  (float)(this.getYA() + this.getWidthA() - 2 + 13),
                  var12[0],
                       ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
               );
            } else {
               RenderUtil.drawString(
                  var11,
                  (float)(this.getXA() + (this.getWidthA() - var11.getWidth(var12[0])) / 2),
                  (float)(this.getYA() + this.getWidthA() - 2 + 6),
                  var12[0],
                       ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
               );
            }

            RenderUtil.endScissor();
         }
      }
   }
}
