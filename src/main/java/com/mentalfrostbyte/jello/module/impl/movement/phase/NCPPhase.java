package com.mentalfrostbyte.jello.module.impl.movement.phase;


import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.world.EventPushBlock;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.PremiumModule;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class NCPPhase extends PremiumModule {
    public boolean field23651;
    public int field23652;
    public int field23653;

    public NCPPhase() {
        super(ModuleCategory.MOVEMENT, "NCP", "Phase for NCP anticheat");
        this.registerSetting(new BooleanSetting("Hypixel", "Hypixel bypass", true));
    }

    @Override
    public void onEnable() {
        this.field23651 = false;
        if (!mc.player.collidedHorizontally) {
            this.field23652 = -1;
        } else {
            this.field23652 = 0;
            if (mc.player.isOnGround()) {
                double var3 = mc.player.getPosX();
                double var5 = mc.player.getPosY();
                double var7 = mc.player.getPosZ();
                mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var3, var5 + 0.0626, var7, false));
            }
        }
    }

    @EventTarget
    public void method16426(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && event.isPre()) {
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                double var4 = mc.player.getPosX();
                double var6 = mc.player.getPosY();
                double var8 = mc.player.getPosZ();
                if (!MovementUtil.isMoving()) {
                    if (BlockUtil.isAboveBounds(mc.player, 0.001F) && !PlayerUtil.isCollidingWithSurroundingBlocks()) {
                        mc.player.setPosition(var4, var6 - 1.0, var8);
                        event.setY(var6 - 1.0);
                        event.setMoving(true);
                        event.setYaw(event.getYaw() + 10.0F);
                        mc.player.setMotion(mc.player.getMotion().x, 0.0, mc.player.getMotion().z);
                    } else if (mc.player.getPosY() == (double) ((int) mc.player.getPosY())) {
                        mc.player.setPosition(var4, var6 - 0.3, var8);
                    }
                }
            }

            if (this.field23652 > 0 && this.field23651 && PlayerUtil.isCollidingWithSurroundingBlocks()) {
                this.field23653++;
                float var10 = (float) Math.sin(this.field23653) * 5.0F;
                float var11 = (float) Math.cos(this.field23653) * 5.0F;
                event.setYaw(event.getYaw() + var10);
                event.setPitch(event.getPitch() + var11);
            } else if (this.field23652 < 0) {
                return;
            }

            event.setMoving(true);
        }
    }

    @EventTarget
    public void method16427(EventMove var1) {
        if (this.isEnabled()) {
            if (mc.player.collidedHorizontally && this.field23652 != 0) {
                this.field23652 = 0;
                if (mc.player.isOnGround()) {
                    double var4 = mc.player.getPosX();
                    double var6 = mc.player.getPosY();
                    double var8 = mc.player.getPosZ();
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(var4, var6 + 0.0626, var8, false));
                }
            }

            if (this.field23652 >= 0) {
                if (this.field23652 != 0) {
                    if (!PlayerUtil.isCollidingWithSurroundingBlocks()) {
                        this.field23651 = false;
                        MovementUtil.setMotion(var1, 0.0);
                        return;
                    }

                    if (!this.field23651) {
                        MovementUtil.setMotion(var1, !this.getBooleanValueFromSettingName("Hypixel") ? 0.0031 : 0.03);
                    } else {
                        MovementUtil.setMotion(var1, 0.617);
                    }
                } else {
                    MovementUtil.setMotion(var1, 0.0);
                    MovementUtil.movePlayerInDirection(6.000000238415E-4);
                }

                this.field23652++;
            }
        }
    }

    @EventTarget
    public void method16428(EventPushBlock var1) {
        if (this.isEnabled()) {
            var1.cancelled = true;
        }
    }

    @EventTarget
    public void method16429(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var5 = (SPlayerPositionLookPacket) var4;
                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
                this.field23651 = true;
            }
        }
    }
}
