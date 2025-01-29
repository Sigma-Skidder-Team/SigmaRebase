package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.gui.base.EasingFunctions;
import com.mentalfrostbyte.jello.gui.base.QuadraticEasing;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.LowestPriority;

public class Coords extends Module {
    private final Animation animation = new Animation(1500, 1500, Direction.BACKWARDS);
    private double x, y, z;

    public Coords() {
        super(ModuleCategory.GUI, "Coords", "Displays coordinates");
    }

    @EventTarget
    public void onTick(EventPlayerTick tickEvent) {
        if (this.isEnabled()) {
            boolean moved = x != mc.player.getPosX() || y != mc.player.getPosY() || z != mc.player.getPosZ();

            x = mc.player.getPosX();
            y = mc.player.getPosY();
            z = mc.player.getPosZ();
            boolean var4 = moved || (!mc.player.isOnGround()) || mc.player.isSneaking();
            if (!var4) {
                if (this.animation.calcPercent() == 1.0F && this.animation.getDirection() == Direction.FORWARDS) {
                    this.animation.changeDirection(Direction.BACKWARDS);
                }
            } else {
                this.animation.changeDirection(Direction.FORWARDS);
            }
        }
    }

    @EventTarget
    @LowestPriority
    public void onRender(EventRender2DOffset eventRender2DOffset) {
        if (this.isEnabled()) {
            if (mc.player != null) {
                if (!(mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI)) {
                    float animation = Math.min(1.0F, 0.6F + this.animation.calcPercent() * 2.0F);
                    String xyz = String.format("%.2f", mc.player.getPosX())
                            + " "
                            + String.format("%.2f", mc.player.getPosY())
                            + " "
                            + String.format("%.2f", mc.player.getPosZ());
                    float var6 = 85;
                    int var7 = eventRender2DOffset.getyOffset();
                    float var8 = 150;
                    float var9 = (float) ResourceRegistry.JelloLightFont18.getWidth(xyz);
                    float var10 = Math.min(1.0F, (float) var8 / var9);
                    if (this.animation.getDirection() != Direction.FORWARDS) {
                        var10 *= 0.9F + QuadraticEasing.easeInQuad(Math.min(1.0F, this.animation.calcPercent() * 8.0F), 0.0F, 1.0F, 1.0F) * 0.1F;
                    } else {
                        var10 *= 0.9F + EasingFunctions.easeOutBack(Math.min(1.0F, this.animation.calcPercent() * 7.0F), 0.0F, 1.0F, 1.0F) * 0.1F;
                    }

                    GL11.glPushMatrix();
                    GL11.glTranslatef(var6, (float) (var7 + 10), 0.0F);
                    GL11.glScalef(var10, var10, 1.0F);
                    GL11.glTranslatef(-var6, (float) (-var7 - 10), 0.0F);
                    RenderUtil.drawString(
                            ResourceRegistry.JelloLightFont18_1,
                            var6,
                            (float) var7,
                            xyz,
                            ColorUtils.applyAlpha(-16777216, 0.5F * animation),
                            Class2218.field14492,
                            Class2218.field14488
                    );
                    RenderUtil.drawString(
                            ResourceRegistry.JelloLightFont18,
                            var6,
                            (float) var7,
                            xyz,
                            ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F * animation),
                            Class2218.field14492,
                            Class2218.field14488
                    );
                    GL11.glPopMatrix();
                }
            }
        }
    }
}
