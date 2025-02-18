package com.mentalfrostbyte.jello.gui.impl.jello.altmanager;

import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.Account;
import net.minecraft.client.Minecraft;

import java.util.List;

public record AccountListUpdater(AltManagerScreen screen, AltManagerScreen prevScreen, List<Account> accounts,
                                 boolean forceRefresh) implements Runnable {
    @Override
    public void run() {
        int var3 = 0;
        if (AltManagerScreen.method13382(this.screen) != null) {
            var3 = AltManagerScreen.method13382(this.screen).method13513();
            this.prevScreen.removeChildren(AltManagerScreen.method13382(this.screen));
        }

        CustomGuiScreen var4 = this.prevScreen.method13221("alts");
        if (var4 != null) {
            this.prevScreen.removeChildren(var4);
        }

        this.prevScreen
                .showAlert(
                        AltManagerScreen.method13383(
                                this.screen,
                                new ScrollableContentPanel(
                                        this.prevScreen,
                                        "alts",
                                        0,
                                        114,
                                        (int) ((float) Minecraft.getInstance().getMainWindow().getWidth() * AltManagerScreen.method13384(this.screen)) - 4,
                                        Minecraft.getInstance().getMainWindow().getHeight() - 119 - AltManagerScreen.getTitleOffset(this.screen)
                                )
                        )
                );

        for (Account var6 : this.accounts) {
            AltManagerScreen.method13386(this.screen, var6, this.forceRefresh);
        }

        AltManagerScreen.method13382(this.screen).method13512(var3);
        AltManagerScreen.method13382(this.screen).setListening(false);
        AltManagerScreen.method13382(this.screen).method13515(false);
    }
}
