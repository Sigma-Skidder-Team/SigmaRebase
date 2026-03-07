package com.mentalfrostbyte.jello.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.gui.impl.jello.KeyManager;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.data.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.impl.gui.jello.*;
import com.mentalfrostbyte.jello.module.impl.combat.*;
import com.mentalfrostbyte.jello.module.impl.item.*;
import com.mentalfrostbyte.jello.module.impl.misc.*;
import com.mentalfrostbyte.jello.module.impl.movement.*;
import com.mentalfrostbyte.jello.module.impl.player.*;
import com.mentalfrostbyte.jello.module.impl.render.*;

import com.mentalfrostbyte.jello.module.impl.world.*;
import com.mentalfrostbyte.jello.util.system.other.GsonUtil;
import team.sdhq.eventBus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class ModuleManager {

    private final Map<Class<? extends Module>, Module> moduleMap = new LinkedHashMap<>();
    private ProfileManager profile;
    private KeyManager keyManager;
    private List<Module> modules = new ArrayList<>();

    private void register(Module module) {
        this.modules.add(module);
    }

    public void reset() {
        modules.forEach(EventBus::unregister);
        this.modules.clear();
    }

    private void sortBySuffixAndRegisterEvents() {
        this.modules.sort(Comparator.comparing(Module::getFormattedName));

        for (Module mod : this.modules) {
            EventBus.register(mod);
            this.moduleMap.put(mod.getClass(), mod);
        }
    }

    public void register(ClientMode clientMode) {
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
            this.register(new com.mentalfrostbyte.jello.module.impl.gui.classic.ActiveMods());
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
        this.register(new Cape());
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
        this.register(new CorrectMovement());
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

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public Module getModuleByClass(Class<? extends Module> module) {
        return this.moduleMap.get(module);
    }

    public JsonObject load(JsonObject json) {
        JsonArray array = null;

        try {
            array = json.getAsJsonArray("mods");
        } catch (JsonParseException exc) {
            Client.logger.warn(exc);
        }

        for (Module modulesFound : this.moduleMap.values()) {
            modulesFound.resetModuleState();
        }

        if (array != null) {
            for (int var15 = 0; var15 < array.size(); var15++) {
                JsonObject moduleObject;
                try {
                    moduleObject = array.get(var15).getAsJsonObject();
                } catch (JsonParseException e) {
                    throw new RuntimeException(e);
                }

                String moduleName = null;

                try {
                    moduleName = moduleObject.get("name").getAsString();
                } catch (JsonParseException exc) {
                    Client.logger.warn("Invalid name in mod list config", exc);
                }

                for (Module module : this.moduleMap.values()) {
                    if (module.getName().equals(moduleName)) {
                        try {
                            module.initialize(moduleObject);
                        } catch (JsonParseException exc) {
                            Client.logger.warn("Could not initialize mod {} from config. All settings for this mod have been erased.", module.getName(), exc);
                        }
                        break;
                    }
                }
            }
        } else {
            Client.logger.warn("Mods array does not exist in config. Assuming a blank profile...");
        }

        for (Module module : this.moduleMap.values()) {
            if (module.isEnabled()) {
                EventBus.register(module);
                if (module instanceof ModuleWithModuleSettings moduleWithSettings) {
                    if (moduleWithSettings.parentModule != null) {
                        EventBus.register(moduleWithSettings.parentModule);
                    }
                }
            } else {
                EventBus.unregister(module);
                if (module instanceof ModuleWithModuleSettings moduleWithSettings) {

                    for (Module module1 : moduleWithSettings.moduleArray) {
                        EventBus.unregister(module1);
                    }
                }
            }

            module.initialize();
        }

        return json;
    }

    public void loadProfileFromJSON(JsonObject json) {
        String profileName = null;

        try {
            profileName = GsonUtil.getStringOrDefault(json, "profile", "Default");
        } catch (JsonParseException ignored) {
        }

        if (Client.getInstance().clientMode == ClientMode.CLASSIC) {
            profileName = "Classic";
        }

        this.profile = new ProfileManager();
        this.keyManager = new KeyManager();

        try {
            this.profile.loadProfile(profileName);
            this.keyManager.method13732(json);
        } catch (IOException exc) {
            Client.logger.warn("Could not load profiles!", exc);
        }
    }

    public JsonObject loadCurrentConfig(JsonObject obj) {
        JsonArray array = new JsonArray();

        for (Module module : this.moduleMap.values()) {
            array.add(module.buildUpModuleData(new JsonObject()));
        }

        obj.add("mods", array);
        return obj;
    }

    public void method14660(JsonObject var1) {
        var1.addProperty("profile", this.profile.getCurrentConfig().profileName);
        this.profile.getCurrentConfig().moduleConfig = this.loadCurrentConfig(new JsonObject());

        try {
            this.profile.saveAndReplaceConfigs();
            this.keyManager.getKeybindsJSONObject(var1);
        } catch (IOException exc) {
            Client.logger.warn("Unable to save mod profiles...", exc);
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

    public ProfileManager getConfigurationManager() {
        return this.profile;
    }
}