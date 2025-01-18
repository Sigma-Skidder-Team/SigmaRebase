package com.mentalfrostbyte.jello.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;

// i couldn't think of better name..
public class NetworkUtil {

    // from MultiUtilities.method17705()
    public static int getPlayerResponseTime() {
        for (NetworkPlayerInfo networkPlayer : Minecraft.getInstance().getConnection().getPlayerInfoMap()) {
            if (networkPlayer.getGameProfile().getId().equals(Minecraft.getInstance().player.getUniqueID()) && !Minecraft.getInstance().isIntegratedServerRunning()) {
                return networkPlayer.getResponseTime();
            }
        }

        return 0;
    }
}
