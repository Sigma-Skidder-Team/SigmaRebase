package com.mentalfrostbyte.jello.managers.util.waypoints;

import net.minecraft.util.math.ChunkPos;

import java.io.Serializable;

public class Class2531 implements Serializable {
    private static String[] field16733;
    public int field16734;
    public int field16735;

    public Class2531(int var1, int var2) {
        this.field16734 = var1;
        this.field16735 = var2;
    }

    public Class2531(long var1) {
        this.field16734 = (int) var1;
        this.field16735 = (int) (var1 >> 32);
    }

    public Long method10678() {
        return ChunkPos.asLong(this.field16734, this.field16735);
    }

    @Override
    public boolean equals(Object var1) {
        if (this != var1) {
            if (!(var1 instanceof Class2531 var4)) {
                return false;
            } else {
				return this.field16734 == var4.field16734 && this.field16735 == var4.field16735;
            }
        } else {
            return true;
        }
    }
}
