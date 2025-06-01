package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import team.sdhq.eventBus.annotations.EventTarget;

/**
 * @author alarmingly_good (on discord)
 */
public class VerusFly extends Module {
    public final ModeSetting mode;
    public double speed;

    public VerusFly() {
        super(ModuleCategory.MOVEMENT, "Verus", "A fly for Verus");
        registerSetting(mode = new ModeSetting(
                        "Mode",
                        "Mode",
                        0,
                        "Glide", "Fast"
                )
        );
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onMotion(EventMotion event) {
        ClientPlayerEntity player = mc.player;
        ClientPlayNetHandler network = mc.getConnection();
        assert player != null;
        assert network != null;
        switch (mode.currentValue) {
            case "Glide":
                if (!MovementUtil.isMoving()) {
                    MovementUtil.stop();
                }

                if (!player.onGround) MovementUtil.strafe(0.334);

                if (mc.gameSettings.keyBindJump.pressed) {
                    if (player.ticksExisted % 2 == 0) {
                        player.setMotion(player.getMotion().x, 0.42F, player.getMotion().z);
                    }
                } else {
                    player.setMotion(player.getMotion().x, Math.max(player.getMotion().y, -0.09800000190734863), player.getMotion().z);
                }
                break;
            case "Fast":

                if (!mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSneak.pressed) {
                    mc.player.setMotion(mc.player.getMotion().x, 0.019, mc.player.getMotion().z);
                    MovementUtil.strafe(0.34);

                    if (player.ticksExisted % 2 == 0) {
                        player.setMotion(player.getMotion().x, -0.019, player.getMotion().z);
                    }
                }

                if (mc.gameSettings.keyBindJump.pressed && player.ticksExisted % 2 == 0) {
                    player.setMotion(player.getMotion().x, 0.42F, player.getMotion().z);
                }
                if (mc.gameSettings.keyBindSneak.pressed) {
                    player.setMotion(player.getMotion().x, Math.max(player.getMotion().y, -0.09800000190734863), player.getMotion().z);
                }
                    break;
                }
        }
    }