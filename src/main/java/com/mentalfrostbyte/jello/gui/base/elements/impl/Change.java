package com.mentalfrostbyte.jello.gui.base.elements.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.SmoothInterpolator;

public class Change extends CustomGuiScreen {
    public Animation animation2 = new Animation(370, 150, Animation.Direction.BACKWARDS);

    public Change(CustomGuiScreen var1, String var2, JsonObject var3) throws JsonParseException {
        super(var1, var2);
        this.setWidthA(this.getParent().getWidthA());
        int var6 = 0;

        String var7 = var3.get("title").getAsString();
        JsonArray var8 = var3.getAsJsonArray("changes");
        this.addToList(new Text(this, "title", 0, var6, 0, 0, ColorHelper.field27961, var7, ResourceRegistry.JelloMediumFont40));
        var6 += 55;

        for (int var9 = 0; var9 < var8.size(); var9++) {
            String var10 = " - " + var8.get(var9).getAsString();
            this.addToList(
                    new Text(
                            this,
                            "change" + var9,
                            0,
                            var6,
                            0,
                            0,
                            new ColorHelper(0, 0, 0, RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)),
                            var10,
                            ResourceRegistry.JelloLightFont20
                    )
            );
            var6 += 22;
        }

        var6 += 75;
        this.setHeightA(var6);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = SmoothInterpolator.interpolate(this.animation2.calcPercent(), 0.17, 1.0, 0.51, 1.0);
        this.drawBackground((int) ((1.0F - var4) * 100.0F));
        this.method13225();
        partialTicks *= this.animation2.calcPercent();
        super.draw(partialTicks);
    }
}
