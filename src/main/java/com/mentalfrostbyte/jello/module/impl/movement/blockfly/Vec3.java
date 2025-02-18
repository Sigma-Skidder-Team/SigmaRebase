package com.mentalfrostbyte.jello.module.impl.movement.blockfly;

public class Vec3 {
    public final double xCoord;
    public final double yCoord;
    public final double zCoord;

    public Vec3(double xCoord, double yCoord, double zCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
    }

    public double distanceTo(Vec3 vec) {
        double d0 = vec.xCoord - this.xCoord;
        double d1 = vec.yCoord - this.yCoord;
        double d2 = vec.zCoord - this.zCoord;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Vec3 vec3 = (Vec3) obj;

        return Double.compare(vec3.xCoord, xCoord) == 0 &&
                Double.compare(vec3.yCoord, yCoord) == 0 &&
                Double.compare(vec3.zCoord, zCoord) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(xCoord);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yCoord);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(zCoord);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Vec3{" +
                "xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                ", zCoord=" + zCoord +
                '}';
    }
}


