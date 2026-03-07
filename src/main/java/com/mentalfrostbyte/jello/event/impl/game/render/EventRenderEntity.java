package com.mentalfrostbyte.jello.event.impl.game.render;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import net.minecraft.entity.LivingEntity;

public class EventRenderEntity extends CancellableEvent {

    private float yawOffset;
    private float headYaw;
    private float yaw;
    private float pitch;
    private final float partialTicks;
    private final LivingEntity entity;
    private boolean render = true;
    private RenderState state;

    public EventRenderEntity(float yawOffset, float headYaw, float yaw, float pitch, float partialTicks, LivingEntity entity) {
        this.yawOffset = yawOffset;
        this.headYaw = headYaw;
        this.yaw = yaw;
        this.pitch = pitch;
        this.partialTicks = partialTicks;
        this.entity = entity;
        this.state = RenderState.PRE;
    }

    public void setState(RenderState state) {
        this.state = state;
    }

    public RenderState getState() {
        return this.state;
    }

    public float getYawOffset() {
        return this.yawOffset;
    }

    public float getHeadYaw() {
        return this.headYaw;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public void setYawOffset(float yawOffset) {
        this.yawOffset = yawOffset;
    }

    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public boolean isRender() {
        return this.render;
    }

    public void setRender(boolean var1) {
        this.render = var1;
    }

    public enum RenderState {
        PRE,
        MID,
        POST
    }
}
