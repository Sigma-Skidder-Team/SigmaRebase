package com.mentalfrostbyte.jello.module.impl.combat.killaura;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.misc.Pair;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.impl.combat.Teams;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.sorters.*;
import com.mentalfrostbyte.jello.module.impl.player.Blink;
import com.mentalfrostbyte.jello.util.EntityUtil;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.player.RotationHelper;
import com.mentalfrostbyte.jello.util.player.Rotations;
import com.mentalfrostbyte.jello.util.player.TeamUtil;
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
    private float[] field44344;
    public final int field44345 = 3;
    private Module parent;
    public Minecraft mc = Minecraft.getInstance();
    public boolean blocking;
    public HashMap<Entity, List<Pair<Vector3d, Long>>> field44349 = new HashMap<>();

    public InteractAutoBlock(Module parent) {
        this.parent = parent;
        this.method36818();
    }

    public boolean isBlocking() {
        return this.blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void block(Entity var1, float var2, float var3) {
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

        MultiUtilities.block();
        this.setBlocking(true);
    }

    public void method36816() {
        MultiUtilities.unblock();
        this.setBlocking(false);
    }

    public boolean canBlock() {
        String settingValue = this.parent.getStringSettingValueByName("Auto block mode");
        return settingValue != null && !settingValue.equals("None")
                && Objects.requireNonNull(this.mc.player).getHeldItemMainhand().getItem() instanceof SwordItem
                && !this.isBlocking();
    }

    public void method36818() {
        this.field44344 = new float[3];
        float var3 = 20.0F / this.parent.getNumberValueBySettingName("Min CPS");
        float var4 = 20.0F / this.parent.getNumberValueBySettingName("Max CPS");
        if (var3 > var4) {
            float var5 = var3;
            var3 = var4;
            var4 = var5;
        }

        for (int var7 = 0; var7 < 3; var7++) {
            float var6 = var3 + (float) Math.random() * (var4 - var3);
            this.field44344[var7] = var6;
        }
    }

    public float getInitialDelay(int var1) {
        return var1 >= 0 && var1 < this.field44344.length ? this.field44344[var1] : -1.0F;
    }

    public boolean method36820(int var1) {
        int var4 = (int) this.getInitialDelay(0) - var1;
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
                float var6 = this.getInitialDelay(0);
                float var7 = this.getInitialDelay(1);
                float var8 = this.getInitialDelay(1);
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

    public boolean method36821(int var1) {
        return var1 >= (int) this.field44344[0];
    }

    public void setupDelay() {
        float var3 = 20.0F / this.parent.getNumberValueBySettingName("Min CPS");
        float var4 = 20.0F / this.parent.getNumberValueBySettingName("Max CPS");
        if (var3 > var4) {
            float var5 = var3;
            var3 = var4;
            var4 = var5;
        }

        float var8 = this.field44344[0] - (float) ((int) this.field44344[0]);
        this.field44344[0] = this.field44344[1] + var8;

        for (int var6 = 1; var6 < 3; var6++) {
            float var7 = var3 + (float) Math.random() * (var4 - var3);
            this.field44344[1] = var7;
        }
    }

    public List<TimedEntity> getEntitiesInRange(float var1) {
        ArrayList<TimedEntity> timedEntityList = new ArrayList<>();

        for (Entity ent : EntityUtil.getEntitesInWorld(__ -> true)) {
            timedEntityList.add(new TimedEntity(ent));
        }

        Iterator<TimedEntity> entities = timedEntityList.iterator();
        // TODO: uncomment these when Disabler is ported
        // ModuleWithModuleSettings disabler = (ModuleWithModuleSettings)
        // Client.getInstance().moduleManager.getModuleByClass(Disabler.class);
        float ping = 150.0F;
        // if (disabler.isEnabled() &&
        // disabler.getStringSettingValueByName("Type").equalsIgnoreCase("PingSpoof")) {
        // ping +=
        // disabler.getModWithTypeSetToName().getNumberValueBySettingName("Lag");
        // }

        while (entities.hasNext()) {
            TimedEntity timedEntity = entities.next();
            Entity ent = timedEntity.getEntity();
            if (ent == this.mc.player || ent == Blink.clientPlayerEntity) {
                entities.remove();
            } else if (Client.getInstance().friendManager.method26997(ent)) {
                entities.remove();
            } else if (!(ent instanceof LivingEntity)) {
                entities.remove();
            } else if (((LivingEntity) ent).getHealth() == 0.0F) {
                entities.remove();
            } else if (!this.mc.player.canAttack((LivingEntity) ent)) {
                entities.remove();
            } else if (ent instanceof ArmorStandEntity) {
                entities.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Players") && ent instanceof PlayerEntity) {
                entities.remove();
            } else if (ent instanceof PlayerEntity && Client.getInstance().combatManager.isTargetABot(ent)) {
                entities.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Invisible") && ent.isInvisible()) {
                entities.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Animals")
                    && (ent instanceof AnimalEntity || ent instanceof VillagerEntity)) {
                entities.remove();
            } else if (!this.parent.getBooleanValueFromSettingName("Monsters") && ent instanceof MonsterEntity) {
                entities.remove();
            } else if (this.mc.player.getRidingEntity() != null && this.mc.player.getRidingEntity().equals(ent)) {
                entities.remove();
            } else if (ent.isInvulnerable()) {
                entities.remove();
            } else if (!(ent instanceof PlayerEntity)
                    || !TeamUtil.method31662((PlayerEntity) ent)
                    || !Client.getInstance().moduleManager.getModuleByClass(Teams.class).isEnabled()) {
                Vector3d var10 = MultiUtilities.method17751(ent);
                if (!(this.mc.player.getDistance(ent) < 40.0F)) {
                    if (this.field44349.containsKey(ent)) {
                        this.field44349.remove(ent);
                    }
                } else if (!this.field44349.containsKey(ent)) {
                    this.field44349.put(ent, new ArrayList<Pair<Vector3d, Long>>());
                } else {
                    for (List var12 : this.field44349.values()) {
                        int var13 = var12.size();

                        for (int var14 = 0; var14 < var13; var14++) {
                            Pair var15 = (Pair) var12.get(var14);
                            if (var15 != null) {
                                Long var16 = (Long) var15.getValue();
                                long var17 = System.currentTimeMillis() - var16;
                                if ((float) var17 > ping) {
                                    var12.remove(var14);
                                    var13--;
                                }
                            }
                        }
                    }
                }

                if (!(MultiUtilities.method17754(var10) > 8.0)) {
                    boolean var26 = true;
                    if (this.parent.getBooleanValueFromSettingName("Smart Reach")) {
                        List<Pair<Vector3d, Long>> var27 = this.field44349.get(ent);
                        if (var27 != null) {
                            for (Pair<Vector3d, Long> var30 : var27) {
                                AxisAlignedBB var21 = getAxisAlignedBBThing(var30);
                                double var22 = MultiUtilities.method17755(var21);
                                if (var22 < (double) var1) {
                                    var26 = false;
                                }
                            }
                        }
                    }

                    if (var26 && MultiUtilities.method17754(var10) > (double) var1) {
                        entities.remove();
                    } else if (!this.parent.getBooleanValueFromSettingName("Through walls")) {
                        Rotations rotations = RotationHelper.getRotations(ent, true);
                        if (rotations == null) {
                            entities.remove();
                        }
                    }
                } else {
                    entities.remove();
                }
            } else {
                entities.remove();
            }
        }

        timedEntityList.sort(new FriendSorter2(this));
        return timedEntityList;
    }

    private @NotNull AxisAlignedBB getAxisAlignedBBThing(Pair<Vector3d, Long> var30) {
        Vector3d var31 = var30.getKey();
        double var19 = 0.15;
        AxisAlignedBB var21 = new AxisAlignedBB(
                var31.x - var19,
                var31.y,
                var31.z - var19,
                var31.x + var19,
                var31.y + this.mc.player.getBoundingBox().getYSize(),
                var31.z + var19);
        return var21;
    }

    public List<TimedEntity> sortEntities(List<TimedEntity> timedEntities) {
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