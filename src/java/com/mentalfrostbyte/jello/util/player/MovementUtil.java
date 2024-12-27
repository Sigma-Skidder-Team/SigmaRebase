package com.mentalfrostbyte.jello.util.player;

import com.mentalfrostbyte.jello.event.impl.EventMove;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.client.Minecraft;
import com.mentalfrostbyte.Client;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;

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
    public static double getSpeed() {
        double var2 = 0.2873;
        float var4 = 1.0F;
        ModifiableAttributeInstance var5 = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
        var4 = (float)((double)var4 * ((var5.getValue() / (double) mc.player.abilities.getWalkSpeed() + 1.0) / 2.0));
        if (mc.player.isSprinting()) {
            var4 = (float)((double)var4 - 0.15);
        }

        if (mc.player.isPotionActive(Effects.SPEED) && mc.player.isSprinting()) {
            var4 = (float)((double)var4 - 0.03000002 * (double)(mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1));
        }

        if (mc.player.isSneaking()) {
            var2 *= 0.25;
        }

        if (isInWater()) {
            var2 *= 0.3;
        }

        return var2 * (double)var4;
    }

    public static void setSpeed(EventMove moveEvent, double motionSpeed) {
        float[] var5 = lenientStrafe();
        float var6 = var5[1];
        float var7 = var5[2];
        float var8 = var5[0];
        if (var6 == 0.0F && var7 == 0.0F) {
            moveEvent.setX(0.0);
            moveEvent.setZ(0.0);
        }

        double var9 = Math.cos(Math.toRadians((double)var8));
        double var11 = Math.sin(Math.toRadians((double)var8));
        double var13 = ((double)var6 * var9 + (double)var7 * var11) * motionSpeed;
        double var15 = ((double)var6 * var11 - (double)var7 * var9) * motionSpeed;
        moveEvent.setX(var13);
        moveEvent.setZ(var15);
        MultiUtilities.setPlayerXMotion(moveEvent.getX());
        MultiUtilities.setPlayerZMotion(moveEvent.getZ());
    }
    public static double method37076() {
        double var2 = 0.2873 + (double)method37078() * 0.057;
        if (mc.player.isSneaking()) {
            var2 *= 0.25;
        }

        return var2;
    }

    public static double method37077() {
        double var2 = 0.2873;
        if (mc.player.isSneaking()) {
            var2 *= 0.25;
        }

        if (isInWater()) {
            var2 *= 0.3;
        }

        return var2;
    }

    public static int method37078() {
        return ! mc.player.isPotionActive(Effects.SPEED) ? 0 : mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1;
    }

    public static int method37079() {
        return ! mc.player.isPotionActive(Effects.JUMP_BOOST) ? 0 : mc.player.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1;
    }

    public static double method37080() {
        return 0.42F + (double)method37079() * 0.1;
    }

    public static boolean isInWater() {
        return mc.player.isInWater();
    }

    public static float[] lenientStrafe() {
        MovementInput var2 = mc.player.movementInput;
        float var3 = var2.moveForward;
        float var4 = var2.moveStrafe;
        return method37084(var3, var4);
    }

    public static float method37093(double var0, float var2, float var3, float var4) {
        float var7 = RotationHelper.angleDiff(var3, var2);
        if (!(var7 > var4)) {
            var3 = var2;
        } else {
            var3 += !(MathHelper.wrapDegrees(var2 - var3) > 0.0F) ? -var4 : var4;
        }

        float var8 = (var3 - 90.0F) * (float) (Math.PI / 180.0);
        MultiUtilities.setPlayerXMotion((double)(-MathHelper.sin(var8)) * var0);
        MultiUtilities.setPlayerZMotion((double) MathHelper.cos(var8) * var0);
        return var3;
    }

    public static float[] method37083() {
        MovementInput var2 = mc.player.movementInput;
        float var3 = var2.moveForward;
        float var4 = var2.moveStrafe;
        return method37085(var3, var4);
    }

    public static float[] method37084(float var0, float var1) {
        float var4 = mc.player.rotationYaw + 90.0F;
        if (Client.getInstance().method19950().method31744() != -999.0F) {
            var4 = Client.getInstance().method19950().method31744() + 90.0F;
        }

        if (var0 != 0.0F) {
            if (!(var1 >= 1.0F)) {
                if (var1 <= -1.0F) {
                    var4 += (float)(!(var0 > 0.0F) ? -45 : 45);
                    var1 = 0.0F;
                }
            } else {
                var4 += (float)(!(var0 > 0.0F) ? 45 : -45);
                var1 = 0.0F;
            }

            if (!(var0 > 0.0F)) {
                if (var0 < 0.0F) {
                    var0 = -1.0F;
                }
            } else {
                var0 = 1.0F;
            }
        }

        if (Client.getInstance().method19950().method31742()
                && !Client.getInstance().method19950().method31741()
                && (mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F)) {
            var0 = 1.0F;
        }

        return new float[]{var4, var0, var1};
    }

    public static float[] method37085(float var0, float var1) {
        float var4 = mc.player.rotationYaw + 90.0F;
        if (var0 == 0.0F) {
            if (var1 != 0.0F) {
                var4 += (float)(!(var1 > 0.0F) ? 90 : -90);
                var1 = 0.0F;
            }
        } else {
            if (!(var1 >= 1.0F)) {
                if (var1 <= -1.0F) {
                    var4 += (float)(!(var0 > 0.0F) ? -45 : 45);
                    var1 = 0.0F;
                }
            } else {
                var4 += (float)(!(var0 > 0.0F) ? 45 : -45);
                var1 = 0.0F;
            }

            if (!(var0 > 0.0F)) {
                if (var0 < 0.0F) {
                    var0 = -1.0F;
                    var4 -= 180.0F;
                }
            } else {
                var0 = 1.0F;
            }
        }

        return new float[]{var4, var0, var1};
    }


    public static float method37086() {
        float var2 = mc.player.moveForward;
        float var3 = mc.player.moveStrafing;
        float var4 = mc.player.rotationYaw + 90.0F;
        if (var2 > 0.0F && mc.gameSettings.keyBindBack.isKeyDown()) {
            var2 = -1.0F;
        }

        if (var3 > 0.0F) {
            var4 -= 90.0F;
        } else if (var3 < 0.0F) {
            var4 += 90.0F;
        }

        if (var2 != 0.0F) {
            if (var3 > 0.0F) {
                var4 -= (float)(!(var2 > 0.0F) ? 45 : -45);
            } else if (var3 < 0.0F) {
                var4 -= (float)(!(var2 > 0.0F) ? -45 : 45);
            }
        }

        if (var2 < 0.0F && var3 == 0.0F) {
            var4 -= 180.0F;
        }

        return var4;
    }

    public static float method37092(EventMove event, double var1, float be, float tween, float var5) {
        float var8 = RotationHelper.angleDiff(tween, be);
        if (!(var8 > var5)) {
            tween = be;
        } else {
            tween += !(MathHelper.wrapDegrees(be - tween) > 0.0F) ? -var5 : var5;
        }



        float var9 = (tween - 90.0F) * (float) (Math.PI / 180.0);
        event.setX((double)(-MathHelper.sin(var9)) * var1);
        event.setZ((double) MathHelper.cos(var9) * var1);
        MultiUtilities.setPlayerXMotion(event.getX());
        MultiUtilities.setPlayerZMotion(event.getZ());
        return tween;
    }

    public static void strafe(double v) {
    }

    public static void method37095(double var0) {
        double var4 = (double) mc.player.movementInput.moveForward;
        double var6 = (double) mc.player.movementInput.moveStrafe;
        float var8 = mc.player.rotationYaw;
        if (var4 != 0.0) {
            if (!(var6 > 0.0)) {
                if (var6 < 0.0) {
                    var8 += (float)(!(var4 > 0.0) ? -45 : 45);
                }
            } else {
                var8 += (float)(!(var4 > 0.0) ? 45 : -45);
            }

            var6 = 0.0;
            if (!(var4 > 0.0)) {
                if (var4 < 0.0) {
                    var4 = -1.0;
                }
            } else {
                var4 = 1.0;
            }
        }

        double var9 = mc.player.getPosX();
        double var11 = mc.player.getPosY();
        double var13 = mc.player.getPosZ();
        double var15 = var4 * var0 * Math.cos(Math.toRadians((double)(var8 + 90.0F))) + var6 * var0 * Math.sin(Math.toRadians((double)(var8 + 90.0F)));
        double var17 = var4 * var0 * Math.sin(Math.toRadians((double)(var8 + 90.0F))) - var6 * var0 * Math.cos(Math.toRadians((double)(var8 + 90.0F)));
        mc.player.setPosition(var9 + var15, var11, var13 + var17);
    }
}
