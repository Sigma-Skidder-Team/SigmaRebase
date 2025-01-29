package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;

import com.mentalfrostbyte.jello.module.impl.combat.killaura.InteractAutoBlock;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public record HealthSorter(InteractAutoBlock interactAB) implements Comparator<TimedEntity> {
    public int compare(TimedEntity var1, TimedEntity var2) {
        Entity var5 = var1.getEntity();
        Entity var6 = var2.getEntity();
        float var7 = ((LivingEntity)var5).getHealth();
        float var8 = ((LivingEntity)var6).getHealth();
        if (!(var7 - var8 < 0.0F)) {
            if (var7 - var8 != 0.0F) {
                return 1;
            } else {
                assert this.interactAB.mc.player != null;
                float var9 = this.interactAB.mc.player.getDistance(var5);
                float var10 = this.interactAB.mc.player.getDistance(var6);
                if (!(var9 - var10 < 0.0F)) {
                    return var9 - var10 != 0.0F ? 1 : 0;
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
    }
}