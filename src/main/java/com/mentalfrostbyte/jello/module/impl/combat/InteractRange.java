package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.jello.event.impl.game.action.EventClick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.EntityUtil;
import com.mentalfrostbyte.jello.util.world.BlockUtil;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;

public class InteractRange extends Module {

    public InteractRange() {
        super(ModuleCategory.COMBAT, "InteractRange", "Allows you to interact farer away");
        this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F, Float.class, 3.0F, 8.0F, 0.01F));
    }

    @EventTarget
    public void onClick(EventClick event) {
        if (this.isEnabled()) {
            Entity var4 = EntityUtil.getEntityFromRayTrace(mc.player.rotationYaw, mc.player.rotationPitch, this.getNumberValueBySettingName("Range"), 0.0);
            BlockRayTraceResult var5 = BlockUtil.rayTrace(mc.player.rotationYaw, mc.player.rotationPitch, this.getNumberValueBySettingName("Range"));
            if (var4 != null && mc.objectMouseOver.getType() == RayTraceResult.Type.MISS) {
                mc.objectMouseOver = new EntityRayTraceResult(var4);
            }

            if (var4 == null && mc.objectMouseOver.getType() == RayTraceResult.Type.MISS && var5 != null) {
                mc.objectMouseOver = var5;
            }
        }
    }
}