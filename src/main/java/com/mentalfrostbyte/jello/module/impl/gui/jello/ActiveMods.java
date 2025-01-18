package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.EventRender;
import com.mentalfrostbyte.jello.event.impl.EventRenderGUI;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.gui.base.QuadraticEasing;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.TrueTypeFont;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
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
    public HashMap<Module, Animation> animations = new HashMap<Module, Animation>();
    public TrueTypeFont font = ResourceRegistry.JelloLightFont20;
    private final List<Module> activeModules = new ArrayList<Module>();

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
        String var3 = this.getStringSettingValueByName("Size");
        switch (var3) {
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

        for (Module var4 : Client.getInstance().moduleManager.getModuleMap().values()) {
            if (var4.getAdjustedCategoryBasedOnClientMode() != ModuleCategory.GUI) {
                this.activeModules.add(var4);
                this.animations.put(var4, new Animation(150, 150, Direction.BACKWARDS));
                if (this.getBooleanValueFromSettingName("Animations")) {
                    this.animations.get(var4)
                            .changeDirection(!var4.isEnabled() ? Direction.BACKWARDS : Direction.FORWARDS);
                }
            }
        }

        this.activeModules.sort(new Class3602(this));
    }

    @EventTarget
    public void onGUI(EventRenderGUI event) {
        if (mc.player != null) {
            if (!event.isRendering) {
                GlStateManager.translatef(0.0F, (float) (-this.totalHeight), 0.0F);
            } else {
                Scoreboard scoreboard = mc.world.getScoreboard();
                ScoreObjective scoreobjective = null;
                ScorePlayerTeam var6 = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
                if (var6 != null) {
                    int var7 = var6.getColor().getColorIndex();
                    if (var7 >= 0) {
                        scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + var7);
                    }
                }

                ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective
                        : scoreboard.getObjectiveInDisplaySlot(1);
                Collection<Score> var8 = scoreboard.getSortedScores(scoreobjective1);
                int var9 = 0;

                for (Module var11 : this.activeModules) {
                    if (var11.isEnabled()) {
                        var9++;
                    }
                }

                int var15 = 23 + var9 * (this.font.getHeight() + 1);
                int var16 = var8.size();
                int var12 = MinecraftClient.getInstance().getWindow().getHeight();
                int var13 = var12 / 2 - (9 + 5) * (var16 - 3 + 2);
                if (var15 <= var13) {
                    this.totalHeight = 0;
                } else {
                    this.totalHeight = (var15 - var13) / 2;
                    GlStateManager.translatef(0.0F, (float) this.totalHeight, 0.0F);
                }
            }
        }
    }

    @EventTarget
    public void method16355(EventRender var1) {
        if (mc.player != null) {
            for (Module var5 : this.animations.keySet()) {
                if (this.getBooleanValueFromSettingName("Animations")) {
                    this.animations.get(var5)
                            .changeDirection(!var5.isEnabled() ? Direction.BACKWARDS : Direction.FORWARDS);
                }
            }

            if (!MinecraftClient.getInstance().gameSettings.hideGUI) {
                int var20 = 10;
                float var21 = 1;
                int var6 = MinecraftClient.getInstance().getWindow().getWidth();
                TrueTypeFont var8 = this.font;
                int var7 = var20 - 4;
                if (this.font == ResourceRegistry.JelloLightFont14) {
                    var20 -= 3;
                }

                if (MinecraftClient.getInstance().gameSettings.showDebugInfo) {
                    var7 = (int) ((double) (mc.ingameGUI.overlayDebug.debugInfoRight.size() * 9)
                            * mc.getWindow().getScaleFactor() + 7.0);
                }

                int var11 = ColorUtils.applyAlpha(-1, 0.95F);

                for (Module var13 : this.activeModules) {
                    float var14 = 1.0F;
                    float var15 = 1.0F;
                    if (!this.getBooleanValueFromSettingName("Animations")) {
                        if (!var13.isEnabled()) {
                            continue;
                        }
                    } else {
                        Animation var16 = this.animations.get(var13);
                        if (var16.calcPercent() == 0.0F) {
                            continue;
                        }

                        var15 = var16.calcPercent();
                        var14 = 0.86F + 0.14F * var15;
                    }

                    String var22 = var13.getSuffix();
                    GL11.glAlphaFunc(519, 0.0F);
                    GL11.glPushMatrix();
                    int var17 = var6 - var20 - var8.getWidth(var22) / 2;
                    int var18 = var7 + 12;
                    GL11.glTranslatef((float) var17, (float) var18, 0.0F);
                    GL11.glScalef(var14, var14, 1.0F);
                    GL11.glTranslatef((float) (-var17), (float) (-var18), 0.0F);
                    float var19 = (float) Math.sqrt(Math.min(1.2F, (float) var8.getWidth(var22) / 63.0F));
                    RenderUtil.drawImage(
                            (float) var6 - (float) var8.getWidth(var22) * 1.5F - (float) var20 - 20.0F,
                            (float) (var7 - 20),
                            (float) var8.getWidth(var22) * 3.0F,
                            var8.getHeight() + var21 + 40,
                            Resources.shadowPNG,
                            ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.36F * var15 * var19));
                    RenderUtil.drawString(
                            var8, (float) (var6 - var20 - var8.getWidth(var22)), (float) var7, var22,
                            var15 != 1.0F ? ColorUtils.applyAlpha(-1, var15 * 0.95F) : var11);
                    GL11.glPopMatrix();
                    var7 = (int) ((float) var7 + (float) (var8.getHeight() + var21)
                            * QuadraticEasing.easeInOutQuad(var15, 0.0F, 1.0F, 1.0F));
                }

                this.offsetY = var7;
            }
        }
    }

    public static class Class3602 implements Comparator<Module> {
        public final ActiveMods field19563;

        public Class3602(ActiveMods var1) {
            this.field19563 = var1;
        }

        public int compare(Module var1, Module var2) {
            int var5 = ResourceRegistry.JelloLightFont20.getWidth(var1.getName());
            int var6 = ResourceRegistry.JelloLightFont20.getWidth(var2.getName());
            if (var5 <= var6) {
                return var5 != var6 ? 1 : 0;
            } else {
                return -1;
            }
        }
    }
}
