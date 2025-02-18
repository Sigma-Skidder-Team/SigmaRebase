package com.mentalfrostbyte.jello.event.impl.player.action;

import com.mentalfrostbyte.jello.event.CancellableEvent;

public class EventInputOptions extends CancellableEvent {
    float strafe, forward;
    boolean sneak;
    boolean jump;
    public static boolean useItem;


    public EventInputOptions(float strafe, float forward,boolean sneak, boolean jump) {
        this.strafe = strafe;
        this.forward = forward;
        this.sneak = sneak;
        this.jump = jump;
    }
    public EventInputOptions(boolean useItem) {
        this.useItem = useItem;
    }
    public boolean isSneaking(){
        return this.sneak;
    }
    public void setSneaking(boolean sneaking){
        this.sneak = sneaking;
    }

    public float getStrafe() {
        return strafe;
    }

    public float getForward() {
        return forward;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public boolean isJumping() {
        return jump;
    }

    public void setJumping(boolean jump) {
        this.jump = jump;
    }



    public static boolean isUseItem() {
        return useItem;
    }


    public void setUseItem(boolean useItem) {
        this.useItem = useItem;
    }
}