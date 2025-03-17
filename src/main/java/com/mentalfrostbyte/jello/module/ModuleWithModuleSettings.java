package com.mentalfrostbyte.jello.module;

import com.google.gson.JsonParseException;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import team.sdhq.eventBus.EventBus;
import totalcross.json.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleWithModuleSettings extends Module {

    private final List<ModuleStateListener> moduleStateListeners = new ArrayList<>();
    public Module[] moduleArray;
    public Module parentModule;
    public ModeSetting modeSetting;
    public String customModeName;
    private final List<String> stringList = new ArrayList<>();

    public ModuleWithModuleSettings(ModuleCategory category, String type, String description, Module... modules) {
        this(category, type, description, "Type", modules);
    }

    public ModuleWithModuleSettings(ModuleCategory category, String type, String description, String customModeName, Module... modules) {
        super(category, type, description);
        this.moduleArray = modules;

        for (Module moduleFromArray : moduleArray) {
            EventBus.register(moduleFromArray);
            stringList.add(moduleFromArray.getName());
            moduleFromArray.setSomeMod(this);
        }

        this.customModeName = customModeName;
        this.registerSetting(modeSetting = new ModeSetting(customModeName, type + " mode", 0, stringList.toArray(new String[0])));
        this.modeSetting.addObserver(var1x -> calledOnEnable());
        this.calledOnEnable();
    }

    public void calledOnEnable() {
        this.isTypeSetToThisModName();

        for (Module module : this.moduleArray) {
            boolean isParent = this.getStringSettingValueByName(this.customModeName).equals(module.name);
            if (this.isEnabled() && mc.player != null) {
                module.setState(isParent);
                if (isParent) {
                    this.parentModule = module;
                }
            } else if (this.isEnabled()) {
                module.setEnabledBasic(isParent);
            }

            this.setModuleEnabled(module, isParent);
        }
    }

    private void isTypeSetToThisModName() {
        boolean isOurName = false;

        for (Module module : this.moduleArray) {
            if (this.getStringSettingValueByName(this.customModeName).equals(module.name)) {
                isOurName = true;
            }
        }

        if (!isOurName) {
            this.setSetting(this.customModeName, this.moduleArray[0].name);
        }
    }

    public Module getModWithTypeSetToName() {
        this.isTypeSetToThisModName();

        for (Module mod : this.moduleArray) {
            if (this.getStringSettingValueByName(this.customModeName).equals(mod.name)) {
                return mod;
            }
        }

        return null;
    }

    @Override
    public boolean isEnabled2() {
        if (this.parentModule == null) {
            this.calledOnEnable();
        }

        return this.parentModule != null ? this.parentModule.isEnabled2() : this.isEnabled();
    }

    @Override
    public JSONObject initialize(JSONObject config) throws JsonParseException {
        JSONObject var4 = CJsonUtils.getJSONObjectOrNull(config, "sub-options");
        if (var4 != null) {
            for (Module var8 : this.moduleArray) {
                JSONArray var9 = CJsonUtils.getJSONArrayOrNull(var4, var8.getName());
                if (var9 != null) {
                    for (int var10 = 0; var10 < var9.length(); var10++) {
                        JSONObject var11 = var9.getJSONObject(var10);
                        String var12 = CJsonUtils.getStringOrDefault(var11, "name", null);

                        for (Setting<?> var14 : var8.settingMap.values()) {
                            if (var14.getName().equals(var12)) {
                                try {
                                    var14.loadCurrentValueFromJSONObject(var11);
                                } catch (JsonParseException e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        JSONObject var18 = super.initialize(config);
        if (this.enabled) {
            this.calledOnEnable();
        }

        return var18;
    }

    @Override
    public JSONObject buildUpModuleData(JSONObject obj) {
        try {
            JSONObject subOptionsObj = new JSONObject();

            for (Module mod : this.moduleArray) {
                JSONArray arr = new JSONArray();

                for (Setting<?> setting : mod.settingMap.values()) {
                    arr.put(setting.buildUpSettingData(new JSONObject()));
                }

                subOptionsObj.put(mod.getName(), arr);
            }

            obj.put("sub-options", subOptionsObj);
            return super.buildUpModuleData(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        this.calledOnEnable();
    }

    @Override
    public void onDisable() {
        for (Module var6 : this.moduleArray) {
            var6.setEnabled(false);
        }
    }

    @Override
    public void resetModuleState() {
        for (Module var6 : this.moduleArray) {
            var6.setState(false);
        }

        super.resetModuleState();
    }

    public final void addModuleStateListener(ModuleStateListener listener) {
        this.moduleStateListeners.add(listener);
    }

    public final void setModuleEnabled(Module module, boolean enabled) {
        for (ModuleStateListener listener : this.moduleStateListeners) {
            listener.onModuleEnabled(this, module, enabled);
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        for (Module mod : this.moduleArray) {
            mod.initialize();
        }
    }

}
