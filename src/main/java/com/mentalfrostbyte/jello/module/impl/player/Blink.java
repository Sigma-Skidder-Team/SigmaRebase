package com.mentalfrostbyte.jello.module.impl.player;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.NoSlow;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.vector.Vector3d;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    public static RemoteClientPlayerEntity clientPlayerEntity;
    public float yaw;
    public float pitch;

    private final List<IPacket<?>> packets = new ArrayList<>();
    private Vector3d vector;

    public Blink() {
        super(ModuleCategory.PLAYER, "Blink", "Stops your packets to blink");
    }

    @Override
    public void onEnable() {
        this.vector = new Vector3d(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        this.yaw = mc.player.rotationYaw;
        this.pitch = mc.player.rotationPitch;
        clientPlayerEntity = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
        clientPlayerEntity.inventory = mc.player.inventory;
        clientPlayerEntity.setPositionAndRotation(this.vector.x, this.vector.y, this.vector.z, this.yaw, this.pitch);
        clientPlayerEntity.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntity(-1, clientPlayerEntity);
    }

    @Override
    public void onDisable() {
        for (IPacket<?> packet : this.packets) {
            mc.getConnection().sendPacket(packet);
        }
        this.packets.clear();
        mc.world.removeEntityFromWorld(-1);
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (mc.player != null && (event.packet instanceof CEntityActionPacket
                || event.packet instanceof CPlayerPacket
                || event.packet instanceof CUseEntityPacket
                || event.packet instanceof CAnimateHandPacket
                || event.packet instanceof CPlayerTryUseItemPacket)) {
            this.packets.add(event.packet);
            event.cancelled = true;
        }
    }

    public static void handleEatingBlink() {
        NoSlow noSlow = (NoSlow) Client.getInstance().moduleManager.getModuleByClass(NoSlow.class);
        Blink blink = (Blink) Client.getInstance().moduleManager.getModuleByClass(Blink.class);

        if (noSlow != null && blink != null && noSlow.isEnabled() && noSlow.getStringSettingValueByName("Mode").equals("Hypixel")) {
            if (mc.player.isHandActive() && mc.player.getHeldItem(mc.player.getActiveHand()).getItem().isFood()) {
                if (!blink.isEnabled()) {
                    blink.setEnabled(true);
                }
            } else {
                if (blink.isEnabled()) {
                    blink.setEnabled(false);
                }
            }
        }
    }
}
