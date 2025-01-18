package com.mentalfrostbyte.jello.module.impl.world;


import com.mentalfrostbyte.jello.event.impl.ReceivePacketEvent;
import com.mentalfrostbyte.jello.event.impl.TickEvent;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class Weather extends Module {
    private float strength;
    private boolean raining;

    public Weather() {
        super(ModuleCategory.WORLD, "Weather", "Removes rain and changes the world's time");
        this.registerSetting(new BooleanSetting("Custom time", "Set the world time", true));
        this.registerSetting(new NumberSetting<>("Time", "Time to set the world to", 12000.0F, Float.class, 0.0F, 24000.0F, 1.0F).addObserver(var1 -> {
            if (this.getBooleanValueFromSettingName("Custom time") && this.isEnabled()) {
                mc.world.setDayTime(-((long) this.getNumberValueBySettingName("Time")));
            }
        }));
        this.registerSetting(new BooleanSetting("Disable rain", "Disable rain", true));
    }

    @Override
    public void onEnable() {
        this.strength = mc.world.getRainStrength(1.0F);
        if (mc.world.getRainStrength(1.0F) != 1.0F) {
            if (mc.world.getRainStrength(1.0F) == 0.0F) {
                this.raining = false;
            }
        } else {
            this.raining = true;
        }

        mc.world.setDayTime((long) this.getNumberValueBySettingName("Time"));
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            if (!this.getBooleanValueFromSettingName("Disable rain")) {
                if (this.raining) {
                    if (!(this.strength < 1.0F)) {
                        if (this.strength > 1.0F) {
                            this.strength = 1.0F;
                        }
                    } else {
                        this.strength = (float) ((double) this.strength + 0.05);
                    }
                }
            } else if (!(this.strength > 0.0F)) {
                if (this.strength < 0.0F) {
                    this.strength = 0.0F;
                }
            } else {
                this.strength = (float) ((double) this.strength - 0.05);
            }

            mc.world.setRainStrength(this.strength);
            mc.world.setThunderStrength(this.strength);
        }
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        if (this.isEnabled()) {
            if (!(event.getPacket() instanceof SUpdateTimePacket)) {
                if (event.getPacket() instanceof SChangeGameStatePacket) {
                    SChangeGameStatePacket var4 = (SChangeGameStatePacket) event.getPacket();
                    if (var4.func_241776_b_().field_241778_b_ == 7) {
                        if (var4.getValue() != 1.0F) {
                            if (var4.getValue() == 0.0F) {
                                this.raining = false;
                            }
                        } else {
                            this.raining = true;
                        }

                        if (!this.getBooleanValueFromSettingName("Disable rain")) {
                            this.strength = var4.getValue();
                        } else {
                            event.setPacket(new SChangeGameStatePacket(var4.func_241776_b_(), 0.0F));
                            this.strength = 0.0F;
                        }
                    }
                }
            }
        } else if (this.getBooleanValueFromSettingName("Custom time")) {
            event.setPacket(new SUpdateTimePacket(-((long) this.getNumberValueBySettingName("Time")), -((long) this.getNumberValueBySettingName("Time")), true));
        }
    }
}

