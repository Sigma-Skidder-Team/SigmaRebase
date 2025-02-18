package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.panels;

import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.impl.*;
import com.mentalfrostbyte.jello.gui.base.elements.impl.colorpicker.ColorPicker;
import com.mentalfrostbyte.jello.gui.base.interfaces.Class4342;
import com.mentalfrostbyte.jello.gui.combined.ContentSize;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.classic.clickgui.buttons.Textbox;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.TextField;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.impl.*;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map.Entry;

public class SettingPanel extends ScrollableContentPanel implements Class4342 {
    private Module module;
    private boolean field21220;
    public int field21222 = 200;
    private HashMap<Text, Setting> field21223 = new HashMap<Text, Setting>();
    public HashMap<Module, CustomGuiScreen> field21224 = new HashMap<Module, CustomGuiScreen>();
    public Animation field21225 = new Animation(114, 114);
    private String field21226 = "";
    private String field21227 = "";

    public SettingPanel(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Module module) {
        super(var1, var2, var3, var4, var5, var6);
        this.module = module;
        this.setListening(false);
        this.method13511();
    }

    private int method13531(CustomGuiScreen panel, Setting setting, int var3, int var4, int var5) {
        switch (setting.getSettingType()) {
            case BOOLEAN:
                Text var37 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.getName());
                Checkbox var45 = new Checkbox(panel, setting.getName() + "checkbox", panel.getWidthA() - 24 - var5, var4 + 6, 24, 24);
                this.field21223.put(var37, setting);
                var45.method13705((Boolean) setting.getCurrentValue(), false);
                setting.addObserver(var1x -> {
                    if (var45.method13703() != (Boolean) var1x.getCurrentValue()) {
                        var45.method13705((Boolean) var1x.getCurrentValue(), false);
                    }
                });
                var45.onPress(var1x -> setting.setCurrentValue(((Checkbox) var1x).method13703()));
                var45.setSize((var1x, var2x) -> var1x.setXA(var2x.getWidthA() - 24 - var5));
                panel.addToList(var37);
                panel.addToList(var45);
                var4 += 24 + var5;
                break;
            case NUMBER:
                Text var36 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.getName());
                this.field21223.put(var36, setting);
                NumberSetting numbaSetting = (NumberSetting) setting;
                Slider var47 = new Slider(panel, setting.getName() + "slider", panel.getWidthA() - 126 - var5, var4 + 6, 126, 24);
                var47.method13137().setFont(ResourceRegistry.JelloLightFont14);
                var47.setText(Float.toString((Float) setting.getCurrentValue()));
                var47.method13140(Slider.method13134(numbaSetting.getMin(), numbaSetting.getMax(), (Float) numbaSetting.getCurrentValue()), false);
                var47.method13143(-1.0F);
                int var13 = numbaSetting.getDecimalPlaces();
                numbaSetting.addObserver(
                        var3x -> {
                            if (Slider.method13135(var47.method13138(), numbaSetting.getMin(), numbaSetting.getMax(), numbaSetting.getStep(), var13)
                                    != (Float) var3x.getCurrentValue()) {
                                var47.setText(Float.toString((Float) var3x.getCurrentValue()));
                                var47.method13140(Slider.method13134(numbaSetting.getMin(), numbaSetting.getMax(), (Float) var3x.getCurrentValue()), false);
                            }
                        }
                );
                var47.onPress(var4x -> {
                    float var7 = ((Slider) var4x).method13138();
                    float var8x = Slider.method13135(var7, numbaSetting.getMin(), numbaSetting.getMax(), numbaSetting.getStep(), var13);
                    if (var8x != (Float) setting.getCurrentValue()) {
                        var47.setText(Float.toString(var8x));
                        setting.setCurrentValue(var8x);
                    }
                });
                var47.setSize((var1x, var2x) -> var1x.setXA(var2x.getWidthA() - 126 - var5));
                panel.addToList(var36);
                panel.addToList(var47);
                var4 += 24 + var5;
                break;
            case INPUT:
                int var19 = 114;
                int var27 = 27;
                Text var43;
                this.addToList(
                        var43 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, var27, Text.defaultColorHelper, setting.getName())
                );
                this.field21223.put(var43, setting);
                TextField var35;
                this.addToList(
                        var35 = new TextField(
                                panel,
                                setting.getName() + "txt",
                                panel.getWidthA() - var5 - var19,
                                var4 + var27 / 4 - 1,
                                var19,
                                var27,
                                TextField.field20741,
                                (String) setting.getCurrentValue()
                        )
                );
                var35.setFont(ResourceRegistry.JelloLightFont18);
                var35.method13151(var1x -> setting.setCurrentValue(var1x.getText()));
                setting.addObserver(var2x -> {
                    if (var35.getText() != ((InputSetting) setting).getCurrentValue()) {
                        var35.setText(((InputSetting) setting).getCurrentValue());
                    }
                });
                var4 += var27 + var5;
                break;
            case MODE:
                Text var34 = new Text(panel, setting.getName() + "lbl", var3, var4 + 2, this.field21222, 27, Text.defaultColorHelper, setting.getName());
                Dropdown var42 = new Dropdown(
                        panel,
                        setting.getName() + "btn",
                        panel.getWidthA() - var5,
                        var4 + 6 - 1,
                        123,
                        27,
                        ((ModeSetting) setting).getAvailableModes(),
                        ((ModeSetting) setting).getModeIndex()
                );
                this.field21223.put(var34, setting);
                setting.addObserver(var2x -> {
                    if (var42.getIndex() != ((ModeSetting) setting).getModeIndex()) {
                        var42.method13656(((ModeSetting) setting).getModeIndex());
                    }
                });
                var42.onPress(var2x -> {
                    ((ModeSetting) setting).setModeByIndex(((Dropdown) var2x).getIndex());
                    var42.method13656(((ModeSetting) setting).getModeIndex());
                });
                var42.setSize((var2x, var3x) -> var2x.setXA(panel.getWidthA() - 123 - var5));
                panel.addToList(var34);
                panel.addToList(var42);
                var4 += 27 + var5;
            case UNUSED:
            default:
                break;
            case SUBOPTION:
                CustomGuiScreen var17 = new CustomGuiScreen(panel, setting.getName() + "view", var3, var4, panel.getWidthA(), 0);
                int var25 = 0;

                for (Setting var41 : ((SubOptionSetting) setting).getSubSettings()) {
                    var25 = this.method13531(var17, var41, 0, var25, var5);
                }

                new ContentSize().setWidth(var17, panel);
                var17.setSize((var1x, var2x) -> var1x.setWidthA(var2x.getWidthA() - var5));
                panel.addToList(var17);
                var4 += var17.getHeightA() + var5;
                break;
            case TEXTBOX:
                Text var32 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, 27, Text.defaultColorHelper, setting.getName());
                Textbox var40 = new Textbox(
                        panel, setting.getName() + "btn", panel.getWidthA() - var5, var4 + 6, 123, 27, ((TextBoxSetting) setting).getOptions(), (Integer) setting.getCurrentValue()
                );
                this.field21223.put(var32, setting);
                setting.addObserver(var1x -> {
                    if (var40.method13720() != (Integer) var1x.getCurrentValue()) {
                        var40.method13722((Integer) var1x.getCurrentValue(), false);
                    }
                });
                var40.onPress(var1x -> setting.setCurrentValue(((Textbox) var1x).method13720()));
                var40.setSize((var2x, var3x) -> var2x.setXA(panel.getWidthA() - 123 - var5));
                panel.addToList(var32);
                panel.addToList(var40);
                var4 += 27 + var5;
                break;
            case BOOLEAN2:
                Text var31 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, 200, Text.defaultColorHelper, setting.getName());
                Picker var39 = new Picker(
                        panel,
                        setting.getName() + "picker",
                        panel.getWidthA() - var5,
                        var4 + 5,
                        175,
                        200,
                        ((BooleanListSetting) setting).isEnabled(),
                        ((BooleanListSetting) setting).getCurrentValue().toArray(new String[0])
                );
                this.field21223.put(var31, setting);
                var39.onPress(var2x -> setting.setCurrentValue(var39.method13072()));
                var39.setSize((var2x, var3x) -> var2x.setXA(panel.getWidthA() - 175 - var5));
                panel.addToList(var31);
                panel.addToList(var39);
                var4 += 200 + var5;
                break;
            case COLOR:
                ColorSetting var30 = (ColorSetting) setting;
                Text var38 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.getName());
                ColorPicker var46 = new ColorPicker(
                        panel, setting.getName() + "color", panel.getWidthA() - 160 - var5 + 10, var4, 160, 114, (Integer) setting.getCurrentValue(), var30.isRainbowEnabled()
                );
                this.field21223.put(var38, setting);
                setting.addObserver(var3x -> {
                    var46.method13048((Integer) setting.getCurrentValue());
                    var46.method13046(var30.isRainbowEnabled());
                });
                var46.onPress(var2x -> {
                    setting.updateCurrentValue(((ColorPicker) var2x).method13049(), false);
                    var30.setRainbowEnabled(((ColorPicker) var2x).method13047());
                });
                panel.addToList(var38);
                panel.addToList(var46);
                var4 += 114 + var5 - 10;
                break;
            case SPEEDRAMP:
                SpeedRampSetting.SpeedRamp var10 = (SpeedRampSetting.SpeedRamp) setting.getCurrentValue();
                Text var11 = new Text(panel, setting.getName() + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.getName());
                Bezier var12 = new Bezier(
                        panel,
                        setting.getName() + "color",
                        panel.getWidthA() - 150 - var5 + 10,
                        var4,
                        150,
                        150,
                        20,
                        var10.startValue,
                        var10.middleValue,
                        var10.endValue,
                        var10.maxValue
                );
                this.field21223.put(var11, setting);
                setting.addObserver(var2x -> {
                    SpeedRampSetting.SpeedRamp var5x = (SpeedRampSetting.SpeedRamp) setting.getCurrentValue();
                    var12.method13041(var5x.startValue, var5x.middleValue, var5x.endValue, var5x.maxValue);
                });
                var12.onPress(
                        var2x -> ((SpeedRampSetting) setting).updateValues(var12.method13040()[0], var12.method13040()[1], var12.method13040()[2], var12.method13040()[3])
                );
                panel.addToList(var11);
                panel.addToList(var12);
                var4 += 150 + var5 - 10;
        }

        return var4 - (var5 - 10);
    }

    private void method13511() {
        int var4 = 20;

        for (Setting var6 : this.module.getSettingMap().values()) {
            //HIDEN SHIT
            if(var6.isHidden()) continue;

            var4 = this.method13531(this, var6, 20, var4, 20);
        }

        int var17 = var4;
        if (this.module instanceof ModuleWithModuleSettings) {
            ModuleWithModuleSettings var18 = (ModuleWithModuleSettings) this.module;

            for (Module var10 : var18.moduleArray) {
                int var11 = 0;
                CustomGuiScreen var12 = new CustomGuiScreen(this, var10.getName() + "SubView", 0, var17, this.widthA, this.heightA - var4);
                var12.setSize((var0, var1) -> var0.setWidthA(var1.getWidthA()));

                for (Setting var14 : var10.getSettingMap().values()) {
                    var11 = this.method13531(var12, var14, 20, var11, 20);
                }

                var4 = Math.max(var4 + var11, var4);

                for (CustomGuiScreen var20 : var12.getChildren()) {
                    if (var20 instanceof Dropdown) {
                        Dropdown var15 = (Dropdown) var20;
                        int var16 = var15.method13649() + var15.getYA() + var15.getHeightA() + 14;
                        var11 = Math.max(var11, var16);
                    }
                }

                var12.setHeightA(var11);
                this.addToList(var12);
                this.field21224.put(var10, var12);
            }

            var18.addModuleStateListener((parent, module, enabled) -> this.field21224.get(module).setEnabled(enabled));
            var18.calledOnEnable();
        }

        this.addToList(new CustomGuiScreen(this, "extentionhack", 0, var4, 0, 20));
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float partialTicks) {
        boolean var4 = false;

        for (Entry var6 : this.field21223.entrySet()) {
            Text var7 = (Text) var6.getKey();
            Setting var8 = (Setting) var6.getValue();
            if (var7.method13298() && var7.method13289()) {
                var4 = true;
                this.field21226 = var8.getDescription();
                this.field21227 = var8.getName();
                break;
            }
        }

        GL11.glPushMatrix();
        super.draw(partialTicks);
        GL11.glPopMatrix();
        this.field21225.changeDirection(!var4 ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + 10),
                (float) (this.getYA() + this.getHeightA() + 24),
                this.field21227,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.field21225.calcPercent())
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + 11),
                (float) (this.getYA() + this.getHeightA() + 24),
                this.field21227,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.field21225.calcPercent())
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + 14 + ResourceRegistry.JelloLightFont14.getWidth(this.field21227) + 2),
                (float) (this.getYA() + this.getHeightA() + 24),
                this.field21226,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.field21225.calcPercent())
        );
    }

    @Override
    public boolean method13525() {
        return this.field21220;
    }

}
