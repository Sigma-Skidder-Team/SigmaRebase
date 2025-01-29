package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;

import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mojang.datafixers.util.Pair;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import team.sdhq.eventBus.annotations.EventTarget;

public class Murderer extends Module {
    public String field23833 = "IBreakerman";
    private Texture field23834;
    private boolean field23835 = true;
    private boolean field23836;

    public Murderer() {
        super(ModuleCategory.MISC, "Murderer", "Detects murderer in murder mystery minigame on hypixel");
        this.registerSetting(new BooleanSetting("GUI", "Shows a GUI with info on the murderer", true));
        this.registerSetting(new BooleanSetting("Chat Message", "Sends a message with the murderer's name", true));
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.getPacket() instanceof SEntityEquipmentPacket entityEquipmentPacket) {

                for (Pair var6 : entityEquipmentPacket.func_241790_c_()) {
                    if (var6.getSecond() != null
                            && ((ItemStack) var6.getSecond()).getItem() instanceof SwordItem
                            && mc.world.getEntityByID(entityEquipmentPacket.getEntityID()) instanceof PlayerEntity) {
                        Entity var7 = mc.world.getEntityByID(entityEquipmentPacket.getEntityID());
                        if (!this.field23833.equalsIgnoreCase(var7.getName().getString())) {
                            if (this.getBooleanValueFromSettingName("Chat Message")) {
                                mc.player.sendChatMessage("Murderer is " + var7.getName() + ", detected by Jello client");
                            }

                            this.field23833 = var7.getName().getUnformattedComponentText();
                            this.field23835 = true;
                            this.field23836 = true;
                        }

                        this.field23833 = var7.getName().getUnformattedComponentText();
                    }
                }
            }

            if (event.getPacket() instanceof SRespawnPacket) {
                this.field23836 = false;
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (this.isEnabled()) {
            if (this.field23836) {
                if (this.getBooleanValueFromSettingName("GUI")) {
                    TrueTypeFont var4 = ResourceRegistry.JelloLightFont20;
                    int width = Minecraft.getInstance().getMainWindow().getWidth();
                    int height = Minecraft.getInstance().getMainWindow().getHeight();
                    if (this.field23835 && this.field23834 != null) {
                        this.field23835 = false;
                    }

                    if (this.field23834 != null) {
                        RenderUtil.drawRoundedRect(
                                (float) (width - var4.getWidth(this.field23833) - 90), (float) (height - 130), (float) (width - 10), (float) (height - 10), 1342177280
                        );
                        RenderUtil.drawImage((float) (width - var4.getWidth(this.field23833) - 80), (float) (height - 120), 50.0F, 100.0F, this.field23834);
                        RenderUtil.drawString(
                                var4, (float) (width - var4.getWidth(this.field23833) - 20), (float) (height - var4.getHeight(this.field23833) - 60), this.field23833, -1
                        );
                    }
                }
            }
        }
    }
}