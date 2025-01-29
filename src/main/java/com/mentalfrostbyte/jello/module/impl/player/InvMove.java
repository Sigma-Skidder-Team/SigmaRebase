package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.misc.Class1144;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CEntityActionPacket;
import org.lwjgl.glfw.GLFW;
import team.sdhq.eventBus.annotations.EventTarget;

public class InvMove extends Module {
    public boolean field23757;

    public InvMove() {
        super(ModuleCategory.PLAYER, "InvMove", "Move freely in the inventory");
        this.registerSetting(new BooleanSetting("AACP", "Bypass for AACP", true));
        this.registerSetting(new BooleanSetting("Hypixel", "Bypass for Hypixel", false));
        this.field23757 = false;
    }

    @EventTarget
    public void method16583(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (var1.getKey() == mc.gameSettings.keyBindInventory.keyCode.getKeyCode() && mc.player.isSprinting()) {
            }
        }
    }

    @EventTarget
    public void method16584(EventSendPacket var1) {
        if (this.isEnabled()) {
            if (this.field23757 && var1.getPacket() instanceof CEntityActionPacket
                    && this.getBooleanValueFromSettingName("AACP")) {
                CEntityActionPacket var4 = (CEntityActionPacket) var1.getPacket();
                if (var4.getAction() == CEntityActionPacket.Action.START_SPRINTING) {
                    var1.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    public void method16585(EventPlayerTick var1) {
        if (this.isEnabled()) {
            boolean isInventoryScreen = mc.currentScreen instanceof InventoryScreen
                    || mc.currentScreen instanceof ChestScreen;
            if (this.getBooleanValueFromSettingName("AACP")) {
                boolean var4 = !(mc.currentScreen instanceof InventoryScreen)
                        || !(mc.currentScreen instanceof ChestScreen);
                if (this.field23757 && !var4) {
                    this.field23757 = !this.field23757;
                    if (mc.player.isSprinting()) {
                        mc.getConnection().sendPacket(
                                new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                    }
                } else if (!this.field23757 && var4) {
                    this.field23757 = !this.field23757;
                    if (mc.player.isSprinting()) {
                        mc.getConnection().sendPacket(
                                new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                    }
                }
            }

            if (mc.currentScreen instanceof ContainerScreen
                    || Client.getInstance().playerTracker.focusGameTicks() <= 1) {
                if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof AnvilScreen
                        || mc.currentScreen instanceof Class1144) {
                    return;
                }

                if (mc.currentScreen instanceof CreativeScreen) {
                    CreativeScreen var9 = (CreativeScreen) mc.currentScreen;
                    if (var9.getSelectedTabIndex() == 5) {
                        return;
                    }
                }

                if (Client.getInstance().guiManager.method33480() != null
                        && Client.getInstance().guiManager.method33480().method13227()) {
                    for (KeyBinding var14 : MinecraftClient.getInstance().gameSettings.keyBindings) {
                        var14.setPressed(false);
                    }

                    return;
                }

                for (KeyBinding bind : mc.gameSettings.keyBindings) {
                    if (bind.keyCode.getKeyCode() > 0
                            && mc.gameSettings.keyBindSneak.keyCode.getKeyCode() != bind.keyCode.getKeyCode()
                            && bind.keyCode.getKeyCode() > 4) {
                        int var8 = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), bind.keyCode.getKeyCode());
                        bind.setPressed(var8 == 1);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMove(EventMove event) {
        if (this.isEnabled() && this.getBooleanValueFromSettingName("Hypixel")) {
            if (mc.currentScreen instanceof ContainerScreen ||
                    mc.currentScreen instanceof AnvilScreen ||
                    mc.currentScreen instanceof ChestScreen) {
                double baseSpeed = 0.7;
                event.setX(event.getX() * baseSpeed);
                event.setZ(event.getZ() * baseSpeed);
            }
        }
    }
}
