package com.mentalfrostbyte.jello.module.impl.movement.speed;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import team.sdhq.eventBus.annotations.EventTarget;

/**
 * @author alarmingly_good (on discord)
 */
public class VerusSpeed extends Module {

    private final ModeSetting mode;
    private final NumberSetting<Long> damageBoostTime;
    // we should go fast for ~3 seconds or more
    public TimerUtil damageTimer = new TimerUtil();
    public VerusSpeed() {
        super(ModuleCategory.MOVEMENT, "Verus", "Speed for Verus.");
        registerSetting(
                this.mode = new ModeSetting(
                        "Mode", "Mode",0,
                        "Basic", "Low",
                        "Ground", "Glide"
                )
        );
        registerSetting(new BooleanSetting("Damage boost", "Boost on damage", false));
        registerSetting(
                this.damageBoostTime = new NumberSetting<>(
                        "Damage boost time",
                        "How long in seconds to boost after damage?",
                        3,
                        Long.class,
                        1L,
                        10L,
                        1L
                )
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        damageTimer.setElapsedTime((long)(damageBoostTime.currentValue * 1000L) + 3000L);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        damageTimer.stop();
    }

    private double speed = 0;
    private int airTicks = 0;

    public BlockState blockStateUnder() {
		assert mc.player != null;
		BlockPos under = new BlockPos(
                mc.player.getPosX(),
                mc.player.getBoundingBox().minY - 0.5000001D,
                mc.player.getPosZ()
        );
		assert mc.world != null;
		return mc.world.getBlockState(under);
    }

    @EventTarget
    public void onMotion(EventUpdateWalkingPlayer event) {
        boolean dmgBoost = getBooleanValueFromSettingName("Damage boost");
        assert mc.player != null;
        switch (mode.currentValue) {
            case "Basic" -> {
                if (!mc.player.onGround) {
                    speed *= 0.9999999999999999D;
                    airTicks++;
                } else {
                    airTicks = 0;
                    if (mc.player.isPotionActive(Effects.SPEED)) {
                        speed = 0.498D;
                    } else {
                        speed = 0.377D;
                    }

                    if (!mc.player.isSprinting())
                        speed *= 0.78D;

                    float slipperiness = blockStateUnder().getBlock().getSlipperiness();
                    if (slipperiness != 0.6f)
                        speed += (slipperiness * 0.36);
                    mc.player.jump();
                }
            }

            case "Low" -> {
                if (!mc.player.onGround) {
                    speed *= 0.9999999999999999D;
                    airTicks++;
                } else {
                    airTicks = 0;
                    if (mc.player.isPotionActive(Effects.SPEED)) {
                        speed = 0.498D;
                    } else {
                        speed = 0.377D;
                    }

                    if (!mc.player.isSprinting())
                        speed *= 0.78D;

                    float slipperiness = blockStateUnder().getBlock().getSlipperiness();

                    if (slipperiness != 0.6f)
                        speed += (slipperiness * 0.35);

                    mc.player.jump();
                }

                if (airTicks == 2) {
                    mc.player.setMotion(mc.player.getMotion().x, -0.0784000015258789, mc.player.getMotion().z);
                }
            }

            case "Ground" -> {
                if (mc.player.onGround) {
                    speed = 0.23 + (Math.random() / 150d);
                    float slipperiness = blockStateUnder().getBlock().getSlipperiness();

                    if (slipperiness != 0.6f)
                        speed += (slipperiness * 0.35);

                    MovementUtil.strafe(speed);
                }
            }

            case "Glide" -> {
                if (!mc.player.onGround) {
                    speed *= 0.9999999999999999D;
                    airTicks++;
                } else {
                    airTicks = 0;
                    if (mc.player.isPotionActive(Effects.SPEED)) {
                        speed = 0.498D;
                    } else {
                        speed = 0.377D;
                    }

                    float slipperiness = blockStateUnder().getBlock().getSlipperiness();
                    if (slipperiness != 0.6f)
                        speed += (slipperiness * 0.37);

                    mc.player.jump();
                }

                if (!mc.player.isSprinting())
                    speed *= 0.78D;

                mc.player.setMotion(
                        mc.player.getMotion().x,
                        Math.max(mc.player.getMotion().y, -0.09800000190734863),
                        mc.player.getMotion().z
                );
            }
        }

        if (mc.player.hurtTime > 0 && dmgBoost) {
            damageTimer.reset();
            damageTimer.start();
        }

        if ((mc.player.hurtTime > 0 || damageTimer.getElapsedTime() <= damageBoostTime.currentValue * 1e3) && dmgBoost) {
            boolean groundMode = mode.currentValue.equals("Ground");
            MovementUtil.strafe(groundMode ? 1.2 : 0.86);
            if (groundMode)
                mc.player.setMotion(mc.player.getMotion().x, -1, mc.player.getMotion().z);
        }
        else
            MovementUtil.strafe(speed);
    }
}
