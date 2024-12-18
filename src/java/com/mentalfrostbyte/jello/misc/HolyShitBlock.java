package com.mentalfrostbyte.jello.misc;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class HolyShitBlock extends Block {

    public HolyShitBlock(Properties var1) {
        super(var1);
    }

    public int method12068(Random var1) {
        if (this != Blocks.COAL_ORE) {
            if (this != Blocks.DIAMOND_BLOCK) {
                if (this != Blocks.EMERALD_ORE) {
                    if (this != Blocks.LAPIS_ORE) {
                        if (this != Blocks.NETHER_QUARTZ_ORE) {
                            return this != Blocks.NETHER_GOLD_ORE ? 0 : MathHelper.nextInt(var1, 0, 1);
                        } else {
                            return MathHelper.nextInt(var1, 2, 5);
                        }
                    } else {
                        return MathHelper.nextInt(var1, 2, 5);
                    }
                } else {
                    return MathHelper.nextInt(var1, 3, 7);
                }
            } else {
                return MathHelper.nextInt(var1, 3, 7);
            }
        } else {
            return MathHelper.nextInt(var1, 0, 2);
        }
    }

    @Override
    public void spawnAdditionalDrops(BlockState var1, ServerWorld var2, BlockPos var3, ItemStack var4) {
        super.spawnAdditionalDrops(var1, var2, var3, var4);
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, var4) == 0) {
            int var7 = this.method12068(var2.rand);
            if (var7 > 0) {
//                wtf??? lol
                this.dropXpOnBlockBreak(var2, var3, var7);
            }
        }
    }
}