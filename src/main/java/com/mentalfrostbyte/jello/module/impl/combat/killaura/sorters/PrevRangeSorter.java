package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;


import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.InteractAutoBlock;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public record PrevRangeSorter(InteractAutoBlock interactAB) implements Comparator<TimedEntity> {

    public int compare(TimedEntity a, TimedEntity b) {
        Entity eA = a.getEntity();
        Entity eB = b.getEntity();
        Object var7 = KillAura.targetData != null && KillAura.targetData.getEntity() != null
                ? KillAura.targetData.getEntity()
                : this.interactAB.mc.player;
        assert var7 != null;
        float distA = ((Entity)var7).getDistance(eA);
        float distB = ((Entity)var7).getDistance(eB);
        if (!(distA - distB < 0.0F)) {
            return distA - distB != 0.0F ? 1 : 0;
        } else {
            return -1;
        }
    }
}
