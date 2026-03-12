package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.combat.antibot.HypixelAntiBot;
import com.mentalfrostbyte.jello.module.impl.combat.antibot.MovementAntiBot;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;

public class AntiBot extends Module {
    public AntiBot() {
        super(ModuleCategory.COMBAT, "AntiBot", "Avoid the client to focus bots.");
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Advanced", "Hypixel").addObserver(var1 -> this.setup()));
    }

    @Override
    public void initialize() {
        if (this.isEnabled()) {
            this.setup();
        }
    }

    @Override
    public void onEnable() {
        this.setup();
    }

    @Override
    public void onDisable() {
        Client.getInstance().botManager.antiBot = null;
        Client.getInstance().botManager.bots.clear();
    }

    public void setup() {
        Client.getInstance().botManager.bots.clear();
        switch (this.getStringSettingValueByName("Mode")) {
            case "Advanced":
                Client.getInstance().botManager.antiBot = new MovementAntiBot();
                break;
            case "Hypixel":
                Client.getInstance().botManager.antiBot = new HypixelAntiBot();
        }
    }
}
