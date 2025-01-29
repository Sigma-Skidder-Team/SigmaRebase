package com.mentalfrostbyte.jello.module.impl.combat.infiniteaura;

import com.mentalfrostbyte.jello.module.impl.combat.InfiniteAura;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public class Sorter implements Comparator<TimedEntity> {
    public final InfiniteAura instance;

    public Sorter(InfiniteAura instance) {
        this.instance = instance;
    }

    public int compare(TimedEntity a, TimedEntity b) {
        Entity eA = a.getEntity();
        Entity eB = b.getEntity();
        float dA = InfiniteAura.getMinecraft().player.getDistance(eA);
        float dB = InfiniteAura.getMinecraft().player.getDistance(eB);
        if (!(dA - dB < 0.0F)) {
            return dA - dB != 0.0F ? 1 : 0;
        } else {
            return -1;
        }
    }
}