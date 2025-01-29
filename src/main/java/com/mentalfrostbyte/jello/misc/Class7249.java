package com.mentalfrostbyte.jello.misc;

import com.mentalfrostbyte.jello.managers.util.combat.BotRecognitionTechnique;
import com.mentalfrostbyte.jello.managers.util.combat.IBotDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.UUID;

public abstract class Class7249 implements IBotDetector {
    public static final MinecraftClient field31119 = MinecraftClient.getInstance();
    private boolean field31120 = true;
    public String field31121;
    public String field31122;
    public BotRecognitionTechnique field31123;

    public Class7249(String var1, String var2, BotRecognitionTechnique var3) {
        this.field31121 = var1;
        this.field31122 = var2;
        this.field31123 = var3;
        // Client.getInstance().getEventManager().register(this);
    }

    public String method22759() {
        return this.field31121;
    }

    public String method22760() {
        return this.field31122;
    }

    public void method22761() {
    }

    public void method22762() {
    }

    public void method22763(boolean var1) {
        this.field31120 = var1;
        if (!var1) {
            this.method22762();
        } else {
            this.method22761();
        }
    }

    public boolean method22764() {
        return this.field31120;
    }

    public List<AbstractClientPlayerEntity> method22765() {
        return field31119.world.getPlayers();
    }

    public List<AbstractClientPlayerEntity> method22766() {
        return field31119.world.getPlayers();
    }

    public PlayerEntity method22767(String var1) {
        for (PlayerEntity var5 : this.method22766()) {
            if (var5.getName().equals(var1)) {
                return var5;
            }
        }

        return null;
    }

    public PlayerEntity method22768(UUID var1) {
        for (PlayerEntity var5 : this.method22766()) {
            if (var5.getUniqueID().equals(var1)) {
                return var5;
            }
        }

        return null;
    }
}
