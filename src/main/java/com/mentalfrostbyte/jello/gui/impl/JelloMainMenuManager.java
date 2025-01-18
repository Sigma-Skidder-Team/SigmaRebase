package com.mentalfrostbyte.jello.gui.impl;

import club.minnced.discord.rpc.DiscordRPC;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.*;
import com.mentalfrostbyte.jello.gui.unmapped.AlertPanel;
import com.mentalfrostbyte.jello.gui.unmapped.FloatingBubble;
import com.mentalfrostbyte.jello.managers.NetworkManager;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.opengl.Texture;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Random;

public class JelloMainMenuManager extends Screen {
    public static long currentTime = 0L;
    private int field20966 = 0;
    private int field20967 = 0;
    private boolean field20968 = true;
    public JelloMainMenu mainMenuScreen;
    public ChangelogScreen changelogScreen;
    public RedeemKeyScreen redeemKeyScreen;
    public Animation field20972 = new Animation(200, 200, Direction.BACKWARDS);
    public Animation animation = new Animation(200, 200, Direction.BACKWARDS);
    private final Animation field20974 = new Animation(325, 325);
    private final Animation field20975 = new Animation(800, 800);
    private static Texture background;
    public List<FloatingBubble> bubbles = new ArrayList<>();

    public static String[] goodbyeTitles = new String[] {
            "Goodbye.",
            "See you soon.",
            "Bye!",
            "Au revoir",
            "See you!",
            "Ciao!",
            "Adios",
            "Farewell",
            "See you later!",
            "Have a good day!",
            "See you arround.",
            "See you tomorrow!",
            "Goodbye, friend.",
            "Logging out.",
            "Signing off!",
            "Shutting down.",
            "Was good to see you!"
    };
    public static String[] goodbyeMessages = new String[] {
            "The two hardest things to say in life are hello for the first time and goodbye for the last.",
            "Don’t cry because it’s over, smile because it happened.",
            "It’s time to say goodbye, but I think goodbyes are sad and I’d much rather say hello. Hello to a new adventure.",
            "We’ll meet again, Don’t know where, don’t know when, But I know we’ll meet again, some sunny day.",
            "This is not a goodbye but a 'see you soon'.",
            "You are my hardest goodbye.",
            "Goodbyes are not forever, are not the end; it simply means I’ll miss you until we meet again.",
            "Good friends never say goodbye. They simply say \"See you soon\".",
            "Every goodbye always makes the next hello closer.",
            "Where's the good in goodbye?",
            "And I'm sorry, so sorry. But, I have to say goodbye."
    };
    public static String currentTitle;
    public static String currentMessage;
    public static float field20982;
    public AlertPanel alertPanel;

    public JelloMainMenuManager() {
        super("Main Screen");
        this.method13300(false);
        currentTime = System.nanoTime();
        if (background == null) {
            background = Resources.createScaledAndProcessedTexture2(
                    "com/mentalfrostbyte/gui/resources/background/panorama5.png", 0.075F, 8);
        }

        this.field20974.changeDirection(Direction.BACKWARDS);
        this.field20975.changeDirection(Direction.BACKWARDS);
        int var3 = MinecraftClient.getInstance().getWindow().getWidth()
                * MinecraftClient.getInstance().getWindow().getHeight() / 14000;
        Random var4 = new Random();

        for (int var5 = 0; var5 < var3; var5++) {
            int var6 = var4.nextInt(MinecraftClient.getInstance().getWindow().getWidth());
            int var7 = var4.nextInt(MinecraftClient.getInstance().getWindow().getHeight());
            int var8 = 7 + var4.nextInt(5);
            int var9 = (1 + var4.nextInt(4)) * (!var4.nextBoolean() ? 1 : -1);
            int var10 = 1 + var4.nextInt(2);
            this.bubbles.add(new FloatingBubble(this, Integer.toString(var5), var6, var7, var8, var9, var10));
        }

        this.addToList(this.mainMenuScreen = new JelloMainMenu(this, "main", 0, 0, this.widthA, this.heightA));
        this.addToList(this.changelogScreen = new ChangelogScreen(this, "changelog", 0, 0, this.widthA, this.heightA));
        this.addToList(this.redeemKeyScreen = new RedeemKeyScreen(this, "redeem", 0, 0, this.widthA, this.heightA));
        this.changelogScreen.method13296(false);
        this.changelogScreen.method13294(true);
        this.redeemKeyScreen.method13296(false);
        this.redeemKeyScreen.method13294(true);
    }

    public void goOut() {
        this.field20972.changeDirection(Direction.BACKWARDS);
        this.changelogScreen.method13296(false);
        this.redeemKeyScreen.method13296(false);
        this.redeemKeyScreen.method13292(false);
        this.redeemKeyScreen.method13294(true);
    }

    public void method13341() {
        this.field20972.changeDirection(Direction.FORWARDS);
        this.animation.changeDirection(Direction.FORWARDS);
    }

    public void animateIn() {
        this.field20972.changeDirection(Direction.FORWARDS);
        this.changelogScreen.method13296(true);
    }

    public void animateNext() {
        this.field20972.changeDirection(Direction.FORWARDS);
        this.redeemKeyScreen.method13296(true);
        this.redeemKeyScreen.method13292(true);
        this.redeemKeyScreen.method13294(false);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        for (CustomGuiScreen var6 : this.bubbles) {
            var6.updatePanelDimensions(newHeight, newWidth);
        }

        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float deltaTime) {
        float transitionProgress = MathHelper.calculateTransition(this.field20972.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.field20972.getDirection() == Direction.BACKWARDS) {
            transitionProgress = MathHelper.calculateBackwardTransition(this.field20972.calcPercent(), 0.0F, 1.0F,
                    1.0F);
        }

        float scaleOffset = 0.07F * transitionProgress;
        this.mainMenuScreen.method13279(1.0F - scaleOffset, 1.0F - scaleOffset);
        this.mainMenuScreen.method13296(this.field20972.calcPercent() == 0.0F);
        long elapsedTime = System.nanoTime() - currentTime;
        field20982 = Math.min(10.0F, Math.max(0.0F, (float) elapsedTime / 1.810361E7F / 2.0F));
        currentTime = System.nanoTime();
        int offsetY = -this.getHeightO();
        float offsetX = (float) this.getWidthO() / (float) this.getWidthA() * -114.0F;
        if (this.field20968) {
            this.field20966 = (int) offsetX;
            this.field20967 = offsetY;
            this.field20968 = false;
        }

        float deltaX = offsetX - (float) this.field20966;
        float deltaY = (float) (offsetY - this.field20967);
        if (MinecraftClient.getInstance().loadingGui != null) {
            if (offsetX != (float) this.field20966) {
                this.field20966 = (int) ((float) this.field20966 + deltaX * field20982);
            }

            if (offsetY != this.field20967) {
                this.field20967 = (int) ((float) this.field20967 + deltaY * field20982);
            }
        } else {
            this.field20974.changeDirection(Direction.FORWARDS);
            this.field20975.changeDirection(Direction.FORWARDS);
            float parallaxFactor = 0.5F - (float) this.field20967
                    / (float) MinecraftClient.getInstance().getWindow().getWidth() * -1.0F;
            float backgroundOpacity = 1.0F - this.field20974.calcPercent();
            float foregroundOpacity = 1.0F - this.field20975.calcPercent();

            float screenScale = (float) this.getWidthA() / 1920.0F;
            int backgroundWidth = (int) (600.0F * screenScale);
            int middleWidth = (int) (450.0F * screenScale);
            int foregroundWidth = 0;

            RenderUtil.drawImage(
                    (float) this.field20967 - (float) backgroundWidth * parallaxFactor,
                    (float) this.field20966,
                    (float) (this.getWidthA() * 2 + backgroundWidth),
                    (float) (this.getHeightA() + 114),
                    Resources.backgroundPNG);
            RenderUtil.drawImage(
                    (float) this.field20967 - (float) middleWidth * parallaxFactor,
                    (float) this.field20966,
                    (float) (this.getWidthA() * 2 + middleWidth),
                    (float) (this.getHeightA() + 114),
                    Resources.middlePNG);

            for (CustomGuiScreen bubble : this.bubbles) {
                GL11.glPushMatrix();
                bubble.draw(deltaTime);
                GL11.glPopMatrix();
            }

            RenderUtil.drawImage(
                    (float) this.field20967 - (float) foregroundWidth * parallaxFactor,
                    (float) this.field20966,
                    (float) (this.getWidthA() * 2 + foregroundWidth),
                    (float) (this.getHeightA() + 114),
                    Resources.foregroundPNG);

            RenderUtil.drawImage(
                    (float) this.field20967,
                    (float) (this.field20966 - 50),
                    (float) (this.getWidthA() * 2),
                    (float) (this.getHeightA() + 200),
                    background,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), transitionProgress),
                    false);

            RenderUtil.drawRoundedRect2(
                    0.0F, 0.0F, (float) this.getWidthA(), (float) this.getHeightA(),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), transitionProgress * 0.3F));

            for (CustomGuiScreen object : this.getChildren()) {
                if (object.isVisible()) {
                    GL11.glPushMatrix();
                    if (object instanceof ChangelogScreen) {
                        if (transitionProgress > 0.0F) {
                            object.draw(deltaTime);
                        }
                    } else {
                        object.draw(deltaTime * (1.0F - transitionProgress));
                    }

                    GL11.glPopMatrix();
                }
            }

            if (foregroundOpacity > 0.0F && Client.getInstance().method19930()) {
                CustomLoadingScreen.xd(backgroundOpacity, 1.0F);
                Client.getInstance().method19931(false);
            }

            field20982 *= 0.7F;
            field20982 = Math.min(field20982, 1.0F);
            if (!this.field20968 && (foregroundOpacity == 0.0F || this.field20966 != 0 || this.field20967 != 0)) {
                if (offsetX != (float) this.field20966) {
                    this.field20966 = (int) ((float) this.field20966 + deltaX * field20982);
                }

                if (offsetY != this.field20967) {
                    this.field20967 = (int) ((float) this.field20967 + deltaY * field20982);
                }
            }

            if (this.animation.getDirection() == Direction.FORWARDS) {
                RenderUtil.drawString(
                        ResourceRegistry.JelloMediumFont50,
                        (float) (this.widthA / 2),
                        (float) (this.heightA / 2 - 30),
                        currentTitle,
                        ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.animation.calcPercent()),
                        Class2218.field14492,
                        Class2218.field14492);
                RenderUtil.drawString(
                        ResourceRegistry.JelloLightFont18,
                        (float) (this.widthA / 2),
                        (float) (this.heightA / 2 + 30),
                        "\"" + currentMessage + "\"",
                        ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                                this.animation.calcPercent() * 0.5F),
                        Class2218.field14492,
                        Class2218.field14492);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) { // escape key
            this.goOut();
        }
    }

    public void logout() {
        if (this.alertPanel == null) {
            this.runThisOnDimensionUpdate(() -> {
                ArrayList<MiniAlert> alert = new ArrayList<>();
                alert.add(new MiniAlert(AlertType.HEADER, "Logout", 45));
                alert.add(new MiniAlert(AlertType.FIRST_LINE, "Are you sure?", 35));
                alert.add(new MiniAlert(AlertType.BUTTON, "Yes", 55));
                this.method13233(this.alertPanel = new AlertPanel(this, "music", true, "Dependencies.",
                        alert.toArray(new MiniAlert[0])));
                this.alertPanel.method13604(var1 -> new Thread(() -> {
                    this.runThisOnDimensionUpdate(() -> {
                        this.method13236(this.alertPanel);
                        this.alertPanel = null;

                        NetworkManager.premium = false;
                        Client.getInstance().getDRPC().smallImageKey = null;
                        Client.getInstance().getDRPC().smallImageText = null;
                        DiscordRPC.INSTANCE.Discord_UpdatePresence(Client.getInstance().getDRPC());
                    });
                }).start());
                this.alertPanel.method13603(true);
            });
        }
    }

    static {
        Locale locale = Locale.getDefault(Category.DISPLAY);
        if (locale == Locale.FRANCE || locale == Locale.FRENCH) {
            goodbyeMessages = ArrayUtils.addAll(
                    goodbyeMessages,
                    "Mon salut jamais dans la fuite, avant d'm'éteindre, faut m'débrancher",
                    "Prêt à partir pour mon honneur");
        }

        currentTitle = goodbyeTitles[new Random().nextInt(goodbyeTitles.length)];
        currentMessage = goodbyeMessages[new Random().nextInt(goodbyeMessages.length)];
    }
}
