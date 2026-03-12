package com.mentalfrostbyte.jello.module.impl.combat.antibot;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.managers.util.combat.AntiBotBase;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SEntityPacket;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.HashMap;

public class MovementAntiBot extends AntiBotBase {
    public HashMap<Entity, Integer> field31116 = new HashMap<>();
    public HashMap<Entity, ArrayList<Integer>> field31117 = new HashMap<>();
    public int field31118 = 30;

    public MovementAntiBot() {
        super("Movement", "Detects bots based on movement patterns");
    }

    @Override
    public boolean isBot(Entity entity) {
        return this.field31116.getOrDefault(entity, 0) < this.field31118;
    }

    @Override
    public boolean isNotBot(Entity entity) {
        return this.field31116.getOrDefault(entity, 0) >= this.field31118;
    }

    @EventTarget
    public void onTick(EventUpdate event) {
        if (mc.player.ticksExisted < 10) {
            this.field31116.clear();
        }

        for (PlayerEntity player : CombatUtil.getPlayers()) {
            if (player != mc.player) {
                if (player == null
                        || !BlockUtil.isAboveBounds(player, 0.01F)
                        || player.isInvisible()
                        || !(player.getDistance(mc.player) > 5.0F)
                        && (player.getPosX() != player.lastTickPosX || player.getPosZ() != player.lastTickPosZ || player.getPosY() != player.lastTickPosY)) {
                    if (this.field31116.getOrDefault(player, 0) < this.field31118) {
                        this.field31116.put(player, 0);
                    }
                } else {
                    this.field31116.put(player, this.field31116.getOrDefault(player, 0) + 1);
                }
            }
        }
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (mc.player != null && this.field31116 != null) {
            if (mc.player.ticksExisted < 10) {
                this.field31116.clear();
            }

            if (event.packet instanceof SEntityPacket.RelativeMovePacket relativeMovePacket) {
                if (mc.world == null)
                    return;

                if (!(relativeMovePacket.getEntity(mc.world) instanceof PlayerEntity)) {
                    return;
                }

                Entity entity = relativeMovePacket.getEntity(mc.world);
                boolean aboveBounds = BlockUtil.isAboveBounds(entity, 0.5F);
                byte pitch = relativeMovePacket.getPitch();
                if (!this.field31117.containsKey(entity)) {
                    this.field31117.put(entity, new ArrayList<>());
                }

                ArrayList<Integer> var8 = this.field31117.get(entity);
                if (aboveBounds) {
                    if (!var8.isEmpty()) {
                        int var9 = this.method22754(var8);
                        int var10 = this.method22755(var8);
                        if (var10 > 0 && var10 < 1300 && var9 < 3500 && var9 > 2900) {
                            this.field31116.put(entity, this.field31116.getOrDefault(entity, 0) + this.field31118);
                        }
                    }

                    var8.clear();
                    this.field31117.put(entity, var8);
                }

                if (!var8.isEmpty() && var8.get(var8.size() - 1) < 0 && pitch > 0) {
                    var8.clear();
                }

                if (this.method22754(var8) > 3600) {
                    var8.clear();
                }

                if ((var8.isEmpty() || !aboveBounds) && pitch != 0) {
                    var8.add((int) pitch);
                }

                this.field31117.put(entity, var8);
            }
        }
    }

    public int method22754(ArrayList<Integer> var1) {
        int var4 = 0;

        for (int var6 : var1) {
            var4 += Math.max(var6, 0);
        }

        return var4;
    }

    public int method22755(ArrayList<Integer> var1) {
        int var4 = 0;

        for (int var6 : var1) {
            var4 += var6;
        }

        return var4;
    }
}