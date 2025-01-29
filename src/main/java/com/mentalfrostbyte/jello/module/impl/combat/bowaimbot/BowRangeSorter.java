package com.mentalfrostbyte.jello.module.impl.combat.bowaimbot;

import com.mentalfrostbyte.jello.module.impl.combat.BowAimbot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public class BowRangeSorter implements Comparator<Entity> {
    public final BowAimbot bowAimbot;

    public BowRangeSorter(BowAimbot var1) {
        this.bowAimbot = var1;
    }

    public int compare(Entity var1, Entity var2) {
        return !(MinecraftClient.getInstance().player.getDistance(var1) > MinecraftClient.getInstance().player
                .getDistance(var2)) ? -1 : 1;
    }
}
