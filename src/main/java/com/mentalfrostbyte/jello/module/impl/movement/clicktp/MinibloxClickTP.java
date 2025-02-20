package com.mentalfrostbyte.jello.module.impl.movement.clicktp;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventClick;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import com.mentalfrostbyte.jello.util.system.math.vector.Vector3d;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MinibloxClickTP extends Module {
    private final List<Vector3d> positions = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();
    private final BooleanSetting doFunnyMoves;
    private final NumberSetting<Integer> funnyMovements;
    private double targetX;
    private double targetZ;
    private boolean teleporting;
    private double prevZ;
    private double prevX;
    private int targetY;

    public MinibloxClickTP() {
        super(ModuleCategory.MOVEMENT, "Miniblox", "Click TP for Miniblox");
        this.registerSetting(this.doFunnyMoves = new BooleanSetting(
                "Funny movement packets",
                "In hopes that Miniblox will lagback us where we teleported to",
                true
        ));
        this.registerSetting(this.funnyMovements = new NumberSetting<>(
                "Funny movements",
                "Number of funny movements to make",
                6,
                Integer.class,
                1, 200, 1
        ));
    }

    @Override
    public void onEnable() {
        teleporting = false;
        this.positions.clear();
    }

    @Override
    public void onDisable() {
        teleporting = false;
        this.positions.clear();
    }

    @EventTarget
    public void onClick(EventClick event) {
        if (!this.isEnabled()) return;
        assert mc.player != null;
        if (!(mc.player.isSneaking() || !this.access().getBooleanValueFromSettingName("Sneak"))) return;
        if (event.getButton() != EventClick.Button.RIGHT) return;
        BlockRayTraceResult var4 = BlockUtil.rayTrace(
                mc.player.rotationYaw, mc.player.rotationPitch,
                this.access().getNumberValueBySettingName("Maximum range"));
        BlockPos hit = var4.getPos();

        this.targetX = (double) hit.getX() + 0.5;
        this.targetY = hit.getY() + 1;
        this.targetZ = (double) hit.getZ() + 0.5;
        this.prevX = mc.player.getPosX();
        this.prevZ = mc.player.getPosZ();
        double curY = mc.player.getPosY();
        this.positions.clear();
        this.positions.add(new Vector3d(prevX, curY, prevZ));

        this.positions.add(new Vector3d(targetX, targetY, targetZ));
        mc.player.setPosition(targetX, targetY, targetZ);
        if (this.doFunnyMoves.currentValue)
            for (int i = 0; i < this.funnyMovements.currentValue; i++) {
                Vector3d pos = new Vector3d(targetX + (i * i % 2 == 0 ? 0.2 : -0.2),
                        targetY + (i * 1.12),
                        targetZ + (i * (i % 2 == 0 ? 0.3 : -0.3)));
                this.positions.add(pos);
                Objects.requireNonNull(mc.getConnection()).sendPacket(
                        new CPlayerPacket.PositionPacket(
                                pos.x,
                                pos.y,
                                pos.z,
                                false
                        )
                );
            }
        teleporting = true;
        mc.player.setPosition(targetX, targetY, targetZ);
        mc.player.setMotion(mc.player.getMotion().x, 0.42f, mc.player.getMotion().z);
        this.timer.reset();
        this.timer.start();
        if (this.access().getBooleanValueFromSettingName("Auto Disable")) {
            this.access().toggle();
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (!this.isEnabled()) return;
        if (event.packet instanceof SPlayerPositionLookPacket packet && teleporting) {
            if (packet.getX() == prevX && packet.getZ() == prevZ)
                Client.getInstance().notificationManager.send(new Notification("Miniblox ClickTP", "Please try again"));
            else
                Client.getInstance().notificationManager.send(new Notification("Miniblox ClickTP", "Success!"));
            assert mc.player != null;
            teleporting = false;
            mc.player.setPosition(targetX, targetY, targetZ);
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (this.isEnabled() && !this.positions.isEmpty()) {
            if (this.timer.getElapsedTime() > 4000L) {
                this.timer.stop();
                this.timer.reset();
                this.positions.clear();
            }

            GL11.glPushMatrix();
            GL11.glEnable(2848);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glEnable(32925);
            GL11.glLineWidth(1.4F);
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
            GL11.glBegin(3);

            for (Vector3d var5 : this.positions) {
                GL11.glVertex3d(
                        var5.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                        var5.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                        var5.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
            }

            GL11.glEnd();

            for (Vector3d position : this.positions) {
                BoundingBox bb = getRenderBoundingBox(position);
                RenderUtil.render3DColoredBox(bb,
                        RenderUtil.applyAlpha(ClientColors.PALE_ORANGE.getColor(), 0.2F));
            }

            GL11.glPushMatrix();
            GL11.glTranslated(
                    mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                    mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                    mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
            GL11.glPopMatrix();
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(32925);
            GL11.glDisable(2848);
            GL11.glPopMatrix();
        }
    }

    private static @NotNull BoundingBox getRenderBoundingBox(Vector3d pos) {
        double renderPosX = pos.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX();
        double renderPosZ = pos.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ();
        return new BoundingBox(
                renderPosX - 0.3F,
                pos.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                renderPosZ - 0.3F,
                renderPosX + 0.3F,
                pos.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY() + 1.6F,
                renderPosZ + 0.3F);
    }
}