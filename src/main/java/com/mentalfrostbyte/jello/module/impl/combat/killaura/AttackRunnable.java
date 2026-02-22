package com.mentalfrostbyte.jello.module.impl.combat.killaura;

import com.mentalfrostbyte.jello.module.impl.combat.KillAura;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Hand;

public class AttackRunnable implements Runnable, MinecraftUtil {
    public final KillAura killAura;

    public AttackRunnable(KillAura killaura) {
        this.killAura = killaura;
    }

    @Override
    public void run() {
        if (KillAura.targetEntity != null
                && KillAura.autoBlockController.isBlocking()
                && !this.killAura.getStringSettingValueByName("Autoblock Mode").equals("Vanilla")) {

            KillAura.autoBlockController.stopAutoBlock();
        }

        if (!mc.player.isAlive() || mc.player.isSpectator()) {
            return;
        }

        String mode = this.killAura.getStringSettingValueByName("Mode");
        if ((this.killAura.getBooleanValueFromSettingName("Raytrace") ||
                !this.killAura.getBooleanValueFromSettingName("Raytrace") ||
                mode.equals("Multi"))) {

            for (TimedEntity timedEntity : KillAura.getTargets(this.killAura)) {
                Entity entity = timedEntity.getEntity();

                if (killAura.getBooleanValueFromSettingName("Raytrace")) {
                    mc.clickMouse();
                } else {
                    mc.playerController.attackEntity(mc.player, entity);

                    boolean packetSwing = killAura.isEnabled() && killAura.getBooleanValueFromSettingName("No swing") && KillAura.targetEntity != null;

                    if (packetSwing) {
                        mc.getConnection().sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                    } else {
                        mc.player.swingArm(Hand.MAIN_HAND);
                    }
                }
            }

            if (mode.equals("Multi2")) {
                KillAura.setAttackDelya(this.killAura, KillAura.getAttackDelay(this.killAura) + 1);
            }
        }

        if (KillAura.targetEntity != null && KillAura.autoBlockController.canAutoBlock() && this.killAura.getStringSettingValueByName("Autoblock Mode").equals("Basic1")) {
            KillAura.autoBlockController
                    .performAutoBlock(KillAura.targetEntity, KillAura.getCurrentRotation(this.killAura).yaw, KillAura.getCurrentRotation(this.killAura).pitch);
        }
    }
}

