package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.SafeWalk;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import com.mentalfrostbyte.jello.util.player.Rots;
import com.mentalfrostbyte.jello.util.unmapped.BlockCache;
import com.mentalfrostbyte.jello.util.world.BlockUtil;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class BlockFlySmoothMode extends Module {
    private float pitch;
    private float yaw;
    private BlockCache blockCache;
    private int previousItem = -1;
    private int rotationStabilityCounter;
    private int offGroundTicks;
    private Hand hand;
    private BlockFly blockFly = null;
    private boolean called = false;
    private double posY;
    private int someOtherField = 0;

    public BlockFlySmoothMode() {
        super(ModuleCategory.MOVEMENT, "Smooth", "Places block underneath");
        this.registerSetting(new ModeSetting("Speed Mode", "Speed mode", 0, "None", "Jump", "AAC", "Cubecraft", "Slow", "Sneak"));
    }

    @Override
    public void initialize() {
        this.blockFly = (BlockFly) this.access();
    }

    @Override
    public void onEnable() {
        this.previousItem = mc.player.inventory.currentItem;
        this.yaw = this.pitch = 999.0F;
        ((BlockFly) this.access()).lastSpoofedSlot = -1;
        this.posY = -1.0;
        this.called = false;
        if (mc.player.isOnGround()) {
            this.posY = mc.player.getPosY();
        }

        this.offGroundTicks = -1;
    }

    @Override
    public void onDisable() {
        if (this.previousItem != -1 && this.access().getStringSettingValueByName("ItemSpoof").equals("Switch")) {
            mc.player.inventory.currentItem = this.previousItem;
        }

        this.previousItem = -1;
        if (((BlockFly) this.access()).lastSpoofedSlot >= 0) {
            mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            ((BlockFly) this.access()).lastSpoofedSlot = -1;
        }

        MovementUtil.strafe(MovementUtil.getSpeed() * 0.9);
        mc.timer.timerSpeed = 1.0F;
        if (this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.offGroundTicks == 0) {
            MultiUtilities.setPlayerYMotion(-0.0789);
        }
    }

    @EventTarget
    public void onSafeWalk(EventSafeWalk event) {
        if (this.isEnabled()) {
            if (this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
                if (mc.world.getCollisionShapes(
                                mc.player,
                                mc.player.getBoundingBox().expand(0.0, -1.5, 0.0).contract(0.05, 0.0, 0.05).contract(-0.05, 0.0, -0.05)
                        ).count()
                        == 0L
                        && mc.player.fallDistance < 1.0F) {
                    event.setSafe(true);
                }
            } else if (mc.player.isOnGround() && Client.getInstance().moduleManager.getModuleByClass(SafeWalk.class).isEnabled()) {
                event.setSafe(true);
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && this.blockFly.getValidItemCount() != 0) {
            if (!event.isPre()) {
                if (this.yaw != 999.0F) {
                    this.blockFly.method16736();
                    if (this.blockCache != null) {
                        BlockRayTraceResult rayTraceResult = BlockUtil.rayTrace(this.yaw, this.pitch, 5.0F, event);
                        if (rayTraceResult.getType() == RayTraceResult.Type.MISS) {
                            return;
                        }

                        if (rayTraceResult.getFace() == Direction.UP
                                && (double) rayTraceResult.getPos().getY() <= mc.player.getPosY() - 1.0
                                && mc.player.isOnGround()) {
                            return;
                        }

                        int currentItem = mc.player.inventory.currentItem;
                        if (!this.access().getStringSettingValueByName("ItemSpoof").equals("None")) {
                            this.blockFly.switchToValidHotbarItem();
                        }

                        new ItemUseContext(mc.player, Hand.MAIN_HAND, rayTraceResult);
                        mc.playerController.func_217292_a(mc.player, mc.world, this.hand, rayTraceResult);
                        this.blockCache = null;
                        if (!this.access().getBooleanValueFromSettingName("NoSwing")) {
                            mc.player.swingArm(this.hand);
                        } else {
                            mc.getConnection().sendPacket(new CAnimateHandPacket(this.hand));
                        }

                        if (this.access().getStringSettingValueByName("ItemSpoof").equals("Spoof") || this.access().getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")) {
                            mc.player.inventory.currentItem = currentItem;
                        }
                    }
                }
            } else {
                this.rotationStabilityCounter++;
                this.someOtherField--;
                event.setMoving(true);
                this.hand = Hand.MAIN_HAND;
                if (BlockFly.shouldPlaceItem(mc.player.getHeldItem(Hand.OFF_HAND).getItem())
                        && (
                        mc.player.getHeldItem(this.hand).isEmpty()
                                || !BlockFly.shouldPlaceItem(mc.player.getHeldItem(this.hand).getItem())
                )) {
                    this.hand = Hand.OFF_HAND;
                }

                double x = event.getX();
                double y = event.getZ();
                double z = event.getY();
                if (!mc.player.collidedHorizontally && !mc.gameSettings.keyBindJump.isPressed()) {
                    double[] expandPos = this.getExpandedPosition();
                    x = expandPos[0];
                    y = expandPos[1];
                }

                if (mc.player.getMotion().y < 0.0
                        && mc.player.fallDistance > 1.0F
                        && BlockUtil.rayTrace(0.0F, 90.0F, 3.0F).getType() == RayTraceResult.Type.MISS) {
                    z += Math.min(mc.player.getMotion().y * 2.0, 4.0);
                } else if ((this.getStringSettingValueByName("Speed Mode").equals("Jump") || this.getStringSettingValueByName("Speed Mode").equals("Cubecraft"))
                        && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    z = this.posY;
                }

                if (!BlockUtil.isValidBlockPosition(
                        new BlockPos(
                                mc.player.getPositionVec().getX(),
                                mc.player.getPositionVec().getY() - 1.0,
                                mc.player.getPositionVec().getZ()
                        )
                )) {
                    x = mc.player.getPositionVec().getX();
                    y = mc.player.getPositionVec().getZ();
                }

                BlockPos blockPos = new BlockPos(x, z - 1.0, y);
                if (!BlockUtil.isValidBlockPosition(blockPos) && this.blockFly.canPlaceItem(this.hand) && this.someOtherField <= 0) {
                    BlockCache blockCache = BlockUtil.findValidBlockCache(blockPos, false);
                    this.blockCache = blockCache;

                    float[] rotations = BlockUtil.getRotationsToBlock();

                    if (blockCache != null && rotations != null) {
                        this.yaw = rotations[0];
                        this.pitch = rotations[1];
                    }
                } else {
                    this.blockCache = null;
                }

                if (this.yaw != 999.0F) {
                    Rots.rotating = true;
                    Rots.prevYaw = this.yaw;
                    Rots.prevPitch = this.pitch;
                    event.setYaw(this.yaw);
                    event.setPitch(this.pitch);
                    Rots.yaw = this.yaw;
                    Rots.pitch = this.pitch;

                    mc.player.rotationYawHead = event.getYaw();
                    mc.player.renderYawOffset = event.getYaw();
                } else {
                    Rots.rotating = false;
                }

                if (mc.player.rotationYaw != event.getYaw() && mc.player.rotationPitch != event.getPitch()) {
                    this.rotationStabilityCounter = 0;
                }
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void onMove(EventMove event) {
        if (this.isEnabled() && this.blockFly.getValidItemCount() != 0) {
            if (mc.player.isOnGround() || MultiUtilities.isAboveBounds(mc.player, 0.01F)) {
                this.posY = mc.player.getPosY();
            }

            if (this.access().getBooleanValueFromSettingName("No Sprint")) {
                mc.player.setSprinting(false);
            }

            if (mc.player.isOnGround()) {
                this.offGroundTicks = 0;
            } else if (this.offGroundTicks >= 0) {
                this.offGroundTicks++;
            }

            if (this.blockFly == null) {
                this.blockFly = (BlockFly) this.access();
            }

            switch (this.getStringSettingValueByName("Speed Mode")) {
                case "Jump":
                    if (mc.player.isOnGround() && MultiUtilities.isMoving() && !mc.player.isSneaking()) {
                        this.called = false;
                        mc.player.jump();
                        ((Speed) Client.getInstance().moduleManager.getModuleByClass(Speed.class)).callHypixelSpeedMethod();
                        this.called = true;
                        event.setY(mc.player.getMotion().y);
                        event.setX(mc.player.getMotion().x);
                        event.setZ(mc.player.getMotion().z);
                    }
                    break;
                case "AAC":
                    if (this.rotationStabilityCounter == 0 && mc.player.isOnGround()) {
                        MovementUtil.setSpeed(event, MovementUtil.getSpeed() * 0.82);
                    }
                    break;
                case "Cubecraft":
                    double speed = 0.2;
                    float newYaw = this.getCorrectedYaw(MathHelper.wrapDegrees(mc.player.rotationYaw));
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.timer.timerSpeed = 1.0F;
                    } else if (mc.player.isOnGround()) {
                        if (MultiUtilities.isMoving() && !mc.player.isSneaking()) {
                            event.setY(1.00000000000001);
                        }
                    } else if (this.offGroundTicks == 1) {
                        if (event.getY() <= 0.9) {
                            this.offGroundTicks = -1;
                        } else {
                            event.setY(0.122);
                            mc.timer.timerSpeed = 0.7F;
                            speed = 2.4;
                        }
                    } else if (this.offGroundTicks == 2) {
                        if (event.getY() > 0.05) {
                            this.offGroundTicks = -1;
                        } else {
                            mc.timer.timerSpeed = 0.7F;
                            speed = 0.28;
                        }
                    } else if (this.offGroundTicks == 3) {
                        mc.timer.timerSpeed = 0.3F;
                        speed = 2.4;
                    } else if (this.offGroundTicks == 4) {
                        speed = 0.28;
                        mc.timer.timerSpeed = 1.0F;
                    } else if (this.offGroundTicks == 6) {
                        event.setY(-1.023456987345906);
                    }

                    if (!MultiUtilities.isMoving()) {
                        speed = 0.0;
                    }

                    if (mc.player.fallDistance < 1.0F) {
                        MovementUtil.setSpeed(event, speed, newYaw, newYaw, 360.0F);
                    }

                    MultiUtilities.setPlayerYMotion(event.getY());
                    break;
                case "Slow":
                    if (mc.player.isOnGround()) {
                        event.setX(event.getX() * 0.75);
                        event.setZ(event.getZ() * 0.75);
                    } else {
                        event.setX(event.getX() * 0.93);
                        event.setZ(event.getZ() * 0.93);
                    }
                    break;
                case "Sneak":
                    if (mc.player.isOnGround()) {
                        event.setX(event.getX() * 0.65);
                        event.setZ(event.getZ() * 0.65);
                    } else {
                        event.setX(event.getX() * 0.85);
                        event.setZ(event.getZ() * 0.85);
                    }
            }

            this.blockFly.onMove(event);
        }
    }

    @EventTarget
    @LowerPriority
    public void onPacket(EventSendPacket event) {
        if (this.isEnabled() && mc.player != null) {
            if (event.getPacket() instanceof CHeldItemChangePacket && ((BlockFly) this.access()).lastSpoofedSlot >= 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (this.isEnabled() && this.called) {
            if (this.access().getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")
                    && (!MultiUtilities.isMoving() || this.access().getBooleanValueFromSettingName("Tower while moving"))) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void on2D(EventRender2D event) {
        if (this.isEnabled() && this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.offGroundTicks >= 0) {
            if (!(mc.player.fallDistance > 1.2F)) {
                if (!(mc.player.chasingPosY < this.posY)) {
                    if (!mc.player.isJumping) {
                        mc.player.setPosition(mc.player.getPosX(), this.posY, mc.player.getPosZ());
                        mc.player.lastTickPosY = this.posY;
                        mc.player.chasingPosY = this.posY;
                        mc.player.prevPosY = this.posY;
                        if (MovementUtil.isMoving()) {
                            mc.player.cameraYaw = 0.099999994F;
                        }
                    }
                }
            }
        }
    }

    public double[] getExpandedPosition() {
        double x = mc.player.getPosX();
        double y = mc.player.getPosZ();
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        BlockPos blockPos = new BlockPos(x, mc.player.getPosY() - 1.0, y);
        double newX = x;
        double newY = y;
        double extend = 0.0;

        for (double extendMultiplied = this.getNumberValueBySettingName("Extend") * 2.0F;
             BlockUtil.isValidBlockPosition(blockPos);
             blockPos = new BlockPos(newX, mc.player.getPosY() - 1.0, newY)
        ) {
            if (++extend > extendMultiplied) {
                extend = extendMultiplied;
            }

            double sin = Math.sin(Math.toRadians(yaw + 90.0F));
            double cos = Math.cos(Math.toRadians(yaw + 90.0F));
            newX = x
                    + (forward * 0.45 * cos + strafe * 0.45 * sin) * extend;
            newY = y
                    + (forward * 0.45 * sin - strafe * 0.45 * cos) * extend;
            if (extend == extendMultiplied) {
                break;
            }
        }

        return new double[]{newX, newY};
    }

    public float getCorrectedYaw(float yaw) {
        float newY = 0.0F;
        float strafe = mc.player.moveStrafing;
        float forward = mc.player.moveForward;
        if (!(strafe > 0.0F)) {
            if (strafe < 0.0F) {
                if (!(forward > 0.0F)) {
                    if (!(forward < 0.0F)) {
                        yaw += 90.0F;
                    } else {
                        yaw -= 45.0F;
                    }
                } else {
                    yaw += 45.0F;
                }
            }
        } else if (!(forward > 0.0F)) {
            if (!(forward < 0.0F)) {
                yaw -= 90.0F;
            } else {
                yaw += 45.0F;
            }
        } else {
            yaw -= 45.0F;
        }

        if (yaw >= 45.0F && yaw <= 135.0F) {
            newY = 90.0F;
        } else if (yaw >= 135.0F || yaw <= -135.0F) {
            newY = 180.0F;
        } else if (yaw <= -45.0F && yaw >= -135.0F) {
            newY = -90.0F;
        } else if (yaw >= -45.0F && yaw <= 45.0F) {
            newY = 0.0F;
        }

        if (forward < 0.0F) {
            newY -= 180.0F;
        }

        return newY + 90.0F;
    }
}
