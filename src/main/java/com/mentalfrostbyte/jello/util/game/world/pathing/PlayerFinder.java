package com.mentalfrostbyte.jello.util.game.world.pathing;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mentalfrostbyte.jello.util.game.world.EntityUtil;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import com.mentalfrostbyte.jello.util.system.math.vector.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.List;

public class PlayerFinder {
    public TimerUtil timer;
    public int field45878;
    public Entity entity;
    public Minecraft mc = Minecraft.getInstance();

    public PlayerFinder() {
        this.timer = new TimerUtil();
        EventBus.register(this);
    }

    @EventTarget
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (this.entity != null) {
            if (this.field45878 != 1) {
                if (this.field45878 == 2) {
                    boolean var4 = ServerUtil.isHypixel();
                    if (var4) {
                        PlayerAbilities abilities = new PlayerAbilities();
                        abilities.isFlying = true;
                        Entity entity1 = null;

                        for (Entity entity2 : EntityUtil.getEntitesInWorld(__ -> true)) {
                            if (entity2 instanceof PlayerEntity
                                    && entity2 != this.mc.player
                                    && (entity1 == null || entity1.getDistance(this.mc.player) > entity2.getDistance(this.mc.player))) {
                                entity1 = entity2;
                            }
                        }

                        this.mc.getConnection().sendPacket(new CConfirmTransactionPacket(0, (short) -1, false));
                        this.mc.getConnection().sendPacket(new CPlayerAbilitiesPacket(abilities));
                        if (entity1 != null) {
                            this.mc.getConnection().sendPacket(new CSpectatePacket(entity1.getUniqueID()));
                        }

                        this.mc.getConnection().sendPacket(new CInputPacket(0.98F, 0.98F, false, false));
                    }

                    Thread thread = new Thread(
                            () -> {
                                try {
                                    Vector3d entityVec = new Vector3d(this.entity.getPosX(), this.entity.getPosY(), this.entity.getPosZ());
                                    Vector3d playerVec = new Vector3d(
                                            this.mc.player.getPosX(), this.mc.player.getPosY(), this.mc.player.getPosZ()
                                    );
                                    List<Vector3d> vectors = PathFinder.pathfindToPos(playerVec, entityVec);
                                    Client.getInstance().notificationManager.send(new Notification("Teleport", "Successfully teleported !"));
                                    Entity ridingEntity = this.mc.player.getRidingEntity();

                                    for (Vector3d vec : vectors) {
                                        if (ridingEntity != null) {
                                            ridingEntity.setPosition(vec.getX() + 0.5, vec.getY(), vec.getZ() + 0.5);
                                            this.mc.getConnection().sendPacket(new CSteerBoatPacket(false, false));
                                            this.mc
                                                    .getConnection()
                                                    .sendPacket(new CPlayerPacket.RotationPacket(this.mc.player.rotationYaw, this.mc.player.rotationPitch, false));
                                            this.mc.getConnection().sendPacket(new CInputPacket(0.0F, 1.0F, false, false));
                                            BoatEntity var10 = new BoatEntity(
                                                    this.mc.world, vec.getX() + 0.5, vec.getY(), vec.getZ() + 0.5
                                            );
                                            var10.rotationYaw = ridingEntity.rotationYaw;
                                            var10.rotationPitch = ridingEntity.rotationPitch;
                                            this.mc.getConnection().sendPacket(new CMoveVehiclePacket(var10));
                                        } else if (var4) {
                                            this.mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(vec.getX(), vec.getY(), vec.getZ(), false));
                                        } else {
                                            this.mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(vec.getX(), vec.getY(), vec.getZ(), true));
                                        }
                                    }

                                    if (var4) {
                                        this.mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(entityVec.x, entityVec.y, entityVec.z, false));
                                    }

                                    this.mc.player.setPosition(entityVec.x, entityVec.y, entityVec.z);
                                    this.entity = null;
                                    if (var4) {
                                        PlayerAbilities var12 = new PlayerAbilities();
                                        var12.isFlying = false;
                                        this.mc.getConnection().sendPacket(new CPlayerAbilitiesPacket(var12));
                                    }
                                } catch (Exception var11) {
                                    var11.printStackTrace();
                                }
                            }
                    );
                    thread.start();
                    this.field45878 = 0;
                }
            } else {
                event.cancelled = true;
            }
        }
    }

    @EventTarget
    public void onMovement(EventMove event) {
        if (this.field45878 == 1) {
            event.setX(0.0);
            event.setY(0.0);
            event.setZ(0.0);
            if (this.entity == null || !this.entity.isAlive() || !EntityUtil.getEntitesInWorld(__ -> true).contains(this.entity)) {
                Client.getInstance().notificationManager.send(new Notification("Teleport", "Target lost"));
                this.field45878 = 0;
                this.entity = null;
            } else if (!this.mc.player.isSneaking()) {
                double posY = this.entity.getPosY() - this.entity.lastTickPosY;
                if (posY < -2.0 && PlayerUtil.isEntityAboveGround(this.entity) && this.entity.getPosY() - this.mc.player.getPosY() < -10.0) {
                    this.field45878 = 0;
                    this.entity = null;
                    Client.getInstance().notificationManager.send(new Notification("Teleport", "Target seems to be falling in void"));
                }
            } else {
                this.field45878 = 0;
                this.entity = null;
                Client.getInstance().notificationManager.send(new Notification("Teleport", "Teleport canceled"));
            }

            if (this.timer.getElapsedTime() > 7000L) {
                this.field45878 = 0;
                this.entity = null;
                Client.getInstance().notificationManager.send(new Notification("Teleport", "Failed teleport !"));
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.field45878 == 1) {
            if (event.packet instanceof SPlayerPositionLookPacket) {
                this.field45878 = 2;
            }
        }
    }
}
