package com.mentalfrostbyte.jello.module.impl.combat.killaura;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Hand;

public class Class338 implements Runnable {
    public final float field1477;
    public final KillAura killauraMod;

    public Class338(KillAura var1, float var2) {
        this.killauraMod = var1;
        this.field1477 = var2;
    }

    @Override
    public void run() {
        if (KillAura.targetEntity != null
                && KillAura.method16844(this.killauraMod).isBlocking()
                && !this.killauraMod.getStringSettingValueByName("Autoblock Mode").equals("Vanilla")) {
            KillAura.method16844(this.killauraMod).stopAutoBlock();
        }
        if(!Minecraft.getInstance().player.isAlive()){
            return;
        }

        String var8 = this.killauraMod.getStringSettingValueByName("Mode");
        if ((this.killauraMod.getBooleanValueFromSettingName("Raytrace") || !this.killauraMod.getBooleanValueFromSettingName("Raytrace") || var8.equals("Multi"))) {
            for (TimedEntity var10 : KillAura.getTargets(this.killauraMod)) {
                Entity var11 = var10.getEntity();
                if(killauraMod.getBooleanValueFromSettingName("Raytrace")){
                    Minecraft.getInstance().clickMouse();
                }else{
                    Minecraft.getInstance().playerController.attackEntity(Minecraft.getInstance().player, var11);
                    boolean nigga = Client.getInstance().moduleManager.getModuleByClass(KillAura.class).isEnabled() && Client.getInstance().moduleManager.getModuleByClass(KillAura.class).getBooleanValueFromSettingName("No swing") && KillAura.targetEntity != null;
                    if (nigga) {
                        Minecraft.getInstance().getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                    } else {
                        Minecraft.getInstance().player.swingArm(Hand.MAIN_HAND);
                    }
                }
            }

            if (var8.equals("Multi2")) {
                KillAura.method16847(this.killauraMod, KillAura.method16846(this.killauraMod) + 1);
            }
        }

        if (KillAura.targetEntity != null && KillAura.method16844(this.killauraMod).canAutoBlock() && this.killauraMod.getStringSettingValueByName("Autoblock Mode").equals("Basic1")) {
            KillAura.method16844(this.killauraMod)
                    .performAutoBlock(KillAura.targetEntity, KillAura.getCurrentRotation(this.killauraMod).yaw, KillAura.getCurrentRotation(this.killauraMod).pitch);
        }
    }
}

