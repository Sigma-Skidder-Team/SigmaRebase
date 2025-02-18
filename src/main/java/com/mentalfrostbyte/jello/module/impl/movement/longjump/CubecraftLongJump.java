package com.mentalfrostbyte.jello.module.impl.movement.longjump;

import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;

public class CubecraftLongJump extends Module {
    private int airTicks;
    private double speed;

    public CubecraftLongJump() {
        super(ModuleCategory.MOVEMENT, "Cubecraft", "Longjump for Cubecraft.");
        this.registerSetting(new NumberSetting<>("Boost", "Longjump boost", 3.0F, Float.class, 1.0F, 5.0F, 0.01F));
    }

    @Override
    public void onDisable() {
        MovementUtil.moveInDirection(MovementUtil.getSmartSpeed() * 0.8);
        mc.timer.timerSpeed = 1.0F;
    }

    @Override
    public void onEnable() {
        this.airTicks = -1;
        mc.timer.timerSpeed = 0.3F;
    }

    @EventTarget
    public void onPlayerTick(EventPlayerTick event) {
        if (this.isEnabled() && mc.player != null) {
            if (!BlockUtil.isAboveBounds(mc.player, 0.001F)) {
                this.airTicks++;
                this.speed -= 0.005;
                if (this.speed < 0.26 || this.airTicks > 6) {
                    this.speed = 0.26;
                }

                MovementUtil.moveInDirection(this.speed);
                if (this.airTicks > 5) {
                    this.access().toggle();
                }
            } else {
                if (this.airTicks > 0) {
                    MovementUtil.moveInDirection(0.0);
                    this.access().toggle();
                    this.airTicks = 0;
                }

                double x = mc.player.getPosX();
                double y = mc.player.getPosY();
                double z = mc.player.getPosZ();
                int packetCount = 49 + MovementUtil.getJumpBoost() * 17;

                for (int i = 0; i < packetCount; i++) {
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.06248, z, false));
                    mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                }

                mc.getConnection().sendPacket(new CPlayerPacket.PositionPacket(x, y, z, true));
                mc.player.setMotion(mc.player.getMotion().x, MovementUtil.getJumpValue(), mc.player.getMotion().z);
                this.airTicks = 0;
                this.speed = this.getNumberValueBySettingName("Boost") / 2.0F;
                MovementUtil.moveInDirection(this.speed);
            }
        }
    }
}
