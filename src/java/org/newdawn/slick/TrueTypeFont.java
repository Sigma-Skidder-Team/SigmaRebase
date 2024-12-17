package org.newdawn.slick;

import com.mentalfrostbyte.jello.util.render.ImageUtil;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;
import org.newdawn.slick.opengl.GLUtils;
import org.newdawn.slick.util.BufferedImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TrueTypeFont implements Font {
    private static final SGL GL = Renderer.get();
    private final int field31944;
    public Texture fontTexture;
    private final IntObject[] charArray = new IntObject[256];
    private final Map customChars = new HashMap();
    private final boolean antiAlias;
    private int fontSize = 0;
    private int fontHeight = 0;
    private int textureWidth = 512;
    private int textureHeight = 512;
    private final java.awt.Font font;
    private FontMetrics fontMetrics;

    public TrueTypeFont(java.awt.Font font, boolean antiAlias, char[] additionalChars, int var4) {
        GLUtils.checkGLContext();
        this.font = font;
        this.fontSize = font.getSize();
        this.antiAlias = antiAlias;
        this.field31944 = var4;
        if (var4 > 0) {
            this.textureWidth = 1024;
            this.textureHeight = 1024;
        }

        this.createSet(additionalChars);
    }

    public TrueTypeFont(java.awt.Font font, boolean antiAlias, char[] additionalChars) {
        this(font, antiAlias, additionalChars, 0);
    }

    public TrueTypeFont(java.awt.Font font, boolean antiAlias) {
        this(font, antiAlias, null);
    }

    public TrueTypeFont(java.awt.Font font, int var2) {
        this(font, true, null, var2);
    }

    private BufferedImage getFontImage(char var1) {
        BufferedImage bufferedImage = new BufferedImage(1, 1, 2);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        if (this.antiAlias) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2d.setFont(this.font);
        this.fontMetrics = g2d.getFontMetrics();
        int charWidth = this.fontMetrics.charWidth(var1);
        if (charWidth <= 0) {
            charWidth = 1;
        }

        int charHeight = this.fontMetrics.getHeight();
        if (charHeight <= 0) {
            charHeight = this.fontSize;
        }

        BufferedImage bufferedImage2 = new BufferedImage(charWidth + this.field31944 * 2, charHeight + this.field31944 * 2, 2);
        Graphics2D g2d2 = (Graphics2D) bufferedImage2.getGraphics();
        if (this.antiAlias) {
            g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2d2.setFont(this.font);
        g2d2.setColor(java.awt.Color.WHITE);
        g2d2.drawString(String.valueOf(var1), this.field31944, this.field31944 + this.fontMetrics.getAscent());
        return this.field31944 <= 0 ? bufferedImage2 : ImageUtil.method35033(bufferedImage2, this.field31944);
    }

    private int method23949(char var1) {
        BufferedImage bufferedImage = new BufferedImage(1, 1, 2);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        if (this.antiAlias) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2d.setFont(this.font);
        this.fontMetrics = g2d.getFontMetrics();
        int charWidth = this.fontMetrics.charWidth(var1);
        if (charWidth <= 0) {
            charWidth = 1;
        }

        return charWidth;
    }

    private void createSet(char[] customCharsArray) {
        if (customCharsArray != null && customCharsArray.length > 0) {
            this.textureWidth *= 2;
        }

        try {
            BufferedImage bufferedImage = this.getFontImage('\u0000');
            if (bufferedImage.getHeight() > 60) {
                this.textureWidth *= 2;
                this.textureHeight *= 2;
            }

            BufferedImage bufferedImage2 = new BufferedImage(this.textureWidth, this.textureHeight, 2);
            Graphics2D g2d = (Graphics2D) bufferedImage2.getGraphics();
            g2d.setColor(new java.awt.Color(255, 255, 255, 1));
            g2d.fillRect(0, 0, this.textureWidth, this.textureHeight);
            int rowHeight = 0;
            int positionX = 0;
            int positionY = 0;
            int customCharsLength = customCharsArray != null ? customCharsArray.length : 0;

            for (int var11 = 0; var11 < 256 + customCharsLength; var11++) {
                char var12 = var11 < 256 ? (char) var11 : customCharsArray[var11 - 256];
                BufferedImage var13 = this.getFontImage(var12);
                IntObject var14 = new IntObject();
                var14.width = var13.getWidth();
                var14.height = var13.getHeight();
                if (this.field31944 > 0) {
                    var14.field35435 = this.method23949(var12);
                } else {
                    var14.field35435 = var14.width;
                }

                if (positionX + var14.width >= this.textureWidth) {
                    positionX = 0;
                    positionY += rowHeight;
                    rowHeight = 0;
                }

                var14.storedX = positionX;
                var14.storedY = positionY;
                if (var14.height > this.fontHeight) {
                    this.fontHeight = var14.height;
                }

                if (var14.height > rowHeight) {
                    rowHeight = var14.height + 1;
                }

                g2d.drawImage(var13, positionX, positionY, null);
                positionX += var14.width + 1;
                if (var11 < 256) {
                    this.charArray[var11] = var14;
                } else {
                    this.customChars.put(new Character(var12), var14);
                }
            }

            this.fontTexture = BufferedImageUtil.getTexture(this.font.toString(), bufferedImage2);
        } catch (IOException var15) {
            System.err.println("Failed to create font.");
            var15.printStackTrace();
        }
    }

    private void method23951(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
        float var11 = var3 - var1;
        float var12 = var4 - var2;
        float var13 = var5 / (float) this.textureWidth;
        float var14 = var6 / (float) this.textureHeight;
        float var15 = var7 - var5;
        float var16 = var8 - var6;
        float var17 = var15 / (float) this.textureWidth;
        float var18 = var16 / (float) this.textureHeight;
        GL.glTexCoord2f(var13, var14);
        GL.glVertex2f(var1, var2);
        GL.glTexCoord2f(var13, var14 + var18);
        GL.glVertex2f(var1, var2 + var12);
        GL.glTexCoord2f(var13 + var17, var14 + var18);
        GL.glVertex2f(var1 + var11, var2 + var12);
        GL.glTexCoord2f(var13 + var17, var14);
        GL.glVertex2f(var1 + var11, var2);
    }

    @Override
    public int getWidth(String var1) {
        int var4 = 0;
        IntObject var5 = null;
        char var6 = '\u0000';
        if (var1 != null) {
            for (int var7 = 0; var7 < var1.length(); var7++) {
                var6 = var1.charAt(var7);
                if (var6 >= 256) {
                    var5 = (IntObject) this.customChars.get(new Character(var6));
                } else {
                    var5 = this.charArray[var6];
                }

                if (var5 != null) {
                    var4 += var5.field35435;
                }
            }

            return var4;
        } else {
            return 0;
        }
    }

    public int getHeight() {
        return this.fontHeight;
    }

    @Override
    public int getHeight(String var1) {
        return this.fontHeight;
    }

    @Override
    public int getLineHeight() {
        return this.fontHeight;
    }

    @Override
    public void drawString(float var1, float var2, String var3, Color var4) {
        this.drawString(var1, var2, var3, var4, 0, var3.length() - 1);
    }

    @Override
    public void drawString(float var1, float var2, String var3, Color var4, int var5, int var6) {
        var4.bind();
        this.fontTexture.bind();
        IntObject var9 = null;
        if (this.field31944 > 0) {
            var2 -= (float) (this.field31944 / 2 - 1);
            var1 -= (float) (this.field31944 - 1);
        }

        GL.glBegin(7);
        int var10 = 0;

        for (int var11 = 0; var11 < var3.length(); var11++) {
            char var12 = var3.charAt(var11);
            if (var12 >= 256) {
                var9 = (IntObject) this.customChars.get(new Character(var12));
            } else {
                var9 = this.charArray[var12];
            }

            if (var9 != null) {
                if (var11 >= var5 || var11 <= var6) {
                    this.method23951(
                            var1 + (float) var10,
                            var2,
                            var1 + (float) var10 + (float) var9.width,
                            var2 + (float) var9.height,
                            (float) var9.storedX,
                            (float) var9.storedY,
                            (float) (var9.storedX + var9.width),
                            (float) (var9.storedY + var9.height)
                    );
                }

                var10 += var9.field35435;
            }
        }

        GL.glEnd();
    }

    @Override
    public void drawString(float var1, float var2, String var3) {
        this.drawString(var1, var2, var3, Color.white);
    }

    public static class IntObject {
       public int width;
       public int field35435;
       public int height;
       public int storedX;
       public int storedY;
    }
}
