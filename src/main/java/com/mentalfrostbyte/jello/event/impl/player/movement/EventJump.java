package com.mentalfrostbyte.jello.event.impl.player.movement;

import com.mentalfrostbyte.jello.event.CancellableEvent;
import com.mentalfrostbyte.jello.util.player.MovementUtil;
import net.minecraft.util.math.Vec3d;

<<<<<<<< HEAD:src/main/java/com/mentalfrostbyte/jello/event/impl/JumpEvent.java
public class JumpEvent extends CancellableEvent {
    public Vec3d vector;
    public boolean modified;

    public JumpEvent(Vec3d vector) {
========
public class EventJump extends CancellableEvent {
    public Vector3d vector;
    public boolean modified;

    public EventJump(Vector3d vector) {
>>>>>>>> d57481e640c641fa8f5a34328b34f3c523118347:src/main/java/com/mentalfrostbyte/jello/event/impl/player/movement/EventJump.java
        this.vector = vector;
    }

    public boolean isModified() {
        return this.modified;
    }

    public Vec3d getVector() {
        return this.vector;
    }

    public void setY(double y) {
        this.vector.y = y;
    }

    public void setStrafeSpeed(double speed) {
        float[] var3 = MovementUtil.lenientStrafe();
        float forward = var3[1];
        float strafe = var3[2];
        float yaw = var3[0];
        if (forward == 0.0F && strafe == 0.0F) {
            this.vector.x = 0.0;
            this.vector.z = 0.0;
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + strafe * sin) * speed;
        double z = (forward * sin - strafe * cos) * speed;
        this.vector.x = x;
        this.vector.z = z;
        this.modified = true;
    }

    public void setVector(Vec3d vector) {
        this.vector = vector;
        this.modified = true;
    }
}
