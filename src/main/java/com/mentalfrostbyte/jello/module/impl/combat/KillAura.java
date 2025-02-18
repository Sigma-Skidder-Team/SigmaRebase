package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.action.EventInputOptions;
import com.mentalfrostbyte.jello.event.impl.player.action.EventPlace;
import com.mentalfrostbyte.jello.event.impl.player.action.EventStopUseItem;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMoveRelative;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.*;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ColorSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.rotation.RotationCore;
import com.mentalfrostbyte.jello.util.client.rotation.util.RandomUtil;
import com.mentalfrostbyte.jello.util.client.rotation.util.RotationHelper;
import com.mentalfrostbyte.jello.util.client.rotation.util.RotationUtils;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import com.mentalfrostbyte.jello.util.system.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.SwordItem;
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
    public static Rotation rotation = new Rotation(0.0F, 0.0F);
    public static int attackCooldown;
    private final ModeSetting rotationMode;
    private final NumberSetting<Float> rotationSpeed;
    private final BooleanSetting useRotationSpeed;
    private final BooleanSetting hitEvent;
    float[] kaROT;
    public HashMap<Entity, Animation> entityAnimation = new HashMap<>();
    public static InteractAutoBlock autoBlock;
    private int attackTimer;
    private int animationTimer;
    private float eventUpdateYaw, eventUpdatePitch;
    private int targetSwitchDelay;
    private int attackDelay;
    private int blockDelay;
    private int field23944;
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
    private double[] previusROP;

    public KillAura() {
        super(ModuleCategory.COMBAT, "KillAura", "Automatically attacks entities");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Single", "Switch", "Multi", "Multi2"));
        this.registerSetting(new ModeSetting("Autoblock Mode", "Autoblock Mode", 0, "None", "NCP", "Basic1", "Basic2", "Basic3", "Vanilla"));
        this.registerSetting(new NumberSetting<>("Unblock Rate", "Unlock Ticks for Vanilla AutoBlock mode.", 0, Integer.class, 0, 2, 1) {
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
                "Test2", "None")
        );
        this.registerSetting(this.useRotationSpeed = new BooleanSetting("Use Rotation Speed", "Max rotation change per tick.", true));
        this.registerSetting(this.rotationSpeed = new NumberSetting<>("Rotation Speed", "Max rotation change per tick.", 6.0F, Float.class, 6.0F, 360, 6F));
        this.registerSetting(new BooleanSetting("Movement Fix", "Fix the XZ motion depending on your yaw.", false));
        this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F, Float.class, 2.8F, 8.0F, 0.01F));
        this.registerSetting(
                new NumberSetting<>("Min CPS", "Min CPS value", 8.0F, Float.class, 1.0F, 20.0F, 1.0F).addObserver(var1 -> this.autoBlock.initializeCpsTimings())
        );
        this.registerSetting(
                new NumberSetting<>("Max CPS", "Max CPS value", 8.0F, Float.class, 1.0F, 20.0F, 1.0F).addObserver(var1 -> this.autoBlock.initializeCpsTimings())
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

    public static InteractAutoBlock method16844(KillAura var0) {
        return autoBlock;
    }

    public static List<TimedEntity> getTargets(KillAura var0) {
        return var0.targets;
    }

    public static int method16846(KillAura var0) {
        return var0.attackDelay;
    }

    public static void method16847(KillAura var0, int var1) {
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
        if (mc.player.onGround) {
            this.targetSwitchDelay = 1;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        targetEntity = null;
        targetData = null;
        this.targets = null;
        isActive = false;
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
    public void method16819(EventPlayerTick var1) {
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
    public void method16820(EventStopUseItem var1) {
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
    public void onMoveRelative(EventMoveRelative event) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (getBooleanValueFromSettingName("Movement Fix")) {
            if (targetEntity != null && !this.targets.isEmpty())
                event.setYaw(RotationCore.currentYaw);
        }
    }

    @EventTarget
    @HighestPriority
    public void m2et125h32od10(EventPlayerTick var1) {
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
                float var4 = 0.07f;
                    /*
                    ModuleWithModuleSettings var5 = (ModuleWithModuleSettings) Client.getInstance().getModuleManager().getModuleByClass(Criticals.class);
                    if (var5.isEnabled() && var5.getStringSettingValueByName("Type").equalsIgnoreCase("Minis")) {
                        this.handleCriticals(var1, var5.method16726().getStringSettingValueByName("Mode"), var5.method16726().getBooleanValueFromSetttingName("Avoid Fall Damage"));
                    }

                     */

                this.updateRotation();
                if (eventUpdateYaw - mc.player.rotationYaw != 0.0F && (rotationMode.currentValue.equals("Test1") || rotationMode.currentValue.equals("Test")) && mc.player.ticksExisted % 50 == 0) {
                    this.currentRotation.yaw = eventUpdateYaw;
                    this.currentRotation.pitch = eventUpdatePitch;
                }
                float hSpeed = rotationSpeed.currentValue;
                float vSpeed = rotationSpeed.currentValue;
                //TODO TODO TODO TODO TODO

                Rotation lastCopy = new Rotation(lastRotation.yaw, lastRotation.pitch);
                Rotation currentCopy = new Rotation(currentRotation.yaw, currentRotation.pitch);
                Rotation limit = !useRotationSpeed.currentValue
                        ? currentCopy
                        : RotationUtils.limitAngleChange(lastCopy, currentCopy, hSpeed, vSpeed);
                this.kaROT = new float[]{limit.yaw, limit.pitch};
                float[] oldRots = {mc.player.lastReportedYaw, mc.player.lastReportedPitch};

                currentRotation.yaw = RotationUtils.gcdFix(kaROT, oldRots)[0];
                currentRotation.pitch = RotationUtils.gcdFix(kaROT, oldRots)[1];

                RotationCore.currentYaw = currentRotation.yaw;
                RotationCore.currentPitch = currentRotation.pitch;

                if (this.hitEvent.currentValue) {
                    boolean var6 = autoBlock.hasReachedCpsTiming(this.attackTimer);
                    float var7 = !((double) mc.player.getCooldownPeriod() < 1.26) && this.getBooleanValueFromSettingName("Cooldown") ? mc.player.getCooledAttackStrength(0.0F) : 1.0F;
                    boolean var8 = attackCooldown == 0 && var6 && var7 >= 1.0F;
                    if (var6) {
                        autoBlock.updateCpsTimings();
                    }

                    if (var8) {
                        Class338 var9 = new Class338(this, var4);
                        var9.run();


                        this.attackTimer = 0;
                    }
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

        if (this.hitEvent.currentValue) {
            boolean var6 = autoBlock.hasReachedCpsTiming(this.attackTimer);
            float var7 = !((double) mc.player.getCooldownPeriod() < 1.26) && this.getBooleanValueFromSettingName("Cooldown") ? mc.player.getCooledAttackStrength(0.0F) : 1.0F;
            boolean var8 = attackCooldown == 0 && var6 && var7 >= 1.0F;
            if (var6) {
                autoBlock.updateCpsTimings();
            }

            if (var8) {
                Class338 var9 = new Class338(this, 0.07f);
                var9.run();


                this.attackTimer = 0;
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void method16821(EventUpdateWalkingPlayer event) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (mc.player != null) {
            if (!event.isPre()) {
                this.blockDelay = mc.player.inventory.currentItem;
                if (targetEntity != null && autoBlock.canAutoBlock() && this.currentRotation != null) {
                    autoBlock.performAutoBlock(targetEntity, this.currentRotation.yaw, this.currentRotation.pitch);
                }
            } else {
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
    public void meth2od10(EventInputOptions var1) {
        if (targetEntity != null) {
            if (mc.objectMouseOver != null) {
                if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
                    if (mc.player.getHeldItemMainhand() != null &&
                            mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) {

                        if (getStringSettingValueByName("Autoblock Mode").equals("Vanilla")) {
                            int value = (int) getNumberValueBySettingName("Unblock Rate");

                            if (mc.player.ticksExisted % (value == 0 ? 0 : value + 1) == 0) {
                                var1.setUseItem(true);
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
                    float var7 = RotationUtils.method34148(RotationUtils.getEntityPosition(targetList.get(0).getEntity())).yaw;
                    float var8 = Math.abs(RotationUtils.getAngleDifference2(var7, rotation.yaw));
                    this.targetYaw = var8 * 1.95F / 50.0F;
                    this.targetPitch++;
                    this.yawDifference = Math.random();
                }

                this.targets = targetList;
                float var12 = RotationUtils.method34148(RotationUtils.getEntityPosition(this.targets.get(0).getEntity())).yaw;
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
                    float var9 = Math.abs(RotationUtils.getAngleDifference2(RotationUtils.method34148(var14).yaw, rotation.yaw));
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

                if (Math.abs(targetYawDifference) > 80.0F) {
                    float var9 = (float) this.randomize(-10.2, 10.2);
                    float var30 = targetYawDifference * targetYawDifference * 1.13F / 2.0F + var9;
                    this.currentRotation.yaw += var30;
                    this.smoothYaw = var30;
                } else if (Math.abs(targetYawDifference) > 30.0F) {
                    float var26 = (float) this.randomize(-10.2, 10.2);
                    float var31 = targetYawDifference * 1.03F / 2.0F + var26;
                    this.currentRotation.yaw += var31;
                    this.smoothYaw = var31;
                } else if (Math.abs(targetYawDifference) > 10.0F) {
                    Entity var27 = RotationUtils.hoveringTarget(
                            this.currentRotation.pitch, this.currentRotation.yaw, this.getNumberValueBySettingName("Range"), 0.07
                    );
                    double var11 = var27 == null ? 13.4 : 1.4;
                    this.smoothYaw = (float) ((double) this.smoothYaw * 0.5296666666666666);
                    if (Math.abs(targetYawDifference) < 20.0F) {
                        this.smoothYaw = targetYawDifference * 0.5F;
                    }

                    this.currentRotation.yaw = this.currentRotation.yaw + targetYawDifference + this.smoothYaw + (float) this.randomize(-var11, var11);
                } else {
                    this.smoothYaw = (float) ((double) this.smoothYaw * 0.05);
                    double var13 = 0.0;
                    this.currentRotation.yaw = this.currentRotation.yaw + this.smoothYaw + (float) this.randomize(-var13, var13);
                }

                if (mc.player.ticksExisted % 5 == 0) {
                    double var32 = 10.0;
                    this.currentRotation.yaw = this.currentRotation.yaw
                            + (float) this.randomize(-var32, var32) / (mc.player.getDistance(entity) + 1.0F);
                    this.currentRotation.pitch = this.currentRotation.pitch
                            + (float) this.randomize(-var32, var32) / (mc.player.getDistance(entity) + 1.0F);
                }

                if (Math.abs(targetPitchDifference) > 10.0F) {
                    this.currentRotation.pitch = (float) ((double) this.currentRotation.pitch + (double) targetPitchDifference * 0.81 + this.randomize(-2.0, 2.0));
                }

                Entity var28 = RotationUtils.hoveringTarget(
                        this.lastRotation.pitch, this.lastRotation.yaw, this.getNumberValueBySettingName("Range"), 0.07
                );
                if (var28 != null && (double) this.blockCooldown > this.randomize(2.0, 5.0)) {
                    this.blockCooldown = 0;
                }
                break;
            case "NCP":
                this.currentRotation.yaw = (float) (RotationHelper.doBasicRotation(entity)[0] + (float) (Math.random() - 0.5) * 4.0);
                this.currentRotation.pitch = (float) (RotationHelper.doBasicRotation(entity)[1] + 3 + (float) (Math.random() - 0.5) * 4.0);
                break;
            case "AAC":
                float var29 = this.targetPitch / Math.max(1.0F, this.targetYaw);
                double var33 = entity.getPosX() - entity.lastTickPosX;
                double var34 = entity.getPosZ() - entity.lastTickPosZ;
                float var35 = (float) Math.sqrt(var33 * var33 + var34 * var34);
                float var36 = MathUtil.lerp(var29, 0.57, -0.135, 0.095, -0.3);
                float var37 = Math.min(1.0F, MathUtil.lerp(var29, 0.57, -0.135, 0.095, -0.3));
                if (this.isBlocking) {
                    var36 = MathUtil.lerp(var29, 0.18, 0.13, 1.0, 1.046);
                    var37 = Math.min(1.0F, MathUtil.lerp(var29, 0.18, 0.13, 1.0, 1.04));
                }

                float var38 = RotationUtils.getAngleDifference2(rotation.yaw, advancedRotation.yaw);
                float var39 = advancedRotation.pitch - rotation.pitch;
                this.currentRotation.yaw = rotation.yaw + var36 * var38;
                this.currentRotation.pitch = (rotation.pitch + var37 * var39) % 90.0F;
                if (var29 == 0.0F || var29 >= 1.0F || (double) var35 > 0.1 && this.targetYaw < 4.0F) {
                    float var41 = Math.abs(RotationUtils.getAngleDifference2(advancedRotation.yaw, rotation.yaw));
                    this.targetYaw = (float) Math.round(var41 * 1.8F / 50.0F);

                    this.targetPitch = 0.0F;
                    if (mc.pointedEntity == null && var29 != 1.0F) {
                        this.yawDifference = Math.random() * 0.5 + 0.25;
                    }

                    rotation.yaw = this.currentRotation.yaw;
                    rotation.pitch = this.currentRotation.pitch;
                }
                break;
            case "Smooth":
                Vector3d entityPosition = RotationUtils.getEntityPosition(targetEntity);

                boolean bool = mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY;
                this.currentRotation.yaw = (float) ((float) ((double) this.currentRotation.yaw + (double) (targetYawDifference * 2.3F) / (bool ? 10 : 3)) + (float) (Math.random() - 0.5) * 5.0);
                this.currentRotation.pitch = (float) ((float) ((double) this.currentRotation.pitch + (double) (targetPitchDifference * 2.3F) / (bool ? 14 : 4)) + (float) (Math.random() - 0.5) * 5.0) + 3;
                break;
            case "None":
                this.currentRotation.yaw = mc.player.rotationYaw;
                this.currentRotation.pitch = mc.player.rotationPitch;
                break;
            case "LockView":
                EntityRayTraceResult lvHoveringTarget = RotationUtils.hoveringTarget(
                        entity, this.currentRotation.yaw, this.currentRotation.pitch, var0 -> true, this.getNumberValueBySettingName("Range")
                );
                if (lvHoveringTarget == null || lvHoveringTarget.getEntity() != entity) {
                    this.currentRotation = advancedRotation;
                }
                break;
            case "Test2":
                EntityRayTraceResult rayTraceResult = RotationUtils.hoveringTarget(
                        entity, this.currentRotation.yaw, this.currentRotation.pitch, var0 -> true, this.getNumberValueBySettingName("Range")
                );
                if (rayTraceResult != null && rayTraceResult.getEntity() == entity) {
                    this.currentRotation.yaw = (float) ((double) this.currentRotation.yaw + (Math.random() - 0.5) * 2.0 + (double) (targetYawDifference / 10.0F)) + RandomUtil.nextFloat(-4, 4);
                    this.currentRotation.pitch = (float) ((double) this.currentRotation.pitch + (Math.random() - 0.5) * 2.0 + (double) (targetPitchDifference / 10.0F)) + RandomUtil.nextFloat(-4, 4);
                    this.targetPitch = 0.0F;
                    this.targetYaw = 1.0F;
                    return;
                }

                float pitchYawRatio = this.targetPitch / Math.max(1.0F, this.targetYaw);
                //double deltaPosX = entity.getPosX() - entity.lastTickPosX;
                //double deltaPosZ = entity.getPosZ() - entity.lastTickPosZ;
                //float entityHypot = (float) Math.sqrt(deltaPosX * deltaPosX + deltaPosZ * deltaPosZ);
                float interpolatedYawAdjustment = MathUtil.lerp(pitchYawRatio, 0.57, -0.135, 0.095, -0.3);
                float interpolatedPitchAdjustment = Math.min(1.0F, MathUtil.lerp(pitchYawRatio, 0.57, -0.135, 0.095, -0.3));
                float yawDifference = RotationUtils.getAngleDifference2(rotation.yaw, advancedRotation.yaw);
                float pitchDifference = advancedRotation.pitch - rotation.pitch;
                this.lastRotation.yaw = this.currentRotation.yaw;
                this.lastRotation.pitch = this.currentRotation.pitch;
                this.currentRotation.yaw = rotation.yaw + interpolatedYawAdjustment * (yawDifference + RandomUtil.nextFloat(-5, 5));
                this.currentRotation.pitch = ((rotation.pitch + interpolatedPitchAdjustment * (pitchDifference + RandomUtil.nextFloat(-5, 5))) % 90.0F);
                if (pitchYawRatio == 0.0F || pitchYawRatio >= 1.0F) {
                    //KAUtils.addChatMessage(pitchYawRatio +"");

                    float angleDifferenceAbs = Math.abs(RotationUtils.getAngleDifference2(advancedRotation.yaw, rotation.yaw));
                    this.targetYaw = (float) Math.round(angleDifferenceAbs * 1.8F / 50.0F);
                    if (this.targetYaw < 3.0F) {
                        this.targetYaw = 3.0F;
                    }

                    this.targetPitch = 0.0F;
                    if (mc.pointedEntity == null && pitchYawRatio != 1.0F) {
                        this.yawDifference = Math.random() * 0.5 + 0.25;
                    }

                    rotation.yaw = this.currentRotation.yaw;
                    rotation.pitch = this.currentRotation.pitch;
                }
        }
    }

    private double randomize(double var1, double var3) {
        return var1 + Math.random() * (var3 - var1);
    }
}
