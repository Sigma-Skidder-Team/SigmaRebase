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
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.SafeWalk;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
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

public class BlockFlyNCPMode extends Module {
    private float pitch;
    private float yaw;
    private BlockCache field23923;
    private int field23924 = -1;
    private int field23925;
    private int field23926;
    private Hand hand;
    private BlockFly parent = null;
    private boolean field23929;
    private boolean field23930 = false;
    private double field23931;

    private boolean updateRotation;

    public BlockFlyNCPMode() {
        super(ModuleCategory.MOVEMENT, "NCP", "Places block underneath");
        this.registerSetting(new ModeSetting("Speed Mode", "Speed mode", 0, "None", "Jump", "AAC", "Slow", "Sneak"));
        this.registerSetting(new BooleanSetting("KeepRotations", "Keeps your rotations.", true));
        this.registerSetting(new NumberSetting<>("Extend", "Extend value", 0.0F, 0.0F, 6.0F, 0.1F));
        this.registerSetting(new BooleanSetting("Downwards", "Allows you to go down when sneaking.", true));
    }

    public static Vector3d method16814(BlockPos var0, Direction var1) {
        double var4 = (double) var0.getX() + 0.5;
        double var6 = (double) var0.getY() + 0.5;
        double var8 = (double) var0.getZ() + 0.5;
        var4 += (double) var1.getXOffset() / 2.0;
        var8 += (double) var1.getZOffset() / 2.0;
        var6 += (double) var1.getYOffset() / 2.0;
        double var10 = 0.2;
        if (var1 != Direction.UP && var1 != Direction.DOWN) {
            var6 += method16815(var10, -var10);
        } else {
            var4 += method16815(var10, -var10);
            var8 += method16815(var10, -var10);
        }

        if (var1 == Direction.WEST || var1 == Direction.EAST) {
            var8 += method16815(var10, -var10);
        }

        if (var1 == Direction.SOUTH || var1 == Direction.NORTH) {
            var4 += method16815(var10, -var10);
        }

        return new Vector3d(var4, var6, var8);
    }

    public static double method16815(double var0, double var2) {
        return Math.random() * (var0 - var2) + var2;
    }

    @Override
    public void initialize() {
        this.parent = (BlockFly) this.access();
    }

    @Override
    public void onEnable() {
        this.field23924 = mc.player.inventory.currentItem;
        this.yaw = this.pitch = 999.0F;
        ((BlockFly) this.access()).lastSpoofedSlot = -1;
        if (mc.gameSettings.keyBindSneak.isKeyDown() && this.getBooleanValueFromSettingName("Downwards")) {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.field23929 = true;
        }

        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            this.field23929 = false;
        }

        this.field23931 = -1.0;
        this.field23930 = false;
        if (mc.player.isOnGround()) {
            this.field23931 = mc.player.getPosY();
        }

        this.field23926 = -1;

        this.updateRotation = false;
    }

    @Override
    public void onDisable() {
        if (this.field23924 != -1 && this.access().getStringSettingValueByName("ItemSpoof").equals("Switch")) {
            mc.player.inventory.currentItem = this.field23924;
        }

        this.field23924 = -1;


        MovementUtil.moveInDirection(MovementUtil.getSmartSpeed() * 0.9);
        mc.timer.timerSpeed = 1.0F;
        if (this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.field23926 == 0) {
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        }
    }

    @EventTarget
    public void method16805(EventSafeWalk var1) {
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
                    && (!this.field23929 || !this.getBooleanValueFromSettingName("Downwards"))) {
                var1.setSafe(true);
            }
        }
    }

    @EventTarget
    public void onKeyPress(EventKeyPress var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Downwards")) {
            if (var1.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23929 = true;
            }
        }
    }

    @EventTarget
    public void onHover(EventMouseHover var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Downwards")) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23929 = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventMotion event) {
        if (this.isEnabled() && this.parent.getValidItemCount() != 0) {
            if (event.isPre()) {
                event.setMoving(true);
                if (this.updateRotation) {
                    event.setYaw(this.yaw);
                    event.setPitch(this.pitch);
                }
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void onUpdate(EventUpdate event) {
        this.updateRotation = false;

        if (this.isEnabled() && this.parent.getValidItemCount() != 0) {
            this.hand = Hand.MAIN_HAND;

            double x = mc.player.getPosX();
            double z = mc.player.getPosZ();
            double y = mc.player.getPosY();

            if (!mc.player.collidedHorizontally && !mc.gameSettings.keyBindJump.isPressed()) {
                double[] expandPos = this.method16813();
                x = expandPos[0];
                z = expandPos[1];
            }

            if (mc.player.getMotion().y < 0.0
                    && mc.player.fallDistance > 1.0F
                    && BlockUtil.rayTrace(0.0F, 90.0F, 3.0F).getType() == RayTraceResult.Type.MISS) {
                y += Math.min(mc.player.getMotion().y * 2.0, 4.0);
            } else if ((this.getStringSettingValueByName("Speed Mode").equals("Jump")
                    || this.getStringSettingValueByName("Speed Mode").equals("Cubecraft"))
                    && !mc.gameSettings.keyBindJump.isKeyDown()) {
                y = this.field23931;
            }

            if (!BlockUtil.isValidBlockPosition(
                    new BlockPos(
                            mc.player.getPositionVec().getX(),
                            mc.player.getPositionVec().getY() - 1.0,
                            mc.player.getPositionVec().getZ()
                    )
            )) {
                x = mc.player.getPositionVec().getX();
                z = mc.player.getPositionVec().getZ();
            }

            BlockPos blockPos = new BlockPos(x, y - 1.0, z);

            if (!BlockUtil.isValidBlockPosition(blockPos)
                    && this.parent.canPlaceItem(this.hand)) {

                BlockCache blockCache = BlockUtil.findValidBlockCache(blockPos, !this.field23929 && this.getBooleanValueFromSettingName("Downwards"));
                this.field23923 = blockCache;

                if (blockCache != null) {
                    float[] rotations = BlockUtil.method34542(blockCache.position, blockCache.direction);
                    if (rotations != null) {
                        this.yaw = rotations[0];
                        this.pitch = rotations[1];
                        this.updateRotation = true;
                        RotationCore.currentYaw = this.yaw;
                        RotationCore.currentPitch = this.pitch;
                    }
                }
            } else {
                this.field23923 = null;
            }

            if (this.yaw != 999.0F) {
                this.updateRotation = true;
                RotationCore.currentYaw = this.yaw;
                RotationCore.currentPitch = this.pitch;
            }

            if (this.yaw != 999.0F) {
                this.parent.method16736();

                if (this.field23923 != null) {
                    BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(
                            BlockFlyNCPMode.method16814(this.field23923.position, this.field23923.direction),
                            this.field23923.direction,
                            this.field23923.position,
                            false
                    );

                    int currentItem = mc.player.inventory.currentItem;
                    int spoofSlot = -1;

                    if (!this.access().getStringSettingValueByName("ItemSpoof").equals("None")) {
                        spoofSlot = this.parent.getValidHotbarItemSlot();
                        if (spoofSlot != -1 && spoofSlot != currentItem) {
                            mc.getConnection().sendPacket(new CHeldItemChangePacket(spoofSlot));
                            mc.player.inventory.currentItem = spoofSlot;
                            ((BlockFly) this.access()).lastSpoofedSlot = spoofSlot;
                        }
                    }

                    mc.playerController.func_217292_a(mc.player, mc.world, this.hand, rayTraceResult);
                    this.field23923 = null;

                    if (!this.access().getBooleanValueFromSettingName("NoSwing")) {
                        mc.player.swingArm(this.hand);
                    } else {
                        mc.getConnection().sendPacket(new CAnimateHandPacket(this.hand));
                    }

                    if (this.access().getStringSettingValueByName("ItemSpoof").equals("Spoof")
                            || this.access().getStringSettingValueByName("ItemSpoof").equals("LiteSpoof")) {
                        if (spoofSlot != -1 && spoofSlot != currentItem) {
                            mc.getConnection().sendPacket(new CHeldItemChangePacket(currentItem));
                            mc.player.inventory.currentItem = currentItem;
                            ((BlockFly) this.access()).lastSpoofedSlot = -1;
                        }
                    }
                }
            }
        }
    }


    @EventTarget
    @HigherPriority
    public void method16809(EventMove var1) {
        if (this.isEnabled() && this.parent.getValidItemCount() != 0) {
            if (mc.player.isOnGround() || BlockUtil.isAboveBounds(mc.player, 0.01F)) {
                this.field23931 = mc.player.getPosY();
            }

            if (this.access().getBooleanValueFromSettingName("No Sprint")) {
                mc.player.setSprinting(false);
            }

            if (mc.player.isOnGround()) {
                this.field23926 = 0;
            } else if (this.field23926 >= 0) {
                this.field23926++;
            }

            if (this.parent == null) {
                this.parent = (BlockFly) this.access();
            }

            String var4 = this.getStringSettingValueByName("Speed Mode");
            switch (var4) {
                case "Jump":
                    if (mc.player.isOnGround() && MovementUtil.isMoving() && !mc.player.isSneaking() && !this.field23929) {
                        this.field23930 = false;
                        mc.player.jump();
                        ((Speed) Client.getInstance().moduleManager.getModuleByClass(Speed.class)).callHypixelSpeedMethod();
                        this.field23930 = true;
                        var1.setY(mc.player.getMotion().y);
                        var1.setX(mc.player.getMotion().x);
                        var1.setZ(mc.player.getMotion().z);
                    }
                    break;
                case "AAC":
                    if (this.field23925 == 0 && mc.player.isOnGround()) {
                        MovementUtil.setMotion(var1, MovementUtil.getSmartSpeed() * 0.82);
                    }
                    break;
                case "Cubecraft":
                    double var6 = 0.2;
                    float var8 = this.method16816(MathHelper.wrapDegrees(mc.player.rotationYaw));
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.timer.timerSpeed = 1.0F;
                    } else if (mc.player.isOnGround()) {
                        if (MovementUtil.isMoving() && !mc.player.isSneaking() && !this.field23929) {
                            var1.setY(1.01);
                        }
                    } else if (this.field23926 == 1) {
                        if (var1.getY() <= 0.9) {
                            this.field23926 = -1;
                        } else {
                            var1.setY(0.122);
                            mc.timer.timerSpeed = 0.7F;
                            var6 = 2.4;
                        }
                    } else if (this.field23926 == 2) {
                        if (var1.getY() > 0.05) {
                            this.field23926 = -1;
                        } else {
                            mc.timer.timerSpeed = 0.7F;
                            var6 = 0.28;
                        }
                    } else if (this.field23926 == 3) {
                        mc.timer.timerSpeed = 0.3F;
                        var6 = 2.4;
                    } else if (this.field23926 == 4) {
                        var6 = 0.28;
                        mc.timer.timerSpeed = 1.0F;
                    } else if (this.field23926 == 6) {
                        var1.setY(-1.023456987345906);
                    }

                    if (!MovementUtil.isMoving()) {
                        var6 = 0.0;
                    }

                    if (mc.player.fallDistance < 1.0F) {
                        MovementUtil.setMotion(var1, var6, var8, var8, 360.0F);
                    }

                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
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
                    break;
            }

            this.parent.onMove(var1);
        }
    }

    @EventTarget
    @LowerPriority
    public void method16810(EventSendPacket var1) {
        if (this.isEnabled() && mc.player != null) {
            if (var1.packet instanceof CHeldItemChangePacket && ((BlockFly) this.access()).lastSpoofedSlot >= 0) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16811(EventJump var1) {
        if (this.isEnabled() && this.field23930) {
            if (this.access().getStringSettingValueByName("Tower Mode").equalsIgnoreCase("Vanilla")
                    && (!MovementUtil.isMoving() || this.access().getBooleanValueFromSettingName("Tower while moving"))) {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16812(EventRender2D var1) {
        if (this.isEnabled() && this.getStringSettingValueByName("Speed Mode").equals("Cubecraft") && this.field23926 >= 0) {
            if (!(mc.player.fallDistance > 1.2F)) {
                if (!(mc.player.chasingPosY < this.field23931)) {
                    if (!mc.player.isJumping) {
                        mc.player.setPosition(mc.player.getPosX(), this.field23931, mc.player.getPosZ());
                        mc.player.lastTickPosY = this.field23931;
                        mc.player.chasingPosY = this.field23931;
                        mc.player.prevPosY = this.field23931;
                        if (MovementUtil.isMoving()) {
                            mc.player.cameraYaw = 0.099999994F;
                        }
                    }
                }
            }
        }
    }

    public double[] method16813() {
        double var3 = mc.player.getPosX();
        double var5 = mc.player.getPosZ();
        double var7 = mc.player.movementInput.moveForward;
        double var9 = mc.player.movementInput.moveStrafe;
        float var11 = mc.player.rotationYaw;
        BlockPos var12 = new BlockPos(var3, mc.player.getPosY() - 1.0, var5);
        double var13 = var3;
        double var15 = var5;
        double var17 = 0.0;

        for (double var19 = this.getNumberValueBySettingName("Extend") * 2.0F;
             BlockUtil.isValidBlockPosition(var12);
             var12 = new BlockPos(var13, mc.player.getPosY() - 1.0, var15)
        ) {
            if (++var17 > var19) {
                var17 = var19;
            }

            double cos = Math.cos(Math.toRadians(var11 + 90.0F));
            double sin = Math.sin(Math.toRadians(var11 + 90.0F));
            var13 = var3
                    + (var7 * 0.45 * cos + var9 * 0.45 * sin) * var17;
            var15 = var5
                    + (var7 * 0.45 * sin - var9 * 0.45 * cos) * var17;
            if (var17 == var19) {
                break;
            }
        }

        return new double[]{var13, var15};
    }

    public float method16816(float var1) {
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
