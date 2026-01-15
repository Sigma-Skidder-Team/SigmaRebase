package com.mentalfrostbyte;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.google.gson.JsonObject;
import com.mentalfrostbyte.jello.managers.*;
import com.mentalfrostbyte.jello.managers.ModuleManager;
import com.mentalfrostbyte.jello.util.game.MinecraftUtil;
import com.mentalfrostbyte.jello.util.game.player.rotation.JelloAI;
import com.mentalfrostbyte.jello.util.game.player.tracker.MinerTracker;
import com.mentalfrostbyte.jello.util.game.player.tracker.SlotChangeTracker;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.util.game.player.tracker.PlayerStateTracker;
import com.mentalfrostbyte.jello.util.game.render.BlurEngine;
import com.mentalfrostbyte.jello.util.system.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.opengl.Texture;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Client implements MinecraftUtil {
    public static int currentVersionIndex = 28;
    public static final Logger logger = LogManager.getLogger("Jello");

    public static final String RELEASE_TARGET = "5.1.0";
    public static final int BETA_ITERATION = 16;
    public static final String FULL_VERSION = RELEASE_TARGET + (BETA_ITERATION > 0 ? "b" + BETA_ITERATION : "");

    public File file = new File("sigma5");

    public static List<Texture> textureList = new ArrayList<>();

    private static Client instance;
    public ClientMode clientMode = ClientMode.INDETERMINATE;
    public DiscordRichPresence discordRichPresence;
    public FriendManager friendManager;
    public SlotChangeTracker slotChangeTracker;

    public JsonObject config;

    public GuiManager guiManager;
    public ModuleManager moduleManager;
    public BotManager botManager;
    public ViaManager viaManager;
    public CommandManager commandManager;
    public SoundManager soundManager;
    public AccountManager accountManager;

    public WaypointsManager waypointsManager;
    public NotificationManager notificationManager;
    public MusicManager musicManager;
    public PlayerStateTracker playerTracker;
    public MinerTracker minerTracker;

    public static boolean dontRenderHand = false;
    public boolean loading = true;

    public BlurEngine blurEngine;

    public void start() {
        logger.info("Initializing...");

        try {
            if (!file.exists()) {
                file.mkdirs();
            }

            config = FileUtil.readFile(new File(file + "/config.json"));
        } catch (IOException exception) {
            logger.error(exception);
        }

        JelloAI.init();
        guiManager = new GuiManager();
        botManager = new BotManager();
        botManager.init();
        viaManager = new ViaManager();
        viaManager.init();
        commandManager = new CommandManager();
        commandManager.init();
        friendManager = new FriendManager();
        friendManager.init();
        musicManager = new MusicManager();
        musicManager.init();
        soundManager = new SoundManager();
        soundManager.init();
        notificationManager = new NotificationManager();
        notificationManager.init();
        accountManager = new AccountManager();
        accountManager.init();
        playerTracker = new PlayerStateTracker();
        playerTracker.init();
        waypointsManager = new WaypointsManager();
        waypointsManager.init();
        blurEngine = new BlurEngine();
        blurEngine.init();
        minerTracker = new MinerTracker();
        minerTracker.init();
        GLFW.glfwSetWindowTitle(mc.getMainWindow().getHandle(), "Sigma " + RELEASE_TARGET);
        logger.info("Initialized.");
    }

    public void shutdown() {
        logger.info("Shutting down...");

        try {
            if (guiManager != null) {
                guiManager.getUIConfig(config);
            }

            if (moduleManager != null) {
                moduleManager.method14660(config);
            }

            FileUtil.save(config, new File(file + "/config.json"));
        } catch (IOException exc) {
            logger.error("Unable to shutdown correctly. Config may be corrupt?", exc);
        }

        logger.info("Done.");
    }

    public void addTexture(Texture texture) {
        textureList.add(texture);
    }

    public static Client getInstance() {
        return instance != null ? instance : (instance = new Client());
    }

    private void initRPC() {
        DiscordRPC updatePresence = DiscordRPC.INSTANCE;
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        eventHandlers.ready = e -> logger.info("Discord RPC Ready!");
        updatePresence.Discord_Initialize("693493612754763907", eventHandlers, true, "var5");
        discordRichPresence = new DiscordRichPresence();
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.state = "Playing Minecraft";
        discordRichPresence.details = "Jello for Sigma";
        discordRichPresence.largeImageKey = "jello";
        updatePresence.Discord_UpdatePresence(discordRichPresence);
    }

    public void setupClient(ClientMode mode) {
        clientMode = mode;
        moduleManager = new ModuleManager();
        moduleManager.reset();

        if (mode == ClientMode.CLASSIC) {
            guiManager.useClassic();
            DiscordRPC.INSTANCE.Discord_ClearPresence();
            GLFW.glfwSetWindowTitle(mc.getMainWindow().getHandle(), "Classic Sigma " + RELEASE_TARGET);
        } else if (mode == ClientMode.JELLO) {
            guiManager.useJello();
            initRPC();
            GLFW.glfwSetWindowTitle(mc.getMainWindow().getHandle(), "Jello for Sigma " + RELEASE_TARGET);
        }

        moduleManager.register(clientMode);
        moduleManager.loadProfileFromJSON(config);
        moduleManager.loadCurrentConfig(config);

        System.gc();
    }

    public void saveClientData() {
        try {
            FileUtil.save(config, new File(file + "/config.json"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}