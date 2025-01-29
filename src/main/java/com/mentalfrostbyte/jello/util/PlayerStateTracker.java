package com.mentalfrostbyte.jello.util;


import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.network.play.server.SKeepAlivePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.client.Minecraft;
import team.sdhq.eventBus.annotations.EventTarget;

public class PlayerStateTracker {
    private static String[] field39247;
    private int focusGameTicks = 0;
    private int groundTicks = 0;
    private int moveTicks = 0;
    private int mode = 0;
    private boolean alive = false;
    private long lastMilis = System.currentTimeMillis();
    private float ping = 1.0F;
    private final Minecraft mc = Minecraft.getInstance();


    @EventTarget
    public void method31324(EventPlayerTick var1) {
        this.focusGameTicks++;
        this.groundTicks++;
        this.moveTicks++;
        this.mode++;
        if (!this.mc.player.isOnGround()) {
            this.groundTicks = 0;
        }

        if (this.mc.currentScreen != null) {
            this.focusGameTicks = 0;
        }

        if (!MovementUtil.isMoving()) {
            this.moveTicks = 0;
            this.alive = true;
        }

        if (this.mc.player.ticksExisted <= 1) {
            this.alive = false;
        }
    }

    @EventTarget
    public void method31325(EventSendPacket var1) {
        if (var1.getPacket() instanceof CClickWindowPacket) {
            this.mode = 0;
        }
    }

    @EventTarget
    public void method31326(EventReceivePacket var1) {
        if (var1.getPacket() instanceof SKeepAlivePacket) {
            long var4 = System.currentTimeMillis() - this.lastMilis;
            this.ping = Math.min(1.05F, Math.max(0.0F, 15000.0F / (float)var4));
            this.lastMilis = System.currentTimeMillis();
        }
    }

    public float getPing() {
        return this.ping;
    }

    public float getPingAsTicks() {
        return this.getPing() * 20.0F;
    }

    public int getgroundTicks() {
        return this.groundTicks;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public int method31331() {
        return this.groundTicks;
    }

    public int focusGameTicks() {
        return this.focusGameTicks;
    }

    public int getMode() {
        return this.mode;
    }

    public void method31334() {
        this.groundTicks = 0;
        this.focusGameTicks = 0;
    }
}
