package com.mentalfrostbyte.jello.module.impl.player.nofall;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Hypixel2NoFall extends Module {
    private double stage;

    public Hypixel2NoFall() {
        super(ModuleCategory.PLAYER, "Hypixel2", "2nd Hypixel NoFall");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.stage = 0.0;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (!this.isEnabled()) return;
        if (event.isPre()) {
            if (BlockUtil.isAboveBounds(mc.player, 1.0E-4F)) {
                this.stage = 0.0;
                return;
            }

            if (mc.player.getMotion().y < -0.1 && mc.player.fallDistance > 3.0F) {
                this.stage++;
                if (this.stage == 1.0) {
                    mc.getConnection().sendPacket(new CPlayerPacket(true));
                } else if (this.stage > 1.0) {
                    this.stage = 0.0;
                }
            }
        }
    }

}
