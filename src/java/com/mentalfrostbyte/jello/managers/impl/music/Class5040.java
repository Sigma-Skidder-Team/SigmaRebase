package com.mentalfrostbyte.jello.managers.impl.music;

import java.io.IOException;

public class Class5040 extends Class4975 {
   private double field23021;

   public Class5040() {
      super("Sound Media Header Box");
   }

   @Override
   public void method15262(DataStreamReader var1) throws IOException {
      super.method15262(var1);
      this.field23021 = var1.method31869(8, 8);
      var1.skipBytes(2L);
   }

   public double method15428() {
      return this.field23021;
   }
}
