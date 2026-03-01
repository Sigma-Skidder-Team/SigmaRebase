package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.panels;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.dropdown.Class7262;
import com.mentalfrostbyte.jello.gui.base.interfaces.Class4342;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.groups.PanelGroup;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ModListView extends ScrollableContentPanel implements Class4342 {
    public final ModuleCategory field21214;
    private final List<Button> field21215 = new ArrayList<>();
    private final boolean field21217;
    private float field21218 = 1.0F;

    public ModListView(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, ModuleCategory var7) {
        super(var1, var2, var3, var4, var5, var6);
        this.field21214 = var7;
        ((PanelGroup) var1).field21195 = 1.0F;
        this.field21217 = true;
        this.setListening(false);
        this.method13511();
    }

    public void method13511() {
        int var3 = 0;

        for (Module var5 : Client.getInstance().moduleManager.getModulesByCategory(this.field21214)) {
            int var9 = RenderUtil2.applyAlpha(-3487030, 0.0F);
            ColorHelper var12 = new ColorHelper(!var5.isEnabled() ? 1895167477 : -14047489, !var5.isEnabled() ? var9 : -14042881)
                    .setTextColor(!var5.isEnabled() ? ClientColors.DEEP_TEAL.getColor() : ClientColors.LIGHT_GREYISH_BLUE.getColor());
            var12.method19412(FontSizeAdjust.field14488);
            Button var13;
            this.getButtonList()
                    .addToList(
                            var13 = new Button(
                                    this.getButtonList(), var5.getName() + "Button", 0, var3 * 30, this.getWidthA(), 30, var12, var5.getName(), ResourceRegistry.JelloLightFont20
                            )
                    );
            if (!var5.isEnabled()) {
                var13.method13034(22);
            } else {
                var13.method13034(30);
            }

            this.field21215.add(var13);
            var13.onClick(
                    (var3x, var4) -> {
                        Button var7 = (Button) var3x;
                        if (var4 != 0) {
                            if (var4 == 1) {
                                PanelGroup var8 = (PanelGroup) this.getParent();
                                var8.method13508(var5);
                            }
                        } else {
                            var5.toggle();
                            ColorHelper var9x = new ColorHelper(!var5.isEnabled() ? 1895167477 : -14047489, !var5.isEnabled() ? var9 : -14042881)
                                    .setTextColor(!var5.isEnabled() ? ClientColors.DEEP_TEAL.getColor() : ClientColors.LIGHT_GREYISH_BLUE.getColor());
                            if (!var5.isEnabled()) {
                                var7.method13034(22);
                            } else {
                                var7.method13034(30);
                            }

                            var9x.method19412(FontSizeAdjust.field14488);
                            var7.setTextColor(var9x);
                        }
                    }
            );
            var13.setSize(new ModListViewSize());
            var3++;
        }

        this.getButtonList().method13246(new Class7262(1));
    }

    private float method13523() {
        return this.field21218 * this.field21218 * (3.0F - 2.0F * this.field21218);
    }

    private float method13524(float var1, float var2, float var3, float var4) {
        var1 /= var4 / 2.0F;
        if (!(var1 < 1.0F)) {
            var1--;
            return -var3 / 2.0F * (var1 * (var1 - 2.0F) - 1.0F) + var2;
        } else {
            return var3 / 2.0F * var1 * var1 + var2;
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        PanelGroup var5 = (PanelGroup) this.parent;
        float var6 = (float) (0.07F * (60.0 / (double) Minecraft.getFps()));
        this.field21218 = this.field21218 + (!this.method13525() ? 0.0F : (!this.field21217 ? var6 : -var6));
        this.field21218 = Math.max(0.0F, Math.min(1.0F, this.field21218));
        var5.field21195 = this.method13524(this.field21218, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        this.method13225();
        super.draw(partialTicks * ((PanelGroup) this.parent).field21195);
    }

    @Override
    public boolean method13525() {
        return false;
    }

    public int method13529(Module var1) {
        int var4 = 0;

        for (Button var6 : this.field21215) {
            var4++;
            if (var6.getName().equals(var1.getName() + "Button")) {
                break;
            }
        }

        return var4 * 30;
    }
}
