package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;


import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.AutoBlockController;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public record PrevRangeSorter(AutoBlockController interactAB) implements Comparator<TimedEntity> {

    public int compare(TimedEntity a, TimedEntity b) {
        Entity first = a.getEntity();
        Entity second = b.getEntity();
        Object auraTarget = KillAura.targetData != null && KillAura.targetData.getEntity() != null
                ? KillAura.targetData.getEntity()
                : this.interactAB.mc.player;
        assert auraTarget != null;
        float distA = ((Entity)auraTarget).getDistance(first);
        float distB = ((Entity)auraTarget).getDistance(second);
        if (!(distA - distB < 0.0F)) {
            return distA - distB != 0.0F ? 1 : 0;
        } else {
            return -1;
        }
    }
}
