package com.mentalfrostbyte.jello.managers.util.account.microsoft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.util.system.network.ImageUtil;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Account {
    private String knownName = "Unknown name";
    private String uuid = "8667ba71-b85a-4004-af54-457a9734eed7";
    private String email;
    private String password;
    private ArrayList<Ban> bans = new ArrayList<>();
    private long lastUsed;
    private final long dateAdded;
    private int useCount;
    private BufferedImage skin;
    private Texture head;
    private Thread skinUpdateThread;

    private String token = "";

    public Account(String email, String password, ArrayList<Ban> bans, String knownName) {
        this.email = email;
        this.password = password;
        this.dateAdded = System.currentTimeMillis();
        this.lastUsed = 0L;
        this.useCount = 0;
        if (bans != null) {
            this.bans = bans;
        }

        if (knownName != null) {
            this.knownName = knownName;
        }
    }

    public Account(String username, String playerID, String token) {
        this(username, playerID, null, null);
        this.token = token;
    }

    public Account(String email, String password, ArrayList<Ban> bans) {
        this(email, password, bans, null);
    }

    public Account(String email, String password) {
        this(email, password, null, null);
    }

    public Account(JsonObject json) throws IOException {
        if (json.has("email")) {
            this.email = json.get("email").getAsString();
        }

        if (json.has("password")) {
            this.password = decodeBase64(json.get("password").getAsString());
        }

        if (json.has("token")) {
            this.token = decodeBase64(json.get("token").getAsString());
        }

        if (json.has("bans")) {
            for (var ban : json.getAsJsonArray("bans")) {
                this.bans.add(new Ban(ban.getAsJsonObject()));
            }
        }

        if (json.has("knownName")) {
            this.knownName = json.get("knownName").getAsString();
        }

        if (json.has("knownUUID")) {
            this.uuid = json.get("knownUUID").getAsString();
        }

        if (json.has("dateAdded")) {
            this.dateAdded = json.get("dateAdded").getAsLong();
        } else {
            this.dateAdded = System.currentTimeMillis();
        }

        if (json.has("lastUsed")) {
            this.lastUsed = json.get("lastUsed").getAsLong();
        }

        if (json.has("useCount")) {
            this.useCount = json.get("useCount").getAsInt();
        }

        if (json.has("skin")) {
            byte[] var7 = parseBase64Binary(json.get("skin").getAsString());

            try {
                this.skin = ImageIO.read(new ByteArrayInputStream(var7));
            } catch (IOException var6) {
                throw new IOException(var6);
            }
        }
    }

    public static String encodeBase64(String s) {
        byte[] bytes = Base64.encodeBase64(s.getBytes());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String decodeBase64(String s) {
        byte[] bytes = Base64.decodeBase64(s.getBytes());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void updateUsedCount() {
        this.useCount++;
    }

    public ArrayList<Ban> getBans() {
        return this.bans;
    }

    public String getEmail() {
        return this.email;
    }

    public String getKnownName() {
        return this.knownName;
    }

    public String getName() {
        return !this.knownName.equals("Unknown name") ? this.knownName : this.email;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getFormattedUUID() {
        return this.uuid.replaceAll("-", "");
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String var1) {
        this.password = var1;
    }

    public void setEmail(String var1) {
        this.email = var1;
    }

    public void registerBan(Ban ban) {
        this.unbanFromServerIP(ban.getServerIP());
        this.bans.add(ban);
    }

    public void unbanFromServerIP(String serverIP) {
        this.bans.removeIf(ban -> ban.getServerIP().equals(serverIP));
    }

    public void setName(String name) {
        this.knownName = name;
        this.skinUpdateThread = null;
    }

    public void setUuid(String var1) {
        this.uuid = var1;
    }

    public Texture setHeadTexture() {
        if (this.head == null) {
            this.head = Resources.head; 
            new Thread(() -> {
                String identifier = (this.password == null || this.password.isEmpty()) ? getName() : getFormattedUUID();
                BufferedImage bufferedImage = ImageUtil.loadBufferedImageFromURL("https://mc-heads.net/avatar/" + identifier + "/100");
                if (bufferedImage != null) {
                    Minecraft.getInstance().execute(() -> {
                        try {
                            this.head = BufferedImageUtil.getTexture(getFormattedUUID(), bufferedImage);
                        } catch (IOException e) {
                            System.out.println("Failed to convert buffered image to texture: " + e.getMessage());
                        }
                    });
                }
            }).start();
        }

        return this.head;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (this.head != null) {
                Client.getInstance().addTexture(this.head);
            }
        } finally {
            super.finalize();
        }
    }

    public BufferedImage getSkin() {
        return this.skin;
    }

    public void updateSkin() {
        if (this.skinUpdateThread == null) {
            this.skinUpdateThread = new Thread(() -> {
                try {
                    String identifier = (this.password == null || this.password.isEmpty()) ? getName() : getFormattedUUID();
                    this.skin = ImageIO.read(new URL(ImageUtil.getSkinUrlByID(identifier)));
                } catch (Exception ignored) {
                }
            });
            this.skinUpdateThread.start();
        }
    }

    public Session.Type getAccountType() {
        return this.email.contains("@") ? Session.Type.MOJANG : Session.Type.LEGACY;
    }

    public Session login() throws MicrosoftAuthenticationException {
        if (!this.isEmailAValidEmailFormat()) {
            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
            MicrosoftAuthResult result = authenticator.loginWithCredentials(email, password);
            System.out.printf("Logged in with '%s'%n", result.getProfile().getName());
            this.setName(result.getProfile().getName());
            this.setUuid(fixUUID(result.getProfile().getId()));
            this.updateSkin();
            this.lastUsed = System.currentTimeMillis();
            return new Session(
                    result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), getAccountType().name()
            );
        } else if (isPossibleRefreshToken(this.token)) {
            this.setName(this.getEmail());
            this.setUuid(fixUUID(this.getPassword()));
            this.updateSkin();
            this.lastUsed = System.currentTimeMillis();
            return new Session(this.getEmail(), this.getPassword(), this.token, "mojang");
        } else {
            this.setName(this.getEmail());
            this.lastUsed = System.currentTimeMillis();
            return new Session(this.getEmail(), "", "", "mojang");
        }
    }

    public boolean isPossibleRefreshToken(String token) {
        if (token.length() > 100) {
            return true;
        }

        return token.matches("^[A-Za-z0-9+/=]+$");
    }

    /**
     * I made this to fix autism caused by OpenAuth
     */
    public static String fixUUID(String uuidString) {
        return uuidString.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }

    public JsonObject toJSON() {
        JsonObject obj = new JsonObject();
        obj.add("bans", this.makeBanJSONArray());
        obj.addProperty("email", this.email);
        obj.addProperty("password", encodeBase64(this.password));
        obj.addProperty("token", encodeBase64(this.token));
        obj.addProperty("knownName", this.knownName);
        obj.addProperty("knownUUID", this.uuid);
        obj.addProperty("useCount", this.useCount);
        obj.addProperty("lastUsed", this.lastUsed);
        obj.addProperty("dateAdded", this.dateAdded);
        if (this.skin != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream);
            String skinBase64 = "";

            try {
                ImageIO.write(this.skin, "png", base64OutputStream);
                skinBase64 = outputStream.toString("UTF-8");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            obj.addProperty("skin", skinBase64);
        }

        return obj;
    }

    public static byte[] parseBase64Binary(String base64String) {
        return Base64.decodeBase64(base64String);
    }

    public JsonArray makeBanJSONArray() {
        JsonArray jsonArray = new JsonArray();

        for (Ban ban : this.bans) {
            jsonArray.add(ban.asJSONObject());
        }

        return jsonArray;
    }

    public int getUseCount() {
        return this.useCount;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public long getDateAdded() {
        return this.dateAdded;
    }

    public Ban getBanInfo(String serverIP) {
        for (Ban ban : this.getBans()) {
            if (ban.getServerIP().equals(serverIP)) {
                return ban;
            }
        }

        return null;
    }

    public boolean isEmailAValidEmailFormat() {
        if (this.getPassword().isEmpty())
            return true;

        Pattern var3 = Pattern.compile("[a-zA-Z0-9_]{2,16}");
        return var3.matcher(this.getEmail()).matches();
    }
}
