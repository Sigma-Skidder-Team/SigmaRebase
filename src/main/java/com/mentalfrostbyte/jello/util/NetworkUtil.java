package com.mentalfrostbyte.jello.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.play.NetworkPlayerInfo;

// i couldn't think of better name..
public class NetworkUtil {

    // from MultiUtilities.method17705()
    public static int getPlayerResponseTime() {
        for (NetworkPlayerInfo networkPlayer : MinecraftClient.getInstance().getConnection().getPlayerInfoMap()) {
            if (networkPlayer.getGameProfile().getId().equals(MinecraftClient.getInstance().player.getUniqueID())
                    && !MinecraftClient.getInstance().isIntegratedServerRunning()) {
                return networkPlayer.getResponseTime();
            }
        }

        return 0;
    }
}
