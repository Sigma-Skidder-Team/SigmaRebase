package com.mentalfrostbyte.jello.module.impl.world.disabler;

//import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
//import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.player.NoFall;
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
    //    public static RemoteClientPlayerEntity clientPlayerEntity;
    private final BooleanSetting floatingTooLongKickBypass;
    private final NumberSetting<Integer> bypassDelay;
    private int ticksSinceClientOffGround;
    //    private boolean wasSetback;
    private boolean waitForPos;
    //    private long ticksSinceLagback;
//    private boolean dontCancelPacket;
    private Vector3d serverPos;
    private Rotation serverRot;

    public MinibloxDisabler() {
        super(ModuleCategory.EXPLOIT, "Miniblox", "Shitty & Horrible but working disabler for Miniblox.");
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
                        20, Integer.class,
                        5, 20,
                        1
                )
        );
    }

//    void updateServerPlayer() {
//        assert mc.player != null;
//        clientPlayerEntity = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
//        clientPlayerEntity.inventory = mc.player.inventory;
//        clientPlayerEntity.setPositionAndRotation(serverPos.x, serverPos.y, serverPos.z, serverRot.yaw, serverRot.pitch);
//        clientPlayerEntity.rotationYawHead = mc.player.rotationYawHead;
//        clientPlayerEntity.collidedHorizontally = false;
//        clientPlayerEntity.collidedVertically = false;
//    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;
        serverPos = new Vector3d(mc.player.lastReportedPosX, mc.player.lastReportedPosY, mc.player.lastReportedPosZ);
        serverRot = new Rotation(mc.player.lastReportedYaw, mc.player.lastReportedPitch);
//        updateServerPlayer();
//        assert mc.world != null;
//        mc.world.addEntity(-2, clientPlayerEntity);
    }

    @Override
    public void onDisable() {
        super.onDisable();
//        if (clientPlayerEntity != null) {
//            clientPlayerEntity.remove();
//            mc.world.removeEntityFromWorld(clientPlayerEntity.getEntityId());
//            clientPlayerEntity = null;
//        }
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onSendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof CClientStatusPacket packet && packet.getStatus() == CClientStatusPacket.State.PERFORM_RESPAWN) {
            waitForPos = true;
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onReceivedPacket(EventReceivePacket event) {
        IPacket<?> rawPacket = event.getPacket();
        if (rawPacket instanceof SRespawnPacket) {
            waitForPos = true;
            return;
        }
        if (mc.player == null) return;
        if (rawPacket instanceof SEntityHeadLookPacket/* && !dontCancelPacket*/) {
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
//            if (!dontCancelPacket)
//            ticksSinceLagback = 0;
//            wasSetback = true;
            serverPos = new Vector3d(packet.getX(), packet.getY(), packet.getZ());
            serverRot = new Rotation(packet.getYaw(), packet.getPitch());
//            updateServerPlayer();
            mc.getConnection().sendPacket(new CPlayerPacket.PositionRotationPacket(
                            serverPos.x, serverPos.y, serverPos.z,
                            serverRot.yaw, serverRot.pitch, mc.player.isOnGround()
                    )
            );
            // prevent a too many packets kick, also happens to make this disabler the same as the Rise disabler :skull:
            if (!mc.player.isOnGround()) {
                CPlayerPacket.PositionRotationPacket posPacket = getLagbackResponsePacket();

                mc.getConnection().sendPacket(posPacket);
            }
            event.cancelled = true;
            // this disabler probably performs worse with this, since if the server accepts our pos,
            // and we'll probably be far away
//            mc.getConnection().sendPacket(new CPlayerPacket.PositionRotationPacket(
//                            serverPos.x, serverPos.y, serverPos.z,
//                            serverRot.yaw, serverRot.pitch, mc.player.isOnGround()
//                    )
//            );
        } else {
            serverPos = new Vector3d(mc.player.lastReportedPosX, mc.player.lastReportedPosY, mc.player.lastReportedPosZ);
            serverRot = new Rotation(mc.player.lastReportedYaw, mc.player.lastReportedPitch);
//            clientPlayerEntity.inventory = mc.player.inventory;
//            clientPlayerEntity.collidedHorizontally = false;
//            clientPlayerEntity.collidedVertically = false;
//            clientPlayerEntity.setPositionAndRotation(serverPos.x, serverPos.y, serverPos.z, serverRot.yaw, serverRot.pitch);
//            clientPlayerEntity.rotationYawHead = mc.player.rotationYawHead;
//            ticksSinceLagback++;
        }
//        if (ticksSinceLagback >= 15e3 && wasSetback && !mc.player.isOnGround() && Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()) {
//            MinecraftUtil.addChatMessage("trying to Resync");
//            dontCancelPacket = true;
//            mc.player.sendChatMessage("/resync");
//        }
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