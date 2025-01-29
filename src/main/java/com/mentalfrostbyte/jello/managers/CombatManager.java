package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.managers.util.combat.AntiBotBase;
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
    public AntiBotBase antiBot;
    public List<Entity> bots = new CopyOnWriteArrayList<Entity>();

    public void init() {
        EventBus.register(this);
    }

    public boolean isTargetABot(Entity var1) {
        return this.bots.contains(var1);
    }

    public void clearBots() {
        this.bots.clear();
    }

    @EventTarget
    @HighestPriority
    public void onLoadWorld(EventLoadWorld var1) {
        this.bots.clear();
    }

    @EventTarget
    @HighestPriority
    public void onPlayerTick(EventPlayerTick var1) {
        if (this.antiBot != null) {
            for (PlayerEntity entity : ColorUtils.getPlayerEntities()) {
                if (!this.antiBot.isBot(entity)) {
                    if (this.antiBot.isNotBot(entity)) {
                        this.bots.remove(entity);
                    }
                } else if (!this.bots.contains(entity)) {
                    this.bots.add(entity);
                }
            }
        }
    }
}
