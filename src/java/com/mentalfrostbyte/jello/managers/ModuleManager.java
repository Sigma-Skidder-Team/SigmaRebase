package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.gui.unmapped.MacOSTouchBar;
import com.mentalfrostbyte.jello.managers.impl.profile.Class6814;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.movement.Fly;
import com.mentalfrostbyte.jello.module.impl.movement.Speed;
import com.mentalfrostbyte.jello.module.impl.player.AutoSprint;
import com.mentalfrostbyte.jello.module.impl.player.Cape;
import com.mentalfrostbyte.jello.module.impl.player.NoFall;
import team.sdhq.eventBus.EventBus;
import totalcross.json.*;

import java.io.IOException;
import java.util.*;

public class ModuleManager {

    private final Map<Class<? extends Module>, Module> moduleMap = new LinkedHashMap<>();
    private Class6814 profile;
    private MacOSTouchBar macOSTouchBar;
    private List<Module> modules;

    private void createModules() {
        this.modules = new ArrayList<>();
    }

    private void register(Module var1) {
        this.modules.add(var1);
    }

    private void sortBySuffixAndRegisterEvents() {
        this.modules.sort(Comparator.comparing(Module::getSuffix));

        for (Module mod : this.modules) {
            EventBus.register(mod);
            this.moduleMap.put(mod.getClass(), mod);
        }
    }

    public void register(ClientMode clientMode) {
        this.createModules();

        // GUI
        if (clientMode == ClientMode.JELLO) {
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.ActiveMods());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.BrainFreeze());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.Compass());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.Coords());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.MusicParticles());
        }

        if (clientMode == ClientMode.CLASSIC) {
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.classic.TabGUI());
        }
        // COMBAT

        // RENDER

        // WORLD

        // MISC

        // PLAYER
        this.register(new AutoSprint());
        this.register(new Cape());
        this.register(new NoFall());

        // ITEM

        // MOVEMENT
        this.register(new Speed());
        this.register(new Fly());

        this.sortBySuffixAndRegisterEvents();
    }

    public Map<Class<? extends Module>, Module> getModuleMap() {
        return moduleMap;
    }

    public MacOSTouchBar getMacOSTouchBar() {
        return macOSTouchBar;
    }

    public Module getModuleByClass(Class<? extends Module> module) {
        return this.moduleMap.get(module);
    }

    public JSONObject load(JSONObject json) {
        JSONArray array = null;

        try {
            array = CJsonUtils.getJSONArrayOrNull(json, "mods");
        } catch (JSONException2 ignored) {
        }

        for (Module modulesFound : this.moduleMap.values()) {
            modulesFound.resetModuleState();
        }

        if (array != null) {
            for (int var15 = 0; var15 < array.length(); var15++) {
                JSONObject moduleObject = null;
                try {
                    moduleObject = array.getJSONObject(var15);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String moduleName = null;

                try {
                    moduleName = CJsonUtils.getStringOrDefault(moduleObject, "name", null);
                } catch (JSONException2 var13) {
                    Client.getInstance().getLogger().warn("Invalid name in mod list config");
                }

                for (Module module : this.moduleMap.values()) {
                    if (module.getName().equals(moduleName)) {
                        try {
                            module.initialize(moduleObject);
                        } catch (JSONException2 | JSONException var12) {
                            Client.getInstance().getLogger().warn("Could not initialize mod " + module.getName() + " from config. All settings for this mod have been erased.");
                        }
                        break;
                    }
                }
            }
        } else {
            Client.getInstance().getLogger().warn("Mods array does not exist in config. Assuming a blank profile...");
        }

        for (Module module : this.moduleMap.values()) {
            if (module.isEnabled()) {
                EventBus.register(module);
                if (module instanceof ModuleWithModuleSettings) {
                    ModuleWithModuleSettings moduleWithSettings = (ModuleWithModuleSettings) module;
                    if (moduleWithSettings.parentModule != null) {
                        EventBus.register(moduleWithSettings.parentModule);
                    }
                }
            } else {
                EventBus.unregister(module);
                if (module instanceof ModuleWithModuleSettings) {
                    ModuleWithModuleSettings moduleWithSettings = (ModuleWithModuleSettings) module;

                    for (Module module1 : moduleWithSettings.moduleArray) {
                        EventBus.unregister(module1);
                    }
                }
            }

            module.initialize();
        }

        return json;
    }

    public void method14659(JSONObject var1) {
        String var4 = null;

        try {
            var4 = var1.getString("profile");
        } catch (JSONException ignored) {}

        if (Client.getInstance().clientMode == ClientMode.CLASSIC) {
            var4 = "Classic";
        }

        this.profile = new Class6814();
        this.macOSTouchBar = new MacOSTouchBar();

        try {
            this.profile.loadProfile(var4);
            this.macOSTouchBar.method13732(var1);
        } catch (IOException var6) {
            Client.getInstance().getLogger().warn("Could not load profiles!");
            var6.printStackTrace();
            throw new RuntimeException("sorry m8");
        }

        this.macOSTouchBar.init();
    }

    public JSONObject saveCurrentConfigToJSON(JSONObject obj) {
        JSONArray array = new JSONArray();

        for (Module module : this.moduleMap.values()) {
            array.put(module.buildUpModuleData(new JSONObject()));
        }

        obj.put("mods", array);
        return obj;
    }

    public void method14660(JSONObject var1) {
        var1.put("profile", this.profile.getCurrentConfig().getName);
        this.profile.getCurrentConfig().serializedConfigData = this.saveCurrentConfigToJSON(new JSONObject());

        try {
            this.profile.saveAndReplaceConfigs();
            this.macOSTouchBar.getKeybindsJSONObject(var1);
        } catch (IOException var5) {
            var5.printStackTrace();
            Client.getInstance().getLogger().warn("Unable to save mod profiles...");
        }
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        ArrayList<Module> moduleList = new ArrayList<>();

        for (Module moduleFromMap : this.moduleMap.values()) {
            if (moduleFromMap.getAdjustedCategoryBasedOnClientMode().equals(category)) {
                moduleList.add(moduleFromMap);
            }
        }

        return moduleList;
    }

    public List<Module> getEnabledModules() {
        ArrayList<Module> moduleList = new ArrayList();

        for (Module moduleFromMap : this.moduleMap.values()) {
            if (moduleFromMap.isEnabled()) {
                moduleList.add(moduleFromMap);
            }
        }

        return moduleList;
    }

    public Class6814 getConfigurationManager() {
        return this.profile;
    }
}
