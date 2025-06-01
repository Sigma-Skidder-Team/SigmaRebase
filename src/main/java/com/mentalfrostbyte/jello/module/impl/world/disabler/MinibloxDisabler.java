package com.mentalfrostbyte.jello.module.impl.world.disabler;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
//import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.player.nofall.CancelNoFall;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.constructor.Rotation;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SEntityHeadLookPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import team.sdhq.eventBus.annotations.EventTarget;

public class MinibloxDisabler extends Module {
    private final BooleanSetting floatingTooLongKickBypass;
    private final NumberSetting<Integer> bypassDelay;
    private int ticksSinceClientOffGround;
    private boolean waitForPos;
    private Vector3d serverPos;
    private Rotation serverRot;

    public MinibloxDisabler() {
        super(ModuleCategory.EXPLOIT, "Miniblox", "Disabler for Miniblox. (not the prediction ac)");
        this.registerSetting(
                this.floatingTooLongKickBypass = new BooleanSetting(
                        "Floating Kick Bypass",
                        "Sets onGround to true after a specified amount of ticks",
                        true
                )
        );
        this.registerSetting(
                this.bypassDelay = new NumberSetting<>(
                        "Floating Kick Bypass Delay",
                        "Ticks off ground before we spoof our ground value",
                        20,
                        5, 20,
                        1
                )
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;
        serverPos = new Vector3d(mc.player.lastReportedPosX, mc.player.lastReportedPosY, mc.player.lastReportedPosZ);
        serverRot = new Rotation(mc.player.lastReportedYaw, mc.player.lastReportedPitch);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onSendPacket(EventSendPacket event) {
        if (event.packet instanceof CClientStatusPacket packet && packet.getStatus() == CClientStatusPacket.State.PERFORM_RESPAWN) {
            waitForPos = true;
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onReceivedPacket(EventReceivePacket event) {
        IPacket<?> rawPacket = event.packet;
        if (rawPacket instanceof SRespawnPacket) {
            waitForPos = true;
            return;
        }
        if (mc.player == null) return;
        if (rawPacket instanceof SEntityHeadLookPacket) {
            event.cancelled = true;
        }
        if (rawPacket instanceof SPlayerPositionLookPacket packet
                && mc.getConnection() != null
                && mc.player.ticksExisted >= 100
                && !CancelNoFall.falling) {
            if (waitForPos) {
                MinecraftUtil.addChatMessage("[Miniblox Disabler] Accepting pos due to respawn");
                waitForPos = false;
                return;
            }
            serverPos = new Vector3d(packet.getX(), packet.getY(), packet.getZ());
            serverRot = new Rotation(packet.getYaw(), packet.getPitch());
            mc.getConnection().sendPacket(new CPlayerPacket.PositionRotationPacket(
                            serverPos.x, serverPos.y, serverPos.z,
                            serverRot.yaw, serverRot.pitch, mc.player.isOnGround()
                    )
            );
            // prevent too many packets kicks, also happens to make this disabler the same as the Rise disabler :skull:
            if (!mc.player.isOnGround()) {
                CPlayerPacket.PositionRotationPacket posPacket = getLagbackResponsePacket();

                mc.getConnection().sendPacket(posPacket);
            }
            event.cancelled = true;
        } else {
            serverPos = new Vector3d(mc.player.lastReportedPosX, mc.player.lastReportedPosY, mc.player.lastReportedPosZ);
            serverRot = new Rotation(mc.player.lastReportedYaw, mc.player.lastReportedPitch);
        }
    }

    @NotNull
    private CPlayerPacket.PositionRotationPacket getLagbackResponsePacket() {
        boolean spoofGround = floatingTooLongKickBypass.currentValue && ticksSinceClientOffGround + 1 >= bypassDelay.currentValue;
        assert mc.player != null;
        CPlayerPacket.PositionRotationPacket posPacket = new CPlayerPacket.PositionRotationPacket(
                mc.player.getPosX(),
                mc.player.getPosY(),
                mc.player.getPosZ(),
                mc.player.rotationYaw,
                mc.player.rotationPitch,
                spoofGround || mc.player.isOnGround()
        );
        if (posPacket.onGround)
            ticksSinceClientOffGround = 0;
        else
            ticksSinceClientOffGround++;
        return posPacket;
    }
}