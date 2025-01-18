package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.gui.base.Screen;
import com.mentalfrostbyte.jello.gui.unmapped.Class4333;
import com.mentalfrostbyte.jello.gui.unmapped.Class4334;
import com.mentalfrostbyte.jello.gui.unmapped.Class4335;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import totalcross.json.JSONObject;

public class ClassicScreenk extends Screen {
    private static final MinecraftClient field21078 = MinecraftClient.getInstance();
    private static Animation field21079;
    private static final boolean field21080 = true;
    private Class4333 field21081;

    public ClassicScreenk() {
        super("ClassicScreen");
        field21079 = new Animation(250, 200, Direction.FORWARDS);
        this.method13419();
        ColorUtils.blur();
    }

    public void method13417() {
        this.runThisOnDimensionUpdate(() -> this.method13419());
    }

    public void method13418(String var1, ModuleCategory... var2) {
        this.runThisOnDimensionUpdate(() -> {
            if (this.field21081 != null) {
                this.method13236(this.field21081);
            }

            this.addToList(
                    this.field21081 = new Class4335(this, var1, this.getWidthA() / 2, this.getHeightA() / 2, var2));
        });
    }

    private void method13419() {
        if (this.field21081 != null) {
            this.method13236(this.field21081);
        }

        this.addToList(this.field21081 = new Class4334(this, "Sigma", this.getWidthA() / 2, this.getHeightA() / 2));
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public int method13313() {
        return MinecraftClient.getFps();
    }

    @Override
    public JSONObject toConfigWithExtra(JSONObject config) {
        ColorUtils.resetShaders();
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JSONObject config) {
        super.loadConfig(config);
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            field21078.displayGuiScreen(null);
        }
    }

    @Override
    public void draw(float var1) {
        float var4 = field21079.calcPercent();
        RenderUtil.drawRoundedRect(
                (float) this.xA,
                (float) this.yA,
                (float) (this.xA + this.widthA),
                (float) (this.yA + this.heightA),
                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var4 * 0.35F));
        super.draw(var1);
    }
}
