package com.mentalfrostbyte.jello.module;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.managers.impl.sound.CustomSoundPlayer;
import com.mentalfrostbyte.jello.module.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvents;
import team.sdhq.eventBus.EventBus;
import totalcross.json.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Module {

    public static Minecraft mc = Minecraft.getInstance();
    public String name;
    public String descriptor;
    public ModuleCategory category;
    public boolean enabled;
    public boolean allowed;
    private boolean availableOnClassic = true;
    private Module someMod = null;

    private static final List<Class<? extends Module>> moduleList = new ArrayList<>();
    public Map<String, Setting> settingMap = new LinkedHashMap<>();
    private int randomAssOffset = 0;

    public Module(ModuleCategory category, String name, String description) {
        this.category = category;
        this.name = name;
        this.descriptor = description;
    }

    public void registerSetting(Setting setting) {
        if (!this.settingMap.containsKey(setting.getName())) {
            this.settingMap.put(setting.getName(), setting);
        } else {
            throw new IllegalArgumentException("Attempted to add an option with the same name");
        }
    }

    public Object getSettingValueBySettingName(String settingName) {
        return this.settingMap.get(settingName).getCurrentValue();
    }

    public boolean getBooleanValueFromSettingName(String var1) {
        try {
            return Boolean.parseBoolean(this.getSettingValueBySettingName(var1).toString());
        } catch (Exception var5) {
            return false;
        }
    }

    public int parseSettingValueToIntBySettingName(String settingName) {
        try {
            return Integer.parseInt(this.getSettingValueBySettingName(settingName).toString());
        } catch (Exception var5) {
            return -1;
        }
    }

    public float getNumberValueBySettingName(String var1) {
        try {
            return Float.parseFloat(this.getSettingValueBySettingName(var1).toString());
        } catch (Exception var5) {
            return -1.0F;
        }
    }

    public String getStringSettingValueByName(String var1) {
        try {
            return (String) this.getSettingValueBySettingName(var1);
        } catch (Exception var5) {
            return null;
        }
    }

    public void method15984(String var1, String var2) {
        this.settingMap.get(var1).setCurrentValue(var2);
    }

    public void resetModuleState() {
        if (this.enabled) {
            this.onDisable();
        }

        this.enabled = false;
        this.allowed = true;

        for (Setting var4 : this.settingMap.values()) {
            var4.resetToDefault();
        }
    }

    public JSONObject initialize(JSONObject config) throws JSONException {
        JSONArray options = CJsonUtils.getJSONArrayOrNull(config, "options");

        this.enabled = config.getBoolean("enabled");

        this.allowed = config.getBoolean("allowed");

        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject settingCfg = options.getJSONObject(i);
                String optName = CJsonUtils.getStringOrDefault(settingCfg, "name", null);

                for (Setting<?> setting : this.settingMap.values()) {
                    if (setting.getName().equals(optName)) {
                        try {
                            setting.loadCurrentValueFromJSONObject(settingCfg);
                        } catch (JSONException2 jsonException2) {
                            System.err.println("Could not initialize settings of " + this.getName() + "." + setting.getName() + " from config.");
                        }
                        break;
                    }
                }
            }
        }

        if (this.enabled && mc.world != null) {
            this.onEnable();
        }

        return config;
    }

    public JSONObject buildUpModuleData(JSONObject obj) {
        try {
            obj.put("name", this.getName());
            obj.put("enabled", this.enabled);
            obj.put("allowed", this.isAllowed());
            JSONArray jsonArray = new JSONArray();

            for (Setting<?> s : this.settingMap.values()) {
                jsonArray.put(s.buildUpSettingData(new JSONObject()));
            }

            obj.put("options", jsonArray);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getSuffix() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.descriptor;
    }

    public void onEnable() {
        if (this.getClass().isAnnotationPresent(InDevelopment.class) && !moduleList.contains(this.getClass())) {
            System.err.println("This mod is still in development. Be careful!");
            moduleList.add(this.getClass());
        }
    }

    public void onDisable() {
    }

    public ModuleCategory getAdjustedCategoryBasedOnClientMode() {
        if (Client.getInstance().clientMode == ClientMode.CLASSIC && this.category == ModuleCategory.ITEM) {
            return ModuleCategory.PLAYER;
        } else {
            return Client.getInstance().clientMode == ClientMode.CLASSIC && this.category == ModuleCategory.EXPLOIT
                    ? ModuleCategory.MISC
                    : this.category;
        }
    }

    public int getRandomAssOffset() {
        return this.randomAssOffset;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public boolean isEnabled() {
        if (Client.getInstance().clientMode != ClientMode.NOADDONS) {
            return (Client.getInstance().clientMode != ClientMode.CLASSIC || this.isAvailableOnClassic()) && this.enabled;
        } else {
            return false;
        }
    }

    public void setState(boolean enabled) {
        if (this.enabled != enabled) {
            if (!(this.enabled = enabled)) {
               // Client.getInstance().getEventManager().unsubscribe(this);
                this.onDisable();
            } else {
                EventBus.register(this);
                this.onEnable();
            }
        }

        Client.getInstance().moduleManager.getMacOSTouchBar().onModuleToggled(this);
    }

    public void setEnabledBasic(boolean enabled) {
        this.enabled = enabled;
        if (!this.enabled) {
            EventBus.unregister(this);
        } else {
            EventBus.register(this);
        }
    }

    public void setEnabled(boolean newEnabled) {
        if (this.enabled != newEnabled) {
            if (!(this.enabled = newEnabled)) {
                EventBus.unregister(this);
                if (!(this instanceof ModuleWithModuleSettings)) {
                    if (Client.getInstance().clientMode == ClientMode.JELLO
                            /*&& Client.getInstance().moduleManager.getModuleByClass(com.mentalfrostbyte.jello.module.impl.gui.jello.ActiveMods.class).getBooleanValueFromSettingName("Sound")*/) {
                        Client.getInstance().soundManager.play("deactivate");
                    }

                    if (Client.getInstance().clientMode == ClientMode.CLASSIC
                        /*&& Client.getInstance().moduleManager.getModuleByClass(ActiveMods.class).getBooleanValueFromSettingName("Sound")*/) {
                        Minecraft.getInstance().getSoundHandler().play(CustomSoundPlayer.playSoundWithCustomPitch(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 0.6F));
                    }
                }

                this.onDisable();
            } else {
                EventBus.register(this);
                if (Client.getInstance().clientMode == ClientMode.JELLO
                        /*&& Client.getInstance().moduleManager.getModuleByClass(com.mentalfrostbyte.jello.module.impl.gui.jello.ActiveMods.class).getBooleanValueFromSettingName("Sound")*/) {
                    Client.getInstance().soundManager.play("activate");
                }

                if (Client.getInstance().clientMode == ClientMode.CLASSIC
                        /*&& Client.getInstance().moduleManager.getModuleByClass(ActiveMods.class).getBooleanValueFromSettingName("Sound")*/) {
                    Minecraft.getInstance().getSoundHandler().play(CustomSoundPlayer.playSoundWithCustomPitch(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 0.7F));
                }

                this.onEnable();
                this.randomAssOffset++;
            }
        }

        Client.getInstance().moduleManager.getMacOSTouchBar().onModuleToggled(this);
    }

    public void toggle() {
        this.setEnabled(!this.isEnabled());
    }

    public boolean isAllowed() {
        return this.allowed;
    }

    public void setAllowed(boolean var1) {
        this.allowed = var1;
    }

    public void setSomeMod(Module mod) {
        this.someMod = mod;
    }

    public Module access() {
        return this.someMod != null ? this.someMod : this;
    }

    public void setAvailableOnClassic(boolean var1) {
        this.availableOnClassic = var1;
    }

    public boolean isAvailableOnClassic() {
        return this.availableOnClassic;
    }

    public void initialize() {
    }

    public boolean isEnabled2() {
        return this.isEnabled();
    }

    public Map<String, Setting> getSettingMap() {
        return this.settingMap;
    }
}
