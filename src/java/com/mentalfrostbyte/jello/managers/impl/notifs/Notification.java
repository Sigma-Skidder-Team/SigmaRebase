package com.mentalfrostbyte.jello.managers.impl.notifs;

import com.mentalfrostbyte.jello.util.TimerUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import org.newdawn.slick.opengl.Texture;

import java.awt.*;

public class Notification {
    public static final int field43601 = 3;
    public static final int field43602 = 4000;
    public String field43603;
    public String field43604;
    public Texture field43605;
    public int field43606;
    public TimerUtil field43607 = new TimerUtil();
    public Color[] field43608 = new Color[field43601];
    public Color[] field43609 = new Color[field43601];
    public int field43610 = 0;

    public Notification(String var1, String var2, int var3, Texture var4) {
        this.field43603 = var1;
        this.field43604 = var2;
        this.field43605 = var4;
        this.field43606 = var3;
        this.field43607.start();
    }

    public Notification(String var1, String var2, Texture var3) {
        this(var1, var2, field43602, var3);
    }

    public Notification(String var1, String var2, int var3) {
        this(var1, var2, var3, Resources.infoIconPNG);
    }

    public Notification(String var1, String var2) {
        this(var1, var2, field43602);
    }

    @Override
    public boolean equals(Object var1) {
        return var1 instanceof Notification && ((Notification) var1).field43603.equals(this.field43603);
    }
}
