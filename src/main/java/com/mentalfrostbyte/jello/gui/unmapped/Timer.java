package com.mentalfrostbyte.jello.gui.unmapped;

public class Timer {
   private long startTime;
   private long elapsedTime;
   private boolean isRunning;

   public Timer() {
      this.startTime = 0L;
      this.elapsedTime = 0L;
      this.isRunning = false;
   }

   public void start() {
      this.isRunning = true;
      this.startTime = System.currentTimeMillis();
   }

   public void stop() {
      this.isRunning = false;
   }

   public void reset() {
      this.elapsedTime = 0L;
      this.startTime = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      if (this.isRunning) {
         this.elapsedTime = this.elapsedTime + (System.currentTimeMillis() - this.startTime);
         this.startTime = System.currentTimeMillis();
      }

      return this.elapsedTime;
   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
