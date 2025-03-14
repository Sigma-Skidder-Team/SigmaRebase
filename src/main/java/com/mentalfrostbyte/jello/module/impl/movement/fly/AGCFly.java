package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.math.shapes.VoxelShape;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.block.Block;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class AGCFly extends Module {
    private int preUpdates;
    private int field23903;
    private boolean down;

    public AGCFly() {
        super(ModuleCategory.MOVEMENT, "AGC", "A fly for AntiGamingChair");
    }

    @Override
    public void onEnable() {
        this.preUpdates = 0;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            this.down = false;
        } else {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.down = true;
        }

        this.field23903 = 1;
    }

    @EventTarget
    public void onKeyPress(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (var1.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.down = true;
            }
        }
    }

    @EventTarget
    public void onHover(EventMouseHover var1) {
        if (this.isEnabled()) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.down = false;
            }
        }
    }

    @Override
    public void onDisable() {
        MovementUtil.moveInDirection(0.0);
        if (mc.player.getMotion().y > 0.0) {
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        }
    }

    @EventTarget
    @LowerPriority
    public void onMove(EventMove var1) {
        if (this.isEnabled()) {
            if (this.preUpdates <= (this.field23903 != 3 ? this.field23903 : 1) - 2) {
                if (this.preUpdates == -1) {
                    var1.setY(this.field23903 != 3 ? 0.001 : 0.095);
                    if (this.field23903 != 3) {
                        MovementUtil.setMotion(var1, 0.32);
                    }

                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                }
            } else {
                var1.setY(0.0);
                MovementUtil.setMotion(var1, 0.0);
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.preUpdates++;
            if (this.preUpdates != (this.field23903 != 3 ? this.field23903 : 1)) {
                if (this.preUpdates > (this.field23903 != 3 ? this.field23903 : 1)) {
                    if (this.preUpdates % 20 != 0) {
                        var1.cancelled = true;
                    } else {
                        double var4 = this.method16785();
                        var1.setY(var4 - 1.0E-4);
                        var1.setMoving(true);
                        var1.setOnGround(true);
                    }
                }
            } else {
                double var6 = this.method16785();
                var1.setY(var6 - 1.0E-4);
                var1.setOnGround(true);
                var1.setMoving(true);
                this.field23903 = !this.down
                        ? (!mc.gameSettings.keyBindJump.isKeyDown() ? 1 : 3)
                        : (!mc.gameSettings.keyBindJump.isKeyDown() ? 2 : 1);
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof SPlayerPositionLookPacket var5) {
				if (this.preUpdates >= (this.field23903 != 3 ? this.field23903 : 1)) {
                    this.preUpdates = -1;
                }

                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
            }
        }
    }

    private double method16785() {
        if (!(mc.player.getPositionVec().y < 1.0)) {
            if (!mc.player.isOnGround()) {
                AxisAlignedBB var3 = mc.player.getBoundingBox().expand(0.0, -mc.player.getPositionVec().y, 0.0);
                Iterator var4 = mc.world.getCollisionShapes(mc.player, var3).iterator();
                double var5 = -1.0;
                BlockPos var7 = null;

                while (var4.hasNext()) {
                    VoxelShape var8 = (VoxelShape) var4.next();
                    if (var8.getBoundingBox().maxY > var5) {
                        var5 = var8.getBoundingBox().maxY;
                        var7 = new BlockPos(var8.getBoundingBox().minX, var8.getBoundingBox().minY,
                                var8.getBoundingBox().minZ);
                    }
                }

                if (var7 != null) {
                    Block var9 = mc.world.getBlockState(var7).getBlock();
                    if (var9 instanceof SnowBlock) {
                        var5 = (double) ((int) var5) - 1.0E-4;
                    }
                }

                return var5;
            } else {
                return mc.player.getPosY();
            }
        } else {
            return -1.0;
        }
    }
}
