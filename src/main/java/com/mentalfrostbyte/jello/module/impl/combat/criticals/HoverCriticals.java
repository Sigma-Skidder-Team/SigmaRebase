package com.mentalfrostbyte.jello.module.impl.combat.criticals;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.Step;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class HoverCriticals extends Module {
    private double field23997;

    public HoverCriticals() {
        super(ModuleCategory.COMBAT, "Hover", "Hover criticals");
    }

    @Override
    public void onEnable() {
        this.field23997 = 1.0E-11;
    }

    @EventTarget
    public void method16921(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof SPlayerPositionLookPacket) {
                this.field23997 = 1.0E-11;
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            boolean var4 = mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK;
            boolean var5 = mc.playerController.getIsHittingBlock() || mc.gameSettings.keyBindAttack.isKeyDown() && var4;
            if (Client.getInstance().playerTracker.getgroundTicks() > 0 && !var5) {
                this.field23997 -= 1.0E-14;
                if (this.field23997 < 0.0 || Step.updateTicksBeforeStep == 0) {
                    this.field23997 = 1.0E-11;
                }

                var1.setMoving(true);
                var1.setY(var1.getY() + this.field23997);
                var1.setOnGround(false);
            } else {
                this.field23997 = 1.0E-11;
            }
        }
    }
}
