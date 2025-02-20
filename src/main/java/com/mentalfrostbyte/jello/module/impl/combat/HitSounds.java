package com.mentalfrostbyte.jello.module.impl.combat;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

public class HitSounds extends Module {
    public HitSounds() {
        super(ModuleCategory.COMBAT, "HitSounds", "Changes the player hurting sounds client side.");
        this.registerSetting(new BooleanSetting("Hypixel", "Replicate hypixel hit onDamage.", false));
        this.registerSetting(new BooleanSetting("Criticals", "Play critical sound onDamage.", false));
        this.registerSetting(new NumberSetting<Float>("Sound Pitch", "Reproduced sound pitch.", 1, Float.class, 1, 4, 0.1f));
    }

    @EventTarget
    @LowerPriority
    public void onReceivePAKAR(EventReceivePacket var1) {
        if (this.isEnabled()) {
            if (var1.packet instanceof SPlaySoundPacket) {
                SPlaySoundPacket sp = (SPlaySoundPacket) var1.packet;
                if(sp.getSoundName().toString().equalsIgnoreCase("minecraft:entity.player.hurt")){
                    if(getBooleanValueFromSettingName("Hypixel")){
                        for(int i = 0; i < 2; i++){
                            mc.world.playSound((PlayerEntity) mc.player, mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1F, (float) (getNumberValueBySettingName("Sound Pitch") + Math.random()));
                        }
                    }
                    if(getBooleanValueFromSettingName("Criticals")){
                        for(int i = 0; i < 2; i++){
                            mc.world.playSound((PlayerEntity) mc.player, mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1F, (float) (getNumberValueBySettingName("Sound Pitch") + Math.random()));
                        }
                    }

                }
            }
        }
    }
}
