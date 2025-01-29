package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.SettingType;
import com.mentalfrostbyte.jello.module.settings.impl.InputSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.module.settings.impl.TextBoxSetting;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.TrueTypeFont;

import java.util.HashMap;

public class Class4345 extends MusicTabs {
    private final Module module;
    public TrueTypeFont settingsNameFont = Resources.regular20;
    public Animation anim = new Animation(150, 150);
    public HashMap<Module, CustomGuiScreen> moduleWithSettingGuiMap = new HashMap<>();

    public Class4345(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Module var7) {
        super(var1, var2, var3, var4, var5, var6);
        this.module = var7;
        this.setListening(false);
        this.method13511();
    }

    private int renderModuleSettings(CustomGuiScreen screen, Setting setting, int x, int y, int value) {
        UITextDisplay name = new UITextDisplay(screen, setting.getName() + "lbl", x, y, 0, 0, UITextDisplay.defaultColorHelper, setting.getName(), this.settingsNameFont);
        UITextDisplaySmall description = new UITextDisplaySmall(screen, setting.getName() + "desc", x + 195, y + 4, 330, 18, setting);
        screen.addToList(name);
        screen.addToList(description);
        switch (setting.getSettingType()) {
            case BOOLEAN:
                Class4262 var26 = new Class4262(screen, setting.getName() + "checkbox", x + 135, y + 4, 40, 18);
                var26.method13094((Boolean) setting.getCurrentValue(), false);
                setting.addObserver(var1x -> {
                    if (var26.method13092() != (Boolean) var1x.getCurrentValue()) {
                        var26.method13094((Boolean) var1x.getCurrentValue(), false);
                    }
                });
                var26.onPress(var1x -> setting.setCurrentValue(((Class4262) var1x).method13092()));
                screen.addToList(var26);
                y += 18 + value;
                break;
            case NUMBER:
                NumberSetting var25 = (NumberSetting) setting;
                Class4370 var13 = new Class4370(screen, setting.getName() + "slider", x, y + 31, 240, 4);
                var13.setTypedText(Float.toString((Float) setting.getCurrentValue()));
                name.setTypedText(setting.getName() + ": " + (Float) setting.getCurrentValue());
                var13.method13699(Class4277.method13134(var25.getMin(), var25.getMax(), (Float) var25.getCurrentValue()), false);
                int var14 = var25.getDecimalPlaces();
                var25.addObserver(
                        var5x -> {
                            if (Class4370.method13694(var13.method13697(), var25.getMin(), var25.getMax(), var25.getStep(), var14)
                                    != (Float) var5x.getCurrentValue()) {
                                var13.setTypedText(Float.toString((Float) var5x.getCurrentValue()));
                                var13.method13699(Class4370.method13693(var25.getMin(), var25.getMax(), (Float) var5x.getCurrentValue()), false);
                                name.setTypedText(setting.getName() + ": " + (Float) setting.getCurrentValue());
                            }
                        }
                );
                var13.onPress(var5x -> {
                    float var8x = ((Class4370) var5x).method13697();
                    float var9x = Class4370.method13694(var8x, var25.getMin(), var25.getMax(), var25.getStep(), var14);
                    if (var9x != (Float) setting.getCurrentValue()) {
                        var13.setTypedText(Float.toString(var9x));
                        setting.setCurrentValue(var9x);
                    }

                    name.setTypedText(setting.getName() + ": " + (Float) setting.getCurrentValue());
                });
                screen.addToList(var13);
                y += 54;
                break;
            case INPUT:
                int var16 = 114;
                int var20 = 27;
                SigmaClassicTextBox var24;
                this.addToList(
                        var24 = new SigmaClassicTextBox(
                                screen,
                                setting.getName() + "txt",
                                x,
                                y + 27,
                                var16,
                                var20,
                                UIInput.field20741,
                                (String) setting.getCurrentValue(),
                                setting.getName(),
                                ResourceRegistry.DefaultClientFont
                        )
                );
                var24.setFont(ResourceRegistry.JelloLightFont18);
                var24.method13151(var1x -> setting.setCurrentValue(var1x.getTypedText()));
                setting.addObserver(var2x -> {
                    if (var24.getTypedText() != ((InputSetting) setting).getCurrentValue()) {
                        var24.setTypedText(((InputSetting) setting).getCurrentValue());
                    }
                });
                y += var20 + value;
                break;
            case MODE:
                Class4366 var23 = new Class4366(
                        screen, setting.getName() + "btn", x, y + 27, 80, 20, ((ModeSetting) setting).getAvailableModes(), ((ModeSetting) setting).getModeIndex()
                );
                setting.addObserver(var2x -> {
                    if (var23.method13671() != ((ModeSetting) setting).getModeIndex()) {
                        var23.method13672(((ModeSetting) setting).getModeIndex());
                    }
                });
                var23.onPress(var1x -> ((ModeSetting) setting).setModeByIndex(((Class4366) var1x).method13671()));
                screen.addToList(var23);
                y += 65;
                break;
            case TEXTBOX:
                Class4377 var12 = new Class4377(
                        screen, setting.getName() + "btn", screen.getWidthA() - value, y + 6, 123, 27, ((TextBoxSetting) setting).getOptions(), (Integer) setting.getCurrentValue()
                );
                setting.addObserver(var1x -> {
                    if (var12.method13720() != (Integer) var1x.getCurrentValue()) {
                        var12.method13722((Integer) var1x.getCurrentValue(), false);
                    }
                });
                var12.onPress(var1x -> setting.setCurrentValue(((Class4377) var1x).method13720()));
                var12.setSize((var2x, var3x) -> var2x.setXA(screen.getWidthA() - 123 - value));
                screen.addToList(var12);
                y += 27 + value;
            case SUBOPTION:
            case BOOLEAN2:
            case UNUSED:
        }

        return y - (value - 10);
    }

    private void method13511() {
        UITextDisplay var3 = new UITextDisplay(
                this, "settingsname", 12, 2, this.widthA, 20, UITextDisplay.defaultColorHelper, this.module.getSuffix() + " Settings", this.settingsNameFont
        );
        this.addToList(var3);
        int var6 = 35;

        for (Setting var8 : this.module.getSettingMap().values()) {
            if (var8.getSettingType() != SettingType.SPEEDRAMP && var8.getSettingType() != SettingType.COLOR) {
                var6 = this.renderModuleSettings(this, var8, 30, var6, 20);
            }
        }

        if (this.module instanceof ModuleWithModuleSettings) {
            ModuleWithModuleSettings moduleWithModuleSettings = (ModuleWithModuleSettings) this.module;

            for (Module var11 : moduleWithModuleSettings.moduleArray) {
                int var12 = 10;
                CustomGuiScreen var13 = new CustomGuiScreen(this, var11.getName() + "SubView", 0, var6, this.widthA, this.heightA - var6);
                var13.setSize((var0, var1) -> var0.setWidthA(var1.getWidthA()));

                for (Setting<?> var15 : var11.getSettingMap().values()) {
                    var12 = this.renderModuleSettings(var13, var15, 30, var12, 20);
                }

                this.addToList(var13);
                this.moduleWithSettingGuiMap.put(var11, var13);
            }

            moduleWithModuleSettings.addModuleStateListener((parent, module, enabled) -> this.moduleWithSettingGuiMap.get(module).setEnabled(enabled));
            moduleWithModuleSettings.calledOnEnable();
        }

        this.addToList(new UITextDisplay(this, "lbl", 5, 200, 0, 33, UITextDisplay.defaultColorHelper, this.typedText));
    }

    public void method13556() {
        this.anim.changeDirection(Direction.BACKWARDS);
    }

    public boolean method13557() {
        return this.anim.getDirection() == Direction.BACKWARDS && this.anim.calcPercent() == 0.0F;
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = Math.round((float) this.getHeightA() * MathHelper.calculateTransition(this.anim.calcPercent(), 0.0F, 1.0F, 1.0F));
        if (this.anim.getDirection() == Direction.BACKWARDS) {
            var4 = Math.round((float) this.getHeightA() * MathHelper.calculateBackwardTransition(this.anim.calcPercent(), 0.0F, 1.0F, 1.0F));
        }

        RenderUtil.startScissor((float) this.xA, (float) (70 + this.getHeightA() - var4), (float) this.getWidthA(), (float) var4);
        RenderUtil.drawRoundedRect2((float) this.xA, (float) (70 + this.getHeightA() - var4), (float) this.getWidthA(), (float) var4, -2631721);
        super.draw(partialTicks);
        RenderUtil.endScissor();
    }
}
