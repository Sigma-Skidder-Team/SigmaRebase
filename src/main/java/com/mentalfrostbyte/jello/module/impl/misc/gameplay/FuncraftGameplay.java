package com.mentalfrostbyte.jello.module.impl.misc.gameplay;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.misc.GamePlay;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class FuncraftGameplay extends Module {
    public GamePlay parentModule;

    public FuncraftGameplay() {
        super(ModuleCategory.MISC, "Funcraft", "Gameplay for Funcraft");
    }

    @Override
    public void initialize() {
        this.parentModule = (GamePlay) this.access();
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (this.isEnabled() && mc.player != null) {
            IPacket<?> var4 = event.packet;
            if (var4 instanceof SChatPacket chatPacket) {
                String text = chatPacket.getChatComponent().getString();
                String playerName = mc.player.getName().getString().toLowerCase();
                if (this.parentModule.getBooleanValueFromSettingName("AutoL")
                        && (text.toLowerCase().contains("a été tué par " + playerName) || text.toLowerCase().contains("a été tué par le vide et " + playerName))) {
                    this.parentModule.processAutoLMessage(text);
                }
            }
        }
    }
}
