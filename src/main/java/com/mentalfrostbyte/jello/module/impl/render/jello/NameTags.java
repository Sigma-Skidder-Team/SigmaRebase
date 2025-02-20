package com.mentalfrostbyte.jello.module.impl.render.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.network.EventReceivePacket;
import com.mentalfrostbyte.jello.event.impl.game.network.EventSendPacket;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRenderNameTag;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.impl.player.Blink;
import com.mentalfrostbyte.jello.module.impl.render.Freecam;
import com.mentalfrostbyte.jello.module.impl.render.NameProtect;
import com.mentalfrostbyte.jello.module.impl.render.jello.esp.util.Class8781;
import com.mentalfrostbyte.jello.module.impl.render.jello.nametags.FurnaceTracker;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.player.PlayerUtil;
import com.mentalfrostbyte.jello.util.game.player.combat.CombatUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.world.PositionUtil;
import com.mentalfrostbyte.jello.util.game.world.blocks.BlockUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.gui.screen.inventory.FurnaceScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.play.server.SWindowPropertyPacket;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import team.sdhq.eventBus.annotations.EventTarget;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class NameTags extends Module {
    public static final HashMap<String, Texture> field24003 = new HashMap<>();

    static {
        field24003.put("Tomygaims", Resources.tomyPNG);
        field24003.put("Andro24", Resources.androPNG);
        field24003.put("Gretorm", Resources.lpPNG);
        field24003.put("Flyinqq", Resources.codyPNG);
        field24003.put("cxbot", Resources.cxPNG);
    }

    public int backgroundColor = RenderUtil.applyAlpha(RenderUtil
            .method17690(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 75.0F), 0.5F);
    public final HashMap<BlockPos, FurnaceTracker> furnaceTrackers = new HashMap<>();
    public BlockPos currentBlockPos;
    public final List<Entity> entities = new ArrayList<>();
    public boolean trackFurnaces = false;
    public final HashMap<UUID, String> field24007 = new HashMap<>();

    public NameTags() {
        super(ModuleCategory.RENDER, "NameTags", "Render better name tags");
        this.registerSetting(new BooleanSetting("Magnify", "Scales nametags to keep them readable", true));
        this.registerSetting(new BooleanSetting("Furnaces", "Shows furnaces info once open", true));
        this.registerSetting(new BooleanSetting("Mob Owners", "Shows mob owners", true));
        this.setAvailableOnClassic(false);
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            this.trackFurnaces = this.getBooleanValueFromSettingName("Furnaces");
            if (!this.trackFurnaces) {
                this.furnaceTrackers.clear();
            } else {
                Iterator<Entry<BlockPos, FurnaceTracker>> iterator = this.furnaceTrackers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<BlockPos, FurnaceTracker> entry = iterator.next();

                    // Remove furnace tracker if the block at its position is no longer a furnace
                    if (!(mc.world.getBlockState(entry.getKey()).getBlock() instanceof FurnaceBlock)) {
                        iterator.remove();
                    }

                    // Update the smelting progress for the furnace
                    entry.getValue().updateSmelting();
                }
            }

            this.entities.clear();

            for (Entity var7 : BlockUtil.method34549(CombatUtil.getAllPlayersInWorld())) {
                if (var7 != mc.player
                        && var7 != Freecam.player
                        && var7 != Blink.clientPlayerEntity
                        && !var7.isInvisible()
                        && !Client.getInstance().combatManager.isTargetABot(var7)) {
                    this.entities.add(var7);
                }
            }
        }
    }

    @EventTarget
    public void onSendPacket(EventSendPacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof CPlayerTryUseItemOnBlockPacket) {
                CPlayerTryUseItemOnBlockPacket var4 = (CPlayerTryUseItemOnBlockPacket) event.packet;
                if (mc.world.getBlockState(var4.func_218794_c().getPos()).getBlock() instanceof FurnaceBlock) {
                    this.currentBlockPos = var4.func_218794_c().getPos();
                }
            }

            if (event.packet instanceof CClickWindowPacket clickWindowPacket) {
                FurnaceTracker var5 = this.getFurnaceTrackerByWindowId(clickWindowPacket.getWindowId());
                if (var5 == null) {
                    return;
                }

                if (mc.currentScreen instanceof FurnaceScreen furnace) {
                    var5.inputStack = furnace.getContainer().getSlot(0).getStack();
                    var5.fuelStack = new ItemStack(furnace.getContainer().getSlot(1).getStack().getItem());
                    var5.fuelStack.count = furnace.getContainer().getSlot(1).getStack().count;
                    var5.outputStack = furnace.getContainer().getSlot(2).getStack();
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (this.isEnabled()) {
            if (event.packet instanceof SOpenWindowPacket sOpenWindowPacket) {
                if (sOpenWindowPacket.getContainerType() != ContainerType.FURNACE) {
                    return;
                }

                this.furnaceTrackers.put(this.currentBlockPos, new FurnaceTracker(sOpenWindowPacket.getWindowId()));
            }

            if (event.packet instanceof SSetSlotPacket sSetSlotPacket) {
                FurnaceTracker var5 = this.getFurnaceTrackerByWindowId(sSetSlotPacket.getWindowId());
                if (var5 == null) {
                    return;
                }

                if (sSetSlotPacket.getSlot() == 0) {
                    var5.inputStack = new ItemStack(sSetSlotPacket.getStack().getItem());
                    var5.inputStack.count = sSetSlotPacket.getStack().count;
                } else if (sSetSlotPacket.getSlot() == 1) {
                    var5.fuelStack = new ItemStack(sSetSlotPacket.getStack().getItem());
                    var5.fuelStack.count = sSetSlotPacket.getStack().count;
                } else if (sSetSlotPacket.getSlot() == 2) {
                    var5.outputStack = new ItemStack(sSetSlotPacket.getStack().getItem());
                    var5.outputStack.count = sSetSlotPacket.getStack().count;
                }
            }

            if (event.packet instanceof SWindowPropertyPacket sWindowPropertyPacket) {
                FurnaceTracker var8 = this.getFurnaceTrackerByWindowId(sWindowPropertyPacket.getWindowId());
                if (var8 == null) {
                    return;
                }

                switch (sWindowPropertyPacket.getProperty()) {
                    case 0:
                        var8.smeltDelay = sWindowPropertyPacket.getValue();
                        break;
                    case 1:
                        var8.cooldown = sWindowPropertyPacket.getValue();
                        break;
                    case 2:
                        var8.smeltTime = (float) sWindowPropertyPacket.getValue();
                        break;
                    case 3:
                        var8.smeltProgress = (float) sWindowPropertyPacket.getValue();
                }
            }
        }
    }

    public FurnaceTracker getFurnaceTrackerByWindowId(int windowId) {
        for (Entry<BlockPos, FurnaceTracker> entry : this.furnaceTrackers.entrySet()) {
            if (entry.getValue().windowId == windowId) {
                return entry.getValue();
            }
        }
        return null;
    }

    @EventTarget
    public void on3D(EventRender3D event) {
        if (this.isEnabled()) {
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            boolean shouldMagnify = this.getBooleanValueFromSettingName("Magnify");

            for (Entity entity : this.entities) {
                float scale = 1.0F;
                if (shouldMagnify) {
                    scale = (float) Math.max(1.0, Math.sqrt(PositionUtil.calculateDistanceSquared(entity) / 30.0));
                }

                this.drawNametag(
                        PositionUtil.getEntityPosition(entity).x,
                        PositionUtil.getEntityPosition(entity).y + (double) entity.getHeight(),
                        PositionUtil.getEntityPosition(entity).z,
                        entity,
                        scale,
                        null);
                entity.getDataManager().set(Entity.CUSTOM_NAME_VISIBLE, false);
            }

            for (Entry<BlockPos, FurnaceTracker> entry : this.furnaceTrackers.entrySet()) {
                float scale = 1.0F;
                if (shouldMagnify) {
                    scale = (float) Math.max(0.8F,
                            Math.sqrt(PositionUtil.calculateDistanceSquared(entry.getKey()) / 30.0));
                }

                this.drawFurnaceNametag(entry.getKey(), entry.getValue(), scale);
            }

            if (this.getBooleanValueFromSettingName("Mob Owners")) {
                for (Entity entity : mc.world.getAllEntities()) {
                    if (entity instanceof TameableEntity || entity instanceof HorseEntity) {
                        UUID uuid = (entity instanceof TameableEntity)
                                ? ((TameableEntity) entity).getOwnerId()
                                : ((HorseEntity) entity).getOwnerUniqueId();
                        if (uuid != null) {
                            if (!this.field24007.containsKey(uuid)) {
                                this.field24007.put(uuid, null);

                                new Thread(() -> {
                                    try {
                                        List<String> var4x = PlayerUtil.getMobOwners(uuid.toString());
                                        if (var4x == null || var4x.isEmpty()) {
                                            return;
                                        }
                                        this.field24007.put(uuid, var4x.get(var4x.size() - 1));
                                    } catch (Exception ignored) {
                                    }
                                }).start();
                            }

                            if (this.field24007.get(uuid) != null) {
                                float scale = 1.0F;
                                if (this.getBooleanValueFromSettingName("Magnify")) {
                                    scale = (float) Math.max(1.0,
                                            Math.sqrt(PositionUtil.calculateDistanceSquared(entity) / 30.0));
                                }

                                this.drawNametag(
                                        PositionUtil.getEntityPosition(entity).x,
                                        PositionUtil.getEntityPosition(entity).y + (double) entity.getHeight(),
                                        PositionUtil.getEntityPosition(entity).z,
                                        entity,
                                        scale,
                                        this.field24007.get(uuid));
                                entity.getDataManager().set(Entity.CUSTOM_NAME_VISIBLE, false);
                            }
                        }
                    }
                }
            }

            GL11.glDisable(2896);
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        }
    }

    public void drawFurnaceNametag(BlockPos furnacePos, FurnaceTracker furnace, float partialTicks) {
        TrueTypeFont font = ResourceRegistry.JelloLightFont25;

        float renderX = (float) ((double) furnacePos.getX() - mc.gameRenderer.getActiveRenderInfo().getPos().getX() + 0.5);
        float renderY = (float) ((double) furnacePos.getY() - mc.gameRenderer.getActiveRenderInfo().getPos().getY() + 1.0);
        float renderZ = (float) ((double) furnacePos.getZ() - mc.gameRenderer.getActiveRenderInfo().getPos().getZ() + 0.5);

        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        float smeltingProgress = Math.min(furnace.smeltTime / furnace.smeltProgress, 1.0F);
        float cooldownProgress = Math.min((float) furnace.smeltDelay / (float) furnace.cooldown, 1.0F);
        int padding = 14;
        GL11.glPushMatrix();
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glTranslated(renderX, renderY + 0.6F - 0.33333334F * (1.0F - partialTicks), renderZ);
        GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getPitch(), 1.0F, 0.0F, 0.0F);
        GL11.glPushMatrix();

        float scale = 0.008F;
        GL11.glScalef(-scale * partialTicks, -scale * partialTicks, -scale * partialTicks);

        int nameplateWidth;
        ItemStack outputItem = furnace.refreshOutput();
        if (outputItem != null) {
            nameplateWidth = Math.max(ResourceRegistry.JelloLightFont20.getWidth(outputItem.getDisplayName().getString()), 50);
        } else {
            nameplateWidth = 37;
        }

        int boxWidth = 51 + nameplateWidth + padding * 2;
        int boxHeight = 85 + padding * 2;

        GL11.glTranslated(-boxWidth / 2, -boxHeight / 2, 0.0);

        RenderUtil.drawRect(0.0F, 0.0F, (float) boxWidth, (float) boxHeight, this.backgroundColor);
        RenderUtil.drawRoundedRect(0.0F, 0.0F, (float) boxWidth, (float) boxHeight, 20.0F, 0.5F);

        RenderUtil.drawString(font, padding, (float) (padding - 5), "Furnace", ClientColors.LIGHT_GREYISH_BLUE.getColor());
        if (outputItem == null) {
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont20, (float) (padding + 15), (float) (padding + 40), "Empty",
                    RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));
        }

        ItemStack itemStack = furnace.refreshOutput();
        if (itemStack != null) {
            RenderUtil.drawItem(itemStack, padding, padding + 27, 45, 45);
            RenderUtil.drawString(ResourceRegistry.JelloLightFont20, (float) (padding + 51), 40.0F,
                    itemStack.getDisplayName().getString(), ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtil.drawString(ResourceRegistry.JelloLightFont14, (float) (padding + 51), 62.0F,
                    "Count: " + itemStack.count, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        RenderUtil.drawRect(0.0F, (float) boxHeight - 12.0F, Math.min((float) boxWidth * cooldownProgress, (float) boxWidth),
                (float) boxHeight - 6.0F, RenderUtil.applyAlpha(-106750, 0.3F));
        RenderUtil.drawRect(
                0.0F, (float) boxHeight - 6.0F, Math.min((float) boxWidth * smeltingProgress, (float) boxWidth), (float) boxHeight,
                RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.75F));
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawNametag(double x, double y, double z, Entity var7, float var8, String var9) {
        TrueTypeFont var12 = ResourceRegistry.JelloLightFont25;
        String var13 = var9 == null ? var7.getName().getString().replaceAll("§.", "") : var9;
        if (Client.getInstance().moduleManager.getModuleByClass(NameProtect.class).isEnabled()
                && var13.equals(mc.getSession().getUsername())) {
            var13 = Client.getInstance().moduleManager.getModuleByClass(NameProtect.class)
                    .getStringSettingValueByName("Username");
        }

        if (var13.length() != 0) {
            float var14 = (float) (x - mc.gameRenderer.getActiveRenderInfo().getPos().getX());
            float var15 = (float) (y - mc.gameRenderer.getActiveRenderInfo().getPos().getY());
            float var16 = (float) (z - mc.gameRenderer.getActiveRenderInfo().getPos().getZ());
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDisable(2896);
            GL11.glDepthMask(false);
            String var17 = (float) Math.round(((LivingEntity) var7).getHealth() * 10.0F) / 10.0F + "";
            float var18 = Math.min(((LivingEntity) var7).getHealth() / ((LivingEntity) var7).getMaxHealth(), 1.0F);
            GL11.glPushMatrix();
            GL11.glAlphaFunc(519, 0.0F);
            GL11.glTranslated(var14, var15 + 0.6F - 0.33333334F * (1.0F - var8), var16);
            GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.0F, -1.0F, 0.0F);
            GL11.glRotatef(mc.gameRenderer.getActiveRenderInfo().getPitch(), 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-0.009F * var8, -0.009F * var8, -0.009F * var8);
            int var19 = this.backgroundColor;
            if (!Client.getInstance().friendManager.method26997(var7)) {
                if (Client.getInstance().friendManager.isFriend(var7)) {
                    var19 = RenderUtil.applyAlpha(-6750208, 0.5F);
                }
            } else {
                var19 = RenderUtil.applyAlpha(-16171506, 0.5F);
            }

            int var20 = RenderUtil
                    .applyAlpha(!(var7 instanceof PlayerEntity) ? ClientColors.LIGHT_GREYISH_BLUE.getColor()
                            : new Color(Class8781.method31663((PlayerEntity) var7)).getRGB(), 0.5F);
            int var21 = var12.getWidth(var13) / 2;
            if (!field24003.containsKey(var13)) {
                RenderUtil.drawRoundedRect((float) (-var21 - 10), -25.0F, (float) (var21 * 2 + 20),
                        (float) (var12.getHeight() + 27), 20.0F, 0.5F);
            } else {
                int var22 = Color.getHSBColor((float) (System.currentTimeMillis() % 10000L) / 10000.0F, 0.5F, 1.0F)
                        .getRGB();

                RenderUtil.drawImage(
                        (float) (-var21 - 10 - 31),
                        -25.0F, (float) (var12.getHeight() + 27),
                        (float) (var12.getHeight() + 27),
                        field24003.get(var13),
                        RenderUtil.applyAlpha(var22, 0.7f)
                );

                RenderUtil.drawImage((float) (-var21 - 10 - 31 + var12.getHeight() + 27), -25.0F, 14.0F,
                        (float) (var12.getHeight() + 27), Resources.shadowRightPNG,
                        RenderUtil.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));

                RenderUtil.drawRoundedRect((float) (-var21 - 10 - 31), -25.0F, (float) (var21 * 2 + 20 + 31 + 27),
                        (float) (var12.getHeight() + 27), 20.0F, 0.5F);
                GL11.glTranslatef(27.0F, 0.0F, 0.0F);
            }

            RenderUtil.drawRect((float) (-var21 - 10), -25.0F, (float) (var21 + 10), (float) (var12.getHeight() + 2),
                    var19);
            RenderUtil.drawRect((float) (-var21 - 10),
                    (float) (var12.getHeight() - 1) - (float) ((LivingEntity) var7).hurtTime / 3.0F,
                    Math.min((float) (var21 * 2 + 20) * (var18 - 0.5F), (float) (var21 + 10)),
                    (float) (var12.getHeight() + 2), var20);
            GL11.glPushMatrix();
            GL11.glTranslated(-var12.getWidth(var13) / 2, 0.0, 0.0);
            int var26 = ResourceRegistry.JelloLightFont14.getWidth("Health: 20.0");
            String var23 = "Health: ";
            int var24 = var12.getWidth(var13);
            if (var26 > var24) {
                var23 = "H: ";
            }

            RenderUtil.drawString(var12, 0.0F, -20.0F, var13, ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtil.drawString(ResourceRegistry.JelloLightFont14, 0.0F, 10.0F, var23 + var17,
                    ClientColors.LIGHT_GREYISH_BLUE.getColor());
            //        SigmaIRC.Class8433 var25 = Client.getInstance().networkManager.field38429.method29512(var7);
            //       if (var25 != null) {
            //       RenderUtil.drawString(ResourceRegistry.JelloLightFont14, 0.0F, -30.0F, var25.field36141,
            //               ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glEnable(2929);
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }


    @EventTarget
    public void method16934(EventRenderNameTag event) {
        if (this.isEnabled()
                && event.getEntity() instanceof PlayerEntity) {
            event.cancelled = true;
        }
    }
}

