package com.mentalfrostbyte.jello.module.impl.combat.criticals;

import com.mentalfrostbyte.jello.event.impl.game.EventRayTraceResult;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.Step;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.system.math.MathHelper;
import com.mentalfrostbyte.jello.util.system.math.counter.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class PacketCriticals extends Module {
    private final TimerUtil timer = new TimerUtil();
    private boolean field23999;

    public PacketCriticals() {
        super(ModuleCategory.COMBAT, "Packet", "Packet criticals");
        this.registerSetting(new NumberSetting<Integer>("HurtTime", "The hurtTime to crit at", 15.0F, Integer.class, 0.0F, 20.0F, 1.0F));
        this.registerSetting(new ModeSetting("Mode", "Mode", 0, "Basic", "Hypixel"));
    }

    @EventTarget
    public void method16923(EventRayTraceResult var1) {
        if (this.isEnabled() && var1.getEntity() != null && var1.isHovering()) {
            Entity var4 = var1.getEntity();
            if (var4 instanceof LivingEntity
                    && var4.hurtResistantTime <= (int) this.getNumberValueBySettingName("HurtTime")
                    && Step.updateTicksBeforeStep > 1
                    && (this.timer.getElapsedTime() > 200L || var4.hurtResistantTime > 0)
                    && mc.player.isOnGround()
                    && mc.player.collidedVertically) {
                double[] var5 = new double[]{0.2, 0.0};
                if (this.getStringSettingValueByName("Mode").equals("Hypixel")) {
                    var5 = new double[]{
                            0.0624 + MathHelper.getRandomValue(), 1.0E-14 + MathHelper.getRandomValue(), 0.0624 + MathHelper.getRandomValue(), 1.0E-14 + MathHelper.getRandomValue()
                    };
                }

                for (int var6 = 0; var6 < var5.length; var6++) {
                    mc.getConnection()
                            .sendPacket(
                                    new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + var5[var6], mc.player.getPosZ(), false)
                            );
                }

                this.field23999 = true;
                this.timer.reset();
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16924(EventSendPacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof CPlayerPacket) {
                if (!this.timer.isEnabled()) {
                    this.timer.start();
                }

                if (this.field23999 && mc.player.isOnGround()) {
                    event.cancelled = true;
                    this.field23999 = false;
                }
            }
        }
    }
}
