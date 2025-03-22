package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.world.Timer;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class FastEat extends Module {
    public FastEat() {
        super(ModuleCategory.PLAYER, "FastEat", "Allows you to eat faster");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Basic", "Hypixel", "Timer")

        );
        this.registerSetting(new NumberSetting<Float>("Speed", "Eating speed.", 0.55F, Float.class, 0.0F, 1.0F, 0.01F));
    }

    @EventTarget
    public void TickEvent(EventPlayerTick event) {
        if (this.isEnabled() && mc.player != null) {
            if (mc.player.isHandActive()) {
                ItemStack itemStack = mc.player.getActiveItemStack();
                if (itemStack != null
                        && (itemStack.getUseAction() == UseAction.DRINK || itemStack.getUseAction() == UseAction.EAT)
                        && (float) mc.player.getItemInUseCount() < this.getNumberValueBySettingName("Speed") * 32.0F) {
                    String mode = this.getStringSettingValueByName("Mode");
                    switch (mode) {
                        case "Timer":
                            Client.getInstance().moduleManager.getModuleByClass(Timer.class).toggle();
                            break;
                        case "Basic":
                            int x = mc.player.getItemInUseCount() + 2;

                            for (int i = 0; i < x; i++) {
                                mc.getConnection().sendPacket(new CPlayerPacket(mc.player.isOnGround()));
                                mc.player.stopActiveHand();
                            }
                            break;
                        case "Hypixel":
                            mc.getConnection()
                                    .sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem + 1 >= 9 ? 0 : mc.player.inventory.currentItem + 1));
                            mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                            break;
                    }
                }
            }
        }
    }
}
