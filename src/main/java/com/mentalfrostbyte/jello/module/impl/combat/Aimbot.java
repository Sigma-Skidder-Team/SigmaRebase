package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.combat.aimbot.BasicAimbot;
import com.mentalfrostbyte.jello.module.impl.combat.aimbot.CandCAimbot;
import com.mentalfrostbyte.jello.module.impl.combat.aimbot.SmoothAimbot;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.EntityUtil;
import com.mentalfrostbyte.jello.util.player.TeamUtil;
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

    public Entity getTarget(float maxDistance) {
        List<Entity> entities = EntityUtil.getEntitesInWorld(__ -> true);
        Entity target = null;

        for (Entity entity : entities) {
            if (entity != mc.player) {
                if (!Client.getInstance().friendManager.method26997(entity)) {
                    if (entity instanceof LivingEntity) {
                        if (((LivingEntity) entity).getHealth() != 0.0F) {
                            if (!(mc.player.getDistance(entity) > maxDistance)) {
                                if (mc.player.canAttack((LivingEntity) entity)) {
                                    if (entity instanceof ArmorStandEntity)
                                        continue;
                                    if (!this.getBooleanValueFromSettingName("Players") && entity instanceof PlayerEntity) {
                                        
                                    } else if (entity instanceof PlayerEntity && Client.getInstance().combatManager.isTargetABot(entity)) {
                                        
                                    } else if (!this.getBooleanValueFromSettingName("Invisible") && entity.isInvisible()) {
                                        
                                    } else if (!this.getBooleanValueFromSettingName("Animals/Monsters") && !(entity instanceof PlayerEntity)) {
                                        
                                    } else if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity().equals(entity)) {
                                        
                                    } else if (!entity.isInvulnerable()) {
                                        if (entity instanceof PlayerEntity
                                                && TeamUtil.method31662((PlayerEntity) entity)
                                                && Client.getInstance().moduleManager.getModuleByClass(Teams.class).isEnabled()) {
                                            
                                        } else if (target == null || mc.player.getDistance(entity) < mc.player.getDistance(target)) {
                                            target = entity;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return target;
    }
}
