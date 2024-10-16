package com.mentalfrostbyte.jello.utilities.assets;

import com.mentalfrostbyte.jello.Client;
import com.mentalfrostbyte.jello.utilities.ImageUtil;
import com.mentalfrostbyte.jello.utilities.TextureUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ResourcesDecrypter {
    public static final String field32485 = "com/mentalfrostbyte/gui/resources/";
    public static Texture multiplayerPNG;
    public static Texture optionsPNG;
    public static Texture singleplayerPNG;
    public static Texture shopPNG;
    public static Texture altPNG;
    public static Texture logoLargePNG;
    public static Texture logoLarge2xPNG;
    public static Texture verticalScrollBarTopPNG;
    public static Texture verticalScrollBarBottomPNG;
    public static Texture[] panoramaPNGs = new Texture[1];
    public static Texture checkPNG;
    public static Texture trashcanPNG;
    public static Texture jelloWatermarkPNG;
    public static Texture jelloWatermark2xPNG;
    public static Texture shadowLeftPNG;
    public static Texture shadowRightPNG;
    public static Texture shadowTopPNG;
    public static Texture shadowBottomPNG;
    public static Texture shadowCorner1PNG;
    public static Texture shadowCorner2PNG;
    public static Texture shadowCorner3PNG;
    public static Texture shadowCorner4PNG;
    public static Texture panoramaPNG;
    public static Texture playPNG;
    public static Texture pausePNG;
    public static Texture forwardsPNG;
    public static Texture backwardsPNG;
    public static Texture bgPNG;
    public static Texture artworkPNG;
    public static Texture particlePNG;
    public static Texture repeatPNG;
    public static Texture playIconPNG;
    public static Texture infoIconPNG;
    public static Texture shoutIconPNG;
    public static Texture alertIconPNG;
    public static Texture directionIconPNG;
    public static Texture cancelIconPNG;
    public static Texture doneIconPNG;
    public static Texture gingerbreadIconPNG;
    public static Texture floatingBorderPNG;
    public static Texture floatingCornerPNG;
    public static Texture cerclePNG;
    public static Texture selectPNG;
    public static Texture activePNG;
    public static Texture errorsPNG;
    public static Texture shadowPNG;
    public static Texture imgPNG;
    public static Texture skinPNG;
    public static Texture loadingIndicatorPNG;
    public static Texture mentalfrostbytePNG;
    public static Texture sigmaPNG;
    public static Texture tomyPNG;
    public static Texture androPNG;
    public static Texture lpPNG;
    public static Texture cxPNG;
    public static Texture codyPNG;
    public static Texture accountPNG;
    public static Texture waypointPNG;
    public static Texture noaddonsPNG;
    public static Texture jelloPNG;
    public static Texture sigmaLigmaPNG;
    public static Texture searchPNG;
    public static Texture optionsPNG1;
    public static Texture dvdPNG;
    public static Texture gemPNG;
    public static Texture foregroundPNG;
    public static Texture backgroundPNG;
    public static Texture middlePNG;
    public static Texture youtubePNG;
    public static Texture guildedPNG;
    public static Texture redditPNG;
    private static final byte[] xorKey = new byte[]{89, -73, -35, -84, 17, -87, -79, -44};

    public static void decrypt() {
        multiplayerPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/icons/multiplayer.png");
        optionsPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/icons/options.png");
        singleplayerPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/icons/singleplayer.png");
        shopPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/icons/shop.png");
        altPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/icons/alt.png");
        panoramaPNGs[0] = loadTexture("com/mentalfrostbyte/gui/resources/" + getPanoramaPNG());
        logoLargePNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/logo_large.png");
        logoLarge2xPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/logo_large@2x.png");
        shadowLeftPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_left.png");
        shadowRightPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_right.png");
        shadowTopPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_top.png");
        shadowBottomPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_bottom.png");
        shadowCorner1PNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_corner.png");
        shadowCorner2PNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_corner_2.png");
        shadowCorner3PNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_corner_3.png");
        shadowCorner4PNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/shadow_corner_4.png");
        floatingCornerPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/floating_corner.png");
        floatingBorderPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/floating_border.png");
        verticalScrollBarTopPNG = loadTexture("com/mentalfrostbyte/gui/resources/component/verticalscrollbartop.png");
        verticalScrollBarBottomPNG = loadTexture("com/mentalfrostbyte/gui/resources/component/verticalscrollbarbottom.png");
        checkPNG = loadTexture("com/mentalfrostbyte/gui/resources/component/check.png");
        waypointPNG = loadTexture("com/mentalfrostbyte/gui/resources/component/waypoint.png");
        trashcanPNG = loadTexture("com/mentalfrostbyte/gui/resources/component/trashcan.png");
        gemPNG = loadTexture("com/mentalfrostbyte/gui/resources/sigma/gem.png");
        jelloWatermarkPNG = loadTexture("com/mentalfrostbyte/gui/resources/sigma/jello_watermark.png");
        jelloWatermark2xPNG = loadTexture("com/mentalfrostbyte/gui/resources/sigma/jello_watermark@2x.png");
        playPNG = loadTexture("com/mentalfrostbyte/gui/resources/music/play.png");
        pausePNG = loadTexture("com/mentalfrostbyte/gui/resources/music/pause.png");
        forwardsPNG = loadTexture("com/mentalfrostbyte/gui/resources/music/forwards.png");
        backwardsPNG = loadTexture("com/mentalfrostbyte/gui/resources/music/backwards.png");
        bgPNG = loadTexture("com/mentalfrostbyte/gui/resources/music/bg.png");
        artworkPNG = loadTexture("com/mentalfrostbyte/gui/resources/music/artwork.png");
        particlePNG = loadTexture("com/mentalfrostbyte/gui/resources/music/particle.png");
        repeatPNG = loadTexture("com/mentalfrostbyte/gui/resources/music/repeat.png");
        playIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/play-icon.png");
        infoIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/info-icon.png");
        shoutIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/shout-icon.png");
        alertIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/alert-icon.png");
        directionIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/direction-icon.png");
        cancelIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/cancel-icon.png");
        doneIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/done-icon.png");
        gingerbreadIconPNG = loadTexture("com/mentalfrostbyte/gui/resources/notifications/gingerbread-icon.png");
        cerclePNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/cercle.png");
        selectPNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/select.png");
        activePNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/active.png");
        errorsPNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/errors.png");
        shadowPNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/shadow.png");
        imgPNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/img.png");
        skinPNG = loadTexture("com/mentalfrostbyte/gui/resources/alt/skin.png");
        noaddonsPNG = loadTexture("com/mentalfrostbyte/gui/resources/loading/noaddons.png");
        jelloPNG = loadTexture("com/mentalfrostbyte/gui/resources/loading/jello.png");
        sigmaLigmaPNG = loadTexture("com/mentalfrostbyte/gui/resources/loading/sigma.png");
        loadingIndicatorPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/loading_indicator.png");
        searchPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/search.png");
        optionsPNG1 = loadTexture("com/mentalfrostbyte/gui/resources/jello/options.png");
        mentalfrostbytePNG = loadTexture("com/mentalfrostbyte/gui/resources/mentalfrostbyte/mentalfrostbyte.png");
        sigmaPNG = loadTexture("com/mentalfrostbyte/gui/resources/mentalfrostbyte/sigma.png");
        tomyPNG = loadTexture("com/mentalfrostbyte/gui/resources/mentalfrostbyte/tomy.png");
        androPNG = loadTexture("com/mentalfrostbyte/gui/resources/sigma/andro.png");
        lpPNG = loadTexture("com/mentalfrostbyte/gui/resources/sigma/lp.png");
        cxPNG = loadTexture("com/mentalfrostbyte/gui/resources/user/cx.png");
        codyPNG = loadTexture("com/mentalfrostbyte/gui/resources/user/cody.png");
        accountPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/account.png");
        foregroundPNG = loadTexture("com/mentalfrostbyte/gui/resources/background/foreground.png");
        backgroundPNG = loadTexture("com/mentalfrostbyte/gui/resources/background/background.png");
        middlePNG = loadTexture("com/mentalfrostbyte/gui/resources/background/middle.png");
        youtubePNG = loadTexture("com/mentalfrostbyte/gui/resources/loading/youtube.png");
        guildedPNG = loadTexture("com/mentalfrostbyte/gui/resources/loading/guilded.png");
        redditPNG = loadTexture("com/mentalfrostbyte/gui/resources/loading/reddit.png");
        dvdPNG = loadTexture("com/mentalfrostbyte/gui/resources/jello/dvd.png");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/activate.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/deactivate.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/click.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/error.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/pop.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/connect.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/switch.mp3");
        readInputStream("com/mentalfrostbyte/gui/resources/audio/clicksound.mp3");
        panoramaPNG = createScaledAndProcessedTexture1("com/mentalfrostbyte/gui/resources/" + getPanoramaPNG(), 0.25F, 30);
    }

    public static Texture loadTexture(String filePath) {
        try {
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase();
            return loadTexture(filePath, extension);
        } catch (Exception e) {
            e.printStackTrace();
            Client.getInstance().getLogger().error(
                    "Unable to load texture " + filePath +
                            ". Please make sure it is a valid path and has a valid extension."
            );
            throw e;
        }
    }

    public static Texture loadTexture(String filePath, String fileType) {
        try {
            return TextureLoader.getTexture(fileType, readInputStream(filePath));
        } catch (IOException e) {
            try (InputStream inputStream = readInputStream(filePath)) {
                byte[] header = new byte[8];
                inputStream.read(header);
                StringBuilder headerInfo = new StringBuilder();
                for (int value : header) {
                    headerInfo.append(" ").append(value);
                }
                throw new IllegalStateException("Unable to load texture " + filePath + " header: " + headerInfo);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load texture " + filePath, ex);
            }
        }
    }

    public static InputStream readInputStream(String fileName) {
        try {
            // The file path within the Minecraft assets folder
            String assetPath = "assets/minecraft/" + fileName;

            // Attempt to load the resource directly from the classpath
            InputStream resourceStream = Client.getInstance().getClass().getClassLoader().getResourceAsStream(assetPath);

            if (resourceStream != null) {
                return resourceStream;
            } else {
                throw new IllegalStateException("Resource not found: " + assetPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load resource " + fileName + ". Error during resource loading.", e
            );
        }
    }

    public static Texture createScaledAndProcessedTexture1(String var0, float var1, int var2) {
        try {
            BufferedImage var5 = ImageIO.read(readInputStream(var0));
            BufferedImage var6 = new BufferedImage((int) (var1 * (float) var5.getWidth(null)), (int) (var1 * (float) var5.getHeight(null)), 2);
            Graphics2D var7 = (Graphics2D) var6.getGraphics();
            var7.scale(var1, var1);
            var7.drawImage(var5, 0, 0, null);
            var7.dispose();
            var5 = ImageUtil.method35032(var6, var2);
            var5 = ImageUtil.method35042(var5, 0.0F, 1.3F, -0.35F);
            return TextureUtil.method32933(var0, var5);
        } catch (IOException var8) {
            throw new IllegalStateException(
                    "Unable to find " + var0 + ". You've probably obfuscated the archive and forgot to transfer the assets or keep package names."
            );
        }
    }

    public static Texture createScaledAndProcessedTexture2(String var0, float var1, int var2) {
        try {
            BufferedImage var5 = ImageIO.read(readInputStream(var0));
            BufferedImage var6 = new BufferedImage((int) (var1 * (float) var5.getWidth(null)), (int) (var1 * (float) var5.getHeight(null)), 2);
            Graphics2D var7 = (Graphics2D) var6.getGraphics();
            var7.scale(var1, var1);
            var7.drawImage(var5, 0, 0, null);
            var7.dispose();
            var5 = ImageUtil.method35032(ImageUtil.method35041(var6, var2), var2);
            var5 = ImageUtil.method35042(var5, 0.0F, 1.1F, 0.0F);
            return TextureUtil.method32933(var0, var5);
        } catch (IOException var8) {
            throw new IllegalStateException(
                    "Unable to find " + var0 + ". You've probably obfuscated the archive and forgot to transfer the assets or keep package names."
            );
        }
    }

    public static String getPanoramaPNG() {
        return "background/panorama5.png";
    }
}
