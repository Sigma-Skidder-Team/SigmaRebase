package com.mentalfrostbyte.jello.gui.impl.jello.altmanager;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.alerts.AlertComponent;
import com.mentalfrostbyte.jello.gui.base.alerts.ComponentType;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Alert;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Dropdown;
import com.mentalfrostbyte.jello.gui.base.elements.impl.VerticalScrollBar;
import com.mentalfrostbyte.jello.gui.base.elements.impl.altmanager.AccountUI;
import com.mentalfrostbyte.jello.gui.base.elements.impl.altmanager.Head;
import com.mentalfrostbyte.jello.gui.base.elements.impl.altmanager.Info;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.TextButton;
import com.mentalfrostbyte.jello.gui.base.elements.impl.critical.Screen;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.TextField;
import com.mentalfrostbyte.jello.gui.impl.others.AccountSorter;
import com.mentalfrostbyte.jello.managers.AccountManager;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.Account;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.sorting.AccountCompareType;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuHolder;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.newdawn.slick.opengl.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import totalcross.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AltManagerScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(AltManagerScreen.class);
    private int field21005;
    private float field21006;
    private float field21007 = 0.75F;
    private boolean field21008 = true;
    public TextButton field21009;
    private ScrollableContentPanel field21010;
    private ScrollableContentPanel field21011;
    private Alert loginDialog;
    private Alert deleteAlert;
    private float field21014 = 0.65F;
    private float field21015 = 1.0F - this.field21014;
    private int titleOffset = 30;
    private Head field21017;
    private Info field21018;
    public AccountManager accountManager = Client.getInstance().accountManager;
    private Texture field21020;
    private float field21021;
    private TextButton field21022;
    private AccountCompareType field21023 = AccountCompareType.DateAdded;
    private String field21024 = "";
    private boolean field21025 = false;
    private TextField field21026;

    public AltManagerScreen() {
        super("Alt Manager");
        this.setListening(false);
        ArrayList<String> sortingOptions = new ArrayList<>();
        sortingOptions.add("Alphabetical");
        sortingOptions.add("Bans");
        sortingOptions.add("Date Added");
        sortingOptions.add("Last Used");
        sortingOptions.add("Use count");
        ArrayList<String> servers = new ArrayList();
        ServerList serverList = new ServerList(Minecraft.getInstance());
        serverList.loadServerList();
        int serverListSize = serverList.countServers();

        for (int i = 0; i < serverListSize; i++) {
            ServerData server = serverList.getServerData(i);
            if (!servers.contains(server.serverIP)) {
                servers.add(server.serverIP);
            }
        }

        this.getLoginDialog();
        this.deleteAlert();
        this.addToList(
                this.field21010 = new ScrollableContentPanel(
                        this,
                        "alts",
                        0,
                        114,
                        (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21014) - 4,
                        Minecraft.getInstance().getMainWindow().getHeight() - 119 - this.titleOffset
                )
        );
        this.addToList(
                this.field21011 = new ScrollableContentPanel(
                        this,
                        "altView",
                        (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21014),
                        114,
                        (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015) - this.titleOffset,
                        Minecraft.getInstance().getMainWindow().getHeight() - 119 - this.titleOffset
                )
        );
        this.field21010.setListening(false);
        this.field21011.setListening(false);
        this.field21010.method13515(false);
        this.field21011
                .addToList(
                        this.field21017 = new Head(
                                this.field21011,
                                "",
                                (int) (
                                        (float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015
                                                - (float) ((int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015))
                                )
                                        / 2
                                        - 10,
                                Minecraft.getInstance().getMainWindow().getHeight() / 12,
                                (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015),
                                350,
                                "steve"
                        )
                );
        this.field21011
                .addToList(
                        this.field21018 = new Info(
                                this.field21011,
                                "info",
                                (int) (
                                        (float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015
                                                - (float) ((int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015))
                                )
                                        / 2
                                        - 10,
                                this.method13374(),
                                (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015),
                                500
                        )
                );
        Dropdown var9 = new Dropdown(this, "drop", (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21014) - 220, 44, 200, 32, sortingOptions, 0);
        var9.method13643(servers, 1);
        var9.method13656(2);
        this.addToList(var9);
        var9.onPress(var2 -> {
            switch (var9.method13655()) {
                case 0:
                    this.field21023 = AccountCompareType.Alphabetical;
                    break;
                case 1:
                    this.field21023 = AccountCompareType.Bans;
                    this.field21024 = var9.method13645(1).method13636().get(var9.method13645(1).method13640());
                    break;
                case 2:
                    this.field21023 = AccountCompareType.DateAdded;
                    break;
                case 3:
                    this.field21023 = AccountCompareType.LastUsed;
                    break;
                case 4:
                    this.field21023 = AccountCompareType.UseCount;
            }

            this.method13372(false);
        });
        this.addToList(
                this.field21026 = new TextField(
                        this,
                        "textbox",
                        (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21014),
                        44,
                        150,
                        32,
                        TextField.field20741,
                        "",
                        "Search...",
                        ResourceRegistry.JelloLightFont18
                )
        );
        this.field21026.setFont(ResourceRegistry.JelloLightFont18);
        this.field21026.method13151(var1 -> this.method13372(false));
        this.addToList(this.field21022 = new TextButton(this, "btnt", this.getWidthA() - 90, 43, 70, 30, ColorHelper.field27961, "Add +", ResourceRegistry.JelloLightFont25));
        this.field21010.method13242();
        this.field21022.doThis((var1, var2) -> {
            if (this.method13369()) {
                this.loginDialog.method13603(!this.loginDialog.isHovered());
            }
        });
    }

    private void method13360(Account var1, boolean var2) {
        AccountUI var5;
        this.field21010
                .addToList(
                        var5 = new AccountUI(
                                this.field21010,
                                var1.getEmail(),
                                this.titleOffset,
                                (100 + this.titleOffset / 2) * this.method13370(),
                                this.field21010.getWidthA() - this.titleOffset * 2 + 4,
                                100,
                                var1
                        )
                );
        if (!var2) {
            var5.field20805 = new Animation(0, 0);
        }

        if (this.accountManager.isCurrentAccount(var1)) {
            var5.method13172(true);
        }

        var5.method13247((var2x, var3) -> {
            if (var3 != 0) {
                this.deleteAlert.onPress(element -> {
                    this.accountManager.removeAccountDirectly(var5.selectedAccount);
                    this.field21018.handleSelectedAccount(null);
                    this.field21017.handleSelectedAccount(null);
                    this.method13372(false);
                });
                this.deleteAlert.method13145(true);
                this.deleteAlert.method13603(true);
            } else {
                if (this.field21017.account == var5.selectedAccount && var5.method13168()) {
                    this.loginToAccount(var5);
                } else {
                    this.field21011.method13512(0);
                }

                this.field21017.handleSelectedAccount(var5.selectedAccount);
                this.field21018.handleSelectedAccount(var5.selectedAccount);

                for (CustomGuiScreen var7 : this.field21010.getChildren()) {
                    if (!(var7 instanceof VerticalScrollBar)) {
                        for (CustomGuiScreen var9 : var7.getChildren()) {
                            ((AccountUI) var9).method13166(false);
                        }
                    }
                }

                var5.method13166(true);
            }
        });
        if (Client.getInstance().accountManager.isCurrentAccount(var1)) {
            this.field21017.handleSelectedAccount(var5.selectedAccount);
            this.field21018.handleSelectedAccount(var5.selectedAccount);
            var5.method13167(true, true);
        }
    }

    private void loginToAccount(AccountUI account) {
        account.method13174(true);
        new Thread(() -> {
            if (!this.accountManager.login(account.selectedAccount)) {
                account.method13173(114);
                Client.getInstance().soundManager.play("error");
            } else {
                this.method13368();
                account.method13172(true);
                Client.getInstance().soundManager.play("connect");
                this.method13372(false);
            }

            account.method13174(false);
        }).start();
    }

    private void getLoginDialog() {
        AlertComponent header = new AlertComponent(ComponentType.HEADER, "Add Alt", 50);
        AlertComponent firstline1 = new AlertComponent(ComponentType.FIRST_LINE, "Login with your minecraft", 15);
        AlertComponent firstline2 = new AlertComponent(ComponentType.FIRST_LINE, "account here!", 25);
        AlertComponent emailInput = new AlertComponent(ComponentType.SECOND_LINE, "Email", 50);
        AlertComponent passwordInput = new AlertComponent(ComponentType.SECOND_LINE, "Password", 50);
        AlertComponent button = new AlertComponent(ComponentType.BUTTON, "Add alt", 50);
        AlertComponent button2 = new AlertComponent(ComponentType.BUTTON, "Cookie login", 50);
        AlertComponent button3 = new AlertComponent(ComponentType.BUTTON, "Web login", 50);
        this.addToList(this.loginDialog = new Alert(this, "Testt", true, "Add Alt", header, firstline1, firstline2, emailInput, passwordInput, button, button2, button3));

        this.loginDialog.onPress(element -> {
            if (!this.loginDialog.getInputMap().get("Email").contains(":")) {
                Account account = new Account(this.loginDialog.getInputMap().get("Email"), this.loginDialog.getInputMap().get("Password"));
                if (!this.accountManager.containsAccount(account)) {
                    this.accountManager.updateAccount(account);
                }

                this.method13372(false);
            } else {
                String[] emails = this.loginDialog.getInputMap().get("Email").replace("\r", "\n").replace("\n\n", "\n").split("\n");

                for (String email : emails) {
                    String[] splitted = email.split(":");
                    if (splitted.length == 2) {
                        Account account = new Account(splitted[0], splitted[1]);
                        if (!this.accountManager.containsAccount(account)) {
                            this.accountManager.updateAccount(account);
                        }
                    }
                }

                this.method13372(false);
            }
        });
    }

    private void deleteAlert() {
        AlertComponent title = new AlertComponent(ComponentType.HEADER, "Delete?", 50);
        AlertComponent firstLine = new AlertComponent(ComponentType.FIRST_LINE, "Are you sure you want", 15);
        AlertComponent secondLine = new AlertComponent(ComponentType.FIRST_LINE, "to delete this alt?", 40);
        AlertComponent button = new AlertComponent(ComponentType.BUTTON, "Delete", 50);
        this.addToList(this.deleteAlert = new Alert(this, "delete", true, "Delete", title, firstLine, secondLine, button));
    }

    @Override
    public void draw(float partialTicks) {
        this.drawBackground();
        RenderUtil.method11465(
                (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21014),
                114,
                (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * this.field21015) - this.titleOffset,
                Minecraft.getInstance().getMainWindow().getHeight() - 119 - this.titleOffset,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        this.method13367();
        this.drawTitle();
        super.draw(partialTicks);
    }

    /**
     * Hell yeah
     */
    private void emptyMethod() {
    }

    private void drawTitle() {
        int xPos = this.xA + this.titleOffset;
        int yPos = this.yA + this.titleOffset;
        int color = RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F);
        RenderUtil.drawString(ResourceRegistry.JelloLightFont40, (float) xPos, (float) yPos, "Jello", color);
        RenderUtil.drawString(ResourceRegistry.JelloLightFont25, (float) (xPos + 87), (float) (yPos + 15), "Alt Manager", color);
    }

    private void method13367() {
        float var3 = 1.0F;

        for (CustomGuiScreen var5 : this.field21010.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    if (var7 instanceof AccountUI) {
                        AccountUI var8 = (AccountUI) var7;
                        if (var7.getYA() <= Minecraft.getInstance().getMainWindow().getHeight() && this.field21010.method13513() == 0) {
                            if (var3 > 0.2F) {
                                var8.field20805.changeDirection(Animation.Direction.FORWARDS);
                            }

                            float var9 = MathUtil.lerp(var8.field20805.calcPercent(), 0.51, 0.82, 0.0, 0.99);
                            var8.method13284((int) (-((1.0F - var9) * (float) (var7.getWidthA() + 30))));
                            var3 = var8.field20805.calcPercent();
                        } else {
                            var8.method13284(0);
                            var8.field20805.changeDirection(Animation.Direction.FORWARDS);
                        }
                    }
                }
            }
        }
    }

    private void method13368() {
        boolean var3 = false;

        for (CustomGuiScreen var5 : this.field21010.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    AccountUI var8 = (AccountUI) var7;
                    var8.method13172(false);
                }
            }
        }
    }

    private boolean method13369() {
        boolean var3 = false;

        for (CustomGuiScreen var5 : this.field21010.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    if (var7.method13280() != 0 && var7.getXA() > this.widthA) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int method13370() {
        int var3 = 0;

        for (CustomGuiScreen var5 : this.field21010.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    var3++;
                }
            }
        }

        return var3;
    }

    private void drawBackground() {
        int var3 = this.getHeightO() * -1;
        float var4 = (float) this.getWidthO() / (float) this.getWidthA() * -114.0F;
        if (this.field21008) {
            this.field21006 = (float) ((int) var4);
            this.field21005 = var3;
            this.field21008 = false;
        }

        float var5 = var4 - this.field21006;
        float var6 = (float) (var3 - this.field21005);
        RenderUtil.drawImage((float) this.field21005, this.field21006, (float) (this.getWidthA() * 2), (float) (this.getHeightA() + 114), Resources.panoramaPNG);
        float var7 = 0.5F;
        if (var4 != this.field21006) {
            this.field21006 += var5 * var7;
        }

        if (var3 != this.field21005) {
            this.field21005 = (int) ((float) this.field21005 + var6 * var7);
        }

        RenderUtil.drawRoundedRect(0.0F, 0.0F, (float) this.getWidthA(), (float) this.getHeightA(), RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.95F));
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            Minecraft.getInstance().displayGuiScreen(new MainMenuHolder());
        }
    }

    @Override
    public JSONObject toConfigWithExtra(JSONObject config) {
        this.accountManager.saveAlts();
        return config;
    }

    @Override
    public void loadConfig(JSONObject config) {
        for (CustomGuiScreen var5 : this.field21010.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (CustomGuiScreen var7 : var5.getChildren()) {
                    this.field21010.method13234(var7);
                }
            }
        }

        this.method13372(true);
    }

    public void method13372(boolean var1) {
        List<Account> var5 = AccountSorter.sortByInputAltAccounts(this.accountManager.getAccounts(), this.field21023, this.field21024, this.field21026.getText());
        this.runThisOnDimensionUpdate(new Class1428(this, this, var5, var1));
    }

    private void method13373(Object var1) {
    }

    public int method13374() {
        return Minecraft.getInstance().getMainWindow().getHeight() / 12 + 280 + Minecraft.getInstance().getMainWindow().getHeight() / 12;
    }

    // $VF: synthetic method
    public static ScrollableContentPanel method13382(AltManagerScreen instance) {
        return instance.field21010;
    }

    // $VF: synthetic method
    public static ScrollableContentPanel method13383(AltManagerScreen instance, ScrollableContentPanel var1) {
        return instance.field21010 = var1;
    }

    // $VF: synthetic method
    public static float method13384(AltManagerScreen instance) {
        return instance.field21014;
    }

    // $VF: synthetic method
    public static int getTitleOffset(AltManagerScreen instance) {
        return instance.titleOffset;
    }

    // $VF: synthetic method
    public static void method13386(AltManagerScreen instance, Account var1, boolean var2) {
        instance.method13360(var1, var2);
    }
}