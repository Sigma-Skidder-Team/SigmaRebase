package com.mentalfrostbyte.jello.managers.impl.music;

import java.io.IOException;

public class Class5083 extends Class4975 {
   private int field23141;

   public Class5083() {
      super("Primary Item Box");
   }

   @Override
   public void method15262(DataStreamReader var1) throws IOException {
      super.method15262(var1);
      this.field23141 = (int)var1.readBits(2);
   }

   public int method15539() {
      return this.field23141;
   }
}
