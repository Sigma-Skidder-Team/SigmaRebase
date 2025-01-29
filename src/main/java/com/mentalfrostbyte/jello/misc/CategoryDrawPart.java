package com.mentalfrostbyte.jello.misc;

import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

import java.util.List;

public class CategoryDrawPart extends CategoryDrawPartBackground {
    private List<String> categories;
    private int maxCategoryWidth;
    public int currentOffset;
    public int scrollOffset;

    public CategoryDrawPart(List<String> categoryList, int priority) {
        super(priority);
        this.categories = categoryList;
        this.calculateMaxCategoryWidth();
    }

    public void updateCategory(int index, String newCategory) {
        if (index < this.categories.size()) {
            this.categories.set(index, newCategory);
        }
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
        if (this.currentOffset < 0) {
            this.currentOffset = 0;
        }

        if (this.currentOffset > this.categories.size()) {
            this.currentOffset = this.categories.size();
        }

        this.calculateMaxCategoryWidth();
    }

    private void calculateMaxCategoryWidth() {
        this.maxCategoryWidth = 0;

        for (String category : this.categories) {
            this.maxCategoryWidth = Math.max(this.maxCategoryWidth, this.font.getWidth(category));
        }
    }

    @Override
    public void drawContent(float partialTicks) {
        RenderUtil.drawRoundedRect2(
                (float) this.getStartX(),
                (float) this.getStartY(),
                (float) this.getWidth(),
                (float) this.getHeight(),
                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.6F)
        );
        this.scrollOffset += calculateScrollOffset();
        int startX = this.getStartX() + 4;
        int startY = this.getStartY() + this.scrollOffset + 4;
        int width = this.getWidth() - 8;
        int gradientStart = -15781024;
        int gradientEnd = -15626304;
        RenderUtil.drawQuad(startX, startY, startX + width, startY + 25, gradientStart, gradientEnd, gradientEnd, gradientStart);

        float categoryOffsetY = 6;
        for (String categoryName : this.categories) {
            int color = ClientColors.LIGHT_GREYISH_BLUE.getColor();

            if (categoryName.startsWith("§7")) {
                categoryName = categoryName.substring("§7".length());
                color = ClientColors.MID_GREY.getColor();
            }

            RenderUtil.drawString(
                    this.font,
                    (float) (7 + this.getStartX()),
                    this.getStartY() + categoryOffsetY,
                    categoryName,
                    ColorUtils.applyAlpha(color, Math.min(1.0F, partialTicks * 1.7F))
            );
            categoryOffsetY += 25;
        }
    }

    @Override
    public int getWidth() {
        return Math.max(super.getWidth(), this.maxCategoryWidth + 14);
    }

    @Override
    public int getHeight() {
        return this.categories.size() * 25 + 8;
    }

    public void scrollUp() {
        this.currentOffset--;
        if (this.currentOffset < 0) {
            this.currentOffset = this.categories.size() - 1;
        }
    }

    public void scrollDown() {
        this.currentOffset++;
        if (this.currentOffset > this.categories.size() - 1) {
            this.currentOffset = 0;
        }
    }

    public boolean isAnimating() {
        return this.animation.calcPercent() == 1.0F;
    }

    public int calculateScrollOffset() {
        float targetOffset = (float) (this.currentOffset * 25);
        float delta = Math.abs(targetOffset - (float) this.scrollOffset);
        boolean isNegative = targetOffset - (float) this.scrollOffset != delta;
        float frameFactor = 60.0F / (float) Minecraft.getFps();
        float adjustment = Math.min(delta * 0.8F, delta * 0.3F * frameFactor);
        if (isNegative) {
            adjustment *= -1.0F;
        }

        if (adjustment > 0.0F && adjustment < 1.0F) {
            adjustment = 1.0F;
        }

        if (adjustment < 0.0F && adjustment > -1.0F) {
            adjustment = -1.0F;
        }

        return Math.round(adjustment);
    }
}
