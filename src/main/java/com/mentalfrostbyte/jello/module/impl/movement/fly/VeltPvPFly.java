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
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SChatPacket;

public class VeltPvPFly extends Module {
    private int field23419;
    private int field23420;
    private double field23421;
    private double field23422;
    private boolean field23423;

    public VeltPvPFly() {
        super(ModuleCategory.MOVEMENT, "VeltPvP", "A fly for VeltPvP");
        this.registerSetting(new NumberSetting<Float>("Speed", "Fly speed", 4.0F, Float.class, 0.2F, 5.0F, 0.1F));
    }

    @Override
    public void onEnable() {
        this.field23421 = mc.player.getPosY();
        this.field23419 = 0;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                this.field23423 = false;
            }
        } else {
            mc.gameSettings.keyBindSneak.setPressed(false);
            this.field23423 = true;
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
    public void method16045(EventKeyPress var1) {
        if (this.isEnabled()) {
            if (var1.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23423 = true;
            }
        }
    }

    @EventTarget
    public void method16046(EventMouseHover var1) {
        if (this.isEnabled()) {
            if (var1.getMouseButton() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                var1.cancelled = true;
                this.field23423 = false;
            }
        }
    }

    @EventTarget
    @LowerPriority
    public void method16047(EventMove var1) {
        if (this.isEnabled()) {
            double var4 = (double) this.getNumberValueBySettingName("Speed");
            if (this.field23419 <= 0) {
                if (this.field23419 != -1) {
                    if (this.field23419 == 0) {
                        if (!mc.gameSettings.keyBindJump.isKeyDown() && var1.getY() > 0.0) {
                            var1.setY(-MovementUtil.getJumpValue());
                        }

                        mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                        MovementUtil.setMotion(var1, var4 - 0.1);
                    }
                } else {
                    if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                        var1.setY(!this.field23423 ? MovementUtil.getJumpValue() : -var4 / 2.0);
                    } else {
                        var1.setY(!this.field23423 ? var4 / 2.0 : MovementUtil.getJumpValue());
                        this.field23422 = this.field23421;
                        this.field23421 = !this.field23423 ? mc.player.getPosY() + var1.getY() : this.field23421;
                    }

                    mc.player.setMotion(mc.player.getMotion().x, var1.getY(), mc.player.getMotion().z);
                    MovementUtil.setMotion(var1, var4);
                }
            } else {
                var1.setY(0.0);
                MovementUtil.setMotion(var1, 0.0);
            }
        }
    }

    @EventTarget
    public void method16048(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled() && var1.isPre()) {
            this.field23419++;
            if (this.field23419 != 2) {
                if (this.field23419 > 2) {
                    if (this.field23419 >= 20 && this.field23419 % 20 == 0) {
                        double var4 = 150.0 + Math.random() * 150.0;
                        double var6 = -var4;
                        var1.setY(var6);
                        this.field23420 += 2;
                    } else {
                        var1.cancelled = true;
                    }
                }
            } else {
                double var8 = 150.0 + Math.random() * 150.0;
                double var9 = -var8;
                var1.setY(var9);
                this.field23420 += 2;
            }

            var1.setMoving(true);
        }
    }

    @EventTarget
    public void method16049(EventReceivePacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (!(var4 instanceof SPlayerPositionLookPacket)) {
                if (var4 instanceof SChatPacket) {
                    SChatPacket var5 = (SChatPacket) var4;
                    String var6 = var5.getChatComponent().getString();
                    if (this.field23420 > 0 && (var6.contains("Now leaving: §") || var6.contains("Now entering: §"))) {
                        this.field23420--;
                        var1.cancelled = true;
                    }
                }
            } else {
                SPlayerPositionLookPacket var7 = (SPlayerPositionLookPacket) var4;
                if (this.field23419 >= 1) {
                    this.field23419 = -1;
                }

                this.field23422 = this.field23421;
                this.field23421 = var7.getY();
            }
        }
    }

    @EventTarget
    public void method16050(EventSendPacket var1) {
        if (this.isEnabled()) {
            IPacket var4 = var1.packet;
            if (var4 instanceof CPlayerPacket) {
                CPlayerPacket var5 = (CPlayerPacket) var4;
                if (this.field23419 == -1) {
                    var5.onGround = true;
                }
            }
        }
    }

    @EventTarget
    public void method16051(EventRender2D var1) {
        if (this.isEnabled()) {
            double var4 = this.field23421;
            mc.player.setPosition(mc.player.getPosX(), var4, mc.player.getPosZ());
            mc.player.lastTickPosY = var4;
            mc.player.chasingPosY = var4;
            mc.player.prevPosY = var4;
        }
    }
}
