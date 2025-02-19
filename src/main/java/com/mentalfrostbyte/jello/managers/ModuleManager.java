package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.gui.impl.jello.JelloTouch;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.gui.jello.*;
import com.mentalfrostbyte.jello.module.impl.combat.*;
import com.mentalfrostbyte.jello.module.impl.item.*;
import com.mentalfrostbyte.jello.module.impl.misc.*;
import com.mentalfrostbyte.jello.module.impl.movement.*;
import com.mentalfrostbyte.jello.module.impl.player.*;
import com.mentalfrostbyte.jello.module.impl.render.*;


import com.mentalfrostbyte.jello.module.impl.world.*;
import team.sdhq.eventBus.EventBus;
import totalcross.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class ModuleManager {

    private final Map<Class<? extends Module>, Module> moduleMap = new LinkedHashMap<>();
    private ProfileManager profile;
    private JelloTouch jelloTouch;
    private List<Module> modules;

    private void createModules() {
        this.modules = new ArrayList<>();
    }

    private void register(Module module) {
        this.modules.add(module);
    }

    private void sortBySuffixAndRegisterEvents() {
        this.modules.sort(Comparator.comparing(Module::getFormattedName));

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
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.RearView());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.MiniMap());
            this.register(new KeyStrokes());
            this.register(new com.mentalfrostbyte.jello.module.impl.render.jello.ESP());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.InfoHUD());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.ShulkerInfo());
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.jello.TabGUI());
            this.register(new com.mentalfrostbyte.jello.module.impl.render.jello.NameTags());
            this.register(new com.mentalfrostbyte.jello.module.impl.render.jello.Waypoints());
        }

        if (clientMode == ClientMode.CLASSIC) {
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.classic.TabGUI());
            this.register(new com.mentalfrostbyte.jello.module.impl.render.classic.ESP());
        }
        // COMBAT
        this.register(new AutoClicker());
        this.register(new AntiKnockback());
        this.register(new Criticals());
        this.register(new Teams());
        this.register(new Aimbot());
        this.register(new AntiBot());
        this.register(new AutoLog());
        this.register(new FastBow());
        this.register(new HitSounds());
        this.register(new KillAura());
        this.register(new Regen());
        this.register(new WTap());
        this.register(new BowAimbot());
        this.register(new InfiniteAura());
        // RENDER
        this.register(new AntiBlind());
        this.register(new DVDSimulator());
        this.register(new FPSBooster());
        this.register(new Fullbright());
        this.register(new ItemPhysics());
        this.register(new LowFire());
        this.register(new NoHurtCam());
        this.register(new Streaming());
        this.register(new CameraNoClip());
        this.register(new XRay());
        this.register(new NameProtect());
        this.register(new Tracers());
        this.register(new Search());
        this.register(new Projectiles());
        this.register(new NoServerInfo());
        this.register(new Breadcrumbs());
        this.register(new Freecam());
        this.register(new ChestESP());
        // WORLD
        this.register(new Weather());
        this.register(new FakeLag());
        this.register(new AntiVanish());
        this.register(new Timer());
        this.register(new InteractRange());
        this.register(new Disabler());
        this.register(new FastBreak());
        this.register(new FastPlace());
        this.register(new Nuker());
        this.register(new CakeEater());
        this.register(new ServerCrasher());
        this.register(new NewChunks());
        this.register(new AntiCactus());
        this.register(new Auto32k());
        // MISC
        this.register(new GamePlay());
        this.register(new ChatCleaner());
        this.register(new ChatFilter());
        this.register(new FakeForge());
        this.register(new GameIdler());
        this.register(new Jargon());
        this.register(new Murderer());
        this.register(new NickNameDetector());
        this.register(new Spammer());
        this.register(new StaffRepealer());
        this.register(new Unstuck());
        this.register(new GamePlay());
        this.register(new AntiLevitation());
        this.register(new NoteblockPlayer());
        this.register(new PacketEssentials());
        this.register(new PacketDumper());
        this.register(new PortalGodMode());
        this.register(new DebugSpeed());
        this.register(new AutoReconnect());
        // PLAYER
        this.register(new AutoSprint());
        this.register(new NoFall());
        this.register(new Blink());
        this.register(new AntiVoid());
        this.register(new AutoRespawn());
        this.register(new AutoWalk());
        this.register(new Derp());
        this.register(new FlagDetector());
        this.register(new NoViewReset());
        this.register(new Parkour());
        this.register(new Sneak());
        this.register(new OldHitting());
        this.register(new InvMove());
        this.register(new FastEat());
        this.register(new XCarry());
        // ITEM
        this.register(new AutoArmor());
        this.register(new AutoMLG());
        this.register(new AutoTools());
        this.register(new AutoTotem());
        this.register(new ChestStealer());
        this.register(new InvManager());
        this.register(new AutoPotion());
        this.register(new AutoSoup());
        this.register(new AutoGapple());
        // MOVEMENT
        this.register(new ClickTP());
        this.register(new Speed());
        this.register(new FastLadder());
        this.register(new Fly());
        this.register(new ElytraFly());
        this.register(new BoatFly());
        this.register(new Step());
        this.register(new Jesus());
        this.register(new SafeWalk());
        this.register(new Spider());
        this.register(new HighJump());
        this.register(new LongJump());
        this.register(new BlockFly());
        this.register(new NoSlow());
        this.register(new TargetStrafe());
        this.register(new Phase());
        this.register(new VClip());
        this.register(new Strafe());
        this.register(new EntitySpeed());

        this.sortBySuffixAndRegisterEvents();
    }

    public Map<Class<? extends Module>, Module> getModuleMap() {
        return moduleMap;
    }

    public JelloTouch getMacOSTouchBar() {
        return jelloTouch;
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

    public void loadProfileFromJSON(JSONObject json) {
        String profileName = null;

        try {
            profileName = json.getString("profile");
        } catch (JSONException ignored) {
        }

        if (Client.getInstance().clientMode == ClientMode.CLASSIC) {
            profileName = "Classic";
        }

        this.profile = new ProfileManager();
        this.jelloTouch = new JelloTouch();

        try {
            this.profile.loadProfile(profileName);
            this.jelloTouch.method13732(json);
        } catch (IOException var6) {
            Client.getInstance().getLogger().warn("Could not load profiles!");
            var6.printStackTrace();
            throw new RuntimeException("sorry m8");
        }

        this.jelloTouch.init();
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
        var1.put("profile", this.profile.getCurrentConfig().profileName);
        this.profile.getCurrentConfig().moduleConfig = this.saveCurrentConfigToJSON(new JSONObject());

        try {
            this.profile.saveAndReplaceConfigs();
            this.jelloTouch.getKeybindsJSONObject(var1);
        } catch (IOException var5) {
            var5.printStackTrace();
            Client.getInstance().getLogger().warn("Unable to save mod profiles...");
        }
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        ArrayList<Module> moduleList = new ArrayList<>();

        for (Module moduleFromMap : this.moduleMap.values()) {
            if (moduleFromMap.getCategoryBasedOnMode().equals(category)) {
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

    public ProfileManager getConfigurationManager() {
        return this.profile;
    }
}