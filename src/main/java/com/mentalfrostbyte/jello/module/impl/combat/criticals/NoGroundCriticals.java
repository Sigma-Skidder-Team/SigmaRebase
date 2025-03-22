package com.mentalfrostbyte.jello.module.impl.combat.criticals;

import com.mentalfrostbyte.jello.event.impl.player.movement.EventUpdateWalkingPlayer;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventStep;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventJump;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.data.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.movement.Jesus;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.util.math.RayTraceResult;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HigherPriority;

public class NoGroundCriticals extends Module {
    private int field23410;
    private boolean field23411;
    private boolean field23412;
    private boolean field23413;

    public NoGroundCriticals() {
        super(ModuleCategory.COMBAT, "NoGround", "NoGround criticals");
        this.registerSetting(new ModeSetting("Offset", "The way you will fake no ground", 0, "Vanilla", "OldHypixel"));
        this.registerSetting(new BooleanSetting("Avoid Fall Damage", "Avoid fall damages.", true));
    }

    @Override
    public void onEnable() {
        if (BlockUtil.isAboveBounds(mc.player, 0.001F)) {
            this.field23411 = this.getStringSettingValueByName("Offset").equals("OldHypixel");
            this.field23410 = !this.field23411 ? 1 : 2;
        }

        this.field23412 = false;
    }

    @EventTarget
    @HigherPriority
    public void method16034(EventStep event) {
        if (this.isEnabled() && !(event.getHeight() < 0.625)) {
            if (this.field23410 == 0 && this.field23411) {
                event.cancelled = true;
            }
        }
    }

    @EventTarget
    public void method16035(EventJump event) {
        if (this.isEnabled()) {
            if (this.field23410 == 1) {
                event.cancelled = true;
                this.field23412 = true;
            }
        }
    }

    @EventTarget
    @HigherPriority
    public void method16036(EventUpdateWalkingPlayer var1) {
        if (this.isEnabled()) {
            if (mc.player.isOnGround()) {
                this.field23413 = false;
                if (this.field23412 && this.field23410 != 1) {
                    this.field23412 = !this.field23412;
                    mc.player.jump();
                }

                if (this.getStringSettingValueByName("Offset").equals("OldHypixel") != this.field23411) {
                    this.field23411 = this.getStringSettingValueByName("Offset").equals("OldHypixel");
                    this.field23410 = 2;
                }

                double var4 = this.field23411 ? 1.0E-14 : 0.0;
                boolean var6 = false;
                boolean var7 = mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK;
                boolean var8 = mc.playerController.getIsHittingBlock() || mc.gameSettings.keyBindAttack.isKeyDown() && var7;
                if (!var8 && !Jesus.isWalkingOnLiquid()) {
                    switch (this.field23410) {
                        case 0:
                            var1.setMoving(true);
                            this.field23410--;
                            break;
                        case 1:
                            var4 = 0.065;
                            this.field23410--;
                            break;
                        case 2:
                            var1.setMoving(true);
                            var4 = 0.065;
                            this.field23410--;
                            if (!this.field23411) {
                                var4 = 1.0E-14;
                                this.field23410--;
                            }
                            break;
                        case 3:
                            var1.setMoving(true);
                            var4 = 0.0;
                            var6 = true;
                            this.field23410--;
                    }
                } else {
                    this.field23410 = 2;
                    var6 = true;
                }

                var1.setY(var1.getY() + var4);
                var1.setOnGround(var6);
            } else {
                this.field23410 = this.getBooleanValueFromSettingName("Avoid Fall Damage") && !this.field23411 ? 3 : 0;
                if (this.getBooleanValueFromSettingName("Avoid Fall Damage") && this.field23411 && !this.field23413 && mc.player.getMotion().y < -0.1) {
                    this.field23413 = !this.field23413;
                    var1.setOnGround(true);
                }
            }
        }
    }
}
