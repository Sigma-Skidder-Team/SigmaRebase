package com.mentalfrostbyte.jello.gui.combined.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.image.Image;
import com.mentalfrostbyte.jello.gui.base.elements.impl.image.types.FadedImage;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.system.math.MathUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuHolder;
import net.minecraft.util.Util;

import java.net.MalformedURLException;
import java.net.URL;

public class SwitchScreen extends Screen {
    public static Animation field21069 = new Animation(1050, 200, Animation.Direction.BACKWARDS);
    public static float field21070;
    public static float field21071;


    public SwitchScreen() {
        super("Switch");
        this.setListening(false);
        int var3 = 537;
        int var4 = 264;
        int var5 = 93;
        int var6 = 61;
        int var7 = (this.getWidthA() - var3) / 2;
        int var8 = (this.getHeightA() - var5) / 2 + 14;
        FadedImage var9 = null;
        FadedImage var10 = null;
        FadedImage var11 = null;
        this.addToList(var9 = new FadedImage(this, "pb", var7, var8, var3, var5, Resources.noaddonsPNG));
        this.addToList(var11 = new FadedImage(this, "pb2", var7, var5 + var8 + 9, var4, var6, Resources.sigmaLigmaPNG));
        this.addToList(var10 = new FadedImage(this, "pb3", var7 + var4 + 9, var5 + var8 + 9, var4, var6, Resources.jelloPNG));
        var9.doThis((var0, var1) -> {
            Client.getInstance().setupClient(ClientMode.NOADDONS);
            Minecraft.getInstance().displayGuiScreen(new MainMenuHolder());
        });
        var10.doThis((var0, var1) -> {
            Client.getInstance().setupClient(ClientMode.JELLO);
            Minecraft.getInstance().displayGuiScreen(new MainMenuHolder());
        });
        var11.doThis((var0, var1) -> {
            Client.getInstance().setupClient(ClientMode.CLASSIC);
            Minecraft.getInstance().displayGuiScreen(new MainMenuHolder());
        });
        CustomGuiScreen var12 = new CustomGuiScreen(this, "socialbtns", (this.getWidthA() - 174) / 2, this.getHeightA() - 70, 174, 34);
        Image var13;
        var12.addToList(var13 = new Image(var12, "youtube", 0, 0, 65, 34, Resources.youtubePNG));
        Image var14;
        var12.addToList(var14 = new Image(var12, "reddit", 85, 0, 36, 34, Resources.redditPNG));
        Image var15;
        var12.addToList(var15 = new Image(var12, "guilded", 142, 0, 32, 34, Resources.guildedPNG));
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
    public void draw(float partialTicks) {
        if (LoadingScreen.back != null) {
            field21069.changeDirection(Animation.Direction.FORWARDS);
            int var4 = 40;
            float var5 = -field21070 / (float) Minecraft.getInstance().getMainWindow().getWidth();
            float var6 = -field21071 / (float) Minecraft.getInstance().getMainWindow().getHeight();
            RenderUtil.drawImage(
                    (float) var4 * var5,
                    (float) var4 * var6,
                    (float) (Minecraft.getInstance().getMainWindow().getWidth() + var4),
                    (float) (Minecraft.getInstance().getMainWindow().getHeight() + var4),
                    LoadingScreen.back
            );
            float var7 = MathUtil.lerp(field21069.calcPercent(), 0.16, 0.71, 0.0, 0.99);
            int var8 = (Minecraft.getInstance().getMainWindow().getWidth() - 455) / 2;
            int var9 = (int) ((float) ((Minecraft.getInstance().getMainWindow().getHeight() - 78) / 2 - 14) - 116.0F * var7);
            RenderUtil.drawRoundedRect2(
                    0.0F,
                    0.0F,
                    (float) Minecraft.getInstance().getMainWindow().getWidth(),
                    (float) Minecraft.getInstance().getMainWindow().getHeight(),
                    RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F)
            );
            super.draw(partialTicks);
            RenderUtil.drawImage(
                    0.0F,
                    0.0F,
                    (float) Minecraft.getInstance().getMainWindow().getWidth(),
                    (float) Minecraft.getInstance().getMainWindow().getHeight(),
                    LoadingScreen.background,
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - field21069.calcPercent())
            );
            RenderUtil.drawRoundedRect2(
                    0.0F,
                    0.0F,
                    (float) Minecraft.getInstance().getMainWindow().getWidth(),
                    (float) Minecraft.getInstance().getMainWindow().getHeight(),
                    RenderUtil2.applyAlpha(0, 0.75F * (1.0F - field21069.calcPercent()))
            );
            RenderUtil.drawImage((float) var8, (float) var9, 455.0F, 78.0F, LoadingScreen.sigmaLogo);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            Minecraft.getInstance().displayGuiScreen(new MainMenuHolder());
        }
    }
}
