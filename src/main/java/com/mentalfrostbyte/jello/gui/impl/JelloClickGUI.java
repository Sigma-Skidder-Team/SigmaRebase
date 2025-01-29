package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.*;
import com.mentalfrostbyte.jello.gui.unmapped.*;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.gui.jello.BrainFreeze;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import totalcross.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JelloClickGUI extends Screen {
    public ClickGUIBlurOverlay blurOverlay;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Animation animationProgress;
    private static boolean animationStarted;
    private static boolean animationCompleted;
    private final Map<ModuleCategory, JelloClickGUIPanels> categoryPanels = new HashMap<>();
    public MusicPlayer musicPlayer;
    public BrainFreezeGui brainFreeze;
    public ConfigButtonOnClickGui configButton;
    public ModuleSettingUI moduleSettingUI;
    public AlertPanel dependenciesAlert;
    public JelloClickGUIPanels jelloClickGUIPanels = null;

    public JelloClickGUI() {
        super("JelloScreen");
        animationCompleted = animationCompleted | !animationStarted;
        int x = 30;
        int y = 30;
        this.addToList(this.brainFreeze = new BrainFreezeGui(this, "brainFreeze"));

        for (Module module : Client.getInstance().moduleManager.getModuleMap().values()) {
            if (!this.categoryPanels.containsKey(module.getAdjustedCategoryBasedOnClientMode())) {
                JelloClickGUIPanels clickGUIPanels = new JelloClickGUIPanels(this,
                        module.getAdjustedCategoryBasedOnClientMode().name(), x, y,
                        module.getAdjustedCategoryBasedOnClientMode());
                this.categoryPanels.put(module.getAdjustedCategoryBasedOnClientMode(), clickGUIPanels);
                this.addToList(clickGUIPanels);

                x += clickGUIPanels.getWidthA() + 10;
                if (this.categoryPanels.size() == 4) {
                    x = 30;
                    y += clickGUIPanels.getHeightA() - 20;
                }

                clickGUIPanels.method13507(var2 -> this.runThisOnDimensionUpdate(() -> {
                    this.addToList(this.moduleSettingUI = new ModuleSettingUI(this, "settings", 0, 0, this.widthA,
                            this.heightA, var2));
                    this.moduleSettingUI.method13292(true);
                }));
            }
        }

        this.addToList(this.musicPlayer = new MusicPlayer(this, "musicPlayer"));
        this.musicPlayer.method13215(true);
        PNGIconButton moreButton;
        this.addToList(moreButton = new PNGIconButton(this, "more", this.getWidthA() - 69, this.getHeightA() - 55, 55,
                41, Resources.optionsPNG1));

        moreButton.getTextColor().method19406(ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F));
        moreButton.setListening(false);

        this.musicPlayer.setEnabled(true);
        moreButton.doThis((var1, var2) -> this.runThisOnDimensionUpdate(() -> {
            if (this.configButton != null && this.hasChild(this.configButton)) {
                this.method13234(this.configButton);
            } else {
                this.addToList(this.configButton = new ConfigButtonOnClickGui(this, "morepopover",
                        this.getWidthA() - 14, this.getHeightA() - 14));
                this.configButton.method13292(true);
            }
        }));

        animationProgress = new Animation(450, 125);
        this.blurOverlay = new ClickGUIBlurOverlay(this, this, "overlay");
        ColorUtils.blur();
        ColorUtils.setShaderParamsRounded(animationProgress.calcPercent());
    }

    public boolean hasJelloMusicRequirements() {
        if (Client.getInstance().musicManager.hasPython() && Client.getInstance().musicManager.hasVCRedist()) {
            return true;
        } else if (this.dependenciesAlert == null) {
            this.runThisOnDimensionUpdate(() -> {
                List<MiniAlert> alerts = new ArrayList<>();
                alerts.add(new MiniAlert(AlertType.HEADER, "Music", 40));
                alerts.add(new MiniAlert(AlertType.FIRST_LINE, "Jello Music requires:", 20));

                if (!Client.getInstance().musicManager.hasPython()) {
                    alerts.add(new MiniAlert(AlertType.FIRST_LINE, "- Python 3.12.5", 30));
                }

                if (!Client.getInstance().musicManager.hasVCRedist()) {
                    alerts.add(new MiniAlert(AlertType.FIRST_LINE, "- Visual C++ 2010 x86", 30));
                }

                alerts.add(new MiniAlert(AlertType.BUTTON, "Download", 55));
                this.showAlert(this.dependenciesAlert = new AlertPanel(this, "music", true, "Dependencies.",
                        alerts.toArray(new MiniAlert[0])));

                this.dependenciesAlert.onPress(thread -> {
                    if (!Client.getInstance().musicManager.hasPython()) {
                        Util.getOSType().openLink("https://www.python.org/ftp/python/3.12.5/python-3.12.5-macos11.pkg");
                    }

                    if (!Client.getInstance().musicManager.hasVCRedist()) {
                        Util.getOSType().openLink("https://www.microsoft.com/en-us/download/details.aspx?id=26999");
                    }
                });

                this.dependenciesAlert.method13604(thread -> new Thread(() -> {
                    this.runThisOnDimensionUpdate(() -> {
                        this.method13236(this.dependenciesAlert);
                        this.dependenciesAlert = null;
                    });
                }).start());

                this.dependenciesAlert.method13603(true);
            });
            return true;
        } else {
            return true;
        }
    }

    public void method13315() {
        for (JelloClickGUIPanels panel : this.categoryPanels.values()) {
            panel.method13504();
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        this.musicPlayer.setEnabled(
                this.musicPlayer.getWidthA() < this.getWidthA() && this.musicPlayer.getHeightA() < this.getHeightA());
        super.updatePanelDimensions(newHeight, newWidth);
        ColorUtils.setShaderParamsRounded(Math.min(1.0F, animationProgress.calcPercent() * 4.0F));
        this.brainFreeze.setEnabled(Client.getInstance().moduleManager.getModuleByClass(BrainFreeze.class).isEnabled());
        if (this.configButton != null) {
            int newHeightValue = newHeight - this.configButton.method13271();
            int newWidthValue = newWidth - this.configButton.method13272();
            boolean conditionMet = newHeightValue >= -10 && newWidthValue >= -10;
            if (!conditionMet) {
                this.configButton.method13613();
            }
        }

        if (this.configButton != null && this.configButton.method13614()) {
            this.method13236(this.configButton);
            this.configButton = null;
        }

        if (animationProgress.getDirection() == Direction.BACKWARDS && this.moduleSettingUI != null
                && !this.moduleSettingUI.field20671) {
            this.moduleSettingUI.field20671 = true;
        }

        if (this.moduleSettingUI != null && this.moduleSettingUI.field20671
                && this.moduleSettingUI.animation1.calcPercent() == 0.0F) {
            this.runThisOnDimensionUpdate(() -> {
                this.method13236(this.moduleSettingUI);
                this.moduleSettingUI = null;
            });
        }

        if (animationCompleted) {
            Direction direction = animationProgress.getDirection();
            animationProgress.changeDirection(!animationStarted ? Direction.FORWARDS : Direction.BACKWARDS);

            if (animationProgress.calcPercent() <= 0.0F && animationStarted) {
                animationStarted = false;
                this.handleAnimationCompletion(animationStarted);
            } else if (animationProgress.calcPercent() >= 1.0F && animationProgress.getDirection() == direction) {
                animationStarted = true;
                this.handleAnimationCompletion(animationStarted);
            }
        }

        if (animationCompleted && animationStarted) {
            ColorUtils.resetShaders();
        }
    }

    @Override
    public int getFPS() {
        return MinecraftClient.getFps();
    }

    @Override
    public JSONObject toConfigWithExtra(JSONObject config) {
        ColorUtils.resetShaders();
        this.method13234(this.blurOverlay);
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JSONObject config) {
        super.loadConfig(config);
    }

    private void handleAnimationCompletion(boolean started) {
        animationCompleted = false;
        if (!started) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton <= 1) {
            return super.onClick(mouseX, mouseY, mouseButton);
        } else {
            this.keyPressed(mouseButton);
            return false;
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        int keyBindForClickGui = Client.getInstance().moduleManager.getMacOSTouchBar().getKeybindFor(ClickGui.class);
        if (keyCode == 256 || keyCode == keyBindForClickGui && this.moduleSettingUI == null && !this.method13227()) {
            if (animationCompleted) {
                animationStarted = !animationStarted;
            }

            animationCompleted = true;
        }
    }

    public float method13317(float var1, float var2) {
        return animationProgress.getDirection() != Direction.BACKWARDS
                ? (float) (Math.pow(2.0, (double) (-10.0F * var1))
                        * Math.sin((double) (var1 - var2 / 4.0F) * (Math.PI * 2) / (double) var2) + 1.0)
                : QuadraticEasing.easeOutQuad(var1, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        float alphaFactor = animationCompleted && !animationStarted
                ? this.method13317(animationProgress.calcPercent(), 0.8F) * 0.5F + 0.5F
                : (!animationCompleted ? 1.0F : this.method13317(animationProgress.calcPercent(), 1.0F));
        float alpha = 0.2F * partialTicks * alphaFactor;
        RenderUtil.drawRoundedRect(
                (float) this.xA,
                (float) this.yA,
                (float) (this.xA + this.widthA),
                (float) (this.yA + this.heightA),
                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha));
        float fadeAmount = 1.0F;
        if (this.moduleSettingUI != null) {
            float var8 = EasingFunctions.easeOutBack(this.moduleSettingUI.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
            if (this.moduleSettingUI.animation.getDirection() == Direction.BACKWARDS) {
                var8 = MathHelper.calculateBackwardTransition(this.moduleSettingUI.animation.calcPercent(), 0.0F, 1.0F,
                        1.0F);
            }

            fadeAmount -= this.moduleSettingUI.animation.calcPercent() * 0.1F;
            alphaFactor *= 1.0F + var8 * 0.2F;
        }

        if (Client.getInstance().moduleManager.getConfigurationManager().getCurrentConfig() != null
                && !Client.getInstance().notificationManager.isRenderingNotification()) {
            String configName = Client.getInstance().moduleManager.getConfigurationManager().getCurrentConfig().getName;
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont20,
                    (float) (this.widthA - ResourceRegistry.JelloLightFont20.getWidth(configName) - 80),
                    (float) (this.heightA - 47),
                    configName,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                            0.5F * Math.max(0.0F, Math.min(1.0F, alphaFactor))));
        }

        for (CustomGuiScreen child : this.getChildren()) {
            float x = (float) (child.getXA() + child.getWidthA() / 2 - mc.getMainWindow().getWidth() / 2)
                    * (1.0F - alphaFactor) * 0.5F;
            float y = (float) (child.getYA() + child.getHeightA() / 2 - mc.getMainWindow().getHeight() / 2)
                    * (1.0F - alphaFactor) * 0.5F;
            child.draw((int) x, (int) y);
            child.method13279(1.5F - alphaFactor * 0.5F, 1.5F - alphaFactor * 0.5F);
        }

        super.draw(partialTicks * Math.min(1.0F, alphaFactor) * fadeAmount);
        if (this.jelloClickGUIPanels != null) {
            this.jelloClickGUIPanels.method13292(false);
        }

        this.blurOverlay.method13292(false);
        this.method13234(this.blurOverlay);
    }
}
