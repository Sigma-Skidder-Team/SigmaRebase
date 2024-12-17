package com.mentalfrostbyte.jello.util.render;

import com.mentalfrostbyte.Client;
import org.newdawn.slick.TrueTypeFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColorUtils {
    private static final Minecraft mc = Minecraft.getInstance();
    public static final float[] field24951 = new float[4];
    public static final float[] field24952 = new float[4];
    public static final ResourceLocation field24953 = new ResourceLocation("shaders/post/blur.json");
    private static boolean field24954 = false;

    public static int applyAlpha(int color, float alpha) {
        return (int)(alpha * 255.0F) << 24 | color & 16777215;
    }

    public static int method17691(int var0, float var1) {
        int var4 = var0 >> 24 & 0xFF;
        int var5 = var0 >> 16 & 0xFF;
        int var6 = var0 >> 8 & 0xFF;
        int var7 = var0 & 0xFF;
        int var8 = (int)((float)var5 * (1.0F - var1));
        int var9 = (int)((float)var6 * (1.0F - var1));
        int var10 = (int)((float)var7 * (1.0F - var1));
        return var4 << 24 | (var8 & 0xFF) << 16 | (var9 & 0xFF) << 8 | var10 & 0xFF;
    }

    public static List<PlayerEntity> method17680() {
        ArrayList<PlayerEntity> var2 = new ArrayList<>();
        mc.world.entitiesById.forEach((var1, var2x) -> {
            if (var2x instanceof PlayerEntity) {
                var2.add((PlayerEntity)var2x);
            }
        });
        return var2;
    }

    public static int method17690(int var0, int var1, float var2) {
        int var5 = var0 >> 24 & 0xFF;
        int var6 = var0 >> 16 & 0xFF;
        int var7 = var0 >> 8 & 0xFF;
        int var8 = var0 & 0xFF;
        int var9 = var1 >> 24 & 0xFF;
        int var10 = var1 >> 16 & 0xFF;
        int var11 = var1 >> 8 & 0xFF;
        int var12 = var1 & 0xFF;
        float var13 = 1.0F - var2;
        float var14 = (float)var5 * var2 + (float)var9 * var13;
        float var15 = (float)var6 * var2 + (float)var10 * var13;
        float var16 = (float)var7 * var2 + (float)var11 * var13;
        float var17 = (float)var8 * var2 + (float)var12 * var13;
        return (int)var14 << 24 | ((int)var15 & 0xFF) << 16 | ((int)var16 & 0xFF) << 8 | (int)var17 & 0xFF;
    }

    public static float method17710(int var0) {
        return (float)(var0 >> 24 & 0xFF) / 255.0F;
    }

    public static float[] method17701(float var0, float var1, float var2, float var3) {
        float var6 = var0 / var1;
        float var7 = var2 / var3;
        float var8;
        float var9;
        if (!(var7 <= var6)) {
            var8 = var2;
            var9 = var1 * var2 / var0;
        } else {
            var8 = var0 * var3 / var1;
            var9 = var3;
        }

        float var10 = (var2 - var8) / 2.0F;
        float var11 = (var3 - var9) / 2.0F;
        return new float[]{var10, var11, var8, var9};
    }

    public static String[] method17745(String var0, int var1, TrueTypeFont var2) {
        String[] var5 = var0.split(" ");
        HashMap<Integer, String> var6 = new HashMap();
        int var7 = 0;

        for (String var11 : var5) {
            String var12 = var6.get(var7) != null ? (String)var6.get(var7) : "";
            boolean var13 = var6.get(var7) == null;
            boolean var14 = var2.getWidth(var12) + var2.getWidth(var11) <= var1;
            boolean var15 = var2.getWidth(var11) >= var1;
            if (!var14 && !var15) {
                var7++;
                var12 = var6.get(var7) != null ? (String)var6.get(var7) : "";
                var13 = var6.get(var7) == null;
                var14 = var2.getWidth(var12) + var2.getWidth(var11) <= var1;
                var15 = var2.getWidth(var11) >= var1;
            }

            if (var14) {
                if (!var13) {
                    var6.put(var7, var12 + " " + var11);
                } else {
                    var6.put(var7, var11);
                }
            } else if (var15) {
                while (var15 && !var14) {
                    int var16 = 0;

                    while (true) {
                        if (var16 <= var11.length()) {
                            String var17 = var11.substring(0, var11.length() - var16);
                            if (var2.getWidth(var17) > var1) {
                                var16++;
                                continue;
                            }

                            var6.put(++var7, var17);
                            var11 = var11.substring(var11.length() - var16, var11.length());
                        }

                        var12 = var6.get(var7) != null ? (String)var6.get(var7) : "";
                        var14 = var2.getWidth(var12) + var2.getWidth(var11) <= var1;
                        var15 = var2.getWidth(var11) >= var1;
                        var13 = var6.get(var7) == null;
                        break;
                    }
                }

                if (!var14) {
                    var7++;
                }

                var6.put(var7, var11);
            }
        }

        return var6.values().toArray(new String[var6.values().size()]);
    }

    public static void method17739() {
        if (mc.getRenderViewEntity() instanceof PlayerEntity && Client.getInstance().guiManager.getGuiBlur()) {
            if (mc.gameRenderer.shaderGroup != null) {
                mc.gameRenderer.shaderGroup.close();
            }

            mc.gameRenderer.loadShader(field24953);
        }

        method17741(20);
    }

    public static void method17741(int var0) {
        if (mc.gameRenderer.shaderGroup != null) {
            mc.gameRenderer.shaderGroup.listShaders.get(0).getShaderManager().getShaderUniform("Radius").set((float)var0);
            mc.gameRenderer.shaderGroup.listShaders.get(1).getShaderManager().getShaderUniform("Radius").set((float)var0);
        }
    }

    public static void method17742() {
        if (mc.gameRenderer.shaderIndex == GameRenderer.SHADER_COUNT) {
            mc.gameRenderer.shaderGroup = null;
        } else {
            mc.gameRenderer.loadShader(GameRenderer.SHADERS_TEXTURES[mc.gameRenderer.shaderIndex]);
        }
    }

    public static float[] method17709(int var0) {
        float var3 = (float)(var0 >> 24 & 0xFF) / 255.0F;
        float var4 = (float)(var0 >> 16 & 0xFF) / 255.0F;
        float var5 = (float)(var0 >> 8 & 0xFF) / 255.0F;
        float var6 = (float)(var0 & 0xFF) / 255.0F;
        return new float[]{var4, var5, var6, var3};
    }

    public static int method17692(int var0, float var1) {
        int var4 = var0 >> 24 & 0xFF;
        int var5 = var0 >> 16 & 0xFF;
        int var6 = var0 >> 8 & 0xFF;
        int var7 = var0 & 0xFF;
        int var8 = (int)((float)var5 + (float)(255 - var5) * var1);
        int var9 = (int)((float)var6 + (float)(255 - var6) * var1);
        int var10 = (int)((float)var7 + (float)(255 - var7) * var1);
        return var4 << 24 | (var8 & 0xFF) << 16 | (var9 & 0xFF) << 8 | var10 & 0xFF;
    }

    public static Color method17682(Color... var0) {
        if (var0 != null) {
            if (var0.length > 0) {
                float var3 = 1.0F / (float)var0.length;
                float var4 = 0.0F;
                float var5 = 0.0F;
                float var6 = 0.0F;
                float var7 = 0.0F;

                for (Color var11 : var0) {
                    if (var11 == null) {
                        var11 = Color.BLACK;
                    }

                    var4 += (float)var11.getRed() * var3;
                    var5 += (float)var11.getGreen() * var3;
                    var6 += (float)var11.getBlue() * var3;
                    var7 += (float)var11.getAlpha() * var3;
                }

                return new Color(var4 / 255.0F, var5 / 255.0F, var6 / 255.0F, var7 / 255.0F);
            } else {
                return Color.WHITE;
            }
        } else {
            return Color.WHITE;
        }
    }

    public static Color method17681(Color var0, Color var1, float var2) {
        float var5 = 1.0F - var2;
        float var6 = (float)var0.getRed() * var2 + (float)var1.getRed() * var5;
        float var7 = (float)var0.getGreen() * var2 + (float)var1.getGreen() * var5;
        float var8 = (float)var0.getBlue() * var2 + (float)var1.getBlue() * var5;
        return new Color(var6 / 255.0F, var7 / 255.0F, var8 / 255.0F);
    }

    public static void method17740(float var0) {
        method17741(Math.round(var0 * 20.0F));
    }
}
