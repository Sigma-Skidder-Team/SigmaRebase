package com.mentalfrostbyte.jello.util.unmapped;

public class PlacementPattern {
    public int offsetX;
    public int offsetY;
    public int offsetZ;
    public boolean isOffset;

    public PlacementPattern(int offsetX, int offsetY, int offsetZ, boolean isOffset) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.isOffset = isOffset;
    }
}