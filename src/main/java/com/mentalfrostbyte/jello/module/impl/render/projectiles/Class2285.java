package com.mentalfrostbyte.jello.module.impl.render.projectiles;

import net.minecraft.util.math.vector.Vector3i;

import java.util.List;

public enum Class2285 {
    field15121(null),
    field15122(new Vector3i(-1, 0, 0)),
    field15123(new Vector3i(1, 0, 0)),
    field15124(new Vector3i(0, 0, -1)),
    field15125(new Vector3i(0, 0, 1)),
    field15126(new Vector3i(0, 0, 0)),
    field15127(new Vector3i(0, 1, 0)),
    field15128(new Vector3i(0, -1, 0));

    public Vector3i field15129;
    private static final Class2285[] field15130 = new Class2285[]{
            field15121, field15122, field15123, field15124, field15125, field15126, field15127, field15128
    };

    Class2285(Vector3i var3) {
        this.field15129 = var3;
    }

    public static List<Class2285> method9022() {
        return new Class45();
    }

    public static List<Class2285> method9023() {
        return new Class44();
    }
}