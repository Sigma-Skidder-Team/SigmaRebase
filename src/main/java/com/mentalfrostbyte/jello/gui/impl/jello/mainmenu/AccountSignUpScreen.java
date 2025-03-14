package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.LoadingIndicator;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.TextField;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Text;
import com.mentalfrostbyte.jello.gui.base.elements.Element;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.TextButton;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;

public class AccountSignUpScreen extends Element {
    private Text stringPanel;
    private TextField usernameInputBox;
    private TextField emailInputBox;
    private TextField passwordInputBox;
    private TextField captchaBox;
    private TextButton registerButton;
    private TextButton loginButton;
    private LoadingIndicator loadingBox;
    public static int widthy = 390;
    public static int height = 590;

    public AccountSignUpScreen(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.addToList(
                this.stringPanel = new Text(
                        this,
                        "Register",
                        228,
                        43,
                        ResourceRegistry.JelloMediumFont40.getWidth("New Account"),
                        50,
                        new ColorHelper(ClientColors.DEEP_TEAL.getColor(), ClientColors.DEEP_TEAL.getColor(), ClientColors.DEEP_TEAL.getColor(), -7631989),
                        "New Account",
                        ResourceRegistry.JelloMediumFont40
                )
        );
        this.addToList(
                this.registerButton = new TextButton(
                        this, "RegisterButton", 468, 291, ResourceRegistry.JelloLightFont25.getWidth("Register"), 70, ColorHelper.field27961, "Register", ResourceRegistry.JelloLightFont25
                )
        );
        this.addToList(
                this.loginButton = new TextButton(
                        this, "LoginButton", 98, 333, ResourceRegistry.JelloLightFont14.getWidth("Login"), 14, ColorHelper.field27961, "Login", ResourceRegistry.JelloLightFont14
                )
        );
        this.addToList(this.loadingBox = new LoadingIndicator(this, "loading", 530, 314, 30, 30));
        this.loadingBox.setHovered(false);
        this.loadingBox.method13294(true);
        int var9 = 50;
        int var10 = 320;
        int var11 = 106;
        ColorHelper var12 = new ColorHelper(-892679478, -892679478, -892679478, ClientColors.MID_GREY.getColor(), FontSizeAdjust.field14488, FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2);
        this.addToList(this.usernameInputBox = new TextField(this, "Username", 228, var11, var10, var9, var12, "", "Username"));
        this.usernameInputBox.setFont(ResourceRegistry.JelloLightFont20);
        this.addToList(this.emailInputBox = new TextField(this, "Email", 228, var11 + 53, var10, var9, var12, "", "Email"));
        this.emailInputBox.setFont(ResourceRegistry.JelloLightFont20);
        this.addToList(this.passwordInputBox = new TextField(this, "Password", 228, var11 + 106, var10, var9, var12, "", "Password"));
        this.passwordInputBox.setFont(ResourceRegistry.JelloLightFont20);
        this.passwordInputBox.setCensorText(true);
        this.addToList(this.captchaBox = new TextField(this, "CaptchaBox", 228, var11 + 53 + 135, 80, var9, var12, "", "Captcha"));
        this.captchaBox.setFont(ResourceRegistry.JelloLightFont20);
        this.captchaBox.setEnabled(false);
        this.registerButton.doThis((var1x, var2x) -> this.method13126());
        this.loginButton.doThis((var1x, var2x) -> {
            RegisterScreen var5x = (RegisterScreen) this.getParent();
            var5x.method13423();
        });
    }

    @Override
    public void draw(float partialTicks) {
        super.method13224();
        super.method13225();
        int var4 = 28;
        RenderUtil.drawImage((float) (this.xA + var4), (float) (this.yA + var4 + 10), 160.0F, 160.0F, Resources.sigmaPNG, partialTicks);

        super.draw(partialTicks);
    }

    public void method13126() {
        new Thread(
                () -> {
                    this.loadingBox.setHovered(true);
                    this.registerButton.setEnabled(false);
                    Client.getInstance().networkManager.resetLicense();
                    Client.getInstance().networkManager.signup(this.usernameInputBox.getText(), this.passwordInputBox.getText(), this.emailInputBox.getText());
                    String s = Client.getInstance().networkManager.newAccount(this.usernameInputBox.getText(), this.passwordInputBox.getText());
                    RegisterScreen var5 = (RegisterScreen) this.getParent();
                    if (s != null) {
                        var5.method13424("Error", s);
                        this.captchaBox.setText("");
                    } else {
                        var5.method13424("Success", "You can now login.");
                        var5.method13423();
                    }

                    this.loadingBox.setHovered(false);
                    this.registerButton.setEnabled(true);
                }
        ).start();
    }
}
