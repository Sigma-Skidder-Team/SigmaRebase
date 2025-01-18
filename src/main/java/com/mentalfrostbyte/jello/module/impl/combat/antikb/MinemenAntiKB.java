package com.mentalfrostbyte.jello.module.impl.combat.antikb;

import com.mentalfrostbyte.jello.event.impl.EventMove;
import com.mentalfrostbyte.jello.event.impl.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.ReceivePacketEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class MinemenAntiKB extends Module {
    private boolean aboveBounds = false;
    private boolean field23853;

    public MinemenAntiKB() {
        super(ModuleCategory.COMBAT, "Minemen", "Minemen Club bypass");
    }

    @Override
    public void onEnable() {
        this.aboveBounds = false;
        this.field23853 = true;
    }

    @EventTarget
    public void onUpdate(EventUpdate var1) {
        if (var1.isPre()) {
            if (false/*MultiUtilities.isAboveBounds(mc.player, 1.0E-5F)*/) {
                this.aboveBounds = true;
                var1.setY(var1.getY() - 5.0E-7);
                var1.setGround(false);
            } else {
                if (this.aboveBounds && mc.player.getMotion().y < 0.0) {
                    this.aboveBounds = false;
                    var1.setGround(true);
                }
            }
        }
    }

    @EventTarget
    public void onMove(EventMove var1) {
        if (this.field23853) {
            if (!mc.player.isOnGround()) {
                if (mc.player.fallDistance > 1.0F) {
                    this.field23853 = false;
                }
            } else {
                // TODO
//                var1.setY(MovementUtil.method37080());
                this.field23853 = false;
            }
        }

        MovementUtil.setPlayerYMotion(var1.getY());
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent var1) {
        if (mc.player != null && var1.getPacket() instanceof SEntityVelocityPacket) {
            SEntityVelocityPacket var5 = (SEntityVelocityPacket) var1.getPacket();
            if (var5.getEntityID() == mc.player.getEntityId() && var5.motionY < 0 && mc.player.isOnGround()) {
                var1.cancelled = true;
            }
        } else if (var1.getPacket() instanceof SPlayerPositionLookPacket) {
            SPlayerPositionLookPacket var4 = (SPlayerPositionLookPacket) var1.getPacket();
            this.field23853 = true;
        }
    }
}
