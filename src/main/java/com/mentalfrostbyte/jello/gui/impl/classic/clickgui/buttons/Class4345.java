package com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons;

import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.TextField;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Slider;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.SettingType;
import com.mentalfrostbyte.jello.module.settings.impl.InputSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.module.settings.impl.TextBoxSetting;
import com.mentalfrostbyte.jello.util.system.math.MathHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import org.newdawn.slick.TrueTypeFont;

import java.util.HashMap;

public class Class4345 extends ScrollableContentPanel {
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
        com.mentalfrostbyte.jello.gui.base.elements.impl.Text name = new com.mentalfrostbyte.jello.gui.base.elements.impl.Text(screen, setting.getName() + "lbl", x, y, 0, 0, com.mentalfrostbyte.jello.gui.base.elements.impl.Text.defaultColorHelper, setting.getName(), this.settingsNameFont);
        com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Text description = new com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Text(screen, setting.getName() + "desc", x + 195, y + 4, 330, 18, setting);
        screen.addToList(name);
        screen.addToList(description);
        switch (setting.getSettingType()) {
            case BOOLEAN:
                Checkbox var26 = new Checkbox(screen, setting.getName() + "checkbox", x + 135, y + 4, 40, 18);
                var26.method13094((Boolean) setting.getCurrentValue(), false);
                setting.addObserver(var1x -> {
                    if (var26.method13092() != (Boolean) var1x.getCurrentValue()) {
                        var26.method13094((Boolean) var1x.getCurrentValue(), false);
                    }
                });
                var26.onPress(var1x -> setting.setCurrentValue(((Checkbox) var1x).method13092()));
                screen.addToList(var26);
                y += 18 + value;
                break;
            case NUMBER:
                NumberSetting sett = (NumberSetting) setting;
                com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Slider var13 = new com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Slider(screen, setting.getName() + "slider", x, y + 31, 240, 4);
                var13.setText(Float.toString((Float) setting.getCurrentValue()));
                name.setText(setting.getName() + ": " + setting.getCurrentValue());
                var13.method13699(Slider.method13134(sett.getMin(), sett.getMax(), (Float) sett.getCurrentValue()), false);
                int decimalPlacs = sett.getDecimalPlaces();
                sett.addObserver(
                        var5x -> {
                            if (com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Slider.method13694(var13.method13697(), sett.getMin(), sett.getMax(), sett.getStep(), decimalPlacs)
                                    != (Float) var5x.getCurrentValue()) {
                                var13.setText(Float.toString((Float) var5x.getCurrentValue()));
                                var13.method13699(com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Slider.method13693(sett.getMin(), sett.getMax(), (Float) var5x.getCurrentValue()), false);
                                name.setText(setting.getName() + ": " + setting.getCurrentValue());
                            }
                        }
                );
                var13.onPress(var5x -> {
                    float var8x = ((com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Slider) var5x).method13697();
                    float var9x = com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Slider.method13694(var8x, sett.getMin(), sett.getMax(), sett.getStep(), decimalPlacs);
                    if (var9x != (Float) setting.getCurrentValue()) {
                        var13.setText(Float.toString(var9x));
                        setting.setCurrentValue(var9x);
                    }

                    name.setText(setting.getName() + ": " + setting.getCurrentValue());
                });
                screen.addToList(var13);
                y += 54;
                break;
            case INPUT:
                int var16 = 114;
                int var20 = 27;
                Input var24;
                this.addToList(
                        var24 = new Input(
                                screen,
                                setting.getName() + "txt",
                                x,
                                y + 27,
                                var16,
                                var20,
                                TextField.field20741,
                                (String) setting.getCurrentValue(),
                                setting.getName(),
                                ResourceRegistry.DefaultClientFont
                        )
                );
                var24.setFont(ResourceRegistry.JelloLightFont18);
                var24.addChangeListener(var1x -> setting.setCurrentValue(var1x.getText()));
                setting.addObserver(var2x -> {
                    if (var24.getText() != ((InputSetting) setting).getCurrentValue()) {
                        var24.setText(((InputSetting) setting).getCurrentValue());
                    }
                });
                y += var20 + value;
                break;
            case MODE:
                Dropdown var23 = new Dropdown(
                        screen, setting.getName() + "btn", x, y + 27, 80, 20, ((ModeSetting) setting).getAvailableModes(), ((ModeSetting) setting).getModeIndex()
                );
                setting.addObserver(var2x -> {
                    if (var23.method13671() != ((ModeSetting) setting).getModeIndex()) {
                        var23.method13672(((ModeSetting) setting).getModeIndex());
                    }
                });
                var23.onPress(var1x -> ((ModeSetting) setting).setModeByIndex(((Dropdown) var1x).method13671()));
                screen.addToList(var23);
                y += 65;
                break;
            case TEXTBOX:
                Textbox var12 = new Textbox(
                        screen, setting.getName() + "btn", screen.getWidthA() - value, y + 6, 123, 27, ((TextBoxSetting) setting).getOptions(), (Integer) setting.getCurrentValue()
                );
                setting.addObserver(var1x -> {
                    if (var12.method13720() != (Integer) var1x.getCurrentValue()) {
                        var12.method13722((Integer) var1x.getCurrentValue(), false);
                    }
                });
                var12.onPress(var1x -> setting.setCurrentValue(((Textbox) var1x).method13720()));
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
        com.mentalfrostbyte.jello.gui.base.elements.impl.Text var3 = new com.mentalfrostbyte.jello.gui.base.elements.impl.Text(
                this, "settingsname", 12, 2, this.widthA, 20, com.mentalfrostbyte.jello.gui.base.elements.impl.Text.defaultColorHelper, this.module.getFormattedName() + " Settings", this.settingsNameFont
        );
        this.addToList(var3);
        int var6 = 35;

        for (Setting var8 : this.module.getSettingMap().values()) {
            if (var8.getSettingType() != SettingType.SPEEDRAMP && var8.getSettingType() != SettingType.COLOR) {
                var6 = this.renderModuleSettings(this, var8, 30, var6, 20);
            }
        }

        if (this.module instanceof ModuleWithModuleSettings moduleWithModuleSettings) {

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

            moduleWithModuleSettings.addModuleStateListener((parent, module, enabled) -> this.moduleWithSettingGuiMap.get(module).setSelfVisible(enabled));
            moduleWithModuleSettings.calledOnEnable();
        }

        this.addToList(new com.mentalfrostbyte.jello.gui.base.elements.impl.Text(this, "lbl", 5, 200, 0, 33, com.mentalfrostbyte.jello.gui.base.elements.impl.Text.defaultColorHelper, this.text));
    }

    public void method13556() {
        this.anim.changeDirection(Animation.Direction.BACKWARDS);
    }

    public boolean method13557() {
        return this.anim.getDirection() == Animation.Direction.BACKWARDS && this.anim.calcPercent() == 0.0F;
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = Math.round((float) this.getHeightA() * MathHelper.easeOutCubic(this.anim.calcPercent(), 0.0F, 1.0F, 1.0F));
        if (this.anim.getDirection() == Animation.Direction.BACKWARDS) {
            var4 = Math.round((float) this.getHeightA() * MathHelper.easeInCubic(this.anim.calcPercent(), 0.0F, 1.0F, 1.0F));
        }

        RenderUtil.startScissor((float) this.xA, (float) (70 + this.getHeightA() - var4), (float) this.getWidthA(), (float) var4);
        RenderUtil.drawRoundedRect2((float) this.xA, (float) (70 + this.getHeightA() - var4), (float) this.getWidthA(), (float) var4, -2631721);
        super.draw(partialTicks);
        RenderUtil.restoreScissor();
    }
}
