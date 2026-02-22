package com.mentalfrostbyte.jello.module.impl.combat.bowaimbot;

import com.mentalfrostbyte.jello.module.impl.combat.BowAimbot;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public class BowAngleSorter implements Comparator<Entity> {
    public final BowAimbot field19538;

    public BowAngleSorter(BowAimbot var1) {
        this.field19538 = var1;
    }

    public int compare(Entity var1, Entity var2) {
        float var5 = RotationUtil.getWrappedAngleDifference(RotationUtil.getRotationsToEntity(var1).yaw, Minecraft.getInstance().player.rotationYaw);
        float var6 = RotationUtil.getWrappedAngleDifference(RotationUtil.getRotationsToEntity(var2).yaw, Minecraft.getInstance().player.rotationYaw);
        if (!(var5 - var6 < 0.0F)) {
            if (var5 - var6 != 0.0F) {
                return 1;
            } else {
                float var7 = Minecraft.getInstance().player.getDistance(var1);
                float var8 = Minecraft.getInstance().player.getDistance(var2);
                if (!(var7 - var8 < 0.0F)) {
                    return var7 - var8 != 0.0F ? 1 : 0;
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
    }
}
