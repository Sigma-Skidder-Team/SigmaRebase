package com.mentalfrostbyte.jello.misc;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public class Class3280 extends Item {
    private static String[] field18787;
    private final Block field18788;

    public Class3280(Block var1, Properties var2) {
        super(var2);
        this.field18788 = var1;
    }

    @Override
    public String getTranslationKey() {
        return this.field18788.getTranslationKey();
    }
}

//    @Override
//    public void method11730(ItemStack var1, World var2, List<ITextComponent> var3, Class2216 var4) {
//        super.addInformation(var1, var2, var3, var4);
//        this.field18788.addInformation(var1, var2, var3, var4);
//    }
//}
