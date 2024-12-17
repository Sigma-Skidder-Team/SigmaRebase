package com.mentalfrostbyte.jello.util.player;

import com.mentalfrostbyte.jello.event.impl.EventMove;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import net.minecraft.client.Minecraft;
import com.mentalfrostbyte.Client;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;

public class MovementUtil {
    protected static Minecraft mc = Minecraft.getInstance();

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
            var4 = (float)((double)var4 - 0.03000002 * (double)(mc.player.getActivePotionEffect(Effects.SPEED).method8629() + 1));
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
        return ! mc.player.isPotionActive(Effects.SPEED) ? 0 : mc.player.getActivePotionEffect(Effects.SPEED).method8629() + 1;
    }

    public static int method37079() {
        return ! mc.player.isPotionActive(Effects.JUMP_BOOST) ? 0 : mc.player.getActivePotionEffect(Effects.JUMP_BOOST).method8629() + 1;
    }

    public static double method37080() {
        return 0.42F + (double)method37079() * 0.1;
    }

    public static boolean isInWater() {
        return mc.player.isInWater();
    }

    public static float[] lenientStrafe() {
        MovementInput var2 = mc.player.movementInput;
        float var3 = var2.field43908;
        float var4 = var2.field43907;
        return method37084(var3, var4);
    }

    public static float[] method37083() {
        MovementInput var2 = mc.player.movementInput;
        float var3 = var2.field43908;
        float var4 = var2.field43907;
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
}
