package com.mentalfrostbyte.jello.managers.util.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import team.sdhq.eventBus.EventBus;

import java.util.List;

public abstract class AntiBotBase implements IBotDetector {
    protected static final Minecraft mc = Minecraft.getInstance();

    protected String name;
    protected String description;

    public AntiBotBase(String name, String description) {
        this.name = name;
        this.description = description;
        EventBus.register(this);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<AbstractClientPlayerEntity> getPlayers() {
        return mc.world.getPlayers();
    }
}