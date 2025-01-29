package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.SafeWalk;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import com.mentalfrostbyte.jello.util.player.Rots;
import com.mentalfrostbyte.jello.util.unmapped.BlockCache;
import com.mentalfrostbyte.jello.util.world.BlockUtil;
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
    private float pitch;
    private float yaw;
    private BlockCache field23468;
    private int field23469 = -1;
    private int field23470;
    private int field23471;
    private Hand field23472;
    private BlockFly field23473 = null;
    private boolean field23474;
    private boolean field23475 = false;
    private double field23476;

    public BlockFlyHypixelMode() {
        super(ModuleCategory.MOVEMENT, "Hypixel", "Places block underneath");
        this.registerSetting(new ModeSetting("Speed Mode", "Speed mode", 0, "None", "Jump", "AAC", "Cubecraft", "Slow", "Sneak"));
        this.registerSetting(new BooleanSetting("KeepRotations", "Keeps your rotations.", true));
        this.registerSetting(new BooleanSetting("Downwards", "Allows you to go down when sneaking.", true));
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
        this.field23473 = (BlockFly) this.access();
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

        this.field23471 = -1;
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

        MovementUtil.strafe(MovementUtil.getSpeed() * 0.9);
        mc.timer.timerSpeed = 1.0F;
        if (this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.field23471 == 0) {
            MultiUtilities.setPlayerYMotion(-0.0789);
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
                var1.setCancelled(true);
                this.field23474 = true;
            }
        }
    }

    @EventTarget
    public void onHover(EventMouseHover var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Downwards")) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.setCancelled(true);
                this.field23474 = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.isEnabled() && this.field23473.getValidItemCount() != 0) {
            ModuleWithModuleSettings var4 = (ModuleWithModuleSettings) Client.getInstance().moduleManager.getModuleByClass(Fly.class);
            if (!var4.isEnabled() || !var4.getStringSettingValueByName("Type").equalsIgnoreCase("Hypixel") || !var4.getModWithTypeSetToName().getStringSettingValueByName("Bypass").equals("Blink")) {
                if (!event.isPre()) {
                    this.field23473.method16736();
                    if (this.field23468 != null) {
                        BlockRayTraceResult var20 = new BlockRayTraceResult(
                                method16116(this.field23468.position, this.field23468.direction), this.field23468.direction, this.field23468.position, false
                        );
                        int var21 = mc.player.inventory.currentItem;
                        if (!this.access().getStringSettingValueByName("ItemSpoof").equals("None")) {
                            this.field23473.switchToValidHotbarItem();
                        }

                        mc.playerController.func_217292_a(mc.player, mc.world, this.field23472, var20);
                        if (!this.access().getBooleanValueFromSettingName("NoSwing")) {
                            mc.player.swingArm(this.field23472);
                        } else {
                            mc.getConnection().sendPacket(new CAnimateHandPacket(this.field23472));
                        }

                        if (this.access().getStringSettingValueByName("ItemSpoof").equals("Spoof") || this.access().getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")) {
                            mc.player.inventory.currentItem = var21;
                        }
                    }
                } else {
                    this.field23470++;
                    event.setMoving(true);
                    this.field23472 = Hand.MAIN_HAND;
                    if (BlockFly.shouldPlaceItem(mc.player.getHeldItem(Hand.OFF_HAND).getItem())
                            && (
                            mc.player.getHeldItem(this.field23472).isEmpty()
                                    || !BlockFly.shouldPlaceItem(mc.player.getHeldItem(this.field23472).getItem())
                    )) {
                        this.field23472 = Hand.OFF_HAND;
                    }

                    double var5 = event.getX();
                    double var7 = event.getZ();
                    double var9 = event.getY();
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
                    if (!BlockUtil.isValidBlockPosition(var11) && this.field23473.canPlaceItem(this.field23472)) {
                        BlockCache var12 = BlockUtil.findValidBlockCache(var11, !this.field23474 && this.getBooleanValueFromSettingName("Downwards"));
                        this.field23468 = var12;
                        if (var12 != null) {
                            float[] var13 = BlockUtil.method34542(this.field23468.position, this.field23468.direction);
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
                            Rots.rotating = true;
                            Rots.prevPitch = this.pitch;
                            Rots.prevYaw = this.yaw;
                            event.setYaw(this.yaw);
                            event.setPitch(this.pitch);
                            Rots.pitch = this.pitch;
                            Rots.yaw = this.yaw;

                            mc.player.rotationYawHead = this.yaw;
                            mc.player.renderYawOffset = this.yaw;
                        }
                    } else {
                        if (this.getBooleanValueFromSettingName("KeepRotations") && this.pitch != 999.0F) {
                            Rots.rotating = true;
                            Rots.prevPitch = 90.0F;
                            Rots.prevYaw = mc.player.rotationYaw + 1.0F;
                            event.setPitch(90.0F);
                            event.setYaw(mc.player.rotationYaw + 1.0F);
                            Rots.pitch = 90.0F;
                            Rots.yaw = mc.player.rotationYaw + 1.0F;

                            mc.player.rotationYawHead = mc.player.rotationYaw + 1.0F;
                            mc.player.renderYawOffset = mc.player.rotationYaw + 1.0F;
                        }

                        this.field23468 = null;
                    }

                    if (mc.player.rotationYaw != event.getYaw() && mc.player.rotationPitch != event.getPitch()) {
                        this.field23470 = 0;
                    }
                }
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16112(EventMove var1) {
        if (this.isEnabled() && this.field23473.getValidItemCount() != 0) {
            if (mc.player.isOnGround() || MultiUtilities.isAboveBounds(mc.player, 0.01F)) {
                this.field23476 = mc.player.getPosY();
            }

            if (this.access().getBooleanValueFromSettingName("No Sprint")) {
                mc.player.setSprinting(false);
            }

            if (mc.player.isOnGround()) {
                this.field23471 = 0;
            } else if (this.field23471 >= 0) {
                this.field23471++;
            }

            if (this.field23473 == null) {
                this.field23473 = (BlockFly) this.access();
            }

            String var4 = this.getStringSettingValueByName("Speed Mode");
            switch (var4) {
                case "Jump":
                    if (mc.player.isOnGround() && MultiUtilities.isMoving() && !mc.player.isSneaking() && !this.field23474) {
                        this.field23475 = false;
                        mc.player.jump();
                        ((Speed) Client.getInstance().moduleManager.getModuleByClass(Speed.class)).callHypixelSpeedMethod();
                        this.field23475 = true;
                        var1.setY(mc.player.getMotion().y);
                        var1.setX(mc.player.getMotion().x);
                        var1.setZ(mc.player.getMotion().z);
                    }
                    break;
                case "AAC":
                    if (this.field23470 == 0 && mc.player.isOnGround()) {
                        MovementUtil.setSpeed(var1, MovementUtil.getSpeed() * 0.82);
                    }
                    break;
                case "Cubecraft":
                    double var6 = 0.2;
                    float var8 = this.method16118(MathHelper.wrapDegrees(mc.player.rotationYaw));
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.timer.timerSpeed = 1.0F;
                    } else if (mc.player.isOnGround()) {
                        if (MultiUtilities.isMoving() && !mc.player.isSneaking() && !this.field23474) {
                            var1.setY(1.00000000000001);
                        }
                    } else if (this.field23471 == 1) {
                        if (var1.getY() <= 0.9) {
                            this.field23471 = -1;
                        } else {
                            var1.setY(0.122);
                            mc.timer.timerSpeed = 0.7F;
                            var6 = 2.4;
                        }
                    } else if (this.field23471 == 2) {
                        if (var1.getY() > 0.05) {
                            this.field23471 = -1;
                        } else {
                            mc.timer.timerSpeed = 0.7F;
                            var6 = 0.28;
                        }
                    } else if (this.field23471 == 3) {
                        mc.timer.timerSpeed = 0.3F;
                        var6 = 2.4;
                    } else if (this.field23471 == 4) {
                        var6 = 0.28;
                        mc.timer.timerSpeed = 1.0F;
                    } else if (this.field23471 == 6) {
                        var1.setY(-1.023456987345906);
                    }

                    if (!MultiUtilities.isMoving()) {
                        var6 = 0.0;
                    }

                    if (mc.player.fallDistance < 1.0F) {
                        MovementUtil.setSpeed(var1, var6, var8, var8, 360.0F);
                    }

                    MultiUtilities.setPlayerYMotion(var1.getY());
                    break;
                case "Slow":
                    if (mc.player.isOnGround()) {
                        var1.setX(var1.getX() * 0.75);
                        var1.setZ(var1.getZ() * 0.75);
                    } else {
                        var1.setX(var1.getX() * 0.93);
                        var1.setZ(var1.getZ() * 0.93);
                    }
                    break;
                case "Sneak":
                    if (mc.player.isOnGround()) {
                        var1.setX(var1.getX() * 0.65);
                        var1.setZ(var1.getZ() * 0.65);
                    } else {
                        var1.setX(var1.getX() * 0.85);
                        var1.setZ(var1.getZ() * 0.85);
                    }
            }

            this.field23473.onMove(var1);
        }
    }

    @EventTarget
    @LowerPriority
    public void method16113(EventSendPacket var1) {
        if (this.isEnabled() && mc.player != null) {
            if (var1.getPacket() instanceof CHeldItemChangePacket && ((BlockFly) this.access()).lastSpoofedSlot >= 0) {
                var1.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void method16114(EventJump var1) {
        if (this.isEnabled() && this.field23475) {
            if (this.access().getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")
                    && (!MultiUtilities.isMoving() || this.access().getBooleanValueFromSettingName("Tower while moving"))) {
                var1.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void method16115(EventRender2D var1) {
        if (this.isEnabled() && this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.field23471 >= 0) {
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

    public float method16118(float var1) {
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
