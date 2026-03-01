package com.mentalfrostbyte.jello.module.impl.render.classic.esp;


import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventUpdate;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import net.minecraft.entity.Entity;
import team.sdhq.eventBus.annotations.EventTarget;

public class VanillaESP extends Module {
    public VanillaESP() {
        super(ModuleCategory.RENDER, "Vanilla", "Draws a line arround players");
    }

    @EventTarget
    public void method16625(EventUpdate var1) {
        if (this.isEnabled()) {
            for (Entity var5 : mc.world.getAllEntities()) {
                if (!Client.getInstance().botManager.isBot(var5)) {
                    boolean var6 = PlayerUtil.getEntityCategory(var5) == PlayerUtil.EntityTypeCategory.PLAYER && this.getParent().getBooleanValueFromSettingName("Show Players");
                    boolean var7 = PlayerUtil.getEntityCategory(var5) == PlayerUtil.EntityTypeCategory.MONSTER && this.getParent().getBooleanValueFromSettingName("Show Mobs");
                    boolean var8 = PlayerUtil.getEntityCategory(var5) == PlayerUtil.EntityTypeCategory.NON_PLAYER && this.getParent().getBooleanValueFromSettingName("Show Passives");
                    boolean var9 = !var5.isInvisible() || this.getParent().getBooleanValueFromSettingName("Show Invisibles");
                    var5.setGlowing((var7 || var6 || var8) && var9 && var5 != mc.player);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        for (Entity var4 : mc.world.getAllEntities()) {
            var4.setGlowing(false);
        }
    }
}
