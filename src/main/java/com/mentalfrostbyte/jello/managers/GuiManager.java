package com.mentalfrostbyte.jello.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.client.ClientMode;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.holders.*;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.impl.classic.mainmenu.ClassicMainScreen;
import com.mentalfrostbyte.jello.gui.impl.classic.clickgui.ClassicClickGui;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.*;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.holders.CreditsHolder;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.ClickGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.options.buttons.JelloOptionsButton;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.options.JelloOptions;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.options.CreditsScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.mainmenu.MainMenuScreen;
import com.mentalfrostbyte.jello.gui.combined.holders.NoAddonHolder;
import com.mentalfrostbyte.jello.gui.impl.jello.viamcp.JelloPortalScreen;
import com.mentalfrostbyte.jello.gui.combined.impl.SwitchScreen;
import com.mentalfrostbyte.jello.module.impl.gui.classic.TabGUI;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuHolder;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import team.sdhq.eventBus.EventBus;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
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
        replacementScreens.put(MainMenuHolder.class, MainMenuScreen.class);
        replacementScreens.put(ClickGuiHolder.class, ClickGuiScreen.class);
        replacementScreens.put(KeyboardHolder.class, KeyboardScreen.class);
        replacementScreens.put(MapsHolder.class, MapsScreen.class);
        replacementScreens.put(SnakeHolder.class, SnakeGameScreen.class);
        replacementScreens.put(BirdHolder.class, BirdGameScreen.class);
        replacementScreens.put(SpotlightHolder.class, SpotlightScreen.class);
        replacementScreens.put(JelloOptionsHolder.class, JelloOptions.class);
        replacementScreens.put(CreditsHolder.class, CreditsScreen.class);
        screenToScreenName.put(ClickGuiHolder.class, "Click GUI");
        screenToScreenName.put(KeyboardHolder.class, "Keybind Manager");
        screenToScreenName.put(MapsHolder.class, "Jello Maps");
        screenToScreenName.put(SnakeHolder.class, "Snake");
        screenToScreenName.put(BirdHolder.class, "Bird");
        screenToScreenName.put(SpotlightHolder.class, "Spotlight");
    }

    public double field41347;
    public int[] field41354 = new int[2];
    public boolean field41357;
    private final List<Integer> keysPressed = new ArrayList<>();
    private final List<Integer> modifiersPressed = new ArrayList<>();
    private final List<Integer> mouseButtonsPressed = new ArrayList<>();
    private final List<Integer> mouseButtonsReleased = new ArrayList<>();
    private final List<Integer> charsTyped = new ArrayList<>();
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
        } else if (screen instanceof IngameMenuScreen && !(screen instanceof JelloOptionsButton)) {
            Minecraft.getInstance().currentScreen = null;
            Minecraft.getInstance().displayGuiScreen(new JelloOptionsButton());
            return true;
        } else if (Client.getInstance().clientMode == ClientMode.NOADDONS && screen instanceof MainMenuHolder && !(screen instanceof NoAddonHolder)) {
            Minecraft.getInstance().currentScreen = null;
            Minecraft.getInstance().displayGuiScreen(new NoAddonHolder());
            return true;
        } else {
            return false;
        }
    }
    public Screen getScreen() {
        return this.screen;
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
                return replacementScreens.get(screen.getClass()).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
					 NoSuchMethodException e) {
                Client.logger.error("Error creating replacement screen", e);
            }

			return null;
        }
    }

    public void useClassic() {
        replacementScreens.clear();
        replacementScreens.put(MainMenuHolder.class, ClassicMainScreen.class);
        replacementScreens.put(ClickGuiHolder.class, ClassicClickGui.class);
    }

    public void useJello() {
        replacementScreens.clear();
        replacementScreens.put(MainMenuHolder.class, MainMenuScreen.class);
        replacementScreens.put(ClickGuiHolder.class, ClickGuiScreen.class);
        replacementScreens.put(KeyboardHolder.class, KeyboardScreen.class);
        replacementScreens.put(MapsHolder.class, MapsScreen.class);
        replacementScreens.put(SnakeHolder.class, SnakeGameScreen.class);
        replacementScreens.put(BirdHolder.class, BirdGameScreen.class);
        replacementScreens.put(SpotlightHolder.class, SpotlightScreen.class);
        replacementScreens.put(JelloOptionsHolder.class, JelloOptions.class);
        replacementScreens.put(CreditsHolder.class, CreditsScreen.class);
        screenToScreenName.put(ClickGuiHolder.class, "Click GUI");
        screenToScreenName.put(KeyboardHolder.class, "Keybind Manager");
        screenToScreenName.put(MapsHolder.class, "Jello Maps");
        screenToScreenName.put(SnakeHolder.class, "Snake");
        screenToScreenName.put(BirdHolder.class, "Bird");
        screenToScreenName.put(SpotlightHolder.class, "Spotlight");
    }

    /**
     * @see net.minecraft.client.KeyboardListener#onKeyEvent
	 */
    public void handleKeyEvent(int key, int action) {
        if (action == 1 || action == 2) {
            this.keysPressed.add(key);
        } else if (action == 0) {
            this.modifiersPressed.add(key);
        }
    }

    public void addTypedChar(int codePoint, int modifiers) {
        this.charsTyped.add(codePoint);
    }

    public void method33455(double var1, double var3) {
        this.field41347 += var3;
    }

    public void onMouseButtonCallback(int button, int action) {
        if (action != 1) {
            if (action == 0) {
                this.mouseButtonsReleased.add(button);
            }
        } else {
            this.mouseButtonsPressed.add(button);
        }
    }

    public void endTick() {
        if (this.screen != null) {
            this.field41354[0] = Math.max(0, Math.min(Minecraft.getInstance().getMainWindow().getWidth(), (int) Minecraft.getInstance().mouseHelper.getMouseX()));
            this.field41354[1] = Math.max(0, Math.min(Minecraft.getInstance().getMainWindow().getHeight(), (int) Minecraft.getInstance().mouseHelper.getMouseY()));

            for (int key : this.keysPressed) {
                this.onKeyPressed(key);
            }

            for (int modifierPresed : this.modifiersPressed) {
                this.onModifierPressed(modifierPresed);
            }

            for (int var10 : this.mouseButtonsPressed) {
                this.onMouseClick(this.field41354[0], this.field41354[1], var10);
            }

            for (int var11 : this.mouseButtonsReleased) {
                this.onMouseClick2(this.field41354[0], this.field41354[1], var11);
            }

            for (int chr : this.charsTyped) {
                this.onCharTyped((char) chr);
            }

            this.keysPressed.clear();
            this.modifiersPressed.clear();
            this.mouseButtonsPressed.clear();
            this.mouseButtonsReleased.clear();
            this.charsTyped.clear();
            if (this.field41347 == 0.0) {
                this.field41357 = false;
            } else {
                this.method33465((float) this.field41347);
                this.field41347 = 0.0;
                this.field41357 = true;
            }

            if (this.screen != null) {
                this.screen.updatePanelDimensions(this.field41354[0], this.field41354[1]);
            }
        }
    }

    public void onModifierPressed(int modifier) {
        if (this.screen != null) {
            this.screen.modifierPressed(modifier);
        }
    }

    public void onCharTyped(char chr) {
        if (this.screen != null) {
            this.screen.charTyped(chr);
        }
    }

    public void onKeyPressed(int key) {
        if (this.screen != null) {
            this.screen.keyPressed(key);
        }
    }

    public void renderWatermark() {
        if (Minecraft.getInstance().world != null) {
            GL11.glDisable(GL11.GL_LIGHTING);
            int var3 = 0;
            int var4 = 0;
            int var5 = 170;

            if (Minecraft.getInstance().gameSettings.showDebugInfo) {
                var3 = Minecraft.getInstance().getMainWindow().getWidth() / 2 - var5 / 2;
            }

            if (Client.getInstance().clientMode != ClientMode.JELLO) {
                float var7 = 0.5F + TabGUI.animationProgress.calcPercent() * 0.5F;
                GL11.glAlphaFunc(516, 0.1F);
                RenderUtil.drawRoundedRect2(4.0F, 2.0F, 106.0F, 28.0F, RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F * var7));
                RenderUtil.drawString(Resources.bold22, 9.0F, 2.0F, "Sigma", RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F * var7));
                RenderUtil.drawString(
                        Resources.bold22, 8.0F, 1.0F, "Sigma", RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), Math.min(1.0F, var7 * 1.2F))
                );
                int var8 = Color.getHSBColor((float) (System.currentTimeMillis() % 4000L) / 4000.0F, 1.0F, 1.0F).getRGB();
                RenderUtil.drawString(Resources.bold14, 73.0F, 2.0F, "5.1.0", RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
                RenderUtil.drawString(Resources.bold14, 72.0F, 1.0F, "5.1.0", RenderUtil2.applyAlpha(var8, Math.min(1.0F, var7 * 1.4F)));
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

            EventBus.call(new EventRender2DOffset());
        }

        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.draw(1.0F);
        }
    }

    public void method33465(float var1) {
        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.onScroll(var1);
        }
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.onClick(mouseX, mouseY, mouseButton);
        }
    }

    public void onMouseClick2(int mouseX, int mouseY, int mouseButton) {
        if (this.screen != null && Minecraft.getInstance().loadingGui == null) {
            this.screen.onClick2(mouseX, mouseY, mouseButton);
        }
    }

    public void getUIConfig(JsonObject uiConfig) {
        if (this.screen != null) {
            JsonObject var4 = this.screen.toConfigWithExtra(new JsonObject());
            if (var4.size() != 0) {
                uiConfig.add(this.screen.getName(), var4);
            }
        }

        uiConfig.addProperty("guiBlur", this.guiBlur);
        uiConfig.addProperty("hqIngameBlur", this.hqIngameBlur);
        uiConfig.addProperty("hidpicocoa", hidpiCocoa);
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

    public void loadUIConfig(JsonObject uiConfig) {
        if (this.screen != null) {
            JsonObject var4 = null;

            try {
                var4 = Client.getInstance().config.getAsJsonObject(this.screen.getName());
            } catch (Exception var9) {
                var4 = new JsonObject();
            } finally {
                this.screen.loadConfig(var4);
            }
        }

        if (uiConfig.has("guiBlur")) {
            this.guiBlur = uiConfig.get("guiBlur").getAsBoolean();
        }

        if (uiConfig.has("hqIngameBlur")) {
            this.hqIngameBlur = uiConfig.get("hqIngameBlur").getAsBoolean();
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

    public String getNameForTarget(Class<? extends net.minecraft.client.gui.screen.Screen> screen) {
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

    public void onResize() throws JsonParseException {
        if (this.screen != null) {
            this.getUIConfig(Client.getInstance().config);

            try {
                this.screen = this.screen.getClass().newInstance();
            } catch (IllegalAccessException | InstantiationException exc) {
                Client.logger.warn(exc);
            }

            this.loadUIConfig(Client.getInstance().config);
        }

        if (Minecraft.getInstance().getMainWindow().getWidth() != 0 && Minecraft.getInstance().getMainWindow().getHeight() != 0) {
            scaleFactor = (float) Math.max(
                    Minecraft.getInstance().getMainWindow().getFramebufferWidth() / Minecraft.getInstance().getMainWindow().getWidth(),
                    Minecraft.getInstance().getMainWindow().getFramebufferHeight() / Minecraft.getInstance().getMainWindow().getHeight()
            );
        }
    }

    public Screen getCurrentScreen() {
        return this.screen;
    }

    public void handleCurrentScreen() throws JsonParseException {
        this.handleScreen(handleScreen(Minecraft.getInstance().currentScreen));
    }

    public void handleScreen(Screen screen) {
        if (this.screen != null) {
            this.getUIConfig(Client.getInstance().config);
        }

        this.screen = screen;
        this.loadUIConfig(Client.getInstance().config);
        if (this.screen != null) {
            this.screen.updatePanelDimensions(this.field41354[0], this.field41354[1]);
        }
    }

    public boolean hasReplacement(net.minecraft.client.gui.screen.Screen screen) {
        return replacementScreens.containsKey(screen.getClass());
    }
}
