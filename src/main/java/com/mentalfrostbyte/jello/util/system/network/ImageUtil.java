package com.mentalfrostbyte.jello.util.system.network;

import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

public class ImageUtil {

    public static String getSkinUrlByID(String identifier) {
        return "https://mc-heads.net/skin/" + identifier;
    }

    public static BufferedImage applyBlur(BufferedImage image, int amount) {
        if (image != null) {
            if (image.getWidth() > amount + amount) {
                if (image.getHeight() > amount + amount) {
                    ConvolveOp var4 = new ConvolveOp(createGaussianKernel((float) amount));
                    BufferedImage var5 = var4.filter(image, null);
                    var5 = var4.filter(applyEdgeWrap(var5), null);
                    var5 = applyEdgeWrap(var5);
                    return var5.getSubimage(amount, amount, image.getWidth() - amount - amount, image.getHeight() - amount - amount);
                } else {
                    return image;
                }
            } else {
                return image;
            }
        } else {
            return image;
        }
    }

    public static BufferedImage addPadding(BufferedImage var0, int var1) {
        int var4 = var0.getWidth() + var1 * 2;
        int var5 = var0.getHeight() + var1 * 2;
        BufferedImage var6 = method35043(var0, (float) var4 / (float) var0.getWidth(), (float) var5 / (float) var0.getHeight());

        for (int var7 = 0; var7 < var0.getWidth(); var7++) {
            for (int var8 = 0; var8 < var0.getHeight(); var8++) {
                var6.setRGB(var1 + var7, var1 + var8, var0.getRGB(var7, var8));
            }
        }

        return var6;
    }

    public static BufferedImage method35043(BufferedImage var0, double var1, double var3) {
        BufferedImage var7 = null;
        if (var0 != null) {
            int var8 = (int) ((double) var0.getHeight() * var3);
            int var9 = (int) ((double) var0.getWidth() * var1);
            var7 = new BufferedImage(var9, var8, var0.getType());
            Graphics2D var10 = var7.createGraphics();
            AffineTransform var11 = AffineTransform.getScaleInstance(var1, var3);
            var10.drawRenderedImage(var0, var11);
        }

        return var7;
    }

    public static BufferedImage adjustImageHSB(BufferedImage var0, float var1, float var2, float var3) {
        int var6 = var0.getWidth();
        int var7 = var0.getHeight();

        for (int var8 = 0; var8 < var7; var8++) {
            for (int var9 = 0; var9 < var6; var9++) {
                int var10 = var0.getRGB(var9, var8);
                int var11 = var10 >> 16 & 0xFF;
                int var12 = var10 >> 8 & 0xFF;
                int var13 = var10 & 0xFF;
                float[] var14 = Color.RGBtoHSB(var11, var12, var13, null);
                float var15 = Math.max(0.0F, Math.min(1.0F, var14[0] + var1));
                float var16 = Math.max(0.0F, Math.min(1.0F, var14[1] * var2));
                float var17 = Math.max(0.0F, Math.min(1.0F, var14[2] + var3));
                int var18 = Color.HSBtoRGB(var15, var16, var17);
                var0.setRGB(var9, var8, var18);
            }
        }

        return var0;
    }

    public static BufferedImage applyEdgeWrap(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage wrappedImage = new BufferedImage(height, width, inputImage.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                wrappedImage.setRGB(height - 1 - y, width - 1 - x, inputImage.getRGB(x, y));
            }
        }

        return wrappedImage;
    }

    public static Kernel createGaussianKernel(float radius) {
        int kernelRadius = (int) Math.ceil(radius);
        int kernelSize = kernelRadius * 2 + 1;
        float[] kernelData = new float[kernelSize];
        float standardDeviation = radius / 3.0F;
        float twoSigmaSquared = 2.0F * standardDeviation * standardDeviation;
        float normalizationFactor = (float) ((Math.PI * 2) * (double) standardDeviation);
        normalizationFactor = (float) Math.sqrt(normalizationFactor);
        float maxDistanceSquared = radius * radius;
        float sum = 0.0F;
        int index = 0;

        for (int offset = -kernelRadius; offset <= kernelRadius; offset++) {
            float distanceSquared = (float) (offset * offset);
            if (!(distanceSquared > maxDistanceSquared)) {
                kernelData[index] = (float) Math.exp(-distanceSquared / twoSigmaSquared) / normalizationFactor;
            } else {
                kernelData[index] = 0.0F;
            }

            sum += kernelData[index];
            index++;
        }

        for (int i = 0; i < kernelSize; i++) {
            kernelData[i] /= sum;
        }

        return new Kernel(kernelSize, 1, kernelData);
    }

    public static BufferedImage applyGaussianBlur(BufferedImage image, int blurRadius) {
        if (image == null) {
            return image;
        } else {
            ConvolveOp blurOperation = new ConvolveOp(createGaussianKernel((float) blurRadius), ConvolveOp.EDGE_NO_OP, null);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            BufferedImage extendedImage = new BufferedImage(imageWidth + blurRadius * 2, imageHeight + blurRadius * 2, image.getType());

            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    extendedImage.setRGB(x + blurRadius, y + blurRadius / 2, image.getRGB(x, y));
                }
            }

            BufferedImage blurredImage = blurOperation.filter(extendedImage, null);
            blurredImage = blurOperation.filter(applyEdgeWrap(blurredImage), null);
            blurredImage = applyEdgeWrap(blurredImage);
            return blurredImage.getSubimage(blurRadius, blurRadius, extendedImage.getWidth() - blurRadius * 2, extendedImage.getHeight() - blurRadius * 2);
        }
    }

    public static BufferedImage method35036(int var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
        int var10 = 4;
        var1 = (int) ((float) var1 * GuiManager.scaleFactor);
        var0 = (int) ((float) var0 * GuiManager.scaleFactor);
        var2 = (int) ((float) var2 * GuiManager.scaleFactor);
        var3 = (int) ((float) var3 * GuiManager.scaleFactor);
        var4 = (int) ((float) var4 * GuiManager.scaleFactor);
        var1 = Minecraft.getInstance().getMainWindow().getFramebufferHeight() - var1 - var3;
        if (var4 <= 0) {
            var4 = 1;
        }

        ByteBuffer var11 = BufferUtils.createByteBuffer(var2 * var3 * var10);
        GL11.glReadPixels(var0, var1, var2, var3, 6408, 5121, var11);
        BufferedImage var12 = new BufferedImage(var2 / var4, var3 / var4, 1);

        for (int var13 = var4 / 2; var13 < var2; var13 += var4) {
            for (int var14 = var4 / 2; var14 < var3; var14 += var4) {
                if (var13 / var4 < var2 / var4 && var14 / var4 < var3 / var4) {
                    int var15 = (var13 + var2 * var14) * var10;
                    int var16 = var11.get(var15) & 255;
                    int var17 = var11.get(var15 + 1) & 255;
                    int var18 = var11.get(var15 + 2) & 255;
                    var12.setRGB(var13 / var4, var3 / var4 - (var14 / var4 + 1), 0xFF000000 | var16 << 16 | var17 << 8 | var18);
                }
            }
        }

        if (var5 <= 1) {
            return var12;
        } else {
            return !var7 ? applyBlur(method35040(var12, var5, var6), var5) : applyBlur(addPadding(var12, var5), var5);
        }
    }

    public static BufferedImage method35040(BufferedImage var0, int var1, int var2) {
        int var5 = var0.getWidth() + var1 * 2;
        int var6 = var0.getHeight() + var1 * 2;
        BufferedImage var7 = new BufferedImage(var5, var6, var0.getType());
        if (var2 != ClientColors.DEEP_TEAL.getColor()) {
            for (int var8 = 0; var8 < var5; var8++) {
                for (int var9 = 0; var9 < var6; var9++) {
                    var7.setRGB(var8, var9, var2);
                }
            }
        }

        for (int var10 = 0; var10 < var0.getWidth(); var10++) {
            for (int var11 = 0; var11 < var0.getHeight(); var11++) {
                var7.setRGB(var1 + var10, var1 + var11, var0.getRGB(var10, var11));
            }
        }

        return var7;
    }

    public static BufferedImage method35038(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        return method35036(var0, var1, var2, var3, var4, var5, var6, false);
    }

    public static BufferedImage method35039(int var0, int var1, int var2, int var3, int var4, int var5, boolean var6) {
        return method35036(var0, var1, var2, var3, var4, var5, ClientColors.DEEP_TEAL.getColor(), var6);
    }

    public static BufferedImage method35037(int var0, int var1, int var2, int var3, int var4, int var5) {
        return method35036(var0, var1, var2, var3, var4, var5, ClientColors.DEEP_TEAL.getColor(), false);
    }

    public static BufferedImage loadBufferedImageFromURL(String urlString) {
        try (InputStream inputStream = getInputStreamFromURL(urlString)) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            System.out.println("Failed to load buffered image: " + e.getMessage());
            return null;
        }
    }

    public static Texture loadTextureFromURL(String urlString) {
        try (InputStream inputStream = getInputStreamFromURL(urlString)) {
            return TextureLoader.getTexture("PNG", inputStream);
        } catch (IOException e) {
            System.out.println("Failed to load texture: " + e.getMessage());
            return null;
        }
    }

    public static InputStream getInputStreamFromURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to load image, HTTP response code: " + connection.getResponseCode());
        }

        InputStream stream = connection.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(stream, 8192);

        if (bis.available() <= 0) {
            throw new IOException("Empty stream from URL: " + urlString);
        }

        return bis;
    }

    public static net.minecraft.client.renderer.texture.NativeImage toNativeImage(BufferedImage bufferedImage) {
        net.minecraft.client.renderer.texture.NativeImage nativeImage = new net.minecraft.client.renderer.texture.NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int argb = bufferedImage.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                nativeImage.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        return nativeImage;
    }
}