package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.item.InvManager;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import team.sdhq.eventBus.annotations.EventTarget;

// Skidded from titties client lol
public class HypixelPredictionDisabler extends Module {
    public static boolean watchDogDisabled = false;
    public static boolean stuckOnAir = false;
    public static int airStuckTicks = 0;
    public static boolean startDisabler = false;
    public static int airTicks = 0;
    private final BooleanSetting motion;
    private final BooleanSetting inventoryMove;
    public boolean sentFirstOpen;
    public boolean caughtClientStatus;
    public boolean caughtCloseWindow;

    public HypixelPredictionDisabler() {
        super(
                ModuleCategory.EXPLOIT,
                "Hypixel Prediction",
                "Disables some checks for Hypixel's Prediction anti-cheat (untested)"
        );
        this.registerSetting(this.motion = new BooleanSetting("Motion", "Motion check disabler", true));
        this.registerSetting(this.inventoryMove = new BooleanSetting("InvMove", "Inventory move check disabler", true));
    }

    @Override
    public void onEnable() {
        super.onEnable();

        startDisabler = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        startDisabler = false;
        watchDogDisabled = false;
        stuckOnAir = false;
        airStuckTicks = 0;
        airTicks = 0;
        caughtClientStatus = false;
        caughtCloseWindow = false;
        sentFirstOpen = false;
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer __) {
        if (HypixelPredictionDisabler.mc.player == null)
            return;

        if (motion.currentValue && !watchDogDisabled) {
            airTicks = HypixelDisabler.mc.player.isOnGround() ? 0 : ++airTicks;
            if (stuckOnAir && airTicks >= 9) {
                MovementUtil.stop();
            }
        }
        if (inventoryMove.currentValue) {
            caughtClientStatus = false;
            caughtCloseWindow = false;
            // TODO: invmanager thingy
            if (mc.currentScreen instanceof InventoryScreen || Client.getInstance().moduleManager.getModuleByClass(InvManager.class).isEnabled()/* && InvManager*/) {
                if (!sentFirstOpen) {
                    mc.currentScreen.closeScreen();
                    mc.getConnection().getNetworkManager().sendNoEventPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                    sentFirstOpen = true;
                }

                if (mc.player.ticksExisted % (mc.player.isPotionActive(Effects.SPEED) ? 3 : 4) == 0) {
                    mc.getConnection().getNetworkManager().sendNoEventPacket(new CCloseWindowPacket(0));
                } else if (mc.player.ticksExisted % (mc.player.isPotionActive(Effects.SPEED) ? 3 : 4) == 1) {
                    mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
                }
            } else {
                sentFirstOpen = false;
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (mc.player == null) return;
        if (event.packet instanceof CClientStatusPacket) {
            if (caughtClientStatus) {
                event.cancelled = true;
            }

            caughtClientStatus = false;
        }
        if (event.packet instanceof CCloseWindowPacket) {
            if (caughtCloseWindow) {
                event.cancelled = true;
            }

            caughtCloseWindow = true;
        }

        if (motion.currentValue && !watchDogDisabled && stuckOnAir && event.packet instanceof SPlayerPositionLookPacket) {
            airStuckTicks++;
            if (airStuckTicks >= 20) {
                MinecraftUtil.addChatMessage("Watchdog jump checks disabled.");
                airStuckTicks = 0;
                airTicks = 0;
                stuckOnAir = false;
                watchDogDisabled = true;
            }
        }
    }

    @EventTarget
    public void onMove(EventMove event) {
        if (HypixelDisabler.mc.player == null) {
            return;
        }
        if (motion.currentValue && !watchDogDisabled) {
            if (startDisabler && HypixelDisabler.mc.player.isOnGround()) {
                HypixelDisabler.mc.player.jump();
                startDisabler = false;
                stuckOnAir = true;
            } else if (stuckOnAir && airTicks >= 9) {
                if (airTicks % 2 == 0) {
                    event.setZ(event.getZ() + 0.095);
                }
                mc.player.setMotion(mc.player.getMotion().x, 0.0, mc.player.getMotion().z);
            }
        }
    }
    @EventTarget
    public void onWorldLoad(EventLoadWorld event) {
        watchDogDisabled = false;
        stuckOnAir = false;
        airStuckTicks = 0;
        airTicks = 0;
        sentFirstOpen = false;
        caughtClientStatus = false;
        caughtCloseWindow = false;
        startDisabler = true;
    }
}
