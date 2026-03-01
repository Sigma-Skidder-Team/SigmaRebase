package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui;

import com.google.gson.JsonObject;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.alerts.AlertComponent;
import com.mentalfrostbyte.jello.gui.base.alerts.ComponentType;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Alert;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.image.types.SmallImage;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.configs.ConfigScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.groups.PanelGroup;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.groups.SettingGroup;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.holders.ClickGuiHolder;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.musicplayer.MusicPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.gui.jello.BrainFreeze;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.system.math.MathHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.system.math.smoothing.EasingFunctions;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {
    public BlurOverlay blurOverlay;
    private static final Minecraft mc = Minecraft.getInstance();
    private static Animation animationProgress;
    private static boolean animationStarted;
    private static boolean animationCompleted;
    private final Map<ModuleCategory, PanelGroup> categoryPanels = new HashMap<>();
    public MusicPlayer musicPlayer;
    public BrainFreezeOverlay brainFreeze;
    public ConfigScreen configButton;
    public SettingGroup settingGroup;
    public Alert dependenciesAlert;
    public PanelGroup panelGroup = null;

    public ClickGuiScreen() {
        super("JelloScreen");
        animationCompleted = animationCompleted | !animationStarted;
        int x = 30;
        int y = 30;
        this.addToList(this.brainFreeze = new BrainFreezeOverlay(this, "brainFreeze"));

        for (Module module : Client.getInstance().moduleManager.getModuleMap().values()) {
            if (!this.categoryPanels.containsKey(module.getCategoryBasedOnMode())) {
                PanelGroup clickGUIPanels = new PanelGroup(this, module.getCategoryBasedOnMode().name(), x, y, module.getCategoryBasedOnMode());
                this.categoryPanels.put(module.getCategoryBasedOnMode(), clickGUIPanels);
                this.addToList(clickGUIPanels);

                x += clickGUIPanels.getWidthA() + 10;
                if (this.categoryPanels.size() == 4) {
                    x = 30;
                    y += clickGUIPanels.getHeightA() - 20;
                }

                clickGUIPanels.method13507(var2 -> this.runThisOnDimensionUpdate(() -> {
                    this.addToList(this.settingGroup = new SettingGroup(this, "settings", 0, 0, this.widthA, this.heightA, var2));
                    this.settingGroup.setReAddChildren(true);
                }));
            }
        }

        this.addToList(this.musicPlayer = new MusicPlayer(this, "musicPlayer"));
        this.musicPlayer.method13215(true);
        SmallImage moreButton;
        this.addToList(moreButton = new SmallImage(this, "more", this.getWidthA() - 69, this.getHeightA() - 55, 55, 41, Resources.optionsPNG1));

        moreButton.getTextColor().setPrimaryColor(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F));
        moreButton.setListening(false);

        this.musicPlayer.setSelfVisible(true);
        moreButton.onClick((var1, var2) -> this.runThisOnDimensionUpdate(() -> {
            if (this.configButton != null && this.hasChild(this.configButton)) {
                this.method13234(this.configButton);
            } else {
                this.addToList(this.configButton = new ConfigScreen(this, "morepopover", this.getWidthA() - 14, this.getHeightA() - 14));
                this.configButton.setReAddChildren(true);
            }
        }));

        animationProgress = new Animation(450, 125);
        this.blurOverlay = new BlurOverlay(this, this, "overlay");
        RenderUtil2.blur();
        RenderUtil2.setShaderParamsRounded(animationProgress.calcPercent());
    }

    public boolean hasJelloMusicRequirements() {
        if (Client.getInstance().musicManager.hasPython() && Client.getInstance().musicManager.hasVCRedist()) {
            return true;
        } else if (this.dependenciesAlert == null) {
            this.runThisOnDimensionUpdate(() -> {
                List<AlertComponent> alerts = new ArrayList<>();
                alerts.add(new AlertComponent(ComponentType.HEADER, "Music", 40));
                alerts.add(new AlertComponent(ComponentType.FIRST_LINE, "Jello Music requires:", 20));

                if (!Client.getInstance().musicManager.hasPython()) {
                    alerts.add(new AlertComponent(ComponentType.FIRST_LINE, "- Python 3.12.5", 30));
                }

                if (!Client.getInstance().musicManager.hasVCRedist()) {
                    alerts.add(new AlertComponent(ComponentType.FIRST_LINE, "- Visual C++ 2010 x86", 30));
                }

                alerts.add(new AlertComponent(ComponentType.BUTTON, "Download", 55));
                this.showAlert(this.dependenciesAlert = new Alert(this, "music", true, "Dependencies.", alerts.toArray(new AlertComponent[0])));

                this.dependenciesAlert.onPress(thread -> {
                    if (!Client.getInstance().musicManager.hasPython()) {
                        Util.getOSType().openLink("https://www.python.org/ftp/python/3.12.5/");
                    }

                    if (!Client.getInstance().musicManager.hasVCRedist()) {
                        Util.getOSType().openLink("https://www.microsoft.com/en-us/download/details.aspx?id=26999");
                    }
                });

                this.dependenciesAlert.method13604(thread -> new Thread(() -> {
                    this.runThisOnDimensionUpdate(() -> {
                        this.removeChildren(this.dependenciesAlert);
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
        for (PanelGroup panel : this.categoryPanels.values()) {
            panel.method13504();
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        this.musicPlayer.setSelfVisible(this.musicPlayer.getWidthA() < this.getWidthA() && this.musicPlayer.getHeightA() < this.getHeightA());
        super.updatePanelDimensions(newHeight, newWidth);
        RenderUtil2.setShaderParamsRounded(Math.min(1.0F, animationProgress.calcPercent() * 4.0F));
        this.brainFreeze.setSelfVisible(Client.getInstance().moduleManager.getModuleByClass(BrainFreeze.class).isEnabled());
        if (this.configButton != null) {
            int newHeightValue = newHeight - this.configButton.method13271();
            int newWidthValue = newWidth - this.configButton.method13272();
            boolean conditionMet = newHeightValue >= -10 && newWidthValue >= -10;
            if (!conditionMet) {
                this.configButton.method13613();
            }
        }

        if (this.configButton != null && this.configButton.method13614()) {
            this.removeChildren(this.configButton);
            this.configButton = null;
        }

        if (animationProgress.getDirection() == Animation.Direction.BACKWARDS && this.settingGroup != null && !this.settingGroup.field20671) {
            this.settingGroup.field20671 = true;
        }

        if (this.settingGroup != null && this.settingGroup.field20671 && this.settingGroup.animation1.calcPercent() == 0.0F) {
            this.runThisOnDimensionUpdate(() -> {
                this.removeChildren(this.settingGroup);
                this.settingGroup = null;
            });
        }

        if (animationCompleted) {
            Animation.Direction direction = animationProgress.getDirection();
            animationProgress.changeDirection(!animationStarted ? Animation.Direction.FORWARDS : Animation.Direction.BACKWARDS);

            if (animationProgress.calcPercent() <= 0.0F && animationStarted) {
                animationStarted = false;
                this.handleAnimationCompletion(animationStarted);
            } else if (animationProgress.calcPercent() >= 1.0F && animationProgress.getDirection() == direction) {
                animationStarted = true;
                this.handleAnimationCompletion(animationStarted);
            }
        }

        if (animationCompleted && animationStarted) {
            RenderUtil2.resetShaders();
        }
    }

    @Override
    public int getFPS() {
        return Minecraft.getFps();
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        RenderUtil2.resetShaders();
        this.method13234(this.blurOverlay);
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JsonObject config) {
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
        int keyBindForClickGui = Client.getInstance().moduleManager.getKeyManager().getKeybindFor(ClickGuiHolder.class);
        if (keyCode == 256 || keyCode == keyBindForClickGui && this.settingGroup == null && !this.method13227()) {
            if (animationCompleted) {
                animationStarted = !animationStarted;
            }

            animationCompleted = true;
        }
    }

    public float method13317(float var1, float var2) {
        return animationProgress.getDirection() != Animation.Direction.BACKWARDS
                ? (float) (Math.pow(2.0, -10.0F * var1) * Math.sin((double) (var1 - var2 / 4.0F) * (Math.PI * 2) / (double) var2) + 1.0)
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
                RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha)
        );
        float fadeAmount = 1.0F;
        if (this.settingGroup != null) {
            float var8 = EasingFunctions.easeOutBack(this.settingGroup.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
            if (this.settingGroup.animation.getDirection() == Animation.Direction.BACKWARDS) {
                var8 = MathHelper.easeInCubic(this.settingGroup.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
            }

            fadeAmount -= this.settingGroup.animation.calcPercent() * 0.1F;
            alphaFactor *= 1.0F + var8 * 0.2F;
        }

        if (Client.getInstance().moduleManager.getConfigurationManager().getCurrentConfig() != null && !Client.getInstance().notificationManager.isRenderingNotification()) {
            String configName = Client.getInstance().moduleManager.getConfigurationManager().getCurrentConfig().profileName;
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont20,
                    (float) (this.widthA - ResourceRegistry.JelloLightFont20.getWidth(configName) - 80),
                    (float) (this.heightA - 47),
                    configName,
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * Math.max(0.0F, Math.min(1.0F, alphaFactor)))
            );
        }

        for (CustomGuiScreen child : this.getChildren()) {
            float x = (float) (child.getXA() + child.getWidthA() / 2 - mc.getMainWindow().getWidth() / 2) * (1.0F - alphaFactor) * 0.5F;
            float y = (float) (child.getYA() + child.getHeightA() / 2 - mc.getMainWindow().getHeight() / 2) * (1.0F - alphaFactor) * 0.5F;
            child.draw((int) x, (int) y);
            child.method13279(1.5F - alphaFactor * 0.5F, 1.5F - alphaFactor * 0.5F);
        }

        super.draw(partialTicks * Math.min(1.0F, alphaFactor) * fadeAmount);
        if (this.panelGroup != null) {
            this.panelGroup.setReAddChildren(false);
        }

        this.blurOverlay.setReAddChildren(false);
        this.method13234(this.blurOverlay);
    }
}
