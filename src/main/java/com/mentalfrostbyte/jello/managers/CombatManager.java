package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.event.impl.WorldLoadEvent;
import com.mentalfrostbyte.jello.managers.impl.combat.Class7249;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CombatManager {
    private static String[] field35945;
    public Class7249 antiBot;
    public List<Entity> bots = new CopyOnWriteArrayList<Entity>();

    public void init() {
        EventBus.register(this);
    }

    public boolean isTargetABot(Entity var1) {
        return this.bots.contains(var1);
    }

    public void method29347() {
        this.bots.clear();
    }

    @EventTarget
    @HighestPriority
    public void method29348(WorldLoadEvent var1) {
        this.bots.clear();
    }

    @EventTarget
    @HighestPriority
    public void method29349(TickEvent var1) {
        if (this.antiBot != null) {
            for (PlayerEntity var5 : ColorUtils.method17680()) {
                if (!this.antiBot.method22751(var5)) {
                    if (this.antiBot.method22758(var5)) {
                        this.bots.remove(var5);
                    }
                } else if (!this.bots.contains(var5)) {
                    this.bots.add(var5);
                }
            }
        }
    }
}
