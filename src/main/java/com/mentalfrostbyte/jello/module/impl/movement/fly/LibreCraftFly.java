package com.mentalfrostbyte.jello.module.impl.movement.fly;

import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.action.EventMouseHover;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventMove;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.util.game.player.MovementUtil;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

public class LibreCraftFly extends Module {
    private int field23910;
    private boolean field23911;

    public LibreCraftFly() {
        super(ModuleCategory.MOVEMENT, "LibreCraft", "A fly for LibreCraft");
        this.registerSetting(new NumberSetting<Float>("Speed", "Fly speed", 4.0F, Float.class, 0.3F, 10.0F, 0.1F));
    }

    @Override
    public void onEnable() {
        this.field23910 = 0;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                this.field23911 = false;
            }
        } else {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.field23911 = true;
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
    public void method16791(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (var1.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23911 = true;
            }
        }
    }

    @EventTarget
    public void method16792(EventMouseHover var1) {
        if (this.isEnabled()) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23911 = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void method16793(EventMove var1) {
        if (this.isEnabled()) {
            if (this.field23910 <= 0) {
                if (this.field23910 != -1) {
                    if (this.field23910 == 0) {
                        var1.setY(0.0);
                        mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                        MovementUtil.setMotion(var1, 0.35);
                    }
                } else {
                    var1.setY(0.299);
                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                    MovementUtil.setMotion(var1, this.getNumberValueBySettingName("Speed"));
                }
            } else {
                var1.setY(0.0);
                MovementUtil.setMotion(var1, 0.0);
            }
        }
    }

    @EventTarget
    public void method16794(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.field23910++;
            if (this.field23910 != 2) {
                if (this.field23910 > 2) {
                    if (this.field23910 >= 20 && this.field23910 % 20 == 0) {
                        var1.setY(0.1);
                    } else {
                        var1.cancelled = true;
                    }
                }
            } else {
                var1.setY(0.1);
            }

            var1.setMoving(true);
        }
    }

    @EventTarget
    public void method16795(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (mc.player != null && var4 instanceof SPlayerPositionLookPacket) {
                SPlayerPositionLookPacket var5 = (SPlayerPositionLookPacket) var4;
                if (this.field23910 >= 1) {
                    this.field23910 = -1;
                }

                var5.yaw = mc.player.rotationYaw;
                var5.pitch = mc.player.rotationPitch;
            }
        }
    }
}
