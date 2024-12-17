package com.mentalfrostbyte.jello.misc;

import com.mentalfrostbyte.jello.util.world.BlockUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;

public final class PlayerEntityDistanceComparator implements Comparator<PlayerEntity> {
    public int compare(PlayerEntity a, PlayerEntity b) {
        float distanceA = BlockUtil.getMinecraft().player.getDistance(a);
        float distanceB = BlockUtil.getMinecraft().player.getDistance(b);
        if (!(distanceA - distanceB < 0.0F)) {
            return distanceA - distanceB != 0.0F ? -1 : 0;
        } else {
            return 1;
        }
    }
}
