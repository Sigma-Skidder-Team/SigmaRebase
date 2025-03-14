package com.mentalfrostbyte.jello.util.game.world.pathing;

import com.mentalfrostbyte.jello.util.system.math.vector.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PathFinder {
   private final Vector3d field40284;
   private final Vector3d field40285;
   private ArrayList<Vector3d> field40286 = new ArrayList<>();
   private final ArrayList<Path> field40287 = new ArrayList<>();
   private final ArrayList<Path> field40288 = new ArrayList<>();
   private final double field40289 = 9.0;
   private final boolean field40290 = true;
   private static final Minecraft field40291 = Minecraft.getInstance();
   private static final Vector3d[] field40292 = new Vector3d[]{
      new Vector3d(1.0, 0.0, 0.0), new Vector3d(-1.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 1.0), new Vector3d(0.0, 0.0, -1.0)
   };

   public PathFinder(Vector3d var1, Vector3d var2) {
      this.field40284 = var1.method29879(0.0, 0.0, 0.0).method29880();
      this.field40285 = var2.method29879(0.0, 0.0, 0.0).method29880();
   }

   public ArrayList<Vector3d> method32444() {
      return this.field40286;
   }

   public void method32445() {
      this.method32446(1000, 4);
   }

   public void method32446(int var1, int var2) {
      this.field40286.clear();
      this.field40288.clear();
      ArrayList var5 = new ArrayList();
      var5.add(this.field40284);
      this.field40288.add(new Path(this.field40284, null, var5, this.field40284.method29881(this.field40285), 0.0, 0.0));

      label72:
      for (int var6 = 0; var6 < var1; var6++) {
         Collections.sort(this.field40288, new Class3604(this));
         int var7 = 0;
         if (this.field40288.size() == 0) {
            break;
         }

         for (Path var9 : new ArrayList<Path>(this.field40288)) {
            if (++var7 <= var2) {
               this.field40288.remove(var9);
               this.field40287.add(var9);

               for (Vector3d var13 : field40292) {
                  Vector3d var14 = var9.method30354().method29882(var13).method29880();
                  if (method32448(var14, false) && this.method32451(var9, var14, 0.0)) {
                     break label72;
                  }
               }

               Vector3d var15 = var9.method30354().method29879(0.0, 1.0, 0.0).method29880();
               if (method32448(var15, false) && this.method32451(var9, var15, 0.0)) {
                  break label72;
               }

               Vector3d var16 = var9.method30354().method29879(0.0, -1.0, 0.0).method29880();
               if (method32448(var16, false) && this.method32451(var9, var16, 0.0)) {
                  break label72;
               }
               continue;
            }
         }
      }

      if (this.field40290) {
         Collections.sort(this.field40287, new Class3604(this));
         this.field40286 = this.field40287.get(0).method30356();
      }
   }

   public static ArrayList<Vector3d> pathfindToPos(Vector3d location, Vector3d destination) {
      PathFinder instance = new PathFinder(location, destination);
      instance.method32445();
      byte var5 = 5;
      int var6 = 0;
      Vector3d var7 = null;
      Vector3d var8 = null;
      ArrayList<Vector3d> steps = new ArrayList<>();
      List<Vector3d> var10 = instance.method32444();

      for (Vector3d var12 : var10) {
         if (var6 != 0 && var6 != var10.size() - 1) {
            boolean var13 = true;
            if (!(var12.method29881(var8) > (double)(var5 * var5))) {
               double var14 = Math.min(var8.getX(), var12.getX());
               double var16 = Math.min(var8.getY(), var12.getY());
               double var18 = Math.min(var8.getZ(), var12.getZ());
               double var20 = Math.max(var8.getX(), var12.getX());
               double var22 = Math.max(var8.getY(), var12.getY());
               double var24 = Math.max(var8.getZ(), var12.getZ());

               label62:
               for (int var26 = (int)var14; (double)var26 <= var20; var26++) {
                  for (int var27 = (int)var16; (double)var27 <= var22; var27++) {
                     for (int var28 = (int)var18; (double)var28 <= var24; var28++) {
                        if (!method32449(var26, var27, var28, false)) {
                           var13 = false;
                           break label62;
                        }
                     }
                  }
               }
            } else {
               var13 = false;
            }

            if (!var13) {
               steps.add(var7.method29879(0.5, 0.0, 0.5));
               var8 = var7;
            }
         } else {
            if (var7 != null) {
               steps.add(var7.method29879(0.5, 0.0, 0.5));
            }

            steps.add(var12.method29879(0.5, 0.0, 0.5));
            var8 = var12;
         }

         var7 = var12;
         var6++;
      }

      return steps;
   }

   public static boolean method32448(Vector3d var0, boolean var1) {
      return method32449((int)var0.getX(), (int)var0.getY(), (int)var0.getZ(), var1);
   }

   public static boolean method32449(int var0, int var1, int var2, boolean var3) {
      AxisAlignedBB var6 = field40291.player.getRidingEntity() != null ? field40291.player.getRidingEntity().getBoundingBox() : field40291.player.getBoundingBox();
      AxisAlignedBB var7 = new AxisAlignedBB(
         (double)((float)var0 + 0.5F) - var6.getXSize() / 2.0,
			  var1,
         (double)((float)var2 + 0.5F) - var6.getZSize() / 2.0,
         (double)((float)var0 + 0.5F) + var6.getXSize() / 2.0,
         (double)var1 + var6.getYSize(),
         (double)((float)var2 + 0.5F) + var6.getZSize() / 2.0
      );
      return field40291.world.getCollisionShapes(field40291.player, var7).count() == 0L;
   }

   public Path method32450(Vector3d var1) {
      for (Path var5 : this.field40287) {
         if (var5.method30354().getX() == var1.getX()
            && var5.method30354().getY() == var1.getY()
            && var5.method30354().getZ() == var1.getZ()) {
            return var5;
         }
      }

      for (Path var7 : this.field40288) {
         if (var7.method30354().getX() == var1.getX()
            && var7.method30354().getY() == var1.getY()
            && var7.method30354().getZ() == var1.getZ()) {
            return var7;
         }
      }

      return null;
   }

   public boolean method32451(Path var1, Vector3d var2, double var3) {
      Path instance = this.method32450(var2);
      double var8 = var3;
      if (var1 != null) {
         var8 = var3 + var1.method30364();
      }

      if (instance != null) {
         if (instance.method30358() > var3) {
            ArrayList var11 = new ArrayList<Vector3d>(var1.method30356());
            var11.add(var2);
            instance.method30359(var2);
            instance.method30360(var1);
            instance.method30361(var11);
            instance.method30362(var2.method29881(this.field40285));
            instance.method30363(var3);
            instance.method30365(var8);
         }
      } else {
         if (var2.getX() == this.field40285.getX()
               && var2.getY() == this.field40285.getY()
               && var2.getZ() == this.field40285.getZ()
            || this.field40289 != 0.0 && var2.method29881(this.field40285) <= this.field40289) {
            this.field40286.clear();
            this.field40286 = var1.method30356();
            this.field40286.add(var2);
            return true;
         }

         ArrayList var10 = new ArrayList<Vector3d>(var1.method30356());
         var10.add(var2);
         this.field40288.add(new Path(var2, var1, var10, var2.method29881(this.field40285), var3, var8));
      }

      return false;
   }

    public static class Class3604 implements Comparator<Path> {
        public final PathFinder field19567;

        public Class3604(PathFinder var1) {
            this.field19567 = var1;
        }

        public int compare(Path var1, Path var2) {
            return (int)(var1.method30357() + var1.method30364() - (var2.method30357() + var2.method30364()));
        }
    }
}
