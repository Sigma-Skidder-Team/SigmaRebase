package com.mentalfrostbyte.jello.module.impl.gui.jello;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.event.impl.game.action.EventKeyPress;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.module.Module;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.render.ColorUtils;
import com.mentalfrostbyte.jello.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import team.sdhq.eventBus.annotations.EventTarget;

import java.util.ArrayList;
import java.util.Iterator;

public class KeyStrokes extends Module {
    public int xBase = 10;
    public int yBase = 260;
    public ArrayList<KeyAnimationData> animations = new ArrayList<>();

    public KeyStrokes() {
        super(ModuleCategory.GUI, "KeyStrokes", "Shows what keybind you are pressing");
        this.setAvailableOnClassic(false);
    }

    public Keystroke getKeyStrokeForKey(int key) {
        Keystroke[] keystrokes = Keystroke.values();
        for (Keystroke keystroke : keystrokes) {
            assert keystroke.getKeyBinding() != null;
            if (key == keystroke.getKeyBinding().keyCode.getKeyCode()) {
                return keystroke;
            }
        }
        return null;
    }

    @EventTarget
    public void onRender(EventRender2DOffset var1) {
        if (this.isEnabled() && mc.player != null) {
            if (!Minecraft.getInstance().gameSettings.showDebugInfo) {
                if (!Minecraft.getInstance().gameSettings.hideGUI) {
                    this.yBase = var1.getyOffset();
                    if (Client.getInstance().guiManager.getGuiBlur()) {
                        for (Keystroke var7 : Keystroke.values()) {
                            KeyPosition var8 = var7.getTopLeftPosition();
                            KeyPosition var9 = var7.getBottomRightPosition();
                            RenderUtil.drawPortalBackground(
                                    this.xBase + var8.x,
                                    this.yBase + var8.y,
                                    this.xBase + var8.x + var9.x,
                                    this.yBase + var8.y + var9.y
                            );
                            // TODO: blur
//                            BlurEngine.drawBlur(this.field23585 + var8.field42635, this.field23586 + var8.field42636, var9.field42635, var9.field42636);
//                            BlurEngine.endBlur();
                            RenderUtil.endScissor();
                        }
                    }

                    for (Keystroke keystroke : Keystroke.values()) {
                        KeyPosition var21 = keystroke.getTopLeftPosition();
                        KeyPosition var23 = keystroke.getBottomRightPosition();
                        float var10 = 1.0F;
                        float var11 = 1.0F;
                        if (Client.getInstance().guiManager.getGuiBlur()) {
                            var11 = 0.5F;
                            var10 = 0.5F;
                        }

                        String var12 = RenderUtil.getKeyName(keystroke.bind.keyCode.getKeyCode());
                        if (keystroke.bind != mc.gameSettings.keyBindAttack) {
                            if (keystroke.bind == mc.gameSettings.keyBindUseItem) {
                                var12 = "R";
                            }
                        } else {
                            var12 = "L";
                        }

                        RenderUtil.drawRoundedRect( // TODO: check this, again
                                (float) (this.xBase + var21.x),
                                (float) (this.yBase + var21.y),
                                (float) (this.xBase + var21.x + var23.x),
                                (float) (this.yBase + var21.y + var23.y),
                                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F * var10)
                        );
                        RenderUtil.drawRoundedRect(
                                (float) (this.xBase + var21.x),
                                (float) (this.yBase + var21.y),
                                (float) var23.x,
                                (float) var23.y,
                                10.0F,
                                0.75F * var11
                        );
                        RenderUtil.drawString(
                                ResourceRegistry.JelloLightFont18,
                                (float) (this.xBase + var21.x + (var23.x - ResourceRegistry.JelloLightFont18.getWidth(var12)) / 2),
                                (float) (this.yBase + var21.y + 12),
                                var12,
                                ClientColors.LIGHT_GREYISH_BLUE.getColor()
                        );
                    }

                    Iterator iter = this.animations.iterator();

                    while (iter.hasNext()) {
                        KeyAnimationData animationData = (KeyAnimationData) iter.next();
                        Keystroke keyStroke = animationData.keyStroke;
                        KeyPosition topLeftPosition = keyStroke.getTopLeftPosition();
                        KeyPosition bottomRightPosition = keyStroke.getBottomRightPosition();
                        RenderUtil.drawPortalBackground(
                                this.xBase + topLeftPosition.x,
                                this.yBase + topLeftPosition.y,
                                this.xBase + topLeftPosition.x + bottomRightPosition.x,
                                this.yBase + topLeftPosition.y + bottomRightPosition.y
                        );
                        float maxAnimPercent = 0.7F;
                        int duplicates = 0;

                        for (KeyAnimationData animationData1 : this.animations) {
                            if (animationData1.keyStroke.equals(keyStroke)) {
                                duplicates++;
                            }
                        }

                        if (keyStroke.getKeyBinding().isKeyDown() && animationData.animation.calcPercent() >= maxAnimPercent && duplicates < 2) {
                            animationData.animation.updateStartTime(maxAnimPercent);
                        }

                        float var27 = animationData.animation.calcPercent();
                        float alpha = (1.0F - var27 * (0.5F + var27 * 0.5F)) * 0.8F;
                        int color = ColorUtils.applyAlpha(-5658199, alpha);
                        if (Client.getInstance().guiManager.getGuiBlur()) { // TODO: check this
                            color = ColorUtils.applyAlpha(-1, alpha);
                        }

                        RenderUtil.drawFilledArc(
                                (float) (this.xBase + topLeftPosition.x + bottomRightPosition.getX() / 2),
                                (float) (this.yBase + topLeftPosition.y + bottomRightPosition.y / 2),
                                (float) (bottomRightPosition.getX() - 4) * var27 + 4.0F,
                                color
                        );
                        RenderUtil.endScissor();
                        if (animationData.animation.calcPercent() == 1.0F) {
                            iter.remove();
                        }
                    }

                    var1.addOffset(160);
                }
            }
        }
    }

    @EventTarget
    public void onKeyPress(EventKeyPress event) {
        if (this.isEnabled() && mc.player != null) {
            if (this.getKeyStrokeForKey(event.getKey()) != null && !event.isPressed()) {
                this.animations.add(new KeyAnimationData(this.getKeyStrokeForKey(event.getKey())));
            }
        }
    }

//    @EventTarget
//    public void onClick(ClickEvent var1) {
//        if (!this.isEnabled() || mc.player == null) {
//        }
//    }
    /**
     * {@link Keystroke} represents a key on the keyboard that can be pressed
     * or released. It provides information about the key such as its position
     * on the keyboard, its binding, and its state.
     */
    public enum Keystroke {
        Left(0.0F, 1.0F, mc.gameSettings.keyBindLeft),
        Right(2.0F, 1.0F, mc.gameSettings.keyBindRight),
        Forward(1.0F, 0.0F, mc.gameSettings.keyBindForward),
        Back(1.0F, 1.0F, mc.gameSettings.keyBindBack),
        Attack(0.0F, 2.0F, 74, mc.gameSettings.keyBindAttack),
        UseItem(1.02F, 2.0F, 73, mc.gameSettings.keyBindUseItem);

        public final float positionX;
        public final float positionY;
        public int width = 48;
        public int height = 48;
        public int padding = 3;
        public final KeyBinding bind;

        private Keystroke(float positionX, float positionY, KeyBinding bind) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.bind = bind;
        }

        private Keystroke(float positionX, float positionY, int width, KeyBinding bind) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.bind = bind;
            this.width = width;
        }

        /**
         * Gets the top left position of the key on the keyboard.
         * @return the top left position of the key on the keyboard.
         */
        public KeyPosition getTopLeftPosition() {
            return new KeyPosition(
                    this, (int)(this.positionX * (float)(this.width + this.padding)), (int)(this.positionY * (float)(this.height + this.padding))
            );
        }

        /**
         * Gets the bottom right position of the key on the keyboard.
         * @return the bottom right position of the key on the keyboard.
         */
        public KeyPosition getBottomRightPosition() {
            return new KeyPosition(this, this.width, this.height);
        }

        /**
         * Gets the key binding for the key.
         * @return the key binding for the key.
         */
        public KeyBinding getKeyBinding() {
            switch (this) {
                case Left: return mc.gameSettings.keyBindLeft;
                case Right: return mc.gameSettings.keyBindRight;
                case Forward: return mc.gameSettings.keyBindForward;
                case Back: return mc.gameSettings.keyBindBack;
                case Attack: return mc.gameSettings.keyBindAttack;
                case UseItem: return mc.gameSettings.keyBindUseItem;
                default: return null;
            }
        }
    }

    public static class KeyAnimationData {
        public Keystroke keyStroke;
        public Animation animation;

        public KeyAnimationData(Keystroke keyStroke) {
            this.animation = new Animation(300, 0);
            this.keyStroke = keyStroke;
        }
    }

    /**
     * represents the position of a key on the keyboard.
     */
    public static class KeyPosition {
        public int x;
        public int y;
        public final Keystroke keystroke;

        public KeyPosition(Keystroke keystroke, int x, int y) {
            this.keystroke = keystroke;
            this.x = x;
            this.y = y;
        }

        /**
         * Gets the x position of the key.
         * @return the x position of the key.
         */
        public int getX() {
            return this.x;
        }
    }
}