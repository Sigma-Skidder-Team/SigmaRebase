package com.mentalfrostbyte.jello.module.impl.movement;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;


import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class TargetStrafe extends Module {
    public int field23495 = 1;
    public boolean field23496;

    public TargetStrafe() {
        super(ModuleCategory.MOVEMENT, "TargetStrafe", "Allows you to strafe arround targets with speed");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Basic", "Ninja", "Random"));
        this.registerSetting(new NumberSetting<Float>("Radius", "Radius of the circle", 2.0F, Float.class, 1.0F, 6.0F, 0.01F));
        this.registerSetting(new BooleanSetting("Only speed", "Use target strafe only when speed is enabled", true));
        this.registerSetting(new BooleanSetting("Hold Spacebar", "Use target strafe only when spacebar is held down", true));
        this.registerSetting(new BooleanSetting("Anti Scaffold", "Prevents strafing when scaffolding", true));
        this.registerSetting(new ModeSetting("AntiVoid", "The way you will avoid the void", 0, "Smart", "Halt", "Command", "None").addObserver(var1 -> this.field23496 = false));
    }

    // $VF: synthetic method
    public static Minecraft method16156() {
        return mc;
    }

    // $VF: synthetic method
    public static Minecraft method16157() {
        return mc;
    }

    @EventTarget
    @LowerPriority
    public void method16151(EventMove var1) {
        if (this.isEnabled()) {
            if (this.getBooleanValueFromSettingName("Hold Spacebar") && !mc.gameSettings.keyBindJump.isKeyDown()) {
                return;
            }
            if (this.getBooleanValueFromSettingName("Anti Scaffold") && Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).isEnabled()) {
                return;
            }
            Entity var4 = null;
            if (Client.getInstance().moduleManager.getModuleByClass(Speed.class).isEnabled2() || !this.getBooleanValueFromSettingName("Only speed")) {
                if (KillAura.targetData != null) {
                    var4 = KillAura.targetData.getEntity();
                } else if (KillAura.targetEntity != null) {
                    var4 = KillAura.targetEntity;
                }
            }

            if (var4 != null) {
                double var5 = Math.sqrt(var1.getX() * var1.getX() + var1.getZ() * var1.getZ());
                float var7 = this.getNumberValueBySettingName("Radius");
                String var8 = this.getStringSettingValueByName("Mode");
                switch (var8) {
                    case "Basic":
                        this.method16152(var4, var5, var7, var1);
                        break;
                    case "Ninja":
                        float var15 = (float) Math.toRadians(var4.getRotationYawHead() - 180.0F);
                        double var16 = var4.getPositionVec().x - (double) (MathHelper.sin(var15) * var7);
                        double var17 = var4.getPositionVec().z + (double) (MathHelper.cos(var15) * var7);
                        var1.setX(var16 - mc.player.getPositionVec().x);
                        var1.setZ(var17 - mc.player.getPositionVec().z);
                        break;
                    case "Random":
                        float var10 = (float) (Math.random() * 2.0 * Math.PI);
                        double var11 = var4.getPositionVec().x - (double) (MathHelper.sin(var10) * var7);
                        double var13 = var4.getPositionVec().z + (double) (MathHelper.cos(var10) * var7);
                        var1.setX(var11 - mc.player.getPositionVec().x);
                        var1.setZ(var13 - mc.player.getPositionVec().z);
                }
            }
        }
    }

    public void method16152(Entity var1, double var2, double var4, EventMove var6) {
        double var9 = var2 / (var4 * Math.PI * 2.0) * 360.0 * (double) this.field23495;
        double var11 = mc.player.getPosX() - var1.getPosX();
        double var13 = mc.player.getPosZ() - var1.getPosZ();
        double var15 = Math.atan2(var13, var11) * 180.0 / Math.PI - 90.0;
        double var17 = (var15 + var9) * Math.PI / 180.0;
        double var19 = var1.getPositionVec().x - Math.sin(var17) * var4;
        double var21 = var1.getPositionVec().z + Math.cos(var17) * var4;
        var11 = var19 - mc.player.getPosX();
        var13 = var21 - mc.player.getPosZ();
        double var23 = Math.atan2(var13, var11) * 180.0 / Math.PI - 90.0;
        var17 = var23 * Math.PI / 180.0;
        var6.setX(-Math.sin(var17) * var2);
        var6.setZ(Math.cos(var17) * var2);
        Vector3d var25 = mc.player.getAllowedMovement(var6.getVector());
        if (var25.x != var6.getX() || var25.z != var6.getZ()) {
            this.field23495 *= -1;
            var9 *= -1.0;
            var17 = (var15 + var9) * Math.PI / 180.0;
            var19 = var1.getPositionVec().x - Math.sin(var17) * var4;
            var21 = var1.getPositionVec().z + Math.cos(var17) * var4;
            var11 = var19 - mc.player.getPosX();
            var13 = var21 - mc.player.getPosZ();
            var23 = Math.atan2(var13, var11) * 180.0 / Math.PI - 90.0;
            var17 = var23 * Math.PI / 180.0;
            var6.setX(-Math.sin(var17) * var2);
            var6.setZ(Math.cos(var17) * var2);
        }

        Vector3d var26 = new Vector3d(
                mc.player.getPositionVec().x + var6.getX(),
                mc.player.getPositionVec().y + var6.getY(),
                mc.player.getPositionVec().z + var6.getZ()
        );
        String var27 = this.getStringSettingValueByName("AntiVoid");
        if (!var27.equals("None")) {
            if (!this.field23496 && this.method16153(var26) && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
                this.field23495 *= -1;
                this.field23496 = true;
            } else if (this.field23496 && !this.method16153(var26)) {
                this.field23496 = false;
            }
        }

        if (this.field23496 && mc.player.getDistance(var1) > this.getNumberValueBySettingName("Radius")) {
            if (!var27.equals("Halt")) {
                MovementUtil.setMotion(var6, var2);
                if (var27.equals("Smart")) {
                    var26 = new Vector3d(
                            mc.player.getPositionVec().x + var6.getX(),
                            mc.player.getPositionVec().y + var6.getY(),
                            mc.player.getPositionVec().z + var6.getZ()
                    );
                    if (this.method16153(var26) && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
                        MovementUtil.setMotion(var6, 0.0);
                    }
                }
            } else {
                MovementUtil.setMotion(var6, 0.0);
            }
        }
    }

    public boolean method16153(Vector3d var1) {
        if (!(mc.player.getPositionVec().y < 1.0)) {
            AxisAlignedBB var4 = new AxisAlignedBB(var1.add(-0.15, 0.0, -0.15), var1.add(0.15, mc.player.getHeight(), 0.15));
            var4 = var4.expand(0.0, -mc.player.getPositionVec().y, 0.0);
            return mc.world.getCollisionShapes(mc.player, var4).count() == 0L;
        } else {
            return true;
        }
    }

    public boolean method16154(double var1, double var3, double var5) {
        return mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().offset(var1, var3, var5)).count() == 0L;
    }
}
