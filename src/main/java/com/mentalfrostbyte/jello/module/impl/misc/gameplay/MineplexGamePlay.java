package com.mentalfrostbyte.jello.module.impl.misc.gameplay;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.misc.GamePlay;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class MineplexGamePlay extends Module {
    public GamePlay parentModule;
    public boolean foundTeam;
    public final TimerUtil timer = new TimerUtil();

    public MineplexGamePlay() {
        super(ModuleCategory.MISC, "Mineplex", "Gameplay for Mineplex");
    }

    @Override
    public void initialize() {
        this.parentModule = (GamePlay) this.access();
    }

    @Override
    public void onEnable() {
        this.foundTeam = false;
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (this.isEnabled() && mc.player != null) {
            IPacket<?> packet = event.packet;
            if (packet instanceof SChatPacket chatPacket) {
                String text = chatPacket.getChatComponent().getString();
                String playerName = mc.player.getName().getString().toLowerCase();
                if (this.parentModule.getBooleanValueFromSettingName("AutoL") && text.toLowerCase().contains("killed by " + playerName + " ")) {
                    this.parentModule.processAutoLMessage(text);
                }

                String[] teamColors = new String[]{"Green", "Red", "Blue", "Yellow"};

                for (int i = 0; i < teamColors.length; i++) {
                    if (text.equals(teamColors[i] + " won the game!")) {
                        this.timer.reset();
                        this.foundTeam = true;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onWorldLoad(EventLoadWorld event) {
        if (this.isEnabled()) {
            this.foundTeam = false;
        }
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (this.getBooleanValueFromSettingName("AutoGG") && this.timer.getElapsedTime() > 5000L && this.foundTeam) {
                this.foundTeam = false;
                this.timer.reset();
                this.parentModule.initializeAutoL();
            }
        }
    }
}
