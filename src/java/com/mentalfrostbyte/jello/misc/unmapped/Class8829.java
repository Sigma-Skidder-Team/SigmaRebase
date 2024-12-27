package com.mentalfrostbyte.jello.misc.unmapped;

public class Class8829 extends Vector2d {
   private static String[] field39807;
   public static final Class8829 field39808 = new Class8829(0, 0);
   public static final Class8829 field39809 = new Class8829(1, 0);
   public static final Class8829 field39810 = new Class8829(0, 1);
   public static final Class8829 field39811 = new Class8829(1, 1);

   public Class8829(Vector2d var1) {
      super(var1);
   }

   public Class8829(int var1, int var2) {
      super(var1, var2);
   }

   public Class8829(float var1, float var2) {
      super(var1, var2);
   }

   public Class8829(double var1, double var3) {
      super(var1, var3);
   }

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof Vector2d)) {
         return false;
      } else {
         Vector2d var4 = (Vector2d)var1;
         return (int)var4.field39805 == (int)this.field39805 && (int)var4.field39806 == (int)this.field39806;
      }
   }

   @Override
   public int hashCode() {
      return Integer.valueOf((int)this.field39805).hashCode() >> 13 ^ Integer.valueOf((int)this.field39806).hashCode();
   }

   @Override
   public Class8829 method31921() {
      return this;
   }
}
