package com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters;

import com.mentalfrostbyte.jello.module.impl.combat.killaura.AutoBlockController;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import net.minecraft.entity.Entity;

import java.util.Comparator;

public record AngleSorter(AutoBlockController autoBlockController) implements Comparator<TimedEntity> {
    public int compare(TimedEntity firstTimedEntity, TimedEntity secondTimedEntity) {
        Entity firstEntity = firstTimedEntity.getEntity();
        Entity secondEntity = secondTimedEntity.getEntity();

        assert this.autoBlockController.mc.player != null;

        float playerYaw = this.autoBlockController.mc.player.rotationYaw;

        float firstAngleDiff = RotationUtil.getWrappedAngleDifference(
                RotationUtil.getRotationsToEntity(firstEntity).yaw,
                playerYaw
        );

        float secondAngleDiff = RotationUtil.getWrappedAngleDifference(
                RotationUtil.getRotationsToEntity(secondEntity).yaw,
                playerYaw
        );

        int angleComparison = Float.compare(firstAngleDiff, secondAngleDiff);
        if (angleComparison != 0) {
            return angleComparison;
        }

        float firstDistance = this.autoBlockController.mc.player.getDistance(firstEntity);
        float secondDistance = this.autoBlockController.mc.player.getDistance(secondEntity);

        return Float.compare(firstDistance, secondDistance);
    }

}
