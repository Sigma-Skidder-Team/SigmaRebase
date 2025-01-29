package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.module.settings.impl.SubOptionSetting;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;

import static com.mentalfrostbyte.jello.util.player.MovementUtil.isInWater;

public class MinibloxSpeed extends Module {
    private final BooleanSetting autoJump;
    private final BooleanSetting assumeSprinting;
    private final BooleanSetting ignoreSneaking;
    private final BooleanSetting ignoreInWater;
    private final NumberSetting<Integer> boostAfterTicks;
    private int ticksSinceBoost;
    private final NumberSetting<Double> boostSpeed;
    private final NumberSetting<Double> normalSpeed;

    public MinibloxSpeed() {
        super(ModuleCategory.MOVEMENT, "Miniblox", "Speed for Miniblox");
        this.registerSetting(this.autoJump = new BooleanSetting("Auto Jump", "Automatically jumps for you.", true));
        this.registerSetting(new SubOptionSetting(
                "Ignore Slowdowns",
                "Ignore slowdowns like sneaking, not sprinting, or being in water",
                true,
                this.assumeSprinting = new BooleanSetting("Assume Sprinting", "Assume you are sprinting (broken with TargetStrafe)", false),
                this.ignoreSneaking = new BooleanSetting("Ignore Sneaking", "Ignore sneaking", true),
                this.ignoreInWater = new BooleanSetting("Ignore In Water", "Ignore being in water", true)
                )
        );
        this.registerSetting(this.boostAfterTicks = new NumberSetting<>("Boost After Ticks", "Boost after ticks since last boost", 15f, Integer.class, 1f, 40f, 1f));
        this.registerSetting(this.boostSpeed = new NumberSetting<>("Boost Speed", "Boost speed", 1.5f, Double.class, 1f, 10f, 0.01f));
        this.registerSetting(this.normalSpeed = new NumberSetting<>("Normal Speed", "Normal speed", 1.2f, Double.class, 1f, 10f, 0.01f));
    }

    @Override
    public void onEnable() {
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (autoJump.currentValue && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    @EventTarget
    public void onMove(EventMove event) {
        double calculatedSpeed = MovementUtil.getSpeed();

        // vector probably doesn't take these into account, but the getSpeed function does,
        // so we have to account for it here, since we want the fastest speed possible
        if (!mc.player.isSprinting() && assumeSprinting.currentValue) {
            calculatedSpeed += 0.15;
        }

        if (mc.player.isSneaking() && ignoreSneaking.currentValue) {
            calculatedSpeed *= 1.25;
        }

        if (isInWater() && ignoreInWater.currentValue) {
            calculatedSpeed *= 1.3;
        }
        if (mc.player.isOnGround() && ticksSinceBoost >= boostAfterTicks.currentValue) {
            calculatedSpeed *= boostSpeed.currentValue;
            ticksSinceBoost = 0;
        } else {
            calculatedSpeed *= normalSpeed.currentValue;
            ticksSinceBoost++;
        }

        MovementUtil.setSpeed(event, calculatedSpeed);
    }
}