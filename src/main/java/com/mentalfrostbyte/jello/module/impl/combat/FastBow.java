package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import team.sdhq.eventBus.annotations.EventTarget;

public class FastBow extends Module {

    public FastBow() {
        super(ModuleCategory.COMBAT, "FastBow", "Shoots arrows faster");
    }

    @EventTarget
    public void TickEvent(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (mc.player.getHeldItemMainhand() != null
                    && mc.player.getHeldItemMainhand().getItem() instanceof BowItem
                    && mc.player.isOnGround()) {
                for (int i = 0; i < 25; i++) {
                    mc.getConnection().sendPacket(new CPlayerPacket(true));
                }

                mc.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
            }
        }
    }
}
