package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.Account;
import com.mentalfrostbyte.jello.managers.util.account.microsoft.BanListener;
import com.mentalfrostbyte.jello.util.system.FileUtil;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import team.sdhq.eventBus.EventBus;
import totalcross.json.JSONArray;
import totalcross.json.JSONException2;
import totalcross.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AccountManager {
    public ArrayList<Account> accounts = new ArrayList<Account>();
    public File altsFile = new File(Client.getInstance().file + "/alts.json");
    private String email;
    private final BanListener banListener = new BanListener();

    public AccountManager() {
        this.loadAltsFromFile();
    }

    public void registerEvents() {
        EventBus.register(this);
        EventBus.register(this.banListener);
    }

    public void updateAccount(Account account) {
        for (int i = 0; i < this.accounts.size(); i++) {
            if (this.accounts.get(i).getEmail().equals(account.getEmail())) {
                this.accounts.set(i, account);
                return;
            }
        }

        this.accounts.add(account);
    }

    public void removeAccount(Account account) {
        for (int i = 0; i < this.accounts.size(); i++) {
            if (this.accounts.get(i).getEmail().equals(account.getEmail())) {
                this.accounts.remove(i);
                return;
            }
        }
    }

    public boolean containsAccount(Account account) {
        for (Account acc : this.accounts) {
            if (acc.getEmail().equals(account.getEmail())) {
                return true;
            }
        }

        return false;
    }

    public Account containsAccount() {
        for (Account var4 : this.accounts) {
            if (var4.getEmail().equals(this.email)) {
                return var4;
            }
        }

        return null;
    }

    /**
     * Logs into the account
     *
     * @param account specified account
     * @return if the login was successful
     */
    public boolean login(Account account) {
        try {
            Session session = Minecraft.getInstance().getSession();
            Session newSession = account.login();
            session.username = newSession.getUsername();
            session.playerID = newSession.getPlayerID();
            session.token = newSession.getToken();
            this.email = account.getEmail();
            return true;
        } catch (MicrosoftAuthenticationException e) {
            return false;
        }
    }

    // What
    public boolean updateSelectedEmail(Account account) {
        try {
            account.login();
            this.email = account.getEmail();
            return true;
        } catch (MicrosoftAuthenticationException var5) {
            return false;
        }
    }

    public void removeAccountDirectly(Account var1) {
        this.accounts.remove(var1);
    }

    public ArrayList<Account> getAccounts() {
        return this.accounts;
    }

    public void saveAlts() {
        JSONArray jsonArray = new JSONArray();

        for (Account account : this.accounts) {
            jsonArray.put(account.toJSON());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("alts", jsonArray);

        try {
            FileUtil.save(jsonObject, new File(Client.getInstance().file + "/alts.json"));
        } catch (IOException | JSONException2 var6) {
            Client.getInstance().getLogger().error(var6.getMessage());
        }
    }

    private void loadAltsFromFile() {
        try {
            JSONObject jsonObject = FileUtil.readFile(this.altsFile);
            if (!jsonObject.has("alts")) {
                jsonObject.put("alts", new JSONArray());
            }

            for (Object obj : jsonObject.getJSONArray("alts")) {
                this.accounts.add(new Account((JSONObject) obj));
            }
        } catch (IOException e) {
            Client.getInstance().getLogger().error(e.getMessage());
        }
    }

    public String getEmail() {
        return this.email;
    }

    public boolean isCurrentAccount(Account account) {
        return this.getEmail() != null
                ? account.getEmail().equals(this.getEmail())
                : account.getKnownName().equals(Minecraft.getInstance().getSession().getUsername());
    }
}
