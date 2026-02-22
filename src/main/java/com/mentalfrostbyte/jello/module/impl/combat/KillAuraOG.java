/*package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.EventKeepSprint;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.player.EventSprint;
import com.mentalfrostbyte.jello.event.impl.player.action.EventStopUseItem;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mentalfrostbyte.jello.util.system.other.Pair;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.ExpirationTimer;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.InteractAutoBlock;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.AttackRunnable;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import com.mentalfrostbyte.jello.module.impl.movement.Jesus;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.impl.movement.Step;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ColorSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.world.EntityUtil;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.RotationUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SEntityPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowestPriority;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import static com.mentalfrostbyte.jello.util.game.render.RenderUtil.drawCircle;

@SuppressWarnings({"unused", "cast"})
public class KillAuraOG extends Module {
    public static boolean isAuraActive = false;
    public static Entity currentTarget;
    public static TimedEntity currentTimedEntity;
    public static Rotation previousRotation = new Rotation(0.0F, 0.0F);
    public static int attackCooldown;
    public HashMap<Entity, Animation> entityGlowAnimations = new HashMap<>();
    public static InteractAutoBlock autoBlockController;
    private int attackDelay;
    private int blockDelay;
    private int groundTicks;
    private int targetIndex;
    private int currentItemIndex;
    private int swingDelay;
    private int blockCooldown;
    private int hitDelay;
    public static List<TimedEntity> targetEntities;
    private Rotation currentRotations;
    private Rotation secondaryRotations;
    private double randomOffset;
    private float rotationSpeed;
    private float rotationProgress;
    private float rotationAdjustment;
    private boolean isBlocking;
    private double[] positionOffset;
    float randomAngle = 0;

    private AttackRunnable attackLambda;

    private BooleanSetting keepSprint = new BooleanSetting("Keep sprint", "Keep Sprinting after hitting a player", false);
    private BooleanSetting sprintFix = new BooleanSetting("Sprint fix", "Fix sprint flags on strict anticheats", true);

    public KillAuraOG() {
        super(ModuleCategory.COMBAT, "KillAuraOG", "Automatically attacks entities");
        this.registerSetting(
                new ModeSetting("Mode", "Mode", 0, "Single", "Switch", "Multi", "Multi2"));
        this.registerSetting(
                new ModeSetting("Autoblock Mode", "Autoblock Mode", 0, "None", "NCP", "Basic1", "Basic2", "Vanilla", "Fake"));
        this.registerSetting(
                new ModeSetting("Sort Mode", "Sort Mode", 0, "Range", "Health", "Angle", "Armor", "Prev Range"));
        this.registerSetting(
                new ModeSetting("Attack Mode", "Attacks after or before sending the movement", 0, "Pre", "Post"));
        this.registerSetting(new ModeSetting("Rotation Mode", "The way you will look at entities", 0, "NCP", "New", "Smooth", "LockView", "None"));
        this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F,  2.8F, 8.0F, 0.01F));
        this.registerSetting(new NumberSetting<>("Block Range", "Block Range value", 4.0F,  2.8F, 8.0F, 0.2F));
        this.registerSetting(new NumberSetting<>("Min CPS", "Min CPS value", 8.0F, 1.0F,  20.0F, 1.0F)
                        .addObserver(setting -> autoBlockController.initializeCpsTimings()));
        this.registerSetting(new NumberSetting<>("Max CPS", "Max CPS value", 8.0F, 1.0F, 20.0F, 1.0F)
                        .addObserver(setting -> autoBlockController.initializeCpsTimings()));
        this.registerSetting(new NumberSetting<>("Hit box expand", "Hit Box expand", 0.05F, 0.0F, 1.0F, 0.01F));
        this.registerSetting(new NumberSetting<>("Hit Chance", "Hit Chance", 100.0F, 25.0F, 100.0F, 1.0F));
        this.registerSetting(new BooleanSetting("Interact auto block", "Send interact packet when blocking", true));
        this.registerSetting(new BooleanSetting("Players", "Hit players", true));
        this.registerSetting(new BooleanSetting("Animals", "Hit animals", false));
        this.registerSetting(new BooleanSetting("Monsters", "Hit monsters", false));
        this.registerSetting(new BooleanSetting("Invisible", "Hit invisible entities", true));
        this.registerSetting(new BooleanSetting("Raytrace", "Helps the aura become more legit", true));
        this.registerSetting(new BooleanSetting("Cooldown", "Use attack cooldown (1.9+)", false));
        this.registerSetting(new BooleanSetting("No swing", "Hit without swinging", false));
        this.registerSetting(new BooleanSetting("Disable on death", "Disable on death", true));
        this.registerSetting(new BooleanSetting("Through walls", "Target entities through walls", true));
        this.registerSetting(new BooleanSetting("Smart Reach", "Allows you to get more reach (depends on your ping)", true));
        this.registerSetting(new BooleanSetting("Silent", "Silent rotations", true));
        this.registerSetting(new BooleanSetting("ESP", "ESP on targets", true));
        this.registerSetting(new ColorSetting("ESP Color", "The render color", ClientColors.LIGHT_GREYISH_BLUE.getColor()));
        this.registerSetting(keepSprint);
        this.registerSetting(sprintFix);
        keepSprint.addObserver(setting -> sprintFix.currentValue = false);
        sprintFix.addObserver(setting -> keepSprint.currentValue = false);
    }

    public static Rotation getRotations2(KillAura killaura) {
        return killaura.secondaryRotations;
    }

    public static Rotation getRotations(KillAura killaura) {
        return killaura.currentRotations;
    }

    public static int getTargetIndex(KillAura killaura) {
        return killaura.targetIndex;
    }

    public static void setTargetIndex(KillAura killaura, int targetIndex) {
        killaura.targetIndex = targetIndex;
    }

    @Override
    public void initialize() {
        targetEntities = new ArrayList<>();
        autoBlockController = new InteractAutoBlock(this);
        super.initialize();
    }

    @Override
    public void onEnable() {
        resetState();
        initializeRotations();
        autoBlockController.setBlockingState(isHoldingSword() && isUseItemKeyDown());
        super.onEnable();
    }

    private void resetState() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        targetEntities = new ArrayList<>();
        currentTarget = null;
        currentTimedEntity = null;
        attackDelay = 0;
        blockDelay = 0;
        targetIndex = 0;
        attackCooldown = 0;
        blockCooldown = -1;
        entityGlowAnimations.clear();
        if (mc.player.isOnGround()) {
            groundTicks = 1;
        }
    }

    private void initializeRotations() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        secondaryRotations = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        currentRotations = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        previousRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
        rotationProgress = -1.0F;
    }

    private boolean isHoldingSword() {
        if (mc.player == null || mc.world == null) {
            return false;
        }

        return mc.player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof SwordItem;
    }

    private boolean isUseItemKeyDown() {
        return mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        currentTimedEntity = null;
        targetEntities = null;
        isAuraActive = false;
        attackLambda = null;
        super.onDisable();
    }

    @EventTarget
    public void onKeepSprint(EventKeepSprint event) {
        if (keepSprint.getCurrentValue()) {
            event.greater = false;
        }
    }

    @EventTarget
    public void onSprint(EventSprint event) {
        if (sprintFix.getCurrentValue() && currentTarget != null) {
            event.cancelled = true;
        }
    }

    @EventTarget
    public void onWorldChange(EventLoadWorld event) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Disable on death")) {
            Client.getInstance().notificationManager.send(new Notification("Aura", "Aura disabled due to respawn"));
            this.toggle();
        }
    }

    @EventTarget
    public void onTick(EventUpdate event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (this.isEnabled()) {
            if (this.rotationProgress != -1.0F) {
                this.rotationProgress++;
            }

            if (this.getBooleanValueFromSettingName("Disable on death")) {
                if (!mc.player.isAlive()) {
                    this.toggle();
                    Client.getInstance().notificationManager.send(new Notification("Aura", "Aura disabled due to death"));
                }
            }
        }
    }

    @EventTarget
    public void onStopuseItem(EventStopUseItem event) {
        if (this.isEnabled()) {
            if (!this.getStringSettingValueByName("Autoblock Mode").equals("None")
                    && (Objects.requireNonNull(mc.player).getHeldItemMainhand().getItem() instanceof SwordItem || this.currentItemIndex != mc.player.inventory.currentItem)
                    && currentTarget != null) {
                event.cancelled = true;
            } else if (Objects.requireNonNull(mc.player).getHeldItemMainhand().getItem() instanceof SwordItem) {
                this.swingDelay = 2;
            }
        }
    }

    @EventTarget
    @LowestPriority
    public void onUpdate(EventMotion event) {
        if (!event.isPre()) {
            currentItemIndex = mc.player.inventory.currentItem;
            if (currentTarget != null && autoBlockController.canAutoBlock() && currentRotations != null) {
                if (this.getStringSettingValueByName("Autoblock Mode").equals("Fake")) {
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(
                            mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(),
                            mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround
                    ));
                } else {
                    autoBlockController.performAutoBlock(currentTarget, currentRotations.yaw, currentRotations.pitch);
                    isBlocking = true;
                }
            }
            return;
        }
        if (autoBlockController.isBlocking() && (!(mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) || currentTarget == null)) {
            if (!this.getStringSettingValueByName("Autoblock Mode").equals("Fake")) {
                autoBlockController.setBlockingState(false);
                mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));

            }
        }

        if (!isEnabled() || mc.player == null) {
            return;
        }

        if (!event.isPre()) {
            currentItemIndex = mc.player.inventory.currentItem;

            if (currentTarget != null && autoBlockController.canAutoBlock() && currentRotations != null) {
                autoBlockController.performAutoBlock(currentTarget, currentRotations.yaw, currentRotations.pitch);
            }
            return;
        }

        if (swingDelay > 0) {
            swingDelay--;
        }

        if (currentTarget != null
                && autoBlockController.isBlocking()
                && MovementUtil.isMoving()
                && getStringSettingValueByName("Autoblock Mode").equals("NCP")) {
            CombatUtil.unblock();
        }

        if (autoBlockController.isBlocking() && (!(mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) || currentTarget == null)) {
            autoBlockController.setBlockingState(false);
        }

        if (blockCooldown >= 0) {
            if (blockCooldown == 0) {
                CombatUtil.unblock();
                autoBlockController.setBlockingState(true);
            }
            blockCooldown--;
        }

        handleAura();

        if (targetEntities == null || targetEntities.isEmpty()) {
            return;
        }

        attackDelay++;

        float hitboxExpand = getNumberValueBySettingName("Hit box expand");
        ModuleWithModuleSettings criticalsModule = (ModuleWithModuleSettings) Client.getInstance().moduleManager.getModuleByClass(Criticals.class);

        if (criticalsModule.isEnabled()
                && criticalsModule.getStringSettingValueByName("Type").equalsIgnoreCase("Minis")) {
            handleEventUpdate(
                    event,
                    criticalsModule.getModWithTypeSetToName().getStringSettingValueByName("Mode"),
                    criticalsModule.getModWithTypeSetToName().getBooleanValueFromSettingName("Avoid Fall Damage")
            );
        }

        setRotation();

        if (event.getYaw() - mc.player.rotationYaw != 0.0F) {
            currentRotations.yaw = event.getYaw();
            currentRotations.pitch = event.getPitch();
        }

        if (currentTarget != null) {
            event.setYaw(currentRotations.yaw);
            event.setPitch(currentRotations.pitch);
        }

        boolean canAttack = autoBlockController.shouldAttack(attackDelay);

        boolean shouldAttack = canAttack;
        if (getBooleanValueFromSettingName("Cooldown")) {
            shouldAttack = mc.player.getCooledAttackStrength(0.5F) >= 1.0;
        }

        if (canAttack) {
            autoBlockController.setupDelay();
        }

        if (shouldAttack) {
            if (attackLambda == null)
                attackLambda = new AttackRunnable(this, hitboxExpand);

            boolean isPre = getStringSettingValueByName("Attack Mode").equals("Pre");
            if (!isPre) {
                event.attackPost(attackLambda);
            } else {
                attackLambda.run();
            }
            attackDelay = 0;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2DOffset event) {

        if (mc.player == null || mc.world == null) {
            return;
        }

        if (currentTimedEntity == null || getBooleanValueFromSettingName("Silent")
                || getStringSettingValueByName("Rotation Mode").equals("None")) {
            return;
        }

        float interpolatedYaw = MathHelper.wrapDegrees(secondaryRotations.yaw + (currentRotations.yaw - secondaryRotations.yaw) * mc.getRenderPartialTicks());
        float interpolatedPitch = MathHelper.wrapDegrees(secondaryRotations.pitch + (currentRotations.pitch - secondaryRotations.pitch) * mc.getRenderPartialTicks());

        mc.player.rotationYaw = interpolatedYaw;
        mc.player.rotationPitch = interpolatedPitch;
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (targetEntities == null) {
            return;
        }

        Iterator<Entry<Entity, Animation>> animationIterator = entityGlowAnimations.entrySet().iterator();

        while (animationIterator.hasNext()) {
            Entry<Entity, Animation> entry = animationIterator.next();
            Animation animation = entry.getValue();

            animation.changeDirection(Animation.Direction.BACKWARDS);

            if (animation.calcPercent() == 0.5F) {
                animationIterator.remove();
            }
        }

        for (TimedEntity timedEntity : targetEntities) {
            if (timedEntity != null) {
                Entity entity = timedEntity.getEntity();

                if (!entityGlowAnimations.containsKey(entity)) {
                    entityGlowAnimations.put(entity, new Animation(250, 250));
                } else {
                    entityGlowAnimations.get(entity).changeDirection(Animation.Direction.FORWARDS);
                }
            }
        }

        for (Entry<Entity, Animation> entry : entityGlowAnimations.entrySet()) {
            renderTargetESP(entry.getKey());
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {

        if (mc.player == null || mc.world == null) {
            return;
        }

        IPacket<?> packet = event.getPacket();

        if (packet instanceof SEntityStatusPacket statusPacket) {

            if (statusPacket.getOpCode() == 3) {
                Entity entity = statusPacket.getEntity(mc.world);
                autoBlockController.field44349.remove(entity);
            }
            return;
        }

        if (packet instanceof SEntityPacket entityPacket) {
            if (entityPacket.func_229745_h_() && (entityPacket.posX != 0 || entityPacket.posY != 0 || entityPacket.posZ != 0)) {
                for (Entry<Entity, List<Pair<Vector3d, Long>>> entry : autoBlockController.field44349.entrySet()) {
                    Entity trackedEntity = entry.getKey();
                    List<Pair<Vector3d, Long>> trackedPositions = entry.getValue();

                    if (entityPacket.getEntity(mc.world) == trackedEntity) {
                        Vector3d scaledVector = trackedEntity.field_242272_av.scale(2.4414062E-4F);
                        trackedPositions.add(new Pair<>(scaledVector, System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    public void renderTargetESP(Entity targetEntity) {
        if (targetEntity == null || !targetEntity.isAlive()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glLineWidth(1.4F);



        double partialTicks = Minecraft.getInstance().timer.renderPartialTicks;
        double interpolatedX = targetEntity.lastTickPosX + (targetEntity.getPosX() - targetEntity.lastTickPosX) * partialTicks;
        double interpolatedY = targetEntity.lastTickPosY + (targetEntity.getPosY() - targetEntity.lastTickPosY) * partialTicks;
        double interpolatedZ = targetEntity.lastTickPosZ + (targetEntity.getPosZ() - targetEntity.lastTickPosZ) * partialTicks;

        double cameraX = mc.gameRenderer.getActiveRenderInfo().getProjectedView().getX();
        double cameraY = mc.gameRenderer.getActiveRenderInfo().getProjectedView().getY();
        double cameraZ = mc.gameRenderer.getActiveRenderInfo().getProjectedView().getZ();

        GL11.glTranslated(interpolatedX - cameraX, interpolatedY - cameraY, interpolatedZ - cameraZ);

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

        long cycleDurationMs = 1425;
        float glowProgress = (float) (System.currentTimeMillis() % cycleDurationMs) / cycleDurationMs;
        boolean isFadingOut = glowProgress > 0.5F;
        glowProgress = !isFadingOut ? glowProgress * 2.0F : 1.0F - (glowProgress * 2.0F % 1.0F);

        GL11.glTranslatef(0.0F, (targetEntity.getHeight() + 0.4F) * glowProgress, 0.0F);

        float glowFactor = (float) Math.sin(glowProgress * Math.PI);
        Animation animation = this.entityGlowAnimations.get(targetEntity);
        if (animation != null) {
            drawCircle(isFadingOut, 0.45F * glowFactor, 0.6F, 0.35F * glowFactor, animation.calcPercent());
        }

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
    public void drawCircle(boolean isFadingOut, float circleHeight, float radius, float glowStrength, float glowOpacity) {
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_QUAD_STRIP);

        int angleStep = (int) (360.0F / (40.0F * radius));

        Color espColor = new Color(this.parseSettingValueToIntBySettingName("ESP Color"));
        float red = (float) espColor.getRed() / 255.0F;
        float green = (float) espColor.getGreen() / 255.0F;
        float blue = (float) espColor.getBlue() / 255.0F;

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int effectiveAngle = angle > 360 ? 0 : angle;

            double x = Math.sin(Math.toRadians(effectiveAngle)) * radius;
            double z = Math.cos(Math.toRadians(effectiveAngle)) * radius;

            GL11.glColor4f(red, green, blue, isFadingOut ? 0.0F : glowStrength * glowOpacity);
            GL11.glVertex3d(x, 0.0, z);

            GL11.glColor4f(red, green, blue, isFadingOut ? glowStrength * glowOpacity : 0.0F);
            GL11.glVertex3d(x, circleHeight, z);
        }

        GL11.glEnd();

        GL11.glLineWidth(2.2F);
        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int effectiveAngle = angle > 360 ? 0 : angle;

            double x = Math.sin(Math.toRadians(effectiveAngle)) * radius;
            double z = Math.cos(Math.toRadians(effectiveAngle)) * radius;

            GL11.glColor4f(red, green, blue, (0.5F + 0.5F * glowStrength) * glowOpacity);
            GL11.glVertex3d(x, isFadingOut ? 0.0 : circleHeight, z);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderSystem.shadeModel(GL11.GL_FLAT);
    }

    public boolean isAutoBlockEnabled() {
        if (mc.player == null) {
            return false;
        }

        return currentTarget != null && !mc.player.getHeldItemMainhand().isEmpty() && mc.player.getHeldItemMainhand().getItem() instanceof SwordItem &&
                !this.getStringSettingValueByName("Autoblock Mode").equals("None");
    }

    @Override
    public boolean isEnabled2() {
        return this.isEnabled() && this.isAutoBlockEnabled();
    }

    public void handleEventUpdate(EventMotion event, String serverType, boolean avoidFallDamage) {

        if (mc.player == null || mc.world == null) {
            return;
        }

        double yOffset = !serverType.equals("Hypixel") ? 0.0 : 1.0E-14;
        boolean shouldSetGround = true;

        if (blockDelay == 0 && groundTicks >= 1 && Step.updateTicksBeforeStep > 1) {
            if (autoBlockController.method36820(attackDelay)) {
                blockDelay = 1;
                shouldSetGround = avoidFallDamage;
                yOffset = !serverType.equals("Cubecraft") ? 0.0626 : MovementUtil.getJumpValue() / 10.0;
                positionOffset = new double[]{event.getX(), event.getY() + yOffset, event.getZ()};
            }
        } else if (blockDelay == 1) {
            blockDelay = 0;
            shouldSetGround = false;
            if (serverType.equals("Hypixel") && positionOffset != null && mc.player.getMotion().y < 0.0) {
                Objects.requireNonNull(mc.getConnection()).sendPacket(new CPlayerPacket.PositionPacket(positionOffset[0], positionOffset[1], positionOffset[2], false));
                positionOffset = null;
            }
        }

        boolean isOnGroundOrAboveBounds = !Jesus.isWalkingOnLiquid() && (Objects.requireNonNull(mc.player).isOnGround() || BlockUtil.isAboveBounds(mc.player, 0.001F));
        if (!isOnGroundOrAboveBounds) {
            groundTicks = 0;
            blockDelay = 0;
        } else {
            groundTicks++;
            if ((!Client.getInstance().moduleManager.getModuleByClass(Speed.class).isEnabled() || Client.getInstance().moduleManager.getModuleByClass(Speed.class).getStringSettingValueByName("Type").equalsIgnoreCase("Cubecraft") || Client.getInstance().moduleManager.getModuleByClass(Speed.class).getStringSettingValueByName("Type").equalsIgnoreCase("Vanilla")) && mc.player.collidedVertically && !mc.player.isJumping && !mc.player.isInWater() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                isAuraActive = yOffset > 0.001;

                event.setY(mc.player.getPosY() + yOffset);
                event.setOnGround(shouldSetGround);
            }
        }
    }

    private Entity getClosestEntity(List<TimedEntity> var1) {

        if (mc.player == null || mc.world == null) {
            return null;
        }

        var1 = autoBlockController.sortEntities(var1);
        return !var1.isEmpty() && var1.get(0).getEntity().getDistance(mc.player) <= this.getNumberValueBySettingName("Block Range")
                ? var1.get(0).getEntity()
                : null;
    }

    private void handleAura() {
        float blockRange = getNumberValueBySettingName("Block Range");
        float range = getNumberValueBySettingName("Range");
        String mode = getStringSettingValueByName("Mode");

        List<TimedEntity> potentialTargets = autoBlockController.getEntitiesInRange(Math.max(blockRange, range));
        if (potentialTargets == null || potentialTargets.isEmpty() || mc.gameSettings.keyBindAttack.isPressed()) {
            resetState();
            return;
        }

        potentialTargets = autoBlockController.sortEntities(potentialTargets);
        Entity closestEntity = getClosestEntity(potentialTargets);
        if (currentRotations == null) {
            onEnable();
        }

        currentTarget = closestEntity;
        potentialTargets = autoBlockController.getEntitiesInRange(range);

        if (mode.equals("Single") || mode.equals("Multi")) {
            potentialTargets = autoBlockController.sortEntities(potentialTargets);
        }

        if (potentialTargets.isEmpty()) {
            resetState();
        } else {
            processTargets(potentialTargets, mode, range);
        }
    }

    private void processTargets(List<TimedEntity> targets, String mode, float range) {
        if (rotationProgress == -1.0F) {
            float targetYaw = RotationUtil.getRotationsToVector(PlayerUtil.method17751(targets.get(0).getEntity())).yaw;
            float yawDifference = Math.abs(getYawDifference(targetYaw, previousRotation.yaw));
            rotationSpeed = yawDifference * 1.95F / 50.0F;
            rotationProgress++;
            randomOffset = Math.random();
        }

        targetEntities = targets;

        if (!mode.equals("Switch")) {
            if (mode.equals("Single")) {
                currentTimedEntity = targets.get(0);
            } else if (mode.equals("Multi2")) {
                if (targetIndex >= targets.size()) {
                    targetIndex = 0;
                }
                currentTimedEntity = targets.get(targetIndex);
            } else if (currentTimedEntity == null || currentTimedEntity.getEntity() != targets.get(0).getEntity()) {
                float targetYaw = RotationUtil.getRotationsToVector(PlayerUtil.method17751(targets.get(0).getEntity())).yaw;
                float yawDifference = Math.abs(getYawDifference(targetYaw, previousRotation.yaw));
                rotationSpeed = yawDifference * 1.95F / 50.0F;
                rotationProgress++;
                randomOffset = Math.random();

                currentTimedEntity = targets.get(0);
            }
        } else {
            boolean shouldSwitch = currentTimedEntity == null
                    || currentTimedEntity.isExpired()
                    || !targets.contains(currentTimedEntity)
                    || Objects.requireNonNull(mc.player).getDistance(currentTimedEntity.getEntity()) > range;

            if (shouldSwitch && !targets.isEmpty()) {
                targetIndex = (targetIndex + 1) % targets.size();
                TimedEntity nextTarget = targets.get(targetIndex);
                float targetYaw = RotationUtil.getRotationsToVector(PlayerUtil.method17751(nextTarget.getEntity())).yaw;
                float yawDifference = Math.abs(getYawDifference(targetYaw, previousRotation.yaw));
                rotationSpeed = yawDifference * 1.95F / 50.0F;
                randomOffset = Math.random();
                currentTimedEntity = new TimedEntity(nextTarget.getEntity(), createExpirationTimer());
            }
        }

        if (!mode.equals("Multi")) {

            if (currentTimedEntity == null) return;

            targetEntities = List.of(currentTimedEntity);
        }
    }

    private float getYawDifference(float targetYaw, float currentYaw) {
        return RotationUtil.getShortestYawDifference(targetYaw, currentYaw);
    }

    private ExpirationTimer createExpirationTimer() {
        boolean isNCPMode = getStringSettingValueByName("Rotation Mode").equals("NCP");
        return new ExpirationTimer(isNCPMode ? 270L : 500L);
    }

    private void setRotation() {

        if (mc.player == null || mc.world == null) {
            return;
        }

        Entity targ = currentTimedEntity.getEntity();
        Rotation newRots = RotationUtil.getRotations(targ, !getBooleanValueFromSettingName("Through walls"));
        if (newRots == null) {
            System.out.println("[KillAura] newRots is null??? on line 612");
            return;
        }
        float yawDifference = RotationUtil.getShortestAngleDifference(currentRotations.yaw, newRots.yaw);
        float pitchDifference = newRots.pitch - currentRotations.pitch;
        String rotationMode = getStringSettingValueByName("Rotation Mode");
        float range = getNumberValueBySettingName("Range");
        switch (rotationMode) {
            case "NCP":
                secondaryRotations.yaw = currentRotations.yaw;
                secondaryRotations.pitch = currentRotations.pitch;
                currentRotations = newRots;
                break;
            case "New":
                updateRotations(mc.player, currentTarget, currentRotations, secondaryRotations, 0.1, 0.1);
                break;
            case "Smooth":
                secondaryRotations.yaw = currentRotations.yaw;
                secondaryRotations.pitch = currentRotations.pitch;
                currentRotations.yaw += (float) (yawDifference * 2.0F / 5.0);
                currentRotations.pitch += (float) (pitchDifference * 2.0F / 5.0);
                break;
            case "None":
                secondaryRotations.yaw = currentRotations.yaw;
                secondaryRotations.pitch = currentRotations.pitch;
                currentRotations.yaw = mc.player.rotationYaw;
                currentRotations.pitch = mc.player.rotationPitch;
                break;
            case "LockView":
                secondaryRotations.yaw = currentRotations.yaw;
                secondaryRotations.pitch = currentRotations.pitch;
                EntityRayTraceResult ray = EntityUtil.method17714(targ, currentRotations.yaw, currentRotations.pitch, e -> true, getNumberValueBySettingName("Range"));
                if (ray == null || ray.getEntity() != targ) {
                    currentRotations = newRots;
                }
                break;
        }
    }

    public void updateRotations(Entity player, Entity target, Rotation currentRotations, Rotation secondaryRotations, double mouseDeltaX, double mouseDeltaY) {
        if (player == null || target == null || currentRotations == null || secondaryRotations == null) {
            return;
        }

        List<Vector3d> aimPoints = new ArrayList<>();
        Vector3d closestPoint = findClosestPoint(player.getPositionVec(), target.getBoundingBox(), 25.0f, 0.05);
        if (closestPoint != null) {
            aimPoints.add(closestPoint);
        }

        Vector3d aimPoint = aimPoints.get(0);

        double targetEyeX = aimPoint.x;
        double targetEyeY = aimPoint.y;
        double targetEyeZ = aimPoint.z;

        double playerEyeX = player.getPosX();
        double playerEyeY = player.getPosY() + player.getEyeHeight();
        double playerEyeZ = player.getPosZ();

        double deltaX = targetEyeX - playerEyeX;
        double deltaY = targetEyeY - playerEyeY;
        double deltaZ = targetEyeZ - playerEyeZ;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        final float factor = Math.min(Math.max(mc.player.getDistance(target), 0.0F), 3.0F) / 3.0F * (float) (1.0F + Math.sin(randomAngle) * 2.0F) / 2.0F;


        targetPitch = MathHelper.clamp(targetPitch, -90.0F, 90.0F);

        {
            final float radius = 10.0F * factor;

            float x = radius * (float) Math.cos(randomAngle);
            float y = radius * (float) Math.sin(randomAngle);
            float z = radius * (float) Math.sin(randomAngle / 2.0F);

            targetYaw += x * (float) Math.cos(randomAngle / 3.0F);
            targetPitch += y * (float) Math.sin(randomAngle / 3.0F);

            randomAngle += 0.25F;
        }

        double radianFactor = Math.sin(Math.toRadians(targetYaw)) * Math.cos(Math.toRadians(targetPitch));
        double advancedCalculationX = deltaX * radianFactor + mouseDeltaX;
        double advancedCalculationY = deltaY * radianFactor + mouseDeltaY;
        double advancedCalculationZ = deltaZ * radianFactor;

        currentRotations.yaw = (float) (targetYaw + advancedCalculationX);
        currentRotations.pitch = (float) (targetPitch + advancedCalculationY);
        currentRotations.pitch = MathHelper.clamp(currentRotations.pitch, -90.0F, 90.0F);
        secondaryRotations.yaw = (float) (targetYaw + advancedCalculationZ);
        secondaryRotations.pitch = (float) (targetPitch + advancedCalculationY);
    }

    // simple because i didnt bother making better one.
    private Vector3d findClosestPoint(Vector3d origin, AxisAlignedBB boundingBox, float maxDistance, double stepSize) {
        Vector3d closestPoint = null;
        double closestDistanceSquared = maxDistance * maxDistance;
        List<Vector3d> points = new ArrayList<>();

        double minX = boundingBox.minX;
        double maxX = boundingBox.maxX;
        double minY = boundingBox.maxY - 0.4;
        double maxY = boundingBox.maxY;
        double minZ = boundingBox.minZ;
        double maxZ = boundingBox.maxZ;

        for (double x = minX; x <= maxX; x += stepSize) {
            for (double y = minY; y <= maxY; y += stepSize) {
                for (double z = minZ; z <= maxZ; z += stepSize) {
                    points.add(new Vector3d(x, y, z));
                }
            }
        }

        for (Vector3d point : points) {
            double distanceSquared = origin.squareDistanceTo(point);
            if (distanceSquared <= closestDistanceSquared) {
                if (closestPoint == null || origin.squareDistanceTo(closestPoint) > distanceSquared) {
                    closestPoint = point;
                    closestDistanceSquared = distanceSquared;
                }
            }
        }

        return closestPoint;
    }

    private float normalizeAngle(float angle) {
        while (angle <= -180.0F) {
            angle += 360.0F;
        }
        while (angle > 180.0F) {
            angle -= 360.0F;
        }
        return angle;
    }
}
 */