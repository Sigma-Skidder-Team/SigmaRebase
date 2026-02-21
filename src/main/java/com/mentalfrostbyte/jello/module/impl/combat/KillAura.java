package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;

import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.player.action.EventPlace;
import com.mentalfrostbyte.jello.event.impl.player.action.EventStopUseItem;
import com.mentalfrostbyte.jello.event.impl.player.action.EventUseItem;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.*;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ColorSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.player.rotation.JelloAI;
import com.mentalfrostbyte.jello.util.game.player.rotation.RotationCore;
import com.mentalfrostbyte.jello.util.game.player.rotation.util.RotationUtils;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings({"unused", "cast"})
public class KillAura extends Module {
    public static boolean isActive = false;
    public static Entity targetEntity;
    public static TimedEntity targetData;
    private Rotation rotation = new Rotation(0.0F, 0.0F);
    public static int attackCooldown;
    private final ModeSetting rotationMode;
    private final NumberSetting<Float> rotationSpeed;
    private final BooleanSetting useRotationSpeed;
    private final BooleanSetting hitEvent;
    public HashMap<Entity, Animation> entityAnimation = new HashMap<>();
    public static InteractAutoBlock autoBlock;
    private int attackTimer;
    private int animationTimer;
    private float eventUpdateYaw, eventUpdatePitch;
    private int attackDelay;
    private int blockDelay;
    private int animationProgress;
    private int currentSlot;
    private int blockCooldown;

    private List<TimedEntity> targets;

    private Rotation currentRotation;
    private Rotation lastRotation;

    private double yawDifference;
    private float targetYaw;
    private float targetPitch;
    private float smoothYaw;

    private boolean isBlocking;

    // Fields for JelloAI hit tracking
    private Entity lastAttackEntity = null;
    private long lastAttackTime = 0;
    private boolean lastAttackWasMoving = false;

    public KillAura() {
        super(ModuleCategory.COMBAT, "KillAura", "Automatically attacks entities");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Single", "Switch", "Multi", "Multi2"));
        this.registerSetting(new ModeSetting("Autoblock Mode", "Autoblock Mode", 0, "None", "NCP", "Basic1", "Basic2", "Basic3", "Vanilla"));
        this.registerSetting(new NumberSetting<>("Unblock Rate", "Unlock Ticks for Vanilla AutoBlock mode.", 0, 0, 2, 1) {
            @Override
            public boolean isHidden() {
                return !getStringSettingValueByName("Autoblock Mode").equals("Vanilla");
            }
        });
        this.registerSetting(new ModeSetting("Sort Mode", "Sort Mode", 0, "Range", "Health", "Angle", "Armor", "Prev Range"));
        this.registerSetting(this.rotationMode = new ModeSetting("Rotation Mode",
                "The way you will look at entities",
                0,
                "NCP",
                "AAC", "Smooth",
                "LockView", "Test",
                "Test2", "JelloAI", "None")
        );
        this.registerSetting(this.useRotationSpeed = new BooleanSetting("Use Rotation Speed", "Max rotation change per tick.", true));
        this.registerSetting(this.rotationSpeed = new NumberSetting<>("Rotation Speed", "Max rotation change per tick.", 6.0F, 6.0F, 360, 6F));
        this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F, 2.8F, 8.0F, 0.01F));
        this.registerSetting(
                new NumberSetting<>("Min CPS", "Min CPS value", 8.0F, 1.0F, 20.0F, 1.0F).addObserver(var1 -> autoBlock.initializeCpsTimings())
        );
        this.registerSetting(
                new NumberSetting<>("Max CPS", "Max CPS value", 8.0F, 1.0F, 20.0F, 1.0F).addObserver(var1 -> autoBlock.initializeCpsTimings())
        );
        this.registerSetting(new BooleanSetting("Interact autoblock", "Send interact packet when blocking", true));
        this.registerSetting(this.hitEvent = new BooleanSetting("HitEvent", "Change the hit event (vanilla autoblock?legit)", true));
        this.registerSetting(new BooleanSetting("Players", "Hit players", true));
        this.registerSetting(new BooleanSetting("Animals", "Hit animals", false));
        this.registerSetting(new BooleanSetting("Monsters", "Hit monsters", false));
        this.registerSetting(new BooleanSetting("Invisible", "Hit invisible entites", true));
        this.registerSetting(new BooleanSetting("Raytrace", "Helps the aura become more legit", true));
        this.registerSetting(new BooleanSetting("Cooldown", "Use attack cooldown (1.9+)", false));
        this.registerSetting(new BooleanSetting("No swing", "Hit without swinging", false));
        this.registerSetting(new BooleanSetting("Disable on death", "Disable on death", true));
        this.registerSetting(new BooleanSetting("Through walls", "Target entities through walls", true));
        this.registerSetting(new BooleanSetting("Silent", "Silent rotations", true));
        this.registerSetting(new BooleanSetting("ESP", "ESP on targets", true));
        this.registerSetting(new ColorSetting("ESP Color", "The render color", ClientColors.LIGHT_GREYISH_BLUE.getColor()));
    }

    public static Rotation getCurrentRotation(KillAura var0) {
        return var0.currentRotation;
    }

    public static List<TimedEntity> getTargets(KillAura var0) {
        return var0.targets;
    }

    public static int getAttackDelay(KillAura var0) {
        return var0.attackDelay;
    }

    public static void setAttackDelya(KillAura var0, int var1) {
        var0.attackDelay = var1;
    }


    @Override
    public void initialize() {
        this.targets = new ArrayList<TimedEntity>();
        autoBlock = new InteractAutoBlock(this);
        super.initialize();
    }

    @Override
    public void onEnable() {
        this.targets = new ArrayList<TimedEntity>();
        targetEntity = null;
        targetData = null;
        this.attackTimer = (int) autoBlock.getCpsTiming(0);
        this.animationTimer = 0;
        this.attackDelay = 0;
        attackCooldown = 0;
        this.lastRotation = new Rotation(mc.player.lastReportedYaw, mc.player.lastReportedPitch);
        this.currentRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        rotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        this.targetPitch = -1.0F;
        autoBlock
                .setBlockingState(mc.player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof SwordItem && mc.gameSettings.keyBindUseItem.isKeyDown());
        this.isBlocking = false;
        this.currentSlot = -1;
        this.entityAnimation.clear();

        // Initialize JelloAI when KillAura is enabled
        if (rotationMode.currentValue.equals("JelloAI")) {
            JelloAI.init();
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        targetEntity = null;
        targetData = null;
        this.targets = null;
        isActive = false;

        // Reset JelloAI rotations when KillAura is disabled
        if (rotationMode.currentValue.equals("JelloAI")) {
            JelloAI.updateRotations();
        }

        super.onDisable();
    }

    @EventTarget
    public void onWorldLoadd(EventLoadWorld var1) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Disable on death")) {
            Client.getInstance().notificationManager.send(new Notification("Aura", "Aura disabled due to respawn"));
            this.toggle();
        }
    }

    @EventTarget
    public void onTick2(EventUpdate var1) {
        if (this.isEnabled()) {
            if (this.targetPitch != -1.0F) {
                this.targetPitch++;
            }

            if (this.getBooleanValueFromSettingName("Disable on death")) {
                if (!mc.player.isAlive()) {
                    this.toggle();
                    Client.getInstance().notificationManager
                            .send(new Notification("Aura", "Aura disabled due to death"));
                }
            }
        }
    }

    @EventTarget
    public void onStopUseItem(EventStopUseItem var1) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (this.isEnabled()) {
            if (!this.getStringSettingValueByName("Autoblock Mode").equals("None") && !this.getStringSettingValueByName("Autoblock Mode").equals("Vanilla")
                    && (mc.player.getHeldItemMainhand().getItem() instanceof SwordItem || this.blockDelay != mc.player.inventory.currentItem)
                    && targetEntity != null) {
                var1.cancelled = true;
            } else if (mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) {
                this.animationProgress = 2;
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void onTick(EventUpdate var1) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (mc.player != null) {
            if (this.animationProgress > 0) {
                this.animationProgress--;
            }

            if (targetEntity != null && autoBlock.isBlocking() && MovementUtil.isMoving() && this.getStringSettingValueByName("Autoblock Mode").equals("NCP")) {
                autoBlock.stopAutoBlock();
            }

            if (autoBlock.isBlocking() && (!(mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) || targetEntity == null)) {
                autoBlock.setBlockingState(false);
            }

            if (this.currentSlot >= 0) {
                if (this.currentSlot == 0) {
                    autoBlock.stopAutoBlock();
                    autoBlock.setBlockingState(true);
                }

                this.currentSlot--;
            }

            this.updateTargetSelection();

            if (this.targets != null && !this.targets.isEmpty()) {
                this.attackTimer++;
                    /*
                    ModuleWithModuleSettings var5 = (ModuleWithModuleSettings) Client.getInstance().getModuleManager().getModuleByClass(Criticals.class);
                    if (var5.isEnabled() && var5.getStringSettingValueByName("Type").equalsIgnoreCase("Minis")) {
                        this.handleCriticals(var1, var5.method16726().getStringSettingValueByName("Mode"), var5.method16726().getBooleanValueFromSetttingName("Avoid Fall Damage"));
                    }

                     */

                this.updateRotation();

                // Add JelloAI rotation handling
                if (rotationMode.currentValue.equals("JelloAI") && targetEntity != null) {
                    // Let JelloAI handle the rotations
                    JelloAI.faceEntity(targetEntity);

                    // Update current rotation with JelloAI values
                    this.currentRotation.yaw = JelloAI.getCurrentYaw();
                    this.currentRotation.pitch = JelloAI.getCurrentPitch();

                    // Update RotationCore values
                    RotationCore.currentYaw = this.currentRotation.yaw;
                    RotationCore.currentPitch = this.currentRotation.pitch;
                } else if (eventUpdateYaw - mc.player.rotationYaw != 0.0F && (rotationMode.currentValue.equals("Test1") || rotationMode.currentValue.equals("Test")) && mc.player.ticksExisted % 50 == 0) {
                    this.currentRotation.yaw = eventUpdateYaw;
                    this.currentRotation.pitch = eventUpdatePitch;
                }

                // Only apply GCD and rotation limits if not using JelloAI
                if (!rotationMode.currentValue.equals("JelloAI")) {
                    float hSpeed = rotationSpeed.currentValue;
                    float vSpeed = rotationSpeed.currentValue;

                    Rotation lastCopy = new Rotation(lastRotation.yaw, lastRotation.pitch);
                    Rotation currentCopy = new Rotation(currentRotation.yaw, currentRotation.pitch);
                    Rotation limit = !useRotationSpeed.currentValue
                            ? currentCopy
                            : RotationUtils.limitAngleChange(lastCopy, currentCopy, hSpeed, vSpeed);

                    float[] limitedRotation = new float[]{limit.yaw, limit.pitch};
                    float[] oldRots = {mc.player.lastReportedYaw, mc.player.lastReportedPitch};

                    currentRotation.yaw = RotationUtils.gcdFix(limitedRotation, oldRots)[0];
                    currentRotation.pitch = RotationUtils.gcdFix(limitedRotation, oldRots)[1];

                    RotationCore.currentYaw = currentRotation.yaw;
                    RotationCore.currentPitch = currentRotation.pitch;
                }

                // Update JelloAI rotations every tick
                if (rotationMode.currentValue.equals("JelloAI")) {
                    JelloAI.updateRotations();
                }

                mc.gameRenderer.getMouseOver(1.0F); // might fix issue with slow raytrace update

                if (!this.hitEvent.currentValue) {
                    attack();
                }

                if (attackCooldown > 0) {
                    attackCooldown--;
                }
            }
        }
    }


    @EventTarget
    @HighestPriority
    public void method16821(EventPlace var1) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (this.targets != null && !this.targets.isEmpty()) {
            if (this.hitEvent.currentValue) {
                attack();
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void method16821(EventMotion event) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (this.isEnabled() && this.targets != null && !this.targets.isEmpty()) {
            if (this.getBooleanValueFromSettingName("Silent")) {
                // If using JelloAI, use its rotation values
                if (rotationMode.currentValue.equals("JelloAI")) {
                    // Use reflection to set the fields since they're private
                    try {
                        // Get the field and make it accessible
                        java.lang.reflect.Field yawField = event.getClass().getDeclaredField("yaw");
                        java.lang.reflect.Field pitchField = event.getClass().getDeclaredField("pitch");

                        yawField.setAccessible(true);
                        pitchField.setAccessible(true);

                        // Set the values
                        yawField.set(event, JelloAI.getCurrentYaw());
                        pitchField.set(event, JelloAI.getCurrentPitch());
                    } catch (Exception e) {
                        Client.logger.error("Error setting rotation values", e);
                    }
                } else {
                    // Use reflection for non-JelloAI rotations too
                    try {
                        java.lang.reflect.Field yawField = event.getClass().getDeclaredField("yaw");
                        java.lang.reflect.Field pitchField = event.getClass().getDeclaredField("pitch");

                        yawField.setAccessible(true);
                        pitchField.setAccessible(true);

                        yawField.set(event, this.currentRotation.yaw);
                        pitchField.set(event, this.currentRotation.pitch);
                    } catch (Exception e) {
                        Client.logger.error("Error setting rotation values", e);
                    }
                }
            }

            if (!event.isPre()) {
                this.blockDelay = mc.player.inventory.currentItem;
            } else {
                if (targetEntity != null && autoBlock.canAutoBlock() && this.currentRotation != null) {
                    autoBlock.performAutoBlock(targetEntity, this.currentRotation.yaw, this.currentRotation.pitch);
                }
                eventUpdateYaw = event.getYaw();
                eventUpdatePitch = event.getPitch();

                if (targetEntity != null && !this.targets.isEmpty()) {
                    event.setYaw(RotationCore.currentYaw);
                    event.setPitch(RotationCore.currentPitch);
                }
            }
        }
    }

    @EventTarget
    public void method16822(EventRender2DOffset var1) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (targetData != null && !this.getBooleanValueFromSettingName("Silent") && !this.rotationMode.currentValue.equals("None")) {
            float var4 = MathHelper.wrapAngleTo180_float(this.lastRotation.yaw + (this.currentRotation.yaw - this.lastRotation.yaw) * mc.getRenderPartialTicks());
            float var5 = MathHelper.wrapAngleTo180_float(this.lastRotation.pitch + (this.currentRotation.pitch - this.lastRotation.pitch) * mc.getRenderPartialTicks());
            mc.player.rotationYaw = var4;
            mc.player.rotationPitch = var5;
        }
    }

    @EventTarget
    @HighestPriority
    public void meth2od10(EventUseItem event) {
        if (targetEntity != null) {
            if (mc.objectMouseOver != null) {
                if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
                    if (mc.player.getHeldItemMainhand() != null &&
                            mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) {

                        if (getStringSettingValueByName("Autoblock Mode").equals("Vanilla")) {
                            int value = (int) getNumberValueBySettingName("Unblock Rate");

                            if (mc.player.ticksExisted % (value == 0 ? 0 : value + 1) == 0) {
                                event.useItem = true;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void method16823(EventRender3D var1) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (this.targets != null) {
            Iterator<Entry<Entity, Animation>> var4 = this.entityAnimation.entrySet().iterator();

            while (var4.hasNext()) {
                Entry<Entity, Animation> var5 = var4.next();

                var5.getValue().changeDirection(Animation.Direction.BACKWARDS);
                if (var5.getValue().calcPercent() == 0.0F) {
                    var4.remove();
                }
            }

            for (TimedEntity var10 : this.targets) {
                if (var10 != null) {
                    if (!this.entityAnimation.containsKey(var10.getEntity())) {
                        this.entityAnimation.put(var10.getEntity(), new Animation(250, 250));
                    } else {
                        this.entityAnimation.get(var10.getEntity()).changeDirection(Animation.Direction.FORWARDS);
                    }
                }
            }

            for (Entry<Entity, Animation> var11 : this.entityAnimation.entrySet()) {
                AuraESP auraESP = new AuraESP(this);
                auraESP.renderEsp(var11.getKey());
            }
        }
    }

    public boolean isAutoblockActive() {
        return targetEntity != null
                && mc.player.getHeldItemMainhand() != null
                && mc.player.getHeldItemMainhand().getItem() instanceof SwordItem
                && !this.getStringSettingValueByName("Autoblock Mode").equals("None") && !this.getStringSettingValueByName("Autoblock Mode").equals("Vanilla");
    }

    @Override
    public boolean isEnabled2() {
        return this.isEnabled() && this.isAutoblockActive();
    }


    private Entity getClosestTargetInBlockRange(List<TimedEntity> var1) {
        var1 = autoBlock.sortTargets(var1);
        return !var1.isEmpty() && var1.get(0).getEntity().getDistance(mc.player) <= this.getNumberValueBySettingName("Range")
                ? var1.get(0).getEntity()
                : null;
    }

    private void updateTargetSelection() {
        float rangeF = this.getNumberValueBySettingName("Range");
        String mode = this.getStringSettingValueByName("Mode");
        List<TimedEntity> targetList = autoBlock.getPotentialTargets(rangeF);
        targetList = autoBlock.sortTargets(targetList);
        if (this.currentRotation == null) {
            this.onEnable();
        }

        if (targetList != null && !targetList.isEmpty() && !mc.gameSettings.keyBindAttack.isPressed()) {
            targetEntity = this.getClosestTargetInBlockRange(targetList);
            targetList = autoBlock.getPotentialTargets(rangeF);
            if (mode.equals("Single") || mode.equals("Multi")) {
                targetList = autoBlock.sortTargets(targetList);
            }

            if (targetList.isEmpty()) {
                targetData = null;
                this.targets.clear();
                this.attackTimer = (int) autoBlock.getCpsTiming(0);
                this.animationTimer = 0;
                isActive = false;
                this.currentRotation.yaw = mc.player.rotationYaw;
                this.currentRotation.pitch = mc.player.rotationPitch;
                rotation.yaw = this.currentRotation.yaw;
                rotation.pitch = this.currentRotation.pitch;
                this.targetPitch = -1.0F;
                this.yawDifference = Math.random();
                this.currentSlot = -1;
            } else {
                if (this.targetPitch == -1.0F) {
                    float var7 = RotationUtils.getRotationsToPosition(RotationUtils.getEntityPosition(targetList.get(0).getEntity())).yaw;
                    float var8 = Math.abs(RotationUtils.getAngleDifference2(var7, rotation.yaw));
                    this.targetYaw = var8 * 1.95F / 50.0F;
                    this.targetPitch++;
                    this.yawDifference = Math.random();
                }

                this.targets = targetList;
                float var12 = RotationUtils.getRotationsToPosition(RotationUtils.getEntityPosition(this.targets.get(0).getEntity())).yaw;
                if (!this.targets.isEmpty() & !mode.equals("Switch")) {
                    if (targetData != null && targetData.getEntity() != this.targets.get(0).getEntity()) {
                        float var13 = Math.abs(RotationUtils.getAngleDifference2(var12, rotation.yaw));
                        this.targetYaw = var13 * 1.95F / 50.0F;
                        this.yawDifference = Math.random();
                    }

                    targetData = this.targets.get(0);
                }

                if (!mode.equals("Switch")) {
                    if (!mode.equals("Multi2")) {
                        if (mode.equals("Single")
                                && !this.targets.isEmpty()
                                && (targetData == null || targetData.getEntity() != this.targets.get(0).getEntity())) {
                            targetData = this.targets.get(0);
                        }
                    } else {
                        if (this.attackDelay >= this.targets.size()) {
                            this.attackDelay = 0;
                        }

                        targetData = this.targets.get(this.attackDelay);
                    }
                } else if ((
                        targetData == null
                                || targetData.getTimer() == null
                                || targetData.isExpired()
                                || !this.targets.contains(targetData)
                                || mc.player.getDistance(targetData.getEntity()) > rangeF
                )
                        && !this.targets.isEmpty()) {
                    if (this.attackDelay + 1 < this.targets.size()) {
                        if (targetData != null && !Client.getInstance().friendManager.isFriend(this.targets.get(this.attackDelay).getEntity())) {
                            this.attackDelay++;
                        }
                    } else {
                        this.attackDelay = 0;
                    }

                    Vector3d var14 = RotationUtils.getEntityPosition(this.targets.get(this.attackDelay).getEntity());
                    float var9 = Math.abs(RotationUtils.getAngleDifference2(RotationUtils.getRotationsToPosition(var14).yaw, rotation.yaw));
                    this.targetYaw = var9 * 1.95F / 50.0F;
                    this.yawDifference = Math.random();
                    targetData = new TimedEntity(
                            this.targets.get(this.attackDelay).getEntity(), new ExpirationTimer(!this.rotationMode.currentValue.equals("NCP") ? 500L : 270L)
                    );
                }

                if (this.attackDelay >= this.targets.size()) {
                    this.attackDelay = 0;
                }

                if (!mode.equals("Multi")) {
                    this.targets.clear();
                    this.targets.add(targetData);
                }
            }
        } else {
            targetData = null;
            targetEntity = null;
            if (this.targets != null) {
                this.targets.clear();
            }

            this.attackTimer = (int) autoBlock.getCpsTiming(0);
            this.animationTimer = 0;
            isActive = false;
            this.currentRotation.yaw = mc.player.rotationYaw;
            this.currentRotation.pitch = mc.player.rotationPitch;
            rotation.yaw = this.currentRotation.yaw;
            rotation.pitch = this.currentRotation.pitch;
            this.targetPitch = -1.0F;
            this.yawDifference = Math.random();
            this.currentSlot = -1;
        }
    }

    private void updateRotation() {
        Entity entity = targetData.getEntity();

        Rotation advancedRotation = RotationUtils.getAdvancedRotation(entity, !this.getBooleanValueFromSettingName("Through walls"));

        float targetYawDifference = RotationUtils.wrapAngleDifference(this.currentRotation.yaw, advancedRotation.yaw);
        float targetPitchDifference = advancedRotation.pitch - this.currentRotation.pitch;

        lastRotation.yaw = currentRotation.yaw;
        lastRotation.pitch = currentRotation.pitch;

        switch (rotationMode.currentValue) {
            case "Test":
                float yawSpeed = 10.0F;
                float pitchSpeed = 10.0F;

                if (Math.abs(targetYawDifference) > yawSpeed) {
                    targetYawDifference = yawSpeed * Math.signum(targetYawDifference);
                }

                if (Math.abs(targetPitchDifference) > pitchSpeed) {
                    targetPitchDifference = pitchSpeed * Math.signum(targetPitchDifference);
                }

                this.currentRotation.yaw += targetYawDifference;
                this.currentRotation.pitch += targetPitchDifference;
                break;
            case "Test2":
                float yawSpeed2 = 10.0F;
                float pitchSpeed2 = 10.0F;

                if (Math.abs(targetYawDifference) > yawSpeed2) {
                    targetYawDifference = yawSpeed2 * Math.signum(targetYawDifference);
                }

                if (Math.abs(targetPitchDifference) > pitchSpeed2) {
                    targetPitchDifference = pitchSpeed2 * Math.signum(targetPitchDifference);
                }

                this.currentRotation.yaw += targetYawDifference;
                this.currentRotation.pitch += targetPitchDifference;
                break;
            case "NCP":
                this.currentRotation.yaw = advancedRotation.yaw;
                this.currentRotation.pitch = advancedRotation.pitch;
                break;
            case "AAC":
                float yawSpeed3 = 10.0F;
                float pitchSpeed3 = 10.0F;

                if (Math.abs(targetYawDifference) > yawSpeed3) {
                    targetYawDifference = yawSpeed3 * Math.signum(targetYawDifference);
                }

                if (Math.abs(targetPitchDifference) > pitchSpeed3) {
                    targetPitchDifference = pitchSpeed3 * Math.signum(targetPitchDifference);
                }

                this.currentRotation.yaw += targetYawDifference;
                this.currentRotation.pitch += targetPitchDifference;
                break;
            case "Smooth":
                float yawSpeed4 = 10.0F;
                float pitchSpeed4 = 10.0F;

                if (Math.abs(targetYawDifference) > yawSpeed4) {
                    targetYawDifference = yawSpeed4 * Math.signum(targetYawDifference);
                }

                if (Math.abs(targetPitchDifference) > pitchSpeed4) {
                    targetPitchDifference = pitchSpeed4 * Math.signum(targetPitchDifference);
                }

                this.currentRotation.yaw += targetYawDifference;
                this.currentRotation.pitch += targetPitchDifference;
                break;
            case "LockView":
                this.currentRotation.yaw = advancedRotation.yaw;
                this.currentRotation.pitch = advancedRotation.pitch;
                break;
            case "JelloAI":
                // JelloAI rotation handling is done in onTick method
                break;
            case "None":
                this.currentRotation.yaw = mc.player.rotationYaw;
                this.currentRotation.pitch = mc.player.rotationPitch;
                break;
        }

        this.currentRotation.pitch = MathHelper.clamp(this.currentRotation.pitch, -90.0F, 90.0F);
    }

    private void attack() {
        if (this.attackTimer >= autoBlock.getCpsTiming(0)) {
            if (targetData != null && targetData.getEntity() != null) {
                Entity entity = targetData.getEntity();
                if (entity.getDistance(mc.player) <= this.getNumberValueBySettingName("Range")) {
                    boolean canAttack = true;
                    if (this.getBooleanValueFromSettingName("Raytrace")) {
                        RayTraceResult result = mc.objectMouseOver;
                        if (result == null || result.getType() != RayTraceResult.Type.ENTITY ||
                                !(result instanceof EntityRayTraceResult) ||
                                ((EntityRayTraceResult) result).getEntity() != entity) {
                            canAttack = false;
                        }
                    }

                    if (canAttack) {
                        // Existing attack code
                        if (autoBlock.isBlocking()) {
                            autoBlock.stopAutoBlock();
                        }

                        if (!this.getBooleanValueFromSettingName("No swing")) {
                            mc.playerController.attackEntity(mc.player, entity);
                            mc.player.swingArm(Hand.MAIN_HAND);
                        }
                        // Record successful hit for JelloAI
                        if (rotationMode.currentValue.equals("JelloAI")) {
                            boolean wasMoving = MovementUtil.isMoving();
                            lastAttackEntity = entity;
                            lastAttackTime = System.currentTimeMillis();
                            lastAttackWasMoving = wasMoving;
                        }

                        // Rest of existing attack code
                        this.attackTimer = 0;
                        isActive = true;

                        if (this.getStringSettingValueByName("Mode").equals("Multi2")) {
                            this.attackDelay++;
                        }
                    } else if (rotationMode.currentValue.equals("JelloAI")) {
                        // Record miss for JelloAI
                        JelloAI.recordMiss(entity);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        // Handle hit detection for JelloAI learning
        if (rotationMode.currentValue.equals("JelloAI") && lastAttackEntity != null &&
                System.currentTimeMillis() - lastAttackTime < 500) {

            if (event.packet instanceof SEntityStatusPacket packet) {
                // Check if this is a hit confirmation packet (status 2 = hurt)
                if (packet.getOpCode() == 2) {
                    Entity hitEntity = packet.getEntity(mc.world);

                    // If this is the entity we just attacked, record the hit
                    if (hitEntity != null && hitEntity.equals(lastAttackEntity)) {
                        JelloAI.recordHit(hitEntity, lastAttackWasMoving);
                        lastAttackEntity = null; // Reset to avoid duplicate recordings
                    }
                }
            }
        }
    }
}