package com.mentalfrostbyte.jello.gui.impl.classic.mainmenu;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.AnimatedIconPanel;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Text;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viamcp.protocolinfo.ProtocolInfo;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClassicMainScreen extends Screen {
    public final AnimatedIconPanel field21094;
    public final AnimatedIconPanel field21095;
    private final Animation field21100;
    private final Animation field21101 = new Animation(800, 800);
    private final ParticleOverlay particleOverlay;
    private final ClassicMainScreenGroup field21103;
    private float field21104;
    private float field21105;

    public ClassicMainScreen() {
        super("Main Screen");
        this.setListening(false);
        this.field21100 = new Animation(175, 325);
        this.field21100.changeDirection(Animation.Direction.FORWARDS);
        this.field21101.changeDirection(Animation.Direction.BACKWARDS);
        TrueTypeFont font = Resources.regular20;
        String copyrightTag = "© Sigma Prod";
        StringBuilder versionBuilder = new StringBuilder().append("Sigma ");
        Client.getInstance();
        List<ProtocolVersion> sorted = ProtocolInfo.
                PROTOCOL_INFOS
                .stream()
                .sorted(Comparator.comparing(ProtocolInfo::protocolVersion))
                .map(ProtocolInfo::protocolVersion)
                .toList();
        String oldestVersion = sorted.get(0).getIncludedVersions().stream().toList().get(0);
        List<String> newestIncludedVersions = sorted.get(sorted.size() - 1).getIncludedVersions().stream().toList();
        String newestVersion = newestIncludedVersions.get(newestIncludedVersions.size() - 1);
        String versionTag = versionBuilder.append(Client.FULL_VERSION).append(" for Minecraft ").append(oldestVersion).append(" to ").append(newestVersion).toString();
        this.addToList(this.particleOverlay = new ParticleOverlay(this, "particles"));
        int var13 = 480;
        int var14 = 480;
        this.addToList(this.field21103 = new ClassicMainScreenGroup(this, "group", (this.getWidthA() - var13) / 2, this.getHeightA() / 2 - 230, var13, var14));
        this.addToList(
                this.field21095 = new Text(
                        this, "Copyright", 10, 8, font.getWidth(copyrightTag), 140, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), copyrightTag, ResourceRegistry.JelloLightFont18
                )
        );
        ColorHelper var15 = new ColorHelper(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F));
        var15.setTextColor(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F));
        ArrayList<String> var16 = new ArrayList<>();
        var16.add("LeakedPvP");
        var16.add("Omikron");
        Collections.shuffle(var16);
        String var17 = "by " + var16.get(0) + ", " + var16.get(1);
        this.addToList(new Text(this, "names", 130, 9, font.getWidth(copyrightTag), 140, var15, var17, Resources.regular17));
        this.addToList(
                this.field21094 = new Text(
                        this,
                        "Version",
                        this.getWidthA() - font.getWidth(versionTag) - 9,
                        this.getHeightA() - 31,
                        114,
                        140,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()),
                        versionTag,
                        font
                )
        );
        this.addToList(new Text(this, "Hello", 10, this.getHeightA() - 55, 114, 140, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), "Hello,", font));
        this.addToList(
                new Text(
                        this, "Latest", 10, this.getHeightA() - 31, 114, 140, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), "You are using the latest version", font
                )
        );
        this.field21104 = (float) (this.getWidthA() / 2);
        this.field21105 = (float) (this.getHeightA() / 2);
    }

    private int method13432(int var1) {
        int var6 = 4;
        int var7 = -6;
        int var8 = 122 * var6 + var6 * var7;
        if (var1 < var6) {
            return this.getWidthA() / 2 - var8 / 2 + var1 * 122 + var1 * var7;
        } else {
            var1 -= var6;
            var6 = 3;
            var7 = 6;
            var8 = 122 * var6 + var6 * var7;
            return this.getWidthA() / 2 - var8 / 2 + var1 * 122 + var1 * var7;
        }
    }

    private int method13433() {
        return this.getHeightA() / 2 - 100;
    }

    public void method13434(net.minecraft.client.gui.screen.Screen var1) {
        Minecraft.getInstance().displayGuiScreen(var1);
        this.method13436();
    }

    public void method13435(Screen var1) {
        Client.getInstance().guiManager.handleScreen(var1);
        this.method13436();
    }

    public void method13436() {
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        float var5 = (float) newHeight - this.field21104;
        float var6 = (float) newWidth - this.field21105;
        this.field21104 += var5 * 0.055F;
        this.field21105 += var6 * 0.055F;
        super.updatePanelDimensions(newHeight, newWidth);
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = Math.round((1.0F - QuadraticEasing.easeOutQuad(this.field21100.calcPercent(), 0.0F, 1.0F, 1.0F)) * 5.0F);
        this.drawBackground(var4);
        this.method13225();
        GL11.glPushMatrix();
        GL11.glTranslated(
                (int) ((float) (-this.getWidthA() / 200) + this.field21104 / 200.0F),
                (int) ((float) (-this.getHeightA() / 100) + this.field21105 / 100.0F) - var4,
                0.0
        );
        RenderUtil.drawImage(-10.0F, -10.0F, (float) (this.getWidthA() + 20), (float) (this.getHeightA() + 20), Resources.mainmenubackground);
        GL11.glPopMatrix();
        this.field21103
                .draw(
                        (int) ((float) (-this.getWidthA() / 40) + this.field21104 / 40.0F), (int) ((float) (-this.getHeightA() / 40) + this.field21105 / 40.0F) + var4
                );
        this.particleOverlay
                .draw((int) ((float) (-this.getWidthA() / 12) + this.field21104 / 12.0F), (int) ((float) (-this.getHeightA() / 12) + this.field21105 / 12.0F));
        super.draw(partialTicks);
    }
}
