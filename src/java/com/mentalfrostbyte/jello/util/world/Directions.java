package com.mentalfrostbyte.jello.util.world;


import net.minecraft.util.Direction;

// $VF: synthetic class
public class Directions {
    private static String[] field21589;
    public static final int[] directions = new int[Direction.values().length];

    static {
        try {
            directions[Direction.EAST.ordinal()] = 1;
        } catch (NoSuchFieldError var8) {
        }

        try {
            directions[Direction.NORTH.ordinal()] = 2;
        } catch (NoSuchFieldError var7) {
        }

        try {
            directions[Direction.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError var6) {
        }

        try {
            directions[Direction.WEST.ordinal()] = 4;
        } catch (NoSuchFieldError var5) {
        }

        try {
            directions[Direction.UP.ordinal()] = 5;
        } catch (NoSuchFieldError var4) {
        }

        try {
            directions[Direction.DOWN.ordinal()] = 6;
        } catch (NoSuchFieldError var3) {
        }
    }
}