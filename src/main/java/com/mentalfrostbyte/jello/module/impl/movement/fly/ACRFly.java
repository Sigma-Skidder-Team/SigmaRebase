package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

public class ACRFly extends Module {
    private int preUpdates;
    private double field23987;
    private boolean notSneaking;

    public ACRFly() {
        super(ModuleCategory.MOVEMENT, "Reloaded", "A fly for AnticheatReloaded");
        this.registerSetting(new NumberSetting<Float>("Speed", "Fly speed", 4.0F, Float.class, 0.3F, 8.0F, 0.1F));
        this.registerSetting(new BooleanSetting("Offset", "Offset while flying", false));
        this.registerSetting(new BooleanSetting("NoFall", "Avoid getting fall damage when flying down", true));
    }

    @Override
    public void onEnable() {
        this.field23987 = mc.player.getPosY();
        this.preUpdates = 0;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            this.notSneaking = false;
        } else {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.notSneaking = true;
        }
    }

    @Override
    public void onDisable() {
        MovementUtil.moveInDirection(0.0);
        if (mc.player.getMotion().y > 0.0) {
            mc.player.setMotion(mc.player.getMotion().x, -0.0789, mc.player.getMotion().z);
        }
    }

    @EventTarget
    public void method16902(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (var1.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.notSneaking = true;
            }
        }
    }

    @EventTarget
    public void method16903(EventMouseHover var1) {
        if (this.isEnabled()) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.notSneaking = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void method16904(EventMove var1) {
        if (this.isEnabled()) {
            if (this.preUpdates != -1) {
                if (this.preUpdates == 0) {
                    if (Math.abs(var1.getY()) < 0.08) {
                        var1.setY(!this.getBooleanValueFromSettingName("Offset") ? 0.0 : -0.01);
                    }

                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                    MovementUtil.setMotion(var1, 0.35);
                }
            } else {
                double var4 = !this.getBooleanValueFromSettingName("Offset") ? 0.0 : 0.01;
                if (this.notSneaking) {
                    var4 -= this.getNumberValueBySettingName("Speed") / 2.0F;
                }

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    var4 += this.getNumberValueBySettingName("Speed") / 2.0F;
                }

                var1.setY(var4);
                mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                MovementUtil.setMotion(var1, this.getNumberValueBySettingName("Speed"));
            }
        }
    }

    @EventTarget
    public void method16905(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.preUpdates++;
            if (this.preUpdates != 2) {
                if (this.preUpdates > 2 && this.preUpdates >= 20 && this.preUpdates % 20 == 0) {
                    var1.setY(-150.0 - Math.random() * 150.0);
                }
            } else {
                var1.setY(-150.0 - Math.random() * 150.0);
            }

            if (this.getBooleanValueFromSettingName("NoFall")) {
                var1.setOnGround(true);
            }

            var1.setMoving(true);
        }
    }

    @EventTarget
    public void method16906(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof SPlayerPositionLookPacket lookPacket) {
				if (this.preUpdates >= 1) {
                    this.preUpdates = -1;
                }

                this.field23987 = lookPacket.getY();
                lookPacket.yaw = mc.player.rotationYaw;
                lookPacket.pitch = mc.player.rotationPitch;
            }
        }
    }

    @EventTarget
    public void method16907(EventSendPacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof CPlayerPacket var5) {
				if (this.preUpdates == -1 && this.getBooleanValueFromSettingName("NoFall")) {
                    var5.onGround = true;
                }
            }
        }
    }

    @EventTarget
    public void method16908(EventRender2D var1) {
        if (this.isEnabled()) {
            double y = this.field23987;
            mc.player.setPosition(mc.player.getPosX(), y, mc.player.getPosZ());
            mc.player.lastTickPosY = y;
            mc.player.chasingPosY = y; // wtf???
            mc.player.prevPosY = y;
        }
    }
}
