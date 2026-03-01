package com.mentalfrostbyte.jello.gui.base.elements.impl;

import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.TextField;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.BlockButton;
import com.mentalfrostbyte.jello.gui.base.interfaces.Class7261;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Picker extends Element {
    private final List<String> field20641 = new ArrayList<String>();
    private ScrollableContentPanel field20642;
    private final TextField field20643;
    private final boolean field20644;

    public Picker(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, boolean var7, String... var8) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field20644 = var7;
        this.addToList(this.field20643 = new TextField(this, "textbox", 0, 0, var5, 32, TextField.field20741, "", "Search...", ResourceRegistry.JelloLightFont14));
        this.field20643.setFont(ResourceRegistry.JelloLightFont18);
        this.field20643.addChangeListener(var1x -> this.method13069(this.field20643.getText()));
        this.method13071(var8);
        this.method13069("");
    }

    public void method13069(String var1) {
        this.runThisOnDimensionUpdate(() -> {
            if (this.field20642 != null) {
                this.removeChildren(this.field20642);
            }

            this.addToList(this.field20642 = new ScrollableContentPanel(this, "scrollview", 0, 40, this.widthA, this.heightA - 40));
            ArrayList<Item> var5 = new ArrayList<>();

            for (Item var7 : Registry.ITEM) {
                var5.add(var7);
            }

            var5.add(new BlockItem(Blocks.NETHER_PORTAL, new Item.Properties().group(ItemGroup.MISC)));
            var5.add(new BlockItem(Blocks.END_PORTAL, new Item.Properties().group(ItemGroup.MISC)));

            for (Item var13 : method13070(var5, var1)) {
                if (var13 != Items.AIR && (!this.field20644 || var13 instanceof BlockItem)) {
                    ResourceLocation var8 = Registry.ITEM.getKey(var13);
                    String var9;
                    if (var13 instanceof BlockItem && var8.getPath().equals("air")) {
                        var9 = Registry.BLOCK.getKey(((BlockItem) var13).getBlock()).toString();
                    } else {
                        var9 = var8.toString();
                    }

                    BlockButton var10;
                    this.field20642.addToList(var10 = new BlockButton(this, "btn" + var9, 0, 0, 40, 40, var13.getDefaultInstance()));
                    var10.method13702(this.field20641.contains(var9), false);
                    var10.onPress(var3 -> {
                        int var6 = this.field20641.size();
                        this.field20641.remove(var9);
                        if (var10.method13700()) {
                            this.field20641.add(var9);
                        }

                        if (var6 != this.field20641.size()) {
                            this.callUIHandlers();
                        }
                    });
                }
            }

            this.field20642.getButtonList().method13246(new Class7260(0));
        });
    }

    public static List<Item> method13070(List<Item> var0, String var1) {
        var1 = var1.toLowerCase();
        if (!var1.isEmpty()) {
            ArrayList<Item> var4 = new ArrayList<>();
            Iterator<Item> var5 = var0.iterator();

            while (var5.hasNext()) {
                Item var6 = var5.next();
                if (var6.getName().getString().toLowerCase().startsWith(var1.toLowerCase())) {
                    var4.add(var6);
                    var5.remove();
                }
            }

            Iterator<Item> var9 = var0.iterator();

            while (var9.hasNext()) {
                Item var7 = var9.next();
                if (var7.getName().getString().toLowerCase().contains(var1.toLowerCase())) {
                    var4.add(var7);
                    var9.remove();
                }
            }

            var4.addAll(var0);
            return var4;
        } else {
            return var0;
        }
    }

    @Override
    public void draw(float partialTicks) {
        super.draw(partialTicks);
    }

    public void method13071(String... var1) {
        this.field20641.clear();
        this.field20641.addAll(Arrays.asList(var1));
    }

    public List<String> method13072() {
        return this.field20641;
    }

    public static class Class7260 implements Class7261 {
        private static String[] field31148;
        public int field31149;

        public Class7260(int var1) {
            this.field31149 = var1;
        }

        @Override
        public void method22796(CustomGuiScreen var1) {
            if (var1.getChildren().size() > 0) {
                int var4 = 0;
                int var5 = 0;
                int var6 = 0;

                for (int var7 = 0; var7 < var1.getChildren().size(); var7++) {
                    CustomGuiScreen var8 = var1.getChildren().get(var7);
                    if (var4 + var8.getWidthA() + this.field31149 > var1.getWidthA()) {
                        var4 = 0;
                        var5 += var6;
                    }

                    var8.setYA(var5);
                    var8.setXA(var4);
                    var4 += var8.getWidthA() + this.field31149;
                    var6 = Math.max(var8.getHeightA(), var6);
                }
            }
        }
    }
}
