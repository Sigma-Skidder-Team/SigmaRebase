package com.mentalfrostbyte.jello.module.impl.render;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderBlocks;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import team.sdhq.eventBus.annotations.EventTarget;

public class XRay extends Module {
    public static XRay instance;

    public XRay() {
        super(ModuleCategory.RENDER, "XRay", "Shows ores");
        instance = this;
    }

    @Override
    public void onEnable() {
        mc.worldRenderer.loadRenderers();
        Fullbright fullbrightModule = (Fullbright) Client.getInstance().moduleManager.getModuleByClass(Fullbright.class);
        if (!fullbrightModule.isEnabled()) {
            fullbrightModule.setState(true);
        }
    }

    @Override
    public void onDisable() {
        mc.worldRenderer.loadRenderers();
    }

    @EventTarget
    public void onRenderBlocks(EventRenderBlocks event) {
        if (this.isEnabled()) {
            AbstractBlock.AbstractBlockState blockState = event.getBlockState();
            if (!(blockState.getBlock() instanceof OreBlock) && blockState.getBlock() != Blocks.NETHERITE_BLOCK) {
                event.cancelled = true;
            } else {
                event.method13972(true);
            }
        }
    }
}
