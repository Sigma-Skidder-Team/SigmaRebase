package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;

import com.mentalfrostbyte.jello.module.settings.impl.InputSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.MultiUtilities;
import team.sdhq.eventBus.annotations.EventTarget;

public class Spammer extends Module {
    public int ticks;

    public Spammer() {
        super(ModuleCategory.MISC, "Spammer", "Spam a message");
        this.registerSetting(new InputSetting("Message", "The message sent.", "Use Sigma Client, it's free ! %r"));
        this.registerSetting(new NumberSetting<Float>("Messages delay", "Delay between messages", 3.0F, Float.class, 0.1F, 10.0F, 0.1F));
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            this.ticks++;
            if ((float) this.ticks > this.getNumberValueBySettingName("Messages delay") * 20.0F) {
                this.ticks = 0;
                String customMessage = this.getStringSettingValueByName("Message").replaceAll("%r", Integer.toString(Math.round(10.0F + (float) Math.random() * 89.0F)));
                MultiUtilities.sendChatMessage(customMessage);
            }
        }
    }
}
