package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderGUI;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import org.jetbrains.annotations.NotNull;
import org.newdawn.slick.TrueTypeFont;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.List;
import java.util.*;

public class ActiveMods extends Module {
    public int offsetY = 0;
    public int totalHeight;
    public HashMap<Module, Animation> animations = new HashMap<>();
    public TrueTypeFont font = ResourceRegistry.JelloLightFont20;
    private final List<Module> activeModules = new ArrayList<>();

    public ActiveMods() {
        super(ModuleCategory.GUI, "ActiveMods", "Renders active mods");
        this.registerSetting(new ModeSetting("Size", "The font size", 0, "Normal", "Small", "Tiny"));
        this.registerSetting(new BooleanSetting("Animations", "Scale in animation", true));
        this.registerSetting(new BooleanSetting("Sound", "Toggle sound", true));
        this.getSettingMap().get("Size").addObserver(var1 -> this.setFontSize());
        this.setAvailableOnClassic(false);
    }

    @Override
    public void onEnable() {
        this.setFontSize();
    }

    public void setFontSize() {
        switch (getStringSettingValueByName("Size")) {
            case "Normal":
                this.font = ResourceRegistry.JelloLightFont20;
                break;
            case "Small":
                this.font = ResourceRegistry.JelloLightFont18;
                break;
            default:
                this.font = ResourceRegistry.JelloLightFont14;
        }
    }

    @Override
    public void initialize() {
        this.activeModules.clear();

        for (Module module : Client.getInstance().moduleManager.getModuleMap().values()) {
            if (module.getCategory() != ModuleCategory.GUI) {
                this.activeModules.add(module);
                this.animations.put(module, new Animation(150, 150, Animation.Direction.BACKWARDS));

                if (!this.getBooleanValueFromSettingName("Animations")) {
                    continue;
                }
                this.animations.get(module).changeDirection(!module.isEnabled() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
            }
        }

        this.activeModules.sort(new ModuleNameLengthComparator());
    }

    @EventTarget
    public void onGUI(EventRenderGUI event) {
        if (!this.isEnabled()) return;
        if (mc.player != null) {
            if (!event.isRendering) {
                GlStateManager.translatef(0.0F, (float) (-this.totalHeight), 0.0F);
            } else {
                Collection<Score> scores = getScores();
                int offset = 0;

                for (Module module : this.activeModules) {
                    if (module.isEnabled()) {
                        offset++;
                    }
                }

                int y = 23 + offset * (this.font.getHeight() + 1);
                int totalScores = scores.size();

                int windowHeight = Minecraft.getInstance().getMainWindow().getHeight();
                int windowCenterY = windowHeight / 2 - (9 + 5) * (totalScores - 3 + 2);

                if (y <= windowCenterY) {
                    this.totalHeight = 0;
                } else {
                    this.totalHeight = (y - windowCenterY) / 2;
                    GlStateManager.translatef(0.0F, (float) this.totalHeight, 0.0F);
                }
            }
        }
    }

    private static @NotNull Collection<Score> getScores() {
        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(mc.player.getScoreboardName());

        if (playerTeam != null) {
            int colorIndex = playerTeam.getColor().getColorIndex();
            if (colorIndex >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + colorIndex);
            }
        }

        ScoreObjective scoreObjective = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
		return scoreboard.getSortedScores(scoreObjective);
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (mc.player != null) {
            for (Module module : this.animations.keySet()) {
                if (this.getBooleanValueFromSettingName("Animations")) {
                    this.animations.get(module).changeDirection(!module.isEnabled() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
                }
            }

            if (!Minecraft.getInstance().gameSettings.hideGUI) {
                int margin = 10;
                float scale = 1;
                int screenWidth = Minecraft.getInstance().getMainWindow().getWidth();
                int screenHeight = margin - 4;

                if (this.font == ResourceRegistry.JelloLightFont14) {
                    margin -= 3;
                }

                if (Minecraft.getInstance().gameSettings.showDebugInfo) {
                    screenHeight = (int) ((double) (mc.ingameGUI.overlayDebug.debugInfoRight.size() * 9) * mc.getMainWindow().getGuiScaleFactor() + 7.0);
                }

                int color = RenderUtil2.applyAlpha(-1, 0.95F);

                for (Module module : this.activeModules) {
                    float animationScale = 1.0F;
                    float transparency = 1.0F;

                    if (!this.getBooleanValueFromSettingName("Animations")) {
                        if (!module.isEnabled()) {
                            continue;
                        }
                    } else {
                        Animation animation = this.animations.get(module);
                        if (animation.calcPercent() == 0.0F) {
                            continue;
                        }

                        transparency = animation.calcPercent();
                        animationScale = 0.86F + 0.14F * transparency;
                    }

                    String moduleName = module.getFormattedName();
                    GL11.glAlphaFunc(519, 0.0F);
                    GL11.glPushMatrix();

                    int xPos = screenWidth - margin - this.font.getWidth(moduleName) / 2;
                    int yPos = screenHeight + 12;

                    GL11.glTranslatef((float) xPos, (float) yPos, 0.0F);
                    GL11.glScalef(animationScale, animationScale, 1.0F);
                    GL11.glTranslatef((float) (-xPos), (float) (-yPos), 0.0F);

                    float scaleFactor = (float) Math.sqrt(Math.min(1.2F, (float) this.font.getWidth(moduleName) / 63.0F));
                    RenderUtil.drawImage(
                            (float) screenWidth - (float) this.font.getWidth(moduleName) * 1.5F - (float) margin - 20.0F,
                            (float) (screenHeight - 20),
                            (float) this.font.getWidth(moduleName) * 3.0F,
                            this.font.getHeight() + scale + 40,
                            Resources.shadowPNG,
                            RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.36F * transparency * scaleFactor)
                    );
                    RenderUtil.drawString(
                            this.font, (float) (screenWidth - margin - this.font.getWidth(moduleName)), (float) screenHeight, moduleName, transparency != 1.0F ? RenderUtil2.applyAlpha(-1, transparency * 0.95F) : color
                    );
                    GL11.glPopMatrix();
                    screenHeight = (int) ((float) screenHeight + (this.font.getHeight() + scale) * QuadraticEasing.easeInOutQuad(transparency, 0.0F, 1.0F, 1.0F));
                }

                this.offsetY = screenHeight;
            }
        }
    }

    public static class ModuleNameLengthComparator implements Comparator<Module> {
        public int compare(Module module1, Module module2) {
            int length1 = ResourceRegistry.JelloLightFont20.getWidth(module1.getName());
            int length2 = ResourceRegistry.JelloLightFont20.getWidth(module2.getName());
            if (length1 <= length2) {
                return length1 != length2 ? 1 : 0;
            } else {
                return -1;
            }
        }
    }
}
