package com.mentalfrostbyte.jello.module.impl.world;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Weather extends Module {
    private float rainStrength;
    private boolean isRaining;

    public Weather() {
        super(ModuleCategory.WORLD, "Weather", "Removes rain and changes the world's time");
        this.registerSetting(new BooleanSetting("Custom time", "Set the world time", true));
        this.registerSetting(new NumberSetting<>("Time", "Time to set the world to", 12000.0F, Float.class, 0.0F, 24000.0F, 1.0F));
        this.registerSetting(new BooleanSetting("Disable rain", "Disable rain", true));
    }

    @Override
    public void onEnable() {
        if (mc.world != null) {
            this.rainStrength = mc.world.getRainStrength(1.0F);
            this.isRaining = mc.world.getRainStrength(1.0F) == 1.0F;
            applyCustomTime();
            if (this.getBooleanValueFromSettingName("Disable rain")) {
                applyWeatherOverride();
            }
        }
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            applyCustomTime();
            if (this.getBooleanValueFromSettingName("Disable rain")) {
                applyWeatherOverride();
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof SUpdateTimePacket) {
                event.setCancelled(true);
            } else if (event.getPacket() instanceof SChangeGameStatePacket) {
                SChangeGameStatePacket gameStatePacket = (SChangeGameStatePacket) event.getPacket();
                if (gameStatePacket.func_241776_b_().field_241778_b_ == 7) {
                    this.isRaining = gameStatePacket.getValue() == 1.0F;
                    if (this.getBooleanValueFromSettingName("Disable rain")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private void applyCustomTime() {
        if (this.getBooleanValueFromSettingName("Custom time")) {
            long customTime = (long) this.getNumberValueBySettingName("Time");
            mc.world.setDayTime(customTime);
        }
    }

    private void applyWeatherOverride() {
        this.rainStrength = 0.0F;
        mc.world.setRainStrength(this.rainStrength);
        mc.world.setThunderStrength(this.rainStrength);
    }
}
