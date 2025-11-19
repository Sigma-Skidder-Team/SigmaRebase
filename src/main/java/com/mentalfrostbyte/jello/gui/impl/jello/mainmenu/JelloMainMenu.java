package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.combined.impl.SwitchScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.altmanager.AltManagerScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.viamcp.JelloPortalScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Text;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.TextButton;
import com.mentalfrostbyte.jello.managers.GuiManager;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viamcp.protocolinfo.ProtocolInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

import java.util.Comparator;
import java.util.List;

public class JelloMainMenu extends CustomGuiScreen {
    private final Button singleplayerButton;
    private final Button multiplayerButton;
    private final Button realmsButton;
    private final Button optionsButton;
    private final Button altManagerButton;
    private final Text version;
    private final Text copyright;
    private final TextButton switchButton;
    private final TextButton changelogButton;
    private final TextButton quitButton;
    public int field21134 = 0;

    public JelloMainMenu(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        TrueTypeFont font = ResourceRegistry.JelloLightFont20;
        int var17 = 0;
        String prod = "© Sigma Prod";
        StringBuilder clientInfo = new StringBuilder().append("Jello for Sigma ");
        List<ProtocolVersion> sorted = ProtocolInfo.
                PROTOCOL_INFOS
                .stream()
                .sorted(Comparator.comparing(ProtocolInfo::protocolVersion))
                .map(ProtocolInfo::protocolVersion)
                .toList();
        String oldestVersion = sorted.get(0).getIncludedVersions().stream().toList().get(0);
        List<String> newestIncludedVersions = sorted.get(sorted.size() - 1).getIncludedVersions().stream().toList();
        String newestVersion = newestIncludedVersions.get(newestIncludedVersions.size() - 1);
        this.addToList(
                this.singleplayerButton = new MainMenuButton(
                        this,
                        "Singleplayer",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.singleplayerPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );
        String version = clientInfo.append(Client.FULL_VERSION).append("  -  ").append(oldestVersion).append(" to ").append(newestVersion).toString();
        this.addToList(
                this.multiplayerButton = new MainMenuButton(
                        this,
                        "Multiplayer",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.multiplayerPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );
        this.addToList(
                this.realmsButton = new MainMenuButton(
                        this,
                        "Realms",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.shopPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );
        this.addToList(
                this.optionsButton = new MainMenuButton(
                        this,
                        "Options",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.optionsPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );
        this.addToList(this.altManagerButton = new MainMenuButton(this, "Alt Manager", this.method13447(var17++), this.method13448(), 128, 128, Resources.altPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())));
        this.addToList(this.copyright = new Text(this, "Copyright", 10, this.getHeightA() - 31, font.getWidth(prod), 128, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), prod, font));
        this.addToList(this.version = new Text(this, "Version", this.getWidthA() - font.getWidth(version) - 9, this.getHeightA() - 31, 128, 128, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), version, font));
        this.copyright.shadow = true;
        this.version.shadow = true;
        this.addToList(this.switchButton = new TextButton(this, "switch", 220, 24, 50, 50, new ColorHelper(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F)), "Switch", ResourceRegistry.JelloLightFont20));
        this.addToList(this.changelogButton = new TextButton(this, "changelog", 432, 24, 110, 50, new ColorHelper(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F)), "Changelog", ResourceRegistry.JelloLightFont20));
        this.addToList(this.quitButton = new TextButton(this, "quit", 30, 24, 50, 50, new ColorHelper(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F)), "Exit", ResourceRegistry.JelloLightFont20));
        this.quitButton.onClick((var1x, var2x) -> {
            ((MainMenuScreen) this.getParent()).method13341();
            new Thread(() -> {
                try {
                    Thread.sleep(2000L);
                    Minecraft.getInstance().shutdown();
                } catch (InterruptedException e) {
                    Minecraft.getInstance().shutdown();
                }
            }).start();
        });
        this.changelogButton.onClick((var1x, var2x) -> ((MainMenuScreen) this.getParent()).animateIn());
        this.switchButton.onClick((var1x, var2x) -> this.displayScreen(new SwitchScreen()));
        this.singleplayerButton.onClick((var1x, var2x) -> this.displayGUI(new WorldSelectionScreen(Minecraft.getInstance().currentScreen)));
        this.multiplayerButton.onClick((var1x, var2x) -> this.displayGUI(new JelloPortalScreen(Minecraft.getInstance().currentScreen)));
        this.optionsButton.onClick((var1x, var2x) -> this.displayGUI(new OptionsScreen(Minecraft.getInstance().currentScreen, Minecraft.getInstance().gameSettings)));
        this.altManagerButton.onClick((var1x, var2x) -> this.displayScreen(new AltManagerScreen()));
        this.realmsButton.onClick((var1x, var2x) -> this.method13443());
    }

    public void method13443() {
        this.playClickSound();
    }

    @Override
    public void draw(float partialTicks) {
        this.method13224();
        Texture largeLogo = Resources.logoLargePNG;
        int imageWidth = largeLogo.getImageWidth();
        int imageHeight = largeLogo.getImageHeight();
        if (GuiManager.scaleFactor > 1.0F) {
            largeLogo = Resources.logoLarge2xPNG;
        }

        RenderUtil.drawImage(
                (float) (this.getWidthA() / 2 - imageWidth / 2),
                (float) (this.getHeightA() / 2 - imageHeight),
                (float) imageWidth,
                (float) imageHeight,
                largeLogo,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        super.draw(partialTicks);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        int var5 = 30;
        int var6 = 90;
        this.changelogButton.setXA(var6 + 0);
        this.quitButton.setXA(var5 + 0);
        super.updatePanelDimensions(newHeight, newWidth);
    }

    public void playClickSound() {
        Client.getInstance().soundManager.play("clicksound");
    }

    public void displayGUI(net.minecraft.client.gui.screen.Screen var1) {
        Minecraft.getInstance().displayGuiScreen(var1);
        this.playClickSound();
    }

    public void displayScreen(Screen var1) {
        Client.getInstance().guiManager.handleScreen(var1);
        this.playClickSound();
    }

    private int method13447(int var1) {
        return this.getWidthA() / 2 - 305 + var1 * 128 + var1 * -6;
    }

    private int method13448() {
        return this.getHeightA() / 2 + 14;
    }
}
