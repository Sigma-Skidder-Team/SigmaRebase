package com.mentalfrostbyte.jello.gui.base.elements.impl.altmanager;

import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.LoadingIndicator;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.combined.AnimatedIconPanel;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.Account;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;

public class AccountUI extends AnimatedIconPanel {
    public Account selectedAccount;
    //   private BufferedImage field20799;
//   private Texture field20800;
    private LoadingIndicator loadingIndicator;
    private boolean field20802 = false;
    private Animation field20803 = new Animation(814, 114, Animation.Direction.BACKWARDS);
    private float field20804 = 0.0F;
    public Animation field20805 = new Animation(800, 300, Animation.Direction.BACKWARDS);
    private int field20806 = 0;
    private int field20807 = 0;
    private int color = RenderUtil2.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 20.0F);

    public AccountUI(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Account var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.selectedAccount = var7;
        this.addToList(this.loadingIndicator = new LoadingIndicator(this, "loading", var5 - 50, 35, 30, 30));
        this.loadingIndicator.setHovered(false);
    }

    public void method13166(boolean var1) {
        this.method13167(var1, false);
    }

    public void method13167(boolean var1, boolean var2) {
        this.field20803.changeDirection(!var1 ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
        if (var2) {
            this.field20803.updateStartTime(1.0F);
        }
    }

    public boolean method13168() {
        return this.field20803.getDirection() == Animation.Direction.FORWARDS;
    }

    @Override
    public void draw(float partialTicks) {
        this.method13225();
        this.color = RenderUtil2.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 2.0F);
        int var4 = ((ScrollableContentPanel) this.parent.getParent()).method13513();
        int var5 = Math.max(0, this.yA - var4);
        int var6 = Math.max(0, this.heightA + Math.min(100, this.yA - var4 - var5));
        float var7 = (float) Math.min(50, var6) / 50.0F;
        int var8 = this.getParent().getParent().getHeightA() + this.getParent().getParent().method13272();
        int var9 = 0;
        var5 += var4;
        if (var5 - var4 <= var8) {
            if (var7 != 0.0F) {
                RenderUtil.method11467(
                        this.xA,
                        var5,
                        this.widthA,
                        Math.max(20, var6),
                        RenderUtil2.applyAlpha(!this.method13212() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.color, var7)
                );
                RenderUtil.drawBlurredBackground(this.xA, var5, this.xA + this.widthA + 20, var5 + var6, true);
                if (this.selectedAccount != null) {
                    this.method13169();
                    this.method13170();
                    this.method13171(var7);
                    if (this.field20803.calcPercent() > 0.0F && var6 > 55) {
                        RenderUtil.drawImage(
                                (float) (this.xA + this.getWidthA()),
                                (float) var5 + (float) (26 * var6) / 100.0F,
                                18.0F * this.field20803.calcPercent() * (float) var6 / 100.0F,
                                (float) (47 * var6) / 100.0F,
                                Resources.selectPNG,
                                !this.method13212() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.color
                        );
                    }

                    super.draw(partialTicks * var7);
                    RenderUtil.endScissor();
                }
            }
        } else {
            var9++;
        }
    }

    public void method13169() {
        RenderUtil.drawImage(
                (float) (this.xA + 13), (float) (this.yA + 13), 75.0F, 75.0F, this.selectedAccount.setSkinTexture(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), true
        );
        RenderUtil.method11464((float) (this.xA + 13), (float) (this.yA + 13), 75.0F, 75.0F, 20.0F, 1.0F);
        RenderUtil.drawImage(
                (float) (this.xA + 1),
                (float) this.yA,
                100.0F,
                100.0F,
                Resources.cerclePNG,
                !this.method13212() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.color
        );
    }

    public void method13170() {
        if (this.selectedAccount.isEmailAValidEmailFormat()) {
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont25, (float) (this.xA + 110), (float) (this.yA + 18), this.selectedAccount.getEmail(), ClientColors.DEEP_TEAL.getColor()
            );
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont14,
                    (float) (this.xA + 110),
                    (float) (this.yA + 50),
                    "Username: " + this.selectedAccount.getEmail(),
                    ClientColors.MID_GREY.getColor()
            );
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont14, (float) (this.xA + 110), (float) (this.yA + 65), "Offline account", ClientColors.MID_GREY.getColor()
            );
        } else {
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont25, (float) (this.xA + 110), (float) (this.yA + 18), this.selectedAccount.getKnownName(), ClientColors.DEEP_TEAL.getColor()
            );
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont14,
                    (float) (this.xA + 110),
                    (float) (this.yA + 50),
                    "Email: " + this.selectedAccount.getEmail(),
                    ClientColors.MID_GREY.getColor()
            );
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont14,
                    (float) (this.xA + 110),
                    (float) (this.yA + 65),
                    "Password: " + this.selectedAccount.getPassword().replaceAll(".", Character.toString('·')),
                    ClientColors.MID_GREY.getColor()
            );
        }
    }

    public void method13171(float var1) {
        this.field20804 = this.field20804 + (this.field20802 ? 0.33333334F : -0.33333334F);
        this.field20804 = Math.min(1.0F, Math.max(0.0F, this.field20804));
        this.field20806 = Math.max(0, this.field20806 - 1);
        float var4 = this.field20806 <= 20 ? 20.0F : -20.0F;
        float var5 = (float) this.field20806 >= var4 && (float) this.field20806 <= (float) this.field20807 - var4 ? 1.0F : (float) this.field20806 % var4 / var4;
        RenderUtil.drawImage(
                (float) (this.xA + this.widthA - 45),
                (float) (this.yA + 42),
                17.0F,
                17.0F,
                Resources.errorsPNG,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5 * var1)
        );
        RenderUtil.drawImage(
                (float) (this.xA + this.widthA - 45),
                (float) (this.yA + 45),
                17.0F,
                13.0F,
                Resources.activePNG,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.field20804 * var1)
        );
    }

    public void method13172(boolean var1) {
        this.field20802 = var1;
    }

    public void method13173(int var1) {
        this.field20806 = var1;
        this.field20807 = var1;
    }

    public void method13174(boolean notInThread) {
        this.loadingIndicator.setHovered(notInThread);
    }
}
