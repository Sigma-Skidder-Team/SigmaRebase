package com.mentalfrostbyte.jello.util.render;

import com.mentalfrostbyte.Client;
import org.newdawn.slick.TrueTypeFont;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColorUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final Identifier BLUR_SHADER = new Identifier("shaders/post/blur.json");

    public static int applyAlpha(int color, float alpha) {
        return (int) (alpha * 255.0F) << 24 | color & 16777215;
    }

    /**
     * Adjusts the RGB components of a color towards black by a specified factor.
     * The alpha component remains unchanged.
     *
     * @param color The original color represented as an integer, where the highest
     *              byte is the alpha component,
     *              followed by red, green, and blue components.
     * @param shift The factor by which to adjust the color towards black. A value
     *              of 0.0 will leave the color unchanged,
     *              while a value of 1.0 will result in a completely black color.
     * @return The adjusted color as an integer, with the same alpha component as
     *         the original color and RGB components
     *         adjusted towards black by the specified factor.
     */
    public static int shiftTowardsBlack(int color, float shift) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int shiftedR = (int) ((float) r * (1.0F - shift));
        int shiftedG = (int) ((float) g * (1.0F - shift));
        int shiftedB = (int) ((float) b * (1.0F - shift));
        return a << 24 | (shiftedR & 0xFF) << 16 | (shiftedG & 0xFF) << 8 | shiftedB & 0xFF;
    }

    public static List<PlayerEntity> method17680() {
        ArrayList<PlayerEntity> var2 = new ArrayList<>();
        mc.world.entitiesById.forEach((var1, var2x) -> {
            if (var2x instanceof PlayerEntity) {
                var2.add((PlayerEntity) var2x);
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
        float var14 = (float) var5 * var2 + (float) var9 * var13;
        float var15 = (float) var6 * var2 + (float) var10 * var13;
        float var16 = (float) var7 * var2 + (float) var11 * var13;
        float var17 = (float) var8 * var2 + (float) var12 * var13;
        return (int) var14 << 24 | ((int) var15 & 0xFF) << 16 | ((int) var16 & 0xFF) << 8 | (int) var17 & 0xFF;
    }

    public static float getAlpha(int color) {
        return (float) (color >> 24 & 0xFF) / 255.0F;
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
        return new float[] { var10, var11, var8, var9 };
    }

    public static String[] method17745(String var0, int var1, TrueTypeFont var2) {
        String[] var5 = var0.split(" ");
        HashMap<Integer, String> var6 = new HashMap();
        int var7 = 0;

        for (String var11 : var5) {
            String var12 = var6.get(var7) != null ? (String) var6.get(var7) : "";
            boolean var13 = var6.get(var7) == null;
            boolean var14 = var2.getWidth(var12) + var2.getWidth(var11) <= var1;
            boolean var15 = var2.getWidth(var11) >= var1;
            if (!var14 && !var15) {
                var7++;
                var12 = var6.get(var7) != null ? (String) var6.get(var7) : "";
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

                        var12 = var6.get(var7) != null ? (String) var6.get(var7) : "";
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

    public static void blur() {
        if (mc.getRenderViewEntity() instanceof PlayerEntity && Client.getInstance().guiManager.getGuiBlur()) {
            if (mc.gameRenderer.shaderGroup != null) {
                mc.gameRenderer.shaderGroup.close();
            }

            mc.gameRenderer.loadShader(BLUR_SHADER);
        }

        setShaderParams(20);
    }

    public static void setShaderParams(int radius) {
        if (mc.gameRenderer.shaderGroup != null) {
            mc.gameRenderer.shaderGroup.listShaders.get(0).getShaderManager().getShaderUniform("Radius")
                    .set((float) radius);
            mc.gameRenderer.shaderGroup.listShaders.get(1).getShaderManager().getShaderUniform("Radius")
                    .set((float) radius);
        }
    }

    /**
     * Resets the current shader to its default state or loads a specific shader
     * based on the current shader index.
     * If the shader index is equal to the total number of shaders, it sets the
     * shader group to null, effectively
     * disabling any active shaders. Otherwise, it loads the shader corresponding to
     * the current shader index.
     */
    public static void resetShaders() {
        if (mc.gameRenderer.shaderIndex == GameRenderer.SHADER_COUNT) {
            mc.gameRenderer.shaderGroup = null;
        } else {
            mc.gameRenderer.loadShader(GameRenderer.SHADERS_TEXTURES[mc.gameRenderer.shaderIndex]);
        }
    }

    public static float[] intColorToFloatArrayColor(int color) {
        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        return new float[] { r, g, b, a };
    }

    /**
     * Adjusts the RGB components of a color towards white by a specified factor.
     *
     * @param original The original color represented as an integer, where the
     *                 highest byte is the alpha component,
     *                 followed by red, green, and blue components.
     * @param shift    The factor by which to adjust the color towards white. A
     *                 value of 0.0 will leave the color unchanged,
     *                 while a value of 1.0 will result in a completely white color.
     * @return The adjusted color as an integer, with the same alpha component as
     *         the original color and RGB components
     *         adjusted towards white by the specified factor.
     */
    public static int adjustColorTowardsWhite(int original, float shift) {
        int a = original >> 24 & 0xFF;
        int r = original >> 16 & 0xFF;
        int g = original >> 8 & 0xFF;
        int b = original & 0xFF;
        int var8 = (int) ((float) r + (float) (255 - r) * shift);
        int var9 = (int) ((float) g + (float) (255 - g) * shift);
        int var10 = (int) ((float) b + (float) (255 - b) * shift);
        return a << 24 | (var8 & 0xFF) << 16 | (var9 & 0xFF) << 8 | var10 & 0xFF;
    }

    public static Color method17682(Color... var0) {
        if (var0 != null) {
            if (var0.length > 0) {
                float var3 = 1.0F / (float) var0.length;
                float var4 = 0.0F;
                float var5 = 0.0F;
                float var6 = 0.0F;
                float var7 = 0.0F;

                for (Color var11 : var0) {
                    if (var11 == null) {
                        var11 = Color.BLACK;
                    }

                    var4 += (float) var11.getRed() * var3;
                    var5 += (float) var11.getGreen() * var3;
                    var6 += (float) var11.getBlue() * var3;
                    var7 += (float) var11.getAlpha() * var3;
                }

                return new Color(var4 / 255.0F, var5 / 255.0F, var6 / 255.0F, var7 / 255.0F);
            } else {
                return Color.WHITE;
            }
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Blends two colors by a specified factor, resulting in a color that is a mix
     * of the two.
     * The blending is done by interpolating each RGB component separately.
     *
     * @param first  The first color to blend, represented as a {@link Color}
     *               object.
     * @param second The second color to blend, represented as a {@link Color}
     *               object.
     * @param factor The blending factor, where 0.0 results in the second color, and
     *               1.0 results in the first color.
     *               Values between 0.0 and 1.0 will result in a mix of the two
     *               colors.
     * @return A new {@link Color} object representing the blended color.
     */
    public static Color blendColor(Color first, Color second, float factor) {
        float newFactor = 1.0F - factor;
        float blendedR = (float) first.getRed() * factor + (float) second.getRed() * newFactor;
        float blendedG = (float) first.getGreen() * factor + (float) second.getGreen() * newFactor;
        float blendedB = (float) first.getBlue() * factor + (float) second.getBlue() * newFactor;
        return new Color(blendedR / 255.0F, blendedG / 255.0F, blendedB / 255.0F);
    }

    public static void setShaderParamsRounded(float radius) {
        setShaderParams(Math.round(radius * 20.0F));
    }
}
