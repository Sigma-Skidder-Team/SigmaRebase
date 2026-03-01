package com.mentalfrostbyte.jello.gui.base.elements.impl;

import com.google.gson.JsonObject;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.VerticalScrollBarButton;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.combined.AnimatedIconPanel;
import com.mentalfrostbyte.jello.gui.base.interfaces.Class4293;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import com.mentalfrostbyte.jello.util.system.other.GsonUtil;

public class VerticalScrollBar extends AnimatedIconPanel implements Class4293 {
    public int offset;
    public float field20794;
    public boolean field20795;
    public final VerticalScrollBarButton field20796;
    public TimerUtil field20797 = new TimerUtil();

    public VerticalScrollBar(CustomGuiScreen var1, int var2) {
        super(var1, "verticalScrollBar", var1.getWidthA() - var2 - 5, 5, var2, var1.getHeightA() - 10, false);
        this.setSize((var1x, var2x) -> {
            var1x.setXA(var2x.getWidthA() - var2 - 5);
            var1x.setYA(5);
            var1x.setWidthA(var2);
            var1x.setHeightA(var2x.getHeightA() - 10);
        });
        this.addToList(this.field20796 = new VerticalScrollBarButton(this, this, var2));
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);
        if (this.parent != null && this.parent.method13228(this.getHeightO(), this.getWidthO(), false) || ((ScrollableContentPanel) this.parent).scrollable) {
            float var4 = (float) ((ScrollableContentPanel) this.getParent()).getButtonList().getHeightA();
            float var5 = (float) this.getParent().getHeightA();
            float var6 = (float) this.getHeightA();
            if (var4 == 0.0F) {
                return;
            }

            float var7 = var5 / var4;
            if (var7 >= 1.0F) {
                return;
            }

            this.offset = this.offset
                    - Math.round(!(scroll < 0.0F) ? (float) ((ScrollableContentPanel) this.parent).scrollStep * scroll : 1.0F * (float) ((ScrollableContentPanel) this.parent).scrollStep * scroll);
            this.field20797.reset();
            this.field20797.start();
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        this.field20908 = this.method13228(newHeight, newWidth, false);
        this.field20794 = this.field20794
                + (
                this.field20796.getHeightA() >= this.getHeightA()
                        ? -1.0F
                        : (
                        !this.method13298() && !this.field20796.method13216() && (!this.field20797.isEnabled() || this.field20797.getElapsedTime() >= 500L)
                                ? -0.05F
                                : 0.05F
                )
        );
        this.field20794 = Math.min(Math.max(0.0F, this.field20794), 1.0F);
        float var5 = (float) ((ScrollableContentPanel) this.getParent()).getButtonList().getHeightA();
        float var6 = (float) this.getParent().getHeightA();
        float var7 = (float) this.getHeightA();
        float var8 = var6 / var5;
        boolean var9 = var8 < 1.0F && var5 > 0.0F && this.field20794 >= 0.0F;
        this.setSelfVisible(var9);
        this.setHovered(var9);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks *= this.field20794;
        int var4 = 5;
        int var5 = RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.2F * partialTicks);
        int var6 = this.xA;
        int var7 = this.widthA;
        if (Client.getInstance().clientMode != ClientMode.JELLO) {
            var4 = 0;
            var7 -= 8;
            var6 += 8;
            RenderUtil.drawRoundedRect(
                    (float) var6,
                    (float) (this.yA + var4),
                    (float) (var6 + var7),
                    (float) (this.yA + this.heightA - var4),
                    RenderUtil2.applyAlpha(ClientColors.MID_GREY.getColor(), 0.1F * partialTicks)
            );
        } else {
            RenderUtil.drawImage((float) var6, (float) this.yA, (float) var7, 5.0F, Resources.verticalScrollBarTopPNG, 0.45F * partialTicks);
            RenderUtil.drawImage((float) var6, (float) (this.yA + this.heightA - var4), (float) var7, 5.0F, Resources.verticalScrollBarBottomPNG, 0.45F * partialTicks);
            RenderUtil.drawRoundedRect((float) var6, (float) (this.yA + var4), (float) (var6 + var7), (float) (this.yA + this.heightA - var4), var5);
        }

        super.draw(partialTicks);
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onClick(mouseX, mouseY, mouseButton)) {
            this.field20908 = this.method13228(mouseX, mouseY, false);
            if (this.method13298()) {
                int var6 = mouseY - this.method13272();
                if (var6 <= this.field20796.getYA() + this.field20796.getHeightA()) {
                    if (var6 < this.field20796.getYA()) {
                        this.offset = this.offset - (int) ((float) ((ScrollableContentPanel) this.parent).getButtonList().getHeightA() / 4.0F);
                    }
                } else {
                    this.offset = this.offset + (int) ((float) ((ScrollableContentPanel) this.parent).getButtonList().getHeightA() / 4.0F);
                }
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        config.addProperty("offset", this.offset);
        return super.toConfigWithExtra(config);
    }

    @Override
    public void loadConfig(JsonObject config) {
        super.loadConfig(config);
        this.offset = GsonUtil.getIntOrDefault(config, "offset", this.offset);
    }

    @Override
    public int method13162() {
        return this.offset;
    }

}
