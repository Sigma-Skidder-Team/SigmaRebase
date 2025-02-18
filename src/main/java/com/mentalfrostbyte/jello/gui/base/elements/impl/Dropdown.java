package com.mentalfrostbyte.jello.gui.base.elements.impl;

import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.dropdown.Class7262;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.MathHelper;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Dropdown extends Element {
    public static final ColorHelper field21325 = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.DEEP_TEAL.getColor()).method19414(FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2);
    public List<String> values;
    public int selectedIdx;
    public boolean field21328;
    private Animation animation = new Animation(220, 220);
    private Map<Integer, Sub> field21331 = new HashMap<Integer, Sub>();

    public Dropdown(CustomGuiScreen var1, String typeThingIdk, int x, int y, int width, int height, List<String> values, int selectedIdx) {
        super(var1, typeThingIdk, x, y, width, height, field21325, false);
        this.values = values;
        this.selectedIdx = selectedIdx;
        this.addButtons();
    }

    public void method13643(List<String> var1, int var2) {
        Sub var5 = new Sub(this, "sub" + var2, this.widthA + 10, this.getHeightA() * (var2 + 1), 200, this.getHeightA(), var1, 0);
        this.field21331.put(var2, var5);
        var5.setEnabled(false);
        var5.onPress(var2x -> {
            this.method13656(var2);
            this.method13658(false);
            this.callUIHandlers();
        });
        this.addToList(var5);
    }

    public Sub method13645(int var1) {
        for (Entry var5 : this.field21331.entrySet()) {
            if ((Integer) var5.getKey() == var1) {
                return (Sub) var5.getValue();
            }
        }

        return null;
    }

    private void addButtons() {
        this.getChildren().clear();
        this.font = ResourceRegistry.JelloLightFont18;
        Button dropdownButton;
        this.addToList(dropdownButton = new Button(this, "dropdownButton", 0, 0, this.getHeightA(), this.getHeightA(), this.textColor));
        dropdownButton.setSize((var1, var2) -> {
            var1.setXA(0);
            var1.setYA(0);
            var1.setWidthA(this.getWidthA());
            var1.setHeightA(this.getHeightA());
        });
        dropdownButton.doThis((var1, var2) -> this.method13658(!this.method13657()));

        for (String mode : this.values) {
            Button button;
            this.addToList(
                    button = new Button(
                            this,
                            mode,
                            0,
                            this.getHeightA(),
                            this.getWidthA(),
                            this.getHeightA(),
                            new ColorHelper(
                                    ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                                    -1381654,
                                    this.textColor.getPrimaryColor(),
                                    this.textColor.getPrimaryColor(),
                                    FontSizeAdjust.field14488,
                                    FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2
                            ),
                            mode,
                            this.getFont()
                    )
            );
            button.method13034(10);
            button.doThis((var2, var3x) -> {
                int var6x = this.getIndex();
                this.method13656(this.values.indexOf(mode));
                this.method13658(false);
                if (var6x != this.getIndex()) {
                    this.callUIHandlers();
                }
            });
        }

        this.animation.changeDirection(Animation.Direction.BACKWARDS);
        this.method13246(new Class7262(1));
    }

    private int method13647() {
        int var3 = this.method13648();

        for (Entry var5 : this.field21331.entrySet()) {
            if (((Sub) var5.getValue()).isVisible()) {
                var3 = Math.max(
                        var3,
                        (((Sub) var5.getValue()).values.size() - 1) * ((Sub) var5.getValue()).getHeightA() + ((Sub) var5.getValue()).getYA()
                );
            }
        }

        return var3;
    }

    private int method13648() {
        float var3 = MathHelper.calculateTransition(this.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.animation.getDirection() != Animation.Direction.FORWARDS) {
            var3 = QuadraticEasing.easeInQuad(this.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        return (int) ((float) (this.getHeightA() * this.values.size() + 1) * var3);
    }

    public int method13649() {
        return (int) ((float) (this.getHeightA() * this.values.size() + 1));
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        if (!this.method13114(newHeight, newWidth) && this.animation.getDirection() == Animation.Direction.FORWARDS) {
            this.method13658(false);
        }

        int var5 = (newWidth - this.method13272()) / this.getHeightA() - 1;
        if (var5 >= 0
                && var5 < this.values.size()
                && this.animation.getDirection() == Animation.Direction.FORWARDS
                && this.animation.calcPercent() == 1.0F
                && newHeight - this.method13271() < this.getWidthA()) {
            for (Entry var9 : this.field21331.entrySet()) {
                ((Sub) var9.getValue()).setEnabled((Integer) var9.getKey() == var5);
            }
        } else if (!this.method13114(newHeight, newWidth) || this.animation.getDirection() == Animation.Direction.BACKWARDS) {
            for (Entry var7 : this.field21331.entrySet()) {
                ((Sub) var7.getValue()).setEnabled(false);
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtil.drawRoundedRect(
                (float) this.getXA(),
                (float) this.getYA(),
                (float) (this.getXA() + this.getWidthA()),
                (float) (this.getYA() + this.getHeightA()),
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * this.animation.calcPercent())
        );
        RenderUtil.drawRoundedRect(
                (float) this.getXA(),
                (float) this.getYA(),
                (float) this.getWidthA(),
                (float) (this.getHeightA() + this.method13648() - 1),
                6.0F,
                partialTicks * 0.1F * this.animation.calcPercent()
        );
        RenderUtil.drawRoundedRect(
                (float) this.getXA(),
                (float) this.getYA(),
                (float) this.getWidthA(),
                (float) (this.getHeightA() + this.method13648() - 1),
                20.0F,
                partialTicks * 0.2F * this.animation.calcPercent()
        );
        if (this.getText() != null) {
            RenderUtil.method11415(this);
            String var4 = "";

            for (Entry var6 : this.field21331.entrySet()) {
                if (this.selectedIdx == (Integer) var6.getKey()) {
                    var4 = " (" + ((Sub) var6.getValue()).values.get(((Sub) var6.getValue()).field21324) + ")";
                }
            }

            RenderUtil.drawString(
                    this.getFont(),
                    (float) (this.getXA() + 10),
                    (float) (this.getYA() + (this.getHeightA() - this.getFont().getHeight()) / 2 + 1),
                    this.getText() + var4,
                    RenderUtil2.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F)
            );
            RenderUtil.endScissor();
        }

        boolean var8 = this.animation.calcPercent() < 1.0F;
        if (var8) {
            RenderUtil.drawBlurredBackground(
                    this.method13271(), this.method13272(), this.method13271() + this.getWidthA() + 140, this.method13272() + this.getHeightA() + this.method13647()
            );
        }

        GL11.glPushMatrix();
        if (this.animation.calcPercent() > 0.0F) {
            super.draw(partialTicks);
        }

        GL11.glPopMatrix();
        if (var8) {
            RenderUtil.endScissor();
        }

        int var9 = this.getWidthA() - (int) ((float) this.getHeightA() / 2.0F + 0.5F);
        int var10 = (int) ((float) this.getHeightA() / 2.0F + 0.5F) + 1;
        int var7 = (int) ((float) this.getHeightA() / 6.0F + 0.5F);
        GL11.glTranslatef((float) (this.getXA() + var9), (float) (this.getYA() + var10), 0.0F);
        GL11.glRotatef(90.0F * this.animation.calcPercent(), 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.getXA() - var9), (float) (-this.getYA() - var10), 0.0F);
        RenderUtil.drawString(
                this.font,
                (float) (this.getXA() + var9 - 6),
                (float) (this.getYA() + var10 - 14),
                ">",
                RenderUtil2.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F * (!this.method13114(this.getHeightO(), this.getWidthO()) ? 0.5F : 1.0F))
        );
    }

    public List<String> method13651() {
        return this.values;
    }

    public void method13652(String var1, int var2) {
        this.method13651().add(var2, var1);
        this.addButtons();
    }

    public int getIndex() {
        return this.selectedIdx;
    }

    public void method13656(int var1) {
        this.selectedIdx = var1;
    }

    public boolean method13657() {
        return this.field21328;
    }

    public void method13658(boolean var1) {
        this.field21328 = var1;
        this.animation.changeDirection(!this.method13657() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
    }

    @Override
    public String getText() {
        return this.method13651().size() <= 0 ? null : this.method13651().get(this.getIndex());
    }

    @Override
    public boolean method13114(int var1, int var2) {
        for (Entry var6 : this.field21331.entrySet()) {
            if (((Sub) var6.getValue()).isVisible() && ((Sub) var6.getValue()).method13114(var1, var2)) {
                return true;
            }
        }

        var1 -= this.method13271();
        var2 -= this.method13272();
        return var1 >= 0 && var1 <= this.getWidthA() && var2 >= 0 && var2 <= this.getHeightA() + this.method13648();
    }
}
