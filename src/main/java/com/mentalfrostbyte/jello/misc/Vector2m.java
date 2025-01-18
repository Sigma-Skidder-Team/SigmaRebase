package com.mentalfrostbyte.jello.misc;

/**
 * Represents a 2-dimensional vector with x and y coordinates.
 */
public class Vector2m {
   /**
    * The x-coordinate of the vector.
    */
   public final double x;

   /**
    * The y-coordinate of the vector.
    */
   public final double y;

   /**
    * Constructs a `Vector2m` with the specified x and y coordinates.
    *
    * @param x the x-coordinate
    * @param y the y-coordinate
    */
   public Vector2m(double x, double y) {
      this.x = x;
      this.y = y;
   }

   /**
    * Constructs a `Vector2m` with the specified x and y coordinates.
    *
    * @param x the x-coordinate
    * @param y the y-coordinate
    */
   public Vector2m(int x, int y) {
      this.x = x;
      this.y = y;
   }

   /**
    * Constructs a `Vector2m` with the specified x and y coordinates.
    *
    * @param x the x-coordinate
    * @param y the y-coordinate
    */
   public Vector2m(float x, float y) {
      this.x = x;
      this.y = y;
   }

   /**
    * Constructs a `Vector2m` by copying another `Vector2m`.
    *
    * @param from the `Vector2m` to copy
    */
   public Vector2m(Vector2m from) {
      this.x = from.x;
      this.y = from.y;
   }

   /**
    * Constructs a `Vector2m` with coordinates (0.0, 0.0).
    */
   public Vector2m() {
      this.x = 0.0;
      this.y = 0.0;
   }

   /**
    * Returns the x-coordinate of this vector.
    *
    * @return the x-coordinate
    */
   public double getX() {
      return this.x;
   }

   /**
    * Returns the rounded value of the x-coordinate.
    *
    * @return the rounded x-coordinate
    */
   public int roundX() {
      return (int)Math.round(this.x);
   }

   /**
    * Returns a new `Vector2m` with the specified x-coordinate and the y-coordinate of this vector.
    *
    * @param x the new x-coordinate
    * @return a new `Vector2m` with the specified x-coordinate
    */
   public Vector2m withX(double x) {
      return new Vector2m(x, this.y);
   }

   /**
    * Returns a new `Vector2m` with the specified x-coordinate and the y-coordinate of this vector.
    *
    * @param x the new x-coordinate
    * @return a new `Vector2m` with the specified x-coordinate
    */
   public Vector2m withX(int x) {
      return new Vector2m(x, this.y);
   }

   /**
    * Returns the y-coordinate of this vector.
    *
    * @return the y-coordinate
    */
   public double getY() {
      return this.y;
   }

   /**
    * Returns the rounded value of the y-coordinate.
    *
    * @return the rounded y-coordinate
    */
   public int roundY() {
      return (int)Math.round(this.y);
   }

   /**
    * Returns a new `Vector2m` with the same x-coordinate and the specified y-coordinate.
    *
    * @param y the new y-coordinate
    * @return a new `Vector2m` with the updated y-coordinate
    */
   public Vector2m withY(double y) {
      return new Vector2m(this.x, y);
   }

   /**
    * Returns a new `Vector2m` with the same x-coordinate and the specified y-coordinate.
    *
    * @param y the new y-coordinate
    * @return a new `Vector2m` with the updated y-coordinate
    */
   public Vector2m withY(int y) {
      return new Vector2m(this.x, y);
   }

   /**
    * Adds the specified `Vector2m` to this vector and returns the result.
    *
    * @param of the vector to add
    * @return the sum of this vector and the specified vector
    */
   public Vector2m add(Vector2m of) {
      return new Vector2m(this.x + of.x, this.y + of.y);
   }

   /**
    * Adds the specified x and y coordinates to this vector and returns the result.
    *
    * @param x the x-coordinate to add
    * @param y the y-coordinate to add
    * @return the sum of this vector and the specified coordinates
    */
   public Vector2m add(double x, double y) {
      return new Vector2m(this.x + x, this.y + y);
   }

   /**
    * Adds the specified x and y coordinates to this vector and returns the result.
    *
    * @param x the x-coordinate to add
    * @param y the y-coordinate to add
    * @return the sum of this vector and the specified coordinates
    */
   public Vector2m add(int x, int y) {
      return new Vector2m(this.x + (double)x, this.y + (double)y);
   }

   /**
    * Adds all the specified vectors to this vector and returns the result.
    *
    * @param vectorsIn the vectors to add
    * @return the sum of this vector and all the specified vectors
    */
   public Vector2m addAll(Vector2m... vectorsIn) {
      double x = this.x;
      double y = this.y;

      for (Vector2m vec : vectorsIn) {
         x += vec.x;
         y += vec.y;
      }

      return new Vector2m(x, y);
   }

   /**
    * Subtracts the specified vector from this vector and returns the result.
    *
    * @param vec the vector to subtract
    * @return the difference between this vector and the specified vector
    */
   public Vector2m subtract(Vector2m vec) {
      return new Vector2m(this.x - vec.x, this.y - vec.y);
   }

   /**
    * Subtracts the specified x and y coordinates from this vector and returns the result.
    *
    * @param x the x-coordinate to subtract
    * @param y the y-coordinate to subtract
    * @return the difference between this vector and the specified coordinates
    */
   public Vector2m subtract(double x, double y) {
      return new Vector2m(this.x - x, this.y - y);
   }

   /**
    * Subtracts the specified x and y coordinates from this vector and returns the result.
    *
    * @param x the x-coordinate to subtract
    * @param y the y-coordinate to subtract
    * @return the difference between this vector and the specified coordinates
    */
   public Vector2m subtract(int x, int y) {
      return new Vector2m(this.x - (double)x, this.y - (double)y);
   }

   /**
    * Subtracts all specified vectors from this vector and returns the result.
    *
    * @param vectorsIn the vectors to subtract
    * @return the difference between this vector and all specified vectors
    */
   public Vector2m subtractAll(Vector2m... vectorsIn) {
      double x = this.x;
      double y = this.y;

      for (Vector2m vec : vectorsIn) {
         x -= vec.x;
         y -= vec.y;
      }

      return new Vector2m(x, y);
   }

   /**
    * Multiplies this vector by the specified vector and returns the result.
    *
    * @param vec the vector to multiply by
    * @return the product of this vector and the specified vector
    */
   public Vector2m multiply(Vector2m vec) {
      return new Vector2m(this.x * vec.x, this.y * vec.y);
   }

   /**
    * Multiplies this vector by the specified x and y coordinates and returns the result.
    *
    * @param x the x-coordinate to multiply by
    * @param y the y-coordinate to multiply by
    * @return the product of this vector and the specified coordinates
    */
   public Vector2m multiply(double x, double y) {
      return new Vector2m(this.x * x, this.y * y);
   }

   /**
    * Multiplies this vector by the specified x and y coordinates and returns the result.
    *
    * @param x the x-coordinate to multiply by
    * @param y the y-coordinate to multiply by
    * @return the product of this vector and the specified coordinates
    */
   public Vector2m multiply(int x, int y) {
      return new Vector2m(this.x * (double)x, this.y * (double)y);
   }

   /**
    * Multiplies this vector by all specified vectors and returns the result.
    *
    * @param vectorsIn the vectors to multiply by
    * @return the product of this vector and all specified vectors
    */
   public Vector2m multiplyAll(Vector2m... vectorsIn) {
      double x = this.x;
      double y = this.y;

      for (Vector2m vec : vectorsIn) {
         x *= vec.x;
         y *= vec.y;
      }

      return new Vector2m(x, y);
   }

   /**
    * Scales this vector by the specified scalar and returns the result.
    *
    * @param scalar the scalar value to scale by
    * @return the scaled vector
    */
   public Vector2m scale(double scalar) {
      return new Vector2m(this.x * scalar, this.y * scalar);
   }

   /**
    * Multiplies this vector by the specified scalar and returns the result.
    *
    * @param scalar the scalar value to multiply by
    * @return the product of this vector and the scalar
    */
   public Vector2m multiply(float scalar) {
      return new Vector2m(this.x * (double)scalar, this.y * (double)scalar);
   }

   /**
    * Multiplies this vector by the specified scalar and returns the result.
    *
    * @param scalar the scalar value to multiply by
    * @return the product of this vector and the scalar
    */
   public Vector2m multiply(int scalar) {
      return new Vector2m(this.x * (double)scalar, this.y * (double)scalar);
   }

   /**
    * Divides this vector by the specified vector and returns the result.
    *
    * @param vectorIn the vector to divide by
    * @return the quotient of this vector and the specified vector
    */
   public Vector2m divide(Vector2m vectorIn) {
      return new Vector2m(this.x / vectorIn.x, this.y / vectorIn.y);
   }

   /**
    * Divides this vector by the specified x and y coordinates and returns the result.
    *
    * @param x the x-coordinate to divide by
    * @param y the y-coordinate to divide by
    * @return the quotient of this vector and the specified coordinates
    */
   public Vector2m divide(double x, double y) {
      return new Vector2m(this.x / x, this.y / y);
   }

   /**
    * Divides this vector by the specified x and y coordinates and returns the result.
    *
    * @param x the x-coordinate to divide by
    * @param y the y-coordinate to divide by
    * @return the quotient of this vector and the specified coordinates
    */
   public Vector2m divide(int x, int y) {
      return new Vector2m(this.x / (double)x, this.y / (double)y);
   }

   /**
    * Divides this vector by the specified scalar and returns the result.
    *
    * @param scalar the scalar value to divide by
    * @return the quotient of this vector and the scalar
    */
   public Vector2m divide(int scalar) {
      return new Vector2m(this.x / (double)scalar, this.y / (double)scalar);
   }

   /**
    * Divides this vector by the specified scalar and returns the result.
    *
    * @param scalar the scalar value to divide by
    * @return the quotient of this vector and the scalar
    */
   public Vector2m divide(double scalar) {
      return new Vector2m(this.x / scalar, this.y / scalar);
   }

   /**
    * Divides this vector by the specified scalar and returns the result.
    *
    * @param scalar the scalar value to divide by
    * @return the quotient of this vector and the scalar
    */
   public Vector2m divide(float scalar) {
      return new Vector2m(this.x / (double)scalar, this.y / (double)scalar);
   }

   /**
    * Returns the magnitude of this vector.
    *
    * @return the magnitude of this vector
    */
   public double magnitude() {
      return Math.sqrt(this.x * this.x + this.y * this.y);
   }

   /**
    * Returns the squared magnitude of this vector.
    *
    * @return the squared magnitude of this vector

   /**
    * Rotates this vector around a specified point.
    *
    * @param angleDegrees The angle of rotation in degrees.
    * @param centerX The x-coordinate of the center of rotation.
    * @param centerZ The z-coordinate of the center of rotation.
    * @param offsetX Additional x-offset to apply after rotation.
    * @param offsetZ Additional z-offset to apply after rotation.
    * @return A new `Vector2m` representing the rotated vector.
    */
   public Vector2m rotateAroundPoint(double angleDegrees, double centerX, double centerZ, double offsetX, double offsetZ) {
      angleDegrees = Math.toRadians(angleDegrees);
      double centeredX = this.x - centerX;
      double centeredZ = this.y - centerZ;
      double rotatedX = centeredX * Math.cos(angleDegrees) - centeredZ * Math.sin(angleDegrees);
      double rotatedY = centeredX * Math.sin(angleDegrees) + centeredZ * Math.cos(angleDegrees);
      return new Vector2m(rotatedX + centerX + offsetX, rotatedY + centerZ + offsetZ);
   }
   /**
    * @param vectorIn the vector to compare to
    * @return if the two vectors are proportional (that is, if they have the same direction)
    */
   public boolean isProportionalTo(Vector2m vectorIn) {
      if (this.x == 0.0 && this.y == 0.0) {
         return true;
      } else {
         double x = vectorIn.x;
         double y = vectorIn.y;
         if (x == 0.0 && y == 0.0) {
            return true;
         } else if (this.x == 0.0 == (x == 0.0)) {
            if (this.y == 0.0 == (y == 0.0)) {
               double var8 = x / this.x;
               if (Double.isNaN(var8)) {
                  double var10 = y / this.y;
                  if (Double.isNaN(var10)) {
                     throw new RuntimeException("This should not happen");
                  } else {
                     return vectorIn.equals(this.scale(var10));
                  }
               } else {
                  return vectorIn.equals(this.scale(var8));
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   /**
    * @return a Class8829 instance created from this vector
    */
   public Class8829 toClass8829() {
      return new Class8829(this);
   }

   /**
    * @return a Vector3m instance created from this vector with the z-coordinate set to 0
    */
   public Vector3m toVec3() {
      return new Vector3m(this.x, 0.0, this.y);
   }

   /**
    * @param y the y-coordinate to use for the Vector3m
    * @return a Vector3m instance created from this vector
    */
   public Vector3m toVec3(double y) {
      return new Vector3m(this.x, y, this.y);
   }

   /**
    * @param to the object to compare to
    * @return if the two objects are equal
    */
   @Override
   public boolean equals(Object to) {
      if (!(to instanceof Vector2m)) {
         return false;
      } else {
         Vector2m other = (Vector2m)to;
         return other.x == this.x && other.y == this.y;
      }
   }

   /**
    * @return the hash code of this vector
    */
   @Override
   public int hashCode() {
      return new Double(this.x).hashCode() >> 13 ^ new Double(this.y).hashCode();
   }

   /**
    * @return a string representing this vector
    */
   @Override
   public String toString() {
      return "(" + this.x + ", " + this.y + ")";
   }

   /**
    * @param a the first vector
    * @param b the second vector
    * @return a vector representing the minimum of the two vectors
    */
   public static Vector2m min(Vector2m a, Vector2m b) {
      return new Vector2m(Math.min(a.x, b.x), Math.min(a.y, b.y));
   }

   /**
    * @param a the first vector
    * @param b the second vector
    * @return a vector representing the maximum of the two vectors
    */
   public static Vector2m max(Vector2m a, Vector2m b) {
      return new Vector2m(Math.max(a.x, b.x), Math.max(a.y, b.y));
   }
}
