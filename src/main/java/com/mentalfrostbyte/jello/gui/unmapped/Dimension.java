package com.mentalfrostbyte.jello.gui.unmapped;

public class Dimension {
   public int width;
   public int height;

   public Dimension(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public int getWidth() {
      return this.width;
   }

   public Dimension add(Dimension from) {
      return new Dimension(this.width + from.width, this.height + from.height);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Dimension other = (Dimension)obj;
         return this.width == other.width && this.height == other.height;
      } else {
         return false;
      }
   }
}
