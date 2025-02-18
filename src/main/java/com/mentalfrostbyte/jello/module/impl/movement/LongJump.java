package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.longjump.NCPLongJump;
import com.mentalfrostbyte.jello.module.impl.movement.longjump.CubecraftLongJump;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;

public class LongJump extends ModuleWithModuleSettings {
    public LongJump() {
        super(ModuleCategory.MOVEMENT,
                "LongJump",
                "Makes you jump far away",
                new NCPLongJump(),
                new CubecraftLongJump());
        this.registerSetting(new BooleanSetting("Auto Disable", "Disable Longjump when landing", true));
        this.registerSetting(new BooleanSetting("BorderJump", "Jumps when you are close to a border", true));
        this.registerSetting(new BooleanSetting("Auto Jump", "Automatically jumps when you can", true));
    }

    public double getNCPBasicY(int airTicks) {
        double[] normalY = new double[]{0.345, 0.2699, 0.183, 0.103, 0.024, -0.008, -0.04, -0.072, -0.104, -0.13, -0.019, -0.097};
        double[] collidedY = new double[]{0.345, 0.2699, 0.183, 0.103, 0.024, -0.008, -0.04, -0.072, -0.14, -0.17, -0.019, -0.13};
        airTicks--;
        if (airTicks < 0 || airTicks >= normalY.length) {
            return mc.player.getMotion().y;
        } else {
            return MovementUtil.isMoving() && !mc.player.collidedHorizontally ? normalY[airTicks] : collidedY[airTicks];
        }
    }

    public double getNCPHighY(int airTicks) {
        double[] yValues = new double[]{
                0.423,
                0.35,
                0.28,
                0.217,
                0.15,
                0.025,
                -0.00625,
                -0.038,
                -0.0693,
                -0.102,
                -0.13,
                -0.018,
                -0.1,
                -0.117,
                -0.14532,
                -0.1334,
                -0.1581,
                -0.183141,
                -0.170695,
                -0.195653,
                -0.221,
                -0.209,
                -0.233,
                -0.25767,
                -0.314917,
                -0.371019,
                -0.426
        };
        airTicks--;
        return airTicks >= 0 && airTicks < yValues.length ? yValues[airTicks] : mc.player.getMotion().y;
    }
}
