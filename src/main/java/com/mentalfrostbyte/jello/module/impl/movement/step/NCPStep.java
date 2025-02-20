package com.mentalfrostbyte.jello.module.impl.movement.step;

import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventSafeWalk;
import com.mentalfrostbyte.jello.event.impl.game.world.EventLoadWorld;
import com.mentalfrostbyte.jello.event.impl.player.movement.EventStep;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowerPriority;

import java.util.ArrayList;

public class NCPStep extends Module {
    private float field23992;
    private final ArrayList<IPacket<?>> field23993 = new ArrayList<IPacket<?>>();
    private int field23994;

    public NCPStep() {
        super(ModuleCategory.MOVEMENT, "NCP", "Step for NCP");
        this.registerSetting(
                new NumberSetting<Float>("Maximum heigh", "Maximum heigh", 2.0F, Float.class, 1.0F, 2.5F, 0.5F));
        this.registerSetting(new NumberSetting<Float>("Timer", "Timer speed", 0.1F, Float.class, 0.0F, 1.0F, 0.01F));
    }

    @Override
    public void onEnable() {
        this.field23992 = -1.0F;
        this.field23994 = 0;
    }

    @EventTarget
    public void onWorldLoad(EventLoadWorld var1) {
        this.field23994 = 0;
        this.field23993.clear();
    }

    @EventTarget
    @LowerPriority
    public void onStep(EventStep var1) {
        if (this.isEnabled() && !var1.cancelled) {
            double var4 = var1.getHeight();
            if (BlockUtil.isAboveBounds(mc.player, 1.0E-4F) && !mc.player.isInWater()) {
                if (var4 >= 0.625) {
                    double var6 = mc.player.getPosX();
                    double var8 = mc.player.getPosY();
                    double var10 = mc.player.getPosZ();
                    this.field23994 = 1;
                    if (!(var4 < 1.1)) {
                        if (!(var4 < 1.6)) {
                            if (!(var4 < 2.1)) {
                                double[] var21 = new double[] { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869,
                                        2.019, 1.907 };
                                float var24 = 1.0F / (float) (var21.length + 1);
                                this.field23992 = var24 + (1.0F - var24) * this.getNumberValueBySettingName("Timer");

                                for (double var36 : var21) {
                                    this.field23993
                                            .add(new CPlayerPacket.PositionPacket(var6, var8 + var36, var10, false));
                                }
                            } else {
                                double[] var20 = new double[] { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652,
                                        1.869 };
                                float var23 = 1.0F / (float) (var20.length + 1);
                                this.field23992 = var23 + (1.0F - var23) * this.getNumberValueBySettingName("Timer");

                                for (double var35 : var20) {
                                    if (var35 - 0.027 <= var4) {
                                        this.field23993.add(
                                                new CPlayerPacket.PositionPacket(var6, var8 + var35, var10, false));
                                    }
                                }
                            }
                        } else {
                            double[] var19 = new double[] { 0.41999998, 0.7531999805212, 1.01, 1.093, 1.015 };
                            float var22 = 1.0F / (float) (var19.length + 1);
                            this.field23992 = var22 + (1.0F - var22) * this.getNumberValueBySettingName("Timer");

                            for (double var34 : var19) {
                                this.field23993.add(new CPlayerPacket.PositionPacket(var6, var8 + var34, var10, false));
                            }
                        }
                    } else {
                        double[] var12 = new double[] { 0.41999998688698 * var4, 0.7531999805212 * var4 };
                        float var13 = 1.0F / (float) (var12.length + 1);
                        this.field23992 = var13 + (1.0F - var13) * this.getNumberValueBySettingName("Timer");

                        for (double var17 : var12) {
                            this.field23993.add(new CPlayerPacket.PositionPacket(var6, var8 + var17, var10, false));
                        }
                    }

                    mc.timer.timerSpeed = this.field23992;
                }
            } else {
                var1.cancelled = true;
            }
        }
    }

    @EventTarget
    public void onSafeWalk(EventSafeWalk var1) {
        if (this.isEnabled() && mc.player != null) {
            if (this.field23994 > 0) {
                this.field23994--;
            }

            if (!var1.isOnEdge()) {
                mc.player.stepHeight = this.getNumberValueBySettingName("Maximum heigh");
            } else {
                mc.player.stepHeight = 0.5F;
                if (this.field23992 != -1.0F) {
                    this.field23992 = -1.0F;
                    mc.timer.timerSpeed = 1.0F;
                }
            }
        }
    }

    @EventTarget
    public void onSendPacketEvent(EventSendPacket var1) {
        if (var1.packet instanceof CPlayerPacket && !this.field23993.isEmpty()) {
            this.field23993.add(var1.packet);
            var1.cancelled = true;
            if (this.field23994 == 0) {
                for (IPacket var5 : this.field23993) {
                    mc.getConnection().getNetworkManager().sendNoEventPacket(var5);
                }

                this.field23993.clear();
            }
        }
    }
}
