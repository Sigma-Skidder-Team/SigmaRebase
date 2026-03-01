package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.SafeWalk;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.InvManagerUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.rotation.RotationCore;
import com.mentalfrostbyte.jello.util.game.world.pathing.BlockCache;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class BlockFlyHypixelMode extends Module {
    private final NumberSetting<Float> constantSpeed;
    private float pitch;
    private float yaw;
    private BlockCache blockCache;
    private int field23469 = -1;
    private int rotationStabilityCounter;
    private int offGroundTicks;
    private Hand hand;
    private BlockFly blockFly = null;
    private boolean field23474;
    private boolean field23475 = false;
    private double field23476;

    private boolean useRotation;

    public BlockFlyHypixelMode() {
        super(ModuleCategory.MOVEMENT, "Hypixel", "Places block underneath");
        this.registerSetting(new ModeSetting("Speed Mode", "Speed mode", 0, "None", "Jump", "Constant", "AAC", "Cubecraft", "Slow", "Sneak"));
        this.registerSetting(new BooleanSetting("KeepRotations", "Keeps your rotations.", true));
        this.registerSetting(new BooleanSetting("Downwards", "Allows you to go down when sneaking.", true));
        this.registerSetting(this.constantSpeed = new NumberSetting<>("Constant Speed", "Constant speed", 0.0F, 0.0F, 6.0F, 0.1F));
    }

    public static Vector3d method16116(BlockPos var0, Direction var1) {
        double var4 = (double) var0.getX() + 0.5;
        double var6 = (double) var0.getY() + 0.5;
        double var8 = (double) var0.getZ() + 0.5;
        var4 += (double) var1.getXOffset() / 2.0;
        var8 += (double) var1.getZOffset() / 2.0;
        var6 += (double) var1.getYOffset() / 2.0;
        double var10 = 0.2;
        if (var1 != Direction.UP && var1 != Direction.DOWN) {
            var6 += method16117(var10, -var10);
        } else {
            var4 += method16117(var10, -var10);
            var8 += method16117(var10, -var10);
        }

        if (var1 == Direction.WEST || var1 == Direction.EAST) {
            var8 += method16117(var10, -var10);
        }

        if (var1 == Direction.SOUTH || var1 == Direction.NORTH) {
            var4 += method16117(var10, -var10);
        }

        return new Vector3d(var4, var6, var8);
    }

    public static double method16117(double var0, double var2) {
        return Math.random() * (var0 - var2) + var2;
    }

    @Override
    public void initialize() {
        this.blockFly = (BlockFly) this.access();
    }

    @Override
    public void onEnable() {
        this.field23469 = mc.player.inventory.currentItem;
        this.yaw = this.pitch = 999.0F;
        ((BlockFly) this.access()).lastSpoofedSlot = -1;
        if (mc.gameSettings.keyBindSneak.isKeyDown() && this.getBooleanValueFromSettingName("Downwards")) {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.field23474 = true;
        }

        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            this.field23474 = false;
        }

        this.field23476 = -1.0;
        this.field23475 = false;
        if (mc.player.isOnGround()) {
            this.field23476 = mc.player.getPosY();
        }

        this.offGroundTicks = -1;

        this.useRotation = false;
    }

    @Override
    public void onDisable() {
        if (this.field23469 != -1 && this.access().getStringSettingValueByName("ItemSpoof").equals("Switch")) {
            mc.player.inventory.currentItem = this.field23469;
        }

        this.field23469 = -1;
        if (((BlockFly) this.access()).lastSpoofedSlot >= 0) {
            mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            ((BlockFly) this.access()).lastSpoofedSlot = -1;
        }

        MovementUtil.moveInDirection(MovementUtil.getSmartSpeed() * 0.9);
        mc.timer.timerSpeed = 1.0F;
        if (this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.offGroundTicks == 0) {
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        }
    }

    @EventTarget
    public void method16108(EventSafeWalk var1) {
        if (this.isEnabled()) {
            if (this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
                if (mc.world
                        .getCollisionShapes(
                                mc.player,
                                mc.player.getBoundingBox().expand(0.0, -1.5, 0.0).contract(0.05, 0.0, 0.05).contract(-0.05, 0.0, -0.05)
                        )
                        .count()
                        == 0L
                        && mc.player.fallDistance < 1.0F) {
                    var1.setSafe(true);
                }
            } else if (mc.player.isOnGround()
                    && Client.getInstance().moduleManager.getModuleByClass(SafeWalk.class).isEnabled()
                    && (!this.field23474 || !this.getBooleanValueFromSettingName("Downwards"))) {
                var1.setSafe(true);
            }
        }
    }

    @EventTarget
    public void onKeyPress(EventKeyPress var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Downwards")) {
            if (var1.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23474 = true;
            }
        }
    }

    @EventTarget
    public void onHover(EventMouseHover var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Downwards")) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23474 = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventMotion event) {
        if (this.isEnabled() && this.blockFly.getValidItemCount() != 0) {
            ModuleWithModuleSettings var4 = (ModuleWithModuleSettings) Client.getInstance().moduleManager.getModuleByClass(Fly.class);
            if (!var4.isEnabled() || !var4.getStringSettingValueByName("Type").equalsIgnoreCase("Hypixel") || !var4.getModWithTypeSetToName().getStringSettingValueByName("Bypass").equals("Blink")) {
                if (!event.isPre()) {
                    this.blockFly.method16736();
                    if (this.blockCache != null) {
                        BlockRayTraceResult var20 = new BlockRayTraceResult(
                                method16116(this.blockCache.position, this.blockCache.direction), this.blockCache.direction, this.blockCache.position, false
                        );
                        int var21 = mc.player.inventory.currentItem;
                        if (!this.access().getStringSettingValueByName("ItemSpoof").equals("None")) {
                            this.blockFly.switchToValidHotbarItem();
                        }

                        mc.playerController.func_217292_a(mc.player, mc.world, this.hand, var20);
                        if (!this.access().getBooleanValueFromSettingName("NoSwing")) {
                            mc.player.swingArm(this.hand);
                        } else {
                            mc.getConnection().sendPacket(new CAnimateHandPacket(this.hand));
                        }

                        if (this.access().getStringSettingValueByName("ItemSpoof").equals("Spoof") || this.access().getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")) {
                            mc.player.inventory.currentItem = var21;
                        }
                    }
                } else {
                    event.setMoving(true);

                    if (this.useRotation) {
                        event.setYaw(this.yaw);
                        event.setPitch(this.pitch);
                    } else {
                        if (this.getBooleanValueFromSettingName("KeepRotations") && this.pitch != 999.0F) {
                            event.setPitch(90.0F);
                            event.setYaw(mc.player.rotationYaw + 1.0F);
                        }
                    }

                    if (mc.player.rotationYaw != event.getYaw() && mc.player.rotationPitch != event.getPitch()) {
                        this.rotationStabilityCounter = 0;
                    }
                }
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventUpdate event) {
        this.useRotation = false;
        if (this.isEnabled() && this.blockFly.getValidItemCount() != 0) {
            ModuleWithModuleSettings var4 = (ModuleWithModuleSettings) Client.getInstance().moduleManager.getModuleByClass(Fly.class);
            if (!var4.isEnabled() || !var4.getStringSettingValueByName("Type").equalsIgnoreCase("Hypixel") || !var4.getModWithTypeSetToName().getStringSettingValueByName("Bypass").equals("Blink")) {
                this.rotationStabilityCounter++;
                this.hand = Hand.MAIN_HAND;
                if (InvManagerUtil.shouldPlaceItem(mc.player.getHeldItem(Hand.OFF_HAND).getItem())
                        && (
                        mc.player.getHeldItem(this.hand).isEmpty()
                                || !InvManagerUtil.shouldPlaceItem(mc.player.getHeldItem(this.hand).getItem())
                )) {
                    this.hand = Hand.OFF_HAND;
                }

                double var5 = mc.player.getPosX();
                double var7 = mc.player.getPosZ();
                double var9 = mc.player.getPosY();
                if (mc.player.getMotion().y < 0.0
                        && mc.player.fallDistance > 1.0F
                        && BlockUtil.rayTrace(0.0F, 90.0F, 3.0F).getType() == RayTraceResult.Type.MISS) {
                    var9 += Math.min(mc.player.getMotion().y * 2.0, 4.0);
                } else if (this.field23474 && this.getBooleanValueFromSettingName("Downwards")) {
                    var9--;
                } else if ((this.getStringSettingValueByName("Speed Mode").equals("Jump") || this.getStringSettingValueByName("Speed Mode").equals("Cubecraft"))
                        && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    var9 = this.field23476;
                }

                if (!BlockUtil.isValidBlockPosition(
                        new BlockPos(
                                mc.player.getPositionVec().getX(),
                                mc.player.getPositionVec().getY() - 1.0,
                                mc.player.getPositionVec().getZ()
                        )
                )) {
                    var5 = mc.player.getPositionVec().getX();
                    var7 = mc.player.getPositionVec().getZ();
                }

                BlockPos var11 = new BlockPos(var5, var9 - 1.0, var7);
                if (!BlockUtil.isValidBlockPosition(var11) && this.blockFly.canPlaceItem(this.hand)) {
                    BlockCache var12 = BlockUtil.findValidBlockCache(var11, !this.field23474 && this.getBooleanValueFromSettingName("Downwards"));
                    this.blockCache = var12;
                    if (var12 != null) {
                        float[] var13 = BlockUtil.method34542(this.blockCache.position, this.blockCache.direction);
                        if ((double) var12.position.getY() - mc.player.getPosY() < 0.0) {
                            double var14 = mc.player.getPosX()
                                    - ((double) var12.position.getX() + 0.5 + (double) var12.direction.getXOffset() / 2.0);
                            double var16 = mc.player.getPosZ()
                                    - ((double) var12.position.getZ() + 0.5 + (double) var12.direction.getZOffset() / 2.0);
                            double var18 = Math.sqrt(var14 * var14 + var16 * var16);
                            if (var18 < 2.0) {
                                var13[0] = mc.player.rotationYaw + 1.0F;
                                var13[1] = 90.0F;
                            }
                        }

                        this.yaw = var13[0];
                        this.pitch = var13[1];
                        this.useRotation = true;
                        RotationCore.currentYaw = this.yaw;
                        RotationCore.currentPitch = this.pitch;
                    }
                } else {
                    if (this.getBooleanValueFromSettingName("KeepRotations") && this.pitch != 999.0F) {
                        RotationCore.currentYaw = mc.player.rotationYaw + 1.0F;
                        RotationCore.currentPitch = 90.0F;
                    }

                    this.blockCache = null;
                }
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16112(EventMove event) {
        if (this.isEnabled() && this.blockFly.getValidItemCount() != 0) {
            if (mc.player.isOnGround() || BlockUtil.isAboveBounds(mc.player, 0.01F)) {
                this.field23476 = mc.player.getPosY();
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

            String var4 = this.getStringSettingValueByName("Speed Mode");
            switch (var4) {
                case "Jump":
                    if (mc.player.isOnGround() && MovementUtil.isMoving() && !mc.player.isSneaking() && !this.field23474) {
                        this.field23475 = false;
                        mc.player.jump();
                        ((Speed) Client.getInstance().moduleManager.getModuleByClass(Speed.class)).callHypixelSpeedMethod();
                        this.field23475 = true;
                        event.setY(mc.player.getMotion().y);
                        event.setX(mc.player.getMotion().x);
                        event.setZ(mc.player.getMotion().z);
                    }
                    break;
                case "Constant": {
                    double speed = this.constantSpeed.currentValue;
                    if (!MovementUtil.isMoving())
                        speed = 0;
                    MovementUtil.setMotion(event, speed);
                    break;
                }
                case "AAC":
                    if (this.rotationStabilityCounter == 0 && mc.player.isOnGround()) {
                        MovementUtil.setMotion(event, MovementUtil.getSmartSpeed() * 0.82);
                    }
                    break;
                case "Cubecraft":
                    double speed = 0.2;
                    float newYaw = this.getCorrectedYaw(MathHelper.wrapDegrees(mc.player.rotationYaw));
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.timer.timerSpeed = 1.0F;
                    } else if (mc.player.isOnGround()) {
                        if (MovementUtil.isMoving() && !mc.player.isSneaking() && !this.field23474) {
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

                    if (!MovementUtil.isMoving()) {
                        speed = 0.0;
                    }

                    if (mc.player.fallDistance < 1.0F) {
                        MovementUtil.setMotion(event, speed, newYaw, newYaw, 360.0F);
                    }

                    mc.player.setMotion(mc.player.getMotion().x, event.getY(), mc.player.getMotion().z);
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
    public void method16113(EventSendPacket var1) {
        if (this.isEnabled() && mc.player != null) {
            if (var1.packet instanceof CHeldItemChangePacket && ((BlockFly) this.access()).lastSpoofedSlot >= 0) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16114(EventJump var1) {
        if (this.isEnabled() && this.field23475) {
            if (this.access().getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")
                    && (!MovementUtil.isMoving() || this.access().getBooleanValueFromSettingName("Tower while moving"))) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16115(EventRender2D var1) {
        if (this.isEnabled() && this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.offGroundTicks >= 0) {
            if (!(mc.player.fallDistance > 1.2F)) {
                if (!(mc.player.chasingPosY < this.field23476)) {
                    if (!mc.player.isJumping) {
                        mc.player.setPosition(mc.player.getPosX(), this.field23476, mc.player.getPosZ());
                        mc.player.lastTickPosY = this.field23476;
                        mc.player.chasingPosY = this.field23476;
                        mc.player.prevPosY = this.field23476;
                        if (MovementUtil.isMoving()) {
                            mc.player.cameraYaw = 0.099999994F;
                        }
                    }
                }
            }
        }
    }

    public float getCorrectedYaw(float var1) {
        float var4 = 0.0F;
        float var5 = mc.player.moveStrafing;
        float var6 = mc.player.moveForward;
        if (!(var5 > 0.0F)) {
            if (var5 < 0.0F) {
                if (!(var6 > 0.0F)) {
                    if (!(var6 < 0.0F)) {
                        var1 += 90.0F;
                    } else {
                        var1 -= 45.0F;
                    }
                } else {
                    var1 += 45.0F;
                }
            }
        } else if (!(var6 > 0.0F)) {
            if (!(var6 < 0.0F)) {
                var1 -= 90.0F;
            } else {
                var1 += 45.0F;
            }
        } else {
            var1 -= 45.0F;
        }

        if (var1 >= 45.0F && var1 <= 135.0F) {
            var4 = 90.0F;
        } else if (var1 >= 135.0F || var1 <= -135.0F) {
            var4 = 180.0F;
        } else if (var1 <= -45.0F && var1 >= -135.0F) {
            var4 = -90.0F;
        } else if (var1 >= -45.0F && var1 <= 45.0F) {
            var4 = 0.0F;
        }

        if (var6 < 0.0F) {
            var4 -= 180.0F;
        }

        return var4 + 90.0F;
    }
}