package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventKeepSprint;
import com.mentalfrostbyte.jello.event.impl.player.EventRunLoop;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMotion;
import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.ClickDelayCalculator;
import com.mentalfrostbyte.jello.module.impl.movement.BlockFly;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.rotation.util.RotationUtils;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import com.mentalfrostbyte.jello.util.game.world.EntityUtil;

import com.mentalfrostbyte.jello.util.system.math.counter.Counter;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.ArrayList;
import java.util.List;

public class NewAura extends Module {
    private final ModeSetting sortMode = new ModeSetting("Sort mode", "In what order should entities be sorted in?", 0, "Range", "Health", "Armor", "Ticks");

    private final ClickDelayCalculator delayCalculator = new ClickDelayCalculator(9, 11);
    private final ModeSetting clickMode = new ModeSetting("Click mode", "What should be the clicking mode?", 0, "CPS", "1.9");

    private final NumberSetting<Long> minCPS = new NumberSetting<>("Min CPS", "Minimal CPS?", 10L, 1L, 20L, 1.0f) {
        @Override
        public boolean isHidden() {
            return clickMode.getCurrentValue().equals("1.9");
        }
    };

    private final NumberSetting<Long> maxCPS = new NumberSetting<>("Max CPS", "Maximal CPS?", 11L, 2L, 21L, 1.0f) {
        @Override
        public boolean isHidden() {
            return clickMode.getCurrentValue().equals("1.9");
        }
    };

    private final BooleanSetting delayPatterns = new BooleanSetting("Delay patterns", "", false) {
        @Override
        public boolean isHidden() {
            return clickMode.getCurrentValue().equals("1.9");
        }
    };

    private final NumberSetting<Double> delayPattern1 = new NumberSetting<>("Delay pattern 1", "", 90, 0, 700, 1) {
        @Override
        public boolean isHidden() {
            return clickMode.getCurrentValue().equals("1.9") && delayPatterns.getCurrentValue();
        }
    };

    private final NumberSetting<Double> delayPattern2 = new NumberSetting<>("Delay pattern 2", "", 110, 0, 700, 1) {
        @Override
        public boolean isHidden() {
            return clickMode.getCurrentValue().equals("1.9") && delayPatterns.getCurrentValue();
        }
    };

    private final NumberSetting<Double> delayPattern3 = new NumberSetting<>("Delay pattern 3", "", 130, 0, 700, 1) {
        @Override
        public boolean isHidden() {
            return clickMode.getCurrentValue().equals("1.9") && delayPatterns.getCurrentValue();
        }
    };

    private final NumberSetting<Float> searchRange = new NumberSetting<>("Search range", "What should be the search range for entities?", 4, 1, 7, 0.1f);
    private final NumberSetting<Float> attackRange = new NumberSetting<>("Attack range", "What should be the attack range for entities?", 3.4f, 1, 6, 0.1f);

    private final BooleanSetting players = new BooleanSetting("Players", "Should aura attack players?", true);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", "Should aura target monsters?", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", "Should aura target animals?", false);
    private final BooleanSetting invisibles = new BooleanSetting("Invisibles", "Should aura target invisible entities?", false);
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", "Should aura attack entities through walls?", false);
    private final BooleanSetting raycast = new BooleanSetting("Raycast", "Should aura raycast a line to each target?", true);
    private final BooleanSetting keepSprint = new BooleanSetting("Keep sprint", "Should aura keep sprinting while attacking?", false);
    private final BooleanSetting sprintFix = new BooleanSetting("Sprint fix", "Should sprint be fixed? (disables Keep sprint)", true);

    public NewAura() {
        super(ModuleCategory.COMBAT, "NewAura", "Attacks entities.");

        minCPS.addObserver(a -> {
            NumberSetting<Long> setting = (NumberSetting<Long>) a;
            if (minCPS.getCurrentValue() >= maxCPS.getCurrentValue()) {
                minCPS.setCurrentValue(maxCPS.getCurrentValue());
            }
            delayCalculator.minCPS = setting.getCurrentValue();
        });

        maxCPS.addObserver(a -> {
            NumberSetting<Long> setting = (NumberSetting<Long>) a;
            if (maxCPS.getCurrentValue() <= minCPS.getCurrentValue()) {
                maxCPS.setCurrentValue(minCPS.getCurrentValue());
            }
            delayCalculator.maxCPS = setting.getCurrentValue();
        });

        delayPatterns.addObserver(a -> delayCalculator.patternEnabled = (boolean) a.currentValue);
        delayPattern1.addObserver(a -> {
            NumberSetting<Double> setting = (NumberSetting<Double>) a;
            delayCalculator.delayPattern1 = setting.getCurrentValue();
        });
        delayPattern2.addObserver(a -> {
            NumberSetting<Double> setting = (NumberSetting<Double>) a;
            delayCalculator.delayPattern2 = setting.getCurrentValue();
        });
        delayPattern3.addObserver(a -> {
            NumberSetting<Double> setting = (NumberSetting<Double>) a;
            delayCalculator.delayPattern3 = setting.getCurrentValue();
        });

        registerSetting(
                sortMode, clickMode,
                minCPS, maxCPS,
                delayPatterns, delayPattern1, delayPattern2, delayPattern3,
                searchRange, attackRange,
                players, monsters, animals, invisibles,
                throughWalls, raycast, keepSprint, sprintFix
        );
    }

    private final Counter attackCounter = new Counter();
    private LivingEntity target;
    private List<LivingEntity> targets = new ArrayList<>();
    private Rotation rots;

    @Override
    public void onEnable() {
        super.onEnable();

        target = null;
        attackCounter.reset();
        rots = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Override
    public void onDisable() {
        rots = null;
        target = null;
        attackCounter.reset();
    }

    @EventTarget
    @HighestPriority
    public void onUpdate(EventUpdate event) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (target != null && (!target.isAlive() || target.getDistance(mc.player) > searchRange.getCurrentValue())) {
            target = null;
        }

        if (sprintFix.getCurrentValue() && target != null) {
            mc.player.setSprinting(false);
        }

        targets.clear();
        for (Entity entity : PlayerUtil.getAllEntitiesInWorld()) {
            if (!(entity instanceof LivingEntity living)) continue;

            boolean valid = false;
            if (players.currentValue && entity instanceof PlayerEntity) valid = true;
            if (animals.currentValue && (entity instanceof AnimalEntity || entity instanceof VillagerEntity))
                valid = true;
            if (monsters.currentValue && entity instanceof MonsterEntity) valid = true;
            if (!valid) continue;

            double distance = mc.player.getDistance(entity);
            if (distance > searchRange.currentValue) continue;

            if (!living.isAlive()
                    || entity == mc.player
                    || (!invisibles.currentValue && entity.isInvisible())
                    || entity.getName() == null
                    || entity.getDisplayName() == null
                    || Client.getInstance().friendManager.isFriend(entity)
                    || Client.getInstance().botManager.isBot(entity)) continue;

            targets.add(living);
        }

        if (!targets.isEmpty()) {
            targets.sort(EntityUtil.sortEntities(sortMode.currentValue));
            target = targets.get(0);

            Rotation calculatedRot = RotationUtils.getAdvancedRotation(target, !raycast.currentValue);
            if (calculatedRot != null) {
                // Add non-linear rotation changes to avoid Aim K detection
                float yawDifference = MathHelper.wrapAngleTo180_float(calculatedRot.yaw - rots.yaw);
                float pitchDifference = calculatedRot.pitch - rots.pitch;
                
                // Dynamic rotation speed with non-linear factors
                float distanceFactor = (float) Math.pow(Math.min(1.0f, 3.0f / target.getDistance(mc.player)), 1.3);
                float baseSpeed = 8.0f + (distanceFactor * 7.0f);
                
                // Non-linear randomization to break patterns
                float randomFactor = 0.85f + (float)(Math.random() * 0.3f);
                if (Math.random() > 0.7) {
                    randomFactor *= 0.92f + (float)(Math.random() * 0.16f);
                }
                
                // Apply non-linear smoothing
                float yawSpeed = Math.min(baseSpeed * randomFactor, Math.abs(yawDifference) * (0.4f + (float)(Math.random() * 0.2f)));
                float pitchSpeed = Math.min(baseSpeed * 0.8f * randomFactor, Math.abs(pitchDifference) * (0.4f + (float)(Math.random() * 0.2f)));
                
                // Ensure we never exceed the Vulcan threshold
                yawSpeed = Math.min(yawSpeed, 19.0f);
                pitchSpeed = Math.min(pitchSpeed, 19.0f);
                
                // Apply the rotation changes with non-linear adjustments
                if (Math.abs(yawDifference) > 0.1f) {
                    // Non-linear application to break GCD patterns
                    float appliedYaw = Math.signum(yawDifference) * yawSpeed;
                    if (Math.random() > 0.8) {
                        appliedYaw *= 0.94f + (float)(Math.random() * 0.12f);
                    }
                    rots.yaw += appliedYaw;
                }
                
                if (Math.abs(pitchDifference) > 0.1f) {
                    // Non-linear application to break GCD patterns
                    float appliedPitch = Math.signum(pitchDifference) * pitchSpeed;
                    if (Math.random() > 0.8) {
                        appliedPitch *= 0.94f + (float)(Math.random() * 0.12f);
                    }
                    rots.pitch += appliedPitch;
                }
                
                // Ensure pitch stays within bounds
                rots.pitch = MathHelper.clamp(rots.pitch, -90.0F, 90.0F);
            }
        } else {
            target = null;
        }
    }

    @EventTarget
    @HighestPriority
    public void onMotion(EventMotion event) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (target != null) {
            float[] fixedRotations = RotationUtils.gcdFix(
                    new float[]{rots.yaw, rots.pitch},
                    new float[]{mc.player.rotationYaw, mc.player.rotationPitch}
            );

            event.setYaw(fixedRotations[0]);
            event.setPitch(fixedRotations[1]);
        }
    }

    @EventTarget
    public void onSprint(EventKeepSprint event) {
        if (keepSprint.getCurrentValue()) {
            event.greater = false;
        }
    }

    @EventTarget
    @HighestPriority
    public void onRun(EventRunLoop event) {
        if (Client.getInstance().moduleManager.getModuleByClass(BlockFly.class).enabled)
            return;

        if (mc.player == null) {
            return;
        }

        if (target != null && target.getDistance(mc.player) <= attackRange.currentValue) {
            if (throughWalls.currentValue && !mc.player.canEntityBeSeen(target)) {
                return;
            }

            switch (clickMode.currentValue) {
                case "CPS" -> {
                    if (attackCounter.hasElapsed(delayCalculator.getClickDelay(), true)) {
                        attackEntity(target);
                    }
                }
                case "1.9" -> {
                    if (mc.player.getCooledAttackStrength(0) >= 1) {
                        attackEntity(target);
                    }
                }
            }
        }
    }
    private void attackEntity(LivingEntity entity) {
        var viaversion = JelloPortal.getVersion();
        if(viaversion.newerThanOrEqualTo(ProtocolVersion.v1_9)){
            mc.playerController.attackEntity(mc.player, entity);
            mc.player.swingArm(Hand.MAIN_HAND);
        }else{
            mc.player.swingArm(Hand.MAIN_HAND);
            mc.playerController.attackEntity(mc.player, entity);
        }
    }
}