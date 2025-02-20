package com.mentalfrostbyte.jello.module.impl.misc;

import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import team.sdhq.eventBus.annotations.EventTarget;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Murderer extends Module {
    public String murdererSkinName = "IBreakerman";
    private Texture murdererSkinHead;
    private boolean initialisedTexture = true;
    private boolean foundMurderer;

    public Murderer() {
        super(ModuleCategory.MISC, "Murderer", "Detects murderer in murder mystery minigame on hypixel");
        this.registerSetting(new BooleanSetting("GUI", "Shows a GUI with info on the murderer", true));
        this.registerSetting(new BooleanSetting("Chat Message", "Sends a message with the murderer's name", true));
    }

    @EventTarget
    public void onReceive(EventReceivePacket event) {
        if (event.packet instanceof SEntityEquipmentPacket entityEquipmentPacket) {
            for (Pair<EquipmentSlotType, ItemStack> pair : entityEquipmentPacket.func_241790_c_()) {
                if (pair.getSecond() != null
                        && pair.getSecond().getItem() instanceof SwordItem
                        && mc.world.getEntityByID(entityEquipmentPacket.getEntityID()) instanceof PlayerEntity) {
                    Entity entity = mc.world.getEntityByID(entityEquipmentPacket.getEntityID());

                    if (!this.murdererSkinName.equalsIgnoreCase(entity.getName().getString())) {
                        if (this.getBooleanValueFromSettingName("Chat Message")) {
                            //mc.player.sendChatMessage("Murderer is " + entity.getName() + ", detected by Jello client");
                        }

                        this.murdererSkinName = entity.getName().getUnformattedComponentText();
                        new Thread(() -> loadMurdererSkinHead(entity.getUniqueID().toString().replaceAll("-", ""))).start();
                        this.initialisedTexture = true;
                        this.foundMurderer = true;
                    }

                    this.murdererSkinName = entity.getName().getUnformattedComponentText();
                }
            }
        }

        if (event.packet instanceof SRespawnPacket) {
            this.foundMurderer = false;
        }
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (this.foundMurderer) {
            if (this.getBooleanValueFromSettingName("GUI")) {
                TrueTypeFont var4 = ResourceRegistry.JelloLightFont20;
                int width = Minecraft.getInstance().getMainWindow().getWidth();
                int height = Minecraft.getInstance().getMainWindow().getHeight();
                if (this.initialisedTexture && this.murdererSkinHead != null) {
                    this.initialisedTexture = false;
                }

                if (this.murdererSkinHead != null) {
                    RenderUtil.drawRoundedRect(
                            (float) (width - var4.getWidth(this.murdererSkinName) - 90), (float) (height - 130), (float) (width - 10), (float) (height - 10), 1342177280
                    );
                    RenderUtil.drawImage((float) (width - var4.getWidth(this.murdererSkinName) - 80), (float) (height - 120), 50.0F, 100.0F, this.murdererSkinHead);
                    RenderUtil.drawString(
                            var4, (float) (width - var4.getWidth(this.murdererSkinName) - 20), (float) (height - var4.getHeight(this.murdererSkinName) - 60), this.murdererSkinName, -1
                    );
                }
            }
        }
    }

    public void loadMurdererSkinHead(String playerUUID) {
        if (this.murdererSkinHead != null && playerUUID.equals(this.murdererSkinName)) {
            return;
        }

        String skinUrl = "https://crafatar.com/avatars/" + playerUUID + "?size=48";

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(skinUrl).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoInput(true);
            connection.connect();

            BufferedImage bufferedImage = ImageIO.read(connection.getInputStream());

            if (bufferedImage != null) {
                InputStream inputStream = new ByteArrayInputStream(convertBufferedImageToBytes(bufferedImage));

                if (GL11.glGetError() == GL11.GL_NO_ERROR) {
                    this.murdererSkinHead = TextureLoader.getTexture("PNG", inputStream, GL11.GL_NEAREST);
                } else {
                    throw new IllegalStateException("No OpenGL context is current.");
                }
            }
        } catch (Exception e) {
            this.murdererSkinHead = null;
            e.printStackTrace();
        }
    }

    private byte[] convertBufferedImageToBytes(BufferedImage image) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}