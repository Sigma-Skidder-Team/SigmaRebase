package net.minecraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IRenderable
{
    void render(MatrixStack matrices, int mouseX, int mouseY, float delta);
}
