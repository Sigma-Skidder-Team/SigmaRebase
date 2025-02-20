package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class GommeAntiKB extends Module {
    private int velocityPackets;
    private double motionY;

    public GommeAntiKB() {
        super(ModuleCategory.COMBAT, "Gomme", "AntiKB for GommeHD");
        this.registerSetting(new NumberSetting<Float>("Delay", "Boost delay", 0.5F, Float.class, 0.0F, 1.0F, 0.01F));
        this.registerSetting(new NumberSetting<Float>("Boost", "Boost strength", 0.1F, Float.class, 0.05F, 0.25F, 0.01F));
    }

    @EventTarget
    public void onWorldLoad(EventLoadWorld _event) {
        this.velocityPackets = 0;
    }

    @EventTarget
    public void onMove(EventMove event) {
        double maxY = this.motionY * (double) (1.0F - this.getNumberValueBySettingName("Delay")) - this.motionY / 2.0;
//        if (mc.player.isOnGround() && this.field23610 <= 0) {
//        }

        if (this.velocityPackets == 1 && event.getY() < maxY) {
            this.velocityPackets++;
            event.setX(event.getX() * 0.5);
            event.setZ(event.getZ() * 0.5);
        } else if (this.velocityPackets == 2) {
            this.velocityPackets++;
            MovementUtil.setMotion(event, this.getNumberValueBySettingName("Boost"));
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (event.packet instanceof SEntityVelocityPacket) {
            SEntityVelocityPacket var4 = (SEntityVelocityPacket) event.packet;
            if (var4.getEntityID() == mc.player.getEntityId()) {
                this.velocityPackets = 1;
                this.motionY = (double) var4.getMotionY() / 8000.0;
            }
        }
    }
}
