package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.alerts.AlertComponent;
import com.mentalfrostbyte.jello.gui.base.alerts.ComponentType;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Alert;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.TextButton;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import org.newdawn.slick.opengl.Texture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuHolder;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class RegisterScreen extends Screen {
    private int field21082 = 0;
    private int field21083 = 0;
    private boolean field21084 = true;
    private float field21085 = 0.0F;
    private float field21086 = 0.0F;
    private Texture field21087;
    private LoginScreen field21088;
    private AccountSignUpScreen field21089;
    private Alert field21090;
    private TextButton field21091;
    private boolean loggedIn = false;
    private Animation field21093 = new Animation(250, 250, Animation.Direction.BACKWARDS);

    public RegisterScreen() {
        super("Credits");
        this.setListening(false);
        this.field21087 = Resources.createScaledAndProcessedTexture2("com/mentalfrostbyte/gui/resources/background/panorama5.png", 0.075F, 8);
        this.addToList(
                this.field21088 = new LoginScreen(
                        this,
                        "login",
                        (this.widthA - LoginScreen.heighty) / 2,
                        (this.heightA - LoginScreen.widthy) / 2,
                        LoginScreen.heighty,
                        LoginScreen.widthy
                )
        );
        this.addToList(
                this.field21089 = new AccountSignUpScreen(
                        this,
                        "register",
                        (this.widthA - AccountSignUpScreen.height) / 2,
                        (this.heightA - AccountSignUpScreen.widthy) / 2,
                        AccountSignUpScreen.height,
                        AccountSignUpScreen.widthy
                )
        );
        this.method13423();
        this.addToList(
                this.field21091 = new TextButton(
                        this,
                        "continue",
                        this.widthA / 2 - 120,
                        this.heightA / 2 + 120,
                        240,
                        60,
                        new ColorHelper(RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F)),
                        "Continue",
                        ResourceRegistry.JelloLightFont25
                )
        );
        this.field21091.setVisible(false);
        this.field21088.onPress(var1 -> {
            this.loggedIn = true;
            this.field21091.setVisible(true);
        });
        this.field21091.doThis((var0, var1) -> Minecraft.getInstance().displayGuiScreen(new MainMenuHolder()));
    }

    public void method13422() {
        this.field21088.setVisible(false);
        this.field21089.setVisible(true);
    }

    public void method13423() {
        this.field21088.setVisible(true);
        this.field21089.setVisible(false);
    }

    public void method13424(String var1, String var2) {
        if (this.field21090 == null) {
            this.runThisOnDimensionUpdate(() -> {
                ArrayList<AlertComponent> var5 = new ArrayList();
                var5.add(new AlertComponent(ComponentType.HEADER, var1, 45));
                String[] var6 = RenderUtil2.method17745(var2, 240, ResourceRegistry.JelloLightFont20);

                for (int var7 = 0; var7 < var6.length; var7++) {
                    var5.add(new AlertComponent(ComponentType.FIRST_LINE, var6[var7], var7 != var6.length - 1 ? 14 : 35));
                }

                var5.add(new AlertComponent(ComponentType.BUTTON, "Ok", 55));
                this.showAlert(this.field21090 = new Alert(this, "modal", true, "", var5.toArray(new AlertComponent[0])));
                this.field21090.method13604(var1xx -> new Thread(() -> this.runThisOnDimensionUpdate(() -> {
                    this.removeChildren(this.field21090);
                    this.field21090 = null;
                })).start());
                this.field21090.method13603(true);
            });
        }
    }

    @Override
    public void draw(float partialTicks) {
        Resources.cancelIconPNG.bind();
        this.field21085 = Math.max(0.0F, Math.min(this.field21085 + 0.075F, 1.0F));
        if (this.loggedIn) {
            this.field21093.changeDirection(Animation.Direction.FORWARDS);
        }

        this.method13425();
        float var4 = (float) Math.sin((double) QuadraticEasing.easeOutQuad(this.field21085, 0.0F, 1.0F, 1.0F) * Math.PI / 2.0);
        if (this.loggedIn) {
            var4 = 1.0F
                    - (float) Math.sin((Math.PI / 2) + (double) QuadraticEasing.easeInOutQuad(1.0F - this.field21093.calcPercent(), 0.0F, 1.0F, 1.0F) * (Math.PI / 2)) * 0.2F;
        }

        this.field21088.method13277(var4);
        this.field21088.method13278(var4);
        this.field21089.method13277(var4);
        this.field21089.method13278(var4);
        Rectangle var5 = RenderUtil.method11413(RenderUtil.method11414(this.field21088), this.field21088.method13273(), this.field21088.method13275());
        if (this.field21089.isVisible()) {
            var5 = RenderUtil.method11413(RenderUtil.method11414(this.field21089), this.field21089.method13273(), this.field21089.method13275());
        }

        if ((double) var4 > 0.1) {
            RenderUtil.method11465(
                    (int) ((double) this.widthA - var5.getWidth()) / 2,
                    (int) ((double) this.heightA - var5.getHeight()) / 2,
                    (int) var5.getWidth(),
                    (int) var5.getHeight(),
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - this.field21093.calcPercent())
            );
        }

        if (this.loggedIn && this.field21093.calcPercent() == 1.0F) {
            this.field21088.setVisible(false);
        }

        if (this.loggedIn && Client.getInstance().networkManager.account != null) {
            String username = Client.getInstance().networkManager.account.username;
            String welcomeBackSign = "Welcome back";
            int var8 = 100;
            int var9 = 10;
            int var10 = var8 + Math.max(ResourceRegistry.JelloMediumFont40.getWidth(welcomeBackSign), ResourceRegistry.JelloLightFont36.getWidth(username)) + var9 * 10;
            int var11 = (this.widthA - var10) / 2;
            int var12 = (this.heightA - var8 * 2) / 2;
            RenderUtil.drawRoundedRect(
                    0.0F,
                    0.0F,
                    (float) this.widthA,
                    (float) this.heightA,
                    RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * this.field21093.calcPercent())
            );
            RenderUtil.drawImage(
                    (float) (var11 + 20), (float) (var12 + 40), (float) (var8 + 30), (float) (var8 + 30), Resources.sigmaPNG, this.field21093.calcPercent()
            );
            int var13 = 165;
            int var14 = 54;
            RenderUtil.drawString(ResourceRegistry.JelloMediumFont40, (float) (var11 + var13), (float) (var12 + var14), welcomeBackSign, ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtil.drawString(ResourceRegistry.JelloLightFont36, (float) (var11 + var13), (float) (var12 + var14 + 45), username, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        GL11.glPushMatrix();
        this.field21091.draw(this.field21093.calcPercent());
        GL11.glPopMatrix();
        super.draw(1.0F - this.field21093.calcPercent());
    }

    private void method13425() {
        this.field21086 = Math.max(0.0F, Math.min(this.field21086 + 0.01F, 1.0F));
        int var3 = this.getHeightO() * -1;
        float var4 = (float) this.getWidthO() / (float) this.getWidthA() * -114.0F;
        if (this.field21084) {
            this.field21082 = (int) var4;
            this.field21083 = var3;
            this.field21084 = false;
        }

        float var5 = var4 - (float) this.field21082;
        float var6 = (float) (var3 - this.field21083);
        float var7 = (float) Math.sin((double) QuadraticEasing.easeOutQuad(this.field21086, 0.0F, 1.0F, 1.0F) * Math.PI / 2.0);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.widthA / 2.0F, (float) this.widthA / 2.0F, 0.0F);
        GL11.glScalef(1.0F + var7 * 0.2F, 1.0F + var7 * 0.2F, 0.0F);
        GL11.glTranslatef((float) (-this.widthA) / 2.0F, (float) (-this.widthA) / 2.0F, 0.0F);
        RenderUtil.drawTexture(
                (float) this.field21083,
                (float) this.field21082,
                (float) (this.getWidthA() * 2),
                (float) (this.getHeightA() + 114),
                this.field21087,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.field21085)
        );
        GL11.glPopMatrix();
        float var8 = 0.5F;
        if (var4 != (float) this.field21082) {
            this.field21082 = (int) ((float) this.field21082 + var5 * var8);
        }

        if (var3 != this.field21083) {
            this.field21083 = (int) ((float) this.field21083 + var6 * var8);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            Minecraft.getInstance().displayGuiScreen(new MainMenuHolder());
        }
    }
}
