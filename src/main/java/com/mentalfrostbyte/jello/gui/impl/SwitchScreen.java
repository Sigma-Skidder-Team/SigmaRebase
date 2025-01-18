package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.gui.base.Screen;
import com.mentalfrostbyte.jello.gui.unmapped.ImageQ;
import com.mentalfrostbyte.jello.gui.unmapped.RectangleFaded;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.MathUtils;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.Util;

import java.net.MalformedURLException;
import java.net.URL;

public class SwitchScreen extends Screen {
    public static Animation field21069 = new Animation(300, 200, Direction.BACKWARDS);
    public static float field21070;
    public static float field21071;

    public SwitchScreen() {
        super("Switch");
        this.method13300(false);
        int var3 = 537;
        int var4 = 264;
        int var5 = 93;
        int var6 = 61;
        int var7 = (this.getWidthA() - var3) / 2;
        int var8 = (this.getHeightA() - var5) / 2 + 14;
        RectangleFaded var9 = null;
        RectangleFaded var10 = null;
        RectangleFaded var11 = null;
        this.addToList(var9 = new RectangleFaded(this, "pb", var7, var8, var3, var5, Resources.noaddonsPNG));
        this.addToList(
                var11 = new RectangleFaded(this, "pb2", var7, var5 + var8 + 9, var4, var6, Resources.sigmaLigmaPNG));
        this.addToList(var10 = new RectangleFaded(this, "pb3", var7 + var4 + 9, var5 + var8 + 9, var4, var6,
                Resources.jelloPNG));
        var9.doThis((var0, var1) -> {
            Client.getInstance().setupClient(ClientMode.NOADDONS);
            MinecraftClient.getInstance().displayGuiScreen(new MainMenuScreen());
        });
        var10.doThis((var0, var1) -> {
            Client.getInstance().setupClient(ClientMode.JELLO);
            MinecraftClient.getInstance().displayGuiScreen(new MainMenuScreen());
        });
        var11.doThis((var0, var1) -> {
            Client.getInstance().setupClient(ClientMode.CLASSIC);
            MinecraftClient.getInstance().displayGuiScreen(new MainMenuScreen());
        });
        CustomGuiScreen var12 = new CustomGuiScreen(this, "socialbtns", (this.getWidthA() - 174) / 2,
                this.getHeightA() - 70, 174, 34);
        ImageQ var13;
        var12.addToList(var13 = new ImageQ(var12, "youtube", 0, 0, 65, 34, Resources.youtubePNG));
        ImageQ var14;
        var12.addToList(var14 = new ImageQ(var12, "reddit", 85, 0, 36, 34, Resources.redditPNG));
        ImageQ var15;
        var12.addToList(var15 = new ImageQ(var12, "guilded", 142, 0, 32, 34, Resources.guildedPNG));
        var13.doThis((var0, var1) -> {
            try {
                Util.getOSType().openURL(new URL("https://www.youtube.com/@sigmaclient2950"));
            } catch (MalformedURLException var5x) {
            }
        });
        var14.doThis((var0, var1) -> {
            try {
                Util.getOSType().openURL(new URL("https://www.reddit.com/r/SigmaClient/"));
            } catch (MalformedURLException var5x) {
            }
        });
        var15.doThis((var0, var1) -> {
            try {
                Util.getOSType().openURL(new URL("https://discord.gg/KBGX8FTAXa"));
            } catch (MalformedURLException var5x) {
            }
        });
        this.addToList(var12);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        float var5 = (float) newHeight - field21070;
        float var6 = (float) newWidth - field21071;
        field21070 += var5 * 0.09F;
        field21071 += var6 * 0.09F;
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float var1) {
        if (CustomLoadingScreen.back != null) {
            field21069.changeDirection(Direction.FORWARDS);
            int var4 = 40;
            float var5 = -field21070 / (float) MinecraftClient.getInstance().getWindow().getWidth();
            float var6 = -field21071 / (float) MinecraftClient.getInstance().getWindow().getHeight();
            RenderUtil.drawImage(
                    (float) var4 * var5,
                    (float) var4 * var6,
                    (float) (MinecraftClient.getInstance().getWindow().getWidth() + var4),
                    (float) (MinecraftClient.getInstance().getWindow().getHeight() + var4),
                    CustomLoadingScreen.back);
            float var7 = MathUtils.lerp(field21069.calcPercent(), 0.16, 0.71, 0.0, 0.99);
            int var8 = (MinecraftClient.getInstance().getWindow().getWidth() - 455) / 2;
            int var9 = (int) ((float) ((MinecraftClient.getInstance().getWindow().getHeight() - 78) / 2 - 14)
                    - 116.0F * var7);
            RenderUtil.drawRoundedRect2(
                    0.0F,
                    0.0F,
                    (float) MinecraftClient.getInstance().getWindow().getWidth(),
                    (float) MinecraftClient.getInstance().getWindow().getHeight(),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F));
            super.draw(var1);
            RenderUtil.drawImage(
                    0.0F,
                    0.0F,
                    (float) MinecraftClient.getInstance().getWindow().getWidth(),
                    (float) MinecraftClient.getInstance().getWindow().getHeight(),
                    CustomLoadingScreen.background,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - field21069.calcPercent()));
            RenderUtil.drawRoundedRect2(
                    0.0F,
                    0.0F,
                    (float) MinecraftClient.getInstance().getWindow().getWidth(),
                    (float) MinecraftClient.getInstance().getWindow().getHeight(),
                    ColorUtils.applyAlpha(0, 0.75F * (1.0F - field21069.calcPercent())));
            RenderUtil.drawImage((float) var8, (float) var9, 455.0F, 78.0F, CustomLoadingScreen.sigmaLogo);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            MinecraftClient.getInstance().displayGuiScreen(new MainMenuScreen());
        }
    }
}
