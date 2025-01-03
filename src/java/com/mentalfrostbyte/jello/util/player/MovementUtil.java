package com.mentalfrostbyte.jello.util.player;

import com.mentalfrostbyte.jello.event.impl.EventMove;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.MovementInput;

/**
 * Utility class for handling player movement-related operations.
 * This class provides methods for calculating and manipulating player movement,
 * including speed adjustments, strafing, and motion control.
 */
public class MovementUtil {
    protected static Minecraft mc = Minecraft.getInstance();

    /**
     * Gets the current speed boost level of the player.
     *
     * @return The amplifier of the speed effect plus one, or 0 if the player doesn't have a speed effect.
     */
    public static int getSpeedBoost() {
        return !mc.player.isPotionActive(Effects.SPEED) ? 0 : mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1;
    }

    /**
     * Gets the current jump boost level of the player.
     *
     * @return The amplifier of the jump boost effect plus one, or 0 if the player doesn't have a jump boost effect.
     */
    public static int getJumpBoost() {
        return !mc.player.isPotionActive(Effects.JUMP_BOOST) ? 0 : mc.player.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1;
    }

    /**
     * Calculates the player's current movement speed, taking into account various factors such as sprinting, potion effects, sneaking, and being in water.
     *
     * @return The calculated movement speed as a double value.
     */
    public static double getSpeed() {
        double speed = 0.2873;
        float multiplier = 1.0F;
        ModifiableAttributeInstance var5 = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
        multiplier = (float)((double)multiplier * ((var5.getValue() / (double) mc.player.abilities.getWalkSpeed() + 1.0) / 2.0));
        if (mc.player.isSprinting()) {
            multiplier = (float)((double)multiplier - 0.15);
        }

        if (mc.player.isPotionActive(Effects.SPEED) && mc.player.isSprinting()) {
            multiplier = (float)((double)multiplier - 0.03000002 * (double)(mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1));
        }

        if (mc.player.isSneaking()) {
            speed *= 0.25;
        }

        if (isInWater()) {
            speed *= 0.3;
        }

        return speed * (double)multiplier;
    }

    /**
     * Calculates movement angles and directions based on input forward and strafe values.
     * This method adjusts the player's yaw and movement direction for smooth motion.
     *
     * @param forward The forward movement input (-1.0 to 1.0, where negative is backwards)
     * @param strafe The strafe movement input (-1.0 to 1.0, where negative is left)
     * @return A float array containing:
     *         [0] - Adjusted yaw angle
     *         [1] - Normalized forward movement (-1.0, 0.0, or 1.0)
     *         [2] - Adjusted strafe movement
     */
    public static float[] getAdjustedStrafe(float forward, float strafe) {
        float yaw = mc.player.rotationYaw + 90.0F;
//        if (Client.getInstance().getOrientation().getAdjustedYaw() != -999.0F) {
//            yaw = Client.getInstance().getOrientation().getAdjustedYaw() + 90.0F;
//        }

        if (forward != 0.0F) {
            if (!(strafe >= 1.0F)) {
                if (strafe <= -1.0F) {
                    yaw += (float)(!(forward > 0.0F) ? -45 : 45);
                    strafe = 0.0F;
                }
            } else {
                yaw += (float)(!(forward > 0.0F) ? 45 : -45);
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

        if (/*Client.getInstance().method19950().method31742()
                && !Client.getInstance().method19950().method31741()
                &&*/ (mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F)) {
            forward = 1.0F;
        }

        return new float[]{yaw, forward, strafe};
    }
    public static boolean isMovingHorizontally() {
        return mc.player.moveStrafing != 0.0F || mc.player.moveForward != 0.0F;
    }
    /**
     * Calculates adjusted strafe values based on the player's current movement input.
     *
     * @return A float array containing adjusted yaw, forward, and strafe values.
     */
    public static float[] lenientStrafe() {
        MovementInput input = mc.player.movementInput;
        float moveForward = input.moveForward;
        float moveStrafe = input.moveStrafe;
        return getAdjustedStrafe(moveForward, moveStrafe);
    }

    /**
     * Applies strafing movement to the player based on the given speed.
     *
     * @param speed The speed at which to strafe.
     */
    public static void strafe(double speed) {
        float[] adjusted = lenientStrafe();
        float forward = adjusted[1];
        float side = adjusted[2];
        float yaw = adjusted[0];
        if (forward == 0.0F && side == 0.0F) {
            setPlayerXMotion(0.0);
            setPlayerZMotion(0.0);
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + side * sin) * speed;
        double z = (forward * sin - side * cos) * speed;
        setPlayerXMotion(x);
        setPlayerZMotion(z);
    }

    /**
     * Sets the player's X motion component.
     *
     * @param x The new X motion value.
     * @return The set X motion value.
     */
    public static double setPlayerXMotion(double x) {
        mc.player.setMotion(x, mc.player.getMotion().y, mc.player.getMotion().z);
        return x;
    }

    /**
     * Sets the player's Y motion component.
     *
     * @param y The new Y motion value.
     * @return The set Y motion value.
     */
    public static double setPlayerYMotion(double y) {
        mc.player.setMotion(mc.player.getMotion().x, y, mc.player.getMotion().z);
        return y;
    }

    /**
     * Sets the player's Z motion component.
     *
     * @param z The new Z motion value.
     * @return The set Z motion value.
     */
    public static double setPlayerZMotion(double z) {
        mc.player.setMotion(mc.player.getMotion().x, mc.player.getMotion().y, z);
        return z;
    }

    /**
     * Sets the player's movement speed for a given event.
     *
     * @param moveEvent The movement event to modify.
     * @param motionSpeed The desired motion speed.
     */
    public static void setSpeed(EventMove moveEvent, double motionSpeed) {
        float[] strafe = lenientStrafe();
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
        setPlayerXMotion(moveEvent.getX());
        setPlayerZMotion(moveEvent.getZ());
    }

    /**
     * Checks if the player is currently in water.
     *
     * @return true if the player is in water, false otherwise.
     */
    public static boolean isInWater() {
        return mc.player.isInWater();
    }

    /**
     * Checks if the player is currently moving based on keyboard input.
     *
     * @return true if any movement key is pressed, false otherwise.
     */
    public static boolean isMoving() {
        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();
        boolean back = mc.gameSettings.keyBindBack.isKeyDown();
        return forward || left || right || back;
    }
}
