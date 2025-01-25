package com.mentalfrostbyte.jello.module.impl.render;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.misc.Class3420;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import team.sdhq.eventBus.annotations.EventTarget;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderBlocks;

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
            AbstractBlock.AbstractBlockState blockState = event.method13970();
            if (!(blockState.getBlock() instanceof Class3420) && blockState.getBlock() != Blocks.NETHERITE_BLOCK) {
                event.setCancelled(true);
            } else {
                event.method13972(true);
            }
        }
    }
}
