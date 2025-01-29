package com.mentalfrostbyte.jello.managers.util.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import team.sdhq.eventBus.EventBus;

import java.util.List;
import java.util.UUID;

public abstract class AntiBotBase implements IBotDetector {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean field31120 = true;
    public String name;
    public String description;
    public BotRecognitionTechnique technique;

    public AntiBotBase(String name, String description, BotRecognitionTechnique technique) {
        this.name = name;
        this.description = description;
        this.technique = technique;
        EventBus.register(this);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
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

    public List<AbstractClientPlayerEntity> getPlayers() {
        return mc.world.getPlayers();
    }

    public List<AbstractClientPlayerEntity> method22766() {
        return mc.world.getPlayers();
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
