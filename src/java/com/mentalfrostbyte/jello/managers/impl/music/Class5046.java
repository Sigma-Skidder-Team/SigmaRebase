package com.mentalfrostbyte.jello.managers.impl.music;

import java.io.IOException;

public abstract class Class5046 extends Class5041 {
   private long field23033;

   public Class5046(String var1) {
      super(var1);
   }

   @Override
   public void method15262(DataStreamReader var1) throws IOException {
      var1.skipBytes(6L);
      this.field23033 = var1.readBits(2);
   }

   public long method15448() {
      return this.field23033;
   }
}
