package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.JelloMainMenuManager;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

public class FloatingBubble extends CustomGuiScreen {
    public float field20928;
    public float field20929;
    public float field20930;
    public float field20931;
    public float field20932;
    public float field20933;
    public float field20934;
    public int field20936;
    public int field20937;
    public int field20938 = 114;

    public FloatingBubble(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, int var7) {
        super(var1, var2, var3, var4, var5, var5);
        this.field20928 = this.field20930 = (float) var6;
        this.field20929 = this.field20931 = (float) var7;
        this.field20932 = (float) var3;
        this.field20933 = (float) var4;
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        if (this.field20932 == -9999.0F || this.field20933 == -9999.0F) {
            this.field20932 = (float) this.xA;
            this.field20933 = (float) this.yA;
        }

        this.field20932 = this.field20932 + this.field20928 * JelloMainMenuManager.field20982;
        this.field20933 = this.field20933 + this.field20929 * JelloMainMenuManager.field20982;
        this.xA = Math.round(this.field20932);
        this.yA = Math.round(this.field20933);
        if (!(this.field20932 + (float) this.widthA < 0.0F)) {
            if (this.field20932 > (float) Minecraft.getInstance().getMainWindow().getWidth()) {
                this.field20932 = (float) (0 - this.widthA);
            }
        } else {
            this.field20932 = (float) Minecraft.getInstance().getMainWindow().getWidth();
        }

        if (!(this.field20933 + (float) this.heightA < 0.0F)) {
            if (this.field20933 > (float) Minecraft.getInstance().getMainWindow().getHeight()) {
                this.field20933 = (float) (0 - this.heightA);
            }
        } else {
            this.field20933 = (float) Minecraft.getInstance().getMainWindow().getHeight();
        }

        float var5 = (float) (newHeight - this.method13271());
        float var6 = (float) (newWidth - this.method13272());
        this.field20934 = (float) (1.0 - Math.sqrt((double) (var5 * var5 + var6 * var6)) / (double) this.field20938);
        if (!(Math.sqrt((double) (var5 * var5 + var6 * var6)) < (double) this.field20938)) {
            this.field20928 = this.field20928 - (this.field20928 - this.field20930) * 0.05F * JelloMainMenuManager.field20982;
            this.field20929 = this.field20929 - (this.field20929 - this.field20931) * 0.05F * JelloMainMenuManager.field20982;
        } else {
            float var7 = this.field20932 - (float) newHeight;
            float var8 = this.field20933 - (float) newWidth;
            float var9 = (float) Math.sqrt((double) (var7 * var7 + var8 * var8));
            float var10 = var9 / 2.0F;
            float var11 = var7 / var10;
            float var12 = var8 / var10;
            this.field20928 = this.field20928 + var11 / (1.0F + this.field20934) * JelloMainMenuManager.field20982;
            this.field20929 = this.field20929 + var12 / (1.0F + this.field20934) * JelloMainMenuManager.field20982;
        }

        this.field20936 = newHeight;
        this.field20937 = newWidth;
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtil.drawFilledArc(
                (float) this.xA,
                (float) this.yA,
                (float) this.getWidthA(),
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.07F + (!(this.field20934 > 0.0F) ? 0.0F : this.field20934 * 0.3F))
        );
        super.draw(partialTicks);
    }
}
