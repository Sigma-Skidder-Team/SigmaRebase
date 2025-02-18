package com.mentalfrostbyte;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DCustom;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender3D;
import com.mentalfrostbyte.jello.managers.*;
import com.mentalfrostbyte.jello.managers.ModuleManager;
import com.mentalfrostbyte.jello.util.client.ModuleSettingInitializr;
import com.mentalfrostbyte.jello.util.client.network.auth.CloudConfigs;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.game.player.tracker.MinerTracker;
import com.mentalfrostbyte.jello.util.game.player.tracker.SlotChangeTracker;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.util.client.logger.Logger;
import com.mentalfrostbyte.jello.util.game.player.tracker.PlayerStateTracker;
import com.mentalfrostbyte.jello.util.client.logger.ClientLogger;
import com.mentalfrostbyte.jello.util.game.render.BlurEngine;
import com.mentalfrostbyte.jello.util.system.FileUtil;
import org.newdawn.slick.opengl.Texture;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.EventBus;
import totalcross.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Client {

    private static final Minecraft mc = Minecraft.getInstance();
    public static int currentVersionIndex = 28;

    public static final String RELEASE_TARGET = "5.1.0";
    public static final int BETA_ITERATION = 16;
    public static final String FULL_VERSION = RELEASE_TARGET + (BETA_ITERATION > 0 ? "b" + BETA_ITERATION : "");
    public static String NAME = "Jello";
    public static String PROD = "Sigma Production";

    public File file = new File("sigma5");

    public static List<Texture> textureList = new ArrayList<Texture>();

    private static Client instance;
    public ClientMode clientMode = ClientMode.INDETERMINATE;
    public DiscordRichPresence discordRichPresence;
    public FriendManager friendManager;
    public SlotChangeTracker slotChangeTracker;

    private JSONObject config;

    public GuiManager guiManager;
    public ModuleManager moduleManager;
    public NetworkManager networkManager;
    public CombatManager combatManager;
    public ViaManager viaManager;
    public CommandManager commandManager;
    public SoundManager soundManager;
    public AccountManager accountManager;

    public WaypointsManager waypointsManager;
    public NotificationManager notificationManager;
    public MusicManager musicManager;
    public PlayerStateTracker playerTracker;
    public MinerTracker minerTracker;

    private Logger logger;

    public static boolean dontRenderHand = false;
    private boolean loading = true;

    public BlurEngine blurEngine;

    public void start() {
        this.logger = new ClientLogger(System.out, System.out, System.err);
        System.setProperty("java.awt.headless", "false");
        this.logger.info("Initializing...");
        CloudConfigs.start();

        try {
            if (!this.file.exists()) {
                this.file.mkdirs();
            }

            this.config = FileUtil.readFile(new File(this.file + "/config.json"));
        } catch (IOException var8) {
            var8.printStackTrace();
        }

        this.networkManager = new NetworkManager();
        this.networkManager.init();
        this.guiManager = new GuiManager();
        this.combatManager = new CombatManager();
        this.combatManager.init();
        this.viaManager = new ViaManager();
        this.viaManager.init();
        this.commandManager = new CommandManager();
        this.commandManager.init();
        this.friendManager = new FriendManager();
        this.friendManager.init();
        this.musicManager = new MusicManager();
        this.musicManager.init();
        this.soundManager = new SoundManager();
        this.soundManager.init();
        this.notificationManager = new NotificationManager();
        this.notificationManager.init();
        this.accountManager = new AccountManager();
        this.accountManager.registerEvents();
        this.playerTracker = new PlayerStateTracker();
        this.playerTracker.init();
        this.waypointsManager = new WaypointsManager();
        this.waypointsManager.init();
        this.blurEngine = new BlurEngine();
        this.blurEngine.init();
        this.minerTracker = new MinerTracker();
        this.minerTracker.init();
        GLFW.glfwSetWindowTitle(mc.getMainWindow().getHandle(), "Sigma " + RELEASE_TARGET);
        this.logger.info("Initialized.");
    }

    public void shutdown() {
        this.logger.info("Shutting down...");

        try {
            if (this.guiManager != null) {
                this.guiManager.getUIConfig(this.config);
            }

            if (this.moduleManager != null) {
                this.moduleManager.method14660(this.config);
            }

            FileUtil.save(this.config, new File(this.file + "/config.json"));
        } catch (IOException var4) {
            this.logger.error("Unable to shutdown correctly. Config may be corrupt?");
            var4.printStackTrace();
        }

        this.logger.info("Done.");
    }

    public void endTick() {
        this.guiManager.endTick();
    }

    public void method19926() {
        GL11.glPushMatrix();
        double var3 = mc.getMainWindow().getGuiScaleFactor() / (double) ((float) Math.pow(mc.getMainWindow().getGuiScaleFactor(), 2.0));
        GL11.glScaled(var3, var3, var3);
        GL11.glScaled(GuiManager.scaleFactor, GuiManager.scaleFactor, GuiManager.scaleFactor);
        GL11.glDisable(2912);
        RenderSystem.disableDepthTest();
        RenderSystem.translatef(0.0F, 0.0F, 1000.0F);
        RenderSystem.alphaFunc(519, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(2896);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Resources.gingerbreadIconPNG.bind();
        EventBus.call(new EventRender2DCustom());
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.alphaFunc(518, 0.1F);
        GL11.glPopMatrix();
    }

    public void hook3DRenderEvent() {
        if (mc != null && mc.world != null && mc.player != null && !dontRenderHand) {
            GL11.glTranslatef(0.0F, 0.0F, 0.0F);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            GL11.glDisable(2896);
            EventBus.call(new EventRender3D());
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            mc.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        }
    }

    public void addTexture(Texture texture) {
        textureList.add(texture);
    }

    public void renderVisuals() {
        if (!textureList.isEmpty()) {
            try {
                for (Texture texture : textureList) {
                    texture.release();
                }

                textureList.clear();
            } catch (ConcurrentModificationException ignored) {
            }
        }

        if (getInstance().clientMode != ClientMode.NOADDONS) {
            double scaleFactor = mc.getMainWindow().getGuiScaleFactor() / (double) ((float) Math.pow(mc.getMainWindow().getGuiScaleFactor(), 2.0));
            GL11.glScaled(scaleFactor, scaleFactor, 1.0);
            GL11.glScaled(GuiManager.scaleFactor, GuiManager.scaleFactor, 1.0);
            RenderSystem.disableDepthTest();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 1000.0F);
            this.guiManager.renderWatermark();
            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.enableAlphaTest();
            GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);
        }
    }

    public DiscordRichPresence getDRPC() {
        return this.discordRichPresence;
    }

    private Client() {
    }

    public static Client getInstance() {
        return instance != null ? instance : (instance = new Client());
    }

    private void initRPC() {
        DiscordRPC updatePresence = DiscordRPC.INSTANCE;
        String id = "693493612754763907";
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        eventHandlers.ready = e -> logger.info("Discord RPC Ready!");
        updatePresence.Discord_Initialize(id, eventHandlers, true, "var5");
        discordRichPresence = new DiscordRichPresence();
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.state = "Playing Minecraft";
        discordRichPresence.details = "Jello for Sigma";
        discordRichPresence.largeImageKey = "jello";
        updatePresence.Discord_UpdatePresence(discordRichPresence);
    }

    public void setupClient(ClientMode mode) {
        this.clientMode = mode;
        if (mode == ClientMode.CLASSIC) {
            getInstance().guiManager.useClassicReplacementScreens();
            GLFW.glfwSetWindowTitle(mc.getMainWindow().getHandle(), "Classic Sigma " + RELEASE_TARGET);
        } else if (mode == ClientMode.JELLO) {
            this.initRPC();
            GLFW.glfwSetWindowTitle(mc.getMainWindow().getHandle(), "Jello for Sigma " + RELEASE_TARGET);
        }

        if (this.moduleManager == null && ModuleSettingInitializr.thisThread != null) {
            this.moduleManager = new ModuleManager();
            this.moduleManager.register(this.clientMode);
            this.moduleManager.loadProfileFromJSON(this.config);
            this.moduleManager.saveCurrentConfigToJSON(this.config);
        }

        System.gc();
    }

    public void saveClientData() {
        try {
            FileUtil.save(this.config, new File(this.file + "/config.json"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public JSONObject getConfig() {
        return this.config;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
