package com.mentalfrostbyte.jello.managers.impl.account.microsoft;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.ServerList;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;
import net.minecraft.client.MinecraftClient;

import java.util.Calendar;
import java.util.Date;

public class Ban {
    private final String serverIP;
    private final Date date;

    public Ban(String var1, Date var2) {
        this.serverIP = var1;
        this.date = var2;
    }

    public Ban(JSONObject var1) throws JSONException {
        Calendar var4 = Calendar.getInstance();
        long var5;
        if (!(var1.get("until") instanceof Integer)) {
            var5 = (Long) var1.get("until");
        } else {
            var5 = ((Integer) var1.get("until")).longValue();
        }

        if (var5 == 1L) {
            var5 = 9223372036854775806L;
        }

        var4.setTimeInMillis(var5);
        this.serverIP = var1.getString("server");
        this.date = var4.getTime();
    }

    public JSONObject asJSONObject() {
        JSONObject var3 = new JSONObject();
        var3.put("server", this.serverIP);
        var3.put("until", this.date.getTime());
        return var3;
    }

    public String getServerIP() {
        return this.serverIP;
    }

    public Date method31735() {
        return this.date;
    }

    public ServerInfo method31736() {
        ServerList var3 = new ServerList(MinecraftClient.getInstance());
        var3.loadFile();
        int var4 = var3.size();

        for (int var5 = 0; var5 < var4; var5++) {
            ServerInfo var6 = var3.get(var5);
            if (var6.address.equals(this.serverIP)) {
                return var6;
            }
        }

        return null;
    }
}
