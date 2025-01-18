package com.mentalfrostbyte.jello.managers.impl.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.util.Identifier;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class CustomSoundPlayer extends LocatableSound {

   public CustomSoundPlayer(SoundEvent var1, SoundCategory var2, float var3, float var4, BlockPos var5) {
      this(var1, var2, var3, var4, (double) var5.getX() + 0.5, (double) var5.getY() + 0.5, (double) var5.getZ() + 0.5);
   }

   public static CustomSoundPlayer playSoundWithCustomPitch(SoundEvent var0, float var1) {
      return method19293(var0, var1, 0.25F);
   }

   public static CustomSoundPlayer method19293(SoundEvent var0, float var1, float var2) {
      return new CustomSoundPlayer(var0.getName(), SoundCategory.MASTER, var2, var1, false, 0,
            ISound.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
   }

   public static CustomSoundPlayer method19294(SoundEvent var0) {
      return new CustomSoundPlayer(var0.getName(), SoundCategory.MUSIC, 1.0F, 1.0F, false, 0,
            ISound.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
   }

   public static CustomSoundPlayer method19295(SoundEvent var0, double var1, double var3, double var5) {
      return new CustomSoundPlayer(var0, SoundCategory.RECORDS, 4.0F, 1.0F, false, 0, ISound.AttenuationType.LINEAR,
            var1, var3, var5);
   }

   public static CustomSoundPlayer method19296(SoundEvent var0, float var1, float var2) {
      return new CustomSoundPlayer(var0.getName(), SoundCategory.AMBIENT, var2, var1, false, 0,
            ISound.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
   }

   public static CustomSoundPlayer method19297(SoundEvent var0) {
      return method19296(var0, 1.0F, 1.0F);
   }

   public static CustomSoundPlayer method19298(SoundEvent var0, double var1, double var3, double var5) {
      return new CustomSoundPlayer(var0, SoundCategory.AMBIENT, 1.0F, 1.0F, false, 0, ISound.AttenuationType.LINEAR,
            var1, var3, var5);
   }

   public CustomSoundPlayer(SoundEvent var1, SoundCategory var2, float var3, float var4, double var5, double var7,
         double var9) {
      this(var1, var2, var3, var4, false, 0, ISound.AttenuationType.LINEAR, var5, var7, var9);
   }

   private CustomSoundPlayer(SoundEvent var1, SoundCategory var2, float var3, float var4, boolean var5, int var6,
         ISound.AttenuationType var7, double var8, double var10, double var12) {
      this(var1.getName(), var2, var3, var4, var5, var6, var7, var8, var10, var12, false);
   }

   public CustomSoundPlayer(
         Identifier var1, SoundCategory var2, float var3, float var4, boolean var5, int var6,
         ISound.AttenuationType var7, double var8, double var10, double var12, boolean var14) {
      super(var1, var2);
      this.volume = var3;
      this.pitch = var4;
      this.x = var8;
      this.y = var10;
      this.z = var12;
      this.repeat = var5;
      this.repeatDelay = var6;
      this.attenuationType = var7;
      this.global = var14;
   }
}
