package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.misc.ViperEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SKeepAlivePacket;
import team.sdhq.eventBus.annotations.EventTarget;

/*
*  very bad code
*
*
* */

import java.util.ArrayList;

public class ViperDisabler extends Module {
    private int tickCounter;
    private final ArrayList<ViperEvent> pendingEvents = new ArrayList<>();

    public ViperDisabler() {
        super(ModuleCategory.EXPLOIT, "Viper", "Disabler for ViperMC");
    }

    @Override
    public void onEnable() {
        this.tickCounter = 0;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && mc.player != null) {
            this.tickCounter++;
            boolean isPlayerInAir = event.getY() > mc.player.getPosY() - 1.0E-6 && event.getY() < mc.player.getPosY() + 1.0E-6;
            if (isPlayerInAir) {
                event.setY(mc.player.getPosY() + 0.4);
                event.setGround(false);
            }

            if (this.tickCounter > 60) {
                event.setY(mc.player.getPosY() + 0.4);
                event.setGround(false);
            } else {
                for (int i = 0; i < 10; i++) {
                    boolean isMiddleIteration = i > 2 && i < 8;
                    double verticalAdjustment = isMiddleIteration ? 0.2 : -0.2;
                    CPlayerPacket.PositionPacket positionPacket = new CPlayerPacket.PositionPacket(
                            mc.player.getPosX(), mc.player.getPosY() + verticalAdjustment, mc.player.getPosZ(), true
                    );
                    mc.getConnection().sendPacket(positionPacket);
                }

                mc.player.lastReportedPosY = 0.0;
                if (mc.player.ticksExisted <= 1) {
                    this.pendingEvents.clear();
                }

                if (!this.pendingEvents.isEmpty()) {
                    for (int index = 0; index < this.pendingEvents.size(); index++) {
                        ViperEvent event1 = this.pendingEvents.get(index);
                        if (event1.shouldSendPacket()) {
                            mc.getConnection().sendPacket(event1.getPacket());
                            this.pendingEvents.remove(index);
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            IPacket incomingPacket = event.getPacket();
            if (incomingPacket instanceof SKeepAlivePacket) {
                event.setCancelled(true);
            }

            if (incomingPacket instanceof SConfirmTransactionPacket) {
                event.setCancelled(true);
            }
        }
    }
}
