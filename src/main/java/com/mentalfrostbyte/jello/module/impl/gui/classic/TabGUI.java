package com.mentalfrostbyte.jello.module.impl.gui.classic;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.misc.CategoryDrawPart;
import com.mentalfrostbyte.jello.misc.CategoryDrawPartBackground;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.module.ModuleWithModuleSettings;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.impl.BooleanSetting;
import com.mentalfrostbyte.jello.module.settings.impl.ModeSetting;
import com.mentalfrostbyte.jello.module.settings.impl.NumberSetting;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.MathHelper;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import team.sdhq.eventBus.annotations.EventTarget;
import team.sdhq.eventBus.annotations.priority.HighestPriority;

import java.util.ArrayList;
import java.util.List;

public class TabGUI extends Module {
    public static final Animation animationProgress = new Animation(200, 200, Direction.BACKWARDS);
    public Animation secondAnimationProgress = new Animation(500, 0, Direction.BACKWARDS);
    private final Animation firstAnimationProgress = new Animation(300, 300, Direction.BACKWARDS);

    private final List<ModuleCategory> categories = new ArrayList<>();
    private int animationCooldown = 0;
    private static final List<CategoryDrawPart> categoryDrawParts = new ArrayList<>();

    public TabGUI() {
        super(ModuleCategory.GUI, "TabGUI", "Manage mods without opening the ClickGUI");
    }

    @Override
    public void initialize() {
        this.categories.add(ModuleCategory.COMBAT);
        this.categories.add(ModuleCategory.PLAYER);
        this.categories.add(ModuleCategory.MOVEMENT);
        this.categories.add(ModuleCategory.RENDER);
        this.categories.add(ModuleCategory.WORLD);
        this.categories.add(ModuleCategory.MISC);
        ArrayList<String> categoryList = new ArrayList<>();

        for (ModuleCategory category : this.categories) {
            categoryList.add(category.name());
        }

        categoryDrawParts.add(0, new CategoryDrawPart(categoryList, 0));
    }

    @EventTarget
    public void onKeyPress(EventKeyPress event) {
        if (this.isEnabled()) {
            KeyAction action = mapKeyToAction(event.getKey());
            if (action != null) {
                animationProgress.changeDirection(Direction.FORWARDS);
                this.animationCooldown = 80;
                int categoryState = this.getCurrentCategoryState();
                CategoryDrawPart category = categoryDrawParts.get(categoryState - 1);
                if (action != KeyAction.EnterKey && (!this.isAnimationForwards() && action != KeyAction.RightArrowKey || categoryState != 3)) {
                    this.secondAnimationProgress = new Animation(500, 200, Direction.BACKWARDS);
                }

                switch (action) {
                    case LeftArrowKey:
                        if (categoryState == 3 && this.isAnimationForwards()) {
                            this.setAnimationDirection(false);
                        } else if (categoryState > 1) {
                            if (categoryDrawParts.get(categoryDrawParts.size() - 1).isExpanded()) {
                                categoryDrawParts.remove(categoryDrawParts.size() - 1);
                            }

                            category.expand();
                        }
                        break;
                    case DownArrowKey:
                        if (categoryState == 3 && this.isAnimationForwards()) {
                            this.onSettingChange(true);
                        } else if (category != null) {
                            category.scrollDown();
                        }
                        break;
                    case UpArrowKey:
                        if (categoryState == 3 && this.isAnimationForwards()) {
                            this.onSettingChange(false);
                        } else if (category != null) {
                            category.scrollUp();
                        }
                        break;
                    case RightArrowKey:
                        if (categoryState == 1) {
                            this.updateCategoryParts(this.categories.get(category.currentOffset));
                        } else if (categoryState == 2 && category != null) {
                            CategoryDrawPart drawPart = categoryDrawParts.get(0);
                            ModuleCategory modCategory = this.categories.get(drawPart.currentOffset);
                            Module module = Client.getInstance().moduleManager.getModulesByCategory(modCategory).get(category.currentOffset);
                            this.updateModuleSettingsParts(module);
                        } else if (categoryState == 3) {
                            this.setAnimationDirection(true);
                        }
                        break;
                    case EnterKey:
                        if (categoryState == 2 && category != null) {
                            CategoryDrawPart drawPart = categoryDrawParts.get(0);
                            ModuleCategory modCat = this.categories.get(drawPart.currentOffset);
                            Module mod = Client.getInstance().moduleManager.getModulesByCategory(modCat).get(category.currentOffset);
                            mod.setEnabled(!mod.isEnabled());
                        }
                        break;
                }
            }
        }
    }

    private void onSettingChange(boolean goDown) {
        CategoryDrawPart categoryIndex = categoryDrawParts.get(0);
        CategoryDrawPart moduleIndex = categoryDrawParts.get(1);
        CategoryDrawPart settingIndex = categoryDrawParts.get(2);
        ModuleCategory category = this.categories.get(categoryIndex.currentOffset);
        Module module = Client.getInstance().moduleManager.getModulesByCategory(category).get(moduleIndex.currentOffset);
        Setting<?> setting = this.getModuleSettings(module).get(settingIndex.currentOffset);
        if (!(setting instanceof ModeSetting mode)) {
            if (!(setting instanceof BooleanSetting bool)) {
                if (setting instanceof NumberSetting<?> numberSetting) {
                    Object obj = numberSetting.getCurrentValue();
                    if (obj != null) {
                        Float value = numberSetting.getCurrentValue();
                        if (goDown) {
                            value = value - numberSetting.getStep();
                        } else {
                            value = value + numberSetting.getStep();
                        }

                        value = Math.min(Math.max(value, numberSetting.getMin()), numberSetting.getMax());
                        numberSetting.setCurrentValue(value);
                    }
                }
            } else {
                bool.setCurrentValue(!bool.getCurrentValue());
            }
        } else {
            int index = mode.getModeIndex();
            if (!goDown) {
                index--;
            } else {
                index++;
            }

            if (index > mode.getAvailableModes().size() - 1) {
                index = 0;
            }

            if (index < 0) {
                index = mode.getAvailableModes().size() - 1;
            }

            mode.setModeByIndex(index);
        }

        settingIndex.setCategories(this.getModuleSettingsWithValues(module));
    }

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (this.isEnabled()) {
            if (this.animationCooldown <= 0) {
                animationProgress.changeDirection(Direction.BACKWARDS);
                this.secondAnimationProgress.changeDirection(Direction.BACKWARDS);
            } else {
                this.animationCooldown--;
            }
        }
    }

    @EventTarget
    @HighestPriority
    public void onRender(EventRender2DOffset event) {
        if (this.isEnabled() && mc.player != null) {
            if (!Minecraft.getInstance().gameSettings.showDebugInfo) {
                if (!Minecraft.getInstance().gameSettings.hideGUI) {
                    this.refreshCategoryModules();

                    for (CategoryDrawPartBackground cat : categoryDrawParts) {
                        cat.render((float) (0.5 + (double) animationProgress.calcPercent() * 0.5));
                    }

                    this.drawCategories((float) (0.5 + (double) animationProgress.calcPercent() * 0.5));
                    RenderUtil.drawRoundedRect2(12.0F, 30.0F, 90.0F, 1.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor());
                }
            }
        }
    }

    private void refreshCategoryModules() {
        if (categoryDrawParts.size() >= 2) {
            CategoryDrawPart categoryDisplay = categoryDrawParts.get(1);
            CategoryDrawPart activeCategory = categoryDrawParts.get(0);
            ModuleCategory currentCategory = this.categories.get(activeCategory.currentOffset);
            int index = 0;

            for (Module module : Client.getInstance().moduleManager.getModulesByCategory(currentCategory)) {
                categoryDisplay.updateCategory(index++, (!module.isEnabled() ? "§7" : "") + module.getSuffix());
            }
        }
    }

    private void drawCategories(float partialTicks) {
        try {
            int drawState = this.getCurrentCategoryState();
            if (drawState == 2 || drawState == 3) {
                CategoryDrawPart firstCategoryPart = categoryDrawParts.get(0);
                CategoryDrawPart secondCategoryPart = categoryDrawParts.get(1);
                CategoryDrawPart thirdCategoryPart = drawState != 3 ? null : categoryDrawParts.get(2);
                CategoryDrawPart activeCategoryPart = secondCategoryPart;
                if (thirdCategoryPart != null) {
                    activeCategoryPart = thirdCategoryPart;
                }

                if (activeCategoryPart.isAnimating() && animationProgress.getDirection() == Direction.FORWARDS) {
                    if (this.getCurrentCategoryState() == categoryDrawParts.size()) {
                        this.secondAnimationProgress.changeDirection(Direction.FORWARDS);
                    } else if (categoryDrawParts.get(categoryDrawParts.size() - 1).isFullyCollapsed()) {
                        this.secondAnimationProgress.changeDirection(Direction.FORWARDS);
                    }
                }

                ModuleCategory currentCategory = this.categories.get(firstCategoryPart.currentOffset);
                Module currentModule = Client.getInstance().moduleManager.getModulesByCategory(currentCategory).get(secondCategoryPart.currentOffset);
                String description = currentModule.getDescription();
                if (drawState == 3) {
                    Setting<?> currentSetting = this.getModuleSettings(currentModule).get(thirdCategoryPart.currentOffset);
                    description = currentSetting.getDescription();
                }

                float animationProgressValue = MathHelper.calculateTransition(this.firstAnimationProgress.calcPercent(), 0.0F, 1.0F, 1.0F) * animationProgress.calcPercent();
                if (this.firstAnimationProgress.getDirection() == Direction.BACKWARDS) {
                    animationProgressValue = MathHelper.calculateBackwardTransition(this.firstAnimationProgress.calcPercent(), 0.0F, 1.0F, 1.0F);
                }

                RenderUtil.renderCategoryBox(
                        (float) activeCategoryPart.getStartX() + (float) activeCategoryPart.getWidth() + 14.0F * animationProgressValue,
                        (float) activeCategoryPart.getStartY() + 16.0F + (float) (25 * activeCategoryPart.currentOffset),
                        24.0F * animationProgressValue,
                        ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.6F),
                        ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.6F)
                );
                int descriptionX = activeCategoryPart.getStartX() + activeCategoryPart.getWidth() + 4 + Math.round(animationProgressValue * 28.0F);
                int descriptionY = activeCategoryPart.getStartY() + 25 * activeCategoryPart.currentOffset + 4;
                int descriptionWidth = activeCategoryPart.font.getWidth(description) + 8;
                float secondAnimationValue = MathHelper.calculateTransition(this.secondAnimationProgress.calcPercent(), 0.0F, 1.0F, 1.0F);
                RenderUtil.drawRoundedRect2((float) descriptionX, (float) descriptionY, (float) descriptionWidth * secondAnimationValue, 25.0F, ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.6F));
                RenderUtil.startScissor((float) descriptionX, (float) descriptionY, (float) descriptionWidth * secondAnimationValue, 25.0F);
                RenderUtil.drawString(
                        activeCategoryPart.font, (float) (descriptionX + 4), (float) (descriptionY + 2), description, ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), Math.min(1.0F, partialTicks * 1.7F))
                );
                RenderUtil.endScissor();
            }
        } catch (IndexOutOfBoundsException e) {
            Client.getInstance().getLogger().warn("bruh your modules aren't enough for this sexy ass tabgui");
        }
    }

    public static KeyAction mapKeyToAction(int keyCode) {
        switch (keyCode) {
            case 257: // ENTER key
                return KeyAction.EnterKey;
            case 258: // TAB key
            case 259: // BACKSPACE key
            case 260: // INSERT key
            case 261: // DELETE key
            default:
                return null;
            case 262:
                return KeyAction.RightArrowKey;
            case 263:
                return KeyAction.LeftArrowKey;
            case 264:
                return KeyAction.DownArrowKey;
            case 265:
                return KeyAction.UpArrowKey;
        }
    }

    public static int calculateStartX(int index) {
        int totalWidth = 0;

        for (int i = 0; i < index; i++) {
            totalWidth += categoryDrawParts.get(i).getWidth();
        }

        return 4 + totalWidth + 5 * index;
    }

    public void updateCategoryParts(ModuleCategory category) {
        List<String> suffixes = new ArrayList<>();

        for (Module module : Client.getInstance().moduleManager.getModulesByCategory(category)) {
            suffixes.add(module.getSuffix());
        }

        this.removePartsByThreshold(1);
        categoryDrawParts.add(1, new CategoryDrawPart(suffixes, 1));
    }

    public void updateModuleSettingsParts(Module module) {
        List<String> settings = this.getModuleSettingsWithValues(module);

        if (!settings.isEmpty()) {
            this.removePartsByThreshold(2);
            categoryDrawParts.add(2, new CategoryDrawPart(settings, 2));
        }
    }

    public void removePartsByThreshold(int threshold) {
        categoryDrawParts.removeIf(categoryDrawPart -> categoryDrawPart.priority >= threshold);
    }

    @Override
    public void onDisable() {
        animationProgress.changeDirection(Direction.BACKWARDS);
        this.animationCooldown = 0;
    }

    @Override
    public void onEnable() {
        animationProgress.changeDirection(Direction.FORWARDS);
        this.animationCooldown = 40;
    }

    public List<String> getModuleSettingsWithValues(Module module) {
        List<String> settings = new ArrayList<>();

        for (Setting<?> setting : this.getModuleSettings(module)) {
            settings.add(setting.getName() + " " + setting.getCurrentValue());
        }

        return settings;
    }

    public List<Setting> getModuleSettings(Module module) {
        List<Setting> setting = new ArrayList<>(module.getSettingMap().values());
        if (module instanceof ModuleWithModuleSettings moduleWithSubModules) {
            moduleWithSubModules.calledOnEnable();
            if (moduleWithSubModules.getModWithTypeSetToName() != null) {
                setting.addAll(moduleWithSubModules.getModWithTypeSetToName().getSettingMap().values());
            }
        }

        setting.removeIf(nextSetting -> nextSetting.getName().equals("Keybind"));

        return setting;
    }

    private int getCurrentCategoryState() {
        CategoryDrawPartBackground lastPart = categoryDrawParts.get(categoryDrawParts.size() - 1);
        int visibleCount = categoryDrawParts.size();
        if (lastPart.isExpanded()) {
            visibleCount--;
        }

        return visibleCount;
    }

    private void setAnimationDirection(boolean isForwards) {
        this.firstAnimationProgress.changeDirection(!isForwards ? Direction.BACKWARDS : Direction.FORWARDS);
    }

    private boolean isAnimationForwards() {
        return this.firstAnimationProgress.getDirection() == Direction.FORWARDS;
    }

    public enum KeyAction {
        RightArrowKey,
        LeftArrowKey,
        EnterKey,
        UpArrowKey,
        DownArrowKey;
    }
}
