package com.mentalfrostbyte.jello.gui.unmapped;

public class AnimationManager {
   private float currentValue;
   private final RandomIntGenerator random = new RandomIntGenerator();
   private final Timer fieldTimer = new Timer();
   private long nextInterval;
   private boolean isAnimating = false;
   private float targetValue = -1.0F;

   public AnimationManager() {
      this.fieldTimer.start();
      this.nextInterval = (long)this.random.nextInt(8000, 10000);
      this.currentValue = this.random.nextFloat();
   }

   public void update() {
      if (this.fieldTimer.getElapsedTime() > this.nextInterval) {
         this.nextInterval = (long)this.random.nextInt(8000, 10000);
         this.isAnimating = true;
         this.targetValue = this.random.nextFloat() + 0.75F;
         boolean shouldInvert = this.random.nextBoolean();
         if (shouldInvert) {
            this.targetValue *= -1.0F;
         }

         this.fieldTimer.reset();
      }

      if (this.isAnimating && this.targetValue != -1.0F && this.fieldTimer.getElapsedTime() % 10L == 0L) {
         if (!(this.targetValue > this.currentValue)) {
            this.currentValue -= 0.02F;
            if (this.targetValue > this.currentValue) {
               this.currentValue = this.targetValue;
               this.isAnimating = false;
               this.targetValue = -1.0F;
            }
         } else {
            this.currentValue += 0.02F;
            if (this.targetValue < this.currentValue) {
               this.currentValue = this.targetValue;
               this.isAnimating = false;
               this.targetValue = -1.0F;
            }
         }
      }
   }

   public float getCurrentValue() {
      return this.currentValue;
   }
}
