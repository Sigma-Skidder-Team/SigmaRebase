package com.mentalfrostbyte.jello.gui.impl.jello.ingame.options;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.panels.MapPanel;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointList extends ScrollableContentPanel {
    private List<Waypoint> field21209 = new ArrayList<Waypoint>();
    public final int field21210 = 70;
    public Animation field21211 = new Animation(300, 300);
    public boolean field21212;
    public Waypoint field21213;

    public WaypointList(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.field21211.changeDirection(Animation.Direction.BACKWARDS);
        this.field20883 = true;
        this.setListening(false);
        this.method13511();
    }

    public void method13511() {
        boolean var3 = false;
    }

    public void addWaypoint(String var1, Vector3i var2, int var3) {
        String var6 = "waypoint x" + var2.getX() + " z" + var2.getZ();
        if (this.buttonList.method13221(var6) == null) {
            Waypoint var7 = new Waypoint(
                    this, var6, this.xA, this.getChildren().get(0).getChildren().size() * this.field21210, this.widthA, this.field21210, var1, var2, var3
            );
            var7.field21288 = var7.getYA();
            this.field21209.add(var7);
            this.addToList(var7);
            var7.doThis((var2x, var3x) -> {
                if (var3x == 1) {
                    Goal goal = new GoalBlock(var7.field21292.getX(), var7.field21292.getY() - 1, var7.field21292.getZ());

                    BaritoneAPI.getProvider()
                            .getPrimaryBaritone()
                            .getCustomGoalProcess()
                            .setGoalAndPath(goal);

                    System.out.println("Baritone is going to: " + var7.field21292.getX() + ", " + var7.field21292.getZ());
                } else {
                    MapPanel var6x = (MapPanel) this.getParent();
                    var6x.field20614.method13077(var7.field21292.getX(), var7.field21292.getZ());
                }
            });
            var7.onPress(
                    var3x -> {
                        Client.getInstance()
                                .waypointsManager
                                .method29993(new Waypoint2(var7.field21291, var7.field21292.getX(), var7.field21292.getZ(), var7.field21293));
                        this.buttonList.method13237(var7);
                        this.field21209.remove(var3x);
                    }
            );
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        this.field21209.sort((var0, var1x) -> var0.field21288 < var1x.field21288 + var0.getHeightA() / 2 ? -1 : 1);
        int var5 = 0;
        if (this.field21213 != null && !this.field21213.method13216() && this.field21212) {
            this.field21213.method13608();
            this.field21213 = null;
            this.field21212 = false;
        }

        for (Waypoint var7 : this.field21209) {
            if (!var7.method13216() && var7.field21290.getDirection() == Animation.Direction.BACKWARDS) {
                var7.field21288 = var5 + 5;
            } else {
                var7.field21288 = var7.getYA();
            }

            var5 += var7.getHeightA();
        }

        for (Waypoint var11 : this.field21209) {
            if (var11.method13216()) {
                this.field21211.changeDirection(Animation.Direction.FORWARDS);
                if (newHeight > this.method13271() + 10
                        && newHeight < this.method13271() + 50
                        && newWidth < this.method13272() + this.getHeightA() - 10
                        && newWidth > this.method13272() + this.getHeightA() - 50) {
                    this.field21212 = true;
                    this.field21213 = var11;
                } else {
                    this.field21212 = false;
                    this.field21213 = null;
                }
                break;
            }

            if (!var11.method13216() && this.field21211.getDirection() == Animation.Direction.FORWARDS) {
                Client.getInstance().waypointsManager.getWaypoints().clear();

                for (Waypoint var9 : this.field21209) {
                    Client.getInstance()
                            .waypointsManager
                            .getWaypoints()
                            .add(new Waypoint2(var9.field21291, var9.field21292.getX(), var9.field21292.getZ(), var9.field21293));
                }

                Collections.reverse(Client.getInstance().waypointsManager.getWaypoints());
                Client.getInstance().waypointsManager.method29991();
            }

            this.field21211.changeDirection(Animation.Direction.BACKWARDS);
        }
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = Math.min(1.0F, 0.21F * (60.0F / (float) Minecraft.getFps()));

        for (Waypoint var6 : this.field21209) {
            if (!var6.method13216()) {
                float var7 = (float) (var6.getYA() - var6.field21288) * var4;
                if (Math.round(var7) == 0 && var7 > 0.0F) {
                    var7 = 1.0F;
                } else if (Math.round(var7) == 0 && var7 < 0.0F) {
                    var7 = -1.0F;
                }

                var6.setYA(Math.round((float) var6.getYA() - var7));
            }
        }

        super.draw(partialTicks);
        int var8 = Math.round(QuadraticEasing.easeInQuad(1.0F - this.field21211.calcPercent(), 0.0F, 1.0F, 1.0F) * 30.0F);
        RenderUtil.drawImage(
                (float) (this.xA - var8 + 18),
                (float) (this.heightA - 46),
                22.0F,
                26.0F,
                Resources.trashcanPNG,
                RenderUtil2.applyAlpha(!this.field21212 ? ClientColors.DEEP_TEAL.getColor() : ClientColors.PALE_YELLOW.getColor(), this.field21211.calcPercent() * 0.5F),
                false
        );
    }
}
