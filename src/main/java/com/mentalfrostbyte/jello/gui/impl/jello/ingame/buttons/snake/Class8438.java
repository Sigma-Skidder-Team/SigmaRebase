package com.mentalfrostbyte.jello.gui.impl.jello.ingame.buttons.snake;

import com.mentalfrostbyte.jello.util.system.render.ScreenDimension;

import java.util.ArrayList;
import java.util.List;

public class Class8438 {
   private static String[] field36151;
   private final List<ScreenDimension> field36152 = new ArrayList<ScreenDimension>();
   private Class2097 field36153 = Class2097.field13663;
   private boolean field36154 = false;
   private boolean field36155 = false;
   private boolean field36156 = false;

   public Class8438(ScreenDimension var1) {
      this.field36152.add(var1.add(this.method29650().add(this.method29650())));
      this.field36152.add(var1.add(this.method29650()));
      this.field36152.add(var1);
   }

   private ScreenDimension method29650() {
      return Class8437.method29649(this.field36153);
   }

   public void method29651() {
      ScreenDimension var3 = this.field36152.get(0).add(this.method29650());
      this.field36156 = this.field36152.contains(var3);
      this.field36152.add(0, var3);
      if (!this.field36155) {
         this.field36152.remove(this.field36152.size() - 1);
      }

      this.field36154 = false;
      this.field36155 = false;
   }

   public void method29652() {
      this.field36155 = true;
   }

   public void method29653(Class2097 var1) {
      ScreenDimension var4 = Class8437.method29649(var1).add(Class8437.method29649(this.field36153));
      if ((var4.width != 0 || var4.height != 0) && var1 != this.field36153 && !this.field36154) {
         this.field36153 = var1;
         this.field36154 = true;
      }
   }

   public boolean method29654() {
      return this.field36156;
   }

   public List<ScreenDimension> method29655() {
      return this.field36152;
   }

   public boolean method29656(ScreenDimension var1) {
      return var1 == null || this.field36152.contains(var1);
   }
}
