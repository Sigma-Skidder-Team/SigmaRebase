package net.minecraft.util;

public class Timer
{
    public float renderPartialTicks;
    public float elapsedPartialTicks;
    private long lastSyncSysClock;
    private final float tickLength;
    // MODIFICATION START: Allows modifying how fast timer goes
    public float timerSpeed;
    // MODIFICATION END

    public Timer(float ticks, long lastSyncSysClock)
    {
        this.tickLength = 1000.0F / ticks;
        this.lastSyncSysClock = lastSyncSysClock;
    }

    public int getPartialTicks(long gameTime)
    {
        // MODIFICATION START: Part of the timerSpeed modification,
        // >>>>        >         multiplies the original elapsed partial ticks by `timerSpeed`
        this.elapsedPartialTicks = (float)(gameTime - this.lastSyncSysClock) / this.tickLength * timerSpeed;
        // MODIFICATION END
        this.lastSyncSysClock = gameTime;
        this.renderPartialTicks += this.elapsedPartialTicks;
        int i = (int)this.renderPartialTicks;
        this.renderPartialTicks -= (float)i;
        return i;
    }
}
