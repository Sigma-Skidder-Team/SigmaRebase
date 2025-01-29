package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.InteractAutoBlock;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public record FriendSorter(InteractAutoBlock interactAB) implements Comparator<TimedEntity> {

    public int compare(TimedEntity sortingEntity1, TimedEntity sortingEntity2) {
        Entity entity1 = sortingEntity1.getEntity();
        Entity entity2 = sortingEntity2.getEntity();
        boolean friended1 = Client.getInstance().friendManager.isFriend(entity1);
        boolean friended2 = Client.getInstance().friendManager.isFriend(entity2);
        if (friended1 && !friended2) {
            return -1;
        } else {
            return friended1 && friended2 ? 0 : 1;
        }
    }
}
