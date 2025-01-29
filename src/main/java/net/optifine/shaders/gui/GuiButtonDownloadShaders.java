package net.optifine.shaders.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.optifine.Config;
import net.optifine.gui.GuiButtonOF;

public class GuiButtonDownloadShaders extends GuiButtonOF
{
    public GuiButtonDownloadShaders(int buttonID, int xPos, int yPos)
    {
        super(buttonID, xPos, yPos, 22, 20, "");
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        if (this.visible)
        {
            super.render(matrices, mouseX, mouseY, delta);
            ResourceLocation resourcelocation = new ResourceLocation("optifine/textures/icons.png");
            Config.getTextureManager().bindTexture(resourcelocation);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(matrices, this.x + 3, this.y + 2, 0, 0, 16, 16);
        }
    }
}
