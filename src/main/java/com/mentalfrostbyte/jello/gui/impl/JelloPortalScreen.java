package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.protocolinfo.ProtocolInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static de.florianmichael.viamcp.protocolinfo.ProtocolInfo.PROTOCOL_INFOS;

public class JelloPortalScreen extends MultiplayerScreen {

    private AbstractButtonWidget versionSelectorWidget;

    public JelloPortalScreen(Screen parentScreen) {
        super(parentScreen);
    }

    @Override
    public void init() {
        super.init();
        // Create the slider
        DoubleOption versionSelector = new DoubleOption(
                "jello.portaloption",
                0.0,
                this.getAvailableVersions().size() - 1,
                1.0F,
                (var1) -> (double) getCurrentVersionIndex(),
                this::onSliderChange,
                (settings, slider) -> new LiteralText(getVersion(getCurrentVersionIndex())).getString());
        this.versionSelectorWidget = this
                .addButton(versionSelector.createButton(this.client.options, this.width / 2 + 40, 7, 114));
    }

    private void onSliderChange(GameOptions settings, Double aDouble) {
        int newIndex = aDouble.intValue();
        if (newIndex >= 0 && newIndex < getAvailableVersions().size()) {
            ViaLoadingBase.getInstance().reload(getVersion(newIndex));
            Client.currentVersionIndex = newIndex;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        RenderUtil.drawPortalBackground(
                0, 0, MinecraftClient.getInstance().getWindow().getWidth(),
                (int) (30.0 * MinecraftClient.getInstance().getWindow().getScaleFactor()
                        / (double) GuiManager.scaleFactor));
        this.renderBackground(matrices);
        RenderUtil.endScissor();
        this.versionSelectorWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, Text.of(this.getTitle().getString()), this.width / 2 - 146, 13, 16777215);
        client.textRenderer.drawWithShadow(matrices, "Jello Portal:", (float) this.width / 2 - 30, 13, -1);
    }

    private int getCurrentVersionIndex() {
        return Client.currentVersionIndex;
    }

    private List<ProtocolVersion> getAvailableVersions() {
        ArrayList<ProtocolVersion> availableVersions = new ArrayList<>();

        for (ProtocolInfo version : PROTOCOL_INFOS) {
            availableVersions.add(version.getProtocolVersion());
        }

        return availableVersions;
    }

    private ProtocolVersion getVersion(int index) {
        List<ProtocolVersion> availableVersions = getAvailableVersions();
        if (index < 0 || index >= availableVersions.size()) {
            return ProtocolInfo.R1_16_4.getProtocolVersion(); // Fallback version
        }
        return availableVersions.get(index);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
