package com.mentalfrostbyte.jello.util.render.unmapped;

import org.newdawn.slick.util.ResourceLoader;
import org.newdawn.slick.util.Identifier;

import java.io.InputStream;
import java.net.URL;

public class Class7903 implements Identifier {
   private static String[] field33859;

   @Override
   public URL getResource(String ref) {
      String var4 = ref.replace('\\', '/');
      return ResourceLoader.class.getClassLoader().getResource(var4);
   }

   @Override
   public InputStream getResourceAsStream(String ref) {
      String var4 = ref.replace('\\', '/');
      return ResourceLoader.class.getClassLoader().getResourceAsStream(var4);
   }
}
