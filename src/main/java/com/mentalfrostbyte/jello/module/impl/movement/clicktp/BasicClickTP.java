package com.mentalfrostbyte.jello.module.impl.movement.clicktp;

import com.mentalfrostbyte.jello.event.impl.game.action.EventClick;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.ClickTP;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import com.mentalfrostbyte.jello.util.game.world.BoundingBox;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class BasicClickTP extends Module {
    private final List<com.mentalfrostbyte.jello.util.system.math.vector.Vector3d> positions = new ArrayList<com.mentalfrostbyte.jello.util.system.math.vector.Vector3d>();
    private final TimerUtil timer = new TimerUtil();

    public BasicClickTP() {
        super(ModuleCategory.MOVEMENT, "Basic", "Basic click tp");
    }

    @Override
    public void onEnable() {
        this.positions.clear();
    }

    @Override
    public void onDisable() {
        this.positions.clear();
    }

    @EventTarget
    public void onClick(EventClick event) {
        if (this.isEnabled() && (mc.player.isSneaking() || !this.getParent().getBooleanValueFromSettingName("Sneak"))) {
            if (event.getButton() == EventClick.Button.RIGHT) {
                BlockPos hit = ((ClickTP)this.getParent()).getRotationHit();

                double targetX = (double) hit.getX() + 0.5;
                double targetY = hit.getY() + 1;
                double targetZ = (double) hit.getZ() + 0.5;
                double dX = mc.player.getPosX() - targetX;
                double dZ = mc.player.getPosZ() - targetZ;
                double dY = mc.player.getPosY() - targetY;
                double horMag = dX * dX + dZ * dZ;
                double mag = Math.sqrt(horMag) + Math.abs(dY);
                double maxDist = mag / 8.0;
                double divX = dX / maxDist;
                double divZ = dZ / maxDist;
                double divY = dY / maxDist;
                double curX = mc.player.getPosX();
                double curZ = mc.player.getPosZ();
                double curY = mc.player.getPosY();
                this.positions.clear();
                this.positions.add(new com.mentalfrostbyte.jello.util.system.math.vector.Vector3d(curX, curY, curZ));

                for (int i = 0; (double) i < maxDist - 1.0; i++) {
                    curX -= divX;
                    curZ -= divZ;
                    curY -= divY;
                    double speed = 0.3;
                    AxisAlignedBB var39 = new AxisAlignedBB(curX - speed, curY, curZ - speed, curX + speed,
                            curY + 1.9, curZ + speed);
                    if (mc.world.getCollisionShapes(mc.player, var39).count() == 0L) {
                        mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(curX, curY, curZ, true));
                    }

                    this.positions.add(new com.mentalfrostbyte.jello.util.system.math.vector.Vector3d(curX, curY, curZ));
                }

                this.positions.add(new com.mentalfrostbyte.jello.util.system.math.vector.Vector3d(targetX, targetY, targetZ));
                mc.player.setPosition(targetX, targetY, targetZ);
                this.timer.reset();
                this.timer.start();
                if (this.getParent().getBooleanValueFromSettingName("Auto Disable")) {
                    this.getParent().toggle();
                }
            }
        }
    }

    @EventTarget
    public void method16325(EventRender3D var1) {
        if (this.isEnabled() && this.positions != null && this.positions.size() != 0) {
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

            for (com.mentalfrostbyte.jello.util.system.math.vector.Vector3d var5 : this.positions) {
                GL11.glVertex3d(
                        var5.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                        var5.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                        var5.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
            }

            GL11.glEnd();

            for (com.mentalfrostbyte.jello.util.system.math.vector.Vector3d var12 : this.positions) {
                double var6 = var12.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX();
                double var8 = var12.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ();
                BoundingBox var10 = new BoundingBox(
                        var6 - 0.3F,
                        var12.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                        var8 - 0.3F,
                        var6 + 0.3F,
                        var12.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY() + 1.6F,
                        var8 + 0.3F);
                RenderUtil.render3DColoredBox(var10,
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
}