package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;


import com.mentalfrostbyte.jello.module.impl.combat.killaura.AutoBlockController;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;

public record ArmorSorter(AutoBlockController interactAB) implements Comparator<TimedEntity> {
    public int compare(TimedEntity var1, TimedEntity var2) {
        Entity var5 = var1.getEntity();
        Entity var6 = var2.getEntity();
        int var7 = !(var5 instanceof PlayerEntity) ? 0 : InvManagerUtil.getTotalArmorProtection((PlayerEntity)var5);
        int var8 = !(var6 instanceof PlayerEntity) ? 0 : InvManagerUtil.getTotalArmorProtection((PlayerEntity)var6);
        if (var7 - var8 >= 0) {
            if (var7 - var8 != 0) {
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
