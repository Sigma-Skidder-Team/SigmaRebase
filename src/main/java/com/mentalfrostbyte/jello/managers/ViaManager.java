package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderEntity;
import com.mentalfrostbyte.jello.event.impl.game.world.EventBlockCollision;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.player.action.EventStopUseItem;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.gui.base.JelloPortal;
import com.mentalfrostbyte.jello.module.impl.player.Blink;
import com.mentalfrostbyte.jello.module.impl.player.OldHitting;
import com.mentalfrostbyte.jello.module.impl.render.Freecam;
import com.mentalfrostbyte.jello.util.client.MovementHelper;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.ServerUtil;
import com.mojang.datafixers.util.Pair;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.block.Block;
import net.minecraft.block.GrassPathBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.*;

public class ViaManager implements MinecraftUtil {
    public static List<Entity> entities = new ArrayList<>();
    public static int field31494 = 0;
    private UUID field31497;
    public boolean field31498 = false;
    public boolean field31499;

    //public final Class8982 field31495;
    public CTabCompletePacket cTabComplete;

    public void init() {
        EventBus.register(this);
    }

    @EventTarget
    public void onStopUse(EventStopUseItem event) {
        if (JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8)) {
            if (mc.player.getItemInUseMaxCount() <= 1) {
                event.cancelled = true;
            }
        }
    }

    @EventTarget
    public void onKeyPress(EventKeyPress event) {
        if (event.getKey() == mc.gameSettings.keyBindInventory.keyCode.getKeyCode() && JelloPortal.getVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
            mc.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.OPEN_INVENTORY));
        }

        if (JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8) && event.getKey() == 258 && cTabComplete != null && mc.currentScreen instanceof ChatScreen) {
            mc.getConnection().getNetworkManager().sendNoEventPacket(cTabComplete);
            cTabComplete = null;
        }
    }

    @EventTarget
    @HighestPriority
    public void method23344(EventLoadWorld event) {
        field31494 = 0;
        //field31495.method33176();
    }

    @EventTarget
    @HighestPriority
    public void method23345(EventRender2DOffset event) {
        if (mc.player != null && mc.player.getPose() == Pose.SWIMMING && (JelloPortal.getVersion().olderThan(ProtocolVersion.v1_13) || ServerUtil.isHypixel())) {
            mc.player.setPose(Pose.STANDING);
        }
    }

    @EventTarget
    @HighestPriority
    public void onCollision(EventBlockCollision event) {
        if (mc.world != null && mc.player != null) {
            Block block = mc.world.getBlockState(event.getBlockPos()).getBlock();
            if (JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8) && block instanceof GrassPathBlock) {
                VoxelShape voxelShape = VoxelShapes.create(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
                event.setBoxelShape(voxelShape);
            }

            if (JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8)) {
                if (mc.player.boundingBox.maxY - mc.player.boundingBox.minY == 1.5) {
                    mc.player.boundingBox = mc.player.boundingBox.expand(0.0, 0.29999995F, 0.0);
                }

                if (mc.player.eyeHeight == 1.27F) {
                    mc.player.eyeHeight = 1.38F;
                }
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void onTick(EventPlayerTick event) {
        if (isOlderThan_v1_12_2()) {
            int entitiesSize = entities.size();

            for (int i = 0; i < entitiesSize; i++) {
                Entity entity = entities.get(i);

                if (!PlayerUtil.getAllEntitiesInWorld().contains(entity)) {
                    entities.remove(entity);
                    entitiesSize--;
                    i--;
                } else {
                    Iterator<ItemStack> var7 = entity.getHeldEquipment().iterator();
                    boolean var8 = false;

                    while (var7.hasNext()) {
                        ItemStack var9 = var7.next();
                        if (var9.getItem() instanceof SwordItem) {
                            var8 = true;
                        }
                    }

                    if (!var8) {
                        entities.remove(entity);
                        entitiesSize--;
                        i--;
                    }
                }
            }

            try {
                //field31495.method33175();
            } catch (ConcurrentModificationException var10) {
            }

            //field31495.method33177();

            for (int x = -5; x < 5; x++) {
                for (int z = -5; z < 5; z++) {
                    Chunk chunk = mc.world.getChunk(mc.player.chunkCoordX + x, mc.player.chunkCoordZ + z);
                    if (chunk instanceof EmptyChunk) {
                        int var10001 = mc.player.chunkCoordX + x;
                        int var14 = mc.world.getChunkProvider().array.getIndex(var10001, mc.player.chunkCoordZ + z);
                        Chunk var15 = new Chunk(
                                mc.world,
                                new ChunkPos(mc.player.chunkCoordX + x, mc.player.chunkCoordZ + z),
                                chunk.getBiomes()
                        );
                        mc.world.getChunkProvider().array.replace(var14, var15);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof CHeldItemChangePacket) {
            int var4 = ((CHeldItemChangePacket) event.getPacket()).getSlotId();
            if (PlayerInventory.isHotbar(var4)) {
                field31494 = var4;
            }
        }

        if (event.getPacket() instanceof CTabCompletePacket) {
            if (((CTabCompletePacket) event.getPacket()).getCommand().length() == 1) {
                return;
            }

            cTabComplete = (CTabCompletePacket) event.getPacket();
            event.cancelled = true;
        }
    }

    @EventTarget
    @HighestPriority
    public void onReceivePacket(EventReceivePacket event) {
        if (!Client.getInstance().moduleManager.getModuleByClass(OldHitting.class).isEnabled() && !JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8)) {
            if (!entities.isEmpty()) {
                entities.clear();
            }
        } else if (event.getPacket() instanceof SEntityEquipmentPacket packet) {
            for (Pair<EquipmentSlotType, ItemStack> pair : packet.func_241790_c_()) {
                if (pair.getFirst() == EquipmentSlotType.OFFHAND && pair.getSecond() != null && (Client.getInstance().moduleManager.getModuleByClass(OldHitting.class).isEnabled() || JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8))) {
                    if (!(pair.getSecond().getItem() instanceof ShieldItem)) {
                        Entity entity = mc.world.getEntityByID(packet.getEntityID());
                        if (entities.contains(entity)) {
                            entities.remove(entity);
                        }
                    } else {
                        Entity entity = mc.world.getEntityByID(packet.getEntityID());
                        if (!entities.contains(entity) && !ServerUtil.isMineplex()) {
                            entities.add(entity);
                        }

                        event.cancelled = true;
                    }
                }
            }
        }

        if (isOlderThan_v1_12_2()) {
            //Class8920.method32597(event, field31495);
            if (!(event.getPacket() instanceof SHeldItemChangePacket)) {
                if (event.getPacket() instanceof SUnloadChunkPacket && ServerUtil.isMinemen()) {
                    event.cancelled = true;
                } else if (!(event.getPacket() instanceof SAnimateHandPacket packet)) {
                    if (!(event.getPacket() instanceof SUpdateViewDistancePacket)) {
                        if (event.getPacket() instanceof SUpdateBossInfoPacket packet) {
                            if (packet.getOperation() != SUpdateBossInfoPacket.Operation.ADD) {
                                if (field31497 != null && packet.getOperation() == SUpdateBossInfoPacket.Operation.REMOVE) {
                                    if (field31497.compareTo(packet.getUniqueId()) != 0) {
                                        event.cancelled = true;
                                    } else {
                                        field31497 = null;
                                    }
                                } else if (field31497 != null && field31497.compareTo(packet.getUniqueId()) != 0) {
                                    event.cancelled = true;
                                }
                            } else if (field31497 != null) {
                                event.cancelled = true;
                            } else {
                                field31497 = packet.getUniqueId();
                            }
                        }
                    }
                } else {
                    Entity var13 = mc.world.getEntityByID(packet.getEntityID());
                    if (var13 != null && packet.getAnimationType() == 3 && JelloPortal.getVersion().equalTo(ProtocolVersion.v1_8)) {
                        event.cancelled = true;
                    }
                }
            } else {
                int var12 = ((SHeldItemChangePacket) event.getPacket()).getHeldItemHotbarIndex();
                if (PlayerInventory.isHotbar(var12)) {
                    field31494 = var12;
                }
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void onMove(EventMove event) {
        if (JelloPortal.getVersion().olderThan(ProtocolVersion.v1_13) || ServerUtil.isHypixel()) {
            if (mc.player.isInWater()) {
                field31498 = true;
                double var4 = mc.player.getPosY();
                float var6 = MovementHelper.method34128();
                float var7 = 0.02F;
                float var8 = (float) EnchantmentHelper.getDepthStriderModifier(mc.player);
                if (var8 > 3.0F) {
                    var8 = 3.0F;
                }

                if (!mc.player.onGround) {
                    var8 *= 0.5F;
                }

                if (var8 > 0.0F) {
                    var6 += (0.54600006F - var6) * var8 / 3.0F;
                    var7 += (mc.player.getAIMoveSpeed() - var7) * var8 / 3.0F;
                }

                if (!mc.gameSettings.keyBindSprint.isKeyDown()) {
                    if (mc.player.moveStrafing == 0.0F && mc.player.moveForward == 0.0F) {
                        mc.player.setSprinting(false);
                    }
                } else {
                    mc.player.setSprinting(true);
                }

                var7 *= !mc.player.isSprinting() ? 1.0F : (!mc.player.onGround ? 1.3F : 1.5F);
                MovementHelper.applyMotion(mc.player.moveStrafing, mc.player.moveVertical, mc.player.moveForward, var7);
                MovementHelper.method34126(MovementHelper.x, MovementHelper.y, MovementHelper.z);
                MovementHelper.x *= (double) var6;
                MovementHelper.y *= 0.8F;
                MovementHelper.z *= (double) var6;
                if (!mc.player.hasNoGravity()) {
                    MovementHelper.y -= 0.02;
                }

                if (mc.player.collidedHorizontally && mc.player.isOffsetPositionInLiquid(MovementHelper.x, MovementHelper.y + 0.6F - mc.player.getPosY() + var4, MovementHelper.z)) {
                    MovementHelper.y = 0.3F;
                }

                if (mc.player.isJumping) {
                    MovementHelper.jump();
                }

                event.setX(MovementHelper.x);
                event.setY(MovementHelper.y);
                event.setZ(MovementHelper.z);
            } else {
                MovementHelper.y = mc.player.getMotion().y;
                if (field31498 && MovementHelper.isPlayerInWater()) {
                    MovementHelper.y = 0.2F;
                    mc.player.setMotion(mc.player.getMotion().x, MovementHelper.y, mc.player.getMotion().z);
                }

                MovementHelper.x = mc.player.getMotion().x;
                MovementHelper.z = mc.player.getMotion().z;
                field31498 = false;
            }
        }
    }

    public boolean isOlderThan_v1_12_2() {
        return JelloPortal.getVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2);
    }

    @EventTarget
    @HighestPriority
    public void onRenderEntity(EventRenderEntity event) {
        if (event.getEntity() == mc.player || event.getEntity() == Freecam.player || event.getEntity() == Blink.clientPlayerEntity) {
            if (event.getPartialTicks() != 1.0F) {
                if (EventUpdateWalkingPlayer.prevPitch - mc.player.rotationYawHead == 0.0F) {
                    if (field31499) {
                        event.setYawOffset(MathHelper.interpolateAngle(event.getPartialTicks(), EventUpdateWalkingPlayer.postPitch, event.getEntity().renderYawOffset));
                        event.setHeadYaw(MathHelper.interpolateAngle(event.getPartialTicks(), EventUpdateWalkingPlayer.postPitch, event.getEntity().rotationYawHead));
                        event.setPitch(MathHelper.lerp(event.getPartialTicks(), EventUpdateWalkingPlayer.postYaw, event.getEntity().rotationPitch));
                        event.setYaw(event.getHeadYaw() - event.getYawOffset());
                        event.getEntity().prevRotationPitch = EventUpdateWalkingPlayer.postYaw;
                        event.getEntity().prevRotationYaw = EventUpdateWalkingPlayer.postPitch;
                        event.getEntity().prevRotationYawHead = EventUpdateWalkingPlayer.postPitch;
                        event.getEntity().prevRenderYawOffset = EventUpdateWalkingPlayer.postPitch;
                        field31499 = !field31499;
                    }
                } else {
                    event.setYawOffset(MathHelper.interpolateAngle(event.getPartialTicks(), EventUpdateWalkingPlayer.postPitch, EventUpdateWalkingPlayer.prevPitch));
                    event.setHeadYaw(MathHelper.interpolateAngle(event.getPartialTicks(), EventUpdateWalkingPlayer.postPitch, EventUpdateWalkingPlayer.prevPitch));
                    event.setPitch(MathHelper.lerp(event.getPartialTicks(), EventUpdateWalkingPlayer.postYaw, EventUpdateWalkingPlayer.prevYaw));
                    event.setYaw(event.getHeadYaw() - event.getYawOffset());
                    field31499 = true;
                }
            }
        }
    }

}
