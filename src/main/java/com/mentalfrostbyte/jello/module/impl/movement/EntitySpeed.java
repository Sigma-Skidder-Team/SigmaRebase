package com.mentalfrostbyte.jello.module.impl.movement;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMoveRideable;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.server.SMoveVehiclePacket;
import net.minecraft.util.MovementInput;
import team.sdhq.eventBus.annotations.EventTarget;

public class EntitySpeed extends Module {
    public float field23546 = 3.0F;
    public int field23547 = 0;

    public EntitySpeed() {
        super(ModuleCategory.MOVEMENT, "EntitySpeed", "Speed up your rideable entities");
    }

    @Override
    public void onEnable() {
    }

    @EventTarget
    public void method16240(EventMoveRideable var1) {
        if (this.isEnabled()) {
            if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.getRidingEntity().onGround) {
            }

            MovementInput var4 = mc.player.movementInput;
            float var5 = var4.moveForward;
            float var6 = var4.moveStrafe;
            if (!mc.player.getRidingEntity().collidedHorizontally
                    && !mc.player.getRidingEntity().onGround
                    && BlockUtil.isAboveBounds(mc.player.getRidingEntity(), 5.0F)
                    && !mc.player.getRidingEntity().isInWater()
                    && mc.world.getBlockState(mc.player.getRidingEntity().getPosition()).getBlock() != Blocks.WATER) {
                var1.setY(-2.0);
            }

            if (mc.player.getRidingEntity().isInWater()) {
                if (mc.player.getRidingEntity().collidedHorizontally) {
                    var1.setY(-1.0);
                } else {
                    double var13 = Math.floor(mc.player.getRidingEntity().getPosY()) + 0.7725465413369861 - mc.player.getRidingEntity().getPosY();
                    var1.setY(var13 / 2.0);
                }
            }

            if (var5 == 0.0F && var6 == 0.0F) {
                var1.setX(0.0);
                var1.setZ(0.0);
            } else {
                float var7 = MovementUtil.getYaw();
                double var8 = Math.cos(Math.toRadians(var7));
                double var10 = Math.sin(Math.toRadians(var7));
                this.field23546 = 2.75F;
                float var12 = this.field23546;
                if (mc.player.getRidingEntity() instanceof HorseEntity var15) {
                    if (var15.onGround && mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.horseJumpPower = 1.0F;
                    }
                }

                var1.setX(var8 * (double) var12);
                var1.setZ(var10 * (double) var12);
                if (this.field23547 < 2 || !mc.player.getRidingEntity().onGround) {
                    mc.player.getRidingEntity().stepHeight = 0.0F;
                }
            }
        }
    }

    @EventTarget
    public void method16241(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (!(var1.packet instanceof SMoveVehiclePacket)) {
            }
        }
    }

    @EventTarget
    public void method16242(EventSendPacket var1) {
        if (var1.packet instanceof CMoveVehiclePacket
                && mc.player.getRidingEntity() != null
                && this.field23547++ > 2
                && mc.player.getRidingEntity().onGround) {
            mc.player.getRidingEntity().stepHeight = 1.0F;
        }
    }

//    @EventTarget
//    public void method16243(EventUnused var1) {
//        mc.player.getRidingEntity().stepHeight = 0.0F;
//        this.field23547 = 0;
//    }
}