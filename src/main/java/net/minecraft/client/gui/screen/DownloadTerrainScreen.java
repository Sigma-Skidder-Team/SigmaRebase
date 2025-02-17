package net.minecraft.client.gui.screen;

import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class DownloadTerrainScreen extends Screen {
    private static final ITextComponent field_243307_a = new TranslationTextComponent("multiplayer.downloadingTerrain");
    private CustomLoadingScreen customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();

    public DownloadTerrainScreen() {
        super(NarratorChatListener.EMPTY);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (Minecraft.getInstance().world != null && Minecraft.getInstance().player != null && JelloPortal.getVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            Minecraft.getInstance().displayGuiScreen(null);
            return;
        }

        if (this.customLoadingScreen != null) {
            this.customLoadingScreen.drawBackground(this.width, this.height);
        } else {
            this.renderDirtBackground(0);
        }

        drawCenteredString(matrices, this.font, field_243307_a, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public boolean isPauseScreen() {
        return false;
    }
}
