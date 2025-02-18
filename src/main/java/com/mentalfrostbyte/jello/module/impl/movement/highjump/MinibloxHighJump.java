package com.mentalfrostbyte.jello.module.impl.movement.highjump;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class MinibloxHighJump extends Module {
    private final NumberSetting<Float> motion;
    private boolean isJumping = false;

    public MinibloxHighJump() {
        super(ModuleCategory.MOVEMENT, "Miniblox", "HighJump for Miniblox");
        this.registerSetting(
                this.motion =
                        new NumberSetting<>(
                                "Motion",
                                "Motion",
                                0.75F,
                                Float.class,
                                0.42F,
                                5.0F, 0.05F
                        )
        );
    }

    public void onMove(EventMove __) {
        if (!this.isEnabled()) return;
        if (isJumping || motion.currentValue < 4)
            return;
        if (mc.player.isOnGround()) {
            isJumping = false;
            return;
        }
        mc.getConnection().getNetworkManager().sendNoEventPacket(new CPlayerPacket.PositionPacket(
                mc.player.getPosX(),
                mc.player.getPosY() - 0.000000000000000000000000000000000000000000001, // real
                mc.player.getPosZ(),
                mc.player.isOnGround()
        ));
        mc.player.jump();
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (!this.isEnabled()) return;
        event.setY(motion.currentValue);
        isJumping = true;
    }
}
