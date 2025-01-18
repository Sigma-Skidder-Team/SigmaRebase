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

/**
 * A TrueType font implementation for Slick
 *
 * @author James Chambers (Jimmy)
 * @author Jeremy Adams (elias4444)
 * @author Kevin Glass (kevglass)
 * @author Peter Korzuszek (genail)
 */
public class TrueTypeFont implements Font {
    /**
     * The renderer to use for all GL operations
     */
    private static final SGL GL = Renderer.get();
    private final int size;
    public Texture fontTexture;
    /**
     * Array that holds necessary information about the font characters
     */
    private final IntObject[] charArray = new IntObject[256];
    /**
     * Map of user defined font characters (Character <-> IntObject)
     */
    private final Map customChars = new HashMap();
    /**
     * Boolean flag on whether AntiAliasing is enabled or not
     */
    private final boolean antiAlias;
    /**
     * Font's size
     */
    private int fontSize = 0;
    /**
     * Font's height
     */
    private int fontHeight = 0;
    /**
     * Default font texture width
     */
    private int textureWidth = 512;

    /**
     * Default font texture height
     */
    private int textureHeight = 512;
    /**
     * A reference to Java's AWT Font that we create our font texture from
     */
    private final java.awt.Font font;

    /**
     * The font metrics for our Java AWT font
     */
    private FontMetrics fontMetrics;

    /**
     * Constructor for the TrueTypeFont class Pass in the preloaded standard
     * Java TrueType font, and whether you want it to be cached with
     * AntiAliasing applied.
     *
     * @param font            Standard Java AWT font
     * @param antiAlias       Whether to apply AntiAliasing to the cached font
     * @param additionalChars Characters of font that will be used in addition of first 256 (by Unicode).
     * @param size            The size of the font
     */
    public TrueTypeFont(java.awt.Font font, boolean antiAlias, char[] additionalChars, int size) {
        GLUtils.checkGLContext();
        this.font = font;
        this.fontSize = font.getSize();
        this.antiAlias = antiAlias;
        this.size = size;
        if (size > 0) {
            this.textureWidth = 1024;
            this.textureHeight = 1024;
        }

        this.createSet(additionalChars);
    }

    /**
     * Constructor for the TrueTypeFont class Pass in the preloaded standard
     * Java TrueType font, and whether you want it to be cached with
     * AntiAliasing applied.
     *
     * @param font      Standard Java AWT font
     * @param antiAlias Whether to apply AntiAliasing to the cached font
     */
    public TrueTypeFont(java.awt.Font font, boolean antiAlias, char[] additionalChars) {
        this(font, antiAlias, additionalChars, 0);
    }

    public TrueTypeFont(java.awt.Font font, boolean antiAlias) {
        this(font, antiAlias, null);
    }

    public TrueTypeFont(java.awt.Font font, int size) {
        this(font, true, null, size);
    }

    /**
     * Create a standard Java2D BufferedImage of the given character
     *
     * @param of The character to create a BufferedImage for
     * @return A BufferedImage containing the character
     */
    private BufferedImage getFontImage(char of) {
        BufferedImage bufferedImage = new BufferedImage(1, 1, 2);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        if (this.antiAlias) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2d.setFont(this.font);
        this.fontMetrics = g2d.getFontMetrics();
        int charWidth = this.fontMetrics.charWidth(of);
        if (charWidth <= 0) {
            charWidth = 1;
        }

        int charHeight = this.fontMetrics.getHeight();
        if (charHeight <= 0) {
            charHeight = this.fontSize;
        }

        BufferedImage bufferedImage2 = new BufferedImage(charWidth + this.size * 2, charHeight + this.size * 2, 2);
        Graphics2D g2d2 = (Graphics2D) bufferedImage2.getGraphics();
        if (this.antiAlias) {
            g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2d2.setFont(this.font);
        g2d2.setColor(java.awt.Color.WHITE);
        g2d2.drawString(String.valueOf(of), this.size, this.size + this.fontMetrics.getAscent());
        return this.size <= 0 ? bufferedImage2 : ImageUtil.applyGaussianBlur(bufferedImage2, this.size);
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

    /**
     * Create and store the font
     *
     * @param customCharsArray Characters that should be also added to the cache.
     */
    private void createSet(char[] customCharsArray) {
        // If there are custom chars then I expand the font texture twice
        if (customCharsArray != null && customCharsArray.length > 0) {
            this.textureWidth *= 2;
        }

        // In any case this should be done in other way. Texture with size 512x512
        // can maintain only 256 characters with resolution of 32x32. The texture
        // size should be calculated dynamically by looking at character sizes.

        try {
            BufferedImage bufferedImage = this.getFontImage('\u0000');
            if (bufferedImage.getHeight() > 60) {
                this.textureWidth *= 2;
                this.textureHeight *= 2;
            }

            BufferedImage imgTemp = new BufferedImage(this.textureWidth, this.textureHeight, 2);
            Graphics2D g2d = (Graphics2D) imgTemp.getGraphics();
            g2d.setColor(new java.awt.Color(255, 255, 255, 1));
            g2d.fillRect(0, 0, this.textureWidth, this.textureHeight);
            int rowHeight = 0;
            int positionX = 0;
            int positionY = 0;
            int customCharsLength = customCharsArray != null ? customCharsArray.length : 0;

            for (int i = 0; i < 256 + customCharsLength; i++) {
                // get 0-255 characters and then custom characters
                char ch = i < 256 ? (char) i : customCharsArray[i - 256];
                BufferedImage var13 = this.getFontImage(ch);
                IntObject newIntObject = new IntObject();
                newIntObject.width = var13.getWidth();
                newIntObject.height = var13.getHeight();
                if (this.size > 0) {
                    newIntObject.actualWidth = this.method23949(ch);
                } else {
                    newIntObject.actualWidth = newIntObject.width;
                }

                if (positionX + newIntObject.width >= this.textureWidth) {
                    positionX = 0;
                    positionY += rowHeight;
                    rowHeight = 0;
                }

                newIntObject.storedX = positionX;
                newIntObject.storedY = positionY;
                if (newIntObject.height > this.fontHeight) {
                    this.fontHeight = newIntObject.height;
                }

                if (newIntObject.height > rowHeight) {
                    rowHeight = newIntObject.height + 1;
                }

                // Draw it here
                g2d.drawImage(var13, positionX, positionY, null);
                positionX += newIntObject.width + 1;
                if (i < 256) { // standard characters
                    this.charArray[i] = newIntObject;
                } else { // custom characters
                    this.customChars.put(ch, newIntObject);
                }
            }

            this.fontTexture = BufferedImageUtil.getTexture(this.font.toString(), imgTemp);
        } catch (IOException var15) {
            System.err.println("Failed to create font.");
            var15.printStackTrace();
        }
    }

    /**
     * Draw a textured quad
     *
     * @param drawX  The left x position to draw to
     * @param drawY  The top y position to draw to
     * @param drawX2 The right x position to draw to
     * @param drawY2 The bottom y position to draw to
     * @param srcX   The left source x position to draw from
     * @param srcY   The top source y position to draw from
     * @param srcX2  The right source x position to draw from
     * @param srcY2  The bottom source y position to draw from
     */
    private void drawQuad(float drawX, float drawY, float drawX2, float drawY2,
                          float srcX, float srcY, float srcX2, float srcY2) {
        float DrawWidth = drawX2 - drawX;
        float DrawHeight = drawY2 - drawY;
        float TextureSrcX = srcX / textureWidth;
        float TextureSrcY = srcY / textureHeight;
        float SrcWidth = srcX2 - srcX;
        float SrcHeight = srcY2 - srcY;
        float RenderWidth = (SrcWidth / textureWidth);
        float RenderHeight = (SrcHeight / textureHeight);

        GL.glTexCoord2f(TextureSrcX, TextureSrcY);
        GL.glVertex2f(drawX, drawY);
        GL.glTexCoord2f(TextureSrcX, TextureSrcY + RenderHeight);
        GL.glVertex2f(drawX, drawY + DrawHeight);
        GL.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY + RenderHeight);
        GL.glVertex2f(drawX + DrawWidth, drawY + DrawHeight);
        GL.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY);
        GL.glVertex2f(drawX + DrawWidth, drawY);
    }

    /**
     * Get the width of a given String
     *
     * @param whatchars The characters to get the width of
     * @return The width of the characters
     */
    public int getWidth(String whatchars) {
        int totalwidth = 0;
        IntObject intObject = null;
        int currentChar = 0;
        for (int i = 0; i < whatchars.length(); i++) {
            currentChar = whatchars.charAt(i);
            if (currentChar < 256) {
                intObject = charArray[currentChar];
            } else {
                intObject = (IntObject) customChars.get(new Character((char) currentChar));
            }

            if (intObject != null)
                totalwidth += intObject.width;
        }
        return totalwidth;
    }

    /**
     * Get the font's height
     *
     * @return The height of the font
     */
    public int getHeight() {
        return fontHeight;
    }

    /**
     * Get the height of a String
     *
     * @return The height of a given string
     */
    public int getHeight(String HeightString) {
        return fontHeight;
    }

    /**
     * Get the font's line height
     *
     * @return The line height of the font
     */
    public int getLineHeight() {
        return fontHeight;
    }

    /**
     * Draw a string
     *
     * @param x         The x position to draw the string
     * @param y         The y position to draw the string
     * @param whatchars The string to draw
     * @param color     The color to draw the text
     */
    public void drawString(float x, float y, String whatchars,
                           org.newdawn.slick.Color color) {
        drawString(x, y, whatchars, color, 0, whatchars.length() - 1);
    }

    /**
     * @see Font#drawString(float, float, String, org.newdawn.slick.Color, int, int)
     */
    @Override
    public void drawString(float x, float y, String string, Color color, int startIndex, int endIndex) {
        color.bind();
        this.fontTexture.bind();
        IntObject intObject = null;
        if (this.size > 0) {
            y -= (float) (this.size / 2 - 1);
            x -= (float) (this.size - 1);
        }

        GL.glBegin(7);
        int charIdx = 0;

        for (int var11 = 0; var11 < string.length(); var11++) {
            char charCurrent = string.charAt(var11);
            if (charCurrent >= 256) {
                intObject = (IntObject) this.customChars.get(charCurrent);
            } else {
                intObject = this.charArray[charCurrent];
            }

            if (intObject != null) {
                if (var11 >= startIndex || var11 <= endIndex) {
                    this.drawQuad(
                            x + (float) charIdx,
                            y,
                            x + (float) charIdx + (float) intObject.width,
                            y + (float) intObject.height,
                            (float) intObject.storedX,
                            (float) intObject.storedY,
                            (float) (intObject.storedX + intObject.width),
                            (float) (intObject.storedY + intObject.height)
                    );
                }

                charIdx += intObject.actualWidth;
            }
        }

        GL.glEnd();
    }

    /**
     * Draw a string
     *
     * @param x      The x position to draw the string
     * @param y      The y position to draw the string
     * @param string The string to draw
     */
    @Override
    public void drawString(float x, float y, String string) {
        this.drawString(x, y, string, Color.white);
    }

    /**
     * This is a special internal class that holds our necessary information for
     * the font characters. This includes width, height, and where the character
     * is stored on the font texture.
     */
    public static class IntObject {
        /**
         * Character's width
         */
        public int width;
        public int actualWidth;
        /**
         * Character's height
         */
        public int height;
        /**
         * Character's stored x position
         */
        public int storedX;
        /**
         * Character's stored y position
         */
        public int storedY;
    }
}
