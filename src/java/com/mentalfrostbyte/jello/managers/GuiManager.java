package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.event.impl.EventRender;
import com.mentalfrostbyte.jello.gui.base.Bird;
import com.mentalfrostbyte.jello.gui.base.Screen;
import com.mentalfrostbyte.jello.gui.impl.*;
import com.mentalfrostbyte.jello.gui.unmapped.*;
import com.mentalfrostbyte.jello.module.impl.gui.classic.TabGUI;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.FileUtil;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import com.mentalfrostbyte.jello.util.render.Resources;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.EventBus;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class GuiManager {
    public static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, String> screenToScreenName = new HashMap<Class<? extends net.minecraft.client.gui.screen.Screen>, String>();
    private static final Map<Class<? extends net.minecraft.client.gui.screen.Screen>, Class<? extends Screen>> replacementScreens = new HashMap<Class<? extends net.minecraft.client.gui.screen.Screen>, Class<? extends Screen>>();
    public static long arrowCursor;
    public static long pointingHandCursor;
    public static long iBeamCursor;
    public static float scaleFactor = 1.0F;
    private static boolean hidpiCocoa = true;

    static {
        replacementScreens.put(MainMenuScreen.class, JelloMainMenuScreen.class);
        replacementScreens.put(ClickGui.class, JelloClickGUI.class);
        replacementScreens.put(KeyboardScreen.class, JelloKeyboardScreen.class);
        replacementScreens.put(Maps.class, JelloMaps.class);
        replacementScreens.put(Snake.class, SnakeGameScreen.class);
        replacementScreens.put(Bird.class, BirdGameScreen.class);
        replacementScreens.put(SpotLight.class, SearchBar.class);
        replacementScreens.put(InGameOptionsScreen.class, JelloInGameOptions.class);
        replacementScreens.put(CreditToCreatorsScreen.class, CreditsToCreators.class);
        screenToScreenName.put(ClickGui.class, "Click GUI");
        screenToScreenName.put(KeyboardScreen.class, "Keybind Manager");
        screenToScreenName.put(Maps.class, "Jello Maps");
        screenToScreenName.put(Snake.class, "Snake");
        screenToScreenName.put(Bird.class, "Bird");
        screenToScreenName.put(SpotLight.class, "Spotlight");
    }

    public double field41347;
    public int[] field41354 = new int[2];
    public boolean field41357;
    private final List<Integer> field41339 = new ArrayList<Integer>();
    private final List<Integer> field41340 = new ArrayList<Integer>();
    private final List<Integer> field41341 = new ArrayList<Integer>();
    private final List<Integer> field41342 = new ArrayList<Integer>();
    private final List<Integer> field41343 = new ArrayList<Integer>();
    private boolean guiBlur = true;
    private boolean hqIngameBlur = true;
    private Screen screen;

    public GuiManager() {
        // https://www.glfw.org/docs/3.4/group__shapes.html
        // convert the shape parameters to hex and prepend a few `0`s because the site adds those.
        arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
        pointingHandCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR);
        iBeamCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), arrowCursor);
        scaleFactor = (float) (Minecraft.getInstance().getMainWindow().getFramebufferHeight() / Minecraft.getInstance().getMainWindow().getHeight());
    }

    public static boolean method33457(net.minecraft.client.gui.screen.Screen screen) {
        if (screen instanceof MultiplayerScreen && !(screen instanceof JelloPortalScreen)) {
            Minecraft.getInstance().currentScreen = null;
            Minecraft.getInstance().displayGuiScreen(new JelloPortalScreen(((MultiplayerScreen) screen).parentScreen));
            return true;
        } else if (screen instanceof IngameMenuScreen && !(screen instanceof JelloForSigmaOptions)) {
            Minecraft.getInstance().currentScreen = null;
            Minecraft.getInstance().displayGuiScreen(new JelloForSigmaOptions());
            return true;
        } else if (Client.getInstance().clientMode == ClientMode.NOADDONS && screen instanceof MainMenuScreen && !(screen instanceof NoAddOnnScreenMenu)) {
            Minecraft.getInstance().currentScreen = null;
            Minecraft.getInstance().displayGuiScreen(new NoAddOnnScreenMenu());
            return true;
        } else {
            return false;
        }
    }

    public static Screen handleScreen(net.minecraft.client.gui.screen.Screen screen) {
        if (screen == null) {
            return null;
        } else if (Client.getInstance().clientMode == ClientMode.INDETERMINATE) {
            return new SwitchScreen();
        } else if (method33457(screen)) {
            return null;
        } else if (!replacementScreens.containsKey(screen.getClass())) {
            return null;
        } else {
            try {
                return replacementScreens.get(screen.getClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException var4) {
                var4.printStackTrace();
            }

            return null;
        }
    }

    public static void method33475() {
        Minecraft.getInstance();
        if (Minecraft.IS_RUNNING_ON_MAC) {
            try {
                JSONObject var2 = FileUtil.readFile(new File(Client.getInstance().file + "/config.json"));
                if (var2.has("hidpicocoa")) {
                    hidpiCocoa = var2.getBoolean("hidpicocoa");
                }

                GLFW.glfwWindowHint(143361, hidpiCocoa ? 1 : 0);
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }
    }

    public void method33452() {
        replacementScreens.clear();
        replacementScreens.put(MainMenuScreen.class, ClassicMainScreen.class);
        replacementScreens.put(ClickGui.class, ClassicScreenk.class);
    }

    public void method33453(int var1, int var2) {
        if (var2 == 1 || var2 == 2) {
            this.field41339.add(var1);
        } else if (var2 == 0) {
            this.field41340.add(var1);
        }
    }

    public void method33454(int var1, int var2) {
        this.field41343.add(var1);
    }

    public void method33455(double var1, double var3) {
        this.field41347 += var3;
    }

    public void method33456(int var1, int var2) {
        if (var2 != 1) {
            if (var2 == 0) {
                this.field41342.add(var1);
            }
        } else {
            this.field41341.add(var1);
        }
    }

    public void endTick() {
        if (this.screen != null) {
            this.field41354[0] = Math.max(0, Math.min(Minecraft.getInstance().getMainWindow().getWidth(), (int) Minecraft.getInstance().mouseHelper.getMouseX()));
            this.field41354[1] = Math.max(0, Math.min(Minecraft.getInstance().getMainWindow().getHeight(), (int) Minecraft.getInstance().mouseHelper.getMouseY()));

            for (Integer var4 : this.field41339) {
                this.method33463(var4);
            }

            for (Integer var9 : this.field41340) {
                this.method33461(var9);
            }

            for (Integer var10 : this.field41341) {
                this.method33466(this.field41354[0], this.field41354[1], var10);
            }

            for (Integer var11 : this.field41342) {
                this.method33467(this.field41354[0], this.field41354[1], var11);
            }

            for (Integer var12 : this.field41343) {
                this.method33462((char) var12.intValue());
            }

            this.field41339.clear();
            this.field41340.clear();
            this.field41341.clear();
            this.field41342.clear();
            this.field41343.clear();
            if (this.field41347 == 0.0) {
                this.field41357 = false;
            } else {
                this.method33465((float) this.field41347);
                this.field41347 = 0.0;
                this.field41357 = true;
            }

            if (this.screen != null) {
                this.screen.method13028(this.field41354[0], this.field41354[1]);
            }
        }
    }

    public void method33461(int var1) {
        if (this.screen != null) {
            this.screen.method13103(var1);
        }
    }

    public void method33462(char var1) {
        if (this.screen != null) {
            this.screen.charTyped(var1);
        }
    }

    public void method33463(int var1) {
        if (this.screen != null) {
            this.screen.keyPressed(var1);
        }
    }

    public void renderWatermark() {
        if (Minecraft.getInstance().world != null) {
            GL11.glDisable(2896);
            int var3 = 0;
            int var4 = 0;
            int var5 = 170;

            if (Minecraft.getInstance().gameSettings.showDebugInfo) {
                var3 = Minecraft.getInstance().getMainWindow().getWidth() / 2 - var5 / 2;
            }

            if (Client.getInstance().clientMode != ClientMode.JELLO) {
                float var7 = 0.5F + TabGUI.animationProgress.calcPercent() * 0.5F;
                GL11.glAlphaFunc(516, 0.1F);
                RenderUtil.renderBackgroundBox(4.0F, 2.0F, 106.0F, 28.0F, ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F * var7));
                RenderUtil.drawString(Resources.bold22, 9.0F, 2.0F, "Sigma", ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F * var7));
                RenderUtil.drawString(
                        Resources.bold22, 8.0F, 1.0F, "Sigma", ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), Math.min(1.0F, var7 * 1.2F))
                );
                int var8 = Color.getHSBColor((float) (System.currentTimeMillis() % 4000L) / 4000.0F, 1.0F, 1.0F).getRGB();
                RenderUtil.drawString(Resources.bold14, 73.0F, 2.0F, "5.0.0", ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
                RenderUtil.drawString(Resources.bold14, 72.0F, 1.0F, "5.0.0", ColorUtils.applyAlpha(var8, Math.min(1.0F, var7 * 1.4F)));
            } else {
                if (!(scaleFactor > 1.0F)) {
                    Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("com/mentalfrostbyte/gui/resources/sigma/jello_watermark.png"));
                } else {
                    Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("com/mentalfrostbyte/gui/resources/sigma/jello_watermark@2x.png"));
                }

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                AbstractGui.blit(new MatrixStack(), var3, var4, 0, 0, (int) 170.0F, (int) 104.0F, (int) 170.0F, (int) 104.0F);

                // Reset states
                RenderSystem.disableBlend();
            }

            EventBus.call(new EventRender());
        }

        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.draw(1.0F);
        }
    }

    public void method33465(float var1) {
        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.method13079(var1);
        }
    }

    public void method33466(int var1, int var2, int var3) {
        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.method13078(var1, var2, var3);
        }
    }

    public void method33467(int var1, int var2, int var3) {
        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.method13095(var1, var2, var3);
        }
    }

    public JSONObject getUIConfig(JSONObject uiConfig) {
        if (this.screen != null) {
            JSONObject var4 = this.screen.method13160(new JSONObject());
            if (var4.length() != 0) {
                uiConfig.put(this.screen.getName(), var4);
            }
        }

        uiConfig.put("guiBlur", this.guiBlur);
        uiConfig.put("hqIngameBlur", this.hqIngameBlur);
        uiConfig.put("hidpicocoa", hidpiCocoa);
        return uiConfig;
    }

    public void setGuiBlur(boolean to) {
        this.guiBlur = to;
    }

    public boolean getGuiBlur() {
        return this.guiBlur;
    }

    public void setHqIngameBlur(boolean to) {
        this.hqIngameBlur = to;
    }

    public boolean getHqIngameBlur() {
        return this.hqIngameBlur;
    }

    public void setHidpiCocoa(boolean var1) {
        hidpiCocoa = var1;
    }

    public boolean getHidpiCocoa() {
        return hidpiCocoa;
    }

    public void loadUIConfig(JSONObject uiConfig) {
        if (this.screen != null) {
            JSONObject var4 = null;

            try {
                var4 = Client.getInstance().getConfig().getJSONObject(this.screen.getName());
            } catch (Exception var9) {
                var4 = new JSONObject();
            } finally {
                this.screen.method13161(var4);
            }
        }

        if (uiConfig.has("guiBlur")) {
            this.guiBlur = uiConfig.getBoolean("guiBlur");
        }

        if (uiConfig.has("hqIngameBlur")) {
            this.hqIngameBlur = uiConfig.getBoolean("hqIngameBlur");
        }
    }

    public Class<? extends net.minecraft.client.gui.screen.Screen> method33477(String var1) {
        for (Entry var5 : screenToScreenName.entrySet()) {
            if (var1.equals(var5.getValue())) {
                return (Class<? extends net.minecraft.client.gui.screen.Screen>) var5.getKey();
            }
        }

        return null;
    }

    public String method33478(Class<? extends net.minecraft.client.gui.screen.Screen> screen) {
        if (screen == null) {
            return "";
        } else {
            for (Entry var5 : screenToScreenName.entrySet()) {
                if (screen == var5.getKey()) {
                    return (String) var5.getValue();
                }
            }

            return "";
        }
    }

    public void onResize() throws JSONException {
        if (this.screen != null) {
            this.getUIConfig(Client.getInstance().getConfig());

            try {
                this.screen = this.screen.getClass().newInstance();
            } catch (IllegalAccessException | InstantiationException var4) {
                var4.printStackTrace();
            }

            this.loadUIConfig(Client.getInstance().getConfig());
        }

        if (Minecraft.getInstance().getMainWindow().getWidth() != 0 && Minecraft.getInstance().getMainWindow().getHeight() != 0) {
            scaleFactor = (float) Math.max(
                    Minecraft.getInstance().getMainWindow().getFramebufferWidth() / Minecraft.getInstance().getMainWindow().getWidth(),
                    Minecraft.getInstance().getMainWindow().getFramebufferHeight() / Minecraft.getInstance().getMainWindow().getHeight()
            );
        }
    }

    public Screen getCurrentScreen()  {
        return this.screen;
    }

    public void method33481() throws JSONException {
        this.method33482(handleScreen(Minecraft.getInstance().currentScreen));
    }

    public void method33482(Screen screen) {
        if (this.screen != null) {
            this.getUIConfig(Client.getInstance().getConfig());
        }

        this.screen = screen;
        this.loadUIConfig(Client.getInstance().getConfig());
        if (this.screen != null) {
            this.screen.method13028(this.field41354[0], this.field41354[1]);
        }

        if (Client.getInstance().moduleManager != null) {
            Client.getInstance().moduleManager.getMacOSTouchBar().method13734(null);
        }
    }

    public boolean method33484(net.minecraft.client.gui.screen.Screen screen) {
        return replacementScreens.containsKey(screen.getClass());
    }
}
