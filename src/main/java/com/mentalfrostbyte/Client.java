package com.mentalfrostbyte;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.mentalfrostbyte.jello.event.impl.EventRender2D;
import com.mentalfrostbyte.jello.event.impl.EventWriter;
import com.mentalfrostbyte.jello.event.impl.Render3DEvent;
import com.mentalfrostbyte.jello.managers.*;
import com.mentalfrostbyte.jello.trackers.RandomModuleThread;
import com.mentalfrostbyte.jello.util.ClientLogger;
import com.mentalfrostbyte.jello.util.FileUtil;
import net.minecraft.client.texture.TextureManager;
import org.newdawn.slick.opengl.Texture;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
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

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static int currentVersionIndex = 28;

    public static final String VERSION = "5.0.0b15";
    public static String NAME = "Jello";
    public static String PROD = "Sigma Production";

    public File file = new File("sigma5");

    public static List<Texture> textureList = new ArrayList<Texture>();

    private static Client instance;
    public ClientMode clientMode = ClientMode.INDETERMINATE;
    public DiscordRichPresence discordRichPresence;
    public FriendManager friendManager;

    private JSONObject config;

    public GuiManager guiManager;
    public ModuleManager moduleManager;
    public NetworkManager networkManager;
    public CombatManager combatManager;
    public CommandManager commandManager;
    public SoundManager soundManager;
    public AccountManager accountManager;
    public WaypointsManager waypointsManager;
    public NotificationManager notificationManager;
    public MusicManager musicManager;
    private Logger logger;

    public static boolean dontRenderHand = false;
    private boolean field28968 = true;

    public void start() {
        this.logger = new ClientLogger(System.out, System.out, System.err);
        this.logger.info("Initializing...");

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
        this.waypointsManager = new WaypointsManager();
        this.waypointsManager.init();
        GLFW.glfwSetWindowTitle(mc.getWindow().getHandle(), "Sigma 5.1");
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

            EventWriter var3 = new EventWriter(this.config);
            EventBus.call(var3);

            FileUtil.save(var3.getFile(), new File(this.file + "/config.json"));
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
        double var3 = mc.getWindow().getScaleFactor()
                / (double) ((float) Math.pow(mc.getWindow().getScaleFactor(), 2.0));
        GL11.glScaled(var3, var3, var3);
        GL11.glScaled(GuiManager.scaleFactor, GuiManager.scaleFactor, GuiManager.scaleFactor);
        GL11.glDisable(2912);
        RenderSystem.disableDepthTest();
        RenderSystem.translatef(0.0F, 0.0F, 1000.0F);
        RenderSystem.alphaFunc(519, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(2896);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        EventBus.call(new EventRender2D());
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
            EventBus.call(new Render3DEvent());
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            mc.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);
        }
    }

    public void method19927(Texture var1) {
        textureList.add(var1);
    }

    public void method19928() {
        if (!textureList.isEmpty()) {
            try {
                for (Texture var4 : textureList) {
                    var4.release();
                }

                textureList.clear();
            } catch (ConcurrentModificationException var7) {
            }
        }

        if (getInstance().clientMode != ClientMode.NOADDONS) {
            double var5 = mc.getWindow().getScaleFactor()
                    / (double) ((float) Math.pow(mc.getWindow().getScaleFactor(), 2.0));
            GL11.glScaled(var5, var5, 1.0);
            GL11.glScaled(GuiManager.scaleFactor, GuiManager.scaleFactor, 1.0);
            RenderSystem.disableDepthTest();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 1000.0F);
            this.guiManager.renderWatermark();
            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.enableAlphaTest();
            GL11.glAlphaFunc(518, 0.1F);
            TextureManager var10000 = mc.getTextureManager();
            mc.getTextureManager();
            var10000.bindTexture(TextureManager.MISSING_IDENTIFIER);
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
        if (mode != ClientMode.CLASSIC) {
            if (mode == ClientMode.JELLO) {
                this.initRPC();
                GLFW.glfwSetWindowTitle(mc.getWindow().getHandle(), "Jello for Sigma 5.1");
            }
        } else {
            getInstance().guiManager.method33452();
            GLFW.glfwSetWindowTitle(mc.getWindow().getHandle(), "Classic Sigma 5.1");
        }

        if (this.moduleManager == null && RandomModuleThread.field8341 != null) {
            this.moduleManager = new ModuleManager();
            this.moduleManager.register(this.clientMode);
            this.moduleManager.method14659(this.config);
            this.moduleManager.saveCurrentConfigToJSON(this.config);
        }
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

    public boolean method19930() {
        return this.field28968;
    }

    public void method19931(boolean var1) {
        this.field28968 = var1;
    }
}
