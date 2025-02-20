package com.mentalfrostbyte.jello.util.game.player;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.action.EventInputOptions;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;

public class MovementUtil implements MinecraftUtil {

    public static void stop() {
        stop(false);
    }

    public static void stop(boolean stopY) {
        mc.player.setMotion(0, stopY ? 0.0 : mc.player.getMotion().y, 0);
    }

    public static double getSpeed() {
        if (mc.player == null) {
            return 0.0;
        }

        double motionX = mc.player.getMotion().x;
        double motionZ = mc.player.getMotion().z;
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public static void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            yaw += (strafe > 0.0D) ? (forward > 0.0D ? -45 : 45) : (strafe < 0.0D) ? (forward > 0.0D ? 45 : -45) : 0;
            strafe = 0.0D;
            forward = (forward > 0.0D) ? 1.0D : -1.0D;
        }

        if (strafe != 0.0D) {
            strafe = (strafe > 0.0D) ? 1.0D : -1.0D;
        }

        double radianYaw = Math.toRadians(yaw + 90.0F);
        double cosYaw = Math.cos(radianYaw);
        double sinYaw = Math.sin(radianYaw);

        mc.player.setMotion(
                forward * moveSpeed * cosYaw + strafe * moveSpeed * sinYaw,
                mc.player.getMotion().y,
                forward * moveSpeed * sinYaw - strafe * moveSpeed * cosYaw
        );
    }

    public static void setSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.player.rotationYaw, mc.player.movementInput.moveStrafe, mc.player.movementInput.moveForward);
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void strafe(double moveSpeed) {
        if (mc.player.movementInput.moveForward != 0.0) {
            mc.player.movementInput.moveForward = (mc.player.movementInput.moveForward > 0.0) ? 1.0f : -1.0f;
        }

        if (mc.player.movementInput.moveStrafe != 0.0) {
            mc.player.movementInput.moveStrafe = (mc.player.movementInput.moveStrafe > 0.0) ? 1.0f : -1.0f;
        }

        if (mc.player.movementInput.moveForward == 0.0 && mc.player.movementInput.moveStrafe == 0.0) {
            mc.player.setMotion(0.0, mc.player.getMotion().y, 0.0);
            return;
        }

        if (mc.player.movementInput.moveForward != 0.0 && mc.player.movementInput.moveStrafe != 0.0) {
            mc.player.movementInput.moveForward *= (float) Math.sin(Math.toRadians(36.67));
            mc.player.movementInput.moveStrafe *= (float) Math.cos(Math.toRadians(36.67));
        }

        double yawRadians = Math.toRadians(mc.player.rotationYaw);

        mc.player.setMotion(
                mc.player.movementInput.moveForward * moveSpeed * -Math.sin(yawRadians)
                        + mc.player.movementInput.moveStrafe * moveSpeed * Math.cos(yawRadians),
                mc.player.getMotion().y,
                mc.player.movementInput.moveForward * moveSpeed * Math.cos(yawRadians)
                        - mc.player.movementInput.moveStrafe * moveSpeed * -Math.sin(yawRadians
                )
        );
    }

    public static boolean isMoving() {
        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();
        boolean back = mc.gameSettings.keyBindBack.isKeyDown();
        return forward || left || right || back;
    }

    public static float getDirection(float forward, float strafing, float yaw) {
        if (forward == 0.0f && strafing == 0.0f) {
            return yaw;
        }

        boolean isReversed = forward < 0.0f;
        float strafingAdjustment = 90.0f * (isReversed ? -0.5f : (forward > 0.0f ? 0.5f : 1.0f));

        if (isReversed) {
            yaw += 180.0f;
        }

        if (strafing != 0.0f) {
            yaw += (strafing > 0.0f) ? -strafingAdjustment : strafingAdjustment;
        }

        return yaw;
    }

    public static float getDirection() {
        return getDirection(mc.player.moveForward, mc.player.moveStrafing, mc.player.rotationYaw);
    }

    public static int getSpeedBoost() {
        return !mc.player.isPotionActive(Effects.SPEED) ? 0 : mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1;
    }

    public static int getJumpBoost() {
        return !mc.player.isPotionActive(Effects.JUMP_BOOST) ? 0 : mc.player.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1;
    }

    public static double getJumpValue() {
        return 0.42F + (double) getJumpBoost() * 0.1;
    }

    public static float getYaw() {
        float forward = mc.player.moveForward;
        float strafe = mc.player.moveStrafing;
        float movementYaw = mc.player.rotationYaw + 90.0F;

        if (forward > 0.0F && mc.gameSettings.keyBindBack.isKeyDown()) {
            forward = -1.0F;
        }

        if (strafe > 0.0F) {
            movementYaw -= 90.0F;
        } else if (strafe < 0.0F) {
            movementYaw += 90.0F;
        }

        if (forward != 0.0F) {
            if (strafe > 0.0F) {
                movementYaw -= (forward > 0.0F) ? -45 : 45;
            } else if (strafe < 0.0F) {
                movementYaw -= (forward > 0.0F) ? 45 : -45;
            }
        }

        if (forward < 0.0F && strafe == 0.0F) {
            movementYaw -= 180.0F;
        }

        return movementYaw;
    }

    public static void movePlayerInDirection(double speed) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;

        float yaw = mc.player.rotationYaw;

        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (forward > 0.0) ? -45 : 45;
            } else if (strafe < 0.0) {
                yaw += (forward > 0.0) ? 45 : -45;
            }

            strafe = 0.0;
            forward = (forward > 0.0) ? 1.0 : -1.0;
        }

        double posX = mc.player.getPosX();
        double posY = mc.player.getPosY();
        double posZ = mc.player.getPosZ();

        double cos = Math.cos(Math.toRadians(yaw + 90.0F));
        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        double moveX = forward * speed * cos +
                strafe * speed * sin;
        double moveZ = forward * speed * sin -
                strafe * speed * cos;

        mc.player.setPosition(posX + moveX, posY, posZ + moveZ);
    }

    /**
     * Sets the player's movement speed for a given event.
     *
     * @param moveEvent   The movement event to modify.
     * @param motionSpeed The desired motion speed.
     */
    public static void setMotion(EventMove moveEvent, double motionSpeed) {
        float[] strafe = getDirectionArray();
        float forward = strafe[1];
        float side = strafe[2];
        float yaw = strafe[0];

        if (forward == 0.0F && side == 0.0F) {
            moveEvent.setX(0.0);
            moveEvent.setZ(0.0);
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + side * sin) * motionSpeed;
        double z = (forward * sin - side * cos) * motionSpeed;

        moveEvent.setX(x);
        moveEvent.setZ(z);

        mc.player.setMotion(moveEvent.getX(), mc.player.getMotion().y, moveEvent.getZ());
    }

    public static float setMotion(double speed, float currentYaw, float targetYaw, float maxYawChange) {
        float yawDifference = RotationUtil.angleDiff(targetYaw, currentYaw);
        if (!(yawDifference > maxYawChange)) {
            targetYaw = currentYaw;
        } else {
            targetYaw += !(MathHelper.wrapDegrees(currentYaw - targetYaw) > 0.0F) ? -maxYawChange : maxYawChange;
        }

        float yawRadians = (targetYaw - 90.0F) * (float) (Math.PI / 180.0);
        mc.player.setMotion((double) (-MathHelper.sin(yawRadians)) * speed, mc.player.getMotion().y, (double) MathHelper.cos(yawRadians) * speed);
        return targetYaw;
    }

    public static float setMotion(EventMove event, double speed, float currentYaw, float targetYaw, float maxYawChange) {
        float yawDifference = RotationUtil.angleDiff(targetYaw, currentYaw);
        if (!(yawDifference > maxYawChange)) {
            targetYaw = currentYaw;
        } else {
            targetYaw += !(MathHelper.wrapDegrees(currentYaw - targetYaw) > 0.0F) ? -maxYawChange : maxYawChange;
        }

        float yawRadians = (targetYaw - 90.0F) * (float) (Math.PI / 180.0);
        event.setX((double) (-MathHelper.sin(yawRadians)) * speed);
        event.setZ((double) MathHelper.cos(yawRadians) * speed);

        mc.player.setMotion(event.getX(), mc.player.getMotion().y, event.getZ());

        return targetYaw;
    }

    public static double getSmartSpeed() {
        double speed = 0.2873;
        float multiplier = 1.0F;
        ModifiableAttributeInstance var5 = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
        multiplier = (float) ((double) multiplier * ((var5.getValue() / (double) mc.player.abilities.getWalkSpeed() + 1.0) / 2.0));
        if (mc.player.isSprinting()) {
            multiplier = (float) ((double) multiplier - 0.15);
        }

        if (mc.player.isPotionActive(Effects.SPEED) && mc.player.isSprinting()) {
            multiplier = (float) ((double) multiplier - 0.03000002 * (double) (mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1));
        }

        if (mc.player.isSneaking()) {
            speed *= 0.25;
        }

        if (mc.player.isInWater()) {
            speed *= 0.3;
        }

        return speed * (double) multiplier;
    }

    public static double getDumberSpeed() {
        double baseSpeed = 0.2873 + (double) getSpeedBoost() * 0.057;
        if (mc.player.isSneaking()) {
            baseSpeed *= 0.25;
        }

        return baseSpeed;
    }
    public static double lowHopSpeed() {
        double baseSpeed = 0.3973 + (double) getSpeedBoost() * 0.857;
        if (mc.player.isSneaking()) {
            baseSpeed *= 0.25;
        }

        return baseSpeed;
    }

    public static void moveInDirection(double speed) {
        float[] adjusted = getDirectionArray();
        float forward = adjusted[1];
        float side = adjusted[2];
        float yaw = adjusted[0];

        if (forward == 0.0F && side == 0.0F) {
            stop();
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + side * sin) * speed;
        double z = (forward * sin - side * cos) * speed;

        mc.player.setMotion(x, mc.player.getMotion().y, z);
    }

    public static float[] getDirectionArray(float forward, float strafe) {
        float yaw = mc.player.rotationYaw + 90.0F;
        if (Client.getInstance().minerTracker.getAdjustedYaw() != -999.0f) {
            yaw = Client.getInstance().minerTracker.getAdjustedYaw() + 90.0f;
        }

        if (forward != 0.0F) {
            if (!(strafe >= 1.0F)) {
                if (strafe <= -1.0F) {
                    yaw += (float) (!(forward > 0.0F) ? -45 : 45);
                    strafe = 0.0F;
                }
            } else {
                yaw += (float) (!(forward > 0.0F) ? 45 : -45);
                strafe = 0.0F;
            }

            if (!(forward > 0.0F)) {
                if (forward < 0.0F) {
                    forward = -1.0F;
                }
            } else {
                forward = 1.0F;
            }
        }

        return new float[]{yaw, forward, strafe};
    }

    public static float[] getDirectionArray() {
        MovementInput input = mc.player.movementInput;
        return getDirectionArray(input.moveForward, input.moveStrafe);
    }

    public static void sendRandomizedPlayerPositionPackets(boolean isRandomized) {
        double playerX = Module.mc.player.getPosX();
        double playerY = Module.mc.player.getPosY();
        double playerZ = Module.mc.player.getPosZ();
        int jumpBoostHeight = 49 + getJumpBoost() * 17;

        for (int i = 0; i < jumpBoostHeight; i++) {
            double randomOffset = !isRandomized ? 0.0 : com.mentalfrostbyte.jello.util.system.math.MathHelper.generateRandomSmallValue();
            Module.mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(playerX + randomOffset, playerY + 0.06248 + com.mentalfrostbyte.jello.util.system.math.MathHelper.generateRandomSmallValue(), playerZ + randomOffset, false));
            if (ServerUtil.isHypixel()) {
                Module.mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(playerX + randomOffset, playerY + 0.05 + com.mentalfrostbyte.jello.util.system.math.MathHelper.generateRandomSmallValue(), playerZ + randomOffset, false));
            }
        }
    }

    public static double[] getVerticalOffsets() {
        return new double[]{0.0, 0.0625, 0.125, 0.25, 0.3125, 0.5, 0.625, 0.75, 0.8125, 0.875, 0.9375, 1.0, 1.0625,
                1.125, 1.25, 1.3125, 1.375};
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static void silentStrafe(final EventInputOptions event, float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.rotationYaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
            boolean skaf = Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).getStringSettingValueByName("Mode").equals("SideHit");

            event.setForward(skaf ? -1 : closestForward);
            event.setStrafe(closestStrafe);
        }
    }

}