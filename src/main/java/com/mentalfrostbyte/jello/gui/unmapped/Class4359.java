package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.managers.MusicManager;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;

public class Class4359 extends UIBase {
    private final MusicManager musicManager = Client.getInstance().musicManager;
    public float field21315 = -1.0F;

    public Class4359(CustomGuiScreen parentScreen, String var2, int var3, int var4, int var5, int var6) {
        super(parentScreen, var2, var3, var4, var5, var6, false);
        this.method13247((var1x, var2x) -> {
            int var5x = (int) this.musicManager.getDuration();
            int var6x = this.musicManager.getDurationInt();
            this.field21315 = Math.min((float) var5x / (float) var6x, 1.0F);
        });
        this.method13249((var1x, var2x) -> {
            if (this.method13298() && this.method13297()) {
                int var5x = (int) Math.min((double) ((int) (this.field21315 * (float) this.musicManager.getDurationInt())), this.musicManager.method24322());
                this.musicManager.method24329((double) var5x);
            }
        });
    }

    @Override
    public void draw(float partialTicks) {
        long durationLong = (int) this.musicManager.getDuration();
        double var5 = this.musicManager.method24322();
        int durationInt = this.musicManager.getDurationInt();
        float var8 = Math.max(0.0F, Math.min((float) durationLong / (float) durationInt, 1.0F));
        float var9 = Math.max(0.0F, Math.min((float) var5 / (float) durationInt, 1.0F));
        if (this.method13212() && this.method13298() && var5 != 0.0) {
            int var10 = this.getHeightO() - this.method13271();
            this.field21315 = Math.min(Math.max((float) var10 / (float) this.getWidthA(), 0.0F), var9);
            var8 = this.field21315;
        }

        if (durationLong == 0 && !this.musicManager.isPlayingSong()) {
            RenderUtil.drawRoundedRect2(
                    (float) this.getXA(),
                    (float) this.getYA(),
                    (float) this.getWidthA(),
                    (float) this.getHeightA(),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
            );
        } else {
            RenderUtil.drawRoundedRect2(
                    (float) this.getXA(),
                    (float) this.getYA(),
                    (float) this.getWidthA(),
                    (float) this.getHeightA(),
                    ColorUtils.applyAlpha(ClientColors.MID_GREY.getColor(), 0.075F)
            );
            RenderUtil.drawRoundedRect2(
                    (float) this.getXA() + (float) this.getWidthA() * var9,
                    (float) this.getYA(),
                    (float) this.getWidthA() * (1.0F - var9),
                    (float) this.getHeightA(),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
            );
            RenderUtil.drawRoundedRect2(
                    (float) this.getXA(),
                    (float) this.getYA(),
                    (float) this.getWidthA() * var8,
                    (float) this.getHeightA(),
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
            );
            if (var8 != 0.0F) {
                RenderUtil.drawImage((float) this.getXA() + (float) this.getWidthA() * var8, (float) this.getYA(), 5.0F, 5.0F, Resources.shadowRightPNG);
            }
        }
    }
}
