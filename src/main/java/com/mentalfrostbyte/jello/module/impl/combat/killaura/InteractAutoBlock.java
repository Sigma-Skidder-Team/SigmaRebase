package com.mentalfrostbyte.jello.module.impl.combat.killaura;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.client.rotation.util.RotationHelper;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.system.other.Pair;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.combat.Teams;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters.*;
import com.mentalfrostbyte.jello.module.impl.player.Blink;
import com.mentalfrostbyte.jello.module.impl.world.Disabler;
import com.mentalfrostbyte.jello.util.game.world.EntityUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InteractAutoBlock {
    private float[] cpsTimings;
    private Module parent;
    public Minecraft mc = Minecraft.getInstance();
    public boolean blocking;

    public InteractAutoBlock(Module parent) {
        this.parent = parent;
        this.initializeCpsTimings();
    }

    public boolean isBlocking() {
        return this.blocking;
    }

    public void setBlockingState(boolean blocking) {
        this.blocking = blocking;
    }

    public void performAutoBlock(Entity var1, float var2, float var3) {
        if (this.parent.getBooleanValueFromSettingName("Interact autoblock")) {
            EntityRayTraceResult var6 = EntityUtil.method17714(
                    !this.parent.getBooleanValueFromSettingName("Raytrace") ? var1 : null, var2, var3, var0 -> true,
                    (double) this.parent.getNumberValueBySettingName("Range"));
            if (var6 != null) {
                this.mc
                        .getConnection()
                        .sendPacket(new CUseEntityPacket(var6.getEntity(), Hand.MAIN_HAND, var6.getHitVec(),
                                this.mc.player.isSneaking()));
                this.mc.getConnection().sendPacket(
                        new CUseEntityPacket(var6.getEntity(), Hand.MAIN_HAND, this.mc.player.isSneaking()));
            }
        }

        CombatUtil.block();
        this.setBlockingState(true);
    }

    public void stopAutoBlock() {
        CombatUtil.unblock();
        this.setBlockingState(false);
    }

    public boolean canAutoBlock() {
        String settingValue = this.parent.getStringSettingValueByName("Autoblock Mode");
        return settingValue != null && !settingValue.equals("None")
                && Objects.requireNonNull(this.mc.player).getHeldItemMainhand().getItem() instanceof SwordItem
                && !this.isBlocking();
    }

    public void initializeCpsTimings() {
        this.cpsTimings = new float[3];
        float minCPT = 20.0F / this.parent.getNumberValueBySettingName("Min CPS");
        float maxCPT = 20.0F / this.parent.getNumberValueBySettingName("Max CPS");
        if (minCPT > maxCPT) {
            float mCPT = minCPT;
            minCPT = maxCPT;
            maxCPT = mCPT;
        }

        for (int i = 0; i < 3; i++) {
            float rand = minCPT + (float) Math.random() * (maxCPT - minCPT);
            this.cpsTimings[i] = rand;
        }
    }

    public float getCpsTiming(int var1) {
        return var1 >= 0 && var1 < this.cpsTimings.length ? this.cpsTimings[var1] : -1.0F;
    }

    public boolean shouldAttack(int var1) {
        int var4 = (int) this.getCpsTiming(0) - var1;
        boolean var5 = this.parent.getStringSettingValueByName("Attack Mode").equals("Pre");
        if (!var5) {
            var4++;
        }

        if (this.mc.player.getCooldownPeriod() > 1.26F && this.parent.getBooleanValueFromSettingName("Cooldown")) {
            int var11 = !var5 ? 1 : 2;
            float var12 = this.mc.player.getCooldownPeriod() - (float) this.mc.player.ticksSinceLastSwing
                    - (float) var11;
            return var12 <= 0.0F && var12 > -1.0F;
        } else if (var4 != 2) {
            if (var4 < 2) {
                float var6 = this.getCpsTiming(0);
                float var7 = this.getCpsTiming(1);
                float var8 = this.getCpsTiming(1);
                int var9 = (int) (var7 + var6 - (float) ((int) var6));
                if (var4 + var9 == 2) {
                    return true;
                }

                if (var4 + var9 == 1) {
                    float var10 = var6 + var7 + var8 - (float) ((int) var6 + (int) var7 + (int) var8);
                    return var10 < 1.0F;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    public boolean hasReachedCpsTiming(int var1) {
        return var1 >= (int) this.cpsTimings[0];
    }

    public void updateCpsTimings() {
        float minCPT = 20.0F / this.parent.getNumberValueBySettingName("Min CPS");
        float maxCPT = 20.0F / this.parent.getNumberValueBySettingName("Max CPS");
        if (minCPT > maxCPT) {
            float mCPT = minCPT;
            minCPT = maxCPT;
            maxCPT = mCPT;
        }

        float var8 = this.cpsTimings[0] - (float) ((int) this.cpsTimings[0]);
        this.cpsTimings[0] = this.cpsTimings[1] + var8;

        for (int var6 = 1; var6 < 3; var6++) {
            float var7 = minCPT + (float) Math.random() * (maxCPT - minCPT);
            this.cpsTimings[1] = var7;
        }
    }

    public List<TimedEntity> getPotentialTargets(float range) {
        ArrayList var4 = new ArrayList();

        for (Entity ent : EntityUtil.getEntitesInWorld(__ -> true)) {
            var4.add(new TimedEntity(ent));
        }

        Iterator var24 = var4.iterator();

        while (var24.hasNext()) {
            TimedEntity targetData = (TimedEntity)var24.next();
            Entity entity = targetData.getEntity();
            if (entity == this.mc.player || entity == Blink.clientPlayerEntity) {
                var24.remove();
            } else if (mc.player.getDistance(entity) > range) {
                var24.remove();
            } else if (Client.getInstance().friendManager.method26997(entity)) {
                var24.remove();
            } else if (!(entity instanceof LivingEntity)) {
                var24.remove();
            } else if (entity instanceof ArmorStandEntity) {
                var24.remove();
            } else if (((LivingEntity)entity).getHealth() == 0.0F) {
                var24.remove();
            } else if (!this.mc.player.canAttack((LivingEntity)entity)) {
                var24.remove();
            } else if (entity instanceof ArmorStandEntity) {
                var24.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Players") && entity instanceof PlayerEntity) {
                var24.remove();
            } else if (entity instanceof PlayerEntity && Client.getInstance().combatManager.isTargetABot(entity)) {
                var24.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Invisible") && entity.isInvisible()) {
                var24.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Animals") && (entity instanceof AnimalEntity || entity instanceof VillagerEntity)) {
                var24.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Monsters") && entity instanceof MonsterEntity) {
                var24.remove();
            } else if (this.mc.player.getRidingEntity() != null && this.mc.player.getRidingEntity().equals(entity)) {
                var24.remove();
            } else if (entity.isInvulnerable()) {
                var24.remove();
            } else if (!(entity instanceof PlayerEntity) || !CombatUtil.arePlayersOnSameTeam((PlayerEntity)entity) || !Client.getInstance().moduleManager.getModuleByClass(Teams.class).isEnabled()) {

                if (!this.parent.getBooleanValueFromSettingName("Through walls")) {
                    Rotation var28 = RotationHelper.getRotations(entity, true);
                    if (var28 == null) {
                        var24.remove();
                    }
                }

            } else {
                var24.remove();
            }
        }

        Collections.sort(var4, new FriendSorter2(this));
        return var4;
    }


    public List<TimedEntity> sortTargets(List<TimedEntity> timedEntities) {
        switch (this.parent.getStringSettingValueByName("Sort Mode")) {
            case "Range":
                timedEntities.sort(new RangeSorter(this));
                break;
            case "Health":
                timedEntities.sort(new HealthSorter(this));
                break;
            case "Angle":
                timedEntities.sort(new AngleSorter(this));
                break;
            case "Prev Range":
                timedEntities.sort(new PrevRangeSorter(this));
                break;
            case "Armor":
                timedEntities.sort(new ArmorSorter(this));
        }

        timedEntities.sort(new FriendSorter(this));
        return timedEntities;
    }
}