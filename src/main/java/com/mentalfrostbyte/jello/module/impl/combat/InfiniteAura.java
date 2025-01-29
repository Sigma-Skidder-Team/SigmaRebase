package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.misc.Class8901;
import com.mentalfrostbyte.jello.misc.Vector3d;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.infiniteaura.Sorter;
import com.mentalfrostbyte.jello.module.impl.combat.killaura.TimedEntity;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.EntityUtil;
import com.mentalfrostbyte.jello.util.player.TeamUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InfiniteAura extends Module {
    private final BooleanSetting pathfind;
    private int attacksPerTick;
    private boolean alwaysFalse;
    private float attacksPerTickF;
    private final List<List<Vector3d>> renderPositions;
    private final NumberSetting<Float> attackRange;
    private Thread thread;

    public InfiniteAura() {
        super(ModuleCategory.COMBAT, "InfiniteAura", "Basically infinite aura");
        this.registerSetting(new NumberSetting<>("Range", "Range value", 4.0F, Float.class, 8.0F, 120.0F, 1.0F));
        this.registerSetting(
                attackRange = new NumberSetting<>(
                        "Attack Range",
                        "Range where the player can attack entities in",
                        4.0F,
                        Float.class,
                        8.0F,
                        120.0F,
                        1.0F));
        this.registerSetting(new NumberSetting<>("CPS", "CPS value", 8.0F, Float.class, 1.0F, 20.0F, 1.0F));
        this.registerSetting(new NumberSetting<>("Targets", "Number of targets", 4.0F, Float.class, 1.0F, 10.0F, 1.0F));
        this.registerSetting(
                pathfind = new BooleanSetting(
                        "Pathfind",
                        "Pathfind to targets or teleport directly to targets?",
                        true));
        this.registerSetting(new BooleanSetting("Players", "Hit players", true));
        this.registerSetting(new BooleanSetting("Animals/Monsters", "Hit animals and monsters", false));
        this.registerSetting(new BooleanSetting("Anti-Bot", "Doesn't hit bots", true));
        this.registerSetting(new BooleanSetting("Invisible", "Hit invisible entites", true));
        this.registerSetting(new BooleanSetting("No Swing", "Doesn't swing", false));
        this.renderPositions = new ArrayList<>();
    }

    // $VF: synthetic method
    public static MinecraftClient getMinecraft() {
        return mc;
    }

    @Override
    public void onEnable() {
        this.alwaysFalse = false;
        this.attacksPerTick = (int) (20.0F / this.access().getNumberValueBySettingName("CPS"));
        this.attacksPerTickF = (float) this.attacksPerTick;
    }

    @Override
    public void onDisable() {
        this.renderPositions.clear();
        this.alwaysFalse = false;
        this.thread = null;
    }

    @EventTarget
    @LowerPriority
    public void method16772(EventPlayerTick var1) {
        if (this.isEnabled()) {
            List<TimedEntity> var4 = this.getTimedEntities((float) ((int) this.getNumberValueBySettingName("Range")));
            if (var4 != null && !var4.isEmpty()) {
                if (this.attacksPerTickF < 1.0F) {
                    this.attacksPerTickF = this.attacksPerTickF
                            + 20.0F / this.access().getNumberValueBySettingName("CPS");
                }

                this.attacksPerTick++;
                if (this.attacksPerTick >= (int) this.attacksPerTickF && this.thread == null) {
                    this.attacksPerTick = 0;
                    this.renderPositions.clear();
                    this.attacksPerTickF = this.attacksPerTickF - (float) ((int) this.attacksPerTickF);
                    Entity var5 = mc.player.getRidingEntity() == null ? mc.player : mc.player.getRidingEntity();
                    this.thread = new Thread(() -> {
                        try {
                            int targetCount = 0;

                            for (TimedEntity timedTarget : var4) {
                                Entity target = timedTarget.getEntity();
                                if ((int) this.getNumberValueBySettingName("Targets") < ++targetCount) {
                                    break;
                                }

                                Vector3d destination = new Vector3d(var5.getPosX(), var5.getPosY(), var5.getPosZ());
                                Vector3d location = new Vector3d(target.getPosX(), target.getPosY(), target.getPosZ());
                                ArrayList<Vector3d> path = pathfind.currentValue
                                        ? Class8901.pathfindToPos(location, destination)
                                        : new ArrayList<>(List.of(location));
                                this.renderPositions.add(path);
                                Collections.reverse(path);
                                this.moveToThingy(path, Client.getInstance().moduleManager
                                        .getModuleByClass(Criticals.class).isEnabled());
                                EntityUtil.swing(target, !this.getBooleanValueFromSettingName("No Swing"));
                                Collections.reverse(path);
                                this.moveToThingy(path, false);
                            }
                        } catch (Exception var12) {
                            this.thread = null;
                        }

                        this.thread = null;
                    });
                    this.thread.start();
                }
            } else {
                this.alwaysFalse = false;
                this.renderPositions.clear();
            }
        }
    }

    public void moveToThingy(List<Vector3d> steps, boolean criticalsEnabled) {
        Entity var5 = mc.player.getRidingEntity();
        Vector3d lastPos = null;

        for (Vector3d pos : steps) {
            lastPos = pos;
            if (var5 == null) {
                mc.getConnection()
                        .sendPacket(new CPlayerPacket.PositionPacket(pos.getX(), pos.getY(), pos.getZ(), true));
            } else {
                var5.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                mc.getConnection().sendPacket(new CSteerBoatPacket(false, false));
                mc.getConnection().sendPacket(
                        new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, false));
                mc.getConnection().sendPacket(new CInputPacket(0.0F, 1.0F, false, false));
                BoatEntity var9 = new BoatEntity(mc.world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                var9.rotationYaw = var5.rotationYaw;
                var9.rotationPitch = var5.rotationPitch;
                mc.getConnection().sendPacket(new CMoveVehiclePacket(var9));
            }
        }

        if (criticalsEnabled && lastPos != null) {
            mc.getConnection().sendPacket(
                    new CPlayerPacket.PositionPacket(lastPos.getX(), lastPos.getY() + 1.0E-14, lastPos.getZ(), false));
            mc.getConnection().sendPacket(
                    new CPlayerPacket.PositionPacket(lastPos.getX(), lastPos.getY(), lastPos.getZ(), false));
        }
    }

    @EventTarget
    public void onRender3d(EventRender3D var1) {
        if (this.isEnabled() && this.renderPositions != null && !this.renderPositions.isEmpty()) {
            for (List<Vector3d> list : this.renderPositions) {
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

                for (Vector3d pos : list) {
                    GL11.glVertex3d(
                            pos.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX(),
                            pos.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY(),
                            pos.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
                }

                GL11.glEnd();
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

    public List<TimedEntity> getTimedEntities(float range) {
        ArrayList<TimedEntity> list = new ArrayList<>();

        for (Entity entity : EntityUtil.getEntitesInWorld(__ -> true)) {
            list.add(new TimedEntity(entity));
        }

        Iterator<TimedEntity> iter = list.iterator();

        while (iter.hasNext()) {
            Entity var8 = iter.next().getEntity();
            if (var8 != mc.player) {
                if (!Client.getInstance().friendManager.method26997(var8)) {
                    if (var8 instanceof LivingEntity) {
                        if (((LivingEntity) var8).getHealth() != 0.0F) {
                            if (!(mc.player.getDistance(var8) > range)) {
                                if (mc.player.canAttack((LivingEntity) var8)) {
                                    if (!(var8 instanceof ArmorStandEntity)) {
                                        if (!this.getBooleanValueFromSettingName("Players")
                                                && var8 instanceof PlayerEntity) {
                                            iter.remove();
                                        } else if (var8 instanceof PlayerEntity
                                                && Client.getInstance().combatManager.isTargetABot(var8)) {
                                            iter.remove();
                                        } else if (!this.getBooleanValueFromSettingName("Invisible")
                                                && var8.isInvisible()) {
                                            iter.remove();
                                        } else if (!this.getBooleanValueFromSettingName("Animals/Monsters")
                                                && !(var8 instanceof PlayerEntity)) {
                                            iter.remove();
                                        } else if (mc.player.getRidingEntity() != null
                                                && mc.player.getRidingEntity().equals(var8)) {
                                            iter.remove();
                                        } else if (!var8.isInvulnerable()) {
                                            if (var8 instanceof PlayerEntity
                                                    && TeamUtil.method31662((PlayerEntity) var8)
                                                    && Client.getInstance().moduleManager.getModuleByClass(Teams.class)
                                                            .isEnabled()) {
                                                iter.remove();
                                            }
                                        } else {
                                            iter.remove();
                                        }
                                    } else {
                                        iter.remove();
                                    }
                                } else {
                                    iter.remove();
                                }
                            } else {
                                iter.remove();
                            }
                        } else {
                            iter.remove();
                        }
                    } else {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            } else {
                iter.remove();
            }
        }

        Collections.sort(list, new Sorter(this));
        return list;
    }

    public boolean hasSword() {
        return this.alwaysFalse
                && MinecraftClient.getInstance().player.getHeldItemMainhand() != null
                && MinecraftClient.getInstance().player.getHeldItemMainhand().getItem() instanceof SwordItem;
    }

    @Override
    public boolean isEnabled2() {
        return this.isEnabled() && this.hasSword() && Client.getInstance().playerTracker.isAlive();
    }
}