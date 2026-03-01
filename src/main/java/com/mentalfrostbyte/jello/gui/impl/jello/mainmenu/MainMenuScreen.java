package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.impl.FloatingBubble;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.combined.impl.LoadingScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Alert;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.system.math.MathHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import org.newdawn.slick.opengl.Texture;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Random;

public class MainMenuScreen extends Screen {
    public static long currentTime = 0L;
    private int field20966 = 0;
    private int field20967 = 0;
    private boolean field20968 = true;
    public JelloMainMenu mainMenuScreen;
    public ChangelogScreen changelogScreen;
    public Animation field20972 = new Animation(200, 200, Animation.Direction.BACKWARDS);
    public Animation animation = new Animation(200, 200, Animation.Direction.BACKWARDS);
    private final Animation field20974 = new Animation(325, 325);
    private final Animation field20975 = new Animation(800, 800);
    private static Texture background;
    public List<FloatingBubble> bubbles = new ArrayList<>();

    public static String[] goodbyeTitles = new String[]{
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
    public static String[] goodbyeMessages = new String[]{
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
    public Alert alert;

    public MainMenuScreen() {
        super("Main Screen");
        this.setListening(false);
        currentTime = System.nanoTime();
        if (background == null) {
            background = Resources.createScaledAndProcessedTexture2("com/mentalfrostbyte/gui/resources/background/panorama5.png", 0.075F, 8);
        }

        this.field20974.changeDirection(Animation.Direction.BACKWARDS);
        this.field20975.changeDirection(Animation.Direction.BACKWARDS);
        int var3 = Minecraft.getInstance().getMainWindow().getWidth() * Minecraft.getInstance().getMainWindow().getHeight() / 14000;
        Random var4 = new Random();

        for (int var5 = 0; var5 < var3; var5++) {
            int var6 = var4.nextInt(Minecraft.getInstance().getMainWindow().getWidth());
            int var7 = var4.nextInt(Minecraft.getInstance().getMainWindow().getHeight());
            int var8 = 7 + var4.nextInt(5);
            int var9 = (1 + var4.nextInt(4)) * (!var4.nextBoolean() ? 1 : -1);
            int var10 = 1 + var4.nextInt(2);
            this.bubbles.add(new FloatingBubble(this, Integer.toString(var5), var6, var7, var8, var9, var10));
        }

        this.addToList(this.mainMenuScreen = new JelloMainMenu(this, "main", 0, 0, this.widthA, this.heightA));
        this.addToList(this.changelogScreen = new ChangelogScreen(this, "changelog", 0, 0, this.widthA, this.heightA));
        this.changelogScreen.setHovered(false);
        this.changelogScreen.method13294(true);
    }

    public void goOut() {
        this.field20972.changeDirection(Animation.Direction.BACKWARDS);
        this.changelogScreen.setHovered(false);
    }

    public void method13341() {
        this.field20972.changeDirection(Animation.Direction.FORWARDS);
        this.animation.changeDirection(Animation.Direction.FORWARDS);
    }

    public void animateIn() {
        this.field20972.changeDirection(Animation.Direction.FORWARDS);
        this.changelogScreen.setHovered(true);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        for (CustomGuiScreen var6 : this.bubbles) {
            var6.updatePanelDimensions(newHeight, newWidth);
        }

        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float partialTicks) {
        float transitionProgress = MathHelper.easeOutCubic(this.field20972.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.field20972.getDirection() == Animation.Direction.BACKWARDS) {
            transitionProgress = MathHelper.easeInCubic(this.field20972.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        float scaleOffset = 0.07F * transitionProgress;
        this.mainMenuScreen.method13279(1.0F - scaleOffset, 1.0F - scaleOffset);
        this.mainMenuScreen.setHovered(this.field20972.calcPercent() == 0.0F);
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
        if (Minecraft.getInstance().loadingGui != null) {
            if (offsetX != (float) this.field20966) {
                this.field20966 = (int) ((float) this.field20966 + deltaX * field20982);
            }

            if (offsetY != this.field20967) {
                this.field20967 = (int) ((float) this.field20967 + deltaY * field20982);
            }
        } else {
            this.field20974.changeDirection(Animation.Direction.FORWARDS);
            this.field20975.changeDirection(Animation.Direction.FORWARDS);
            float parallaxFactor = 0.5F - (float) this.field20967 / (float) Minecraft.getInstance().getMainWindow().getWidth() * -1.0F;
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
                    Resources.backgroundPNG
            );
            RenderUtil.drawImage(
                    (float) this.field20967 - (float) middleWidth * parallaxFactor,
                    (float) this.field20966,
                    (float) (this.getWidthA() * 2 + middleWidth),
                    (float) (this.getHeightA() + 114),
                    Resources.middlePNG
            );

            for (CustomGuiScreen bubble : this.bubbles) {
                GL11.glPushMatrix();
                bubble.draw(partialTicks);
                GL11.glPopMatrix();
            }

            RenderUtil.drawImage(
                    (float) this.field20967 - (float) foregroundWidth * parallaxFactor,
                    (float) this.field20966,
                    (float) (this.getWidthA() * 2 + foregroundWidth),
                    (float) (this.getHeightA() + 114),
                    Resources.foregroundPNG
            );


            RenderUtil.drawImage(
                    (float) this.field20967,
                    (float) (this.field20966 - 50),
                    (float) (this.getWidthA() * 2),
                    (float) (this.getHeightA() + 200),
                    background,
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), transitionProgress),
                    false
            );

            RenderUtil.drawRoundedRect2(
                    0.0F, 0.0F, (float) this.getWidthA(), (float) this.getHeightA(), RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), transitionProgress * 0.3F)
            );


            for (CustomGuiScreen object : this.getChildren()) {
                if (object.isSelfVisible()) {
                    GL11.glPushMatrix();
                    if (object instanceof ChangelogScreen) {
                        if (transitionProgress > 0.0F) {
                            object.draw(partialTicks);
                        }
                    } else {
                        object.draw(partialTicks * (1.0F - transitionProgress));
                    }

                    GL11.glPopMatrix();
                }
            }


            if (foregroundOpacity > 0.0F && Client.getInstance().loading) {
                LoadingScreen.xd(backgroundOpacity, 1.0F);
                Client.getInstance().loading = false;
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

            if (this.animation.getDirection() == Animation.Direction.FORWARDS) {
                RenderUtil.drawString(
                        ResourceRegistry.JelloMediumFont50,
                        (float) (this.widthA / 2),
                        (float) (this.heightA / 2 - 30),
                        currentTitle,
                        RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.animation.calcPercent()),
                        FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2,
                        FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2
                );
                RenderUtil.drawString(
                        ResourceRegistry.JelloLightFont18,
                        (float) (this.widthA / 2),
                        (float) (this.heightA / 2 + 30),
                        "\"" + currentMessage + "\"",
                        RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.animation.calcPercent() * 0.5F),
                        FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2,
                        FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2
                );
            }
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) { //escape key
            this.goOut();
        }
    }

    static {
        Locale locale = Locale.getDefault(Category.DISPLAY);
        if (locale == Locale.FRANCE || locale == Locale.FRENCH) {
            goodbyeMessages = ArrayUtils.addAll(
                    goodbyeMessages,
                    "Mon salut jamais dans la fuite, avant d'm'éteindre, faut m'débrancher", "Prêt à partir pour mon honneur");
        }

        currentTitle = goodbyeTitles[new Random().nextInt(goodbyeTitles.length)];
        currentMessage = goodbyeMessages[new Random().nextInt(goodbyeMessages.length)];
    }
}
