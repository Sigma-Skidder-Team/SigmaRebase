package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.combat.aimbot.BasicAimbot;
import com.mentalfrostbyte.jello.module.impl.combat.aimbot.CandCAimbot;
import com.mentalfrostbyte.jello.module.impl.combat.aimbot.SmoothAimbot;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import com.mentalfrostbyte.jello.util.TeamsUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Iterator;
import java.util.List;

public class Aimbot extends ModuleWithModuleSettings {
    public Aimbot() {
        super(ModuleCategory.COMBAT,
                "Aimbot",
                "Automatically aim at players",
                new BasicAimbot(),
                new SmoothAimbot(),
                new CandCAimbot());
        this.registerSetting(new BooleanSetting("Players", "Aim at players", true));
        this.registerSetting(new BooleanSetting("Animals/Monsters", "Aim at animals and monsters", false));
        this.registerSetting(new BooleanSetting("Invisible", "Aim at invisible entites", true));
    }

    public Entity getTarget(float var1) {
        List var4 = MultiUtilities.getEntitesInWorld();
        Entity var5 = null;
        Iterator entityIterator = var4.iterator();

        while (entityIterator.hasNext()) {
            Entity target = (Entity) entityIterator.next();
            if (target != mc.player) {
                if (!Client.getInstance().friendManager.method26997(target)) {
                    if (target instanceof LivingEntity) {
                        if (((LivingEntity) target).getHealth() != 0.0F) {
                            if (!(mc.player.getDistance(target) > var1)) {
                                if (mc.player.canAttack((LivingEntity) target)) {
                                    if (!(target instanceof ArmorStandEntity)) {
                                        if (!this.getBooleanValueFromSettingName("Players") && target instanceof PlayerEntity) {
                                            entityIterator.remove();
                                        } else if (target instanceof PlayerEntity && Client.getInstance().combatManager.isTargetABot(target)) {
                                            entityIterator.remove();
                                        } else if (!this.getBooleanValueFromSettingName("Invisible") && target.isInvisible()) {
                                            entityIterator.remove();
                                        } else if (!this.getBooleanValueFromSettingName("Animals/Monsters") && !(target instanceof PlayerEntity)) {
                                            entityIterator.remove();
                                        } else if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity().equals(target)) {
                                            entityIterator.remove();
                                        } else if (!target.isInvulnerable()) {
                                            if (target instanceof PlayerEntity
                                                    && TeamsUtil.isTeammate((PlayerEntity) target)
                                                    && Client.getInstance().moduleManager.getModuleByClass(Teams.class).isEnabled()) {
                                                entityIterator.remove();
                                            } else if (var5 == null || mc.player.getDistance(target) < mc.player.getDistance(var5)) {
                                                var5 = target;
                                            }
                                        } else {
                                            entityIterator.remove();
                                        }
                                    } else {
                                        entityIterator.remove();
                                    }
                                } else {
                                    entityIterator.remove();
                                }
                            } else {
                                entityIterator.remove();
                            }
                        } else {
                            entityIterator.remove();
                        }
                    } else {
                        entityIterator.remove();
                    }
                } else {
                    entityIterator.remove();
                }
            } else {
                entityIterator.remove();
            }
        }

        return var5;
    }
}
