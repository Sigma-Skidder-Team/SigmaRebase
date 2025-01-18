package com.mentalfrostbyte.misc;

import net.minecraft.entity.ai.pathing.NavigationType;

// $VF: synthetic class
public class Class9444 {
    private static String[] field43873;
    public static final int[] field43874 = new int[NavigationType.values().length];

    static {
        try {
            field43874[NavigationType.LAND.ordinal()] = 1;
        } catch (NoSuchFieldError var5) {
        }

        try {
            field43874[NavigationType.WATER.ordinal()] = 2;
        } catch (NoSuchFieldError var4) {
        }

        try {
            field43874[NavigationType.AIR.ordinal()] = 3;
        } catch (NoSuchFieldError var3) {
        }
    }
}
