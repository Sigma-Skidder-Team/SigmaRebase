package com.mentalfrostbyte.jello.gui.combined;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.interfaces.Class7261;
import com.mentalfrostbyte.jello.gui.base.interfaces.IGuiEventListener;
import com.mentalfrostbyte.jello.gui.base.interfaces.IWidthSetter;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.TextField;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import org.newdawn.slick.TrueTypeFont;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import totalcross.json.CJsonUtils;
import totalcross.json.JSONArray;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class CustomGuiScreen implements IGuiEventListener {
    private final List<CustomGuiScreen> children = new ArrayList<>();
    private final List<IWidthSetter> field20894 = new ArrayList<>();
    private final List<CustomGuiScreen> field20916 = new ArrayList<>();
    private final List<CustomGuiScreen> field20918 = new ArrayList<>();
    private final List<Class7914> field20920 = new ArrayList<>();
    private final List<MouseListener> mouseButtonListeners = new ArrayList<>();
    private final List<DoThis> doThese = new ArrayList<>();
    private final List<KeyPressedListener> keyPressedListeners = new ArrayList<>();
    private final List<CharTypedListener> charTypedListeners = new ArrayList<>();
    public String name;
    public CustomGuiScreen parent;
    public int xA;
    public int yA;
    public int widthA;
    public int heightA;
    public float field20899 = 1.0F;
    public float field20900 = 1.0F;
    public int field20901 = 0;
    public int field20902 = 0;
    public boolean visible;
    public boolean hovered;
    public boolean focused;
    public boolean reAddChildren;
    public boolean field20907;
    public boolean field20908;
    public boolean field20909;
    public boolean listening;
    public boolean saveSize;
    public String text;
    public TrueTypeFont font;
    public ColorHelper textColor;
    private final ArrayList<Runnable> runOnDimensionUpdate = new ArrayList<Runnable>();
    private boolean field20917;
    private CustomGuiScreen field20919;
    private int heightO;
    private int widthO;

    public CustomGuiScreen(CustomGuiScreen parent, String name) {
        this(parent, name, 0, 0, 0, 0);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int xA, int yA, int widthA, int heightA) {
        this(parent, name, xA, yA, widthA, heightA, ColorHelper.field27961);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int xA, int yA, int widthA, int heightA, ColorHelper textColor) {
        this(parent, name, xA, yA, widthA, heightA, textColor, null);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int xA, int yA, int widthA, int heightA, ColorHelper textColor, String text) {
        this(parent, name, xA, yA, widthA, heightA, textColor, text, ResourceRegistry.JelloLightFont25);
    }

    /**
     * Constructs a new CustomGuiScreen with specified parameters.
     *
     * @param parent    The parent CustomGuiScreen object.
     * @param name      The name of this CustomGuiScreen.
     * @param xA        The x-coordinate of the top-left corner.
     * @param yA        The y-coordinate of the top-left corner.
     * @param widthA    The width of the screen.
     * @param heightA   The height of the screen.
     * @param textColor The color of the text.
     * @param text The initial typed text (can be null).
     * @param font      The TrueTypeFont to be used for rendering text.
     */
    public CustomGuiScreen(CustomGuiScreen parent, String name, int xA, int yA, int widthA, int heightA, ColorHelper textColor, String text, TrueTypeFont font) {
        this.name = name;
        this.parent = parent;
        this.xA = xA;
        this.yA = yA;
        this.widthA = widthA;
        this.heightA = heightA;
        this.text = text;
        this.textColor = textColor;
        this.font = font;
        this.visible = true;
        this.hovered = true;
        this.listening = true;
        this.saveSize = false;
    }

    private void method13220() {
        for (CustomGuiScreen screen : new ArrayList<CustomGuiScreen>(this.children)) {
            if (screen.shouldReAddChildren()) {
                this.children.remove(screen);
                this.children.add(screen);
            }

            if (screen.method13293()) {
                this.children.remove(screen);
                this.children.add(0, screen);
            }
        }
    }

    public CustomGuiScreen method13221(String var1) {
        for (CustomGuiScreen var5 : this.children) {
            if (var5.getName().equals(var1)) {
                return var5;
            }
        }

        return null;
    }

    public void runThisOnDimensionUpdate(Runnable that) {
        synchronized (this) {
            if (that != null) {
                this.runOnDimensionUpdate.add(that);
            }
        }
    }
    /**
     * Manages the arrangement and removal of CustomGuiScreen objects within various lists.
     * This method performs the following operations:
     * 1. Removes specified screens from iconPanelList and clears field20919 if necessary.
     * 2. Clears and repopulates iconPanelList with elements from field20916.
     * 3. Ensures field20919, if not null, is at the end of iconPanelList.
     * 4. Calls method13220() to further arrange the iconPanelList.
     * <p>
     * This method does not take any parameters and does not return a value.
     * It operates on the class's internal lists and fields.
     */
    private void method13223() {
        for (CustomGuiScreen var4 : this.field20918) {
            this.children.remove(var4);
            if (this.field20919 == var4) {
                this.field20919 = null;
            }
        }

        this.field20916.clear();

        this.children.addAll(this.field20916);

        this.field20916.clear();
        if (this.field20919 != null) {
            this.children.remove(this.field20919);
            this.children.add(this.field20919);
        }

        this.method13220();
    }

    public void updatePanelDimensions(int newHeight, int newWidth) {
        this.widthO = newWidth;
        this.heightO = newHeight;
        this.field20908 = this.isVisible() && this.method13229(newHeight, newWidth);

        try {
            for (Runnable runnable : this.runOnDimensionUpdate) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        } catch (ConcurrentModificationException e) {
            Client.getInstance().getLogger().info("kys concurrent modification exception go away");
        }

        this.runOnDimensionUpdate.clear();
        this.field20917 = true;

        try {
            for (CustomGuiScreen iconPanel : this.children) {
                iconPanel.updatePanelDimensions(newHeight, newWidth);
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        this.field20909 = this.field20909 & this.field20908;

        for (IWidthSetter var11 : this.method13260()) {
            if (this.visible) {
                var11.setWidth(this, this.getParent());
            }
        }

        this.method13223();
        this.field20917 = false;
    }

    public void method13224() {
        GL11.glTranslatef((float) (this.getXA() + this.getWidthA() / 2), (float) (this.getYA() + this.getHeightA() / 2), 0.0F);
        GL11.glScalef(this.method13273(), this.method13275(), 0.0F);
        GL11.glTranslatef((float) (-this.getXA() - this.getWidthA() / 2), (float) (-this.getYA() - this.getHeightA() / 2), 0.0F);
    }

    public void method13225() {
        GL11.glTranslatef((float) this.method13280(), (float) this.method13282(), 0.0F);
    }

    public void draw(float partialTicks) {
        this.method13226(partialTicks);
    }

    public final void method13226(float partialTicks) {
        GlStateManager.enableAlphaTest();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslatef((float) this.getXA(), (float) this.getYA(), 0.0F);

        for (CustomGuiScreen child : this.children) {
            if (child.isSelfVisible()) {
                GL11.glPushMatrix();
                child.draw(partialTicks);
                GL11.glPopMatrix();
            }
        }
    }

    public boolean method13227() {
        for (CustomGuiScreen var4 : this.getChildren()) {
            if (var4 instanceof TextField && var4.focused) {
                return true;
            }

            if (var4.method13227()) {
                return true;
            }
        }

        return false;
    }

    public void modifierPressed(int var1) {
        for (CustomGuiScreen var5 : this.children) {
            if (var5.isHovered() && var5.isSelfVisible()) {
                var5.modifierPressed(var1);
            }
        }
    }

    @Override
    public void charTyped(char typed) {
        for (CustomGuiScreen var5 : this.children) {
            if (var5.isHovered() && var5.isSelfVisible()) {
                var5.charTyped(typed);
            }
        }

        this.callCharTypedListeners(typed);
    }

    @Override
    public void keyPressed(int keyCode) {
        for (CustomGuiScreen child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.keyPressed(keyCode);
            }
        }

        this.callKeyPressedListeners(keyCode);
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        boolean var6 = false;

        for (int var7 = this.children.size() - 1; var7 >= 0; var7--) {
            CustomGuiScreen var8 = this.children.get(var7);
            boolean var9 = var8.getParent() != null
                    && var8.getParent() instanceof ScrollableContentPanel
                    && var8.getParent().method13114(mouseX, mouseY)
                    && var8.getParent().isSelfVisible()
                    && var8.getParent().isHovered();
            if (var6 || !var8.isHovered() || !var8.isSelfVisible() || !var8.method13114(mouseX, mouseY) && !var9) {
                var8.setFocused(false);
                if (var8 != null) {
                    for (CustomGuiScreen var12 : var8.getChildren()) {
                        var12.setFocused(false);
                    }
                }
            } else {
                var8.onClick(mouseX, mouseY, mouseButton);
                var6 = !var9;
            }
        }

        if (!var6) {
            this.field20909 = this.field20908 = true;
            this.method13242();
            this.method13248(mouseButton);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick2(int mouseX, int mouseY, int mouseButton) {
        this.field20908 = this.method13114(mouseX, mouseY);

        for (CustomGuiScreen var7 : this.children) {
            if (var7.isHovered() && var7.isSelfVisible()) {
                var7.onClick2(mouseX, mouseY, mouseButton);
            }
        }

        this.onMouseButtonUsed(mouseButton);
        if (this.method13212() && this.method13298()) {
            this.onClick3(mouseX, mouseY, mouseButton);
        }

        this.field20909 = false;
    }

    @Override
    public void onClick3(int mouseX, int mouseY, int mouseButton) {
        this.method13252(mouseButton);
    }

    @Override
    public void voidEvent3(float scroll) {
        for (CustomGuiScreen var5 : this.children) {
            if (var5.isHovered() && var5.isSelfVisible()) {
                var5.voidEvent3(scroll);
            }
        }
    }

    public boolean method13114(int var1, int var2) {
        var1 -= this.method13271();
        var2 -= this.method13272();
        return var1 >= 0 && var1 <= this.widthA && var2 >= 0 && var2 <= this.heightA;
    }

    public boolean method13228(int var1, int var2, boolean var3) {
        boolean var6 = this.method13114(var1, var2);
        if (var6 && this.parent != null) {
            if (var3) {
                for (CustomGuiScreen var8 : this.getChildren()) {
                    if (var8.isSelfVisible() && var8.method13114(var1, var2)) {
                        return false;
                    }
                }
            }

            CustomGuiScreen var11 = this;

            for (CustomGuiScreen var12 = this.getParent(); var12 != null; var12 = var12.getParent()) {
                for (int var9 = var12.findChild(var11) + 1; var9 < var12.getChildren().size(); var9++) {
                    CustomGuiScreen var10 = var12.getChildren().get(var9);
                    if (var10 != var11 && var10.isSelfVisible() && var10.method13114(var1, var2)) {
                        return false;
                    }
                }

                var11 = var12;
            }
        }

        return var6;
    }

    public boolean method13229(int var1, int var2) {
        return this.method13228(var1, var2, true);
    }

    public void addToList(CustomGuiScreen var1) {
        if (var1 != null) {
            for (CustomGuiScreen var5 : this.getChildren()) {
                if (var5.getName().equals(var1.getName())) {
                    System.out.println("Children with duplicate IDs! Child with id \"" + var5.getName() + "\" already exists in view \"" + this.getName() + "\"!");
                    return;
                }
            }

            var1.setParent(this);
            if (this.field20917) {
                this.field20916.add(var1);
            } else {
                try {
                    this.children.add(var1);
                } catch (ConcurrentModificationException var6) {
                    this.field20916.add(var1);
                }
            }
        }
    }

    public boolean isntQueue(String var1) {
        for (CustomGuiScreen var5 : this.getChildren()) {
            if (var5.getName().equals(var1)) {
                return true;
            }
        }

        return false;
    }

    public void method13232(CustomGuiScreen var1) {
        if (var1 != null) {
            for (CustomGuiScreen var5 : this.getChildren()) {
                if (var5.getName().equals(var1.getName())) {
                    throw new RuntimeException("Children with duplicate IDs!");
                }
            }

            var1.setParent(this);
            this.field20916.add(var1);
        }
    }

    public void showAlert(CustomGuiScreen var1) {
        if (var1 != null) {
            for (CustomGuiScreen var5 : this.getChildren()) {
                if (var5.getName().equals(var1.getName())) {
                    throw new RuntimeException("Children with duplicate IDs!");
                }
            }

            var1.setParent(this);

            try {
                this.children.add(var1);
            } catch (ConcurrentModificationException var6) {
            }
        }
    }

    public void method13234(CustomGuiScreen var1) {
        if (this.field20917) {
            this.field20918.add(var1);
        } else {
            this.removeChildren(var1);
        }
    }

    public void removeChildren(CustomGuiScreen guiIn) {
        this.children.remove(guiIn);
        if (this.field20919 != null && this.field20919.equals(guiIn)) {
            this.field20919 = null;
        }

        this.field20916.remove(guiIn);
    }

    public void method13237(CustomGuiScreen var1) {
        for (CustomGuiScreen var5 : this.getChildren()) {
            if (var5.name.equals(var1.name)) {
                this.method13234(var5);
            }
        }
    }

    public void clearChildren() {
        this.children.clear();
    }

    public boolean hasChild(CustomGuiScreen child) {
        return this.children.contains(child);
    }

    public int findChild(CustomGuiScreen child) {
        return this.children.indexOf(child);
    }

    public List<CustomGuiScreen> getChildren() {
        return this.children;
    }

    public void method13242() {
        this.setFocused(true);
        if (this.parent != null) {
            this.parent.field20919 = this;
            this.parent.method13242();
        }
    }

    public void method13243() {
        for (CustomGuiScreen var4 : this.parent.getChildren()) {
            if (var4 == this) {
                return;
            }

            var4.method13242();
        }
    }

    public JSONObject toConfigWithExtra(JSONObject config) {
        if (this.isListening()) {
            config.put("id", this.getName());
            config.put("x", this.getXA());
            config.put("y", this.getYA());
            if (this.shouldSaveSize()) {
                config.put("width", this.getWidthA());
                config.put("height", this.getHeightA());
            }

            config.put("index", this.parent == null ? 0 : this.parent.findChild(this));
            return this.toConfig(config);
        } else {
            return config;
        }
    }

    public final JSONObject toConfig(JSONObject base) {
        JSONArray children = new JSONArray();

        for (CustomGuiScreen child : this.children) {
            if (child.isListening()) {
                JSONObject var7 = child.toConfigWithExtra(new JSONObject());
                if (var7.length() > 0) {
                    children.put(var7);
                }
            }
        }

        base.put("children", children);
        return base;
    }

    public void loadConfig(JSONObject config) {
        if (this.isListening()) {
            this.xA = CJsonUtils.getIntOrDefault(config, "x", this.xA);
            this.yA = CJsonUtils.getIntOrDefault(config, "y", this.yA);
            if (this.shouldSaveSize()) {
                this.widthA = CJsonUtils.getIntOrDefault(config, "width", this.widthA);
                this.heightA = CJsonUtils.getIntOrDefault(config, "height", this.heightA);
            }

            JSONArray children = CJsonUtils.getJSONArrayOrNull(config, "children");
            if (children != null) {
                List<CustomGuiScreen> childrenArray = new ArrayList<>(this.children);

                for (int i = 0; i < children.length(); i++) {
                    JSONObject childJson = null;
                    try {
                        childJson = children.getJSONObject(i);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    String id = CJsonUtils.getStringOrDefault(childJson, "id", null);
                    int index = CJsonUtils.getIntOrDefault(childJson, "index", -1);

                    for (CustomGuiScreen child : childrenArray) {
                        if (child.getName().equals(id)) {
                            child.loadConfig(childJson);
                            if (index >= 0) {
                                this.children.remove(child);
                                if (index > this.children.size()) {
                                    this.children.add(child);
                                } else {
                                    this.children.add(index, child);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this != other) {
            if (!(other instanceof CustomGuiScreen)) {
                return false;
            } else {
                CustomGuiScreen to = (CustomGuiScreen) other;
                return this.name.equals(to.name) && (this.getParent() == null || this.getParent().equals(to.getParent()));
            }
        } else {
            return true;
        }
    }

    public void method13246(Class7261 var1) {
        var1.method22796(this);
    }

    public final CustomGuiScreen method13247(Class7914 var1) {
        this.field20920.add(var1);
        return this;
    }

    public void method13248(int var1) {
        for (Class7914 var5 : this.field20920) {
            var5.method26544(this, var1);
        }
    }

    public CustomGuiScreen method13249(MouseListener var1) {
        this.mouseButtonListeners.add(var1);
        return this;
    }

    public void onMouseButtonUsed(int mouseButton) {
        for (MouseListener mouse : this.mouseButtonListeners) {
            mouse.mouseButtonUsed(this, mouseButton);
        }
    }

    public CustomGuiScreen doThis(DoThis that) {
        this.doThese.add(that);
        return this;
    }

    public void method13252(int i) {
        for (DoThis doThis : this.doThese) {
            doThis.doIt(this, i);
        }
    }

    public final void addKeyPressListener(KeyPressedListener listener) {
        this.keyPressedListeners.add(listener);
    }

    public void callKeyPressedListeners(int key) {
        for (KeyPressedListener listener : this.keyPressedListeners) {
            listener.keyPressed(this, key);
        }
    }

    public void callCharTypedListeners(char chr) {
        for (CharTypedListener listener : this.charTypedListeners) {
            listener.charTyped(chr);
        }
    }

    public String getName() {
        return this.name;
    }

    public CustomGuiScreen getParent() {
        return this.parent;
    }

    public void setParent(CustomGuiScreen var1) {
        this.parent = var1;
    }

    public List<IWidthSetter> method13260() {
        return this.field20894;
    }

    public void setSize(IWidthSetter var1) {
        this.field20894.add(var1);
    }

    public int getXA() {
        return this.xA;
    }

    public void setXA(int var1) {
        this.xA = var1;
    }

    public int getYA() {
        return this.yA;
    }

    public void setYA(int var1) {
        this.yA = var1;
    }

    public int getWidthA() {
        return this.widthA;
    }

    public void setWidthA(int var1) {
        this.widthA = var1;
    }

    public int getHeightA() {
        return this.heightA;
    }

    public void setHeightA(int var1) {
        this.heightA = var1;
    }

    public int method13271() {
        return this.parent == null ? this.xA : this.parent.method13271() + this.xA;
    }

    public int method13272() {
        return this.parent == null ? this.yA : this.parent.method13272() + this.yA;
    }

    public float method13273() {
        return this.field20899;
    }

    public float method13275() {
        return this.field20900;
    }

    public void method13277(float var1) {
        this.field20899 = var1;
    }

    public void method13278(float var1) {
        this.field20900 = var1;
    }

    public void method13279(float var1, float var2) {
        this.field20899 = var1;
        this.field20900 = var2;
    }

    public int method13280() {
        return this.field20901;
    }

    public int method13282() {
        return this.field20902;
    }

    public void method13284(int var1) {
        this.field20901 = var1;
    }

    public void drawBackground(int var1) {
        this.field20902 = var1;
    }

    public void draw(int var1, int var2) {
        this.field20901 = var1;
        this.field20902 = var2;
    }

    /**
     * @return If this screen is visible.
     *         doesn't account for the parent, but {@link CustomGuiScreen#isVisible()} does
     * @see CustomGuiScreen#isVisible()
     */
    public boolean isSelfVisible() {
        return this.visible;
    }

    public void setSelfVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return If this screen and its parent (if it has one) are visible.
     * @see CustomGuiScreen#isSelfVisible()
     */
    public boolean isVisible() {
        return this.parent == null ? this.visible : this.visible && this.parent.isVisible();
    }

    /**
     * used in {@link CustomGuiScreen#method13220} to re-add a child (if this returns true)
     */
    public boolean shouldReAddChildren() {
        return this.reAddChildren;
    }

    public void setReAddChildren(boolean reAddChildren) {
        this.reAddChildren = reAddChildren;
    }

    public boolean method13293() {
        return this.field20907;
    }

    public void method13294(boolean var1) {
        this.field20907 = var1;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean method13298() {
        return this.field20908;
    }

    public boolean method13212() {
        return this.field20909;
    }

    public boolean isListening() {
        return this.listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean shouldSaveSize() {
        return this.saveSize;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TrueTypeFont getFont() {
        return this.font;
    }

    public void setFont(TrueTypeFont font) {
        this.font = font;
    }

    public ColorHelper getTextColor() {
        return this.textColor;
    }

    public void setTextColor(ColorHelper textColor) {
        this.textColor = textColor;
    }

    public int getHeightO() {
        return this.heightO;
    }

    public int getWidthO() {
        return this.widthO;
    }

    public interface DoThis {
        void doIt(CustomGuiScreen parent, int var2);
    }

    public interface CharTypedListener {
        void charTyped(char chr);
    }

    public interface MouseListener {
        void mouseButtonUsed(CustomGuiScreen screen, int mouseButton);
    }

    public interface KeyPressedListener {
        void keyPressed(CustomGuiScreen screen, int key);
    }

    public interface Class7914 {
       void method26544(CustomGuiScreen screen, int var2);
    }
}
