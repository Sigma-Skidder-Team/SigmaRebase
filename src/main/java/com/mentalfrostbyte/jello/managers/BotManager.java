package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.managers.data.Manager;
import com.mentalfrostbyte.jello.managers.util.combat.AntiBotBase;
import com.mentalfrostbyte.jello.util.game.world.EntityUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotManager extends Manager {
    public AntiBotBase antiBot;
    public List<Entity> bots = new CopyOnWriteArrayList<>();

    public boolean isBot(Entity entity) {
        return this.bots.contains(entity);
    }

    @EventTarget
    @HighestPriority
    public void onLoadWorld(EventLoadWorld event) {
        this.bots.clear();
    }

    @EventTarget
    @HighestPriority
    public void onPlayerTick(EventUpdate event) {
        if (this.antiBot != null) {
            for (PlayerEntity entity : EntityUtil.getPlayerEntities()) {
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
