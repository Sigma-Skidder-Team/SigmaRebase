package com.mentalfrostbyte.jello.event.impl.player;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public class EventHandAnimation extends CancellableEvent {
    private final boolean field21504;
    private boolean blocking;
    private final float field21506;
    private final float field21507;
    private final Arm hand;
    private final ItemStack itemStack;
    private final MatrixStack matrixStack;

    public EventHandAnimation(boolean var1, float var2, float var3, Arm var4, ItemStack var5, MatrixStack var6) {
        this.field21504 = var1;
        this.field21506 = var2;
        this.field21507 = var3;
        this.hand = var4;
        this.itemStack = var5;
        this.matrixStack = var6;
        this.blocking = true;
    }

    public float method13924() {
        return this.field21506;
    }

    public float method13925() {
        return this.field21507;
    }

    public boolean method13926() {
        return this.field21504;
    }

    public Arm getHand() {
        return this.hand;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public MatrixStack getMatrix() {
        return this.matrixStack;
    }

    public boolean isBlocking() {
        return this.blocking;
    }

    public void renderBlocking(boolean blocking) {
        this.blocking = blocking;
    }
}
