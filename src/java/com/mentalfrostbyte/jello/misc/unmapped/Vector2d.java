package com.mentalfrostbyte.jello.misc.unmapped;

import com.mentalfrostbyte.jello.misc.Vector3m;

public class Vector2d {
   public final double field39805;
   public final double field39806;

   public Vector2d(double var1, double var3) {
      this.field39805 = var1;
      this.field39806 = var3;
   }

   public Vector2d(int var1, int var2) {
      this.field39805 = (double)var1;
      this.field39806 = (double)var2;
   }

   public Vector2d(float var1, float var2) {
      this.field39805 = (double)var1;
      this.field39806 = (double)var2;
   }

   public Vector2d(Vector2d var1) {
      this.field39805 = var1.field39805;
      this.field39806 = var1.field39806;
   }

   public Vector2d() {
      this.field39805 = 0.0;
      this.field39806 = 0.0;
   }

   public double method31878() {
      return this.field39805;
   }

   public int method31879() {
      return (int)Math.round(this.field39805);
   }

   public Vector2d method31880(double var1) {
      return new Vector2d(var1, this.field39806);
   }

   public Vector2d method31881(int var1) {
      return new Vector2d((double)var1, this.field39806);
   }

   public double method31882() {
      return this.field39806;
   }

   public int method31883() {
      return (int)Math.round(this.field39806);
   }

   public Vector2d method31884(double var1) {
      return new Vector2d(this.field39805, var1);
   }

   public Vector2d method31885(int var1) {
      return new Vector2d(this.field39805, (double)var1);
   }

   public Vector2d method31886(Vector2d var1) {
      return new Vector2d(this.field39805 + var1.field39805, this.field39806 + var1.field39806);
   }

   public Vector2d method31887(double var1, double var3) {
      return new Vector2d(this.field39805 + var1, this.field39806 + var3);
   }

   public Vector2d method31888(int var1, int var2) {
      return new Vector2d(this.field39805 + (double)var1, this.field39806 + (double)var2);
   }

   public Vector2d method31889(Vector2d... var1) {
      double var4 = this.field39805;
      double var6 = this.field39806;

      for (Vector2d var11 : var1) {
         var4 += var11.field39805;
         var6 += var11.field39806;
      }

      return new Vector2d(var4, var6);
   }

   public Vector2d method31890(Vector2d var1) {
      return new Vector2d(this.field39805 - var1.field39805, this.field39806 - var1.field39806);
   }

   public Vector2d method31891(double var1, double var3) {
      return new Vector2d(this.field39805 - var1, this.field39806 - var3);
   }

   public Vector2d method31892(int var1, int var2) {
      return new Vector2d(this.field39805 - (double)var1, this.field39806 - (double)var2);
   }

   public Vector2d method31893(Vector2d... var1) {
      double var4 = this.field39805;
      double var6 = this.field39806;

      for (Vector2d var11 : var1) {
         var4 -= var11.field39805;
         var6 -= var11.field39806;
      }

      return new Vector2d(var4, var6);
   }

   public Vector2d method31894(Vector2d var1) {
      return new Vector2d(this.field39805 * var1.field39805, this.field39806 * var1.field39806);
   }

   public Vector2d method31895(double var1, double var3) {
      return new Vector2d(this.field39805 * var1, this.field39806 * var3);
   }

   public Vector2d method31896(int var1, int var2) {
      return new Vector2d(this.field39805 * (double)var1, this.field39806 * (double)var2);
   }

   public Vector2d method31897(Vector2d... var1) {
      double var4 = this.field39805;
      double var6 = this.field39806;

      for (Vector2d var11 : var1) {
         var4 *= var11.field39805;
         var6 *= var11.field39806;
      }

      return new Vector2d(var4, var6);
   }

   public Vector2d method31898(double var1) {
      return new Vector2d(this.field39805 * var1, this.field39806 * var1);
   }

   public Vector2d method31899(float var1) {
      return new Vector2d(this.field39805 * (double)var1, this.field39806 * (double)var1);
   }

   public Vector2d method31900(int var1) {
      return new Vector2d(this.field39805 * (double)var1, this.field39806 * (double)var1);
   }

   public Vector2d method31901(Vector2d var1) {
      return new Vector2d(this.field39805 / var1.field39805, this.field39806 / var1.field39806);
   }

   public Vector2d method31902(double var1, double var3) {
      return new Vector2d(this.field39805 / var1, this.field39806 / var3);
   }

   public Vector2d method31903(int var1, int var2) {
      return new Vector2d(this.field39805 / (double)var1, this.field39806 / (double)var2);
   }

   public Vector2d method31904(int var1) {
      return new Vector2d(this.field39805 / (double)var1, this.field39806 / (double)var1);
   }

   public Vector2d method31905(double var1) {
      return new Vector2d(this.field39805 / var1, this.field39806 / var1);
   }

   public Vector2d method31906(float var1) {
      return new Vector2d(this.field39805 / (double)var1, this.field39806 / (double)var1);
   }

   public double method31907() {
      return Math.sqrt(this.field39805 * this.field39805 + this.field39806 * this.field39806);
   }

   public double method31908() {
      return this.field39805 * this.field39805 + this.field39806 * this.field39806;
   }

   public double method31909(Vector2d var1) {
      return Math.sqrt(Math.pow(var1.field39805 - this.field39805, 2.0) + Math.pow(var1.field39806 - this.field39806, 2.0));
   }

   public double method31910(Vector2d var1) {
      return Math.pow(var1.field39805 - this.field39805, 2.0) + Math.pow(var1.field39806 - this.field39806, 2.0);
   }

   public Vector2d method31911() {
      return this.method31905(this.method31907());
   }

   public double method31912(Vector2d var1) {
      return this.field39805 * var1.field39805 + this.field39806 * var1.field39806;
   }

   public boolean method31913(Vector2d var1, Vector2d var2) {
      return this.field39805 >= var1.field39805
         && this.field39805 <= var2.field39805
         && this.field39806 >= var1.field39806
         && this.field39806 <= var2.field39806;
   }

   public boolean method31914(Vector2d var1, Vector2d var2) {
      return this.method31879() >= var1.method31879()
         && this.method31879() <= var2.method31879()
         && this.method31883() >= var1.method31883()
         && this.method31883() <= var2.method31883();
   }

   public Vector2d method31915() {
      return new Vector2d(Math.floor(this.field39805), Math.floor(this.field39806));
   }

   public Vector2d method31916() {
      return new Vector2d(Math.ceil(this.field39805), Math.ceil(this.field39806));
   }

   public Vector2d method31917() {
      return new Vector2d(Math.floor(this.field39805 + 0.5), Math.floor(this.field39806 + 0.5));
   }

   public Vector2d method31918() {
      return new Vector2d(Math.abs(this.field39805), Math.abs(this.field39806));
   }

   public Vector2d method31919(double var1, double var3, double var5, double var7, double var9) {
      var1 = Math.toRadians(var1);
      double var13 = this.field39805 - var3;
      double var15 = this.field39806 - var5;
      double var17 = var13 * Math.cos(var1) - var15 * Math.sin(var1);
      double var19 = var13 * Math.sin(var1) + var15 * Math.cos(var1);
      return new Vector2d(var17 + var3 + var7, var19 + var5 + var9);
   }

   public boolean method31920(Vector2d var1) {
      if (this.field39805 == 0.0 && this.field39806 == 0.0) {
         return true;
      } else {
         double var4 = var1.field39805;
         double var6 = var1.field39806;
         if (var4 == 0.0 && var6 == 0.0) {
            return true;
         } else if (this.field39805 == 0.0 == (var4 == 0.0)) {
            if (this.field39806 == 0.0 == (var6 == 0.0)) {
               double var8 = var4 / this.field39805;
               if (Double.isNaN(var8)) {
                  double var10 = var6 / this.field39806;
                  if (Double.isNaN(var10)) {
                     throw new RuntimeException("This should not happen");
                  } else {
                     return var1.equals(this.method31898(var10));
                  }
               } else {
                  return var1.equals(this.method31898(var8));
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public Class8829 method31921() {
      return new Class8829(this);
   }

   public Vector3m method31922() {
      return new Vector3m(this.field39805, 0.0, this.field39806);
   }

   public Vector3m method31923(double var1) {
      return new Vector3m(this.field39805, var1, this.field39806);
   }

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof Vector2d)) {
         return false;
      } else {
         Vector2d var4 = (Vector2d)var1;
         return var4.field39805 == this.field39805 && var4.field39806 == this.field39806;
      }
   }

   @Override
   public int hashCode() {
      return new Double(this.field39805).hashCode() >> 13 ^ new Double(this.field39806).hashCode();
   }

   @Override
   public String toString() {
      return "(" + this.field39805 + ", " + this.field39806 + ")";
   }

   public static Vector2d method31924(Vector2d var0, Vector2d var1) {
      return new Vector2d(Math.min(var0.field39805, var1.field39805), Math.min(var0.field39806, var1.field39806));
   }

   public static Vector2d method31925(Vector2d var0, Vector2d var1) {
      return new Vector2d(Math.max(var0.field39805, var1.field39805), Math.max(var0.field39806, var1.field39806));
   }
}
