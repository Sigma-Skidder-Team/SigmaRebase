package com.mentalfrostbyte.jello.misc;

import net.minecraft.util.math.vector.Vector2f;

public class MovementInput {
    public float moveStrafe; //moveStrafe
    public float moveForward; //moveForward
    public boolean field43909;
    public boolean field43910;
    public boolean field43911;
    public boolean field43912;
    public boolean field43913;
    public boolean field43914;

    public void method36336(boolean var1) {
    }

    public Vector2f method36337() {
        return new Vector2f(this.field43907, this.field43908);
    }

    public boolean method36338() {
        return this.field43908 > 1.0E-5F;
    }
}
