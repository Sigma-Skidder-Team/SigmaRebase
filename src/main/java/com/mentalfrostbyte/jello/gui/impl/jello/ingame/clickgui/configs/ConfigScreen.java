package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.configs;

import com.google.gson.JsonObject;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.TextButton;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.ClickGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.configs.groups.ConfigGroup;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.configs.groups.ProfileGroup;
import com.mentalfrostbyte.jello.managers.ProfileManager;
import com.mentalfrostbyte.jello.managers.util.profile.Profile;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.system.math.SmoothInterpolator;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Element {
    private final List<Button> field21297 = new ArrayList<Button>();
    public final Animation field21298;
    public ScrollableContentPanel profileScrollView;
    public ConfigGroup field21300;
    private final List<ProfileGroup> field21301 = new ArrayList<ProfileGroup>();

    public ConfigScreen(CustomGuiScreen var1, String var2, int var3, int var4) {
        super(var1, var2, var3 - 250, var4 - 500, 250, 500, ColorHelper.field27961, false);
        this.field21298 = new Animation(300, 100);
        this.setReAddChildren(true);
        this.setListening(false);
        TextButton addButton;
        this.addToList(
                addButton = new TextButton(
                        this, "addButton", this.widthA - 55, 0, ResourceRegistry.JelloLightFont25.getWidth("Add"), 69, ColorHelper.field27961, "+", ResourceRegistry.JelloLightFont25
                )
        );
        addButton.onClick((var1x, var2x) -> this.field21300.method13119(true));
        this.addToList(this.field21300 = new ConfigGroup(this, "profile", 0, 69, this.widthA, 200));
        this.field21300.setReAddChildren(true);
        this.method13615();
    }

    public void method13610() {
        Client.getInstance();
        ProfileManager var3 = Client.getInstance().moduleManager.getConfigurationManager();
        Profile var4 = var3.getCurrentConfig();
        int var5 = 1;

        while (var3.getConfigByCaseInsensitiveName(var4.profileName + " Copy " + var5)) {
            var5++;
        }

        var3.saveConfig(var4.cloneWithName(var4.profileName + " Copy " + var5));
        this.runThisOnDimensionUpdate(() -> this.method13615());
        this.field21300.method13119(false);
    }

    public void method13611(Profile var1) {
        Client.getInstance();
        ProfileManager var4 = Client.getInstance().moduleManager.getConfigurationManager();
        Profile var5 = var4.getCurrentConfig();
        int var6 = 1;

        while (var4.getConfigByCaseInsensitiveName(var1.profileName + " " + var6)) {
            var6++;
        }

        var4.saveConfig(var1.cloneWithName(var1.profileName + " " + var6));
        this.runThisOnDimensionUpdate(() -> this.method13615());
        this.field21300.method13119(false);
    }

    public void method13612() {
        Client.getInstance();
        ProfileManager var3 = Client.getInstance().moduleManager.getConfigurationManager();
        int var4 = 1;

        while (var3.getConfigByCaseInsensitiveName("New Profile " + var4)) {
            var4++;
        }

        var3.saveConfig(new Profile("New Profile " + var4, new JsonObject()));
        this.runThisOnDimensionUpdate(this::method13615);
        this.field21300.method13119(false);
    }

    public void method13613() {
        this.field21300.field20703.changeDirection(Animation.Direction.BACKWARDS);
        if (this.field21298.getDirection() != Animation.Direction.BACKWARDS) {
            this.field21298.changeDirection(Animation.Direction.BACKWARDS);
        }
    }

    public boolean method13614() {
        return this.field21298.getDirection() == Animation.Direction.BACKWARDS && this.field21298.calcPercent() == 0.0F;
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        if (newWidth > this.field21300.method13272() + this.field21300.getHeightA()) {
            this.field21300.method13119(false);
        }

        super.updatePanelDimensions(newHeight, newWidth);
    }

    public void method13615() {
        int var3 = 0;
        if (this.profileScrollView != null) {
            var3 = this.profileScrollView.getScrollBarOffset();
            this.removeChildren(this.profileScrollView);
        }

        this.addToList(this.profileScrollView = new ScrollableContentPanel(this, "profileScrollView", 10, 80, this.widthA - 20, this.heightA - 80 - 10));
        this.profileScrollView.setScrollBarOffset(var3);
        this.field21301.clear();
        int var4 = 0;
        int var5 = 70;

        for (Profile var7 : Client.getInstance().moduleManager.getConfigurationManager().getAllConfigs()) {
            ProfileGroup var8 = new ProfileGroup(this, "profile" + var4, 0, var5 * var4, this.profileScrollView.getWidthA(), var5, var7, var4);
            this.profileScrollView.addToList(var8);
            this.field21301.add(var8);
            var4++;
        }

        ClickGuiScreen var9 = (ClickGuiScreen) this.getParent();
        var9.method13315();
    }

    public void method13616() {
        int var3 = 0;

        for (ProfileGroup var5 : this.field21301) {
            var5.setYA(var3);
            var3 += var5.getHeightA();
        }
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.field21298.calcPercent();
        this.method13616();
        float var4 = SmoothInterpolator.interpolate(partialTicks, 0.37, 1.48, 0.17, 0.99);
        if (this.field21298.getDirection() == Animation.Direction.BACKWARDS) {
            var4 = SmoothInterpolator.interpolate(partialTicks, 0.38, 0.73, 0.0, 1.0);
        }

        this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        this.drawBackground((int) ((float) this.widthA * 0.25F * (1.0F - var4)));
        this.method13284((int) ((float) this.widthA * 0.14F * (1.0F - var4)));
        super.method13224();
        super.method13225();
        int var5 = 10;
        int var6 = RenderUtil2.applyAlpha(-723724, QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F));
        RenderUtil.drawRoundedRect(
                (float) (this.xA + var5 / 2),
                (float) (this.yA + var5 / 2),
                (float) (this.widthA - var5),
                (float) (this.heightA - var5),
                35.0F,
                partialTicks
        );
        RenderUtil.drawRoundedRect(
                (float) (this.xA + var5 / 2),
                (float) (this.yA + var5 / 2),
                (float) (this.xA - var5 / 2 + this.widthA),
                (float) (this.yA - var5 / 2 + this.heightA),
                RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.25F)
        );
        RenderUtil.drawRoundedRect((float) this.xA, (float) this.yA, (float) this.widthA, (float) this.heightA, (float) var5, var6);
        float var7 = 0.9F + (1.0F - SmoothInterpolator.interpolate(this.field21300.field20703.calcPercent(), 0.0, 0.96, 0.69, 0.99)) * 0.1F;
        if (this.field21300.field20703.getDirection() == Animation.Direction.BACKWARDS) {
            var7 = 0.9F + (1.0F - SmoothInterpolator.interpolate(this.field21300.field20703.calcPercent(), 0.61, 0.01, 0.87, 0.16)) * 0.1F;
        }

        this.profileScrollView.method13279(var7, var7);
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont25,
                (float) (this.xA + 25),
                (float) (this.yA + 20),
                "Profiles",
                RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * partialTicks)
        );
        RenderUtil.drawRoundedRect(
                (float) (this.xA + 25),
                (float) (this.yA + 69),
                (float) (this.xA + this.widthA - 25),
                (float) (this.yA + 70),
                RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks)
        );
        super.draw(partialTicks);
    }
}
