package com.mentalfrostbyte.jello.gui.impl.jello.viamcp;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.protocolinfo.ProtocolInfo;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

import static de.florianmichael.viamcp.protocolinfo.ProtocolInfo.PROTOCOL_INFOS;

public class JelloPortalScreen extends MultiplayerScreen {

    private Widget versionSelectorWidget;

    public JelloPortalScreen(Screen parentScreen) {
        super(parentScreen);
    }

    @Override
    public void init() {
        super.init();
        // Create the slider
        SliderPercentageOption versionSelector = new SliderPercentageOption(
                "jello.portaloption",
                0.0,
                this.getAvailableVersions().size() - 1,
                1.0F,
                (var1) -> (double) getCurrentVersionIndex(),
                this::onSliderChange,
                (settings, slider) -> new StringTextComponent(getVersion(getCurrentVersionIndex()).getName())
        );
        this.versionSelectorWidget = this.addButton(versionSelector.createWidget(this.minecraft.gameSettings, this.width / 2 + 40, 7, 114));
    }

    private void onSliderChange(GameSettings settings, Double aDouble) {
        int newIndex = aDouble.intValue();
        if (newIndex >= 0 && newIndex < getAvailableVersions().size()) {
            ViaLoadingBase.getInstance().reload(getVersion(newIndex));
            Client.currentVersionIndex = newIndex;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        RenderUtil.drawBlurredBackground(
                0, 0, Minecraft.getInstance().getMainWindow().getWidth(), (int)(30.0 * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / (double) GuiManager.scaleFactor)
        );
        this.renderBackground(matrices);
        RenderUtil.restoreScissor();
        this.versionSelectorWidget.render(matrices, mouseX, mouseY, delta);
        drawString(matrices, this.font, this.getTitle().getString(), this.width / 2 - 146, 13, 16777215);
        minecraft.fontRenderer.drawStringWithShadow(matrices, "Jello Portal:", (float) this.width / 2 - 30, 13, -1);
    }

    private int getCurrentVersionIndex() {
        return Client.currentVersionIndex;
    }

    private List<ProtocolVersion> getAvailableVersions() {
        ArrayList<ProtocolVersion> availableVersions = new ArrayList<>();

        for (ProtocolInfo version : PROTOCOL_INFOS) {
            availableVersions.add(version.protocolVersion());
        }

        return availableVersions;
    }

    private ProtocolVersion getVersion(int index) {
        List<ProtocolVersion> availableVersions = getAvailableVersions();
        if (index < 0 || index >= availableVersions.size()) {
            return ProtocolInfo.R1_16_4.protocolVersion(); // Fallback version
        }
        return availableVersions.get(index);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
