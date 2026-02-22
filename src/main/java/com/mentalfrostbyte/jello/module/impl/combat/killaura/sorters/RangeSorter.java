package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;


import com.mentalfrostbyte.jello.module.impl.combat.killaura.AutoBlockController;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public record RangeSorter(AutoBlockController interactAB) implements Comparator<TimedEntity> {

    public int compare(TimedEntity var1, TimedEntity var2) {
        Entity var5 = var1.getEntity();
        Entity var6 = var2.getEntity();
        assert this.interactAB.mc.player != null;
        float var7 = this.interactAB.mc.player.getDistance(var5);
        float var8 = this.interactAB.mc.player.getDistance(var6);
        if (!(var7 - var8 < 0.0F)) {
            return var7 - var8 != 0.0F ? 1 : 0;
        } else {
            return -1;
        }
    }
}